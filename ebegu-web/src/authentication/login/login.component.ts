/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
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

import {StateService, TargetState} from '@uirouter/core';
import {IComponentOptions, IController, IHttpParamSerializer, ILocationService, ITimeoutService, IWindowService} from 'angular';
import {TSRole} from '../../models/enums/TSRole';
import {navigateToStartPageForRole} from '../../utils/AuthenticationUtil';
import {IAuthenticationStateParams} from '../authentication.route';
import AuthServiceRS from '../service/AuthServiceRS.rest';

export const LoginComponentConfig: IComponentOptions = {
    transclude: false,
    template: require('./login.component.html'),
    controllerAs: 'vm',
    bindings: {
        returnTo: '<'
    }
};

export class LoginComponentController implements IController {

    static $inject: string[] = ['$state', '$stateParams', '$window', '$httpParamSerializer', '$timeout', 'AuthServiceRS', '$location'];

    private redirectionUrl: string = '/ebegu/saml2/jsp/fedletSSOInit.jsp';
    private redirectionHref: string;

    private logoutHref: string;
    private redirecting: boolean;
    private countdown: number = 0;

    public returnTo: TargetState;

    constructor(private readonly $state: StateService, private readonly $stateParams: IAuthenticationStateParams,
                private readonly $window: IWindowService, private readonly $httpParamSerializer: IHttpParamSerializer,
                private readonly $timeout: ITimeoutService, private readonly authService: AuthServiceRS, private readonly $location: ILocationService) {
    }

    public $onInit(): void {
        //wir leiten hier mal direkt weiter, theoretisch koennte man auch eine auswahl praesentieren
        const relayUrl = this.$state.href(this.returnTo.$state(), this.returnTo.params, {absolute: true});

        this.authService.initSSOLogin(relayUrl)
            .then(response => {
                this.redirectionUrl = response;
                this.redirectionHref = response;
                if (this.$stateParams.type !== undefined && this.$stateParams.type === 'logout') {
                    this.doLogout();
                } else {
                    this.redirecting = true;
                    if (this.countdown > 0) {
                        this.$timeout(this.doCountdown, 1000);
                    }

                    this.$timeout(() => this.redirect(), this.countdown * 1000);
                }
            });

        if (this.authService.getPrincipal()) {  // wenn logged in
            this.authService.initSingleLogout(this.getBaseURL())
                .then((responseLogut) => {
                    this.logoutHref = responseLogut;
                });
        }
    }

    public getBaseURL(): string {
        //let port = (this.$location.port() === 80 || this.$location.port() === 443) ? '' : ':' + this.$location.port();
        const absURL = this.$location.absUrl();
        const index = absURL.indexOf(this.$location.url());
        let result = absURL;
        if (index !== -1) {
            result = absURL.substr(0, index);
            const hashindex = result.indexOf('#');
            if (hashindex !== -1) {
                result = absURL.substr(0, hashindex);
            }

        }
        return result;
    }

    public singlelogout() {
        this.authService.logoutRequest().then(() => {
            if (this.logoutHref !== '' || this.logoutHref === undefined) {
                this.$window.open(this.logoutHref, '_self');
            } else {
                // wenn wir nicht in iam ausloggen gehen wir auf den anonymous state
                navigateToStartPageForRole(TSRole.ANONYMOUS, this.$state);
            }
        });
    }

    public isLoggedId(): boolean {
        console.log('logged in principal', this.authService.getPrincipal());
        return this.authService.getPrincipal() ? true : false;
    }

    private redirect() {
        const urlToGoTo = this.redirectionHref;
        console.log('redirecting to login', urlToGoTo);

        this.$window.open(urlToGoTo, '_self');
    }

    /**
     * triggered einen logout, fuer iam user sowohl in iam als auch in ebegu,
     * bei lokalen benutzern wird auch nur bei uns ausgeloggt
     */
    private doLogout() {
        if (this.authService.getPrincipal()) {  // wenn logged in
            this.authService.initSingleLogout(this.getBaseURL()).then((responseLogut) => {
                this.logoutHref = responseLogut;
                this.singlelogout();
            });
        }

    }

    private readonly doCountdown = () => {
        if (this.countdown > 0) {
            this.countdown--;
            this.$timeout(this.doCountdown, 1000);
        }

    }
}

LoginComponentConfig.controller = LoginComponentController;
