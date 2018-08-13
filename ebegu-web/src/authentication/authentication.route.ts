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
import {TargetState, Transition} from '@uirouter/core';
import {RouterHelper} from '../dvbModules/router/route-helper-provider';
import {getTSRoleValues, TSRole} from '../models/enums/TSRole';
import {getRoleBasedTargetState} from '../utils/AuthenticationUtil';

authenticationRoutes.$inject = ['RouterHelper'];

export function authenticationRoutes(routerHelper: RouterHelper) {
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
        url: '/login?type',
        resolve: {
            returnTo: returnTo
        },
        data: {
            roles: getTSRoleValues()
        }
    },
    {

        name: 'authentication.schulung',
        template: '<dv-schulung flex="auto" class="overflow-scroll">',
        url: '/schulung',
        data: {
            roles: getTSRoleValues(),
            requiresDummyLogin: true,
        }
    }
];

export class IAuthenticationStateParams {
    type: string;
}

/**
 * A resolve function for 'login' state which figures out what state to return to, after a successful login.
 *
 * If the user was initially redirected to login state (due to the requiresAuth redirect), then return the toState/params
 * they were redirected from.
 * Otherwise, if they transitioned directly, return the fromState/params.
 * Otherwise return the main "home" state.
 */
returnTo.$inject = ['$transition$'];

export function returnTo($transition$: Transition): TargetState {
    if ($transition$.redirectedFrom() != null) {
        // The user was redirected to the login state (e.g., via the requiresAuth hook when trying to activate contacts)
        // Return to the original attempted target state (e.g., contacts)
        return $transition$.redirectedFrom().targetState();
    }

    const $state = $transition$.router.stateService;

    // The user was not redirected to the login state; they directly activated the login state somehow.
    // Return them to the state they came from.
    const prohibitetReturnStates = ['', 'authentication.login', 'authentication.locallogin'];
    if (!prohibitetReturnStates.includes($transition$.from().name)) {
        return $state.target($transition$.from(), $transition$.params('from'));
    }

    // If the fromState's name is empty, then this was the initial transition. Just return them to the default ANONYMOUS state
    return getRoleBasedTargetState(TSRole.ANONYMOUS, $state);
}
