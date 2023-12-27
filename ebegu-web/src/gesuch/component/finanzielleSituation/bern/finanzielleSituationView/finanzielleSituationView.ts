/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import {IComponentOptions} from 'angular';
import {EinstellungRS} from '../../../../../admin/service/einstellungRS.rest';
import {DvDialog} from '../../../../../app/core/directive/dv-dialog/dv-dialog';
import {ErrorService} from '../../../../../app/core/errors/service/ErrorService';
import {LogFactory} from '../../../../../app/core/logging/LogFactory';
import {ApplicationPropertyRS} from '../../../../../app/core/rest-services/applicationPropertyRS.rest';
import {DemoFeatureRS} from '../../../../../app/core/service/demoFeatureRS.rest';
import {AuthServiceRS} from '../../../../../authentication/service/AuthServiceRS.rest';
import {TSFinanzielleSituationResultateDTO} from '../../../../../models/dto/TSFinanzielleSituationResultateDTO';
import {TSEinstellungKey} from '../../../../../models/enums/TSEinstellungKey';
import {TSFinanzielleSituationSubStepName} from '../../../../../models/enums/TSFinanzielleSituationSubStepName';
import {TSRole} from '../../../../../models/enums/TSRole';
import {isSteuerdatenAnfrageStatusErfolgreich} from '../../../../../models/enums/TSSteuerdatenAnfrageStatus';
import {TSWizardStepName} from '../../../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../../../models/enums/TSWizardStepStatus';
import {TSFinanzielleSituation} from '../../../../../models/TSFinanzielleSituation';
import {TSFinanzielleSituationContainer} from '../../../../../models/TSFinanzielleSituationContainer';
import {TSFinanzModel} from '../../../../../models/TSFinanzModel';
import {EbeguUtil} from '../../../../../utils/EbeguUtil';
import {FinanzielleSituationAufteilungDialogController} from '../../../../dialog/FinanzielleSituationAufteilungDialogController';
import {IStammdatenStateParams} from '../../../../gesuch.route';
import {BerechnungsManager} from '../../../../service/berechnungsManager';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';
import {GesuchRS} from '../../../../service/gesuchRS.rest';
import {WizardStepManager} from '../../../../service/wizardStepManager';
import {AbstractFinSitBernView} from '../AbstractFinSitBernView';
import IPromise = angular.IPromise;
import IQService = angular.IQService;
import IScope = angular.IScope;
import ITimeoutService = angular.ITimeoutService;
import ITranslateService = angular.translate.ITranslateService;

const aufteilungDialogTemplate = require('../../../../dialog/finanzielleSituationAufteilungDialogTemplate.html');

const LOG = LogFactory.createLog('FinanzielleSituationViewController');
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
        'DvDialog',
        'AuthServiceRS',
        'ApplicationPropertyRS',
        'GesuchRS',
        'DemoFeatureRS'
    ];

    public showSelbstaendig: boolean;
    public showSelbstaendigGS: boolean;
    public ersatzeinkommenSelbststaendigkeitActivated: boolean;
    public showErsatzeinkommenSelbststaendigkeit: boolean;
    public showErsatzeinkommenSelbststaendigkeitGS: boolean;
    public allowedRoles: ReadonlyArray<TSRole>;
    private readonly $stateParams: IStammdatenStateParams;
    private triedSavingWithoutForm: boolean = false;

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
        einstellungRS: EinstellungRS,
        dvDialog: DvDialog,
        protected readonly authServiceRS: AuthServiceRS,
        applicationPropertyRS: ApplicationPropertyRS,
        private readonly gesuchRS: GesuchRS,
        private readonly demoFeatureRS: DemoFeatureRS
    ) {
        super(gesuchModelManager,
            berechnungsManager,
            wizardStepManager,
            $scope,
            $timeout,
            authServiceRS,
            einstellungRS,
            dvDialog,
            applicationPropertyRS);
        this.$stateParams = $stateParams;
        this.copyDataAndInit();
    }

    private copyDataAndInit(): void {
        let parsedNum = parseInt(this.$stateParams.gesuchstellerNumber, 10);
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
        this.initFinSitVorMutation();
    }

    private async initFinSitVorMutation(): Promise<void> {
        // beim Erstgesuch macht dies keinen Sinn
        if (EbeguUtil.isNullOrUndefined(this.getGesuch().vorgaengerId)) {
            return;
        }
        const gesuchVorMutation = await this.gesuchRS.findVorgaengerGesuchNotIgnoriert(this.getGesuch().vorgaengerId);
        this.model.initFinSitVorMutation(gesuchVorMutation);
    }

    private initViewModel(): void {
        this.wizardStepManager.updateCurrentWizardStepStatusSafe(
            TSWizardStepName.FINANZIELLE_SITUATION,
            TSWizardStepStatus.IN_BEARBEITUNG);
        this.initEinstellungen().then(() => {
            this.initSelbstaendigkeit();
            this.initErsatzeinkommenSelbststaendigkeit();
        });
    }

    public showSelbstaendigClicked(): void {
        if (!this.showSelbstaendig) {
            this.resetSelbstaendigFields();
        }
    }

    public showErsatzeinkommenSelbststaendigkeitClicked(): void {
        if (!this.showErsatzeinkommenSelbststaendigkeit) {
            this.resetErsatzeinkommenSelbststaendigkeitFields();
        } else if (this.getModel().finanzielleSituationJA.ersatzeinkommen === 0) {
            this.getModel().finanzielleSituationJA.ersatzeinkommenBasisjahr = 0;
        }
    }

    private initSelbstaendigkeit(): void {
        this.showSelbstaendig = this.model.getFiSiConToWorkWith().finanzielleSituationJA.isSelbstaendig();
        this.showSelbstaendigGS = this.model.getFiSiConToWorkWith().finanzielleSituationGS
            ? this.model.getFiSiConToWorkWith().finanzielleSituationGS.isSelbstaendig() : false;

    }

    private initErsatzeinkommenSelbststaendigkeit(): void {
        this.showErsatzeinkommenSelbststaendigkeit =
            this.model.getFiSiConToWorkWith().finanzielleSituationJA.hasErsatzeinkommenSelbststaendigkeit();
        this.showErsatzeinkommenSelbststaendigkeitGS = this.model.getFiSiConToWorkWith().finanzielleSituationGS ?
            this.model.getFiSiConToWorkWith().finanzielleSituationGS.hasErsatzeinkommenSelbststaendigkeit() :
            false;
    }

    private resetSelbstaendigFields(): void {
        if (!this.model.getFiSiConToWorkWith()) {
            return;
        }

        this.model.getFiSiConToWorkWith().finanzielleSituationJA.geschaeftsgewinnBasisjahr = undefined;
        this.model.getFiSiConToWorkWith().finanzielleSituationJA.geschaeftsgewinnBasisjahrMinus1 = undefined;
        this.model.getFiSiConToWorkWith().finanzielleSituationJA.geschaeftsgewinnBasisjahrMinus2 = undefined;
        this.resetErsatzeinkommenSelbststaendigkeitFields();
        this.calculate();
    }

    private resetErsatzeinkommenSelbststaendigkeitFields(): void {
        if (!this.model.getFiSiConToWorkWith()) {
            return;
        }

        this.model.getFiSiConToWorkWith().finanzielleSituationJA.ersatzeinkommenBasisjahr = undefined;
        this.model.getFiSiConToWorkWith().finanzielleSituationJA.ersatzeinkommenBasisjahrMinus1 = undefined;
        this.model.getFiSiConToWorkWith().finanzielleSituationJA.ersatzeinkommenBasisjahrMinus2 = undefined;
        this.initErsatzeinkommenSelbststaendigkeit();
        this.calculate();
    }

    public showSteuerveranlagung(): boolean {
        // falls die Einstellung noch nicht geladen ist, zeigen wir die Fragen noch nicht
        if (EbeguUtil.isNullOrUndefined(this.steuerSchnittstelleAktivForPeriode)) {
            return false;
        }
        // bei gemeinsamer Steuererklärung wird die Frage immer auf der StartView gezeigt
        if (this.model.familienSituation.gemeinsameSteuererklaerung) {
            return false;
        }

        if (super.showZugriffAufSteuerdatenForGemeinde()) {
            return false;
        }

        // bei einem Papiergesuch muss man es anzeigen, die Steuerdatenzugriff Frage ist nicht gestellt
        if (!this.gesuchModelManager.getGesuch().isOnlineGesuch()
            || (!this.authServiceRS.isRole(TSRole.GESUCHSTELLER)
                && EbeguUtil.isNullOrUndefined(this.model.getFiSiConToWorkWith().finanzielleSituationGS))) {
            return true;
        }
        // falls steuerschnittstelle aktiv, aber zugriffserlaubnis noch nicht beantwortet, dann zeigen wir die Frage
        // nicht
        if (this.steuerSchnittstelleAktivForPeriode &&
            EbeguUtil.isNullOrUndefined(this.getModel().finanzielleSituationJA.steuerdatenZugriff)) {
            return false;
        }
        // falls Zugriffserlaubnis nicht gegeben, dann zeigen wir die Frage
        if (!this.getModel().finanzielleSituationJA.steuerdatenZugriff) {
            return true;
        }
        // falls Abfrage noch nicht erfolgt ist, zeigen wir die Frage nicht
        if (EbeguUtil.isNullOrUndefined(this.getModel().finanzielleSituationJA.steuerdatenAbfrageStatus)) {
            return false;
        }
        // falls Steuerabfrage nicht erfolgreich, zeigen wir die Frage ebenfalls
        return !isSteuerdatenAnfrageStatusErfolgreich(this.getModel().finanzielleSituationJA.steuerdatenAbfrageStatus);
    }

    public showSteuererklaerung(): boolean {
        return !this.model.getFiSiConToWorkWith().finanzielleSituationJA.steuerveranlagungErhalten;
    }

    public showZugriffAufSteuerdaten(): boolean {
        return super.showZugriffAufSteuerdaten() && !this.model.familienSituation.gemeinsameSteuererklaerung;
    }

    public save(): IPromise<TSFinanzielleSituationContainer> {
        if (!this.isGesuchValid()) {
            return undefined;
        }
        if (!this.isAtLeastOneErsatzeinkommenSelbststaendigkeitProvided() || !this.isErsatzeinkommenValid()) {
            this.scrollToErsatzeinkommenSelbststaendigkeit();
            return undefined;
        }
        // speichern darf nicht möglich sein, wenn das Formular nicht sichtbar ist
        if (this.showSteuerdatenAbholenButton()) {
            this.triedSavingWithoutForm = true;
            return undefined;
        }
        this.triedSavingWithoutForm = false;

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

    public steuerdatenzugriffClicked(): void {
        this.resetAutomatischePruefungSteuerdaten();
        if (this.getModel().finanzielleSituationJA.steuerdatenZugriff) {
            return;
        }
        this.resetKiBonAnfrageFinSitIfRequired();
    }

    public showFormular(): boolean {
        // falls die Einstellung noch nicht geladen wurde zeigen wir das Formular nicht
        if (EbeguUtil.isNullOrUndefined(this.steuerSchnittstelleAktivForPeriode)) {
            return false;
        }
        // falls Schnittstelle deaktiviert, erfolgt das Ausfüllen immer manuell
        if (!this.steuerSchnittstelleAktivForPeriode) {
            return true;
        }
        // bei einem Papiergesuch ebenfalls
        if (!this.gesuchModelManager.getGesuch().isOnlineGesuch()
            || (!this.authServiceRS.isRole(TSRole.GESUCHSTELLER)
                && EbeguUtil.isNullOrUndefined(this.model.getFiSiConToWorkWith().finanzielleSituationGS))) {
            return true;
        }
        // wenn es sich um ein sozialdienstfall handelt ebenfalls
        if (this.gesuchModelManager.getFall().isSozialdienstFall()) {
            return true;
        }
        // falls die Frage bei nicht gmeinsamer stek noch nicht beantwortet wurde, zeigen wir das Formular noch nicht
        if (EbeguUtil.isNotNullAndFalse(this.model.familienSituation.gemeinsameSteuererklaerung)
            && EbeguUtil.isNullOrUndefined(this.getModel().finanzielleSituationJA.steuerdatenZugriff)) {
            return false;
        }
        // falls die Frage mit ja beantwortet wurde, die Abfrage aber noch nicht gemacht wurde,
        // zeigen wir das Formular noch nicht
        if (this.getModel().finanzielleSituationJA.steuerdatenZugriff
            && EbeguUtil.isNullOrUndefined(this.getModel().finanzielleSituationJA.steuerdatenAbfrageStatus)) {
            return false;
        }
        return true;
    }

    public isSteueranfrageErfolgreich(): boolean {
        return this.steuerSchnittstelleAktivForPeriode
            && EbeguUtil.isNotNullAndTrue(this.getModel().finanzielleSituationJA.steuerdatenZugriff)
            && isSteuerdatenAnfrageStatusErfolgreich(this.getModel().finanzielleSituationJA.steuerdatenAbfrageStatus);
    }

    public showAufteilung(): boolean {
        return this.isSteueranfrageErfolgreich()
            && this.gesuchModelManager.getGesuch().familiensituationContainer.familiensituationJA.gemeinsameSteuererklaerung;
    }

    public startAufteilung(): void {
        // zwischenspeichern dieser werte. Das sind die einzigen, die nicht readonly und dementsprechend schon
        // durch den User verändert sein könnten. Beim erneuten laden würden diese überschrieben
        const einkommenAusVereinfachtemVerfahren =
            this.getModel().finanzielleSituationJA.einkommenInVereinfachtemVerfahrenAbgerechnet;
        const einkommenAusVereinfachtemVerfahrenAmount
            = this.getModel().finanzielleSituationJA.amountEinkommenInVereinfachtemVerfahrenAbgerechnet;
        this.dvDialog.showDialogFullscreen(aufteilungDialogTemplate, FinanzielleSituationAufteilungDialogController)
            .then(() => {
                this.copyDataAndInit();
                this.getModel().finanzielleSituationJA.einkommenInVereinfachtemVerfahrenAbgerechnet =
                    einkommenAusVereinfachtemVerfahren;
                this.getModel().finanzielleSituationJA.amountEinkommenInVereinfachtemVerfahrenAbgerechnet =
                    einkommenAusVereinfachtemVerfahrenAmount;
            });
    }

    public resetKiBonAnfrageFinSit(): void {
        this.model.copyFinSitDataToGesuch(this.gesuchModelManager.getGesuch());
        this.gesuchModelManager.resetKiBonAnfrageFinSit(false).then(() => {
                this.initAfterKiBonAnfrageUpdate();
            }
        );
    }

    private initAfterKiBonAnfrageUpdate(): void {
        this.wizardStepManager.updateCurrentWizardStepStatusSafe(
            TSWizardStepName.FINANZIELLE_SITUATION,
            TSWizardStepStatus.NOK);
        this.model.copyFinSitDataFromGesuch(this.gesuchModelManager.getGesuch());
        this.initSelbstaendigkeit();
        this.initErsatzeinkommenSelbststaendigkeit();
    }

    protected showAutomatischePruefungSteuerdatenFrage(): boolean {
        if (!this.steuerSchnittstelleAktivForPeriode) {
            return false;
        }

        return EbeguUtil.isNotNullOrUndefined(this.gesuchModelManager.getGesuch()) &&
            this.gesuchModelManager.getGesuch().isOnlineGesuch() &&
            !this.model.familienSituation.gemeinsameSteuererklaerung &&
            this.gesuchModelManager.getGesuchstellerNumber() === 1 &&
            EbeguUtil.isNotNullAndFalse(this.getModel().finanzielleSituationJA.steuerdatenZugriff);
    }

    protected isNotFinSitStartOrGS2Required(): boolean {
        return true;
    }

    public getFinSitVorMutationToWorkWith(): TSFinanzielleSituation | any {
        if (this.model.getFinSitVorMutationToWorkWith()) {
            return this.model.getFinSitVorMutationToWorkWith();
        }
        // wir returnieren leeres Objekt, damit wir im Template nicht überall den Nullcheck machen müssen
        return {};
    }

    private initEinstellungen(): Promise<void> {
        return this.einstellungRS.getAllEinstellungenBySystemCached(this.gesuchModelManager.getGesuchsperiode().id).toPromise()
            .then(einstellungen => {
                const showErsatzeinkommen = einstellungen.find(einstellung => einstellung.key
                    === TSEinstellungKey.ZUSATZLICHE_FELDER_ERSATZEINKOMMEN);
                if (showErsatzeinkommen === undefined) {
                    // eslint-disable-next-line max-len
                    LOG.error(`Missing Einstellung "ZUSATZLICHE_FELDER_ERSATZEINKOMMEN" in gesuchsperiode ${this.gesuchModelManager.getGesuchsperiode().gesuchsperiodeString}`);
                    this.ersatzeinkommenSelbststaendigkeitActivated = false;
                    return;
                }
                this.ersatzeinkommenSelbststaendigkeitActivated = showErsatzeinkommen.getValueAsBoolean();
            });
    }

    public getTextErsatzeinkommenSelbstaendigKorrektur(): string {
        const finSitGS = this.getModel().finanzielleSituationGS;
        if (!finSitGS || !finSitGS.hasErsatzeinkommenSelbststaendigkeit()) {
            return this.$translate.instant('LABEL_KEINE_ANGABE');
        }

        const ersatzeinkommen = finSitGS.ersatzeinkommenBasisjahr;
        const ersatzeinkommen2 = finSitGS.ersatzeinkommenBasisjahrMinus1;
        const ersatzeinkommen3 = finSitGS.ersatzeinkommenBasisjahrMinus2;
        const basisjahr = this.gesuchModelManager.getBasisjahr();
        const params = {basisjahr, ersatzeinkommen, ersatzeinkommen2, ersatzeinkommen3};

        return this.$translate.instant('JA_KORREKTUR_ERSATZEINKOMMEN_SELBSTAENDIG', params);
    }

    public isErsatzeinkommenValid(): boolean {
            const finSit: TSFinanzielleSituation = this.model.getFiSiConToWorkWith().finanzielleSituationJA;
        return EbeguUtil.isNullOrUndefined(finSit.ersatzeinkommen)
            || EbeguUtil.isNullOrUndefined(finSit.ersatzeinkommenBasisjahr)
            || finSit.ersatzeinkommen - finSit.ersatzeinkommenBasisjahr >= 0;
    }

    public isAtLeastOneErsatzeinkommenSelbststaendigkeitProvided(): boolean {
        const finSit = this.model.getFiSiConToWorkWith().finanzielleSituationJA;
        return !finSit.hasErsatzeinkommenSelbststaendigkeit() ||
            (EbeguUtil.isNotNullOrUndefined(finSit.ersatzeinkommenBasisjahr)
                && finSit.ersatzeinkommenBasisjahr > 0)
                || (EbeguUtil.isNotNullOrUndefined(finSit.ersatzeinkommenBasisjahrMinus1)
                    && finSit.ersatzeinkommenBasisjahrMinus1 > 0)
            || (EbeguUtil.isNotNullOrUndefined(finSit.ersatzeinkommenBasisjahrMinus2)
                    && finSit.ersatzeinkommenBasisjahrMinus2 > 0);
    }

    public hasGeschaeftsgewinn(basisjahrMinus: number): boolean {
        const finSit = this.model.getFiSiConToWorkWith().finanzielleSituationJA;
        if (basisjahrMinus === 2) {
            return EbeguUtil.isNotNullOrUndefined(finSit.geschaeftsgewinnBasisjahrMinus2);
        }
        if (basisjahrMinus === 1) {
            return EbeguUtil.isNotNullOrUndefined(finSit.geschaeftsgewinnBasisjahrMinus1);
        }
        return EbeguUtil.isNotNullOrUndefined(finSit.geschaeftsgewinnBasisjahr);
    }

    public geschaeftsgewinnChange(basisjahrMinus: number): void {
        const finSit = this.model.getFiSiConToWorkWith().finanzielleSituationJA;
        if (basisjahrMinus === 2 && finSit.geschaeftsgewinnBasisjahrMinus2 !== undefined) {
            finSit.ersatzeinkommenBasisjahrMinus2 = null;
        } else if (basisjahrMinus === 1 && finSit.geschaeftsgewinnBasisjahrMinus1 !== undefined) {
            finSit.ersatzeinkommenBasisjahrMinus1 = null;
        }
    }

    public ersatzeinkommenChanged(): void {
        if (this.getModel().finanzielleSituationJA.ersatzeinkommen === 0) {
            this.getModel().finanzielleSituationJA.ersatzeinkommenBasisjahr = 0;
        }
    }

    private scrollToErsatzeinkommenSelbststaendigkeit(): void {
        const tmp = document.getElementById('ersatzeinkommen-selbststaendigkeit-container');
        if (tmp) {
            tmp.scrollIntoView();
        }
    }
}
