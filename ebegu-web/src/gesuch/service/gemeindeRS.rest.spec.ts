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

import {IRootScopeService} from 'angular';
import {EbeguWebCore} from '../../core/core.module';
import {ngServicesMock} from '../../hybridTools/ngServicesMocks';
import {TSRole} from '../../models/enums/TSRole';
import TSGemeinde from '../../models/TSGemeinde';
import TSUser from '../../models/TSUser';
import TestDataUtil from '../../utils/TestDataUtil';
import GemeindeRS from './gemeindeRS.rest';

describe('dossier', function () {

    let gemeindeRS: GemeindeRS;
    let $http: angular.IHttpService;
    let $httpBackend: angular.IHttpBackendService;
    let $q: angular.IQService;
    let allGemeinde: TSGemeinde[];
    let $rootScope: IRootScopeService;

    beforeEach(angular.mock.module(EbeguWebCore.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(angular.mock.inject(function ($injector: angular.auto.IInjectorService) {
        gemeindeRS = $injector.get('GemeindeRS');
        $httpBackend = $injector.get('$httpBackend');
        $http = $injector.get('$http');
        $q = $injector.get('$q');
        $rootScope = $injector.get('$rootScope');

        createAllGemeinden();
    }));

    describe('getGemeindenForPrincipal', function () {
        it('should give the gemeinden linked to the given user', function () {
            let user = createUser(TSRole.SACHBEARBEITER_JA, true);

            let gemeindeList: TSGemeinde[];
            gemeindeRS.getGemeindenForPrincipal(user).then(promiseValue => {
                gemeindeList = promiseValue;
            });
            $rootScope.$apply();
            expect(gemeindeList).toBeDefined();
            expect(gemeindeList.length).toBe(1);
            expect(gemeindeList[0]).toEqual(user.extractCurrentGemeinden()[0]);
        });
        it('should give all gemeinden for a role without gemeinde', function () {
            $httpBackend.expectGET(gemeindeRS.serviceURL + '/all').respond(allGemeinde);
            let user = createUser(TSRole.SACHBEARBEITER_INSTITUTION, false);

            let gemeindeList: TSGemeinde[];
            gemeindeRS.getGemeindenForPrincipal(user).then(promiseValue => {
                gemeindeList = promiseValue;
            });
            $httpBackend.flush();
            expect(gemeindeList).toBeDefined();
            expect(gemeindeList.length).toBe(2);
            expect(gemeindeList[0]).toEqual(allGemeinde[0]);
            expect(gemeindeList[1]).toEqual(allGemeinde[1]);
        });
        it('should return empty list for undefined role', function () {
            let gemeindeList: TSGemeinde[] = undefined; // to test that it changes to undefined
            gemeindeRS.getGemeindenForPrincipal(undefined).then(promiseValue => {
                gemeindeList = promiseValue;
            });
            $rootScope.$apply();
            expect(gemeindeList).toBeDefined();
            expect(gemeindeList.length).toBe(0);
        });
    });


    function createUser(role: TSRole, createGemeinde: boolean) {
        let user: TSUser = new TSUser('Pedrito', 'Fuentes');
        user.currentBerechtigung = TestDataUtil.createBerechtigung(role, createGemeinde);
        return user;
    }

    function createAllGemeinden() {
        allGemeinde = [
            TestDataUtil.createGemeindeBern(),
            TestDataUtil.createGemeindeOstermundigen(),
        ];
    }
});
