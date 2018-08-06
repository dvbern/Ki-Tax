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

import {StateService, Transition, TransitionService} from '@uirouter/core';
import * as angular from 'angular';
import {AuthLifeCycleService} from '../../authentication/service/authLifeCycle.service';
import AuthServiceRS from '../../authentication/service/AuthServiceRS.rest';
import {RouterHelper} from '../../dvbModules/router/route-helper-provider';
import {environment} from '../../environments/environment';
import GemeindeRS from '../../gesuch/service/gemeindeRS.rest';
import GesuchModelManager from '../../gesuch/service/gesuchModelManager';
import GlobalCacheService from '../../gesuch/service/globalCacheService';
import {TSAuthEvent} from '../../models/enums/TSAuthEvent';
import {TSCacheTyp} from '../../models/enums/TSCacheTyp';
import TSApplicationProperty from '../../models/TSApplicationProperty';
import {TSRoleUtil} from '../../utils/TSRoleUtil';
import ErrorService from './errors/service/ErrorService';
import {ApplicationPropertyRS} from './rest-services/applicationPropertyRS.rest';
import GesuchsperiodeRS from './service/gesuchsperiodeRS.rest';
import {InstitutionStammdatenRS} from './service/institutionStammdatenRS.rest';
import ListResourceRS from './service/listResourceRS.rest';
import {MandantRS} from './service/mandantRS.rest';
import IInjectorService = angular.auto.IInjectorService;
import ILocationService = angular.ILocationService;
import ILogService = angular.ILogService;
import ITimeoutService = angular.ITimeoutService;

appRun.$inject = ['angularMomentConfig', 'RouterHelper', 'ListResourceRS', 'MandantRS', '$injector', 'AuthLifeCycleService', 'hotkeys',
    '$timeout', 'AuthServiceRS', '$state', '$location', '$window', '$log', 'ErrorService', 'GesuchModelManager', 'GesuchsperiodeRS',
    'InstitutionStammdatenRS', 'GlobalCacheService', '$transitions', 'GemeindeRS', '$trace'];

export function appRun(angularMomentConfig: any, routerHelper: RouterHelper, listResourceRS: ListResourceRS,
                       mandantRS: MandantRS, $injector: IInjectorService, authLifeCycleService: AuthLifeCycleService, hotkeys: any, $timeout: ITimeoutService,
                       authServiceRS: AuthServiceRS, $state: StateService, $location: ILocationService, $window: ng.IWindowService,
                       $log: ILogService, errorService: ErrorService, gesuchModelManager: GesuchModelManager,
                       gesuchsperiodeRS: GesuchsperiodeRS, institutionsStammdatenRS: InstitutionStammdatenRS, globalCacheService: GlobalCacheService,
                       $transitions: TransitionService, gemeindeRS: GemeindeRS, $trace: any) {
    // navigationLogger.toggle();

    $trace.enable(1);

    $transitions.onStart({}, transition => {
        stateChangeStart(transition);
        transition.promise
            .then(() => stateChangeSuccess())
            .catch(() => stateChangeError(transition));
        // .finally(() => stateChangeSuccess());
    });

    // Fehler beim Navigieren ueber ui-route ins Log schreiben
    function stateChangeError(transition: Transition) {
        $log.error('Fehler beim Navigieren');
        $log.error('$stateChangeError --- event, toState, toParams, fromState, fromParams, error');
        $log.error(transition);
    }

    function stateChangeStart(transition: Transition) {
        // TODO HEFA migrate to state definition
        //Normale Benutzer duefen nicht auf admin Seite
        const forbiddenPlaces = ['admin.view', 'admin.institution', 'admin.parameter', 'admin.traegerschaft'];
        const isAdmin: boolean = authServiceRS.isOneOfRoles(TSRoleUtil.getAdministratorRevisorRole());
        if (forbiddenPlaces.indexOf(transition.to().name) !== -1 && authServiceRS.getPrincipal() && !isAdmin) {
            errorService.addMesageAsError('ERROR_UNAUTHORIZED');
            $log.debug('prevented navigation to page because user is not admin');
            event.preventDefault();
        }
    }

    function stateChangeSuccess() {
        errorService.clearAll();
    }

    angularMomentConfig.format = 'DD.MM.YYYY';
    // dieser call macht mit tests probleme, daher wird er fuer test auskommentiert

    // not used anymore?
    authLifeCycleService.get$(TSAuthEvent.LOGIN_SUCCESS)
        .subscribe(() => {
            if (!environment.test) {
                listResourceRS.getLaenderList();  //initial aufruefen damit cache populiert wird
                mandantRS.getFirst();
            }
            globalCacheService.getCache(TSCacheTyp.EBEGU_INSTITUTIONSSTAMMDATEN).removeAll(); // muss immer geleert werden
            //since we will need these lists anyway we already load on login
            gesuchsperiodeRS.updateActiveGesuchsperiodenList().then((gesuchsperioden) => {
                if (gesuchsperioden.length > 0) {
                    const newestGP = gesuchsperioden[0];
                    institutionsStammdatenRS.getAllActiveInstitutionStammdatenByGesuchsperiode(newestGP.id);
                }
            });
            gemeindeRS.getAllGemeinden();
            gesuchsperiodeRS.updateNichtAbgeschlosseneGesuchsperiodenList();
            gesuchModelManager.updateFachstellenList();
        });

    authLifeCycleService.get$(TSAuthEvent.NOT_AUTHENTICATED)
        .subscribe(() => {
            //user is not yet authenticated, show loginpage

            const currentPath = angular.copy($location.absUrl());
            console.log('going to login page with current path ', currentPath);

            //wenn wir schon auf der lognseite oder im redirect sind redirecten wir nicht
            if (currentPath.indexOf('fedletSSOInit') === -1
                && ($state.current !== undefined && $state.current.name !== 'authentication.login')
                && ($state.current !== undefined && $state.current.name !== 'authentication.locallogin')
                && ($state.current !== undefined && $state.current.name !== 'authentication.schulung')
                && currentPath.indexOf('sendRedirectForValidation') === -1) {
                $state.go('authentication.login', {relayPath: currentPath, type: 'login'});
            } else {
                console.log('supressing redirect to ', currentPath);
            }
        });

    // Attempt to restore a user session upon startup
    authServiceRS.initWithCookie().then(() => {
        $log.debug('logged in from cookie');
    });

    if (!environment.test) {
        //Hintergrundfarbe anpassen (testsystem kann zB andere Farbe haben)
        const applicationPropertyRS = $injector.get<ApplicationPropertyRS>('ApplicationPropertyRS');
        applicationPropertyRS.getBackgroundColor().then((prop: TSApplicationProperty) => {
            if (prop && prop.value !== '#FFFFFF') {
                angular.element('#Intro').css('background-color', prop.value);
                angular.element('.user-menu').find('button').first().css('background', prop.value);
            }
        });
    }

    // Wir meochten eigentlich ueberall mit einem hotkey das formular submitten koennen
    //https://github.com/chieffancypants/angular-hotkeys#angular-hotkeys-
    hotkeys.add({
        combo: 'ctrl+shift+x',
        description: 'Press the last button with style class .next',
        callback: () => {
            $timeout(() => angular.element('.next').last().click());
        }
    });

}
