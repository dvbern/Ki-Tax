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

import {IComponentControllerService, IScope} from 'angular';
import {ngServicesMock} from '../../../hybridTools/ngServicesMocks';
import {GESUCH_JS_MODULE} from '../../gesuch.module';
import GesuchModelManager from '../../service/gesuchModelManager';
import {ErwerbspensumListViewComponentConfig} from './erwerbspensumListView';
import IInjectorService = angular.auto.IInjectorService;

describe('erwerbspensumListView', () => {

    beforeEach(angular.mock.module(GESUCH_JS_MODULE.name));

    beforeEach(angular.mock.module(ngServicesMock));

    let component: ErwerbspensumListViewComponentConfig;
    let scope: IScope;
    let $componentController: IComponentControllerService;
    let gesuchModelManager: GesuchModelManager;
    let $q: angular.IQService;

    beforeEach(angular.mock.inject(($injector: IInjectorService) => {
        gesuchModelManager = $injector.get('GesuchModelManager');
        $componentController = $injector.get('$componentController');
        $q = $injector.get('$q');
        scope = $injector.get('$rootScope').$new();
        spyOn(gesuchModelManager, 'showInfoAusserordentlichenAnspruch').and.returnValue($q.when(false));
    }));

    it('should be defined', () => {
        /*
         To initialise your component controller you have to setup your (mock) bindings and
         pass them to $componentController.
         */
        const bindings = {};
        component = $componentController('erwerbspensumListView', {$scope: scope}, bindings);
        expect(component).toBeDefined();
    });
});
