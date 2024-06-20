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

import {IHttpInterceptor, IQService, IRootScopeService} from 'angular';
import {TSHTTPEvent} from '../events/TSHTTPEvent';

/**
 * this interceptor boradcasts a REQUEST_FINISHED event whenever a rest service responds
 */
export class HttpResponseInterceptor implements IHttpInterceptor {
    public static $inject = ['$rootScope', '$q'];

    public constructor(
        private readonly $rootScope: IRootScopeService,
        private readonly $q: IQService
    ) {}

    public responseError = (response: any) => {
        this.$rootScope.$broadcast(
            TSHTTPEvent[TSHTTPEvent.REQUEST_FINISHED],
            response
        );
        return this.$q.reject(response);
    };

    public response = (response: any) => {
        this.$rootScope.$broadcast(
            TSHTTPEvent[TSHTTPEvent.REQUEST_FINISHED],
            response
        );
        return response;
    };
}
