import {Directive, ElementRef, EventEmitter, Injector, Input, Output} from '@angular/core';
import {UpgradeComponent} from '@angular/upgrade/static';

@Directive({
    selector: 'dv-loading-button'
})
export class LoadingButtonDirective extends UpgradeComponent {

    @Input() public type: string;
    @Input() public delay: string;
    @Input() public buttonClass: string;
    @Input() public forceWaitService: string;
    @Input() public buttonDisabled: '<';
    @Input() public ariaLabel: string;
    @Input() public inputId: string;

    @Output() public readonly buttonClick: EventEmitter<void>;

    public constructor(elementRef: ElementRef, injector: Injector) {
        super('dvLoadingButton', elementRef, injector);
    }

}
