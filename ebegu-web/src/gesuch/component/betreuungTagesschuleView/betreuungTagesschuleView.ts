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
import {IComponentOptions} from 'angular';
import * as moment from 'moment';
import {EinstellungRS} from '../../../admin/service/einstellungRS.rest';
import {CONSTANTS} from '../../../app/core/constants/CONSTANTS';
import {DvDialog} from '../../../app/core/directive/dv-dialog/dv-dialog';
import {ErrorService} from '../../../app/core/errors/service/ErrorService';
import {ApplicationPropertyRS} from '../../../app/core/rest-services/applicationPropertyRS.rest';
import {DownloadRS} from '../../../app/core/service/downloadRS.rest';
import {MitteilungRS} from '../../../app/core/service/mitteilungRS.rest';
import {I18nServiceRSRest} from '../../../app/i18n/services/i18nServiceRS.rest';
import {MandantService} from '../../../app/shared/services/mandant.service';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {
    getTSAbholungTagesschuleValues,
    TSAbholungTagesschule
} from '../../../models/enums/TSAbholungTagesschule';
import {TSAnmeldungMutationZustand} from '../../../models/enums/TSAnmeldungMutationZustand';
import {
    getTSBelegungTagesschuleModulIntervallValues,
    TSBelegungTagesschuleModulIntervall
} from '../../../models/enums/TSBelegungTagesschuleModulIntervall';
import {TSBetreuungsstatus} from '../../../models/enums/betreuung/TSBetreuungsstatus';
import {TSBrowserLanguage} from '../../../models/enums/TSBrowserLanguage';
import {
    getWeekdaysValues,
    TSDayOfWeek
} from '../../../models/enums/TSDayOfWeek';
import {TSDokumentTyp} from '../../../models/enums/TSDokumentTyp';
import {TSEinstellungKey} from '../../../models/enums/TSEinstellungKey';
import {
    getTSFleischOptionValues,
    TSFleischOption
} from '../../../models/enums/TSFleischOption';
import {TSModulTagesschuleIntervall} from '../../../models/enums/TSModulTagesschuleIntervall';
import {TSModulTagesschuleTyp} from '../../../models/enums/TSModulTagesschuleTyp';
import {TSBelegungTagesschuleModul} from '../../../models/TSBelegungTagesschuleModul';
import {TSBelegungTagesschuleModulGroup} from '../../../models/TSBelegungTagesschuleModulGroup';
import {TSBetreuung} from '../../../models/TSBetreuung';
import {TSEinstellungenTagesschule} from '../../../models/TSEinstellungenTagesschule';
import {TSMandant} from '../../../models/TSMandant';
import {TSModulTagesschuleGroup} from '../../../models/TSModulTagesschuleGroup';
import {DateUtil} from '../../../utils/DateUtil';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {TagesschuleUtil} from '../../../utils/TagesschuleUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {OkDialogLongTextController} from '../../dialog/OkDialogLongTextController';
import {RemoveDialogController} from '../../dialog/RemoveDialogController';
import {IBetreuungStateParams} from '../../gesuch.route';
import {BerechnungsManager} from '../../service/berechnungsManager';
import {GemeindeRS} from '../../service/gemeindeRS.rest';
import {GesuchModelManager} from '../../service/gesuchModelManager';
import {GlobalCacheService} from '../../service/globalCacheService';
import {WizardStepManager} from '../../service/wizardStepManager';
import {BetreuungViewController} from '../betreuungView/betreuungView';
import IFormController = angular.IFormController;
import ILogService = angular.ILogService;
import IPromise = angular.IPromise;
import IScope = angular.IScope;
import ITimeoutService = angular.ITimeoutService;
import ITranslateService = angular.translate.ITranslateService;

const removeDialogTemplate = require('../../dialog/removeDialogTemplate.html');
const okHtmlDialogTempl = require('../../../gesuch/dialog/okDialogLongTextTemplate.html');

export class BetreuungTagesschuleViewComponentConfig
    implements IComponentOptions
{
    public transclude = false;
    public bindings = {
        betreuung: '=',
        onSave: '&',
        cancel: '&',
        anmeldungSchulamtUebernehmen: '&',
        anmeldungSchulamtAblehnen: '&',
        anmeldungSchulamtFalscheInstitution: '&',
        anmeldungSchulamtFalscheAngaben: '&',
        anmeldungSchulamtStornieren: '&',
        form: '='
    };
    public template = require('./betreuungTagesschuleView.html');
    public controller = BetreuungTagesschuleViewController;
    public controllerAs = 'vm';
}

export class BetreuungTagesschuleViewController extends BetreuungViewController {
    public static $inject = [
        '$state',
        'GesuchModelManager',
        'EbeguUtil',
        '$scope',
        'BerechnungsManager',
        'ErrorService',
        'AuthServiceRS',
        'WizardStepManager',
        '$stateParams',
        'MitteilungRS',
        'DvDialog',
        '$log',
        'EinstellungRS',
        'GlobalCacheService',
        '$timeout',
        '$translate',
        'ApplicationPropertyRS',
        'DownloadRS',
        'GemeindeRS',
        'I18nServiceRSRest',
        'MandantService',
        'EbeguRestUtil'
    ];

    public gemeindeTSZusaetzlicheAngabenZurAnmeldungEnabled: boolean;
    public readonly CONSTANTS: any = CONSTANTS;
    public onSave: () => void;
    public form: IFormController;
    public betreuung: TSBetreuung;
    public showErrorMessageNoModule: boolean;
    public showNochNichtFreigegeben: boolean = false;
    public showMutiert: boolean = false;
    public aktuellGueltig: boolean = true;
    public agbTSAkzeptiert: boolean = false;
    public isAnmeldenClicked: boolean = false;
    public erlaeuterung: string = null;
    public agbVorhanden: boolean;
    private _showWarningModuleZugewiesen: boolean = false;

    public isScolaris: boolean = false;

    public modulGroups: TSBelegungTagesschuleModulGroup[] = [];
    public lastModifiedModul?: TSBelegungTagesschuleModul;

    public constructor(
        $state: StateService,
        gesuchModelManager: GesuchModelManager,
        ebeguUtil: EbeguUtil,
        $scope: IScope,
        berechnungsManager: BerechnungsManager,
        errorService: ErrorService,
        authServiceRS: AuthServiceRS,
        wizardStepManager: WizardStepManager,
        $stateParams: IBetreuungStateParams,
        mitteilungRS: MitteilungRS,
        dvDialog: DvDialog,
        $log: ILogService,
        einstellungRS: EinstellungRS,
        globalCacheService: GlobalCacheService,
        $timeout: ITimeoutService,
        $translate: ITranslateService,
        applicationPropertyRS: ApplicationPropertyRS,
        private readonly downloadRS: DownloadRS,
        private readonly gemeindeRS: GemeindeRS,
        private readonly i18nServiceRS: I18nServiceRSRest,
        mandantService: MandantService,
        ebeguRestUtil: EbeguRestUtil
    ) {
        super(
            $state,
            gesuchModelManager,
            ebeguUtil,
            $scope,
            berechnungsManager,
            errorService,
            authServiceRS,
            wizardStepManager,
            $stateParams,
            mitteilungRS,
            dvDialog,
            $log,
            einstellungRS,
            globalCacheService,
            $timeout,
            $translate,
            applicationPropertyRS,
            mandantService,
            ebeguRestUtil
        );

        this.$scope.$watch(
            () => this.getBetreuungModel().institutionStammdaten,
            (newValue, oldValue) => {
                this.existMerkblattAnmeldungTS();
                if (newValue === oldValue) {
                    return;
                }
                if (
                    this.getBetreuungModel().isBetreuungsstatus(
                        TSBetreuungsstatus.SCHULAMT_FALSCHE_INSTITUTION
                    ) &&
                    newValue.institution.id !== oldValue.institution.id
                ) {
                    this.modulGroups =
                        TagesschuleUtil.initModuleTagesschuleAfterInstitutionChange(
                            this.getBetreuungModel(),
                            oldValue,
                            this.gesuchModelManager.getGesuchsperiode(),
                            false
                        );
                    this._showWarningModuleZugewiesen = true;
                } else {
                    this.modulGroups = TagesschuleUtil.initModuleTagesschule(
                        this.getBetreuungModel(),
                        this.gesuchModelManager.getGesuchsperiode(),
                        false
                    );
                }

                if (this.betreuung.institutionStammdaten) {
                    this.loadEinstellungPropertiesForTagesschule();
                }
            }
        );
        this.$scope.$on('$mdMenuClose', () => {
            if (this.lastModifiedModul.modulTagesschule.angemeldet) {
                this.form[
                    `modul_${this.lastModifiedModul.modulTagesschule.identifier}`
                ].$setValidity(
                    'nointerval',
                    this.lastModifiedModul.intervall !== undefined
                );
            }
        });
    }

    public $onInit(): void {
        this.modulGroups = TagesschuleUtil.initModuleTagesschule(
            this.getBetreuungModel(),
            this.gesuchModelManager.getGesuchsperiode(),
            false
        );
        if (this.betreuung.institutionStammdaten) {
            this.loadEinstellungPropertiesForTagesschule();
        }
        if (this.betreuung.isEnabled()) {
            this.minEintrittsdatum = this.getMinErsterSchultag();
            this.setErsterSchultag();
        }
        this.isKesbPlatzierung = EbeguUtil.isNullOrUndefined(
            this.betreuung?.belegungTagesschule?.keineKesbPlatzierung
        )
            ? null
            : !this.betreuung?.belegungTagesschule?.keineKesbPlatzierung;
        this.initMutation();
    }

    public getTagesschuleAnmeldungNotYetReadyText(): string {
        if (
            this.gesuchModelManager.gemeindeKonfiguration.isTagesschulAnmeldungBeforePeriode()
        ) {
            const terminValue = DateUtil.momentToLocalDateFormat(
                this.gesuchModelManager.gemeindeKonfiguration
                    .konfigTagesschuleAktivierungsdatum,
                'DD.MM.YYYY'
            );
            return this.$translate.instant(
                'FREISCHALTUNG_TAGESSCHULE_AB_INFO',
                {
                    termin: terminValue
                }
            );
        }
        return this.$translate.instant('FREISCHALTUNG_TAGESSCHULE_INFO');
    }

    private initMutation(): void {
        if (!this.getBetreuungModel().anmeldungMutationZustand) {
            return;
        }
        if (
            this.getBetreuungModel().anmeldungMutationZustand ===
            TSAnmeldungMutationZustand.MUTIERT
        ) {
            this.showMutiert = true;
            this.aktuellGueltig = false;
            return;
        }

        if (
            this.getBetreuungModel().anmeldungMutationZustand ===
            TSAnmeldungMutationZustand.NOCH_NICHT_FREIGEGEBEN
        ) {
            // Die Warnung wollen wir dem GS nicht anzeigen!
            if (!this.isGesuchstellerSozialdienst()) {
                this.showNochNichtFreigegeben = true;
            }
            this.aktuellGueltig = false;
        }
    }

    private loadEinstellungPropertiesForTagesschule(): void {
        const stammdatenTagesschule =
            this.getBetreuungModel().institutionStammdaten
                .institutionStammdatenTagesschule;
        if (
            !stammdatenTagesschule ||
            EbeguUtil.isNullOrUndefined(
                this.gesuchModelManager.getGesuchsperiode()
            )
        ) {
            return;
        }
        const tsEinstellungenTagesschule =
            stammdatenTagesschule.einstellungenTagesschule
                .filter(
                    (einstellung: TSEinstellungenTagesschule) =>
                        einstellung.gesuchsperiode.id ===
                        this.gesuchModelManager.getGesuchsperiode().id
                )
                .pop();
        if (!tsEinstellungenTagesschule) {
            return;
        }
        this.erlaeuterung = tsEinstellungenTagesschule.erlaeuterung;
        this.isScolaris =
            tsEinstellungenTagesschule.modulTagesschuleTyp ===
            TSModulTagesschuleTyp.SCOLARIS;
        this.einstellungRS
            .findEinstellung(
                TSEinstellungKey.GEMEINDE_TAGESSCHULE_ZUSAETZLICHE_ANGABEN_ZUR_ANMELDUNG,
                stammdatenTagesschule.gemeinde.id,
                this.getBetreuungModel().gesuchsperiode.id
            )
            .subscribe(einstellung => {
                this.gemeindeTSZusaetzlicheAngabenZurAnmeldungEnabled =
                    einstellung.value === 'true';
            });
    }

    public getWeekDays(): TSDayOfWeek[] {
        return getWeekdaysValues();
    }

    public isTagesschuleAlreadySelected(): boolean {
        return (
            EbeguUtil.isNotNullOrUndefined(
                this.getBetreuungModel().institutionStammdaten
            ) && !this.getBetreuungModel().keineDetailinformationen
        );
    }

    public hasTagesschuleAnyModulGroupDefined(): boolean {
        return (
            EbeguUtil.isNotNullOrUndefined(this.modulGroups) &&
            this.modulGroups.length > 0
        );
    }

    public getButtonTextSpeichern(): string {
        return this.direktAnmeldenSchulamt()
            ? 'ANMELDEN_TAGESSCHULE'
            : 'SPEICHERN';
    }

    /**
     * Vor dem Speichern der Betreuung muessen die angemeldeten Module wieder auf
     * die Betreuung zurueckgeschrieben werden
     */
    private preSave(): void {
        const anmeldungen: TSBelegungTagesschuleModul[] = [];
        for (const group of this.modulGroups) {
            for (const belegungModul of group.module) {
                if (belegungModul.modulTagesschule.angemeldet) {
                    anmeldungen.push(belegungModul);
                }
            }
        }
        this.getBetreuungModel().belegungTagesschule.belegungTagesschuleModule =
            anmeldungen;
    }

    /**
     * Diese Methode wird aufgerufen wenn die Anmeldung erfasst oder gespeichert wird.
     */
    public anmelden(): IPromise<any> {
        this.isAnmeldenClicked = true;
        if (this.form.$valid) {
            // Die Anmeldungen wieder auf die Betreuung schreiben
            this.preSave();
            // Validieren, dass mindestens 1 Modul ausgewählt war --> ausser der Betreuungsstatus ist (noch)
            // SCHULAMT_FALSCHE_INSTITUTION
            if (
                !(
                    this.betreuung.isBetreuungsstatus(
                        TSBetreuungsstatus.SCHULAMT_FALSCHE_INSTITUTION
                    ) || this.betreuung.keineDetailinformationen
                ) &&
                !this.isThereAnyAnmeldung()
            ) {
                this.showErrorMessageNoModule = true;
                return undefined;
            }
            this.showErrorMessageNoModule = false;
            // Falls es "ohne Details" ist, muessen die Module entfernt werden
            if (this.betreuung.keineDetailinformationen) {
                this.getBetreuungModel().belegungTagesschule = undefined;
            }
            if (this.direktAnmeldenSchulamt()) {
                return this.dvDialog
                    .showRemoveDialog(
                        removeDialogTemplate,
                        this.form,
                        RemoveDialogController,
                        {
                            title: 'CONFIRM_SAVE_TAGESSCHULE',
                            deleteText: 'BESCHREIBUNG_SAVE_TAGESSCHULE',
                            parentController: undefined,
                            elementID: undefined
                        }
                    )
                    .then(() => {
                        this.onSave();
                    });
            }
            this.showWarningModuleZugewiesen = false;
            this.onSave();
        }
        return undefined;
    }

    public getModulBezeichnungInLanguage(
        group: TSModulTagesschuleGroup
    ): string {
        if (
            group.bezeichnung.textDeutsch &&
            group.bezeichnung.textFranzoesisch
        ) {
            if (TSBrowserLanguage.FR === this.i18nServiceRS.currentLanguage()) {
                return group.bezeichnung.textFranzoesisch;
            }
            return group.bezeichnung.textDeutsch;
        }
        return this.$translate.instant(group.modulTagesschuleName);
    }

    public getModulTimeAsString(modul: TSModulTagesschuleGroup): string {
        return TagesschuleUtil.getModulTimeAsString(modul);
    }

    public showButtonsInstitution(): boolean {
        return (
            this.getBetreuungModel().betreuungsstatus ===
                TSBetreuungsstatus.SCHULAMT_ANMELDUNG_AUSGELOEST &&
            !this.gesuchModelManager.isGesuchReadonlyForRole()
        );
    }

    /**
     * Muss ueberschrieben werden, damit die richtige betreuung zurueckgegeben wird
     */
    public getBetreuungModel(): TSBetreuung {
        return this.betreuung;
    }

    public isTageschulenAnmeldungAktiv(): boolean {
        return this.gesuchModelManager.gemeindeKonfiguration.isTageschulenAnmeldungAktiv();
    }

    public getAbholungTagesschuleValues(): Array<TSAbholungTagesschule> {
        return getTSAbholungTagesschuleValues();
    }

    public getFleischOptionValues(): Array<TSFleischOption> {
        return getTSFleischOptionValues();
    }

    public isModuleEditable(modul: TSBelegungTagesschuleModul): boolean {
        return modul.modulTagesschule.angeboten && this.isAnmeldungTSEditable();
    }

    public openMenu(
        modul: TSBelegungTagesschuleModul,
        belegungGroup: TSBelegungTagesschuleModulGroup,
        $mdMenu: any,
        ev: Event
    ): any {
        this.toggleWarnungModule();
        this.lastModifiedModul = modul;
        if (!modul.modulTagesschule.angemeldet) {
            // Das Modul wurde abgewählt. Wir entfernen auch das gewählte Intervall
            modul.intervall = undefined;
            this.form[
                `modul_${modul.modulTagesschule.identifier}`
            ].$setValidity('nointerval', true);
            return;
        }
        if (
            belegungGroup.group.intervall ===
            TSModulTagesschuleIntervall.WOECHENTLICH
        ) {
            // Es gibt keine Auswahl für dieses Modul, es ist immer Wöchentlich
            modul.intervall = TSBelegungTagesschuleModulIntervall.WOECHENTLICH;
            this.form[
                `modul_${modul.modulTagesschule.identifier}`
            ].$setValidity('nointerval', true);
            return;
        }
        $mdMenu.open(ev);
    }

    public getIntervalle(): TSBelegungTagesschuleModulIntervall[] {
        return getTSBelegungTagesschuleModulIntervallValues();
    }

    public setIntervall(
        modul: TSBelegungTagesschuleModul,
        intervall: TSBelegungTagesschuleModulIntervall,
        $mdMenu: any
    ): void {
        modul.intervall = intervall;
        $mdMenu.close();
    }

    public saveAnmeldungSchulamtUebernehmen(): void {
        this.showErrorMessageNoModule = !this.isThereAnyAnmeldung();
        if (!this.form.$valid) {
            this.form.$setDirty();
            return undefined;
        }
        this.preSave();
        this.anmeldungSchulamtUebernehmen({isScolaris: this.isScolaris});
    }

    public saveAnmeldungSchulamtAblehnen(): void {
        if (this.form.$valid) {
            this.preSave();
            this.anmeldungSchulamtAblehnen();
        }
    }

    public saveAnmeldungSchulamtStornieren(): void {
        if (!this.form.$valid) {
            return;
        }
        const deleteText = this.isScolaris
            ? 'CONFIRM_STORNIEREN_TAGESSCHULE_WARNING_SCOLARIS'
            : '';
        this.dvDialog
            .showRemoveDialog(
                removeDialogTemplate,
                this.form,
                RemoveDialogController,
                {
                    title: 'CONFIRM_STORNIEREN_TAGESSCHULE',
                    deleteText,
                    parentController: undefined,
                    elementID: undefined
                }
            )
            .then(() => {
                this.preSave();
                this.anmeldungSchulamtStornieren();
            });
    }

    public saveAnmeldungSchulamtFalscheInstitution(): void {
        if (this.form.$valid) {
            this.preSave();
            this.anmeldungSchulamtFalscheInstitution();
        }
    }

    private getMinErsterSchultag(): moment.Moment {
        if (this.getBetreuungModel()) {
            return moment.max(
                this.gesuchModelManager.gemeindeKonfiguration
                    .konfigTagesschuleErsterSchultag,
                TSMandant.earliestDateOfTSAnmeldung
            );
        }
        return undefined;
    }

    public downloadGemeindeGesuchsperiodeDokument(): void {
        const sprache =
            this.gesuchModelManager.getGesuch().gesuchsteller1.gesuchstellerJA
                .korrespondenzSprache;
        this.gemeindeRS
            .downloadGemeindeGesuchsperiodeDokument(
                this.gesuchModelManager.getGemeinde().id,
                this.gesuchModelManager.getGesuchsperiode().id,
                sprache,
                TSDokumentTyp.MERKBLATT_ANMELDUNG_TS
            )
            .then(response => {
                const file = new Blob([response], {type: 'application/pdf'});
                const filename = this.$translate.instant(
                    'MERKBLATT_ANMELDUNG_TAGESSCHULE_DATEI_NAME'
                );
                this.downloadRS.openDownload(file, filename);
            });
    }

    private existMerkblattAnmeldungTS(): void {
        const sprache =
            this.gesuchModelManager.getGesuch().gesuchsteller1.gesuchstellerJA
                .korrespondenzSprache;
        this.gemeindeRS
            .existGemeindeGesuchsperiodeDokument(
                this.gesuchModelManager.getGemeinde().id,
                this.gesuchModelManager.getGesuchsperiode().id,
                sprache,
                TSDokumentTyp.MERKBLATT_ANMELDUNG_TS
            )
            .then(result => {
                this.agbVorhanden = result;
            });
    }

    public set showWarningModuleZugewiesen(value: boolean) {
        this._showWarningModuleZugewiesen = value;
    }

    public get showWarningModuleZugewiesen(): boolean {
        return this._showWarningModuleZugewiesen;
    }

    private toggleWarnungModule(): void {
        this.showErrorMessageNoModule = !this.isThereAnyAnmeldung();
    }

    private isThereAnyAnmeldung(): boolean {
        // Das Modell, welches mit dem GUI verknuepft ist, ist nicht dasselbe, das dann (nach preSave())
        // gespeichert wird, wir muessen beide abfragen!
        return (
            this.isThereAnyAnmeldungSaveModel() ||
            this.isThereAnyAnmeldungInputModel()
        );
    }

    private isThereAnyAnmeldungInputModel(): boolean {
        // Ermittelt, ob im InputModel, also noch bevor preSave() aufgerufen wird, eine Anmeldung vorhanden ist
        for (const group of this.modulGroups) {
            for (const belegungModul of group.module) {
                if (belegungModul.modulTagesschule.angemeldet) {
                    return true;
                }
            }
        }
        return false;
    }

    private isThereAnyAnmeldungSaveModel(): boolean {
        const moduleTagessule =
            this.getBetreuungModel().belegungTagesschule
                ?.belegungTagesschuleModule;
        if (EbeguUtil.isNotNullOrUndefined(moduleTagessule)) {
            return (
                moduleTagessule.filter(
                    modul => modul.modulTagesschule.angemeldet
                ).length > 0
            );
        }
        return false;
    }

    public showFalscheAngabenTagesschule(): boolean {
        return this.isStatusOkAndGesuchBearbeitbar() && !this.isFromMutation();
    }

    public showAngabenKorrigieren(): boolean {
        return this.isStatusOkAndGesuchBearbeitbar() && this.isFromMutation();
    }

    private isStatusOkAndGesuchBearbeitbar(): boolean {
        return (
            (this.getBetreuungModel().isBetreuungsstatus(
                TSBetreuungsstatus.SCHULAMT_MODULE_AKZEPTIERT
            ) ||
                this.getBetreuungModel().isBetreuungsstatus(
                    TSBetreuungsstatus.SCHULAMT_ANMELDUNG_ABGELEHNT
                ) ||
                this.getBetreuungModel().isBetreuungsstatus(
                    TSBetreuungsstatus.SCHULAMT_ANMELDUNG_UEBERNOMMEN
                )) &&
            !this.isGesuchReadonlyForTSAnmeldungen()
        );
    }

    public tsPlatzAnfordern(): void {
        if (!this.gesuchModelManager.isNeuestesGesuch()) {
            this.dvDialog.showDialog(
                okHtmlDialogTempl,
                OkDialogLongTextController,
                {
                    title: this.$translate.instant('TS_MUTATION_EXISTIERT'),
                    body: this.$translate.instant('TS_MUTATION_EXISTIERT_BODY')
                }
            );
            return;
        }

        if (this.isStatusOkAndGesuchBearbeitbar() || this.form.$valid) {
            this.preSave();
            this.anmeldungSchulamtFalscheAngaben();
        }
    }

    /**
     * Returns true when the TS Anmeldung must be readonly
     */
    private isGesuchReadonlyForTSAnmeldungen(): boolean {
        return (
            !this.getGesuch() ||
            this.gesuchModelManager.isGesuchReadonlyForRole() ||
            this.getGesuch().gesperrtWegenBeschwerde
        );
    }

    public showStornierenForRole(): boolean {
        return this.authServiceRS.isOneOfRoles(
            this.TSRoleUtil.getSchulamtRoles()
        );
    }

    public changeKeineKesbPlatzierung(): void {
        this.getBetreuungModel().belegungTagesschule.keineKesbPlatzierung =
            !this.isKesbPlatzierung;
    }

    public enableKESBFrage(): boolean {
        if (this.isDuplicated) {
            return true;
        }
        if (!this.gesuchModelManager.getGesuch() || !this.isEditableOrNew()) {
            return false;
        }
        const gesuchsteller = this.authServiceRS.isOneOfRoles(
            TSRoleUtil.getGesuchstellerOnlyRoles()
        );
        const gemeindeUser = this.authServiceRS.isOneOfRoles(
            TSRoleUtil.getAdministratorOrAmtOrSozialdienstRolle()
        );
        return !this.isSavingData && (gesuchsteller || gemeindeUser);
    }

    private isEditableOrNew(): boolean {
        // alle Status die Bearbeitung erlauben ausser "AUSSTEHEND"
        return (
            this.isAnmeldungTSEditable() ||
            // neue Anmeldungen haben Status AUSSTEHEND
            this.getBetreuungModel().isBetreuungsstatus(
                TSBetreuungsstatus.AUSSTEHEND
            )
        );
    }

    public getMinDateInCurrentPeriode(): moment.Moment {
        return this.gesuchModelManager.getGesuchsperiode().gueltigkeit
            .gueltigAb;
    }

    public getMaxDateInCurrentPeriode(): moment.Moment {
        return this.gesuchModelManager.getGesuchsperiode().gueltigkeit
            .gueltigBis;
    }

    public getStartYearCurrentPeriode(): number {
        return this.gesuchModelManager
            .getGesuchsperiode()
            .gueltigkeit.gueltigAb.year();
    }

    public getEndYearCurrentPeriode(): number {
        return this.gesuchModelManager
            .getGesuchsperiode()
            .gueltigkeit.gueltigBis.year();
    }
}
