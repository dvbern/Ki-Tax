/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

import {IHttpBackendService} from 'angular';
import {CORE_JS_MODULE} from '../../app/core/core.angularjs.module';
import {ngServicesMock} from '../../hybridTools/ngServicesMocks';
import {translationsMock} from '../../hybridTools/translationsMock';
import {TSRole} from '../../models/enums/TSRole';
import {TSBenutzer} from '../../models/TSBenutzer';
import {TSGemeinde} from '../../models/TSGemeinde';
import {TestDataUtil} from '../../utils/TestDataUtil.spec';
import {GemeindeRS} from './gemeindeRS.rest';

describe('GemeindeRS', () => {

    let gemeindeRS: GemeindeRS;
    let $httpBackend: IHttpBackendService;
    let allGemeinde: TSGemeinde[];

    beforeEach(angular.mock.module(CORE_JS_MODULE.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(angular.mock.module(translationsMock));

    beforeEach(angular.mock.inject($injector => {
        gemeindeRS = $injector.get('GemeindeRS');
        $httpBackend = $injector.get('$httpBackend');

        createAllGemeinden();
    }));

    describe('getGemeindenForPrincipal', () => {
        it('should give the gemeinden linked to the given user', done => {
            const user = createUser(TSRole.SACHBEARBEITER_BG, true);

            gemeindeRS.toGemeindenForPrincipal$(user).subscribe(
                gemeinden => {
                    expect(gemeinden).toBeDefined();
                    expect(gemeinden.length).toBe(1);
                    expect(gemeinden[0]).toEqual(user.extractCurrentGemeinden()[0]);
                    done();
                },
                err => done.fail(err),
            );
        });

        it('should give all gemeinden for a role without gemeinde', done => {
            TestDataUtil.mockDefaultGesuchModelManagerHttpCalls($httpBackend);
            $httpBackend.expectGET(gemeindeRS.serviceURL + '/all').respond(allGemeinde);
            const user = createUser(TSRole.SACHBEARBEITER_INSTITUTION, false);

            gemeindeRS.toGemeindenForPrincipal$(user).subscribe(
                gemeinden => {
                    expect(gemeinden).toBeDefined();
                    expect(gemeinden.length).toBe(2);
                    expect(gemeinden[0]).toEqual(allGemeinde[0]);
                    expect(gemeinden[1]).toEqual(allGemeinde[1]);
                    done();
                },
                err => done.fail(err),
            );

            $httpBackend.flush();
        });
        it('should return empty list for undefined role', done => {
            gemeindeRS.toGemeindenForPrincipal$(undefined).subscribe(
                gemeinden => {
                    expect(gemeinden).toBeDefined();
                    expect(gemeinden.length).toBe(0);
                    done();
                },
                err => done.fail(err),
            );
        });
    });

    function createUser(role: TSRole, createGemeinde: boolean): TSBenutzer {
        const user = new TSBenutzer('Pedrito', 'Fuentes');
        user.currentBerechtigung = TestDataUtil.createBerechtigung(role, createGemeinde);
        return user;
    }

    function createAllGemeinden(): void {
        allGemeinde = [
            TestDataUtil.createGemeindeParis(),
            TestDataUtil.createGemeindeLondon(),
        ];
    }
});
