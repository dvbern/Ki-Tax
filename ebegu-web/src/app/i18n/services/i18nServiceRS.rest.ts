/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {Observable} from 'rxjs';
import {TSLanguage} from '../../../models/enums/TSLanguage';
import {CONSTANTS, LOCALSTORAGE_LANGUAGE_KEY} from '../../core/constants/CONSTANTS';
import {WindowRef} from '../../core/service/windowRef.service';

@Injectable({
    providedIn: 'root',
})
export class I18nServiceRSRest {

    public serviceURL: string;

    public constructor(
        private readonly http: HttpClient,
        private readonly translate: TranslateService,
        private readonly $window: WindowRef,
    ) {
        this.serviceURL =  `${CONSTANTS.REST_API}i18n`;
    }

    /**
     * Calling this method we change the language that the server is using
     */
    public changeServerLanguage(selectedLanguage: TSLanguage): Observable<any> {
        return this.http.put<TSLanguage>(this.serviceURL, selectedLanguage);
    }

    /**
     * This method will change the language that the plugin of angular5 uses.
     * This method must exist in order to be able to change the language from within an angularjs component
     */
    public changeClientLanguage(selectedLanguage: TSLanguage): void {
        this.$window.nativeLocalStorage.setItem(LOCALSTORAGE_LANGUAGE_KEY, selectedLanguage);
        this.translate.use(selectedLanguage);
    }

    /**
     * Because it still is an hybrid application we call this method from within angular, while the exported
     * function will be used con configuring the language from config.ts (angular.js) because there we don't have
     * any service registered yet
     */
    public getPreferredLanguage(): string {
        return getPreferredLanguage(this.$window.nativeWindow);
    }

}

/**
 * This function will try to get the selected language out of the localStorage. If the kibonLanguage is not found
 * it will retrieve the language of the browser and stores it in the localStorage so it is stored for the next time
 */
export function getPreferredLanguage($window: Window): string {
    const myStorage = $window.localStorage;
    const kibonLanguage = myStorage.getItem('kibonLanguage');
    if (kibonLanguage) {
        return kibonLanguage;
    }

    const firstBrowserLanguage = getFirstBrowserLanguage($window);
    myStorage.setItem(LOCALSTORAGE_LANGUAGE_KEY, firstBrowserLanguage);
    return firstBrowserLanguage;
}

/**
 * This function gets the preferred language of the browser
 */
function getFirstBrowserLanguage($window: Window): TSLanguage {
    const navigator: Navigator = $window.navigator;
    const browserLanguagePropertyKeys = ['language', 'browserLanguage', 'systemLanguage', 'userLanguage'];
    let foundLanguages: TSLanguage[];

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

    return foundLanguages && foundLanguages.length > 0 ? foundLanguages[0] : TSLanguage.DE;
}

/**
 * In order to support languages independently of the country, we return just the language
 * fr-CH --> fr
 * de-CH --> de
 * de    --> de
 */
function extractLanguage(lang: string): TSLanguage {
    if (lang && lang.startsWith('fr')) {
        return TSLanguage.FR;
    }
    return TSLanguage.DE;
}
