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

import {StateService, TransitionPromise} from '@uirouter/core';
import {IComponentOptions, ILogService, IPromise, IScope, IWindowService} from 'angular';
import {DvDialog} from '../../../app/core/directive/dv-dialog/dv-dialog';
import {ApplicationPropertyRS} from '../../../app/core/rest-services/applicationPropertyRS.rest';
import {DownloadRS} from '../../../app/core/service/downloadRS.rest';
import {I18nServiceRSRest} from '../../../app/i18n/services/i18nServiceRS.rest';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import {getTSAbholungTagesschuleValues, TSAbholungTagesschule} from '../../../models/enums/TSAbholungTagesschule';
import {TSAntragStatus} from '../../../models/enums/TSAntragStatus';
import {TSBetreuungsstatus} from '../../../models/enums/TSBetreuungsstatus';
import {TSBrowserLanguage} from '../../../models/enums/TSBrowserLanguage';
import {getWeekdaysValues, TSDayOfWeek} from '../../../models/enums/TSDayOfWeek';
import {TSRole} from '../../../models/enums/TSRole';
import {TSWizardStepName} from '../../../models/enums/TSWizardStepName';
import TSBelegungTagesschuleModulGroup from '../../../models/TSBelegungTagesschuleModulGroup';
import TSBetreuung from '../../../models/TSBetreuung';
import TSDownloadFile from '../../../models/TSDownloadFile';
import TSModulTagesschuleGroup from '../../../models/TSModulTagesschuleGroup';
import TSVerfuegung from '../../../models/TSVerfuegung';
import TSVerfuegungZeitabschnitt from '../../../models/TSVerfuegungZeitabschnitt';
import EbeguUtil from '../../../utils/EbeguUtil';
import {TagesschuleUtil} from '../../../utils/TagesschuleUtil';
import {RemoveDialogController} from '../../dialog/RemoveDialogController';
import {StepDialogController} from '../../dialog/StepDialogController';
import {IBetreuungStateParams} from '../../gesuch.route';
import BerechnungsManager from '../../service/berechnungsManager';
import ExportRS from '../../service/exportRS.rest';
import GesuchModelManager from '../../service/gesuchModelManager';
import WizardStepManager from '../../service/wizardStepManager';
import AbstractGesuchViewController from '../abstractGesuchView';
import ITimeoutService = angular.ITimeoutService;

const removeDialogTempl = require('../../dialog/removeDialogTemplate.html');
const stepDialogTempl = require('../../dialog/stepDialog.html');

export class VerfuegenViewComponentConfig implements IComponentOptions {
    public transclude = false;
    public template = require('./verfuegenView.html');
    public controller = VerfuegenViewController;
    public controllerAs = 'vm';
}

export class VerfuegenViewController extends AbstractGesuchViewController<any> {

    public static $inject: string[] = [
        '$state',
        'GesuchModelManager',
        'BerechnungsManager',
        'EbeguUtil',
        '$scope',
        'WizardStepManager',
        'DvDialog',
        'DownloadRS',
        '$log',
        '$stateParams',
        '$window',
        'ExportRS',
        'ApplicationPropertyRS',
        '$timeout',
        'AuthServiceRS',
        'I18nServiceRSRest',
    ];

    // this is the model...
    public bemerkungen: string;

    public showSchemas: boolean;
    public sameVerfuegteVerfuegungsrelevanteDaten: boolean;
    public fragenObIgnorieren: boolean;
    public verfuegungsBemerkungenKontrolliert: boolean = false;
    public isVerfuegenClicked: boolean = false;

    public modulGroups: TSBelegungTagesschuleModulGroup[] = [];

    public constructor(
        private readonly $state: StateService,
        gesuchModelManager: GesuchModelManager,
        berechnungsManager: BerechnungsManager,
        private readonly ebeguUtil: EbeguUtil,
        $scope: IScope,
        wizardStepManager: WizardStepManager,
        private readonly dvDialog: DvDialog,
        private readonly downloadRS: DownloadRS,
        private readonly $log: ILogService,
        $stateParams: IBetreuungStateParams,
        private readonly $window: IWindowService,
        private readonly exportRS: ExportRS,
        private readonly applicationPropertyRS: ApplicationPropertyRS,
        $timeout: ITimeoutService,
        private readonly authServiceRs: AuthServiceRS,
        private readonly i18nServiceRS: I18nServiceRSRest,
    ) {

        super(gesuchModelManager, berechnungsManager, wizardStepManager, $scope, TSWizardStepName.VERFUEGEN, $timeout);

        const kindIndex = this.gesuchModelManager.convertKindNumberToKindIndex(parseInt($stateParams.kindNumber, 10));
        if (kindIndex === -1) {
            this.$log.error('Kind konnte nicht gefunden werden');
        }
        this.gesuchModelManager.setKindIndex(kindIndex);
        const betreuungNumber = parseInt($stateParams.betreuungNumber, 10);
        const betreuungIndex = this.gesuchModelManager.convertBetreuungNumberToBetreuungIndex(betreuungNumber);
        if (betreuungIndex === -1) {
            this.$log.error('Betreuung konnte nicht gefunden werden');
        }
        this.gesuchModelManager.setBetreuungIndex(betreuungIndex);
        this.wizardStepManager.setCurrentStep(TSWizardStepName.VERFUEGEN);

        this.initView();

        // EBEGE-741: Bemerkungen sollen automatisch zum Inhalt der Verfügung hinzugefügt werden
        if (!$scope) {
            return;
        }

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

    private initView(): void {
        const gesuch = this.gesuchModelManager.getGesuch();
        if (!gesuch) {
            return;
        }
        if (this.isTagesschuleVerfuegung()) {
            this.modulGroups = TagesschuleUtil.initModuleTagesschule(this.getBetreuung(), this.gesuchModelManager.getGesuchsperiode(), true);
        }

        if (this.gesuchModelManager.getVerfuegenToWorkWith()) {
            this.setBemerkungen();
            this.setParamsDependingOnCurrentVerfuegung();
        } else {
            this.gesuchModelManager.calculateVerfuegungen().then(() => {
                this.setBemerkungen();
                this.setParamsDependingOnCurrentVerfuegung();
            });
        }
        this.initDevModeParameter();
    }

    private setParamsDependingOnCurrentVerfuegung(): void {
        this.setSameVerfuegteVerfuegungsrelevanteDaten();
        this.setFragenObIgnorieren();
    }

    private initDevModeParameter(): void {
        this.applicationPropertyRS.isDevMode().then((response: boolean) => {
            // Schemas are only visible in devmode
            this.showSchemas = response;
        });
    }

    public cancel(): void {
        this.form.$setPristine();
    }

    private setSameVerfuegteVerfuegungsrelevanteDaten(): void {
        this.sameVerfuegteVerfuegungsrelevanteDaten = false; // by default
        if (this.getVerfuegenToWorkWith()) {
            this.sameVerfuegteVerfuegungsrelevanteDaten = this.getVerfuegenToWorkWith().areSameVerfuegteVerfuegungsrelevanteDaten();
        }
    }

    public isSameVerfuegteVerfuegungsrelevanteDaten(): boolean {
        return this.sameVerfuegteVerfuegungsrelevanteDaten;
    }

    /**
     * Checks whether all Abschnitte that are already paid, have the same value of the new abschnitte from
     * the new verfuegung. Returns true if they are the same
     */
    private setFragenObIgnorieren(): void {
        this.fragenObIgnorieren = false; // by default
        if (this.getVerfuegenToWorkWith()) {
            this.fragenObIgnorieren = this.getVerfuegenToWorkWith().fragenObIgnorieren();
        }
    }

    private isFragenObIgnorieren(): boolean {
        return this.fragenObIgnorieren;
    }

    private isAlreadyIgnored(): boolean {
        if (this.getVerfuegenToWorkWith()) {
            return this.getVerfuegenToWorkWith().isAlreadyIgnored();
        }
        return false; // by default
    }

    public save(): void {
        this.isVerfuegenClicked = true;
        if (!this.isGesuchValid() || !this.isVerfuegenValid()) {
            return;
        }

        const direktVerfuegen = !this.isFragenObIgnorieren() || !this.isMutation()
            || this.isAlreadyIgnored();
        // Falls es bereits ignoriert war, soll eine Warung angezeigt werden
        if (this.isAlreadyIgnored()) {
            this.dvDialog.showRemoveDialog(removeDialogTempl, this.form, RemoveDialogController, {
                title: 'CONFIRM_ALREADY_IGNORED',
                deleteText: 'BESCHREIBUNG_CONFIRM_ALREADY_IGNORED',
                parentController: undefined,
                elementID: undefined,
            }).then(() => {
                const promise = this.askForIgnoringIfNecessaryAndSaveVerfuegung(direktVerfuegen);
                promise.then(() => this.goToVerfuegen());
            });
        } else {
            const promise = this.askForIgnoringIfNecessaryAndSaveVerfuegung(direktVerfuegen);
            promise.then(() => this.goToVerfuegen());
        }
    }

    private askForIgnoringIfNecessaryAndSaveVerfuegung(direktVerfuegen: boolean): IPromise<TSVerfuegung> {
        return direktVerfuegen
            ? this.saveVerfuegung()
            // wenn Mutation, und die Verfuegung neue Daten hat, kann sie ignoriert oder uebernommen werden
            : this.saveMutierteVerfuegung();
    }

    private isVerfuegenValid(): boolean {
        return this.verfuegungsBemerkungenKontrolliert && EbeguUtil.isNotNullOrUndefined(this.bemerkungen);
    }

    private goToVerfuegen(): TransitionPromise {
        return this.$state.go('gesuch.verfuegen', {
            gesuchId: this.getGesuchId(),
        });
    }

    // noinspection JSUnusedGlobalSymbols
    public schliessenOhneVerfuegen(): void {
        if (!this.isGesuchValid()) {
            return;
        }

        this.verfuegungSchliessenOhenVerfuegen().then(() => this.goToVerfuegen());
    }

    // noinspection JSUnusedGlobalSymbols
    public nichtEintreten(): void {
        if (!this.isGesuchValid()) {
            return;
        }

        this.verfuegungNichtEintreten().then(() => this.goToVerfuegen());
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

    public getFall(): any {
        if (this.gesuchModelManager) {
            return this.gesuchModelManager.getFall();
        }
        return undefined;
    }

    public getGesuchsperiode(): any {
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

    public getInstitutionPhone(): string {
        if (this.gesuchModelManager && this.gesuchModelManager.getGesuch()
            && this.getBetreuung() && this.getBetreuung().institutionStammdaten) {
            return this.getBetreuung().institutionStammdaten.telefon;
        }
        return undefined;
    }

    public getBetreuungNumber(): string {
        if (this.ebeguUtil && this.gesuchModelManager && this.gesuchModelManager.getGesuch()
            && this.gesuchModelManager.getKindToWorkWith() && this.gesuchModelManager.getBetreuungToWorkWith()) {
            return this.ebeguUtil.calculateBetreuungsId(this.getGesuchsperiode(),
                this.getFall(),
                this.gesuchModelManager.getDossier().gemeinde,
                this.gesuchModelManager.getKindToWorkWith().kindNummer,
                this.getBetreuung().betreuungNummer);
        }
        return undefined;
    }

    public getBetreuungsstatus(): TSBetreuungsstatus {
        if (!this.gesuchModelManager
            || !this.gesuchModelManager.getGesuch()
            || !this.gesuchModelManager.getBetreuungToWorkWith()) {
            return undefined;
        }
        return this.getBetreuung().betreuungsstatus;
    }

    /**
     * Nur wenn das Gesuch im Status VERFUEGEN und die Betreuung im Status BESTAETIGT oder STORNIERT
     * sind, kann der Benutzer das Angebot verfuegen. Sonst ist dieses nicht erlaubt.
     * STORNIERT ist erlaubt weil die Kita verantwortlicherBG dafuer ist, die Betreuung in diesem Status zu setzen,
     * d.h. die Betreuung hat bereits diesen Status wenn man auf den Step Verfuegung kommt
     */
    public showVerfuegen(): boolean {
        return this.gesuchModelManager.isGesuchStatus(TSAntragStatus.VERFUEGEN)
            && [TSBetreuungsstatus.BESTAETIGT, TSBetreuungsstatus.STORNIERT].includes(this.getBetreuungsstatus())
            && !this.isTagesschuleVerfuegung();
    }

    public saveVerfuegung(): IPromise<TSVerfuegung> {
        return this.dvDialog.showRemoveDialog(removeDialogTempl, this.form, RemoveDialogController, {
            title: 'CONFIRM_SAVE_VERFUEGUNG',
            deleteText: 'BESCHREIBUNG_SAVE_VERFUEGUNG',
            parentController: undefined,
            elementID: undefined,
        }).then(() => {
            this.getVerfuegenToWorkWith().manuelleBemerkungen = this.bemerkungen;
            this.isVerfuegenClicked = false;
            return this.gesuchModelManager.saveVerfuegung(false);
        });
    }

    public saveMutierteVerfuegung(): IPromise<TSVerfuegung> {
        return this.dvDialog.showDialog(stepDialogTempl, StepDialogController, {
            institutionName: this.getInstitutionName(),
            institutionPhone: this.getInstitutionPhone(),
        }).then(response => {
            this.getVerfuegenToWorkWith().manuelleBemerkungen = this.bemerkungen;
            this.isVerfuegenClicked = false;
            return this.gesuchModelManager.saveVerfuegung(response === 2);
        });
    }

    public verfuegungSchliessenOhenVerfuegen(): IPromise<void> {
        return this.dvDialog.showRemoveDialog(removeDialogTempl, this.form, RemoveDialogController, {
            title: 'CONFIRM_CLOSE_VERFUEGUNG_OHNE_VERFUEGEN',
            deleteText: 'BESCHREIBUNG_CLOSE_VERFUEGUNG_OHNE_VERFUEGEN',
            parentController: undefined,
            elementID: undefined,
        }).then(() => {
            this.getVerfuegenToWorkWith().manuelleBemerkungen = this.bemerkungen;
            this.gesuchModelManager.verfuegungSchliessenOhenVerfuegen();
        });
    }

    public verfuegungNichtEintreten(): IPromise<TSVerfuegung> {
        return this.dvDialog.showRemoveDialog(removeDialogTempl, this.form, RemoveDialogController, {
            title: 'CONFIRM_CLOSE_VERFUEGUNG_NICHT_EINTRETEN',
            deleteText: 'BESCHREIBUNG_CLOSE_VERFUEGUNG_NICHT_EINTRETEN',
            parentController: undefined,
            elementID: undefined,
        }).then(() => {
            this.getVerfuegenToWorkWith().manuelleBemerkungen = this.bemerkungen;
            return this.gesuchModelManager.verfuegungSchliessenNichtEintreten();
        });
    }

    /**
     * Die Bemerkungen sind immer die generierten, es sei denn das Angebot ist schon verfuegt
     */
    private setBemerkungen(): void {
        const verfuegungen = this.getVerfuegenToWorkWith();
        if (verfuegungen
            && this.getBetreuung()
            && (this.getBetreuung().betreuungsstatus === TSBetreuungsstatus.VERFUEGT ||
                this.getBetreuung().betreuungsstatus === TSBetreuungsstatus.GESCHLOSSEN_OHNE_VERFUEGUNG)) {
            this.bemerkungen = verfuegungen.manuelleBemerkungen;
            return;
        }

        this.bemerkungen = '';
        if (verfuegungen && verfuegungen.generatedBemerkungen && verfuegungen.generatedBemerkungen.length > 0) {
            this.bemerkungen = verfuegungen.generatedBemerkungen + '\n';
        }
        if (this.gesuchModelManager.getGesuch() && this.gesuchModelManager.getGesuch().bemerkungen) {
            this.bemerkungen += this.gesuchModelManager.getGesuch().bemerkungen;
        }
    }

    public isBemerkungenDisabled(): boolean {
        // GS darf das Feld nicht bearbeiten
        if (this.authServiceRs.isRole(TSRole.GESUCHSTELLER)) {
            return true;
        }

        return this.gesuchModelManager.getGesuch()
            && (this.gesuchModelManager.getGesuch().status !== TSAntragStatus.VERFUEGEN
                || this.getBetreuung().betreuungsstatus === TSBetreuungsstatus.VERFUEGT
                || this.getBetreuung().betreuungsstatus === TSBetreuungsstatus.GESCHLOSSEN_OHNE_VERFUEGUNG);
    }

    public openVerfuegungPDF(): void {
        const win = this.downloadRS.prepareDownloadWindow();
        this.downloadRS.getAccessTokenVerfuegungGeneratedDokument(this.gesuchModelManager.getGesuch().id,
            this.getBetreuung().id, false, this.bemerkungen)
            .then((downloadFile: TSDownloadFile) => {
                this.$log.debug('accessToken: ' + downloadFile.accessToken);
                this.downloadRS.startDownload(downloadFile.accessToken, downloadFile.filename, false, win);
            })
            .catch(ex => EbeguUtil.handleDownloadError(win, ex));
    }

    public openExport(): void {
        const win = this.downloadRS.prepareDownloadWindow();
        this.downloadRS.getDokumentAccessTokenVerfuegungExport(this.getBetreuung().id)
            .then((downloadFile: TSDownloadFile) => {
                this.$log.debug('accessToken for export: ' + downloadFile.accessToken);
                this.downloadRS.startDownload(downloadFile.accessToken, downloadFile.filename, true, win);
            })
            .catch(ex => EbeguUtil.handleDownloadError(win, ex));
    }

    public openNichteintretenPDF(): void {
        const win = this.downloadRS.prepareDownloadWindow();
        this.downloadRS.getAccessTokenNichteintretenGeneratedDokument(this.getBetreuung().id, false)
            .then((downloadFile: TSDownloadFile) => {
                this.$log.debug('accessToken: ' + downloadFile.accessToken);
                this.downloadRS.startDownload(downloadFile.accessToken, downloadFile.filename, false, win);
            })
            .catch(ex => EbeguUtil.handleDownloadError(win, ex));
    }

    public showVerfuegungsDetails(): boolean {
        return !this.isBetreuungInStatus(TSBetreuungsstatus.NICHT_EINGETRETEN);
    }

    public showVerfuegungPdfLink(): boolean {
        return !this.isBetreuungInStatus(TSBetreuungsstatus.NICHT_EINGETRETEN);
    }

    // noinspection JSUnusedGlobalSymbols
    public showExportLink(): boolean {
        return this.isBetreuungInStatus(TSBetreuungsstatus.VERFUEGT);
    }

    public exportJsonSchema(): void {
        const win = this.$window.open('', EbeguUtil.generateRandomName(5));
        this.exportRS.getJsonSchemaString().then(result => {
            win.document.write(`<body><pre>${result}</pre></body>`);
        });
    }

    public exportXmlSchema(): void {
        // ACHTUNG popup blocker muss deaktiviert sein
        this.exportRS.getXmlSchemaString().then(result => {
            this.$window.open(`data:application/octet-streem;charset=utf-8,${result}`, '', '');
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

    public isTagesschuleVerfuegung(): boolean {
        return this.getBetreuung().isAngebotTagesschule();
    }

    public getAbholungTagesschuleValues(): Array<TSAbholungTagesschule> {
        return getTSAbholungTagesschuleValues();
    }

    public getWeekDays(): TSDayOfWeek[] {
        return getWeekdaysValues();
    }

    public getModulBezeichnungInLanguage(group: TSModulTagesschuleGroup): string {
        if (TSBrowserLanguage.FR === this.i18nServiceRS.currentLanguage()) {
            return group.bezeichnung.textFranzoesisch;
        }
        return group.bezeichnung.textDeutsch;
    }

    public getModulTimeAsString(modul: TSModulTagesschuleGroup): string {
        return TagesschuleUtil.getModulTimeAsString(modul);
    }
}
