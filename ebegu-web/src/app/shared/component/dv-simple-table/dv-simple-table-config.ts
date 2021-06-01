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
import {SortDirection} from '@angular/material/sort';

export class DvSimpleTableConfig {
    // sets cursor style. Should be true, if clickEvent is defined
    private readonly _cursorPointer: boolean;
    // name of column for initial sorting
    private readonly _initialSortColumn: string;
    // direction for initial sorting
    private readonly _initialSortDirection: SortDirection;

    /**
     * @param initialSortColumn: name of column for initial sorting
     * @param initialSortDirection: direction for initial sorting
     * @param cursorPointer: sets cursor style. Should be true, if clickEvent is defined
     */
    public constructor(
        initialSortColumn: string,
        initialSortDirection: SortDirection = 'asc',
        cursorPointer: boolean = true) {
        this._initialSortColumn = initialSortColumn;
        this._initialSortDirection = initialSortDirection;
        this._cursorPointer = cursorPointer;
    }

    public get cursorPointer(): boolean {
        return this._cursorPointer;
    }

    public get initialSortColumn(): string {
        return this._initialSortColumn;
    }

    public get initialSortDirection(): SortDirection {
        return this._initialSortDirection;
    }
}
