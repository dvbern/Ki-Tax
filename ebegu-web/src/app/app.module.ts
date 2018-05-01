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
import {DummyAuthenticationListViewComponent} from '../authentication/dummyAuthenticaton';
import {applicationPropertyRSProvider, authServiceRSProvider} from '../hybridTools/ajs-upgraded-providers';
import appModule from '../app.module';

@NgModule({
    imports: [
        BrowserModule,
        UpgradeModule,
        UIRouterUpgradeModule,
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

export class AppModule {

    constructor(@Inject(UpgradeModule) private upgrade: UpgradeModule) {
        console.log('Angular called********!!!!');
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
}
