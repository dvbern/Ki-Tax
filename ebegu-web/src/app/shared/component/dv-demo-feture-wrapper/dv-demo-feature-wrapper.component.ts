import {Component, ChangeDetectionStrategy, Input} from '@angular/core';
import {TSDemoFeature} from '../../../core/directive/dv-hide-feature/TSDemoFeature';

@Component({
    selector: 'dv-dv-demo-feature-wrapper',
    templateUrl: './dv-demo-feature-wrapper.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class DvDemoFeatureWrapperComponent {

    @Input() public demoFeature: TSDemoFeature;

    public constructor() {}
}
