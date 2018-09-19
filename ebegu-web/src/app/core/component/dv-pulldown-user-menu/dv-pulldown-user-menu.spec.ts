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
import {IComponentControllerService, IQService, IScope} from 'angular';
import {of} from 'rxjs';
import AuthServiceRS from '../../../../authentication/service/AuthServiceRS.rest';
import {ngServicesMock} from '../../../../hybridTools/ngServicesMocks';
import TSBenutzer from '../../../../models/TSBenutzer';
import {EbeguWebCore} from '../../core.angularjs.module';
import {DvPulldownUserMenuController} from './dv-pulldown-user-menu';
import IInjectorService = angular.auto.IInjectorService;

describe('DvPulldownUserMenuController', () => {

    let authServiceRS: AuthServiceRS;
    let $state: StateService;
    let $q: IQService;
    let scope: IScope;
    let component: DvPulldownUserMenuController;
    let $componentController: IComponentControllerService;

    const user: TSBenutzer = new TSBenutzer('pedro');

    beforeEach(angular.mock.module(EbeguWebCore.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(angular.mock.inject(($injector: IInjectorService) => {
        authServiceRS = $injector.get('AuthServiceRS');
        scope = $injector.get('$rootScope').$new();
        $q = $injector.get('$q');
        $state = $injector.get('$state');
        $componentController = $injector.get('$componentController');
    }));

    it('should be defined', () => {
        component = $componentController('dvPulldownUserMenu', {$scope: scope});
        expect(component).toBeDefined();
    });

    describe('API Usage', () => {

        describe('logout()', () => {
            it('must call the logout function and redirect to the login page', () => {
                spyOnProperty(authServiceRS, 'principal$', 'get').and.returnValue(of(user));
                spyOn($state, 'go');

                component = $componentController('dvPulldownUserMenu', {$scope: scope}, {});
                component.$onInit();

                component.logout();

                //actual logout happens on login page
                expect($state.go).toHaveBeenCalledWith('authentication.login', {type: 'logout'});
            });
        });
        describe('change Principal', () => {

            it('just after the controller is created Principal is undefined', () => {
                spyOnProperty(authServiceRS, 'principal$', 'get').and.returnValue(of(undefined));

                component = $componentController('dvPulldownUserMenu', {$scope: scope});
                component.$onInit();

                expect(component.principal).toBeUndefined();
            });
            it('When the user logs in the principal must be updated', () => {
                spyOnProperty(authServiceRS, 'principal$', 'get').and.returnValue(of(user));

                component = $componentController('dvPulldownUserMenu', {$scope: scope});
                component.$onInit();

                expect(component.principal).toBe(user);
            });
        });
    });

});
