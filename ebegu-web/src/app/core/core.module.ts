import {ModuleWithProviders, NgModule, Optional, SkipSelf} from '@angular/core';
import {applicationPropertyRSProvider, mitteilungRSProvider} from '../../hybridTools/ajs-upgraded-providers';

@NgModule({
    imports: [
        // only those modules required by the providers/components of the core module (other global modules go to shared module)
    ],
    providers: [
        // Insert global singleton services here that have no configuration (ExceptionService, LoggerService etc.)
        applicationPropertyRSProvider,
        mitteilungRSProvider,
    ],
    declarations: [
        // Insert app wide single use components (NavComponent, SpinnerComponent)
    ]
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
            ]
        };
    }
}
