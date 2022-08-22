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
import {TSPagination} from './TSPagination';

export class TSBenutzerTableFilterDTO {
    private _pagination: TSPagination;
    private _sort: MatSort;
    private _search: TSBenutzerTableFilterSearchDTO;

    public constructor(pagination: TSPagination, sort: MatSort, search: TSBenutzerTableFilterSearchDTO) {
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

    public get search(): TSBenutzerTableFilterSearchDTO {
        return this._search;
    }

    public set search(value: TSBenutzerTableFilterSearchDTO) {
        this._search = value;
    }

    public toSmartTableDTO(): any {
        return {
            pagination: this._pagination.toPaginationDTO(),
            search: {
                predicateObject: this._search
            },
            sort: {
                predicate: this._sort.active,
                reverse: this._sort.direction === 'desc'
            }
        };
    }
}

export interface TSBenutzerTableFilterSearchDTO {
    username?: string;
    vorname?: string;
    nachname?: string;
    email?: string;
    role?: string;
    roleGueltigBis?: string;
    gemeinde?: string;
    institution?: string;
    traegerschaft?: string;
    sozialdienst?: string;
    status?: string;
}
