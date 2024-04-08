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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import {IComponentOptions} from 'angular';
import * as moment from 'moment';
import {Subject} from 'rxjs';
import {takeUntil} from 'rxjs/operators';
import {EinstellungRS} from '../../../admin/service/einstellungRS.rest';
import {CONSTANTS} from '../../../app/core/constants/CONSTANTS';
import {EinschulungTypesVisitor} from '../../../app/core/constants/EinschulungTypesVisitor';
import {KindGeschlechtVisitor} from '../../../app/core/constants/KindGeschlechtVisitor';
import {KiBonMandant} from '../../../app/core/constants/MANDANTS';
import {TSDemoFeature} from '../../../app/core/directive/dv-hide-feature/TSDemoFeature';
import {ErrorService} from '../../../app/core/errors/service/ErrorService';
import {LogFactory} from '../../../app/core/logging/LogFactory';
import {MandantService} from '../../../app/shared/services/mandant.service';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {TSAnspruchBeschaeftigungAbhaengigkeitTyp} from '../../../models/enums/TSAnspruchBeschaeftigungAbhaengigkeitTyp';
import {TSEinschulungTyp} from '../../../models/enums/TSEinschulungTyp';
import {TSEinstellungKey} from '../../../models/enums/TSEinstellungKey';
import {TSFachstellenTyp} from '../../../models/enums/TSFachstellenTyp';
import {TSGeschlecht} from '../../../models/enums/TSGeschlecht';
import {TSGruendeZusatzleistung} from '../../../models/enums/TSGruendeZusatzleistung';
import {TSIntegrationTyp} from '../../../models/enums/TSIntegrationTyp';
import {getTSKinderabzugValues, TSKinderabzug} from '../../../models/enums/TSKinderabzug';
import {isKinderabzugTypFKJV, TSKinderabzugTyp} from '../../../models/enums/TSKinderabzugTyp';
import {TSRole} from '../../../models/enums/TSRole';
import {TSWizardStepName} from '../../../models/enums/TSWizardStepName';
import {TSEinstellung} from '../../../models/TSEinstellung';
import {TSKind} from '../../../models/TSKind';
import {TSKindContainer} from '../../../models/TSKindContainer';
import {TSPensumAusserordentlicherAnspruch} from '../../../models/TSPensumAusserordentlicherAnspruch';
import {TSPensumFachstelle} from '../../../models/TSPensumFachstelle';
import {TSDateRange} from '../../../models/types/TSDateRange';
import {DateUtil} from '../../../utils/DateUtil';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {EnumEx} from '../../../utils/EnumEx';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {IKindStateParams} from '../../gesuch.route';
import {BerechnungsManager} from '../../service/berechnungsManager';
import {GesuchModelManager} from '../../service/gesuchModelManager';
import {GlobalCacheService} from '../../service/globalCacheService';
import {HybridFormBridgeService} from '../../service/hybrid-form-bridge.service';
import {WizardStepManager} from '../../service/wizardStepManager';
import {AbstractGesuchViewController} from '../abstractGesuchView';
import {KinderabzugExchangeService} from './service/kinderabzug-exchange.service';
import {TSAusserordentlicherAnspruchTyp} from '../../../models/enums/TSAusserordentlicherAnspruchTyp';
import IPromise = angular.IPromise;
import IQService = angular.IQService;
import IScope = angular.IScope;
import ITimeoutService = angular.ITimeoutService;
import ITranslateService = angular.translate.ITranslateService;

const LOG = LogFactory.createLog('KindViewController');

export class KindViewComponentConfig implements IComponentOptions {
    public transclude = false;
    public template = require('./kindView.html');
    public controller = KindViewController;
    public controllerAs = 'vm';
}

export class KindViewController extends AbstractGesuchViewController<TSKindContainer> {

    public static $inject: string[] = [
        '$stateParams',
        'GesuchModelManager',
        'BerechnungsManager',
        '$scope',
        'ErrorService',
        'WizardStepManager',
        '$q',
        '$translate',
        '$timeout',
        'EinstellungRS',
        'GlobalCacheService',
        'AuthServiceRS',
        'EbeguRestUtil',
        'MandantService',
        'KinderabzugExchangeService',
        'HybridFormBridgeService',
    ];

    public readonly CONSTANTS: any = CONSTANTS;
    public integrationTypes: Array<string>;
    public gruendeZusatzleistung: Array<string>;
    public geschlechter: Array<string>;
    public kinderabzugValues: Array<TSKinderabzug>;
    public einschulungTypValues: ReadonlyArray<TSEinschulungTyp>;
    public showFachstelle: boolean;
    public showFachstelleGS: boolean;
    public showAusserordentlicherAnspruch: boolean;
    // der ausgewaehlte fachstelleId wird hier gespeichert und dann in die entsprechende Fachstelle umgewandert
    public allowedRoles: ReadonlyArray<TSRole>;
    public kontingentierungEnabled: boolean;
    public anspruchUnabhaengingVomBeschaeftigungspensum: boolean;

    private kinderabzugTyp: TSKinderabzugTyp;
    private fachstellenTyp: TSFachstellenTyp;
    private ausserodentlicherAnspruchTyp: TSAusserordentlicherAnspruchTyp;
    public maxPensumAusserordentlicherAnspruch: string;
    // When migrating to ng, use observable in template
    public submitted: boolean = false;
    private isSpracheAmtspracheDisabled: boolean;
    private isZemisDeaktiviert: boolean = false;
    private mandant: KiBonMandant;
    public readonly demoFeature = TSDemoFeature.MEHRERE_FACHSTELLEN;
    private fachstellenPensumOverlapsMessageKey: string = undefined;
    private sozialeIntegrationBisSchulstufe: TSEinschulungTyp;
    private sprachlicheIntegrationBisSchulstufe: TSEinschulungTyp;

    private readonly unsubscribe$ = new Subject();

    public constructor(
        $stateParams: IKindStateParams,
        gesuchModelManager: GesuchModelManager,
        berechnungsManager: BerechnungsManager,
        $scope: IScope,
        private readonly errorService: ErrorService,
        wizardStepManager: WizardStepManager,
        private readonly $q: IQService,
        private readonly $translate: ITranslateService,
        $timeout: ITimeoutService,
        private readonly einstellungRS: EinstellungRS,
        private readonly globalCacheService: GlobalCacheService,
        private readonly authServiceRS: AuthServiceRS,
        private readonly ebeguRestUtil: EbeguRestUtil,
        private readonly mandantService: MandantService,
        private readonly kinderabzugExchangeService: KinderabzugExchangeService,
        private readonly hybridFormBridgeService: HybridFormBridgeService,
    ) {
        super(gesuchModelManager, berechnungsManager, wizardStepManager, $scope, TSWizardStepName.KINDER, $timeout);
        if ($stateParams.kindNumber) {
            const kindNumber = parseInt($stateParams.kindNumber, 10);
            const kindIndex = this.gesuchModelManager.convertKindNumberToKindIndex(kindNumber);
            if (kindIndex >= 0) {
                this.model = angular.copy(this.gesuchModelManager.getGesuch().kindContainers[kindIndex]);
                this.gesuchModelManager.setKindIndex(kindIndex);
            }
        } else {
            // wenn kind nummer nicht definiert ist heisst dass, das wir ein neues erstellen sollten
            this.model = this.initEmptyKind(undefined);
            const kindIndex = this.gesuchModelManager.getGesuch().kindContainers ?
                this.gesuchModelManager.getGesuch().kindContainers.length :
                0;
            this.gesuchModelManager.setKindIndex(kindIndex);
        }
        this.allowedRoles = this.TSRoleUtil.getAllRolesButTraegerschaftInstitution();
        // TODO: Replace with angularX async template pipe during ablösung
        this.mandantService.mandant$
            .pipe(takeUntil(this.unsubscribe$))
            .subscribe(mandant => {
                this.mandant = mandant;
                this.initViewModel();
        }, err => LOG.error(err));

    }

    public $onDestroy(): void {
        this.unsubscribe$.next();
    }

    private initViewModel(): void {
        this.gruendeZusatzleistung = EnumEx.getNames(TSGruendeZusatzleistung);
        this.geschlechter = EnumEx.getNames(TSGeschlecht);
        this.kinderabzugValues = getTSKinderabzugValues();
        this.einschulungTypValues = new EinschulungTypesVisitor().process(this.mandant);
        this.initFachstelle();
        this.initAusserordentlicherAnspruch();
        this.getEinstellungKontingentierung();
        this.loadEinstellungen();
    }

    public $postLink(): void {
        // Bei einer neuen Periode werden gewisse Kinderdaten nicht kopiert. In diesem Fall sollen diese
        // bereits rot angezeigt werden.
        if (!this.model.kindJA.isNew() && !this.model.kindJA.isGeprueft()) {
            this.form.$setSubmitted();
        }
    }

    public getTextSprichtAmtssprache(): string {
        return this.$translate.instant('SPRICHT_AMTSSPRACHE',
            {
                amtssprache: EbeguUtil
                    .getAmtsspracheAsString(this.gesuchModelManager.gemeindeStammdaten, this.$translate),
            });
    }

    private initFachstelle(): void {
        this.showFachstelle = this.model.kindJA.pensumFachstellen?.length > 0;
        this.showFachstelleGS = this.model.kindGS?.pensumFachstellen?.length > 0;
    }

    private initAusserordentlicherAnspruch(): void {
        this.showAusserordentlicherAnspruch = !!(this.model.kindJA.pensumAusserordentlicherAnspruch);
    }

    public save(): IPromise<TSKindContainer> {
        this.submitted = true;
        this.errorService.clearAll();
        this.kinderabzugExchangeService.triggerFormValidation();
        if (!this.isGesuchValid() || this.kinderabzugExchangeService.form?.invalid) {
            return undefined;
        }
        const invalidPensumFachstellen = this.checkFachstellenValidity();
        if (invalidPensumFachstellen.length > 0) {
            this.errorService.addMesageAsError(invalidPensumFachstellen[0].error);
            return undefined;
        }

        if (!this.hybridFormBridgeService.forms.reduce((prev, cur) => cur.valid && prev, true)) {
            return undefined;
        }

        if (!this.getPensumFachstellen().reduce((prev, cur) => prev && cur.isComplete(this.fachstellenTyp), true)) {
            return undefined;
        }

        this.getModel().zukunftigeGeburtsdatum = this.isGeburtsdatumInZunkunft();
        this.getModel().inPruefung = false;

        this.errorService.clearAll();
        if (this.isGeburtstagInvalidForFkjv()) {
            this.errorService.addMesageAsError(this.$translate.instant('ERROR_KIND_VOLLJAEHRIG_AND_HAS_BETREUNG'));
            return undefined;
        }
        return this.gesuchModelManager.saveKind(this.model);
    }

    /**
     * Geburtsdatum darf nicht auf über 18 Jahre sein, wenn für das Kind bereits eine Betreuung erfasst wurde
     */
    public isGeburtstagInvalidForFkjv(): boolean {
        return this.showFkjvKinderabzug()
            && this.isOrGetsKindVolljaehrigDuringGP()
            && this.hasKindBetreuungen();
    }

    public isOrGetsKindVolljaehrigDuringGP(): boolean {
        return EbeguUtil.calculateKindIsOrGetsVolljaehrig(
            this.getModel().geburtsdatum,
            this.gesuchModelManager.getGesuchsperiode());
    }

    public cancel(): void {
        this.reset();
        this.form.$setPristine();
    }

    public reset(): void {
        this.removeKindFromList();
    }

    private removeKindFromList(): void {
        if (!this.model.timestampErstellt) {
            // wenn das Kind noch nicht erstellt wurde, löschen wir das Kind vom Array
            this.gesuchModelManager.removeKindFromList();
        }
    }

    public showFachstelleClicked(): void {
        if (this.showFachstelle) {
            this.addNewPensumFachstelle();
        } else {
            this.resetPensumFachstellen();
        }
    }

    public addNewPensumFachstelle(): void {
        const pensumFachstelle: TSPensumFachstelle = new TSPensumFachstelle();
        pensumFachstelle.gueltigkeit = new TSDateRange();
        this.getPensumFachstellen()?.push(pensumFachstelle);
    }

    public removePensumFachstelle(index: number): void {
        this.getPensumFachstellen().splice(index, 1);
    }

    public showAusserordentlicherAnspruchCheckbox(): boolean {
        // Checkbox nie anzeigen, wenn kein ausserordentlicher Anspruch
        if (this.ausserodentlicherAnspruchTyp === TSAusserordentlicherAnspruchTyp.KEINE) {
            return false;
        }
        // Checkbox wird nur angezeigt, wenn das Kind externe Betreuung hat und entweder bereits ein
        // Anspruch gesetzt ist, oder es sich um einen Gemeinde-User handelt
        return this.getModel().familienErgaenzendeBetreuung && (
            this.showAusserordentlicherAnspruch
            || this.authServiceRS.isOneOfRoles(TSRoleUtil.getAdministratorOrAmtRole()));
    }

    public isAusserordentlicherAnspruchEnabled(): boolean {
        return !this.isGesuchReadonly()
            && this.authServiceRS.isOneOfRoles(TSRoleUtil.getAdministratorOrAmtRole());
    }

    public showAusserordentlicherAnspruchClicked(): void {
        this.getModel().pensumAusserordentlicherAnspruch =
            this.showAusserordentlicherAnspruch ? new TSPensumAusserordentlicherAnspruch() : undefined;
    }

    public familienErgaenzendeBetreuungClicked(): void {
        if (!this.getModel().familienErgaenzendeBetreuung) {
            this.showFachstelle = false;
            this.resetPensumFachstellen();
        }
        this.familienErgaenzendeBetreuungChanged();
    }

    private resetPensumFachstellen(): void {
        this.getModel().pensumFachstellen = [];
    }

    public getModel(): TSKind {
        if (this.model) {
            return this.model.kindJA;
        }
        return undefined;
    }

    public getContainer(): TSKindContainer {
        if (this.model) {
            return this.model;
        }
        return undefined;
    }

    public getPensumFachstellen(): TSPensumFachstelle[] {
        if (this.getModel()) {
            return this.getModel().pensumFachstellen;
        }
        return undefined;
    }

    public isFachstelleRequired(): boolean {
        return this.getModel() && this.getModel().familienErgaenzendeBetreuung && this.showFachstelle;
    }

    public getPensumAusserordentlicherAnspruch(): TSPensumAusserordentlicherAnspruch {
        if (this.getModel()) {
            return this.getModel().pensumAusserordentlicherAnspruch;
        }
        return undefined;
    }

    // diese Fragen sind nur relevant, wenn der Anpruch abhängig vom Beschäftigungspensum ist.
    public showExtendedKindQuestions(): boolean {
        return !this.anspruchUnabhaengingVomBeschaeftigungspensum;
    }

    public showAusAsylwesen(): boolean {
        // Checkbox wird nur angezeigt, wenn das Kind externe Betreuung hat und zemis nicht deaktiviert ist
        return this.getModel().familienErgaenzendeBetreuung && !this.isZemisDeaktiviert;
    }

    public showZemisNummer(): boolean {
        return this.showAusAsylwesen() && this.getModel().ausAsylwesen;
    }

    public showAsivKinderabzug(): boolean {
        return this.kinderabzugTyp === TSKinderabzugTyp.ASIV;
    }

    public showFkjvKinderabzug(): boolean {
        return isKinderabzugTypFKJV(this.kinderabzugTyp);
    }

    public showSchwyzKinderabzug(): boolean {
        return this.kinderabzugTyp === TSKinderabzugTyp.SCHWYZ;
    }

    public isGeburtsdatumValid(): boolean {
        return this.getModel()?.geburtsdatum?.isValid();
    }

    public isAusserordentlicherAnspruchRequired(): boolean {
        return this.getModel() && this.getModel().familienErgaenzendeBetreuung && this.showAusserordentlicherAnspruch;
    }

    public getYearEinschulung(): number {
        if (this.gesuchModelManager && this.gesuchModelManager.getGesuchsperiodeBegin()) {
            return this.gesuchModelManager.getGesuchsperiodeBegin().year();
        }
        return undefined;
    }

    public getTextFachstelleKorrekturJA(): string {
        return this.getContainer().kindGS?.pensumFachstellen.map(fachstelle => {
                const vonText = DateUtil.momentToLocalDateFormat(fachstelle.gueltigkeit.gueltigAb, 'DD.MM.YYYY');
                const bisText = fachstelle.gueltigkeit.gueltigBis ?
                    DateUtil.momentToLocalDateFormat(fachstelle.gueltigkeit.gueltigBis, 'DD.MM.YYYY') :
                    CONSTANTS.END_OF_TIME_STRING;
                const integrationTyp = this.$translate.instant(fachstelle.integrationTyp);
                const fachstellenname = fachstelle.fachstelle ? fachstelle.fachstelle.name : '';
                return this.$translate.instant('JA_KORREKTUR_FACHSTELLE', {
                    name: fachstellenname,
                    integration: integrationTyp,
                    pensum: fachstelle.pensum,
                    von: vonText,
                    bis: bisText,
                });
            }).reduce((previousValue, currentValue) => previousValue.concat('\n', currentValue), '')
            || this.$translate.instant('LABEL_KEINE_ANGABE');

    }

    private initEmptyKind(kindNumber: number): TSKindContainer {
        const tsKindContainer = new TSKindContainer();
        tsKindContainer.kindGS = undefined;
        tsKindContainer.kindJA = new TSKind();
        tsKindContainer.kindNummer = kindNumber;
        return tsKindContainer;
    }

    /**
     * Returns true if the Kind has a Betreuung
     */
    public hasKindBetreuungen(): boolean {
        return this.model.betreuungen && this.model.betreuungen.length > 0;
    }

    public hasAngebotBGOnly(): boolean {
        return this.getGesuch()
            && this.getGesuch().dossier
            && this.getGesuch().dossier.gemeinde
            && this.getGesuch().dossier.gemeinde.angebotBG
            && !(this.getGesuch().dossier.gemeinde.angebotTS && !this.getGesuch().dossier.gemeinde.nurLats);
    }

    public hasAngebotTSOnly(): boolean {
        return this.getGesuch()
            && this.getGesuch().dossier
            && this.getGesuch().dossier.gemeinde
            && this.getGesuch().dossier.gemeinde.angebotTS && !this.getGesuch().dossier.gemeinde.nurLats
            && !this.getGesuch().dossier.gemeinde.angebotBG;
    }

    public hasAngebotBGAndTS(): boolean {
        return this.getGesuch()
            && this.getGesuch().dossier
            && this.getGesuch().dossier.gemeinde
            && this.getGesuch().dossier.gemeinde.angebotTS && !this.getGesuch().dossier.gemeinde.nurLats
            && this.getGesuch().dossier.gemeinde.angebotBG;
    }

    public showKeinSelbstbehaltDurchGemeinde(): boolean {
        return this.model.keinSelbstbehaltDurchGemeinde !== null
            && this.authServiceRS.isOneOfRoles(TSRoleUtil.getMandantRoles());
    }

    public deleteZemisNummer(): void {
        if (!this.getModel().ausAsylwesen) {
            this.getModel().zemisNummer = null;
        }
    }

    private isGeburtsdatumInZunkunft(): boolean {
        return EbeguUtil.isNotNullOrUndefined(this.getModel().geburtsdatum) &&
            this.getModel().geburtsdatum.isAfter(moment());
    }

    public showGeburtsdatumWarning(): boolean {
        return this.isGeburtsdatumInZunkunft() || this.getModel().zukunftigeGeburtsdatum;
    }

    private getEinstellungKontingentierung(): void {
        this.kontingentierungEnabled = this.gesuchModelManager.gemeindeKonfiguration.konfigKontingentierung;
    }

    public geburtsdatumChanged(): void {
        this.kinderabzugExchangeService.triggerGeburtsdatumChanged(this.getModel().geburtsdatum);
    }

    public familienErgaenzendeBetreuungChanged(): void {
        this.kinderabzugExchangeService.triggerFamilienErgaenzendeBetreuungChanged();
    }

    public isFachstellenTypLuzern(): boolean {
        return this.fachstellenTyp === TSFachstellenTyp.LUZERN;
    }

    public getMessageKeyWarnFachstellenPensumOverlap(): string {
        return this.fachstellenPensumOverlapsMessageKey;
    }

    public setFachstellenPensumOverlaps(messageKeyWarn: string): void {
        this.fachstellenPensumOverlapsMessageKey = messageKeyWarn;
    }

    public showWarningPensumOverlaps(): boolean {
        return EbeguUtil.isNotNullOrUndefined(this.fachstellenPensumOverlapsMessageKey);
    }

    private loadEinstellungen(): void {
        this.einstellungRS.getAllEinstellungenBySystemCached(this.gesuchModelManager.getGesuchsperiode().id)
            .pipe(takeUntil(this.unsubscribe$))
            .subscribe(einstellungen => {
                this.loadEinstellungZemisDisabled(einstellungen);
                this.loadEinstellungMaxAusserordentlicherAnspruch(einstellungen);
                this.loadEinstellungKinderabzugTyp(einstellungen);
                this.loadEinstellungAnspruchUnabhaengig(einstellungen);
                this.loadEinstellungSpracheAmtsprache(einstellungen);
                this.loadEinstellungFachstellenTyp(einstellungen);
                this.loadEinstellungAusserordentlicherAnspruchTyp(einstellungen);
                this.loadEinstellungSozialeIntegrationBisSchulstufe(einstellungen);
                this.loadEinstellungSprachlicheIntegrationBisSchulstufe(einstellungen);
            }, error => LOG.error(error));
    }

    private loadEinstellungSpracheAmtsprache(einstellungen: TSEinstellung[]): void {
        const einstellung = einstellungen
            .find(e => e.key === TSEinstellungKey.SPRACHE_AMTSPRACHE_DISABLED);
        this.isSpracheAmtspracheDisabled = einstellung.value === 'true';
        if (this.isSpracheAmtspracheDisabled) {
            this.getModel().sprichtAmtssprache = true;
        }
    }

    private loadEinstellungZemisDisabled(einstellungen: TSEinstellung[]): void {
        const einstellung = einstellungen
            .find(e => e.key === TSEinstellungKey.ZEMIS_DISABLED);
        this.isZemisDeaktiviert = einstellung.value === 'true';
    }

    private loadEinstellungMaxAusserordentlicherAnspruch(einstellungen: TSEinstellung[]): void {
        const einstellung = einstellungen
            .find(e => e.key === TSEinstellungKey.FKJV_MAX_PENSUM_AUSSERORDENTLICHER_ANSPRUCH);
        this.maxPensumAusserordentlicherAnspruch = einstellung.value;
    }

    private loadEinstellungKinderabzugTyp(einstellungen: TSEinstellung[]): void {
        const einstellung = einstellungen
            .find(e => e.key === TSEinstellungKey.KINDERABZUG_TYP);
        this.kinderabzugTyp = this.ebeguRestUtil.parseKinderabzugTyp(einstellung.value);
    }

    private loadEinstellungAnspruchUnabhaengig(einstellungen: TSEinstellung[]): void {
        const einstellungAbhaengigkeitAnspruchBeschaeftigung = this.ebeguRestUtil
            .parseAnspruchBeschaeftigungAbhaengigkeitTyp(einstellungen
                .find(e => e.key === TSEinstellungKey.ABHAENGIGKEIT_ANSPRUCH_BESCHAEFTIGUNGPENSUM));

        this.anspruchUnabhaengingVomBeschaeftigungspensum =
            einstellungAbhaengigkeitAnspruchBeschaeftigung ===
            TSAnspruchBeschaeftigungAbhaengigkeitTyp.UNABHAENGING;
    }

    private loadEinstellungFachstellenTyp(einstellungen: TSEinstellung[]): void {
        const einstellung = einstellungen
            .find(e => e.key === TSEinstellungKey.FACHSTELLEN_TYP);
        this.fachstellenTyp = this.ebeguRestUtil.parseFachstellenTyp(einstellung.value);

        this.integrationTypes = this.fachstellenTyp === TSFachstellenTyp.LUZERN ?
            [TSIntegrationTyp.SPRACHLICHE_INTEGRATION, TSIntegrationTyp.ZUSATZLEISTUNG_INTEGRATION] :
            [TSIntegrationTyp.SOZIALE_INTEGRATION, TSIntegrationTyp.SPRACHLICHE_INTEGRATION];
    }

    private loadEinstellungAusserordentlicherAnspruchTyp(einstellungen: TSEinstellung[]): void {
        const einstellung = einstellungen
            .find(e => e.key === TSEinstellungKey.AUSSERORDENTLICHER_ANSPRUCH_RULE);
        this.ausserodentlicherAnspruchTyp = this.ebeguRestUtil.parseAusserordentlicherAnspruchTyp(einstellung.value);
    }

    private loadEinstellungSozialeIntegrationBisSchulstufe(einstellungen: TSEinstellung[]): void {
        const einstellung = einstellungen
            .find(e => e.key === TSEinstellungKey.FKJV_SOZIALE_INTEGRATION_BIS_SCHULSTUFE);
        this.sozialeIntegrationBisSchulstufe = this.ebeguRestUtil.parseEinschulungTyp(einstellung.value);
    }

    private loadEinstellungSprachlicheIntegrationBisSchulstufe(einstellungen: TSEinstellung[]): void {
        const einstellung = einstellungen
            .find(e => e.key === TSEinstellungKey.SPRACHLICHE_INTEGRATION_BIS_SCHULSTUFE);
        this.sprachlicheIntegrationBisSchulstufe = this.ebeguRestUtil.parseEinschulungTyp(einstellung.value);
    }

    public isEinschulungTypObligatorischerKindergarten(): boolean {
        return this.getModel().einschulungTyp === TSEinschulungTyp.OBLIGATORISCHER_KINDERGARTEN;
    }

    public einschulungTypChanged(): void {
        if (this.getModel().einschulungTyp !== TSEinschulungTyp.OBLIGATORISCHER_KINDERGARTEN) {
            this.getModel().keinPlatzInSchulhort = false;
        }
    }

    public isGeschlechtOfKindRequired(): boolean {
        return new KindGeschlechtVisitor().process(this.mandant);
    }

    public isFachstellenActivated(): boolean {
        return this.fachstellenTyp !== TSFachstellenTyp.KEINE;
    }

    public checkFachstellenValidity(): {pensumFachstelle: TSPensumFachstelle; error: string}[] {
        const errors: { pensumFachstelle: TSPensumFachstelle; error: string }[] = [];

        if (EbeguUtil.isNullOrUndefined(this.getPensumFachstellen()) || this.getPensumFachstellen().length < 1) {
            return errors;
        }
        const einschulungstypen = new EinschulungTypesVisitor().process(this.mandant);
        const ordinalitaetKind =
            einschulungstypen.findIndex(einschulungstyp => einschulungstyp === this.getModel().einschulungTyp);
        if (ordinalitaetKind < 0) {
            throw new Error(`Einschulungstyp ${this.getModel().einschulungTyp} not found for ${this.mandant}`);
        }

        for (const pensumFachstelle of this.getPensumFachstellen()) {
            let ordinalitaetEinstellung;
            switch (pensumFachstelle.integrationTyp) {
                case TSIntegrationTyp.SOZIALE_INTEGRATION:
                    ordinalitaetEinstellung =
                        einschulungstypen.findIndex(einschulungstyp => einschulungstyp === this.sozialeIntegrationBisSchulstufe);
                    if (ordinalitaetKind > ordinalitaetEinstellung) {
                        errors.push({
                            pensumFachstelle,
                            error: this.$translate.instant('PENSUM_FACHSTELLE_INVALID_FACHSTELLEN_SOZIAL',
                                {stufe: this.$translate.instant(this.sozialeIntegrationBisSchulstufe)})
                        });
                    }
                    break;
                case TSIntegrationTyp.SPRACHLICHE_INTEGRATION:
                case TSIntegrationTyp.ZUSATZLEISTUNG_INTEGRATION:
                    ordinalitaetEinstellung =
                        einschulungstypen.findIndex(einschulungstyp =>
                            einschulungstyp === this.sprachlicheIntegrationBisSchulstufe);
                    if (ordinalitaetKind > ordinalitaetEinstellung) {
                        errors.push({
                            pensumFachstelle,
                            error: this.$translate.instant('PENSUM_FACHSTELLE_INVALID_FACHSTELLEN_SPRACHLICH')
                        });
                    }
                    break;
                default:
                    throw new Error(`Unhandled TSIntegrationTyp ${pensumFachstelle.integrationTyp}`);
            }
        }
        return errors;
    }
}
