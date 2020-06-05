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
import {IComponentOptions, IController, ILocationService, ITimeoutService, IWindowService} from 'angular';
import {DvDialog} from '../../app/core/directive/dv-dialog/dv-dialog';
import {ApplicationPropertyRS} from '../../app/core/rest-services/applicationPropertyRS.rest';
import {TSRole} from '../../models/enums/TSRole';
import {navigateToStartPageForRole} from '../../utils/AuthenticationUtil';
import {IAuthenticationStateParams} from '../authentication.route';
import {RedirectWarningDialogController} from '../redirect-warning-dialog/RedirectWarningDialogController';
import {AuthServiceRS} from '../service/AuthServiceRS.rest';

// tslint:disable-next-line:naming-convention variable-name
export const LoginComponentConfig: IComponentOptions = {
    transclude: false,
    template: require('./login.component.html'),
    controllerAs: 'vm',
    bindings: {
        returnTo: '<',
    },
};

const dialogTemplate = require('../redirect-warning-dialog/redirectWarningDialogTemplate.html');

export class LoginComponentController implements IController {

    public static $inject: string[] = ['$state', '$stateParams', '$window', '$timeout', 'AuthServiceRS', '$location',
        'DvDialog', 'ApplicationPropertyRS'];

    public redirectionHref: string;
    public logoutHref: string;
    public redirecting: boolean;
    public countdown: number = 0;

    public returnTo: TargetState;

    public constructor(
        private readonly $state: StateService,
        private readonly $stateParams: IAuthenticationStateParams,
        private readonly $window: IWindowService,
        private readonly $timeout: ITimeoutService,
        private readonly authService: AuthServiceRS,
        private readonly $location: ILocationService,
        private readonly dvDialog: DvDialog,
        private readonly applicationPropertyRS: ApplicationPropertyRS,
    ) {
    }

    public $onInit(): void {

        if (this.$stateParams.type !== undefined && this.$stateParams.type === 'logout') {
            this.doLogout();
            return;
        }

        this.applicationPropertyRS.isDevMode().then(isDevMode => {
            // tslint:disable-next-line:early-exit
            if (isDevMode) {
                this.dvDialog.showDialog(dialogTemplate, RedirectWarningDialogController, {})
                    .then(() => {
                        this.doRelocate();
                    });
            } else {
                this.doRelocate();
            }
        });
    }

    private doRelocate(): void {
        // wir leiten hier mal direkt weiter, theoretisch koennte man auch eine auswahl praesentieren
        const relayUrl = this.$state.href(this.returnTo.$state(), this.returnTo.params(), {absolute: true});
        // wrap in burn timeout request, note that this will always produce an error
        // because no Access-Control-Allow-Origin is set, this should not matter however because
        // the point of the request ist to clear the timeout page
        this.authService.burnPortalTimeout().finally(() => {

            this.authService.initSSOLogin(relayUrl)
                .then(url => {
                    this.redirectionHref = url;

                    this.redirecting = true;
                    if (this.countdown > 0) {
                        this.$timeout(this.doCountdown, 1000);
                    }
                    this.$timeout(() => this.redirect(this.redirectionHref), this.countdown * 1000);
                });
        });
    }

    public getBaseURL(): string {
        // let port = (this.$location.port() === 80 || this.$location.port() === 443) ? '' : ':' + tslint:disable-line
        // this.$location.port();
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

    public singlelogout(): void {
        this.authService.logoutRequest().then(() => {
            if (this.logoutHref !== '' || this.logoutHref === undefined) {
                this.$window.open(this.logoutHref, '_self');
            } else {
                // wenn wir nicht im connector ausloggen gehen wir auf den anonymous state
                navigateToStartPageForRole(TSRole.ANONYMOUS, this.$state);
            }
        });
    }

    public isLoggedId(): boolean {
        console.log('logged in principal', this.authService.getPrincipal());
        return !!this.authService.getPrincipal();
    }

    private redirect(urlToGoTo: string): void {
        console.log('redirecting to login', urlToGoTo);

        this.$window.open(urlToGoTo, '_self');
    }

    /**
     * triggered einen logout, fuer iam user sowohl in iam als auch in ebegu,
     * bei lokalen benutzern wird auch nur bei uns ausgeloggt
     */
    private doLogout(): void {
        if (this.authService.getPrincipal()) {
            // wenn logged in
            this.authService.initSingleLogout(this.getBaseURL()).then(responseLogut => {
                this.logoutHref = responseLogut;
                this.singlelogout();
            });
            return;
        }

        // wenn wir nicht in iam ausloggen gehen wir auf den anonymous state
        navigateToStartPageForRole(TSRole.ANONYMOUS, this.$state);
    }

    private readonly doCountdown = () => {
        if (this.countdown > 0) {
            this.countdown--;
            this.$timeout(this.doCountdown, 1000);
        }

    }
}

LoginComponentConfig.controller = LoginComponentController;
