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
import {TSLanguage} from '../../models/enums/TSLanguage';
import {DEFAULT_LOCALE} from './constants/CONSTANTS';
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
    '$qProvider',
];

export function configure(
    $translateProvider: ITranslateProvider,
    $injector: IInjectorService,
    $httpProvider: IHttpProvider,
    $mdThemingProvider: IThemingProvider,
    hotkeysProvider: any,
    $logProvider: ILogProvider,
    $locationProvider: ILocationProvider,
    $qProvider: IQProvider,
): void {
    // Translation Provider configuration
    const translPropDE = require('../../assets/translations/translations_de.json');
    const translPropFR = require('../../assets/translations/translations_fr.json');

    // In case you have issues with double-escaped parameters, check out this issue:
    // https://github.com/angular-translate/angular-translate/issues/1101
    $translateProvider.useSanitizeValueStrategy('escapeParameters');

    const preferredLanguage = getFirstBrowserLanguage();

    $translateProvider
        .translations(TSLanguage.DE, translPropDE)
        .translations(TSLanguage.FR, translPropFR)
        .fallbackLanguage(TSLanguage.DE)
        .preferredLanguage(preferredLanguage);

    // Dirty Check configuration (nur wenn plugin vorhanden)
    if ($injector.has('unsavedWarningsConfigProvider')) {
        const unsavedWarningsConfigProvider = $injector.get<any>('unsavedWarningsConfigProvider');
        unsavedWarningsConfigProvider.useTranslateService = true;
        unsavedWarningsConfigProvider.logEnabled = false;
        unsavedWarningsConfigProvider.navigateMessage = 'UNSAVED_WARNING';
        unsavedWarningsConfigProvider.reloadMessage = 'UNSAVED_WARNING_RELOAD';
    }
    // Config Angular Module Theme
    $mdThemingProvider.theme('default')
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

    $locationProvider.hashPrefix('');

    // disable debug log messages in production
    if (environment.production) {
        $logProvider.debugEnabled(false);
    }

    // Disable "Possibly unhandled rejection:" from angular
    $qProvider.errorOnUnhandledRejections(false);
}

/**
 * This function gets the preferred language of the browser
 */
export function getFirstBrowserLanguage(): string {
    const navigator: Navigator = window.navigator;
    const browserLanguagePropertyKeys = ['language', 'browserLanguage', 'systemLanguage', 'userLanguage'];
    let foundLanguages: string[];

    // support for HTML 5.1 "navigator.languages"
    if (Array.isArray(navigator.languages)) {
        foundLanguages = navigator.languages
            .filter(lang => lang && lang.length)
            .map(extractLanguage);
        if (foundLanguages && foundLanguages.length > 0) {
            return foundLanguages[0];
        }
    }

    // support for other well known properties in browsers
    foundLanguages = browserLanguagePropertyKeys
        .map(key => (navigator as any)[key])
        .filter(lang => lang && lang.length)
        .map(extractLanguage);

    return foundLanguages && foundLanguages.length > 0 ? foundLanguages[0] : DEFAULT_LOCALE;
}

/**
 * In order to support languages independently of the country, we return just the language
 * fr-CH --> fr
 * de-CH --> de
 * de    --> de
 */
export function extractLanguage(lang: string): string {
    return lang ? lang.substr(0, lang.indexOf('-')) : '';
}
