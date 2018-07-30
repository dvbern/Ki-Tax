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
import {filter} from 'rxjs/operators';
import {ReplaySubject, Observable} from 'rxjs';
import {TSAuthEvent} from '../../models/enums/TSAuthEvent';
import {Log} from '../../utils/LogFactory';

/**
 * This service can be used to throw TSAuthEvent. When a user logs in or out, throwing the right event
 * will let every class using this service know, that something happened.
 */
@Injectable()
export class AuthLifeCycleService {

    private readonly LOG: Log = Log.createLog(AuthLifeCycleService);

    private readonly _authLifeCycleSubject$ = new ReplaySubject<TSAuthEvent>(1); // use ReplaySubject because we don't have an initial value

    constructor() {}

    public changeAuthStatus(status: TSAuthEvent, message?: string): void {
        if (status) {
            this.LOG.info(`An Auth Event has been thrown ${status}. Message: ${message}`);
            this._authLifeCycleSubject$.next(status);

        } else {
            this.LOG.error(`An undefined AuthEvent is not allowed. No event thrown. Message: ${message}`);
        }
    }

    public getAll$(): Observable<TSAuthEvent> {
        return this._authLifeCycleSubject$.asObservable();
    }

    public get$(event: TSAuthEvent): Observable<TSAuthEvent> {
        return this._authLifeCycleSubject$
            .asObservable()
            .pipe(filter(value => value === event)) as Observable<TSAuthEvent>;
    }
}
