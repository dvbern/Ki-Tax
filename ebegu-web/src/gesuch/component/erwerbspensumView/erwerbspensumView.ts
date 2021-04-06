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
import {CONSTANTS} from '../../../app/core/constants/CONSTANTS';
import {ErrorService} from '../../../app/core/errors/service/ErrorService';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {getTSTaetigkeit, getTSTaetigkeitWithFreiwilligenarbeit, TSTaetigkeit} from '../../../models/enums/TSTaetigkeit';
import {TSWizardStepName} from '../../../models/enums/TSWizardStepName';
import {TSErwerbspensum} from '../../../models/TSErwerbspensum';
import {TSErwerbspensumContainer} from '../../../models/TSErwerbspensumContainer';
import {TSGesuchstellerContainer} from '../../../models/TSGesuchstellerContainer';
import {TSUnbezahlterUrlaub} from '../../../models/TSUnbezahlterUrlaub';
import {DateUtil} from '../../../utils/DateUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {IErwerbspensumStateParams} from '../../gesuch.route';
import {BerechnungsManager} from '../../service/berechnungsManager';
import {GesuchModelManager} from '../../service/gesuchModelManager';
import {WizardStepManager} from '../../service/wizardStepManager';
import {AbstractGesuchViewController} from '../abstractGesuchView';
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
        '$timeout',
    ];

    public gesuchsteller: TSGesuchstellerContainer;
    public patternPercentage: string;
    public hasUnbezahlterUrlaub: boolean;
    public hasUnbezahlterUrlaubGS: boolean;

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
        this.initUnbezahlterUrlaub();
    }

    public getTaetigkeitenList(): Array<TSTaetigkeit> {
        return this.gesuchModelManager.gemeindeKonfiguration.konfigZusaetzlicherAnspruchFreiwilligenarbeitEnabled ?
            getTSTaetigkeitWithFreiwilligenarbeit() : getTSTaetigkeit();
    }

    public save(): IPromise<any> {
        if (!this.isGesuchValid()) {
            return undefined;
        }

        if (!this.form.$dirty) {
            // If there are no changes in form we don't need anything to update on Server and we could return the
            // promise immediately
            return this.$q.when(this.model);
        }
        this.errorService.clearAll();
        return this.gesuchModelManager.saveErwerbspensum(this.gesuchsteller, this.model);
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

    public taetigkeitChanged(): void {
        if (!this.isUnbezahlterUrlaubVisible()) {
            this.model.erwerbspensumJA.unbezahlterUrlaub = undefined;
            this.hasUnbezahlterUrlaub = false;
        }
    }

    public erwerbspensumDisabled(): boolean {
        // Disabled wenn Mutation, ausser bei Bearbeiter Jugendamt oder Schulamt
        if (this.model && this.model.erwerbspensumJA) {
            return this.model.erwerbspensumJA.vorgaengerId
                && !this.authServiceRS.isOneOfRoles(TSRoleUtil.getAdministratorOrAmtRole());
        }
        return false;
    }

    public isUnbezahlterUrlaubVisible(): boolean {
        return this.model && this.model.erwerbspensumJA
            && (this.model.erwerbspensumJA.taetigkeit === TSTaetigkeit.ANGESTELLT
                || this.model.erwerbspensumJA.taetigkeit === TSTaetigkeit.SELBSTAENDIG);
    }

    public isUnbezahlterUrlaubDisabled(): boolean {
        return !this.isUnbezahlterUrlaubVisible() || this.isGesuchReadonly();
    }

    private initUnbezahlterUrlaub(): void {
        this.hasUnbezahlterUrlaub = !!(this.model && this.model.erwerbspensumJA
            && this.model.erwerbspensumJA.unbezahlterUrlaub);
        this.hasUnbezahlterUrlaubGS = !!(this.model && this.model.erwerbspensumGS
            && this.model.erwerbspensumGS.unbezahlterUrlaub);
    }

    public unbezahlterUrlaubClicked(): void {
        this.model.erwerbspensumJA.unbezahlterUrlaub =
            this.hasUnbezahlterUrlaub ? new TSUnbezahlterUrlaub() : undefined;
    }

    public getTextUnbezahlterUrlaubKorrekturJA(): string {
        if (this.model.erwerbspensumGS && this.model.erwerbspensumGS.unbezahlterUrlaub) {
            const urlaub = this.model.erwerbspensumGS.unbezahlterUrlaub;
            const vonText = DateUtil.momentToLocalDateFormat(urlaub.gueltigkeit.gueltigAb, 'DD.MM.YYYY');
            const bisText = urlaub.gueltigkeit.gueltigBis ?
                DateUtil.momentToLocalDateFormat(urlaub.gueltigkeit.gueltigBis, 'DD.MM.YYYY') :
                CONSTANTS.END_OF_TIME_STRING;
            return this.$translate.instant('JA_KORREKTUR_UNBEZAHLTER_URLAUB', {
                von: vonText,
                bis: bisText,
            });
        }
        return this.$translate.instant('LABEL_KEINE_ANGABE');
    }
}
