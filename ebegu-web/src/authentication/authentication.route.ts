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

import {Ng1StateDeclaration} from '@uirouter/angularjs';
import {StateService} from '@uirouter/core';
import {ApplicationPropertyRS} from '../app/core/rest-services/applicationPropertyRS.rest';
import {RouterHelper} from '../dvbModules/router/route-helper-provider';
import ILogService = angular.ILogService;
import IPromise = angular.IPromise;
import IQService = angular.IQService;

authenticationRun.$inject = ['RouterHelper'];

export function authenticationRun(routerHelper: RouterHelper) {
    routerHelper.configureStates(ng1States, []);
}

const ng1States: Ng1StateDeclaration[] = [
    {
        parent: 'app',
        abstract: true,
        name: 'authentication',
    },
    {
        name: 'authentication.login',
        component: 'dvLogin',
        //HINWEIS: Soweit ich sehen kann koennen url navigationen mit mehr als einem einzigen slash am Anfang nicht manuell in der Adressbar aufgerufen werden?
        url: '/login?type&relayPath',
        data: {
            isPublic: true,
        }
    },
    {

        name: 'authentication.schulung',
        template: '<dv-schulung flex="auto" class="overflow-scroll">',
        url: '/schulung',
        resolve: {
            dummyLoginEnabled: readDummyLoginEnabled
        },
        data: {
            isPublic: true,
        }
    },
    {
        name: 'authentication.start',
        component: 'dvStart',
        url: '/start',
    }
];

export class IAuthenticationStateParams {
    relayPath: string;
    type: string;
}

readDummyLoginEnabled.$inject = ['ApplicationPropertyRS', '$state', '$q', '$log'];

export function readDummyLoginEnabled(applicationPropertyRS: ApplicationPropertyRS, $state: StateService, $q: IQService,
                                      $log: ILogService): IPromise<boolean> {
    return applicationPropertyRS.isDummyMode()
        .then((response: boolean) => {
            if (response === false) {
                $log.debug('page is disabled');
                $state.go('authentication.start');
            }
            return response;
        }).catch(() => {
            const deferred = $q.defer<boolean>();
            deferred.resolve(undefined);
            $state.go('authentication.login');
            return deferred.promise;
        });

}

