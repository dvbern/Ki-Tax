import {Component, ChangeDetectionStrategy, Input} from '@angular/core';
import {TranslateModule} from '@ngx-translate/core';

@Component({
    selector: 'dv-warning',
    templateUrl: './warning.component.html',
    styleUrls: ['./warning.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [TranslateModule],
    standalone: true
})
export class WarningComponent {
    @Input() public text: string;
}
