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
import {DvDialog} from '../../../app/core/directive/dv-dialog/dv-dialog';
import ErrorService from '../../../app/core/errors/service/ErrorService';
import MitteilungRS from '../../../app/core/service/mitteilungRS.rest';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import {TSAnmeldungMutationZustand} from '../../../models/enums/TSAnmeldungMutationZustand';
import {TSBetreuungsstatus} from '../../../models/enums/TSBetreuungsstatus';
import {getWeekdaysValues, TSDayOfWeek} from '../../../models/enums/TSDayOfWeek';
import {getTSModulTagesschuleNameValues, TSModulTagesschuleName} from '../../../models/enums/TSModulTagesschuleName';
import TSBetreuung from '../../../models/TSBetreuung';
import TSModulTagesschule from '../../../models/TSModulTagesschule';
import DateUtil from '../../../utils/DateUtil';
import EbeguUtil from '../../../utils/EbeguUtil';
import {RemoveDialogController} from '../../dialog/RemoveDialogController';
import {IBetreuungStateParams} from '../../gesuch.route';
import BerechnungsManager from '../../service/berechnungsManager';
import GesuchModelManager from '../../service/gesuchModelManager';
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
        $timeout: ITimeoutService,
        $translate: ITranslateService,
    ) {

        super($state, gesuchModelManager, ebeguUtil, CONSTANTS, $scope, berechnungsManager, errorService, authServiceRS,
            wizardStepManager, $stateParams, mitteilungRS, dvDialog, $log, $timeout, $translate);

        this.$scope.$watch(() => {
            return this.getBetreuungModel().institutionStammdaten;
        }, (newValue, oldValue) => {
            if (newValue !== oldValue) {
                this.filterOnlyAngemeldeteModule();
                this.copyModuleToBelegung();
            }
        });
    }

    public $onInit(): void {
        this.copyModuleToBelegung();
        this.datumErsterSchultag = this.gesuchModelManager.getGesuchsperiode().datumErsterSchultag;
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
        const gp = this.gesuchModelManager.getGesuch().gesuchsperiode;
        if (gp.hasTagesschulenAnmeldung()) {
            if (gp.isTagesschulenAnmeldungKonfiguriert()) {
                const terminValue = DateUtil.momentToLocalDateFormat(gp.datumFreischaltungTagesschule, 'DD.MM.YYYY');
                return this.$translate.instant('FREISCHALTUNG_TAGESSCHULE_AB_INFO', {
                    termin: terminValue,
                });
            }
            return this.$translate.instant('FREISCHALTUNG_TAGESSCHULE_INFO');
        }
        return '';
    }

    public getModulTagesschuleNameList(): TSModulTagesschuleName[] {
        return getTSModulTagesschuleNameValues();
    }

    public getWeekDays(): TSDayOfWeek[] {
        return getWeekdaysValues();
    }

    public isTagesschuleAlreadySelected(): boolean {
        return EbeguUtil.isNotNullOrUndefined(this.getBetreuungModel().institutionStammdaten)
            && !this.getBetreuungModel().keineDetailinformationen;
    }

    public isModulEnabled(modulName: TSModulTagesschuleName, weekday: TSDayOfWeek): boolean {
        return this.getBetreuungModel().isEnabled() && this.isModulDefinedInSelectedTS(modulName, weekday);
    }

    public getMonday(): TSDayOfWeek {
        return TSDayOfWeek.MONDAY;
    }

    /**
     * Gibt true zurueck wenn das gegebene Modul fuer die ausgewaehlte TS definiert wurde und zwar mit zeitBis und
     * zeitVon.
     */
    public isModulDefinedInSelectedTS(modulName: TSModulTagesschuleName, weekday: TSDayOfWeek): boolean {
        const modulTS = this.getModul(modulName, weekday);
        return !!(modulTS && modulTS.zeitBis && modulTS.zeitVon);
    }

    public getModul(modulName: TSModulTagesschuleName, weekday: TSDayOfWeek): TSModulTagesschule {
        if (this.getBetreuungModel().belegungTagesschule
            && this.getBetreuungModel().belegungTagesschule.moduleTagesschule) {

            for (const modulTS of this.getBetreuungModel().belegungTagesschule.moduleTagesschule) {
                if (modulTS.modulTagesschuleName === modulName && modulTS.wochentag === weekday) {
                    return modulTS;
                }
            }
        }
        return null;
    }

    public getButtonTextSpeichern(): string {
        return this.direktAnmeldenSchulamt() ? 'ANMELDEN_TAGESSCHULE' : 'SPEICHERN';
    }

    /**
     * Diese Methode wird aufgerufen wenn die Anmeldung erfasst oder gespeichert wird.
     */
    public anmelden(): IPromise<any> {
        if (this.form.$valid) {
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

    public getModulName(modulName: TSModulTagesschuleName): string {
        const modul = this.getModul(modulName, TSDayOfWeek.MONDAY); // monday ist der Vertreter fuer die ganze Woche
        return this.$translate.instant(TSModulTagesschuleName[modulName]) + this.getModulTimeAsString(modul);
    }

    public getModulTimeAsStringViaName(modulName: TSModulTagesschuleName): string {
        const modul = this.getModul(modulName, TSDayOfWeek.MONDAY);
        if (modul) {
            return `${modul.zeitVon.format('HH:mm')} - ${modul.zeitBis.format('HH:mm')}`;
        }
        return '';
    }

    public getModulTimeAsString(modul: TSModulTagesschule): string {
        if (modul) {
            return ` (${modul.zeitVon.format('HH:mm')} - ${modul.zeitBis.format('HH:mm')})`;
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
        return this.gesuchModelManager.getGesuchsperiode()
            && this.gesuchModelManager.getGesuchsperiode().isTageschulenAnmeldungAktiv();
    }
}
