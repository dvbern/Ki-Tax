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
    transclude = false;
    bindings = {
        traegerschaften: '<',
        mandant: '<'
    };
    template = require('./institutionView.html');
    controller = InstitutionViewController;
    controllerAs = 'vm';
}

export class InstitutionViewController extends AbstractAdminViewController {

    static $inject = ['InstitutionRS', 'InstitutionStammdatenRS', 'ErrorService', 'DvDialog', 'EbeguUtil', 'AuthServiceRS', '$stateParams', '$state'];

    form: IFormController;

    traegerschaften: TSTraegerschaft[];
    mandant: TSMandant;
    instStammdatenList: TSInstitutionStammdaten[] = [];
    selectedInstitution: TSInstitution = undefined;
    selectedInstitutionStammdaten: TSInstitutionStammdaten = undefined;
    betreuungsangebotValues: Array<any>;
    errormessage: string = undefined;

    constructor(private readonly institutionRS: InstitutionRS, private readonly institutionStammdatenRS: InstitutionStammdatenRS,
                private readonly errorService: ErrorService, private readonly dvDialog: DvDialog, private readonly ebeguUtil: EbeguUtil,
                authServiceRS: AuthServiceRS, private readonly $stateParams: IInstitutionStateParams,
                private readonly $state: StateService) {
        super(authServiceRS);
    }

    $onInit() {
        this.setBetreuungsangebotTypValues();
        if (this.$stateParams.institutionId) {
            this.institutionRS.findInstitution(this.$stateParams.institutionId).then((found: TSInstitution) => {
                this.setSelectedInstitution(found);
            });
        } else {
            this.createInstitution();
        }
    }

    getTreagerschaftList(): Array<TSTraegerschaft> {
        return this.traegerschaften;
    }

    createInstitution(): void {
        this.selectedInstitution = new TSInstitution();
        this.selectedInstitution.mandant = this.mandant;
        this.selectedInstitutionStammdaten = undefined;
    }

    setSelectedInstitution(institution: TSInstitution): void {
        this.selectedInstitution = institution;
        this.selectedInstitutionStammdaten = undefined;
        if (!this.isCreateInstitutionsMode()) {
            this.institutionStammdatenRS.getAllInstitutionStammdatenByInstitution(this.selectedInstitution.id).then((loadedInstStammdaten) => {
                this.instStammdatenList = loadedInstStammdaten;
            });
        }
        this.errormessage = undefined;
    }

    isCreateInstitutionsMode(): boolean {
        return this.selectedInstitution && this.selectedInstitution.isNew();
    }

    getSelectedInstitution(): TSInstitution {
        return this.selectedInstitution;
    }

    saveInstitution(form: IFormController): void {
        if (form.$valid) {
            this.errorService.clearAll();
            if (this.isCreateInstitutionsMode()) {
                this.institutionRS.createInstitution(this.selectedInstitution).then((institution: TSInstitution) => {
                    this.setSelectedInstitution(institution);
                });
            } else {
                this.institutionRS.updateInstitution(this.selectedInstitution).then((institution: TSInstitution) => {
                });
            }
        }
    }

    private goBack() {
        this.$state.go('admin.institutionen');
    }

    getInstitutionStammdatenList(): TSInstitutionStammdaten[] {
        return this.instStammdatenList;
    }

    removeInstitutionStammdaten(institutionStammdaten: TSInstitutionStammdaten): void {
        this.dvDialog.showRemoveDialog(removeDialogTemplate, this.form, RemoveDialogController, {
            deleteText: '',
            title: 'LOESCHEN_DIALOG_TITLE',
            parentController: undefined,
            elementID: undefined,
        }).then(() => {   //User confirmed removal
            this.institutionStammdatenRS.removeInstitutionStammdaten(institutionStammdaten.id).then((result) => {
                const index = EbeguUtil.getIndexOfElementwithID(institutionStammdaten, this.instStammdatenList);
                if (index > -1) {
                    this.instStammdatenList.splice(index, 1);
                }
            }).catch((ex) => {
                this.errormessage = 'INSTITUTION_STAMMDATEN_DELETE_FAILED';
            });
        });
    }

    getDateString(dateRange: TSDateRange, format: string): string {
        if (dateRange.gueltigAb) {
            if (!dateRange.gueltigBis) {
                return dateRange.gueltigAb.format(format);
            } else {
                return dateRange.gueltigAb.format(format) + ' - ' + dateRange.gueltigBis.format(format);
            }
        }
        return '';
    }

    getBetreuungsangebotFromInstitutionList(betreuungsangebotTyp: TSBetreuungsangebotTyp) {
        return $.grep(this.betreuungsangebotValues, (value: any) => {
            return value.key === betreuungsangebotTyp;
        })[0];
    }

    private setBetreuungsangebotTypValues(): void {
        this.betreuungsangebotValues = this.ebeguUtil.translateStringList(getTSBetreuungsangebotTypValues());
    }

    editInstitutionStammdaten(institutionstammdaten: TSInstitutionStammdaten) {
        this.$state.go('admin.institutionstammdaten', {
            institutionId: this.selectedInstitution.id,
            institutionStammdatenId: institutionstammdaten.id
        });
    }

    createInstitutionStammdaten(): void {
        this.$state.go('admin.institutionstammdaten', {
            institutionId: this.selectedInstitution.id,
            institutionStammdatenId: undefined
        });
    }
}
