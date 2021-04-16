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

import {ILogService, IPromise, IQService} from 'angular';
import * as moment from 'moment';
import {CONSTANTS} from '../../app/core/constants/CONSTANTS';
import {ErrorService} from '../../app/core/errors/service/ErrorService';
import {AntragStatusHistoryRS} from '../../app/core/service/antragStatusHistoryRS.rest';
import {BetreuungRS} from '../../app/core/service/betreuungRS.rest';
import {ErwerbspensumRS} from '../../app/core/service/erwerbspensumRS.rest';
import {EwkRS} from '../../app/core/service/ewkRS.rest';
import {FachstelleRS} from '../../app/core/service/fachstelleRS.rest';
import {GesuchstellerRS} from '../../app/core/service/gesuchstellerRS.rest';
import {InstitutionStammdatenRS} from '../../app/core/service/institutionStammdatenRS.rest';
import {KindRS} from '../../app/core/service/kindRS.rest';
import {VerfuegungRS} from '../../app/core/service/verfuegungRS.rest';
import {AuthLifeCycleService} from '../../authentication/service/authLifeCycle.service';
import {AuthServiceRS} from '../../authentication/service/AuthServiceRS.rest';
import {TSAdressetyp} from '../../models/enums/TSAdressetyp';
import {
    isAnyStatusOfVerfuegt,
    isAtLeastFreigegeben,
    isAtLeastFreigegebenOrFreigabequittung,
    isStatusVerfuegenVerfuegt,
    TSAntragStatus,
} from '../../models/enums/TSAntragStatus';
import {TSAntragTyp} from '../../models/enums/TSAntragTyp';
import {TSAuthEvent} from '../../models/enums/TSAuthEvent';
import {isSchulamt} from '../../models/enums/TSBetreuungsangebotTyp';
import {TSBetreuungsstatus} from '../../models/enums/TSBetreuungsstatus';
import {TSCacheTyp} from '../../models/enums/TSCacheTyp';
import {TSCreationAction} from '../../models/enums/TSCreationAction';
import {TSEingangsart} from '../../models/enums/TSEingangsart';
import {TSErrorLevel} from '../../models/enums/TSErrorLevel';
import {TSErrorType} from '../../models/enums/TSErrorType';
import {TSGesuchBetreuungenStatus} from '../../models/enums/TSGesuchBetreuungenStatus';
import {TSGesuchsperiodeStatus} from '../../models/enums/TSGesuchsperiodeStatus';
import {TSRole} from '../../models/enums/TSRole';
import {TSWizardStepName} from '../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../models/enums/TSWizardStepStatus';
import {TSAdresse} from '../../models/TSAdresse';
import {TSAdresseContainer} from '../../models/TSAdresseContainer';
import {TSBenutzerNoDetails} from '../../models/TSBenutzerNoDetails';
import {TSBetreuung} from '../../models/TSBetreuung';
import {TSDossier} from '../../models/TSDossier';
import {TSEinkommensverschlechterungContainer} from '../../models/TSEinkommensverschlechterungContainer';
import {TSEinkommensverschlechterungInfoContainer} from '../../models/TSEinkommensverschlechterungInfoContainer';
import {TSErwerbspensumContainer} from '../../models/TSErwerbspensumContainer';
import {TSEWKResultat} from '../../models/TSEWKResultat';
import {TSExceptionReport} from '../../models/TSExceptionReport';
import {TSFachstelle} from '../../models/TSFachstelle';
import {TSFall} from '../../models/TSFall';
import {TSFamiliensituation} from '../../models/TSFamiliensituation';
import {TSFamiliensituationContainer} from '../../models/TSFamiliensituationContainer';
import {TSFinanzielleSituationContainer} from '../../models/TSFinanzielleSituationContainer';
import {TSGemeinde} from '../../models/TSGemeinde';
import {TSGemeindeKonfiguration} from '../../models/TSGemeindeKonfiguration';
import {TSGemeindeStammdaten} from '../../models/TSGemeindeStammdaten';
import {TSGesuch} from '../../models/TSGesuch';
import {TSGesuchsperiode} from '../../models/TSGesuchsperiode';
import {TSGesuchsteller} from '../../models/TSGesuchsteller';
import {TSGesuchstellerContainer} from '../../models/TSGesuchstellerContainer';
import {TSInstitutionStammdaten} from '../../models/TSInstitutionStammdaten';
import {TSKindContainer} from '../../models/TSKindContainer';
import {TSVerfuegung} from '../../models/TSVerfuegung';
import {EbeguUtil} from '../../utils/EbeguUtil';
import {TSRoleUtil} from '../../utils/TSRoleUtil';
import {DossierRS} from './dossierRS.rest';
import {EinkommensverschlechterungContainerRS} from './einkommensverschlechterungContainerRS.rest';
import {FinanzielleSituationRS} from './finanzielleSituationRS.rest';
import {GemeindeRS} from './gemeindeRS.rest';
import {GesuchGenerator} from './gesuchGenerator';
import {GesuchRS} from './gesuchRS.rest';
import {GlobalCacheService} from './globalCacheService';
import {WizardStepManager} from './wizardStepManager';

export class GesuchModelManager {

    public static $inject = [
        'GesuchRS', 'GesuchstellerRS', 'FinanzielleSituationRS', 'KindRS', 'FachstelleRS',
        'ErwerbspensumRS', 'InstitutionStammdatenRS', 'BetreuungRS', '$log', 'AuthServiceRS',
        'EinkommensverschlechterungContainerRS', 'VerfuegungRS', 'WizardStepManager',
        'AntragStatusHistoryRS', 'EbeguUtil', 'ErrorService', '$q', 'AuthLifeCycleService', 'EwkRS',
        'GlobalCacheService', 'DossierRS', 'GesuchGenerator', 'GemeindeRS',
    ];
    private gesuch: TSGesuch;
    private neustesGesuch: boolean;
    public gesuchstellerNumber: number = 1;
    public basisJahrPlusNumber: number = 1;
    private kindIndex: number;
    private betreuungIndex: number;
    private fachstellenAnspruchList: Array<TSFachstelle>;
    private fachstellenErweiterteBetreuungList: Array<TSFachstelle>;
    private activInstitutionenForGemeindeList: Array<TSInstitutionStammdaten>;
    public gemeindeStammdaten: TSGemeindeStammdaten;
    public gemeindeKonfiguration: TSGemeindeKonfiguration;

    public ewkResultat: TSEWKResultat;

    // initialize empty KinderContainer list to avoid infinite loop in smart table
    public emptyKinderList: Array<TSKindContainer> = [];

    public constructor(
        private readonly gesuchRS: GesuchRS,
        private readonly gesuchstellerRS: GesuchstellerRS,
        private readonly finanzielleSituationRS: FinanzielleSituationRS,
        private readonly kindRS: KindRS,
        private readonly fachstelleRS: FachstelleRS,
        private readonly erwerbspensumRS: ErwerbspensumRS,
        private readonly instStamRS: InstitutionStammdatenRS,
        private readonly betreuungRS: BetreuungRS,
        private readonly log: ILogService,
        private readonly authServiceRS: AuthServiceRS,
        private readonly einkommensverschlechterungContainerRS: EinkommensverschlechterungContainerRS,
        private readonly verfuegungRS: VerfuegungRS,
        private readonly wizardStepManager: WizardStepManager,
        private readonly antragStatusHistoryRS: AntragStatusHistoryRS,
        private readonly ebeguUtil: EbeguUtil,
        private readonly errorService: ErrorService,
        private readonly $q: IQService,
        private readonly authLifeCycleService: AuthLifeCycleService,
        private readonly ewkRS: EwkRS,
        private readonly globalCacheService: GlobalCacheService,
        private readonly dossierRS: DossierRS,
        private readonly gesuchGenerator: GesuchGenerator,
        private readonly gemeindeRS: GemeindeRS,
    ) {
    }

    public $onInit(): void {
        this.authLifeCycleService.get$(TSAuthEvent.LOGOUT_SUCCESS)
            .subscribe(() => {
                    this.setGesuch(undefined);
                    this.log.debug('Cleared gesuch on logout');
                },
                err => this.log.error(err),
            );
    }

    /**
     * Je nach dem welche Rolle der Benutzer hat, wird das Gesuch aus der DB anders geholt.
     * Fuer Institutionen z.B. wird das Gesuch nur mit den relevanten Daten geholt
     */
    public openGesuch(gesuchId: string): IPromise<TSGesuch> {
        // Superadmin muss als "normale" Benutzer betrachtet werden
        const gesuchPromise = this.authServiceRS.isOneOfRoles(TSRoleUtil.getTraegerschaftInstitutionOnlyRoles()) ?
            this.gesuchRS.findGesuchForInstitution(gesuchId) :
            this.gesuchRS.findGesuch(gesuchId);

        return gesuchPromise
            .then(gesuch => this.wizardStepManager.findStepsFromGesuch(gesuchId)
                .then(() => {
                    if (gesuch) {
                        this.setGesuchDaten(gesuch);
                    }
                }).then(() => this.loadGemeindeStammdaten().then(stammdaten => {
                    if (gesuch) {
                        this.gemeindeStammdaten = stammdaten;
                        this.initGemeindeKonfiguration();
                    }
                    return gesuch;
                })),
            );
    }

    /**
     * In dieser Methode wird das Gesuch ohne die Gemeinde Stammdaten ersetzt.
     *
     * @param gesuch das Gesuch. Null und undefined werden erlaubt.
     */
    private setGesuchDaten(gesuch: TSGesuch): TSGesuch {
        this.gesuch = gesuch;
        this.neustesGesuch = undefined;
        if (this.gesuch && !this.getGesuch().isNew()) {
            this.wizardStepManager.findStepsFromGesuch(this.gesuch.id);
            this.wizardStepManager.setHiddenSteps(this.gesuch);
            // Es soll nur einmalig geprueft werden, ob das aktuelle Gesuch das neueste dieses Falls fuer die
            // gewuenschte Periode ist.
            if (this.gesuch.id) {
                this.gesuchRS.isNeuestesGesuch(this.gesuch.id).then((resp: boolean) => {
                    this.neustesGesuch = resp;
                });
            }
        }
        // Liste zuruecksetzen, da u.U. im Folgegesuch andere Stammdaten gelten!
        this.ewkResultat = undefined;
        this.activInstitutionenForGemeindeList = undefined;

        this.antragStatusHistoryRS.loadLastStatusChange(this.getGesuch());
        return gesuch;
    }

    /**
     * In dieser Methode wird das Gesuch ersetzt. Das Gesuch ist jetzt private und darf nur ueber diese Methode
     * geaendert werden.
     *
     * @param gesuch das Gesuch. Null und undefined werden erlaubt.
     */
    public setGesuch(gesuch: TSGesuch): TSGesuch {
        this.setGesuchDaten(gesuch);
        this.loadGemeindeStammdaten().then(stammdaten => {
            this.gemeindeStammdaten = stammdaten;
            this.initGemeindeKonfiguration();
        });
        return gesuch;
    }

    public getGesuch(): TSGesuch {
        return this.gesuch;
    }

    public getFall(): TSFall | undefined {
        return this.getGesuch() && this.getGesuch().dossier ? this.getGesuch().dossier.fall : undefined;
    }

    public getDossier(): TSDossier | undefined {
        return this.gesuch ? this.gesuch.dossier : undefined;
    }

    /**
     * Prueft ob der 2. Gesuchtsteller eingetragen werden muss je nach dem was in Familiensituation ausgewaehlt wurde.
     * Wenn es sich um eine Mutation handelt wird nur geschaut ob der 2GS bereits existiert. Wenn ja, dann wird er
     * benoetigt, da bei Mutationen der 2GS nicht geloescht werden darf
     */
    public isGesuchsteller2Required(): boolean {
        if (this.gesuch && this.getFamiliensituation() && this.getFamiliensituation().familienstatus) {
            return this.getFamiliensituation().hasSecondGesuchsteller(this.getGesuchsperiode().gueltigkeit.gueltigBis)
                || !!this.gesuch.gesuchsteller2;
        }

        return false;
    }

    public isLastGesuchsteller(): boolean {
        return this.isGesuchsteller2Required()
            ? this.getGesuchstellerNumber() === 2
            : this.getGesuchstellerNumber() === 1;
    }

    // tslint:disable-next-line:naming-convention
    public isRequiredEKV_GS_BJ(gs: number, bj: number): boolean {
        return gs === 2 ?
            this.getEkvFuerBasisJahrPlus(bj) && this.isGesuchsteller2Required() :
            this.getEkvFuerBasisJahrPlus(bj);

    }

    public getFamiliensituation(): TSFamiliensituation {
        return this.gesuch ? this.gesuch.extractFamiliensituation() : undefined;
    }

    public getFamiliensituationErstgesuch(): TSFamiliensituation {
        return this.gesuch ? this.gesuch.extractFamiliensituationErstgesuch() : undefined;
    }

    /**
     * Loads the Stammdaten of the gemiende of the current Dossier so we can access them
     * while filling out the Gesuch, wihtout having to load it from server again and again
     */
    private loadGemeindeStammdaten(): IPromise<TSGemeindeStammdaten> {
        if (!(this.getDossier() && this.getDossier().gemeinde)) {
            return Promise.resolve(undefined);
        }
        return this.gemeindeRS.getGemeindeStammdaten(this.getDossier().gemeinde.id);
    }

    /**
     * Loads the GemeindeKonfiguration for the current Gesuch, i.e. the current Gemeinde and Gesuchsperiode
     */
    private initGemeindeKonfiguration(): void {
        if (EbeguUtil.isNullOrUndefined(this.gemeindeStammdaten)) {
            return;
        }
        for (const konfigurationsListeElement of this.gemeindeStammdaten.konfigurationsListe) {
            // tslint:disable-next-line:early-exit
            if (konfigurationsListeElement.gesuchsperiode.id === this.getGesuchsperiode().id) {
                this.gemeindeKonfiguration = konfigurationsListeElement;
                this.gemeindeKonfiguration.initProperties();
                return;
            }
        }
    }

    public updateFachstellenAnspruchList(): void {
        this.fachstelleRS.getAnspruchFachstellen().then((response: TSFachstelle[]) => {
            this.fachstellenAnspruchList = response;
        });
    }

    public updateFachstellenErweiterteBetreuungList(): void {
        this.fachstelleRS.getErweiterteBetreuungFachstellen().then((response: TSFachstelle[]) => {
            this.fachstellenErweiterteBetreuungList = response;
        });
    }

    /**
     * Retrieves the list of InstitutionStammdaten for the date of today.
     */
    public updateActiveInstitutionenForGemeindeList(): void {
        if (!this.getGesuchsperiode()) {
            return;
        }
        this.instStamRS.getAllActiveInstitutionStammdatenByGesuchsperiodeAndGemeinde(this.getGesuchsperiode().id,
            this.getGemeinde().id)
            .then((response: TSInstitutionStammdaten[]) => {
                this.activInstitutionenForGemeindeList = response;
            });
    }

    /**
     * Retrieves the InstitutionStammdaten of the unknown Institution
     */
    public getUnknownInstitutionStammdaten(unknownInstitutionStammdatenId: string): IPromise<TSInstitutionStammdaten> {
        return this.instStamRS.findInstitutionStammdaten(unknownInstitutionStammdatenId)
            .then((response: TSInstitutionStammdaten) => {
                return response;
            });
    }

    /**
     * Depending on the value of the parameter creationAction, it creates new Fall, Dossier, Gesuch, Mutation or
     * Folgegesuch
     */
    public createNewAntrag(
        gesuchId: string,
        dossierId: string,
        eingangsart: TSEingangsart,
        gemeindeId: string,
        gesuchsperiodeId: string,
        creationAction: TSCreationAction,
        sozialdienstId: string,
    ): IPromise<TSGesuch> {

        switch (creationAction) {
            case TSCreationAction.CREATE_NEW_FALL:
                return this.gesuchGenerator.initFall(eingangsart, gemeindeId, sozialdienstId, gesuchsperiodeId)
                    .then(gesuch => this.setGesuch(gesuch));

            case TSCreationAction.CREATE_NEW_DOSSIER:
                return this.gesuchGenerator.initDossierForCurrentFall(eingangsart,
                    gemeindeId,
                    this.getFall(),
                    null)
                    .then(gesuch => this.setGesuch(gesuch));

            case TSCreationAction.CREATE_NEW_GESUCH:
                return this.gesuchGenerator.initGesuch(eingangsart,
                    creationAction,
                    gesuchsperiodeId,
                    this.getFall(),
                    this.getDossier(),
                    null)
                    .then(gesuch => this.setGesuch(gesuch));

            case TSCreationAction.CREATE_NEW_FOLGEGESUCH:
                return this.gesuchGenerator.initErneuerungsgesuch(gesuchId,
                    eingangsart,
                    gesuchsperiodeId,
                    dossierId,
                    this.getFall(),
                    this.getDossier())
                    .then(gesuch => this.setGesuch(gesuch));

            case TSCreationAction.CREATE_NEW_MUTATION:
                return this.gesuchGenerator.initMutation(gesuchId,
                    eingangsart,
                    gesuchsperiodeId,
                    dossierId,
                    this.getFall(),
                    this.getDossier())
                    .then(gesuch => this.setGesuch(gesuch));

            default:
                // for no action we return the current Gesuch and log an error
                this.log.error(
                    'No action or an invalid action have been passed. This method must always been called with a valide action',
                    creationAction);

                return Promise.resolve(this.getGesuch());
        }
    }

    /**
     * Wenn das Gesuch schon gespeichert ist (timestampErstellt != null), wird dieses nur aktualisiert. Wenn es sich um
     * ein neues Gesuch handelt dann wird zuerst der Fall erstellt, dieser ins Gesuch kopiert und dann das Gesuch
     * erstellt
     */
    public saveGesuchAndFall(): IPromise<TSGesuch> {
        if (this.gesuch && this.gesuch.timestampErstellt) {
            // Gesuch schon vorhanden
            return this.updateGesuch();
        }

        // Gesuch noch nicht vorhanden
        if (this.gesuch.dossier && !this.gesuch.dossier.isNew()) {
            // Dossier schon vorhaden -> Wir koennen davon ausgehen, dass auch der Fall vorhanden ist
            return this.createNewGesuchForCurrentDossier();
        }
        if (this.gesuch.dossier.fall && !this.gesuch.dossier.fall.isNew()) {
            // Fall ist schon vorhanden
            return this.createNewDossierForCurrentFall();
        }
        return this.createNewFall();
    }

    /**
     * Save the fall contained in the gesuch only
     * the createNewFall call a save method at the end
     */
    public saveFall(): IPromise<TSFall> {
        return this.gesuchGenerator.createNewFall(this.gesuch.dossier.fall)
            .then((fallResponse: TSFall) => {
                this.gesuch.dossier.fall = angular.copy(fallResponse);
                return this.gesuch.dossier.fall;
            });
    }

    /**
     * Creates and saves the fall contained in the gesuch object of the class
     */
    private createNewFall(): IPromise<TSGesuch> {
        return this.gesuchGenerator.createNewFall(this.gesuch.dossier.fall)
            .then((fallResponse: TSFall) => {
                this.gesuch.dossier.fall = angular.copy(fallResponse);
                return this.createNewDossierForCurrentFall();
            });
    }

    /**
     * Creates and saves the dossier contained in the gesuch object of the class. The Fall must exist in the DB
     */
    private createNewDossierForCurrentFall(): IPromise<TSGesuch> {
        return this.gesuchGenerator.createNewDossier(this.gesuch.dossier)
            .then((dossierResponse: TSDossier) => {
                this.gesuch.dossier = angular.copy(dossierResponse);
                return this.createNewGesuchForCurrentDossier();
            });
    }

    /**
     * Creates and saves the gesuch contained in the gesuch object of the class. Dossier and Fall must exist in the DB
     */
    private createNewGesuchForCurrentDossier(): IPromise<TSGesuch> {
        return this.gesuchGenerator.createNewGesuch(this.gesuch).then(gesuchResponse => {
            this.setGesuch(gesuchResponse);
            return this.gesuch;
        });
    }

    public reloadGesuch(): IPromise<TSGesuch> {
        return this.gesuchRS.findGesuch(this.gesuch.id)
            .then(gesuchResponse => this.setGesuch(gesuchResponse));
    }

    public updateGesuch(): IPromise<TSGesuch> {
        return this.gesuchRS.updateGesuch(this.gesuch).then((gesuchResponse: any) => {
            this.gesuch = gesuchResponse;
            this.calculateNewStatus(this.gesuch.status); // just to be sure that the status has been correctly updated

            return this.gesuch;
        });
    }

    /**
     * Speichert den StammdatenToWorkWith.
     */
    public updateGesuchsteller(umzug: boolean): IPromise<TSGesuchstellerContainer> {
        // Da showUmzug nicht im Server gespeichert wird, muessen wir den alten Wert kopieren und nach der
        // Aktualisierung wiedersetzen
        return this.gesuchstellerRS.saveGesuchsteller(this.getStammdatenToWorkWith(),
            this.gesuch.id,
            this.gesuchstellerNumber,
            umzug)
            .then((gesuchstellerResponse: any) => {
                this.setStammdatenToWorkWith(gesuchstellerResponse);

                return this.getStammdatenToWorkWith();
            });
    }

    public saveFinanzielleSituationStart(): IPromise<TSGesuch> {
        // tslint:disable-next-line:no-identical-functions
        return this.finanzielleSituationRS.saveFinanzielleSituationStart(this.gesuch).then(gesuchResponse => {
            this.gesuch = gesuchResponse;

            return this.gesuch;
        });
    }

    public saveFinanzielleSituation(): IPromise<TSFinanzielleSituationContainer> {
        return this.finanzielleSituationRS.saveFinanzielleSituation(
            this.gesuch.id,
            this.getStammdatenToWorkWith())
            .then((finSitContRespo: TSFinanzielleSituationContainer) => {
                this.getStammdatenToWorkWith().finanzielleSituationContainer = finSitContRespo;

                return this.getStammdatenToWorkWith().finanzielleSituationContainer;
            });
    }

    public saveEinkommensverschlechterungContainer(): IPromise<TSEinkommensverschlechterungContainer> {
        return this.einkommensverschlechterungContainerRS.saveEinkommensverschlechterungContainer(
            this.getStammdatenToWorkWith().einkommensverschlechterungContainer,
            this.getStammdatenToWorkWith().id,
            this.gesuch.id)
            .then((ekvContRespo: TSEinkommensverschlechterungContainer) => {
                this.getStammdatenToWorkWith().einkommensverschlechterungContainer = ekvContRespo;

                return this.getStammdatenToWorkWith().einkommensverschlechterungContainer;
            });
    }

    /**
     * Gesuchsteller nummer darf nur 1 oder 2 sein. Wenn die uebergebene Nummer nicht 1 oder 2 ist, wird dann 1 gesetzt
     */
    public setGesuchstellerNumber(gsNumber: number): void {
        this.gesuchstellerNumber = gsNumber === 1 || gsNumber === 2 ? gsNumber : 1;
    }

    /**
     * BasisJahrPlus nummer darf nur 1 oder 2 sein. Wenn die uebergebene Nummer nicht 1 oder 2 ist, wird dann 1 gesetzt
     */
    public setBasisJahrPlusNumber(bjpNumber: number): void {
        this.basisJahrPlusNumber = bjpNumber === 1 || bjpNumber === 2 ? bjpNumber : 1;
    }

    /**
     * Setzt den Kind Index. Dies ist der Index des aktuellen Kindes in der Liste der Kinder
     */
    public setKindIndex(kindIndex: number): void {
        this.kindIndex = kindIndex >= 0 ? kindIndex : 0;
    }

    /**
     * Setzt den BetreuungsIndex.
     */
    public setBetreuungIndex(betreuungIndex: number): void {
        if (betreuungIndex >= 0) {
            this.betreuungIndex = betreuungIndex;
        } else {
            this.setKindIndex(0);
        }
    }

    public convertKindNumberToKindIndex(kindNumber: number): number {
        if (!this.getGesuch()) {
            return -1;
        }
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

    public getFachstellenAnspruchList(): Array<TSFachstelle> {
        if (this.fachstellenAnspruchList === undefined) {
            this.fachstellenAnspruchList = []; // init empty while we wait for promise
            this.updateFachstellenAnspruchList();
        }

        return this.fachstellenAnspruchList;
    }

    public getFachstellenErweiterteBetreuungList(): Array<TSFachstelle> {
        if (this.fachstellenErweiterteBetreuungList === undefined) {
            this.fachstellenErweiterteBetreuungList = []; // init empty while we wait for promise
            this.updateFachstellenErweiterteBetreuungList();
        }

        return this.fachstellenErweiterteBetreuungList;
    }

    public getActiveInstitutionenForGemeindeList(): Array<TSInstitutionStammdaten> {
        if (this.activInstitutionenForGemeindeList === undefined) {
            this.activInstitutionenForGemeindeList = []; // init empty while we wait for promise
            this.updateActiveInstitutionenForGemeindeList();
        }
        return this.activInstitutionenForGemeindeList;
    }

    public resetActiveInstitutionenForGemeindeList(): void {
        // Der Cache muss geloescht werden, damit die Institutionen beim nächsten Aufruf neu geladen werden
        this.globalCacheService.getCache(TSCacheTyp.EBEGU_INSTITUTIONSSTAMMDATEN_GEMEINDE).removeAll();
        this.updateActiveInstitutionenForGemeindeList();
    }

    public getStammdatenToWorkWith(): TSGesuchstellerContainer {
        if (this.gesuch) {
            return this.gesuchstellerNumber === 2 ? this.gesuch.gesuchsteller2 : this.gesuch.gesuchsteller1;
        }
        return undefined;
    }

    public getEkvFuerBasisJahrPlus(basisJahrPlus: number): boolean {
        if (!this.gesuch.extractEinkommensverschlechterungInfo()) {
            this.initEinkommensverschlechterungInfo();
        }

        return basisJahrPlus === 2 ?
            this.gesuch.extractEinkommensverschlechterungInfo().ekvFuerBasisJahrPlus2 :
            this.gesuch.extractEinkommensverschlechterungInfo().ekvFuerBasisJahrPlus1;
    }

    public setStammdatenToWorkWith(gesuchsteller: TSGesuchstellerContainer): TSGesuchstellerContainer {
        if (this.gesuchstellerNumber === 1) {
            this.gesuch.gesuchsteller1 = gesuchsteller;

            return this.gesuch.gesuchsteller1;
        }

        this.gesuch.gesuchsteller2 = gesuchsteller;

        return this.gesuch.gesuchsteller2;
    }

    public initStammdaten(): void {
        if (this.getStammdatenToWorkWith()) {
            return;
        }

        let gesuchsteller: TSGesuchsteller;
        gesuchsteller = new TSGesuchsteller();
        if (this.gesuchstellerNumber === 1 && this.authServiceRS.isOneOfRoles(TSRoleUtil.getGesuchstellerOnlyRoles())) {
            const principal = this.authServiceRS.getPrincipal();
            const name = principal ? principal.nachname : undefined;
            const vorname = principal ? principal.vorname : undefined;
            const email = principal ? principal.email : undefined;
            gesuchsteller.vorname = vorname;
            gesuchsteller.nachname = name;
            gesuchsteller.mail = email;
        }
        if (this.getFall().isSozialdienstFall()) {
            if (this.gesuchstellerNumber === 1) {
                gesuchsteller.vorname = this.getFall().sozialdienstFall.vorname;
                gesuchsteller.nachname = this.getFall().sozialdienstFall.name;
                gesuchsteller.geburtsdatum = this.getFall().sozialdienstFall.geburtsdatum;
            } else {
                gesuchsteller.vorname = this.getFall().sozialdienstFall.vornameGs2;
                gesuchsteller.nachname = this.getFall().sozialdienstFall.nameGs2;
                gesuchsteller.geburtsdatum = this.getFall().sozialdienstFall.geburtsdatumGs2;
            }
        }
        this.setStammdatenToWorkWith(new TSGesuchstellerContainer(gesuchsteller));
        // Nur GS 1 muss die Wohnadresse angeben!
        if (this.gesuchstellerNumber === 1) {
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
     * This method is only used and should be only used in karma test
     */
    public initGesuch(
        eingangsart: TSEingangsart,
        creationAction: TSCreationAction,
        gesuchsperiodeId: string,
    ): IPromise<TSGesuch> {
        return this.gesuchGenerator.initGesuch(eingangsart,
            creationAction,
            gesuchsperiodeId,
            this.getFall(),
            this.getDossier(),
            null,
        )
            .then(gesuch => {
                this.gesuch = gesuch;
                return this.gesuch;
            });
    }

    public initFamiliensituation(): void {
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
     * Gibt das Jahr des Anfangs der Gesuchsperiode minus 1 zurueck. undefined wenn die Gesuchsperiode nicht richtig
     * gesetzt wurde
     */
    public getBasisjahr(): number | undefined {
        if (this.getGesuchsperiodeBegin()) {
            return this.getGesuchsperiodeBegin().year() - 1;
        }

        return undefined;
    }

    /**
     * Gibt das Jahr des Anfangs der Gesuchsperiode minus 1 zurueck. undefined wenn die Gesuchsperiode nicht richtig
     * gesetzt wurde
     */
    public getBasisjahrPlus(plus: number): number | undefined {
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
     */
    public getGesuchsperiode(): TSGesuchsperiode | undefined {
        if (this.gesuch) {
            return this.gesuch.gesuchsperiode;
        }
        return undefined;
    }

    public getGemeinde(): TSGemeinde | undefined {
        if (this.gesuch && this.gesuch.dossier) {
            return this.gesuch.dossier.gemeinde;
        }
        return undefined;
    }

    /**
     * Gibt den Anfang der Gesuchsperiode als Moment zurueck
     */
    public getGesuchsperiodeBegin(): moment.Moment | undefined {
        if (this.getGesuchsperiode() && this.getGesuchsperiode().gueltigkeit) {
            return this.getGesuchsperiode().gueltigkeit.gueltigAb;
        }

        return undefined;
    }

    private initWohnAdresse(): Array<TSAdresseContainer> {
        const wohnAdresseContanier = new TSAdresseContainer();
        const wohnAdresse = new TSAdresse();
        wohnAdresse.adresseTyp = TSAdressetyp.WOHNADRESSE;
        if (this.getFall().isSozialdienstFall()) {
            wohnAdresse.strasse = this.getFall().sozialdienstFall.adresse.strasse;
            wohnAdresse.plz = this.getFall().sozialdienstFall.adresse.plz;
            wohnAdresse.zusatzzeile = this.getFall().sozialdienstFall.adresse.zusatzzeile;
            wohnAdresse.hausnummer = this.getFall().sozialdienstFall.adresse.hausnummer;
            wohnAdresse.ort = this.getFall().sozialdienstFall.adresse.ort;
        }

        wohnAdresseContanier.showDatumVon = false;
        wohnAdresseContanier.adresseJA = wohnAdresse;
        return [wohnAdresseContanier];
    }

    public getKinderList(): Array<TSKindContainer> {
        if (this.gesuch) {
            return this.gesuch.kindContainers;
        }
        return this.emptyKinderList;
    }

    /**
     *
     * @returns Alle KindContainer in denen das Kind Betreuung benoetigt
     */
    public getKinderWithBetreuungList(): Array<TSKindContainer> {
        let listResult: Array<TSKindContainer> = [];
        if (this.gesuch) {
            listResult = this.gesuch.getKinderWithBetreuungList();
        }

        return listResult;
    }

    public saveBetreuung(
        betreuungToSave: TSBetreuung,
        betreuungsstatusNeu: TSBetreuungsstatus,
        abwesenheit: boolean,
    ): IPromise<TSBetreuung> {
        const handleStatus = (betreuungenStatus: TSGesuchBetreuungenStatus, storedBetreuung: TSBetreuung) => {
            this.gesuch.gesuchBetreuungenStatus = betreuungenStatus;

            return this.handleSavedBetreuung(storedBetreuung);
        };

        return this.doSaveBetreuung(betreuungToSave, betreuungsstatusNeu, abwesenheit)
            .then(storedBetreuung => this.gesuchRS.getGesuchBetreuungenStatus(this.gesuch.id)
                .then(betreuungenStatus => handleStatus(betreuungenStatus, storedBetreuung)));
    }

    private doSaveBetreuung(
        betreuungToSave: TSBetreuung,
        betreuungsstatusNeu: TSBetreuungsstatus,
        abwesenheit: boolean,
    ): IPromise<TSBetreuung> {

        switch (betreuungsstatusNeu) {
            case TSBetreuungsstatus.ABGEWIESEN:
                return this.betreuungRS.betreuungsPlatzAbweisen(betreuungToSave, this.gesuch.id);
            case TSBetreuungsstatus.BESTAETIGT:
                return this.betreuungRS.betreuungsPlatzBestaetigen(betreuungToSave, this.gesuch.id);
            case TSBetreuungsstatus.SCHULAMT_MODULE_AKZEPTIERT:
                return this.betreuungRS.anmeldungSchulamtModuleAkzeptiert(betreuungToSave, this.gesuch.id);
            case TSBetreuungsstatus.SCHULAMT_ANMELDUNG_UEBERNOMMEN:
                return this.verfuegungRS.anmeldungUebernehmen(betreuungToSave);
            case TSBetreuungsstatus.SCHULAMT_ANMELDUNG_ABGELEHNT:
                return this.betreuungRS.anmeldungSchulamtAblehnen(betreuungToSave, this.gesuch.id);
            case TSBetreuungsstatus.SCHULAMT_FALSCHE_INSTITUTION:
                return this.betreuungRS.anmeldungSchulamtFalscheInstitution(betreuungToSave, this.gesuch.id);
            case null:
                return this.betreuungRS.saveBetreuung(betreuungToSave, this.gesuch.id, abwesenheit);
            default:
                betreuungToSave.betreuungsstatus = betreuungsstatusNeu;
                return this.betreuungRS.saveBetreuung(betreuungToSave, this.gesuch.id, abwesenheit);
        }
    }

    private handleSavedBetreuung(storedBetreuung: TSBetreuung): TSBetreuung {
        this.getKindFromServer();
        if (!storedBetreuung.isNew()) {   // gespeichertes kind war nicht neu
            const i = EbeguUtil.getIndexOfElementwithID(storedBetreuung, this.getKindToWorkWith().betreuungen);
            if (i >= 0) {
                this.getKindToWorkWith().betreuungen[i] = storedBetreuung;
                this.setBetreuungIndex(i);
            }
        } else {
            this.getKindToWorkWith().betreuungen.push(storedBetreuung);  // neues kind anfuegen
            this.setBetreuungIndex(this.getKindToWorkWith().betreuungen.length - 1);
        }
        this.getCurrentDossierFromServer(); // to reload the verantwortliche that may have changed

        return storedBetreuung;
    }

    public saveKind(kindToSave: TSKindContainer): IPromise<TSKindContainer> {
        return this.kindRS.saveKind(kindToSave, this.gesuch.id)
            .then((storedKindCont: TSKindContainer) => {
                this.getCurrentDossierFromServer();
                if (!kindToSave.isNew()) {   // gespeichertes kind war nicht neu
                    const i = EbeguUtil.getIndexOfElementwithID(kindToSave, this.gesuch.kindContainers);
                    if (i >= 0) {
                        this.gesuch.kindContainers[i] = storedKindCont;
                    }
                } else {
                    this.gesuch.kindContainers.push(storedKindCont);  // neues kind anfuegen
                }

                return storedKindCont;
            });
    }

    /**
     * Sucht das KindToWorkWith im Server und aktualisiert es mit dem bekommenen Daten
     */
    private getKindFromServer(): IPromise<TSKindContainer> {
        return this.kindRS.findKind(this.getKindToWorkWith().id).then(kindResponse => {
            return this.setKindToWorkWith(kindResponse);
        });
    }

    /**
     * Loads the current Dossier from the DB.
     */
    private getCurrentDossierFromServer(): IPromise<TSDossier> {
        return this.dossierRS.findDossier(this.gesuch.dossier.id).then(dossierResponse => {
            this.gesuch.dossier = dossierResponse;

            return this.gesuch.dossier;
        });
    }

    public getKindToWorkWith(): TSKindContainer {
        if (this.gesuch) {
            if (this.gesuch.kindContainers && this.gesuch.kindContainers.length > this.kindIndex) {
                return this.gesuch.kindContainers[this.kindIndex];
            }
            this.log.error('kindContainers is not set or kindIndex is out of bounds', this.kindIndex);
        }

        return undefined;
    }

    /**
     * Sucht im ausgewaehlten Kind (kindIndex) nach der aktuellen Betreuung. Deshalb muessen sowohl
     * kindIndex als auch betreuungNumber bereits gesetzt sein.
     */
    public getBetreuungToWorkWith(): TSBetreuung {
        if (this.getKindToWorkWith()) {
            if (this.getKindToWorkWith().betreuungen.length > this.betreuungIndex) {
                return this.getKindToWorkWith().betreuungen[this.betreuungIndex];
            }
            this.log.error('kindToWorkWith is not set or index of betreuung is out of bounds', this.betreuungIndex);
        }

        return undefined;
    }

    /**
     * Ersetzt das Kind in der aktuelle Position "kindIndex" durch das gegebene Kind. Aus diesem Grund muss diese
     * Methode nur aufgerufen werden, wenn die Position "kindIndex" schon richtig gesetzt wurde.
     */
    public setKindToWorkWith(kind: TSKindContainer): TSKindContainer {
        this.gesuch.kindContainers[this.kindIndex] = kind;

        return this.gesuch.kindContainers[this.kindIndex];
    }

    /**
     * Ersetzt die Betreuung in der aktuelle Position "betreuungIndex" durch die gegebene Betreuung. Aus diesem Grund
     * muss diese Methode nur aufgerufen werden, wenn die Position "betreuungIndex" schon richtig gesetzt wurde.
     */
    public setBetreuungToWorkWith(betreuung: TSBetreuung): TSBetreuung {
        this.getKindToWorkWith().betreuungen[this.betreuungIndex] = betreuung;

        return this.getKindToWorkWith().betreuungen[this.betreuungIndex];
    }

    /**
     * Entfernt das aktuelle Kind von der Liste aber nicht von der DB.
     */
    public removeKindFromList(): void {
        this.gesuch.kindContainers.splice(this.kindIndex, 1);
        this.setKindIndex(undefined); // by default auf undefined setzen
    }

    /**
     * Entfernt die aktuelle Betreuung des aktuellen Kindes von der Liste aber nicht von der DB.
     */
    public removeBetreuungFromKind(): void {
        this.getKindToWorkWith().betreuungen.splice(this.betreuungIndex, 1);
        this.setBetreuungIndex(undefined); // by default auf undefined setzen
        // recalculates the current status because a change in a Betreuung could mean a change in the gesuchstatus, for
        // example when the status was PLATZBESTAETIGUNG_ABGEWIESEN and the declined Platz is removed, the new status
        // should be GEPRUEFT
        this.getGesuch().status = this.calculateNewStatus(this.getGesuch().status);
    }

    public getKindIndex(): number {
        return this.kindIndex;
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
     */
    public findKind(kind: TSKindContainer): number {
        if (this.gesuch.kindContainers.indexOf(kind) >= 0) {
            this.setKindIndex(this.gesuch.kindContainers.indexOf(kind));

            return this.kindIndex;
        }

        return -1;
    }

    /**
     * Sucht das Kind mit der eingegebenen KindID in allen KindContainers des Gesuchs. kindIndex wird gesetzt und
     * zurueckgegeben
     */
    public findKindById(kindID: string): number {
        if (this.gesuch.kindContainers) {
            for (let i = 0; i < this.gesuch.kindContainers.length; i++) {
                // tslint:disable-next-line:early-exit
                if (this.gesuch.kindContainers[i].id === kindID) {
                    this.setKindIndex(i);

                    return this.kindIndex;
                }
            }
        }

        return -1;
    }

    public removeKind(): IPromise<any> {
        return this.kindRS.removeKind(this.getKindToWorkWith().id, this.gesuch.id).then(() => {
            this.removeKindFromList();

            return this.gesuchRS.getGesuchBetreuungenStatus(this.gesuch.id).then(betreuungenStatus => {
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
     * Sucht die Betreuung mit der eingegebenen betreuungID in allen Betreuungen des aktuellen Kind. betreuungIndex
     * wird gesetzt und zurueckgegeben
     */
    public findBetreuungById(betreuungID: string): number {
        const kindToWorkWith = this.getKindToWorkWith();
        if (kindToWorkWith) {
            for (let i = 0; i < kindToWorkWith.betreuungen.length; i++) {
                // tslint:disable-next-line:early-exit
                if (kindToWorkWith.betreuungen[i].id === betreuungID) {
                    this.setBetreuungIndex(i);

                    return this.betreuungIndex;
                }
            }
        }

        return -1;
    }

    public removeBetreuung(): IPromise<void> {
        return this.betreuungRS.removeBetreuung(
            this.getBetreuungToWorkWith().id,
            this.gesuch.id,
        ).then(() => {
            this.removeBetreuungFromKind();
            return this.gesuchRS.getGesuchBetreuungenStatus(this.gesuch.id).then(betreuungenStatus => {
                this.gesuch.gesuchBetreuungenStatus = betreuungenStatus;
                this.kindRS.saveKind(this.getKindToWorkWith(), this.gesuch.id);
            });
        });
    }

    public removeErwerbspensum(pensum: TSErwerbspensumContainer): IPromise<any> {
        let erwerbspensenOfCurrentGS: Array<TSErwerbspensumContainer>;
        erwerbspensenOfCurrentGS = this.getStammdatenToWorkWith().erwerbspensenContainer;
        const index = erwerbspensenOfCurrentGS.indexOf(pensum);
        if (index < 0) {
            this.log.error('can not remove Erwerbspensum since it could not be found in list');
            return this.createDeferPromise<any>();
        }

        const pensumToRemove = this.getStammdatenToWorkWith().erwerbspensenContainer[index];
        if (pensumToRemove.id) { // wenn id vorhanden dann aus der DB loeschen
            return this.erwerbspensumRS.removeErwerbspensum(pensumToRemove.id, this.getGesuch().id)
                .then(() => {
                    erwerbspensenOfCurrentGS.splice(index, 1);
                });
        }

        // sonst nur vom gui wegnehmen
        erwerbspensenOfCurrentGS.splice(index, 1);
        return this.createDeferPromise<any>();
    }

    private createDeferPromise<T>(): IPromise<T> {
        const defer = this.$q.defer<T>();
        defer.resolve();
        return defer.promise;
    }

    public findIndexOfErwerbspensum(gesuchstellerNumber: number, pensum: any): number {
        let gesuchsteller: TSGesuchstellerContainer;
        gesuchsteller = gesuchstellerNumber === 2 ? this.gesuch.gesuchsteller2 : this.gesuch.gesuchsteller1;

        return gesuchsteller.erwerbspensenContainer.indexOf(pensum);
    }

    public saveErwerbspensum(
        gesuchsteller: TSGesuchstellerContainer,
        erwerbspensum: TSErwerbspensumContainer,
    ): IPromise<TSErwerbspensumContainer> {
        if (erwerbspensum.id) {
            return this.erwerbspensumRS.saveErwerbspensum(erwerbspensum, gesuchsteller.id, this.gesuch.id)
                .then((response: TSErwerbspensumContainer) => {

                    const i = EbeguUtil.getIndexOfElementwithID(erwerbspensum, gesuchsteller.erwerbspensenContainer);
                    if (i >= 0) {
                        gesuchsteller.erwerbspensenContainer[i] = erwerbspensum;
                    }
                    return response;
                });
        }

        return this.erwerbspensumRS.saveErwerbspensum(erwerbspensum, gesuchsteller.id, this.gesuch.id)
            .then((storedErwerbspensum: TSErwerbspensumContainer) => {
                gesuchsteller.erwerbspensenContainer.push(storedErwerbspensum);
                return storedErwerbspensum;
            });
    }

    /**
     * Sets the current user as VerantwortlicherTS and saves it in the DB
     */
    public setUserAsFallVerantwortlicherTS(user: TSBenutzerNoDetails): void {
        if (!(this.gesuch && this.gesuch.dossier)) {
            return;
        }

        this.dossierRS.setVerantwortlicherTS(this.gesuch.dossier.id, user ? user.username : null)
            .then(() => {
                this.gesuch.dossier.verantwortlicherTS = user;
            });
    }

    /**
     * Sets the current user as VerantwortlicherBG and saves it in the DB
     */
    public setUserAsFallVerantwortlicherBG(user: TSBenutzerNoDetails): void {
        if (!this.gesuch || !this.gesuch.dossier || !this.gesuch.dossier.id) {
            return;
        }
        this.dossierRS.setVerantwortlicherBG(this.gesuch.dossier.id, user ? user.username : null)
            .then(() => {
                this.gesuch.dossier.verantwortlicherBG = user;
            });
    }

    public getFallVerantwortlicherBG(): TSBenutzerNoDetails {
        return this.gesuch && this.gesuch.dossier ? this.gesuch.dossier.getHauptverantwortlicher() : undefined;
    }

    public getFallVerantwortlicherTS(): TSBenutzerNoDetails {
        return this.gesuch && this.gesuch.dossier ? this.gesuch.dossier.verantwortlicherTS : undefined;
    }

    public calculateVerfuegungen(): IPromise<void> {
        return this.verfuegungRS.calculateVerfuegung(this.gesuch.id)
            .then((response: TSKindContainer[]) => {
                this.updateKinderListWithCalculatedVerfuegungen(response);
            });
    }

    private updateKinderListWithCalculatedVerfuegungen(kinderWithVerfuegungen: TSKindContainer[]): void {
        if (kinderWithVerfuegungen.length !== this.gesuch.kindContainers.length) {
            const msg = `ACHTUNG Ungueltiger Zustand, Anzahl zurueckgelieferter Container ${kinderWithVerfuegungen.length ?
                kinderWithVerfuegungen.length :
                'no_container'} stimmt nicht mit erwarteter ueberein ${this.gesuch.kindContainers.length}`;
            this.log.error(msg);
            const error = new TSExceptionReport(TSErrorType.INTERNAL, TSErrorLevel.SEVERE, msg, kinderWithVerfuegungen);
            this.errorService.addDvbError(error);
        }
        let numOfAssigned = 0;
        this.gesuch.kindContainers.forEach(kindContainer => {
            kinderWithVerfuegungen.forEach(kindContainerVerfuegt => {
                if (kindContainer.id !== kindContainerVerfuegt.id) {
                    return;
                }

                numOfAssigned++;
                for (let k = 0; k < kindContainer.betreuungen.length; k++) {
                    if (kindContainer.betreuungen.length !== kindContainerVerfuegt.betreuungen.length) {
                        const msg = `ACHTUNG unvorhergesehener Zustand. Anzahl Betreuungen eines Kindes stimmt nicht mit der berechneten Anzahl Betreuungen ueberein; erwartet: ${kindContainer.betreuungen.length} erhalten: ${kindContainerVerfuegt.betreuungen.length}`;
                        this.log.error(msg, kindContainer, kindContainerVerfuegt);
                        this.errorService.addMesageAsError(msg);
                    }
                    kindContainer.betreuungen[k] = kindContainerVerfuegt.betreuungen[k];
                }
            });
        });
        if (numOfAssigned !== this.gesuch.kindContainers.length) {
            const msg = 'ACHTUNG unvorhergesehener Zustand. Es konnte nicht jeder calculated Kindcontainer vom Server'
                + ' einem Container auf dem Client zugeordnet werden';
            this.log.error(msg, this.gesuch.kindContainers, kinderWithVerfuegungen);

            this.errorService.addMesageAsError(msg);
        }
        EbeguUtil.handleSmarttablesUpdateBug(this.gesuch.kindContainers);

    }

    public saveVerfuegung(
        ignorieren: boolean,
        ignorierenMahlzeiten: boolean,
        bemerkungen: string,
    ): IPromise<TSVerfuegung> {
        const manuelleBemerkungen = EbeguUtil.isNullOrUndefined(bemerkungen) ? '' : bemerkungen;
        return this.verfuegungRS.saveVerfuegung(
            manuelleBemerkungen,
            this.gesuch.id,
            this.getBetreuungToWorkWith().id,
            ignorieren,
            ignorierenMahlzeiten,
        ).then((response: TSVerfuegung) => {
            this.setVerfuegenToWorkWith(response);
            this.getBetreuungToWorkWith().betreuungsstatus = TSBetreuungsstatus.VERFUEGT;
            this.calculateGesuchStatusVerfuegt();

            return this.getVerfuegenToWorkWith();
        });
    }

    private calculateGesuchStatusVerfuegt(): void {
        if (!this.isThereAnyOpenBetreuung()) {
            this.gesuch.status = this.calculateNewStatus(TSAntragStatus.VERFUEGT);
            this.antragStatusHistoryRS.loadLastStatusChange(this.getGesuch());
        }
    }

    public verfuegungSchliessenOhenVerfuegen(): IPromise<void> {
        return this.verfuegungRS.verfuegungSchliessenOhneVerfuegen(this.gesuch.id,
            this.getBetreuungToWorkWith().id).then(() => {
            this.getBetreuungToWorkWith().betreuungsstatus = TSBetreuungsstatus.GESCHLOSSEN_OHNE_VERFUEGUNG;
            this.calculateGesuchStatusVerfuegt();
        });
    }

    public verfuegungSchliessenNichtEintreten(): IPromise<TSVerfuegung> {
        return this.verfuegungRS.nichtEintreten(
            this.gesuch.id,
            this.getBetreuungToWorkWith().id).then((response: TSVerfuegung) => {
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
        const kinderList = this.getKinderList();
        for (const kind of kinderList) {
            // das kind muss schon gespeichert sein damit es zahelt
            if (kind.kindJA && !kind.kindJA.isNew() && kind.kindJA.familienErgaenzendeBetreuung) {
                return true;
            }
        }

        return false;
    }

    public isThereAnyNotGeprueftesKind(): boolean {
        const kinderList = this.getKinderList();
        if (EbeguUtil.isNotNullOrUndefined(kinderList)) {
            for (const kind of kinderList) {
                // das kind muss schon gespeichert sein damit es zahelt
                if (kind.kindJA && !kind.kindJA.isNew() && !kind.kindJA.isGeprueft()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Gibt true zurueck wenn es mindestens eine Betreuung gibt, dessen Status anders als VERFUEGT oder
     * GESCHLOSSEN_OHNE_VERFUEGUNG oder SCHULAMT ist
     */
    public isThereAnyOpenBetreuung(): boolean {
        const kinderWithBetreuungList = this.getKinderWithBetreuungList();
        for (const kind of kinderWithBetreuungList) {
            for (const betreuung of kind.betreuungen) {
                if (betreuung.betreuungsstatus !== TSBetreuungsstatus.SCHULAMT_FALSCHE_INSTITUTION
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
     */
    public isThereAnyAbgewieseneBetreuung(): boolean {
        const kinderWithBetreuungList = this.getKinderWithBetreuungList();
        for (const kind of kinderWithBetreuungList) {
            for (const betreuung of kind.betreuungen) {
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
        const kinderWithBetreuungList = this.getKinderWithBetreuungList();
        if (kinderWithBetreuungList.length <= 0) {
            return false; // no Kind with bedarf
        }
        for (const kind of kinderWithBetreuungList) {
            for (const betreuung of kind.betreuungen) {
                if (isSchulamt(betreuung.institutionStammdaten.betreuungsangebotTyp)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Setzt den Status des Gesuchs und speichert es in der Datenbank. Anstatt das ganze Gesuch zu schicken, rufen wir
     * den Service auf der den Status aktualisiert und erst wenn das geklappt hat, aktualisieren wir den Status auf dem
     * Client. Wird nur durchgefuehrt, wenn der gegebene Status nicht der aktuelle Status ist
     */
    public saveGesuchStatus(status: TSAntragStatus): IPromise<TSAntragStatus> | undefined {
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
        return this.gesuchRS.antragFreigeben(antragId, usernameJA, usernameSCH).then(response => {
            this.setGesuch(response);

            return response;
        });
    }

    /**
     * Antrag zurueckziehen
     */
    public antragZurueckziehen(antragId: string): IPromise<TSGesuch> {
        // tslint:disable-next-line:no-identical-functions
        return this.gesuchRS.antragZurueckziehen(antragId).then(response => {
            this.setGesuch(response);

            return response;
        });
    }

    /**
     * Returns true if the Gesuch has the given status
     */
    public isGesuchStatus(status: TSAntragStatus): boolean {
        if (!this.gesuch) {
            return undefined;
        }
        return this.gesuch.status === status;
    }

    public isGesuchStatusIn(statuse: TSAntragStatus[]): boolean {
        return this.gesuch ? statuse.includes(this.gesuch.status) : false;
    }

    /**
     * Returns true when the Gesuch must be readonly
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
        const periodeReadonly = this.isGesuchsperiodeReadonly();
        if (this.authServiceRS.isOneOfRoles(TSRoleUtil.getReadOnlyRoles())) {
            return true;  // schulamt hat immer nur readonly zugriff
        }

        if (this.authServiceRS.isOneOfRoles([TSRole.GESUCHSTELLER].concat(TSRoleUtil.getSozialdienstRolle()))) {
            // readonly fuer gs wenn gesuch freigegeben oder weiter
            const gesuchReadonly = !this.getGesuch() || isAtLeastFreigegebenOrFreigabequittung(this.getGesuch().status);
            return gesuchReadonly || periodeReadonly;
        }

        return periodeReadonly;
    }

    public isGesuchsperiodeReadonly(): boolean {
        return this.getGesuch()
            && this.getGesuch().gesuchsperiode
            && (this.getGesuch().gesuchsperiode.status === TSGesuchsperiodeStatus.GESCHLOSSEN);
    }

    /**
     * Wenn das Gesuch Online durch den GS erstellt wurde, nun aber in Bearbeitung beim JA ist, handelt es sich um
     * den Korrekturmodus des Jugendamtes.
     */
    public isKorrekturModusJugendamt(): boolean {
        return this.getGesuch()
            && isAtLeastFreigegeben(this.gesuch.status)
            && !isAnyStatusOfVerfuegt(this.gesuch.status)
            && (TSEingangsart.ONLINE === this.getGesuch().eingangsart);
    }

    /**
     * Einige Status wie GEPRUEFT haben "substatus" auf dem Client die berechnet werden muessen. Aus diesem Grund rufen
     * wir diese Methode auf, bevor wir den Wert setzen.
     */
    public calculateNewStatus(status: TSAntragStatus): TSAntragStatus {
        switch (status) {
            case TSAntragStatus.GEPRUEFT:
            case TSAntragStatus.PLATZBESTAETIGUNG_ABGEWIESEN:
            case TSAntragStatus.PLATZBESTAETIGUNG_WARTEN:
                if (this.wizardStepManager.hasStepGivenStatus(TSWizardStepName.BETREUUNG, TSWizardStepStatus.NOK)) {
                    return this.getGesuch().isThereAnyBetreuung() ?
                        TSAntragStatus.PLATZBESTAETIGUNG_ABGEWIESEN :
                        TSAntragStatus.GEPRUEFT;
                }

                if (this.getGesuch() && this.getGesuch().hasProvisorischeBetreuungen()) {
                    return TSAntragStatus.IN_BEARBEITUNG_GS;
                }

                if (this.wizardStepManager.hasStepGivenStatus(TSWizardStepName.BETREUUNG,
                    TSWizardStepStatus.PLATZBESTAETIGUNG)) {
                    return TSAntragStatus.PLATZBESTAETIGUNG_WARTEN;
                }

                if (this.wizardStepManager.hasStepGivenStatus(TSWizardStepName.BETREUUNG, TSWizardStepStatus.OK)) {
                    return TSAntragStatus.GEPRUEFT;
                }

                return status;
            default:
                return status;
        }
    }

    /**
     * Gibt true zurueck, wenn der Antrag ein Erstgesuch ist. False bekommt man wenn der Antrag eine Mutation ist
     * By default (beim Fehler oder leerem Gesuch) wird auch true zurueckgegeben
     */
    public isGesuch(): boolean {
        return this.gesuch ?
            this.gesuch.typ === TSAntragTyp.ERSTGESUCH || this.gesuch.typ === TSAntragTyp.ERNEUERUNGSGESUCH :
            true;
    }

    /**
     * Aktualisiert alle gegebenen Betreuungen.
     * ACHTUNG. Die Betreuungen muessen existieren damit alles richtig funktioniert
     */
    public updateBetreuungen(
        betreuungenToUpdate: Array<TSBetreuung>,
        saveForAbwesenheit: boolean,
    ): IPromise<Array<TSBetreuung>> {
        if (betreuungenToUpdate && betreuungenToUpdate.length > 0) {
            return this.betreuungRS.saveBetreuungen(betreuungenToUpdate,
                this.gesuch.id,
                saveForAbwesenheit).then((updatedBetreuungen: Array<TSBetreuung>) => {
                // update data of Betreuungen
                this.gesuch.kindContainers.forEach((kindContainer: TSKindContainer) => {
                    for (let i = 0; i < kindContainer.betreuungen.length; i++) {
                        const indexOfUpdatedBetreuung = this.wasBetreuungUpdated(kindContainer.betreuungen[i],
                            updatedBetreuungen);
                        if (indexOfUpdatedBetreuung >= 0) {
                            kindContainer.betreuungen[i] = updatedBetreuungen[indexOfUpdatedBetreuung];
                        }
                    }
                });

                return updatedBetreuungen;
            });
        }

        return this.createDeferPromise<Array<TSBetreuung>>();
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
    public isFinanzielleSituationRequired(): boolean {
        return EbeguUtil.isFinanzielleSituationRequiredForGesuch(this.getGesuch());
    }

    /**
     * gibt true zurueck wenn es keine defaultTagesschule ist oder wenn es eine defaultTagesschule ist aber die
     * Gesuchsperiode noch keine TagesschulenAnmeldung erlaubt.
     *
     * Eine DefaultTagesschule ist eine Tagesschule, die fuer die erste Gescuhsperiode erstellt wurde, damit man
     * Betreuungen der Art TAGESSCHULE erstellen darf. Jede Betreuung muss mit einer Institution verknuepft sein und
     * TagesschuleBetreuungen wurden mit der defaultTagesschule verknuepft. Die DefaultTagesschule wird anhand der ID
     * erkannt.
     */
    public isDefaultTagesschuleAllowed(instStamm: TSInstitutionStammdaten): boolean {
        if (instStamm.id === CONSTANTS.ID_UNKNOWN_INSTITUTION_STAMMDATEN_TAGESSCHULE) {
            return !(this.gemeindeKonfiguration.hasTagesschulenAnmeldung());
        }
        return true;
    }

    /**
     * Ermittelt, ob fuer das Gesuch ein ausserordentlicher Anspruch in Frage kommt
     */
    public showInfoAusserordentlichenAnspruch(): IPromise<boolean> {
        return this.gesuchRS.isAusserordentlicherAnspruchPossible(this.gesuch.id);
    }

    public isTagesschulangebotEnabled(): boolean {
        return this.authServiceRS.hasMandantAngebotTS();
    }

    public isFerieninselangebotEnabled(): boolean {
        return this.authServiceRS.hasMandantAngebotFI();
    }

    public isSozialhilfeBezueger(): boolean {
        return this.getFamiliensituation() && this.getFamiliensituation().sozialhilfeBezueger;
    }

    public isSozialhilfeBezuegerZeitraeumeRequired(): boolean {
        return this.isSozialhilfeBezueger()
            && (this.gemeindeKonfiguration.konfigMahlzeitenverguenstigungEnabled
                || this.gemeindeKonfiguration.konfigZusaetzlicherGutscheinEnabled);
    }

    public isMahlzeitenverguenstigungEnabled(): boolean {
        return this.gemeindeKonfiguration.konfigMahlzeitenverguenstigungEnabled;
    }

    public updateAlwaysEditableProperties(properties: any): IPromise<TSGesuch> {
        return this.gesuchRS.updateAlwaysEditableProperties(properties)
            .then(gesuchResponse => {
                this.setGesuch(gesuchResponse);
                return gesuchResponse;
            });
    }

    public isTagesschuleTagisEnabled(): boolean {
        return this.gemeindeKonfiguration.konfigTagesschuleTagisEnabled;
    }

    public openSozialdienstFall(
        fallId: string,
        gemeindeId: string,
        gesuchId: string,
        gesuchsperiodeId: string,
    ): IPromise<TSGesuch> {
        return this.gesuchGenerator.initSozialdienstFall(fallId, gemeindeId, gesuchId, gesuchsperiodeId)
            .then(gesuch => {
                    this.setGesuch(gesuch);
                    if (!gesuch.isNew()) {
                        return gesuch;
                    }
                    return this.createNewDossierForCurrentFall();
                },
            );
    }
}
