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
import ErrorService from '../../../app/core/errors/service/ErrorService';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import TSFinanzielleSituationResultateDTO from '../../../models/dto/TSFinanzielleSituationResultateDTO';
import {TSWizardStepName} from '../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../models/enums/TSWizardStepStatus';
import TSEinkommensverschlechterung from '../../../models/TSEinkommensverschlechterung';
import TSEinkommensverschlechterungContainer from '../../../models/TSEinkommensverschlechterungContainer';
import TSFinanzModel from '../../../models/TSFinanzModel';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {IEinkommensverschlechterungResultateStateParams} from '../../gesuch.route';
import BerechnungsManager from '../../service/berechnungsManager';
import GesuchModelManager from '../../service/gesuchModelManager';
import WizardStepManager from '../../service/wizardStepManager';
import AbstractGesuchViewController from '../abstractGesuchView';
import IQService = angular.IQService;
import IScope = angular.IScope;
import ITimeoutService = angular.ITimeoutService;

export class EinkommensverschlechterungResultateViewComponentConfig implements IComponentOptions {
    public transclude = false;
    public template = require('./einkommensverschlechterungResultateView.html');
    public controller = EinkommensverschlechterungResultateViewController;
    public controllerAs = 'vm';
}

/**
 * Controller fuer die Finanzielle Situation
 */
export class EinkommensverschlechterungResultateViewController extends AbstractGesuchViewController<TSFinanzModel> {

    public static $inject: string[] = ['$stateParams', 'GesuchModelManager', 'BerechnungsManager', 'ErrorService',
        'WizardStepManager', '$q', '$scope', 'AuthServiceRS', '$timeout'];

    public resultatBasisjahr: TSFinanzielleSituationResultateDTO;
    public resultatProzent: string;

    public constructor($stateParams: IEinkommensverschlechterungResultateStateParams,
                       gesuchModelManager: GesuchModelManager,
                       berechnungsManager: BerechnungsManager,
                       private readonly errorService: ErrorService,
                       wizardStepManager: WizardStepManager,
                       private readonly $q: IQService,
                       $scope: IScope,
                       private readonly authServiceRS: AuthServiceRS,
                       $timeout: ITimeoutService) {
        super(gesuchModelManager,
            berechnungsManager,
            wizardStepManager,
            $scope,
            TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG,
            $timeout);
        const parsedBasisJahrPlusNum = parseInt($stateParams.basisjahrPlus, 10);
        this.model = new TSFinanzModel(this.gesuchModelManager.getBasisjahr(),
            this.gesuchModelManager.isGesuchsteller2Required(),
            null,
            parsedBasisJahrPlusNum);
        this.model.copyEkvDataFromGesuch(this.gesuchModelManager.getGesuch());
        this.model.copyFinSitDataFromGesuch(this.gesuchModelManager.getGesuch());
        this.gesuchModelManager.setBasisJahrPlusNumber(parsedBasisJahrPlusNum);
        this.calculate();
        this.resultatBasisjahr = null;
        this.calculateResultateVorjahr();
    }

    public showGS2(): boolean {
        return this.model.isGesuchsteller2Required();
    }

    public showResult(): boolean {
        if (this.model.getBasisJahrPlus() !== 1) {
            return true;
        }

        const infoContainer = this.model.einkommensverschlechterungInfoContainer;
        const ekvFuerBasisJahrPlus = infoContainer.einkommensverschlechterungInfoJA.ekvFuerBasisJahrPlus1;

        return ekvFuerBasisJahrPlus && ekvFuerBasisJahrPlus === true;
    }

    private save(): IPromise<void> {
        if (this.isGesuchValid()) {
            if (!this.form.$dirty) {
                // If there are no changes in form we don't need anything to update on Server and we could return the
                // promise immediately
                // Update wizardStepStatus also if the form is empty and not dirty
                return this.updateStatus(false);
            }

            this.model.copyEkvSitDataToGesuch(this.gesuchModelManager.getGesuch());
            this.errorService.clearAll();
            if (this.gesuchModelManager.getGesuch().gesuchsteller1) {
                this.gesuchModelManager.setGesuchstellerNumber(1);
                if (this.gesuchModelManager.getGesuch().gesuchsteller2) {
                    return this.gesuchModelManager.saveEinkommensverschlechterungContainer().then(() => {
                        this.gesuchModelManager.setGesuchstellerNumber(2);
                        return this.gesuchModelManager.saveEinkommensverschlechterungContainer().then(() => {
                            return this.updateStatus(true);
                        });
                    });
                }
                return this.gesuchModelManager.saveEinkommensverschlechterungContainer().then(() => {
                    return this.updateStatus(true);
                });
            }
        }
        return undefined;
    }

    /**
     * Hier wird der Status von WizardStep auf OK (MUTIERT fuer Mutationen) aktualisiert aber nur wenn es die letzt
     * Seite EVResultate gespeichert wird. Sonst liefern wir einfach den aktuellen GS als Promise zurueck.
     */
    private updateStatus(changes: boolean): IPromise<any> {
        if (this.isLastEinkVersStep()) {
            if (this.gesuchModelManager.getGesuch().isMutation()) {
                if (this.wizardStepManager.getCurrentStep().wizardStepStatus === TSWizardStepStatus.NOK || changes) {
                    return this.wizardStepManager.updateCurrentWizardStepStatusMutiert();
                }
            } else {
                return this.wizardStepManager.updateCurrentWizardStepStatus(TSWizardStepStatus.OK);
            }
        }
        // wenn nichts gespeichert einfach den aktuellen GS zurueckgeben
        return this.$q.when(this.gesuchModelManager.getStammdatenToWorkWith());
    }

    public calculate(): void {
        if (!this.model || !this.model.getBasisJahrPlus()) {
            console.log('No gesuch and Basisjahr to calculate');
            return;
        }

        this.berechnungsManager.calculateEinkommensverschlechterungTemp(this.model, this.model.getBasisJahrPlus())
            .then(() => {
                this.resultatProzent = this.calculateVeraenderung();
            });
    }

    public getEinkommensverschlechterungContainerGS1(): TSEinkommensverschlechterungContainer {
        return this.model.einkommensverschlechterungContainerGS1;
    }

    // tslint:disable-next-line:naming-convention
    public getEinkommensverschlechterungGS1_GS(): TSEinkommensverschlechterung {
        return this.model.getBasisJahrPlus() === 2 ?
            this.getEinkommensverschlechterungContainerGS1().ekvGSBasisJahrPlus2 :
            this.getEinkommensverschlechterungContainerGS1().ekvGSBasisJahrPlus1;
    }

    // tslint:disable-next-line:naming-convention
    public getEinkommensverschlechterungGS1_JA(): TSEinkommensverschlechterung {
        return this.model.getBasisJahrPlus() === 2 ?
            this.getEinkommensverschlechterungContainerGS1().ekvJABasisJahrPlus2 :
            this.getEinkommensverschlechterungContainerGS1().ekvJABasisJahrPlus1;
    }

    public getEinkommensverschlechterungContainerGS2(): TSEinkommensverschlechterungContainer {
        return this.model.einkommensverschlechterungContainerGS2;
    }

    // tslint:disable-next-line:naming-convention
    public getEinkommensverschlechterungGS2_GS(): TSEinkommensverschlechterung {
        return this.model.getBasisJahrPlus() === 2 ?
            this.getEinkommensverschlechterungContainerGS2().ekvGSBasisJahrPlus2 :
            this.getEinkommensverschlechterungContainerGS2().ekvGSBasisJahrPlus1;
    }

    // tslint:disable-next-line:naming-convention
    public getEinkommensverschlechterungGS2_JA(): TSEinkommensverschlechterung {
        return this.model.getBasisJahrPlus() === 2 ?
            this.getEinkommensverschlechterungContainerGS2().ekvJABasisJahrPlus2 :
            this.getEinkommensverschlechterungContainerGS2().ekvJABasisJahrPlus1;
    }

    public getResultate(): TSFinanzielleSituationResultateDTO {
        return this.model.getBasisJahrPlus() === 2 ?
            this.berechnungsManager.einkommensverschlechterungResultateBjP2 :
            this.berechnungsManager.einkommensverschlechterungResultateBjP1;
    }

    public calculateResultateVorjahr(): void {

        this.berechnungsManager.calculateFinanzielleSituationTemp(this.model).then(resultatVorjahr => {
            this.resultatBasisjahr = resultatVorjahr;
            this.resultatProzent = this.calculateVeraenderung();
        });
    }

    /**
     *
     * @returns Veraenderung im Prozent im vergleich zum Vorjahr
     */
    public calculateVeraenderung(): string {
        if (this.resultatBasisjahr) {

            const massgebendesEinkVorAbzFamGr = this.getResultate().massgebendesEinkVorAbzFamGr;
            const massgebendesEinkVorAbzFamGrBJ = this.resultatBasisjahr.massgebendesEinkVorAbzFamGr;
            if (massgebendesEinkVorAbzFamGr && massgebendesEinkVorAbzFamGrBJ) {

                // we divide it by 10000 because we need a result with two decimals
                const tenThousand = 10000;
                let promil = tenThousand - (massgebendesEinkVorAbzFamGr * tenThousand / massgebendesEinkVorAbzFamGrBJ);
                promil = Math.round(promil);
                const sign = promil > 0 ? '- ' : '+ ';
                const value = (Math.abs(promil) / 100).toFixed(2);

                return `${sign + value} %`;
            }
            if (!massgebendesEinkVorAbzFamGr && !massgebendesEinkVorAbzFamGrBJ) {
                // case: Kein Einkommen in diesem Jahr und im letzten Jahr
                return '+ 0.00 %';
            }
            if (!massgebendesEinkVorAbzFamGr) {
                // case: Kein Einkommen in diesem Jahr aber Einkommen im letzten Jahr
                return '- 100.00 %';
            }
            // case: Kein Einkommen im letzten Jahr aber Einkommen in diesem Jahr
            return '+ 100.00 %';
        }
        return '';
    }

    /**
     * Prueft ob es die letzte Seite von EVResultate ist. Es ist die letzte Seite wenn es zum letzten EV-Jahr gehoert
     */
    private isLastEinkVersStep(): boolean {
        // Letztes Jahr haengt von den eingegebenen Daten ab
        const info = this.gesuchModelManager.getGesuch().extractEinkommensverschlechterungInfo();

        return info.ekvFuerBasisJahrPlus2 && this.gesuchModelManager.basisJahrPlusNumber === 2
            || !info.ekvFuerBasisJahrPlus2 && this.gesuchModelManager.basisJahrPlusNumber === 1;
    }

    /**
     * Da fuer STEUERAMT der Step EINKOMMENSVERSCHLECHTERUNG der letzte ist, muss man den Button next verstecken, wenn
     * wir wirklich im LastEinkVersStep sind. Fuer alle andere rollen wird der Button immer eingeblendet
     */
    public isSteueramtLetzterStep(): boolean {
        if (this.authServiceRS.isOneOfRoles(TSRoleUtil.getSteueramtOnlyRoles())) {
            return this.isLastEinkVersStep();
        }
        return false;
    }
}
