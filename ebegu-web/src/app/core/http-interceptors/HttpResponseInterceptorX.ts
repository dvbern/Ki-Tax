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
    HttpErrorResponse,
    HttpEvent,
    HttpHandler,
    HttpInterceptor,
    HttpRequest,
    HttpResponse,
} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {catchError, tap} from 'rxjs/operators';
import {TSHTTPEvent} from '../events/TSHTTPEvent';
import {BroadcastService} from '../service/broadcast.service';

/**
 * this interceptor boradcasts a REQUEST_FINISHED event whenever a rest service responds
 */
@Injectable()
export class HttpResponseInterceptorX implements HttpInterceptor {

    public constructor(private readonly broadcastService: BroadcastService) {
    }

    public intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        return next.handle(req)
            .pipe(
                catchError((err: HttpErrorResponse) => {
                    this.broadcastService.broadcast(TSHTTPEvent[TSHTTPEvent.REQUEST_FINISHED], err);
                    throw err;
                }),
                tap((res: HttpResponse<any>) => {
                    this.broadcastService.broadcast(TSHTTPEvent[TSHTTPEvent.REQUEST_FINISHED], res);
                })
            );
    }

}
