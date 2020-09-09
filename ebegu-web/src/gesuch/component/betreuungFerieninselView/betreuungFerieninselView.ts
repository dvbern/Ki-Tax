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
import {IComponentOptions, IFormController, IPromise} from 'angular';
import * as moment from 'moment';
import {EinstellungRS} from '../../../admin/service/einstellungRS.rest';
import {FerieninselStammdatenRS} from '../../../admin/service/ferieninselStammdatenRS.rest';
import {DvDialog} from '../../../app/core/directive/dv-dialog/dv-dialog';
import {ErrorService} from '../../../app/core/errors/service/ErrorService';
import {MitteilungRS} from '../../../app/core/service/mitteilungRS.rest';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {TSAnmeldungMutationZustand} from '../../../models/enums/TSAnmeldungMutationZustand';
import {TSBetreuungsstatus} from '../../../models/enums/TSBetreuungsstatus';
import {getTSFeriennameValues, TSFerienname} from '../../../models/enums/TSFerienname';
import {TSBelegungFerieninsel} from '../../../models/TSBelegungFerieninsel';
import {TSBelegungFerieninselTag} from '../../../models/TSBelegungFerieninselTag';
import {TSBetreuung} from '../../../models/TSBetreuung';
import {TSEinstellungenFerieninsel} from '../../../models/TSEinstellungenFerieninsel';
import {TSFerieninselStammdaten} from '../../../models/TSFerieninselStammdaten';
import {DateUtil} from '../../../utils/DateUtil';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {RemoveDialogController} from '../../dialog/RemoveDialogController';
import {IBetreuungStateParams} from '../../gesuch.route';
import {BerechnungsManager} from '../../service/berechnungsManager';
import {GesuchModelManager} from '../../service/gesuchModelManager';
import {GlobalCacheService} from '../../service/globalCacheService';
import {WizardStepManager} from '../../service/wizardStepManager';
import {BetreuungViewController} from '../betreuungView/betreuungView';
import ILogService = angular.ILogService;
import IScope = angular.IScope;
import ITimeoutService = angular.ITimeoutService;
import ITranslateService = angular.translate.ITranslateService;

const dialogTemplate = require('../../dialog/removeDialogTemplate.html');

export class BetreuungFerieninselViewComponentConfig implements IComponentOptions {
    public transclude = false;
    public bindings = {
        betreuung: '=',
        onSave: '&',
        anmeldungSchulamtUebernehmen: '&',
        anmeldungSchulamtAblehnen: '&',
        anmeldungSchulamtFalscheInstitution: '&',
        cancel: '&',
        form: '=',
    };
    public template = require('./betreuungFerieninselView.html');
    public controller = BetreuungFerieninselViewController;
    public controllerAs = 'vm';
}

export class BetreuungFerieninselViewController extends BetreuungViewController {

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
        'FerieninselStammdatenRS',
    ];

    public betreuung: TSBetreuung;
    public onSave: () => void;
    public form: IFormController;
    public showErrorMessage: boolean;

    public ferieninselStammdaten: TSFerieninselStammdaten;
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
        einstellungRS: EinstellungRS,
        globalCacheService: GlobalCacheService,
        $timeout: ITimeoutService,
        $translate: ITranslateService,
        private readonly ferieninselStammdatenRS: FerieninselStammdatenRS,
    ) {
        super($state,
            gesuchModelManager,
            ebeguUtil,
            CONSTANTS,
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
            $translate);
    }

    public $onInit(): void {
        this.initFerieninselViewModel();

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

    public getFeriennamen(): Array<TSFerienname> {
        return getTSFeriennameValues();
    }

    private initFerieninselViewModel(): void {
        if (EbeguUtil.isNotNullOrUndefined(this.betreuung.belegungFerieninsel)) {
            this.changedFerien();

            return;
        }

        this.betreuung.betreuungsstatus = TSBetreuungsstatus.SCHULAMT_ANMELDUNG_ERFASST;
        this.betreuung.belegungFerieninsel = new TSBelegungFerieninsel();
        this.betreuung.belegungFerieninsel.tage = [];
    }

    public isFerieninselAnmeldungAktiv(): boolean {
        return this.gesuchModelManager.gemeindeKonfiguration.isFerieninselAnmeldungAktiv();
    }

    public getFerieninselAnmeldungNotYetReadyText(): string {
        if (this.gesuchModelManager.gemeindeKonfiguration.isFerieninselAnmeldungBeforePeriode()) {
            const terminValue = DateUtil.momentToLocalDateFormat(
                this.gesuchModelManager.gemeindeKonfiguration.konfigFerieninselAktivierungsdatum, 'DD.MM.YYYY');
            return this.$translate.instant('FREISCHALTUNG_FERIENINSEL_AB_INFO', {
                termin: terminValue,
            });
        }
        return this.$translate.instant('FREISCHALTUNG_FERIENINSEL_INFO');
    }

    public changedFerien(): void {
        if (!this.betreuung.belegungFerieninsel || !this.betreuung.belegungFerieninsel.ferienname) {
            return;
        }

        this.ferieninselStammdatenRS.findFerieninselStammdatenByGesuchsperiodeAndFerien(
            this.gesuchModelManager.getGesuchsperiode().id,
            this.gesuchModelManager.getGemeinde().id,
            this.betreuung.belegungFerieninsel.ferienname).then((response: TSFerieninselStammdaten) => {
                this.ferieninselStammdaten = response;
                // Bereits gespeicherte Daten wieder ankreuzen
                for (const obj of this.ferieninselStammdaten.potenzielleFerieninselTageFuerBelegung) {
                    for (const tagAngemeldet of this.betreuung.belegungFerieninsel.tage) {
                        if (tagAngemeldet.tag.isSame(obj.tag)) {
                            obj.angemeldet = true;
                        }
                    }
                }
            });
    }

    public isAnmeldungNichtFreigegeben(): boolean {
        // Ferien sind ausgewaehlt, aber es gibt keine Stammdaten dazu
        return EbeguUtil.isNotNullOrUndefined(this.betreuung.belegungFerieninsel.ferienname)
            && EbeguUtil.isNotNullOrUndefined(this.ferieninselStammdaten)
            && EbeguUtil.isNullOrUndefined(this.ferieninselStammdaten.anmeldeschluss);
    }

    public isAnmeldeschlussAbgelaufen(): boolean {
        // Ferien sind ausgewaehlt, es gibt Stammdaten, aber das Anmeldedatum ist abgelaufen
        return EbeguUtil.isNotNullOrUndefined(this.betreuung.belegungFerieninsel.ferienname)
            && EbeguUtil.isNotNullOrUndefined(this.ferieninselStammdaten)
            && EbeguUtil.isNotNullOrUndefined(this.ferieninselStammdaten.anmeldeschluss)
            && this.ferieninselStammdaten.anmeldeschluss.isBefore(DateUtil.today());
    }

    public isAnmeldungMoeglich(): boolean {
        return EbeguUtil.isNotNullOrUndefined(this.betreuung.belegungFerieninsel.ferienname)
            && !this.isAnmeldeschlussAbgelaufen()
            && !this.isAnmeldungNichtFreigegeben();
    }

    public getButtonTextSpeichern(): string {
        return this.direktAnmeldenSchulamt() ? 'ANMELDEN_FERIENINSEL' : 'SPEICHERN';
    }

    public anmelden(): IPromise<any> {
        if (this.form.$valid) {
            // Validieren, dass mindestens 1 Tag ausgewählt war
            this.setChosenFerientage();
            if (this.betreuung.belegungFerieninsel.tage.length <= 0) {
                if (this.isAnmeldungMoeglich()) {
                    this.showErrorMessage = true;
                }
                return undefined;
            }
            if (this.direktAnmeldenSchulamt()) {
                return this.dvDialog.showRemoveDialog(dialogTemplate, this.form, RemoveDialogController, {
                    title: 'CONFIRM_SAVE_FERIENINSEL',
                    deleteText: 'BESCHREIBUNG_SAVE_FERIENINSEL',
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

    private setChosenFerientage(): void {
        this.betreuung.belegungFerieninsel.tage = [];
        for (const tag of this.ferieninselStammdaten.potenzielleFerieninselTageFuerBelegung) {
            if (tag.angemeldet) {
                this.betreuung.belegungFerieninsel.tage.push(tag);
            }
        }
    }

    public showButtonsInstitution(): boolean {
        return this.betreuung.betreuungsstatus === TSBetreuungsstatus.SCHULAMT_ANMELDUNG_AUSGELOEST
            && !this.gesuchModelManager.isGesuchReadonlyForRole();
    }

    /**
     * Muss ueberschrieben werden, damit die richtige betreuung zurueckgegeben wird
     */
    public getBetreuungModel(): TSBetreuung {
        return this.betreuung;
    }

    public getMomentWeekdays(): string[] {
        const weekdays = moment.weekdays();
        weekdays.splice(0, 1);
        weekdays.splice(5, 1);
        return weekdays;
    }

    public displayBreak(
        tag: TSBelegungFerieninselTag,
        index: number,
        dayArray: Array<TSBelegungFerieninselTag>,
    ): boolean {
        return dayArray[index + 1] ? tag.tag.week() !== dayArray[index + 1].tag.week() : false;
    }

    public displayWeekRow(
        tag: TSBelegungFerieninselTag,
        index: number,
        dayArray: Array<TSBelegungFerieninselTag>,
    ): boolean {
        return dayArray[index + 1] ? dayArray[index + 1].tag.diff(tag.tag, 'days') > 7 : false;
    }

    public getEinstellungenFerieninsel(): TSEinstellungenFerieninsel {
        const institutionStammdaten = this.getBetreuungModel().institutionStammdaten;
        if (!institutionStammdaten) {
            return undefined;
        }
        const stammdatenFerieninsel = institutionStammdaten.institutionStammdatenFerieninsel;
        if (!stammdatenFerieninsel || EbeguUtil.isNullOrUndefined(this.gesuchModelManager.getGesuchsperiode())) {
            return undefined;
        }
        const tsEinstellungenTFerieninsel =
            stammdatenFerieninsel.einstellungenFerieninsel
                .filter((einstellung: TSEinstellungenFerieninsel) =>
                    einstellung.gesuchsperiode.id === this.gesuchModelManager.getGesuchsperiode().id)
                .pop();
        return tsEinstellungenTFerieninsel;
    }

    public hasAusweichstandort(): boolean {

        const einstellungen = this.getEinstellungenFerieninsel();

        if (!einstellungen) {
            return false;
        }
        return einstellungen.isAusweichstandortDefined(this.betreuung.belegungFerieninsel.ferienname);
    }

    public getAusgewaehltFeriensequenz(): string {

        const einstellungen = this.getEinstellungenFerieninsel();

        if (!einstellungen) {
            return '';
        }

        if (this.hasAusweichstandort()) {
            return einstellungen.getAusweichstandortFromFerienname(this.betreuung.belegungFerieninsel.ferienname);
        }
        return '';
    }

    public saveAnmeldungSchulamtUebernehmen(): void {
        if (this.form.$valid) {
            this.anmeldungSchulamtUebernehmen({isScolaris: false});
        }
    }
}
