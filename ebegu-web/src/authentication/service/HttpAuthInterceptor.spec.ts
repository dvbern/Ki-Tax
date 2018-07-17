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

import {EbeguWebCore} from '../../core/core.module';
import {ngServicesMock} from '../../hybridTools/ngServicesMocks';
import {TSAuthEvent} from '../../models/enums/TSAuthEvent';
import {EbeguAuthentication} from '../authentication.module';
import {AuthLifeCycleService} from './authLifeCycle.service';
import HttpAuthInterceptor from './HttpAuthInterceptor';

describe('HttpAuthInterceptor', function () {

    let httpAuthInterceptor: HttpAuthInterceptor;
    let $window: angular.IWindowService;
    let authLifeCycleService: AuthLifeCycleService;

    let authErrorResponse: any = {
        status: 401,
        data: '',
        statusText: 'Unauthorized'
    };

    beforeEach(angular.mock.module(EbeguWebCore.name));
    beforeEach(angular.mock.module(EbeguAuthentication.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(angular.mock.inject(function ($injector: angular.auto.IInjectorService) {
        httpAuthInterceptor = $injector.get('HttpAuthInterceptor');
        $window = $injector.get('$window');
        authLifeCycleService = $injector.get('AuthLifeCycleService');
        window.onbeforeunload = () => 'Oh no!';
        spyOn(authLifeCycleService, 'changeAuthStatus').and.callFake(() => {
        });
    }));

    describe('Public API', function () {
        it('should include a responseError() function', function () {
            expect(httpAuthInterceptor.responseError).toBeDefined();
        });
    });

    describe('API usage', function () {
        beforeEach(function () {
            httpAuthInterceptor.responseError(authErrorResponse);
        });
        it('should capture and broadcast "AUTH_EVENTS.notAuthenticated" on 401', function () {
            expect(authLifeCycleService.changeAuthStatus).toHaveBeenCalledWith(TSAuthEvent.NOT_AUTHENTICATED, authErrorResponse);
        });
    });
});
