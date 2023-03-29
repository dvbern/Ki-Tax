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

/* eslint-disable */
import {StateService, TransitionPromise} from '@uirouter/core';
import {IComponentOptions, ILogService, IPromise, IQService, IScope, IWindowService} from 'angular';
import {map} from 'rxjs/operators';
import {EinstellungRS} from '../../../admin/service/einstellungRS.rest';
import {MANDANTS} from '../../../app/core/constants/MANDANTS';
import {DvDialog} from '../../../app/core/directive/dv-dialog/dv-dialog';
import {TSDemoFeature} from '../../../app/core/directive/dv-hide-feature/TSDemoFeature';
import {LogFactory} from '../../../app/core/logging/LogFactory';
import {ApplicationPropertyRS} from '../../../app/core/rest-services/applicationPropertyRS.rest';
import {DemoFeatureRS} from '../../../app/core/service/demoFeatureRS.rest';
import {DownloadRS} from '../../../app/core/service/downloadRS.rest';
import {I18nServiceRSRest} from '../../../app/i18n/services/i18nServiceRS.rest';
import {MandantService} from '../../../app/shared/services/mandant.service';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {getTSAbholungTagesschuleValues, TSAbholungTagesschule} from '../../../models/enums/TSAbholungTagesschule';
import {TSAntragStatus} from '../../../models/enums/TSAntragStatus';
import {TSBetreuungsstatus} from '../../../models/enums/TSBetreuungsstatus';
import {TSBrowserLanguage} from '../../../models/enums/TSBrowserLanguage';
import {getWeekdaysValues, TSDayOfWeek} from '../../../models/enums/TSDayOfWeek';
import {TSEinstellungKey} from '../../../models/enums/TSEinstellungKey';
import {TSPensumAnzeigeTyp} from '../../../models/enums/TSPensumAnzeigeTyp';
import {TSRole} from '../../../models/enums/TSRole';
import {TSWizardStepName} from '../../../models/enums/TSWizardStepName';
import {TSZahlungslaufTyp} from '../../../models/enums/TSZahlungslaufTyp';
import {TSBelegungTagesschuleModulGroup} from '../../../models/TSBelegungTagesschuleModulGroup';
import {TSBetreuung} from '../../../models/TSBetreuung';
import {TSDownloadFile} from '../../../models/TSDownloadFile';
import {TSEinstellung} from '../../../models/TSEinstellung';
import {TSEinstellungenTagesschule} from '../../../models/TSEinstellungenTagesschule';
import {TSGesuch} from '../../../models/TSGesuch';
import {TSModulTagesschuleGroup} from '../../../models/TSModulTagesschuleGroup';
import {TSPublicAppConfig} from '../../../models/TSPublicAppConfig';
import {TSVerfuegung} from '../../../models/TSVerfuegung';
import {TSVerfuegungZeitabschnitt} from '../../../models/TSVerfuegungZeitabschnitt';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {TagesschuleUtil} from '../../../utils/TagesschuleUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {RemoveDialogController} from '../../dialog/RemoveDialogController';
import {StepDialogController} from '../../dialog/StepDialogController';
import {IBetreuungStateParams} from '../../gesuch.route';
import {BerechnungsManager} from '../../service/berechnungsManager';
import {ExportRS} from '../../service/exportRS.rest';
import {GesuchModelManager} from '../../service/gesuchModelManager';
import {GesuchRS} from '../../service/gesuchRS.rest';
import {WizardStepManager} from '../../service/wizardStepManager';
import {AbstractGesuchViewController} from '../abstractGesuchView';
import ITimeoutService = angular.ITimeoutService;
import ITranslateService = angular.translate.ITranslateService;

const removeDialogTempl = require('../../dialog/removeDialogTemplate.html');
const stepDialogTempl = require('../../dialog/stepDialog.html');

const LOG = LogFactory.createLog('VerfuegenViewController');

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
        '$q',
        '$translate',
        'MandantService',
        'EinstellungRS',
        'EbeguRestUtil',
        'DemoFeatureRS',
        'GesuchRS'
    ];

    // this is the model...
    public bemerkungen: string;

    public showSchemas: boolean;
    public sameVerfuegteVerfuegungsrelevanteDaten: boolean;
    public fragenObIgnorieren: boolean;
    public fragenObIgnorierenMahlzeiten: boolean;
    public mahlzeitenChanged: boolean;
    public verfuegungsBemerkungenKontrolliert: boolean = false;
    public isVerfuegenClicked: boolean = false;
    public showPercent: boolean;
    public showHours: boolean;
    public showDays: boolean;
    private isVerfuegungExportEnabled: boolean;

    public showVerfuegung: boolean;
    public modulGroups: TSBelegungTagesschuleModulGroup[] = [];
    public tagesschuleZeitabschnitteMitBetreuung: Array<TSVerfuegungZeitabschnitt>;
    public tagesschuleZeitabschnitteOhneBetreuung: Array<TSVerfuegungZeitabschnitt>;

    public isLuzern: boolean;
    public isAppenzell: boolean;
    private isAuszahlungAnAntragstellerEnabled: boolean = false;

    private showAuszahlungAnInstitutionen: boolean;
    private showAuszahlungAnEltern: boolean;

    public readonly demoFeature = TSDemoFeature.VERAENDERUNG_BEI_MUTATION;
    private demoFeatureZahlungsstatusAllowed: boolean = false;
    public vorgaengerZeitabschnitte: TSVerfuegungZeitabschnitt[];

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
        private readonly $q: IQService,
        private readonly $translate: ITranslateService,
        private readonly mandantService: MandantService,
        private readonly einstellungRS: EinstellungRS,
        private readonly ebeguRestUtil: EbeguRestUtil,
        private readonly demoFeatureRS: DemoFeatureRS,
        private readonly gesuchRS: GesuchRS
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

        this.mandantService.mandant$.pipe(map(mandant => mandant === MANDANTS.LUZERN)).subscribe(isLuzern => {
            this.isLuzern = isLuzern;
        }, error => this.$log.error(error));

        this.mandantService.mandant$.pipe(map(mandant => mandant === MANDANTS.APPENZELL_AUSSERRHODEN)).subscribe(isAppenzell => {
            this.isAppenzell = isAppenzell;
        }, error => this.$log.error(error));

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

        this.demoFeatureRS.isDemoFeatureAllowed(TSDemoFeature.ZAHLUNGSSTATUS).then(res => {
            this.demoFeatureZahlungsstatusAllowed = res;
        });

        this.initVorgaengerGebuehren();
    }

    private initView(): void {
        const gesuch = this.gesuchModelManager.getGesuch();
        if (!gesuch) {
            return;
        }
        if (this.isTagesschuleVerfuegung()) {
            this.modulGroups = TagesschuleUtil.initModuleTagesschule(this.getBetreuung(),
                this.gesuchModelManager.getGesuchsperiode(),
                true);
            this.tagesschuleZeitabschnitteMitBetreuung =
                this.onlyZeitabschnitteSinceEntryTagesschule(this.getTagesschuleZeitabschnitteMitBetreuung());
            this.tagesschuleZeitabschnitteOhneBetreuung =
                this.onlyZeitabschnitteSinceEntryTagesschule(this.getTagesschuleZeitabschnitteOhneBetreuung());
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
        this.einstellungRS.getAllEinstellungenBySystemCached(
            this.gesuchModelManager.getGesuchsperiode().id,
        ).subscribe((response: TSEinstellung[]) => {
            response.filter(r => r.key === TSEinstellungKey.PENSUM_ANZEIGE_TYP)
                .forEach(einstellung => {
                    this.loadPensumAnzeigeTyp(einstellung);
                });
        }, error => LOG.error(error));
        this.showVerfuegung = this.showVerfuegen();
    }

    private setParamsDependingOnCurrentVerfuegung(): void {
        this.setSameVerfuegteVerfuegungsrelevanteDaten();
        this.setMahlzeitenChanges();
        this.initProperties();
    }

    private initProperties(): void {
        this.applicationPropertyRS.getPublicPropertiesCached().then((response: TSPublicAppConfig) => {
            // Schemas are only visible in devmode
            this.showSchemas = response.devmode;
            this.isAuszahlungAnAntragstellerEnabled = response.infomaZahlungen;

            this.setFragenObIgnorieren();
        });

        this.einstellungRS.findEinstellung(
            TSEinstellungKey.VERFUEGUNG_EXPORT_ENABLED,
            this.gesuchModelManager.getDossier().gemeinde.id,
            this.gesuchModelManager.getGesuchsperiode().id,
        ).subscribe(response => {
            this.isVerfuegungExportEnabled = JSON.parse(response.value);
        });
    }

    public cancel(): void {
        this.form.$setPristine();
    }

    private setSameVerfuegteVerfuegungsrelevanteDaten(): void {
        this.sameVerfuegteVerfuegungsrelevanteDaten = false; // by default
        if (this.getVerfuegenToWorkWith()) {
            this.sameVerfuegteVerfuegungsrelevanteDaten =
                this.getVerfuegenToWorkWith().areSameVerfuegteVerfuegungsrelevanteDaten();
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
        this.fragenObIgnorierenMahlzeiten = false; // by default
        if (this.getVerfuegenToWorkWith()) {
            this.fragenObIgnorieren =
                this.getVerfuegenToWorkWith().fragenObIgnorieren(!this.isAuszahlungAnAntragstellerEnabled);
            this.fragenObIgnorierenMahlzeiten = this.getVerfuegenToWorkWith().fragenObIgnorierenMahlzeiten();
        }
    }

    private setMahlzeitenChanges(): void {
        this.mahlzeitenChanged = false; // by default

        if (this.getVerfuegenToWorkWith()) {
            this.mahlzeitenChanged = this.getVerfuegenToWorkWith().mahlzeitenChangedSincePreviousVerfuegung();
        }
    }

    private isAlreadyIgnored(): boolean {
        if (this.getVerfuegenToWorkWith()) {
            return this.getVerfuegenToWorkWith().isAlreadyIgnored();
        }
        return false; // by default
    }

    private isAlreadyIgnoredMahlzeiten(): boolean {
        if (this.getVerfuegenToWorkWith()) {
            return this.getVerfuegenToWorkWith().isAlreadyIgnoredMahlzeiten();
        }
        return false; // by default
    }

    public save(): void {
        this.isVerfuegenClicked = true;
        if (!this.isGesuchValid() || !this.isVerfuegenValid()) {
            return;
        }

        // Wir muessen die Frage nach dem Verfuegen fuer die Verguenstigung und die Mahlzeiten separat stellen!
        const direktVerfuegenVerguenstigung = !this.fragenObIgnorieren || !this.isMutation() || this.isAlreadyIgnored();
        const direktVerfuegenMahlzeiten = !this.fragenObIgnorierenMahlzeiten || !this.isMutation() || this.isAlreadyIgnoredMahlzeiten();

        // Zuerst zeigen wir aber eine Warnung an, falls schon ignoriert war (wiederum separat fuer Verguenstigung
        // und Mahlzeiten)
        // Normal
        this.warnIfAlreadyIgnored(
            this.isAlreadyIgnored(),
            'CONFIRM_ALREADY_IGNORED',
            'BESCHREIBUNG_CONFIRM_ALREADY_IGNORED')
            .then(() => {
                // Mahlzeiten
                this.warnIfAlreadyIgnored(
                    this.isAlreadyIgnoredMahlzeiten(),
                    'CONFIRM_ALREADY_IGNORED_MAHLZEITEN',
                    'BESCHREIBUNG_CONFIRM_ALREADY_IGNORED_MAHLZEITEN')
                    .then(() => {
                        // Jetzt wenn notwendig nach ingorieren fragen und dann verfuegen
                        this.askForIgnoringIfNecessaryAndSaveVerfuegung(direktVerfuegenVerguenstigung,
                            direktVerfuegenMahlzeiten
                        ).then(() => {
                            this.gesuchModelManager.reloadGesuch();
                            this.showVerfuegung = this.showVerfuegen();
                        });
                    });
            });
    }

    private warnIfAlreadyIgnored(alreadyIgnored: boolean, warningTitle: string, warningText: string): IPromise<void> {
        // Falls es bereits ignoriert war, soll eine Warung angezeigt werden
        if (alreadyIgnored) {
            return this.dvDialog.showRemoveDialog(removeDialogTempl, this.form, RemoveDialogController, {
                title: warningTitle,
                deleteText: warningText,
                parentController: undefined,
                elementID: undefined,
            }).then(() => {
                return this.createDeferPromise<void>();
            });
        }
        return this.createDeferPromise<void>();
    }

    private createDeferPromise<T>(): IPromise<T> {
        const defer = this.$q.defer<T>();
        defer.resolve();
        return defer.promise;
    }

    private askForIgnoringIfNecessaryAndSaveVerfuegung(
        direktVerfuegen: boolean,
        direktVerfuegenMahlzeiten: boolean,
    ): IPromise<TSVerfuegung> {
        // Falls sowohl die Verfuegung wie die Mahlzeiten "direkt" verfuegt werden duerfen, kann direkt weitergefahren
        // werden
        if (direktVerfuegen && direktVerfuegenMahlzeiten) {
            return this.saveVerfuegung();
        }
        return this.askForIgnoringIfNecessary(TSZahlungslaufTyp.GEMEINDE_INSTITUTION, direktVerfuegen)
            .then(ignoreVerguenstigung => {
                return this.askForIgnoringIfNecessary(TSZahlungslaufTyp.GEMEINDE_ANTRAGSTELLER,
                    direktVerfuegenMahlzeiten)
                    .then(ignoreMahlzeiten => {
                        return this.saveMutierteVerfuegung(ignoreVerguenstigung, ignoreMahlzeiten);
                    });
            });
    }

    private askForIgnoringIfNecessary(
        zahlungslaufTyp: TSZahlungslaufTyp, isDirektVerfuegen: boolean,
    ): IPromise<boolean> {
        if (isDirektVerfuegen) {
            return this.createDeferPromise<boolean>();
        }

        return this.askIfIgnorieren(zahlungslaufTyp)
            .then(ignoreVerguenstigung => {
                return ignoreVerguenstigung;
            });
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
            this.isVerfuegenClicked = false;
            // Auch wenn wir nicht nach dem Ignorieren gefragt haben, muessen wir u.U. ignorieren:
            // Dann naemlich, wenn fuer diese Verfuegung bereits frueher ignoriert wurde!
            return this.gesuchModelManager.saveVerfuegung(
                this.isAlreadyIgnored(),
                this.isAlreadyIgnoredMahlzeiten(),
                this.bemerkungen);
        });
    }

    public saveMutierteVerfuegung(ignoreVerguenstigung: boolean, ignoreMahlzeiten: boolean): IPromise<TSVerfuegung> {
        return this.gesuchModelManager.saveVerfuegung(ignoreVerguenstigung, ignoreMahlzeiten, this.bemerkungen);
    }

    private askIfIgnorieren(myZahlungslaufTyp: TSZahlungslaufTyp): IPromise<boolean> {
        const zahlungDirektIgnorieren = this.isFKJV() && this.getBetreuung().finSitRueckwirkendKorrigiertInThisMutation;

        return this.dvDialog.showDialog(stepDialogTempl, StepDialogController, {
            institutionName: this.getInstitutionName(),
            institutionPhone: this.getInstitutionPhone(),
            zahlungslaufTyp: myZahlungslaufTyp,
            zahlungDirektIgnorieren: zahlungDirektIgnorieren,
        }).then(response => {
            this.isVerfuegenClicked = false;
            return response === 2;
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
                this.$log.debug('accessToken for verfuegung: ' + downloadFile.accessToken);
                this.downloadRS.startDownload(downloadFile.accessToken, downloadFile.filename, false, win);
            })
            .catch(ex => EbeguUtil.handleDownloadError(win, ex));
    }

    public async openExport(): Promise<void> {
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
                this.$log.debug('accessToken for nichteintreten: ' + downloadFile.accessToken);
                this.downloadRS.startDownload(downloadFile.accessToken, downloadFile.filename, false, win);
            })
            .catch(ex => EbeguUtil.handleDownloadError(win, ex));
    }

    public showVerfuegungsDetails(): boolean {
        return !this.isBetreuungInStatus(TSBetreuungsstatus.NICHT_EINGETRETEN);
    }

    public hasBetreuungOnlyAuszahlungenAnElternAndInstitutionRole(): boolean {
        if (!this.authServiceRs.isOneOfRoles(TSRoleUtil.getTraegerschaftInstitutionOnlyRoles())) {
            return false;
        }
        return this.gesuchModelManager.getBetreuungToWorkWith().auszahlungAnEltern
            && EbeguUtil.isEmptyArrayNullOrUndefined(this.getVerfuegungZeitabschnitte());
    }

    public showVerfuegungPdfLink(): boolean {
        if (this.isLuzern && this.authServiceRs.isOneOfRoles(TSRoleUtil.getTraegerschaftInstitutionOnlyRoles())) {
            return false;
        }
        return !this.isBetreuungInStatus(TSBetreuungsstatus.NICHT_EINGETRETEN);
    }

    // noinspection JSUnusedGlobalSymbols
    public showExportLink(): boolean {
        return this.isBetreuungInStatus(TSBetreuungsstatus.VERFUEGT) && this.isVerfuegungExportEnabled;
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
        return this.getBetreuung() ? this.getBetreuung().isAngebotTagesschule() : false;
    }

    public isTagesfamilienVerfuegung(): boolean {
        return this.getBetreuung() ? this.getBetreuung().isAngebotTagesfamilien() : false;
    }

    public isKITAVerfuegung(): boolean {
        return this.getBetreuung() ? this.getBetreuung().isAngebotKITA() : false;
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

    public isSuperuser(): boolean {
        return this.authServiceRs.isRole(TSRole.SUPER_ADMIN);
    }

    public showPensumInHours(): boolean {
        return this.isTagesfamilienVerfuegung();
    }

    public showPensumInPercent(): boolean {
        return !this.isTagesfamilienVerfuegung() || this.isSuperuser();
    }

    public showPensumInDays(): boolean {
        return this.isKITAVerfuegung() && this.isSuperuser();
    }

    private getTagesschuleZeitabschnitteMitBetreuung(): Array<TSVerfuegungZeitabschnitt> {
        if (this.getBetreuung().verfuegung && this.getBetreuung().verfuegung.zeitabschnitte) {
            return this.getBetreuung().verfuegung.zeitabschnitte.filter(anmeldungTagesschuleZeitabschnitt =>
                EbeguUtil.isNotNullOrUndefined(anmeldungTagesschuleZeitabschnitt.tsCalculationResultMitPaedagogischerBetreuung));
        }
        return undefined;
    }

    private getTagesschuleZeitabschnitteOhneBetreuung(): Array<TSVerfuegungZeitabschnitt> {
        if (this.getBetreuung().verfuegung && this.getBetreuung().verfuegung.zeitabschnitte) {
            return this.getBetreuung().verfuegung.zeitabschnitte.filter(anmeldungTagesschuleZeitabschnitt =>
                EbeguUtil.isNotNullOrUndefined(anmeldungTagesschuleZeitabschnitt.tsCalculationResultOhnePaedagogischerBetreuung));
        }
        return undefined;
    }

    public showAnmeldebestaetigungOhneTarifPdfLink(): boolean {
        return this.isBetreuungInStatus(TSBetreuungsstatus.SCHULAMT_MODULE_AKZEPTIERT) ||
            (this.isBetreuungInStatus(TSBetreuungsstatus.SCHULAMT_ANMELDUNG_UEBERNOMMEN) &&
                this.authServiceRs.isOneOfRoles(this.TSRoleUtil.getTraegerschaftInstitutionSteueramtOnlyRoles()));
    }

    public showAnmeldebestaetigungMitTarifPdfLink(): boolean {
        return this.isBetreuungInStatus(TSBetreuungsstatus.SCHULAMT_ANMELDUNG_UEBERNOMMEN) &&
            !this.authServiceRs.isOneOfRoles(this.TSRoleUtil.getTraegerschaftInstitutionSteueramtOnlyRoles());
    }

    public openAnmeldebestaetigungOhneTarifPDF(): void {
        this.openAnmeldebestaetigungPDF(false);
    }

    public openAnmeldebestaetigungMitTarifPDF(): void {
        this.openAnmeldebestaetigungPDF(true);
    }

    private openAnmeldebestaetigungPDF(mitTarif: boolean): void {
        const win = this.downloadRS.prepareDownloadWindow();
        this.downloadRS.getAccessTokenAnmeldebestaetigungGeneratedDokument(this.gesuchModelManager.getGesuch().id,
            this.getBetreuung().id, false, mitTarif)
            .then((downloadFile: TSDownloadFile) => {
                this.$log.debug('accessToken for Anmeldebestaetigung: ' + downloadFile.accessToken);
                this.downloadRS.startDownload(downloadFile.accessToken, downloadFile.filename, false, win);
            })
            .catch(ex => EbeguUtil.handleDownloadError(win, ex));
    }

    public isTagesschuleTagi(): boolean {
        const gesuchsPeriode = this.getGesuchsperiode();
        const stammdatenTagesschule = this.getBetreuung().institutionStammdaten.institutionStammdatenTagesschule;
        if (stammdatenTagesschule) {
            const tsEinstellungenTagesschule =
                stammdatenTagesschule.einstellungenTagesschule
                    .filter((einstellung: TSEinstellungenTagesschule) =>
                        einstellung.gesuchsperiode.id === gesuchsPeriode.id)
                    .pop();
            if (!tsEinstellungenTagesschule) {
                return false;
            }
            return tsEinstellungenTagesschule.tagi;
        }
        return false;
    }

    public showTarifeTable(): boolean {
        return !this.isInstitutionenTraegerschaftRoleAndTSModuleAkzeptiert();
    }

    private isInstitutionenTraegerschaftRoleAndTSModuleAkzeptiert(): boolean {
        return this.authServiceRs.isOneOfRoles(TSRoleUtil.getTraegerschaftInstitutionOnlyRoles())
            && this.isBetreuungInStatus(TSBetreuungsstatus.SCHULAMT_MODULE_AKZEPTIERT);
    }

    public isMahlzeitenverguenstigungEnabled(): boolean {
        return this.gesuchModelManager.isMahlzeitenverguenstigungEnabled();
    }

    public auszahlungAnEltern(): boolean {
        return this.getBetreuung().auszahlungAnEltern;
    }

    public showGutscheinProStunde(): boolean {
        return this.isLuzern && this.getBetreuung().isAngebotTagesfamilien();
    }

    public showMahlzeitenverguenstigung(): boolean {
        return this.isMahlzeitenverguenstigungEnabled()
            && this.authServiceRs.isOneOfRoles(this.TSRoleUtil.getAdministratorOrAmtRole());
    }

    public showAuszahlungAnInstitutionenCol(): boolean {
        if (EbeguUtil.isNullOrUndefined(this.getVerfuegungZeitabschnitte())) {
            return false;
        }

        // Wenn Vergünstigung in mindestens einem Zeitabschnitt nicht an die Eltern ausbezahlt wird soll die
        // Auszahlung an Insitutionen Row angezeigt werden
        if (EbeguUtil.isNullOrUndefined(this.showAuszahlungAnInstitutionen)) {
            this.showAuszahlungAnInstitutionen =
                this.getVerfuegungZeitabschnitte()
                    .some(zeitabschnitt => !zeitabschnitt.auszahlungAnEltern && this.hasBetreuungInZeitabschnitt(
                        zeitabschnitt));
        }

        return this.showAuszahlungAnInstitutionen;
    }

    private showZahlungsstatusCol(): boolean {
        if (EbeguUtil.isNullOrUndefined(this.getBetreuung())) {
            return false;
        }
        if (!this.demoFeatureZahlungsstatusAllowed) {
            return false;
        }
        if (this.authServiceRs.isRole(TSRole.GESUCHSTELLER)) {
            return false;
        }
        if (this.getBetreuung().betreuungsstatus !== TSBetreuungsstatus.VERFUEGT) {
            return false;
        }
        return true;
    }

    public showZahlungsstatusInstitutionenCol(): boolean {
        if (!this.showZahlungsstatusCol()) {
            return false;
        }
        return this.showAuszahlungAnInstitutionenCol();
    }
    public showZahlungsstatusAntragstellerCol(): boolean {
        if (!this.showZahlungsstatusCol()) {
            return false;
        }
        return this.showAuszahlungAnElternCol() || this.showMahlzeitenverguenstigung()
    }

    private hasBetreuungInZeitabschnitt(zeitabschnitt: TSVerfuegungZeitabschnitt): boolean {
        return zeitabschnitt.betreuungspensumProzent !== 0;
    }

    public showAuszahlungAnElternCol(): boolean {
        if (EbeguUtil.isNullOrUndefined(this.getVerfuegungZeitabschnitte())) {
            return false;
        }

        // Wenn Vergünstigung in mindestens einem Zeitabschnitt an die Eltern ausbezahlt wird soll die Auszahlung
        // an Insitutionen Row angezeigt werden
        if (EbeguUtil.isNullOrUndefined(this.showAuszahlungAnEltern)) {
            this.showAuszahlungAnEltern =
                this.getVerfuegungZeitabschnitte().some(zeitabschnitt => zeitabschnitt.auszahlungAnEltern);
        }

        return this.showAuszahlungAnEltern;
    }

    public getVerguenstigungAnInstitution(zeiabschnitt: TSVerfuegungZeitabschnitt): number {
        if (!zeiabschnitt.auszahlungAnEltern) {
            return zeiabschnitt.verguenstigung;
        }

        return 0;
    }

    public getVerguenstigungAnEltern(zeiabschnitt: TSVerfuegungZeitabschnitt): number {
        if (zeiabschnitt.auszahlungAnEltern) {
            return zeiabschnitt.verguenstigung;
        }

        return 0;
    }

    private onlyZeitabschnitteSinceEntryTagesschule(tagesschuleZeitabschnitte: Array<TSVerfuegungZeitabschnitt>): Array<TSVerfuegungZeitabschnitt> {
        if (!tagesschuleZeitabschnitte) {
            return undefined;
        }
        return tagesschuleZeitabschnitte.filter(this.fullZeitAbschnittBeforeEntryTagesschule.bind(this))
            .map(this.mapPartialZeitabschnitteSinceEntryTagesschule.bind(this));
    }

    private fullZeitAbschnittBeforeEntryTagesschule(tagesschuleZeitabschnitt: TSVerfuegungZeitabschnitt): boolean {
        return tagesschuleZeitabschnitt.gueltigkeit.gueltigBis.isSameOrAfter(this.getBetreuung().belegungTagesschule.eintrittsdatum);
    }

    private mapPartialZeitabschnitteSinceEntryTagesschule(tagesschuleZeitabschnitt: TSVerfuegungZeitabschnitt): TSVerfuegungZeitabschnitt {
        if (tagesschuleZeitabschnitt.gueltigkeit.gueltigAb.isBefore(this.getBetreuung().belegungTagesschule.eintrittsdatum)) {
            tagesschuleZeitabschnitt.gueltigkeit.gueltigAb = this.getBetreuung().belegungTagesschule.eintrittsdatum;
        }
        return tagesschuleZeitabschnitt;
    }

    private loadPensumAnzeigeTyp(einstellung: TSEinstellung) {
        const einstellungPensumAnzeigeTyp = this.ebeguRestUtil
            .parsePensumAnzeigeTyp(einstellung);
        if (einstellungPensumAnzeigeTyp === TSPensumAnzeigeTyp.ZEITEINHEIT_UND_PROZENT) {
            this.showPercent = this.showPensumInPercent();
            this.showHours = this.showPensumInHours();
            this.showDays = this.showPensumInDays();
        }
        if (einstellungPensumAnzeigeTyp === TSPensumAnzeigeTyp.NUR_PROZENT) {
            this.showPercent = true;
            this.showHours = false;
            this.showDays = false;
        }
        if (einstellungPensumAnzeigeTyp === TSPensumAnzeigeTyp.NUR_STUNDEN) {
            this.showPercent = false;
            this.showHours = true;
            this.showDays = false;
        }
    }

    public showInfoUeberKorrekutren(): boolean {
        if (!this.isMutation()) {
            return false;
        }

        if (this.getBetreuung().isAngebotSchulamt()) {
            return false;
        }

        return this.hasKorrekturAuszahlungInstitution() || this.hasKorrekturAuszahlungEltern();
    }


    private hasKorrekturAuszahlungInstitution(): boolean {
        return EbeguUtil.isNotNullOrUndefined(this.getVerfuegenToWorkWith()?.korrekturAusbezahltInstitution) &&
            this.getVerfuegenToWorkWith().korrekturAusbezahltInstitution !== 0;
    }

    private hasKorrekturAuszahlungEltern() {
        return EbeguUtil.isNotNullOrUndefined(this.getVerfuegenToWorkWith()?.korrekturAusbezahltEltern) &&
            this.getVerfuegenToWorkWith().korrekturAusbezahltEltern !== 0;
    }

    public getKorrekturenString(): string {
        let text = '';

        if (this.hasKorrekturAuszahlungInstitution()) {
            const betrag = this.gesuchModelManager.getVerfuegenToWorkWith().korrekturAusbezahltInstitution;
            if (betrag < 0) {
                text += this.$translate.instant('MUTATION_KORREKTUR_AUSBEZAHLT_INSTITUTION_RUECKZAHLUNG',
                    {betrag: Math.abs(betrag).toFixed(2)});
            } else {
                text += this.$translate.instant('MUTATION_KORREKTUR_AUSBEZAHLT_INSTITUTION_RUECKFORDERUNG',
                    {betrag: betrag.toFixed(2)});
            }
            text += this.getTextKorrekturAusbezahlt(this.getVerfuegenToWorkWith().isAlreadyIgnorierend());
            text += '\n';
        }

        if (this.hasKorrekturAuszahlungEltern()) {
            const betrag = this.gesuchModelManager.getVerfuegenToWorkWith().korrekturAusbezahltEltern;
            if (betrag < 0) {
                text += this.$translate.instant('MUTATION_KORREKTUR_AUSBEZAHLT_ELTERN_RUECKZAHLUNG',
                    {betrag: Math.abs(betrag).toFixed(2)});
            } else {
                text += this.$translate.instant('MUTATION_KORREKTUR_AUSBEZAHLT_ELTERN_RUECKFORDERUNG',
                    {betrag: betrag.toFixed(2)});
            }
            text += this.getTextKorrekturAusbezahlt(this.getVerfuegenToWorkWith().isAlreadyIgnorierendMahlzeiten());
        }

        return text.trim();
    }

    private getTextKorrekturAusbezahlt(isZahlungIgnored: boolean) : string {
        if (this.getBetreuungsstatus() !== TSBetreuungsstatus.VERFUEGT) {
            return '';
        }

        if (isZahlungIgnored) {
            return ' ' + this.$translate.instant('MUTATION_KORREKTUR_AUSBEZAHLT_AUSSERHALB_KIBON');
        }

        return ' ' + this.$translate.instant('MUTATION_KORREKTUR_AUSBEZAHLT_INNERHLAB_KIBON');
    }

    private initVorgaengerGebuehren(): void {
        // beim Erstgesuch macht dies keinen Sinn
        if (EbeguUtil.isNullOrUndefined(this.getGesuch().vorgaengerId)) {
            return;
        }
        this.getBetreuung().kindId
        this.gesuchRS
            .findVorgaengerGesuchNotIgnoriert(this.getGesuch().vorgaengerId)
            .then(gesuch => {
                this.vorgaengerZeitabschnitte = this.extractVoraengerZeitabschnitteFromVorgaengerGesuch(gesuch)
            });

    }

    private extractVoraengerZeitabschnitteFromVorgaengerGesuch(gesuch: TSGesuch): TSVerfuegungZeitabschnitt[] {
        const vorgaengerKind = gesuch.kindContainers
            .find(kc => kc.kindNummer = this.getBetreuung().kindNummer);

        if (!vorgaengerKind) {
            return [];
        }
        const vorgaengerBetreuung = vorgaengerKind.betreuungen
            .find(b => b.betreuungNummer === this.getBetreuung().betreuungNummer)

        if (!vorgaengerBetreuung || !vorgaengerBetreuung.anmeldungTagesschuleZeitabschnitts) {
            return [];
        }
        return vorgaengerBetreuung.verfuegung.zeitabschnitte;
    }


}
