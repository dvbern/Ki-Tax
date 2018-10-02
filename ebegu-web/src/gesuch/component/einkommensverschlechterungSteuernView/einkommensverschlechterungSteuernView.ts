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
import {TSRole} from '../../../models/enums/TSRole';
import {TSWizardStepName} from '../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../models/enums/TSWizardStepStatus';
import TSEinkommensverschlechterung from '../../../models/TSEinkommensverschlechterung';
import TSEinkommensverschlechterungInfo from '../../../models/TSEinkommensverschlechterungInfo';
import TSFinanzModel from '../../../models/TSFinanzModel';
import TSGesuch from '../../../models/TSGesuch';
import BerechnungsManager from '../../service/berechnungsManager';
import GesuchModelManager from '../../service/gesuchModelManager';
import WizardStepManager from '../../service/wizardStepManager';
import AbstractGesuchViewController from '../abstractGesuchView';
import IQService = angular.IQService;
import IScope = angular.IScope;
import ITimeoutService = angular.ITimeoutService;

export class EinkommensverschlechterungSteuernViewComponentConfig implements IComponentOptions {
    public transclude = false;
    public template = require('./einkommensverschlechterungSteuernView.html');
    public controller = EinkommensverschlechterungSteuernViewController;
    public controllerAs = 'vm';
}

export class EinkommensverschlechterungSteuernViewController extends AbstractGesuchViewController<TSFinanzModel> {

    public static $inject: string[] = ['GesuchModelManager', 'BerechnungsManager', 'ErrorService',
        'WizardStepManager', '$q', '$scope', '$timeout'];

    public allowedRoles: Array<TSRole>;
    public initialModel: TSFinanzModel;

    public constructor(gesuchModelManager: GesuchModelManager, berechnungsManager: BerechnungsManager,
                       private readonly errorService: ErrorService, wizardStepManager: WizardStepManager,
                       private readonly $q: IQService, $scope: IScope, $timeout: ITimeoutService) {
        super(gesuchModelManager,
            berechnungsManager,
            wizardStepManager,
            $scope,
            TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG,
            $timeout);
        this.model = new TSFinanzModel(this.gesuchModelManager.getBasisjahr(),
            this.gesuchModelManager.isGesuchsteller2Required(),
            null);
        this.model.copyEkvDataFromGesuch(this.gesuchModelManager.getGesuch());
        this.initialModel = angular.copy(this.model);

        this.allowedRoles = this.TSRoleUtil.getAllRolesButTraegerschaftInstitution();
        this.initViewModel();
    }

    private initViewModel(): void {
        // Basis Jahr 1 braucht es nur wenn gewünscht
        if (this.getEinkommensverschlechterungsInfo().ekvFuerBasisJahrPlus1) {
            this.model.initEinkommensverschlechterungContainer(1, 1);
            this.model.initEinkommensverschlechterungContainer(1, 2);
        }

        // Basis Jahr 2 braucht es nur wenn gewünscht
        if (this.getEinkommensverschlechterungsInfo().ekvFuerBasisJahrPlus2) {
            this.model.initEinkommensverschlechterungContainer(2, 1);
            this.model.initEinkommensverschlechterungContainer(2, 2);
        }
    }

    public getEinkommensverschlechterungsInfo(): TSEinkommensverschlechterungInfo {
        return this.model.einkommensverschlechterungInfoContainer.einkommensverschlechterungInfoJA;
    }

    // tslint:disable-next-line:naming-convention
    public showSteuerveranlagung_BjP1(): boolean {
        return this.getEinkommensverschlechterungsInfo().gemeinsameSteuererklaerung_BjP1;
    }

    // tslint:disable-next-line:naming-convention
    public showSteuererklaerung_BjP1(): boolean {
        return !this.isSteuerveranlagungErhaltenGS1_Bjp1();
    }

    // tslint:disable-next-line:naming-convention
    public isSteuerveranlagungErhaltenGS1_Bjp1(): boolean {
        return this.getEkv_GS1_Bjp1() ? this.getEkv_GS1_Bjp1().steuerveranlagungErhalten : false;
    }

    private save(): IPromise<TSGesuch> {
        if (this.isGesuchValid()) {
            if (!this.form.$dirty) {
                // If there are no changes in form we don't need anything to update on Server and we could return the
                // promise immediately
                return this.$q.when(this.gesuchModelManager.getGesuch());
            }
            this.removeNotNeededEKV();
            this.errorService.clearAll();
            this.model.copyEkvSitDataToGesuch(this.gesuchModelManager.getGesuch());
            return this.gesuchModelManager.updateGesuch().then(gesuch => {
                // Noetig, da nur das ganze Gesuch upgedated wird und die Aenderng bei der FinSit sonst nicht bemerkt
                // werden
                if (this.gesuchModelManager.getGesuch().isMutation()
                    && this.wizardStepManager.getCurrentStep().wizardStepStatus !== TSWizardStepStatus.NOK) {
                    // wenn es NOK wir duerfen es erst im letzten Schritt aendern
                    this.wizardStepManager.updateCurrentWizardStepStatusMutiert();
                }
                return gesuch;
            });
        }
        return undefined;
    }

    // tslint:disable-next-line:naming-convention
    public getEkv_GS1_Bjp1(): TSEinkommensverschlechterung {
        return this.model.einkommensverschlechterungContainerGS1.ekvJABasisJahrPlus1;
    }

    // tslint:disable-next-line:naming-convention
    public getEkv_GS2_Bjp1(): TSEinkommensverschlechterung {
        return this.model.einkommensverschlechterungContainerGS2.ekvJABasisJahrPlus1;
    }

    // tslint:disable-next-line:naming-convention
    private gemeinsameStekClicked_BjP1(): void {
        // Wenn neu NEIN -> Fragen loeschen

        const container = this.model.einkommensverschlechterungContainerGS1;
        const ekvJABasisJahrPlus1 = container.ekvJABasisJahrPlus1;
        const ekvJaBasisJahrPlus1WasAlreadyEntered = ekvJABasisJahrPlus1 && !ekvJABasisJahrPlus1.isNew();
        const info = this.getEinkommensverschlechterungsInfo();

        if (!info.gemeinsameSteuererklaerung_BjP1 && ekvJaBasisJahrPlus1WasAlreadyEntered) {
            // Wenn neu NEIN und schon was eingegeben -> Fragen mal auf false setzen und Status auf nok damit man
            // sicher noch weiter muss!
            this.initSteuerFragen();
            this.wizardStepManager.updateCurrentWizardStepStatus(TSWizardStepStatus.NOK);
        } else if (!info.gemeinsameSteuererklaerung_BjP1) {
            // Wenn neu NEIN und noch nichts eingegeben -> Fragen loeschen da noch nichts eingegeben worden ist
            container.ekvJABasisJahrPlus1 = undefined;
            this.model.einkommensverschlechterungContainerGS2.ekvJABasisJahrPlus1 = undefined;
        } else {
            // Wenn neu JA
            this.initViewModel();  // review @gapa fragen ist das nicht ein change genueber vorher
        }
    }

    /**
     * Es muss ein Wert geschrieben werden, um ekv persisierten zu können
     */
    private initSteuerFragen(): void {
        const gs1EkvJABasisJahrPlus1 = this.model.einkommensverschlechterungContainerGS1.ekvJABasisJahrPlus1;
        if (gs1EkvJABasisJahrPlus1) {
            gs1EkvJABasisJahrPlus1.steuererklaerungAusgefuellt = gs1EkvJABasisJahrPlus1.steuererklaerungAusgefuellt ?
                gs1EkvJABasisJahrPlus1.steuererklaerungAusgefuellt :
                false;

            gs1EkvJABasisJahrPlus1.steuerveranlagungErhalten = gs1EkvJABasisJahrPlus1.steuerveranlagungErhalten ?
                gs1EkvJABasisJahrPlus1.steuerveranlagungErhalten :
                false;
        }

        const gs2EkvJABasisJahrPlus1 = this.model.einkommensverschlechterungContainerGS2.ekvJABasisJahrPlus1;
        if (!gs2EkvJABasisJahrPlus1) {
            return;
        }

        gs2EkvJABasisJahrPlus1.steuererklaerungAusgefuellt = gs2EkvJABasisJahrPlus1.steuererklaerungAusgefuellt ?
            gs2EkvJABasisJahrPlus1.steuererklaerungAusgefuellt :
            false;
        gs2EkvJABasisJahrPlus1.steuerveranlagungErhalten = gs2EkvJABasisJahrPlus1.steuerveranlagungErhalten ?
            gs2EkvJABasisJahrPlus1.steuerveranlagungErhalten :
            false;
    }

    private removeNotNeededEKV(): void {

        if (!this.getEinkommensverschlechterungsInfo().gemeinsameSteuererklaerung_BjP1) {
            this.model.einkommensverschlechterungContainerGS1.ekvJABasisJahrPlus1 = undefined;
            this.model.einkommensverschlechterungContainerGS2.ekvJABasisJahrPlus1 = undefined;
        }
    }

    // tslint:disable-next-line:naming-convention
    private steuerveranlagungClicked_BjP1(): void {
        // Wenn Steuerveranlagung JA -> auch StekErhalten -> JA
        // Wenn zusätzlich noch GemeinsameStek -> Dasselbe auch für GS2
        // Wenn Steuerveranlagung erhalten, muss auch STEK ausgefüllt worden sein
        if (this.getEkv_GS1_Bjp1().steuerveranlagungErhalten) {
            this.getEkv_GS1_Bjp1().steuererklaerungAusgefuellt = true;
            if (this.getEinkommensverschlechterungsInfo().gemeinsameSteuererklaerung_BjP1) {
                this.getEkv_GS2_Bjp1().steuerveranlagungErhalten = true;
                this.getEkv_GS2_Bjp1().steuererklaerungAusgefuellt = true;
            }
        } else if (!this.getEkv_GS1_Bjp1().steuerveranlagungErhalten) {
            // Steuerveranlagung neu NEIN -> Fragen loeschen
            this.getEkv_GS1_Bjp1().steuererklaerungAusgefuellt = undefined;
            if (this.getEinkommensverschlechterungsInfo().gemeinsameSteuererklaerung_BjP1) {
                this.getEkv_GS2_Bjp1().steuerveranlagungErhalten = false;
                this.getEkv_GS2_Bjp1().steuererklaerungAusgefuellt = undefined;
            }
        }
    }

    // tslint:disable-next-line:naming-convention
    private steuererklaerungClicked_BjP1(): void {
        if (this.getEinkommensverschlechterungsInfo().gemeinsameSteuererklaerung_BjP1) {
            this.getEkv_GS2_Bjp1().steuererklaerungAusgefuellt = this.getEkv_GS1_Bjp1().steuererklaerungAusgefuellt;
        }
    }

}
