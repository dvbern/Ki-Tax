/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

import {Injectable} from '@angular/core';
import {NgForm} from '@angular/forms';
import * as moment from 'moment';
import {Observable, Subject} from 'rxjs';

@Injectable()
export class FjkvKinderabzugExchangeService {

    private _form: NgForm;
    private _geburtsdatumChanged: Subject<moment.Moment> = new Subject();
    private _formValidationTriggered: Subject<void> = new Subject();

    public get form(): NgForm {
        return this._form;
    }

    public set form(value: NgForm) {
        this._form = value;
    }

    public get geburtsdatumChanged$(): Observable<moment.Moment> {
        return this._geburtsdatumChanged.asObservable();
    }

    public get formValidationTriggered$(): Observable<void> {
        return this._formValidationTriggered.asObservable();
    }

    public triggerGeburtsdatumChanged(date: moment.Moment): void {
        this._geburtsdatumChanged.next(date);
    }

    public triggerFormValidation(): void {
        this._formValidationTriggered.next();
    }
}
