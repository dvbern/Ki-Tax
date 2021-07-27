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

export class TSPagination {

    private _number: number = 20;
    private _totalItemCount: number = 0;
    private _start: number = 0;

    public get number(): number {
        return this._number;
    }

    public set number(value: number) {
        this._number = value;
    }

    public get totalItemCount(): number {
        return this._totalItemCount;
    }

    public set totalItemCount(value: number) {
        this._totalItemCount = value;
    }

    public get start(): number {
        return this._start;
    }

    public set start(value: number) {
        this._start = value;
    }

    public calculatePage(): number {
        return Math.floor(this.start / this.number);
    }
}
