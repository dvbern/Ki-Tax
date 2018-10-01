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

import {StateService} from '@uirouter/core';
import {IComponentOptions} from 'angular';
import {TSWizardStepName} from '../../../models/enums/TSWizardStepName';
import TSBetreuung from '../../../models/TSBetreuung';
import BerechnungsManager from '../../service/berechnungsManager';
import GesuchModelManager from '../../service/gesuchModelManager';
import WizardStepManager from '../../service/wizardStepManager';
import AbstractGesuchViewController from '../abstractGesuchView';
import IFormController = angular.IFormController;
import IScope = angular.IScope;
import ITimeoutService = angular.ITimeoutService;

export class BetreuungMitteilungViewComponentConfig implements IComponentOptions {
    public transclude = false;
    public template = require('./betreuungMitteilungView.html');
    public controller = BetreuungMitteilungViewController;
    public controllerAs = 'vm';
}

export class BetreuungMitteilungViewController extends AbstractGesuchViewController<TSBetreuung> {

    public static $inject = ['$state', 'GesuchModelManager', '$scope', 'BerechnungsManager', 'WizardStepManager', '$timeout'];

    public form: IFormController;

    public constructor(private readonly $state: StateService, gesuchModelManager: GesuchModelManager, $scope: IScope,
                       berechnungsManager: BerechnungsManager, wizardStepManager: WizardStepManager, $timeout: ITimeoutService) {
        super(gesuchModelManager, berechnungsManager, wizardStepManager, $scope, TSWizardStepName.BETREUUNG, $timeout);
    }

    public cancel(): void {
        this.$state.go('gesuch.betreuungen', {gesuchId: this.getGesuchId()});
    }
}
