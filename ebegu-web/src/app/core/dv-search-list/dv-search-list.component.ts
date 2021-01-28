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
import {Component, OnInit, ChangeDetectionStrategy, Output, EventEmitter, ViewChild} from '@angular/core';
import {MatSort} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {DVEntitaetListItem} from '../../shared/interfaces/DVEntitaetListItem';

@Component({
    selector: 'dv-search-list',
    templateUrl: './dv-search-list.component.html',
    styleUrls: ['./dv-search-list.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DvSearchListComponent implements OnInit {


    /**
     * Emits when the user clicks on a row
     */
    @Output() public readonly openEvent: EventEmitter<string> = new EventEmitter<any>();

    public displayedColumns: string[] = ['name', 'status', 'detail'];
    public dataSource: MatTableDataSource<DVEntitaetListItem>;

    @ViewChild(MatSort, { static: true }) public sort: MatSort;

    public constructor() {
    }

    public ngOnInit(): void {
        this.sortTable();
    }

    public showNoContentMessage(): boolean {
        return !this.dataSource || this.dataSource.data.length === 0;
    }

    public doFilter(value: string): void {
        this.dataSource.filter = value.trim().toLocaleLowerCase();
    }

    public open(id: string): void  {
        this.openEvent.emit(id);
    }

    public canEdit(id: string): boolean {
        return true;
    }

    /**
     * It sorts the table by default using the variable sort.
     */
    private sortTable(): void {
        this.sort.sort({
                id: 'name',
                start: 'asc',
                disableClear: false,
            },
        );
    }
}
