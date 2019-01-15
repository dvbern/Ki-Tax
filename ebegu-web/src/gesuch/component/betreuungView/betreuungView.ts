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
import * as $ from 'jquery';
import {DvDialog} from '../../../app/core/directive/dv-dialog/dv-dialog';
import ErrorService from '../../../app/core/errors/service/ErrorService';
import MitteilungRS from '../../../app/core/service/mitteilungRS.rest';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import {TSAnmeldungMutationZustand} from '../../../models/enums/TSAnmeldungMutationZustand';
import {isVerfuegtOrSTV, TSAntragStatus} from '../../../models/enums/TSAntragStatus';
import {
    getTSBetreuungsangebotTypValues,
    getTSBetreuungsangebotTypValuesNoTagesschuleanmeldungen,
    TSBetreuungsangebotTyp,
} from '../../../models/enums/TSBetreuungsangebotTyp';
import {TSBetreuungsstatus} from '../../../models/enums/TSBetreuungsstatus';
import {TSGesuchsperiodeStatus} from '../../../models/enums/TSGesuchsperiodeStatus';
import {TSPensumUnits} from '../../../models/enums/TSPensumUnits';
import {TSWizardStepName} from '../../../models/enums/TSWizardStepName';
import TSBelegungTagesschule from '../../../models/TSBelegungTagesschule';
import TSBetreuung from '../../../models/TSBetreuung';
import TSBetreuungsmitteilung from '../../../models/TSBetreuungsmitteilung';
import TSBetreuungspensum from '../../../models/TSBetreuungspensum';
import TSBetreuungspensumContainer from '../../../models/TSBetreuungspensumContainer';
import TSErweiterteBetreuung from '../../../models/TSErweiterteBetreuung';
import TSErweiterteBetreuungContainer from '../../../models/TSErweiterteBetreuungContainer';
import TSExceptionReport from '../../../models/TSExceptionReport';
import {TSFachstelle} from '../../../models/TSFachstelle';
import TSInstitutionStammdaten from '../../../models/TSInstitutionStammdaten';
import TSKindContainer from '../../../models/TSKindContainer';
import TSModulTagesschule from '../../../models/TSModulTagesschule';
import {TSDateRange} from '../../../models/types/TSDateRange';
import DateUtil from '../../../utils/DateUtil';
import EbeguUtil from '../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {RemoveDialogController} from '../../dialog/RemoveDialogController';
import {IBetreuungStateParams} from '../../gesuch.route';
import BerechnungsManager from '../../service/berechnungsManager';
import GesuchModelManager from '../../service/gesuchModelManager';
import WizardStepManager from '../../service/wizardStepManager';
import AbstractGesuchViewController from '../abstractGesuchView';
import ILogService = angular.ILogService;
import IScope = angular.IScope;
import ITimeoutService = angular.ITimeoutService;
import ITranslateService = angular.translate.ITranslateService;

const removeDialogTemplate = require('../../dialog/removeDialogTemplate.html');

export class BetreuungViewComponentConfig implements IComponentOptions {
    public transclude = false;
    public template = require('./betreuungView.html');
    public controller = BetreuungViewController;
    public controllerAs = 'vm';
}

const GESUCH_BETREUUNGEN = 'gesuch.betreuungen';
const PENDENZEN_BETREUUNG = 'pendenzenBetreuungen.list-view';

export class BetreuungViewController extends AbstractGesuchViewController<TSBetreuung> {

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
    public betreuungsangebot: any;
    public betreuungsangebotValues: Array<any>;
    public instStammId: string; // der ausgewaehlte instStammId wird hier gespeichert und dann in die entsprechende
    // InstitutionStammdaten umgewandert
    public isSavingData: boolean; // Semaphore
    public initialBetreuung: TSBetreuung;
    public flagErrorVertrag: boolean;
    public kindModel: TSKindContainer;
    public betreuungIndex: number;
    public isMutationsmeldungStatus: boolean;
    public mutationsmeldungModel: TSBetreuung;
    public existingMutationsMeldung: TSBetreuungsmitteilung;
    public isNewestGesuch: boolean;
    public dvDialog: DvDialog;
    public $translate: ITranslateService;
    public moduleBackup: TSModulTagesschule[] = undefined;
    public aktuellGueltig: boolean = true;
    public isDuplicated: boolean = false;
    // der ausgewaehlte fachstelleId wird hier gespeichert und dann in die entsprechende Fachstelle umgewandert
    public fachstelleId: string;

    public constructor(
        private readonly $state: StateService,
        gesuchModelManager: GesuchModelManager,
        private readonly ebeguUtil: EbeguUtil,
        private readonly CONSTANTS: any,
        $scope: IScope,
        berechnungsManager: BerechnungsManager,
        private readonly errorService: ErrorService,
        private readonly authServiceRS: AuthServiceRS,
        wizardStepManager: WizardStepManager,
        private readonly $stateParams: IBetreuungStateParams,
        private readonly mitteilungRS: MitteilungRS,
        dvDialog: DvDialog,
        private readonly $log: ILogService,
        $timeout: ITimeoutService,
        $translate: ITranslateService,
    ) {
        super(gesuchModelManager, berechnungsManager, wizardStepManager, $scope, TSWizardStepName.BETREUUNG, $timeout);
        this.dvDialog = dvDialog;
        this.$translate = $translate;
    }

    // tslint:disable-next-line:cognitive-complexity
    public $onInit(): void {
        this.mutationsmeldungModel = undefined;
        this.isMutationsmeldungStatus = false;
        const kindNumber = parseInt(this.$stateParams.kindNumber, 10);
        const kindIndex = this.gesuchModelManager.convertKindNumberToKindIndex(kindNumber);
        if (kindIndex >= 0) {
            this.gesuchModelManager.setKindIndex(kindIndex);
            if (this.$stateParams.betreuungNumber && this.$stateParams.betreuungNumber.length > 0) {
                const betreuungNumber = parseInt(this.$stateParams.betreuungNumber, 10);
                this.betreuungIndex = this.gesuchModelManager.convertBetreuungNumberToBetreuungIndex(betreuungNumber);
                this.model = angular.copy(this.gesuchModelManager.getKindToWorkWith().betreuungen[this.betreuungIndex]);
                this.initialBetreuung =
                    angular.copy(this.gesuchModelManager.getKindToWorkWith().betreuungen[this.betreuungIndex]);
                this.gesuchModelManager.setBetreuungIndex(this.betreuungIndex);
            } else {
                // wenn betreuung-nummer nicht definiert ist heisst das, dass wir ein neues erstellen sollten
                this.model = this.initEmptyBetreuung();
                this.initialBetreuung = angular.copy(this.model);
                this.betreuungIndex = this.gesuchModelManager.getKindToWorkWith().betreuungen
                    ? this.gesuchModelManager.getKindToWorkWith().betreuungen.length
                    : 0;
                this.gesuchModelManager.setBetreuungIndex(this.betreuungIndex);
            }

            this.setBetreuungsangebotTypValues();
            // Falls ein Typ gesetzt ist, handelt es sich um eine direkt-Anmeldung
            if (this.$stateParams.betreuungsangebotTyp) {
                for (const obj of this.betreuungsangebotValues) {
                    if (obj.key === this.$stateParams.betreuungsangebotTyp) {
                        this.betreuungsangebot = obj;
                        this.changedAngebot();
                    }
                }
            } else {
                this.betreuungsangebot = undefined;
            }
            this.initViewModel();

            if (this.getErweiterteBetreuungJA() && this.getErweiterteBetreuungJA().fachstelle) {
                this.fachstelleId = this.getErweiterteBetreuungJA().fachstelle.id;
            }

            // just to read!
            this.kindModel = this.gesuchModelManager.getKindToWorkWith();
        } else {
            this.$log.error('There is no kind available with kind-number:' + this.$stateParams.kindNumber);
        }
        this.isNewestGesuch = this.gesuchModelManager.isNeuestesGesuch();

        this.findExistingBetreuungsmitteilung();
        const anmeldungMutationZustand = this.getBetreuungModel().anmeldungMutationZustand;
        if (!anmeldungMutationZustand) {
            return;
        }

        if (anmeldungMutationZustand === TSAnmeldungMutationZustand.MUTIERT) {
            this.aktuellGueltig = false;
        } else if (anmeldungMutationZustand === TSAnmeldungMutationZustand.NOCH_NICHT_FREIGEGEBEN) {
            this.aktuellGueltig = false;
        }
    }

    /**
     * Creates a Betreuung for the kind given by the kindNumber attribute of the class.
     * Thus the kindnumber must be set before this method is called.
     */
    public initEmptyBetreuung(): TSBetreuung {
        const tsBetreuung = new TSBetreuung();
        tsBetreuung.erweiterteBetreuungContainer = new TSErweiterteBetreuungContainer();
        tsBetreuung.erweiterteBetreuungContainer.erweiterteBetreuungJA = new TSErweiterteBetreuung();
        tsBetreuung.betreuungsstatus = TSBetreuungsstatus.AUSSTEHEND;

        return tsBetreuung;
    }

    private initViewModel(): void {
        this.isSavingData = false;
        this.flagErrorVertrag = false;
        if (this.getInstitutionSD()) {
            this.instStammId = this.getInstitutionSD().id;
            this.betreuungsangebot = this.getBetreuungsangebotFromInstitutionList();
        }
        this.startEmptyListOfBetreuungspensen();
        // institutionen lazy laden
        if (!this.gesuchModelManager.getActiveInstitutionenList()
            || this.gesuchModelManager.getActiveInstitutionenList().length <= 0) {
            this.gesuchModelManager.updateActiveInstitutionenList();
        }
        if (this.getErweiterteBetreuungJA() && this.getErweiterteBetreuungJA().fachstelle) {
            this.fachstelleId = this.getErweiterteBetreuungJA().fachstelle.id;
        }
        if (!this.gesuchModelManager.getFachstellenErweiterteBetreuungList()
            || this.gesuchModelManager.getFachstellenErweiterteBetreuungList().length <= 0) {
            this.gesuchModelManager.updateFachstellenErweiterteBetreuungList();
        }
    }

    /**
     * Fuer Institutionen und Traegerschaften wird es geprueft ob es schon ein Betreuungspensum existiert,
     * wenn nicht wir die Liste dann mit einem leeren initiallisiert
     */
    private startEmptyListOfBetreuungspensen(): void {
        if ((!this.getBetreuungspensen() || this.getBetreuungspensen().length === 0)
            && (this.authServiceRS.isOneOfRoles(TSRoleUtil.getTraegerschaftInstitutionOnlyRoles()))) {
            // nur fuer Institutionen wird ein Betreuungspensum by default erstellt
            this.createBetreuungspensum();
        }
    }

    public getGesuchsperiodeBegin(): moment.Moment {
        return this.gesuchModelManager.getGesuchsperiodeBegin();
    }

    private getBetreuungsangebotFromInstitutionList(): any {
        return $.grep(this.betreuungsangebotValues, (value: any) => {
            return value.key === this.getInstitutionSD().betreuungsangebotTyp;
        })[0];
    }

    public getKindModel(): TSKindContainer {
        return this.kindModel;
    }

    public getBetreuungModel(): TSBetreuung {
        if (this.isMutationsmeldungStatus && this.mutationsmeldungModel) {
            return this.mutationsmeldungModel;
        }

        return this.model;
    }

    public changedAngebot(): void {
        if (!this.getBetreuungModel()) {
            return;
        }

        if (this.isSchulamt()) {
            if (this.isTagesschule()) {
                // Nur fuer die neuen Gesuchsperiode kann die Belegung erfast werden
                if (this.gesuchModelManager.getGesuchsperiode().hasTagesschulenAnmeldung()
                    && this.isTageschulenAnmeldungAktiv()) {
                    this.getBetreuungModel().betreuungsstatus = TSBetreuungsstatus.SCHULAMT_ANMELDUNG_ERFASST;
                    if (!this.getBetreuungModel().belegungTagesschule) {
                        this.getBetreuungModel().belegungTagesschule = new TSBelegungTagesschule();
                        this.setErsterSchultag();
                    }
                } else {
                    // "Alte" Tagesschule: Noch keine Modulanmeldung moeglich. Wir setzen Default-Institution
                    this.getBetreuungModel().betreuungsstatus = TSBetreuungsstatus.SCHULAMT;
                    // Fuer Tagesschule setzen wir eine Dummy-Tagesschule als Institution
                    this.instStammId = this.CONSTANTS.INSTITUTIONSSTAMMDATENID_DUMMY_TAGESSCHULE;
                    this.setSelectedInstitutionStammdaten();
                }
            }
        } else {
            this.getBetreuungModel().betreuungsstatus = TSBetreuungsstatus.AUSSTEHEND;
            this.cleanInstitutionStammdaten();
        }
        this.cleanBelegungen();
    }

    public setErsterSchultag(): void {
        // Default Eintrittsdatum ist erster Schultag, wenn noch in Zukunft
        const ersterSchultag = this.gesuchModelManager.getGesuchsperiode().datumErsterSchultag;
        if (ersterSchultag && !this.getBetreuungModel().keineDetailinformationen && DateUtil.today().isBefore(
            ersterSchultag)) {
            this.getBetreuungModel().belegungTagesschule.eintrittsdatum = ersterSchultag;
        }
    }

    private save(newStatus: TSBetreuungsstatus, nextStep: string, params: any): void {
        this.isSavingData = true;
        const oldStatus = this.model.betreuungsstatus;
        if (this.getBetreuungModel() && this.isSchulamt()) {
            // fuer Tagesschule werden keine Betreuungspensum benoetigt, deswegen löschen wir sie vor dem Speichern
            this.getBetreuungModel().betreuungspensumContainers = [];
            if (this.isTagesschule()) {
                this.filterOnlyAngemeldeteModule();
            }
        }
        this.errorService.clearAll();
        this.gesuchModelManager.saveBetreuung(this.model, newStatus, false).then(() => {
            this.gesuchModelManager.setBetreuungToWorkWith(this.model); // setze model
            this.isSavingData = false;
            this.form.$setPristine();
            this.$state.go(nextStep, params);
        }).catch((exception: TSExceptionReport[]) => {
            // starting over
            this.$log.error('there was an error saving the betreuung ', this.model, exception);
            if (exception[0].errorCodeEnum === 'ERROR_DUPLICATE_BETREUUNG') {
                this.isDuplicated = true;
                this.copyModuleToBelegung();
            } else {
                this.isSavingData = false;
                this.model.betreuungsstatus = oldStatus;
                this.startEmptyListOfBetreuungspensen();
                this.form.$setUntouched();
                this.form.$setPristine();
                this.model.institutionStammdaten = this.initialBetreuung.institutionStammdaten;
            }

            return undefined;
        });
    }

    /**
     * This method saves the Betreuung as it is and it doesn't trigger any other action.
     */
    public saveBetreuung(): void {
        if (this.isGesuchValid()) {
            this.save(null, GESUCH_BETREUUNGEN, {gesuchId: this.getGesuchId()});
        }
    }

    /**
     * Entfernt alle Module die nicht als angemeldet markiert sind. Nur fuer Gesuchsperiode in denen die
     * Tagesschuleanmeldung aktiv ist.
     */
    public filterOnlyAngemeldeteModule(): void {
        const betreuungModel = this.getBetreuungModel();
        if (!(this.gesuchModelManager.getGesuchsperiode().hasTagesschulenAnmeldung() &&
            betreuungModel.belegungTagesschule && betreuungModel.belegungTagesschule.moduleTagesschule)) {
            return;
        }
        if (this.moduleBackup === undefined
            && this.isBetreuungsstatus(TSBetreuungsstatus.SCHULAMT_FALSCHE_INSTITUTION)) {
            this.moduleBackup = betreuungModel.belegungTagesschule.moduleTagesschule
                .filter(modul => modul.angemeldet);
            betreuungModel.belegungTagesschule.moduleTagesschule = this.moduleBackup;
        } else if (this.moduleBackup !== undefined
            && this.isBetreuungsstatus(TSBetreuungsstatus.SCHULAMT_FALSCHE_INSTITUTION)) {
            betreuungModel.belegungTagesschule.moduleTagesschule = this.moduleBackup;
        } else {
            betreuungModel.belegungTagesschule.moduleTagesschule =
                betreuungModel.belegungTagesschule.moduleTagesschule
                    .filter(modul => modul.angemeldet);
        }
    }

    /**
     * Kopiert alle Module der ausgewaehlten Tagesschule in die Belegung, sodass man direkt in die Belegung die Module
     * auswaehlen kann.
     */
    public copyModuleToBelegung(): void {
        const stammdaten = this.getBetreuungModel().institutionStammdaten;
        if (!(stammdaten && stammdaten.institutionStammdatenTagesschule
            && stammdaten.institutionStammdatenTagesschule.moduleTagesschule)) {
            return;
        }

        const tagesschule = this.getBetreuungModel().belegungTagesschule.moduleTagesschule;
        const angemeldeteModule = angular.copy(tagesschule);
        this.getBetreuungModel().belegungTagesschule.moduleTagesschule =
            angular.copy(stammdaten.institutionStammdatenTagesschule.moduleTagesschule);

        if (!angemeldeteModule) {
            return;
        }

        angemeldeteModule.forEach(angemeldetesModul => {
            this.getBetreuungModel().belegungTagesschule.moduleTagesschule.forEach(instModul => {
                if (angemeldetesModul.isSameModul(instModul)) {
                    instModul.angemeldet = true;
                }
            });
        });
    }

    public anmeldenSchulamt(): void {
        if (this.direktAnmeldenSchulamt()) {
            this.save(TSBetreuungsstatus.SCHULAMT_ANMELDUNG_AUSGELOEST,
                GESUCH_BETREUUNGEN,
                {gesuchId: this.getGesuchId()});
        } else {
            this.save(TSBetreuungsstatus.SCHULAMT_ANMELDUNG_ERFASST,
                GESUCH_BETREUUNGEN,
                {gesuchId: this.getGesuchId()});
        }
    }

    public direktAnmeldenSchulamt(): boolean {
        // Eigentlich immer ausser in Bearbeitung GS
        return !(this.isGesuchInStatus(TSAntragStatus.IN_BEARBEITUNG_GS)
            || this.isGesuchInStatus(TSAntragStatus.FREIGABEQUITTUNG));
    }

    public enableBetreuungsangebotsTyp(): boolean {
        return this.model
            && this.model.isNew()
            && !this.gesuchModelManager.isGesuchReadonly()
            && !this.gesuchModelManager.isKorrekturModusJugendamt();
    }

    public showInstitutionenList(): boolean {
        return this.getBetreuungModel()
            && (
                this.isTageschulenAnmeldungAktiv() &&
                (this.isEnabled() || this.isBetreuungsstatus(TSBetreuungsstatus.SCHULAMT_FALSCHE_INSTITUTION))
                || !this.isTageschulenAnmeldungAktiv() && (this.isEnabled() && !this.isTagesschule())
            )
            && !this.getBetreuungModel().keineDetailinformationen;
    }

    public showInstitutionenAsText(): boolean {
        return (
                (
                    this.isTageschulenAnmeldungAktiv() &&
                    !this.isEnabled() &&
                    !this.isBetreuungsstatus(TSBetreuungsstatus.SCHULAMT_FALSCHE_INSTITUTION)
                )
                ||
                (
                    !this.isTageschulenAnmeldungAktiv() && !this.isEnabled() && !this.isTagesschule()
                )
            )
            && !this.getBetreuungModel().keineDetailinformationen;
    }

    public isTageschulenAnmeldungAktiv(): boolean {
        return this.gesuchModelManager.getGesuchsperiode().isTageschulenAnmeldungAktiv();
    }

    public isFalscheInstitutionAndUserInRole(): boolean {
        return this.authServiceRS.isOneOfRoles(TSRoleUtil.getAdministratorJugendamtSchulamtRoles())
            && this.isBetreuungsstatus(TSBetreuungsstatus.SCHULAMT_FALSCHE_INSTITUTION)
            && this.aktuellGueltig;
    }

    public anmeldungSchulamtUebernehmen(): void {
        this.copyBGNumberLToClipboard();
        this.dvDialog.showRemoveDialog(removeDialogTemplate, this.form, RemoveDialogController, {
            title: 'CONFIRM_UEBERNAHME_SCHULAMT',
            deleteText: 'BESCHREIBUNG_UEBERNAHME_SCHULAMT',
        }).then(() => {
            if (this.authServiceRS.isOneOfRoles(TSRoleUtil.getTraegerschaftInstitutionOnlyRoles())) {
                this.save(TSBetreuungsstatus.SCHULAMT_ANMELDUNG_UEBERNOMMEN,
                    PENDENZEN_BETREUUNG,
                    undefined);
            } else {
                this.save(TSBetreuungsstatus.SCHULAMT_ANMELDUNG_UEBERNOMMEN,
                    GESUCH_BETREUUNGEN,
                    {gesuchId: this.getGesuchId()});
            }
        });
    }

    public anmeldungSchulamtAblehnen(): void {
        if (this.authServiceRS.isOneOfRoles(TSRoleUtil.getTraegerschaftInstitutionOnlyRoles())) {
            this.save(TSBetreuungsstatus.SCHULAMT_ANMELDUNG_ABGELEHNT, PENDENZEN_BETREUUNG, undefined);
        } else {
            const params = {gesuchId: this.getGesuchId()};
            this.save(TSBetreuungsstatus.SCHULAMT_ANMELDUNG_ABGELEHNT, GESUCH_BETREUUNGEN, params);
        }
    }

    public anmeldungSchulamtFalscheInstitution(): void {
        if (this.authServiceRS.isOneOfRoles(TSRoleUtil.getTraegerschaftInstitutionOnlyRoles())) {
            this.save(TSBetreuungsstatus.SCHULAMT_FALSCHE_INSTITUTION, PENDENZEN_BETREUUNG, undefined);
        } else {
            const params = {gesuchId: this.getGesuchId()};
            this.save(TSBetreuungsstatus.SCHULAMT_FALSCHE_INSTITUTION, GESUCH_BETREUUNGEN, params);
        }
    }

    private copyBGNumberLToClipboard(): void {
        const bgNumber = this.ebeguUtil.calculateBetreuungsIdFromBetreuung(this.gesuchModelManager.getFall(),
            this.gesuchModelManager.getDossier().gemeinde, this.getBetreuungModel());
        const $temp = $('<input>');
        $('body').append($temp);
        $temp.val(bgNumber).select();
        document.execCommand('copy');
        $temp.remove();
    }

    private setBetreuungsangebotTypValues(): void {
        const gesuchsperiode = this.gesuchModelManager.getGesuchsperiode();
        const betreuungsangebotTypValues = gesuchsperiode && gesuchsperiode.hasTagesschulenAnmeldung() ?
            getTSBetreuungsangebotTypValues() :
            getTSBetreuungsangebotTypValuesNoTagesschuleanmeldungen();
        this.betreuungsangebotValues = this.ebeguUtil.translateStringList(betreuungsangebotTypValues);
    }

    public cancel(): void {
        this.reset();
        this.form.$setPristine();
        this.$state.go(GESUCH_BETREUUNGEN, {gesuchId: this.getGesuchId()});
    }

    public reset(): void {
        this.removeBetreuungFromKind(); // wenn model existiert und nicht neu ist wegnehmen, sonst resetten
    }

    private removeBetreuungFromKind(): void {
        if (this.model && !this.model.timestampErstellt) {
            // wenn die Betreeung noch nicht erstellt wurde, loeschen wir die Betreuung vom Array
            this.gesuchModelManager.removeBetreuungFromKind();
        }
    }

    public getInstitutionenSDList(): Array<TSInstitutionStammdaten> {
        if (!this.betreuungsangebot) {
            return [];
        }

        return this.gesuchModelManager.getActiveInstitutionenList()
            .filter(instStamm => instStamm.betreuungsangebotTyp === this.betreuungsangebot.key
                && this.gesuchModelManager.isDefaultTagesschuleAllowed(instStamm));
    }

    public getInstitutionSD(): TSInstitutionStammdaten {
        if (this.getBetreuungModel()) {
            return this.getBetreuungModel().institutionStammdaten;
        }

        return undefined;
    }

    public getErweiterteBetreuungJA(): TSErweiterteBetreuung {
        if (this.getBetreuungModel() && this.getBetreuungModel().erweiterteBetreuungContainer) {
            return this.getBetreuungModel().erweiterteBetreuungContainer.erweiterteBetreuungJA;
        }
        return undefined;
    }

    public getErweiterteBetreuungGS(): TSErweiterteBetreuung {
        if (this.getBetreuungModel() && this.getBetreuungModel().erweiterteBetreuungContainer) {
            return this.getBetreuungModel().erweiterteBetreuungContainer.erweiterteBetreuungGS;
        }

        return undefined;
    }

    public getBetreuungspensen(): Array<TSBetreuungspensumContainer> {
        if (this.getBetreuungModel()) {
            return this.getBetreuungModel().betreuungspensumContainers;
        }

        return undefined;
    }

    public getBetreuungspensum(index: number): TSBetreuungspensumContainer {
        if (this.getBetreuungspensen() && index >= 0 && index < this.getBetreuungspensen().length) {
            return this.getBetreuungspensen()[index];
        }

        return undefined;
    }

    public createBetreuungspensum(): void {
        if (this.getBetreuungModel()
            && (this.getBetreuungspensen() === undefined || this.getBetreuungspensen() === null)) {
            this.getBetreuungModel().betreuungspensumContainers = [];
        }
        if (!this.getBetreuungModel()) {
            this.errorService.addMesageAsError('Betreuungsmodel ist nicht initialisiert.');
        }
        this.getBetreuungspensen().push(new TSBetreuungspensumContainer(undefined,
            new TSBetreuungspensum(TSPensumUnits.PERCENTAGE, false, undefined, undefined, new TSDateRange())));
    }

    public removeBetreuungspensum(betreuungspensumToDelete: TSBetreuungspensumContainer): void {
        const position = this.getBetreuungspensen().indexOf(betreuungspensumToDelete);
        if (position > -1) {
            this.getBetreuungspensen().splice(position, 1);
        }
    }

    public setSelectedInstitutionStammdaten(): void {
        const instStamList = this.gesuchModelManager.getActiveInstitutionenList();
        const found = instStamList.find(i => i.id === this.instStammId);
        if (found) {
            this.model.institutionStammdaten = found;
        }
    }

    public platzAnfordern(): void {
        if (this.isGesuchValid() && this.getBetreuungModel().vertrag) {
            this.flagErrorVertrag = false;
            if (this.getBetreuungModel().keineKesbPlatzierung) {
                this.save(TSBetreuungsstatus.WARTEN, GESUCH_BETREUUNGEN, {gesuchId: this.getGesuchId()});
            } else {
                this.dvDialog.showRemoveDialog(removeDialogTemplate, undefined, RemoveDialogController, {
                    title: 'KEINE_KESB_PLATZIERUNG_POPUP_TEXT',
                    deleteText: 'Möchten Sie die Betreuung trotzdem speichern?',
                    cancelText: 'LABEL_ABBRECHEN',
                    confirmText: 'LABEL_SPEICHERN',
                })
                    .then(() => {   // User confirmed removal
                        this.save(TSBetreuungsstatus.WARTEN, GESUCH_BETREUUNGEN, {gesuchId: this.getGesuchId()});
                    });
            }
        } else if (!this.getBetreuungModel().vertrag) {
            this.flagErrorVertrag = true;
        }
    }

    public platzBestaetigen(): void {
        if (this.isGesuchValid()) {
            this.getBetreuungModel().datumBestaetigung = DateUtil.today();
            this.save(TSBetreuungsstatus.BESTAETIGT, PENDENZEN_BETREUUNG, undefined);
        }
    }

    /**
     * Wenn ein Betreuungsangebot abgewiesen wird, muss man die neu eingegebenen Betreuungspensen zuruecksetzen, da sie
     * nicht relevant sind. Allerdings muessen der Grund und das Datum der Ablehnung doch gespeichert werden. In diesem
     * Fall machen wir keine Validierung weil die Daten die eingegeben werden muessen, direkt auf dem Server gecheckt
     * werden
     */
    public platzAbweisen(): void {
        // copy values modified by the Institution in initialBetreuung

        this.initialBetreuung.grundAblehnung = this.getBetreuungModel().grundAblehnung;
        // restore initialBetreuung
        this.model = angular.copy(this.initialBetreuung);
        this.model.datumAblehnung = DateUtil.today();
        this.save(TSBetreuungsstatus.ABGEWIESEN, PENDENZEN_BETREUUNG, undefined);
    }

    public stornieren(): void {
        if (!this.isGesuchValid()) {
            return;
        }

        this.getBetreuungModel().datumBestaetigung = DateUtil.today();
        for (let i = 0; i < this.getBetreuungspensen().length; i++) {
            this.getBetreuungspensum(i).betreuungspensumJA.pensum = 0;
            this.getBetreuungspensum(i).betreuungspensumJA.nichtEingetreten = true;
        }

        this.save(TSBetreuungsstatus.STORNIERT, PENDENZEN_BETREUUNG, undefined);
    }

    public saveSchulamt(): void {
        if (this.isGesuchValid()) {
            this.save(TSBetreuungsstatus.SCHULAMT, GESUCH_BETREUUNGEN, {gesuchId: this.getGesuchId()});
        }
    }

    /**
     * Returns true when the user is allowed to edit the content. This happens when the status is AUSSTEHEHND
     * or SCHULAMT and we are not yet in the KorrekturmodusJugendamt
     */
    public isEnabled(): boolean {
        if (this.isDuplicated) {
            return false;
        }
        if (this.getBetreuungModel() && this.getBetreuungModel().betreuungsstatus) {
            return !this.getBetreuungModel().hasVorgaenger()
                && (this.isBetreuungsstatus(TSBetreuungsstatus.AUSSTEHEND)
                    || this.isBetreuungsstatus(TSBetreuungsstatus.SCHULAMT_ANMELDUNG_ERFASST)
                    || (this.isBetreuungsstatus(TSBetreuungsstatus.SCHULAMT)
                        && this.getBetreuungModel().isNew()))
                && !this.isFreigabequittungAusstehend();
        }

        return true;
    }

    public isPensumEditable(): boolean {
        return (this.isBetreuungsstatusWarten() && !this.isSavingData) || this.isMutationsmeldungStatus;
    }

    /**
     * Returns true when the Gesuch must be readonly
     */
    public isGesuchReadonly(): boolean {
        if (!this.getBetreuungModel() || !this.getBetreuungModel().isAngebotSchulamt()) {
            return super.isGesuchReadonly();
        }
        return !this.getBetreuungModel().isEnabled();
    }

    public isBetreuungsstatusWarten(): boolean {
        return this.isBetreuungsstatus(TSBetreuungsstatus.WARTEN);
    }

    public isBetreuungsstatusAbgewiesen(): boolean {
        return this.isBetreuungsstatus(TSBetreuungsstatus.ABGEWIESEN);
    }

    public isBetreuungsstatusBestaetigt(): boolean {
        return this.isBetreuungsstatus(TSBetreuungsstatus.BESTAETIGT);
    }

    public isBetreuungsstatusNichtEingetreten(): boolean {
        return this.isBetreuungsstatus(TSBetreuungsstatus.NICHT_EINGETRETEN);
    }

    public isStorniert(): boolean {
        return this.isBetreuungsstatus(TSBetreuungsstatus.STORNIERT);
    }

    public isBetreuungsstatusAusstehend(): boolean {
        return this.isBetreuungsstatus(TSBetreuungsstatus.AUSSTEHEND);
    }

    public isBetreuungsstatusSchulamt(): boolean {
        return this.isBetreuungsstatus(TSBetreuungsstatus.SCHULAMT);
    }

    public isBetreuungsstatusStorniert(): boolean {
        return this.isBetreuungsstatus(TSBetreuungsstatus.STORNIERT);
    }

    public isBetreuungsstatus(status: TSBetreuungsstatus): boolean {
        if (this.getBetreuungModel()) {
            return this.getBetreuungModel().betreuungsstatus === status;
        }

        return false;
    }

    public isTagesschule(): boolean {
        return this.isBetreuungsangebottyp(TSBetreuungsangebotTyp.TAGESSCHULE);
    }

    public isFerieninsel(): boolean {
        return this.isBetreuungsangebottyp(TSBetreuungsangebotTyp.FERIENINSEL);
    }

    public isTageseltern(): boolean {
        return this.isBetreuungsangebottyp(TSBetreuungsangebotTyp.TAGESFAMILIEN);
    }

    public isSchulamt(): boolean {
        return this.isTagesschule() || this.isFerieninsel();
    }

    private isBetreuungsangebottyp(betAngTyp: TSBetreuungsangebotTyp): boolean {
        if (this.betreuungsangebot) {
            return this.betreuungsangebot.key === TSBetreuungsangebotTyp[betAngTyp];
        }

        return false;
    }

    /**
     * Erweiterte Beduerfnisse wird nur beim Institutionen oder Traegerschaften eingeblendet oder wenn das Feld schon
     * als true gesetzt ist.
     */
    public showErweiterteBeduerfnisse(): boolean {
        return this.authServiceRS.isOneOfRoles(TSRoleUtil.getTraegerschaftInstitutionRoles())
            || this.authServiceRS.isOneOfRoles(TSRoleUtil.getAdministratorJugendamtSchulamtGesuchstellerRoles())
            || this.getBetreuungModel().erweiterteBetreuungContainer.erweiterteBetreuungJA.erweiterteBeduerfnisse;
    }

    public showFalscheAngaben(): boolean {
        return (this.isBetreuungsstatusBestaetigt() || this.isBetreuungsstatusAbgewiesen())
            && !this.isGesuchReadonly() && !this.isFromMutation();
    }

    public showAngabenKorrigieren(): boolean {
        return (
                this.isBetreuungsstatusBestaetigt()
                || this.isBetreuungsstatusAbgewiesen()
                || this.isBetreuungsstatusStorniert()
            )
            && !this.isGesuchReadonly() && this.isFromMutation();
    }

    public isFromMutation(): boolean {
        return this.getBetreuungModel() && !!this.getBetreuungModel().vorgaengerId;
    }

    public showAngabeKorrigieren(): boolean {
        return (this.isBetreuungsstatusBestaetigt() || this.isBetreuungsstatusAbgewiesen())
            && !this.isGesuchReadonly() && this.isFromMutation();
    }

    public mutationsmeldungErstellen(): void {
        // create dummy copy of model
        this.mutationsmeldungModel = angular.copy(this.getBetreuungModel());
        this.isMutationsmeldungStatus = true;
    }

    /**
     * Mutationsmeldungen werden nur Betreuungen erlaubt, die verfuegt sind oder bereits irgendwann
     * verfuegt wurden bzw. ein vorgaengerId haben. Ausserdem muss es sich um das letzte bzw. neueste Gesuch handeln
     */
    public isMutationsmeldungAllowed(): boolean {
        return (
                (
                    this.isMutation()
                    && (
                        this.getBetreuungModel().vorgaengerId
                        || this.getBetreuungModel().betreuungsstatus === TSBetreuungsstatus.VERFUEGT
                    )
                )
                || (
                    !this.isMutation()
                    && isVerfuegtOrSTV(this.gesuchModelManager.getGesuch().status)
                    && this.getBetreuungModel().betreuungsstatus === TSBetreuungsstatus.VERFUEGT
                )
            )
            && this.getBetreuungModel().betreuungsstatus !== TSBetreuungsstatus.WARTEN
            && this.gesuchModelManager.getGesuch().gesuchsperiode.status === TSGesuchsperiodeStatus.AKTIV
            && this.isNewestGesuch
            && !this.gesuchModelManager.getGesuch().gesperrtWegenBeschwerde;
    }

    public mutationsmeldungSenden(): void {
        // send mutationsmeldung (dummy copy)
        if (!(this.isGesuchValid() && this.mutationsmeldungModel)) {
            return;
        }
        this.dvDialog.showRemoveDialog(removeDialogTemplate, this.form, RemoveDialogController, {
            title: 'MUTATIONSMELDUNG_CONFIRMATION',
            deleteText: 'MUTATIONSMELDUNG_BESCHREIBUNG',
            parentController: undefined,
            elementID: undefined,
        }).then(() => {   // User confirmed removal
            this.mitteilungRS.sendbetreuungsmitteilung(this.gesuchModelManager.getDossier(),
                this.mutationsmeldungModel).then(() => {

                this.form.$setUntouched();
                this.form.$setPristine();
                // reset values. is needed??????
                this.isMutationsmeldungStatus = false;
                this.mutationsmeldungModel = undefined;
                this.$state.go(GESUCH_BETREUUNGEN, {gesuchId: this.getGesuchId()});
            });
        });
    }

    /**
     * Prueft dass das Objekt existingMutationsMeldung existiert und dass es ein sentDatum hat. Das wird gebraucht,
     * um zu vermeiden, dass ein leeres Objekt als gueltiges Objekt erkannt wird.
     * Ausserdem muss die Meldung nicht applied sein und nicht den Status ERLEDIGT haben
     */
    public showExistingBetreuungsmitteilungInfoBox(): boolean {
        return this.existingMutationsMeldung !== undefined && this.existingMutationsMeldung !== null
            && this.existingMutationsMeldung.sentDatum !== undefined && this.existingMutationsMeldung.sentDatum !== null
            && !this.existingMutationsMeldung.applied && !this.existingMutationsMeldung.isErledigt();
    }

    public getDatumLastBetreuungsmitteilung(): string {
        if (this.showExistingBetreuungsmitteilungInfoBox()) {
            return DateUtil.momentToLocalDateFormat(this.existingMutationsMeldung.sentDatum, 'DD.MM.YYYY');
        }

        return '';
    }

    public getTimeLastBetreuungsmitteilung(): string {
        if (this.showExistingBetreuungsmitteilungInfoBox()) {
            return DateUtil.momentToLocalDateTimeFormat(this.existingMutationsMeldung.sentDatum, 'HH:mm');
        }

        return '';
    }

    public openExistingBetreuungsmitteilung(): void {
        this.$state.go('gesuch.mitteilung', {
            dossierId: this.gesuchModelManager.getDossier().id,
            gesuchId: this.gesuchModelManager.getGesuch().id,
            betreuungId: this.getBetreuungModel().id,
            mitteilungId: this.existingMutationsMeldung.id,
        });
    }

    /**
     * Sucht die neueste Betreuungsmitteilung fuer die aktuelle Betreuung. Da es nur fuer die Rollen
     * INST und TRAEG relevant ist, wird es nur fuer diese Rollen geholt
     */
    private findExistingBetreuungsmitteilung(): void {
        if (!(!this.getBetreuungModel().isNew()
            && this.authServiceRS.isOneOfRoles(TSRoleUtil.getTraegerschaftInstitutionOnlyRoles()))) {
            return;
        }

        this.mitteilungRS.getNewestBetreuungsmitteilung(this.getBetreuungModel().id)
            .then((response: TSBetreuungsmitteilung) => {
                this.existingMutationsMeldung = response;
            });
    }

    public tageschuleSaveDisabled(): boolean {
        if (this.getBetreuungModel().isNew()) {
            const gp = this.gesuchModelManager.getGesuch().gesuchsperiode;

            return (this.isTagesschule() && gp.hasTagesschulenAnmeldung() && !gp.isTageschulenAnmeldungAktiv()
                || this.isFerieninsel() && !this.getBetreuungModel().isEnabled());
        }

        return true;
    }

    /**
     * Die globale navigation Buttons werden nur angezeigt, wenn es  kein Schulamtangebot ist oder wenn beim
     * Tagesschulangebot die Periode keine Tagesschuleanmeldung definiert hat.
     */
    public displayGlobalNavigationButtons(): boolean {
        return !this.isSchulamt() ||
            (this.isTagesschule() && !this.gesuchModelManager.getGesuch().gesuchsperiode.hasTagesschulenAnmeldung());
    }

    /**
     * Die Felder fuer die Module muessen nur angezeigt werden wenn es Tagesschule ist oder status=SCHULAMT,
     * das letzte um die alten Betreuungen zu unterstuetzen.
     */
    public displayModuleTagesschule(): boolean {
        return this.isTagesschule() && this.gesuchModelManager.getGesuch().gesuchsperiode.hasTagesschulenAnmeldung();
    }

    /**
     * Based on the type of the Angebot it resets the belegungen.
     */
    private cleanBelegungen(): void {
        if (this.betreuungsangebot.key !== TSBetreuungsangebotTyp.FERIENINSEL) {
            this.getBetreuungModel().belegungFerieninsel = undefined;
        }
        if (this.betreuungsangebot.key !== TSBetreuungsangebotTyp.TAGESSCHULE) {
            this.getBetreuungModel().belegungTagesschule = undefined;
        }
    }

    private cleanInstitutionStammdaten(): void {
        if (this.getBetreuungModel()) {
            this.getBetreuungModel().institutionStammdaten = undefined;
        }
    }

    public enableErweiterteBeduerfnisse(): boolean {
        const gesuchsteller = this.authServiceRS.isOneOfRoles(TSRoleUtil.getGesuchstellerOnlyRoles());
        const gemeindeUser = this.authServiceRS
            .isOneOfRoles(TSRoleUtil.getAdministratorJugendamtSchulamtRoles());

        return !this.isSavingData
            && (this.gesuchModelManager.getGesuch() && !isVerfuegtOrSTV(this.gesuchModelManager.getGesuch().status))
            && ((gesuchsteller && this.isBetreuungsstatusAusstehend())
                || gemeindeUser);
    }

    /**
     * Schulamt-Angebote ändern erst beim Einlesen der Freigabequittung den Zustand von SCHULAMT_ANMELDUNG_ERFASST zu
     * SCHULAMT_ANMELDUNG_AUSGELOEST. Betreuungen in Gesuchen im Zustand FREIGABEQUITTUNG dürfen jedoch nicht editiert
     * werden. Deshalb braucht es diese Funktion.
     */
    public isFreigabequittungAusstehend(): boolean {
        if (this.gesuchModelManager.getGesuch() && this.gesuchModelManager.getGesuch().status) {
            return this.gesuchModelManager.getGesuch().status === TSAntragStatus.FREIGABEQUITTUNG;
        }

        return false;
    }

    public keineDetailAnmeldungClicked(): void {
        if (this.getBetreuungModel().keineDetailinformationen) {
            // Fuer Tagesschule setzen wir eine Dummy-Tagesschule als Institution
            this.instStammId = this.CONSTANTS.INSTITUTIONSSTAMMDATENID_DUMMY_TAGESSCHULE;
        } else {
            this.instStammId = undefined;
            this.getBetreuungModel().institutionStammdaten = undefined;
        }
        this.setSelectedInstitutionStammdaten();
    }

    public isFachstelleRequired(): boolean {
        return this.getErweiterteBetreuungJA() && this.getErweiterteBetreuungJA().erweiterteBeduerfnisse;
    }

    public setSelectedFachsstelle(): void {
        const fachstellenList = this.getFachstellenList();
        const found = fachstellenList.find(f => f.id === this.fachstelleId);
        if (found) {
            this.getErweiterteBetreuungJA().fachstelle = found;
        }
    }

    public getFachstellenList(): Array<TSFachstelle> {
        return this.gesuchModelManager.getFachstellenErweiterteBetreuungList();
    }

    public getTextFachstelleKorrekturJA(): string {
        if ((this.getErweiterteBetreuungGS() && this.getErweiterteBetreuungGS().erweiterteBeduerfnisse)
            && (this.getErweiterteBetreuungJA() && !this.getErweiterteBetreuungJA().erweiterteBeduerfnisse)) {
            return this.getErweiterteBetreuungGS().fachstelle.name;
        }
        return this.$translate.instant('LABEL_KEINE_ANGABE');
    }
}
