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
import {ApplicationPropertyRS} from '../../../../../app/core/rest-services/applicationPropertyRS.rest';
import {ListResourceRS} from '../../../../../app/core/service/listResourceRS.rest';
import {AuthServiceRS} from '../../../../../authentication/service/AuthServiceRS.rest';
import {TSRole} from '../../../../../models/enums/TSRole';
import {isSteuerdatenAnfrageStatusErfolgreich} from '../../../../../models/enums/TSSteuerdatenAnfrageStatus';
import {TSWizardStepName} from '../../../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../../../models/enums/TSWizardStepStatus';
import {TSAdresse} from '../../../../../models/TSAdresse';
import {TSFinanzielleSituation} from '../../../../../models/TSFinanzielleSituation';
import {TSFinanzModel} from '../../../../../models/TSFinanzModel';
import {TSGesuch} from '../../../../../models/TSGesuch';
import {TSLand} from '../../../../../models/types/TSLand';
import {EbeguRestUtil} from '../../../../../utils/EbeguRestUtil';
import {EbeguUtil} from '../../../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../../../utils/TSRoleUtil';
import {RemoveDialogController} from '../../../../dialog/RemoveDialogController';
import {BerechnungsManager} from '../../../../service/berechnungsManager';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../../service/wizardStepManager';
import {AbstractFinSitBernView} from '../AbstractFinSitBernView';
import IPromise = angular.IPromise;
import IQService = angular.IQService;
import IScope = angular.IScope;
import ITimeoutService = angular.ITimeoutService;

const removeDialogTemplate = require('../../../../dialog/removeDialogTemplate.html');

export class FinanzielleSituationStartViewComponentConfig implements IComponentOptions {
    public transclude = false;
    public template = require('./finanzielleSituationStartView.html');
    public controller = FinanzielleSituationStartViewController;
    public controllerAs = 'vm';
}

export class FinanzielleSituationStartViewController extends AbstractFinSitBernView {

    public static $inject: string[] = [
        'GesuchModelManager',
        'BerechnungsManager',
        'ErrorService',
        'WizardStepManager',
        '$q',
        '$scope',
        '$timeout',
        'DvDialog',
        'AuthServiceRS',
        'EbeguRestUtil',
        'ListResourceRS',
        'EinstellungRS',
        'ApplicationPropertyRS'
    ];

    public finanzielleSituationRequired: boolean;
    public areThereOnlySchulamtangebote: boolean;
    public allowedRoles: ReadonlyArray<TSRoleUtil>;
    private readonly initialModel: TSFinanzModel;
    public laenderList: TSLand[];
    private triedSavingWithoutForm: boolean = false;

    public constructor(
        gesuchModelManager: GesuchModelManager,
        berechnungsManager: BerechnungsManager,
        private readonly errorService: ErrorService,
        wizardStepManager: WizardStepManager,
        private readonly $q: IQService,
        $scope: IScope,
        $timeout: ITimeoutService,
        dvDialog: DvDialog,
        protected readonly authServiceRS: AuthServiceRS,
        private readonly ebeguRestUtil: EbeguRestUtil,
        listResourceRS: ListResourceRS,
        einstellungRS: EinstellungRS,
        applicationPropertyRS: ApplicationPropertyRS
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

        listResourceRS.getLaenderList().then((laenderList: TSLand[]) => {
            this.laenderList = laenderList;
        });
        this.model = new TSFinanzModel(this.gesuchModelManager.getBasisjahr(),
            this.gesuchModelManager.isGesuchsteller2Required(),
            null);
        this.model.copyFinSitDataFromGesuch(this.gesuchModelManager.getGesuch());
        this.initialModel = angular.copy(this.model);

        this.allowedRoles = this.TSRoleUtil.getAllRolesButTraegerschaftInstitution();
        this.wizardStepManager.updateCurrentWizardStepStatusSafe(
            TSWizardStepName.FINANZIELLE_SITUATION,
            TSWizardStepStatus.IN_BEARBEITUNG);
        this.areThereOnlySchulamtangebote = this.gesuchModelManager.areThereOnlySchulamtAngebote(); // so we load it
                                                                                                    // just once

        this.gesuchModelManager.setGesuchstellerNumber(1);
    }

    public showSteuerveranlagung(): boolean {
        // falls die Einstellung noch nicht geladen ist, zeigen wir die Fragen noch nicht
        if (EbeguUtil.isNullOrUndefined(this.steuerSchnittstelleAktivForPeriode)) {
            return false;
        }
        // bei alleiniger Steuererklärung wird die Frage immer auf der finSitView gezeigt
        if (!this.model.gemeinsameSteuererklaerung) {
            return false;
        }

        if (super.showZugriffAufSteuerdatenForGemeinde()) {
            return false;
        }

        // bei einem Papiergesuch muss man es anzeigen, die Steuerdatenzugriff Frage ist nicht gestellt
        if (!this.gesuchModelManager.getGesuch().isOnlineGesuch()) {
            return true;
        }
        // falls steuerschnittstelle aktiv, aber zugriffserlaubnis noch nicht beantwortet, dann zeigen wir die Frage
        // nicht
        if (this.steuerSchnittstelleAktivForPeriode && EbeguUtil.isNullOrUndefined(this.getModel().finanzielleSituationJA.steuerdatenZugriff)) {
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
        return !this.getFinanzielleSituationGS1().steuerveranlagungErhalten;
    }

    private save(): IPromise<TSGesuch> {
        this.errorService.clearAll();
        return this.gesuchModelManager.saveFinanzielleSituationStart()
            .then((gesuch: TSGesuch) => gesuch).catch(error => {
                this.initialModel.copyFinSitDataToGesuch(this.gesuchModelManager.getGesuch());
                throw(error);
            });
    }

    public confirmAndSave(): IPromise<TSGesuch> {
        if (!this.isGesuchValid()) {
            return undefined;
        }

        this.model.copyFinSitDataToGesuch(this.gesuchModelManager.getGesuch());
        if (!this.form.$dirty) {
            if (this.updateStepDueToSozialhilfeOhneBenoetigteZeitraeume()) {
                return this.wizardStepManager.updateWizardStepStatus(TSWizardStepName.FINANZIELLE_SITUATION,
                    TSWizardStepStatus.OK).then(() => this.gesuchModelManager.getGesuch());
            }
            // If there are no changes in form we don't need anything to update on Server and we could return the
            // promise immediately
            return this.$q.when(this.gesuchModelManager.getGesuch());
        }
        if (this.finanzielleSituationTurnedNotRequired()) {
            return this.dvDialog.showRemoveDialog(removeDialogTemplate, this.form, RemoveDialogController, {
                title: 'FINSIT_WARNING',
                deleteText: 'FINSIT_WARNING_BESCHREIBUNG'
            }).then(() =>    // User confirmed changes
                 this.save()
            );
        }
        return this.save();
    }

    /**
     * Step ist noch in Bearbeitung, es handelt sich aber um einen Sozialhilfebezüger in einer Gemeinde
     * in welcher die Zeiträume nicht angegeben werden müssen -> direkt auf OK setzen
     */
    private updateStepDueToSozialhilfeOhneBenoetigteZeitraeume(): boolean {
        return this.wizardStepManager.hasStepGivenStatus(TSWizardStepName.FINANZIELLE_SITUATION,
            TSWizardStepStatus.IN_BEARBEITUNG)
            && this.gesuchModelManager.isSozialhilfeBezueger()
            && !this.gesuchModelManager.isSozialhilfeBezuegerZeitraeumeRequired();
    }

    public finanzielleSituationTurnedNotRequired(): boolean {
        return this.initialModel.isFinanzielleSituationRequired() && !this.model.isFinanzielleSituationRequired();
    }

    public getFinanzielleSituationGS1(): TSFinanzielleSituation {
        return this.model.finanzielleSituationContainerGS1.finanzielleSituationJA;
    }

    public getFinanzielleSituationGS2(): TSFinanzielleSituation {
        return this.model.finanzielleSituationContainerGS2.finanzielleSituationJA;
    }

    public isFinanziellesituationRequired(): boolean {
        return this.finanzielleSituationRequired;
    }

    public hasTagesschulenAnmeldung(): boolean {
        if (!this.gesuchModelManager.gemeindeKonfiguration) {
            return undefined;
        }
        return this.gesuchModelManager.gemeindeKonfiguration.hasTagesschulenAnmeldung();
    }

    public gemeinsameStekClicked(): void {
        if (!this.model.gemeinsameSteuererklaerung && this.model.finanzielleSituationContainerGS1 && !this.model.finanzielleSituationContainerGS1.isNew()) {
            // Wenn neu NEIN und schon was eingegeben -> Fragen mal auf false setzen und Status auf nok damit man
            // sicher noch weiter muss!
            this.initSteuerFragen();
            this.wizardStepManager.updateCurrentWizardStepStatusSafe(
                TSWizardStepName.FINANZIELLE_SITUATION,
                TSWizardStepStatus.NOK);
        } else if (!this.model.gemeinsameSteuererklaerung) {
            // Wenn neu NEIN -> Fragen loeschen wenn noch nichts eingegeben worden ist
            this.model.finanzielleSituationContainerGS1 = undefined;
            this.model.finanzielleSituationContainerGS2 = undefined;
            this.model.initFinSit();
        } else {
            this.model.initFinSit();
        }

        this.model.finanzielleSituationContainerGS1.finanzielleSituationJA.steuerdatenZugriff = undefined;
        this.model.finanzielleSituationContainerGS2.finanzielleSituationJA.steuerdatenZugriff = undefined;
        this.model.finanzielleSituationContainerGS1.finanzielleSituationJA.automatischePruefungErlaubt = undefined;
        this.model.finanzielleSituationContainerGS2.finanzielleSituationJA.automatischePruefungErlaubt = undefined;
        this.getModel().finanzielleSituationJA.steuerdatenZugriff = undefined;
        this.getModel().finanzielleSituationJA.automatischePruefungErlaubt = undefined;
        // first, reset local properties before sending request
        this.resetKiBonAnfrageFinSit();
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
        gs2.steuerveranlagungErhalten = !!gs2.steuerveranlagungErhalten;
    }

    public steuererklaerungClicked(): void {
        if (this.model.gemeinsameSteuererklaerung) {
            this.model.finanzielleSituationContainerGS2.finanzielleSituationJA.steuererklaerungAusgefuellt =
                this.model.finanzielleSituationContainerGS1.finanzielleSituationJA.steuererklaerungAusgefuellt;
        }
        if (this.getFinanzielleSituationGS1().steuererklaerungAusgefuellt) {
            return;
        }
    }

    public is2GSRequired(): boolean {
        return this.gesuchModelManager.isGesuchsteller2Required();
    }

    public isMahlzeitenverguenstigungToggleDisabled(): boolean {
        // In einer Mutation darf der Button nicht neu auf JA (d.h. wir beantragen KEINE...) gesetzt werden
        if (this.gesuchModelManager.getGesuch().isMutation()) {
            return !this.getGesuch().extractFamiliensituation().keineMahlzeitenverguenstigungBeantragtEditable;
        }
        return false;
    }

    public isMahlzeitenverguenstigungEnabled(): boolean {
        return this.gesuchModelManager.isMahlzeitenverguenstigungEnabled() &&
            (this.model.sozialhilfeBezueger || this.model.verguenstigungGewuenscht)
            && this.getGesuch() && !this.getGesuch().areThereOnlyFerieninsel();
    }

    public isZahlungsdatenRequired(): boolean {
        return this.isMahlzeitenverguenstigungEnabled() && !this.model.zahlungsinformationen.keineMahlzeitenverguenstigungBeantragt;
    }

    public areZahlungsdatenEditable(): boolean {
        return this.gesuchModelManager.isNeuestesGesuch()
        && this.authServiceRS.isOneOfRoles(TSRoleUtil.getGesuchstellerSozialdienstJugendamtSchulamtRoles()) ?
            true :
            !this.isGesuchReadonly();
    }

    public preSave(): IPromise<TSGesuch> {
        if (!this.isGesuchValid()) {
            return undefined;
        }
        // speichern darf nicht möglich sein, wenn Steuerabfrage Button sichtbar
        if (this.showSteuerdatenAbholenButton() && this.isFinanziellesituationRequired()) {
            this.triedSavingWithoutForm = true;
            return undefined;
        }
        this.triedSavingWithoutForm = false;

        if (this.areZahlungsdatenEditable() && this.isGesuchReadonly()) {
            if (!this.form.$dirty) {
                // If there are no changes in form we don't need to try to update the zahlung informations
                // promise immediately
                return this.$q.when(this.gesuchModelManager.getGesuch());
            }
            const properties = this.ebeguRestUtil.alwaysEditablePropertiesToRestObject({},
                this.gesuchModelManager.getGesuch());

            properties.keineMahlzeitenverguenstigungBeantragt =
                this.model.zahlungsinformationen.keineMahlzeitenverguenstigungBeantragt;
            properties.iban = this.model.zahlungsinformationen.iban?.toLocaleUpperCase();
            properties.kontoinhaber = this.model.zahlungsinformationen.kontoinhaber;
            properties.abweichendeZahlungsadresse = this.model.zahlungsinformationen.abweichendeZahlungsadresse;
            properties.zahlungsadresse =
                this.ebeguRestUtil.adresseToRestObject({}, this.model.zahlungsinformationen.zahlungsadresse);

            return this.gesuchModelManager.updateAlwaysEditableProperties(properties);
        }

        return this.confirmAndSave();
    }

    public changeAbweichendeZahlungsadresse(): void {
        this.model.zahlungsinformationen.zahlungsadresse =
            this.model.zahlungsinformationen.abweichendeZahlungsadresse ? new TSAdresse() : null;
    }

    public showZugriffAufSteuerdaten(): boolean {
        return super.showZugriffAufSteuerdaten() && this.model.gemeinsameSteuererklaerung;
    }

    public showAutomatischePruefungSteuerdatenFrage(): boolean {
        if (!this.steuerSchnittstelleAktivForPeriode) {
            return false;
        }

        if (!this.isFinanziellesituationRequired()) {
            return false;
        }

        return this.gesuchModelManager.getGesuch().isOnlineGesuch() &&
            this.model.gemeinsameSteuererklaerung &&
            (EbeguUtil.isNotNullAndFalse(this.getModel().finanzielleSituationJA.steuerdatenZugriff));
    }

    public steuerdatenzugriffClicked(): void {
        this.resetAutomatischePruefungSteuerdaten();
        if (this.getModel().finanzielleSituationJA.steuerdatenZugriff) {
            return;
        }
        this.resetKiBonAnfrageFinSitIfRequired();
    }

    public callKiBonAnfrageAndUpdateFinSit(): void {
       super.callKiBonAnfrage(EbeguUtil.isNotNullAndTrue(this.model.gemeinsameSteuererklaerung))
            .then(() => {
                    this.model.copyFinSitDataFromGesuch(this.gesuchModelManager.getGesuch());
                    this.form.$setDirty();
                }
            );
    }

    private getAbfrageStatus(): string {
        return this.getModel().finanzielleSituationJA.steuerdatenAbfrageStatus;
    }

    protected resetKiBonAnfrageFinSit(): void {
        this.model.copyFinSitDataToGesuch(this.gesuchModelManager.getGesuch());
        this.gesuchModelManager.resetKiBonAnfrageFinSit(EbeguUtil.isNotNullOrUndefined(this.model.finanzielleSituationContainerGS2))
            .then(() => {
                    this.wizardStepManager.updateCurrentWizardStepStatusSafe(
                        TSWizardStepName.FINANZIELLE_SITUATION,
                        TSWizardStepStatus.NOK);
                    this.model.copyFinSitDataFromGesuch(this.gesuchModelManager.getGesuch());
                    this.form.$setDirty();
                }
            );
    }
}
