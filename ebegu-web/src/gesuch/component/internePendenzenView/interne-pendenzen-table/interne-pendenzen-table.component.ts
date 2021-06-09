import {ChangeDetectionStrategy, Component, EventEmitter, Input, OnInit, Output, SimpleChanges} from '@angular/core';
import {Sort, SortDirection} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {isMoment, Moment} from 'moment';
import {TSInternePendenz} from '../../../../models/TSInternePendenz';

@Component({
    selector: 'dv-interne-pendenzen-table',
    templateUrl: './interne-pendenzen-table.component.html',
    styleUrls: ['./interne-pendenzen-table.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class InternePendenzenTableComponent implements OnInit {

    @Input()
    public internePendenzen: TSInternePendenz[] = [];

    @Output()
    public readonly rowClicked: EventEmitter<any> = new EventEmitter<any>();

    public datasource: MatTableDataSource<TSInternePendenz>;
    public readonly initialSortColumn = 'termin';
    public readonly initialSortDirection: SortDirection = 'asc';
    public readonly shownColumns = ['termin', 'text', 'erledigt'];

    public constructor() {
        this.datasource = new MatTableDataSource<TSInternePendenz>(this.internePendenzen);
    }

    public ngOnInit(): void {
    }

    public ngOnChanges(changes: SimpleChanges): void {
        if (changes.internePendenzen) {
            this.datasource.data = changes.internePendenzen.currentValue;
        }
    }

    public onRowClicked(element: any, $event: MouseEvent): void {
        this.rowClicked.emit({element, event: $event});
    }

    public sortData(sortEvent: Sort): void {
        if (sortEvent.direction === '') {
            this.datasource.data = this.internePendenzen;
            return;
        }
        // copy so we don't manipulate the original input array
        this.datasource.data = [].concat(this.internePendenzen).sort(((a, b) => sortEvent.direction === 'asc' ?
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
        if (isMoment(a) && isMoment(b)) {
            const dateA = a as Moment;
            const dateB = b as Moment;
            return dateA.toDate().getTime() - dateB.toDate().getTime();
        }
        throw new Error('Compare type not defined');
    }

}
