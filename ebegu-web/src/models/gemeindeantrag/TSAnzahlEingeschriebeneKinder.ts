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

export class TSAnzahlEingeschriebeneKinder {
    private _overall: number;
    private _vorschulalter: number;
    private _kindergarten:  number;
    private _primarstufe: number;
    private _sekundarstufe: number;

    public constructor() {
    }

    public get overall(): number {
        return this._overall;
    }

    public set overall(value: number) {
        this._overall = value;
    }

    public get vorschulalter(): number {
        return this._vorschulalter;
    }

    public set vorschulalter(value: number) {
        this._vorschulalter = value;
    }

    public get kindergarten(): number {
        return this._kindergarten;
    }

    public set kindergarten(value: number) {
        this._kindergarten = value;
    }

    public get primarstufe(): number {
        return this._primarstufe;
    }

    public set primarstufe(value: number) {
        this._primarstufe = value;
    }

    public get sekundarstufe(): number {
        return this._sekundarstufe;
    }

    public set sekundarstufe(value: number) {
        this._sekundarstufe = value;
    }
}
