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
    HookResult,
    RejectType,
    Transition,
    TransitionService
} from '@uirouter/core';
import {TSRole} from '../../../models/enums/TSRole';
import {navigateToStartPageForRole} from '../../../utils/AuthenticationUtil';
import {OnErrorPriorities} from './onErrorPriorities';

/**
 * This error recovery hook will ignore any errors with the following message.
 */
export const DISABLE_RECOVERY_ERROR_MESSAGE = 'ignore me. Already handled';

errorRecoveryHookRunBlock.$inject = ['$transitions'];

export function errorRecoveryHookRunBlock(
    $transitions: TransitionService
): void {
    $transitions.onError({from: state => !state || !state.name}, onError, {
        priority: OnErrorPriorities.ERROR_RECOVERY
    });
}

function onError(transition: Transition): HookResult {
    if (
        !transition.isActive() ||
        transition.error().message === DISABLE_RECOVERY_ERROR_MESSAGE
    ) {
        return;
    }

    const errorType = transition.error().type;

    if (errorType === RejectType.ABORTED || errorType === RejectType.ERROR) {
        // we have been blocked by some hook, but we are on no state -> open fallback state
        navigateToStartPageForRole(
            TSRole.ANONYMOUS,
            transition.router.stateService
        );
    }
}
