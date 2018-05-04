/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import {Inject, NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';
import {UpgradeModule} from '@angular/upgrade/static';
import {UIRouterUpgradeModule} from '@uirouter/angular-hybrid';
import {HttpClient} from '@angular/common/http';
import { HttpClientModule } from '@angular/common/http';
import {DummyAuthenticationListViewComponent} from '../authentication/dummyAuthenticaton';
import {applicationPropertyRSProvider, authServiceRSProvider} from '../hybridTools/ajs-upgraded-providers';
import appModule from '../app.module';
import {TranslateModule, TranslateLoader, TranslateService} from '@ngx-translate/core';
import {TranslateHttpLoader} from '@ngx-translate/http-loader';


export function createTranslateLoader(http: HttpClient) {
    return new TranslateHttpLoader(http, '../assets/translations/translations_', '.json');
}

@NgModule({
    imports: [
        BrowserModule,
        UpgradeModule,
        UIRouterUpgradeModule,
        HttpClientModule,
        TranslateModule.forRoot({
            loader: {
                provide: TranslateLoader,
                useFactory: (createTranslateLoader),
                deps: [HttpClient]
            }
        }),
    ],
    declarations: [
        DummyAuthenticationListViewComponent,
    ],
    entryComponents: [
        DummyAuthenticationListViewComponent,
    ],
    providers: [
        authServiceRSProvider,
        applicationPropertyRSProvider,
    ],
})

export class NgAppModule {

    constructor(@Inject(UpgradeModule) private upgrade: UpgradeModule, translate: TranslateService) {
        this.initTranslateService(translate);
    }

    ngDoBootstrap() {
        // it should be possible to inject UpgradeModule and then in the entrz point bootstrp.ts call
        // platformBrowserDynamic().bootstrapModule(AppModule);
        // so that we launch Angular from AngularJS and Angular launches the AngularJS-Module
        // but this is not working for me. I get an error 'Can't resolve all parameters for AppModule'
        // So I decided to bootstrap the whole thing directly in AngularJS. AngularJS calls this
        // Angular-Module and on Promise response it botstraps the application

        // EDIT -> Both versions are here. the uncommented version seems to be the newest one.
        // I am not sure which one is better but I don't see any problem with the selected one

        this.upgrade.bootstrap(document.body, [appModule.name], { strictDi: true });
    }

    private initTranslateService(translate: TranslateService) {
        // this language will be used as a fallback when a translation isn't found in the current language
        translate.setDefaultLang('de');
        // the lang to use, if the lang isn't available, it will use the current loader to get them
        translate.use('de');
    }
}
