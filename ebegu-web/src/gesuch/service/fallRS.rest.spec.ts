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

import {EbeguWebCore} from '../../core/core.angularjs.module';
import {ngServicesMock} from '../../hybridTools/ngServicesMocks';
import TSFall from '../../models/TSFall';
import EbeguRestUtil from '../../utils/EbeguRestUtil';
import FallRS from './fallRS.rest';

describe('fallRS', () => {

    let fallRS: FallRS;
    let $httpBackend: angular.IHttpBackendService;
    let ebeguRestUtil: EbeguRestUtil;
    let REST_API: string;
    let mockFall: TSFall;
    let mockFallRest: any;

    beforeEach(angular.mock.module(EbeguWebCore.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(angular.mock.inject($injector => {
        fallRS = $injector.get('FallRS');
        $httpBackend = $injector.get('$httpBackend');
        ebeguRestUtil = $injector.get('EbeguRestUtil');
        REST_API = $injector.get('REST_API');
    }));

    beforeEach(() => {
        mockFall = new TSFall();
        mockFall.fallNummer = 2;
        mockFallRest = ebeguRestUtil.fallToRestObject({}, mockFall);
    });

    describe('Public API', () => {
        it('check URI', () => {
            expect(fallRS.serviceURL).toContain('falle');
        });
        it('check Service name', () => {
            expect(fallRS.getServiceName()).toBe('FallRS');
        });
        it('should include a createFall() function', () => {
            expect(fallRS.createFall).toBeDefined();
        });
        it('should include a findFall() function', () => {
            expect(fallRS.findFall).toBeDefined();
        });
        it('should include a updateFall() function', () => {
            expect(fallRS.updateFall).toBeDefined();
        });
    });

    describe('API Usage', () => {
        describe('findFall', () => {
            it('should return the Fall by id', () => {
                $httpBackend.expectGET(fallRS.serviceURL + '/id/' + mockFall.id).respond(mockFallRest);

                let foundFall: TSFall;
                fallRS.findFall(mockFall.id).then((result) => {
                    foundFall = result;
                });
                $httpBackend.flush();
                expect(foundFall).toBeDefined();
                expect(foundFall.fallNummer).toEqual(mockFall.fallNummer);
            });
        });
        describe('createFall', () => {
            it('should create an fall', () => {
                let createdFall: TSFall;
                $httpBackend.expectPUT(fallRS.serviceURL, mockFallRest).respond(mockFallRest);

                fallRS.createFall(mockFall).then((result) => {
                    createdFall = result;
                });
                $httpBackend.flush();
                expect(createdFall).toBeDefined();
                expect(createdFall.fallNummer).toEqual(mockFall.fallNummer);
            });
        });
    });
});
