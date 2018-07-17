/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
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

import {async, TestBed} from '@angular/core/testing';
import {Observable} from 'rxjs/Observable';
import {TSPostEingangEvent} from '../../models/enums/TSPostEingangEvent';
import {PosteingangService} from './posteingang.service';

describe('posteingangService', function () {

    let posteingangService: PosteingangService;

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            providers: [
                PosteingangService,
            ],
        });

        posteingangService = TestBed.get(PosteingangService);
    }));

    describe('posteingangChanged', function () {
        it('changes the status to POSTEINGANG_MAY_CHANGED', function () {
            posteingangService.posteingangChanged();

            let event: Observable<TSPostEingangEvent> = posteingangService.get$(TSPostEingangEvent.POSTEINGANG_MAY_CHANGED);
            event.subscribe(value => {
                expect(value).toBe(TSPostEingangEvent.POSTEINGANG_MAY_CHANGED);
            });
        });
    });
});
