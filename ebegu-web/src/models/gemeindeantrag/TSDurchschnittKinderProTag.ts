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

export class TSDurchschnittKinderProTag {
    private _fruehbetreuung: number;
    private _mittagsbetreuung: number;
    private _nachmittagsbetreuung1: number;
    private _nachmittagsbetreuung2: number;

    public constructor() {
    }

    public get fruehbetreuung(): number {
        return this._fruehbetreuung;
    }

    public set fruehbetreuung(value: number) {
        this._fruehbetreuung = value;
    }

    public get mittagsbetreuung(): number {
        return this._mittagsbetreuung;
    }

    public set mittagsbetreuung(value: number) {
        this._mittagsbetreuung = value;
    }

    public get nachmittagsbetreuung1(): number {
        return this._nachmittagsbetreuung1;
    }

    public set nachmittagsbetreuung1(value: number) {
        this._nachmittagsbetreuung1 = value;
    }

    public get nachmittagsbetreuung2(): number {
        return this._nachmittagsbetreuung2;
    }

    public set nachmittagsbetreuung2(value: number) {
        this._nachmittagsbetreuung2 = value;
    }
}
