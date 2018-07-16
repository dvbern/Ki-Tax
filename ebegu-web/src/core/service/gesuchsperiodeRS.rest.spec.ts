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

import * as moment from 'moment';
import {ngServicesMock} from '../../hybridTools/ngServicesMocks';
import {TSGesuchsperiodeStatus} from '../../models/enums/TSGesuchsperiodeStatus';
import TSGesuchsperiode from '../../models/TSGesuchsperiode';
import {TSDateRange} from '../../models/types/TSDateRange';
import DateUtil from '../../utils/DateUtil';
import EbeguRestUtil from '../../utils/EbeguRestUtil';
import TestDataUtil from '../../utils/TestDataUtil';
import {EbeguWebCore} from '../core.module';
import GesuchsperiodeRS from './gesuchsperiodeRS.rest';

describe('gesuchsperiodeRS', function () {

    let gesuchsperiodeRS: GesuchsperiodeRS;
    let $httpBackend: angular.IHttpBackendService;
    let ebeguRestUtil: EbeguRestUtil;
    let mockGesuchsperiode: TSGesuchsperiode;
    let mockGesuchsperiodeRest: any;
    let date: moment.Moment;

    beforeEach(angular.mock.module(EbeguWebCore.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(angular.mock.inject(function ($injector: angular.auto.IInjectorService) {
        gesuchsperiodeRS = $injector.get('GesuchsperiodeRS');
        $httpBackend = $injector.get('$httpBackend');
        ebeguRestUtil = $injector.get('EbeguRestUtil');
    }));

    beforeEach(() => {
        date = DateUtil.today();
        mockGesuchsperiode = new TSGesuchsperiode(TSGesuchsperiodeStatus.AKTIV, new TSDateRange(date, date));
        TestDataUtil.setAbstractFieldsUndefined(mockGesuchsperiode);
        mockGesuchsperiodeRest = ebeguRestUtil.gesuchsperiodeToRestObject({}, mockGesuchsperiode);
    });

    describe('Public API', function () {
        it('check URI', function () {
            expect(gesuchsperiodeRS.serviceURL).toContain('gesuchsperioden');
        });
        it('check Service name', function () {
            expect(gesuchsperiodeRS.getServiceName()).toBe('GesuchsperiodeRS');
        });
        it('should include a findGesuchsperiode() function', function () {
            expect(gesuchsperiodeRS.findGesuchsperiode).toBeDefined();
        });
        it('should include a createGesuchsperiode() function', function () {
            expect(gesuchsperiodeRS.createGesuchsperiode).toBeDefined();
        });
        it('should include a updateGesuchsperiode() function', function () {
            expect(gesuchsperiodeRS.updateGesuchsperiode).toBeDefined();
        });
        it('should include a removeGesuchsperiode() function', function () {
            expect(gesuchsperiodeRS.removeGesuchsperiode).toBeDefined();
        });
        it('should include a getAllActiveGesuchsperioden() function', function () {
            expect(gesuchsperiodeRS.getAllActiveGesuchsperioden).toBeDefined();
        });
    });

    describe('API Usage', function () {
        describe('findGesuchsperiode', () => {
            it('should return the Gesuchsperiode by id', () => {
                $httpBackend.expectGET(gesuchsperiodeRS.serviceURL + '/gesuchsperiode/' + encodeURIComponent(mockGesuchsperiode.id)).respond(mockGesuchsperiodeRest);

                let foundGesuchsperiode: TSGesuchsperiode;
                gesuchsperiodeRS.findGesuchsperiode(mockGesuchsperiode.id).then((result) => {
                    foundGesuchsperiode = result;
                });
                $httpBackend.flush();
                checkFieldValues(foundGesuchsperiode, mockGesuchsperiode, true);
            });
        });
        describe('getAllActiveGesuchsperioden', () => {
            it('should return all active Gesuchsperiode by id', () => {
                let gesuchsperiodenList: Array<any> = [mockGesuchsperiodeRest];
                $httpBackend.expectGET(gesuchsperiodeRS.serviceURL + '/active').respond(gesuchsperiodenList);

                let foundGesuchsperioden: Array<TSGesuchsperiode>;
                gesuchsperiodeRS.getAllActiveGesuchsperioden().then((result) => {
                    foundGesuchsperioden = result;
                });
                $httpBackend.flush();
                expect(foundGesuchsperioden).toBeDefined();
                expect(foundGesuchsperioden.length).toBe(1);
                expect(foundGesuchsperioden[0].status).toBe(TSGesuchsperiodeStatus.AKTIV);
                TestDataUtil.checkGueltigkeitAndSetIfSame(foundGesuchsperioden[0], mockGesuchsperiode);
                expect(foundGesuchsperioden[0]).toEqual(mockGesuchsperiode);
            });
        });
        describe('createGesuchsperiode', () => {
            it('should create a gesuchsperiode', () => {
                let createdGesuchsperiode: TSGesuchsperiode;
                $httpBackend.expectPUT(gesuchsperiodeRS.serviceURL, mockGesuchsperiodeRest).respond(mockGesuchsperiodeRest);

                gesuchsperiodeRS.createGesuchsperiode(mockGesuchsperiode)
                    .then((result) => {
                        createdGesuchsperiode = result;
                    });
                $httpBackend.flush();
                checkFieldValues(createdGesuchsperiode, mockGesuchsperiode, true);
            });
        });
        describe('updateGesuchsperiode', () => {
            it('should update a gesuchsperiode', () => {
                mockGesuchsperiode.status = TSGesuchsperiodeStatus.AKTIV;
                mockGesuchsperiodeRest = ebeguRestUtil.gesuchsperiodeToRestObject({}, mockGesuchsperiode);
                let updatedGesuchsperiode: TSGesuchsperiode;
                $httpBackend.expectPUT(gesuchsperiodeRS.serviceURL, mockGesuchsperiodeRest).respond(mockGesuchsperiodeRest);

                gesuchsperiodeRS.updateGesuchsperiode(mockGesuchsperiode)
                    .then((result) => {
                        updatedGesuchsperiode = result;
                    });
                $httpBackend.flush();
                checkFieldValues(updatedGesuchsperiode, mockGesuchsperiode, false);
            });
        });
        describe('removeGesuchsperiode', () => {
            it('should remove a gesuchsperiode', () => {
                $httpBackend.expectDELETE(gesuchsperiodeRS.serviceURL + '/' + mockGesuchsperiode.id)
                    .respond(200);

                let deleteResult: any;
                gesuchsperiodeRS.removeGesuchsperiode(mockGesuchsperiode.id)
                    .then((result) => {
                        deleteResult = result;
                    });
                $httpBackend.flush();
                expect(deleteResult).toBeDefined();
                expect(deleteResult.status).toEqual(200);
            });
        });
    });

    function checkFieldValues(createdGesuchsperiode: TSGesuchsperiode, mockGesuchsperiode: TSGesuchsperiode,
                              active: boolean) {
        expect(createdGesuchsperiode).toBeDefined();
        expect(createdGesuchsperiode.status).toBe(TSGesuchsperiodeStatus.AKTIV);
        TestDataUtil.checkGueltigkeitAndSetIfSame(createdGesuchsperiode, mockGesuchsperiode);
    }
});
