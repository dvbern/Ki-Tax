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

import {waitForAsync, TestBed} from '@angular/core/testing';
import {TSPostEingangEvent} from '../../../models/enums/TSPostEingangEvent';
import {PosteingangService} from './posteingang.service';

describe('posteingangService', () => {

    let posteingangService: PosteingangService;

    beforeEach(waitForAsync(() => {
        TestBed.configureTestingModule({
            providers: [
                PosteingangService,
            ],
        });

        posteingangService = TestBed.inject<PosteingangService>(PosteingangService);
    }));

    describe('posteingangChanged', () => {
        it('changes the status to POSTEINGANG_MIGHT_HAVE_CHANGED', done => {
            posteingangService.posteingangChanged();

            posteingangService.get$(TSPostEingangEvent.POSTEINGANG_MIGHT_HAVE_CHANGED)
                .subscribe(
                    value => {
                        expect(value).toBe(TSPostEingangEvent.POSTEINGANG_MIGHT_HAVE_CHANGED);
                        done();
                    },
                    done.fail,
                );
        });
    });
});
