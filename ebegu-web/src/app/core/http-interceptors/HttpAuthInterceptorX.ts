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

import {HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable, Subject} from 'rxjs';
import {catchError} from 'rxjs/operators';
import {AuthLifeCycleService} from '../../../authentication/service/authLifeCycle.service';
import {TSAuthEvent} from '../../../models/enums/TSAuthEvent';
import {CONSTANTS, HTTP_ERROR_CODES} from '../constants/CONSTANTS';
import {isIgnorableHttpError} from '../errors/service/HttpErrorInterceptorX';

@Injectable({
    providedIn: 'root',
})
export class HttpAuthInterceptorX implements HttpInterceptor {

    public constructor(
        private readonly authLifeCycleService: AuthLifeCycleService
    ) {
    }

    public intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        return next.handle(req)
        .pipe(
            catchError((err: HttpErrorResponse) => {
                switch (err.status) {
                    case HTTP_ERROR_CODES.UNAUTHORIZED:
                        // exclude requests from the login form
                        if (req.url === `${CONSTANTS.REST_API}auth/login`) {
                            throw err;
                        }
                        // if this request was a background polling request we do not want to relogin or show errors
                        if (
                            isIgnorableHttpError(req)) {
                            console.debug('rejecting failed notokenrefresh response');
                            throw err;
                        }
                        const deferred$ = new Subject<HttpEvent<any>>();
                        this.authLifeCycleService.changeAuthStatus(TSAuthEvent.NOT_AUTHENTICATED, err.message);
                        return deferred$;
                    case HTTP_ERROR_CODES.FORBIDDEN:
                        this.authLifeCycleService.changeAuthStatus(TSAuthEvent.NOT_AUTHORISED, err.message);
                        throw err;
                    default:
                        throw err;
                }
            }),
        );

    }
}
