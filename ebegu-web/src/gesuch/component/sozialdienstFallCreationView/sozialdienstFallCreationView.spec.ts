/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

import {IQService, IScope} from 'angular';
import {CORE_JS_MODULE} from '../../../app/core/core.angularjs.module';
import {ngServicesMock} from '../../../hybridTools/ngServicesMocks';
import {TestDataUtil} from '../../../utils/TestDataUtil.spec';
import {GesuchModelManager} from '../../service/gesuchModelManager';
import {SozialdienstFallCreationViewController} from './sozialdienstFallCreationView';

describe('sozialdienstFallCreationView', () => {

    let sozialdienstFallCreationView: SozialdienstFallCreationViewController;
    let gesuchModelManager: GesuchModelManager;
    let $rootScope: IScope;
    let form: any;

    beforeEach(angular.mock.module(CORE_JS_MODULE.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(angular.mock.inject($injector => {
        gesuchModelManager = $injector.get('GesuchModelManager');
        TestDataUtil.mockLazyGesuchModelManagerHttpCalls($injector.get('$httpBackend'));
        $rootScope = $injector.get('$rootScope');
        form = {};
        form.$valid = true;
        form.$dirty = true;
        sozialdienstFallCreationView = new SozialdienstFallCreationViewController(gesuchModelManager,
            $injector.get('BerechnungsManager'),
            $injector.get('ErrorService'),
            $injector.get('$stateParams'),
            $injector.get('WizardStepManager'),
            $injector.get('$translate'),
            $rootScope,
            $injector.get('AuthServiceRS'),
            $injector.get('SozialdienstRS'),
            $injector.get('$state'),
            $injector.get('$timeout'));
        sozialdienstFallCreationView.form = form;
        spyOn(sozialdienstFallCreationView, 'isGesuchValid').and.callFake(() => form.$valid);
    }));
});
