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

import {IComponentOptions, IPromise} from 'angular';
import {DvDialog} from '../../../app/core/directive/dv-dialog/dv-dialog';
import ErrorService from '../../../app/core/errors/service/ErrorService';
import {getTSFamilienstatusValues, TSFamilienstatus} from '../../../models/enums/TSFamilienstatus';
import {TSRole} from '../../../models/enums/TSRole';
import {TSWizardStepName} from '../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../models/enums/TSWizardStepStatus';
import TSFamiliensituation from '../../../models/TSFamiliensituation';
import TSFamiliensituationContainer from '../../../models/TSFamiliensituationContainer';
import EbeguUtil from '../../../utils/EbeguUtil';
import {RemoveDialogController} from '../../dialog/RemoveDialogController';
import BerechnungsManager from '../../service/berechnungsManager';
import FamiliensituationRS from '../../service/familiensituationRS.rest';
import GesuchModelManager from '../../service/gesuchModelManager';
import WizardStepManager from '../../service/wizardStepManager';
import AbstractGesuchViewController from '../abstractGesuchView';
import IQService = angular.IQService;
import IScope = angular.IScope;
import ITimeoutService = angular.ITimeoutService;
import ITranslateService = angular.translate.ITranslateService;

const removeDialogTemplate = require('../../dialog/removeDialogTemplate.html');

export class FamiliensituationViewComponentConfig implements IComponentOptions {
    public transclude = false;
    public bindings = {};
    public template = require('./familiensituationView.html');
    public controller = FamiliensituationViewController;
    public controllerAs = 'vm';
}

export class FamiliensituationViewController extends AbstractGesuchViewController<TSFamiliensituationContainer> {

    public static $inject = [
        'GesuchModelManager',
        'BerechnungsManager',
        'ErrorService',
        'WizardStepManager',
        'DvDialog',
        '$translate',
        '$q',
        '$scope',
        'FamiliensituationRS',
        '$timeout',
    ];
    public familienstatusValues: Array<TSFamilienstatus>;
    public allowedRoles: Array<TSRole>;
    public initialFamiliensituation: TSFamiliensituation;
    public savedClicked: boolean = false;

    public constructor(
        gesuchModelManager: GesuchModelManager,
        berechnungsManager: BerechnungsManager,
        private readonly errorService: ErrorService,
        wizardStepManager: WizardStepManager,
        private readonly dvDialog: DvDialog,
        private readonly $translate: ITranslateService,
        private readonly $q: IQService,
        $scope: IScope,
        private readonly familiensituationRS: FamiliensituationRS,
        $timeout: ITimeoutService,
    ) {

        super(gesuchModelManager,
            berechnungsManager,
            wizardStepManager,
            $scope,
            TSWizardStepName.FAMILIENSITUATION,
            $timeout);
        this.gesuchModelManager.initFamiliensituation();
        this.model = angular.copy(this.gesuchModelManager.getGesuch().familiensituationContainer);
        this.initialFamiliensituation = angular.copy(this.gesuchModelManager.getFamiliensituation());
        this.familienstatusValues = getTSFamilienstatusValues();

        this.initViewModel();

    }

    private initViewModel(): void {
        this.wizardStepManager.updateCurrentWizardStepStatus(TSWizardStepStatus.IN_BEARBEITUNG);
        this.allowedRoles = this.TSRoleUtil.getAllRolesButTraegerschaftInstitution();
    }

    public confirmAndSave(): IPromise<TSFamiliensituationContainer> {
        this.savedClicked = true;
        if (this.isGesuchValid() && !this.hasEmptyAenderungPer() && !this.hasError()) {
            if (!this.form.$dirty) {
                // If there are no changes in form we don't need anything to update on Server and we could return the
                // promise immediately
                // Update wizardStepStatus also if the form is empty and not dirty
                this.wizardStepManager.updateCurrentWizardStepStatus(TSWizardStepStatus.OK);
                return this.$q.when(this.gesuchModelManager.getGesuch().familiensituationContainer);
            }

            if (this.isConfirmationRequired()) {
                const descriptionText: any = this.$translate.instant('FAMILIENSITUATION_WARNING_BESCHREIBUNG', {
                    gsfullname: this.gesuchModelManager.getGesuch().gesuchsteller2
                        ? this.gesuchModelManager.getGesuch().gesuchsteller2.extractFullName() : '',
                });
                return this.dvDialog.showRemoveDialog(removeDialogTemplate, this.form, RemoveDialogController, {
                    title: 'FAMILIENSITUATION_WARNING',
                    deleteText: descriptionText,
                }).then(() => {   // User confirmed changes
                    return this.save();
                });
            }

            return this.save();

        }
        return undefined;
    }

    private save(): IPromise<TSFamiliensituationContainer> {
        this.errorService.clearAll();
        return this.familiensituationRS.saveFamiliensituation(this.model,
            this.gesuchModelManager.getGesuch().id).then((familienContainerResponse: any) => {
            this.model = familienContainerResponse;
            this.gesuchModelManager.getGesuch().familiensituationContainer = familienContainerResponse;
            // Gesuchsteller may changed...
            return this.gesuchModelManager.reloadGesuch().then(() => {
                return this.model;
            });
        });
    }

    public getFamiliensituation(): TSFamiliensituation {
        return this.model.familiensituationJA;
    }

    public isStartKonkubinatVisible(): boolean {
        return this.getFamiliensituation().familienstatus === TSFamilienstatus.KONKUBINAT_KEIN_KIND;
    }

    public getFamiliensituationErstgesuch(): TSFamiliensituation {
        return this.model.familiensituationErstgesuch;
    }

    /**
     * Confirmation is required when the GS2 already exists and the familiensituation changes from 2GS to 1GS. Or when
     * in a Mutation the GS2 is new and will be removed
     */
    private isConfirmationRequired(): boolean {
        return (!this.isMutation() && this.checkChanged2To1GS()) ||
            (this.isMutation() && this.checkChanged2To1GSMutation());
    }

    private checkChanged2To1GS(): boolean {
        const bis = this.gesuchModelManager.getGesuchsperiode().gueltigkeit.gueltigBis;
        return this.gesuchModelManager.getGesuch().gesuchsteller2
            && this.gesuchModelManager.getGesuch().gesuchsteller2.id
            && this.initialFamiliensituation.hasSecondGesuchsteller(bis)
            && this.isScheidung();
    }

    private checkChanged2To1GSMutation(): boolean {
        const bis = this.gesuchModelManager.getGesuchsperiode().gueltigkeit.gueltigBis;
        return this.gesuchModelManager.getGesuch().gesuchsteller2
            && this.gesuchModelManager.getGesuch().gesuchsteller2.id
            && this.isScheidung()
            && this.model.familiensituationErstgesuch
            && !this.model.familiensituationErstgesuch.hasSecondGesuchsteller(bis);
    }

    private isScheidung(): boolean {
        const bis = this.gesuchModelManager.getGesuchsperiode().gueltigkeit.gueltigBis;
        return this.initialFamiliensituation.hasSecondGesuchsteller(bis)
            && !this.getFamiliensituation().hasSecondGesuchsteller(bis);
    }

    public isMutationAndDateSet(): boolean {
        if (!this.isMutation()) {
            return true;
        }

        return EbeguUtil.isNotNullOrUndefined(this.getFamiliensituation().aenderungPer);
    }

    public isEnabled(): boolean {
        return this.isMutationAndDateSet() && !this.isGesuchReadonly() && !this.isKorrekturModusJugendamt();
    }

    public hasEmptyAenderungPer(): boolean {
        return this.isMutation()
            && !this.getFamiliensituation().aenderungPer
            && !this.isKorrekturModusJugendamt()
            && !this.getFamiliensituationErstgesuch().isSameFamiliensituation(this.getFamiliensituation());
    }

    public resetFamsit(): void {
        this.getFamiliensituation().revertFamiliensituation(this.getFamiliensituationErstgesuch());
    }

    public hasError(): boolean {
        return this.isMutation()
            && this.getFamiliensituation().aenderungPer
            && this.getFamiliensituationErstgesuch().isSameFamiliensituation(this.getFamiliensituation());
    }

    public showError(): boolean {
        return this.hasError() && this.savedClicked;
    }

    public onDatumBlur(): void {
        if (this.hasEmptyAenderungPer()) {
            this.resetFamsit();
        }
    }

    public gesuchstellerHasChangedZivilstand(): boolean {
        return this.model.familiensituationGS && !!this.model.familiensituationGS.aenderungPer;
    }
}
