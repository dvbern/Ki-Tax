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

import {ngServicesMock} from '../../hybridTools/ngServicesMocks';
import {TSTraegerschaft} from '../../models/TSTraegerschaft';
import EbeguRestUtil from '../../utils/EbeguRestUtil';
import {EbeguWebCore} from '../core.module';
import {TraegerschaftRS} from './traegerschaftRS.rest';

describe('institutionStammdatenRS', () => {

    let traegerschaftRS: TraegerschaftRS;
    let $httpBackend: angular.IHttpBackendService;
    let ebeguRestUtil: EbeguRestUtil;
    let mockTraegerschaft: TSTraegerschaft;
    let mockTraegerschaftRest: any;

    beforeEach(angular.mock.module(EbeguWebCore.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(angular.mock.inject($injector => {
        traegerschaftRS = $injector.get('TraegerschaftRS');
        $httpBackend = $injector.get('$httpBackend');
        ebeguRestUtil = $injector.get('EbeguRestUtil');
    }));

    beforeEach(() => {
        mockTraegerschaft = new TSTraegerschaft('TraegerschaftTest');
        mockTraegerschaft.id = '2afc9d9a-957e-4550-9a22-97624a1d8f05';
        mockTraegerschaftRest = ebeguRestUtil.traegerschaftToRestObject({}, mockTraegerschaft);
    });

    describe('Public API', () => {
        it('check Service name', () => {
            expect(traegerschaftRS.getServiceName()).toBe('TraegerschaftRS');
        });
        it('should include a findTraegerschaft() function', () => {
            expect(traegerschaftRS.findTraegerschaft).toBeDefined();
        });
        it('should include a createTraegerschaft() function', () => {
            expect(traegerschaftRS.createTraegerschaft).toBeDefined();
        });
        it('should include a updateTraegerschaft() function', () => {
            expect(traegerschaftRS.updateTraegerschaft).toBeDefined();
        });
        it('should include a removeTraegerschaft() function', () => {
            expect(traegerschaftRS.removeTraegerschaft).toBeDefined();
        });
        it('should include a getAllTraegerschaften() function', () => {
            expect(traegerschaftRS.getAllTraegerschaften).toBeDefined();
        });
    });

    describe('API Usage', () => {
        describe('findTraegerschaft', () => {
            it('should return the Traegerschaft by id', () => {
                $httpBackend.expectGET(traegerschaftRS.serviceURL + '/id/' + encodeURIComponent(mockTraegerschaft.id)).respond(mockTraegerschaftRest);

                let foundTraegerschaft: TSTraegerschaft;
                traegerschaftRS.findTraegerschaft(mockTraegerschaft.id).then((result) => {
                    foundTraegerschaft = result;
                });
                $httpBackend.flush();
                checkFieldValues(foundTraegerschaft, mockTraegerschaft);
            });

        });

        describe('createTraegerschaft', () => {
            it('should create a traegerschaft', () => {
                let createdTraegerschaft: TSTraegerschaft;
                $httpBackend.expectPUT(traegerschaftRS.serviceURL, mockTraegerschaftRest).respond(mockTraegerschaftRest);

                traegerschaftRS.createTraegerschaft(mockTraegerschaft)
                    .then((result) => {
                        createdTraegerschaft = result;
                    });
                $httpBackend.flush();
                checkFieldValues(createdTraegerschaft, mockTraegerschaft);
            });
        });

        describe('updateTraegerschaft', () => {
            it('should update a traegerschaft', () => {
                mockTraegerschaft.name = 'changedname';
                mockTraegerschaftRest = ebeguRestUtil.traegerschaftToRestObject({}, mockTraegerschaft);
                let updatedTraegerschaft: TSTraegerschaft;
                $httpBackend.expectPUT(traegerschaftRS.serviceURL, mockTraegerschaftRest).respond(mockTraegerschaftRest);

                traegerschaftRS.updateTraegerschaft(mockTraegerschaft)
                    .then((result) => {
                        updatedTraegerschaft = result;
                    });
                $httpBackend.flush();
                checkFieldValues(updatedTraegerschaft, mockTraegerschaft);
            });
        });

        describe('removeTraegerschaft', () => {
            it('should remove a traegerschaft', () => {
                $httpBackend.expectDELETE(traegerschaftRS.serviceURL + '/' + mockTraegerschaft.id)
                    .respond(200);

                let deleteResult: any;
                traegerschaftRS.removeTraegerschaft(mockTraegerschaft.id)
                    .then((result) => {
                        deleteResult = result;
                    });
                $httpBackend.flush();
                expect(deleteResult).toBeDefined();
                expect(deleteResult.status).toEqual(200);
            });
        });

        describe('getAllTraegerschaften', () => {
            it('should return all Traegerschaften', () => {
                const traegerschaftenRestArray: Array<any> = [mockTraegerschaftRest, mockTraegerschaftRest];
                $httpBackend.expectGET(traegerschaftRS.serviceURL).respond(traegerschaftenRestArray);

                let returnedTraegerschaften: Array<TSTraegerschaft>;
                traegerschaftRS.getAllTraegerschaften().then((result) => {
                    returnedTraegerschaften = result;
                });
                $httpBackend.flush();
                expect(returnedTraegerschaften).toBeDefined();
                expect(returnedTraegerschaften.length).toEqual(2);
                checkFieldValues(returnedTraegerschaften[0], traegerschaftenRestArray[0]);
                checkFieldValues(returnedTraegerschaften[1], traegerschaftenRestArray[1]);
            });
        });
    });

    function checkFieldValues(traegerschaft1: TSTraegerschaft, traegerschaft2: TSTraegerschaft) {
        expect(traegerschaft1).toBeDefined();
        expect(traegerschaft1.name).toEqual(traegerschaft2.name);
        expect(traegerschaft1.id).toEqual(traegerschaft2.id);
    }

});
