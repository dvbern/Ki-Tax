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

import {ngServicesMock} from '../../../hybridTools/ngServicesMocks';
import TSKind from '../../../models/TSKind';
import TSKindContainer from '../../../models/TSKindContainer';
import TSVerfuegung from '../../../models/TSVerfuegung';
import EbeguRestUtil from '../../../utils/EbeguRestUtil';
import TestDataUtil from '../../../utils/TestDataUtil.spec';
import {CORE_JS_MODULE} from '../core.angularjs.module';
import VerfuegungRS from './verfuegungRS.rest';

describe('VerfuegungRS', () => {

    let verfuegungRS: VerfuegungRS;
    let $httpBackend: angular.IHttpBackendService;
    let ebeguRestUtil: EbeguRestUtil;
    let mockKindContainerListRest: Array<any> = [];
    let mockKind: TSKindContainer;
    const gesuchId = '1234567789';
    const betreuungId = '321123';

    beforeEach(angular.mock.module(CORE_JS_MODULE.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(angular.mock.inject($injector => {
        verfuegungRS = $injector.get('VerfuegungRS');
        $httpBackend = $injector.get('$httpBackend');
        ebeguRestUtil = $injector.get('EbeguRestUtil');
    }));

    beforeEach(() => {
        const kindGS = new TSKind('Pedro', 'Bern');
        TestDataUtil.setAbstractMutableFieldsUndefined(kindGS);
        const kindJA = new TSKind('Pedro', 'Bern');
        TestDataUtil.setAbstractMutableFieldsUndefined(kindJA);
        mockKind = new TSKindContainer(kindGS, kindJA, []);
        TestDataUtil.setAbstractMutableFieldsUndefined(mockKind);
        mockKind.id = '2afc9d9a-957e-4550-9a22-97624a1d8feb';
        mockKindContainerListRest = ebeguRestUtil.kindContainerToRestObject({}, mockKind);
    });

    describe('Public API', () => {
        it('check URI', () => {
            expect(verfuegungRS.serviceURL).toContain('verfuegung');
        });
        it('check Service name', () => {
            expect(verfuegungRS.getServiceName()).toBe('VerfuegungRS');
        });
        it('should include a findKind() function', () => {
            expect(verfuegungRS.calculateVerfuegung).toBeDefined();
        });
    });

    describe('API Usage', () => {
        describe('calculate', () => {
            it('should return all KindContainer', () => {
                $httpBackend.expectGET(`${verfuegungRS.serviceURL}/calculate/${gesuchId}`).respond(
                    mockKindContainerListRest);

                let foundKind: Array<TSKindContainer>;
                verfuegungRS.calculateVerfuegung(gesuchId).then(result => {
                    foundKind = result;
                });
                $httpBackend.flush();
                expect(foundKind).toBeDefined();
                expect(foundKind.length).toBe(1);
                expect(foundKind[0]).toEqual(mockKind);
            });
        });
        describe('saveVerfuegung', () => {
            it('should save the given Verfuegung', () => {
                const verfuegung = TestDataUtil.createVerfuegung();
                $httpBackend.expectPUT(`${verfuegungRS.serviceURL}/${gesuchId}/${betreuungId}/false`).respond(
                    ebeguRestUtil.verfuegungToRestObject({}, verfuegung));
                $httpBackend.expectGET(`/ebegu/api/v1/wizard-steps/${gesuchId}`).respond({});

                let savedVerfuegung: TSVerfuegung;
                verfuegungRS.saveVerfuegung(verfuegung, gesuchId, betreuungId, false).then(result => {
                    savedVerfuegung = result;
                });
                $httpBackend.flush();
                expect(savedVerfuegung).toBeDefined();
                expect(savedVerfuegung).toEqual(verfuegung);
            });
        });
    });

});
