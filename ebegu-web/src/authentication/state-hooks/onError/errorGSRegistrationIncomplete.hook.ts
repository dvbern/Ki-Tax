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

import {
    HookMatchCriteria,
    HookResult,
    RejectType,
    Transition,
    TransitionService
} from '@uirouter/core';
import {DISABLE_RECOVERY_ERROR_MESSAGE} from './errorRecovery.hook';
import {OnErrorPriorities} from './onErrorPriorities';

erorGSRegistrationIncompleteHookRunBlock.$inject = ['$transitions'];

export function erorGSRegistrationIncompleteHookRunBlock(
    $transitions: TransitionService
): void {
    const criteria: HookMatchCriteria = {
        to: 'gesuchsteller.dashboard',
        from: state =>
            state.name !== 'onboarding.gesuchsteller.registration-incomplete'
    };

    $transitions.onError(criteria, onGSRegistrationIncompleteError, {
        priority: OnErrorPriorities.ERROR_GS_REGISTRATION_INCOMPLETE
    });
}

function onGSRegistrationIncompleteError(transition: Transition): HookResult {
    if (transition.isActive() && transition.error().type === RejectType.ERROR) {
        // Not very nice, but could find a good solution: since the original transition errored, all error hooks
        // (matching the criteria) are executed. We might thus recover multiple times (and start multiple transitions).
        transition.error().message = DISABLE_RECOVERY_ERROR_MESSAGE;
        transition.router.stateService.go(
            'onboarding.gesuchsteller.registration-incomplete'
        );
    }
}
