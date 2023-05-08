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
import {map} from 'rxjs/operators';
import {EinstellungRS} from '../../../admin/service/einstellungRS.rest';
import {CONSTANTS, MAX_FILE_SIZE} from '../../../app/core/constants/CONSTANTS';
import {MANDANTS} from '../../../app/core/constants/MANDANTS';
import {TSDemoFeature} from '../../../app/core/directive/dv-hide-feature/TSDemoFeature';
import {ErrorService} from '../../../app/core/errors/service/ErrorService';
import {LogFactory} from '../../../app/core/logging/LogFactory';
import {ApplicationPropertyRS} from '../../../app/core/rest-services/applicationPropertyRS.rest';
import {DemoFeatureRS} from '../../../app/core/service/demoFeatureRS.rest';
import {DownloadRS} from '../../../app/core/service/downloadRS.rest';
import {EwkRS} from '../../../app/core/service/ewkRS.rest';
import {UploadRS} from '../../../app/core/service/uploadRS.rest';
import {MandantService} from '../../../app/shared/services/mandant.service';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {TSDokumenteDTO} from '../../../models/dto/TSDokumenteDTO';
import {TSAdressetyp} from '../../../models/enums/TSAdressetyp';
import {TSDokumentGrundTyp} from '../../../models/enums/TSDokumentGrundTyp';
import {TSDokumentTyp} from '../../../models/enums/TSDokumentTyp';
import {TSEingangsart} from '../../../models/enums/TSEingangsart';
import {TSEinstellungKey} from '../../../models/enums/TSEinstellungKey';
import {TSFamilienstatus} from '../../../models/enums/TSFamilienstatus';
import {TSGeschlecht} from '../../../models/enums/TSGeschlecht';
import {TSGesuchstellerKardinalitaet} from '../../../models/enums/TSGesuchstellerKardinalitaet';
import {TSRole} from '../../../models/enums/TSRole';
import {getTSSpracheValues, TSSprache} from '../../../models/enums/TSSprache';
import {TSUnterhaltsvereinbarungAnswer} from '../../../models/enums/TSUnterhaltsvereinbarungAnswer';
import {TSWizardStepName} from '../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../models/enums/TSWizardStepStatus';
import {TSSozialdienstFallDokument} from '../../../models/sozialdienst/TSSozialdienstFallDokument';
import {TSAdresse} from '../../../models/TSAdresse';
import {TSAdresseContainer} from '../../../models/TSAdresseContainer';
import {TSDokument} from '../../../models/TSDokument';
import {TSDokumentGrund} from '../../../models/TSDokumentGrund';
import {TSDownloadFile} from '../../../models/TSDownloadFile';
import {TSFamiliensituation} from '../../../models/TSFamiliensituation';
import {TSGesuchsteller} from '../../../models/TSGesuchsteller';
import {TSGesuchstellerContainer} from '../../../models/TSGesuchstellerContainer';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {EnumEx} from '../../../utils/EnumEx';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {IStammdatenStateParams} from '../../gesuch.route';
import {BerechnungsManager} from '../../service/berechnungsManager';
import {DokumenteRS} from '../../service/dokumenteRS.rest';
import {GesuchModelManager} from '../../service/gesuchModelManager';
import {WizardStepManager} from '../../service/wizardStepManager';
import {AbstractGesuchViewController} from '../abstractGesuchView';
import IPromise = angular.IPromise;
import IQService = angular.IQService;
import IRootScopeService = angular.IRootScopeService;
import IScope = angular.IScope;
import ITimeoutService = angular.ITimeoutService;
import ITranslateService = angular.translate.ITranslateService;

const LOG = LogFactory.createLog('StammdatenViewController');

export class StammdatenViewComponentConfig implements IComponentOptions {
    public transclude = false;
    public bindings = {};
    public template = require('./stammdatenView.html');
    public controller = StammdatenViewController;
    public controllerAs = 'vm';
}

export class StammdatenViewController extends AbstractGesuchViewController<TSGesuchstellerContainer> {

    public static $inject = [
        '$stateParams',
        'EbeguRestUtil',
        'GesuchModelManager',
        'BerechnungsManager',
        'ErrorService',
        'WizardStepManager',
        '$q',
        '$scope',
        '$translate',
        'AuthServiceRS',
        '$rootScope',
        'EwkRS',
        '$timeout',
        'EinstellungRS',
        'UploadRS',
        'DownloadRS',
        'ApplicationPropertyRS',
        'DokumenteRS',
        'MandantService',
        'DemoFeatureRS'
    ];

    public filesTooBig: File[];
    public dokumentGrund: TSDokumentGrund;

    public readonly CONSTANTS: any = CONSTANTS;
    public geschlechter: Array<string>;
    public showKorrespondadr: boolean;
    public showKorrespondadrGS: boolean;
    public showRechnungsadr: boolean;
    public showRechnungsadrGS: boolean;
    public allowedRoles: ReadonlyArray<TSRole>;
    public gesuchstellerNumber: number;
    private isLastVerfuegtesGesuch: boolean = false;
    private diplomatenStatusDisabled: boolean;
    public ausweisNachweisRequiredEinstellung: boolean;
    public dvFileUploadError: object;
    public frenchEnabled: boolean;
    private isLuzern: boolean;
    private demoFeature2754: boolean = false;

    public constructor(
        $stateParams: IStammdatenStateParams,
        public readonly ebeguRestUtil: EbeguRestUtil,
        gesuchModelManager: GesuchModelManager,
        berechnungsManager: BerechnungsManager,
        private readonly errorService: ErrorService,
        wizardStepManager: WizardStepManager,
        private readonly $q: IQService,
        $scope: IScope,
        private readonly $translate: ITranslateService,
        private readonly authServiceRS: AuthServiceRS,
        private readonly $rootScope: IRootScopeService,
        private readonly ewkRS: EwkRS,
        $timeout: ITimeoutService,
        private readonly einstellungRS: EinstellungRS,
        private readonly uploadRS: UploadRS,
        private readonly downloadRS: DownloadRS,
        private readonly applicationPropertyRS: ApplicationPropertyRS,
        private readonly dokumenteRS: DokumenteRS,
        private readonly mandantService: MandantService,
        private readonly demoFeatureRS: DemoFeatureRS
    ) {
        super(gesuchModelManager,
            berechnungsManager,
            wizardStepManager,
            $scope,
            TSWizardStepName.GESUCHSTELLER,
            $timeout);
        this.gesuchstellerNumber = parseInt($stateParams.gesuchstellerNumber, 10);
        this.gesuchModelManager.setGesuchstellerNumber(this.gesuchstellerNumber);
        this.mandantService.mandant$.pipe(map(mandant => mandant === MANDANTS.LUZERN)).subscribe(isLuzern => {
            this.isLuzern = isLuzern;
        }, err => LOG.error(err));
    }

    public $onInit(): void {
        super.$onInit();
        this.initViewmodel();
        this.loadAusweisNachweiseIfNotNewContainer();
        this.setFrenchEnabled();
    }

    private loadAusweisNachweiseIfNotNewContainer(): void {
        this.berechnungsManager
            .getDokumente(this.gesuchModelManager.getGesuch())
            .then((alleDokumente: TSDokumenteDTO) => {
                alleDokumente.dokumentGruende
                    .filter(tsDokument => tsDokument.dokumentGrundTyp === TSDokumentGrundTyp.FAMILIENSITUATION)
                    .filter(tsDokument => tsDokument.dokumentTyp === TSDokumentTyp.AUSWEIS_ID)
                    .forEach(tsDokument =>
                        this.dokumentGrund = tsDokument);
            });
    }

    private initViewmodel(): void {
        this.gesuchModelManager.initStammdaten();
        this.model = angular.copy(this.gesuchModelManager.getStammdatenToWorkWith());
        this.wizardStepManager.updateCurrentWizardStepStatusSafe(
            TSWizardStepName.GESUCHSTELLER,
            TSWizardStepStatus.IN_BEARBEITUNG);
        this.geschlechter = EnumEx.getNames(TSGeschlecht);
        this.showKorrespondadr = !!(this.model.korrespondenzAdresse && this.model.korrespondenzAdresse.adresseJA);
        this.showKorrespondadrGS = !!(this.model.korrespondenzAdresse && this.model.korrespondenzAdresse.adresseGS);
        this.showRechnungsadr = !!(this.model.rechnungsAdresse && this.model.rechnungsAdresse.adresseJA);
        this.showRechnungsadrGS = !!(this.model.rechnungsAdresse && this.model.rechnungsAdresse.adresseGS);
        this.allowedRoles = this.TSRoleUtil.getAllRolesButTraegerschaftInstitution();
        this.getModel().showUmzug = this.getModel().showUmzug || this.getModel().isThereAnyUmzug();
        this.setLastVerfuegtesGesuch();
        this.einstellungRS.findEinstellung(TSEinstellungKey.DIPLOMATENSTATUS_DEAKTIVIERT,
            this.gesuchModelManager.getGemeinde().id,
            this.gesuchModelManager.getGesuchsperiode().id).subscribe(diplomatenStatusDisabled => {
            this.diplomatenStatusDisabled = diplomatenStatusDisabled.value === 'true';
        }, error => LOG.error(error));
        this.einstellungRS.findEinstellung(TSEinstellungKey.AUSWEIS_NACHWEIS_REQUIRED,
            this.gesuchModelManager.getGemeinde().id,
            this.gesuchModelManager.getGesuchsperiode().id).subscribe(ausweisNachweisRequired => {
            this.ausweisNachweisRequiredEinstellung = ausweisNachweisRequired.value === 'true';
        }, error => LOG.error(error));
        this.demoFeatureRS.isDemoFeatureAllowed(TSDemoFeature.KIBON_2754)
            .then(isAllowed => this.demoFeature2754 = isAllowed);
    }

    public getFamilienSituationDisplayValue(): string {
        if (!this.gesuchModelManager.isFKJVTexte || !this.demoFeature2754){
            return this.gesuchstellerNumber.toString();
        }

        if (this.gesuchstellerNumber === 1) {
            return '1';
        }
        let tsFamiliensituation: TSFamiliensituation = this.getGesuch().extractFamiliensituation();
        if (EbeguUtil.isNullOrUndefined(tsFamiliensituation)) {
            return '';
        }
        const partnerIdentisch: boolean = tsFamiliensituation.partnerIdentischMitVorgesuch;
        let familienstatus: TSFamilienstatus = tsFamiliensituation.familienstatus;
        if (EbeguUtil.isNotNullAndFalse(partnerIdentisch)){
            familienstatus= this.getGesuch().extractFamiliensituationErstgesuch().familienstatus;
            tsFamiliensituation = this.getGesuch().extractFamiliensituationErstgesuch();
        }
        switch (familienstatus) {
            case TSFamilienstatus.KONKUBINAT_KEIN_KIND:
                if (tsFamiliensituation.konkubinatGetXYearsInPeriod(this.getGesuch().gesuchsperiode.gueltigkeit)) {
                    if(this.isObhutNeinUnterhaltNichtmoeglich(tsFamiliensituation) ||
                            this.isObhutJaMitAndererPerson(tsFamiliensituation)){
                        return `2 (${this.$translate.instant('GS2_KONKUBINAT_KEIN_KIND')})`;
                    }
                    return `2 (${this.$translate.instant('ANDERER_ELTERNTEIL')})`;
                }
                else {
                    if(this.isObhutJaMitAndererPerson(tsFamiliensituation)){
                        return `2 (${this.$translate.instant('ANDERER_ELTERNTEIL')})`;
                    }
                    return `2 (${this.$translate.instant('GS2_KONKUBINAT_KEIN_KIND')})`;
                }
                break;
            case TSFamilienstatus.ALLEINERZIEHEND:
                if(tsFamiliensituation.gesuchstellerKardinalitaet ===  TSGesuchstellerKardinalitaet.ZU_ZWEIT ||
                    tsFamiliensituation.unterhaltsvereinbarung ===
                        TSUnterhaltsvereinbarungAnswer.NEIN_UNTERHALTSVEREINBARUNG){
                    return `2 (${ this.$translate.instant('ANDERER_ELTERNTEIL')   })`;
                }
                break;
            default:
                break;
        }
        return `2 (${ this.$translate.instant(`GS2_${familienstatus}`)   })`;
    }

    public korrespondenzAdrClicked(): void {
        if (!this.showKorrespondadr) {
            return;
        }

        if (!this.model.korrespondenzAdresse) {
            this.model.korrespondenzAdresse = this.initAdresse(TSAdressetyp.KORRESPONDENZADRESSE);
        } else if (!this.model.korrespondenzAdresse.adresseJA) {
            this.initKorrespondenzAdresseJA();
        }
    }

    public rechnungsAdrClicked(): void {
        if (!this.showRechnungsadr) {
            return;
        }

        if (!this.model.rechnungsAdresse) {
            this.model.rechnungsAdresse = this.initAdresse(TSAdressetyp.RECHNUNGSADRESSE);
        } else if (!this.model.rechnungsAdresse.adresseJA) {
            this.initRechnungsAdresseJA();
        }
    }

    private setLastVerfuegtesGesuch(): void {
        this.isLastVerfuegtesGesuch = this.gesuchModelManager.isNeuestesGesuch();
    }

    public preSave(): IPromise<TSGesuchstellerContainer> {
        if (!this.isGesuchValid()) {
            return undefined;
        }

        if (this.areEmailTelefonEditable() && this.isGesuchReadonly()) {
            const properties = this.ebeguRestUtil.alwaysEditablePropertiesToRestObject({},
                this.gesuchModelManager.getGesuch());
            if (this.gesuchstellerNumber === 2) {
                properties.mailGS2 = this.getModelJA().mail;
                properties.mobileGS2 = this.getModelJA().mobile;
                properties.telefonGS2 = this.getModelJA().telefon;
                properties.telefonAuslandGS2 = this.getModelJA().telefonAusland;
            } else {
                properties.mailGS1 = this.getModelJA().mail;
                properties.mobileGS1 = this.getModelJA().mobile;
                properties.telefonGS1 = this.getModelJA().telefon;
                properties.telefonAuslandGS1 = this.getModelJA().telefonAusland;
            }
            if (!this.form.$dirty) {
                // If there are no changes in form we don't need to try to update email or the telefon
                // promise immediately
                return this.$q.when(this.model);
            }

            return this.gesuchModelManager.updateAlwaysEditableProperties(properties).then(g => {
                if (this.gesuchstellerNumber === 2) {
                    return g.gesuchsteller2;
                }
                return g.gesuchsteller1;
            });

        }

        return this.save();
    }

    public isGesuchValid(): boolean {
        if (!this.form.$valid) {
            EbeguUtil.selectFirstInvalid();
        }

        if (this.isAusweisNachweisRequired() && (EbeguUtil.isNullOrUndefined(this.dokumentGrund)
            || this.dokumentGrund?.dokumente.length === 0)) {
            this.dvFileUploadError = {required: true};
        }

        return this.form.$valid && (!this.isAusweisNachweisRequired() || this.dokumentGrund?.dokumente.length > 0);
    }

    private isAusweisNachweisRequired(): boolean {
        return this.ausweisNachweisRequiredEinstellung && this.gesuchModelManager.getGesuch()
            ?.isOnlineGesuch() && this.gesuchstellerNumber === 1;
    }

    public save(): IPromise<TSGesuchstellerContainer> {
        if (!this.isGesuchValid()) {
            return undefined;
        }

        this.gesuchModelManager.setStammdatenToWorkWith(this.model);
        if (!this.form.$dirty) {
            // If there are no changes in form we don't need anything to update on Server and we could return the
            // promise immediately
            // Update wizardStepStatus also if the form is empty and not dirty
            const isGS2Required = this.gesuchModelManager.isGesuchsteller2Required();
            if ((isGS2Required && this.gesuchstellerNumber === 2) || !isGS2Required) {
                this.wizardStepManager.updateCurrentWizardStepStatus(TSWizardStepStatus.OK);
            }
            return this.$q.when(this.model);
        }
        // wenn keine Korrespondenzaddr oder Rechnungsadr da ist koennen wir sie wegmachen
        this.maybeResetKorrespondadr();
        this.maybeResetRechnungsadr();

        this.updateStatusStepUmzug();
        this.errorService.clearAll();
        return this.gesuchModelManager.updateGesuchsteller(false);
    }

    private updateStatusStepUmzug(): void {
        if (this.gesuchModelManager.getGesuchstellerNumber() !== 1) {
            // umzug can only be introduced for gs1
            return;
        }
        const showUmzug = this.gesuchModelManager.getGesuch().gesuchsteller1.showUmzug;
        if ((this.gesuchModelManager.getGesuch().gesuchsteller1 && showUmzug) || this.isMutation()) {
            this.wizardStepManager.unhideStep(TSWizardStepName.UMZUG);
        } else {
            this.wizardStepManager.hideStep(TSWizardStepName.UMZUG);
        }
    }

    public getModel(): TSGesuchstellerContainer {
        return this.model;
    }

    public getModelJA(): TSGesuchsteller {
        return this.model.gesuchstellerJA;
    }

    /**
     * Die Wohnadresse des GS2 darf bei Mutationen in denen der GS2 bereits existiert, nicht geaendert werden.
     * Die Wohnadresse des GS1 darf bei Mutationen nie geaendert werden
     */
    public disableWohnadresseFor2GS(): boolean {
        return this.isMutation() && (this.gesuchstellerNumber === 1
            || (this.model.vorgaengerId !== null
                && this.model.vorgaengerId !== undefined));
    }

    /**
     * Die Wohnadresse, die Rechungsadresse, sowie das Umzugsflag werden nur für GS 1 angezeigt
     */
    public showWohnadresse(): boolean {
        return this.gesuchstellerNumber === 1;
    }

    public isThereAnyUmzug(): boolean {
        return this.gesuchModelManager.getGesuch() && this.gesuchModelManager.getGesuch().isThereAnyUmzug();
    }

    private maybeResetKorrespondadr(): void {
        if (!this.showKorrespondadr && !this.showKorrespondadrGS) {
            // keine korrAdr weder von GS noch von JA -> entfernen
            this.getModel().korrespondenzAdresse = undefined;
        } else if (!this.showKorrespondadr) {
            // nur adresse JA wird zurueckgesetzt die GS kann bleiben
            this.getModel().korrespondenzAdresse.adresseJA = undefined;
        }
    }

    private maybeResetRechnungsadr(): void {
        if (!this.showRechnungsadr && !this.showRechnungsadrGS) {
            // keine rechnungsAdresse weder von GS noch von JA -> entfernen
            this.getModel().rechnungsAdresse = undefined;
        } else if (!this.showRechnungsadr) {
            // nur adresse JA wird zurueckgesetzt die GS kann bleiben
            this.getModel().rechnungsAdresse.adresseJA = undefined;
        }
    }

    private initAdresse(adresstyp: TSAdressetyp): TSAdresseContainer {
        const adresseContanier = new TSAdresseContainer();
        const adresse = new TSAdresse();
        adresse.adresseTyp = adresstyp;
        adresseContanier.showDatumVon = false;
        adresseContanier.adresseJA = adresse;
        return adresseContanier;
    }

    private initKorrespondenzAdresseJA(): void {
        const addr = new TSAdresse();
        addr.adresseTyp = TSAdressetyp.KORRESPONDENZADRESSE;
        this.model.korrespondenzAdresse.adresseJA = addr;
        this.model.korrespondenzAdresse.showDatumVon = false;
    }

    private initRechnungsAdresseJA(): void {
        const addr = new TSAdresse();
        addr.adresseTyp = TSAdressetyp.RECHNUNGSADRESSE;
        this.model.rechnungsAdresse.adresseJA = addr;
        this.model.rechnungsAdresse.showDatumVon = false;
    }

    public getTextAddrKorrekturJA(adresseContainer: TSAdresseContainer): string {
        if (adresseContainer && adresseContainer.adresseGS) {
            const adr = adresseContainer.adresseGS;
            const organisation = adr.organisation ? adr.organisation : '-';
            const strasse = adr.strasse ? adr.strasse : '-';
            const hausnummer = adr.hausnummer ? adr.hausnummer : '-';
            const zusatzzeile = adr.zusatzzeile ? adr.zusatzzeile : '-';
            const plz = adr.plz ? adr.plz : '-';
            const ort = adr.ort ? adr.ort : '-';
            const land = this.$translate.instant(`Land_${  adr.land}`);
            return this.$translate.instant('JA_KORREKTUR_ADDR', {
                organisation,
                strasse,
                hausnummer,
                zusatzzeile,
                plz,
                ort,
                land
            });
        }

        return this.$translate.instant('LABEL_KEINE_ANGABE');
    }

    /**
     * Checks whether the fields Email and Telefon are editable or not. The conditions for knowing if it is
     * editable or not are the same ones of isGesuchReadonly(). But in this case, if the user is from the jugenadamt
     * and the current gesuch is the newest one they may also edit those fields
     */
    public areEmailTelefonEditable(): boolean {
        return this.isLastVerfuegtesGesuch
        && this.authServiceRS.isOneOfRoles(TSRoleUtil.getGesuchstellerSozialdienstJugendamtSchulamtRoles()) ?
            true :
            !this.isGesuchReadonly();
    }

    /**
     * Gibt alle Sprachen zurueck
     */
    public getSprachen(): Array<TSSprache> {
        return getTSSpracheValues();
    }

    public showRechnungsadresseCheckbox(): boolean {
        return this.gesuchstellerNumber === 1
            && this.gesuchModelManager.isAnmeldungTagesschuleEnabledForMandantAndGemeinde()
            && this.gesuchModelManager.isAnmeldungenTagesschuleEnabledForGemeindeAndGesuchsperiode();
    }

    // Email is not required for Papiergesuche and Sozialdienst Gesuche
    public isMailRequired(): boolean {
        const gesuch = this.gesuchModelManager.getGesuch();
        const fall = this.gesuchModelManager.getFall();
        if (!gesuch || !fall) {
            return true;
        }
        if (fall.isSozialdienstFall()) {
            return false;
        }
        return this.gesuchstellerNumber === 1 && gesuch.eingangsart === TSEingangsart.ONLINE;
    }

    public isLastStepOfSteueramt(): boolean {
        return this.authServiceRS.isOneOfRoles(TSRoleUtil.getSteueramtOnlyRoles())
            && this.gesuchModelManager.isLastGesuchsteller();
    }

    public onUpload(event: any): void {
        if (EbeguUtil.isNullOrUndefined(event?.target?.files?.length)) {
            return;
        }
        const files = event.target.files;
        if (this.checkFilesLength(files as File[])) {
            return;
        }
        if (EbeguUtil.isNullOrUndefined(this.dokumentGrund)) {
            this.dokumentGrund = new TSDokumentGrund();
            this.dokumentGrund.dokumentTyp = TSDokumentTyp.AUSWEIS_ID;
            this.dokumentGrund.dokumentGrundTyp = TSDokumentGrundTyp.FAMILIENSITUATION;
        }
        this.uploadRS.uploadFile(files, this.dokumentGrund, this.gesuchModelManager.getGesuch().id)
            .then(dokumentGrund => {
                this.dokumentGrund = dokumentGrund;
                this.dvFileUploadError = null;
                this.form.$setDirty();
            });
    }

    /**
     * checks if some files are too big and stores them in filesTooBig variable
     */
    private checkFilesLength(files: File[]): boolean {
        this.filesTooBig = [];
        for (const file of files) {
            if (file.size > MAX_FILE_SIZE) {
                this.filesTooBig.push(file);
            }
        }
        return this.filesTooBig.length > 0;
    }

    public onDeleteFile(dokument: TSDokument): void {
        const index = EbeguUtil.getIndexOfElementwithID(dokument, this.dokumentGrund.dokumente);

        if (index > -1) {
            this.dokumentGrund.dokumente.splice(index, 1);
        }

        this.dokumenteRS.removeDokument(dokument).then(() => {
            this.dokumentGrund.dokumente = this.dokumentGrund.dokumente.filter(d => d.id !== dokument.id);
            if (this.dokumentGrund.dokumente.length === 0) {
                this.dvFileUploadError = {required: true};
            }
            this.form.$setDirty();
        }).catch(err => {
            LOG.error(err);
            this.errorService.addMesageAsError(err);
        });
    }

    public downloadAusweisDokument(dokument: TSSozialdienstFallDokument, attachment: boolean): void {
        const win = this.downloadRS.prepareDownloadWindow();
        this.downloadRS.getAccessTokenDokument(dokument.id)
            .then((downloadFile: TSDownloadFile) => {
                this.downloadRS.startDownload(downloadFile.accessToken, downloadFile.filename, attachment, win);
            })
            .catch(() => {
                win.close();
            });
    }

    private setFrenchEnabled(): void {
        this.applicationPropertyRS.getPublicPropertiesCached()
            .then(properties => properties.frenchEnabled)
            .then(frenchEnabled => {
                this.frenchEnabled = frenchEnabled;
            });
    }

    public showKorrespondenzsprache(): boolean {
        return this.gesuchModelManager.getGesuchstellerNumber() === 1 && this.frenchEnabled;
    }

    public showHintMandatoryFields(): boolean {
        return !this.isLuzern || this.gesuchModelManager.getGesuchstellerNumber() === 1;
    }

    private isObhutNeinUnterhaltNichtmoeglich(tsFamiliensituation: TSFamiliensituation): boolean {
        return !tsFamiliensituation.geteilteObhut
                && tsFamiliensituation.unterhaltsvereinbarung ===
                TSUnterhaltsvereinbarungAnswer.UNTERHALTSVEREINBARUNG_NICHT_MOEGLICH;
    }

    private isObhutJaMitAndererPerson(tsFamiliensituation: TSFamiliensituation): boolean {
        return tsFamiliensituation.geteilteObhut &&
                tsFamiliensituation.gesuchstellerKardinalitaet ===
        TSGesuchstellerKardinalitaet.ZU_ZWEIT;
    }
}
