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

import {HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {IQService} from 'angular';
import {from, Observable} from 'rxjs';
import {catchError} from 'rxjs/operators';
import {CONSTANTS, HTTP_ERROR_CODES} from '../../app/core/constants/CONSTANTS';
import {isIgnorableHttpError} from '../../app/core/errors/service/HttpErrorInterceptor';
import {TSAuthEvent} from '../../models/enums/TSAuthEvent';
import {AuthLifeCycleService} from './authLifeCycle.service';
import {HttpBuffer} from './HttpBuffer';

@Injectable()
export class HttpAuthInterceptor implements HttpInterceptor {

    public static $inject = ['AuthLifeCycleService', '$q', 'CONSTANTS', 'httpBuffer'];

    public constructor(
        private readonly authLifeCycleService: AuthLifeCycleService,
        private readonly $q: IQService,
        private readonly httpBuffer: HttpBuffer,
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
                        if (isIgnorableHttpError(err)) {
                            console.debug('rejecting failed notokenrefresh response');
                            throw err;
                        }
                        // all requests that failed due to notAuthenticated are appended to httpBuffer. Use
                        // httpBuffer.retryAll to submit them.
                        const deferred = this.$q.defer();
                        this.httpBuffer.append(req, deferred);
                        this.authLifeCycleService.changeAuthStatus(TSAuthEvent.NOT_AUTHENTICATED, err.message);
                        return from(deferred.promise) as Observable<HttpEvent<any>>;
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
