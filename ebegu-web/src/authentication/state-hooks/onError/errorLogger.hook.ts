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

import {HookResult, RejectType, Transition, TransitionService} from '@uirouter/core';
import {LogFactory} from '../../../app/core/logging/LogFactory';
import {onErrorPriorities} from './onErrorPriorities';

const LOG = LogFactory.createLog('errorLoggerHookRunBlock');

errorLoggerHookRunBlock.$inject = ['$transitions'];

export function errorLoggerHookRunBlock($transitions: TransitionService): void {
    $transitions.onError({}, onError, {priority: onErrorPriorities.ERROR_LOGGER});
}

function onError(transition: Transition): HookResult {
    if (transition.error().type !== RejectType.SUPERSEDED) {
        LOG.error('Fehler beim Navigieren', transition);
    }
}
