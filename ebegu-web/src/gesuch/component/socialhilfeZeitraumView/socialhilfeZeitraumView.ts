/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

import {IComponentOptions, IPromise, IQService, IScope, ITimeoutService} from 'angular';
import {ErrorService} from '../../../app/core/errors/service/ErrorService';
import {SocialhilfeZeitraumRS} from '../../../app/core/service/socialhilfeZeitraumRS.rest';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {TSWizardStepName} from '../../../models/enums/TSWizardStepName';
import {TSFamiliensituationContainer} from '../../../models/TSFamiliensituationContainer';
import {TSSocialhilfeZeitraum} from '../../../models/TSSocialhilfeZeitraum';
import {TSSocialhilfeZeitraumContainer} from '../../../models/TSSocialhilfeZeitraumContainer';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {ISocialhilfeZeitraumStateParams} from '../../gesuch.route';
import {BerechnungsManager} from '../../service/berechnungsManager';
import {GesuchModelManager} from '../../service/gesuchModelManager';
import {WizardStepManager} from '../../service/wizardStepManager';
import {AbstractGesuchViewController} from '../abstractGesuchView';
import ITranslateService = angular.translate.ITranslateService;

export class SocialhilfeZeitraumViewComponentConfig implements IComponentOptions {
    public transclude = false;
    public bindings = {};
    public template = require('./socialhilfeZeitraumView.html');
    public controller = SocialhilfeZeitraumViewController;
    public controllerAs = 'vm';
}

export class SocialhilfeZeitraumViewController extends AbstractGesuchViewController<TSSocialhilfeZeitraumContainer> {

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
        'SocialhilfeZeitraumRS',
    ];

    public familiensituation: TSFamiliensituationContainer;

    public constructor(
        $stateParams: ISocialhilfeZeitraumStateParams,
        gesuchModelManager: GesuchModelManager,
        berechnungsManager: BerechnungsManager,
        $scope: IScope,
        private readonly errorService: ErrorService,
        private readonly authServiceRS: AuthServiceRS,
        wizardStepManager: WizardStepManager,
        private readonly $q: IQService,
        private readonly $translate: ITranslateService,
        $timeout: ITimeoutService,
        private readonly socialhilfeZeitraumRS: SocialhilfeZeitraumRS,
    ) {
        super(gesuchModelManager,
            berechnungsManager,
            wizardStepManager,
            $scope,
            TSWizardStepName.SOCIALHILFEZEITRAEUME,
            $timeout);

        this.familiensituation = this.gesuchModelManager.getGesuch().familiensituationContainer;
        if (this.familiensituation) {
            if ($stateParams.socialhilfeZeitraumNum) {
                const ewpNum = parseInt($stateParams.socialhilfeZeitraumNum, 10) || 0;
                this.model = angular.copy(this.familiensituation.socialhilfeZeitraumContainers[ewpNum]);
            } else {
                this.model = this.initEmptyShZContainer();
            }
        } else {
            errorService.addMesageAsError('Unerwarteter Zustand: Familiensituation unbekannt');
            console.log('kein Familiensituation gefunden');
        }
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
        return this.saveSocialhilfeZeitraum(this.familiensituation, this.model);
    }

    public saveSocialhilfeZeitraum(
        familiensituation: TSFamiliensituationContainer,
        socialhilfeZeitraum: TSSocialhilfeZeitraumContainer,
    ): IPromise<TSSocialhilfeZeitraumContainer> {
        if (socialhilfeZeitraum.id) {
            return this.socialhilfeZeitraumRS.saveSocialhilfeZeitraum(socialhilfeZeitraum, familiensituation.id)
                .then((response: TSSocialhilfeZeitraumContainer) => {
                    const i = EbeguUtil.getIndexOfElementwithID(socialhilfeZeitraum, familiensituation.socialhilfeZeitraumContainers);
                    if (i >= 0) {
                        familiensituation.socialhilfeZeitraumContainers[i] = socialhilfeZeitraum;
                    }
                    return response;
                });
        }
        return this.socialhilfeZeitraumRS.saveSocialhilfeZeitraum(socialhilfeZeitraum, familiensituation.id)
            .then((storedSocialhilfeZeitraum: TSSocialhilfeZeitraumContainer) => {
                familiensituation.socialhilfeZeitraumContainers.push(storedSocialhilfeZeitraum);
                return storedSocialhilfeZeitraum;
            });
    }

    public cancel(): void {
        this.form.$setPristine();
    }

    private initEmptyShZContainer(): TSSocialhilfeZeitraumContainer {
        const shz = new TSSocialhilfeZeitraum();
        const shzContainer = new TSSocialhilfeZeitraumContainer();
        shzContainer.socialhilfeZeitraumJA = shz;
        return shzContainer;

    }

    public socialhilfeZeitraumDisabled(): boolean {
        // Disabled wenn Mutation, ausser bei Bearbeiter Jugendamt oder Schulamt
        if (this.model && this.model.socialhilfeZeitraumJA) {
            return this.model.socialhilfeZeitraumJA.vorgaengerId
                && !this.authServiceRS.isOneOfRoles(TSRoleUtil.getAdministratorOrAmtRole());
        }
        return false;
    }

}
