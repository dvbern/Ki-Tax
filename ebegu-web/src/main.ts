/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
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

import {enableProdMode, NgModuleRef, NgZone} from '@angular/core';
import {platformBrowserDynamic} from '@angular/platform-browser-dynamic';
import {UIRouter, UrlService} from '@uirouter/core';
import * as angular from 'angular';
import {APP_JS_MODULE} from './app/app.angularjs.module';
import {AppModule} from './app/app.module';
import {I18nServiceRSRest} from './app/i18n/services/i18nServiceRS.rest';
import {initHooks} from './authentication/state-hooks/init-hooks';
import {environment} from './environments/environment';

(window as any).angular = angular;

if (environment.production) {
    enableProdMode();
}

// Using AngularJS config block, call `deferIntercept()`.
// This tells UI-Router to delay the initial URL sync (until all bootstrapping is complete)
APP_JS_MODULE.config(['$urlServiceProvider', ($urlService: UrlService) => $urlService.deferIntercept()]);

// Manually bootstrap the Angular app
platformBrowserDynamic().bootstrapModule(AppModule).then(platformRef => {
    // Intialize the Angular Module
    // get() the UIRouter instance from DI to initialize the router
    const urlService = platformRef.injector.get(UIRouter).urlService;

    const startRouter = () => {
        // Instruct UIRouter to listen to URL changes
        urlService.listen();
        urlService.sync();
    };

    platformRef.injector.get<NgZone>(NgZone).run(startRouter);
    // Move to ng-authentication.module once fully migrated in KIBON-2962
    initHooks(platformRef);
    injectAngularJSTranslateIntoNgService(platformRef);
})
    .catch(err => console.error('App bootstrap error:', err));

// Workaround to avoid 'AngularJS Injector not set' error
// Could probably be avoided by cleaning up the modules and i18n service, but this is redundant once the
// angular js migrations is advanced enough
// Will be removed in KIBON-2962
function injectAngularJSTranslateIntoNgService(platformRef: NgModuleRef<AppModule>): void {
    platformRef.injector.get(I18nServiceRSRest)
        .setAngularJSTranslateService(platformRef.injector.get('$injector').get('$translate'));
}

// Show ui-router-visualizer
// APP_JS_MODULE.run(['$uiRouter', visualizer]);
