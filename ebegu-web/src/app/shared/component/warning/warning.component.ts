import {Component, ChangeDetectionStrategy, Input} from '@angular/core';

@Component({
    selector: 'dv-warning',
    templateUrl: './warning.component.html',
    styleUrls: ['./warning.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: true
})
export class WarningComponent {

    @Input() public text: string;

}
