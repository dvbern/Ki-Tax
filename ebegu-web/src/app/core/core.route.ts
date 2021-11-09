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

import {StateService} from '@uirouter/core';
import * as angular from 'angular';
import * as moment from 'moment';
import * as Raven from 'raven-js';
import {take} from 'rxjs/operators';
import {AuthLifeCycleService} from '../../authentication/service/authLifeCycle.service';
import {AuthServiceRS} from '../../authentication/service/AuthServiceRS.rest';
import {environment} from '../../environments/environment';
import {GemeindeRS} from '../../gesuch/service/gemeindeRS.rest';
import {GlobalCacheService} from '../../gesuch/service/globalCacheService';
import {TSAuthEvent} from '../../models/enums/TSAuthEvent';
import {TSCacheTyp} from '../../models/enums/TSCacheTyp';
import {MandantService} from '../shared/services/mandant.service';
import {LogFactory} from './logging/LogFactory';
import {ApplicationPropertyRS} from './rest-services/applicationPropertyRS.rest';
import {GesuchsperiodeRS} from './service/gesuchsperiodeRS.rest';
import {ListResourceRS} from './service/listResourceRS.rest';
import {MandantRS} from './service/mandantRS.rest';
import IInjectorService = angular.auto.IInjectorService;
import ILocationService = angular.ILocationService;
import ITimeoutService = angular.ITimeoutService;

const LOG = LogFactory.createLog('appRun');

appRun.$inject = [
    'angularMomentConfig',
    'ListResourceRS',
    'MandantRS',
    '$injector',
    'AuthLifeCycleService',
    'hotkeys',
    '$timeout',
    'AuthServiceRS',
    '$state',
    '$location',
    'GesuchsperiodeRS',
    'GlobalCacheService',
    'GemeindeRS',
    'LOCALE_ID',
];

export function appRun(
    angularMomentConfig: any,
    listResourceRS: ListResourceRS,
    mandantRS: MandantRS,
    $injector: IInjectorService,
    authLifeCycleService: AuthLifeCycleService,
    hotkeys: any,
    $timeout: ITimeoutService,
    authServiceRS: AuthServiceRS,
    $state: StateService,
    $location: ILocationService,
    gesuchsperiodeRS: GesuchsperiodeRS,
    globalCacheService: GlobalCacheService,
    gemeindeRS: GemeindeRS,
    LOCALE_ID: string,
): void {
    const applicationPropertyRS = $injector.get<ApplicationPropertyRS>('ApplicationPropertyRS');
    const mandantService = $injector.get<MandantService>('MandantService');
    mandantService.mandant$.pipe(
        take(1),
    ).subscribe(() => {
        applicationPropertyRS.getPublicPropertiesCached()
            .then(response => {
                if (environment.test) {
                    return;
                }
                if (response.sentryEnvName) {
                    Raven.setEnvironment(response.sentryEnvName);
                } else {
                    Raven.setEnvironment('unspecified');
                }
            });
    }, error => LOG.error(error));

    function onNotAuthenticated(): void {
        authServiceRS.clearPrincipal();
        const currentPath = angular.copy($location.absUrl());

        const loginConnectorPaths = [
            'fedletSSOInit',
            'sendRedirectForValidation',
            'locallogin',
            'tutorial',
        ];

        if (loginConnectorPaths.some(path => currentPath.includes(path))) {
            LOG.debug('supressing redirect to ', currentPath);
        } else {
            $state.go('authentication.login');
        }
    }

    function onLoginSuccess(): void {
        if (!environment.test) {
            listResourceRS.getLaenderList();  // initial aufruefen damit cache populiert wird
            mandantRS.getFirst();
        }
        // muss immer geleert werden
        globalCacheService.getCache(TSCacheTyp.EBEGU_INSTITUTIONSSTAMMDATEN_GEMEINDE).removeAll();
        // since we will need these lists anyway we already load on login
        gesuchsperiodeRS.updateActiveGesuchsperiodenList();
        gemeindeRS.getAllGemeinden();
        gesuchsperiodeRS.updateNichtAbgeschlosseneGesuchsperiodenList();
    }

    moment.locale(LOCALE_ID);

    authLifeCycleService.get$(TSAuthEvent.LOGIN_SUCCESS)
        .subscribe(
            onLoginSuccess,
            err => LOG.error(err),
        );

    authLifeCycleService.get$(TSAuthEvent.NOT_AUTHENTICATED)
        .subscribe(
            onNotAuthenticated,
            err => LOG.error(err),
        );

    angularMomentConfig.format = 'DD.MM.YYYY';

    // Attempt to restore a user session upon startup
    authServiceRS.initWithCookie().then(() => {
        LOG.debug('logged in from cookie');
    });

    // Wir meochten eigentlich ueberall mit einem hotkey das formular submitten koennen
    // https://github.com/chieffancypants/angular-hotkeys#angular-hotkeys
    hotkeys.add({
        combo: 'ctrl+shift+x',
        description: 'Press the last button with style class .next',
        callback: () => $timeout(() => angular.element('.next').last().trigger('click')),
    });

}
