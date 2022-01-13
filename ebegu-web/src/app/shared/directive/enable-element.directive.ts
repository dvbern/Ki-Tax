import {
    Directive,
    ElementRef,
    Input,
    OnChanges,
    Optional,
    Self,
} from '@angular/core';
import {NgControl} from '@angular/forms';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {TSRole} from '../../../models/enums/TSRole';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';

@Directive({
    selector: '[dvEnableElement]',
})
export class EnableElementDirective implements OnChanges {

    @Input() public allowedRoles: ReadonlyArray<TSRole> = TSRoleUtil.getAllRoles();
    @Input() public enableExpression: boolean = true;

    public constructor(
        private readonly el: ElementRef,
        private readonly authService: AuthServiceRS,
        @Optional() @Self() public ngControl: NgControl,
    ) {
    }

    private setElementDisabled(): void {
        this.ngControl?.valueAccessor.setDisabledState(
            !this.authService.isOneOfRoles(this.allowedRoles) || !this.enableExpression);
    }

    public ngOnChanges(): void {
        this.setElementDisabled();
    }

}
