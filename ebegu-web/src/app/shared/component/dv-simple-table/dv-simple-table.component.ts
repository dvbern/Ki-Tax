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

    @Output() public readonly rowClicked: EventEmitter<any> = new EventEmitter<any>();

    public datasource: MatTableDataSource<any>;

    public ngOnInit(): void {
        this.datasource = new MatTableDataSource<any>(this.data);
    }

    public ngOnChanges(changes: SimpleChanges): void {
        // tslint:disable-next-line:early-exit
        if (changes.data) {
            if (!this.columns) {
                console.error('data can not be used without specifying the columns. Use the columns input');
                return;
            }
            this.datasource = new MatTableDataSource<any>(this.data);
        }

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
        // copy so we don't manipulate the original input array
        this.datasource.data = [].concat(this.data).sort(((a, b) => sortEvent.direction === 'asc' ?
            this.compare(a[sortEvent.active], b[sortEvent.active]) :
            this.compare(b[sortEvent.active], a[sortEvent.active])));
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
}
