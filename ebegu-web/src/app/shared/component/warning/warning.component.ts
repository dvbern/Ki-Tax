import {Component, OnInit, ChangeDetectionStrategy, Input} from '@angular/core';

@Component({
    selector: 'dv-warning',
    templateUrl: './warning.component.html',
    styleUrls: ['./warning.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class WarningComponent implements OnInit {
    @Input() public text: string;

    public constructor() {}

    public ngOnInit(): void {}
}
