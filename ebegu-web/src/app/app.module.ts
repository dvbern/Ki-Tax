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

import {LOCALE_ID, NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {UpgradeModule} from '@angular/upgrade/static';
import {UIRouterUpgradeModule} from '@uirouter/angular-hybrid';
import {NgAdminModule} from '../admin/ng-admin.module';
import {appModuleAngularJS} from '../app.angularjs.module';
import {NgAuthenticationModule} from '../authentication/ng-authentication.module';
import {DEFAULT_LOCALE} from '../core/constants/CONSTANTS';
import {NgGesuchModule} from '../gesuch/ng-gesuch.module';
import {NgPosteingangModule} from '../posteingang/ng-posteingang.module';
import {AppRoutingModule} from './app-routing.module';
import {CoreModule} from './core/core.module';
import {SharedModule} from './shared/shared.module';
import {UIRouterModule} from '@uirouter/angular';
import {AppComponent} from './test/app.component';

@NgModule({
    imports: [
        BrowserModule,
        NoopAnimationsModule, // we don't want material animations in the project yet
        UpgradeModule,

 UIRouterUpgradeModule,
    UIRouterModule.forChild({ states: [] }),

        // Core & Shared
        // CoreModule.forRoot(),
        // SharedModule,

        // AppRoutingModule,
        // TODO hefa only import via router
        // NgAdminModule,
        // NgAuthenticationModule,
        // NgGesuchModule,
        // NgPosteingangModule,
    ],
    declarations: [
        // AppComponent
    ],
     // bootstrap: [AppComponent]
})

export class AppModule {

    constructor(private readonly upgrade: UpgradeModule) {
    }

    ngDoBootstrap() {
        this.upgrade.bootstrap(document.body, [appModuleAngularJS.name], {strictDi: true});
    }
}
