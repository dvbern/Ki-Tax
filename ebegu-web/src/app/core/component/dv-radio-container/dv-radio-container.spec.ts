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
import {ngServicesMock} from '../../../../hybridTools/ngServicesMocks';
import {DvRadioContainerComponentConfig} from './dv-radio-container';
import IInjectorService = angular.auto.IInjectorService;

describe('dvRadioContainer', () => {
    beforeEach(angular.mock.module('ebeguWeb.core'));

    beforeEach(angular.mock.module(ngServicesMock));

    let component: DvRadioContainerComponentConfig;
    let scope: IScope;
    let $componentController: IComponentControllerService;

    beforeEach(
        angular.mock.inject(($injector: IInjectorService) => {
            $componentController = $injector.get('$componentController');
            scope = $injector.get('$rootScope').$new();
        })
    );

    it('should be defined', () => {
        /*
         To initialise your component controller you have to setup your (mock) bindings and
         pass them to $componentController.
         */
        const bindings = {};
        component = $componentController(
            'dvRadioContainer',
            {$scope: scope},
            bindings
        );
        expect(component).toBeDefined();
    });
});
