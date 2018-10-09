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

import {TSGemeindeStatus} from './enums/TSGemeindeStatus';
import TSAbstractEntity from './TSAbstractEntity';

export default class TSGemeinde extends TSAbstractEntity {

    private _name: string;
    private _gemeindeNummer: number;
    private _bfsNummer: number;
    private _status: TSGemeindeStatus;

    public constructor(
        name?: string,
        gemeindeNummer?: number,
        bfsNummer?: number,
        status?: TSGemeindeStatus
    ) {
        super();
        this._name = name;
        this._gemeindeNummer = gemeindeNummer;
        this._bfsNummer = bfsNummer;
        this._status = status;
    }

    public get name(): string {
        return this._name;
    }

    public set name(value: string) {
        this._name = value;
    }

    public get gemeindeNummer(): number {
        return this._gemeindeNummer;
    }

    public set gemeindeNummer(value: number) {
        this._gemeindeNummer = value;
    }

    public get status(): TSGemeindeStatus {
        return this._status;
    }

    public set status(value: TSGemeindeStatus) {
        this._status = value;
    }

    public get bfsNummer(): number {
        return this._bfsNummer;
    }

    public set bfsNummer(value: number) {
        this._bfsNummer = value;
    }
}
