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

import {HookMatchCriteria, HookResult, StateService, Transition, TransitionService} from '@uirouter/core';
import {map, take} from 'rxjs/operators';
import {TSRole} from '../models/enums/TSRole';
import AuthServiceRS from './service/AuthServiceRS.rest';

/**
 * This file contains a Transition Hook which protects a
 * route that requires authorisation.
 *
 * This hook aborts a transition when the user does not have a required role
 */
authorisationHookRunBlock.$inject = ['$transitions'];

export function authorisationHookRunBlock($transitions: TransitionService) {
    // Matches if the destination state's data property has a truthy 'requiresAuth' property
    const requiresAuthCriteria: HookMatchCriteria = {
        to: (state) => {
            return state.data && Array.isArray(state.data.roles);
        },
    };

    // Register the "requires authorisation" hook with the TransitionsService.
    // The priority is lower than the priority of the authentication hook.
    $transitions.onBefore(requiresAuthCriteria, abortWhenUnauthorised, {priority: 9});
}

function abortWhenUnauthorised(transition: Transition): HookResult {
    const authService: AuthServiceRS = transition.injector().get('AuthServiceRS');

    return authService.principal$
        .pipe(
            take(1),
            map(principal => {
                const allowedRoles: TSRole[] = transition.to().data.roles;

                if (!principal) {
                    // since we don't have a principal, the state may be access only when it allows ANONYMOUS users
                    return allowedRoles.some(role => role === TSRole.ANONYMOUS);
                }

                return allowedRoles.some(role => role === principal.getCurrentRole());
            }),
            map(isAuthorised => {
                if (!isAuthorised && (!transition.from() || transition.from().name === '')) {
                    // redirect to an allowed state
                    const $state: StateService = transition.router.stateService;

                    return $state.target('authentication.start');
                }

                return isAuthorised;
            })
        )
        .toPromise();
}
