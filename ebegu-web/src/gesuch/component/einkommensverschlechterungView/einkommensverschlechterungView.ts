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

import {IComponentOptions, ILogService, IPromise, IQService} from 'angular';
import AbstractGesuchViewController from '../abstractGesuchView';
import GesuchModelManager from '../../service/gesuchModelManager';
import {IEinkommensverschlechterungStateParams} from '../../gesuch.route';
import BerechnungsManager from '../../service/berechnungsManager';
import TSFinanzielleSituationResultateDTO from '../../../models/dto/TSFinanzielleSituationResultateDTO';
import ErrorService from '../../../core/errors/service/ErrorService';
import TSEinkommensverschlechterung from '../../../models/TSEinkommensverschlechterung';
import TSFinanzielleSituation from '../../../models/TSFinanzielleSituation';
import WizardStepManager from '../../service/wizardStepManager';
import TSEinkommensverschlechterungContainer from '../../../models/TSEinkommensverschlechterungContainer';
import {TSRole} from '../../../models/enums/TSRole';
import TSFinanzModel from '../../../models/TSFinanzModel';
import {TSWizardStepName} from '../../../models/enums/TSWizardStepName';
import IScope = angular.IScope;
import ITranslateService = angular.translate.ITranslateService;
import ITimeoutService = angular.ITimeoutService;

const template = require('./einkommensverschlechterungView.html');
require('./einkommensverschlechterungView.less');

export class EinkommensverschlechterungViewComponentConfig implements IComponentOptions {
    transclude = false;
    template = template;
    controller = EinkommensverschlechterungViewController;
    controllerAs = 'vm';
}

export class EinkommensverschlechterungViewController extends AbstractGesuchViewController<TSFinanzModel> {

    static $inject: string[] = ['$stateParams', 'GesuchModelManager', 'BerechnungsManager', 'ErrorService', '$log',
        'WizardStepManager', '$q', '$scope', '$translate', '$timeout'];

    public showSelbstaendig: boolean;
    public showSelbstaendigGS: boolean;
    public geschaeftsgewinnBasisjahrMinus1: number;
    public geschaeftsgewinnBasisjahrMinus2: number;
    public geschaeftsgewinnBasisjahrMinus1GS: number;
    public geschaeftsgewinnBasisjahrMinus2GS: number;
    allowedRoles: Array<TSRole>;
    public initialModel: TSFinanzModel;

    /* @ngInject */
    constructor($stateParams: IEinkommensverschlechterungStateParams, gesuchModelManager: GesuchModelManager,
                berechnungsManager: BerechnungsManager, private readonly errorService: ErrorService, private readonly $log: ILogService,
                wizardStepManager: WizardStepManager, private readonly $q: IQService, $scope: IScope, private readonly $translate: ITranslateService,
                $timeout: ITimeoutService) {
        super(gesuchModelManager, berechnungsManager, wizardStepManager, $scope, TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG, $timeout);
        const parsedGesuchstelllerNum: number = parseInt($stateParams.gesuchstellerNumber, 10);
        const parsedBasisJahrPlusNum: number = parseInt($stateParams.basisjahrPlus, 10);
        this.gesuchModelManager.setGesuchstellerNumber(parsedGesuchstelllerNum);
        this.gesuchModelManager.setBasisJahrPlusNumber(parsedBasisJahrPlusNum);
        this.model = new TSFinanzModel(this.gesuchModelManager.getBasisjahr(), this.gesuchModelManager.isGesuchsteller2Required(), parsedGesuchstelllerNum, parsedBasisJahrPlusNum);
        this.model.copyEkvDataFromGesuch(this.gesuchModelManager.getGesuch());
        this.model.copyFinSitDataFromGesuch(this.gesuchModelManager.getGesuch());
        this.model.initEinkommensverschlechterungContainer(parsedBasisJahrPlusNum, parsedGesuchstelllerNum);
        this.initialModel = angular.copy(this.model);
        this.allowedRoles = this.TSRoleUtil.getAllRolesButTraegerschaftInstitution();
        this.initViewModel();
        this.calculate();

    }

    private initViewModel() {
        this.initGeschaeftsgewinnFromFS();
        this.showSelbstaendig = this.model.getFiSiConToWorkWith().finanzielleSituationJA.isSelbstaendig()
            || (this.model.getEkvToWorkWith().geschaeftsgewinnBasisjahr !== null
                && this.model.getEkvToWorkWith().geschaeftsgewinnBasisjahr !== undefined);
        if (this.model.getFiSiConToWorkWith().finanzielleSituationGS && this.model.getEkvToWorkWith_GS()) {
            this.showSelbstaendigGS = this.model.getFiSiConToWorkWith().finanzielleSituationGS.isSelbstaendig()
                || (this.model.getEkvToWorkWith_GS().geschaeftsgewinnBasisjahr !== null
                    && this.model.getEkvToWorkWith_GS().geschaeftsgewinnBasisjahr !== undefined);
        }
    }

    public showSelbstaendigClicked() {
        if (!this.showSelbstaendig) {
            this.resetSelbstaendigFields();
        }
    }

    private resetSelbstaendigFields() {
        if (this.model.getEkvToWorkWith().geschaeftsgewinnBasisjahr) {
            this.model.getEkvToWorkWith().geschaeftsgewinnBasisjahr = undefined;
            this.calculate();
        }
    }

    /**
     *  Wenn z.B. in der Periode 2016/2017 eine Einkommensverschlechterung für 2017 geltend gemacht wird,
     *  ist es unmöglich, dass die Steuerveranlagung und Steuererklärung für 2017 schon dem Gesuchsteller vorliegt
     */
    showSteuerveranlagung(): boolean {
        return (this.model.getBasisJahrPlus() === 1) &&
            (!this.model.getGemeinsameSteuererklaerungToWorkWith() || this.model.getGemeinsameSteuererklaerungToWorkWith() === false);
    }

    showSteuererklaerung(): boolean {
        return this.model.getEkvToWorkWith().steuerveranlagungErhalten === false;
    }

    showHintSteuererklaerung(): boolean {
        return this.model.getEkvToWorkWith().steuererklaerungAusgefuellt === true &&
            this.model.getEkvToWorkWith().steuerveranlagungErhalten === false;
    }

    showHintSteuerveranlagung(): boolean {
        return this.model.getEkvToWorkWith().steuerveranlagungErhalten === true;
    }

    steuerveranlagungClicked(): void {
        // Wenn Steuerveranlagung JA -> auch StekErhalten -> JA
        if (this.model.getEkvToWorkWith().steuerveranlagungErhalten === true) {
            this.model.getEkvToWorkWith().steuererklaerungAusgefuellt = true;
        } else if (this.model.getEkvToWorkWith().steuerveranlagungErhalten === false) {
            // Steuerveranlagung neu NEIN -> Fragen loeschen
            this.model.getEkvToWorkWith().steuererklaerungAusgefuellt = undefined;
        }
    }

    private save(): IPromise<TSEinkommensverschlechterungContainer> {
        if (this.isGesuchValid()) {
            if (!this.form.$dirty) {
                // If there are no changes in form we don't need anything to update on Server and we could return the
                // promise immediately
                return this.$q.when(this.model.getEkvContToWorkWith());
            }
            this.errorService.clearAll();
            this.model.copyEkvSitDataToGesuch(this.gesuchModelManager.getGesuch());
            return this.gesuchModelManager.saveEinkommensverschlechterungContainer();
        }
        return undefined;
    }

    calculate() {
        this.berechnungsManager.calculateEinkommensverschlechterungTemp(this.model, this.model.getBasisJahrPlus());
    }

    public getEinkommensverschlechterung(): TSEinkommensverschlechterung {
        return this.model.getEkvToWorkWith();
    }

    public getResultate(): TSFinanzielleSituationResultateDTO {
        return this.berechnungsManager.getEinkommensverschlechterungResultate(this.model.getBasisJahrPlus());
    }

    public initGeschaeftsgewinnFromFS(): void {
        if (!this.model.getFiSiConToWorkWith()
            || !this.model.getFiSiConToWorkWith().finanzielleSituationJA) {
            this.$log.error('Fehler: FinSit muss existieren');
            return;
        }

        const fs: TSFinanzielleSituation = this.model.getFiSiConToWorkWith().finanzielleSituationJA;
        const fsGS: TSFinanzielleSituation = this.model.getFiSiConToWorkWith().finanzielleSituationGS;
        if (this.model.getBasisJahrPlus() === 2) {
            //basisjahr Plus 2
            if (this.model.einkommensverschlechterungInfoContainer.einkommensverschlechterungInfoJA.ekvFuerBasisJahrPlus1) {
                const einkommensverschlJABasisjahrPlus1 = this.model.getEkvContToWorkWith().ekvJABasisJahrPlus1;
                this.geschaeftsgewinnBasisjahrMinus1 = einkommensverschlJABasisjahrPlus1 ? einkommensverschlJABasisjahrPlus1.geschaeftsgewinnBasisjahr : undefined;
                const einkommensverschlGSBasisjahrPlus1 = this.model.getEkvContToWorkWith().ekvGSBasisJahrPlus1;
                this.geschaeftsgewinnBasisjahrMinus1GS = einkommensverschlGSBasisjahrPlus1 ? einkommensverschlGSBasisjahrPlus1.geschaeftsgewinnBasisjahr : undefined;
            } else {
                const einkommensverschlGS = this.model.getEkvToWorkWith_GS();
                this.geschaeftsgewinnBasisjahrMinus1GS = einkommensverschlGS ? einkommensverschlGS.geschaeftsgewinnBasisjahrMinus1 : undefined;
            }

            this.geschaeftsgewinnBasisjahrMinus2 = fs.geschaeftsgewinnBasisjahr;
            this.geschaeftsgewinnBasisjahrMinus2GS = fsGS ? fsGS.geschaeftsgewinnBasisjahr : undefined;
        } else {
            this.geschaeftsgewinnBasisjahrMinus1 = fs.geschaeftsgewinnBasisjahr;
            this.geschaeftsgewinnBasisjahrMinus2 = fs.geschaeftsgewinnBasisjahrMinus1;
            this.geschaeftsgewinnBasisjahrMinus1GS = fsGS ? fsGS.geschaeftsgewinnBasisjahr : undefined;
            this.geschaeftsgewinnBasisjahrMinus2GS = fsGS ? fsGS.geschaeftsgewinnBasisjahrMinus1 : undefined;
        }
    }

    public enableGeschaeftsgewinnBasisjahrMinus1(): boolean {
        return this.model.getBasisJahrPlus() === 2 && !this.model.einkommensverschlechterungInfoContainer.einkommensverschlechterungInfoJA.ekvFuerBasisJahrPlus1;
    }

    public getTextSelbstaendigKorrektur() {
        if (this.showSelbstaendigGS === true && this.model.getEkvToWorkWith_GS()) {
            const gew1 = this.model.getEkvToWorkWith_GS().geschaeftsgewinnBasisjahr;
            if (gew1) {
                const basisjahr = this.gesuchModelManager.getBasisjahrPlus(this.model.getBasisJahrPlus());
                return this.$translate.instant('JA_KORREKTUR_SELBSTAENDIG_EKV',
                    {basisjahr: basisjahr, gewinn1: gew1});
            }
        }
        return this.$translate.instant('LABEL_KEINE_ANGABE');
    }
}
