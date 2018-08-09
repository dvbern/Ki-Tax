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

import {HookMatchCriteria, HookResult, Transition, TransitionService} from '@uirouter/core';
import {map, take} from 'rxjs/operators';
import {hasFromState} from '../dvbModules/router/route-helper-provider';
import {TSRole} from '../models/enums/TSRole';
import {getRoleBasedTargetState} from '../utils/AuthenticationUtil';
import {OnBeforePriorities} from './onBeforePriorities';
import AuthServiceRS from './service/AuthServiceRS.rest';

/**
 * This file contains a Transition Hook which protects a
 * route that requires authorisation.
 *
 * This hook aborts a transition when the user does not have a required role
 */
authorisationHookRunBlock.$inject = ['$transitions'];

export function authorisationHookRunBlock($transitions: TransitionService) {
    // Matches if the destination state has a data.roles array
    const requiresAuthCriteria: HookMatchCriteria = {
        to: (state) => {
            return state.data && Array.isArray(state.data.roles);
        },
    };

    // Register the "requires authorisation" hook with the TransitionsService.
    // The priority is lower than the priority of the authentication hook (thus it runs later).
    $transitions.onBefore(requiresAuthCriteria, abortWhenUnauthorised, {priority: OnBeforePriorities.AUTHORISATION});
}

function abortWhenUnauthorised(transition: Transition): HookResult {
    const authService: AuthServiceRS = transition.injector().get('AuthServiceRS');

    return authService.principal$
        .pipe(
            take(1),
            map(principal => {
                const allowedRoles: TSRole[] = transition.to().data.roles;

                if (!principal) {
                    // since we don't have a principal, the state may be accessed only when it allows ANONYMOUS users
                    if (allowedRoles.includes(TSRole.ANONYMOUS)) {
                        // ANONYMOUS access granted
                        return true;
                    }

                    // no principal and not allowed to access the target state: redirect to default ANONYMOUS state
                    return getRoleBasedTargetState(TSRole.ANONYMOUS, transition.router.stateService);
                }

                const currentRole = principal.getCurrentRole();
                if (allowedRoles.includes(currentRole)) {
                    // the principal has one of the required roles -> access granted
                    return true;
                }

                if (!hasFromState(transition)) {
                    // the principal is not allowed to access the state. Since it is not yet on any state, navigate to a role-based landing state
                    return getRoleBasedTargetState(currentRole, transition.router.stateService);
                }

                if (transition.from().name === 'authentication.locallogin') {
                    // when changing the user via locallogin and the selected user is not allowed to return to the previous state, navigate to the role-based landing state
                    return getRoleBasedTargetState(currentRole, transition.router.stateService);
                }

                // the principal is not allowed to access the state. Show an error and abort the transition (and stay on the current state)
                const errorService = transition.injector().get('ErrorService');
                errorService.addMesageAsError('ERROR_UNAUTHORIZED');

                return false;
            })
        )
        .toPromise();
}
