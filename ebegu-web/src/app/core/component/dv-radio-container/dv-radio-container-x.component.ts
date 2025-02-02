import {
    ChangeDetectionStrategy,
    Component,
    ViewEncapsulation
} from '@angular/core';

@Component({
    selector: 'dv-radio-container-x',
    templateUrl: './dv-radio-container-x.component.html',
    styleUrls: ['./dv-radio-container-x.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    encapsulation: ViewEncapsulation.None
})
export class DvRadioContainerXComponent {}
