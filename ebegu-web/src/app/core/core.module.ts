/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {registerLocaleData} from '@angular/common';
import {ErrorHandler, LOCALE_ID, ModuleWithProviders, NgModule, Optional, SkipSelf} from '@angular/core';
import { MatPaginatorIntl } from '@angular/material/paginator';
import {TranslateModule, TranslatePipe, TranslateService} from '@ngx-translate/core';
import {UIRouterUpgradeModule} from '@uirouter/angular-hybrid';
import {PaginatorI18n} from '../i18n/PaginatorI18n';
import {DEFAULT_LOCALE} from './constants/CONSTANTS';
import {HTTP_INTERCEPTOR_PROVIDERS} from './http-interceptors/interceptors';
import {UPGRADED_HTTP_INTERCEPTOR_PROVIDERS} from './httpInterceptorProviders';
import {BroadcastService} from './service/broadcast.service';
import {VersionService} from './service/version/version.service';
import {WindowRef} from './service/windowRef.service';
import {configureRaven, RavenErrorHandler} from './sentry/sentryConfigurator';
import {UPGRADED_PROVIDERS} from './upgraded-providers';
// tslint:disable-next-line:match-default-export-name
import deCH from '@angular/common/locales/de-CH';
// sentry
configureRaven();

registerLocaleData(deCH);

export function paginatorI18nFactory(translateService: TranslateService): PaginatorI18n {
    return new PaginatorI18n(translateService);
}

@NgModule({
    imports: [
        // only those modules required by the providers/components of the core module
        // (other global modules go to shared module)
        TranslateModule,
        UIRouterUpgradeModule,
    ],
    providers: [
        // Insert global singleton services here that have no configuration (ExceptionService, LoggerService etc.)
        ...UPGRADED_PROVIDERS,
        ...UPGRADED_HTTP_INTERCEPTOR_PROVIDERS,
        HTTP_INTERCEPTOR_PROVIDERS,
        TranslatePipe,
        WindowRef,
        VersionService,
        BroadcastService,
    ],
    declarations: [
        // Insert app wide single use components (NavComponent, SpinnerComponent). Try not to declare anything here.
        // This module should be used only to provide services
    ]
})
export class CoreModule {

    public constructor(@Optional() @SkipSelf() parentModule: CoreModule) {
        if (parentModule) {
            throw new Error('CoreModule has already been loaded. Import Core modules in the AppModule only.');
        }
    }

    /**
     * @see https://angular.io/guide/singleton-services#forroot
     */
    public static forRoot(): ModuleWithProviders<CoreModule> {
        return {
            ngModule: CoreModule,
            providers: [
                // Insert configurable providers here (will be appended to providers defined in metadata above)
                {provide: LOCALE_ID, useValue: DEFAULT_LOCALE},
                {provide: ErrorHandler, useClass: RavenErrorHandler},
                // {provide: MAT_DIALOG_DEFAULT_OPTIONS, useValue: {disableClose: false, autoFocus: true}},
                {
                    provide: MatPaginatorIntl,
                    deps: [TranslateService],
                    useFactory: paginatorI18nFactory,
                },
            ],
        };
    }
}
