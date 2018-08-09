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

import * as angular from 'angular';
import {Observable, ReplaySubject} from 'rxjs';
import {LogFactory} from '../../app/core/logging/LogFactory';
import UserRS from '../../app/core/service/userRS.rest';
import {TSAuthEvent} from '../../models/enums/TSAuthEvent';
import {TSRole} from '../../models/enums/TSRole';
import TSUser from '../../models/TSUser';
import EbeguRestUtil from '../../utils/EbeguRestUtil';
import {AuthLifeCycleService} from './authLifeCycle.service';
import HttpBuffer from './HttpBuffer';
import ICookiesService = angular.cookies.ICookiesService;
import IHttpService = angular.IHttpService;
import IPromise = angular.IPromise;
import IQService = angular.IQService;
import IRequestConfig = angular.IRequestConfig;
import ITimeoutService = angular.ITimeoutService;

const LOG = LogFactory.createLog('AuthServiceRS');

export default class AuthServiceRS {

    static $inject = ['$http', 'CONSTANTS', '$q', '$timeout', '$cookies', 'EbeguRestUtil', 'httpBuffer', 'AuthLifeCycleService',
        'UserRS'];

    private principal?: TSUser;

    // We are using a ReplaySubject, because it blocks the authenticationHook until the first value is emitted.
    // Thus the session restoration from the cookie is completed before the authenticationHook checks for authentication.
    private readonly principalSubject$ = new ReplaySubject<TSUser | null>(1);

    public principal$: Observable<TSUser | null> = this.principalSubject$.asObservable();

    constructor(private readonly $http: IHttpService,
                private readonly CONSTANTS: any,
                private readonly $q: IQService,
                private readonly $timeout: ITimeoutService,
                private readonly $cookies: ICookiesService,
                private readonly ebeguRestUtil: EbeguRestUtil,
                private readonly httpBuffer: HttpBuffer,
                private readonly authLifeCycleService: AuthLifeCycleService,
                private readonly userRS: UserRS) {
    }

    /**
     * @deprecated use getPrincipal$ instead
     */
    public getPrincipal(): TSUser | undefined {
        return this.principal;
    }

    public getPrincipalRole(): TSRole | undefined {
        if (this.principal) {
            return this.principal.getCurrentRole();
        }
        return undefined;
    }

    public loginRequest(userCredentials: TSUser): IPromise<TSUser> | undefined {
        if (!userCredentials) {
            return undefined;
        }

        return this.$http.post(this.CONSTANTS.REST_API + 'auth/login', this.ebeguRestUtil.userToRestObject({}, userCredentials))
            .then(() => {
                // try to reload buffered requests
                this.httpBuffer.retryAll((config: IRequestConfig) => config);
                //ensure that there is ALWAYS a logout-event before the login-event by throwing it right before
                // login
                this.authLifeCycleService.changeAuthStatus(TSAuthEvent.LOGOUT_SUCCESS, 'logged out before logging in');
                // Response cookies are not immediately accessible, so lets wait for a bit
                return this.$timeout(() => this.initWithCookie(), 100);
            });
    }

    public initWithCookie(): IPromise<TSUser> {
        LOG.debug('initWithCookie');

        const authIdbase64 = this.$cookies.get('authId');
        if (!authIdbase64) {
            this.principalSubject$.next(null);
            return this.$q.reject(TSAuthEvent.NOT_AUTHENTICATED);
        }

        try {
            const authData = angular.fromJson(atob(decodeURIComponent(authIdbase64)));
            // we take the complete user from Server and store it in principal
            return this.userRS.findBenutzer(authData.authId).then(user => {
                this.authLifeCycleService.changeAuthStatus(TSAuthEvent.LOGIN_SUCCESS, 'logged in');
                this.principalSubject$.next(user);
                this.principal = user;

                return user;
            });
        } catch (e) {
            LOG.error('cookie decoding failed', e);
            this.principalSubject$.next(null);
            return this.$q.reject(TSAuthEvent.NOT_AUTHENTICATED);
        }
    }

    public logoutRequest() {
        return this.$http.post(this.CONSTANTS.REST_API + 'auth/logout', null).then((res: any) => {
            this.principal = undefined;
            this.principalSubject$.next(null);
            this.authLifeCycleService.changeAuthStatus(TSAuthEvent.LOGOUT_SUCCESS, 'logged out');
            return res;
        });
    }

    public initSSOLogin(relayPath: string): IPromise<string> {
        return this.$http.get(this.CONSTANTS.REST_API + 'auth/singleSignOn', {params: {relayPath}}).then((res: any) => {
            return res.data;
        });
    }

    public initSingleLogout(relayPath: string): IPromise<string> {
        return this.$http.get(this.CONSTANTS.REST_API + 'auth/singleLogout', {params: {relayPath}}).then((res: any) => {
            return res.data;
        });
    }

    /**
     * Gibt true zurueck, wenn der eingelogte Benutzer die gegebene Role hat. Fuer undefined Werte wird immer false
     * zurueckgegeben.
     */
    public isRole(role: TSRole) {
        if (role && this.principal) {
            return this.principal.getCurrentRole() === role;
        }
        return false;
    }

    /**
     * gibt true zurueck wenn der aktuelle Benutzer eine der uebergebenen Rollen innehat
     */
    public isOneOfRoles(roles: Array<TSRole>): boolean {
        if (roles !== undefined && roles !== null && this.principal) {
            const principalRole = this.principal.getCurrentRole();

            return roles.some(role => role === principalRole);
        }
        return false;
    }
}
