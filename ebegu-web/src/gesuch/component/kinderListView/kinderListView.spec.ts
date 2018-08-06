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

import {ngServicesMock} from '../../../hybridTools/ngServicesMocks';
import {TSEingangsart} from '../../../models/enums/TSEingangsart';
import {EbeguWebGesuch} from '../../gesuch.module';
import GesuchModelManager from '../../service/gesuchModelManager';
import WizardStepManager from '../../service/wizardStepManager';
import {KinderListViewController} from './kinderListView';

describe('kinderListView', function () {

    let gesuchModelManager: GesuchModelManager;
    let scope: angular.IScope;
    let kinderListViewController: KinderListViewController;

    beforeEach(angular.mock.module(EbeguWebGesuch.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(angular.mock.inject(function ($injector: angular.auto.IInjectorService) {
        let wizardStepManager: WizardStepManager = $injector.get('WizardStepManager');
        spyOn(wizardStepManager, 'updateWizardStepStatus').and.returnValue({});
        gesuchModelManager = $injector.get('GesuchModelManager');
        spyOn(gesuchModelManager, 'initKinder').and.returnValue({});
        let $rootScope = $injector.get('$rootScope');
        scope = $rootScope.$new();
        kinderListViewController = new KinderListViewController(null, gesuchModelManager,
            null, null, null, wizardStepManager, scope, null, $injector.get('$timeout'));
    }));

    beforeEach(function () {
        gesuchModelManager.initGesuch(false, TSEingangsart.PAPIER, true);
    });
});
