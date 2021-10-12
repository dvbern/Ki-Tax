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

import {HookResult, StateService, Transition, TransitionService} from '@uirouter/core';
import {combineLatest} from 'rxjs';
import {map, take} from 'rxjs/operators';
import {KiBonMandant} from '../../../app/core/constants/MANDANTS';
import {LogFactory} from '../../../app/core/logging/LogFactory';
import {MandantService} from '../../../app/shared/services/mandant.service';
import {OnBeforePriorities} from './onBeforePriorities';

const LOG = LogFactory.createLog('authenticationHookRunBlockX');

mandantCheck.$inject = ['$transitions', 'MandantService', '$state'];

/**
 * This file contains a Transition Hook which protects a
 * route that requires authentication.
 *
 * This hook redirects to /login when both:
 * - The user is not authenticated
 * - The user is navigating to a state that requires authentication
 */

export function mandantCheck(
    $transitions: TransitionService,
): void {
    // Register the "requires authentication" hook with the TransitionsService
    $transitions.onBefore({
            to: state => !state.name.includes('mandant'),
        },
        redirectToMandantSelection,
        {priority: OnBeforePriorities.AUTHENTICATION});
}

// Function that returns a redirect for the current transition to the login state
// if the user is not currently authenticated (according to the AuthService)
function redirectToMandantSelection(
    transition: Transition,
): HookResult {

    const mandantService: MandantService = transition.injector().get('MandantService');
    const $state: StateService = transition.injector().get('$state');

    return combineLatest([
        mandantService.mandant$,
        mandantService.isMultimandantActive$(),
    ])
        .pipe(
            map(([mandant, isMultimandanActive]) => {

                if (!isMultimandanActive) {
                    return true;
                }

                LOG.debug('checking mandant', mandant);
                const path = transition.router.stateService.href(transition.to(), transition.params());

                const mandantFromHostname = mandantService.parseHostnameForMandant();
                if (mandant === KiBonMandant.NONE) {
                    if (mandantFromHostname === KiBonMandant.NONE) {
                        console.log('redirecting to mandant selection');
                        return $state.target('mandant', {path});
                    }
                    mandantService.selectMandant(mandantFromHostname, path);
                    return true;
                }

                if (mandant !== mandantFromHostname) {
                    mandantService.redirectToMandantSubdomain(mandant, path);
                    return false;
                }

                // continue the original transition
                return true;
            }),
            take(1),
        )
        .toPromise();
}
