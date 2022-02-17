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
import {EinstellungRS} from '../../../../../admin/service/einstellungRS.rest';
import {DvDialog} from '../../../../../app/core/directive/dv-dialog/dv-dialog';
import {ErrorService} from '../../../../../app/core/errors/service/ErrorService';
import {TSFinanzielleSituationResultateDTO} from '../../../../../models/dto/TSFinanzielleSituationResultateDTO';
import {TSEinstellungKey} from '../../../../../models/enums/TSEinstellungKey';
import {TSFinanzielleSituationSubStepName} from '../../../../../models/enums/TSFinanzielleSituationSubStepName';
import {TSFinanzielleSituationTyp} from '../../../../../models/enums/TSFinanzielleSituationTyp';
import {TSRole} from '../../../../../models/enums/TSRole';
import {
    isSteuerdatenAnfrageStatusErfolgreich,
    TSSteuerdatenAnfrageStatus
} from '../../../../../models/enums/TSSteuerdatenAnfrageStatus';
import {TSWizardStepName} from '../../../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../../../models/enums/TSWizardStepStatus';
import {TSFinanzielleSituationContainer} from '../../../../../models/TSFinanzielleSituationContainer';
import {TSFinanzModel} from '../../../../../models/TSFinanzModel';
import {EbeguUtil} from '../../../../../utils/EbeguUtil';
import {FinanzielleSituationAufteilungDialogController} from '../../../../dialog/FinanzielleSituationAufteilungDialogController';
import {IStammdatenStateParams} from '../../../../gesuch.route';
import {BerechnungsManager} from '../../../../service/berechnungsManager';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../../service/wizardStepManager';
import {AbstractFinSitBernView} from '../AbstractFinSitBernView';
import IPromise = angular.IPromise;
import IQService = angular.IQService;
import IScope = angular.IScope;
import ITimeoutService = angular.ITimeoutService;
import ITranslateService = angular.translate.ITranslateService;

const aufteilungDialogTemplate = require('../../../../dialog/finanzielleSituationAufteilungDialogTemplate.html');

export class FinanzielleSituationViewComponentConfig implements IComponentOptions {
    public transclude = false;
    public template = require('./finanzielleSituationView.html');
    public controller = FinanzielleSituationViewController;
    public controllerAs = 'vm';
}

export class FinanzielleSituationViewController extends AbstractFinSitBernView {

    public static $inject: string[] = [
        '$stateParams',
        'GesuchModelManager',
        'BerechnungsManager',
        'ErrorService',
        'WizardStepManager',
        '$q',
        '$scope',
        '$translate',
        '$timeout',
        'EinstellungRS',
        'DvDialog'
    ];

    public showSelbstaendig: boolean;
    public showSelbstaendigGS: boolean;
    public allowedRoles: ReadonlyArray<TSRole>;
    private steuerSchnittstelleAktiv: boolean;
    public showForm: boolean;

    public constructor(
        $stateParams: IStammdatenStateParams,
        gesuchModelManager: GesuchModelManager,
        berechnungsManager: BerechnungsManager,
        private readonly errorService: ErrorService,
        wizardStepManager: WizardStepManager,
        private readonly $q: IQService,
        $scope: IScope,
        private readonly $translate: ITranslateService,
        $timeout: ITimeoutService,
        private readonly settings: EinstellungRS,
        private readonly dvDialog: DvDialog,
    ) {
        super(gesuchModelManager,
            berechnungsManager,
            wizardStepManager,
            $scope,
            $timeout);
        let parsedNum = parseInt($stateParams.gesuchstellerNumber, 10);
        if (!parsedNum) {
            parsedNum = 1;
        }
        this.allowedRoles = this.TSRoleUtil.getAllRolesButTraegerschaftInstitution();
        this.model = new TSFinanzModel(this.gesuchModelManager.getBasisjahr(),
            this.gesuchModelManager.isGesuchsteller2Required(),
            parsedNum);
        this.model.copyFinSitDataFromGesuch(this.gesuchModelManager.getGesuch());
        this.gesuchModelManager.setGesuchstellerNumber(parsedNum);
        this.initViewModel();
        this.calculate();
    }

    private initViewModel(): void {
        this.wizardStepManager.updateCurrentWizardStepStatusSafe(
            TSWizardStepName.FINANZIELLE_SITUATION,
            TSWizardStepStatus.IN_BEARBEITUNG);
        this.initSelbstaendigkeit();

        this.settings.findEinstellung(TSEinstellungKey.SCHNITTSTELLE_STEUERN_AKTIV,
            this.gesuchModelManager.getGemeinde()?.id,
            this.gesuchModelManager.getGesuchsperiode()?.id)
            .then(setting => {
                this.steuerSchnittstelleAktiv = (setting.value === 'true');
                this.showFormular();
            });
    }

    public showSelbstaendigClicked(): void {
        if (!this.showSelbstaendig) {
            this.resetSelbstaendigFields();
        }
    }

    private initSelbstaendigkeit(): void {
        this.showSelbstaendig = this.model.getFiSiConToWorkWith().finanzielleSituationJA.isSelbstaendig();
        this.showSelbstaendigGS = this.model.getFiSiConToWorkWith().finanzielleSituationGS
            ? this.model.getFiSiConToWorkWith().finanzielleSituationGS.isSelbstaendig() : false;
    }

    private resetSelbstaendigFields(): void {
        if (!this.model.getFiSiConToWorkWith()) {
            return;
        }

        this.model.getFiSiConToWorkWith().finanzielleSituationJA.geschaeftsgewinnBasisjahr = undefined;
        this.model.getFiSiConToWorkWith().finanzielleSituationJA.geschaeftsgewinnBasisjahrMinus1 = undefined;
        this.model.getFiSiConToWorkWith().finanzielleSituationJA.geschaeftsgewinnBasisjahrMinus2 = undefined;
        this.calculate();
    }

    public showSteuerveranlagung(): boolean {
        return !this.model.gemeinsameSteuererklaerung;
    }

    public showSteuererklaerung(): boolean {
        return !this.model.getFiSiConToWorkWith().finanzielleSituationJA.steuerveranlagungErhalten;
    }

    public showZugriffAufSteuerdaten(): boolean {
        if (!this.steuerSchnittstelleAktiv) {
            return false;
        }

        return (this.model.getFiSiConToWorkWith().finanzielleSituationJA.steuerveranlagungErhalten ||
            this.model.getFiSiConToWorkWith().finanzielleSituationJA.steuererklaerungAusgefuellt) &&
            this.gesuchModelManager.getGesuch().isOnlineGesuch() && !this.model.gemeinsameSteuererklaerung;
    }

    public save(): IPromise<TSFinanzielleSituationContainer> {
        if (!this.isGesuchValid()) {
            return undefined;
        }
        this.model.copyFinSitDataToGesuch(this.gesuchModelManager.getGesuch());
        const finanzielleSituationContainer =
            this.gesuchModelManager.getStammdatenToWorkWith().finanzielleSituationContainer;
        // Auf der Finanziellen Situation ist nichts zwingend. Zumindest das erste Mal müssen wir daher auch
        // Speichern, wenn das Form nicht dirty ist!
        if (!this.form.$dirty && !finanzielleSituationContainer.isNew()) {
            // If there are no changes in form we don't need anything to update on Server and we could return the
            // promise immediately
            return this.$q.when(finanzielleSituationContainer);
        }
        this.errorService.clearAll();
        return this.gesuchModelManager.saveFinanzielleSituation();
    }

    public calculate(): void {
        this.berechnungsManager.calculateFinanzielleSituationTemp(this.model);
    }

    public resetForm(): void {
        this.initViewModel();
    }

    public getResultate(): TSFinanzielleSituationResultateDTO {
        return this.berechnungsManager.finanzielleSituationResultate;
    }

    public getTextSelbstaendigKorrektur(): string {
        const finSitGS = this.getModel().finanzielleSituationGS;
        if (!finSitGS || !finSitGS.isSelbstaendig()) {
            return this.$translate.instant('LABEL_KEINE_ANGABE');
        }

        const gew1 = finSitGS.geschaeftsgewinnBasisjahr;
        const gew2 = finSitGS.geschaeftsgewinnBasisjahrMinus1;
        const gew3 = finSitGS.geschaeftsgewinnBasisjahrMinus2;
        const basisjahr = this.gesuchModelManager.getBasisjahr();
        const params = {basisjahr, gewinn1: gew1, gewinn2: gew2, gewinn3: gew3};

        return this.$translate.instant('JA_KORREKTUR_SELBSTAENDIG', params);
    }

    public subStepName(): TSFinanzielleSituationSubStepName {
        return this.gesuchModelManager.gesuchstellerNumber === 2 ?
            TSFinanzielleSituationSubStepName.BERN_GS2 :
            TSFinanzielleSituationSubStepName.BERN_GS1;
    }

    public steuererklaerungClicked(): void {
        this.showFormular();
    }

    public steuerdatenzugriffClicked(): void {
        if (this.getModel().finanzielleSituationJA.steuerdatenZugriff) {
            this.callKiBonAnfrageAndUpdateFinSit();
        } else {
            this.showFormular();
        }
    }

    private callKiBonAnfrageAndUpdateFinSit(): void {
        this.model.copyFinSitDataToGesuch(this.gesuchModelManager.getGesuch());
        this.gesuchModelManager.callKiBonAnfrageAndUpdateFinSit(false).then(() => {
                this.model.copyFinSitDataFromGesuch(this.gesuchModelManager.getGesuch());
                this.initSelbstaendigkeit();
                this.showFormular();
            },
        );
    }

    public showFormular(): void {
        if (this.steuerSchnittstelleAktiv
            && this.gesuchModelManager.getGesuch().isOnlineGesuch()
            && this.model.getFiSiConToWorkWith().finanzielleSituationJA.steuererklaerungAusgefuellt
            && (EbeguUtil.isNullOrUndefined(this.getModel().finanzielleSituationJA.steuerdatenZugriff))
            && !this.model.gemeinsameSteuererklaerung
        ) {
            this.showForm = false;
            return;
        }
        this.showForm = true;
    }

    public isSteueranfrageErfolgreich(): boolean {
        return this.steuerSchnittstelleAktiv
            && EbeguUtil.isNotNullAndTrue(this.getModel().finanzielleSituationJA.steuerdatenZugriff)
            && isSteuerdatenAnfrageStatusErfolgreich(this.getModel().finanzielleSituationJA.steuerdatenAbfrageStatus);
    }

    public isFKJV(): boolean {
        return this.getGesuch().finSitTyp === TSFinanzielleSituationTyp.BERN_FKJV;
    }

    public showWarningPartnerNichtGemeinsam(): boolean {
        return this.getModel().finanzielleSituationJA.steuerdatenAbfrageStatus === TSSteuerdatenAnfrageStatus.FAILED_PARTNER_NICHT_GEMEINSAM
            && this.getModel().finanzielleSituationJA.steuerdatenZugriff;
    }

    public openDialog(): void {
        this.dvDialog.showDialog(aufteilungDialogTemplate, FinanzielleSituationAufteilungDialogController);
    }
}
