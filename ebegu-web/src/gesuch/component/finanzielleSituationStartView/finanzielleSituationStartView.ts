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

import {IComponentOptions} from 'angular';
import {DvDialog} from '../../../app/core/directive/dv-dialog/dv-dialog';
import ErrorService from '../../../app/core/errors/service/ErrorService';
import {TSWizardStepName} from '../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../models/enums/TSWizardStepStatus';
import TSFinanzielleSituation from '../../../models/TSFinanzielleSituation';
import TSFinanzModel from '../../../models/TSFinanzModel';
import TSGesuch from '../../../models/TSGesuch';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {RemoveDialogController} from '../../dialog/RemoveDialogController';
import BerechnungsManager from '../../service/berechnungsManager';
import GesuchModelManager from '../../service/gesuchModelManager';
import WizardStepManager from '../../service/wizardStepManager';
import AbstractGesuchViewController from '../abstractGesuchView';
import IPromise = angular.IPromise;
import IQService = angular.IQService;
import IScope = angular.IScope;
import ITimeoutService = angular.ITimeoutService;

const removeDialogTemplate = require('../../dialog/removeDialogTemplate.html');

export class FinanzielleSituationStartViewComponentConfig implements IComponentOptions {
    public transclude = false;
    public template = require('./finanzielleSituationStartView.html');
    public controller = FinanzielleSituationStartViewController;
    public controllerAs = 'vm';
}

export class FinanzielleSituationStartViewController extends AbstractGesuchViewController<TSFinanzModel> {

    public static $inject: string[] = ['GesuchModelManager', 'BerechnungsManager', 'ErrorService',
        'WizardStepManager', '$q', '$scope', '$timeout', 'DvDialog'];

    public finanzielleSituationRequired: boolean;
    public areThereOnlySchulamtangebote: boolean;
    public areThereOnlyFerieninsel: boolean;
    public allowedRoles: Array<TSRoleUtil>;
    private readonly initialModel: TSFinanzModel;

    public constructor(gesuchModelManager: GesuchModelManager,
                       berechnungsManager: BerechnungsManager,
                       private readonly errorService: ErrorService,
                       wizardStepManager: WizardStepManager,
                       private readonly $q: IQService,
                       $scope: IScope,
                       $timeout: ITimeoutService,
                       private readonly dvDialog: DvDialog) {
        super(gesuchModelManager,
            berechnungsManager,
            wizardStepManager,
            $scope,
            TSWizardStepName.FINANZIELLE_SITUATION,
            $timeout);

        this.model = new TSFinanzModel(this.gesuchModelManager.getBasisjahr(),
            this.gesuchModelManager.isGesuchsteller2Required(),
            null);
        this.model.copyFinSitDataFromGesuch(this.gesuchModelManager.getGesuch());
        this.initialModel = angular.copy(this.model);

        this.allowedRoles = this.TSRoleUtil.getAllRolesButTraegerschaftInstitution();
        this.wizardStepManager.updateCurrentWizardStepStatus(TSWizardStepStatus.IN_BEARBEITUNG);
        this.areThereOnlySchulamtangebote = this.gesuchModelManager.areThereOnlySchulamtAngebote(); // so we load it
                                                                                                    // just once
        this.areThereOnlyFerieninsel = this.gesuchModelManager.areThereOnlyFerieninsel(); // so we load it just once
    }

    public showSteuerveranlagung(): boolean {
        return this.model.gemeinsameSteuererklaerung;
    }

    public showSteuererklaerung(): boolean {
        return !this.getFinanzielleSituationGS1().steuerveranlagungErhalten;
    }

    private save(): IPromise<TSGesuch> {
        this.errorService.clearAll();
        return this.gesuchModelManager.saveFinanzielleSituationStart()
            .then((gesuch: TSGesuch) => {
                // Noetig, da nur das ganze Gesuch upgedated wird und die Aeenderng bei der FinSit sonst nicht
                // bemerkt werden
                if (this.gesuchModelManager.getGesuch().isMutation()) {
                    this.wizardStepManager.updateCurrentWizardStepStatusMutiert();
                }
                return gesuch;
            });
    }

    private confirmAndSave(): IPromise<TSGesuch> {
        if (this.isGesuchValid()) {
            this.model.copyFinSitDataToGesuch(this.gesuchModelManager.getGesuch());
            if (!this.form.$dirty) {
                if (this.updateStepDueToOnlyFerieninsel()) {
                    return this.wizardStepManager.updateWizardStepStatus(TSWizardStepName.FINANZIELLE_SITUATION,
                        TSWizardStepStatus.OK).then(() => {
                        return this.gesuchModelManager.getGesuch();
                    });
                }
                // If there are no changes in form we don't need anything to update on Server and we could return the
                // promise immediately
                return this.$q.when(this.gesuchModelManager.getGesuch());
            }
            if (this.finanzielleSituationTurnedNotRequired()) {
                return this.dvDialog.showDialog(removeDialogTemplate, RemoveDialogController, {
                    title: 'FINSIT_WARNING',
                    deleteText: 'FINSIT_WARNING_BESCHREIBUNG',
                    parentController: undefined,
                    elementID: undefined,
                    form: this.form
                }).then(() => {   // User confirmed changes
                    return this.save();
                });
            }
            return this.save();
        }
        return undefined;
    }

    /**
     * Id the Step is still in status IN_BEARBEITUNG and there are only Ferieninsel, the Gesuch must be updated.
     */
    private updateStepDueToOnlyFerieninsel(): boolean {
        return this.wizardStepManager.hasStepGivenStatus(TSWizardStepName.FINANZIELLE_SITUATION,
            TSWizardStepStatus.IN_BEARBEITUNG)
            && this.gesuchModelManager.getGesuch().areThereOnlyFerieninsel();
    }

    public finanzielleSituationTurnedNotRequired(): boolean {
        return this.initialModel.isFinanzielleSituationDesired() && !this.model.isFinanzielleSituationDesired();
    }

    public getFinanzielleSituationGS1(): TSFinanzielleSituation {
        return this.model.finanzielleSituationContainerGS1.finanzielleSituationJA;
    }

    private getFinanzielleSituationGS2(): TSFinanzielleSituation {
        return this.model.finanzielleSituationContainerGS2.finanzielleSituationJA;
    }

    public isFinanziellesituationRequired(): boolean {
        return this.finanzielleSituationRequired;
    }

    private hasTagesschulenAnmeldung(): boolean {
        return this.gesuchModelManager.getGesuchsperiode().hasTagesschulenAnmeldung();
    }

    public gemeinsameStekClicked(): void {
        if (!this.model.gemeinsameSteuererklaerung && this.model.finanzielleSituationContainerGS1 && !this.model.finanzielleSituationContainerGS1.isNew()) {
            // Wenn neu NEIN und schon was eingegeben -> Fragen mal auf false setzen und Status auf nok damit man
            // sicher noch weiter muss!
            this.initSteuerFragen();
            this.wizardStepManager.updateCurrentWizardStepStatus(TSWizardStepStatus.NOK);
        } else if (!this.model.gemeinsameSteuererklaerung) {
            // Wenn neu NEIN -> Fragen loeschen wenn noch nichts eingegeben worden ist
            this.model.finanzielleSituationContainerGS1 = undefined;
            this.model.finanzielleSituationContainerGS2 = undefined;
        } else {
            this.model.initFinSit();
        }
    }

    /**
     * Es muss ein Wert geschrieben werden, um finsit persisierten zu können -> setzt die Antwort der Fragen auf false
     */
    private initSteuerFragen(): void {
        if (this.model.finanzielleSituationContainerGS1) {
            const gs1 = this.model.finanzielleSituationContainerGS1.finanzielleSituationJA;

            gs1.steuererklaerungAusgefuellt = !!gs1.steuererklaerungAusgefuellt;
            gs1.steuerveranlagungErhalten = !!gs1.steuerveranlagungErhalten;
        }

        if (!this.model.finanzielleSituationContainerGS2) {
            return;
        }

        const gs2 = this.model.finanzielleSituationContainerGS2.finanzielleSituationJA;
        gs2.steuererklaerungAusgefuellt = !!gs2.steuererklaerungAusgefuellt;
        gs2.steuerveranlagungErhalten = !gs2.steuerveranlagungErhalten;
    }

    public steuerveranlagungClicked(): void {
        // Wenn Steuerveranlagung JA -> auch StekErhalten -> JA
        // Wenn zusätzlich noch GemeinsameStek -> Dasselbe auch für GS2
        // Wenn Steuerveranlagung erhalten, muss auch STEK ausgefüllt worden sein
        if (this.model.finanzielleSituationContainerGS1.finanzielleSituationJA.steuerveranlagungErhalten) {
            this.model.finanzielleSituationContainerGS1.finanzielleSituationJA.steuererklaerungAusgefuellt = true;
            if (this.model.gemeinsameSteuererklaerung) {
                this.model.finanzielleSituationContainerGS2.finanzielleSituationJA.steuerveranlagungErhalten = true;
                this.model.finanzielleSituationContainerGS2.finanzielleSituationJA.steuererklaerungAusgefuellt = true;
            }
        } else if (!this.model.finanzielleSituationContainerGS1.finanzielleSituationJA.steuerveranlagungErhalten) {
            // Steuerveranlagung neu NEIN -> Fragen loeschen
            this.model.finanzielleSituationContainerGS1.finanzielleSituationJA.steuererklaerungAusgefuellt = undefined;
            if (this.model.gemeinsameSteuererklaerung) {
                this.model.finanzielleSituationContainerGS2.finanzielleSituationJA.steuerveranlagungErhalten = false;
                this.model.finanzielleSituationContainerGS2.finanzielleSituationJA.steuererklaerungAusgefuellt =
                    undefined;
            }
        }
    }

    public steuererklaerungClicked(): void {
        if (this.model.gemeinsameSteuererklaerung) {
            this.model.finanzielleSituationContainerGS2.finanzielleSituationJA.steuererklaerungAusgefuellt =
                this.model.finanzielleSituationContainerGS1.finanzielleSituationJA.steuererklaerungAusgefuellt;
        }
    }

    public is2GSRequired(): boolean {
        return this.gesuchModelManager.isGesuchsteller2Required();
    }
}
