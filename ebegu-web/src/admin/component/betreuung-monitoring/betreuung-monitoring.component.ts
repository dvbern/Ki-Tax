import {Component, OnInit, ChangeDetectionStrategy, ViewChild, ChangeDetectorRef, AfterViewInit} from '@angular/core';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {BetreuungMonitoringRS} from '../../service/betreuungMonitoringRS.rest';
import {TSBetreuungMonitoring} from '../../../models/TSBetreuungMonitoring';

@Component({
    selector: 'dv-betreuung-monitoring',
    templateUrl: './betreuung-monitoring.component.html',
    styleUrls: ['./betreuung-monitoring.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BetreuungMonitoringComponent implements OnInit, AfterViewInit {

    public displayedColumns: string[] = ['refNummer', 'benutzer', 'infoText', 'timestamp'];

    public dataSource: MatTableDataSource<TSBetreuungMonitoring>;

    @ViewChild(MatSort, {static: true}) public sort: MatSort;
    @ViewChild(MatPaginator, {static: true}) public paginator: MatPaginator;

    private readonly MIN_REF_NUMMER_SIZE = 17;

    public constructor(
        private readonly betreuungMonitoringRS: BetreuungMonitoringRS,
        private readonly changeDetectorRef: ChangeDetectorRef,
    ) {
    }

    public ngOnInit(): void {
        this.initData();
        this.sortTable();
    }

    public ngAfterViewInit(): void {
        this.dataSource.sort = this.sort;
    }

    private initData(): void {
        this.dataSource = new MatTableDataSource<TSBetreuungMonitoring>([]);
        this.betreuungMonitoringRS.getBetreuungMonitoringList().subscribe((result: TSBetreuungMonitoring[]) => {
                this.assignResultToDataSource(result);
                this.changeDetectorRef.markForCheck();
            },
            () => {
            });
    }

    private assignResultToDataSource(result: TSBetreuungMonitoring[]): void {
        this.dataSource.data = result;
        this.dataSource.paginator = this.paginator;
    }

    public showNoContentMessage(): boolean {
        return !this.dataSource || this.dataSource.data.length === 0;
    }

    public doFilter(value: string): void {
        if (EbeguUtil.isEmptyStringNullOrUndefined(value)) {
            this.initData();
            return;
        }
        if (value.length < this.MIN_REF_NUMMER_SIZE) {
            return;
        }
        this.dataSource = new MatTableDataSource<TSBetreuungMonitoring>([]);
        this.betreuungMonitoringRS.getBetreuungMonitoringBeiRefNummer(value)
            .subscribe((result: TSBetreuungMonitoring[]) => {
                    this.assignResultToDataSource(result);
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
                id: 'timestamp',
                start: 'desc',
                disableClear: false,
            },
        );
    }
}
