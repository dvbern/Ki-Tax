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

require('./vendor');
import {NgZone} from '@angular/core';
import {platformBrowserDynamic} from '@angular/platform-browser-dynamic';
import {UIRouter, UrlService} from '@uirouter/core';
import {AppModule} from './app/app.module';

// angular.element(document).ready(() => {
// platformBrowserDynamic().bootstrapModule(AppModule);

platformBrowserDynamic().bootstrapModule(AppModule)
    .then(platformRef => {
        // Intialize the Angular Module
        // get() the UIRouter instance from DI to initialize the router
        const urlService: UrlService = platformRef.injector.get(UIRouter).urlService;

        // Instruct UIRouter to listen to URL changes
        function startUIRouter() {
            urlService.listen();
            urlService.sync();
        }

        const ngZone = platformRef.injector.get(NgZone);
        ngZone.run(startUIRouter);
    });
// });



