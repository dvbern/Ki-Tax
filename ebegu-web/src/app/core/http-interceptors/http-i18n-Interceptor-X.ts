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

import {
    HttpEvent,
    HttpHandler,
    HttpInterceptor,
    HttpRequest
} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {HEADER_ACCEPT_LANGUAGE} from '../constants/CONSTANTS';
import {I18nServiceRSRest} from '../../i18n/services/i18nServiceRS.rest';

@Injectable()
export class HttpI18nInterceptorX implements HttpInterceptor {
    public constructor(private readonly i18nService: I18nServiceRSRest) {}

    public intercept(
        req: HttpRequest<any>,
        next: HttpHandler
    ): Observable<HttpEvent<any>> {
        const language = req.headers.get(HEADER_ACCEPT_LANGUAGE)
            ? `${this.i18nService.currentLanguage()}, ${req.headers.get(HEADER_ACCEPT_LANGUAGE)}`
            : this.i18nService.currentLanguage();

        const clone = req.clone({
            headers: req.headers.set('Accept-Language', language)
        });

        return next.handle(clone);
    }
}
