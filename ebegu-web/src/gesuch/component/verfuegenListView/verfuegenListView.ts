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

import {StateService} from '@uirouter/core';
import {IComponentOptions, IPromise} from 'angular';
import * as moment from 'moment';
import {EinstellungRS} from '../../../admin/service/einstellungRS.rest';
import {DvDialog} from '../../../app/core/directive/dv-dialog/dv-dialog';
import {DownloadRS} from '../../../app/core/service/downloadRS.rest';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {isAnyStatusOfMahnung, isAnyStatusOfVerfuegt, TSAntragStatus} from '../../../models/enums/TSAntragStatus';
import {TSAntragTyp} from '../../../models/enums/TSAntragTyp';
import {TSBetreuungsstatus} from '../../../models/enums/TSBetreuungsstatus';
import {TSEinstellungKey} from '../../../models/enums/TSEinstellungKey';
import {TSFinSitStatus} from '../../../models/enums/TSFinSitStatus';
import {TSMahnungTyp} from '../../../models/enums/TSMahnungTyp';
import {TSRole} from '../../../models/enums/TSRole';
import {TSWizardStepName} from '../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../models/enums/TSWizardStepStatus';
import {TSBetreuung} from '../../../models/TSBetreuung';
import {TSDownloadFile} from '../../../models/TSDownloadFile';
import {TSFall} from '../../../models/TSFall';
import {TSGesuch} from '../../../models/TSGesuch';
import {TSGesuchsperiode} from '../../../models/TSGesuchsperiode';
import {TSKindContainer} from '../../../models/TSKindContainer';
import {TSMahnung} from '../../../models/TSMahnung';
import {navigateToStartPageForRole} from '../../../utils/AuthenticationUtil';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {EnumEx} from '../../../utils/EnumEx';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {BemerkungenDialogController} from '../../dialog/BemerkungenDialogController';
import {RemoveDialogController} from '../../dialog/RemoveDialogController';
import {BerechnungsManager} from '../../service/berechnungsManager';
import {GesuchModelManager} from '../../service/gesuchModelManager';
import {GesuchRS} from '../../service/gesuchRS.rest';
import {MahnungRS} from '../../service/mahnungRS.rest';
import {WizardStepManager} from '../../service/wizardStepManager';
import {AbstractGesuchViewController} from '../abstractGesuchView';
import ITimeoutService = angular.ITimeoutService;
import ITranslateService = angular.translate.ITranslateService;

const removeDialogTempl = require('../../dialog/removeDialogTemplate.html');
const bemerkungDialogTempl = require('../../dialog/bemerkungenDialogTemplate.html');

export class VerfuegenListViewComponentConfig implements IComponentOptions {
    public transclude = false;
    public bindings = {
        // Bereits vorhandene Mahnungen
        mahnungList: '<',
    };
    public template = require('./verfuegenListView.html');
    public controller = VerfuegenListViewController;
    public controllerAs = 'vm';
}

export class VerfuegenListViewController extends AbstractGesuchViewController<any> {

    public static $inject: string[] = [
        '$state',
        'GesuchModelManager',
        'BerechnungsManager',
        'WizardStepManager',
        'DvDialog',
        'DownloadRS',
        'MahnungRS',
        'AuthServiceRS',
        '$scope',
        'GesuchRS',
        '$timeout',
        '$translate',
        'EinstellungRS',
        'EbeguUtil',
    ];

    private kinderWithBetreuungList: Array<TSKindContainer>;
    public mahnungList: TSMahnung[];
    private mahnung: TSMahnung;
    private tempAntragStatus: TSAntragStatus;
    public finSitStatus: Array<string>;
    private kontingentierungEnabled: boolean = false;
    private readonly ebeguUtil: EbeguUtil;

    public constructor(
        private readonly $state: StateService,
        gesuchModelManager: GesuchModelManager,
        berechnungsManager: BerechnungsManager,
        wizardStepManager: WizardStepManager,
        private readonly dvDialog: DvDialog,
        private readonly downloadRS: DownloadRS,
        private readonly mahnungRS: MahnungRS,
        private readonly authServiceRs: AuthServiceRS,
        $scope: angular.IScope,
        private readonly gesuchRS: GesuchRS,
        $timeout: ITimeoutService,
        private readonly $translate: ITranslateService,
        private readonly einstellungRS: EinstellungRS,
        ebeguUtil: EbeguUtil,
    ) {

        super(gesuchModelManager, berechnungsManager, wizardStepManager, $scope, TSWizardStepName.VERFUEGEN, $timeout);
        this.initViewModel();
        this.ebeguUtil = ebeguUtil;
    }

    /**
     * Die finanzielle Situation und die Einkommensverschlechterungen muessen mithilfe des Berechnungsmanagers
     * berechnet werden, um manche Daten zur Verfügung zu haben. Das ist notwendig weil die finanzielle Situation nicht
     * gespeichert wird. D.H. das erste Mal in einer Sitzung wenn ein Gesuch geoeffnet wird, ist gar nichts berechnet.
     * Wenn man dann die Verfügen direkt aufmacht, ist alles leer und wird nichts angezeigt, deswegen muss alles auch
     * hier berechnet werden. Um Probleme mit der Performance zu vermeiden, wird zuerst geprueft, ob die Berechnung
     * schon vorher gemacht wurde, wenn ja dann wird sie einfach verwendet ohne sie neu berechnen zu muessen. Dieses
     * geht aber davon aus, dass die Berechnungen immer richtig kalkuliert wurden.
     *
     * Die Verfuegungen werden IMMER geladen, wenn diese View geladen wird. Dieses ist etwas ineffizient. Allerdings
     * muss es eigentlich so funktionieren, weil die Daten sich haben aendern koennen. Es ist ein aehnlicher Fall wie
     * mit der finanziellen Situation. Sollte es Probleme mit der Performance geben, muessen wir ueberlegen, ob wir es
     * irgendwie anders berechnen koennen um den Server zu entlasten.
     */
    private initViewModel(): void {
        this.wizardStepManager.updateCurrentWizardStepStatusSafe(
            TSWizardStepName.VERFUEGEN,
            TSWizardStepStatus.WARTEN);

        // Berechnung aller finanziellen Daten
        const gesuch = this.gesuchModelManager.getGesuch();
        if (!this.berechnungsManager.finanzielleSituationResultate) {
            this.berechnungsManager.calculateFinanzielleSituation(gesuch);
        }
        if (gesuch
            && gesuch.extractEinkommensverschlechterungInfo()
            && gesuch.extractEinkommensverschlechterungInfo().ekvFuerBasisJahrPlus1
            && !this.berechnungsManager.einkommensverschlechterungResultateBjP1) {

            this.berechnungsManager.calculateEinkommensverschlechterung(gesuch, 1);
        }
        if (gesuch
            && gesuch.extractEinkommensverschlechterungInfo()
            && gesuch.extractEinkommensverschlechterungInfo().ekvFuerBasisJahrPlus2
            && !this.berechnungsManager.einkommensverschlechterungResultateBjP2) {

            this.berechnungsManager.calculateEinkommensverschlechterung(gesuch, 2);
        }
        this.refreshKinderListe();
        this.finSitStatus = EnumEx.getNames(TSFinSitStatus);

        // Die Einstellung bezueglich Kontingentierung lesen
        // tslint:disable-next-line:early-exit
        if (EbeguUtil.isNotNullOrUndefined(this.gesuchModelManager.getGesuchsperiode())) {
            this.einstellungRS.findEinstellung(
                TSEinstellungKey.GEMEINDE_KONTINGENTIERUNG_ENABLED,
                this.gesuchModelManager.getDossier().gemeinde.id,
                this.gesuchModelManager.getGesuchsperiode().id,
            )
                .then(response => {
                    this.kontingentierungEnabled = JSON.parse(response.value);
                });
        }
    }

    private refreshKinderListe(): IPromise<any> {
        return this.gesuchModelManager.calculateVerfuegungen().then(() => {
            this.kinderWithBetreuungList = this.gesuchModelManager.getKinderWithBetreuungList();
        });
    }

    public getKinderWithBetreuungList(): Array<TSKindContainer> {
        return this.kinderWithBetreuungList;
    }

    public getMahnungList(): Array<TSMahnung> {
        return this.mahnungList;
    }

    /**
     * Nur bestaetigte Betreuungen koennen geoeffnet werden
     */
    public openVerfuegung(kind: TSKindContainer, betreuung: TSBetreuung): void {
        if (!this.kannVerfuegungOeffnen(betreuung)) {
            return;
        }

        if (!kind || !betreuung) {
            return;
        }

        const kindIndex = this.gesuchModelManager.convertKindNumberToKindIndex(kind.kindNummer);
        if (kindIndex < 0) {
            return;
        }

        /**
         * Falls es sich um den Gesuchsteller handelt und das Gesuch nicht freigegeben ist,
         * wird bei Betreuungen im Status Bestätigt und Unbekannte Institution direkt das PDF erzeugt.
         * Alle anderen darf der GS nicht anschauen (siehe isDetailAvailableForBetreuungstatus())
         */
        if (this.isGesuchstellerOrSozialdienst() && !isAnyStatusOfVerfuegt(this.gesuchModelManager.getGesuch().status)
            && !betreuung.isAngebotTagesschule()) {
            this.openVerfuegungPDF(betreuung);
            return;
        }

        this.gesuchModelManager.setKindIndex(kindIndex);
        this.$state.go('gesuch.verfuegenView', {
            betreuungNumber: betreuung.betreuungNummer,
            kindNumber: kind.kindNummer,
            gesuchId: this.getGesuchId(),
        });
    }

    public openVerfuegungPDF(betreuung: TSBetreuung): void {
        const win = this.downloadRS.prepareDownloadWindow();
        this.downloadRS.getAccessTokenVerfuegungGeneratedDokument(this.gesuchModelManager.getGesuch().id,
            betreuung.id, false, betreuung.verfuegung.generatedBemerkungen)
            .then((downloadFile: TSDownloadFile) => {
                this.downloadRS.startDownload(downloadFile.accessToken, downloadFile.filename, false, win);
            })
            .catch(ex => EbeguUtil.handleDownloadError(win, ex));
    }

    public kannVerfuegungOeffnen(betreuung: TSBetreuung): boolean {
        if (betreuung) {
            if (betreuung.isAngebotTagesschule()) {
                return this.isDetailAvailableForTagesschuleAnmeldung(betreuung.betreuungsstatus);
            }
            return this.isDetailAvailableForBetreuungstatus(betreuung.betreuungsstatus);
        }
        return false;
    }

    /**
     * Der Gesuchsteller kann Verfügungen vor der Freigabe des Gesuchs nur dann öffnen,
     * wenn der Status der Betreuung Bestätigt oder Unbekannte Institution ist.
     * Nach Freigabe darf er wie alle anderen Rollen alle Verfügungen öffnen.
     */
    private isDetailAvailableForBetreuungstatus(betreuungsstatus: TSBetreuungsstatus): boolean {
        const allowedBetstatus: Array<TSBetreuungsstatus> = [
            TSBetreuungsstatus.UNBEKANNTE_INSTITUTION,
            TSBetreuungsstatus.BESTAETIGT,
            TSBetreuungsstatus.VERFUEGT,
            TSBetreuungsstatus.NICHT_EINGETRETEN,
            TSBetreuungsstatus.STORNIERT
        ];
        return allowedBetstatus.indexOf(betreuungsstatus) !== -1;
    }

    private isDetailAvailableForTagesschuleAnmeldung(betreuungsstatus: TSBetreuungsstatus): boolean {
        const allowedBetstatus: Array<TSBetreuungsstatus> = [
            TSBetreuungsstatus.SCHULAMT_ANMELDUNG_AUSGELOEST,
            TSBetreuungsstatus.SCHULAMT_ANMELDUNG_UEBERNOMMEN,
            TSBetreuungsstatus.SCHULAMT_ANMELDUNG_ERFASST,
            TSBetreuungsstatus.SCHULAMT_MODULE_AKZEPTIERT
        ];
        return allowedBetstatus.indexOf(betreuungsstatus) !== -1;
    }

    /**
     * das FinanzielleSituation PDF ist fuer den Gesuchsteller erst sichtbar sobald der Antrag den Status VERFUEGT
     * erreicht hat
     */
    public isFinanziellesituationPDFVisible(): boolean {
        if (!this.gesuchModelManager.isFinanzielleSituationEnabled()
            || !this.gesuchModelManager.isFinanzielleSituationRequired()) {
            return false;
        }
        if (this.isGesuchstellerOrSozialdienst()) {
            return isAnyStatusOfVerfuegt(this.getAntragStatus())
                && !this.isFinSitAbglehnt();
        }
        return !this.isFinSitAbglehnt();

    }

    public hasNichtBerechenbareBetreuungen(): boolean {
        const gesuch = this.gesuchModelManager.getGesuch();
        return (gesuch && gesuch.hasNichtBerechenbareBetreuungen());
    }

    public mustFinanzielleSituationBeValidated(): boolean {
        // Die FinSit muss auch bei Sozialhilfe geprueft werden!
        return this.gesuchModelManager.isFinanzielleSituationRequired()
            || (this.getGesuch()
                && this.getGesuch().familiensituationContainer
                && this.getGesuch().familiensituationContainer.familiensituationJA.sozialhilfeBezueger);
    }

    public isBegleitschreibenVisible(): boolean {
        if (this.isGesuchstellerOrSozialdienst()) {
            return isAnyStatusOfVerfuegt(this.getAntragStatus())
                && !this.gesuchModelManager.areThereOnlySchulamtAngebote()
                && !this.gesuchModelManager.areThereOnlyGeschlossenOhneVerfuegung();
        }
        return !this.gesuchModelManager.areThereOnlySchulamtAngebote()
            && !this.gesuchModelManager.areThereOnlyGeschlossenOhneVerfuegung();
    }

    public isKompletteKorrespondenzVisible(): boolean {
        const status = this.getAntragStatus();

        return this.isBegleitschreibenVisible()
            && isAnyStatusOfVerfuegt(status)
            && this.authServiceRs.isOneOfRoles(this.TSRoleUtil.getJugendamtAndSchulamtRole());
    }

    private getAntragStatus(): TSAntragStatus {
        const status = this.getGesuch() ? this.getGesuch().status : TSAntragStatus.IN_BEARBEITUNG_GS;
        return status;
    }

    public getFall(): TSFall | undefined {
        if (this.gesuchModelManager && this.gesuchModelManager.getGesuch()) {
            return this.gesuchModelManager.getFall();
        }
        return undefined;
    }

    public getGesuch(): TSGesuch {
        if (this.gesuchModelManager && this.gesuchModelManager.getGesuch()) {
            return this.gesuchModelManager.getGesuch();
        }
        return undefined;
    }

    public getGesuchsperiode(): TSGesuchsperiode {
        if (this.gesuchModelManager) {
            return this.gesuchModelManager.getGesuchsperiode();
        }
        return undefined;
    }

    public setGesuchStatusGeprueft(): IPromise<TSAntragStatus> {
        return this.dvDialog.showRemoveDialog(removeDialogTempl, this.form, RemoveDialogController, {
            title: 'CONFIRM_GESUCH_STATUS_GEPRUEFT',
            deleteText: 'BESCHREIBUNG_GESUCH_STATUS_WECHSELN',
            parentController: undefined,
            elementID: undefined,
        }).then(() => {
            const antragStatus = this.setGesuchStatus(TSAntragStatus.GEPRUEFT);
            this.refreshKinderListe();
            return antragStatus;
        });
    }

    public closeWithoutAngebot(): IPromise<TSGesuch> {
        return this.dvDialog.showRemoveDialog(removeDialogTempl, this.form, RemoveDialogController, {
            title: 'CONFIRM_GESUCH_STATUS_KEIN_ANGEBOT',
            deleteText: 'BESCHREIBUNG_GESUCH_KEIN_ANGEBOT',
            parentController: undefined,
            elementID: undefined,

        }).then(() => {
            return this.gesuchRS.closeWithoutAngebot(this.gesuchModelManager.getGesuch().id).then(response => {
                this.gesuchModelManager.setGesuch(response);
                this.form.$setPristine(); // nach dem es gespeichert wird, muessen wir das Form wieder auf clean setzen
                return this.refreshKinderListe().then(() => {
                    return this.gesuchModelManager.getGesuch();
                });
            });
        });
    }

    public setGesuchStatusVerfuegen(): IPromise<TSGesuch> {
        const deleteTextValue = 'BESCHREIBUNG_GESUCH_STATUS_WECHSELN';
        return this.dvDialog.showRemoveDialog(removeDialogTempl, this.form, RemoveDialogController, {
            title: 'CONFIRM_GESUCH_STATUS_VERFUEGEN',
            deleteText: deleteTextValue,
            parentController: undefined,
            elementID: undefined,
        }).then(() => {

            return this.gesuchRS.verfuegenStarten(this.gesuchModelManager.getGesuch().id)
                .then(
                    response => {
                        if (response.status === TSAntragStatus.NUR_SCHULAMT) {
                            // If AntragStatus==NUR_SCHULAMT the Sachbearbeiter_BG has no rights to work with or even
                            // to
                            // see this gesuch any more For this reason we have to navigate directly out of the gesuch
                            // once it has been saved. We navigate to the default start page for the current role.
                            // createNeededPDFs is not being called for the same reason. Anyway, the Gesuch vanishes
                            // for the role JA and is only available for the role SACHBEARBEITER_TS/ADMIN_TS, so JA
                            // doesn't need the PDFs to be created. When a Schulamt worker opens this Gesuch, she can
                            // generate the PDFs by clicking on the corresponding links
                            navigateToStartPageForRole(this.authServiceRs.getPrincipal().getCurrentRole(), this.$state);
                            return this.gesuchModelManager.getGesuch();
                        }
                        // for NUR_SCHULAMT this makes no sense
                        this.gesuchModelManager.setGesuch(response);
                        this.form.$setPristine(); // nach dem es gespeichert wird, muessen wir das Form wieder auf
                                                  // clean setzen
                        return this.refreshKinderListe().then(() => {
                            return this.gesuchModelManager.getGesuch();
                        });
                    });
        });
    }

    private hasOffeneMahnungen(): boolean {
        for (const mahn of this.mahnungList) {
            if (!mahn.timestampAbgeschlossen) {
                return true;
            }
        }
        return false;
    }

    public sendToSteuerverwaltung(): void {
        this.dvDialog.showDialog(bemerkungDialogTempl, BemerkungenDialogController, {
            title: 'SEND_TO_STV_CONFIRMATION',
            bemerkungen: this.gesuchModelManager.getGesuch().bemerkungenSTV,
        }).then((bemerkung: string) => {
            this.gesuchRS.sendGesuchToSTV(this.getGesuch().id, bemerkung).then((gesuch: TSGesuch) => {
                this.gesuchModelManager.setGesuch(gesuch);
            });
        });
    }

    public showSendToSteuerverwaltung(): boolean {
        // hier wird extra nur "VERFUEGT" gestestet statt alle verfuegten status weil das Schulamt das Gesuch nicht
        // pruefen lassen darf
        const statuse = [TSAntragStatus.VERFUEGT, TSAntragStatus.NUR_SCHULAMT];

        return this.gesuchModelManager.isGesuchStatusIn(statuse) && !this.getGesuch().gesperrtWegenBeschwerde;
    }

    public stvPruefungZurueckholen(): void {
        this.callStvPruefungAbschliessen('STV_PRUEFUNG_ZURUECKHOLEN_CONFIRMATION');
    }

    public showSTVPruefungZurueckholen(): boolean {
        return this.gesuchModelManager.isGesuchStatus(TSAntragStatus.PRUEFUNG_STV)
            && !this.getGesuch().gesperrtWegenBeschwerde;
    }

    private stvPruefungAbschliessen(): void {
        this.callStvPruefungAbschliessen('STV_PRUEFUNG_ABSCHLIESSEN_CONFIRMATION');
    }

    public showSTVPruefungAbschliessen(): boolean {
        return this.gesuchModelManager.isGesuchStatus(TSAntragStatus.GEPRUEFT_STV)
            && !this.getGesuch().gesperrtWegenBeschwerde;
    }

    private callStvPruefungAbschliessen(title: string): void {
        this.dvDialog.showRemoveDialog(removeDialogTempl, this.form, RemoveDialogController, {
            title: `${title}`,
            deleteText: '',
            parentController: undefined,
            elementID: undefined,
        }).then(() => {
            this.gesuchRS.stvPruefungAbschliessen(this.getGesuch().id).then((gesuch: TSGesuch) => {
                this.gesuchModelManager.setGesuch(gesuch);
            });
        });
    }

    public showErsteMahnungErstellen(): boolean {
        // Nur wenn keine offenen Mahnungen vorhanden!
        const statuse = [TSAntragStatus.IN_BEARBEITUNG_JA, TSAntragStatus.FREIGEGEBEN];

        return this.gesuchModelManager.isGesuchStatusIn(statuse)
            && this.mahnung === undefined
            && !this.hasOffeneMahnungen()
            && !this.isGesuchReadonly();
    }

    public showErsteMahnungAusloesen(): boolean {
        return this.mahnung !== undefined
            && this.mahnung.mahnungTyp === TSMahnungTyp.ERSTE_MAHNUNG
            && !this.isGesuchReadonly();
    }

    public showZweiteMahnungErstellen(): boolean {
        return this.gesuchModelManager.isGesuchStatus(TSAntragStatus.ERSTE_MAHNUNG_ABGELAUFEN)
            && this.mahnung === undefined
            && !this.isGesuchReadonly();
    }

    public showZweiteMahnungAusloesen(): boolean {
        return this.mahnung !== undefined
            && this.mahnung.mahnungTyp === TSMahnungTyp.ZWEITE_MAHNUNG
            && !this.isGesuchReadonly();
    }

    /**
     * Nur required in Status VERFUEGEN oder GEPRUEFT und wenn der Benutzer nicht am Erstellen einer Mahnung ist.
     */
    public isFinSitStatusRequired(): boolean {
        return !this.showErsteMahnungAusloesen()
            && !this.showZweiteMahnungAusloesen()
            && this.gesuchModelManager.isGesuchStatusIn([TSAntragStatus.VERFUEGEN, TSAntragStatus.GEPRUEFT]);
    }

    public showMahnlaufBeenden(): boolean {
        return this.getGesuch() ? (isAnyStatusOfMahnung(this.getGesuch().status) && !this.isGesuchReadonly()) : false;
    }

    public showDokumenteNichtKomplett(): boolean {
        return this.getGesuch() ? (isAnyStatusOfMahnung(this.getGesuch().status)
            && this.getGesuch().dokumenteHochgeladen
            && !this.isGesuchReadonly()) : false;
    }

    public showZweiteMahnungNichtEingetreten(): boolean {
        return this.gesuchModelManager.isGesuchStatus(TSAntragStatus.ZWEITE_MAHNUNG_ABGELAUFEN)
            && !this.isGesuchReadonly();
    }

    public ersteMahnungErstellen(): void {
        this.tempAntragStatus = TSAntragStatus.ERSTE_MAHNUNG;
        this.createMahnung(TSMahnungTyp.ERSTE_MAHNUNG);
    }

    public zweiteMahnungErstellen(): void {
        this.tempAntragStatus = TSAntragStatus.ZWEITE_MAHNUNG;
        this.createMahnung(TSMahnungTyp.ZWEITE_MAHNUNG);
    }

    public saveMahnung(): void {
        if (!this.form.$valid) {
            return;
        }

        this.mahnungRS.saveMahnung(this.mahnung).then((mahnungResponse: TSMahnung) => {
            this.setGesuchStatus(this.tempAntragStatus).then(() => {
                this.mahnungList.push(mahnungResponse);
                this.tempAntragStatus = undefined;
                this.mahnung = undefined;
            });
        });
    }

    private createMahnung(typ: TSMahnungTyp): IPromise<any> {
        return this.mahnungRS.getInitialeBemerkungen(this.getGesuch()).then(generatedBemerkungen => {
            this.mahnung = new TSMahnung();
            this.mahnung.mahnungTyp = typ;
            this.mahnung.gesuch = this.getGesuch();
            this.mahnung.timestampAbgeschlossen = null;
            this.mahnung.bemerkungen = generatedBemerkungen.data;
            if (this.gesuchModelManager.gemeindeKonfiguration.hasTagesschulenAnmeldung()
                && this.getGesuch().areThereOnlySchulamtAngebote()) {
                this.mahnung.datumFristablauf = moment(moment.now()).add(7, 'days');
            }
            return;
        });
    }

    public mahnlaufBeenden(): void {
        // Gesuchstatus zuruecksetzen UND die Mahnungen auf erledigt setzen
        this.mahnungRS.mahnlaufBeenden(this.getGesuch()).then(() => {
            this.mahnungRS.findMahnungen(this.getGesuch().id).then(reloadedMahnungen => {
                this.mahnungList = reloadedMahnungen;
                this.gesuchModelManager.getGesuch().status = TSAntragStatus.IN_BEARBEITUNG_JA;
            });
        });
    }

    public dokumenteNichtKomplett(): void {
        this.gesuchModelManager.getGesuch().dokumenteHochgeladen = false;
        this.gesuchModelManager.updateGesuch();
    }

    public zweiteMahnungNichtEingetreten(): void {
        // Auf die zweite Mahnung wurde nicht reagiert. Den Status des Gesuchs wieder auf IN_BEARBEITUNG setzen
        // damit die Betreuungen auf NICHT_EINGETRETEN verfügt werden können. Die Mahnungen bleiben aber offen!
        this.setGesuchStatus(TSAntragStatus.IN_BEARBEITUNG_JA);
    }

    /**
     * Der Button Geprueft wird nur beim Status IN_BEARBEITUNG_JA eingeblendet
     */
    public showGeprueft(): boolean {
        const statuse = [TSAntragStatus.IN_BEARBEITUNG_JA, TSAntragStatus.FREIGEGEBEN];

        return this.gesuchModelManager.isGesuchStatusIn(statuse)
            && this.wizardStepManager.areAllStepsOK(this.getGesuch())
            && this.mahnung === undefined
            && (
                !this.gesuchModelManager.areThereOnlySchulamtAngebote()
                || !this.gesuchModelManager.getGesuch().isThereAnyBetreuung()
            ) && !this.isGesuchReadonly();
    }

    /**
     * Der Button Verfuegung starten wird angezeigt, wenn alle Betreuungen bestaetigt und das Gesuch geprueft wurden
     */
    public showVerfuegenStarten(): boolean {
        return this.gesuchModelManager.isGesuchStatus(TSAntragStatus.GEPRUEFT)
            && this.wizardStepManager.isStepStatusOk(TSWizardStepName.BETREUUNG)
            && this.gesuchModelManager.getGesuch().isThereAnyBetreuung()
            && !this.gesuchModelManager.areThereOnlySchulamtAngebote()
            && !this.isGesuchReadonly();
    }

    public showKeinKontingent(): boolean {
        return this.getGesuch() ? (this.getGesuch().typ !== TSAntragTyp.MUTATION
            && this.showVerfuegenStarten()
            && this.kontingentierungEnabled) : false;
    }

    public showKontingentVorhanden(): boolean {
        return this.gesuchModelManager.isGesuchStatus(TSAntragStatus.KEIN_KONTINGENT);
    }

    /**
     * Nur wenn ein Gesuch keine Angebote hat und geprueft ist, kann man es ohne Angebote schliessen.
     */
    public showCloseWithoutAngebot(): boolean {
        return this.gesuchModelManager.isGesuchStatus(TSAntragStatus.GEPRUEFT)
            && !this.gesuchModelManager.getGesuch().isThereAnyBetreuung()
            && !this.isGesuchReadonly();
    }

    /**
     * ausblenden, wenn Gesuch readonly und finSitStatus nicht gesetzt (für alte Gesuche). Fuer GS nicht anzeigen.
     */
    public showFinSitStatus(): boolean {
        return !(this.isGesuchReadonly() && EbeguUtil.isNullOrUndefined(this.getGesuch().finSitStatus))
            && this.authServiceRs.isOneOfRoles(this.TSRoleUtil.getJugendamtAndSchulamtRole());
    }

    public openFinanzielleSituationPDF(): void {
        if (!this.gesuchModelManager.getGesuch()) {
            return;
        }
        const win = this.downloadRS.prepareDownloadWindow();
        this.downloadRS.getFinSitDokumentAccessTokenGeneratedDokument(this.gesuchModelManager.getGesuch().id)
            .then((downloadFile: TSDownloadFile) => {
                this.downloadRS.startDownload(downloadFile.accessToken, downloadFile.filename, false, win);
            })
            .catch(ex => EbeguUtil.handleDownloadError(win, ex));
    }

    public openBegleitschreibenPDF(): void {
        if (!this.gesuchModelManager.getGesuch()) {
            return;
        }
        const win = this.downloadRS.prepareDownloadWindow();
        this.downloadRS.getBegleitschreibenDokumentAccessTokenGeneratedDokument(this.gesuchModelManager.getGesuch().id)
            .then((downloadFile: TSDownloadFile) => {
                this.downloadRS.startDownload(downloadFile.accessToken, downloadFile.filename, false, win);
            })
            .catch(ex => EbeguUtil.handleDownloadError(win, ex));
    }

    public openKompletteKorrespondenzPDF(): void {
        if (!this.gesuchModelManager.getGesuch()) {
            return;
        }
        const win = this.downloadRS.prepareDownloadWindow();
        this.downloadRS.getKompletteKorrespondenzAccessTokenGeneratedDokument(this.gesuchModelManager.getGesuch().id)
            .then((downloadFile: TSDownloadFile) => {
                this.downloadRS.startDownload(downloadFile.accessToken, downloadFile.filename, false, win);
            })
            .catch(ex => EbeguUtil.handleDownloadError(win, ex));
    }

    public openMahnungPDF(mahnung: TSMahnung): void {
        const win = this.downloadRS.prepareDownloadWindow();
        this.downloadRS.getAccessTokenMahnungGeneratedDokument(mahnung || this.mahnung)
            .then((downloadFile: TSDownloadFile) => {
                this.downloadRS.startDownload(downloadFile.accessToken, downloadFile.filename, false, win);
            })
            .catch(ex => EbeguUtil.handleDownloadError(win, ex));
    }

    public showBeschwerdeHaengig(): boolean {
        // Schulamt Status duerfen keine Beschwerde starten
        return isAnyStatusOfVerfuegt(this.getAntragStatus()) && !this.getGesuch().gesperrtWegenBeschwerde;
    }

    public showBeschwerdeAbschliessen(): boolean {
        return TSAntragStatus.BESCHWERDE_HAENGIG === this.getAntragStatus();
    }

    public showAbschliessen(): boolean {
        const status = this.getAntragStatus();
        return (TSAntragStatus.IN_BEARBEITUNG_JA === status || TSAntragStatus.GEPRUEFT === status)
            && this.gesuchModelManager.areThereOnlySchulamtAngebote()
            && this.gesuchModelManager.getGesuch().isThereAnyBetreuung();
    }

    public isFinSitChoosen(): boolean {
        return !!(this.getGesuch() && this.getGesuch().finSitStatus);
    }

    public isFinSitAbglehnt(): boolean {
        return this.isFinSitChoosen() && this.getGesuch().finSitStatus !== TSFinSitStatus.AKZEPTIERT;
    }

    public setAbschliessen(): IPromise<TSGesuch> {
        return this.dvDialog.showRemoveDialog(removeDialogTempl, this.form, RemoveDialogController, {
            title: 'GESUCH_ABSCHLIESSEN',
            deleteText: 'BESCHREIBUNG_GESUCH_ABSCHLIESSEN',
            parentController: undefined,
            elementID: undefined,
        }).then(() => {
            return this.gesuchRS.setAbschliessen(this.getGesuch().id).then((gesuch: TSGesuch) => {
                return this.reloadView(gesuch);
            });
        });
    }

    public setGesuchStatusBeschwerdeHaengig(): IPromise<TSGesuch> {
        return this.dvDialog.showRemoveDialog(removeDialogTempl, this.form, RemoveDialogController, {
            title: 'BESCHWERDE_HAENGIG',
            deleteText: 'BESCHREIBUNG_GESUCH_BESCHWERDE_HAENGIG',
            parentController: undefined,
            elementID: undefined,
        }).then(() => {
            return this.gesuchRS.setBeschwerdeHaengig(this.getGesuch().id).then((gesuch: TSGesuch) => {
                this.gesuchModelManager.setGesuch(gesuch);
                return this.gesuchModelManager.getGesuch();
            });
        });
    }

    public setGesuchStatusBeschwerdeAbschliessen(): IPromise<TSGesuch> {
        return this.dvDialog.showRemoveDialog(removeDialogTempl, this.form, RemoveDialogController, {
            title: 'BESCHWERDE_ABSCHLIESSEN',
            deleteText: 'BESCHREIBUNG_GESUCH_BESCHWERDE_ABSCHLIESSEN',
            parentController: undefined,
            elementID: undefined,
        }).then(() => {
            return this.gesuchRS.removeBeschwerdeHaengig(this.getGesuch().id).then((gesuch: TSGesuch) => {
                this.gesuchModelManager.setGesuch(gesuch);
                return this.gesuchModelManager.getGesuch();
            });
        });
    }

    public setGesuchStatusKeinKontingent(): IPromise<TSGesuch> {
        return this.dvDialog.showRemoveDialog(removeDialogTempl, this.form, RemoveDialogController, {
            title: 'CONFIRM_KEIN_KONTINGENT_TITLE',
            deleteText: 'CONFIRM_KEIN_KONTINGENT_TEXT',
            parentController: undefined,
            elementID: undefined,
        }).then(() => {
            return this.gesuchRS.setKeinKontingent(this.getGesuch().id).then((gesuch: TSGesuch) => {
                return this.reloadView(gesuch);
            });
        });
    }

    private reloadView(gesuchFromServer: TSGesuch): TSGesuch {
        this.gesuchModelManager.setGesuch(gesuchFromServer);
        this.refreshKinderListe();
        return this.gesuchModelManager.getGesuch();
    }

    public changeFinSitStatus(): void {
        if (!this.getGesuch().finSitStatus) {
            return;
        }

        this.gesuchRS.changeFinSitStatus(this.getGesuch().id,
            this.getGesuch().finSitStatus).then(() => {
            this.gesuchModelManager.setGesuch(this.getGesuch());
            // Die Berechnungen neu ausführen, da der FinSit-Status (zumindest bei TS) Einfluss hat auf den Tarif
            this.refreshKinderListe();
            this.form.$setPristine();
        });
    }

    public verfuegungEingeschriebenChanged(): void {
        this.gesuchModelManager.updateGesuch();
    }

    public isSuperAdmin(): boolean {
        return this.authServiceRs.isRole(TSRole.SUPER_ADMIN);
    }

    public getTitle(): string {
        const gesuch = this.gesuchModelManager.getGesuch();
        if (this.isGesuchstellerOrSozialdienst()
            && gesuch && !isAnyStatusOfVerfuegt(gesuch.status)) {
            return this.$translate.instant('PROVISORISCHE_BERECHNUNG');
        }
        return this.$translate.instant('VERFUEGUNGEN');
    }

    public isGesuchstellerOrSozialdienst(): boolean {
        return this.authServiceRs.isOneOfRoles(TSRoleUtil.getGesuchstellerSozialdienstRolle());
    }

    public $postLink(): void {
        // tslint:disable-next-line:no-magic-numbers
        this.doPostLinkActions(500);
    }
}
