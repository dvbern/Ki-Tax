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

/**
 * Language supported by the application.
 */
export enum TSBrowserLanguage {
    DE = 'de',
    FR = 'fr',
}

/**
 * In order to support languages independently of the country, we return just the language
 * fr-CH --> fr
 * de-CH --> de
 * de    --> de
 * *     --> de
 */
export function tsBrowserLanguageFromString(stringLanguage: string): TSBrowserLanguage {
    if (stringLanguage && stringLanguage.startsWith('fr')) {
        return TSBrowserLanguage.FR;
    }
    return TSBrowserLanguage.DE;
}
