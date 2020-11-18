/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
/**
 * HttpInterceptor, der X-XSRF-TOKEN Header setzt. Angular HttpClient setzt diesen nur bei Post Requests, wir benötigen
 * ihn aber bei allen Requests
 */

import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest, HttpXsrfTokenExtractor} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';

@Injectable()
export class XsrfInterceptor implements HttpInterceptor {

    public constructor(private tokenExtractor: HttpXsrfTokenExtractor) {
    }

    public intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        let requestToForward = req;
        const token = this.tokenExtractor.getToken() as string;
        if (token !== null) {
            requestToForward = req.clone({ setHeaders: { 'X-XSRF-TOKEN': token } });
        }
        return next.handle(requestToForward);
    }
}
