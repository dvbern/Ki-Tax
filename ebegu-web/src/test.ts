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

// This file is required by karma.conf.js and loads recursively all the .spec and framework files

require('zone.js/dist/zone-testing');
// zone-testing muss als 1. importiert werden. Danke require Syntax ändert IntelliJ die Reihenfolge nicht wenn man neu
// formatiert
require('jquery');
require('rxjs');
// AngularJs nach jQuery
require('angular');
// AngularJS Mocks nach AngularJS
require('angular-mocks');

import {getTestBed} from '@angular/core/testing';
import {BrowserDynamicTestingModule, platformBrowserDynamicTesting} from '@angular/platform-browser-dynamic/testing';
import '@uirouter/angular-hybrid';
import 'angular-animate';
import 'angular-aria';
import 'angular-cookies';
import 'angular-hotkeys';
import 'angular-i18n/angular-locale_de-ch';
import 'angular-material';
import 'angular-messages';
import 'angular-moment';
import 'angular-sanitize';
import 'angular-smart-table';
import 'angular-translate';
import 'angular-translate-loader-static-files';
import 'angular-ui-bootstrap';
import 'angular-unsavedchanges';
import 'ng-file-upload';



// First, initialize the Angular testing environment.
getTestBed().initTestEnvironment(
    BrowserDynamicTestingModule,
    platformBrowserDynamicTesting()
);

// Then we find all the tests.
const context = require.context('./', true, /spec\.ts$/);
// And load the modules.
context.keys().map(context);
