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
import {DvDialog} from '../../../app/core/directive/dv-dialog/dv-dialog';
import {ApplicationPropertyRS} from '../../../app/core/rest-services/applicationPropertyRS.rest';
import {DownloadRS} from '../../../app/core/service/downloadRS.rest';
import {TSAntragStatus} from '../../../models/enums/TSAntragStatus';
import {TSBetreuungsstatus} from '../../../models/enums/TSBetreuungsstatus';
import {TSWizardStepName} from '../../../models/enums/TSWizardStepName';
import TSBetreuung from '../../../models/TSBetreuung';
import TSDownloadFile from '../../../models/TSDownloadFile';
import TSVerfuegung from '../../../models/TSVerfuegung';
import TSVerfuegungZeitabschnitt from '../../../models/TSVerfuegungZeitabschnitt';
import DateUtil from '../../../utils/DateUtil';
import EbeguUtil from '../../../utils/EbeguUtil';
import {RemoveDialogController} from '../../dialog/RemoveDialogController';
import {ThreeButtonsDialogController} from '../../dialog/ThreeButtonsDialogController';
import {IBetreuungStateParams} from '../../gesuch.route';
import BerechnungsManager from '../../service/berechnungsManager';
import ExportRS from '../../service/exportRS.rest';
import GesuchModelManager from '../../service/gesuchModelManager';
import WizardStepManager from '../../service/wizardStepManager';
import AbstractGesuchViewController from '../abstractGesuchView';
import ITimeoutService = angular.ITimeoutService;

const removeDialogTempl = require('../../dialog/removeDialogTemplate.html');
const threeButtonsDialogTempl = require('../../dialog/threeButtonsDialog.html');

export class VerfuegenViewComponentConfig implements IComponentOptions {
    transclude = false;
    template = require('./verfuegenView.html');
    controller = VerfuegenViewController;
    controllerAs = 'vm';
}

export class VerfuegenViewController extends AbstractGesuchViewController<any> {

    static $inject: string[] = ['$state', 'GesuchModelManager', 'BerechnungsManager', 'EbeguUtil', '$scope', 'WizardStepManager',
        'DvDialog', 'DownloadRS', '$log', '$stateParams', '$window', 'ExportRS', 'ApplicationPropertyRS', '$timeout'];

    //this is the model...
    public bemerkungen: string;

    private showSchemas: boolean;
    private sameVerfuegungsdaten: boolean;
    private sameVerrechneteVerguenstigung: boolean;

    constructor(private readonly $state: StateService, gesuchModelManager: GesuchModelManager, berechnungsManager: BerechnungsManager,
                private readonly ebeguUtil: EbeguUtil, $scope: IScope, wizardStepManager: WizardStepManager,
                private readonly DvDialog: DvDialog, private readonly downloadRS: DownloadRS, private readonly $log: ILogService, $stateParams: IBetreuungStateParams,
                private readonly $window: ng.IWindowService, private readonly exportRS: ExportRS, private readonly applicationPropertyRS: ApplicationPropertyRS,
                $timeout: ITimeoutService) {

        super(gesuchModelManager, berechnungsManager, wizardStepManager, $scope, TSWizardStepName.VERFUEGEN, $timeout);

        const kindIndex: number = this.gesuchModelManager.convertKindNumberToKindIndex(parseInt($stateParams.kindNumber, 10));
        if (kindIndex === -1) {
            this.$log.error('Kind konnte nicht gefunden werden');
        }
        this.gesuchModelManager.setKindIndex(kindIndex);
        const betreuungIndex: number = this.gesuchModelManager.convertBetreuungNumberToBetreuungIndex(parseInt($stateParams.betreuungNumber, 10));
        if (betreuungIndex === -1) {
            this.$log.error('Betreuung konnte nicht gefunden werden');
        }
        this.gesuchModelManager.setBetreuungIndex(betreuungIndex);
        this.wizardStepManager.setCurrentStep(TSWizardStepName.VERFUEGEN);

        this.initView();

        // EBEGE-741: Bemerkungen sollen automatisch zum Inhalt der Verfügung hinzugefügt werden
        if ($scope) {
            $scope.$watch(() => {
                if (this.gesuchModelManager.getGesuch()) {
                    return this.gesuchModelManager.getGesuch().bemerkungen;
                }
                return '';
            }, (newValue, oldValue) => {
                if ((newValue !== oldValue)) {
                    this.setBemerkungen();
                }
            });
        }
    }

    private initView() {
        if (this.gesuchModelManager.getGesuch()) {
            if (this.gesuchModelManager.getVerfuegenToWorkWith()) {
                this.setBemerkungen();
            } else {
                this.gesuchModelManager.calculateVerfuegungen().then(() => {
                    this.setBemerkungen();
                });
            }

            //if finanzielleSituationResultate is undefined/empty (this may happen if user presses reloads this page) then we recalculate it
            if (!this.berechnungsManager.finanzielleSituationResultate || angular.equals(this.berechnungsManager.finanzielleSituationResultate, {})) {
                this.berechnungsManager.calculateFinanzielleSituation(this.gesuchModelManager.getGesuch());
            }
            if (this.gesuchModelManager.getGesuch() && this.gesuchModelManager.getGesuch().extractEinkommensverschlechterungInfo()
                && this.gesuchModelManager.getGesuch().extractEinkommensverschlechterungInfo().ekvFuerBasisJahrPlus1
                && (!this.berechnungsManager.einkommensverschlechterungResultateBjP1 || angular.equals(this.berechnungsManager.einkommensverschlechterungResultateBjP1, {}))) {

                this.berechnungsManager.calculateEinkommensverschlechterung(this.gesuchModelManager.getGesuch(), 1); //.then(() => {});
            }
            if (this.gesuchModelManager.getGesuch() && this.gesuchModelManager.getGesuch().extractEinkommensverschlechterungInfo()
                && this.gesuchModelManager.getGesuch().extractEinkommensverschlechterungInfo().ekvFuerBasisJahrPlus2
                && (!this.berechnungsManager.einkommensverschlechterungResultateBjP2 || angular.equals(this.berechnungsManager.einkommensverschlechterungResultateBjP2, {}))) {

                this.berechnungsManager.calculateEinkommensverschlechterung(this.gesuchModelManager.getGesuch(), 2); //.then(() => {});
            }
            this.initDevModeParameter();

            // folgende Methoden werden hier aufgerufen, weil die Daten sich nicht aendern werden, waehrend man auf der Seite ist.
            // Somit verbessern wir die Performance indem wir diese Daten ganz am Anfang berechnen und in einer Variable speichern
            this.setSameVerfuegungsdaten();
            this.setSameVerrechneteVerfuegungdaten();
        }
    }

    private initDevModeParameter() {
        this.applicationPropertyRS.isDevMode().then((response: boolean) => {
            // Schemas are only visible in devmode
            this.showSchemas = response;
        });
    }

    public cancel(): void {
        this.form.$setPristine();
    }

    private setSameVerfuegungsdaten(): void {
        this.sameVerfuegungsdaten = false; // by default
        if (this.getVerfuegenToWorkWith()) {
            this.sameVerfuegungsdaten = this.getVerfuegenToWorkWith().areSameVerfuegungsdaten();
        }
    }

    /**
     * Checks whether all Abschnitte that are already paid, have the same value of the new abschnitte from
     * the new verfuegung. Returns true if they are the same
     */
    private setSameVerrechneteVerfuegungdaten(): void {
        this.sameVerrechneteVerguenstigung = false; // by default
        if (this.getVerfuegenToWorkWith()) {
            this.sameVerrechneteVerguenstigung = this.getVerfuegenToWorkWith().isSameVerrechneteVerguenstigung();
        }
    }

    public isSameVerfuegungsdaten(): boolean {
        return this.sameVerfuegungsdaten;
    }

    private isSameVerrechneteVerguenstigung(): boolean {
        return this.sameVerrechneteVerguenstigung;
    }

    public save(): void {
        if (this.isGesuchValid()) {
            // wenn Erstgesuch, not KITA oder die neue Verfuegung dieselben Daten hat, wird sie nur gespeichert
            if (!this.getBetreuung().isAngebotKITA() || this.isSameVerrechneteVerguenstigung() || !this.isMutation()) {
                this.saveVerfuegung().then(() => {
                    this.$state.go('gesuch.verfuegen', {
                        gesuchId: this.getGesuchId()
                    });
                });
            } else { // wenn Mutation, und die Verfuegung neue Daten hat, kann sie ignoriert oder uebernommen werden
                this.saveMutierteVerfuegung().then(() => {
                    this.$state.go('gesuch.verfuegen', {
                        gesuchId: this.getGesuchId()
                    });
                });
            }
        }
    }

    public schliessenOhneVerfuegen() {
        if (this.isGesuchValid()) {
            this.verfuegungSchliessenOhenVerfuegen().then(() => {
                this.$state.go('gesuch.verfuegen', {
                    gesuchId: this.getGesuchId()
                });
            });
        }
    }

    public nichtEintreten() {
        if (this.isGesuchValid()) {
            this.verfuegungNichtEintreten().then(() => {
                this.$state.go('gesuch.verfuegen', {
                    gesuchId: this.getGesuchId()
                });
            });
        }
    }

    public getVerfuegenToWorkWith(): TSVerfuegung {
        if (this.gesuchModelManager && this.gesuchModelManager.getGesuch()) {
            return this.gesuchModelManager.getVerfuegenToWorkWith();
        }
        return undefined;
    }

    public getVerfuegungZeitabschnitte(): Array<TSVerfuegungZeitabschnitt> {
        if (this.getVerfuegenToWorkWith()) {
            return this.getVerfuegenToWorkWith().zeitabschnitte;
        }
        return undefined;
    }

    public getFall() {
        if (this.gesuchModelManager) {
            return this.gesuchModelManager.getFall();
        }
        return undefined;
    }

    public getGesuchsperiode() {
        if (this.gesuchModelManager && this.gesuchModelManager.getGesuchsperiode()) {
            return this.gesuchModelManager.getGesuchsperiode();
        }
        return undefined;
    }

    public getBetreuung(): TSBetreuung {
        return this.gesuchModelManager.getBetreuungToWorkWith();
    }

    public getKindName(): string {
        if (this.gesuchModelManager && this.gesuchModelManager.getGesuch()
            && this.gesuchModelManager.getKindToWorkWith() && this.gesuchModelManager.getKindToWorkWith().kindJA) {
            return this.gesuchModelManager.getKindToWorkWith().kindJA.getFullName();
        }
        return undefined;
    }

    public getInstitutionName(): string {
        if (this.gesuchModelManager && this.gesuchModelManager.getGesuch()
            && this.getBetreuung() && this.getBetreuung().institutionStammdaten) {
            return this.getBetreuung().institutionStammdaten.institution.name;
        }
        return undefined;
    }

    public getBetreuungNumber(): string {
        if (this.ebeguUtil && this.gesuchModelManager && this.gesuchModelManager.getGesuch()
            && this.gesuchModelManager.getKindToWorkWith() && this.gesuchModelManager.getBetreuungToWorkWith()) {
            return this.ebeguUtil.calculateBetreuungsId(this.getGesuchsperiode(), this.getFall(), this.gesuchModelManager.getDossier().gemeinde,
                this.gesuchModelManager.getKindToWorkWith().kindNummer, this.getBetreuung().betreuungNummer);
        }
        return undefined;
    }

    public getBetreuungsstatus(): TSBetreuungsstatus {
        if (this.gesuchModelManager && this.gesuchModelManager.getGesuch() && this.gesuchModelManager.getBetreuungToWorkWith()) {
            return this.getBetreuung().betreuungsstatus;
        }
        return undefined;
    }

    public getAnfangsPeriode(): string {
        if (this.ebeguUtil && this.gesuchModelManager.getGesuchsperiode()) {
            return this.ebeguUtil.getFirstDayGesuchsperiodeAsString(this.gesuchModelManager.getGesuchsperiode());
        }
        return undefined;
    }

    public getAnfangsVerschlechterung1(): string {
        if (this.gesuchModelManager && this.gesuchModelManager.getGesuch() && this.gesuchModelManager.getGesuch().extractEinkommensverschlechterungInfo()) {
            return DateUtil.momentToLocalDateFormat(this.gesuchModelManager.getGesuch().extractEinkommensverschlechterungInfo().stichtagFuerBasisJahrPlus1, 'DD.MM.YYYY');
        }
        return undefined;
    }

    public getAnfangsVerschlechterung2(): string {
        if (this.gesuchModelManager && this.gesuchModelManager.getGesuch() && this.gesuchModelManager.getGesuch().extractEinkommensverschlechterungInfo()) {
            return DateUtil.momentToLocalDateFormat(this.gesuchModelManager.getGesuch().extractEinkommensverschlechterungInfo().stichtagFuerBasisJahrPlus2, 'DD.MM.YYYY');
        }
        return undefined;
    }

    /**
     * Nur wenn das Gesuch im Status VERFUEGEN und die Betreuung im Status BESTAETIGT oder STORNIERT
     * sind, kann der Benutzer das Angebot verfuegen. Sonst ist dieses nicht erlaubt.
     * STORNIERT ist erlaubt weil die Kita verantwortlicherBG dafuer ist, die Betreuung in diesem Status zu setzen,
     * d.h. die Betreuung hat bereits diesen Status wenn man auf den Step Verfuegung kommt
     * @returns {boolean}
     */
    public showVerfuegen(): boolean {
        return this.gesuchModelManager.isGesuchStatus(TSAntragStatus.VERFUEGEN)
            && (TSBetreuungsstatus.BESTAETIGT === this.getBetreuungsstatus() || TSBetreuungsstatus.STORNIERT === this.getBetreuungsstatus());
    }

    public saveVerfuegung(): IPromise<TSVerfuegung> {
        return this.DvDialog.showRemoveDialog(removeDialogTempl, this.form, RemoveDialogController, {
            title: 'CONFIRM_SAVE_VERFUEGUNG',
            deleteText: 'BESCHREIBUNG_SAVE_VERFUEGUNG',
            parentController: undefined,
            elementID: undefined
        }).then(() => {
            this.getVerfuegenToWorkWith().manuelleBemerkungen = this.bemerkungen;
            return this.gesuchModelManager.saveVerfuegung(false);
        });
    }

    public saveMutierteVerfuegung(): IPromise<TSVerfuegung> {
        return this.DvDialog.showDialog(threeButtonsDialogTempl, ThreeButtonsDialogController, {
            title: 'CONFIRM_SAVE_MUTIERTE_VERFUEGUNG',
            confirmationText: 'BESCHREIBUNG_SAVE_VERFUEGUNG',
            cancelText: 'LABEL_NEIN',
            firstOkText: 'CONFIRM_MUTIERTE_VERFUEGUNG_UEBERNEHMEN',
            secondOkText: 'CONFIRM_MUTIERTE_VERFUEGUNG_IGNORIEREN'
        }).then((response) => {
            this.getVerfuegenToWorkWith().manuelleBemerkungen = this.bemerkungen;
            return this.gesuchModelManager.saveVerfuegung(response === 2);
        });
    }

    public verfuegungSchliessenOhenVerfuegen(): IPromise<void> {
        return this.DvDialog.showRemoveDialog(removeDialogTempl, this.form, RemoveDialogController, {
            title: 'CONFIRM_CLOSE_VERFUEGUNG_OHNE_VERFUEGEN',
            deleteText: 'BESCHREIBUNG_CLOSE_VERFUEGUNG_OHNE_VERFUEGEN',
            parentController: undefined,
            elementID: undefined
        }).then(() => {
            this.getVerfuegenToWorkWith().manuelleBemerkungen = this.bemerkungen;
            this.gesuchModelManager.verfuegungSchliessenOhenVerfuegen();
        });
    }

    public verfuegungNichtEintreten(): IPromise<TSVerfuegung> {
        return this.DvDialog.showRemoveDialog(removeDialogTempl, this.form, RemoveDialogController, {
            title: 'CONFIRM_CLOSE_VERFUEGUNG_NICHT_EINTRETEN',
            deleteText: 'BESCHREIBUNG_CLOSE_VERFUEGUNG_NICHT_EINTRETEN',
            parentController: undefined,
            elementID: undefined
        }).then(() => {
            this.getVerfuegenToWorkWith().manuelleBemerkungen = this.bemerkungen;
            return this.gesuchModelManager.verfuegungSchliessenNichtEintreten();
        });
    }

    /**
     * Die Bemerkungen sind immer die generierten, es sei denn das Angebot ist schon verfuegt
     */
    private setBemerkungen(): void {
        if (this.getBetreuung()
            && (this.getBetreuung().betreuungsstatus === TSBetreuungsstatus.VERFUEGT ||
                this.getBetreuung().betreuungsstatus === TSBetreuungsstatus.GESCHLOSSEN_OHNE_VERFUEGUNG)) {
            this.bemerkungen = this.getVerfuegenToWorkWith().manuelleBemerkungen;
        } else {
            this.bemerkungen = '';
            if (this.getVerfuegenToWorkWith() && this.getVerfuegenToWorkWith().generatedBemerkungen && this.getVerfuegenToWorkWith().generatedBemerkungen.length > 0) {
                this.bemerkungen = this.getVerfuegenToWorkWith().generatedBemerkungen + '\n';
            }
            if (this.gesuchModelManager.getGesuch() && this.gesuchModelManager.getGesuch().bemerkungen) {
                this.bemerkungen = this.bemerkungen + this.gesuchModelManager.getGesuch().bemerkungen;
            }
        }
    }

    public isBemerkungenDisabled(): boolean {
        return this.gesuchModelManager.getGesuch()
            && (this.gesuchModelManager.getGesuch().status !== TSAntragStatus.VERFUEGEN
                || this.getBetreuung().betreuungsstatus === TSBetreuungsstatus.VERFUEGT
                || this.getBetreuung().betreuungsstatus === TSBetreuungsstatus.GESCHLOSSEN_OHNE_VERFUEGUNG);
    }

    public openVerfuegungPDF(): void {
        const win: Window = this.downloadRS.prepareDownloadWindow();
        this.downloadRS.getAccessTokenVerfuegungGeneratedDokument(this.gesuchModelManager.getGesuch().id,
            this.getBetreuung().id, false, this.bemerkungen)
            .then((downloadFile: TSDownloadFile) => {
                this.$log.debug('accessToken: ' + downloadFile.accessToken);
                this.downloadRS.startDownload(downloadFile.accessToken, downloadFile.filename, false, win);
            })
            .catch((ex) => {
                win.close();
                this.$log.error('An error occurred downloading the document, closing download window.', ex);
            });
    }

    public openExport(): void {
        const win: Window = this.downloadRS.prepareDownloadWindow();
        this.downloadRS.getDokumentAccessTokenVerfuegungExport(this.getBetreuung().id)
            .then((downloadFile: TSDownloadFile) => {
                this.$log.debug('accessToken for export: ' + downloadFile.accessToken);
                this.downloadRS.startDownload(downloadFile.accessToken, downloadFile.filename, true, win);
            })
            .catch((ex) => {
                win.close();
                this.$log.error('An error occurred downloading the document, closing download window.', ex);
            });
    }

    public openNichteintretenPDF(): void {
        const win: Window = this.downloadRS.prepareDownloadWindow();
        this.downloadRS.getAccessTokenNichteintretenGeneratedDokument(this.getBetreuung().id, false)
            .then((downloadFile: TSDownloadFile) => {
                this.$log.debug('accessToken: ' + downloadFile.accessToken);
                this.downloadRS.startDownload(downloadFile.accessToken, downloadFile.filename, false, win);
            })
            .catch(() => {
                win.close();
                this.$log.error('An error occurred downloading the document, closing download window.');
            });
    }

    public showVerfuegungsDetails(): boolean {
        return !this.isBetreuungInStatus(TSBetreuungsstatus.NICHT_EINGETRETEN);
    }

    public showVerfuegungPdfLink(): boolean {
        return !this.isBetreuungInStatus(TSBetreuungsstatus.NICHT_EINGETRETEN);
    }

    public showExportLink(): boolean {
        return this.isBetreuungInStatus(TSBetreuungsstatus.VERFUEGT);
    }

    public exportJsonSchema() {
        const win: Window = this.$window.open('', EbeguUtil.generateRandomName(5));
        this.exportRS.getJsonSchemaString().then((result) => {
            win.document.write('<body><pre>' + result + '</pre></body>');
        });
    }

    public exportXmlSchema() {
        // ACHTUNG popup blocker muss deaktiviert sein
        this.exportRS.getXmlSchemaString().then((result) => {
            this.$window.open('data:application/octet-streem;charset=utf-8,' + result, '', '');
        });
    }

    public showNichtEintretenPdfLink(): boolean {
        const nichtVerfuegt = !this.isBetreuungInStatus(TSBetreuungsstatus.VERFUEGT);
        const mutation = !this.gesuchModelManager.isGesuch();
        const nichtNichteingetreten = !this.isBetreuungInStatus(TSBetreuungsstatus.NICHT_EINGETRETEN);
        return nichtVerfuegt && !(mutation && nichtNichteingetreten);
    }

    public disableAblehnen(): boolean {
        // Der Button "ABLEHNEN" darf im Fall von "STORNIERT" nicht angezeigt werden
        return this.isBetreuungInStatus(TSBetreuungsstatus.STORNIERT);
    }
}
