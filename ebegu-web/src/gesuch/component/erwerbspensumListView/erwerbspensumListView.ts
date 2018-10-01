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
import * as moment from 'moment';
import {IDVFocusableController} from '../../../app/core/component/IDVFocusableController';
import {DvDialog} from '../../../app/core/directive/dv-dialog/dv-dialog';
import ErrorService from '../../../app/core/errors/service/ErrorService';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import {TSRole} from '../../../models/enums/TSRole';
import {TSWizardStepName} from '../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../models/enums/TSWizardStepStatus';
import TSErwerbspensumContainer from '../../../models/TSErwerbspensumContainer';
import {RemoveDialogController} from '../../dialog/RemoveDialogController';
import BerechnungsManager from '../../service/berechnungsManager';
import GesuchModelManager from '../../service/gesuchModelManager';
import WizardStepManager from '../../service/wizardStepManager';
import AbstractGesuchViewController from '../abstractGesuchView';
import ILogService = angular.ILogService;
import IScope = angular.IScope;
import ITimeoutService = angular.ITimeoutService;

const removeDialogTemplate = require('../../dialog/removeDialogTemplate.html');

export class ErwerbspensumListViewComponentConfig implements IComponentOptions {
    public transclude = false;
    public bindings = {};
    public template = require('./erwerbspensumListView.html');
    public controller = ErwerbspensumListViewController;
    public controllerAs = 'vm';
}

export class ErwerbspensumListViewController extends AbstractGesuchViewController<any> implements IDVFocusableController {

    public static $inject: string[] = ['$state', 'GesuchModelManager', 'BerechnungsManager', '$log', 'DvDialog',
        'ErrorService', 'WizardStepManager', '$scope', 'AuthServiceRS', '$timeout'];

    public erwerbspensenGS1: Array<TSErwerbspensumContainer> = undefined;
    public erwerbspensenGS2: Array<TSErwerbspensumContainer>;
    public erwerbspensumRequired: boolean;

    public constructor(private readonly $state: StateService, gesuchModelManager: GesuchModelManager, berechnungsManager: BerechnungsManager,
                       private readonly $log: ILogService, private readonly dvDialog: DvDialog, private readonly errorService: ErrorService,
                       wizardStepManager: WizardStepManager, $scope: IScope, private readonly authServiceRS: AuthServiceRS, $timeout: ITimeoutService) {
        super(gesuchModelManager, berechnungsManager, wizardStepManager, $scope, TSWizardStepName.ERWERBSPENSUM, $timeout);
        this.initViewModel();
    }

    private initViewModel() {
        this.gesuchModelManager.isErwerbspensumRequired(this.getGesuchId()).then((response: boolean) => {
            this.erwerbspensumRequired = response;
            if (this.isSaveDisabled()) {
                this.wizardStepManager.updateCurrentWizardStepStatus(TSWizardStepStatus.IN_BEARBEITUNG);
            } else {
                this.wizardStepManager.updateCurrentWizardStepStatus(TSWizardStepStatus.OK);
            }
        });
    }

    public getErwerbspensenListGS1(): Array<TSErwerbspensumContainer> {
        if (this.erwerbspensenGS1 === undefined) {
            if (this.gesuchModelManager.getGesuch() && this.gesuchModelManager.getGesuch().gesuchsteller1 &&
                this.gesuchModelManager.getGesuch().gesuchsteller1.erwerbspensenContainer) {
                const gesuchsteller1 = this.gesuchModelManager.getGesuch().gesuchsteller1;
                this.erwerbspensenGS1 = gesuchsteller1.erwerbspensenContainer;

            } else {
                this.erwerbspensenGS1 = [];
            }
        }
        return this.erwerbspensenGS1;
    }

    public getErwerbspensenListGS2(): Array<TSErwerbspensumContainer> {
        if (this.erwerbspensenGS2 === undefined) {
            if (this.gesuchModelManager.getGesuch() && this.gesuchModelManager.getGesuch().gesuchsteller2 &&
                this.gesuchModelManager.getGesuch().gesuchsteller2.erwerbspensenContainer) {
                const gesuchsteller2 = this.gesuchModelManager.getGesuch().gesuchsteller2;
                this.erwerbspensenGS2 = gesuchsteller2.erwerbspensenContainer;

            } else {
                this.erwerbspensenGS2 = [];
            }
        }
        return this.erwerbspensenGS2;

    }

    public createErwerbspensum(gesuchstellerNumber: number): void {
        this.openErwerbspensumView(gesuchstellerNumber, undefined);
    }

    public removePensum(pensum: TSErwerbspensumContainer, gesuchstellerNumber: number, element_id: string, index: any): void {
        // Spezielle Meldung, wenn es ein GS ist, der in einer Mutation loescht
        const gsInMutation = (this.authServiceRS.getPrincipalRole() === TSRole.GESUCHSTELLER && pensum.vorgaengerId !== undefined);
        const pensumLaufendOderVergangen = pensum.erwerbspensumJA.gueltigkeit.gueltigAb.isBefore(moment(moment.now()));
        this.errorService.clearAll();
        this.dvDialog.showRemoveDialog(removeDialogTemplate, this.form, RemoveDialogController, {
            deleteText: (gsInMutation && pensumLaufendOderVergangen) ? 'ERWERBSPENSUM_LOESCHEN_GS_MUTATION' : '',
            title: 'ERWERBSPENSUM_LOESCHEN',
            parentController: this,
            elementID: element_id + index
        })
            .then(() => {   // User confirmed removal
                this.gesuchModelManager.setGesuchstellerNumber(gesuchstellerNumber);
                this.gesuchModelManager.removeErwerbspensum(pensum);

            });

    }

    public editPensum(pensum: any, gesuchstellerNumber: any): void {
        const index = this.gesuchModelManager.findIndexOfErwerbspensum(parseInt(gesuchstellerNumber), pensum);
        this.openErwerbspensumView(gesuchstellerNumber, index);
    }

    private openErwerbspensumView(gesuchstellerNumber: number, erwerbspensumNum: number): void {
        this.$state.go('gesuch.erwerbsPensum', {
            gesuchstellerNumber,
            erwerbspensumNum,
            gesuchId: this.getGesuchId()
        });
    }

    /**
     * Gibt true zurueck wenn Erwerbspensen nicht notwendig sind oder wenn sie notwendig sind aber mindestens eines pro Gesuchsteller
     * eingegeben wurde.
     * @returns {boolean}
     */
    public isSaveDisabled(): boolean {
        const erwerbspensenNumber = 0;
        if (this.erwerbspensumRequired) {
            if (this.getErwerbspensenListGS1() && this.getErwerbspensenListGS1().length <= 0) {
                return true;
            }
            if (this.gesuchModelManager.isGesuchsteller2Required() && this.getErwerbspensenListGS2() && this.getErwerbspensenListGS2().length <= 0) {
                return true;
            }
        }
        return false;
    }

    public setFocusBack(elementID: string): void {
        angular.element('#' + elementID).first().focus();
    }
}
