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

import {IHttpBackendService} from 'angular';
import {ngServicesMock} from '../../../hybridTools/ngServicesMocks';
import {translationsMock} from '../../../hybridTools/translationsMock';
import {TSKind} from '../../../models/TSKind';
import {TSKindContainer} from '../../../models/TSKindContainer';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';
import {TestDataUtil} from '../../../utils/TestDataUtil.spec';
import {CORE_JS_MODULE} from '../core.angularjs.module';
import {VerfuegungRS} from './verfuegungRS.rest';

describe('VerfuegungRS', () => {

    let verfuegungRS: VerfuegungRS;
    let $httpBackend: IHttpBackendService;
    let ebeguRestUtil: EbeguRestUtil;
    let mockKindContainerListRest: Array<any> = [];
    let mockKind: TSKindContainer;
    const gesuchId = '1234567789';

    beforeEach(angular.mock.module(CORE_JS_MODULE.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(angular.mock.module(translationsMock));

    beforeEach(angular.mock.inject($injector => {
        verfuegungRS = $injector.get('VerfuegungRS');
        $httpBackend = $injector.get('$httpBackend');
        ebeguRestUtil = $injector.get('EbeguRestUtil');
    }));

    beforeEach(() => {
        const kindGS = new TSKind();
        kindGS.vorname = 'Pedro';
        kindGS.nachname = 'Bern';
        TestDataUtil.setAbstractMutableFieldsUndefined(kindGS);
        const kindJA = new TSKind();
        kindJA.vorname = 'Pedro';
        kindJA.nachname = 'Bern';
        TestDataUtil.setAbstractMutableFieldsUndefined(kindJA);
        mockKind = new TSKindContainer();
        mockKind.kindGS = kindGS;
        mockKind.kindJA = kindJA;
        mockKind.betreuungen = [];
        TestDataUtil.setAbstractMutableFieldsUndefined(mockKind);
        mockKind.id = '2afc9d9a-957e-4550-9a22-97624a1d8feb';
        mockKindContainerListRest = ebeguRestUtil.kindContainerToRestObject({}, mockKind);

        TestDataUtil.mockDefaultGesuchModelManagerHttpCalls($httpBackend);
    });

    describe('Public API', () => {
        it('check URI', () => {
            expect(verfuegungRS.serviceURL).toContain('verfuegung');
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
                expect(foundKind[0].id).toEqual(mockKind.id);
            });
        });
    });
});
