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

import {Injectable} from '@angular/core';
import {Observable, ReplaySubject} from 'rxjs';
import {filter} from 'rxjs/operators';
import {TSPostEingangEvent} from '../../../models/enums/TSPostEingangEvent';
import {Log, LogFactory} from '../../core/logging/LogFactory';

@Injectable({
    providedIn: 'root'
})
export class PosteingangService {
    private readonly LOG: Log = LogFactory.createLog(PosteingangService.name);

    // use ReplaySubject because we don't have an initial value
    private readonly _posteingangSubject$ =
        new ReplaySubject<TSPostEingangEvent>(1);

    public posteingangChanged(): void {
        this.LOG.info(
            'Thwrowing TSPostEingangEvent.POSTEINGANG_MIGHT_HAVE_CHANGED because the number of ' +
                'elements in Posteingang might have changed'
        );
        this._posteingangSubject$.next(
            TSPostEingangEvent.POSTEINGANG_MIGHT_HAVE_CHANGED
        );
    }

    public get$(event: TSPostEingangEvent): Observable<TSPostEingangEvent> {
        return this._posteingangSubject$
            .asObservable()
            .pipe(filter(value => value === event));
    }
}
