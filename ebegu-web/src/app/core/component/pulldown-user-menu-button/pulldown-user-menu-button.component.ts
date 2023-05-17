import {Component, OnInit, ChangeDetectionStrategy, Input, Output, EventEmitter, OnChanges} from '@angular/core';
import {StateService} from '@uirouter/core';
import {TSRole} from '../../../../models/enums/TSRole';
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';

@Component({
    selector: 'dv-pulldown-user-menu-button',
    templateUrl: './pulldown-user-menu-button.component.html',
    styleUrls: ['./pulldown-user-menu-button.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class PulldownUserMenuButtonComponent implements OnChanges {

    @Input()
    public allowedRoles: ReadonlyArray<TSRole>;

    @Input()
    public label: string;

    @Input()
    public uiSRef: string;

    @Output()
    public readonly buttonClick = new EventEmitter<MouseEvent>();

    public allRoles = TSRoleUtil.getAllRoles();
    public href: string;

    public constructor(
        private readonly stateService: StateService
    ) {
    }

    public ngOnChanges(): void {
        this.href = this.stateService.href(this.uiSRef);
    }

}
