import {
    ChangeDetectionStrategy,
    Component,
    EventEmitter,
    Input,
    OnChanges,
    OnInit,
    Output,
    SimpleChanges,
    ViewEncapsulation,
} from '@angular/core';
import {Sort} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {DvSimpleTableColumnDefinition} from './dv-simple-table-column-definition';
import {DvSimpleTableConfig} from './dv-simple-table-config';

@Component({
    selector: 'dv-simple-table',
    templateUrl: './dv-simple-table.component.html',
    styleUrls: ['./dv-simple-table.component.less'],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DvSimpleTableComponent implements OnInit, OnChanges {

    @Input() public data: any[] = [];
    @Input() public columns: DvSimpleTableColumnDefinition[];
    @Input() public config: DvSimpleTableConfig;

    @Output() public readonly rowClicked: EventEmitter<any> = new EventEmitter<any>();

    public datasource: MatTableDataSource<any>;
    private sortedData: any[];
    public page: number = 0;
    public paginationItems: number[];

    public ngOnInit(): void {
        this.initData();
    }

    public ngOnChanges(changes: SimpleChanges): void {
        // tslint:disable-next-line:early-exit
        if (changes.data) {
            if (!this.columns) {
                console.error('data can not be used without specifying the columns. Use the columns input');
                return;
            }
            this.initData();
        }

    }

    private initData(): void {
        this.datasource = new MatTableDataSource<any>(this.data);
        this.sortedData = [].concat(this.data);
        this.applyPaginator(this.page);
    }

    public onRowClicked(element: any, $event: MouseEvent): void {
        this.rowClicked.emit({element, event: $event});
    }

    public getColumnsAttributeName(): string[] {
        return this.columns?.map(column => column.attributeName);
    }

    public sortData(sortEvent: Sort): void {
        if (sortEvent.direction === '') {
            this.datasource.data = this.data;
            return;
        }
        // save sorted Data in separate variable. we use that for pagination
        this.sortedData = [].concat(this.data).sort(((a, b) => sortEvent.direction === 'asc' ?
            this.compare(a[sortEvent.active], b[sortEvent.active]) :
            this.compare(b[sortEvent.active], a[sortEvent.active])));

        // go to page 0 after sorting
        this.applyPaginator(0);
    }

    private compare(a: any, b: any): number {
        if (typeof a === 'string' && typeof b === 'string') {
            return a.localeCompare(b);
        }
        if (typeof a === 'number' && typeof b === 'number') {
            return a - b;
        }
        throw new Error('Compare type not defined');
    }

    public getDisplayValue(element: any, column: any): string {
        if (column.displayFunction !== undefined) {
            return column.displayFunction(element[column.attributeName]);
        }
        return element[column.attributeName];
    }

    /**
     * Navigates to given page if paginator is set. otherwise, sortedData is just bound to datasource
     */
    public applyPaginator(pageIndex: number): void {
        if (!this.config || !this.config.paginate) {
            this.datasource.data = [].concat(this.sortedData);
            return;
        }
        this.page = pageIndex;
        const start = this.page * this.config.paginate;
        this.datasource.data = this.sortedData.slice(start, start + this.config.paginate);
        this.updatePagination();
    }

    private updatePagination(): void {
        this.paginationItems = [];
        for (let i = Math.max(1, this.page - 4); i <= Math.min(Math.ceil(this.data.length / this.config.paginate),
            this.page + 5); i++) {
            this.paginationItems.push(i);
        }
    }
}
