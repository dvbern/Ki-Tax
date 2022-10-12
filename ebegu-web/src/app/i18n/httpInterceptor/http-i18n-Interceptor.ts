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

import {IHttpInterceptor, IRequestConfig} from 'angular';
import {HEADER_ACCEPT_LANGUAGE} from '../../core/constants/CONSTANTS';
import {I18nServiceRSRest} from '../services/i18nServiceRS.rest';

export class HttpI18nInterceptor implements IHttpInterceptor {

    public static $inject = ['I18nServiceRSRest'];

    public constructor(
        private readonly i18nService: I18nServiceRSRest
    ) {
    }

    public request = (config: IRequestConfig) => {
        config.headers[HEADER_ACCEPT_LANGUAGE] = config.headers[HEADER_ACCEPT_LANGUAGE] ?
            `${this.i18nService.currentLanguage()}, ${config.headers[HEADER_ACCEPT_LANGUAGE]}`
            : this.i18nService.currentLanguage();

        return config;
    };
}
