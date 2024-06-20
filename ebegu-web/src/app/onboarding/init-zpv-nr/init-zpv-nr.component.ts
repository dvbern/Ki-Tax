import {Component, OnInit, ChangeDetectionStrategy} from '@angular/core';
import {StateService, UIRouterGlobals} from '@uirouter/core';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {WindowRef} from '../../core/service/windowRef.service';

@Component({
    selector: 'dv-init-zpv-nr',
    templateUrl: './init-zpv-nr.component.html',
    styleUrls: ['./init-zpv-nr.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class InitZpvNrComponent implements OnInit {
    private redirectionHref: string;
    private redirecting: boolean;

    public constructor(
        private readonly authService: AuthServiceRS,
        private readonly $state: StateService,
        private readonly windowRef: WindowRef,
        private readonly uiRouterGlobals: UIRouterGlobals
    ) {}

    public ngOnInit(): void {
        this.authService.burnPortalTimeout().finally(() => {
            const target = this.$state.target('onboarding.zpvgssuccess');
            this.authService
                .initConnectGSZPV(
                    this.$state.href(
                        target.$state(),
                        this.uiRouterGlobals.params,
                        {absolute: true}
                    )
                )
                .then(url => {
                    this.redirectionHref = url;

                    this.redirecting = true;
                    setTimeout(() => this.redirect(this.redirectionHref), 1000);
                });
        });
    }

    private redirect(urlToGoTo: string): void {
        console.log('redirecting to login', urlToGoTo);

        this.windowRef.nativeWindow.open(urlToGoTo, '_self');
    }
}
