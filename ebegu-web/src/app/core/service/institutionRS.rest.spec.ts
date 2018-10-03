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
import TSInstitution from '../../../models/TSInstitution';
import {TSMandant} from '../../../models/TSMandant';
import {TSTraegerschaft} from '../../../models/TSTraegerschaft';
import EbeguRestUtil from '../../../utils/EbeguRestUtil';
import {CORE_JS_MODULE} from '../core.angularjs.module';
import {InstitutionRS} from './institutionRS.rest';
import {IHttpBackendService} from 'angular';

describe('institutionRS', () => {

    let institutionRS: InstitutionRS;
    let $httpBackend: IHttpBackendService;
    let ebeguRestUtil: EbeguRestUtil;
    let mockInstitution: TSInstitution;
    let mockInstitutionRest: any;
    let mandant: TSMandant;
    let traegerschaft: TSTraegerschaft;

    beforeEach(angular.mock.module(CORE_JS_MODULE.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(angular.mock.inject($injector => {
        institutionRS = $injector.get('InstitutionRS');
        $httpBackend = $injector.get('$httpBackend');
        ebeguRestUtil = $injector.get('EbeguRestUtil');
    }));

    beforeEach(() => {
        traegerschaft = new TSTraegerschaft('Traegerschaft_Test');
        mandant = new TSMandant('Mandant_Test');
        mockInstitution = new TSInstitution('InstitutionTest', traegerschaft, mandant);
        mockInstitution.id = '2afc9d9a-957e-4550-9a22-97624a1d8f05';
        mockInstitutionRest = ebeguRestUtil.institutionToRestObject({}, mockInstitution);
    });

    describe('Public API', () => {
        it('check URI', () => {
            expect(institutionRS.serviceURL).toContain('institutionen');
        });
        it('check Service name', () => {
            expect(institutionRS.getServiceName()).toBe('InstitutionRS');
        });
        it('should include a findInstitution() function', () => {
            expect(institutionRS.findInstitution).toBeDefined();
        });
        it('should include a createInstitution() function', () => {
            expect(institutionRS.createInstitution).toBeDefined();
        });
        it('should include a updateInstitution() function', () => {
            expect(institutionRS.updateInstitution).toBeDefined();
        });
        it('should include a removeInstitution() function', () => {
            expect(institutionRS.removeInstitution).toBeDefined();
        });
        it('should include a getAllInstitutionen() function', () => {
            expect(institutionRS.getAllInstitutionen).toBeDefined();
        });
    });

    describe('API Usage', () => {
        describe('findInstitution', () => {
            it('should return the Institution by id', () => {
                const url = `${institutionRS.serviceURL}/id/${mockInstitution.id}`;
                $httpBackend.expectGET(url).respond(mockInstitutionRest);

                let foundInstitution: TSInstitution;
                institutionRS.findInstitution(mockInstitution.id).then(result => {
                    foundInstitution = result;
                });
                $httpBackend.flush();
                checkFieldValues(foundInstitution, mockInstitution);
            });

        });

        describe('createInstitution', () => {
            it('should create an institution', () => {
                let createdInstitution: TSInstitution;
                $httpBackend.expectPOST(institutionRS.serviceURL, mockInstitutionRest).respond(mockInstitutionRest);

                institutionRS.createInstitution(mockInstitution)
                    .then(result => {
                        createdInstitution = result;
                    });
                $httpBackend.flush();
                checkFieldValues(createdInstitution, mockInstitution);
            });
        });

        describe('updateInstitution', () => {
            it('should update an institution', () => {
                mockInstitution.name = 'changedname';
                mockInstitutionRest = ebeguRestUtil.institutionToRestObject({}, mockInstitution);
                let updatedInstitution: TSInstitution;
                $httpBackend.expectPUT(institutionRS.serviceURL, mockInstitutionRest).respond(mockInstitutionRest);

                institutionRS.updateInstitution(mockInstitution)
                    .then(result => {
                        updatedInstitution = result;
                    });
                $httpBackend.flush();
                checkFieldValues(updatedInstitution, mockInstitution);
            });
        });

        describe('removeInstitution', () => {
            it('should remove an institution', () => {
                const httpOk = 200;
                $httpBackend.expectDELETE(`${institutionRS.serviceURL}/${encodeURIComponent(mockInstitution.id)}`)
                    .respond(httpOk);

                let deleteResult: any;
                institutionRS.removeInstitution(mockInstitution.id)
                    .then(result => {
                        deleteResult = result;
                    });
                $httpBackend.flush();
                expect(deleteResult).toBeDefined();
                expect(deleteResult.status).toEqual(httpOk);
            });
        });

        describe('getAllInstitutionen', () => {
            it('should return all Institutionen', () => {
                const institutionenRestArray = [mockInstitutionRest, mockInstitutionRest];
                $httpBackend.expectGET(institutionRS.serviceURL).respond(institutionenRestArray);

                let returnedInstitution: Array<TSInstitution>;
                institutionRS.getAllInstitutionen().then(result => {
                    returnedInstitution = result;
                });
                $httpBackend.flush();
                expect(returnedInstitution).toBeDefined();
                expect(returnedInstitution.length).toEqual(2);
                checkFieldValues(returnedInstitution[0], institutionenRestArray[0]);
                checkFieldValues(returnedInstitution[1], institutionenRestArray[1]);
            });
        });

    });

    function checkFieldValues(institution1: TSInstitution, institution2: TSInstitution): void {
        expect(institution1).toBeDefined();
        expect(institution1.name).toEqual(institution2.name);
        expect(institution1.id).toEqual(institution2.id);
        expect(institution1.mandant.name).toEqual(institution2.mandant.name);
        expect(institution1.traegerschaft.name).toEqual(institution2.traegerschaft.name);
    }

});
