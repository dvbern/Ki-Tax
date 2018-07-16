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

import {AuthLifeCycleService} from '../../authentication/service/authLifeCycle.service';
import {TSCacheTyp} from '../../models/enums/TSCacheTyp';
import TSDossier from '../../models/TSDossier';
import TSFall from '../../models/TSFall';
import TSGesuch from '../../models/TSGesuch';
import TSGesuchsteller from '../../models/TSGesuchsteller';
import TSAdresse from '../../models/TSAdresse';
import {TSAdressetyp} from '../../models/enums/TSAdressetyp';
import TSFamiliensituation from '../../models/TSFamiliensituation';
import DossierRS from './dossierRS.rest';
import FallRS from './fallRS.rest';
import GemeindeRS from './gemeindeRS.rest';
import GesuchRS from './gesuchRS.rest';
import GesuchstellerRS from '../../core/service/gesuchstellerRS.rest';
import FamiliensituationRS from './familiensituationRS.rest';
import {ILogService, IPromise, IQService} from 'angular';
import EbeguRestUtil from '../../utils/EbeguRestUtil';
import TSFinanzielleSituationContainer from '../../models/TSFinanzielleSituationContainer';
import TSEinkommensverschlechterungContainer from '../../models/TSEinkommensverschlechterungContainer';
import FinanzielleSituationRS from './finanzielleSituationRS.rest';
import EinkommensverschlechterungContainerRS from './einkommensverschlechterungContainerRS.rest';
import TSKindContainer from '../../models/TSKindContainer';
import KindRS from '../../core/service/kindRS.rest';
import {TSFachstelle} from '../../models/TSFachstelle';
import {FachstelleRS} from '../../core/service/fachstelleRS.rest';
import TSErwerbspensumContainer from '../../models/TSErwerbspensumContainer';
import ErwerbspensumRS from '../../core/service/erwerbspensumRS.rest';
import TSBetreuung from '../../models/TSBetreuung';
import TSInstitutionStammdaten from '../../models/TSInstitutionStammdaten';
import {InstitutionStammdatenRS} from '../../core/service/institutionStammdatenRS.rest';
import BetreuungRS from '../../core/service/betreuungRS.rest';
import {TSBetreuungsstatus} from '../../models/enums/TSBetreuungsstatus';
import TSGesuchsperiode from '../../models/TSGesuchsperiode';
import GesuchsperiodeRS from '../../core/service/gesuchsperiodeRS.rest';
import AuthServiceRS from '../../authentication/service/AuthServiceRS.rest';
import TSUser from '../../models/TSUser';
import VerfuegungRS from '../../core/service/verfuegungRS.rest';
import TSVerfuegung from '../../models/TSVerfuegung';
import GlobalCacheService from './globalCacheService';
import WizardStepManager from './wizardStepManager';
import EinkommensverschlechterungInfoRS from './einkommensverschlechterungInfoRS.rest';
import {
    isAnyStatusOfVerfuegt,
    isAtLeastFreigegeben,
    isAtLeastFreigegebenOrFreigabequittung,
    isStatusVerfuegenVerfuegt,
    TSAntragStatus
} from '../../models/enums/TSAntragStatus';
import AntragStatusHistoryRS from '../../core/service/antragStatusHistoryRS.rest';
import {TSWizardStepName} from '../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../models/enums/TSWizardStepStatus';
import {TSAntragTyp} from '../../models/enums/TSAntragTyp';
import EbeguUtil from '../../utils/EbeguUtil';
import ErrorService from '../../core/errors/service/ErrorService';
import TSExceptionReport from '../../models/TSExceptionReport';
import {TSErrorType} from '../../models/enums/TSErrorType';
import {TSErrorLevel} from '../../models/enums/TSErrorLevel';
import AdresseRS from '../../core/service/adresseRS.rest';
import {TSRole} from '../../models/enums/TSRole';
import {TSRoleUtil} from '../../utils/TSRoleUtil';
import {isSchulamt} from '../../models/enums/TSBetreuungsangebotTyp';
import {TSEingangsart} from '../../models/enums/TSEingangsart';
import TSEinkommensverschlechterungInfoContainer from '../../models/TSEinkommensverschlechterungInfoContainer';
import TSFamiliensituationContainer from '../../models/TSFamiliensituationContainer';
import TSGesuchstellerContainer from '../../models/TSGesuchstellerContainer';
import TSAdresseContainer from '../../models/TSAdresseContainer';
import {TSAuthEvent} from '../../models/enums/TSAuthEvent';
import * as moment from 'moment';
import TSEWKResultat from '../../models/TSEWKResultat';
import TSEWKPerson from '../../models/TSEWKPerson';
import {TSGesuchsperiodeStatus} from '../../models/enums/TSGesuchsperiodeStatus';
import EwkRS from '../../core/service/ewkRS.rest';

export default class GesuchModelManager {
    private gesuch: TSGesuch;
    private neustesGesuch: boolean;
    gesuchstellerNumber: number = 1;
    basisJahrPlusNumber: number = 1;
    private kindIndex: number;
    private betreuungIndex: number;
    private fachstellenList: Array<TSFachstelle>;
    private activInstitutionenList: Array<TSInstitutionStammdaten>;

    ewkResultatGS1: TSEWKResultat;
    ewkResultatGS2: TSEWKResultat;
    ewkPersonGS1: TSEWKPerson;
    ewkPersonGS2: TSEWKPerson;

    static $inject = ['FamiliensituationRS', 'FallRS', 'GesuchRS', 'GesuchstellerRS', 'FinanzielleSituationRS', 'KindRS', 'FachstelleRS',
        'ErwerbspensumRS', 'InstitutionStammdatenRS', 'BetreuungRS', 'GesuchsperiodeRS', 'EbeguRestUtil', '$log', 'AuthServiceRS',
        'EinkommensverschlechterungContainerRS', 'VerfuegungRS', 'WizardStepManager', 'EinkommensverschlechterungInfoRS',
        'AntragStatusHistoryRS', 'EbeguUtil', 'ErrorService', 'AdresseRS', '$q', 'CONSTANTS', 'AuthLifeCycleService', 'EwkRS', 'GlobalCacheService', 'DossierRS'];
    /* @ngInject */
    constructor(private familiensituationRS: FamiliensituationRS, private fallRS: FallRS, private gesuchRS: GesuchRS, private gesuchstellerRS: GesuchstellerRS,
                private finanzielleSituationRS: FinanzielleSituationRS, private kindRS: KindRS, private fachstelleRS: FachstelleRS, private erwerbspensumRS: ErwerbspensumRS,
                private instStamRS: InstitutionStammdatenRS, private betreuungRS: BetreuungRS, private gesuchsperiodeRS: GesuchsperiodeRS,
                private ebeguRestUtil: EbeguRestUtil, private log: ILogService, private authServiceRS: AuthServiceRS,
                private einkommensverschlechterungContainerRS: EinkommensverschlechterungContainerRS, private verfuegungRS: VerfuegungRS,
                private wizardStepManager: WizardStepManager, private einkommensverschlechterungInfoRS: EinkommensverschlechterungInfoRS,
                private antragStatusHistoryRS: AntragStatusHistoryRS, private ebeguUtil: EbeguUtil, private errorService: ErrorService,
                private adresseRS: AdresseRS, private $q: IQService, private CONSTANTS: any, private authLifeCycleService: AuthLifeCycleService, private ewkRS: EwkRS,
                private globalCacheService: GlobalCacheService, private dossierRS: DossierRS, private gemeindeRS: GemeindeRS) {

        this.authLifeCycleService.get$(TSAuthEvent.LOGOUT_SUCCESS)
            .subscribe(value => {
                    this.setGesuch(undefined);
                    this.log.debug('Cleared gesuch on logout');
                },
            );
    }

    /**
     * Je nach dem welche Rolle der Benutzer hat, wird das Gesuch aus der DB anders geholt.
     * Fuer Institutionen z.B. wird das Gesuch nur mit den relevanten Daten geholt
     */
    public openGesuch(gesuchId: string): IPromise<TSGesuch> {
        if (this.authServiceRS.isOneOfRoles(TSRoleUtil.getTraegerschaftInstitutionOnlyRoles())) { // Superadmin muss als "normale" Benutzer betrachtet werden
            return this.gesuchRS.findGesuchForInstitution(gesuchId)
                .then((response: TSGesuch) => {
                    return this.wizardStepManager.findStepsFromGesuch(gesuchId).then(bla => {
                        if (response) {
                            this.setGesuch(response);
                        }
                        return response;
                    });
                });
        } else {
            return this.gesuchRS.findGesuch(gesuchId)
                .then((response: TSGesuch) => {
                    return this.wizardStepManager.findStepsFromGesuch(gesuchId).then(bla => {
                        if (response) {
                            this.setGesuch(response);
                        }
                        return response;
                    });
                });
        }
    }

    /**
     * Mit den Daten vom Gesuch, werden die entsprechenden Steps der Liste hiddenSteps hinzugefuegt.
     * Oder ggf. aus der Liste entfernt (nur public fuer test)
     */
    public setHiddenSteps(): void {
        if (this.gesuch) {
            //Freigabe
            if (this.gesuch.isOnlineGesuch()) {
                this.wizardStepManager.unhideStep(TSWizardStepName.FREIGABE);
            } else {
                this.wizardStepManager.hideStep(TSWizardStepName.FREIGABE);
            }

            //Abwesenheit
            if (!this.gesuch.isMutation()) {
                this.wizardStepManager.hideStep(TSWizardStepName.ABWESENHEIT);
            } else {
                this.wizardStepManager.unhideStep(TSWizardStepName.ABWESENHEIT);
            }

            //Umzug
            if (!this.gesuch.isMutation() && !this.getGesuch().isThereAnyUmzug()) {
                this.wizardStepManager.hideStep(TSWizardStepName.UMZUG);
            } else {
                this.wizardStepManager.unhideStep(TSWizardStepName.UMZUG);
            }
        }
    }

    /**
     * In dieser Methode wird das Gesuch ersetzt. Das Gesuch ist jetzt private und darf nur ueber diese Methode geaendert werden.
     *
     * @param gesuch das Gesuch. Null und undefined werden erlaubt.
     */
    public setGesuch(gesuch: TSGesuch): void {
        this.gesuch = gesuch;
        this.neustesGesuch = undefined;
        if (this.gesuch) {
            this.wizardStepManager.findStepsFromGesuch(this.gesuch.id);
            this.setHiddenSteps();
            // EWK Service mit bereits existierenden Daten initialisieren
            this.ewkRS.gesuchsteller1 = this.gesuch.gesuchsteller1;
            this.ewkRS.gesuchsteller2 = this.gesuch.gesuchsteller2;
            // Es soll nur einmalig geprueft werden, ob das aktuelle Gesuch das neueste dieses Falls fuer die gewuenschte Periode ist.
            if (this.gesuch.id) {
                this.gesuchRS.isNeuestesGesuch(this.gesuch.id).then((resp: boolean) => {
                    this.neustesGesuch = resp;
                });
            }
        }
        this.ewkPersonGS1 = undefined;
        this.ewkPersonGS2 = undefined;
        this.ewkResultatGS1 = undefined;
        this.ewkResultatGS2 = undefined;
        this.activInstitutionenList = undefined; // Liste zuruecksetzen, da u.U. im Folgegesuch andere Stammdaten gelten!
    }

    public getGesuch(): TSGesuch {
        return this.gesuch;
    }

    public getFall(): TSFall {
        if (this.getGesuch() && this.getGesuch().dossier) {
            return this.getGesuch().dossier.fall;
        }
        return undefined;
    }

    public getDossier(): TSDossier {
        if (this.gesuch) {
            return this.gesuch.dossier;
        }
        return undefined;
    }

    /**
     * Prueft ob der 2. Gesuchtsteller eingetragen werden muss je nach dem was in Familiensituation ausgewaehlt wurde. Wenn es sich
     * um eine Mutation handelt wird nur geschaut ob der 2GS bereits existiert. Wenn ja, dann wird er benoetigt, da bei Mutationen darf
     * der 2GS nicht geloescht werden
     */
    public isGesuchsteller2Required(): boolean {
        if (this.gesuch && this.getFamiliensituation() && this.getFamiliensituation().familienstatus) {
            return this.getFamiliensituation().hasSecondGesuchsteller()
                || (this.gesuch.isMutation() && this.gesuch.gesuchsteller2 != null && this.gesuch.gesuchsteller2 !== undefined);
        } else {
            return false;
        }
    }

    public isBasisJahr2Required(): boolean {
        return this.getEkvFuerBasisJahrPlus(2);
    }

    public isRequiredEKV_GS_BJ(gs: number, bj: number): boolean {
        if (gs === 2) {
            return this.getEkvFuerBasisJahrPlus(bj) && this.isGesuchsteller2Required();
        } else {
            return this.getEkvFuerBasisJahrPlus(bj);
        }

    }

    public getFamiliensituation(): TSFamiliensituation {
        if (this.gesuch) {
            return this.gesuch.extractFamiliensituation();
        }
        return undefined;
    }

    public getFamiliensituationErstgesuch(): TSFamiliensituation {
        if (this.gesuch) {
            return this.gesuch.extractFamiliensituationErstgesuch();
        }
        return undefined;
    }

    public updateFachstellenList(): void {
        this.fachstelleRS.getAllFachstellen().then((response: TSFachstelle[]) => {
            this.fachstellenList = response;
        });
    }

    /**
     * Retrieves the list of InstitutionStammdaten for the date of today.
     */
    public updateActiveInstitutionenList(): void {
        this.instStamRS.getAllActiveInstitutionStammdatenByGesuchsperiode(this.getGesuchsperiode().id).then((response: TSInstitutionStammdaten[]) => {
            this.activInstitutionenList = response;
        });
    }

    /**
     * Wenn das Gesuch schon gespeichert ist (timestampErstellt != null), wird dieses nur aktualisiert. Wenn es sich um ein neues Gesuch handelt
     * dann wird zuerst der Fall erstellt, dieser ins Gesuch kopiert und dann das Gesuch erstellt
     * @returns {IPromise<TSGesuch>}
     */
    public saveGesuchAndFall(): IPromise<TSGesuch> {
        if (this.gesuch && this.gesuch.timestampErstellt) {
            // Gesuch schon vorhanden
            return this.updateGesuch();
        } else {
            // Gesuch noch nicht vorhanden
            if (this.gesuch.dossier && !this.gesuch.dossier.isNew()) {
                // Dossier schon vorhaden -> Wir koennen davon ausgehen, dass auch der Fall vorhanden ist
                return this.createGesuch(this.gesuch);
            } else {
                if (this.gesuch.dossier.fall && !this.gesuch.dossier.fall.isNew()) {
                    // Fall ist schon vorhanden
                    return this.dossierRS.createDossier(this.gesuch.dossier).then((dossierResponse: TSDossier) => {
                        this.gesuch.dossier = angular.copy(dossierResponse);
                        return this.createGesuch(this.gesuch);
                    });
                } else {
                    return this.fallRS.createFall(this.gesuch.dossier.fall).then((fallResponse: TSFall) => {
                        this.gesuch.dossier.fall = angular.copy(fallResponse);
                        return this.dossierRS.createDossier(this.gesuch.dossier).then((dossierResponse: TSDossier) => {
                            this.gesuch.dossier = angular.copy(dossierResponse);
                            return this.createGesuch(this.gesuch);
                        });
                    });
                }
            }
        }
    }

    private createGesuch(gesuch: TSGesuch): IPromise<TSGesuch> {
        return this.gesuchRS.createGesuch(gesuch).then((gesuchResponse: any) => {
            this.gesuch = gesuchResponse;
            return this.gesuch;
        });
    }

    public reloadGesuch(): IPromise<TSGesuch> {
        return this.gesuchRS.findGesuch(this.gesuch.id).then((gesuchResponse: any) => {
            this.setGesuch(gesuchResponse);
            return this.gesuch;
        });
    }

    /**
     * Update das Gesuch
     * @returns {IPromise<TSGesuch>}
     */
    public updateGesuch(): IPromise<TSGesuch> {
        return this.gesuchRS.updateGesuch(this.gesuch).then((gesuchResponse: any) => {
            this.gesuch = gesuchResponse;
            this.calculateNewStatus(this.gesuch.status); // just to be sure that the status has been correctly updated
            return this.gesuch;
        });
    }

    /**
     * Update das Gesuch
     * @returns {IPromise<TSGesuch>}
     */
    public saveFinanzielleSituationStart(): IPromise<TSGesuch> {
        return this.finanzielleSituationRS.saveFinanzielleSituationStart(this.gesuch).then((gesuchResponse: any) => {
            this.gesuch = gesuchResponse;
            return this.gesuch;
        });
    }

    /**
     * Speichert den StammdatenToWorkWith.
     */
    public updateGesuchsteller(umzug: boolean): IPromise<TSGesuchstellerContainer> {
        // Da showUmzug nicht im Server gespeichert wird, muessen wir den alten Wert kopieren und nach der Aktualisierung wiedersetzen
        let tempShowUmzug: boolean = this.getStammdatenToWorkWith().showUmzug;
        return this.gesuchstellerRS.saveGesuchsteller(this.getStammdatenToWorkWith(), this.gesuch.id, this.gesuchstellerNumber, umzug)
            .then((gesuchstellerResponse: any) => {
                this.setStammdatenToWorkWith(gesuchstellerResponse);
                return this.getStammdatenToWorkWith();
            });
    }

    public saveFinanzielleSituation(): IPromise<TSFinanzielleSituationContainer> {
        return this.finanzielleSituationRS.saveFinanzielleSituation(
            this.getStammdatenToWorkWith().finanzielleSituationContainer, this.getStammdatenToWorkWith().id, this.gesuch.id)
            .then((finSitContRespo: TSFinanzielleSituationContainer) => {
                this.getStammdatenToWorkWith().finanzielleSituationContainer = finSitContRespo;
                return this.getStammdatenToWorkWith().finanzielleSituationContainer;
            });
    }

    public saveEinkommensverschlechterungContainer(): IPromise<TSEinkommensverschlechterungContainer> {
        return this.einkommensverschlechterungContainerRS.saveEinkommensverschlechterungContainer(
            this.getStammdatenToWorkWith().einkommensverschlechterungContainer, this.getStammdatenToWorkWith().id, this.gesuch.id)
            .then((ekvContRespo: TSEinkommensverschlechterungContainer) => {
                this.getStammdatenToWorkWith().einkommensverschlechterungContainer = ekvContRespo;
                return this.getStammdatenToWorkWith().einkommensverschlechterungContainer;
            });
    }

    /**
     * Gesuchsteller nummer darf nur 1 oder 2 sein. Wenn die uebergebene Nummer nicht 1 oder 2 ist, wird dann 1 gesetzt
     * @param gsNumber
     */
    public setGesuchstellerNumber(gsNumber: number) {
        if (gsNumber === 1 || gsNumber === 2) {
            this.gesuchstellerNumber = gsNumber;
        } else {
            this.gesuchstellerNumber = 1;
        }
    }

    /**
     * BasisJahrPlus nummer darf nur 1 oder 2 sein. Wenn die uebergebene Nummer nicht 1 oder 2 ist, wird dann 1 gesetzt
     * @param bjpNumber
     */
    public setBasisJahrPlusNumber(bjpNumber: number) {
        if (bjpNumber === 1 || bjpNumber === 2) {
            this.basisJahrPlusNumber = bjpNumber;
        } else {
            this.basisJahrPlusNumber = 1;
        }
    }

    /**
     * Setzt den Kind Index. Dies ist der Index des aktuellen Kindes in der Liste der Kinder
     * @param kindIndex
     */
    public setKindIndex(kindIndex: number) {
        if (kindIndex >= 0) {
            this.kindIndex = kindIndex;
        } else {
            this.kindIndex = 0;
        }
    }

    /**
     * Setzt den BetreuungsIndex.
     * @param betreuungIndex
     */
    public setBetreuungIndex(betreuungIndex: number) {
        if (betreuungIndex >= 0) {
            this.betreuungIndex = betreuungIndex;
        } else {
            this.setKindIndex(0);
        }
    }

    public convertKindNumberToKindIndex(kindNumber: number): number {
        for (let i = 0; i < this.getGesuch().kindContainers.length; i++) {
            if (this.getGesuch().kindContainers[i].kindNummer === kindNumber) {
                return i;
            }
        }
        return -1;
    }

    public convertBetreuungNumberToBetreuungIndex(betreuungNumber: number): number {
        for (let i = 0; i < this.getKindToWorkWith().betreuungen.length; i++) {
            if (this.getKindToWorkWith().betreuungen[i].betreuungNummer === betreuungNumber) {
                return i;
            }
        }
        return -1;
    }

    public getFachstellenList(): Array<TSFachstelle> {
        if (this.fachstellenList === undefined) {
            this.fachstellenList = []; // init empty while we wait for promise
            this.updateFachstellenList();
        }
        return this.fachstellenList;
    }

    public getActiveInstitutionenList(): Array<TSInstitutionStammdaten> {
        if (this.activInstitutionenList === undefined) {
            this.activInstitutionenList = []; // init empty while we wait for promise
            this.updateActiveInstitutionenList();

        }
        return this.activInstitutionenList;
    }

    public resetActiveInstitutionenList(): void {
        // Der Cache muss geloescht werden, damit die Institutionen beim nächsten Aufruf neu geladen werden
        this.globalCacheService.getCache(TSCacheTyp.EBEGU_INSTITUTIONSSTAMMDATEN).removeAll(); // muss immer geleert werden
        this.updateActiveInstitutionenList();
    }

    public getStammdatenToWorkWith(): TSGesuchstellerContainer {
        if (this.gesuchstellerNumber === 2) {
            return this.gesuch.gesuchsteller2;
        } else {
            return this.gesuch.gesuchsteller1;
        }
    }

    public getEkvFuerBasisJahrPlus(basisJahrPlus: number): boolean {
        if (!this.gesuch.extractEinkommensverschlechterungInfo()) {
            this.initEinkommensverschlechterungInfo();
        }

        if (basisJahrPlus === 2) {
            return this.gesuch.extractEinkommensverschlechterungInfo().ekvFuerBasisJahrPlus2;
        } else {
            return this.gesuch.extractEinkommensverschlechterungInfo().ekvFuerBasisJahrPlus1;
        }
    }

    public setStammdatenToWorkWith(gesuchsteller: TSGesuchstellerContainer): TSGesuchstellerContainer {
        if (this.gesuchstellerNumber === 1) {
            this.gesuch.gesuchsteller1 = gesuchsteller;
            return this.gesuch.gesuchsteller1;
        } else {
            this.gesuch.gesuchsteller2 = gesuchsteller;
            return this.gesuch.gesuchsteller2;
        }
    }

    public initStammdaten(): void {
        if (!this.getStammdatenToWorkWith()) {
            let gesuchsteller: TSGesuchsteller;
            // die daten die wir aus iam importiert haben werden bei gs1 abgefuellt
            if (this.gesuchstellerNumber === 1 && this.authServiceRS.isOneOfRoles(TSRoleUtil.getGesuchstellerOnlyRoles())) {
                let principal: TSUser = this.authServiceRS.getPrincipal();
                let name: string = principal ? principal.nachname : undefined;
                let vorname: string = principal ? principal.vorname : undefined;
                let email: string = principal ? principal.email : undefined;
                gesuchsteller = new TSGesuchsteller(vorname, name, undefined, undefined, email);
            } else {
                gesuchsteller = new TSGesuchsteller();
            }
            this.setStammdatenToWorkWith(new TSGesuchstellerContainer(gesuchsteller));
            this.getStammdatenToWorkWith().adressen = this.initWohnAdresse();
        }
    }

    private initEinkommensverschlechterungInfo(): void {
        if (this.gesuch && !this.gesuch.extractEinkommensverschlechterungInfo()) {
            this.gesuch.einkommensverschlechterungInfoContainer = new TSEinkommensverschlechterungInfoContainer();
            this.gesuch.einkommensverschlechterungInfoContainer.init();
        }
    }

    /**
     * Erstellt ein neues Gesuch und einen neuen Fall. Wenn !forced sie werden nur erstellt wenn das Gesuch noch nicht erstellt wurde i.e. es null/undefined ist
     * Wenn force werden Gesuch und Fall immer erstellt.
     */
    public initGesuch(forced: boolean, eingangsart: TSEingangsart) {
        if (forced || (!forced && !this.gesuch)) {
            this.initAntrag(TSAntragTyp.ERSTGESUCH, eingangsart);
        }
        this.antragStatusHistoryRS.loadLastStatusChange(this.getGesuch());
    }

    /**
     * Erstellt ein neues Gesuch mit der angegebenen Eingangsart und Gesuchsperiode. Damit dies im resolve des
     * routing gemacht werden kann, wird das ganze als promise gehandhabt
     * @param forced
     * @param eingangsart
     * @param gesuchsperiodeId
     * @param dossierId
     * @return a void promise that is resolved once all subpromises are done
     */
    public initGesuchWithEingangsart(forced: boolean, eingangsart: TSEingangsart, gesuchsperiodeId: string, dossierId: string): IPromise<TSGesuch> {
        this.initGesuch(forced, eingangsart);
        let setGesuchsperiodeProm: IPromise<void>;
        if (gesuchsperiodeId) {
            setGesuchsperiodeProm = this.gesuchsperiodeRS.findGesuchsperiode(gesuchsperiodeId).then(periode => {
                this.gesuch.gesuchsperiode = periode;
            });
        }

        let setDossierPromise: angular.IPromise<void>;
        if (dossierId) {
            setDossierPromise = this.dossierRS.findDossier(dossierId).then(foundDossier => {
                this.gesuch.dossier = foundDossier;
            });
        }

        if (forced) {
            if (TSEingangsart.ONLINE === eingangsart) {
                this.gesuch.status = TSAntragStatus.IN_BEARBEITUNG_GS;
            } else {
                this.gesuch.status = TSAntragStatus.IN_BEARBEITUNG_JA;
            }
            //ewk zuruecksetzen
            if (this.ewkRS) {
                this.ewkRS.gesuchsteller1 = undefined;
                this.ewkRS.gesuchsteller2 = undefined;
            }
        }

        // this creates a list of promises and resolves them all. once all promises are resolved the .then function is triggered
        return this.$q.all([setGesuchsperiodeProm, setDossierPromise]).then(() => {
            this.log.debug('initialized new gesuch ', this.gesuch);
            return this.gesuch;

        });
    }

    /**
     * Diese Methode erstellt eine Fake-Mutation als gesuch fuer das GesuchModelManager. Die Mutation ist noch leer und hat
     * das ID des Gesuchs aus dem sie erstellt wurde. Wenn der Benutzer auf speichern klickt, wird der Service "antragMutieren"
     * mit dem ID des alten Gesuchs aufgerufen. Das Objekt das man zurueckbekommt, wird dann diese Fake-Mutation mit den richtigen
     * Daten ueberschreiben
     * @param gesuchID
     * @param eingangsart
     * @param gesuchsperiodeId
     * @param dossierId
     */
    public initMutation(gesuchID: string, eingangsart: TSEingangsart, gesuchsperiodeId: string, dossierId: string): void {
        this.initCopyOfGesuch(gesuchID, eingangsart, gesuchsperiodeId, dossierId, TSAntragTyp.MUTATION);
    }

    /**
     * Diese Methode erstellt ein Fake-Erneuerungsgesuch als gesuch fuer das GesuchModelManager. Das Gesuch ist noch leer und hat
     * das ID des Gesuchs aus dem es erstellt wurde.
     */
    public initErneuerungsgesuch(gesuchID: string, eingangsart: TSEingangsart, gesuchsperiodeId: string, dossierId: string) {
        this.initCopyOfGesuch(gesuchID, eingangsart, gesuchsperiodeId, dossierId, TSAntragTyp.ERNEUERUNGSGESUCH);
    }

    private initCopyOfGesuch(gesuchID: string, eingangsart: TSEingangsart, gesuchsperiodeId: string, dossierId: string, antragTyp: TSAntragTyp): void {
        this.gesuchsperiodeRS.findGesuchsperiode(gesuchsperiodeId).then(periode => {
            this.gesuch.gesuchsperiode = periode;
        });
        this.initAntrag(antragTyp, eingangsart);
        this.dossierRS.findDossier(dossierId).then(foundDossier => {
            this.gesuch.dossier = foundDossier;
        });
        this.gesuch.id = gesuchID; //setzen wir das alte gesuchID, um danach im Server die Mutation erstellen zu koennen
        if (TSEingangsart.ONLINE === eingangsart) {
            this.gesuch.status = TSAntragStatus.IN_BEARBEITUNG_GS;
        } else {
            this.gesuch.status = TSAntragStatus.IN_BEARBEITUNG_JA;
        }
        this.gesuch.emptyCopy = true;
    }

    private initAntrag(antragTyp: TSAntragTyp, eingangsart: TSEingangsart): void {
        this.gesuch = new TSGesuch();
        this.gesuch.dossier = new TSDossier();
        this.gesuch.dossier.fall = new TSFall();
        this.gesuch.typ = antragTyp; // by default ist es ein Erstgesuch
        this.gesuch.eingangsart = eingangsart;
        this.setHiddenSteps();
        this.wizardStepManager.initWizardSteps();
        this.setCurrentUserAsFallVerantwortlicher(false);
    }

    public initFamiliensituation() {
        if (!this.getFamiliensituation()) {
            this.gesuch.familiensituationContainer = new TSFamiliensituationContainer();
            this.gesuch.familiensituationContainer.familiensituationJA = new TSFamiliensituation();
        }
    }

    public initKinder(): void {
        if (!this.gesuch.kindContainers) {
            this.gesuch.kindContainers = [];
        }
    }

    /**
     * Gibt das Jahr des Anfangs der Gesuchsperiode minus 1 zurueck. undefined wenn die Gesuchsperiode nicht richtig gesetzt wurde
     * @returns {number}
     */
    public getBasisjahr(): number {
        if (this.getGesuchsperiodeBegin()) {
            return this.getGesuchsperiodeBegin().year() - 1;
        }
        return undefined;
    }

    /**
     * Gibt das Jahr des Anfangs der Gesuchsperiode minus 1 zurueck. undefined wenn die Gesuchsperiode nicht richtig gesetzt wurde
     * @returns {number}
     */
    public getBasisjahrPlus(plus: number): number {
        if (this.getGesuchsperiodeBegin()) {
            return this.getGesuchsperiodeBegin().year() - 1 + plus;
        }
        return undefined;
    }


    public getBasisjahrToWorkWith(): number {
        return this.getBasisjahrPlus(this.basisJahrPlusNumber);
    }

    /**
     * Gibt das gesamte Objekt Gesuchsperiode zurueck, das zum Gesuch gehoert.
     * @returns {any}
     */
    public getGesuchsperiode(): TSGesuchsperiode {
        if (this.gesuch) {
            return this.gesuch.gesuchsperiode;
        }
        return undefined;
    }

    /**
     * Gibt den Anfang der Gesuchsperiode als Moment zurueck
     * @returns {any}
     */
    public getGesuchsperiodeBegin(): moment.Moment {
        if (this.getGesuchsperiode() && this.getGesuchsperiode().gueltigkeit) {
            return this.getGesuchsperiode().gueltigkeit.gueltigAb;
        }
        return undefined;
    }

    private initWohnAdresse(): Array<TSAdresseContainer> {
        let wohnAdresseContanier: TSAdresseContainer = new TSAdresseContainer();
        let wohnAdresse = new TSAdresse();
        wohnAdresse.adresseTyp = TSAdressetyp.WOHNADRESSE;
        wohnAdresseContanier.showDatumVon = false;
        wohnAdresseContanier.adresseJA = wohnAdresse;
        return [wohnAdresseContanier];
    }

    public getKinderList(): Array<TSKindContainer> {
        if (this.gesuch) {
            return this.gesuch.kindContainers;
        }
        return [];
    }

    /**
     *
     * @returns {any} Alle KindContainer in denen das Kind Betreuung benoetigt
     */
    public getKinderWithBetreuungList(): Array<TSKindContainer> {
        let listResult: Array<TSKindContainer> = [];
        if (this.gesuch) {
            listResult = this.gesuch.getKinderWithBetreuungList();
        }
        return listResult;
    }


    public saveBetreuung(betreuungToSave: TSBetreuung, betreuungsstatusNeu: TSBetreuungsstatus, abwesenheit: boolean): IPromise<TSBetreuung> {
        if (betreuungsstatusNeu === TSBetreuungsstatus.ABGEWIESEN) {
            return this.betreuungRS.betreuungsPlatzAbweisen(betreuungToSave, this.getKindToWorkWith().id, this.gesuch.id)
                .then((storedBetreuung: any) => {
                    return this.gesuchRS.getGesuchBetreuungenStatus(this.gesuch.id).then((betreuungenStatus) => {
                        this.gesuch.gesuchBetreuungenStatus = betreuungenStatus;
                        return this.handleSavedBetreuung(storedBetreuung);
                    });
                });
        } else  if (betreuungsstatusNeu === TSBetreuungsstatus.BESTAETIGT) {
            return this.betreuungRS.betreuungsPlatzBestaetigen(betreuungToSave, this.getKindToWorkWith().id, this.gesuch.id)
                .then((storedBetreuung: any) => {
                    return this.gesuchRS.getGesuchBetreuungenStatus(this.gesuch.id).then((betreuungenStatus) => {
                        this.gesuch.gesuchBetreuungenStatus = betreuungenStatus;
                        return this.handleSavedBetreuung(storedBetreuung);
                    });
                });
        } else  if (betreuungsstatusNeu === TSBetreuungsstatus.SCHULAMT_ANMELDUNG_UEBERNOMMEN) {
            return this.betreuungRS.anmeldungSchulamtUebernehmen(betreuungToSave, this.getKindToWorkWith().id, this.gesuch.id)
                .then((storedBetreuung: any) => {
                    return this.gesuchRS.getGesuchBetreuungenStatus(this.gesuch.id).then((betreuungenStatus) => {
                        this.gesuch.gesuchBetreuungenStatus = betreuungenStatus;
                        return this.handleSavedBetreuung(storedBetreuung);
                    });
                });
        } else  if (betreuungsstatusNeu === TSBetreuungsstatus.SCHULAMT_ANMELDUNG_ABGELEHNT) {
            return this.betreuungRS.anmeldungSchulamtAblehnen(betreuungToSave, this.getKindToWorkWith().id, this.gesuch.id)
                .then((storedBetreuung: any) => {
                    return this.gesuchRS.getGesuchBetreuungenStatus(this.gesuch.id).then((betreuungenStatus) => {
                        this.gesuch.gesuchBetreuungenStatus = betreuungenStatus;
                        return this.handleSavedBetreuung(storedBetreuung);
                    });
                });
        } else  if (betreuungsstatusNeu === TSBetreuungsstatus.SCHULAMT_FALSCHE_INSTITUTION) {
            return this.betreuungRS.anmeldungSchulamtFalscheInstitution(betreuungToSave, this.getKindToWorkWith().id, this.gesuch.id)
                .then((storedBetreuung: any) => {
                    return this.gesuchRS.getGesuchBetreuungenStatus(this.gesuch.id).then((betreuungenStatus) => {
                        this.gesuch.gesuchBetreuungenStatus = betreuungenStatus;
                        return this.handleSavedBetreuung(storedBetreuung);
                    });
                });
        } else {
            betreuungToSave.betreuungsstatus = betreuungsstatusNeu;
            return this.betreuungRS.saveBetreuung(betreuungToSave, this.getKindToWorkWith().id, this.gesuch.id, abwesenheit)
                .then((storedBetreuung: any) => {
                    return this.gesuchRS.getGesuchBetreuungenStatus(this.gesuch.id).then((betreuungenStatus) => {
                        this.gesuch.gesuchBetreuungenStatus = betreuungenStatus;
                        return this.handleSavedBetreuung(storedBetreuung);
                    });
                });
        }
    }

    private handleSavedBetreuung(storedBetreuung: TSBetreuung): TSBetreuung {
        this.getKindFromServer();
        if (!storedBetreuung.isNew()) {   //gespeichertes kind war nicht neu
            let i: number = EbeguUtil.getIndexOfElementwithID(storedBetreuung, this.getKindToWorkWith().betreuungen);
            if (i >= 0) {
                this.getKindToWorkWith().betreuungen[i] = storedBetreuung;
                this.setBetreuungIndex(i);
            }
        } else {
            this.getKindToWorkWith().betreuungen.push(storedBetreuung);  //neues kind anfuegen
            this.setBetreuungIndex(this.getKindToWorkWith().betreuungen.length - 1);
        }
        this.getDossierFromServer(); // to reload the verantwortliche that may have changed
        return storedBetreuung;
    }

    public saveKind(kindToSave: TSKindContainer): IPromise<TSKindContainer> {
        return this.kindRS.saveKind(kindToSave, this.gesuch.id)
            .then((storedKindCont: TSKindContainer) => {
                this.getDossierFromServer();
                if (!kindToSave.isNew()) {   //gespeichertes kind war nicht neu
                    let i: number = EbeguUtil.getIndexOfElementwithID(kindToSave, this.gesuch.kindContainers);
                       if (i >= 0) {
                           this.gesuch.kindContainers[i] = storedKindCont;
                       }
                } else {
                    this.gesuch.kindContainers.push(storedKindCont);  //neues kind anfuegen
                }
                return storedKindCont;
            });
    }


    /**
     * Sucht das KindToWorkWith im Server und aktualisiert es mit dem bekommenen Daten
     * @returns {IPromise<TSKindContainer>}
     */
    private getKindFromServer(): IPromise<TSKindContainer> {
        return this.kindRS.findKind(this.getKindToWorkWith().id).then((kindResponse) => {
            return this.setKindToWorkWith(kindResponse);
        });
    }

    /**
     * Sucht das Gesuch im Server und aktualisiert es mit dem bekommenen Daten
     * @returns {IPromise<void>}
     */
    private getDossierFromServer(): IPromise<TSDossier> {
        return this.dossierRS.findDossier(this.gesuch.dossier.id).then((dossierResponse) => {
            this.gesuch.dossier = dossierResponse;
            return this.gesuch.dossier;
        });
    }

    public getKindToWorkWith(): TSKindContainer {
        if (this.gesuch && this.gesuch.kindContainers && this.gesuch.kindContainers.length > this.kindIndex) {
            return this.gesuch.kindContainers[this.kindIndex];
        } else {
            this.log.error('kindContainers is not set or kindIndex is out of bounds ' + this.kindIndex);
        }
        return undefined;
    }

    /**
     * Sucht im ausgewaehlten Kind (kindIndex) nach der aktuellen Betreuung. Deshalb muessen sowohl
     * kindIndex als auch betreuungNumber bereits gesetzt sein.
     * @returns {TSBetreuung}
     */
    public getBetreuungToWorkWith(): TSBetreuung {
        if (this.getKindToWorkWith() && this.getKindToWorkWith().betreuungen.length > this.betreuungIndex) {
            return this.getKindToWorkWith().betreuungen[this.betreuungIndex];
        } else {
            this.log.error('kindToWorkWith is not set or index of betreuung is out of bounds ' + this.betreuungIndex);
        }
        return undefined;
    }

    /**
     * Ersetzt das Kind in der aktuelle Position "kindIndex" durch das gegebene Kind. Aus diesem Grund muss diese Methode
     * nur aufgerufen werden, wenn die Position "kindIndex" schon richtig gesetzt wurde.
     * @param kind
     * @returns {TSKindContainer}
     */
    public setKindToWorkWith(kind: TSKindContainer): TSKindContainer {
        this.gesuch.kindContainers[this.kindIndex] = kind;
        return this.gesuch.kindContainers[this.kindIndex];
    }

    /**
     * Ersetzt die Betreuung in der aktuelle Position "betreuungIndex" durch die gegebene Betreuung. Aus diesem Grund muss diese Methode
     * nur aufgerufen werden, wenn die Position "betreuungIndex" schon richtig gesetzt wurde.
     * @param betreuung
     * @returns {TSBetreuung}
     */
    public setBetreuungToWorkWith(betreuung: TSBetreuung): TSBetreuung {
        this.getKindToWorkWith().betreuungen[this.betreuungIndex] = betreuung;
        return this.getKindToWorkWith().betreuungen[this.betreuungIndex];
    }

    /**
     * Entfernt das aktuelle Kind von der Liste aber nicht von der DB.
     */
    public removeKindFromList() {
        this.gesuch.kindContainers.splice(this.kindIndex, 1);
        this.setKindIndex(undefined); //by default auf undefined setzen
    }

    /**
     * Entfernt die aktuelle Betreuung des aktuellen Kindes von der Liste aber nicht von der DB.
     */
    public removeBetreuungFromKind() {
        this.getKindToWorkWith().betreuungen.splice(this.betreuungIndex, 1);
        this.setBetreuungIndex(undefined); //by default auf undefined setzen
        // recalculates the current status because a change in a Betreuung could mean a change in the gesuchstatus, for example when
        // the status was PLATZBESTAETIGUNG_ABGEWIESEN and the declined Platz is removed, the new status should be GEPRUEFT
        this.getGesuch().status = this.calculateNewStatus(this.getGesuch().status);
    }

    public getKindIndex(): number {
        return this.kindIndex;
    }

    public getBetreuungIndex(): number {
        return this.betreuungIndex;
    }

    public getGesuchstellerNumber(): number {
        return this.gesuchstellerNumber;
    }

    public getBasisJahrPlusNumber(): number {
        return this.basisJahrPlusNumber;
    }

    /**
     * Check whether the Gesuch is already saved in the database.
     * Case yes the fields shouldn't be editable anymore
     */
    public isGesuchSaved(): boolean {
        return this.gesuch && (this.gesuch.timestampErstellt !== undefined)
            && (this.gesuch.timestampErstellt !== null);
    }

    /**
     * Sucht das gegebene KindContainer in der List von KindContainer, erstellt es als KindToWorkWith
     * und gibt die Position in der Array zurueck. Gibt -1 zurueck wenn das Kind nicht gefunden wurde.
     * @param kind
     */
    public findKind(kind: TSKindContainer): number {
        if (this.gesuch.kindContainers.indexOf(kind) >= 0) {
            this.setKindIndex(this.gesuch.kindContainers.indexOf(kind));
            return this.kindIndex;
        }
        return -1;
    }

    /**
     * Sucht das Kind mit der eingegebenen KindID in allen KindContainers des Gesuchs. kindIndex wird gesetzt und zurueckgegeben
     * @param kindID
     * @returns {number}
     */
    public findKindById(kindID: string): number {
        if (this.gesuch.kindContainers) {
            for (let i = 0; i < this.gesuch.kindContainers.length; i++) {
                if (this.gesuch.kindContainers[i].id === kindID) {
                    this.setKindIndex(i);
                    return this.kindIndex;
                }
            }
        }
        return -1;
    }

    public removeKind(): IPromise<any> {
        return this.kindRS.removeKind(this.getKindToWorkWith().id, this.gesuch.id).then((responseKind: any) => {
            this.removeKindFromList();
            return this.gesuchRS.getGesuchBetreuungenStatus(this.gesuch.id).then((betreuungenStatus) => {
                this.gesuch.gesuchBetreuungenStatus = betreuungenStatus;
                return this.updateGesuch();
            });
        });
    }

    public findBetreuung(betreuung: TSBetreuung): number {
        if (this.getKindToWorkWith() && this.getKindToWorkWith().betreuungen) {
            this.setBetreuungIndex(this.getKindToWorkWith().betreuungen.indexOf(betreuung));
            return this.betreuungIndex;
        }
        return -1;
    }

    /**
     * Sucht die Betreuung mit der eingegebenen betreuungID in allen Betreuungen des aktuellen Kind. betreuungIndex wird gesetzt und zurueckgegeben
     * @param betreuungID
     * @returns {number}
     */
    public findBetreuungById(betreuungID: string): number {
        let kindToWorkWith: TSKindContainer = this.getKindToWorkWith();
        if (kindToWorkWith) {
            for (let i = 0; i < kindToWorkWith.betreuungen.length; i++) {
                if (kindToWorkWith.betreuungen[i].id === betreuungID) {
                    this.setBetreuungIndex(i);
                    return this.betreuungIndex;
                }
            }
        }
        return -1;
    }


    public removeBetreuung(): IPromise<void> {
        return this.betreuungRS.removeBetreuung(this.getBetreuungToWorkWith().id, this.gesuch.id).then((responseBetreuung: any) => {
            this.removeBetreuungFromKind();
            return this.gesuchRS.getGesuchBetreuungenStatus(this.gesuch.id).then((betreuungenStatus) => {
                this.gesuch.gesuchBetreuungenStatus = betreuungenStatus;
                this.kindRS.saveKind(this.getKindToWorkWith(), this.gesuch.id);
            });
        });
    }

    public removeErwerbspensum(pensum: TSErwerbspensumContainer): void {
        let erwerbspensenOfCurrentGS: Array<TSErwerbspensumContainer>;
        erwerbspensenOfCurrentGS = this.getStammdatenToWorkWith().erwerbspensenContainer;
        let index: number = erwerbspensenOfCurrentGS.indexOf(pensum);
        if (index >= 0) {
            let pensumToRemove: TSErwerbspensumContainer = this.getStammdatenToWorkWith().erwerbspensenContainer[index];
            if (pensumToRemove.id) { //wenn id vorhanden dann aus der DB loeschen
                this.erwerbspensumRS.removeErwerbspensum(pensumToRemove.id, this.getGesuch().id)
                    .then(() => {
                        erwerbspensenOfCurrentGS.splice(index, 1);
                    });
            } else {
                //sonst nur vom gui wegnehmen
                erwerbspensenOfCurrentGS.splice(index, 1);
            }
        } else {
            console.log('can not remove Erwerbspensum since it  could not be found in list');
        }
    }

    findIndexOfErwerbspensum(gesuchstellerNumber: number, pensum: any): number {
        let gesuchsteller: TSGesuchstellerContainer;
        gesuchsteller = gesuchstellerNumber === 2 ? this.gesuch.gesuchsteller2 : this.gesuch.gesuchsteller1;
        return gesuchsteller.erwerbspensenContainer.indexOf(pensum);
    }

    saveErwerbspensum(gesuchsteller: TSGesuchstellerContainer, erwerbspensum: TSErwerbspensumContainer): IPromise<TSErwerbspensumContainer> {
        if (erwerbspensum.id) {
            return this.erwerbspensumRS.saveErwerbspensum(erwerbspensum, gesuchsteller.id, this.gesuch.id)
                .then((response: TSErwerbspensumContainer) => {

                    let i: number = EbeguUtil.getIndexOfElementwithID(erwerbspensum, gesuchsteller.erwerbspensenContainer);
                    if (i >= 0) {
                        gesuchsteller.erwerbspensenContainer[i] = erwerbspensum;
                    }
                    return response;
                });
        } else {
            return this.erwerbspensumRS.saveErwerbspensum(erwerbspensum, gesuchsteller.id, this.gesuch.id)
                .then((storedErwerbspensum: TSErwerbspensumContainer) => {
                    gesuchsteller.erwerbspensenContainer.push(storedErwerbspensum);
                    return storedErwerbspensum;
                });
        }

    }

    /**
     * Takes current user and sets him as the verantwortlicherBG of Fall. Depending on the role it sets him as
     * verantwortlicherBG or verantworlicherSCH
     *
     * @param {boolean} saveInDB when true it saves the verantwortliche in the database. If falls it only sets it in the client object
     */
    private setCurrentUserAsFallVerantwortlicher(saveInDB: boolean) {
        if (this.authServiceRS && this.authServiceRS.isOneOfRoles(TSRoleUtil.getAdministratorJugendamtRole())) {
            this.setUserAsFallVerantwortlicherBG(this.authServiceRS.getPrincipal(), saveInDB);
        }
        if (this.authServiceRS && this.authServiceRS.isOneOfRoles(TSRoleUtil.getSchulamtOnlyRoles())) {
            this.setUserAsFallVerantwortlicherTS(this.authServiceRS.getPrincipal(), saveInDB);
        }
    }

    public setUserAsFallVerantwortlicherTS(user: TSUser, saveInDB: boolean = true) {
        if (this.gesuch && this.gesuch.dossier) {
            if (saveInDB) {
                this.dossierRS.setVerantwortlicherTS(this.gesuch.dossier.id, user ? user.username : null)
                    .then(() => {
                        this.gesuch.dossier.verantwortlicherTS = user;
                    });
            } else {
                this.gesuch.dossier.verantwortlicherTS = user;
            }
        }
    }

    public setUserAsFallVerantwortlicherBG(user: TSUser, saveInDB: boolean = true) {
        if (this.gesuch && this.gesuch.dossier) {
            if (saveInDB) {
                this.dossierRS.setVerantwortlicherBG(this.gesuch.dossier.id, user ? user.username : null)
                    .then(() => {
                        this.gesuch.dossier.verantwortlicherBG = user;
                    });
            } else {
                this.gesuch.dossier.verantwortlicherBG = user;
            }
        }
    }

    public getFallVerantwortlicherBG(): TSUser {
        if (this.gesuch && this.gesuch.dossier) {
            return this.gesuch.dossier.getHauptverantwortlicher();
        }
        return undefined;
    }

    public getFallVerantwortlicherTS(): TSUser {
        if (this.gesuch && this.gesuch.dossier) {
            return this.gesuch.dossier.verantwortlicherTS;
        }
        return undefined;
    }

    public calculateVerfuegungen(): IPromise<void> {
        return this.verfuegungRS.calculateVerfuegung(this.gesuch.id)
            .then((response: TSKindContainer[]) => {
                this.updateKinderListWithCalculatedVerfuegungen(response);
                return;
            });
    }

    private updateKinderListWithCalculatedVerfuegungen(kinderWithVerfuegungen: TSKindContainer[]) {
        if (kinderWithVerfuegungen.length !== this.gesuch.kindContainers.length) {
            let msg: string = 'ACHTUNG Ungueltiger Zustand, Anzahl zurueckgelieferter Container'
                + (kinderWithVerfuegungen.length ? kinderWithVerfuegungen.length : 'no_container')
                + 'stimmt nicht mit erwareter ueberein ' + this.gesuch.kindContainers.length;
            this.log.error(msg);
            let error: TSExceptionReport = new TSExceptionReport(TSErrorType.INTERNAL, TSErrorLevel.SEVERE, msg, kinderWithVerfuegungen);
            this.errorService.addDvbError(error);
        }
        let numOfAssigned = 0;
        for (let i = 0; i < this.gesuch.kindContainers.length; i++) {
            for (let j = 0; j < kinderWithVerfuegungen.length; j++) {
                if (this.gesuch.kindContainers[i].id === kinderWithVerfuegungen[j].id) {
                    numOfAssigned++;
                    for (let k = 0; k < this.gesuch.kindContainers[i].betreuungen.length; k++) {
                        if (this.gesuch.kindContainers[i].betreuungen.length !== kinderWithVerfuegungen[j].betreuungen.length) {
                            let msg = 'ACHTUNG unvorhergesehener Zustand. Anzahl Betreuungen eines Kindes stimmt nicht' +
                                ' mit der berechneten Anzahl Betreuungen ueberein; erwartet: ' +
                                this.gesuch.kindContainers[i].betreuungen.length + ' erhalten: ' + kinderWithVerfuegungen[j].betreuungen.length;
                            this.log.error(msg, this.gesuch.kindContainers[i], kinderWithVerfuegungen[j]);
                            this.errorService.addMesageAsError(msg);
                        }
                        this.gesuch.kindContainers[i].betreuungen[k] = kinderWithVerfuegungen[j].betreuungen[k];
                    }
                }
            }
        }
        if (numOfAssigned !== this.gesuch.kindContainers.length) {
            let msg = 'ACHTUNG unvorhergesehener Zustand. Es konnte nicht jeder calculated Kindcontainer vom Server einem Container auf dem Client zugeordnet werden';
            this.log.error(msg, this.gesuch.kindContainers, kinderWithVerfuegungen);

            this.errorService.addMesageAsError(msg);
        }
        EbeguUtil.handleSmarttablesUpdateBug(this.gesuch.kindContainers);

    }

    public saveVerfuegung(ignorieren: boolean): IPromise<TSVerfuegung> {
        return this.verfuegungRS.saveVerfuegung(this.getVerfuegenToWorkWith(), this.gesuch.id, this.getBetreuungToWorkWith().id, ignorieren)
            .then((response: TSVerfuegung) => {
            this.setVerfuegenToWorkWith(response);
            this.getBetreuungToWorkWith().betreuungsstatus = TSBetreuungsstatus.VERFUEGT;
            this.calculateGesuchStatusVerfuegt();
            return this.getVerfuegenToWorkWith();
        });
    }

    private calculateGesuchStatusVerfuegt() {
        if (!this.isThereAnyOpenBetreuung()) {
            this.gesuch.status = this.calculateNewStatus(TSAntragStatus.VERFUEGT);
        }
    }

    public verfuegungSchliessenOhenVerfuegen(): IPromise<void> {
        return this.verfuegungRS.verfuegungSchliessenOhneVerfuegen(this.gesuch.id, this.getBetreuungToWorkWith().id).then((response) => {
            this.getBetreuungToWorkWith().betreuungsstatus = TSBetreuungsstatus.GESCHLOSSEN_OHNE_VERFUEGUNG;
            this.calculateGesuchStatusVerfuegt();
            return;
        });
    }

    public verfuegungSchliessenNichtEintreten(): IPromise<TSVerfuegung> {
        return this.verfuegungRS.nichtEintreten(this.getVerfuegenToWorkWith(), this.gesuch.id, this.getBetreuungToWorkWith().id).then((response: TSVerfuegung) => {
            this.setVerfuegenToWorkWith(response);
            this.getBetreuungToWorkWith().betreuungsstatus = TSBetreuungsstatus.NICHT_EINGETRETEN;
            this.calculateGesuchStatusVerfuegt();
            return this.getVerfuegenToWorkWith();
        });
    }

    public getVerfuegenToWorkWith(): TSVerfuegung {
        if (this.getKindToWorkWith() && this.getBetreuungToWorkWith()) {
            return this.getBetreuungToWorkWith().verfuegung;
        }
        return undefined;
    }

    public setVerfuegenToWorkWith(verfuegung: TSVerfuegung): void {
        if (this.getKindToWorkWith() && this.getBetreuungToWorkWith()) {
            this.getBetreuungToWorkWith().verfuegung = verfuegung;
        }
    }

    public isThereAnyKindWithBetreuungsbedarf(): boolean {
        let kinderList: Array<TSKindContainer> = this.getKinderList();
        for (let kind of kinderList) {
            //das kind muss schon gespeichert sein damit es zahelt
            if (kind.kindJA.familienErgaenzendeBetreuung && !kind.kindJA.isNew()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gibt true zurueck wenn es mindestens eine Betreuung gibt, dessen Status anders als VERFUEGT oder GESCHLOSSEN_OHNE_VERFUEGUNG oder SCHULAMT ist
     * @returns {boolean}
     */
    public isThereAnyOpenBetreuung(): boolean {
        let kinderWithBetreuungList: Array<TSKindContainer> = this.getKinderWithBetreuungList();
        for (let kind of kinderWithBetreuungList) {
            for (let betreuung of kind.betreuungen) {
                if (betreuung.betreuungsstatus !== TSBetreuungsstatus.SCHULAMT
                    && betreuung.betreuungsstatus !== TSBetreuungsstatus.SCHULAMT_FALSCHE_INSTITUTION
                    && betreuung.betreuungsstatus !== TSBetreuungsstatus.SCHULAMT_ANMELDUNG_AUSGELOEST
                    && betreuung.betreuungsstatus !== TSBetreuungsstatus.SCHULAMT_ANMELDUNG_UEBERNOMMEN
                    && betreuung.betreuungsstatus !== TSBetreuungsstatus.SCHULAMT_ANMELDUNG_ABGELEHNT
                    && betreuung.betreuungsstatus !== TSBetreuungsstatus.VERFUEGT
                    && betreuung.betreuungsstatus !== TSBetreuungsstatus.NICHT_EINGETRETEN
                    && betreuung.betreuungsstatus !== TSBetreuungsstatus.GESCHLOSSEN_OHNE_VERFUEGUNG) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Gibt true zurueck wenn es mindestens eine Betreuung gibt, dessen Status ABGEWIESEN ist
     * @returns {boolean}
     */
    public isThereAnyAbgewieseneBetreuung(): boolean {
        let kinderWithBetreuungList: Array<TSKindContainer> = this.getKinderWithBetreuungList();
        for (let kind of kinderWithBetreuungList) {
            for (let betreuung of kind.betreuungen) {
                if (betreuung.betreuungsstatus === TSBetreuungsstatus.ABGEWIESEN) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true when all Betreuungen are of kind SCHULAMT.
     * Returns false also if there are no Kinder with betreuungsbedarf
     */
    public areThereOnlySchulamtAngebote(): boolean {
        if (!this.getGesuch()) {
            return false;
        }
        return this.getGesuch().areThereOnlySchulamtAngebote();
    }

    /**
     * Returns true when all Betreuungen are of kind FERIENINSEL.
     * Returns false also if there are no Kinder with betreuungsbedarf
     */
    public areThereOnlyFerieninsel(): boolean {
        if (!this.getGesuch()) {
            return false;
        }
        return this.getGesuch().areThereOnlyFerieninsel();
    }

    /**
     * Returns true when all Betreuungen are of kind SCHULAMT.
     * Returns false also if there are no Kinder with betreuungsbedarf
     */
    public areThereOnlyGeschlossenOhneVerfuegung(): boolean {
        if (!this.gesuch) {
            return false;
        }
        return this.gesuch.areThereOnlyGeschlossenOhneVerfuegung();
    }

    /**
     * Returns true when all Betreuungen are of kind SCHULAMT.
     * Returns false also if there are no Kinder with betreuungsbedarf
     */
    public isThereAnySchulamtAngebot(): boolean {
        let kinderWithBetreuungList: Array<TSKindContainer> = this.getKinderWithBetreuungList();
        if (kinderWithBetreuungList.length <= 0) {
            return false; // no Kind with bedarf
        }
        for (let kind of kinderWithBetreuungList) {
            for (let betreuung of kind.betreuungen) {
                if (isSchulamt(betreuung.institutionStammdaten.betreuungsangebotTyp)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Setzt den Status des Gesuchs und speichert es in der Datenbank. Anstatt das ganze Gesuch zu schicken, rufen wir den Service auf
     * der den Status aktualisiert und erst wenn das geklappt hat, aktualisieren wir den Status auf dem Client.
     * Wird nur durchgefuehrt, wenn der gegebene Status nicht der aktuelle Status ist
     * @param status
     * @returns {IPromise<TSAntragStatus>}
     */
    public saveGesuchStatus(status: TSAntragStatus): IPromise<TSAntragStatus> {
        if (!this.isGesuchStatus(status)) {
            return this.gesuchRS.updateGesuchStatus(this.gesuch.id, status).then(() => {
                return this.antragStatusHistoryRS.loadLastStatusChange(this.getGesuch()).then(() => {
                    this.gesuch.status = this.calculateNewStatus(status);
                    return this.gesuch.status;
                });
            });
        }
        return undefined;
    }

    /**
     * Antrag freigeben
     */
    public antragFreigeben(antragId: string, usernameJA: string, usernameSCH: string): IPromise<TSGesuch> {
        return this.gesuchRS.antragFreigeben(antragId, usernameJA, usernameSCH).then((response) => {
            this.setGesuch(response);
            return response;
        });

    }

    /**
     * Returns true if the Gesuch has the given status
     * @param status
     * @returns {boolean}
     */
    public isGesuchStatus(status: TSAntragStatus): boolean {
        return this.gesuch.status === status;
    }

    /**
     * Returns true when the Gesuch must be readonly
     * @returns {boolean}
     */
    public isGesuchReadonly(): boolean {
        return this.gesuch && (isStatusVerfuegenVerfuegt(this.gesuch.status)
            || this.isGesuchReadonlyForRole()
            || this.getGesuch().gesperrtWegenBeschwerde);
    }

    /**
     * checks if the gesuch is readonly for a given role based on its state
     */
    public isGesuchReadonlyForRole(): boolean {
        let periodeReadonly: boolean = this.isGesuchsperiodeReadonly();
        if (this.authServiceRS.isOneOfRoles(TSRoleUtil.getReadOnlyRoles())) {
            return true;  // schulamt hat immer nur readonly zugriff
        } else if (this.authServiceRS.isRole(TSRole.GESUCHSTELLER)) {
            let gesuchReadonly: boolean = isAtLeastFreigegebenOrFreigabequittung(this.getGesuch().status); //readonly fuer gs wenn gesuch freigegeben oder weiter
            return gesuchReadonly || periodeReadonly;
        }
        return periodeReadonly;
    }

    public isGesuchsperiodeReadonly(): boolean {
        return this.getGesuch() && this.getGesuch().gesuchsperiode && (this.getGesuch().gesuchsperiode.status === TSGesuchsperiodeStatus.GESCHLOSSEN);
    }

    /**
     * Wenn das Gesuch Online durch den GS erstellt wurde, nun aber in Bearbeitung beim JA ist, handelt es sich um
     * den Korrekturmodus des Jugendamtes.
     * @returns {boolean}
     */
    public isKorrekturModusJugendamt(): boolean {
        return isAtLeastFreigegeben(this.gesuch.status) && !isAnyStatusOfVerfuegt(this.gesuch.status) && (TSEingangsart.ONLINE === this.getGesuch().eingangsart);
    }

    /**
     * Einige Status wie GEPRUEFT haben "substatus" auf dem Client die berechnet werden muessen. Aus diesem Grund rufen wir
     * diese Methode auf, bevor wir den Wert setzen.
     * @param status
     */
    public calculateNewStatus(status: TSAntragStatus): TSAntragStatus {
        if (TSAntragStatus.GEPRUEFT === status || TSAntragStatus.PLATZBESTAETIGUNG_ABGEWIESEN === status || TSAntragStatus.PLATZBESTAETIGUNG_WARTEN === status) {
            if (this.wizardStepManager.hasStepGivenStatus(TSWizardStepName.BETREUUNG, TSWizardStepStatus.NOK)) {
                if (this.getGesuch().isThereAnyBetreuung()) {
                    return TSAntragStatus.PLATZBESTAETIGUNG_ABGEWIESEN;
                } else {
                    return TSAntragStatus.GEPRUEFT;
                }
            } else if (this.wizardStepManager.hasStepGivenStatus(TSWizardStepName.BETREUUNG, TSWizardStepStatus.PLATZBESTAETIGUNG)) {
                return TSAntragStatus.PLATZBESTAETIGUNG_WARTEN;
            } else if (this.wizardStepManager.hasStepGivenStatus(TSWizardStepName.BETREUUNG, TSWizardStepStatus.OK)) {
                return TSAntragStatus.GEPRUEFT;
            }
        }
        return status;
    }

    /**
     * Gibt true zurueck, wenn der Antrag ein Erstgesuchist. False bekommt man wenn der Antrag eine Mutation ist
     * By default (beim Fehler oder leerem Gesuch) wird auch true zurueckgegeben
     */
    public isGesuch(): boolean {
        if (this.gesuch) {
            return this.gesuch.typ === TSAntragTyp.ERSTGESUCH || this.gesuch.typ === TSAntragTyp.ERNEUERUNGSGESUCH;
        }
        return true;
    }

    public saveMutation(): IPromise<TSGesuch> {
        return this.gesuchRS.antragMutieren(this.gesuch.id, this.gesuch.eingangsdatum)
            .then((response: TSGesuch) => {
                this.setGesuch(response);
                return this.wizardStepManager.findStepsFromGesuch(response.id).then(() => {
                    return this.getGesuch();
                });
            });
    }

    public saveErneuerungsgesuch(): IPromise<TSGesuch> {
        return this.gesuchRS.antragErneuern(this.gesuch.gesuchsperiode.id, this.gesuch.id, this.gesuch.eingangsdatum)
            .then((response: TSGesuch) => {
                this.setGesuch(response);
                return this.wizardStepManager.findStepsFromGesuch(response.id).then(() => {
                    return this.getGesuch();
                });
            });
    }

    /**
     * Aktualisiert alle gegebenen Betreuungen.
     * ACHTUNG. Die Betreuungen muessen existieren damit alles richtig funktioniert
     */
    public updateBetreuungen(betreuungenToUpdate: Array<TSBetreuung>, saveForAbwesenheit: boolean): IPromise<Array<TSBetreuung>> {
        if (betreuungenToUpdate && betreuungenToUpdate.length > 0) {
            return this.betreuungRS.saveBetreuungen(betreuungenToUpdate, this.gesuch.id, saveForAbwesenheit).then((updatedBetreuungen: Array<TSBetreuung>) => {
                //update data of Betreuungen
                this.gesuch.kindContainers.forEach((kindContainer: TSKindContainer) => {
                    for (let i = 0; i < kindContainer.betreuungen.length; i++) {
                        let indexOfUpdatedBetreuung = this.wasBetreuungUpdated(kindContainer.betreuungen[i], updatedBetreuungen);
                        if (indexOfUpdatedBetreuung >= 0) {
                            kindContainer.betreuungen[i] = updatedBetreuungen[indexOfUpdatedBetreuung];
                        }
                    }
                });
                return updatedBetreuungen;
            });
        } else {
            let defer = this.$q.defer<Array<TSBetreuung>>();
            defer.resolve();
            return defer.promise;
        }
    }

    private wasBetreuungUpdated(betreuung: TSBetreuung, updatedBetreuungen: Array<TSBetreuung>): number {
        if (betreuung && updatedBetreuungen) {
            for (let i = 0; i < updatedBetreuungen.length; i++) {
                if (updatedBetreuungen[i].id === betreuung.id) {
                    return i;
                }
            }
        }
        return -1;
    }

    public clearGesuch(): void {
        this.gesuch = undefined;
    }

    public getGesuchName(): string {
        return this.ebeguUtil.getGesuchNameFromGesuch(this.gesuch);
    }

    public isNeuestesGesuch(): boolean {
        return this.neustesGesuch;
    }

    public isErwerbspensumRequired(gesuchId: string): IPromise<boolean> {
        return this.erwerbspensumRS.isErwerbspensumRequired(gesuchId);
    }

    /**
     * Indicates whether the FinSit is available to be filled out or not.
     */
    public isFinanzielleSituationEnabled(): boolean {
        return !this.areThereOnlyFerieninsel();
    }

    /**
     * Indicates whether FinSit must be filled out or not. It supposes that it is enabled.
     */
    public isFinanzielleSituationDesired(): boolean {
        return !this.getGesuchsperiode().hasTagesschulenAnmeldung()
            || !this.areThereOnlySchulamtAngebote()
            || (this.getGesuch().extractFamiliensituation().verguenstigungGewuenscht === true
                && this.getGesuch().extractFamiliensituation().sozialhilfeBezueger === false);
    }

    public showFinanzielleSituationStart(): boolean {
        return this.isGesuchsteller2Required() ||
            (this.getGesuchsperiode() && this.getGesuchsperiode().hasTagesschulenAnmeldung() && this.areThereOnlySchulamtAngebote());
    }

    /**
     * gibt true zurueck wenn es keine defaultTagesschule ist oder wenn es eine defaultTagesschule ist aber die Gesuchsperiode
     * noch keine TagesschulenAnmeldung erlaubt.
     *
     * Eine DefaultTagesschule ist eine Tagesschule, die fuer die erste Gescuhsperiode erstellt wurde, damit man Betreuungen
     * der Art TAGESSCHULE erstellen darf. Jede Betreuung muss mit einer Institution verknuepft sein und TagesschuleBetreuungen
     * wurden mit der defaultTagesschule verknuepft. Die DefaultTagesschule wird anhand der ID erkannt.
     */
    public isDefaultTagesschuleAllowed(instStamm: TSInstitutionStammdaten): boolean {
        if (instStamm.id === '199ac4a1-448f-4d4c-b3a6-5aee21f89613') {
            return !(this.getGesuchsperiode() && this.getGesuchsperiode().hasTagesschulenAnmeldung());
        }
        return true;
    }
}
