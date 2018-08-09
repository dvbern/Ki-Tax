import {LOCALE_ID, ModuleWithProviders, NgModule, Optional, SkipSelf} from '@angular/core';
import {TranslatePipe} from '@ngx-translate/core';
import {DEFAULT_LOCALE} from './constants/CONSTANTS';
import {UPGRADED_PROVIDERS} from './upgraded-providers';

@NgModule({
    imports: [
        // only those modules required by the providers/components of the core module (other global modules go to shared module)
    ],
    providers: [
        // Insert global singleton services here that have no configuration (ExceptionService, LoggerService etc.)
        ...UPGRADED_PROVIDERS,
        TranslatePipe,
    ],
    declarations: [
        // Insert app wide single use components (NavComponent, SpinnerComponent). Try not to declare anything here.
        // This module should be used only to provide services
    ],
})
export class CoreModule {

    constructor(@Optional() @SkipSelf() parentModule: CoreModule) {
        if (parentModule) {
            throw new Error('CoreModule has already been loaded. Import Core modules in the AppModule only.');
        }
    }

    /**
     * @see https://angular.io/guide/singleton-services#forroot
     */
    public static forRoot(): ModuleWithProviders {
        return {
            ngModule: CoreModule,
            providers: [
                // Insert configurable providers here (will be appended to providers defined in metadata above)
                {provide: LOCALE_ID, useValue: DEFAULT_LOCALE}
            ]
        };
    }
}
