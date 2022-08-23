/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

import {MatSort} from '@angular/material/sort';
import {BenutzerListFilter} from '../../app/core/component/dv-benutzer-list/BenutzerListFilter';
import {TSPagination} from './TSPagination';

export class TSBenutzerTableFilterDTO {
    private _pagination: TSPagination;
    private _sort: MatSort;
    private _search: BenutzerListFilter;

    public constructor(pagination: TSPagination, sort: MatSort, search: BenutzerListFilter) {
        this._pagination = pagination;
        this._sort = sort;
        this._search = search;
    }

    public get pagination(): TSPagination {
        return this._pagination;
    }

    public set pagination(value: TSPagination) {
        this._pagination = value;
    }

    public get sort(): MatSort {
        return this._sort;
    }

    public set sort(value: MatSort) {
        this._sort = value;
    }

    public get search(): BenutzerListFilter {
        return this._search;
    }

    public set search(value: BenutzerListFilter) {
        this._search = value;
    }
}
