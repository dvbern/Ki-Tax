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
import {DvDialog} from '../../../app/core/directive/dv-dialog/dv-dialog';
import {ErrorService} from '../../../app/core/errors/service/ErrorService';
import {MitteilungRS} from '../../../app/core/service/mitteilungRS.rest';
import {I18nServiceRSRest} from '../../../app/i18n/services/i18nServiceRS.rest';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {getTSAbholungTagesschuleValues, TSAbholungTagesschule} from '../../../models/enums/TSAbholungTagesschule';
import {TSAnmeldungMutationZustand} from '../../../models/enums/TSAnmeldungMutationZustand';
import {
    getTSBelegungTagesschuleModulIntervallValues,
    TSBelegungTagesschuleModulIntervall,
} from '../../../models/enums/TSBelegungTagesschuleModulIntervall';
import {TSBetreuungsstatus} from '../../../models/enums/TSBetreuungsstatus';
import {TSBrowserLanguage} from '../../../models/enums/TSBrowserLanguage';
import {getWeekdaysValues, TSDayOfWeek} from '../../../models/enums/TSDayOfWeek';
import {TSModulTagesschuleIntervall} from '../../../models/enums/TSModulTagesschuleIntervall';
import {TSBelegungTagesschuleModul} from '../../../models/TSBelegungTagesschuleModul';
import {TSBelegungTagesschuleModulGroup} from '../../../models/TSBelegungTagesschuleModulGroup';
import {TSBetreuung} from '../../../models/TSBetreuung';
import {TSEinstellungenTagesschule} from '../../../models/TSEinstellungenTagesschule';
import {TSModulTagesschuleGroup} from '../../../models/TSModulTagesschuleGroup';
import {DateUtil} from '../../../utils/DateUtil';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {TagesschuleUtil} from '../../../utils/TagesschuleUtil';
import {RemoveDialogController} from '../../dialog/RemoveDialogController';
import {IBetreuungStateParams} from '../../gesuch.route';
import {BerechnungsManager} from '../../service/berechnungsManager';
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

const dialogTemplate = require('../../dialog/removeDialogTemplate.html');

export class BetreuungTagesschuleViewComponentConfig implements IComponentOptions {
    public transclude = false;
    public bindings = {
        betreuung: '=',
        onSave: '&',
        cancel: '&',
        anmeldungSchulamtUebernehmen: '&',
        anmeldungSchulamtAblehnen: '&',
        anmeldungSchulamtFalscheInstitution: '&',
        form: '=',
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
        'CONSTANTS',
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
        'I18nServiceRSRest',
    ];

    public onSave: () => void;
    public form: IFormController;
    public betreuung: TSBetreuung;
    public showErrorMessageNoModule: boolean;
    public datumErsterSchultag: moment.Moment;
    public showNochNichtFreigegeben: boolean = false;
    public showMutiert: boolean = false;
    public aktuellGueltig: boolean = true;
    public agbTSAkzeptiert: boolean = false;
    public isAnmeldenClicked: boolean = false;
    public erlaeuterung: string = null;

    public modulGroups: TSBelegungTagesschuleModulGroup[] = [];

    public constructor(
        $state: StateService,
        gesuchModelManager: GesuchModelManager,
        ebeguUtil: EbeguUtil,
        CONSTANTS: any,
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
        private readonly i18nServiceRS: I18nServiceRSRest,
    ) {

        super($state, gesuchModelManager, ebeguUtil, CONSTANTS, $scope, berechnungsManager, errorService, authServiceRS,
            wizardStepManager, $stateParams, mitteilungRS, dvDialog, $log, einstellungRS, globalCacheService, $timeout, $translate);

        this.$scope.$watch(() => {
            return this.getBetreuungModel().institutionStammdaten;
        }, (newValue, oldValue) => {
            if (newValue !== oldValue) {
                this.modulGroups = TagesschuleUtil.initModuleTagesschule(this.getBetreuungModel(), this.gesuchModelManager.getGesuchsperiode(), false);
                if (this.betreuung.institutionStammdaten) {
                    this.loadErlaeuterungForTagesschule();
                }
            }
        });
    }

    public $onInit(): void {
        this.modulGroups = TagesschuleUtil.initModuleTagesschule(this.getBetreuungModel(), this.gesuchModelManager.getGesuchsperiode(), false);

        if (this.betreuung.institutionStammdaten) {
            this.loadErlaeuterungForTagesschule();
        }
        if (this.betreuung.isEnabled()) {
            this.datumErsterSchultag = this.gesuchModelManager.gemeindeKonfiguration.konfigTagesschuleErsterSchultag;
            this.setErsterSchultag();
        }
        if (!this.getBetreuungModel().anmeldungMutationZustand) {
            return;
        }
        if (this.getBetreuungModel().anmeldungMutationZustand === TSAnmeldungMutationZustand.MUTIERT) {
            this.showMutiert = true;
            this.aktuellGueltig = false;
            return;
        }
        if (this.getBetreuungModel().anmeldungMutationZustand === TSAnmeldungMutationZustand.NOCH_NICHT_FREIGEGEBEN) {
            this.showNochNichtFreigegeben = true;
            this.aktuellGueltig = false;
        }
    }

    public getTagesschuleAnmeldungNotYetReadyText(): string {
        if (this.gesuchModelManager.gemeindeKonfiguration.hasTagesschulenAnmeldung()) {
            if (this.gesuchModelManager.gemeindeKonfiguration.isTagesschulenAnmeldungKonfiguriert()) {
                const terminValue = DateUtil.momentToLocalDateFormat(
                    this.gesuchModelManager.gemeindeKonfiguration.konfigTagesschuleAktivierungsdatum, 'DD.MM.YYYY');
                return this.$translate.instant('FREISCHALTUNG_TAGESSCHULE_AB_INFO', {
                    termin: terminValue,
                });
            }
            return this.$translate.instant('FREISCHALTUNG_TAGESSCHULE_INFO');
        }
        return '';
    }

    private loadErlaeuterungForTagesschule(): void {
        const tsEinstellungenTagesschule =
            this.getBetreuungModel().institutionStammdaten.institutionStammdatenTagesschule.einstellungenTagesschule
                .filter((einstellung: TSEinstellungenTagesschule) =>
                    einstellung.gesuchsperiode.id === this.gesuchModelManager.getGesuchsperiode().id)
                .pop();
        if (!tsEinstellungenTagesschule) {
            return;
        }
        this.erlaeuterung = tsEinstellungenTagesschule.erlaeuterung;
    }

    public getWeekDays(): TSDayOfWeek[] {
        return getWeekdaysValues();
    }

    public isTagesschuleAlreadySelected(): boolean {
        return EbeguUtil.isNotNullOrUndefined(this.getBetreuungModel().institutionStammdaten)
            && !this.getBetreuungModel().keineDetailinformationen;
    }

    public hasTagesschuleAnyModulGroupDefined(): boolean {
        return EbeguUtil.isNotNullOrUndefined(this.modulGroups) && this.modulGroups.length > 0;
    }

    public getButtonTextSpeichern(): string {
        return this.direktAnmeldenSchulamt() ? 'ANMELDEN_TAGESSCHULE' : 'SPEICHERN';
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
        this.getBetreuungModel().belegungTagesschule.belegungTagesschuleModule = anmeldungen;
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
            if (!(
                this.betreuung.isBetreuungsstatus(TSBetreuungsstatus.SCHULAMT_FALSCHE_INSTITUTION)
                || this.betreuung.keineDetailinformationen
            ) && !this.isThereAnyAnmeldung()) {
                this.showErrorMessageNoModule = true;
                return undefined;
            }
            // Falls es "ohne Details" ist, muessen die Module entfernt werden
            if (this.betreuung.keineDetailinformationen) {
                this.getBetreuungModel().belegungTagesschule = undefined;
            }
            if (this.direktAnmeldenSchulamt()) {
                return this.dvDialog.showRemoveDialog(dialogTemplate, this.form, RemoveDialogController, {
                    title: 'CONFIRM_SAVE_TAGESSCHULE',
                    deleteText: 'BESCHREIBUNG_SAVE_TAGESSCHULE',
                    parentController: undefined,
                    elementID: undefined,
                }).then(() => {
                    this.onSave();
                });
            }
            this.onSave();
        }
        return undefined;
    }

    private isThereAnyAnmeldung(): boolean {
        const moduleTagessule = this.getBetreuungModel().belegungTagesschule.belegungTagesschuleModule;
        if (EbeguUtil.isNotNullOrUndefined(moduleTagessule)) {
            return moduleTagessule
                .filter(modul => modul.modulTagesschule.angemeldet).length > 0;
        }
        return false;
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

    public showButtonsInstitution(): boolean {
        return this.getBetreuungModel().betreuungsstatus === TSBetreuungsstatus.SCHULAMT_ANMELDUNG_AUSGELOEST
            && !this.gesuchModelManager.isGesuchReadonlyForRole();
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

    public isAnmeldungEditable(): boolean {
        return !this.isFreigabequittungAusstehend() && !this.getBetreuungModel().isSchulamtangebotAusgeloest();
    }

    public isModuleEditable(modul: TSBelegungTagesschuleModul): boolean {
        return modul.modulTagesschule.angeboten && this.isAnmeldungEditable();
    }

    public openMenu(modul: TSBelegungTagesschuleModul, belegungGroup: TSBelegungTagesschuleModulGroup, $mdMenu: any,
                    ev: Event): any {
        if (!modul.modulTagesschule.angemeldet) {
            // Das Modul wurde abgewählt. Wir entfernen auch das gewählte Intervall
            modul.intervall = undefined;
            return;
        }
        if (belegungGroup.group.intervall === TSModulTagesschuleIntervall.WOECHENTLICH) {
            // Es gibt keine Auswahl für dieses Modul, es ist immer Wöchentlich
            modul.intervall = TSBelegungTagesschuleModulIntervall.WOECHENTLICH;
            return;
        }
        $mdMenu.open(ev);
    }

    public getIntervalle(): TSBelegungTagesschuleModulIntervall[] {
        return getTSBelegungTagesschuleModulIntervallValues();
    }

    public setIntervall(modul: TSBelegungTagesschuleModul, intervall: TSBelegungTagesschuleModulIntervall): void {
        modul.intervall = intervall;
    }
}
