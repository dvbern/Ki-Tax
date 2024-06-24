/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

export class TSRueckforderungMitteilung extends TSAbstractEntity {
    private _betreff: string;
    private _inhalt: string;
    private _sendeDatum: moment.Moment;

    public constructor() {
        super();
    }

    public get betreff(): string {
        return this._betreff;
    }

    public set betreff(value: string) {
        this._betreff = value;
    }

    public get inhalt(): string {
        return this._inhalt;
    }

    public set inhalt(value: string) {
        this._inhalt = value;
    }

    public get sendeDatum(): moment.Moment {
        return this._sendeDatum;
    }

    public set sendeDatum(value: moment.Moment) {
        this._sendeDatum = value;
    }
}
