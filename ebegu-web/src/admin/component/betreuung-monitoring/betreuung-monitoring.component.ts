import {Component, OnInit, ChangeDetectionStrategy} from '@angular/core';
import {Observable} from 'rxjs';
import {BetreuungMonitoringRS} from '../../../app/core/service/betreuungMonitoringRS.rest';
import {TSBetreuungMonitoring} from '../../../models/TSBetreuungMonitoring';

@Component({
    selector: 'dv-betreuung-monitoring',
    templateUrl: './betreuung-monitoring.component.html',
    styleUrls: ['./betreuung-monitoring.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BetreuungMonitoringComponent implements OnInit {

    public betreuungMonitoringList$: Observable<TSBetreuungMonitoring[]>;

    public constructor(private readonly betreuungMonitoringRS: BetreuungMonitoringRS) {
    }

    public ngOnInit(): void {
        this.initData();
    }

    private initData(): void {
        this.betreuungMonitoringList$ = this.betreuungMonitoringRS.getBetreuungMonitoringList();
    }
}
