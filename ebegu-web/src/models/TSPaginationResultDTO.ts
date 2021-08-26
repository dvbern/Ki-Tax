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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

export class TSPaginationResultDTO<T> {

    private _resultList: Array<T>;
    private _totalResultSize: number;

    public constructor(resultList?: Array<T>, totalResultSize?: number) {
        this._resultList = resultList;
        this._totalResultSize = totalResultSize;
    }

    public get resultList(): Array<T> {
        return this._resultList;
    }

    public set resultList(value: Array<T>) {
        this._resultList = value;
    }

    public get totalResultSize(): number {
        return this._totalResultSize;
    }

    public set totalResultSize(value: number) {
        this._totalResultSize = value;
    }
}
