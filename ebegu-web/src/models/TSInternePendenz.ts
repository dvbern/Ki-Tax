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

import * as moment from 'moment';
import {TSAbstractEntity} from './TSAbstractEntity';
import {TSGesuch} from './TSGesuch';

export class TSInternePendenz extends TSAbstractEntity {
    private _gesuch: TSGesuch;
    private _termin: moment.Moment;
    private _text: string;
    private _erledigt: boolean;

    public get gesuch(): TSGesuch {
        return this._gesuch;
    }

    public set gesuch(value: TSGesuch) {
        this._gesuch = value;
    }

    public get termin(): moment.Moment {
        return this._termin;
    }

    public set termin(value: moment.Moment) {
        this._termin = value;
    }

    public get text(): string {
        return this._text;
    }

    public set text(value: string) {
        this._text = value;
    }

    public get erledigt(): boolean {
        return this._erledigt;
    }

    public set erledigt(value: boolean) {
        this._erledigt = value;
    }
}
