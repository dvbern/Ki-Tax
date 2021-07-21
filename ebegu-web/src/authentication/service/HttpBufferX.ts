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

import {HttpClient, HttpRequest} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Subject} from 'rxjs';
import {NgAuthenticationModule} from '../ng-authentication.module';

@Injectable({
    providedIn: NgAuthenticationModule
})
export class HttpBufferX {

    private buffer: Array<{request: HttpRequest<any>, deferred: Subject<any>}> = [];

    public constructor(
        private http: HttpClient
    ) {
    }

    /**
     * Appends HTTP request object with deferred response attached to buffer.
     */
    public append(request: HttpRequest<any>, deferred: Subject<any>): void {
        this.buffer.push({
            request,
            deferred
        });
    }

    /**
     * Abandon or reject (if reason provided) all the buffered requests.
     */
    public rejectAll(reason: any): void {
        if (reason) {
            this.buffer.forEach(b => b.deferred.error(reason));
        }
        this.buffer = [];
    }

    /**
     * Retries all the buffered requests clears the buffer.
     */
    public retryAll(updater: (transformable: HttpRequest<any>) => HttpRequest<any>): void {
        this.buffer.forEach(b => this.retryHttpRequest(updater(b.request), b.deferred));
        this.buffer = [];
    }

    private retryHttpRequest(request: HttpRequest<any>, deferred: Subject<any>): void {
        this.http.request(request).subscribe(response => {
            deferred.next(response);
            deferred.complete();
        });
    }
}
