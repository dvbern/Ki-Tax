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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import * as moment from 'moment';

export class TSRueckforderungZahlung {
    private _datumErstellt: moment.Moment;
    private _stufe: string;
    private _betrag: number;
    private _ausgeloest: boolean;

    public get datumErstellt(): moment.Moment {
        return this._datumErstellt;
    }

    public set datumErstellt(value: moment.Moment) {
        this._datumErstellt = value;
    }

    public get stufe(): string {
        return this._stufe;
    }

    public set stufe(value: string) {
        this._stufe = value;
    }

    public get betrag(): number {
        return this._betrag;
    }

    public set betrag(value: number) {
        this._betrag = value;
    }

    public get ausgeloest(): boolean {
        return this._ausgeloest;
    }

    public set ausgeloest(value: boolean) {
        this._ausgeloest = value;
    }
}
