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
import {ngServicesMock} from '../../../hybridTools/ngServicesMocks';
import {TSBetreuungsangebotTyp} from '../../../models/enums/TSBetreuungsangebotTyp';
import TSAdresse from '../../../models/TSAdresse';
import TSInstitution from '../../../models/TSInstitution';
import TSInstitutionStammdaten from '../../../models/TSInstitutionStammdaten';
import {TSDateRange} from '../../../models/types/TSDateRange';
import DateUtil from '../../../utils/DateUtil';
import EbeguRestUtil from '../../../utils/EbeguRestUtil';
import {CORE_JS_MODULE} from '../core.angularjs.module';
import {InstitutionStammdatenRS} from './institutionStammdatenRS.rest';

describe('institutionStammdatenRS', () => {

    let institutionStammdatenRS: InstitutionStammdatenRS;
    let $httpBackend: angular.IHttpBackendService;
    let ebeguRestUtil: EbeguRestUtil;
    let mockInstitutionStammdaten: TSInstitutionStammdaten;
    let mockInstitutionStammdatenRest: any;
    let mockInstitution: TSInstitution;
    let mockAdresse: TSAdresse;
    let today: moment.Moment;

    beforeEach(angular.mock.module(CORE_JS_MODULE.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(angular.mock.inject($injector => {
        institutionStammdatenRS = $injector.get('InstitutionStammdatenRS');
        $httpBackend = $injector.get('$httpBackend');
        ebeguRestUtil = $injector.get('EbeguRestUtil');
    }));

    beforeEach(() => {
        today = DateUtil.today();
        mockInstitution = new TSInstitution('Institution_Test');
        mockAdresse = new TSAdresse();
        mockInstitutionStammdaten = new TSInstitutionStammdaten('InstStammDaten_Test', 250, 12,
            TSBetreuungsangebotTyp.KITA, mockInstitution, mockAdresse, new TSDateRange(today, today));
        mockInstitutionStammdaten.id = '2afc9d9a-957e-4550-9a22-97624a1d8f05';
        mockInstitutionStammdatenRest = ebeguRestUtil.institutionStammdatenToRestObject({}, mockInstitutionStammdaten);
    });

    describe('Public API', () => {
        it('check Service name', () => {
            expect(institutionStammdatenRS.getServiceName()).toBe('InstitutionStammdatenRS');
        });
        it('should include a findInstitutionStammdaten() function', () => {
            expect(institutionStammdatenRS.findInstitutionStammdaten).toBeDefined();
        });
        it('should include a createInstitutionStammdaten() function', () => {
            expect(institutionStammdatenRS.createInstitutionStammdaten).toBeDefined();
        });
        it('should include a updateInstitutionStammdaten() function', () => {
            expect(institutionStammdatenRS.updateInstitutionStammdaten).toBeDefined();
        });
        it('should include a removeInstitutionStammdaten() function', () => {
            expect(institutionStammdatenRS.removeInstitutionStammdaten).toBeDefined();
        });
        it('should include a getAllInstitutionStammdaten() function', () => {
            expect(institutionStammdatenRS.getAllInstitutionStammdaten).toBeDefined();
        });
        it('should include a getAllInstitutionStammdatenByDate() function', () => {
            expect(institutionStammdatenRS.getAllInstitutionStammdatenByDate).toBeDefined();
        });
    });

    describe('API Usage', () => {
        describe('findInstitutionStammdaten', () => {
            it('should return the InstitutionStammdaten by id', () => {
                $httpBackend.expectGET(institutionStammdatenRS.serviceURL + '/id/' + encodeURIComponent(mockInstitutionStammdaten.id))
                    .respond(mockInstitutionStammdatenRest);

                let foundInstitutionStammdaten: TSInstitutionStammdaten;
                institutionStammdatenRS.findInstitutionStammdaten(mockInstitutionStammdaten.id).then((result) => {
                    foundInstitutionStammdaten = result;
                });
                $httpBackend.flush();
                checkFieldValues(foundInstitutionStammdaten, mockInstitutionStammdaten);
            });

        });

        describe('createInstitutionStammdaten', () => {
            it('should create a InstitutionStammdaten', () => {
                let createdInstitutionStammdaten: TSInstitutionStammdaten;
                $httpBackend.expectPUT(institutionStammdatenRS.serviceURL, mockInstitutionStammdatenRest).respond(mockInstitutionStammdatenRest);

                institutionStammdatenRS.createInstitutionStammdaten(mockInstitutionStammdaten)
                    .then((result) => {
                        createdInstitutionStammdaten = result;
                    });
                $httpBackend.flush();
                checkFieldValues(createdInstitutionStammdaten, mockInstitutionStammdaten);
            });
        });

        describe('updateInstitutionStammdaten', () => {
            it('should update a InstitutionStammdaten', () => {
                mockInstitutionStammdaten.iban = 'CH123456';
                mockInstitutionStammdatenRest = ebeguRestUtil.institutionStammdatenToRestObject({}, mockInstitutionStammdaten);
                let updatedInstitutionStammdaten: TSInstitutionStammdaten;
                $httpBackend.expectPUT(institutionStammdatenRS.serviceURL, mockInstitutionStammdatenRest).respond(mockInstitutionStammdatenRest);

                institutionStammdatenRS.updateInstitutionStammdaten(mockInstitutionStammdaten)
                    .then((result) => {
                        updatedInstitutionStammdaten = result;
                    });
                $httpBackend.flush();
                checkFieldValues(updatedInstitutionStammdaten, mockInstitutionStammdaten);
            });
        });

        describe('removeInstitutionStammdaten', () => {
            it('should remove a InstitutionStammdaten', () => {
                $httpBackend.expectDELETE(institutionStammdatenRS.serviceURL + '/' + encodeURIComponent(mockInstitutionStammdaten.id))
                    .respond(200);

                let deleteResult: any;
                institutionStammdatenRS.removeInstitutionStammdaten(mockInstitutionStammdaten.id)
                    .then((result) => {
                        deleteResult = result;
                    });
                $httpBackend.flush();
                expect(deleteResult).toBeDefined();
                expect(deleteResult.status).toEqual(200);
            });
        });

        describe('getAllInstitutionStammdaten', () => {
            it('should return all InstitutionStammdaten', () => {
                const institutionStammdatenRestArray: Array<any> = [mockInstitutionStammdatenRest, mockInstitutionStammdatenRest];
                $httpBackend.expectGET(institutionStammdatenRS.serviceURL).respond(institutionStammdatenRestArray);

                let returnedInstitutionStammdaten: Array<TSInstitutionStammdaten>;
                institutionStammdatenRS.getAllInstitutionStammdaten().then((result) => {
                    returnedInstitutionStammdaten = result;
                });
                $httpBackend.flush();
                expect(returnedInstitutionStammdaten).toBeDefined();
                expect(returnedInstitutionStammdaten.length).toEqual(2);
                checkFieldValues(returnedInstitutionStammdaten[0], institutionStammdatenRestArray[0]);
                checkFieldValues(returnedInstitutionStammdaten[1], institutionStammdatenRestArray[1]);
            });
        });

        describe('getAllInstitutionStammdatenByDate', () => {
            it('should return all InstitutionStammdaten im gegebenen Datum', () => {
                const institutionStammdatenRestArray: Array<any> = [mockInstitutionStammdatenRest, mockInstitutionStammdatenRest];
                $httpBackend.expectGET(institutionStammdatenRS.serviceURL + '/date?date='
                    + DateUtil.momentToLocalDate(today))
                    .respond(institutionStammdatenRestArray);

                let returnedInstitutionStammdaten: Array<TSInstitutionStammdaten>;
                institutionStammdatenRS.getAllInstitutionStammdatenByDate(today).then((result) => {
                    returnedInstitutionStammdaten = result;
                });
                $httpBackend.flush();
                expect(returnedInstitutionStammdaten).toBeDefined();
                expect(returnedInstitutionStammdaten.length).toEqual(2);
                checkFieldValues(returnedInstitutionStammdaten[0], institutionStammdatenRestArray[0]);
                checkFieldValues(returnedInstitutionStammdaten[1], institutionStammdatenRestArray[1]);
            });
        });
    });

    function checkFieldValues(institutionStammdaten1: TSInstitutionStammdaten,
                              institutionStammdaten2: TSInstitutionStammdaten) {
        expect(institutionStammdaten1).toBeDefined();
        expect(institutionStammdaten1.iban).toEqual(institutionStammdaten2.iban);
        expect(institutionStammdaten1.id).toEqual(institutionStammdaten2.id);
        expect(institutionStammdaten1.institution.name).toEqual(institutionStammdaten2.institution.name);
    }

});
