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
import ErrorService from '../../../app/core/errors/service/ErrorService';
import MitteilungRS from '../../../app/core/service/mitteilungRS.rest';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import {TSAnmeldungMutationZustand} from '../../../models/enums/TSAnmeldungMutationZustand';
import {TSBetreuungsstatus} from '../../../models/enums/TSBetreuungsstatus';
import {getWeekdaysValues, TSDayOfWeek} from '../../../models/enums/TSDayOfWeek';
import TSBetreuung from '../../../models/TSBetreuung';
import TSModulTagesschule from '../../../models/TSModulTagesschule';
import TSModulTagesschuleGroup from '../../../models/TSModulTagesschuleGroup';
import DateUtil from '../../../utils/DateUtil';
import EbeguUtil from '../../../utils/EbeguUtil';
import {RemoveDialogController} from '../../dialog/RemoveDialogController';
import {IBetreuungStateParams} from '../../gesuch.route';
import BerechnungsManager from '../../service/berechnungsManager';
import GesuchModelManager from '../../service/gesuchModelManager';
import GlobalCacheService from '../../service/globalCacheService';
import WizardStepManager from '../../service/wizardStepManager';
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
    ];

    public onSave: () => void;
    public form: IFormController;
    public betreuung: TSBetreuung;
    public showErrorMessageNoModule: boolean;
    public datumErsterSchultag: moment.Moment;
    public showNochNichtFreigegeben: boolean = false;
    public showMutiert: boolean = false;
    public aktuellGueltig: boolean = true;


    public modulGroups: TSModulTagesschuleGroup[] = [];

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
    ) {

        super($state, gesuchModelManager, ebeguUtil, CONSTANTS, $scope, berechnungsManager, errorService, authServiceRS,
            wizardStepManager, $stateParams, mitteilungRS, dvDialog, $log, einstellungRS, globalCacheService, $timeout, $translate);

        this.$scope.$watch(() => {
            return this.getBetreuungModel().institutionStammdaten;
        }, (newValue, oldValue) => {
            if (newValue !== oldValue) {
                this.loadModule();
            }
        });
    }

    public $onInit(): void {
        this.loadModule();
        this.datumErsterSchultag = this.gesuchModelManager.gemeindeKonfiguration.konfigTagesschuleErsterSchultag;
        this.setErsterSchultag();
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

    /**
     * Lädt die Module: Diejenigen, die bereits auf der Betreuung waren, werden als ANGEMELDET
     * markiert, die grundsätzlich verfügbaren als "ANGEBOTEN". Alle anderen Wochentage pro Gruppe
     * werden als NICHT-ANGEBOTEN trotzdem hinzugefügt
     */
    private loadModule(): void {
        let moduleAngemeldet = this.getBetreuungModel().belegungTagesschule.moduleTagesschule;
        if (this.getBetreuungModel().institutionStammdaten && this.getBetreuungModel().institutionStammdaten.institutionStammdatenTagesschule) {
            let groupsOfTagesschule: TSModulTagesschuleGroup[] =
                this.getBetreuungModel().institutionStammdaten.institutionStammdatenTagesschule.modulTagesschuleGroups;
            for (const groupTagesschule of groupsOfTagesschule) {
                this.initializeGroup(groupTagesschule);
                let moduleOfGroup = groupTagesschule.module;
                for (const modulOfGroup of moduleOfGroup) {
                    for (const angMod of moduleAngemeldet) {
                        if (angMod.isSameModul(modulOfGroup)) {
                            modulOfGroup.angemeldet = true;
                        }
                    }
                }
            }
            this.modulGroups = groupsOfTagesschule;
        }
    }

    private initializeGroup(group: TSModulTagesschuleGroup): void {
        for (const day of getWeekdaysValues()) {
            let modul = this.getModulForDay(group, day);
            if (!modul) {
                let modul: TSModulTagesschule  = new TSModulTagesschule();
                modul.wochentag = day;
                modul.angeboten = false;
                group.module.push(modul);
            }
        }
    }

    private getModulForDay(group: TSModulTagesschuleGroup, day: TSDayOfWeek): TSModulTagesschule {
        for (const modul of group.module) {
            if (day === modul.wochentag) {
                modul.angeboten = true;
                return modul;
            }
        }
        return undefined;
    }

    public getWeekDays(): TSDayOfWeek[] {
        return getWeekdaysValues();
    }

    public isTagesschuleAlreadySelected(): boolean {
        return EbeguUtil.isNotNullOrUndefined(this.getBetreuungModel().institutionStammdaten)
            && !this.getBetreuungModel().keineDetailinformationen;
    }

    public getButtonTextSpeichern(): string {
        return this.direktAnmeldenSchulamt() ? 'ANMELDEN_TAGESSCHULE' : 'SPEICHERN';
    }

    /**
     * Vor dem Speichern der Betreuung muessen die angemeldeten Module wieder auf
     * die Betreuung zurueckgeschrieben werden
     */
    private preSave(): void {
        let anmeldungen: TSModulTagesschule[] = [];
        for (const group of this.modulGroups) {
            for (const mod of group.module) {
                if (mod.angemeldet) {
                    anmeldungen.push(mod);
                }
            }
        }
        this.getBetreuungModel().belegungTagesschule.moduleTagesschule = anmeldungen;
    }

    /**
     * Diese Methode wird aufgerufen wenn die Anmeldung erfasst oder gespeichert wird.
     */
    public anmelden(): IPromise<any> {
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
        return this.getBetreuungModel().belegungTagesschule.moduleTagesschule
            .filter(modul => modul.angemeldet).length > 0;
    }

    public getModulName(group: TSModulTagesschuleGroup): string {
        return this.$translate.instant(group.modulTagesschuleName) + this.getModulTimeAsString(group);
    }

    public getModulTimeAsStringViaName(group: TSModulTagesschuleGroup): string {
        return this.getModulTimeAsString(group);
    }

    public getModulTimeAsString(modul: TSModulTagesschuleGroup): string {
        if (modul) {
            return `${modul.zeitVon} - ${modul.zeitBis}`;
        }
        return '';
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
}
