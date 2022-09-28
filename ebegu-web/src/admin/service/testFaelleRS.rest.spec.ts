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
import {TestFaelleRS} from './testFaelleRS.rest';

describe('TestFaelleRS', () => {

    let testFaelleRS: TestFaelleRS;
    const mockHttpClient = jasmine.createSpyObj<HttpClient>(HttpClient.name, ['get']);

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientModule],
            providers: [{provide: HttpClient, useValue: mockHttpClient}],
        });
        testFaelleRS = TestBed.inject(TestFaelleRS);
    });

    describe('Public API', () => {
        it('check URI', () => {
            expect(testFaelleRS.serviceURL).toContain('testfaelle');
        });
    });

    describe('API Usage', () => {
        describe('createTestFall', () => {
            it('should call createTestFall', () => {
                mockHttpClient.get.and.returnValue(of({}));
                testFaelleRS.createTestFall('1', null, null, false, false);
            });
        });
    });
});
