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

import {IHttpInterceptor, IHttpResponse, IPromise, IQService} from 'angular';
import {isIgnorableHttpError} from '../../app/core/errors/service/HttpErrorInterceptor';
import {TSAuthEvent} from '../../models/enums/TSAuthEvent';
import {AuthLifeCycleService} from './authLifeCycle.service';

export class HttpAuthInterceptor implements IHttpInterceptor {

    public static $inject = ['AuthLifeCycleService', '$q', 'CONSTANTS'];

    public constructor(
        private readonly authLifeCycleService: AuthLifeCycleService,
        private readonly $q: IQService,
        private readonly CONSTANTS: any,
    ) {
    }

    public responseError = <T>(response: any): IPromise<IHttpResponse<T>> | IHttpResponse<T> => {
        const http401 = 401;
        const http403 = 403;

        switch (response.status) {
            case http401:
                // exclude requests from the login form
                if (response.config && response.config.url === `${this.CONSTANTS.REST_API}auth/login`) {
                    return this.$q.reject(response);
                }
                // if this request was a background polling request we do not want to relogin or show errors
                if (isIgnorableHttpError(response)) {
                    console.debug('rejecting failed notokenrefresh response');
                    return this.$q.reject(response);
                }
                const deferred = this.$q.defer();
                this.authLifeCycleService.changeAuthStatus(TSAuthEvent.NOT_AUTHENTICATED, response);
                return deferred.promise as IPromise<IHttpResponse<T>>;
            case http403:
                this.authLifeCycleService.changeAuthStatus(TSAuthEvent.NOT_AUTHORISED, response);
                return this.$q.reject(response);
            default:
                return this.$q.reject(response);
        }
    }
}
