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

import {HookResult, Transition, TransitionService} from '@uirouter/core';
import {map, take} from 'rxjs/operators';
import {LogFactory} from '../../../app/core/logging/LogFactory';
import {KiBonMandant, MandantService} from '../../../app/shared/services/mandant.service';
import {OnBeforePriorities} from './onBeforePriorities';

const LOG = LogFactory.createLog('authenticationHookRunBlockX');

/**
 * This file contains a Transition Hook which protects a
 * route that requires authentication.
 *
 * This hook redirects to /login when both:
 * - The user is not authenticated
 * - The user is navigating to a state that requires authentication
 */

export function mandantCheck($transitions: TransitionService, mandantService: MandantService): void {
    // Register the "requires authentication" hook with the TransitionsService
    $transitions.onBefore({}, transition => redirectToMandantSelection(transition, mandantService), {priority: OnBeforePriorities.AUTHENTICATION});
}

// Function that returns a redirect for the current transition to the login state
// if the user is not currently authenticated (according to the AuthService)
function redirectToMandantSelection(transition: Transition, authService: MandantService): HookResult {

    return authService.mandant$
        .pipe(
            take(1),
            map(mandant => {
                LOG.debug('checking mandant', mandant);

                if (mandant === KiBonMandant.NONE) {
                    console.log('redirecting to mandant selection');

                    // return $state.target('mandant.selection', undefined, {location: false});
                }

                // continue the original transition
                return true;
            }),
        )
        .toPromise();
}
