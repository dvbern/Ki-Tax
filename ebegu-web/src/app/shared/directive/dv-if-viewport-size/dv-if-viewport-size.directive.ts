import {BreakpointObserver, Breakpoints, BreakpointState} from '@angular/cdk/layout';
import {ChangeDetectorRef, Directive, Input, OnDestroy, TemplateRef, ViewContainerRef} from '@angular/core';
import {Subscription} from 'rxjs';

type Size = 'screen' | 'mobile';

const config = {
    mobile: [Breakpoints.XSmall],
    screen: [Breakpoints.Small, Breakpoints.Medium, Breakpoints.Large, Breakpoints.XLarge]
};

@Directive({
    selector: '[dvIfViewportSize]'
})
export class DvIfViewportSizeDirective implements OnDestroy {

    private subscription = new Subscription();

    public constructor(
        private readonly observer: BreakpointObserver,
        private readonly vcRef: ViewContainerRef,
        private readonly templateRef: TemplateRef<any>,
        private readonly cd: ChangeDetectorRef
    ) {
    }

    @Input()
    public set dvIfViewportSize(value: Size) {
        this.subscription.unsubscribe();
        this.subscription = this.observer
            .observe(config[value])
            .subscribe(this.updateView);
    }

    public ngOnDestroy() {
        this.subscription.unsubscribe();
    }

    private readonly updateView = ({matches}: BreakpointState) => {
        if (matches && !this.vcRef.length) {
            this.vcRef.createEmbeddedView(this.templateRef);
        } else if (!matches && this.vcRef.length) {
            this.vcRef.clear();
        }
        this.cd.markForCheck();
    };

}
