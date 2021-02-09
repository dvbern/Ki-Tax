import {Component, OnInit, ChangeDetectionStrategy} from '@angular/core';
import {StateService, TransitionService} from '@uirouter/core';

@Component({
    selector: 'dv-tagesschulen-ui-view',
    templateUrl: './tagesschulen-ui-view.component.html',
    styleUrls: ['./tagesschulen-ui-view.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TagesschulenUiViewComponent implements OnInit {

    public constructor(
        private readonly $state: StateService,
        private readonly $transition: TransitionService,
    ) {
    }

    public ngOnInit(): void {
        this.$transition.onSuccess({to: 'LASTENAUSGLEICH_TS.ANGABEN_TAGESSCHULEN'}, () => {
            this.$state.go('LASTENAUSGLEICH_TS.ANGABEN_TAGESSCHULEN.LIST');
        });
    }

}
