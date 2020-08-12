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
import * as moment from 'moment';
import {ngServicesMock} from '../../../hybridTools/ngServicesMocks';
import {TSGesuchsperiodeStatus} from '../../../models/enums/TSGesuchsperiodeStatus';
import {TSGesuchsperiode} from '../../../models/TSGesuchsperiode';
import {TSDateRange} from '../../../models/types/TSDateRange';
import {DateUtil} from '../../../utils/DateUtil';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';
import {TestDataUtil} from '../../../utils/TestDataUtil.spec';
import {CORE_JS_MODULE} from '../core.angularjs.module';
import {GesuchsperiodeRS} from './gesuchsperiodeRS.rest';

describe('gesuchsperiodeRS', () => {

    let gesuchsperiodeRS: GesuchsperiodeRS;
    let $httpBackend: IHttpBackendService;
    let ebeguRestUtil: EbeguRestUtil;
    let mockGesuchsperiode: TSGesuchsperiode;
    let mockGesuchsperiodeRest: any;
    let date: moment.Moment;
    let $http: IHttpService;

    beforeEach(angular.mock.module(CORE_JS_MODULE.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(angular.mock.inject($injector => {
        gesuchsperiodeRS = $injector.get('GesuchsperiodeRS');
        $httpBackend = $injector.get('$httpBackend');
        ebeguRestUtil = $injector.get('EbeguRestUtil');
        $http = $injector.get('$http');
    }));

    beforeEach(() => {
        date = DateUtil.today();
        mockGesuchsperiode = new TSGesuchsperiode(TSGesuchsperiodeStatus.AKTIV, new TSDateRange(date, date));
        TestDataUtil.setAbstractMutableFieldsUndefined(mockGesuchsperiode);
        mockGesuchsperiodeRest = ebeguRestUtil.gesuchsperiodeToRestObject({}, mockGesuchsperiode);

        TestDataUtil.mockDefaultGesuchModelManagerHttpCalls($httpBackend);
    });

    describe('Public API', () => {
        it('check URI', () => {
            expect(gesuchsperiodeRS.serviceURL).toContain('gesuchsperioden');
        });
    });

    describe('API Usage', () => {
        describe('findGesuchsperiode', () => {
            it('should return the Gesuchsperiode by id', () => {
                $httpBackend.expectGET(`${gesuchsperiodeRS.serviceURL}/gesuchsperiode/${encodeURIComponent(
                    mockGesuchsperiode.id)}`).respond(mockGesuchsperiodeRest);

                let foundGesuchsperiode: TSGesuchsperiode;
                gesuchsperiodeRS.findGesuchsperiode(mockGesuchsperiode.id).then(result => {
                    foundGesuchsperiode = result;
                });
                $httpBackend.flush();
                checkFieldValues(foundGesuchsperiode, mockGesuchsperiode);
            });
        });
        describe('getAllActiveGesuchsperioden', () => {
            it('should return all active Gesuchsperiode by id', () => {
                const gesuchsperiodenList = [mockGesuchsperiodeRest];
                $httpBackend.expectGET(`${gesuchsperiodeRS.serviceURL}/active`).respond(gesuchsperiodenList);
                spyOn($http, 'get').and.callThrough();

                gesuchsperiodeRS.getAllActiveGesuchsperioden();
                $httpBackend.flush();
                // tslint:disable-next-line:no-unbound-method
                expect($http.get).toHaveBeenCalled();
            });
        });
        describe('saveGesuchsperiode', () => {
            it('should create a gesuchsperiode', () => {
                let createdGesuchsperiode: TSGesuchsperiode;
                $httpBackend.expectPUT(gesuchsperiodeRS.serviceURL, mockGesuchsperiodeRest).respond(
                    mockGesuchsperiodeRest);

                gesuchsperiodeRS.saveGesuchsperiode(mockGesuchsperiode)
                    .then(result => {
                        createdGesuchsperiode = result;
                    });
                $httpBackend.flush();
                checkFieldValues(createdGesuchsperiode, mockGesuchsperiode);
            });
        });
        describe('saveGesuchsperiode', () => {
            it('should update a gesuchsperiode', () => {
                mockGesuchsperiode.status = TSGesuchsperiodeStatus.AKTIV;
                mockGesuchsperiodeRest = ebeguRestUtil.gesuchsperiodeToRestObject({}, mockGesuchsperiode);
                let updatedGesuchsperiode: TSGesuchsperiode;
                $httpBackend.expectPUT(gesuchsperiodeRS.serviceURL, mockGesuchsperiodeRest).respond(
                    mockGesuchsperiodeRest);

                gesuchsperiodeRS.saveGesuchsperiode(mockGesuchsperiode)
                    .then(result => {
                        updatedGesuchsperiode = result;
                    });
                $httpBackend.flush();
                checkFieldValues(updatedGesuchsperiode, mockGesuchsperiode);
            });
        });
        describe('removeGesuchsperiode', () => {
            it('should remove a gesuchsperiode', () => {
                const httpOk = 200;
                $httpBackend.expectDELETE(`${gesuchsperiodeRS.serviceURL}/${mockGesuchsperiode.id}`)
                    .respond(httpOk);

                let deleteResult: any;
                gesuchsperiodeRS.removeGesuchsperiode(mockGesuchsperiode.id)
                    .then(result => {
                        deleteResult = result;
                    });
                $httpBackend.flush();
                expect(deleteResult).toBeDefined();
                expect(deleteResult.status).toEqual(httpOk);
            });
        });
    });

    function checkFieldValues(
        createdGesuchsperiode: TSGesuchsperiode,
        mockPeriode: TSGesuchsperiode,
    ): void {
        expect(createdGesuchsperiode).toBeDefined();
        expect(createdGesuchsperiode.status).toBe(TSGesuchsperiodeStatus.AKTIV);
        TestDataUtil.checkGueltigkeitAndSetIfSame(createdGesuchsperiode, mockPeriode);
    }
});
