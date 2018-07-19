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
import * as moment from 'moment';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import {DvDialog} from '../../../core/directive/dv-dialog/dv-dialog';
import ErrorService from '../../../core/errors/service/ErrorService';
import MitteilungRS from '../../../core/service/mitteilungRS.rest';
import {isVerfuegtOrSTV, TSAntragStatus} from '../../../models/enums/TSAntragStatus';
import {getTSBetreuungsangebotTypValues, getTSBetreuungsangebotTypValuesNoTagesschuleanmeldungen, TSBetreuungsangebotTyp} from '../../../models/enums/TSBetreuungsangebotTyp';
import {TSBetreuungsstatus} from '../../../models/enums/TSBetreuungsstatus';
import {TSGesuchsperiodeStatus} from '../../../models/enums/TSGesuchsperiodeStatus';
import {TSWizardStepName} from '../../../models/enums/TSWizardStepName';
import TSBelegungTagesschule from '../../../models/TSBelegungTagesschule';
import TSBetreuung from '../../../models/TSBetreuung';
import TSBetreuungsmitteilung from '../../../models/TSBetreuungsmitteilung';
import TSBetreuungspensum from '../../../models/TSBetreuungspensum';
import TSBetreuungspensumContainer from '../../../models/TSBetreuungspensumContainer';
import TSExceptionReport from '../../../models/TSExceptionReport';
import TSGesuchsperiode from '../../../models/TSGesuchsperiode';
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
import {TSAnmeldungMutationZustand} from '../../../models/enums/TSAnmeldungMutationZustand';
import ILogService = angular.ILogService;
import IScope = angular.IScope;
import ITimeoutService = angular.ITimeoutService;
import ITranslateService = angular.translate.ITranslateService;

declare let require: any;
let template = require('./betreuungView.html');
require('./betreuungView.less');
let removeDialogTemplate = require('../../dialog/removeDialogTemplate.html');

export class BetreuungViewComponentConfig implements IComponentOptions {
    transclude = false;
    template = template;
    controller = BetreuungViewController;
    controllerAs = 'vm';
}

export class BetreuungViewController extends AbstractGesuchViewController<TSBetreuung> {
    betreuungsangebot: any;
    betreuungsangebotValues: Array<any>;
    instStammId: string; //der ausgewaehlte instStammId wird hier gespeichert und dann in die entsprechende InstitutionStammdaten umgewandert
    isSavingData: boolean; // Semaphore
    initialBetreuung: TSBetreuung;
    flagErrorVertrag: boolean;
    kindModel: TSKindContainer;
    betreuungIndex: number;
    isMutationsmeldungStatus: boolean;
    mutationsmeldungModel: TSBetreuung;
    existingMutationsMeldung: TSBetreuungsmitteilung;
    isNewestGesuch: boolean;
    dvDialog: DvDialog;
    $translate: ITranslateService;
    moduleBackup: TSModulTagesschule[] = undefined;
    aktuellGueltig: boolean = true;
    isDuplicated: boolean = false;

    static $inject = ['$state', 'GesuchModelManager', 'EbeguUtil', 'CONSTANTS', '$scope', 'BerechnungsManager', 'ErrorService',
        'AuthServiceRS', 'WizardStepManager', '$stateParams', 'MitteilungRS', 'DvDialog', '$log', '$timeout', '$translate'];

    /* @ngInject */
    constructor(private $state: StateService, gesuchModelManager: GesuchModelManager, private ebeguUtil: EbeguUtil, private CONSTANTS: any,
                $scope: IScope, berechnungsManager: BerechnungsManager, private errorService: ErrorService,
                private authServiceRS: AuthServiceRS, wizardStepManager: WizardStepManager, private $stateParams: IBetreuungStateParams,
                private mitteilungRS: MitteilungRS, dvDialog: DvDialog, private $log: ILogService,
                $timeout: ITimeoutService, $translate: ITranslateService) {
        super(gesuchModelManager, berechnungsManager, wizardStepManager, $scope, TSWizardStepName.BETREUUNG, $timeout);
        this.dvDialog = dvDialog;
        this.$translate = $translate;
    }

    $onInit() {
        this.mutationsmeldungModel = undefined;
        this.isMutationsmeldungStatus = false;
        let kindIndex: number = this.gesuchModelManager.convertKindNumberToKindIndex(parseInt(this.$stateParams.kindNumber, 10));
        if (kindIndex >= 0) {
            this.gesuchModelManager.setKindIndex(kindIndex);
            if (this.$stateParams.betreuungNumber && this.$stateParams.betreuungNumber.length > 0) {
                this.betreuungIndex = this.gesuchModelManager.convertBetreuungNumberToBetreuungIndex(parseInt(this.$stateParams.betreuungNumber));
                this.model = angular.copy(this.gesuchModelManager.getKindToWorkWith().betreuungen[this.betreuungIndex]);
                this.initialBetreuung = angular.copy(this.gesuchModelManager.getKindToWorkWith().betreuungen[this.betreuungIndex]);
                this.gesuchModelManager.setBetreuungIndex(this.betreuungIndex);
            } else {
                //wenn betreuung-nummer nicht definiert ist heisst dass, das wir ein neues erstellen sollten
                this.model = this.initEmptyBetreuung();
                this.initialBetreuung = angular.copy(this.model);
                this.betreuungIndex = this.gesuchModelManager.getKindToWorkWith().betreuungen ? this.gesuchModelManager.getKindToWorkWith().betreuungen.length : 0;
                this.gesuchModelManager.setBetreuungIndex(this.betreuungIndex);
            }

            this.setBetreuungsangebotTypValues();
            // Falls ein Typ gesetzt ist, handelt es sich um eine direkt-Anmeldung
            if (this.$stateParams.betreuungsangebotTyp) {
                for (let obj of this.betreuungsangebotValues) {
                    if (obj.key === this.$stateParams.betreuungsangebotTyp) {
                        this.betreuungsangebot = obj;
                        this.changedAngebot();
                    }
                }
            } else {
                this.betreuungsangebot = undefined;
            }
            this.initViewModel();

            // just to read!
            this.kindModel = this.gesuchModelManager.getKindToWorkWith();
        } else {
            this.$log.error('There is no kind available with kind-number:' + this.$stateParams.kindNumber);
        }
        this.isNewestGesuch = this.gesuchModelManager.isNeuestesGesuch();

        this.findExistingBetreuungsmitteilung();

        if (this.getBetreuungModel().anmeldungMutationZustand) {
            if (this.getBetreuungModel().anmeldungMutationZustand === TSAnmeldungMutationZustand.MUTIERT) {
                this.aktuellGueltig = false;
            } else if (this.getBetreuungModel().anmeldungMutationZustand === TSAnmeldungMutationZustand.NOCH_NICHT_FREIGEGEBEN) {
                this.aktuellGueltig = false;
            }
        }
    }

    /**
     * Creates a Betreuung for the kind given by the kindNumber attribute of the class.
     * Thus the kindnumber must be set before this method is called.
     */
    public initEmptyBetreuung(): TSBetreuung {
        let tsBetreuung: TSBetreuung = new TSBetreuung();
        tsBetreuung.betreuungsstatus = TSBetreuungsstatus.AUSSTEHEND;
        return tsBetreuung;
    }

    private initViewModel() {
        this.isSavingData = false;
        this.flagErrorVertrag = false;
        if (this.getInstitutionSD()) {
            this.instStammId = this.getInstitutionSD().id;
            this.betreuungsangebot = this.getBetreuungsangebotFromInstitutionList();
        }
        this.startEmptyListOfBetreuungspensen();
        //institutionen lazy laden
        if (!this.gesuchModelManager.getActiveInstitutionenList() || this.gesuchModelManager.getActiveInstitutionenList().length <= 0) {
            this.gesuchModelManager.updateActiveInstitutionenList();
        }
    }

    /**
     * Fuer Institutionen und Traegerschaften wird es geprueft ob es schon ein Betreuungspensum existiert,
     * wenn nicht wir die Liste dann mit einem leeren initiallisiert
     */
    private startEmptyListOfBetreuungspensen() {
        if ((!this.getBetreuungspensen() || this.getBetreuungspensen().length === 0)
            && (this.authServiceRS.isOneOfRoles(TSRoleUtil.getTraegerschaftInstitutionOnlyRoles()))) {
            // nur fuer Institutionen wird ein Betreuungspensum by default erstellt
            this.createBetreuungspensum();
        }
    }

    public getGesuchsperiodeBegin(): moment.Moment {
        return this.gesuchModelManager.getGesuchsperiodeBegin();
    }

    private getBetreuungsangebotFromInstitutionList() {
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

    public changedAngebot() {
        if (this.getBetreuungModel()) {
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
    }

    public setErsterSchultag(): void {
        // Default Eintrittsdatum ist erster Schultag, wenn noch in Zukunft
        let ersterSchultag: moment.Moment = this.gesuchModelManager.getGesuchsperiode().datumErsterSchultag;
        if (ersterSchultag && !this.getBetreuungModel().keineDetailinformationen && DateUtil.today().isBefore(ersterSchultag)) {
            this.getBetreuungModel().belegungTagesschule.eintrittsdatum = ersterSchultag;
        }
    }

    private save(newStatus: TSBetreuungsstatus, nextStep: string, params: any): void {
        this.isSavingData = true;
        let oldStatus: TSBetreuungsstatus = this.model.betreuungsstatus;
        if (this.getBetreuungModel()) {
            if (this.isSchulamt()) {
                this.getBetreuungModel().betreuungspensumContainers = []; // fuer Tagesschule werden keine Betreuungspensum benoetigt, deswegen löschen wir sie vor dem Speichern
                if (this.isTagesschule()) {
                    this.filterOnlyAngemeldeteModule();
                }
            }
        }
        this.errorService.clearAll();
        this.gesuchModelManager.saveBetreuung(this.model, newStatus, false).then((betreuungResponse: any) => {
            this.gesuchModelManager.setBetreuungToWorkWith(this.model); //setze model
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
     * Entfernt alle Module die nicht als angemeldet markiert sind. Nur fuer Gesuchsperiode in denen die Tagesschuleanmeldung
     * aktiv ist.
     */
    public filterOnlyAngemeldeteModule(): void {
        if (this.gesuchModelManager.getGesuchsperiode().hasTagesschulenAnmeldung() &&
            this.getBetreuungModel().belegungTagesschule && this.getBetreuungModel().belegungTagesschule.moduleTagesschule) {
            // noinspection UnnecessaryLocalVariableJS
            if (this.moduleBackup === undefined && this.isBetreuungsstatus(TSBetreuungsstatus.SCHULAMT_FALSCHE_INSTITUTION)) {
                this.moduleBackup = this.getBetreuungModel().belegungTagesschule.moduleTagesschule
                    .filter(modul => modul.angemeldet === true);
                this.getBetreuungModel().belegungTagesschule.moduleTagesschule = this.moduleBackup;
            } else if (this.moduleBackup !== undefined && this.isBetreuungsstatus(TSBetreuungsstatus.SCHULAMT_FALSCHE_INSTITUTION)) {
                this.getBetreuungModel().belegungTagesschule.moduleTagesschule = this.moduleBackup;
            } else {
                this.getBetreuungModel().belegungTagesschule.moduleTagesschule = this.getBetreuungModel().belegungTagesschule.moduleTagesschule
                    .filter(modul => modul.angemeldet === true);
            }
        }
    }

    /**
     * Kopiert alle Module der ausgewaehlten Tagesschule in die Belegung, sodass man direkt in die Belegung die Module auswaehlen kann.
     */
    public copyModuleToBelegung() {
        if (this.getBetreuungModel().institutionStammdaten && this.getBetreuungModel().institutionStammdaten.institutionStammdatenTagesschule
            && this.getBetreuungModel().institutionStammdaten.institutionStammdatenTagesschule.moduleTagesschule) {

            let angemeldeteModule: TSModulTagesschule[] = angular.copy(this.getBetreuungModel().belegungTagesschule.moduleTagesschule);
            this.getBetreuungModel().belegungTagesschule.moduleTagesschule = angular.copy(this.getBetreuungModel().institutionStammdaten.institutionStammdatenTagesschule.moduleTagesschule);
            if (angemeldeteModule) {
                angemeldeteModule.forEach(angemeldetesModul => {
                    this.getBetreuungModel().belegungTagesschule.moduleTagesschule.forEach(instModul => {
                        if (angemeldetesModul.isSameModul(instModul)) {
                            instModul.angemeldet = true;
                        }
                    });
                });
            }
        }
    }

    public anmeldenSchulamt(): void {
        if (this.direktAnmeldenSchulamt()) {
            this.save(TSBetreuungsstatus.SCHULAMT_ANMELDUNG_AUSGELOEST, 'gesuch.betreuungen', {gesuchId: this.getGesuchId()});
        } else {
            this.save(TSBetreuungsstatus.SCHULAMT_ANMELDUNG_ERFASST, 'gesuch.betreuungen', {gesuchId: this.getGesuchId()});
        }
    }

    public direktAnmeldenSchulamt(): boolean {
        // Eigentlich immer ausser in Bearbeitung GS
        return !(this.isGesuchInStatus(TSAntragStatus.IN_BEARBEITUNG_GS) || this.isGesuchInStatus(TSAntragStatus.FREIGABEQUITTUNG));
    }

    public enableBetreuungsangebotsTyp(): boolean {
        return this.model.isNew() && !this.gesuchModelManager.isGesuchReadonly() && !this.gesuchModelManager.isKorrekturModusJugendamt();
    }

    public showInstitutionenList(): boolean {
        return (this.isTageschulenAnmeldungAktiv() && (this.isEnabled() || this.isBetreuungsstatus(TSBetreuungsstatus.SCHULAMT_FALSCHE_INSTITUTION))
            || !this.isTageschulenAnmeldungAktiv() && (this.isEnabled() && !this.isTagesschule()))
            && !this.getBetreuungModel().keineDetailinformationen;
    }

    public showInstitutionenAsText(): boolean {
        return ((this.isTageschulenAnmeldungAktiv() && !this.isEnabled() && !this.isBetreuungsstatus(TSBetreuungsstatus.SCHULAMT_FALSCHE_INSTITUTION))
            || (!this.isTageschulenAnmeldungAktiv() && !this.isEnabled() && !this.isTagesschule()))
            && !this.getBetreuungModel().keineDetailinformationen;
    }

    public isTageschulenAnmeldungAktiv() {
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
            parentController: undefined,
            elementID: undefined
        }).then(() => {
            if (this.authServiceRS.isOneOfRoles(TSRoleUtil.getTraegerschaftInstitutionOnlyRoles())) {
                this.save(TSBetreuungsstatus.SCHULAMT_ANMELDUNG_UEBERNOMMEN, 'pendenzenBetreuungen', undefined);
            } else {
                this.save(TSBetreuungsstatus.SCHULAMT_ANMELDUNG_UEBERNOMMEN, 'gesuch.betreuungen', {gesuchId: this.getGesuchId()});
            }
        });
    }

    public anmeldungSchulamtAblehnen(): void {
        if (this.authServiceRS.isOneOfRoles(TSRoleUtil.getTraegerschaftInstitutionOnlyRoles())) {
            this.save(TSBetreuungsstatus.SCHULAMT_ANMELDUNG_ABGELEHNT, 'pendenzenBetreuungen', undefined);
        } else {
            this.save(TSBetreuungsstatus.SCHULAMT_ANMELDUNG_ABGELEHNT, 'gesuch.betreuungen', {gesuchId: this.getGesuchId()});
        }
    }

    public anmeldungSchulamtFalscheInstitution(): void {
        if (this.authServiceRS.isOneOfRoles(TSRoleUtil.getTraegerschaftInstitutionOnlyRoles())) {
            this.save(TSBetreuungsstatus.SCHULAMT_FALSCHE_INSTITUTION, 'pendenzenBetreuungen', undefined);
        } else {
            this.save(TSBetreuungsstatus.SCHULAMT_FALSCHE_INSTITUTION, 'gesuch.betreuungen', {gesuchId: this.getGesuchId()});
        }
    }

    private copyBGNumberLToClipboard(): void {
        let bgNumber: string = this.ebeguUtil.calculateBetreuungsIdFromBetreuung(this.gesuchModelManager.getFall(),
            this.gesuchModelManager.getDossier().gemeinde, this.getBetreuungModel());
        let $temp = $('<input>');
        $('body').append($temp);
        $temp.val(bgNumber).select();
        document.execCommand('copy');
        $temp.remove();
    }

    private setBetreuungsangebotTypValues(): void {
        let betreuungsangebotTypValues = [];
        if (this.gesuchModelManager.getGesuchsperiode() && this.gesuchModelManager.getGesuchsperiode().hasTagesschulenAnmeldung()) {
            betreuungsangebotTypValues = getTSBetreuungsangebotTypValues();
        } else {
            betreuungsangebotTypValues = getTSBetreuungsangebotTypValuesNoTagesschuleanmeldungen();
        }
        this.betreuungsangebotValues = this.ebeguUtil.translateStringList(betreuungsangebotTypValues);
    }

    public cancel() {
        this.reset();
        this.form.$setPristine();
        this.$state.go('gesuch.betreuungen', {gesuchId: this.getGesuchId()});
    }

    reset() {
        this.removeBetreuungFromKind(); //wenn model existiert und nicht neu ist wegnehmen, sonst resetten
    }

    private removeBetreuungFromKind(): void {
        if (this.model && !this.model.timestampErstellt) {
            //wenn die Betreeung noch nicht erstellt wurde, loeschen wir die Betreuung vom Array
            this.gesuchModelManager.removeBetreuungFromKind();
        }
    }

    public getInstitutionenSDList(): Array<TSInstitutionStammdaten> {
        let result: Array<TSInstitutionStammdaten> = [];
        if (this.betreuungsangebot) {
            this.gesuchModelManager.getActiveInstitutionenList().forEach((instStamm: TSInstitutionStammdaten) => {
                if (instStamm.betreuungsangebotTyp === this.betreuungsangebot.key && this.gesuchModelManager.isDefaultTagesschuleAllowed(instStamm)) {
                    result.push(instStamm);
                }
            });
        }
        return result;
    }

    public getInstitutionSD(): TSInstitutionStammdaten {
        if (this.getBetreuungModel()) {
            return this.getBetreuungModel().institutionStammdaten;
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
        if (this.getBetreuungModel() && (this.getBetreuungspensen() === undefined || this.getBetreuungspensen() === null)) {
            this.getBetreuungModel().betreuungspensumContainers = [];
        }
        if (!this.getBetreuungModel()) {
            this.errorService.addMesageAsError('Betreuungsmodel ist nicht initialisiert.');
        }
        this.getBetreuungspensen().push(new TSBetreuungspensumContainer(undefined, new TSBetreuungspensum(false, undefined, new TSDateRange())));
    }

    public removeBetreuungspensum(betreuungspensumToDelete: TSBetreuungspensumContainer): void {
        let position: number = this.getBetreuungspensen().indexOf(betreuungspensumToDelete);
        if (position > -1) {
            this.getBetreuungspensen().splice(position, 1);
        }
    }

    public setSelectedInstitutionStammdaten(): void {
        let instStamList = this.gesuchModelManager.getActiveInstitutionenList();
        for (let i: number = 0; i < instStamList.length; i++) {
            if (instStamList[i].id === this.instStammId) {
                this.model.institutionStammdaten = instStamList[i];
            }
        }
    }

    public platzAnfordern(): void {
        if (this.isGesuchValid() && this.getBetreuungModel().vertrag === true) {
            this.flagErrorVertrag = false;
            this.save(TSBetreuungsstatus.WARTEN, 'gesuch.betreuungen', {gesuchId: this.getGesuchId()});
        } else if (this.getBetreuungModel().vertrag !== true) {
            this.flagErrorVertrag = true;
        }
    }

    public platzBestaetigen(): void {
        if (this.isGesuchValid()) {
            this.getBetreuungModel().datumBestaetigung = DateUtil.today();
            this.save(TSBetreuungsstatus.BESTAETIGT, 'pendenzenBetreuungen', undefined);
        }
    }

    /**
     * Wenn ein Betreuungsangebot abgewiesen wird, muss man die neu eingegebenen Betreuungspensen zuruecksetzen, da sie nicht relevant sind.
     * Allerdings muessen der Grund und das Datum der Ablehnung doch gespeichert werden.
     * In diesem Fall machen wir keine Validierung weil die Daten die eingegeben werden muessen, direkt auf dem Server gecheckt werden
     */
    public platzAbweisen(): void {
        //copy values modified by the Institution in initialBetreuung
        this.initialBetreuung.erweiterteBeduerfnisse = this.getBetreuungModel().erweiterteBeduerfnisse;
        this.initialBetreuung.grundAblehnung = this.getBetreuungModel().grundAblehnung;
        //restore initialBetreuung
        this.model = angular.copy(this.initialBetreuung);
        this.model.datumAblehnung = DateUtil.today();
        this.save(TSBetreuungsstatus.ABGEWIESEN, 'pendenzenBetreuungen', undefined);
    }

    public stornieren(): void {
        if (this.isGesuchValid()) {
            this.getBetreuungModel().datumBestaetigung = DateUtil.today();

            for (let i: number = 0; i < this.getBetreuungspensen().length; i++) {
                this.getBetreuungspensum(i).betreuungspensumJA.pensum = 0;
                this.getBetreuungspensum(i).betreuungspensumJA.nichtEingetreten = true;
            }
            this.getBetreuungModel().erweiterteBeduerfnisse = false;

            this.save(TSBetreuungsstatus.STORNIERT, 'pendenzenBetreuungen', undefined);
        }
    }

    public saveSchulamt(): void {
        if (this.isGesuchValid()) {
            this.save(TSBetreuungsstatus.SCHULAMT, 'gesuch.betreuungen', {gesuchId: this.getGesuchId()});
        }
    }

    /**
     * Returns true when the user is allowed to edit the content. This happens when the status is AUSSTEHEHND
     * or SCHULAMT and we are not yet in the KorrekturmodusJugendamt
     * @returns {boolean}
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

    /**
     * Returns true when the Gesuch must be readonly
     * @returns {boolean}
     */
    public isGesuchReadonly(): boolean {
        if (!this.getBetreuungModel().isAngebotSchulamt()) {
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
     * Erweiterte Beduerfnisse wird nur beim Institutionen oder Traegerschaften eingeblendet oder wenn das Feld schon als true gesetzt ist
     * ACHTUNG: Hier benutzen wir die Direktive dv-show-element nicht, da es unterschiedliche Bedingungen für jede Rolle gibt.
     * @returns {boolean}
     */
    public showErweiterteBeduerfnisse(): boolean {
        return this.authServiceRS.isOneOfRoles(TSRoleUtil.getTraegerschaftInstitutionRoles())
            || this.getBetreuungModel().erweiterteBeduerfnisse === true;
    }

    public showFalscheAngaben(): boolean {
        return (this.isBetreuungsstatusBestaetigt() || this.isBetreuungsstatusAbgewiesen())
            && !this.isGesuchReadonly() && !this.isFromMutation();
    }

    public showAngabenKorrigieren(): boolean {
        return (this.isBetreuungsstatusBestaetigt() || this.isBetreuungsstatusAbgewiesen() || this.isBetreuungsstatusStorniert())
            && !this.isGesuchReadonly() && this.isFromMutation();
    }

    public isFromMutation(): boolean {
        if (this.getBetreuungModel()) {
            if (this.getBetreuungModel().vorgaengerId) {
                return true;
            }
        }
        return false;
    }

    public showAngabeKorrigieren(): boolean {
        return (this.isBetreuungsstatusBestaetigt() || this.isBetreuungsstatusAbgewiesen())
            && !this.isGesuchReadonly() && this.isFromMutation();
    }

    public mutationsmeldungErstellen(): void {
        //create dummy copy of model
        this.mutationsmeldungModel = angular.copy(this.getBetreuungModel());
        this.isMutationsmeldungStatus = true;
    }

    /**
     * Mutationsmeldungen werden nur Betreuungen erlaubt, die verfuegt sind oder bereits irgendwann
     * verfuegt wurden bzw. ein vorgaengerId haben. Ausserdem muss es sich um das letzte bzw. neueste Gesuch handeln
     */
    public isMutationsmeldungAllowed(): boolean {
        return ((this.isMutation() && (this.getBetreuungModel().vorgaengerId || this.getBetreuungModel().betreuungsstatus === TSBetreuungsstatus.VERFUEGT))
            || (!this.isMutation() && isVerfuegtOrSTV(this.gesuchModelManager.getGesuch().status) && this.getBetreuungModel().betreuungsstatus === TSBetreuungsstatus.VERFUEGT))
            && this.getBetreuungModel().betreuungsstatus !== TSBetreuungsstatus.WARTEN
            && this.gesuchModelManager.getGesuch().gesuchsperiode.status === TSGesuchsperiodeStatus.AKTIV
            && this.isNewestGesuch
            && this.gesuchModelManager.getGesuch().gesperrtWegenBeschwerde !== true;
    }

    public mutationsmeldungSenden(): void {
        // send mutationsmeldung (dummy copy)
        if (this.isGesuchValid() && this.mutationsmeldungModel) {
            this.dvDialog.showRemoveDialog(removeDialogTemplate, this.form, RemoveDialogController, {
                title: 'MUTATIONSMELDUNG_CONFIRMATION',
                deleteText: 'MUTATIONSMELDUNG_BESCHREIBUNG',
                parentController: undefined,
                elementID: undefined
            }).then(() => {   //User confirmed removal
                this.mitteilungRS.sendbetreuungsmitteilung(this.gesuchModelManager.getDossier(),
                    this.mutationsmeldungModel).then((response) => {

                    this.form.$setUntouched();
                    this.form.$setPristine();
                    // reset values. is needed??????
                    this.isMutationsmeldungStatus = false;
                    this.mutationsmeldungModel = undefined;
                    this.$state.go('gesuch.betreuungen', {gesuchId: this.getGesuchId()});
                });
            });
        }
    }

    /**
     * Prueft dass das Objekt existingMutationsMeldung existiert und dass es ein sentDatum hat. Das wird gebraucht,
     * um zu vermeiden, dass ein leeres Objekt als gueltiges Objekt erkannt wird.
     * Ausserdem muss die Meldung nicht applied sein und nicht den Status ERLEDIGT haben
     */
    public showExistingBetreuungsmitteilungInfoBox(): boolean {
        return this.existingMutationsMeldung !== undefined && this.existingMutationsMeldung !== null
            && this.existingMutationsMeldung.sentDatum !== undefined && this.existingMutationsMeldung.sentDatum !== null
            && this.existingMutationsMeldung.applied !== true && !this.existingMutationsMeldung.isErledigt();
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
            mitteilungId: this.existingMutationsMeldung.id
        });
    }

    /**
     * Sucht die neueste Betreuungsmitteilung fuer die aktuelle Betreuung. Da es nur fuer die Rollen
     * INST und TRAEG relevant ist, wird es nur fuer diese Rollen geholt
     */
    private findExistingBetreuungsmitteilung() {
        if (!this.getBetreuungModel().isNew() && this.authServiceRS.isOneOfRoles(TSRoleUtil.getTraegerschaftInstitutionOnlyRoles())) {
            this.mitteilungRS.getNewestBetreuungsmitteilung(this.getBetreuungModel().id).then((response: TSBetreuungsmitteilung) => {
                this.existingMutationsMeldung = response;
            });
        }
    }

    public tageschuleSaveDisabled(): boolean {
        if (this.getBetreuungModel().isNew()) {
            let gp: TSGesuchsperiode = this.gesuchModelManager.getGesuch().gesuchsperiode;
            return (this.isTagesschule() && gp.hasTagesschulenAnmeldung() && !gp.isTageschulenAnmeldungAktiv()
                || this.isFerieninsel() && !this.getBetreuungModel().isEnabled());
        }
        return true;
    }

    /**
     * Die globale navigation Buttons werden nur angezeigt, wenn es  kein Schulamtangebot ist oder wenn beim Tagesschulangebot
     * die Periode keine Tagesschuleanmeldung definiert hat.
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
        return (this.isBetreuungsstatusWarten() && !this.isSavingData) || this.isMutationsmeldungStatus;
    }

    /**
     * Schulamt-Angebote ändern erst beim Einlesen der Freigabequittung den Zustand von SCHULAMT_ANMELDUNG_ERFASST zu
     * SCHULAMT_ANMELDUNG_AUSGELOEST. Betreuungen in Gesuchen im Zustand FREIGABEQUITTUNG dürfen jedoch nicht editiert werden.
     * Deshalb braucht es diese Funktion.
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
}
