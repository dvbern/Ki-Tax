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

import {EbeguWebCore} from '../../app/core/core.angularjs.module';
import UserRS from '../../app/core/service/userRS.rest';
import GesuchModelManager from '../../gesuch/service/gesuchModelManager';
import {ngServicesMock} from '../../hybridTools/ngServicesMocks';
import {TSRole} from '../../models/enums/TSRole';
import TSBerechtigung from '../../models/TSBerechtigung';
import TSBenutzer from '../../models/TSBenutzer';
import TestDataUtil from '../../utils/TestDataUtil.spec';
import {EbeguAuthentication} from '../authentication.module';
import AuthServiceRS from './AuthServiceRS.rest';

describe('AuthServiceRS', () => {

    let authServiceRS: AuthServiceRS;
    let $http: angular.IHttpService;
    let $httpBackend: angular.IHttpBackendService;
    let $q: angular.IQService;
    let $rootScope: angular.IScope;
    let $timeout: angular.ITimeoutService;
    let $cookies: angular.cookies.ICookiesService;
    let gesuchModelManager: GesuchModelManager;
    let userRS: UserRS;

    beforeEach(angular.mock.module(EbeguWebCore.name));
    beforeEach(angular.mock.module(EbeguAuthentication.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(angular.mock.inject($injector => {
        authServiceRS = $injector.get('AuthServiceRS');
        $http = $injector.get('$http');
        $httpBackend = $injector.get('$httpBackend');
        $rootScope = $injector.get('$rootScope');
        $q = $injector.get('$q');
        $timeout = $injector.get('$timeout');
        $cookies = $injector.get('$cookies');
        userRS = $injector.get('UserRS');
        gesuchModelManager = $injector.get('GesuchModelManager');
        spyOn(gesuchModelManager, 'getGesuchsperiode').and.returnValue(TestDataUtil.createGesuchsperiode20162017());
    }));

    describe('API usage', () => {
        beforeEach(() => {
            spyOn($http, 'post').and.returnValue($q.when({}));
        });
        it('does not nothing for an undefined user', () => {
            expect(authServiceRS.loginRequest(undefined)).toBeUndefined();
            expect($http.post).not.toHaveBeenCalled();
        });
        it('receives a loginRequest and handles the incoming cookie', () => {
            // Der Inhalt der Cookie muss nicht unbedingt ein TSUser sein. Deswegen machen wir hier ein Objekt mit dem Inhalt, den die Cookie braucht
            const user: TSBenutzer = new TSBenutzer('Emma', 'Gerber', 'geem', 'password5', 'emma.gerber@example.com', undefined, TSRole.GESUCHSTELLER);
            user.currentBerechtigung = new TSBerechtigung(undefined, TSRole.GESUCHSTELLER);
            const cookieContent: any = {vorname: 'Emma', nachname: 'Gerber', username: 'geem', email: 'emma.gerber@example.com', role: 'GESUCHSTELLER'};
            const encodedUser = btoa(JSON.stringify(cookieContent).split('_').join(''));
            spyOn($cookies, 'get').and.returnValue(encodedUser);
            spyOn(userRS, 'findBenutzer').and.returnValue($q.when(user));

            let cookieUser: TSBenutzer;
            //if we can decode the cookie the client application assumes the user is logged in for ui purposes
            TestDataUtil.mockLazyGesuchModelManagerHttpCalls($httpBackend);
            authServiceRS.loginRequest(user).then(response => {
                cookieUser = response;
            });
            $rootScope.$apply();
            $timeout.flush();
            $httpBackend.flush();

            expect($http.post).toHaveBeenCalled();
            expect(cookieUser.vorname).toEqual(user.vorname);
            expect(cookieUser.nachname).toEqual(user.nachname);
            expect(cookieUser.password).toEqual(user.password);
            expect(cookieUser.email).toEqual(user.email);
            expect(cookieUser.currentBerechtigung.role).toEqual(user.currentBerechtigung.role);
        });
        it('sends a logrequest to server', () => {
            authServiceRS.logoutRequest();
            $rootScope.$apply();
            expect($http.post).toHaveBeenCalledWith('/ebegu/api/v1/auth/logout', null);
            expect(authServiceRS.getPrincipal()).toBeUndefined();
        });
    });

});
