import {Directive, Input, OnChanges, SimpleChanges} from '@angular/core';
import {NgControl} from '@angular/forms';
import {TSRole} from '../../../models/enums/TSRole';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';

@Directive({
    selector: '[dvEnableElement]'
})
export class EnableElementDirective implements OnChanges {
    @Input() public allowedRoles: ReadonlyArray<TSRole> =
        TSRoleUtil.getAllRoles();
    @Input() public enableExpression: boolean = true;

    public constructor(
        private readonly authService: AuthServiceRS,
        public ngControl: NgControl
    ) {}

    public ngOnChanges(changes: SimpleChanges): void {
        if (!this.authService.isOneOfRoles(this.allowedRoles) ||
                !this.enableExpression
        ) {
            if (changes.enableExpression && this.ngControl?.control) {
                const action = this.enableExpression ? 'enable' : 'disable';
                this.ngControl?.control[action]();
            }
        }
    }
}
