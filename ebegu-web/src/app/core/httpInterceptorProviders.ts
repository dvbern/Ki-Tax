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

import {Provider} from '@angular/core';
import {HttpI18nInterceptor} from '../i18n/httpInterceptor/http-i18n-Interceptor';
import IInjectorService = angular.auto.IInjectorService;

/* eslint-disable */

// HttpI18nInterceptor
export function httpI18nInterceptorFactory(i: IInjectorService): HttpI18nInterceptor {
    return i.get('HttpI18nInterceptor');
}

export const httpI18nInterceptorProvider = {
    provide: HttpI18nInterceptor,
    useFactory: httpI18nInterceptorFactory,
    deps: ['$injector'],
    multi: true,
};

export const UPGRADED_HTTP_INTERCEPTOR_PROVIDERS: Provider[] = [
    httpI18nInterceptorProvider,
];
