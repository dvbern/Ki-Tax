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
import {EinstellungRS} from '../../../admin/service/einstellungRS.rest';
import {CONSTANTS} from '../../../app/core/constants/CONSTANTS';
import {ErrorService} from '../../../app/core/errors/service/ErrorService';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {getTSEinschulungTypValues, TSEinschulungTyp} from '../../../models/enums/TSEinschulungTyp';
import {TSEinstellungKey} from '../../../models/enums/TSEinstellungKey';
import {TSGeschlecht} from '../../../models/enums/TSGeschlecht';
import {TSIntegrationTyp} from '../../../models/enums/TSIntegrationTyp';
import {getTSKinderabzugValues, TSKinderabzug} from '../../../models/enums/TSKinderabzug';
import {TSRole} from '../../../models/enums/TSRole';
import {TSWizardStepName} from '../../../models/enums/TSWizardStepName';
import {TSEinstellung} from '../../../models/TSEinstellung';
import {TSFachstelle} from '../../../models/TSFachstelle';
import {TSKind} from '../../../models/TSKind';
import {TSKindContainer} from '../../../models/TSKindContainer';
import {TSPensumAusserordentlicherAnspruch} from '../../../models/TSPensumAusserordentlicherAnspruch';
import {TSPensumFachstelle} from '../../../models/TSPensumFachstelle';
import {DateUtil} from '../../../utils/DateUtil';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {EnumEx} from '../../../utils/EnumEx';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {IKindStateParams} from '../../gesuch.route';
import {BerechnungsManager} from '../../service/berechnungsManager';
import {GesuchModelManager} from '../../service/gesuchModelManager';
import {GlobalCacheService} from '../../service/globalCacheService';
import {WizardStepManager} from '../../service/wizardStepManager';
import {AbstractGesuchViewController} from '../abstractGesuchView';
import IPromise = angular.IPromise;
import IQService = angular.IQService;
import IScope = angular.IScope;
import ITimeoutService = angular.ITimeoutService;
import ITranslateService = angular.translate.ITranslateService;

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
    ];

    public readonly CONSTANTS: any = CONSTANTS;
    public integrationTypes: Array<string>;
    public geschlechter: Array<string>;
    public kinderabzugValues: Array<TSKinderabzug>;
    public einschulungTypValues: Array<TSEinschulungTyp>;
    public showFachstelle: boolean;
    public showFachstelleGS: boolean;
    public showAusserordentlicherAnspruch: boolean;
    // der ausgewaehlte fachstelleId wird hier gespeichert und dann in die entsprechende Fachstelle umgewandert
    public fachstelleId: string;
    public allowedRoles: ReadonlyArray<TSRole>;
    public minValueAllowed: number = 0;
    public maxValueAllowed: number = 100;
    public kontingentierungEnabled: boolean;

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
        this.initViewModel();
        this.allowedRoles = this.TSRoleUtil.getAllRolesButTraegerschaftInstitution();
    }

    private initViewModel(): void {
        this.integrationTypes = EnumEx.getNames(TSIntegrationTyp);
        this.geschlechter = EnumEx.getNames(TSGeschlecht);
        this.kinderabzugValues = getTSKinderabzugValues();
        this.einschulungTypValues = getTSEinschulungTypValues();
        this.loadEinstellungenForIntegration();
        this.initFachstelle();
        this.initAusserordentlicherAnspruch();
        this.getEinstellungKontingentierung();
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
                    .getAmtsspracheAsString(this.gesuchModelManager.gemeindeStammdaten, this.$translate)
            });
    }

    private initFachstelle(): void {
        this.showFachstelle = !!(this.model.kindJA.pensumFachstelle);
        this.showFachstelleGS = !!(this.model.kindGS && this.model.kindGS.pensumFachstelle);
        if (this.getPensumFachstelle() && this.getPensumFachstelle().fachstelle) {
            this.fachstelleId = this.getPensumFachstelle().fachstelle.id;
        }
        if (!this.gesuchModelManager.getFachstellenAnspruchList()
            || this.gesuchModelManager.getFachstellenAnspruchList().length <= 0) {
            this.gesuchModelManager.updateFachstellenAnspruchList();
        }
    }

    private getEinstellungenFachstelle(
        minValueEinstellungKey: TSEinstellungKey,
        maxValueEinstellungKey: TSEinstellungKey
    ): void {
        this.einstellungRS.getAllEinstellungenBySystemCached(
            this.gesuchModelManager.getGesuchsperiode().id
        ).then((response: TSEinstellung[]) => {
            response.filter(r => r.key === minValueEinstellungKey)
                .forEach(value => {
                    this.minValueAllowed = Number(value.value);
                });
            response.filter(r => r.key === maxValueEinstellungKey)
                .forEach(value => {
                    this.maxValueAllowed = Number(value.value);
                });

            if (this.isOnlyOneValueAllowed()) {
                this.getModel().pensumFachstelle.pensum = this.minValueAllowed;
            }
        });
    }

    private isOnlyOneValueAllowed(): boolean {
        return this.minValueAllowed === this.maxValueAllowed;
    }

    public loadEinstellungenForIntegration(): void {
        if (!this.model.extractPensumFachstelle()) {
            return;
        }
        if (this.model.extractPensumFachstelle().integrationTyp === TSIntegrationTyp.SOZIALE_INTEGRATION) {
            this.getEinstellungenFachstelle(
                TSEinstellungKey.FACHSTELLE_MIN_PENSUM_SOZIALE_INTEGRATION,
                TSEinstellungKey.FACHSTELLE_MAX_PENSUM_SOZIALE_INTEGRATION,
            );
        } else if (this.model.extractPensumFachstelle().integrationTyp === TSIntegrationTyp.SPRACHLICHE_INTEGRATION) {
            this.getEinstellungenFachstelle(
                TSEinstellungKey.FACHSTELLE_MIN_PENSUM_SPRACHLICHE_INTEGRATION,
                TSEinstellungKey.FACHSTELLE_MAX_PENSUM_SPRACHLICHE_INTEGRATION,
            );
        }
    }

    private initAusserordentlicherAnspruch(): void {
        this.showAusserordentlicherAnspruch = !!(this.model.kindJA.pensumAusserordentlicherAnspruch);
    }

    public save(): IPromise<TSKindContainer> {
        if (!this.isGesuchValid()) {
            return undefined;
        }

        if (!this.form.$dirty) {
            // If there are no changes in form we don't need anything to update on Server and we could return the
            // promise immediately
            return this.$q.when(this.model);
        }

        this.errorService.clearAll();
        return this.gesuchModelManager.saveKind(this.model);
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

    public setSelectedFachsstelle(): void {
        const fachstellenList = this.getFachstellenList();
        const found = fachstellenList.find(f => f.id === this.fachstelleId);
        if (found) {
            this.getModel().pensumFachstelle.fachstelle = found;
        }
    }

    public showFachstelleClicked(): void {
        if (this.showFachstelle) {
            this.getModel().pensumFachstelle = new TSPensumFachstelle();
        } else {
            this.resetFachstelleFields();
        }
    }

    public showAusserordentlicherAnspruchCheckbox(): boolean {
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
            this.resetFachstelleFields();
        }
    }

    private resetFachstelleFields(): void {
        this.fachstelleId = undefined;
        this.getModel().pensumFachstelle = undefined;
    }

    public getFachstellenList(): Array<TSFachstelle> {
        const fachstellen = this.gesuchModelManager.getFachstellenAnspruchList();
        if (this.getModel().pensumFachstelle !== null
            && this.getModel().pensumFachstelle.fachstelle
            && this.getModel().pensumFachstelle.fachstelle.name.toString() === 'KINDES_ERWACHSENEN_SCHUTZBEHOERDE') {
            return fachstellen.concat(this.getModel().pensumFachstelle.fachstelle);
        }
        return fachstellen;
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

    public getPensumFachstelle(): TSPensumFachstelle {
        if (this.getModel()) {
            return this.getModel().pensumFachstelle;
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

    public showAusAsylwesen(): boolean {
        // Checkbox wird nur angezeigt, wenn das Kind externe Betreuung hat
        return this.getModel().familienErgaenzendeBetreuung;
    }

    public showZemisNummer(): boolean {
        return this.showAusAsylwesen() && this.getModel().ausAsylwesen;
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
        if (this.getContainer().kindGS && this.getContainer().kindGS.pensumFachstelle) {
            const fachstelle = this.getContainer().kindGS.pensumFachstelle;
            const vonText = DateUtil.momentToLocalDateFormat(fachstelle.gueltigkeit.gueltigAb, 'DD.MM.YYYY');
            const bisText = fachstelle.gueltigkeit.gueltigBis ?
                DateUtil.momentToLocalDateFormat(fachstelle.gueltigkeit.gueltigBis, 'DD.MM.YYYY') :
                CONSTANTS.END_OF_TIME_STRING;
            const integrationTyp = this.$translate.instant(fachstelle.integrationTyp);
            return this.$translate.instant('JA_KORREKTUR_FACHSTELLE', {
                name: fachstelle.fachstelle.name,
                integration: integrationTyp,
                pensum: fachstelle.pensum,
                von: vonText,
                bis: bisText,
            });
        }

        return this.$translate.instant('LABEL_KEINE_ANGABE');
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
            && !this.getGesuch().dossier.gemeinde.angebotTS;
    }

    public hasAngebotTSOnly(): boolean {
        return this.getGesuch()
            && this.getGesuch().dossier
            && this.getGesuch().dossier.gemeinde
            && this.getGesuch().dossier.gemeinde.angebotTS
            && !this.getGesuch().dossier.gemeinde.angebotBG;
    }

    public hasAngebotBGAndTS(): boolean {
        return this.getGesuch()
            && this.getGesuch().dossier
            && this.getGesuch().dossier.gemeinde
            && this.getGesuch().dossier.gemeinde.angebotTS
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

    private getEinstellungKontingentierung(): void {
        this.kontingentierungEnabled = this.gesuchModelManager.gemeindeKonfiguration.konfigKontingentierung;
    }
}
