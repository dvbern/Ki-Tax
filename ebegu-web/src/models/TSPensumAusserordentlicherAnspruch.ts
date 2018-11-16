/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

import {TSAbstractIntegerPensumEntity} from './TSAbstractIntegerPensumEntity';
import {TSDateRange} from './types/TSDateRange';

export class TSPensumAusserordentlicherAnspruch extends TSAbstractIntegerPensumEntity {

    private _begruendung: string;

    public constructor(begruendung?: string, pensum?: number, gueltigkeit?: TSDateRange) {
        super(pensum, gueltigkeit);
        this._begruendung = begruendung;
    }

    public get begruendung(): string {
        return this._begruendung;
    }

    public set begruendung(value: string) {
        this._begruendung = value;
    }
}
