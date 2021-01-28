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

@Component({
    selector: 'dv-simple-table',
    templateUrl: './dv-simple-table.component.html',
    styleUrls: ['./dv-simple-table.component.less'],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DvSimpleTableComponent implements OnInit, OnChanges {

    @Input() public data: any[] = [];
    @Input() public columns: { displayedName: string, attributeName: string }[];

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
            a[sortEvent.active].localeCompare(b[sortEvent.active]) :
            b[sortEvent.active].localeCompare(a[sortEvent.active])));
    }
}
