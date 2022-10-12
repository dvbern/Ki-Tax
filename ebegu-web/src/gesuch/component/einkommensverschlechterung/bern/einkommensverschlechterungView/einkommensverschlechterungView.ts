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
import {ErrorService} from '../../../../../app/core/errors/service/ErrorService';
import {TSFinanzielleSituationResultateDTO} from '../../../../../models/dto/TSFinanzielleSituationResultateDTO';
import {TSRole} from '../../../../../models/enums/TSRole';
import {TSWizardStepName} from '../../../../../models/enums/TSWizardStepName';
import {TSEinkommensverschlechterung} from '../../../../../models/TSEinkommensverschlechterung';
import {TSEinkommensverschlechterungContainer} from '../../../../../models/TSEinkommensverschlechterungContainer';
import {TSFinanzModel} from '../../../../../models/TSFinanzModel';
import {EbeguUtil} from '../../../../../utils/EbeguUtil';
import {IEinkommensverschlechterungStateParams} from '../../../../gesuch.route';
import {BerechnungsManager} from '../../../../service/berechnungsManager';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../../service/wizardStepManager';
import {AbstractGesuchViewController} from '../../../abstractGesuchView';
import IScope = angular.IScope;
import ITimeoutService = angular.ITimeoutService;
import ITranslateService = angular.translate.ITranslateService;

export class EinkommensverschlechterungViewComponentConfig implements IComponentOptions {
    public transclude = false;
    public template = require('./einkommensverschlechterungView.html');
    public controller = EinkommensverschlechterungViewController;
    public controllerAs = 'vm';
}

export class EinkommensverschlechterungViewController extends AbstractGesuchViewController<TSFinanzModel> {

    public static $inject: string[] = [
        '$stateParams',
        'GesuchModelManager',
        'BerechnungsManager',
        'ErrorService',
        '$log',
        'WizardStepManager',
        '$q',
        '$scope',
        '$translate',
        '$timeout'
    ];

    public showSelbstaendig: boolean;
    public showSelbstaendigGS: boolean;
    public geschaeftsgewinnBasisjahrMinus1: number;
    public geschaeftsgewinnBasisjahrMinus2: number;
    public geschaeftsgewinnBasisjahrMinus1GS: number;
    public geschaeftsgewinnBasisjahrMinus2GS: number;
    public allowedRoles: ReadonlyArray<TSRole>;
    public initialModel: TSFinanzModel;

    public constructor(
        $stateParams: IEinkommensverschlechterungStateParams,
        gesuchModelManager: GesuchModelManager,
        berechnungsManager: BerechnungsManager,
        private readonly errorService: ErrorService,
        private readonly $log: ILogService,
        wizardStepManager: WizardStepManager,
        private readonly $q: IQService,
        $scope: IScope,
        private readonly $translate: ITranslateService,
        $timeout: ITimeoutService
    ) {
        super(gesuchModelManager,
            berechnungsManager,
            wizardStepManager,
            $scope,
            TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG,
            $timeout);
        const parsedGesuchstelllerNum = parseInt($stateParams.gesuchstellerNumber, 10);
        const parsedBasisJahrPlusNum = parseInt($stateParams.basisjahrPlus, 10);
        this.gesuchModelManager.setGesuchstellerNumber(parsedGesuchstelllerNum);
        this.gesuchModelManager.setBasisJahrPlusNumber(parsedBasisJahrPlusNum);
        this.model = new TSFinanzModel(this.gesuchModelManager.getBasisjahr(),
            this.gesuchModelManager.isGesuchsteller2Required(),
            parsedGesuchstelllerNum,
            parsedBasisJahrPlusNum);
        this.model.copyEkvDataFromGesuch(this.gesuchModelManager.getGesuch());
        this.model.copyFinSitDataFromGesuch(this.gesuchModelManager.getGesuch());
        this.model.initEinkommensverschlechterungContainer(parsedBasisJahrPlusNum, parsedGesuchstelllerNum);
        this.initialModel = angular.copy(this.model);
        this.allowedRoles = this.TSRoleUtil.getAllRolesButTraegerschaftInstitution();
        this.initViewModel();
        this.calculate();

    }

    private initViewModel(): void {
        this.initGeschaeftsgewinnFromFS();
        const fiSiConToWorkWith = this.model.getFiSiConToWorkWith();

        this.showSelbstaendig = fiSiConToWorkWith.finanzielleSituationJA.isSelbstaendig()
            || EbeguUtil.isNotNullOrUndefined(this.model.getEkvToWorkWith().geschaeftsgewinnBasisjahr);

        if (!fiSiConToWorkWith.finanzielleSituationGS || !this.model.getEkvToWorkWith_GS()) {
            return;
        }

        this.showSelbstaendigGS = fiSiConToWorkWith.finanzielleSituationGS.isSelbstaendig()
            || EbeguUtil.isNotNullOrUndefined(this.model.getEkvToWorkWith_GS().geschaeftsgewinnBasisjahr);
    }

    public showSelbstaendigClicked(): void {
        if (!this.showSelbstaendig) {
            this.resetSelbstaendigFields();
        }
    }

    private resetSelbstaendigFields(): void {
        if (this.model.getEkvToWorkWith().geschaeftsgewinnBasisjahr) {
            this.model.getEkvToWorkWith().geschaeftsgewinnBasisjahr = undefined;
            this.calculate();
        }
    }

    public save(): IPromise<TSEinkommensverschlechterungContainer> {
        if (!this.isGesuchValid()) {
            return undefined;
        }

        if (!this.form.$dirty) {
            // If there are no changes in form we don't need anything to update on Server and we could return the
            // promise immediately
            return this.$q.when(this.model.getEkvContToWorkWith());
        }
        this.errorService.clearAll();
        this.model.copyEkvSitDataToGesuch(this.gesuchModelManager.getGesuch());
        return this.gesuchModelManager.saveEinkommensverschlechterungContainer();
    }

    public calculate(): void {
        this.berechnungsManager.calculateEinkommensverschlechterungTemp(this.model, this.model.getBasisJahrPlus());
    }

    public getEinkommensverschlechterung(): TSEinkommensverschlechterung {
        return this.model.getEkvToWorkWith();
    }

    public getResultate(): TSFinanzielleSituationResultateDTO {
        return this.berechnungsManager.getEinkommensverschlechterungResultate(this.model.getBasisJahrPlus());
    }

    // eslint-disable-next-line
    public initGeschaeftsgewinnFromFS(): void {
        if (!this.model.getFiSiConToWorkWith()
            || !this.model.getFiSiConToWorkWith().finanzielleSituationJA) {
            this.$log.error('Fehler: FinSit muss existieren');
            return;
        }

        const fs = this.model.getFiSiConToWorkWith().finanzielleSituationJA;
        const fsGS = this.model.getFiSiConToWorkWith().finanzielleSituationGS;
        if (this.model.getBasisJahrPlus() === 2) {
            // basisjahr Plus 2
            const infoContainer = this.model.einkommensverschlechterungInfoContainer;
            if (infoContainer.einkommensverschlechterungInfoJA.ekvFuerBasisJahrPlus1) {
                const einkommensverschlJABasisjahrPlus1 = this.model.getEkvContToWorkWith().ekvJABasisJahrPlus1;
                this.geschaeftsgewinnBasisjahrMinus1 = einkommensverschlJABasisjahrPlus1 ?
                    einkommensverschlJABasisjahrPlus1.geschaeftsgewinnBasisjahr :
                    undefined;
                const einkommensverschlGSBasisjahrPlus1 = this.model.getEkvContToWorkWith().ekvGSBasisJahrPlus1;
                this.geschaeftsgewinnBasisjahrMinus1GS = einkommensverschlGSBasisjahrPlus1 ?
                    einkommensverschlGSBasisjahrPlus1.geschaeftsgewinnBasisjahr :
                    undefined;
            } else {
                const einkommensverschlGS = this.model.getEkvToWorkWith_GS();
                this.geschaeftsgewinnBasisjahrMinus1GS =
                    einkommensverschlGS ? einkommensverschlGS.geschaeftsgewinnBasisjahrMinus1 : undefined;
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
        const info = this.model.einkommensverschlechterungInfoContainer.einkommensverschlechterungInfoJA;

        return this.model.getBasisJahrPlus() === 2 && !info.ekvFuerBasisJahrPlus1;
    }

    public getTextSelbstaendigKorrektur(): any {
        if (this.showSelbstaendigGS && this.model.getEkvToWorkWith_GS()) {
            const gew1 = this.model.getEkvToWorkWith_GS().geschaeftsgewinnBasisjahr;
            if (gew1) {
                const basisjahr = this.gesuchModelManager.getBasisjahrPlus(this.model.getBasisJahrPlus());
                return this.$translate.instant('JA_KORREKTUR_SELBSTAENDIG_EKV',
                    {basisjahr, gewinn1: gew1});
            }
        }
        return this.$translate.instant('LABEL_KEINE_ANGABE');
    }

    public einkommenInVereinfachtemVerfarenClicked(): void {
        this.model.getEkvToWorkWith().amountEinkommenInVereinfachtemVerfahrenAbgerechnet = null;
    }
}
