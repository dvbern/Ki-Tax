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
import {
    Component,
    OnInit,
    ChangeDetectionStrategy,
    Output,
    EventEmitter,
    ViewChild,
    Input,
    SimpleChanges, OnChanges, ChangeDetectorRef,
} from '@angular/core';
import {MatSort} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {Observable} from 'rxjs';
import {DVAntragListItem} from '../../shared/interfaces/DVAntragListItem';
import {DVEntitaetListItem} from '../../shared/interfaces/DVEntitaetListItem';

@Component({
    selector: 'dv-search-list',
    templateUrl: './dv-search-list.component.html',
    styleUrls: ['./dv-search-list.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DvSearchListComponent implements OnInit, OnChanges {

    /**
     * Emits when the user clicks on a row
     */
    @Output() public readonly openEvent: EventEmitter<string> = new EventEmitter<any>();

    @Input() public hiddenColumns: string[] = [];

    @Input() public data$: Observable<DVAntragListItem[]>;

    @Input() public statusPrefix: string;

    public displayedColumns: string[] = ['name', 'status', 'detail'];
    private readonly allColumns = ['name', 'status', 'detail'];
    public dataSource: MatTableDataSource<DVEntitaetListItem>;

    @ViewChild(MatSort, {static: true}) public sort: MatSort;

    public constructor(private readonly changeDetectorRef: ChangeDetectorRef) {
    }

    public ngOnInit(): void {
        this.initTable();
        this.sortTable();
    }

    public ngOnChanges(changes: SimpleChanges): void {
        if (changes.hiddenColumns) {
            this.displayedColumns = this.allColumns.filter(column => !this.hiddenColumns.includes(column));
        }
    }

    public showNoContentMessage(): boolean {
        return !this.dataSource || this.dataSource.data.length === 0;
    }

    public doFilter(value: string): void {
        this.dataSource.filter = value.trim().toLocaleLowerCase();
    }

    public open(id: string): void {
        this.openEvent.emit(id);
    }

    private initTable(): void {
        this.dataSource = new MatTableDataSource<DVAntragListItem>([]);
        this.loadData();
    }

    private loadData(): void {
        this.data$.subscribe((result: DVAntragListItem[]) => {
            this.dataSource.data = result;
            this.changeDetectorRef.markForCheck();
        });
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
