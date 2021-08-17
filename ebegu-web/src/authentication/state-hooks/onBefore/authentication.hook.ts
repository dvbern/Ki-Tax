/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {HookMatchCriteria, HookResult, Transition, TransitionService} from '@uirouter/core';
import {map, take} from 'rxjs/operators';
import {LogFactory} from '../../../app/core/logging/LogFactory';
import {TSRole} from '../../../models/enums/TSRole';
import {AuthServiceRS} from '../../service/AuthServiceRS.rest';
import {OnBeforePriorities} from './onBeforePriorities';

const LOG = LogFactory.createLog('authenticationHookRunBlock');

/**
 * This file contains a Transition Hook which protects a
 * route that requires authentication.
 *
 * This hook redirects to /login when both:
 * - The user is not authenticated
 * - The user is navigating to a state that requires authentication
 */
authenticationHookRunBlock.$inject = ['$transitions'];

export function authenticationHookRunBlock($transitions: TransitionService): void {
    // Matches all states except those that have TSRole.ANONYMOUS in data.roles.
    const requiresAuthCriteria: HookMatchCriteria = {
        to: state => state.data && Array.isArray(state.data.roles) && !state.data.roles.includes(TSRole.ANONYMOUS),
    };

    // Register the "requires authentication" hook with the TransitionsService
    $transitions.onBefore(requiresAuthCriteria, redirectToLogin, {priority: OnBeforePriorities.AUTHENTICATION});
}

// Function that returns a redirect for the current transition to the login state
// if the user is not currently authenticated (according to the AuthService)
function redirectToLogin(transition: Transition): HookResult {
    const authService: AuthServiceRS = transition.injector().get('AuthServiceRS');
    const $state = transition.router.stateService;

    return authService.principal$
    .pipe(
        take(1),
        map(principal => {
            LOG.debug('checking authentication of principal', principal);

            if (!principal) {
                LOG.debug('redirecting to login page');

                return $state.target('authentication.login', undefined, {location: false});
            }

            // continue the original transition
            return true;
        }),
    )
    .toPromise();
}
