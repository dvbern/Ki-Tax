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
import {CONSTANTS} from '../../../app/core/constants/CONSTANTS';
import {ErrorService} from '../../../app/core/errors/service/ErrorService';
import {EwkRS} from '../../../app/core/service/ewkRS.rest';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {TSAdressetyp} from '../../../models/enums/TSAdressetyp';
import {TSEingangsart} from '../../../models/enums/TSEingangsart';
import {TSGeschlecht} from '../../../models/enums/TSGeschlecht';
import {TSRole} from '../../../models/enums/TSRole';
import {getTSSpracheValues, TSSprache} from '../../../models/enums/TSSprache';
import {TSWizardStepName} from '../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../models/enums/TSWizardStepStatus';
import {TSAdresse} from '../../../models/TSAdresse';
import {TSAdresseContainer} from '../../../models/TSAdresseContainer';
import {TSGesuchsteller} from '../../../models/TSGesuchsteller';
import {TSGesuchstellerContainer} from '../../../models/TSGesuchstellerContainer';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';
import {EnumEx} from '../../../utils/EnumEx';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {IStammdatenStateParams} from '../../gesuch.route';
import {BerechnungsManager} from '../../service/berechnungsManager';
import {GesuchModelManager} from '../../service/gesuchModelManager';
import {WizardStepManager} from '../../service/wizardStepManager';
import {AbstractGesuchViewController} from '../abstractGesuchView';
import IPromise = angular.IPromise;
import IQService = angular.IQService;
import IRootScopeService = angular.IRootScopeService;
import IScope = angular.IScope;
import ITimeoutService = angular.ITimeoutService;
import ITranslateService = angular.translate.ITranslateService;

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
    ];

    public readonly CONSTANTS: any = CONSTANTS;
    public geschlechter: Array<string>;
    public showKorrespondadr: boolean;
    public showKorrespondadrGS: boolean;
    public showRechnungsadr: boolean;
    public showRechnungsadrGS: boolean;
    public allowedRoles: ReadonlyArray<TSRole>;
    public gesuchstellerNumber: number;
    private isLastVerfuegtesGesuch: boolean = false;

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
    ) {
        super(gesuchModelManager,
            berechnungsManager,
            wizardStepManager,
            $scope,
            TSWizardStepName.GESUCHSTELLER,
            $timeout);
        this.gesuchstellerNumber = parseInt($stateParams.gesuchstellerNumber, 10);
        this.gesuchModelManager.setGesuchstellerNumber(this.gesuchstellerNumber);
    }

    public $onInit(): void {
        super.$onInit();
        this.initViewmodel();
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
            const properties = this.ebeguRestUtil.alwaysEditablePropertiesToRestObject({}, this.gesuchModelManager.getGesuch());
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

            return this.gesuchModelManager.updateAlwaysEditableProperties(properties).then( g => {
                if (this.gesuchstellerNumber === 2) {
                    return g.gesuchsteller2;
                }
                return g.gesuchsteller1;
            });

        }

        return this.save();
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
            const land = this.$translate.instant('Land_' + adr.land);
            return this.$translate.instant('JA_KORREKTUR_ADDR', {
                organisation,
                strasse,
                hausnummer,
                zusatzzeile,
                plz,
                ort,
                land,
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
        return this.gesuchstellerNumber === 1;
    }

    // Email is not required for Papiergesuche and Sozialdienst Gesuche
    public isMailRequired(): boolean {
        const gesuch = this.gesuchModelManager.getGesuch();
        if (!gesuch) {
            return true;
        }
        if (this.gesuchModelManager.getFall()?.isSozialdienstFall()) {
            return false;
        }
        return this.gesuchstellerNumber === 1 && gesuch.eingangsart === TSEingangsart.ONLINE;
    }

    public isLastStepOfSteueramt(): boolean {
        return this.authServiceRS.isOneOfRoles(TSRoleUtil.getSteueramtOnlyRoles())
            && this.gesuchModelManager.isLastGesuchsteller();
    }
}
