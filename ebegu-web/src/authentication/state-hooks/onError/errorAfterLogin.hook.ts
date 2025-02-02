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
import {map, mergeMap, take} from 'rxjs/operators';
import {LogFactory} from '../../../app/core/logging/LogFactory';
import {TSRole} from '../../../models/enums/TSRole';
import {navigateToStartPageForRole} from '../../../utils/AuthenticationUtil';
import {AuthServiceRS} from '../../service/AuthServiceRS.rest';
import {OnErrorPriorities} from './onErrorPriorities';

/**
 * This hook navigates to a valid state when the failed transition originated from a login state.
 */
errorAfterLoginHookRunBlock.$inject = ['$transitions'];

const loginStates = [
    'authentication.login',
    'authentication.locallogin',
    'authentication.tutorialInstitutionLogin',
    'authentication.tutorialGemeindeLogin'
];

export function errorAfterLoginHookRunBlock(
    $transitions: TransitionService
): void {
    $transitions.onError(
        {from: state => loginStates.includes(state.name)},
        recover,
        {priority: OnErrorPriorities.ERROR_AFTER_LOGIN}
    );
}

const LOG = LogFactory.createLog('errorAfterLoginHookRunBlock');

function recover(transition: Transition): HookResult {
    LOG.debug(
        'recover from navigation error after login',
        transition.isActive()
    );
    if (!transition.isActive()) {
        return;
    }

    const authService: AuthServiceRS = transition
        .injector()
        .get('AuthServiceRS');

    return authService.principal$
        .pipe(
            take(1),
            map(principal =>
                principal ? principal.getCurrentRole() : TSRole.ANONYMOUS
            ),
            mergeMap(role =>
                navigateToStartPageForRole(role, transition.router.stateService)
            ),
            map(() => true)
        )
        .toPromise();
}
