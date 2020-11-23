import {Directive, ElementRef, EventEmitter, Injector, Input, Output} from '@angular/core';
import {UpgradeComponent} from '@angular/upgrade/static';
import {TSBenutzerNoDetails} from '../../../models/TSBenutzerNoDetails';

@Directive({
  selector: '[dvNewUserSelect]'
})
export class NewUserSelectDirective extends UpgradeComponent {

    @Input()
    public showSelectionAll: boolean;

    @Input()
    public angular2: boolean;

    @Input()
    public inputId: string;

    @Input()
    public dvUsersearch: string;

    @Input()
    public initialAll: boolean;

    @Input()
    public selectedUser: TSBenutzerNoDetails;

    @Input()
    public sachbearbeiterGemeinde: boolean;

    @Input()
    public schulamt: boolean;

    @Output()
    public readonly userChanged: EventEmitter<{user: TSBenutzerNoDetails}> = new EventEmitter<{user: TSBenutzerNoDetails}>();

    public constructor(elementRef: ElementRef, injector: Injector) {
      super('dvUserselect', elementRef, injector);
  }

}
