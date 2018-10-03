/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import {StateService} from '@uirouter/core';
import {IComponentOptions} from 'angular';
import * as $ from 'jquery';
import {DvDialog} from '../../../app/core/directive/dv-dialog/dv-dialog';
import ErrorService from '../../../app/core/errors/service/ErrorService';
import {InstitutionRS} from '../../../app/core/service/institutionRS.rest';
import {InstitutionStammdatenRS} from '../../../app/core/service/institutionStammdatenRS.rest';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import {RemoveDialogController} from '../../../gesuch/dialog/RemoveDialogController';
import {getTSBetreuungsangebotTypValues, TSBetreuungsangebotTyp} from '../../../models/enums/TSBetreuungsangebotTyp';
import TSInstitution from '../../../models/TSInstitution';
import TSInstitutionStammdaten from '../../../models/TSInstitutionStammdaten';
import {TSMandant} from '../../../models/TSMandant';
import {TSTraegerschaft} from '../../../models/TSTraegerschaft';
import {TSDateRange} from '../../../models/types/TSDateRange';
import EbeguUtil from '../../../utils/EbeguUtil';
import AbstractAdminViewController from '../../abstractAdminView';
import {IInstitutionStateParams} from '../../admin.route';
import IFormController = angular.IFormController;

const removeDialogTemplate = require('../../../gesuch/dialog/removeDialogTemplate.html');

export class InstitutionViewComponentConfig implements IComponentOptions {
    public transclude = false;
    public bindings = {
        traegerschaften: '<',
        mandant: '<'
    };
    public template = require('./institutionView.html');
    public controller = InstitutionViewController;
    public controllerAs = 'vm';
}

export class InstitutionViewController extends AbstractAdminViewController {

    public static $inject = ['InstitutionRS', 'InstitutionStammdatenRS', 'ErrorService', 'DvDialog', 'EbeguUtil',
        'AuthServiceRS', '$stateParams', '$state'];

    public form: IFormController;

    public traegerschaften: TSTraegerschaft[];
    public mandant: TSMandant;
    public instStammdatenList: TSInstitutionStammdaten[] = [];
    public selectedInstitution: TSInstitution = undefined;
    public selectedInstitutionStammdaten: TSInstitutionStammdaten = undefined;
    public betreuungsangebotValues: Array<any>;
    public errormessage: string = undefined;

    public constructor(private readonly institutionRS: InstitutionRS,
                       private readonly institutionStammdatenRS: InstitutionStammdatenRS,
                       private readonly errorService: ErrorService,
                       private readonly dvDialog: DvDialog,
                       private readonly ebeguUtil: EbeguUtil,
                       authServiceRS: AuthServiceRS,
                       private readonly $stateParams: IInstitutionStateParams,
                       private readonly $state: StateService) {
        super(authServiceRS);
    }

    public $onInit(): void {
        this.setBetreuungsangebotTypValues();

        if (this.$stateParams.institutionId) {
            this.institutionRS.findInstitution(this.$stateParams.institutionId).then((found: TSInstitution) => {
                this.setSelectedInstitution(found);
            });
            return;
        }

        this.createInstitution();
    }

    public getTreagerschaftList(): Array<TSTraegerschaft> {
        return this.traegerschaften;
    }

    public createInstitution(): void {
        this.selectedInstitution = new TSInstitution();
        this.selectedInstitution.mandant = this.mandant;
        this.selectedInstitutionStammdaten = undefined;
    }

    public setSelectedInstitution(institution: TSInstitution): void {
        this.selectedInstitution = institution;
        this.selectedInstitutionStammdaten = undefined;
        if (!this.isCreateInstitutionsMode()) {
            this.institutionStammdatenRS.getAllInstitutionStammdatenByInstitution(this.selectedInstitution.id).then(
                loadedInstStammdaten => {
                    this.instStammdatenList = loadedInstStammdaten;
                });
        }
        this.errormessage = undefined;
    }

    public isCreateInstitutionsMode(): boolean {
        return this.selectedInstitution && this.selectedInstitution.isNew();
    }

    public getSelectedInstitution(): TSInstitution {
        return this.selectedInstitution;
    }

    public saveInstitution(form: IFormController): void {
        if (!form.$valid) {
            return;
        }

        this.errorService.clearAll();
        if (this.isCreateInstitutionsMode()) {
            this.institutionRS.createInstitution(this.selectedInstitution)
                .then(institution => this.setSelectedInstitution(institution));
        } else {
            this.institutionRS.updateInstitution(this.selectedInstitution);
        }
    }

    public goBack(): void {
        this.$state.go('admin.institutionen');
    }

    public getInstitutionStammdatenList(): TSInstitutionStammdaten[] {
        return this.instStammdatenList;
    }

    public removeInstitutionStammdaten(institutionStammdaten: TSInstitutionStammdaten): void {
        this.dvDialog.showRemoveDialog(removeDialogTemplate, this.form, RemoveDialogController, {
            deleteText: '',
            title: 'LOESCHEN_DIALOG_TITLE',
            parentController: undefined,
            elementID: undefined,
        }).then(() => {   // User confirmed removal
            this.institutionStammdatenRS.removeInstitutionStammdaten(institutionStammdaten.id).then(() => {
                const index = EbeguUtil.getIndexOfElementwithID(institutionStammdaten, this.instStammdatenList);
                if (index > -1) {
                    this.instStammdatenList.splice(index, 1);
                }
            }).catch(() => {
                this.errormessage = 'INSTITUTION_STAMMDATEN_DELETE_FAILED';
            });
        });
    }

    public getDateString(dateRange: TSDateRange, format: string): string {
        if (dateRange.gueltigAb) {
            return dateRange.gueltigBis ?
                `${dateRange.gueltigAb.format(format)} - ${dateRange.gueltigBis.format(format)}` :
                dateRange.gueltigAb.format(format);
        }
        return '';
    }

    public getBetreuungsangebotFromInstitutionList(betreuungsangebotTyp: TSBetreuungsangebotTyp): any {
        return $.grep(this.betreuungsangebotValues, (value: any) => {
            return value.key === betreuungsangebotTyp;
        })[0];
    }

    private setBetreuungsangebotTypValues(): void {
        this.betreuungsangebotValues = this.ebeguUtil.translateStringList(getTSBetreuungsangebotTypValues());
    }

    public editInstitutionStammdaten(institutionstammdaten: TSInstitutionStammdaten): void {
        this.$state.go('admin.institutionstammdaten', {
            institutionId: this.selectedInstitution.id,
            institutionStammdatenId: institutionstammdaten.id
        });
    }

    public createInstitutionStammdaten(): void {
        this.$state.go('admin.institutionstammdaten', {
            institutionId: this.selectedInstitution.id,
            institutionStammdatenId: undefined
        });
    }
}
