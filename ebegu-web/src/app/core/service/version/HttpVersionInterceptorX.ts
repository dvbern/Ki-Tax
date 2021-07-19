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

import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest, HttpResponse} from '@angular/common/http';
import {IRootScopeService} from 'angular';
import {Observable} from 'rxjs';
import {tap} from 'rxjs/operators';
import {VERSION} from '../../../../environments/version';
import {CONSTANTS} from '../../constants/CONSTANTS';
import {TSVersionCheckEvent} from '../../events/TSVersionCheckEvent';
import {LogFactory} from '../../logging/LogFactory';

const LOG = LogFactory.createLog('HttpVersionInterceptorX');

/**
 * this interceptor boradcasts a  VERSION_MATCH or VERSION_MISMATCH event whenever a rest service responds
 */
export class HttpVersionInterceptorX implements HttpInterceptor {

    public backendVersion: string;

    // this field is set to true when the event VERSION_MISMATCH has been captured. So we can keep throwing
    // the event until it is handled
    public eventCaptured: boolean = false;

    public constructor(
            private readonly $rootScope: IRootScopeService,
    ) {
    }

    private static hasVersionCompatibility(frontendVersion: string, backendVersion: string): boolean {
        // Wir erwarten, dass die Versionsnummern im Frontend und Backend immer synchronisiert werden
        return frontendVersion === backendVersion;
    }

    public intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        return next.handle(req).pipe(tap((response: HttpResponse<any>) => {
            if (response instanceof HttpResponse &&
                    response.headers &&
                    response.url.indexOf(CONSTANTS.REST_API) === 0) {
                this.updateBackendVersion(response.headers.get('x-ebegu-version'));
            }
        }));
    }

    private updateBackendVersion(newVersion: string): void {

        if (this.eventCaptured && newVersion === this.backendVersion) {
            // if the event hasn't been captured yet we wait until it gets captured
            return;
        }

        this.backendVersion = newVersion;

        if (HttpVersionInterceptorX.hasVersionCompatibility(VERSION, this.backendVersion)) {
            // could throw match event here but currently there is no action we want to perform when it matches
        } else {
            LOG.warn('Versions of Frontend and Backend do not match');
            // before we send the event we say that the event hasn't been captured.
            // After caturing the event this should be set to true
            this.eventCaptured = false;
            this.$rootScope.$broadcast(TSVersionCheckEvent[TSVersionCheckEvent.VERSION_MISMATCH]);
        }
    }
}
