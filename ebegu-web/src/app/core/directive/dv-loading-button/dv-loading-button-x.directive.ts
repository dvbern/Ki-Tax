import {Directive, ElementRef, EventEmitter, Injector, Input, Output} from '@angular/core';
import {UpgradeComponent} from '@angular/upgrade/static';

@Directive({
    selector: '[dvLoadingButtonX]'
})
export class DvLoadingButtonXDirective extends UpgradeComponent {

    @Input() public type: any;
    @Input() public delay: any;
    @Input() public buttonClass: string;
    @Input() public forceWaitService: any;
    @Input() public ariaLabel: any;
    @Input() public buttonDisabled: any;
    @Output() public readonly buttonClick: EventEmitter<any> = new EventEmitter<any>();

    constructor(elementRef: ElementRef, injector: Injector) {
        super('dvLoadingButton', elementRef, injector);
    }




}
