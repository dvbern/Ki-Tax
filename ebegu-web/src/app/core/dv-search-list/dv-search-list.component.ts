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
    AfterViewInit,
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    EventEmitter,
    Input,
    OnChanges,
    OnInit,
    Output,
    SimpleChanges,
    ViewChild,
} from '@angular/core';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {Observable} from 'rxjs';
import {DVAntragListItem} from '../../shared/interfaces/DVAntragListItem';
import {DVEntitaetListItem} from '../../shared/interfaces/DVEntitaetListItem';

@Component({
    selector: 'dv-search-list',
    templateUrl: './dv-search-list.component.html',
    styleUrls: ['./dv-search-list.component.less'],
    changeDetection: ChangeDetectionStrategy.Default,
})
export class DvSearchListComponent implements OnInit, OnChanges, AfterViewInit {

    /**
     * Emits when the user clicks on a row
     */
    @Output() public readonly openEvent: EventEmitter<string> = new EventEmitter<any>();

    @Output() public readonly removeEvent: EventEmitter<string> = new EventEmitter<any>();

    @Input() public hiddenColumns: string[] = [];

    @Input() public data$: Observable<DVAntragListItem[]>;

    @Input() public statusPrefix: string;

    @Input() public columnName: string;

    public displayedColumns: string[] = ['name', 'institutionCount', 'status', 'type', 'detail', 'remove'];
    private readonly allColumns = ['name', 'institutionCount', 'status', 'type', 'detail', 'remove'];
    public dataSource: MatTableDataSource<DVEntitaetListItem>;

    @ViewChild(MatSort, {static: true}) public sort: MatSort;
    @ViewChild(MatPaginator, {static: true}) public paginator: MatPaginator;

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
        // tslint:disable-next-line:early-exit
        if (changes.data$ && !changes.data$.firstChange) {
            this.loadData();
        }
    }

    public ngAfterViewInit(): void {
        this.dataSource.sort = this.sort;
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

    public remove(id: string): void {
        this.removeEvent.emit(id);
    }

    private initTable(): void {
        this.dataSource = new MatTableDataSource<DVAntragListItem>([]);
        this.loadData();
    }

    private loadData(): void {
        this.data$.subscribe((result: DVAntragListItem[]) => {
                this.dataSource.data = result;
                this.dataSource.paginator = this.paginator;
                this.changeDetectorRef.markForCheck();
            },
            () => {
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
