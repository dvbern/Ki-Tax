import {Component, OnInit, ChangeDetectionStrategy} from '@angular/core';
import {StateService} from '@uirouter/core';
import {TSSozialdienst} from '../../../models/sozialdienst/TSSozialdienst';

@Component({
    selector: 'dv-add-sozialdienst',
    templateUrl: './add-sozialdienst.component.html',
    styleUrls: ['./add-sozialdienst.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AddSozialdienstComponent implements OnInit {

    public sozialdienst: TSSozialdienst = undefined;
    public adminEmail: string = undefined;

    public constructor(private readonly $state: StateService) {
    }

    public ngOnInit(): void {
    }

    public socialdienstEinladen(): void {

    }

    public cancel(): void {
        this.$state.go('sozialdienst.list');
    }
}
