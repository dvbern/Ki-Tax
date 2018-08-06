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
import {StateService} from '@uirouter/core';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import {DvDialog} from '../../../core/directive/dv-dialog/dv-dialog';
import ErrorService from '../../../core/errors/service/ErrorService';
import MitteilungRS from '../../../core/service/mitteilungRS.rest';
import {TSBetreuungsstatus} from '../../../models/enums/TSBetreuungsstatus';
import {getWeekdaysValues, TSDayOfWeek} from '../../../models/enums/TSDayOfWeek';
import {getTSModulTagesschuleNameValues, TSModulTagesschuleName} from '../../../models/enums/TSModulTagesschuleName';
import TSBetreuung from '../../../models/TSBetreuung';
import TSGesuchsperiode from '../../../models/TSGesuchsperiode';
import TSModulTagesschule from '../../../models/TSModulTagesschule';
import DateUtil from '../../../utils/DateUtil';
import EbeguUtil from '../../../utils/EbeguUtil';
import {RemoveDialogController} from '../../dialog/RemoveDialogController';
import {IBetreuungStateParams} from '../../gesuch.route';
import BerechnungsManager from '../../service/berechnungsManager';
import GesuchModelManager from '../../service/gesuchModelManager';
import WizardStepManager from '../../service/wizardStepManager';
import {BetreuungViewController} from '../betreuungView/betreuungView';
import {TSAnmeldungMutationZustand} from '../../../models/enums/TSAnmeldungMutationZustand';
import moment = require('moment');
import ILogService = angular.ILogService;
import IPromise = angular.IPromise;
import IScope = angular.IScope;
import ITimeoutService = angular.ITimeoutService;
import ITranslateService = angular.translate.ITranslateService;
import IFormController = angular.IFormController;

const template = require('./betreuungTagesschuleView.html');
require('./betreuungTagesschuleView.less');
const dialogTemplate = require('../../dialog/removeDialogTemplate.html');

export class BetreuungTagesschuleViewComponentConfig implements IComponentOptions {
    transclude = false;
    bindings: any = {
        betreuung: '=',
        onSave: '&',
        cancel: '&',
        anmeldungSchulamtUebernehmen: '&',
        anmeldungSchulamtAblehnen: '&',
        anmeldungSchulamtFalscheInstitution: '&',
        form: '='
    };
    template = template;
    controller = BetreuungTagesschuleViewController;
    controllerAs = 'vm';
}

export class BetreuungTagesschuleViewController extends BetreuungViewController {

    static $inject = ['$state', 'GesuchModelManager', 'EbeguUtil', 'CONSTANTS', '$scope', 'BerechnungsManager', 'ErrorService',
        'AuthServiceRS', 'WizardStepManager', '$stateParams', 'MitteilungRS', 'DvDialog', '$log', '$timeout', '$translate'];

    onSave: () => void;
    form: IFormController;
    betreuung: TSBetreuung;
    showErrorMessageNoModule: boolean;
    datumErsterSchultag: moment.Moment;
    showNochNichtFreigegeben: boolean = false;
    showMutiert: boolean = false;
    aktuellGueltig: boolean = true;

    constructor($state: StateService, gesuchModelManager: GesuchModelManager, ebeguUtil: EbeguUtil, CONSTANTS: any,
                $scope: IScope, berechnungsManager: BerechnungsManager, errorService: ErrorService,
                authServiceRS: AuthServiceRS, wizardStepManager: WizardStepManager, $stateParams: IBetreuungStateParams,
                mitteilungRS: MitteilungRS, dvDialog: DvDialog, $log: ILogService,
                $timeout: ITimeoutService, $translate: ITranslateService) {

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

    $onInit() {
        this.copyModuleToBelegung();
        this.datumErsterSchultag = this.gesuchModelManager.getGesuchsperiode().datumErsterSchultag;
        this.setErsterSchultag();
        //todo dupliziert refactoren
        if (this.getBetreuungModel().anmeldungMutationZustand) {
            if (this.getBetreuungModel().anmeldungMutationZustand === TSAnmeldungMutationZustand.MUTIERT) {
                this.showMutiert = true;
                this.aktuellGueltig = false;
            } else if (this.getBetreuungModel().anmeldungMutationZustand === TSAnmeldungMutationZustand.NOCH_NICHT_FREIGEGEBEN) {
                this.showNochNichtFreigegeben = true;
                this.aktuellGueltig = false;
            }
        }
    }

    public getTagesschuleAnmeldungNotYetReadyText(): string {
        const gp: TSGesuchsperiode = this.gesuchModelManager.getGesuch().gesuchsperiode;
        if (gp.hasTagesschulenAnmeldung()) {
            if (gp.isTagesschulenAnmeldungKonfiguriert()) {
                const terminValue: string = DateUtil.momentToLocalDateFormat(gp.datumFreischaltungTagesschule, 'DD.MM.YYYY');
                return this.$translate.instant('FREISCHALTUNG_TAGESSCHULE_AB_INFO', {
                    termin: terminValue
                });
            } else {
                return this.$translate.instant('FREISCHALTUNG_TAGESSCHULE_INFO');
            }
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
        return EbeguUtil.isNotNullOrUndefined(this.getBetreuungModel().institutionStammdaten) && !this.getBetreuungModel().keineDetailinformationen;
    }

    public isModulEnabled(modulName: TSModulTagesschuleName, weekday: TSDayOfWeek): boolean {
        return this.getBetreuungModel().isEnabled() && this.isModulDefinedInSelectedTS(modulName, weekday);
    }

    public getMonday(): TSDayOfWeek {
        return TSDayOfWeek.MONDAY;
    }

    /**
     * Gibt true zurueck wenn das gegebene Modul fuer die ausgewaehlte TS definiert wurde und zwar mit zeitBis und zeitVon.
     */
    public isModulDefinedInSelectedTS(modulName: TSModulTagesschuleName, weekday: TSDayOfWeek): boolean {
        const modulTS: TSModulTagesschule = this.getModul(modulName, weekday);
        return !!(modulTS && modulTS.zeitBis && modulTS.zeitVon);
    }

    public getModul(modulName: TSModulTagesschuleName, weekday: TSDayOfWeek): TSModulTagesschule {
        if (this.getBetreuungModel().belegungTagesschule && this.getBetreuungModel().belegungTagesschule.moduleTagesschule) {
            for (const modulTS of this.getBetreuungModel().belegungTagesschule.moduleTagesschule) {
                if (modulTS.modulTagesschuleName === modulName && modulTS.wochentag === weekday) {
                    return modulTS;
                }
            }
        }
        return null;
    }

    public getButtonTextSpeichern(): string {
        return this.direktAnmeldenSchulamt() ? 'ANMELDEN_TAGESSCHULE' : 'SPEICHERN_UPPER';
    }

    /**
     * Diese Methode wird aufgerufen wenn die Anmeldung erfasst oder gespeichert wird.
     */
    public anmelden(): IPromise<any> {
        if (this.form.$valid) {
            // Validieren, dass mindestens 1 Modul ausgewählt war --> ausser der Betreuungsstatus ist (noch) SCHULAMT_FALSCHE_INSTITUTION
            if (!(this.betreuung.isBetreuungsstatus(TSBetreuungsstatus.SCHULAMT_FALSCHE_INSTITUTION) || this.betreuung.keineDetailinformationen) && !this.isThereAnyAnmeldung()) {
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
                    elementID: undefined
                }).then(() => {
                    this.onSave();
                });
            } else {
                this.onSave();
            }
        }
        return undefined;
    }

    private isThereAnyAnmeldung(): boolean {
        return this.getBetreuungModel().belegungTagesschule.moduleTagesschule
            .filter(modul => modul.angemeldet === true).length > 0;
    }

    public getModulName(modulName: TSModulTagesschuleName): string {
        const modul: TSModulTagesschule = this.getModul(modulName, TSDayOfWeek.MONDAY); // monday ist der Vertreter fuer die ganze Woche
        return this.$translate.instant(TSModulTagesschuleName[modulName]) + this.getModulTimeAsString(modul);
    }

    public getModulTimeAsStringViaName(modulName: TSModulTagesschuleName): string {
        const modul: TSModulTagesschule = this.getModul(modulName, TSDayOfWeek.MONDAY);
        if (modul) {
            return modul.zeitVon.format('HH:mm') + ' - ' + modul.zeitBis.format('HH:mm');
        }
        return '';
    }

    public getModulTimeAsString(modul: TSModulTagesschule): string {
        if (modul) {
            return ' (' + modul.zeitVon.format('HH:mm') + ' - ' + modul.zeitBis.format('HH:mm') + ')';
        }
        return '';
    }

    public showButtonsInstitution(): boolean {
        return this.getBetreuungModel().betreuungsstatus === TSBetreuungsstatus.SCHULAMT_ANMELDUNG_AUSGELOEST && !this.gesuchModelManager.isGesuchReadonlyForRole();
    }

    /**
     * Muss ueberschrieben werden, damit die richtige betreuung zurueckgegeben wird
     */
    public getBetreuungModel(): TSBetreuung {
        return this.betreuung;
    }
}
