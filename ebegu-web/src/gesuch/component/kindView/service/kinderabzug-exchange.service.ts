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
export class KinderabzugExchangeService {
    private readonly _forms: NgForm[] = [];
    private readonly _geburtsdatumChanged: Subject<moment.Moment> =
        new Subject();
    private readonly _formValidationTriggered: Subject<void> = new Subject();

    private readonly _familienErgaenzendeBetreuungChanged: Subject<moment.Moment> =
        new Subject();

    public get forms(): NgForm[] {
        return this._forms;
    }

    public addForm(toAdd: NgForm) {
        this._forms.push(toAdd);
    }

    public getGeburtsdatumChanged$(): Observable<moment.Moment> {
        return this._geburtsdatumChanged.asObservable();
    }

    public getFormValidationTriggered$(): Observable<void> {
        return this._formValidationTriggered.asObservable();
    }

    public triggerGeburtsdatumChanged(date: moment.Moment): void {
        this._geburtsdatumChanged.next(date);
    }

    public triggerFormValidation(): void {
        this._formValidationTriggered.next();
        for (const form of this.forms) {
            form?.form.markAllAsTouched();
            form?.onSubmit(null);
        }
    }

    public getFamilienErgaenzendeBetreuungChanged$(): Observable<moment.Moment> {
        return this._familienErgaenzendeBetreuungChanged.asObservable();
    }

    public triggerFamilienErgaenzendeBetreuungChanged(): void {
        this._familienErgaenzendeBetreuungChanged.next();
    }

    public anyFormInvalid(): boolean {
        return this._forms.reduce((prev, cur) => cur.invalid || prev, false);
    }
}
