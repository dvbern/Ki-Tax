/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {StateService} from '@uirouter/core';
import {IComponentOptions, IPromise} from 'angular';
import * as $ from 'jquery';
import * as moment from 'moment';
import {map} from 'rxjs/operators';
import {EinstellungRS} from '../../../admin/service/einstellungRS.rest';
import {KiBonMandant, MANDANTS} from '../../../app/core/constants/MANDANTS';
import {UnknownKitaIdVisitor} from '../../../app/core/constants/UnknownKitaIdVisitor';
import {UnknownMittagstischIdVisitor} from '../../../app/core/constants/UnknownMittagstischIdVisitor';
import {UnknownTagesschuleIdVisitor} from '../../../app/core/constants/UnknownTagesschuleIdVisitor';
import {UnknownTFOIdVisitor} from '../../../app/core/constants/UnknownTFOIdVisitor';
import {DvDialog} from '../../../app/core/directive/dv-dialog/dv-dialog';
import {TSDemoFeature} from '../../../app/core/directive/dv-hide-feature/TSDemoFeature';
import {ErrorService} from '../../../app/core/errors/service/ErrorService';
import {LogFactory} from '../../../app/core/logging/LogFactory';
import {ApplicationPropertyRS} from '../../../app/core/rest-services/applicationPropertyRS.rest';
import {MitteilungRS} from '../../../app/core/service/mitteilungRS.rest';
import {MandantService} from '../../../app/shared/services/mandant.service';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {TSAnmeldungMutationZustand} from '../../../models/enums/TSAnmeldungMutationZustand';
import {
    isAnyStatusOfGeprueftVerfuegenVerfuegtOrAbgeschlossenButJA,
    isAnyStatusOfVerfuegt,
    isVerfuegtOrSTV,
    TSAntragStatus
} from '../../../models/enums/TSAntragStatus';
import {
    getTSBetreuungsangebotTypValuesForMandantIfTagesschulanmeldungen,
    isJugendamt,
    TSBetreuungsangebotTyp
} from '../../../models/enums/betreuung/TSBetreuungsangebotTyp';
import {TSBetreuungsstatus} from '../../../models/enums/betreuung/TSBetreuungsstatus';
import {
    stringEingewoehnungTyp,
    TSEingewoehnungTyp
} from '../../../models/enums/TSEingewoehnungTyp';
import {TSEinschulungTyp} from '../../../models/enums/TSEinschulungTyp';
import {TSEinstellungKey} from '../../../models/enums/TSEinstellungKey';
import {TSFachstellenTyp} from '../../../models/enums/TSFachstellenTyp';
import {TSInstitutionStatus} from '../../../models/enums/TSInstitutionStatus';
import {TSPensumAnzeigeTyp} from '../../../models/enums/TSPensumAnzeigeTyp';
import {TSRole} from '../../../models/enums/TSRole';
import {TSWizardStepName} from '../../../models/enums/TSWizardStepName';
import {TSBelegungTagesschule} from '../../../models/TSBelegungTagesschule';
import {TSBetreuung} from '../../../models/TSBetreuung';
import {TSBetreuungsmitteilung} from '../../../models/TSBetreuungsmitteilung';
import {TSBetreuungspensum} from '../../../models/TSBetreuungspensum';
import {TSBetreuungspensumContainer} from '../../../models/TSBetreuungspensumContainer';
import {TSEingewoehnung} from '../../../models/TSEingewoehnung';
import {TSEinstellung} from '../../../models/TSEinstellung';
import {TSErweiterteBetreuung} from '../../../models/TSErweiterteBetreuung';
import {TSErweiterteBetreuungContainer} from '../../../models/TSErweiterteBetreuungContainer';
import {TSExceptionReport} from '../../../models/TSExceptionReport';
import {TSFachstelle} from '../../../models/TSFachstelle';
import {TSInstitutionStammdaten} from '../../../models/TSInstitutionStammdaten';
import {TSInstitutionStammdatenSummary} from '../../../models/TSInstitutionStammdatenSummary';
import {TSKindContainer} from '../../../models/TSKindContainer';
import {TSPublicAppConfig} from '../../../models/TSPublicAppConfig';
import {DateUtil} from '../../../utils/DateUtil';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {OkHtmlDialogController} from '../../dialog/OkHtmlDialogController';
import {RemoveDialogController} from '../../dialog/RemoveDialogController';
import {IBetreuungStateParams} from '../../gesuch.route';
import {BerechnungsManager} from '../../service/berechnungsManager';
import {GesuchModelManager} from '../../service/gesuchModelManager';
import {GlobalCacheService} from '../../service/globalCacheService';
import {WizardStepManager} from '../../service/wizardStepManager';
import {AbstractGesuchViewController} from '../abstractGesuchView';
import {createTSBetreuungspensum} from './betreuungView.util';
import {TSBedarfsstufe} from '../../../models/enums/betreuung/TSBedarfsstufe';
import {ErweiterteBeduerfnisseBestaetigenEinstellungen} from './erweiterte-beduerfnisse-bestaetigung/erweiterte-beduerfnisse-bestaetigung.component';
import ILogService = angular.ILogService;
import IScope = angular.IScope;
import ITimeoutService = angular.ITimeoutService;
import ITranslateService = angular.translate.ITranslateService;

const removeDialogTemplate = require('../../dialog/removeDialogTemplate.html');
const okHtmlDialogTempl = require('../../dialog/okHtmlDialogTemplate.html');

const LOG = LogFactory.createLog('BetreuungViewController');

export class BetreuungViewComponentConfig implements IComponentOptions {
    public transclude = false;
    public template = require('./betreuungView.html');
    public controller = BetreuungViewController;
    public controllerAs = 'vm';
}

const GESUCH_BETREUUNGEN = 'gesuch.betreuungen';
const PENDENZEN_BETREUUNG = 'pendenzenBetreuungen.list-view';
const TAGI_ANGEBOT_VALUE = 'TAGI';

export class BetreuungViewController extends AbstractGesuchViewController<TSBetreuung> {
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
        'MandantService',
        'EbeguRestUtil'
    ];
    public bedarfsstufe: TSBedarfsstufe = null;
    public bedarfsstufeValues: TSBedarfsstufe[] = [
        TSBedarfsstufe.KEINE,
        TSBedarfsstufe.BEDARFSSTUFE_1,
        TSBedarfsstufe.BEDARFSSTUFE_2,
        TSBedarfsstufe.BEDARFSSTUFE_3
    ];
    public betreuungsangebot: any;
    public betreuungsangebotValues: Array<any>;
    // der ausgewaehlte instStamm wird hier gespeichert und dann in die entsprechende
    // InstitutionStammdaten umgewandert
    public instStamm: TSInstitutionStammdatenSummary;
    public isSavingData: boolean; // Semaphore
    public initialBetreuung: TSBetreuung;
    public flagErrorVertrag: boolean;
    public erneutePlatzbestaetigungErforderlich: boolean;
    public kindModel: TSKindContainer;
    public betreuungIndex: number;
    public isMutationsmeldungStatus: boolean;
    public mutationsmeldungModel: TSBetreuung;
    public existingMutationsMeldung: TSBetreuungsmitteilung;
    public isNewestGesuch: boolean;
    public dvDialog: DvDialog;
    public $translate: ITranslateService;
    public aktuellGueltig: boolean = true;
    public isDuplicated: boolean = false;
    // der ausgewaehlte fachstelleId wird hier gespeichert und dann in die entsprechende Fachstelle umgewandert
    public fachstelleId: string;
    public provisorischeBetreuung: boolean;
    public korrekteKostenBestaetigung: boolean = false;
    public isBestaetigenClicked: boolean = false;
    public searchQuery: string = '';
    public allowedRoles: ReadonlyArray<TSRole>;
    public isKesbPlatzierung: boolean;
    public isTFOKostenBerechnungStuendlich: boolean = false;
    public minPensumSprachlicheIndikation: number;
    // felder um aus provisorischer Betreuung ein Betreuungspensum zu erstellen
    public provMonatlicheBetreuungskosten: number;
    public abweichungenAktiviert: boolean;
    public auszahlungAnEltern: boolean;
    public readonly demoFeature = TSDemoFeature.FACHSTELLEN_UEBERGANGSLOESUNG;
    public hoehereBeitraegeWegenBeeintraechtigungBeantragt: boolean = false;
    public isHoehereBeitraegeEinstellungAktiviert: boolean = false;
    public canEditBedarfsstufen: boolean = false;
    protected minEintrittsdatum: moment.Moment;
    private eingewoehnungTyp: TSEingewoehnungTyp = TSEingewoehnungTyp.KEINE;
    private kitaPlusZuschlagAktiviert: boolean = false;
    private fachstellenTyp: TSFachstellenTyp;
    private betreuungspensumAnzeigeTypEinstellung: TSPensumAnzeigeTyp;
    private oeffnungstageKita: number;
    private oeffnungstageTFO: number;
    private oeffnungsstundenTFO: number;
    private kitastundenprotag: number;
    private multiplierKita: number;
    private multiplierTFO: number;
    private hideKesbPlatzierung: boolean;
    private mandant: KiBonMandant;
    private angebotTS: boolean;
    private angebotFI: boolean;
    private angebotTFO: boolean = false;
    private angebotMittagstisch: boolean = false;
    private isLuzern: boolean;
    private sprachfoerderungBestaetigenAktiviert: boolean;
    private schulergaenzendeBetreuungAktiv: boolean = false;
    private erweitereBeduerfnisseAktiv: boolean = false;
    private isAnwesenheitstageProMonatAktiviert: boolean = false;

    erweiterteBeduerfnisseBestaetigungEinstellungen: ErweiterteBeduerfnisseBestaetigenEinstellungen =
        {
            besondereBeduerfnisseAufwandKonfigurierbar: false,
            zuschlagBehinderungProStd: 0,
            zuschlagBehinderungProTag: 0
        };

    public constructor(
        private readonly $state: StateService,
        gesuchModelManager: GesuchModelManager,
        private readonly ebeguUtil: EbeguUtil,
        $scope: IScope,
        berechnungsManager: BerechnungsManager,
        private readonly errorService: ErrorService,
        protected readonly authServiceRS: AuthServiceRS,
        wizardStepManager: WizardStepManager,
        private readonly $stateParams: IBetreuungStateParams,
        private readonly mitteilungRS: MitteilungRS,
        dvDialog: DvDialog,
        private readonly $log: ILogService,
        protected readonly einstellungRS: EinstellungRS,
        private readonly globalCacheService: GlobalCacheService,
        $timeout: ITimeoutService,
        $translate: ITranslateService,
        private readonly applicationPropertyRS: ApplicationPropertyRS,
        private readonly mandantService: MandantService,
        private readonly ebeguRestUtil: EbeguRestUtil
    ) {
        super(
            gesuchModelManager,
            berechnungsManager,
            wizardStepManager,
            $scope,
            TSWizardStepName.BETREUUNG,
            $timeout
        );
        this.dvDialog = dvDialog;
        this.$translate = $translate;
        this.mandantService.mandant$.pipe(map(mandant => mandant)).subscribe(
            mandant => {
                this.mandant = mandant;
            },
            err => LOG.error(err)
        );
    }

    public $onInit(): void {
        super.$onInit();
        this.initAngebotTypenFromEinstellungen()
            .then(() => {
                const gesuchsperiodeId: string =
                    this.gesuchModelManager.getGesuchsperiode().id;
                return this.einstellungRS
                    .getAllEinstellungenBySystemCached(gesuchsperiodeId)
                    .toPromise()
                    .then(
                        (response: TSEinstellung[]) => {
                            response
                                .filter(
                                    r =>
                                        r.key ===
                                        TSEinstellungKey.PENSUM_ANZEIGE_TYP
                                )
                                .forEach(einstellung => {
                                    this.loadPensumAnzeigeTyp(einstellung);
                                });
                            response
                                .filter(
                                    r =>
                                        r.key ===
                                        TSEinstellungKey.FACHSTELLE_MIN_PENSUM_SPRACHLICHE_INTEGRATION
                                )
                                .forEach(value => {
                                    this.minPensumSprachlicheIndikation =
                                        Number(value.value);
                                });
                        },
                        error => LOG.error(error)
                    );
            })
            .then(() => {
                this.mutationsmeldungModel = undefined;
                this.isMutationsmeldungStatus = false;
                const kindNumber = parseInt(this.$stateParams.kindNumber, 10);
                const kindIndex =
                    this.gesuchModelManager.convertKindNumberToKindIndex(
                        kindNumber
                    );

                if (this.mandant === MANDANTS.LUZERN) {
                    this.isTFOKostenBerechnungStuendlich = true;
                }

                if (kindIndex >= 0) {
                    this.gesuchModelManager.setKindIndex(kindIndex);
                    if (
                        this.$stateParams.betreuungNumber &&
                        this.$stateParams.betreuungNumber.length > 0
                    ) {
                        const betreuungNumber = parseInt(
                            this.$stateParams.betreuungNumber,
                            10
                        );
                        this.betreuungIndex =
                            this.gesuchModelManager.convertBetreuungNumberToBetreuungIndex(
                                betreuungNumber
                            );
                        this.model = angular.copy(
                            this.gesuchModelManager.getKindToWorkWith()
                                .betreuungen[this.betreuungIndex]
                        );
                        this.initialBetreuung = angular.copy(
                            this.gesuchModelManager.getKindToWorkWith()
                                .betreuungen[this.betreuungIndex]
                        );

                        this.gesuchModelManager.setBetreuungIndex(
                            this.betreuungIndex
                        );
                    } else {
                        // wenn betreuung-nummer nicht definiert ist heisst das, dass wir ein neues erstellen sollten
                        this.model = this.initEmptyBetreuung();
                        this.initialBetreuung = angular.copy(this.model);
                        this.betreuungIndex =
                            this.gesuchModelManager.getKindToWorkWith()
                                .betreuungen
                                ? this.gesuchModelManager.getKindToWorkWith()
                                      .betreuungen.length
                                : 0;
                        this.gesuchModelManager.setBetreuungIndex(
                            this.betreuungIndex
                        );
                    }

                    this.setBetreuungsangebotTypValues();
                    // Falls ein Typ gesetzt ist, handelt es sich um eine direkt-Anmeldung
                    this.initBetreuungsangebotTyp();
                    this.initViewModel();

                    if (
                        this.getErweiterteBetreuungJA() &&
                        this.getErweiterteBetreuungJA().fachstelle
                    ) {
                        this.fachstelleId =
                            this.getErweiterteBetreuungJA().fachstelle.id;
                    }

                    this.provisorischeBetreuung = false;

                    if (
                        EbeguUtil.isNotNullOrUndefined(
                            this.getBetreuungModel()
                        ) &&
                        this.getBetreuungModel().betreuungsstatus ===
                            TSBetreuungsstatus.UNBEKANNTE_INSTITUTION
                    ) {
                        this.provisorischeBetreuung = true;
                    }

                    // just to read!
                    this.kindModel =
                        this.gesuchModelManager.getKindToWorkWith();
                    this.canRoleEditBedarfsstufe();

                    this.hoehereBeitraegeWegenBeeintraechtigungBeantragt =
                        this.hasHoehereBeitraegeWegenBeeintraechtigungBeantragt(
                            this.kindModel
                        );
                } else {
                    this.$log.error(
                        `There is no kind available with kind-number:${this.$stateParams.kindNumber}`
                    );
                }
                this.isNewestGesuch =
                    this.gesuchModelManager.isNeuestesGesuch();

                if (EbeguUtil.isNotNullOrUndefined(this.getBetreuungModel())) {
                    if (
                        this.getBetreuungModel().getAngebotTyp() ===
                            TSBetreuungsangebotTyp.KITA ||
                        this.getBetreuungModel().getAngebotTyp() ===
                            TSBetreuungsangebotTyp.TAGESFAMILIEN
                    ) {
                        // Falls es Kita oder TFO ist, eine eventuell bereits existierende Betreuungsmitteilung lesen
                        this.findExistingBetreuungsmitteilung();
                    }

                    const anmeldungMutationZustand =
                        this.getBetreuungModel().anmeldungMutationZustand;
                    if (anmeldungMutationZustand) {
                        if (
                            anmeldungMutationZustand ===
                            TSAnmeldungMutationZustand.MUTIERT
                        ) {
                            this.aktuellGueltig = false;
                        } else if (
                            anmeldungMutationZustand ===
                            TSAnmeldungMutationZustand.NOCH_NICHT_FREIGEGEBEN
                        ) {
                            this.aktuellGueltig = false;
                        }
                    }
                }
                this.applicationPropertyRS
                    .getPublicPropertiesCached()
                    .then(res => {
                        this.abweichungenAktiviert = res.abweichungenEnabled;
                    });
                this.initEinstellungen();
            });
    }

    private loadAuszahlungAnEltern(): void {
        if (EbeguUtil.isNotNullOrUndefined(this.auszahlungAnEltern)) {
            // properties wurden bereits geladen
            return;
        }

        this.applicationPropertyRS
            .getPublicPropertiesCached()
            .then((response: TSPublicAppConfig) => {
                this.auszahlungAnEltern = response.auszahlungAnEltern;
            });
    }

    private initBetreuungsangebotTyp() {
        if (this.$stateParams.betreuungsangebotTyp) {
            for (const obj of this.betreuungsangebotValues) {
                if (
                    obj.key === this.$stateParams.betreuungsangebotTyp &&
                    obj.value !==
                        this.ebeguUtil.translateString(TAGI_ANGEBOT_VALUE)
                ) {
                    // Es wurde ein Angebot ueber den Direktlink mitgegeben und dieses ist auch erlaubt
                    // -> wir nehmen alle anderen Angebote aus der Liste raus
                    this.betreuungsangebotValues = new Array<any>();
                    this.betreuungsangebotValues.push(obj);
                    this.betreuungsangebot = obj;
                    this.changedAngebot();
                    return;
                }
            }
        } else {
            this.betreuungsangebot = undefined;
        }

        if (
            !this.hasMandantZusaetzlichesBereuungsangebot() &&
            this.betreuungsangebotValues.length === 1
        ) {
            this.betreuungsangebot = this.betreuungsangebotValues[0];
        }
    }

    private loadPensumAnzeigeTyp(einstellung: TSEinstellung) {
        const einstellungPensumAnzeigeTyp =
            this.ebeguRestUtil.parsePensumAnzeigeTyp(einstellung);

        this.betreuungspensumAnzeigeTypEinstellung =
            EbeguUtil.isNotNullOrUndefined(einstellungPensumAnzeigeTyp)
                ? einstellungPensumAnzeigeTyp
                : TSPensumAnzeigeTyp.ZEITEINHEIT_UND_PROZENT;
    }

    public getBetreuungspensumAnzeigeTyp(): TSPensumAnzeigeTyp {
        if (this.isBetreuungsangebotMittagstisch()) {
            return TSPensumAnzeigeTyp.NUR_MAHLZEITEN;
        }

        return this.betreuungspensumAnzeigeTypEinstellung;
    }

    public changedBedarfsstufe() {
        return this.getBetreuungModel().bedarfsstufe;
    }

    /**
     * Creates a Betreuung for the kind given by the kindNumber attribute of the class.
     * Thus the kindnumber must be set before this method is called.
     */
    public initEmptyBetreuung(): TSBetreuung {
        const tsBetreuung = new TSBetreuung();

        // radio group für vertrag soll zu beginn leer sein falls GS, ansonsten true
        tsBetreuung.vertrag = null;
        if (!this.isGesuchstellerSozialdienst()) {
            tsBetreuung.vertrag = true;
        }
        tsBetreuung.erweiterteBetreuungContainer =
            new TSErweiterteBetreuungContainer();
        tsBetreuung.erweiterteBetreuungContainer.erweiterteBetreuungJA =
            new TSErweiterteBetreuung();
        tsBetreuung.betreuungsstatus = TSBetreuungsstatus.AUSSTEHEND;

        tsBetreuung.kindId = this.gesuchModelManager.getKindToWorkWith().id;
        tsBetreuung.gesuchsperiode =
            this.gesuchModelManager.getGesuchsperiode();

        // sollte defaultmässig true sein, falls AuszahlungAnEltern aktiviert
        this.applicationPropertyRS
            .getPublicPropertiesCached()
            .then((response: TSPublicAppConfig) => {
                tsBetreuung.auszahlungAnEltern = response.auszahlungAnEltern;
                this.auszahlungAnEltern = response.auszahlungAnEltern;
            });

        return tsBetreuung;
    }

    private initViewModel(): void {
        this.isSavingData = false;
        this.flagErrorVertrag = false;
        if (this.getInstitutionSD()) {
            this.instStamm = this.getInstitutionSD();
            this.betreuungsangebot =
                this.getBetreuungsangebotFromInstitutionList();
        }
        this.startEmptyListOfBetreuungspensen();
        // institutionen lazy laden
        if (
            !this.gesuchModelManager.getActiveInstitutionenForGemeindeList() ||
            this.gesuchModelManager.getActiveInstitutionenForGemeindeList()
                .length <= 0
        ) {
            this.gesuchModelManager.updateActiveInstitutionenForGemeindeList();
        }
        if (
            this.getErweiterteBetreuungJA() &&
            this.getErweiterteBetreuungJA().fachstelle
        ) {
            this.fachstelleId = this.getErweiterteBetreuungJA().fachstelle.id;
        }
        this.gesuchModelManager.updateFachstellenErweiterteBetreuungList();
        if (
            this.getErweiterteBetreuungJA() &&
            EbeguUtil.isNotNullOrUndefined(
                this.getErweiterteBetreuungJA().keineKesbPlatzierung
            )
        ) {
            this.isKesbPlatzierung =
                !this.getErweiterteBetreuungJA().keineKesbPlatzierung;
        }
        this.allowedRoles =
            this.TSRoleUtil.getAdminJaSchulamtSozialdienstGesuchstellerRoles();
    }

    /**
     * Fuer Institutionen und Traegerschaften wird es geprueft ob es schon ein Betreuungspensum existiert,
     * wenn nicht wir die Liste dann mit einem leeren initiallisiert
     */
    private startEmptyListOfBetreuungspensen(): void {
        if (
            (!this.getBetreuungspensen() ||
                this.getBetreuungspensen().length === 0) &&
            this.authServiceRS.isOneOfRoles(
                TSRoleUtil.getTraegerschaftInstitutionOnlyRoles()
            )
        ) {
            // nur fuer Institutionen wird ein Betreuungspensum by default erstellt
            this.createBetreuungspensum();
        }
    }

    public getGesuchsperiodeBegin(): moment.Moment {
        return this.gesuchModelManager.getGesuchsperiodeBegin();
    }

    private getBetreuungsangebotFromInstitutionList(): any {
        return $.grep(
            this.betreuungsangebotValues,
            (value: any) =>
                value.key === this.getInstitutionSD().betreuungsangebotTyp
        )[0];
    }

    public getKindModel(): TSKindContainer {
        return this.kindModel;
    }

    public hasHoehereBeitraegeWegenBeeintraechtigungBeantragt(
        kindModel: TSKindContainer
    ): boolean {
        return kindModel.kindJA
            ?.hoehereBeitraegeWegenBeeintraechtigungBeantragen;
    }

    public getBetreuungModel(): TSBetreuung {
        if (this.isMutationsmeldungStatus && this.mutationsmeldungModel) {
            return this.mutationsmeldungModel;
        }
        return this.model;
    }

    public displayBetreuungsPensumChangeWarning(): boolean {
        return this.form.$dirty && this.isMutationsmeldungStatus;
    }

    public changedAngebot(): void {
        if (!this.getBetreuungModel()) {
            return;
        }

        if (this.isSchulamt()) {
            // Fuer saemliche Schulamt-Angebote gilt der Vertrag immer als akzeptiert
            this.getBetreuungModel().vertrag = true;
            this.onChangeVertrag();
            if (
                this.isTagesschule() &&
                this.gesuchModelManager.gemeindeKonfiguration.hasTagesschulenAnmeldung() &&
                this.isTageschulenAnmeldungAktiv()
            ) {
                this.getBetreuungModel().betreuungsstatus =
                    TSBetreuungsstatus.SCHULAMT_ANMELDUNG_ERFASST;
                this.setErsterSchultag();
            }
        } else {
            this.getBetreuungModel().betreuungsstatus =
                TSBetreuungsstatus.AUSSTEHEND;
            if (this.isProvisorischeBetreuung()) {
                this.createProvisorischeBetreuung();
            }
            this.cleanInstitutionStammdaten();
        }
        this.cleanBelegungen();
        this.instStamm = undefined;
    }

    public setErsterSchultag(): void {
        if (
            this.minEintrittsdatum &&
            !this.getBetreuungModel().keineDetailinformationen
        ) {
            if (!this.getBetreuungModel().belegungTagesschule) {
                this.getBetreuungModel().belegungTagesschule =
                    new TSBelegungTagesschule();
            }
            if (
                this.getBetreuungModel().belegungTagesschule.eintrittsdatum ===
                undefined
            ) {
                this.getBetreuungModel().belegungTagesschule.eintrittsdatum =
                    this.minEintrittsdatum;
            }
        }
    }

    private save(
        newStatus: TSBetreuungsstatus,
        nextStep?: string,
        params?: any
    ): void {
        this.isSavingData = true;
        const oldStatus = this.model.betreuungsstatus;
        if (this.getBetreuungModel() && this.isSchulamt()) {
            // fuer Tagesschule werden keine Betreuungspensum benoetigt, deswegen löschen wir sie vor dem Speichern
            this.getBetreuungModel().betreuungspensumContainers = [];
        }
        this.errorService.clearAll();
        this.model.gesuchsperiode = this.gesuchModelManager.getGesuchsperiode();
        if (
            this.getBetreuungModel().institutionStammdaten
                .betreuungsangebotTyp !== this.betreuungsangebot.key
        ) {
            this.errorService.addMesageAsError(
                this.$translate.instant('ERROR_FALSCHE_ANGEBOT')
            );
            return;
        }
        this.gesuchModelManager
            .saveBetreuung(this.model, newStatus, false)
            .then(() => {
                this.gesuchModelManager.setBetreuungToWorkWith(this.model); // setze model
                if (!this.model.isAngebotSchulamt()) {
                    this.gesuchModelManager.updateVerguenstigungGewuenschtFlag();
                }
                this.isSavingData = false;
                this.form.$setPristine();
                if (nextStep) {
                    this.$state.go(nextStep, params);
                }
            })
            .catch((exception: TSExceptionReport[]) => {
                // starting over
                this.$log.error(
                    'there was an error saving the betreuung ',
                    this.model,
                    exception
                );
                if (
                    exception[0].errorCodeEnum === 'ERROR_DUPLICATE_BETREUUNG'
                ) {
                    this.isDuplicated = true;
                    this.model.betreuungsstatus = oldStatus;
                } else {
                    this.isSavingData = false;
                    this.model.betreuungsstatus = oldStatus;
                    this.startEmptyListOfBetreuungspensen();
                    this.form.$setUntouched();
                    this.form.$setPristine();
                    this.model.institutionStammdaten =
                        this.initialBetreuung.institutionStammdaten;
                }

                return undefined;
            });
    }

    /**
     * This method saves the Betreuung as it is and it doesn't trigger any other action except Platzbestätigung has to
     * be done again by the Institution.
     */
    public saveBetreuung(): void {
        if (!this.isGesuchValid()) {
            return;
        }
        if (this.erneutePlatzbestaetigungErforderlich) {
            this.dvDialog
                .showDialog(okHtmlDialogTempl, OkHtmlDialogController, {
                    title: 'ERNEUTE_PLATZBESTAETIGUNG_POPUP_TEXT'
                })
                .then(() => {
                    this.platzAnfordern();
                });
            return;
        }
        this.save(null, GESUCH_BETREUUNGEN, {gesuchId: this.getGesuchId()});
    }

    public anmeldenSchulamt(): void {
        if (this.direktAnmeldenSchulamt()) {
            this.save(
                TSBetreuungsstatus.SCHULAMT_ANMELDUNG_AUSGELOEST,
                GESUCH_BETREUUNGEN,
                {gesuchId: this.getGesuchId()}
            );
        } else {
            this.save(
                TSBetreuungsstatus.SCHULAMT_ANMELDUNG_ERFASST,
                GESUCH_BETREUUNGEN,
                {gesuchId: this.getGesuchId()}
            );
        }
    }

    public direktAnmeldenSchulamt(): boolean {
        // Eigentlich immer ausser in Bearbeitung GS
        return !(
            this.isGesuchInStatus(TSAntragStatus.IN_BEARBEITUNG_GS) ||
            this.isGesuchInStatus(TSAntragStatus.IN_BEARBEITUNG_SOZIALDIENST) ||
            this.isGesuchInStatus(TSAntragStatus.FREIGABEQUITTUNG)
        );
    }

    public enableBetreuungsangebotsTyp(): boolean {
        return (
            this.model &&
            this.model.isNew() &&
            !this.gesuchModelManager.isGesuchReadonly() &&
            this.hasMandantZusaetzlichesBereuungsangebot()
        );
    }

    public showInstitutionenList(): boolean {
        return (
            this.getBetreuungModel() &&
            ((this.isTageschulenAnmeldungAktiv() &&
                (this.isEnabled() ||
                    this.isBetreuungsstatus(
                        TSBetreuungsstatus.SCHULAMT_FALSCHE_INSTITUTION
                    ))) ||
                (!this.isTageschulenAnmeldungAktiv() &&
                    this.isEnabled() &&
                    !this.isTagesschule()) ||
                (this.isFerieninselAnmeldungAktiv() &&
                    (this.isEnabled() ||
                        this.isBetreuungsstatus(
                            TSBetreuungsstatus.SCHULAMT_FALSCHE_INSTITUTION
                        ))) ||
                (!this.isFerieninselAnmeldungAktiv() &&
                    this.isEnabled() &&
                    !this.isFerieninsel())) &&
            !this.getBetreuungModel().keineDetailinformationen
        );
    }

    public showInstitutionenAsText(): boolean {
        return (
            !this.showInstitutionenList() &&
            !this.model.keineDetailinformationen
        );
    }

    public isTageschulenAnmeldungAktiv(): boolean {
        return (
            this.gesuchModelManager.gemeindeKonfiguration &&
            this.gesuchModelManager.gemeindeKonfiguration.isTageschulenAnmeldungAktiv()
        );
    }

    public isFerieninselAnmeldungAktiv(): boolean {
        return (
            this.gesuchModelManager.gemeindeKonfiguration &&
            this.gesuchModelManager.gemeindeKonfiguration.isFerieninselAnmeldungAktiv()
        );
    }

    public isFalscheInstitutionAndUserInRole(): boolean {
        return (
            this.authServiceRS.isOneOfRoles(
                TSRoleUtil.getAdministratorJugendamtSchulamtRoles()
            ) &&
            this.isBetreuungsstatus(
                TSBetreuungsstatus.SCHULAMT_FALSCHE_INSTITUTION
            ) &&
            this.aktuellGueltig
        );
    }

    public anmeldungSchulamtUebernehmen(isScolaris: {
        isScolaris: boolean;
    }): void {
        this.gesuchModelManager.reloadGesuch().then(() => {
            this.copyBGNumberLToClipboard();
            this.dvDialog
                .showRemoveDialog(
                    removeDialogTemplate,
                    this.form,
                    RemoveDialogController,
                    {
                        title: 'CONFIRM_UEBERNAHME_SCHULAMT',
                        deleteText: isScolaris
                            ? 'BESCHREIBUNG_UEBERNAHME_SCHULAMT'
                            : ''
                    }
                )
                .then(() => {
                    let betreuungsstatus: TSBetreuungsstatus;

                    if (
                        this.getBetreuungModel().getAngebotTyp() ===
                        TSBetreuungsangebotTyp.TAGESSCHULE
                    ) {
                        betreuungsstatus =
                            this.anmeldungTagesschuleDirektUebernehmen()
                                ? TSBetreuungsstatus.SCHULAMT_ANMELDUNG_UEBERNOMMEN
                                : TSBetreuungsstatus.SCHULAMT_MODULE_AKZEPTIERT;
                    } else {
                        betreuungsstatus =
                            TSBetreuungsstatus.SCHULAMT_ANMELDUNG_UEBERNOMMEN;
                    }

                    if (
                        this.authServiceRS.isOneOfRoles(
                            TSRoleUtil.getTraegerschaftInstitutionOnlyRoles()
                        )
                    ) {
                        this.save(
                            betreuungsstatus,
                            PENDENZEN_BETREUUNG,
                            undefined
                        );
                    } else {
                        this.save(betreuungsstatus, GESUCH_BETREUUNGEN, {
                            gesuchId: this.getGesuchId()
                        });
                    }
                });
        });
    }

    private anmeldungTagesschuleDirektUebernehmen(): boolean {
        // Falls das Gesuch im Status Verfuegen oder einem Verfuegt-Status ist, soll die Anmeldung
        // beim akzeptieren direkt auf uebernommen gesetzt werden
        // Dasselbe gilt im Falle von KEIN_KONTINTENT, da die Tagesschule-Anmeldungen sonst blockiert sind!
        return (
            this.gesuchModelManager.getGesuch().status ===
                TSAntragStatus.VERFUEGEN ||
            this.gesuchModelManager.getGesuch().status ===
                TSAntragStatus.KEIN_KONTINGENT ||
            isAnyStatusOfVerfuegt(this.gesuchModelManager.getGesuch().status)
        );
    }

    public anmeldungSchulamtAblehnen(): void {
        if (
            this.authServiceRS.isOneOfRoles(
                TSRoleUtil.getTraegerschaftInstitutionOnlyRoles()
            )
        ) {
            this.save(
                TSBetreuungsstatus.SCHULAMT_ANMELDUNG_ABGELEHNT,
                PENDENZEN_BETREUUNG,
                undefined
            );
        } else {
            const params = {gesuchId: this.getGesuchId()};
            this.save(
                TSBetreuungsstatus.SCHULAMT_ANMELDUNG_ABGELEHNT,
                GESUCH_BETREUUNGEN,
                params
            );
        }
    }

    public anmeldungSchulamtFalscheInstitution(): void {
        if (
            this.authServiceRS.isOneOfRoles(
                TSRoleUtil.getTraegerschaftInstitutionOnlyRoles()
            )
        ) {
            this.save(
                TSBetreuungsstatus.SCHULAMT_FALSCHE_INSTITUTION,
                PENDENZEN_BETREUUNG,
                undefined
            );
        } else {
            const params = {gesuchId: this.getGesuchId()};
            this.save(
                TSBetreuungsstatus.SCHULAMT_FALSCHE_INSTITUTION,
                GESUCH_BETREUUNGEN,
                params
            );
        }
    }

    public anmeldungSchulamtStornieren(): void {
        if (
            this.authServiceRS.isOneOfRoles(
                TSRoleUtil.getTraegerschaftInstitutionOnlyRoles()
            )
        ) {
            this.save(
                TSBetreuungsstatus.SCHULAMT_ANMELDUNG_STORNIERT,
                PENDENZEN_BETREUUNG,
                undefined
            );
        } else {
            const params = {gesuchId: this.getGesuchId()};
            this.save(
                TSBetreuungsstatus.SCHULAMT_ANMELDUNG_STORNIERT,
                GESUCH_BETREUUNGEN,
                params
            );
        }
    }

    private copyBGNumberLToClipboard(): void {
        const bgNumber = this.ebeguUtil.calculateBetreuungsIdFromBetreuung(
            this.gesuchModelManager.getFall(),
            this.gesuchModelManager.getDossier().gemeinde,
            this.getBetreuungModel()
        );
        const $temp = $('<input>');
        $('body').append($temp);
        $temp.val(bgNumber).select();
        document.execCommand('copy');
        $temp.remove();
    }

    private setBetreuungsangebotTypValues(): void {
        const betreuungsangebotTypValues =
            getTSBetreuungsangebotTypValuesForMandantIfTagesschulanmeldungen(
                this.angebotTS,
                this.angebotTFO,
                this.angebotMittagstisch,
                this.checkIfGemeindeOrBetreuungHasTSAnmeldung(),
                this.gesuchModelManager.getGemeinde(),
                this.gesuchModelManager.getGesuchsperiode()
            );

        this.betreuungsangebotValues = this.ebeguUtil.translateStringList(
            betreuungsangebotTypValues
        );
        if (!this.gesuchModelManager.isTagesschuleTagisEnabled()) {
            return;
        }
        this.betreuungsangebotValues.push({
            key: TSBetreuungsangebotTyp.TAGESSCHULE,
            value: this.ebeguUtil.translateString(TAGI_ANGEBOT_VALUE)
        });
    }

    public cancel(): void {
        this.reset();
        this.form.$setPristine();
        this.$state.go(GESUCH_BETREUUNGEN, {gesuchId: this.getGesuchId()});
    }

    public reset(): void {
        this.removeBetreuungFromKind(); // wenn model existiert und nicht neu ist wegnehmen, sonst resetten
    }

    public getInstitutionenSDList(): Array<TSInstitutionStammdaten> {
        if (!this.betreuungsangebot) {
            return [];
        }

        let institutionenSDList = this.gesuchModelManager
            .getActiveInstitutionenForGemeindeList()
            .filter(
                instStamm =>
                    instStamm.betreuungsangebotTyp ===
                    this.betreuungsangebot.key
            );

        if (this.betreuungsangebot.key === TSBetreuungsangebotTyp.TAGESSCHULE) {
            institutionenSDList = institutionenSDList.filter(
                instStamm =>
                    instStamm.institution.status !==
                    TSInstitutionStatus.NUR_LATS
            );

            if (
                this.betreuungsangebot.value ===
                this.ebeguUtil.translateString(TAGI_ANGEBOT_VALUE)
            ) {
                institutionenSDList =
                    this.filterTagisTagesschule(institutionenSDList);
            }
        }

        return institutionenSDList;
    }

    private filterTagisTagesschule(
        institutionenList: Array<TSInstitutionStammdaten>
    ): Array<TSInstitutionStammdaten> {
        return institutionenList.filter(instStamm => {
            let isTagi = false;
            instStamm.institutionStammdatenTagesschule.einstellungenTagesschule.forEach(
                einstellungTagesschule => {
                    if (
                        einstellungTagesschule.gesuchsperiode.id ===
                        this.getBetreuungModel().gesuchsperiode.id
                    ) {
                        isTagi = einstellungTagesschule.tagi;
                    }
                }
            );
            return isTagi;
        });
    }

    public getInstitutionSD(): TSInstitutionStammdatenSummary {
        if (this.getBetreuungModel()) {
            return this.getBetreuungModel().institutionStammdaten;
        }

        return undefined;
    }

    public getErweiterteBetreuungJA(): TSErweiterteBetreuung {
        if (
            this.getBetreuungModel() &&
            this.getBetreuungModel().erweiterteBetreuungContainer
        ) {
            return this.getBetreuungModel().erweiterteBetreuungContainer
                .erweiterteBetreuungJA;
        }
        return undefined;
    }

    public getErweiterteBetreuungGS(): TSErweiterteBetreuung {
        if (
            this.getBetreuungModel() &&
            this.getBetreuungModel().erweiterteBetreuungContainer
        ) {
            return this.getBetreuungModel().erweiterteBetreuungContainer
                .erweiterteBetreuungGS;
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
        if (
            this.getBetreuungspensen() &&
            index >= 0 &&
            index < this.getBetreuungspensen().length
        ) {
            return this.getBetreuungspensen()[index];
        }

        return undefined;
    }

    public createBetreuungspensum(): void {
        if (
            this.getBetreuungModel() &&
            (this.getBetreuungspensen() === undefined ||
                this.getBetreuungspensen() === null)
        ) {
            this.getBetreuungModel().betreuungspensumContainers = [];
        }
        const betreuungsangebotTyp = this.getBetreuungsangebot();
        if (!this.getBetreuungModel() || !betreuungsangebotTyp) {
            this.errorService.addMesageAsError(
                'Betreuungsmodel ist nicht initialisiert.'
            );
        }
        const tsBetreuungspensum: TSBetreuungspensum = createTSBetreuungspensum(
            {
                anzeigeEinstellung: this.betreuungspensumAnzeigeTypEinstellung,
                betreuungsangebotTyp,
                instStammdaten: this.instStamm,
                isTFOKostenBerechnungStuendlich:
                    this.isTFOKostenBerechnungStuendlich,
                mahlzeitenverguenstigungActive:
                    this.isMahlzeitenverguenstigungActive()
            }
        );

        this.getBetreuungspensen().push(
            new TSBetreuungspensumContainer(undefined, tsBetreuungspensum)
        );
    }

    public removeBetreuungspensum(
        betreuungspensumToDelete: TSBetreuungspensumContainer
    ): void {
        const position = this.getBetreuungspensen().indexOf(
            betreuungspensumToDelete
        );
        if (position > -1) {
            this.getBetreuungspensen().splice(position, 1);
        }
    }

    public extractInstStammId(): string {
        return this.instStamm.id;
    }

    public setSelectedInstitutionStammdaten(): void {
        if (!this.instStamm) {
            return;
        }
        const instStamList =
            this.gesuchModelManager.getActiveInstitutionenForGemeindeList();
        const found = instStamList.find(
            i => i.id === this.extractInstStammId()
        );
        if (found) {
            this.model.institutionStammdaten = found;
        } else {
            // reset
            this.model.institutionStammdaten = undefined;
            console.error('Institution not found!', this.instStamm.id);
        }
    }

    public angabenKorrigieren(): void {
        this.dvDialog
            .showRemoveDialog(
                removeDialogTemplate,
                undefined,
                RemoveDialogController,
                {
                    title: 'BG_ANMELDUNG_ERNEUT_OEFFNEN',
                    deleteText: '',
                    cancelText: 'LABEL_NEIN',
                    confirmText: 'LABEL_JA'
                }
            )
            .then(() => {
                this.platzAnfordern();
            });
    }

    public platzAnfordern(): void {
        if (this.isGesuchValid() && this.getBetreuungModel().vertrag) {
            this.flagErrorVertrag = false;
            if (this.getErweiterteBetreuungJA().keineKesbPlatzierung) {
                this.save(TSBetreuungsstatus.WARTEN, GESUCH_BETREUUNGEN, {
                    gesuchId: this.getGesuchId()
                });
            } else {
                this.dvDialog
                    .showRemoveDialog(
                        removeDialogTemplate,
                        undefined,
                        RemoveDialogController,
                        {
                            title: 'KEINE_KESB_PLATZIERUNG_POPUP_TEXT',
                            deleteText:
                                'BESCHREIBUNG_KEINE_KESB_PLATZIERUNG_POPUP_TEXT',
                            cancelText: 'LABEL_ABBRECHEN',
                            confirmText: 'LABEL_SPEICHERN'
                        }
                    )
                    .then(() => {
                        // User confirmed removal
                        this.save(
                            TSBetreuungsstatus.WARTEN,
                            GESUCH_BETREUUNGEN,
                            {gesuchId: this.getGesuchId()}
                        );
                    });
            }
        } else if (!this.getBetreuungModel().vertrag) {
            this.flagErrorVertrag = true;
        }
    }

    /**
     * This method saves a provisorische Betreuung and
     * creates a Betreuungspensum for the whole period
     */
    public saveProvisorischeBetreuung(): void {
        if (!this.isGesuchValid()) {
            return;
        }
        if (this.getErweiterteBetreuungJA().keineKesbPlatzierung) {
            this.save(
                TSBetreuungsstatus.UNBEKANNTE_INSTITUTION,
                GESUCH_BETREUUNGEN,
                {gesuchId: this.getGesuchId()}
            );
            return;
        }
        this.dvDialog
            .showRemoveDialog(
                removeDialogTemplate,
                undefined,
                RemoveDialogController,
                {
                    title: 'KEINE_KESB_PLATZIERUNG_POPUP_TEXT',
                    deleteText:
                        'BESCHREIBUNG_KEINE_KESB_PLATZIERUNG_POPUP_TEXT',
                    cancelText: 'LABEL_ABBRECHEN',
                    confirmText: 'LABEL_SPEICHERN'
                }
            )
            .then(() => {
                // User confirmed removal
                this.save(
                    TSBetreuungsstatus.UNBEKANNTE_INSTITUTION,
                    GESUCH_BETREUUNGEN,
                    {gesuchId: this.getGesuchId()}
                );
            });
    }

    public async platzBestaetigen(): Promise<void> {
        this.isBestaetigenClicked = true;
        if (!this.isGesuchValid() || !this.korrekteKostenBestaetigung) {
            return;
        }

        if (
            this.isBetreuungInGemeindeRequired() &&
            !this.getErweiterteBetreuungJA().betreuungInGemeinde
        ) {
            try {
                await this.dvDialog.showRemoveDialog(
                    removeDialogTemplate,
                    undefined,
                    RemoveDialogController,
                    {
                        title: 'BESTAETIGUNG_BETREUUNG_IN_GEMEINDE_POPUP_TEXT',
                        deleteText: 'WOLLEN_SIE_FORTFAHREN',
                        cancelText: 'LABEL_ABBRECHEN',
                        confirmText: 'LABEL_SPEICHERN'
                    }
                );
            } catch {
                return;
            }
        }
        if (
            this.getErweiterteBetreuungJA().kitaPlusZuschlag &&
            !this.getErweiterteBetreuungJA().kitaPlusZuschlagBestaetigt
        ) {
            try {
                await this.dvDialog.showRemoveDialog(
                    removeDialogTemplate,
                    undefined,
                    RemoveDialogController,
                    {
                        title: 'KEINE_KITA_PLUS_BESTAETIGUNG_POPUP_TEXT',
                        deleteText: 'WOLLEN_SIE_FORTFAHREN',
                        cancelText: 'LABEL_ABBRECHEN',
                        confirmText: 'LABEL_SPEICHERN'
                    }
                );
            } catch {
                return;
            }
        }
        this.checkErweiterteBetreuungAndSaveBestaetigung();
    }

    public checkErweiterteBetreuungAndSaveBestaetigung(): void {
        if (
            this.getErweiterteBetreuungJA() &&
            this.getErweiterteBetreuungJA().erweiterteBeduerfnisse &&
            !this.getErweiterteBetreuungJA().erweiterteBeduerfnisseBestaetigt
        ) {
            this.dvDialog
                .showRemoveDialog(
                    removeDialogTemplate,
                    undefined,
                    RemoveDialogController,
                    {
                        title: 'BESTAETIGUNG_AUSSERORDENTLICHER_BETREUUNGSAUFWAND_POPUP_TEXT',
                        deleteText: 'WOLLEN_SIE_FORTFAHREN',
                        cancelText: 'LABEL_ABBRECHEN',
                        confirmText: 'LABEL_SPEICHERN'
                    }
                )
                .then(() => {
                    this.savePlatzBestaetigung();
                });
        } else {
            this.savePlatzBestaetigung();
        }
    }

    public showPensumUnterschrittenCheckBox(): boolean {
        if (
            !this.authServiceRS.isOneOfRoles(
                TSRoleUtil.getGemeindeOrBGRoles().concat(TSRole.SUPER_ADMIN)
            )
        ) {
            return false;
        }

        if (EbeguUtil.isNullOrUndefined(this.getBetreuungspensen())) {
            return false;
        }

        if (!EbeguUtil.hasSprachlicheIndikation(this.getKindModel())) {
            return false;
        }

        return (
            this.getBetreuungspensen().filter(
                pensum =>
                    pensum.betreuungspensumJA?.pensum <
                    this.minPensumSprachlicheIndikation
            ).length > 0
        );
    }

    public showSprachfoerderungBestaetigenCheckBox(): boolean {
        if (!EbeguUtil.hasSprachlicheIndikation(this.getKindModel())) {
            return false;
        }
        if (
            EbeguUtil.isNotNullAndFalse(
                this.sprachfoerderungBestaetigenAktiviert
            )
        ) {
            return false;
        }
        if (this.isBetreuungsstatusAusstehend()) {
            return false;
        }
        if (this.isBetreuungsstatusWarten()) {
            return this.authServiceRS.isOneOfRoles(
                TSRoleUtil.getRolesForBetreuungenView()
            );
        }
        return true;
    }

    public isSprachfoerderungBestaetigenEnabled(): boolean {
        if (
            this.authServiceRS.isOneOfRoles(
                TSRoleUtil.getGesuchstellerOnlyRoles()
            )
        ) {
            return false;
        }

        if (this.isBetreuungsstatusWarten()) {
            return true;
        }

        return this.enableFieldsEditedByGemeinde();
    }

    public isSchulergaezendeBetreuungEnabled(): boolean {
        if (
            this.authServiceRS.isOneOfRoles(
                TSRoleUtil.getGesuchstellerOnlyRoles()
            )
        ) {
            return false;
        }
        return this.isPensumEditable();
    }

    public resetAnspruchFachstelleWennPensumUnterschritten() {
        const unterschritten =
            this.getErweiterteBetreuungJA()
                ?.anspruchFachstelleWennPensumUnterschritten;
        if (!EbeguUtil.isNullOrUndefined(unterschritten) && unterschritten) {
            this.getErweiterteBetreuungJA().anspruchFachstelleWennPensumUnterschritten =
                false;
        }
    }

    private savePlatzBestaetigung(): void {
        this.getBetreuungModel().datumBestaetigung = DateUtil.today();
        this.save(
            TSBetreuungsstatus.BESTAETIGT,
            PENDENZEN_BETREUUNG,
            undefined
        );
        this.isBestaetigenClicked = false;
    }

    /**
     * Wenn ein Betreuungsangebot abgewiesen wird, muss man die neu eingegebenen Betreuungspensen zuruecksetzen, da sie
     * nicht relevant sind. Allerdings muessen der Grund und das Datum der Ablehnung doch gespeichert werden. In diesem
     * Fall machen wir keine Validierung weil die Daten die eingegeben werden muessen, direkt auf dem Server gecheckt
     * werden
     */
    public platzAbweisen(): void {
        // copy values modified by the Institution in initialBetreuung
        this.initialBetreuung.grundAblehnung =
            this.getBetreuungModel().grundAblehnung;
        // restore initialBetreuung
        this.model = angular.copy(this.initialBetreuung);
        this.model.datumAblehnung = DateUtil.today();
        this.save(
            TSBetreuungsstatus.ABGEWIESEN,
            PENDENZEN_BETREUUNG,
            undefined
        );
    }

    public stornieren(): void {
        if (!this.isGesuchValid()) {
            return;
        }

        this.getBetreuungModel().datumBestaetigung = DateUtil.today();
        for (let i = 0; i < this.getBetreuungspensen().length; i++) {
            this.getBetreuungspensum(i).betreuungspensumJA.pensum = 0;
            this.getBetreuungspensum(i).betreuungspensumJA.nichtEingetreten =
                true;
        }

        this.save(TSBetreuungsstatus.STORNIERT, PENDENZEN_BETREUUNG, undefined);
    }

    /**
     * Returns true when the user is allowed to edit the content. This happens when the status is AUSSTEHEHND
     * or SCHULAMT and we are not yet in the KorrekturmodusJugendamt
     */
    public isEnabled(): boolean {
        if (this.isDuplicated) {
            return true;
        }

        if (
            this.isProvisorischeBetreuung() ||
            this.isBetreuungsstatus(TSBetreuungsstatus.UNBEKANNTE_INSTITUTION)
        ) {
            return true;
        }

        if (
            this.getBetreuungModel() &&
            this.getBetreuungModel().betreuungsstatus
        ) {
            return (
                !this.getBetreuungModel().hasVorgaenger() &&
                (this.isBetreuungsstatus(TSBetreuungsstatus.AUSSTEHEND) ||
                    this.isBetreuungsstatus(
                        TSBetreuungsstatus.SCHULAMT_ANMELDUNG_ERFASST
                    )) &&
                !this.isFreigabequittungAusstehend()
            );
        }

        return true;
    }

    public isPensumEditable(): boolean {
        return (
            (this.isBetreuungsstatusWarten() && !this.isSavingData) ||
            this.isMutationsmeldungStatus
        );
    }

    /**
     * Returns true when the Gesuch must be readonly
     */
    public isGesuchReadonly(): boolean {
        if (
            !this.getBetreuungModel() ||
            !this.getBetreuungModel().isAngebotSchulamt()
        ) {
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

    public isBetreuungsstatusBestaetigtOrVerfuegt(): boolean {
        return (
            this.isBetreuungsstatus(TSBetreuungsstatus.BESTAETIGT) ||
            this.isBetreuungsstatus(TSBetreuungsstatus.VERFUEGT)
        );
    }

    public isBetreuungsstatusAusstehend(): boolean {
        return this.isBetreuungsstatus(TSBetreuungsstatus.AUSSTEHEND);
    }

    public isBetreuungsstatusNichtEingetreten(): boolean {
        return this.isBetreuungsstatus(TSBetreuungsstatus.NICHT_EINGETRETEN);
    }

    public isStorniert(): boolean {
        return this.isBetreuungsstatus(TSBetreuungsstatus.STORNIERT);
    }

    public isBetreuungsstatusAusstehendOrUnbekannteInstitution(): boolean {
        return (
            this.isBetreuungsstatus(TSBetreuungsstatus.AUSSTEHEND) ||
            this.isBetreuungsstatus(TSBetreuungsstatus.UNBEKANNTE_INSTITUTION)
        );
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

    public isSchulamt(): boolean {
        return this.isTagesschule() || this.isFerieninsel();
    }

    public isKita(): boolean {
        return this.isBetreuungsangebottyp(TSBetreuungsangebotTyp.KITA);
    }

    private isBetreuungsangebottyp(betAngTyp: TSBetreuungsangebotTyp): boolean {
        if (this.betreuungsangebot) {
            return (
                this.betreuungsangebot.key === TSBetreuungsangebotTyp[betAngTyp]
            );
        }

        return false;
    }

    /**
     * Erweiterte Beduerfnisse wird nur beim Institutionen oder Traegerschaften eingeblendet oder wenn das Feld schon
     * als true gesetzt ist.
     */
    public showErweiterteBeduerfnisse(): boolean {
        const showErweiterteBeduerfnisse =
            this.authServiceRS.isOneOfRoles(
                TSRoleUtil.getTraegerschaftInstitutionRoles()
            ) ||
            this.authServiceRS.isOneOfRoles(
                TSRoleUtil.getAdminJaSchulamtSozialdienstGesuchstellerRoles()
            ) ||
            (this.getBetreuungModel().erweiterteBetreuungContainer
                .erweiterteBetreuungJA &&
                this.getBetreuungModel().erweiterteBetreuungContainer
                    .erweiterteBetreuungJA.erweiterteBeduerfnisse);
        return showErweiterteBeduerfnisse && this.erweitereBeduerfnisseAktiv;
    }

    public showKitaPlusZuschlag(): boolean {
        return this.kitaPlusZuschlagAktiviert;
    }

    public showFalscheAngaben(): boolean {
        return (
            (this.isBetreuungsstatusBestaetigt() ||
                this.isBetreuungsstatusAbgewiesen()) &&
            !this.isGesuchReadonly() &&
            !this.isFromMutation()
        );
    }

    public showAngabenKorrigieren(): boolean {
        return (
            (this.isBetreuungsstatusBestaetigt() ||
                this.isBetreuungsstatusAbgewiesen() ||
                this.isBetreuungsstatusStorniert()) &&
            !this.isGesuchReadonly() &&
            this.isFromMutation()
        );
    }

    public isFromMutation(): boolean {
        return (
            this.getBetreuungModel() && !!this.getBetreuungModel().vorgaengerId
        );
    }

    public showAngabeKorrigieren(): boolean {
        return (
            (this.isBetreuungsstatusBestaetigt() ||
                this.isBetreuungsstatusAbgewiesen()) &&
            !this.isGesuchReadonly() &&
            this.isFromMutation()
        );
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
        return super.isMutationsmeldungAllowed(
            this.getBetreuungModel(),
            this.isNewestGesuch
        );
    }

    public preMutationsmeldungSenden(): void {
        // send mutationsmeldung (dummy copy)
        if (!(this.isGesuchValid() && this.mutationsmeldungModel)) {
            return;
        }

        if (this.showExistingBetreuungsmitteilungInfoBox()) {
            this.dvDialog
                .showRemoveDialog(
                    removeDialogTemplate,
                    this.form,
                    RemoveDialogController,
                    {
                        title: 'MUTATIONSMELDUNG_OVERRIDE_EXISTING_TITLE',
                        deleteText: 'MUTATIONSMELDUNG_OVERRIDE_EXISTING_BODY',
                        parentController: undefined,
                        elementID: undefined
                    }
                )
                .then(() => {
                    // User confirmed removal
                    this.mutationsmeldungSenden();
                });
        } else {
            this.dvDialog
                .showRemoveDialog(
                    removeDialogTemplate,
                    this.form,
                    RemoveDialogController,
                    {
                        title: 'MUTATIONSMELDUNG_CONFIRMATION',
                        deleteText: 'MUTATIONSMELDUNG_BESCHREIBUNG',
                        parentController: undefined,
                        elementID: undefined
                    }
                )
                .then(() => {
                    this.mutationsmeldungSenden();
                });
        }
    }

    public mutationsmeldungSenden(): void {
        this.mitteilungRS
            .sendbetreuungsmitteilung(
                this.gesuchModelManager.getDossier(),
                this.mutationsmeldungModel
            )
            .then(() => {
                this.form.$setUntouched();
                this.form.$setPristine();
                // reset values. is needed??????
                this.isMutationsmeldungStatus = false;
                this.mutationsmeldungModel = undefined;
                this.$state.go(GESUCH_BETREUUNGEN, {
                    gesuchId: this.getGesuchId()
                });
            })
            .catch(err => {
                const outsideInstiGueltigkeitError = err.find(
                    (error: any) =>
                        error._argumentList
                            .toLowerCase()
                            .includes('institution') &&
                        (error._argumentList.includes('liegen ausserhalb') ||
                            error._argumentList.includes(
                                'Les dates de prise en charge ne sont pas comprises'
                            ))
                );

                if (outsideInstiGueltigkeitError) {
                    this.errorService.addMesageAsError(
                        outsideInstiGueltigkeitError._argumentList
                    );
                } else {
                    // We don't know exactly the cause, because it gets lost in the httpinterceptor. We have to use a
                    // general message
                    this.errorService.addMesageAsError(
                        this.$translate.instant('ERROR_COULD_NOT_SAVE')
                    );
                }
            });
    }

    /**
     * Prueft dass das Objekt existingMutationsMeldung existiert und dass es ein sentDatum hat. Das wird gebraucht,
     * um zu vermeiden, dass ein leeres Objekt als gueltiges Objekt erkannt wird.
     * Ausserdem muss die Meldung nicht applied sein und nicht den Status ERLEDIGT haben
     */
    public showExistingBetreuungsmitteilungInfoBox(): boolean {
        return (
            this.existingMutationsMeldung !== undefined &&
            this.existingMutationsMeldung !== null &&
            this.existingMutationsMeldung.sentDatum !== undefined &&
            this.existingMutationsMeldung.sentDatum !== null &&
            !this.existingMutationsMeldung.applied &&
            !this.existingMutationsMeldung.isErledigt()
        );
    }

    public getDatumLastBetreuungsmitteilung(): string {
        if (this.showExistingBetreuungsmitteilungInfoBox()) {
            return DateUtil.momentToLocalDateFormat(
                this.existingMutationsMeldung.sentDatum,
                'DD.MM.YYYY'
            );
        }

        return '';
    }

    public getTimeLastBetreuungsmitteilung(): string {
        if (this.showExistingBetreuungsmitteilungInfoBox()) {
            return DateUtil.momentToLocalDateTimeFormat(
                this.existingMutationsMeldung.sentDatum,
                'HH:mm'
            );
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
    private findExistingBetreuungsmitteilung(): void {
        if (
            EbeguUtil.isNullOrUndefined(this.getBetreuungModel()) ||
            EbeguUtil.isNullOrUndefined(
                isJugendamt(this.getBetreuungModel().getAngebotTyp())
            )
        ) {
            return;
        }
        if (
            !(
                !this.getBetreuungModel().isNew() &&
                this.authServiceRS.isOneOfRoles(
                    TSRoleUtil.getTraegerschaftInstitutionRoles()
                )
            )
        ) {
            return;
        }
        this.mitteilungRS
            .getNewestBetreuungsmitteilung(this.getBetreuungModel().id)
            .then((response: TSBetreuungsmitteilung) => {
                this.existingMutationsMeldung = response;
            });
    }

    public tageschuleSaveDisabled(): boolean {
        if (this.getBetreuungModel().isNew()) {
            return (
                (this.isTagesschule() &&
                    this.gesuchModelManager.gemeindeKonfiguration.hasTagesschulenAnmeldung() &&
                    !this.gesuchModelManager.gemeindeKonfiguration.isTageschulenAnmeldungAktiv()) ||
                (this.isFerieninsel() && !this.getBetreuungModel().isEnabled())
            );
        }
        return true;
    }

    /**
     * Die globale navigation Buttons werden nur angezeigt, wenn es  kein Schulamtangebot ist oder wenn beim
     * Tagesschulangebot die Periode keine Tagesschuleanmeldung definiert hat.
     */
    public displayGlobalNavigationButtons(): boolean {
        return (
            !this.isSchulamt() ||
            (this.isTagesschule() &&
                !this.checkIfGemeindeOrBetreuungHasTSAnmeldung())
        );
    }

    /**
     * Die Felder fuer die Module muessen nur angezeigt werden wenn es Tagesschule ist oder status=SCHULAMT,
     * das letzte um die alten Betreuungen zu unterstuetzen.
     */
    public displayModuleTagesschule(): boolean {
        return (
            this.isTagesschule() &&
            this.checkIfGemeindeOrBetreuungHasTSAnmeldung()
        );
    }

    public showEingewoehnungPeriode(): boolean {
        if (this.isSchulamt()) {
            return false;
        }

        switch (this.eingewoehnungTyp) {
            case TSEingewoehnungTyp.KEINE:
                return false;
            case TSEingewoehnungTyp.FKJV:
                return this.showEingewohenungPeriodeFKJV();
            case TSEingewoehnungTyp.LUZERN:
                return true;
            case TSEingewoehnungTyp.PAUSCHALE:
                return false;
            default: {
                const errorMsg = `not implemented eingewoehnungTyp ${this.eingewoehnungTyp}`;
                LOG.error(errorMsg);
                throw new Error(errorMsg);
            }
        }
    }

    private showEingewohenungPeriodeFKJV(): boolean {
        if (this.isBetreuungsstatusAusstehend()) {
            return false;
        }
        if (this.isBetreuungsstatusWarten()) {
            return this.authServiceRS.isOneOfRoles(
                TSRoleUtil.getTraegerschaftInstitutionRoles()
            );
        }
        return true;
    }

    public showEingewoehnungKosten(): boolean {
        return this.eingewoehnungTyp === TSEingewoehnungTyp.PAUSCHALE;
    }

    public isEingewoehnungPeriodeEnabled(): boolean {
        if (this.isGesuchReadonly()) {
            return false;
        }
        if (this.eingewoehnungTyp === TSEingewoehnungTyp.FKJV) {
            // bei FKJV darf nur die Institution die Checkbox bearbeiten
            return (
                this.authServiceRS.isOneOfRoles(
                    TSRoleUtil.getTraegerschaftInstitutionRoles()
                ) && this.isBetreuungsstatusWarten()
            );
        }
        if (this.eingewoehnungTyp === TSEingewoehnungTyp.LUZERN) {
            // bei luzern immer editierbar, falls das Gesuch nicht readonly ist.
            return true;
        }
        return false;
    }

    public isEingewoehnungKostenEnabled(): boolean {
        if (!this.isPensumEditable()) {
            return false;
        }

        return this.authServiceRS.isOneOfRoles(
            TSRoleUtil.getTraegerschaftInstitutionRoles()
        );
    }

    public onEingewoehnungKostenChange(betreuungspensumIndex: number): void {
        const pensumToUse = this.getBetreuungspensum(
            betreuungspensumIndex
        ).betreuungspensumJA;

        if (pensumToUse.hasEingewoehnung) {
            pensumToUse.eingewoehnung = new TSEingewoehnung();
        } else {
            pensumToUse.eingewoehnung = null;
        }
    }

    private checkIfGemeindeOrBetreuungHasTSAnmeldung(): boolean {
        const gemeindeKonfiguration =
            this.gesuchModelManager.gemeindeKonfiguration;
        const gmdeHasTS = gemeindeKonfiguration
            ? gemeindeKonfiguration.hasTagesschulenAnmeldung()
            : false;
        const isNew =
            this.getBetreuungModel() && this.getBetreuungModel().isNew();
        if (!isNew) {
            const betreuung = this.gesuchModelManager.getBetreuungToWorkWith();
            const betreuungIsTS = betreuung
                ? betreuung.isAngebotTagesschule()
                : false;
            return gmdeHasTS || betreuungIsTS;
        }
        return gmdeHasTS;
    }

    /**
     * Based on the type of the Angebot it resets the belegungen.
     */
    private cleanBelegungen(): void {
        if (!this.betreuungsangebot) {
            return;
        }
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

    public enableFieldsEditedByGemeinde(): boolean {
        if (this.isDuplicated) {
            return true;
        }
        if (!this.gesuchModelManager.getGesuch() || this.isGesuchReadonly()) {
            return false;
        }
        const gesuchsteller = this.authServiceRS.isOneOfRoles(
            TSRoleUtil.getGesuchstellerOnlyRoles()
        );
        const gemeindeUser = this.authServiceRS.isOneOfRoles(
            TSRoleUtil.getAdministratorOrAmtOrSozialdienstRolle()
        );
        return (
            !this.isSavingData &&
            this.gesuchModelManager.getGesuch() &&
            !isVerfuegtOrSTV(this.gesuchModelManager.getGesuch().status) &&
            (gesuchsteller || gemeindeUser)
        );
    }

    /**
     * Schulamt-Angebote ändern erst beim Einlesen der Freigabequittung den Zustand von SCHULAMT_ANMELDUNG_ERFASST zu
     * SCHULAMT_ANMELDUNG_AUSGELOEST. Betreuungen in Gesuchen im Zustand FREIGABEQUITTUNG dürfen jedoch nicht editiert
     * werden. Deshalb braucht es diese Funktion.
     */
    public isFreigabequittungAusstehend(): boolean {
        if (
            this.gesuchModelManager.getGesuch() &&
            this.gesuchModelManager.getGesuch().status
        ) {
            return (
                this.gesuchModelManager.getGesuch().status ===
                TSAntragStatus.FREIGABEQUITTUNG
            );
        }

        return false;
    }

    public keineDetailAnmeldungClicked(): void {
        // clear
        this.getBetreuungModel().betreuungspensumContainers = [];
        this.cleanInstitutionStammdaten();
        this.instStamm = null;
        this.provisorischeBetreuung = false;

        if (this.getBetreuungModel().keineDetailinformationen) {
            // Fuer Tagesschule setzen wir eine Dummy-Tagesschule als Institution
            this.instStamm = new TSInstitutionStammdatenSummary();
            this.instStamm.id = new UnknownTagesschuleIdVisitor().process(
                this.mandant
            );
            this.getBetreuungModel().vertrag = false;
            this.provisorischeBetreuung = true;
            this.createProvisorischeBetreuung();
        } else {
            this.getBetreuungModel().vertrag = true;
            this.instStamm = undefined;
            this.searchQuery = null;
            this.getBetreuungModel().institutionStammdaten = undefined;
            // Im Falle von "nicht mehr keine Detailinfos" muss die Belegung wieder initialisiert werden
            if (this.isTagesschule()) {
                this.getBetreuungModel().belegungTagesschule =
                    new TSBelegungTagesschule();
            }
        }
    }

    public isFachstelleRequired(): boolean {
        return (
            EbeguUtil.isNotNullOrUndefined(this.getErweiterteBetreuungJA()) &&
            EbeguUtil.isNotNullAndTrue(
                this.getErweiterteBetreuungJA().erweiterteBeduerfnisse
            ) &&
            EbeguUtil.isNotNullAndTrue(
                this.getBetreuungModel().isAngebotBetreuungsgutschein()
            )
        );
    }

    public isKitaPlus(): boolean {
        return (
            EbeguUtil.isNotNullOrUndefined(this.getErweiterteBetreuungJA()) &&
            EbeguUtil.isNotNullAndTrue(
                this.getErweiterteBetreuungJA().kitaPlusZuschlag
            )
        );
    }

    public isBesondereBeduerfnisseAufwandVisible(): boolean {
        if (!this.isBesondereBeduerfnisseAufwandKonfigurierbar()) {
            return false;
        }
        // für Institutionen und Trägerschaften ist der Betrag readonly. Deshalb soll er erst sichtbar sein,
        // wenn er durch die Gemeinde ausgefüllt wurde
        if (
            this.authServiceRS.isOneOfRoles(
                TSRoleUtil.getTraegerschaftInstitutionOnlyRoles()
            )
        ) {
            return EbeguUtil.isNotNullOrUndefined(
                this.getErweiterteBetreuungJA().erweitereteBeduerfnisseBetrag
            );
        }
        return true;
    }

    private isBesondereBeduerfnisseAufwandKonfigurierbar(): boolean {
        return this.erweiterteBeduerfnisseBestaetigungEinstellungen
            .besondereBeduerfnisseAufwandKonfigurierbar;
    }

    public isBetreuungInGemeindeRequired(): boolean {
        return (
            EbeguUtil.isNotNullOrUndefined(this.getErweiterteBetreuungJA()) &&
            !this.isSchulamt() &&
            this.gesuchModelManager.gemeindeKonfiguration
                .konfigZusaetzlicherGutscheinEnabled
        );
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
        if (
            this.getErweiterteBetreuungGS() &&
            this.getErweiterteBetreuungGS().erweiterteBeduerfnisse &&
            this.getErweiterteBetreuungJA() &&
            !this.getErweiterteBetreuungJA().erweiterteBeduerfnisse
        ) {
            return this.$translate.instant(
                this.getErweiterteBetreuungGS().fachstelle.name.toLocaleString()
            );
        }
        return this.$translate.instant('LABEL_KEINE_ANGABE');
    }

    private createProvisorischeBetreuung(): void {
        // always clear existing Betreuungspensum
        this.getBetreuungModel().betreuungspensumContainers = [];
        // Die unbekannte Institution ermitteln und lesen
        this.setUnbekannteInstitutionAccordingToAngebot();
        this.gesuchModelManager
            .getUnknownInstitutionStammdaten(this.instStamm.id)
            .then((stammdaten: TSInstitutionStammdaten) => {
                this.getBetreuungModel().institutionStammdaten = stammdaten;
            });
        // Gegebenenfalls ein Pensum zur freien Eingabe inititalisieren
        if (!this.getBetreuungModel().keineDetailinformationen) {
            this.createBetreuungspensum();
        }
    }

    public isProvisorischeBetreuung(): boolean {
        return (
            this.provisorischeBetreuung ||
            (this.getBetreuungModel() &&
                this.getBetreuungModel().keineDetailinformationen)
        );
    }

    private setUnbekannteInstitutionAccordingToAngebot(): void {
        /* eslint-disable */
        this.instStamm = new TSInstitutionStammdatenSummary();
        switch (this.betreuungsangebot?.key) {
            case TSBetreuungsangebotTyp.TAGESFAMILIEN:
                this.instStamm.id = new UnknownTFOIdVisitor().process(
                    this.mandant
                );
                break;
            case TSBetreuungsangebotTyp.TAGESSCHULE:
                this.instStamm.id = new UnknownTagesschuleIdVisitor().process(
                    this.mandant
                );
                break;
            case TSBetreuungsangebotTyp.MITTAGSTISCH:
                this.instStamm.id = new UnknownMittagstischIdVisitor().process(
                    this.mandant
                );
                break;
            case TSBetreuungsangebotTyp.KITA:
                this.instStamm.id = new UnknownKitaIdVisitor().process(
                    this.mandant
                );
                break;
            default:
                throw new Error(
                    'Unbekannte Institution nicht implementiert für Angebottyp ' +
                        this.betreuungsangebot.key
                );
        }
    }

    public onChangeVertrag(): void {
        // clear
        this.getBetreuungModel().betreuungspensumContainers = [];
        this.cleanInstitutionStammdaten();
        this.instStamm = null;
        this.provisorischeBetreuung = false;

        // init prov. betreuung
        if (this.model.vertrag === false) {
            this.provisorischeBetreuung = true;
            this.createProvisorischeBetreuung();
        }
    }

    public isGesuchstellerSozialdienst(): boolean {
        return this.authServiceRS.isOneOfRoles(
            TSRoleUtil.getSozialdienstRolle().concat(TSRole.GESUCHSTELLER)
        );
    }

    public getBetreuungInGemeindeLabel(): string {
        if (
            EbeguUtil.isNullOrUndefined(this.gesuchModelManager.getGemeinde())
        ) {
            return '';
        }
        return this.$translate.instant('BETREUUNG_IN_GEMEINDE', {
            gemeinde: this.gesuchModelManager.getGemeinde().name
        });
    }

    public getErweiterteBeduerfnisseBestaetigtLabel(): string {
        if (
            this.erweiterteBeduerfnisseBestaetigungEinstellungen
                .besondereBeduerfnisseAufwandKonfigurierbar
        ) {
            return this.$translate.instant(
                'BESTAETIGUNG_AUSSERORDENTLICHER_BETREUUNGSAUFWAND_INST_WITHOUT_BETRAG'
            );
        }

        if (
            this.getBetreuungModel() &&
            this.getBetreuungModel().getAngebotTyp() ===
                TSBetreuungsangebotTyp.TAGESFAMILIEN
        ) {
            return this.$translate.instant(
                'BESTAETIGUNG_AUSSERORDENTLICHER_BETREUUNGSAUFWAND_INST_WITH_FIX_BETRAG',
                {
                    betrag: this.erweiterteBeduerfnisseBestaetigungEinstellungen
                        .zuschlagBehinderungProStd,
                    einheit: this.$translate.instant('STUNDE')
                }
            );
        }

        return this.$translate.instant(
            'BESTAETIGUNG_AUSSERORDENTLICHER_BETREUUNGSAUFWAND_INST_WITH_FIX_BETRAG',
            {
                betrag: this.erweiterteBeduerfnisseBestaetigungEinstellungen
                    .zuschlagBehinderungProTag,
                einheit: this.$translate.instant('TAG')
            }
        );
    }

    public isBestaetigungBesondereBeduerfnisseEnabled(): boolean {
        return (
            this.isBetreuungsstatusWarten() &&
            this.authServiceRS.isOneOfRoles(
                TSRoleUtil.getTraegerschaftInstitutionRoles()
            )
        );
    }

    public changedBesondereBeduerfnisse(): void {
        const betreuung = this.getBetreuungModel();
        const erweiterteBetreuung = this.getErweiterteBetreuungJA();

        this.erneutePlatzbestaetigungErforderlich =
            betreuung.betreuungsstatus === TSBetreuungsstatus.BESTAETIGT &&
            erweiterteBetreuung.erweiterteBeduerfnisse &&
            !erweiterteBetreuung.erweiterteBeduerfnisseBestaetigt;

        // reset erweiterteBeduerfnisseBetrag on change from true to false
        if (!erweiterteBetreuung.erweiterteBeduerfnisse) {
            erweiterteBetreuung.erweitereteBeduerfnisseBetrag = null;
        }
    }

    public gotoBetreuungAbweichungen(): void {
        this.$state.go('gesuch.abweichungen', {
            gesuchId: this.gesuchModelManager.getGesuch().id,
            betreuungNumber: this.$stateParams.betreuungNumber,
            kindNumber: this.$stateParams.kindNumber
        });
    }

    public querySearch(query: string): Array<TSInstitutionStammdaten> {
        if (!query) {
            return this.getInstitutionenSDList();
        }
        const searchString = query.toLocaleLowerCase();
        return this.getInstitutionenSDList().filter(
            item =>
                item.institution.name
                    .toLocaleLowerCase()
                    .indexOf(searchString) > -1 ||
                item.adresse.ort.toLocaleLowerCase().indexOf(searchString) >
                    -1 ||
                item.adresse.plz.toLocaleLowerCase().indexOf(searchString) >
                    -1 ||
                item.adresse.strasse.toLocaleLowerCase().indexOf(searchString) >
                    -1
        );
    }

    public isAnmeldungTSEditable(): boolean {
        return (
            !this.isFreigabequittungAusstehend() &&
            (this.getBetreuungModel().isBetreuungsstatus(
                TSBetreuungsstatus.SCHULAMT_ANMELDUNG_ERFASST
            ) ||
                ((this.getBetreuungModel().isBetreuungsstatus(
                    TSBetreuungsstatus.SCHULAMT_ANMELDUNG_AUSGELOEST
                ) ||
                    this.getBetreuungModel().isBetreuungsstatus(
                        TSBetreuungsstatus.SCHULAMT_FALSCHE_INSTITUTION
                    )) &&
                    this.authServiceRS.isOneOfRoles(
                        TSRoleUtil.getSchulamtInstitutionRoles()
                    )))
        );
    }

    public isMahlzeitenverguenstigungActive(): boolean {
        return this.gesuchModelManager.gemeindeKonfiguration
            .konfigMahlzeitenverguenstigungEnabled;
    }

    // die Meldung soll angezeigt werden, wenn eine Mutationsmeldung gemacht wird,
    // oder wenn die Gemeinde die Angaben in einer Mutation über "falsche Angaben" korrigiert
    // ausserdem soll die Meldung nicht gezeigt werden, wenn ein neues Betreuungspensum hinzugefügt wird
    public showOverrideWarning(betreuungspensum: TSBetreuungspensum): boolean {
        return (
            !betreuungspensum.isNew() &&
            (this.isMutationsmeldungStatus || this.isMutation())
        );
    }

    public isInstitutionMobileSelection(): boolean {
        return (
            'none' ===
            document.getElementById('institution_search').style.display
        );
    }

    public anmeldungSchulamtFalscheAngaben(): void {
        // Wir muessen sicher sein dass es keine offene und noch nicht freigegebene Mutation fuer dieser Gesuch gibt
        this.gesuchModelManager.checkIfGesuchIsNeustes().then(response => {
            if (!response) {
                this.errorService.addMesageAsError(
                    this.$translate.instant('ERROR_DATA_CHANGED')
                );
                return;
            }
            this.dvDialog
                .showRemoveDialog(
                    removeDialogTemplate,
                    undefined,
                    RemoveDialogController,
                    {
                        title: 'TS_ANMELDUNG_ERNEUT_OEFFNEN',
                        deleteText: '',
                        cancelText: 'LABEL_ABBRECHEN',
                        confirmText: 'LABEL_SPEICHERN'
                    }
                )
                .then(() => {
                    this.save(TSBetreuungsstatus.SCHULAMT_ANMELDUNG_AUSGELOEST);
                });
        });
    }

    public isStammdatenAusgefuellt(): boolean {
        return (
            this.instStamm.institution.status !== TSInstitutionStatus.EINGELADEN
        );
    }

    public showWarningStammdaten(): boolean {
        return (
            this.authServiceRS.isOneOfRoles(
                TSRoleUtil.getTraegerschaftInstitutionRoles()
            ) && !this.isStammdatenAusgefuellt()
        );
    }

    public changeKeineKesbPlatzierung(): void {
        this.getErweiterteBetreuungJA().keineKesbPlatzierung =
            !this.isKesbPlatzierung;
    }

    public showAbrechnungGutscheine(): boolean {
        return (
            !this.isSavingData &&
            (this.isBetreuungsstatusWarten() ||
                this.isBetreuungsstatus(TSBetreuungsstatus.AUSSTEHEND))
        );
    }

    public isFachstellenTypLuzern(): boolean {
        return this.fachstellenTyp === TSFachstellenTyp.LUZERN;
    }

    public getMultiplierKita(): number {
        if (EbeguUtil.isNullOrUndefined(this.multiplierKita)) {
            this.calculateMuliplyerKita();
        }

        return this.multiplierKita;
    }

    public getMultiplierTFO(): number {
        if (EbeguUtil.isNullOrUndefined(this.multiplierTFO)) {
            this.calculateMultiplierTFO();
        }

        return this.multiplierTFO;
    }

    private calculateMuliplyerKita(): void {
        if (
            this.betreuungspensumAnzeigeTypEinstellung ===
            TSPensumAnzeigeTyp.NUR_STUNDEN
        ) {
            this.multiplierKita =
                (this.oeffnungstageKita * this.kitastundenprotag) / 12 / 100;
            return;
        }
        // Beispiel: 240 Tage Pro Jahr: 240 / 12 = 20 Tage Pro Monat. 100% = 20 days => 1% = 0.2 tage
        this.multiplierKita = this.oeffnungstageKita / 12 / 100;
    }

    private calculateMultiplierTFO(): void {
        // Beispiel: 240 Tage Pro Jahr, 11 Stunden pro Tag: 240 * 11 / 12 = 220 Stunden Pro Monat.
        // 100% = 220 stunden => 1% = 2.2 stunden
        this.multiplierTFO =
            (this.oeffnungstageTFO * this.oeffnungsstundenTFO) / 12 / 100;
    }

    public showBetreuungsPensumInput(): boolean {
        if (this.isBetreuungsangebotMittagstisch()) {
            return false;
        }

        if (!this.isBetreuungsangebotTagesfamilie()) {
            return true;
        }

        return !this.isTFOKostenBerechnungStuendlich;
    }

    public showBetreuungsKostenInput(): boolean {
        return this.showBetreuungsPensumInput();
    }

    public showStuendlicheKostenInput(): boolean {
        if (!this.isBetreuungsangebotTagesfamilie()) {
            return false;
        }

        return this.isTFOKostenBerechnungStuendlich;
    }

    private isBetreuungsangebotTagesfamilie(): boolean {
        return (
            this.getBetreuungsangebot() === TSBetreuungsangebotTyp.TAGESFAMILIEN
        );
    }

    public isBetreuungsangebotMittagstisch(): boolean {
        return (
            this.getBetreuungsangebot() === TSBetreuungsangebotTyp.MITTAGSTISCH
        );
    }

    private getBetreuungsangebot(): TSBetreuungsangebotTyp | undefined {
        return this.betreuungsangebot?.key;
    }

    private showHintUntermonatlich(): boolean {
        return (
            this.getBetreuungspensen()?.length > 0 &&
            this.mandant !== MANDANTS.LUZERN
        );
    }

    private showHintEingewoehnung(): boolean {
        return (
            this.eingewoehnungTyp === TSEingewoehnungTyp.LUZERN &&
            !this.authServiceRS.isOneOfRoles(
                TSRoleUtil.getGesuchstellerSozialdienstRolle()
            )
        );
    }

    public showAuszahlungAnInstituion(): boolean {
        return !this.getBetreuungModel().auszahlungAnEltern;
    }

    public onChangeAbrechnungGutscheine(): void {
        this.getBetreuungModel().begruendungAuszahlungAnInstitution = null;
    }

    public getInstitutionNotFoundHint(): string {
        return this.$translate.instant('INSTITUTION_NOT_FOUND_HINT');
    }

    private initAngebotTypenFromEinstellungen(): IPromise<void> {
        return this.applicationPropertyRS
            .getPublicPropertiesCached()
            .then(res => {
                this.angebotTS = res.angebotTSActivated;
                this.angebotFI = res.angebotFIActivated;
                this.angebotMittagstisch = res.angebotMittagstischActivated;
                //wenn TFO aktiv on mandant then check if tfo is activ on gemeinde
                if (res.angebotTFOActivated) {
                    this.angebotTFO =
                        this.gesuchModelManager.getGemeinde().angebotBGTFO;
                }
            });
    }

    public hasMandantZusaetzlichesBereuungsangebot(): boolean {
        return (
            this.angebotTS ||
            this.angebotFI ||
            this.angebotTFO ||
            this.angebotMittagstisch
        );
    }

    public getEingewoehnungLabel(): string {
        if (this.eingewoehnungTyp === TSEingewoehnungTyp.FKJV) {
            return this.$translate.instant('EINGEWOEHNUNG_FKJV');
        }
        return this.$translate.instant('EINGEWOEHNUNG');
    }

    public showSchulergaezendeBetreuungFrage(): boolean {
        return (
            this.schulergaenzendeBetreuungAktiv &&
            this.kindModel.kindJA.einschulungTyp !==
                TSEinschulungTyp.VORSCHULALTER &&
            this.isBetreuungsangebotTypForShulergaezendeBetreuung()
        );
    }

    private isBetreuungsangebotTypForShulergaezendeBetreuung(): boolean {
        return (
            this.isBetreuungsangebottyp(TSBetreuungsangebotTyp.TAGESFAMILIEN) ||
            this.isBetreuungsangebottyp(TSBetreuungsangebotTyp.KITA)
        );
    }

    public showAnwesenheitstageProMonatInput(): boolean {
        return (
            this.isBetreuungsangebotTagesfamilie() &&
            this.isAnwesenheitstageProMonatAktiviert
        );
    }

    public getMonatlicheBetreuungkostenKey(): string {
        if (this.model.isAngebotTagesfamilien()) {
            return 'MONATLICHE_BETREUUNGSKOSTEN_TFO';
        }
        return 'MONATLICHE_BETREUUNGSKOSTEN';
    }

    public getMonatlicheBetreuungkostenHelpKey(): string {
        if (this.model.isAngebotTagesfamilien()) {
            return 'MONATLICHE_BETREUUNGSKOSTEN_TFO_HELP';
        }
        return 'MONATLICHE_BETREUUNGSKOSTEN_HELP';
    }

    public canRoleEditBedarfsstufe() {
        this.canEditBedarfsstufen =
            (this.authServiceRS.isOneOfRoles(
                TSRoleUtil.getGemeindeOrBGRoles()
            ) ||
                this.authServiceRS.isOneOfRoles(
                    TSRoleUtil.getSuperAdminRoles()
                )) &&
            !isAnyStatusOfGeprueftVerfuegenVerfuegtOrAbgeschlossenButJA(
                this.getGesuch()?.status
            );
    }

    // die Meldung soll angezeigt werden, wenn eine Mutationsmeldung gemacht wird,
    // oder wenn die Gemeinde die Angaben in einer Mutation über "falsche Angaben" korrigiert

    private initEinstellungen(): void {
        this.loadAuszahlungAnEltern();
        const gesuchsperiodeId: string =
            this.gesuchModelManager.getGesuchsperiode().id;
        const gemeindeId: string = this.gesuchModelManager.getGemeinde().id;
        this.einstellungRS
            .getAllEinstellungenBySystemCached(gesuchsperiodeId)
            .subscribe(
                (response: TSEinstellung[]) => {
                    response
                        .filter(
                            r => r.key === TSEinstellungKey.EINGEWOEHNUNG_TYP
                        )
                        .forEach(value => {
                            this.eingewoehnungTyp = stringEingewoehnungTyp(
                                value.value
                            );
                        });
                    response
                        .filter(
                            r =>
                                r.key ===
                                TSEinstellungKey.KITAPLUS_ZUSCHLAG_AKTIVIERT
                        )
                        .forEach(value => {
                            this.kitaPlusZuschlagAktiviert =
                                value.getValueAsBoolean();
                        });
                    response
                        .filter(
                            r =>
                                r.key ===
                                TSEinstellungKey.KESB_PLATZIERUNG_DEAKTIVIEREN
                        )
                        .forEach(value => {
                            if (
                                EbeguUtil.isNotNullAndTrue(
                                    value.getValueAsBoolean()
                                )
                            ) {
                                this.isKesbPlatzierung = false;
                                this.changeKeineKesbPlatzierung();
                            }
                            this.hideKesbPlatzierung =
                                value.getValueAsBoolean();
                        });
                    response
                        .filter(
                            r =>
                                r.key ===
                                TSEinstellungKey.BESONDERE_BEDUERFNISSE_LUZERN
                        )
                        .forEach(value => {
                            if (
                                EbeguUtil.isNotNullAndTrue(
                                    value.getValueAsBoolean()
                                )
                            ) {
                                this.erweiterteBeduerfnisseBestaetigungEinstellungen =
                                    {
                                        ...this
                                            .erweiterteBeduerfnisseBestaetigungEinstellungen,
                                        besondereBeduerfnisseAufwandKonfigurierbar:
                                            true
                                    };
                            }
                        });
                    response
                        .filter(r => r.key === TSEinstellungKey.FACHSTELLEN_TYP)
                        .forEach(einstellung => {
                            this.fachstellenTyp =
                                this.ebeguRestUtil.parseFachstellenTyp(
                                    einstellung.value
                                );
                        });

                    response
                        .filter(
                            r => r.key === TSEinstellungKey.OEFFNUNGSTAGE_KITA
                        )
                        .forEach(einstellung => {
                            this.oeffnungstageKita = parseInt(
                                einstellung.value,
                                10
                            );
                        });
                    response
                        .filter(
                            r => r.key === TSEinstellungKey.OEFFNUNGSTAGE_TFO
                        )
                        .forEach(einstellung => {
                            this.oeffnungstageTFO = parseInt(
                                einstellung.value,
                                10
                            );
                        });
                    response
                        .filter(
                            r => r.key === TSEinstellungKey.OEFFNUNGSSTUNDEN_TFO
                        )
                        .forEach(einstellung => {
                            this.oeffnungsstundenTFO = parseInt(
                                einstellung.value,
                                10
                            );
                        });
                    response
                        .filter(
                            r => r.key === TSEinstellungKey.KITA_STUNDEN_PRO_TAG
                        )
                        .forEach(einstellung => {
                            this.kitastundenprotag = parseInt(
                                einstellung.value,
                                10
                            );
                        });
                    response
                        .filter(
                            r =>
                                r.key ===
                                TSEinstellungKey.SPRACHFOERDERUNG_BESTAETIGEN
                        )
                        .forEach(einstellung => {
                            this.sprachfoerderungBestaetigenAktiviert =
                                einstellung.getValueAsBoolean();
                        });
                    response
                        .filter(
                            r =>
                                r.key ===
                                TSEinstellungKey.SCHULERGAENZENDE_BETREUUNGEN
                        )
                        .forEach(value => {
                            if (
                                EbeguUtil.isNotNullAndTrue(
                                    value.getValueAsBoolean()
                                )
                            ) {
                                this.schulergaenzendeBetreuungAktiv = true;
                            }
                        });
                    response
                        .filter(
                            r =>
                                r.key ===
                                TSEinstellungKey.ERWEITERTE_BEDUERFNISSE_AKTIV
                        )
                        .forEach(value => {
                            if (
                                EbeguUtil.isNotNullAndTrue(
                                    value.getValueAsBoolean()
                                )
                            ) {
                                this.erweitereBeduerfnisseAktiv = true;
                            }
                        });
                    response
                        .filter(
                            r =>
                                r.key ===
                                TSEinstellungKey.ANWESENHEITSTAGE_PRO_MONAT_AKTIVIERT
                        )
                        .forEach(value => {
                            this.isAnwesenheitstageProMonatAktiviert =
                                EbeguUtil.isNotNullAndTrue(
                                    value.getValueAsBoolean()
                                );
                        });
                },
                error => LOG.error(error)
            );

        this.einstellungRS
            .findEinstellung(
                TSEinstellungKey.ZUSCHLAG_BEHINDERUNG_PRO_TG,
                gemeindeId,
                gesuchsperiodeId
            )
            .subscribe(
                res => {
                    this.erweiterteBeduerfnisseBestaetigungEinstellungen = {
                        ...this.erweiterteBeduerfnisseBestaetigungEinstellungen,
                        zuschlagBehinderungProTag: Number(res.value)
                    };
                },
                error => LOG.error(error)
            );

        this.einstellungRS
            .findEinstellung(
                TSEinstellungKey.ZUSCHLAG_BEHINDERUNG_PRO_STD,
                gemeindeId,
                gesuchsperiodeId
            )
            .subscribe(
                res => {
                    this.erweiterteBeduerfnisseBestaetigungEinstellungen = {
                        ...this.erweiterteBeduerfnisseBestaetigungEinstellungen,
                        zuschlagBehinderungProStd: Number(res.value)
                    };
                },
                error => LOG.error(error)
            );

        this.einstellungRS
            .findEinstellung(
                TSEinstellungKey.HOEHERE_BEITRAEGE_BEEINTRAECHTIGUNG_AKTIVIERT,
                gemeindeId,
                gesuchsperiodeId
            )
            .subscribe(res => {
                this.isHoehereBeitraegeEinstellungAktiviert =
                    EbeguUtil.getBoolean(res.value);
            });
    }

    private removeBetreuungFromKind(): void {
        if (this.model && !this.model.timestampErstellt) {
            // wenn die Betreeung noch nicht erstellt wurde, loeschen wir die Betreuung vom Array
            this.gesuchModelManager.removeBetreuungFromKind();
        }
    }
}
