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

import {AuthLifeCycleService} from '../../../authentication/service/authLifeCycle.service';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import {ngServicesMock} from '../../../hybridTools/ngServicesMocks';
import TSUser from '../../../models/TSUser';
import {EbeguWebCore} from '../../core.angularjs.module';
import {DvPulldownUserMenuController} from './dv-pulldown-user-menu';
import {StateService} from '@uirouter/core';

describe('DvPulldownUserMenuController', () => {

    let authServiceRS: AuthServiceRS;
    let authLifeCycleService: AuthLifeCycleService;
    let $state: StateService;
    let controller: DvPulldownUserMenuController;
    let $q: angular.IQService;
    let scope: angular.IScope;

    beforeEach(angular.mock.module(EbeguWebCore.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(angular.mock.inject($injector => {
        authServiceRS = $injector.get('AuthServiceRS');
        authLifeCycleService = $injector.get('AuthLifeCycleService');
        scope = $injector.get('$rootScope').$new();
        $q = $injector.get('$q');
        $state = $injector.get('$state');
    }));

    beforeEach(() => {
        controller = new DvPulldownUserMenuController($state, authServiceRS, authLifeCycleService);
    });

    describe('API Usage', () => {
        describe('logout()', () => {
            it('must call the logout function and redirect to the login page', () => {
                spyOn($state, 'go');

                controller.logout();
                scope.$apply();
                //actual logout happens on login page
                expect($state.go).toHaveBeenCalledWith('login', {type: 'logout'});
            });
        });
        describe('change Principal', () => {
            it('just after the controller is created Principal is undefined', () => {
                expect(controller.principal).toBeUndefined();
            });
            it('When the user logs in the principal must be updated', () => {
                const user: TSUser = new TSUser('pedro');
                spyOn(authServiceRS, 'getPrincipal').and.returnValue(user);
                controller.$onInit();

                expect(controller.principal).toBe(user);
            });
        });
    });

});
