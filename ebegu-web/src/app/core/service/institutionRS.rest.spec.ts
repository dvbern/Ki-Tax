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

import {HttpClient, HttpClientModule} from '@angular/common/http';
import {TestBed} from '@angular/core/testing';
import {of} from 'rxjs';
import {TSInstitution} from '../../../models/TSInstitution';
import {TSMandant} from '../../../models/TSMandant';
import {TSTraegerschaft} from '../../../models/TSTraegerschaft';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';
import {InstitutionRS} from './institutionRS.rest';

describe('institutionRS', () => {

    let institutionRS: InstitutionRS;
    const ebeguRestUtil: EbeguRestUtil = new EbeguRestUtil();
    let mockInstitution: TSInstitution;
    let mockInstitutionRest: any;
    let mandant: TSMandant;
    let traegerschaft: TSTraegerschaft;

    const mockHttpClient = jasmine.createSpyObj<HttpClient>(HttpClient.name, ['get']);

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientModule],
            providers: [{provide: HttpClient, useValue: mockHttpClient}],
        });
        institutionRS = TestBed.inject(InstitutionRS);
    });

    beforeEach(() => {
        traegerschaft = new TSTraegerschaft();
        traegerschaft.name = 'Traegerschaft_Test';
        mandant = new TSMandant('Mandant_Test');
        mockInstitution = new TSInstitution('InstitutionTest', traegerschaft, mandant);
        mockInstitution.id = '2afc9d9a-957e-4550-9a22-97624a1d8f05';
        mockInstitutionRest = ebeguRestUtil.institutionToRestObject({}, mockInstitution);
    });

    describe('Public API', () => {
        it('check URI', () => {
            expect(institutionRS.serviceURL).toContain('institutionen');
        });
    });

    describe('API Usage', () => {
        describe('findInstitution', () => {
            it('should return the Institution by id', () => {
                mockHttpClient.get.and.returnValue(of(mockInstitutionRest));

                let foundInstitution: TSInstitution;
                institutionRS.findInstitution(mockInstitution.id).then(result => {
                    foundInstitution = result;
                    checkFieldValues(foundInstitution, mockInstitution);
                });
            });

        });

        describe('getAllInstitutionen', () => {
            it('should return all Institutionen', () => {
                const institutionenRestArray = [mockInstitutionRest, mockInstitutionRest];
                mockHttpClient.get.and.returnValue(of(institutionenRestArray));

                let returnedInstitution: Array<TSInstitution>;
                institutionRS.getAllInstitutionen().then(result => {
                    returnedInstitution = result;

                    expect(returnedInstitution).toBeDefined();
                    expect(returnedInstitution.length).toEqual(2);
                    checkFieldValues(returnedInstitution[0], institutionenRestArray[0]);
                    checkFieldValues(returnedInstitution[1], institutionenRestArray[1]);
                });
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
