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
// eslint-disable-next-line
import deCH from '@angular/common/locales/de-CH';
import {
    APP_INITIALIZER,
    ErrorHandler,
    LOCALE_ID,
    ModuleWithProviders,
    NgModule,
    Optional,
    SkipSelf
} from '@angular/core';
import {MAT_DATE_LOCALE} from '@angular/material/core';
import {MatPaginatorIntl} from '@angular/material/paginator';
import {TranslateModule, TranslatePipe, TranslateService} from '@ngx-translate/core';
import {UIRouterUpgradeModule} from '@uirouter/angular-hybrid';
import {CookieService} from 'ngx-cookie-service';
import {FjkvKinderabzugExchangeService} from '../../gesuch/component/kindView/fkjv-kinderabzug/fjkv-kinderabzug-exchange.service';
import {SearchRS} from '../../gesuch/service/searchRS.rest';
import {PaginatorI18n} from '../i18n/PaginatorI18n';
import {ColorService} from '../shared/services/color.service';
import {MandantService} from '../shared/services/mandant.service';
import {DEFAULT_LOCALE} from './constants/CONSTANTS';
import {HTTP_INTERCEPTOR_PROVIDERS} from './http-interceptors/interceptors';
import {UPGRADED_HTTP_INTERCEPTOR_PROVIDERS} from './httpInterceptorProviders';
import {configureRaven, RavenErrorHandler} from './sentry/sentryConfigurator';
import {BroadcastService} from './service/broadcast.service';
import {VersionService} from './service/version/version.service';
import {WindowRef} from './service/windowRef.service';
import {UPGRADED_PROVIDERS} from './upgraded-providers';
// sentry
configureRaven();

registerLocaleData(deCH);

export function paginatorI18nFactory(translateService: TranslateService): PaginatorI18n {
    return new PaginatorI18n(translateService);
}

export function initMandantCookie(mandantService: MandantService): () => Promise<any> {
    return () => mandantService.initMandantCookies();
}

@NgModule({
    imports: [
        // only those modules required by the providers/components of the core module
        // (other global modules go to shared module)
        TranslateModule,
        UIRouterUpgradeModule
    ],
    providers: [
        // Insert global singleton services here that have no configuration (ExceptionService, LoggerService etc.)
        {
            provide: APP_INITIALIZER,
            useFactory: initMandantCookie,
            deps: [MandantService],
            multi: true
        },
        ...UPGRADED_PROVIDERS,
        ...UPGRADED_HTTP_INTERCEPTOR_PROVIDERS,
        HTTP_INTERCEPTOR_PROVIDERS,
        TranslatePipe,
        WindowRef,
        VersionService,
        BroadcastService,
        MandantService,
        CookieService,
        ColorService,
        SearchRS,
        FjkvKinderabzugExchangeService
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
                {provide: MAT_DATE_LOCALE, useValue: DEFAULT_LOCALE},
                {provide: ErrorHandler, useClass: RavenErrorHandler},
                // {provide: MAT_DIALOG_DEFAULT_OPTIONS, useValue: {disableClose: false, autoFocus: true}},
                {
                    provide: MatPaginatorIntl,
                    deps: [TranslateService],
                    useFactory: paginatorI18nFactory
                }
            ]
        };
    }
}
