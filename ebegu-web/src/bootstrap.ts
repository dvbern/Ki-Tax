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
import {NgAppModule} from './ngApp/ng-app.module';
import { UrlService } from '@uirouter/core';

angular.element(document).ready(function () {
    // platformBrowserDynamic().bootstrapModule(AppModule);

    platformBrowserDynamic().bootstrapModule(NgAppModule)
        .then(platformRef => {
            // console.log('BOOTSTRAPING AngularJS');
            // const upgrade = platformRef.injector.get(UpgradeModule) as UpgradeModule;
            // upgrade.bootstrap(document.body, [appModule.name], {strictDi: true});

            const url: UrlService = platformRef.injector.get(UrlService);

            // Instruct UIRouter to listen to URL changes
            function startUIRouter() {
                url.listen();
                url.sync();
            }

            const ngZone: NgZone = platformRef.injector.get(NgZone);
            ngZone.run(startUIRouter);
        });
});



