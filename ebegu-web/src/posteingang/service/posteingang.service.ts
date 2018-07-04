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
import {Observable} from 'rxjs/Observable';
import {filter} from 'rxjs/operators';
import {ReplaySubject} from 'rxjs/ReplaySubject';
import {Subject} from 'rxjs/Subject';
import {TSPostEingangEvent} from '../../models/enums/TSPostEingangEvent';
import {Log} from '../../utils/LogFactory';

@Injectable()
export class PosteingangService {

    private LOG: Log = Log.createLog(PosteingangService);

    private _posteingangSubject: Subject<TSPostEingangEvent> = new ReplaySubject(); // use ReplaySubject because we don't have an initial value

    constructor() {}

    public posteingangChanged(): void {
        this.LOG.info('Thwrowing TSPostEingangEvent.POSTEINGANG_MAY_CHANGED because the number of elements in Posteingang may have changed');
        this._posteingangSubject.next(TSPostEingangEvent.POSTEINGANG_MAY_CHANGED);
    }

    public get$(event: TSPostEingangEvent): Observable<TSPostEingangEvent> {
        return this._posteingangSubject
            .asObservable()
            .pipe(filter(value => value === event)) as Observable<TSPostEingangEvent>;
    }
}
