import {Component, OnInit, ChangeDetectionStrategy} from '@angular/core';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';

@Component({
    selector: 'dv-init-zpv-nr',
    templateUrl: './init-zpv-nr.component.html',
    styleUrls: ['./init-zpv-nr.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InitZpvNrComponent implements OnInit {

    public constructor(
        private readonly authService: AuthServiceRS
    ) {
    }

    public ngOnInit(): void {
        this.authService.burnPortalTimeout().finally(() => {
            this.authService.initConnectGSZPV();
        });
    }

}
