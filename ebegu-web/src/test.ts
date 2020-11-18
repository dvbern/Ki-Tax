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

// tslint:disable-next-line:ordered-imports no-import-side-effect
import 'zone.js/dist/zone-testing';
// AngularJs nach jQuery
// formatiert
require('jquery');
// zone-testing muss als 1. importiert werden. Danke require Syntax ändert IntelliJ die Reihenfolge nicht wenn man neu
require('angular');
require('rxjs');
// AngularJS Mocks nach AngularJS
require('angular-mocks');

import {getTestBed} from '@angular/core/testing';
import {BrowserDynamicTestingModule, platformBrowserDynamicTesting} from '@angular/platform-browser-dynamic/testing';
// tslint:disable:no-import-side-effect
require('@uirouter/angular-hybrid');
require('angular-animate');
require('angular-aria');
require('angular-cookies');
require('angular-hotkeys');
require('angular-i18n/angular-locale_de-ch');
require('angular-material');
require('angular-messages');
require('angular-moment');
require('angular-sanitize');
require('angular-smart-table');
require('angular-translate');
require('angular-translate-loader-static-files');
require('angular-ui-bootstrap');
require('angular-unsavedchanges');
require('ng-file-upload');
// tslint:enable:no-import-side-effect

// First, initialize the Angular testing environment.
getTestBed().initTestEnvironment(
    BrowserDynamicTestingModule,
    platformBrowserDynamicTesting(),
);

// Then we find all the tests.
const context = require.context('./', true, /spec\.ts$/);
// And load the modules.
context.keys().forEach(context);
