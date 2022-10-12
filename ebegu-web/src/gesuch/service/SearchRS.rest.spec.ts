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

import {HttpClient} from '@angular/common/http';
import {TestBed} from '@angular/core/testing';
import {of} from 'rxjs';
import {TSAntragDTO} from '../../models/TSAntragDTO';
import {TSAntragSearchresultDTO} from '../../models/TSAntragSearchresultDTO';
import {TestDataUtil} from '../../utils/TestDataUtil.spec';
import {SearchRS} from './searchRS.rest';

/* eslint-disable no-magic-numbers */
describe('searchRS', () => {

    let service: SearchRS;
    const mockHttpClient = jasmine.createSpyObj<HttpClient>(HttpClient.name, ['post']);
    const fallNummer = 1234;

    beforeEach(() => {
        TestBed.configureTestingModule({
            providers: [
                {
                    provide: HttpClient,
                    useValue: mockHttpClient
                }
            ]
        });
        service = TestBed.inject(SearchRS);
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });

    describe('API Usage', () => {
        describe('getPendenzenList', () => {
            it('should return all pending Antraege', () => {
                const tsAntragDTO = new TSAntragDTO();
                tsAntragDTO.fallNummer = fallNummer;
                const searchResult: any = {
                    antragDTOs: [tsAntragDTO],
                    paginationDTO: {totalItemCount: 1}
                };

                const filter: any = {};
                mockHttpClient.post.and.returnValue(of(searchResult));

                let foundPendenzen: TSAntragSearchresultDTO;
                service.getPendenzenList(filter).subscribe(result => {
                    foundPendenzen = result;
                    expect(foundPendenzen).toBeDefined();
                    TestDataUtil.compareDefinedProperties(foundPendenzen.antragDTOs[0], tsAntragDTO);
                }, error => console.error(error));
            });
        });
    });

});
