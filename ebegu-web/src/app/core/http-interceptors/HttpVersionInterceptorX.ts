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

import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest, HttpResponse} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {tap} from 'rxjs/operators';
import {VersionService} from '../service/version/version.service';
import {CONSTANTS} from '../constants/CONSTANTS';

/**
 * this interceptor boradcasts a  VERSION_MATCH or VERSION_MISMATCH event whenever a rest service responds
 */
@Injectable()
export class HttpVersionInterceptorX implements HttpInterceptor {

    public constructor(
            private readonly versionService: VersionService,
    ) {
    }

    public intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        return next.handle(req).pipe(tap((response: HttpResponse<any>) => {
            if (response instanceof HttpResponse &&
                    response.headers &&
                    req.url.indexOf(CONSTANTS.REST_API) === 0) {
                this.versionService.updateBackendVersion(response.headers.get('x-ebegu-version'));
            }
        }));
    }
}
