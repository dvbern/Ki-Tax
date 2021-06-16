import {ChangeDetectionStrategy, Component, EventEmitter, Input, OnInit, Output, SimpleChanges} from '@angular/core';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {MatSort, Sort, SortDirection} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {isMoment} from 'moment';
import {DvNgRemoveDialogComponent} from '../../../../app/core/component/dv-ng-remove-dialog/dv-ng-remove-dialog.component';
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
    public readonly rowClicked: EventEmitter<TSInternePendenz> = new EventEmitter<TSInternePendenz>();
    @Output()
    public readonly deletePendenz: EventEmitter<TSInternePendenz> = new EventEmitter<TSInternePendenz>();

    public datasource: MatTableDataSource<TSInternePendenz>;
    public readonly initialSortColumn = 'termin';
    public readonly initialSortDirection: SortDirection = 'asc';
    public readonly shownColumns = ['termin', 'text', 'erledigt', 'delete'];

    private currentSort: Sort = new MatSort();

    public constructor(
        private readonly dialog: MatDialog
    ) {
        this.datasource = new MatTableDataSource<TSInternePendenz>(this.internePendenzen);
    }

    public ngOnInit(): void {
        this.currentSort.direction = this.initialSortDirection;
        this.currentSort.active = this.initialSortColumn;
    }

    public ngOnChanges(changes: SimpleChanges): void {
        if (changes.internePendenzen) {
            this.updateTableData(changes.internePendenzen.currentValue);
        }
    }

    private updateTableData(internePendenzen: TSInternePendenz[]): void {
        this.datasource.data = internePendenzen;
        this.sortData(this.currentSort);
    }

    public onRowClicked(element: TSInternePendenz): void {
        this.rowClicked.emit(element);
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

        // cache sort to use it later again, when data updates
        this.currentSort = sortEvent;
    }

    private compare(a: any, b: any): number {
        if (typeof a === 'string' && typeof b === 'string') {
            return a.localeCompare(b);
        }
        if (typeof a === 'number' && typeof b === 'number') {
            return a - b;
        }
        if (isMoment(a) && isMoment(b)) {
            return a.toDate().getTime() - b.toDate().getTime();
        }
        throw new Error('Compare type not defined');
    }

    public async delete(pendenz: TSInternePendenz, $event: MouseEvent): Promise<void> {
        $event.stopPropagation();
        const confirmation = await this.confirmDelete();
        if (confirmation) {
            this.deletePendenz.emit(pendenz);
        }
    }

    public confirmDelete(): Promise<boolean> {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            title: 'PENDENZ_WIRKLICH_LOESCHEN',
        };
        return this.dialog.open(DvNgRemoveDialogComponent, dialogConfig)
            .afterClosed().toPromise();
    }
}
