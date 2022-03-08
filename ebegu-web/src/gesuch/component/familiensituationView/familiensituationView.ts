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
import {EinstellungRS} from '../../../admin/service/einstellungRS.rest';
import {DvDialog} from '../../../app/core/directive/dv-dialog/dv-dialog';
import {ErrorService} from '../../../app/core/errors/service/ErrorService';
import {TSEinstellungKey} from '../../../models/enums/TSEinstellungKey';
import {
    getTSFamilienstatusFKJVValues,
    getTSFamilienstatusValues,
    TSFamilienstatus,
} from '../../../models/enums/TSFamilienstatus';
import {
    getTSGesuchstellerKardinalitaetValues,
    TSGesuchstellerKardinalitaet,
} from '../../../models/enums/TSGesuchstellerKardinalitaet';
import {TSRole} from '../../../models/enums/TSRole';
import {TSWizardStepName} from '../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../models/enums/TSWizardStepStatus';
import {TSEinstellung} from '../../../models/TSEinstellung';
import {TSFamiliensituation} from '../../../models/TSFamiliensituation';
import {TSFamiliensituationContainer} from '../../../models/TSFamiliensituationContainer';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {RemoveDialogController} from '../../dialog/RemoveDialogController';
import {BerechnungsManager} from '../../service/berechnungsManager';
import {FamiliensituationRS} from '../../service/familiensituationRS.rest';
import {GesuchModelManager} from '../../service/gesuchModelManager';
import {WizardStepManager} from '../../service/wizardStepManager';
import {AbstractGesuchViewController} from '../abstractGesuchView';
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
        'EinstellungRS',
        '$timeout',
    ];
    private familienstatusValues: Array<TSFamilienstatus>;
    public allowedRoles: ReadonlyArray<TSRole>;
    public initialFamiliensituation: TSFamiliensituation;
    public savedClicked: boolean = false;
    public situationFKJV = false;
    public gesuchstellerKardinalitaetValues: Array<TSGesuchstellerKardinalitaet>;

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
        private readonly einstellungRS: EinstellungRS,
        $timeout: ITimeoutService,
    ) {

        super(gesuchModelManager,
            berechnungsManager,
            wizardStepManager,
            $scope,
            TSWizardStepName.FAMILIENSITUATION,
            $timeout);
        this.gesuchModelManager.initFamiliensituation();
        this.model = angular.copy(this.getGesuch().familiensituationContainer);
        this.initialFamiliensituation = angular.copy(this.gesuchModelManager.getFamiliensituation());
        this.gesuchstellerKardinalitaetValues = getTSGesuchstellerKardinalitaetValues();
        this.initViewModel();

    }

    public $onInit(): void {
        this.einstellungRS.getAllEinstellungenBySystemCached(
            this.gesuchModelManager.getGesuchsperiode().id,
        ).then((response: TSEinstellung[]) => {
            response.filter(r => r.key === TSEinstellungKey.FKJV_FAMILIENSITUATION_NEU)
                .forEach(value => {
                    this.situationFKJV = value.getValueAsBoolean();
                    this.familienstatusValues =
                        this.situationFKJV ? getTSFamilienstatusFKJVValues() : getTSFamilienstatusValues();
                    this.getFamiliensituation().fkjvFamSit = this.situationFKJV;
                });
            response.filter(r => r.key === TSEinstellungKey.MINIMALDAUER_KONKUBINAT)
                .forEach(value => {
                    this.getFamiliensituation().minDauerKonkubinat = Number(value.value);
                });
        });
    }

    private initViewModel(): void {
        this.wizardStepManager.updateCurrentWizardStepStatusSafe(
            TSWizardStepName.FAMILIENSITUATION,
            TSWizardStepStatus.IN_BEARBEITUNG);
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
                return this.$q.when(this.getGesuch().familiensituationContainer);
            }

            if (this.isConfirmationRequired()) {
                const descriptionText: any = this.$translate.instant('FAMILIENSITUATION_WARNING_BESCHREIBUNG', {
                    gsfullname: this.getGesuch().gesuchsteller2
                        ? this.getGesuch().gesuchsteller2.extractFullName() : '',
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
        return this.familiensituationRS.saveFamiliensituation(
            this.model,
            this.getGesuch().id,
        ).then((familienContainerResponse: any) => {
            this.model = familienContainerResponse;
            this.getGesuch().familiensituationContainer = familienContainerResponse;
            // Gesuchsteller may changed...
            return this.gesuchModelManager.reloadGesuch().then(() => {
                return this.model;
            });
        });
    }

    public getFamiliensituation(): TSFamiliensituation {
        return this.model.familiensituationJA;
    }

    public getFamiliensituationGS(): TSFamiliensituation {
        return this.model.familiensituationGS;
    }

    public isStartKonkubinatVisible(): boolean {
        return this.getFamiliensituation().familienstatus === TSFamilienstatus.KONKUBINAT_KEIN_KIND;
    }

    /**
     * Removes startKonkubinat when the familienstatus doesn't require it.
     * If we are in a mutation and we change to KONKUBINAT_KEIN_KIND we need to copy aenderungPer into startKonkubinat
     */
    public familienstatusChanged(): void {
        if (this.getFamiliensituation().familienstatus !== TSFamilienstatus.KONKUBINAT_KEIN_KIND) {
            this.getFamiliensituation().startKonkubinat = undefined;

        } else if (this.isMutation() && this.getFamiliensituation().aenderungPer && this.isStartKonkubinatVisible()) {
            this.getFamiliensituation().startKonkubinat = this.getFamiliensituation().aenderungPer;
        }
        if (!this.showGesuchstellerKardinalitaet()) {
            this.getFamiliensituation().gesuchstellerKardinalitaet = undefined;
        }
    }

    /**
     * This should happen only in a Mutation, where we can change the field aenderungPer but not startKonkubinat.
     * Any change in aenderungPer will copy the value into startKonkubinat if the last is visible
     */
    public aenderungPerChanged(): void {
        if (this.isStartKonkubinatVisible()) {
            this.getFamiliensituation().startKonkubinat = this.getFamiliensituation().aenderungPer;
        }
    }

    public getFamiliensituationErstgesuch(): TSFamiliensituation {
        return this.model.familiensituationErstgesuch;
    }

    /**
     * Confirmation is required when the GS2 already exists and the familiensituation changes from 2GS to 1GS. Or when
     * in a Mutation the GS2 is new and will be removed
     */
    private isConfirmationRequired(): boolean {
        return (!this.isKorrekturModusJugendamt()
            || (this.isKorrekturModusJugendamt()
                && this.getGesuch().gesuchsteller2
                && !this.getGesuch().gesuchsteller2.gesuchstellerGS))
            && ((!this.isMutation() && this.checkChanged2To1GS())
                || (this.isMutation() && this.checkChanged2To1GSMutation()));
    }

    private checkChanged2To1GS(): boolean {
        const bis = this.gesuchModelManager.getGesuchsperiode().gueltigkeit.gueltigBis;
        return this.getGesuch().gesuchsteller2
            && this.getGesuch().gesuchsteller2.id
            && this.initialFamiliensituation.hasSecondGesuchsteller(bis)
            && this.isScheidung();
    }

    private checkChanged2To1GSMutation(): boolean {
        const bis = this.gesuchModelManager.getGesuchsperiode().gueltigkeit.gueltigBis;
        return this.getGesuch().gesuchsteller2
            && this.getGesuch().gesuchsteller2.id
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

    public isFamiliensituationEnabled(): boolean {
        return this.isMutationAndDateSet() && !this.isGesuchReadonly();
    }

    public isStartKonkubinatDisabled(): boolean {
        return this.isMutation() || (this.isGesuchReadonly() && !this.isKorrekturModusJugendamt());
    }

    public hasEmptyAenderungPer(): boolean {
        return this.isMutation()
            && !this.getFamiliensituation().aenderungPer
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

    public getFamiliensituationValues(): Array<TSFamilienstatus> {
        return this.familienstatusValues;
    }

    public showGesuchstellerKardinalitaet(): boolean {
        if (this.getFamiliensituation() && this.situationFKJV) {
            return this.getFamiliensituation().familienstatus === TSFamilienstatus.ALLEINERZIEHEND;
        }
        return false;
    }

    public getTextForFamSitFrage2Tooltip(): string {
        return this.$translate.instant('FAMILIENSITUATION_HELP',
            {jahr: this.getFamiliensituation().minDauerKonkubinat});
    }
}
