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

import {HookMatchCriteria, StateService, TransitionHookFn, TransitionService} from '@uirouter/core';
import {map, take} from 'rxjs/operators';
import {LogFactory} from '../app/core/logging/LogFactory';
import EbeguUtil from '../utils/EbeguUtil';
import AuthServiceRS from './service/AuthServiceRS.rest';

/**
 * This file contains a Transition Hook which protects a
 * route that requires authentication.
 *
 * This hook redirects to /login when both:
 * - The user is not authenticated
 * - The user is navigating to a state that requires authentication
 */
authHookRunBlock.$inject = ['$transitions'];

export function authHookRunBlock($transitions: TransitionService) {
    // Matches if the destination state's data property has a truthy 'requiresAuth' property
    const requiresAuthCriteria: HookMatchCriteria = {
        to: (state) => {
            return EbeguUtil.isNullOrUndefined(state.data) || !state.data.isPublic;
        },
    };

    // Function that returns a redirect for the current transition to the login state
    // if the user is not currently authenticated (according to the AuthService)
    const redirectToLogin: TransitionHookFn = (transition) => {
        const authService: AuthServiceRS = transition.injector().get('AuthServiceRS');
        const $state: StateService = transition.router.stateService;

        return authService.principal$
            .pipe(
                take(1),
                map(principal => {
                    if (!principal) {
                        const LOG = LogFactory.createLog(authHookRunBlock.name);
                        LOG.info('redirecting to login page');

                        // TODO hefa redirect to authentication.login
                        return $state.target('authentication.locallogin', undefined, {location: false});
                    }

                    return true;
                })
            )
            .toPromise();
    };

    // Register the "requires auth" hook with the TransitionsService
    $transitions.onBefore(requiresAuthCriteria, redirectToLogin, {priority: 1001});
}

