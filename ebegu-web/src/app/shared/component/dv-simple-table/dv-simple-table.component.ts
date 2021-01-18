import {
    Component,
    OnInit,
    ChangeDetectionStrategy,
    Input,
    SimpleChanges,
    OnChanges,
    Output,
    EventEmitter,
} from '@angular/core';
import {MatTableDataSource} from '@angular/material/table';

@Component({
    selector: 'dv-simple-table',
    templateUrl: './dv-simple-table.component.html',
    styleUrls: ['./dv-simple-table.component.less'],
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
        if (changes.data && !this.columns) {
            console.error('data can not be used without specifying the columns. Use the columns input');
        }

        if (changes.data && !changes.data.isFirstChange()) {
            this.datasource = new MatTableDataSource<any>(changes.data.currentValue);
        }
    }

    public onRowClicked(element: any, $event: MouseEvent): void {
        this.rowClicked.emit({element, event: $event});
    }

    public getColumnsAttributeName(): string[] {
        return this.columns.map(column => column.attributeName);
    }
}
