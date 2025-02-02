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

import * as angular from 'angular';
import {environment} from '../../environments/environment';
import {TSBrowserLanguage} from '../../models/enums/TSBrowserLanguage';
import {extractPreferredLanguage} from '../i18n/services/i18nServiceRS.rest';
import {getWindowObject} from './service/windowRef.service';
import IInjectorService = angular.auto.IInjectorService;
import IHttpProvider = angular.IHttpProvider;
import ILocationProvider = angular.ILocationProvider;
import ILogProvider = angular.ILogProvider;
import IQProvider = angular.IQProvider;
import IThemingProvider = angular.material.IThemingProvider;
import ITranslateProvider = angular.translate.ITranslateProvider;

configure.$inject = [
    '$translateProvider',
    '$injector',
    '$httpProvider',
    '$mdThemingProvider',
    'hotkeysProvider',
    '$logProvider',
    '$locationProvider',
    '$qProvider'
];

export function configure(
    $translateProvider: ITranslateProvider,
    $injector: IInjectorService,
    $httpProvider: IHttpProvider,
    $mdThemingProvider: IThemingProvider,
    hotkeysProvider: any,
    $logProvider: ILogProvider,
    $locationProvider: ILocationProvider,
    $qProvider: IQProvider
): void {
    // In case you have issues with double-escaped parameters, check out this issue:
    // https://github.com/angular-translate/angular-translate/issues/1101
    $translateProvider.useSanitizeValueStrategy('escapeParameters');

    const preferredLanguage = extractPreferredLanguage(getWindowObject());

    $translateProvider
        .fallbackLanguage(TSBrowserLanguage.DE)
        .preferredLanguage(preferredLanguage);

    $translateProvider.useLoader('CustomTranslationLoader');

    // Dirty Check configuration (nur wenn plugin vorhanden)
    if ($injector.has('unsavedWarningsConfigProvider')) {
        const unsavedWarningsConfigProvider = $injector.get<any>(
            'unsavedWarningsConfigProvider'
        );
        unsavedWarningsConfigProvider.useTranslateService = true;
        unsavedWarningsConfigProvider.logEnabled = false;
        unsavedWarningsConfigProvider.navigateMessage = 'UNSAVED_WARNING';
        unsavedWarningsConfigProvider.reloadMessage = 'UNSAVED_WARNING_RELOAD';
    }
    // Config Angular Module Theme
    $mdThemingProvider
        .theme('default')
        // .primaryPalette('red')
        .accentPalette('red');

    // Config hotkey provider: https://github.com/chieffancypants/angular-hotkeys#angular-hotkeys-
    hotkeysProvider.useNgRoute = false;
    hotkeysProvider.includeCheatSheet = false;

    // Configuration of $http service
    $httpProvider.interceptors.push('HttpErrorInterceptor');
    $httpProvider.interceptors.push('HttpAuthInterceptor');
    $httpProvider.interceptors.push('HttpResponseInterceptor');
    $httpProvider.interceptors.push('HttpVersionInterceptor');
    $httpProvider.interceptors.push('HttpI18nInterceptor');

    $locationProvider.hashPrefix('');

    // disable debug log messages in production
    if (environment.production) {
        $logProvider.debugEnabled(false);
    }

    // Disable "Possibly unhandled rejection:" from angular
    $qProvider.errorOnUnhandledRejections(false);
}
