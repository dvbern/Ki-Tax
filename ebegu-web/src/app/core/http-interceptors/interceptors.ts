/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

import {HTTP_INTERCEPTORS} from '@angular/common/http';
import {HttpAuthInterceptorX} from '../../../authentication/service/HttpAuthInterceptorX';
import {HttpI18nInterceptorX} from '../../i18n/httpInterceptor/http-i18n-Interceptor-X';
import {XsrfInterceptor} from '../../i18n/httpInterceptor/XsrfInterceptor';
import {HttpVersionInterceptorX} from '../service/version/HttpVersionInterceptorX';

export const HTTP_INTERCEPTOR_PROVIDERS = [
    { provide: HTTP_INTERCEPTORS, useClass: HttpVersionInterceptorX, multi: true},
    { provide: HTTP_INTERCEPTORS, useClass: XsrfInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: HttpI18nInterceptorX, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: HttpAuthInterceptorX, multi: true },
];
