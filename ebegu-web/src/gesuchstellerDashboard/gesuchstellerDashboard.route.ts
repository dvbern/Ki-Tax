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
import {RouterHelper} from '../dvbModules/router/route-helper-provider';
import GesuchModelManager from '../gesuch/service/gesuchModelManager';
import {TSRole} from '../models/enums/TSRole';
import TSGesuch from '../models/TSGesuch';
import ILogService = angular.ILogService;
import IPromise = angular.IPromise;
import IQService = angular.IQService;

gesuchstellerDashboardRun.$inject = ['RouterHelper'];

export function gesuchstellerDashboardRun(routerHelper: RouterHelper) {
    routerHelper.configureStates(ng1States, []);
}

export class IAngebotStateParams {
    gesuchId: string;
    type: string;
}

export class IGesuchstellerDashboardStateParams {
    infoMessage: string;
    dossierId: string;
}

const ng1States: Ng1StateDeclaration[] = [
    {
        parent: 'app',
        abstract: true,
        name: 'gesuchsteller',
        data: {
            roles: [TSRole.GESUCHSTELLER]
        }
    },
    {
        name: 'gesuchsteller.dashboard',
        template: '<gesuchsteller-dashboard-view class="layout-column flex-100">',
        url: '/gesuchstellerDashboard',
        params: {
            gesuchstellerDashboardStateParams: IGesuchstellerDashboardStateParams
        },
        resolve: {
            gesuch: resetGesuchModelManager // always when navigating to the Dashboard the gesuchModelManager must be reset
        },
    },
    {
        name: 'gesuchsteller.createAngebot',
        template: '<create-angebot-view class="layout-column flex-100">',
        url: '/createAngebotView/:type/:gesuchId',
        resolve: {
            gesuch: getGesuchModelManager
        },
    }
];

getGesuchModelManager.$inject = ['GesuchModelManager', '$stateParams', '$q', '$log'];

export function getGesuchModelManager(gesuchModelManager: GesuchModelManager, $stateParams: IAngebotStateParams, $q: IQService,
                                      $log: ILogService): IPromise<TSGesuch> {
    if ($stateParams) {
        const gesuchIdParam = $stateParams.gesuchId;
        if (gesuchIdParam) {
            if (!gesuchModelManager.getGesuch() || gesuchModelManager.getGesuch() && gesuchModelManager.getGesuch().id !== gesuchIdParam
                || gesuchModelManager.getGesuch().emptyCopy) {
                // Wenn die antrags id im GescuchModelManager nicht mit der GesuchId ueberreinstimmt wird das gesuch neu geladen
                // Ebenfalls soll das Gesuch immer neu geladen werden, wenn es sich beim Gesuch im Gesuchmodelmanager um eine leere Mutation handelt
                // oder um ein leeres Erneuerungsgesuch

                return gesuchModelManager.openGesuch(gesuchIdParam);
            } else {
                return $q.resolve(gesuchModelManager.getGesuch());
            }

        }
    }

    $log.warn('keine stateParams oder keine gesuchId, gebe undefined zurueck');

    return $q.resolve(undefined);
}

resetGesuchModelManager.$inject = ['GesuchModelManager'];

export function resetGesuchModelManager(gesuchModelManager: GesuchModelManager): IPromise<TSGesuch> {
    if (gesuchModelManager.getGesuch()) {
        gesuchModelManager.setGesuch(undefined);
    }
    return Promise.resolve(gesuchModelManager.getGesuch());
}

