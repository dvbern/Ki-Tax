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

import {TSAbstractEntity} from './TSAbstractEntity';

export class TSBfsGemeinde extends TSAbstractEntity {
    private _name: string;
    private _bfsNummer: number;

    public constructor(gemeinde?: string, bfsNummer?: number) {
        super();
        this._name = gemeinde;
        this._bfsNummer = bfsNummer;
    }

    public get name(): string {
        return this._name;
    }

    public set name(value: string) {
        this._name = value;
    }

    public get bfsNummer(): number {
        return this._bfsNummer;
    }

    public set bfsNummer(value: number) {
        this._bfsNummer = value;
    }
}
