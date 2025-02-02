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

import {IHttpBackendService, IHttpService} from 'angular';
import {ngServicesMock} from '../../../hybridTools/ngServicesMocks';
import {translationsMock} from '../../../hybridTools/translationsMock';
import {TSFachstelleName} from '../../../models/enums/TSFachstelleName';
import {TSFachstelle} from '../../../models/TSFachstelle';
import {TSGesuchsperiode} from '../../../models/TSGesuchsperiode';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';
import {TestDataUtil} from '../../../utils/TestDataUtil.spec';
import {CORE_JS_MODULE} from '../core.angularjs.module';
import {FachstelleRS} from './fachstelleRS.rest';

describe('fachstelleRS', () => {
    let fachstelleRS: FachstelleRS;
    let $httpBackend: IHttpBackendService;
    let ebeguRestUtil: EbeguRestUtil;
    let mockFachstelle: TSFachstelle;
    let mockFachstelleRest: any;
    let $http: IHttpService;

    beforeEach(angular.mock.module(CORE_JS_MODULE.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(angular.mock.module(translationsMock));

    beforeEach(
        angular.mock.inject($injector => {
            fachstelleRS = $injector.get('FachstelleRS');
            $httpBackend = $injector.get('$httpBackend');
            ebeguRestUtil = $injector.get('EbeguRestUtil');
            $http = $injector.get('$http');
        })
    );

    beforeEach(() => {
        mockFachstelle = new TSFachstelle();
        mockFachstelle.id = '2afc9d9a-957e-4550-9a22-9762422d8f05';
        mockFachstelle.name = TSFachstelleName.ERZIEHUNGSBERATUNG;
        mockFachstelle.fachstelleAnspruch = true;
        mockFachstelleRest = ebeguRestUtil.fachstelleToRestObject(
            {},
            mockFachstelle
        );

        TestDataUtil.mockDefaultGesuchModelManagerHttpCalls($httpBackend);
    });

    describe('Public API', () => {
        it('check URI', () => {
            expect(fachstelleRS.serviceURL).toContain('fachstellen');
        });
    });

    describe('API Usage', () => {
        describe('getAnspruchFachstellen', () => {
            it('should return all Anspruch Fachstellen', () => {
                const fachstellenRestArray = [
                    mockFachstelleRest,
                    mockFachstelleRest
                ];
                const gesuchsperiodeId = '0621fb5d-a187-5a91-abaf-8a813c4d263a';
                $httpBackend
                    .expectGET(
                        `${fachstelleRS.serviceURL}/anspruch?gesuchsperiodeId=${gesuchsperiodeId}`
                    )
                    .respond(fachstellenRestArray);
                spyOn($http, 'get').and.callThrough();
                spyOn(ebeguRestUtil, 'parseFachstellen').and.callThrough();
                const gesuchsperiode = new TSGesuchsperiode();
                gesuchsperiode.id = gesuchsperiodeId;

                fachstelleRS.getAnspruchFachstellen(gesuchsperiode);
                $httpBackend.flush();

                expect($http.get).toHaveBeenCalledWith(
                    `${fachstelleRS.serviceURL}/anspruch?gesuchsperiodeId=${gesuchsperiodeId}`
                );

                expect(ebeguRestUtil.parseFachstellen).toHaveBeenCalledWith(
                    fachstellenRestArray
                );
            });
        });
    });
});
