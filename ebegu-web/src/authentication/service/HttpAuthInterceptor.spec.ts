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

import {CORE_JS_MODULE} from '../../app/core/core.angularjs.module';
import {ngServicesMock} from '../../hybridTools/ngServicesMocks';
import {TSAuthEvent} from '../../models/enums/TSAuthEvent';
import {AUTHENTICATION_JS_MODULE} from '../authentication.module';
import {AuthLifeCycleService} from './authLifeCycle.service';
import HttpAuthInterceptor from './HttpAuthInterceptor';

describe('HttpAuthInterceptor', () => {

    let httpAuthInterceptor: HttpAuthInterceptor;
    let $window: angular.IWindowService;
    let authLifeCycleService: AuthLifeCycleService;

    const authErrorResponse: any = {
        status: 401,
        data: '',
        statusText: 'Unauthorized'
    };

    beforeEach(angular.mock.module(CORE_JS_MODULE.name));
    beforeEach(angular.mock.module(AUTHENTICATION_JS_MODULE.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(angular.mock.inject($injector => {
        httpAuthInterceptor = $injector.get('HttpAuthInterceptor');
        $window = $injector.get('$window');
        authLifeCycleService = $injector.get('AuthLifeCycleService');
        window.onbeforeunload = () => 'Oh no!';
        spyOn(authLifeCycleService, 'changeAuthStatus').and.callFake(() => {
        });
    }));

    describe('Public API', () => {
        it('should include a responseError() function', () => {
            expect(httpAuthInterceptor.responseError).toBeDefined();
        });
    });

    describe('API usage', () => {
        beforeEach(() => {
            httpAuthInterceptor.responseError(authErrorResponse);
        });
        it('should capture and broadcast "AUTH_EVENTS.notAuthenticated" on 401', () => {
            expect(authLifeCycleService.changeAuthStatus).toHaveBeenCalledWith(TSAuthEvent.NOT_AUTHENTICATED, authErrorResponse);
        });
    });
});
