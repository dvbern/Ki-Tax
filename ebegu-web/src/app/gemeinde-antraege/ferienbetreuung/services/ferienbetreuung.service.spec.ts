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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
import {HttpClientModule} from '@angular/common/http';
import {TestBed} from '@angular/core/testing';
import {EinstellungRS} from '../../../../admin/service/einstellungRS.rest';

import {FerienbetreuungService} from './ferienbetreuung.service';

const einstellungRSSpy = jasmine.createSpyObj<EinstellungRS>(EinstellungRS.name,
    ['getPauschalbetraegeFerienbetreuung']);

describe('FerienbetreuungService', () => {
  let service: FerienbetreuungService;

  beforeEach(() => {
    TestBed.configureTestingModule({
        imports: [HttpClientModule],
        providers: [
            {provide: EinstellungRS, useValue: einstellungRSSpy}
        ]
    });
    service = TestBed.inject(FerienbetreuungService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
