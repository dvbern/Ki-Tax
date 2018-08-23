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
import {IComponentOptions, ILogService, IPromise, IScope} from 'angular';
import * as moment from 'moment';
import {DvDialog} from '../../../app/core/directive/dv-dialog/dv-dialog';
import {DownloadRS} from '../../../app/core/service/downloadRS.rest';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import {isAnyStatusOfMahnung, isAnyStatusOfVerfuegt, isAtLeastFreigegeben, TSAntragStatus} from '../../../models/enums/TSAntragStatus';
import {TSBetreuungsstatus} from '../../../models/enums/TSBetreuungsstatus';
import {TSFinSitStatus} from '../../../models/enums/TSFinSitStatus';
import {TSMahnungTyp} from '../../../models/enums/TSMahnungTyp';
import {TSRole} from '../../../models/enums/TSRole';
import {TSWizardStepName} from '../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../models/enums/TSWizardStepStatus';
import TSBetreuung from '../../../models/TSBetreuung';
import TSDownloadFile from '../../../models/TSDownloadFile';
import TSGesuch from '../../../models/TSGesuch';
import TSGesuchsperiode from '../../../models/TSGesuchsperiode';
import TSKindContainer from '../../../models/TSKindContainer';
import TSMahnung from '../../../models/TSMahnung';
import {navigateToStartPageForRole} from '../../../utils/AuthenticationUtil';
import EbeguUtil from '../../../utils/EbeguUtil';
import {EnumEx} from '../../../utils/EnumEx';
import {BemerkungenDialogController} from '../../dialog/BemerkungenDialogController';
import {RemoveDialogController} from '../../dialog/RemoveDialogController';
import BerechnungsManager from '../../service/berechnungsManager';
import GesuchModelManager from '../../service/gesuchModelManager';
import GesuchRS from '../../service/gesuchRS.rest';
import MahnungRS from '../../service/mahnungRS.rest';
import WizardStepManager from '../../service/wizardStepManager';
import AbstractGesuchViewController from '../abstractGesuchView';
import ITimeoutService = angular.ITimeoutService;

const removeDialogTempl = require('../../dialog/removeDialogTemplate.html');
const bemerkungDialogTempl = require('../../dialog/bemerkungenDialogTemplate.html');

export class VerfuegenListViewComponentConfig implements IComponentOptions {
    transclude = false;
    bindings = {
        // Bereits vorhandene Mahnungen
        mahnungList: '<'
    };
    template = require('./verfuegenListView.html');
    controller = VerfuegenListViewController;
    controllerAs = 'vm';
}

export class VerfuegenListViewController extends AbstractGesuchViewController<any> {

    static $inject: string[] = ['$state', 'GesuchModelManager', 'BerechnungsManager', 'EbeguUtil', 'WizardStepManager',
        'DvDialog', 'DownloadRS', 'MahnungRS', '$log', 'AuthServiceRS', '$scope', 'GesuchRS', '$timeout'];

    private kinderWithBetreuungList: Array<TSKindContainer>;
    mahnungList: TSMahnung[];
    private mahnung: TSMahnung;
    private tempAntragStatus: TSAntragStatus;
    finSitStatus: Array<string>;

    constructor(private readonly $state: StateService, gesuchModelManager: GesuchModelManager, berechnungsManager: BerechnungsManager,
                private readonly ebeguUtil: EbeguUtil, wizardStepManager: WizardStepManager, private readonly DvDialog: DvDialog,
                private readonly downloadRS: DownloadRS, private readonly mahnungRS: MahnungRS, private readonly $log: ILogService,
                private readonly authServiceRs: AuthServiceRS, $scope: IScope, private readonly gesuchRS: GesuchRS,
                $timeout: ITimeoutService) {

        super(gesuchModelManager, berechnungsManager, wizardStepManager, $scope, TSWizardStepName.VERFUEGEN, $timeout);
        this.initViewModel();
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
        this.wizardStepManager.updateCurrentWizardStepStatus(TSWizardStepStatus.WARTEN);

        //Berechnung aller finanziellen Daten
        if (!this.berechnungsManager.finanzielleSituationResultate) {
            this.berechnungsManager.calculateFinanzielleSituation(this.gesuchModelManager.getGesuch()); //.then(() => {});
        }
        if (this.gesuchModelManager.getGesuch() && this.gesuchModelManager.getGesuch().extractEinkommensverschlechterungInfo()
            && this.gesuchModelManager.getGesuch().extractEinkommensverschlechterungInfo().ekvFuerBasisJahrPlus1
            && !this.berechnungsManager.einkommensverschlechterungResultateBjP1) {

            this.berechnungsManager.calculateEinkommensverschlechterung(this.gesuchModelManager.getGesuch(), 1); //.then(() => {});
        }
        if (this.gesuchModelManager.getGesuch() && this.gesuchModelManager.getGesuch().extractEinkommensverschlechterungInfo()
            && this.gesuchModelManager.getGesuch().extractEinkommensverschlechterungInfo().ekvFuerBasisJahrPlus2
            && !this.berechnungsManager.einkommensverschlechterungResultateBjP2) {

            this.berechnungsManager.calculateEinkommensverschlechterung(this.gesuchModelManager.getGesuch(), 2); //.then(() => {});
        }
        this.refreshKinderListe();
        this.finSitStatus = EnumEx.getNames(TSFinSitStatus);
        this.setHasFSDokumentAccordingToFinSitState();
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
     * @param kind
     * @param betreuung
     */
    public openVerfuegung(kind: TSKindContainer, betreuung: TSBetreuung): void {
        if (this.kannVerfuegungOeffnen(betreuung)) {
            if (kind && betreuung) {
                const kindIndex: number = this.gesuchModelManager.convertKindNumberToKindIndex(kind.kindNummer);
                if (kindIndex >= 0) {
                    this.gesuchModelManager.setKindIndex(kindIndex);
                    this.$state.go('gesuch.verfuegenView', {
                        betreuungNumber: betreuung.betreuungNummer,
                        kindNumber: kind.kindNummer,
                        gesuchId: this.getGesuchId()
                    });
                }
            }
        }
    }

    public kannVerfuegungOeffnen(betreuung: TSBetreuung): boolean {
        return this.isDetailAvailableForGesuchstatus() && this.isDetailAvailableForBetreuungstatus(betreuung.betreuungsstatus);
    }

    private isDetailAvailableForGesuchstatus(): boolean {
        const isGesuchsteller: boolean = this.authServiceRs.isRole(TSRole.GESUCHSTELLER);
        //gesuchsteller hat sicher mal nur Zugriff auf verfuegungsdetail wenn das gesuch mindestens freiggeben ist
        if (isGesuchsteller) {
            return isAtLeastFreigegeben(this.getAntragStatus());
        } else {
            return true;
        }
    }

    private isDetailAvailableForBetreuungstatus(betreuungsstatus: TSBetreuungsstatus): boolean {
        const isGesuchsteller: boolean = this.authServiceRs.isRole(TSRole.GESUCHSTELLER);
        const allowedBetstatus: Array<TSBetreuungsstatus> = [TSBetreuungsstatus.VERFUEGT, TSBetreuungsstatus.NICHT_EINGETRETEN, TSBetreuungsstatus.STORNIERT];
        //Annahme: alle ausser Gesuchsteller duerfen bestaetigte betreuungen sehen wenn sie uberhaupt auf die Seite kommen
        if (!isGesuchsteller) {
            allowedBetstatus.push(TSBetreuungsstatus.BESTAETIGT);
        }
        return allowedBetstatus.indexOf(betreuungsstatus) !== -1;
    }

    /**
     * das FinanzielleSituation PDF ist fuer den Gesuchsteller erst sichtbar sobald der Antrag den Status VERFUEGT
     * erreicht hat
     */
    public isFinanziellesituationPDFVisible(): boolean {
        if (!this.gesuchModelManager.isFinanzielleSituationEnabled() || !this.gesuchModelManager.isFinanzielleSituationDesired()) {
            return false;
        }
        const isGesuchsteller: boolean = this.authServiceRs.isRole(TSRole.GESUCHSTELLER);
        if (isGesuchsteller) {
            return isAnyStatusOfVerfuegt(this.getAntragStatus()) && this.getGesuch().hasFSDokument && !this.isFinSitAbglehnt();
        }
        return this.getGesuch().hasFSDokument && !this.isFinSitAbglehnt();

    }

    public isFinanzielleSituationDesired(): boolean {
        return this.gesuchModelManager.isFinanzielleSituationDesired();
    }

    public isBegleitschreibenVisible(): boolean {
        const isGesuchsteller: boolean = this.authServiceRs.isRole(TSRole.GESUCHSTELLER);
        if (isGesuchsteller) {
            return isAnyStatusOfVerfuegt(this.getAntragStatus()) && !this.gesuchModelManager.areThereOnlySchulamtAngebote() && !this.gesuchModelManager.areThereOnlyGeschlossenOhneVerfuegung();
        }
        return !this.gesuchModelManager.areThereOnlySchulamtAngebote() && !this.gesuchModelManager.areThereOnlyGeschlossenOhneVerfuegung();
    }

    public isKompletteKorrespondenzVisible(): boolean {
        const status = this.getAntragStatus();
        return this.isBegleitschreibenVisible() && isAnyStatusOfVerfuegt(status) && this.authServiceRs.isOneOfRoles(this.TSRoleUtil.getJugendamtAndSchulamtRole());
    }

    private getAntragStatus(): TSAntragStatus {
        const status: TSAntragStatus = this.getGesuch() ? this.getGesuch().status : TSAntragStatus.IN_BEARBEITUNG_GS;
        return status;
    }

    public getFall() {
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
        return this.DvDialog.showRemoveDialog(removeDialogTempl, this.form, RemoveDialogController, {
            title: 'CONFIRM_GESUCH_STATUS_GEPRUEFT',
            deleteText: 'BESCHREIBUNG_GESUCH_STATUS_WECHSELN',
            parentController: undefined,
            elementID: undefined
        }).then(() => {
            return this.setGesuchStatus(TSAntragStatus.GEPRUEFT);
        });
    }

    public closeWithoutAngebot(): IPromise<TSGesuch> {
        return this.DvDialog.showRemoveDialog(removeDialogTempl, this.form, RemoveDialogController, {
            title: 'CONFIRM_GESUCH_STATUS_KEIN_ANGEBOT',
            deleteText: 'BESCHREIBUNG_GESUCH_STATUS_WECHSELN',
            parentController: undefined,
            elementID: undefined

        }).then(() => {
            return this.gesuchRS.closeWithoutAngebot(this.gesuchModelManager.getGesuch().id).then((response) => {  // muss gespeichert werden um hasfsdokument zu aktualisieren
                this.gesuchModelManager.setGesuch(response);
                this.form.$setPristine(); // nach dem es gespeichert wird, muessen wir das Form wieder auf clean setzen
                return this.refreshKinderListe().then(() => {
                    return this.gesuchModelManager.getGesuch();
                });
            });
        });
    }

    public setGesuchStatusVerfuegen(): IPromise<TSGesuch> {
        const deleteTextValue: string = 'BESCHREIBUNG_GESUCH_STATUS_WECHSELN';
        return this.DvDialog.showRemoveDialog(removeDialogTempl, this.form, RemoveDialogController, {
            title: 'CONFIRM_GESUCH_STATUS_VERFUEGEN',
            deleteText: deleteTextValue,
            parentController: undefined,
            elementID: undefined
        }).then(() => {

            return this.gesuchRS.verfuegenStarten(
                this.gesuchModelManager.getGesuch().id, this.gesuchModelManager.getGesuch().hasFSDokument).then((response) => {  // muss gespeichert werden um hasfsdokument zu aktualisieren
                if (response.status === TSAntragStatus.NUR_SCHULAMT) {
                    // If AntragStatus==NUR_SCHULAMT the Sachbearbeiter_JA has no rights to work with or even to see this gesuch any more
                    // For this reason we have to navigate directly out of the gesuch once it has been saved. We navigate to the
                    // default start page for the current role.
                    // createNeededPDFs is not being called for the same reason. Anyway, the Gesuch vanishes for the role JA and is only
                    // available for the role SACHBEARBEITER_TS/ADMIN_TS, so JA doesn't need the PDFs to be created. When a Schulamt worker opens this
                    // Gesuch, she can generate the PDFs by clicking on the corresponding links
                    navigateToStartPageForRole(this.authServiceRs.getPrincipal().getCurrentRole(), this.$state);
                    return this.gesuchModelManager.getGesuch();
                } else { // for NUR_SCHULAMT this makes no sense
                    this.gesuchModelManager.setGesuch(response);
                    this.form.$setPristine(); // nach dem es gespeichert wird, muessen wir das Form wieder auf clean setzen
                    return this.refreshKinderListe().then(() => {
                        return this.gesuchModelManager.getGesuch();
                    });
                }
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
        this.DvDialog.showDialog(bemerkungDialogTempl, BemerkungenDialogController, {
            title: 'SEND_TO_STV_CONFIRMATION',
            bemerkungen: this.gesuchModelManager.getGesuch().bemerkungenSTV
        }).then((bemerkung: string) => {
            this.gesuchRS.sendGesuchToSTV(this.getGesuch().id, bemerkung).then((gesuch: TSGesuch) => {
                this.gesuchModelManager.setGesuch(gesuch);
            });
        });
    }

    public showSendToSteuerverwaltung(): boolean {
        //hier wird extra nur "VERFUEGT" gestestet statt alle verfuegten status weil das Schulamt das Gesuch nicht pruefen lassen darf
        return (this.gesuchModelManager.isGesuchStatus(TSAntragStatus.VERFUEGT) || this.gesuchModelManager.isGesuchStatus(TSAntragStatus.NUR_SCHULAMT))
            && !this.getGesuch().gesperrtWegenBeschwerde;
    }

    public stvPruefungAbschliessen(): void {
        this.DvDialog.showRemoveDialog(removeDialogTempl, this.form, RemoveDialogController, {
            title: 'STV_PRUEFUNG_ABSCHLIESSEN_CONFIRMATION',
            deleteText: '',
            parentController: undefined,
            elementID: undefined
        }).then((bemerkung: string) => {
            this.gesuchRS.stvPruefungAbschliessen(this.getGesuch().id).then((gesuch: TSGesuch) => {
                this.gesuchModelManager.setGesuch(gesuch);
            });
        });
    }

    public showSTVPruefungAbschliessen(): boolean {
        return this.gesuchModelManager.isGesuchStatus(TSAntragStatus.GEPRUEFT_STV) && !this.getGesuch().gesperrtWegenBeschwerde;
    }

    public showErsteMahnungErstellen(): boolean {
        // Nur wenn keine offenen Mahnungen vorhanden!
        return (this.gesuchModelManager.isGesuchStatus(TSAntragStatus.IN_BEARBEITUNG_JA) || this.gesuchModelManager.isGesuchStatus(TSAntragStatus.FREIGEGEBEN))
            && this.mahnung === undefined && !this.hasOffeneMahnungen() && !this.isGesuchReadonly();
    }

    public showErsteMahnungAusloesen(): boolean {
        return this.mahnung !== undefined && this.mahnung.mahnungTyp === TSMahnungTyp.ERSTE_MAHNUNG
            && !this.isGesuchReadonly();
    }

    public showZweiteMahnungErstellen(): boolean {
        return this.gesuchModelManager.isGesuchStatus(TSAntragStatus.ERSTE_MAHNUNG_ABGELAUFEN)
            && this.mahnung === undefined && !this.isGesuchReadonly();
    }

    public showZweiteMahnungAusloesen(): boolean {
        return this.mahnung !== undefined && this.mahnung.mahnungTyp === TSMahnungTyp.ZWEITE_MAHNUNG
            && !this.isGesuchReadonly();
    }

    /**
     * Nur required in Status VERFUEGEN oder GEPRUEFT und wenn der Benutzer nicht am Erstellen einer Mahnung ist.
     */
    public isFinSitStatusRequired(): boolean {
        return !this.showErsteMahnungAusloesen() && !this.showZweiteMahnungAusloesen()
            && (this.getGesuch().status === TSAntragStatus.VERFUEGEN || this.getGesuch().status === TSAntragStatus.GEPRUEFT);
    }

    public showMahnlaufBeenden(): boolean {
        return isAnyStatusOfMahnung(this.getGesuch().status) && !this.isGesuchReadonly();
    }

    public showDokumenteNichtKomplett(): boolean {
        return isAnyStatusOfMahnung(this.getGesuch().status) && this.getGesuch().dokumenteHochgeladen
            && !this.isGesuchReadonly();
    }

    public showZweiteMahnungNichtEingetreten(): boolean {
        return this.gesuchModelManager.isGesuchStatus(TSAntragStatus.ZWEITE_MAHNUNG_ABGELAUFEN) && !this.isGesuchReadonly();
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
        if (this.form.$valid) {
            this.mahnungRS.saveMahnung(this.mahnung).then((mahnungResponse: TSMahnung) => {
                this.setGesuchStatus(this.tempAntragStatus).then(any => {
                    this.mahnungList.push(mahnungResponse);
                    this.tempAntragStatus = undefined;
                    this.mahnung = undefined;
                });
            });
        }
    }

    private createMahnung(typ: TSMahnungTyp): IPromise<any> {
        return this.mahnungRS.getInitialeBemerkungen(this.getGesuch()).then(generatedBemerkungen => {
            this.mahnung = new TSMahnung();
            this.mahnung.mahnungTyp = typ;
            this.mahnung.gesuch = this.getGesuch();
            this.mahnung.timestampAbgeschlossen = null;
            this.mahnung.bemerkungen = generatedBemerkungen.data;
            if (this.getGesuchsperiode().hasTagesschulenAnmeldung() && this.getGesuch().areThereOnlySchulamtAngebote()) {
                this.mahnung.datumFristablauf = moment(moment.now()).add(7, 'days');
            }
            return;
        });
    }

    public mahnlaufBeenden(): void {
        // Gesuchstatus zuruecksetzen UND die Mahnungen auf erledigt setzen
        this.mahnungRS.mahnlaufBeenden(this.getGesuch()).then((gesuch: TSGesuch) => {
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
     * @returns {boolean}
     */
    public showGeprueft(): boolean {
        return (this.gesuchModelManager.isGesuchStatus(TSAntragStatus.IN_BEARBEITUNG_JA) || this.gesuchModelManager.isGesuchStatus(TSAntragStatus.FREIGEGEBEN))
            && this.wizardStepManager.areAllStepsOK(this.getGesuch()) && this.mahnung === undefined
            && (!this.gesuchModelManager.areThereOnlySchulamtAngebote() || !this.gesuchModelManager.getGesuch().isThereAnyBetreuung())
            && !this.isGesuchReadonly();
    }

    /**
     * Der Button Verfuegung starten wird angezeigt, wenn alle Betreuungen bestaetigt und das Gesuch geprueft wurden
     * @returns {boolean}
     */
    public showVerfuegenStarten(): boolean {
        return this.gesuchModelManager.isGesuchStatus(TSAntragStatus.GEPRUEFT)
            && this.wizardStepManager.isStepStatusOk(TSWizardStepName.BETREUUNG)
            && this.gesuchModelManager.getGesuch().isThereAnyBetreuung()
            && !this.gesuchModelManager.areThereOnlySchulamtAngebote()
            && !this.isGesuchReadonly();
        // && this.gesuchModelManager.getGesuch().status !== TSAntragStatus.VERFUEGEN;
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
        const win: Window = this.downloadRS.prepareDownloadWindow();
        this.downloadRS.getFinSitDokumentAccessTokenGeneratedDokument(this.gesuchModelManager.getGesuch().id)
            .then((downloadFile: TSDownloadFile) => {
                this.$log.debug('accessToken: ' + downloadFile.accessToken);
                this.downloadRS.startDownload(downloadFile.accessToken, downloadFile.filename, false, win);
            })
            .catch((ex) => {
                win.close();
                this.$log.error('An error occurred downloading the document, closing download window.');
            });
    }

    public openBegleitschreibenPDF(): void {
        const win: Window = this.downloadRS.prepareDownloadWindow();
        this.downloadRS.getBegleitschreibenDokumentAccessTokenGeneratedDokument(this.gesuchModelManager.getGesuch().id)
            .then((downloadFile: TSDownloadFile) => {
                this.$log.debug('accessToken: ' + downloadFile.accessToken);
                this.downloadRS.startDownload(downloadFile.accessToken, downloadFile.filename, false, win);
            })
            .catch((ex) => {
                win.close();
                this.$log.error('An error occurred downloading the document, closing download window.');
            });
    }

    public openKompletteKorrespondenzPDF(): void {
        const win: Window = this.downloadRS.prepareDownloadWindow();
        this.downloadRS.getKompletteKorrespondenzAccessTokenGeneratedDokument(this.gesuchModelManager.getGesuch().id)
            .then((downloadFile: TSDownloadFile) => {
                this.$log.debug('accessToken: ' + downloadFile.accessToken);
                this.downloadRS.startDownload(downloadFile.accessToken, downloadFile.filename, false, win);
            })
            .catch((ex) => {
                win.close();
                this.$log.error('An error occurred downloading the document, closing download window.', ex);
            });
    }

    public openMahnungPDF(mahnung: TSMahnung): void {
        const win: Window = this.downloadRS.prepareDownloadWindow();
        if (mahnung == null) {
            mahnung = this.mahnung;
        }
        this.downloadRS.getAccessTokenMahnungGeneratedDokument(mahnung)
            .then((downloadFile: TSDownloadFile) => {
                this.$log.debug('accessToken: ' + downloadFile.accessToken);
                this.downloadRS.startDownload(downloadFile.accessToken, downloadFile.filename, false, win);
            })
            .catch((ex) => {
                win.close();
                this.$log.error('An error occurred downloading the document, closing download window.');
            });
    }

    public showBeschwerdeHaengig(): boolean {
        // Schulamt Status duerfen keine Beschwerde starten
        return isAnyStatusOfVerfuegt(this.getAntragStatus()) && !this.getGesuch().gesperrtWegenBeschwerde;
    }

    public showBeschwerdeAbschliessen(): boolean {
        return TSAntragStatus.BESCHWERDE_HAENGIG === this.getAntragStatus();
    }

    public showAbschliessen(): boolean {
        const status: TSAntragStatus = this.getAntragStatus();
        return (TSAntragStatus.IN_BEARBEITUNG_JA === status || TSAntragStatus.GEPRUEFT === status)
            && this.gesuchModelManager.areThereOnlySchulamtAngebote() && this.gesuchModelManager.getGesuch().isThereAnyBetreuung();
    }

    public isFinSitChoosen(): boolean {
        if (this.getGesuch() && this.getGesuch().finSitStatus) {
            return true;
        }
        return false;
    }

    public isFinSitAbglehnt() {
        if (this.isFinSitChoosen() && this.getGesuch().finSitStatus !== TSFinSitStatus.AKZEPTIERT) {
            return true;
        }
        return false;
    }

    public setAbschliessen(): IPromise<TSGesuch> {
        return this.DvDialog.showRemoveDialog(removeDialogTempl, this.form, RemoveDialogController, {
            title: 'ABSCHLIESSEN',
            deleteText: 'BESCHREIBUNG_GESUCH_ABSCHLIESSEN',
            parentController: undefined,
            elementID: undefined
        }).then(() => {
            return this.gesuchRS.setAbschliessen(this.getGesuch().id).then((gesuch: TSGesuch) => {
                this.gesuchModelManager.setGesuch(gesuch);
                this.setHasFSDokumentAccordingToFinSitState();
                return this.gesuchModelManager.getGesuch();
            });
        });
    }

    public setGesuchStatusBeschwerdeHaengig(): IPromise<TSGesuch> {
        return this.DvDialog.showRemoveDialog(removeDialogTempl, this.form, RemoveDialogController, {
            title: 'BESCHWERDE_HAENGIG',
            deleteText: 'BESCHREIBUNG_GESUCH_BESCHWERDE_HAENGIG',
            parentController: undefined,
            elementID: undefined
        }).then(() => {
            return this.gesuchRS.setBeschwerdeHaengig(this.getGesuch().id).then((gesuch: TSGesuch) => {
                this.gesuchModelManager.setGesuch(gesuch);
                return this.gesuchModelManager.getGesuch();
            });
        });
    }

    public setGesuchStatusBeschwerdeAbschliessen(): IPromise<TSGesuch> {
        return this.DvDialog.showRemoveDialog(removeDialogTempl, this.form, RemoveDialogController, {
            title: 'BESCHWERDE_ABSCHLIESSEN',
            deleteText: 'BESCHREIBUNG_GESUCH_BESCHWERDE_ABSCHLIESSEN',
            parentController: undefined,
            elementID: undefined
        }).then(() => {
            return this.gesuchRS.removeBeschwerdeHaengig(this.getGesuch().id).then((gesuch: TSGesuch) => {
                this.gesuchModelManager.setGesuch(gesuch);
                return this.gesuchModelManager.getGesuch();
            });
        });
    }

    public changeFinSitStatus() {
        if (this.getGesuch().finSitStatus) {
            this.setHasFSDokumentAccordingToFinSitState();
            this.gesuchRS.changeFinSitStatus(this.getGesuch().id, this.getGesuch().finSitStatus).then((response: any) => {
                this.gesuchModelManager.setGesuch(this.getGesuch());
                this.form.$setPristine();
            });
        }
    }

    private setHasFSDokumentAccordingToFinSitState() {
        if (this.isFinSitAbglehnt()) {
            this.getGesuch().hasFSDokument = false;
        } else {
            this.getGesuch().hasFSDokument = true;
        }
    }

    public fsDokumentChanged(): void {
        // dirty checker wird hier ausgeschaltet. Aenderungen des fs flag wird automatisch gespeichert wenn gesuch auf geprüft gesetzt wird
        // Aus performance Gründen wird hier daruf verzichtet das Gesuch neu zu persisten, nur weil das Flag ändert.
        this.form.$setPristine();
    }

    public isSuperAdmin(): boolean {
        return this.authServiceRs.isRole(TSRole.SUPER_ADMIN);
    }

    $postLink() {
        this.$timeout(() => {
            EbeguUtil.selectFirst();
        }, 500);
    }
}
