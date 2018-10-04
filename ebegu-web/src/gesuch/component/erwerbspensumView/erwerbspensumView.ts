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

import {IComponentOptions, IPromise, IQService, IScope, ITimeoutService} from 'angular';
import {EinstellungRS} from '../../../admin/service/einstellungRS.rest';
import {CONSTANTS} from '../../../app/core/constants/CONSTANTS';
import ErrorService from '../../../app/core/errors/service/ErrorService';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import {TSCacheTyp} from '../../../models/enums/TSCacheTyp';
import {TSEinstellungKey} from '../../../models/enums/TSEinstellungKey';
import {getTSTaetigkeit, TSTaetigkeit} from '../../../models/enums/TSTaetigkeit';
import {TSWizardStepName} from '../../../models/enums/TSWizardStepName';
import {
    getTSZuschlagsgruendeForGS,
    getTSZuschlagsgrunde,
    TSZuschlagsgrund,
} from '../../../models/enums/TSZuschlagsgrund';
import TSEinstellung from '../../../models/TSEinstellung';
import TSErwerbspensum from '../../../models/TSErwerbspensum';
import TSErwerbspensumContainer from '../../../models/TSErwerbspensumContainer';
import TSGesuchstellerContainer from '../../../models/TSGesuchstellerContainer';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {IErwerbspensumStateParams} from '../../gesuch.route';
import BerechnungsManager from '../../service/berechnungsManager';
import GesuchModelManager from '../../service/gesuchModelManager';
import GlobalCacheService from '../../service/globalCacheService';
import WizardStepManager from '../../service/wizardStepManager';
import AbstractGesuchViewController from '../abstractGesuchView';
import ITranslateService = angular.translate.ITranslateService;

export class ErwerbspensumViewComponentConfig implements IComponentOptions {
    public transclude = false;
    public bindings = {};
    public template = require('./erwerbspensumView.html');
    public controller = ErwerbspensumViewController;
    public controllerAs = 'vm';
}

export class ErwerbspensumViewController extends AbstractGesuchViewController<TSErwerbspensumContainer> {

    public static $inject: string[] = [
        '$stateParams',
        'GesuchModelManager',
        'BerechnungsManager',
        '$scope',
        'ErrorService',
        'AuthServiceRS',
        'WizardStepManager',
        '$q',
        '$translate',
        'EinstellungRS',
        'GlobalCacheService',
        '$timeout',
    ];

    public gesuchsteller: TSGesuchstellerContainer;
    public patternPercentage: string;
    public maxZuschlagsprozent: number = 100;

    public constructor(
        $stateParams: IErwerbspensumStateParams,
        gesuchModelManager: GesuchModelManager,
        berechnungsManager: BerechnungsManager,
        $scope: IScope,
        private readonly errorService: ErrorService,
        private readonly authServiceRS: AuthServiceRS,
        wizardStepManager: WizardStepManager,
        private readonly $q: IQService,
        private readonly $translate: ITranslateService,
        private readonly einstellungRS: EinstellungRS,
        private readonly globalCacheService: GlobalCacheService,
        $timeout: ITimeoutService,
    ) {
        super(gesuchModelManager,
            berechnungsManager,
            wizardStepManager,
            $scope,
            TSWizardStepName.ERWERBSPENSUM,
            $timeout);
        this.patternPercentage = CONSTANTS.PATTERN_PERCENTAGE;
        this.gesuchModelManager.setGesuchstellerNumber(parseInt($stateParams.gesuchstellerNumber, 10));
        this.gesuchsteller = this.gesuchModelManager.getStammdatenToWorkWith();
        if (this.gesuchsteller) {
            if ($stateParams.erwerbspensumNum) {
                const ewpNum = parseInt($stateParams.erwerbspensumNum, 10) || 0;
                this.model = angular.copy(this.gesuchsteller.erwerbspensenContainer[ewpNum]);
            } else {
                // wenn erwerbspensum nummer nicht definiert ist heisst dass, das wir ein neues erstellen sollten
                this.model = this.initEmptyEwpContainer();
            }
        } else {
            errorService.addMesageAsError('Unerwarteter Zustand: Gesuchsteller unbekannt');
            console.log('kein gesuchsteller gefunden');
        }
        this.einstellungRS.getAllEinstellungenBySystemCached(
            this.gesuchModelManager.getGesuchsperiode().id,
            this.globalCacheService.getCache(TSCacheTyp.EBEGU_EINSTELLUNGEN)).then((response: TSEinstellung[]) => {
            const found = response.find(r => r.key === TSEinstellungKey.PARAM_MAXIMALER_ZUSCHLAG_ERWERBSPENSUM);
            if (found) {
                // max Wert für Zuschlag Erwerbspensum
                this.maxZuschlagsprozent = Number(found.value);
            }
        });
    }

    public getTaetigkeitenList(): Array<TSTaetigkeit> {
        return getTSTaetigkeit();
    }

    public getZuschlagsgrundList(): Array<TSZuschlagsgrund> {
        return this.authServiceRS.isOneOfRoles(TSRoleUtil.getGesuchstellerOnlyRoles()) ?
            getTSZuschlagsgruendeForGS() :
            getTSZuschlagsgrunde();
    }

    /**
     * Beim speichern wird geschaut ob Zuschlagsgrund noetig ist, wenn nicht zuruecksetzten
     */
    private maybeResetZuschlagsgrund(erwerbspensum: TSErwerbspensumContainer): void {
        if (erwerbspensum && !erwerbspensum.erwerbspensumJA.zuschlagZuErwerbspensum) {
            erwerbspensum.erwerbspensumJA.zuschlagsprozent = undefined;
            erwerbspensum.erwerbspensumJA.zuschlagsgrund = undefined;
        }
    }

    public save(): IPromise<any> {
        if (this.isGesuchValid()) {

            if (!this.form.$dirty) {
                // If there are no changes in form we don't need anything to update on Server and we could return the
                // promise immediately
                return this.$q.when(this.model);
            }
            this.maybeResetZuschlagsgrund(this.model);
            this.errorService.clearAll();
            return this.gesuchModelManager.saveErwerbspensum(this.gesuchsteller, this.model);
        }
        return undefined;
    }

    public cancel(): void {
        this.form.$setPristine();
    }

    private initEmptyEwpContainer(): TSErwerbspensumContainer {
        const ewp = new TSErwerbspensum();
        const ewpContainer = new TSErwerbspensumContainer();
        ewpContainer.erwerbspensumJA = ewp;
        return ewpContainer;

    }

    public viewZuschlag(): boolean {
        return this.model.erwerbspensumJA.taetigkeit === TSTaetigkeit.ANGESTELLT ||
            this.model.erwerbspensumJA.taetigkeit === TSTaetigkeit.AUSBILDUNG ||
            this.model.erwerbspensumJA.taetigkeit === TSTaetigkeit.SELBSTAENDIG;
    }

    public taetigkeitChanged(): void {
        if (this.viewZuschlag()) {
            return;
        }

        this.model.erwerbspensumJA.zuschlagZuErwerbspensum = false;
        this.model.erwerbspensumJA.zuschlagsprozent = undefined;
        this.model.erwerbspensumJA.zuschlagsgrund = undefined;
    }

    public erwerbspensumDisabled(): boolean {
        // Disabled wenn Mutation, ausser bei Bearbeiter Jugendamt oder Schulamt
        return this.model.erwerbspensumJA.vorgaengerId
            && !this.authServiceRS.isOneOfRoles(TSRoleUtil.getAdministratorOrAmtRole());
    }

    public getTextZuschlagErwerbspensumKorrekturJA(): string {
        if (!this.model.erwerbspensumGS || !this.model.erwerbspensumGS.zuschlagZuErwerbspensum) {
            return this.$translate.instant('LABEL_KEINE_ANGABE');
        }

        const ewp = this.model.erwerbspensumGS;
        const grundText = this.$translate.instant(ewp.zuschlagsgrund.toString());
        return this.$translate.instant('JA_KORREKTUR_ZUSCHLAG_ERWERBSPENSUM', {
            zuschlagsgrund: grundText,
            zuschlagsprozent: ewp.zuschlagsprozent,
        });
    }

    public getZuschlagHelpText(): string {
        return this.$translate.instant('ZUSCHLAGSGRUND_HELP', {
            maxzuschlag: this.maxZuschlagsprozent,
        });
    }

    public isZuschlagErwerbspensumConfigured(): boolean {
        // Wird aktuell ausgeblendet. Koennte aber spaeter von spezifischen Gemeinden einschaltet werden
        return false;
    }
}
