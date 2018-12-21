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

import * as Raven from 'raven-js';
import {Observable, ReplaySubject} from 'rxjs';
import {Permission} from '../../app/authorisation/Permission';
import {PERMISSIONS} from '../../app/authorisation/Permissions';
import {CONSTANTS} from '../../app/core/constants/CONSTANTS';
import {LogFactory} from '../../app/core/logging/LogFactory';
import BenutzerRS from '../../app/core/service/benutzerRS.rest';
import {TSAuthEvent} from '../../models/enums/TSAuthEvent';
import {TSRole} from '../../models/enums/TSRole';
import TSBenutzer from '../../models/TSBenutzer';
import EbeguRestUtil from '../../utils/EbeguRestUtil';
import {TSRoleUtil} from '../../utils/TSRoleUtil';
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

    public static $inject = [
        '$http', '$q', '$timeout', '$cookies', 'EbeguRestUtil', 'httpBuffer',
        'AuthLifeCycleService',
        'BenutzerRS',
    ];

    private principal?: TSBenutzer;

    // We are using a ReplaySubject, because it blocks the authenticationHook until the first value is emitted.
    // Thus the session restoration from the cookie is completed before the authenticationHook checks for
    // authentication.
    private readonly principalSubject$ = new ReplaySubject<TSBenutzer | null>(1);

    private _principal$: Observable<TSBenutzer | null> = this.principalSubject$.asObservable();
    private portalAccCreationLink: string;

    public constructor(
        private readonly $http: IHttpService,
        private readonly $q: IQService,
        private readonly $timeout: ITimeoutService,
        private readonly $cookies: ICookiesService,
        private readonly ebeguRestUtil: EbeguRestUtil,
        private readonly httpBuffer: HttpBuffer,
        private readonly authLifeCycleService: AuthLifeCycleService,
        private readonly benutzerRS: BenutzerRS,
    ) {
    }

    // Use the observable, when the state must be updated automatically, when the principal changes.
    // e.g. printing the name of the current user
    public get principal$(): Observable<TSBenutzer | null> {
        return this._principal$;
    }

    public set principal$(value$: Observable<TSBenutzer | null>) {
        this._principal$ = value$;
    }

    public getPrincipal(): TSBenutzer | undefined {
        return this.principal;
    }

    public getPrincipalRole(): TSRole | undefined {
        if (this.principal) {
            return this.principal.getCurrentRole();
        }
        return undefined;
    }

    public loginRequest(userCredentials: TSBenutzer): IPromise<TSBenutzer> | undefined {
        if (!userCredentials) {
            return undefined;
        }

        return this.$http.post(CONSTANTS.REST_API + 'auth/login',
            this.ebeguRestUtil.userToRestObject({}, userCredentials))
            .then(() => {
                // try to reload buffered requests
                this.httpBuffer.retryAll((config: IRequestConfig) => config);
                // ensure that there is ALWAYS a logout-event before the login-event by throwing it right before
                // login
                this.authLifeCycleService.changeAuthStatus(TSAuthEvent.LOGOUT_SUCCESS, 'logged out before logging in');
                // Response cookies are not immediately accessible, so lets wait for a bit
                return this.$timeout(() => this.initWithCookie(), 100);
            });
    }

    public initWithCookie(): IPromise<TSBenutzer> {
        LOG.debug('initWithCookie');

        const authIdbase64 = this.$cookies.get('authId');
        if (!authIdbase64) {
            this.clearPrincipal();
            return this.$q.reject(TSAuthEvent.NOT_AUTHENTICATED);
        }

        try {
            const authData = angular.fromJson(atob(decodeURIComponent(authIdbase64)));
            // we take the complete user from Server and store it in principal
            return this.reloadUser(authData.authId);
        } catch (e) {
            LOG.error('cookie decoding failed', e);
            this.clearPrincipal();
            return this.$q.reject(TSAuthEvent.NOT_AUTHENTICATED);
        }
    }

    public getPortalAccountCreationPageLink(): IPromise<string> {
        if (this.portalAccCreationLink) {
            return this.$q.when(this.portalAccCreationLink);
        }

        return this.$http.get(CONSTANTS.REST_API + 'auth/portalAccountPage').then((res: any) => {
            this.portalAccCreationLink = res.data;
            return res.data;
        });
    }

    public burnPortalTimout(): IPromise<any> {
        return this.getPortalAccountCreationPageLink().then((linktext: string) => {
            console.log('try to burn timeout page at ' + linktext);
            if (linktext) {
                return this.$http.get(linktext,{withCredentials: true }).then((res: any) => {
                    console.log('retrived portal account creation page to burn unwanted timeout warning ')
                });
            } else {
                return this.$q(undefined);
            }

        })
    }

    public reloadCurrentUser(): IPromise<TSBenutzer> {
        return this.reloadUser(this.getPrincipal().username);
    }

    private reloadUser(username: string): IPromise<TSBenutzer> {
        return this.benutzerRS.findBenutzer(username).then(user => {
            this.principalSubject$.next(user);
            this.principal = user;
            Raven.setUserContext({
                id: this.principal.username,
                email: this.principal.email,
                role: this.principal.getCurrentRole(),
                amt: this.principal.amt,
                status: this.principal.status,
                mandant: this.principal.mandant ? this.principal.mandant.name : null,
                traegerschaft: this.principal.currentBerechtigung.traegerschaft ? this.principal.currentBerechtigung.traegerschaft.name : null,
                institution: this.principal.currentBerechtigung.institution ? this.principal.currentBerechtigung.institution.name : null
            });

            this.authLifeCycleService.changeAuthStatus(TSAuthEvent.LOGIN_SUCCESS, 'logged in');

            return user;
        });
    }

    public logoutRequest(): any {
        return this.$http.post(CONSTANTS.REST_API + 'auth/logout', null).then((res: any) => {
            this.clearPrincipal();
            Raven.setUserContext({});
            this.authLifeCycleService.changeAuthStatus(TSAuthEvent.LOGOUT_SUCCESS, 'logged out');
            return res;
        });
    }

    public clearPrincipal(): void {
        this.principal = undefined;
        this.principalSubject$.next(null);
    }

    public initSSOLogin(relayPath: string): IPromise<string> {
        return this.initSSO(CONSTANTS.REST_API + 'auth/singleSignOn', relayPath);
    }

    public initSingleLogout(relayPath: string): IPromise<string> {
        return this.initSSO(CONSTANTS.REST_API + 'auth/singleLogout', relayPath);
    }

    private initSSO(path: string, relayPath: string): IPromise<string> {
        return this.$http.get(path, {params: {relayPath}}).then((res: any) => {
            return res.data;
        });
    }

    /**
     * Gibt true zurueck, wenn der eingelogte Benutzer die gegebene Role hat. Fuer undefined Werte wird immer false
     * zurueckgegeben.
     */
    public isRole(role: TSRole): boolean {
        if (role && this.principal) {
            return this.principal.hasRole(role);
        }
        return false;
    }

    /**
     * gibt true zurueck wenn der aktuelle Benutzer eine der uebergebenen Rollen innehat
     */
    public isOneOfRoles(roles: Array<TSRole>): boolean {
        if (roles !== undefined && roles !== null && this.principal) {
            return this.principal.hasOneOfRoles(roles);
        }
        return false;
    }

    public getVisibleRolesForPrincipal(): TSRole[] {
        switch (this.getPrincipalRole()) {
            case TSRole.SUPER_ADMIN:
                return TSRoleUtil.getAllRolesButAnonymous();

            case TSRole.ADMIN_INSTITUTION:
                return PERMISSIONS[Permission.ROLE_INSTITUTION];

            case TSRole.ADMIN_TRAEGERSCHAFT:
                return PERMISSIONS[Permission.ROLE_INSTITUTION]
                    .concat(PERMISSIONS[Permission.ROLE_TRAEGERSCHAFT]);

            case TSRole.ADMIN_MANDANT:
                return PERMISSIONS[Permission.ROLE_MANDANT];

            case TSRole.ADMIN_BG:
                return PERMISSIONS[Permission.ROLE_BG];

            case TSRole.ADMIN_TS:
            case TSRole.ADMIN_GEMEINDE:
            case TSRole.REVISOR:
                return PERMISSIONS[Permission.ROLE_GEMEINDE];

            default:
                // by default the role of the user itself. the user can always see his role
                return [this.getPrincipalRole()];
        }
    }
}
