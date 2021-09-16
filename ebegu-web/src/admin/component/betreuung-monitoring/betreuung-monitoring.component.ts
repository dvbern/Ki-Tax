import {AfterViewInit, ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit, ViewChild} from '@angular/core';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {TSBetreuungMonitoring} from '../../../models/TSBetreuungMonitoring';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {BetreuungMonitoringRS} from '../../service/betreuungMonitoringRS.rest';

@Component({
    selector: 'dv-betreuung-monitoring',
    templateUrl: './betreuung-monitoring.component.html',
    styleUrls: ['./betreuung-monitoring.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BetreuungMonitoringComponent implements OnInit, AfterViewInit {

    public displayedColumns: string[] = ['refNummer', 'benutzer', 'infoText', 'timestamp'];

    public dataSource: MatTableDataSource<TSBetreuungMonitoring>;
    public refNummerTooShort: boolean = false;
    public refNumerValue: string;
    public benutzerValue: string;

    @ViewChild(MatSort, {static: true}) public sort: MatSort;
    @ViewChild(MatPaginator, {static: true}) public paginator: MatPaginator;

    private readonly MIN_REF_NUMMER_SIZE = 17;
    private keyupTimeout: NodeJS.Timeout;
    private readonly timeoutMS = 700;

    public constructor(
        private readonly betreuungMonitoringRS: BetreuungMonitoringRS,
        private readonly changeDetectorRef: ChangeDetectorRef,
    ) {
    }

    public ngOnInit(): void {
        this.passFilterToServer();
        this.sortTable();
    }

    public ngAfterViewInit(): void {
        this.dataSource.sort = this.sort;
    }

    private assignResultToDataSource(result: TSBetreuungMonitoring[]): void {
        this.dataSource.data = result;
        this.dataSource.paginator = this.paginator;
    }

    public showNoContentMessage(): boolean {
        return !this.dataSource || this.dataSource.data.length === 0;
    }

    public doFilterRefnummer(value: string): void {
        if (value.length < this.MIN_REF_NUMMER_SIZE) {
            this.refNumerValue = null;
            return;
        }
        this.refNumerValue = value;
        this.applyFilter();
    }

    public doFilterBenutzende(value: string): void {
        this.benutzerValue = value;
        this.applyFilter();
    }

    private applyFilter(): void {
        clearTimeout(this.keyupTimeout);
        this.keyupTimeout = setTimeout(() => {
            this.passFilterToServer();
        }, this.timeoutMS);
    }

    private passFilterToServer(): void {
        this.dataSource = new MatTableDataSource<TSBetreuungMonitoring>([]);
        this.betreuungMonitoringRS.getBetreuungMonitoringBeiRefNummer(this.refNumerValue, this.benutzerValue)
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

    public validateRefNummber(refNummer: string): void {
        this.refNummerTooShort = refNummer.length < this.MIN_REF_NUMMER_SIZE && refNummer.length !== 0;
    }
}
