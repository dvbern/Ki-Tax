/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
import {HttpClientModule} from '@angular/common/http';
import {TestBed} from '@angular/core/testing';
import {TranslateService} from '@ngx-translate/core';
import {ErrorService} from '../../../core/errors/service/ErrorService';

import {LastenausgleichTSService} from './lastenausgleich-ts.service';

describe('LastenausgleichTSService', () => {
    let service: LastenausgleichTSService;
    const errorServiceSpy = jasmine.createSpyObj<ErrorService>(
        ErrorService.name,
        ['addMesageAsInfo']
    );
    const translateSpy = jasmine.createSpyObj<TranslateService>(
        TranslateService.name,
        ['instant']
    );

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientModule],
            providers: [
                {provide: ErrorService, useValue: errorServiceSpy},
                {provide: TranslateService, useValue: translateSpy}
            ]
        });
        service = TestBed.inject(LastenausgleichTSService);
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });
});
