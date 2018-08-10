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
import ErrorService from '../../../app/core/errors/service/ErrorService';
import TSFinanzielleSituationResultateDTO from '../../../models/dto/TSFinanzielleSituationResultateDTO';
import {TSRole} from '../../../models/enums/TSRole';
import {TSWizardStepName} from '../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../models/enums/TSWizardStepStatus';
import TSFinanzielleSituationContainer from '../../../models/TSFinanzielleSituationContainer';
import TSFinanzModel from '../../../models/TSFinanzModel';
import {IStammdatenStateParams} from '../../gesuch.route';
import BerechnungsManager from '../../service/berechnungsManager';
import GesuchModelManager from '../../service/gesuchModelManager';
import WizardStepManager from '../../service/wizardStepManager';
import AbstractGesuchViewController from '../abstractGesuchView';
import IPromise = angular.IPromise;
import IQService = angular.IQService;
import IScope = angular.IScope;
import ITimeoutService = angular.ITimeoutService;
import ITranslateService = angular.translate.ITranslateService;

export class FinanzielleSituationViewComponentConfig implements IComponentOptions {
    transclude = false;
    template = require('./finanzielleSituationView.html');
    controller = FinanzielleSituationViewController;
    controllerAs = 'vm';
}

export class FinanzielleSituationViewController extends AbstractGesuchViewController<TSFinanzModel> {

    static $inject: string[] = ['$stateParams', 'GesuchModelManager', 'BerechnungsManager', 'ErrorService',
        'WizardStepManager', '$q', '$scope', '$translate', '$timeout'];

    public showSelbstaendig: boolean;
    public showSelbstaendigGS: boolean;
    allowedRoles: Array<TSRole>;

    private readonly initialModel: TSFinanzModel;

    constructor($stateParams: IStammdatenStateParams, gesuchModelManager: GesuchModelManager,
                berechnungsManager: BerechnungsManager, private readonly errorService: ErrorService,
                wizardStepManager: WizardStepManager, private readonly $q: IQService, $scope: IScope, private readonly $translate: ITranslateService, $timeout: ITimeoutService) {
        super(gesuchModelManager, berechnungsManager, wizardStepManager, $scope, TSWizardStepName.FINANZIELLE_SITUATION, $timeout);
        let parsedNum: number = parseInt($stateParams.gesuchstellerNumber, 10);
        if (!parsedNum) {
            parsedNum = 1;
        }
        this.allowedRoles = this.TSRoleUtil.getAllRolesButTraegerschaftInstitution();
        this.model = new TSFinanzModel(this.gesuchModelManager.getBasisjahr(), this.gesuchModelManager.isGesuchsteller2Required(), parsedNum);
        this.model.copyFinSitDataFromGesuch(this.gesuchModelManager.getGesuch());
        this.initialModel = angular.copy(this.model);
        this.gesuchModelManager.setGesuchstellerNumber(parsedNum);
        this.initViewModel();
        this.calculate();
    }

    private initViewModel() {
        this.wizardStepManager.updateCurrentWizardStepStatus(TSWizardStepStatus.IN_BEARBEITUNG);
        this.showSelbstaendig = this.model.getFiSiConToWorkWith().finanzielleSituationJA.isSelbstaendig();
        this.showSelbstaendigGS = this.model.getFiSiConToWorkWith().finanzielleSituationGS
            ? this.model.getFiSiConToWorkWith().finanzielleSituationGS.isSelbstaendig() : false;
    }

    public showSelbstaendigClicked() {
        if (!this.showSelbstaendig) {
            this.resetSelbstaendigFields();
        }
    }

    private resetSelbstaendigFields() {
        if (this.model.getFiSiConToWorkWith()) {
            this.model.getFiSiConToWorkWith().finanzielleSituationJA.geschaeftsgewinnBasisjahr = undefined;
            this.model.getFiSiConToWorkWith().finanzielleSituationJA.geschaeftsgewinnBasisjahrMinus1 = undefined;
            this.model.getFiSiConToWorkWith().finanzielleSituationJA.geschaeftsgewinnBasisjahrMinus2 = undefined;
            this.calculate();
        }
    }

    showSteuerveranlagung(): boolean {
        return !this.model.gemeinsameSteuererklaerung;
    }

    showSteuererklaerung(): boolean {
        return this.model.getFiSiConToWorkWith().finanzielleSituationJA.steuerveranlagungErhalten === false;
    }

    //hier neu init
    public steuerveranlagungClicked(): void {
        // Wenn Steuerveranlagung JA -> auch StekErhalten -> JA
        // Wenn zusätzlich noch GemeinsameStek -> Dasselbe auch für GS2
        // Wenn Steuerveranlagung erhalten, muss auch STEK ausgefüllt worden sein
        if (this.model.finanzielleSituationContainerGS1.finanzielleSituationJA.steuerveranlagungErhalten === true) {
            this.model.finanzielleSituationContainerGS1.finanzielleSituationJA.steuererklaerungAusgefuellt = true;
            if (this.model.gemeinsameSteuererklaerung === true) {
                this.model.finanzielleSituationContainerGS2.finanzielleSituationJA.steuerveranlagungErhalten = true;
                this.model.finanzielleSituationContainerGS2.finanzielleSituationJA.steuererklaerungAusgefuellt = true;
            }
        } else if (this.model.finanzielleSituationContainerGS1.finanzielleSituationJA.steuerveranlagungErhalten === false) {
            // Steuerveranlagung neu NEIN -> Fragen loeschen
            this.model.finanzielleSituationContainerGS1.finanzielleSituationJA.steuererklaerungAusgefuellt = undefined;
            if (this.model.gemeinsameSteuererklaerung === true) {
                this.model.finanzielleSituationContainerGS2.finanzielleSituationJA.steuerveranlagungErhalten = false;
                this.model.finanzielleSituationContainerGS2.finanzielleSituationJA.steuererklaerungAusgefuellt = undefined;
            }
        }
    }

    private save(): IPromise<TSFinanzielleSituationContainer> {
        if (this.isGesuchValid()) {
            this.model.copyFinSitDataToGesuch(this.gesuchModelManager.getGesuch());
            if (!this.form.$dirty) {
                // If there are no changes in form we don't need anything to update on Server and we could return the
                // promise immediately
                return this.$q.when(this.gesuchModelManager.getStammdatenToWorkWith().finanzielleSituationContainer);
            }
            this.errorService.clearAll();
            return this.gesuchModelManager.saveFinanzielleSituation();
        }
        return undefined;
    }

    calculate() {
        this.berechnungsManager.calculateFinanzielleSituationTemp(this.model);
    }

    resetForm() {
        this.initViewModel();
    }

    public getModel(): TSFinanzielleSituationContainer {
        return this.model.getFiSiConToWorkWith();
    }

    public getResultate(): TSFinanzielleSituationResultateDTO {
        return this.berechnungsManager.finanzielleSituationResultate;
    }

    public getTextSelbstaendigKorrektur() {
        const finSitGS = this.getModel().finanzielleSituationGS;
        if (finSitGS && finSitGS.isSelbstaendig()) {

            const gew1 = finSitGS.geschaeftsgewinnBasisjahr;
            const gew2 = finSitGS.geschaeftsgewinnBasisjahrMinus1;
            const gew3 = finSitGS.geschaeftsgewinnBasisjahrMinus2;
            const basisjahr = this.gesuchModelManager.getBasisjahr();
            return this.$translate.instant('JA_KORREKTUR_SELBSTAENDIG',
                {basisjahr: basisjahr, gewinn1: gew1, gewinn2: gew2, gewinn3: gew3});

            // return this.$translate.instant('JA_KORREKTUR_FACHSTELLE', {
            //     name: fachstelle.fachstelle.name,
            //     pensum: fachstelle.pensum,
            //     von: vonText,
            //     bis: bisText});
        } else {
            return this.$translate.instant('LABEL_KEINE_ANGABE');
        }

    }

    /**
     * Mindestens einer aller Felder von Geschaftsgewinn muss ausgefuellt sein. Mit dieser Methode kann man es pruefen.
     * @returns {boolean}
     */
    public isGeschaeftsgewinnRequired(): boolean {
        return (this.getModel().finanzielleSituationJA.geschaeftsgewinnBasisjahr === null || this.getModel().finanzielleSituationJA.geschaeftsgewinnBasisjahr === undefined)
            && (this.getModel().finanzielleSituationJA.geschaeftsgewinnBasisjahrMinus1 === null || this.getModel().finanzielleSituationJA.geschaeftsgewinnBasisjahrMinus1 === undefined)
            && (this.getModel().finanzielleSituationJA.geschaeftsgewinnBasisjahrMinus2 === null || this.getModel().finanzielleSituationJA.geschaeftsgewinnBasisjahrMinus2 === undefined);
    }
}
