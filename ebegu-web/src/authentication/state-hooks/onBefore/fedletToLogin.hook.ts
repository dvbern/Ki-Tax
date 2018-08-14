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

import {HookMatchCriteria, TransitionService} from '@uirouter/core';
import {OnBeforePriorities} from './onBeforePriorities';

fedletToLoginHookRunBlock.$inject = ['$transitions'];

/**
 * This file contains a Transition Hook which prevents navigataion from the login connector fedlet to
 * the login page (which would then redirect to the connector again).
 *
 * To reproduce:
 * 1. Navigate to the login state, which redirects you to the connector login.
 * 2. Press the browser back button
 * -> you return to the login page, which forwards you again to the connector page
 *
 * This introduces another issue: now you cannot navigate directly to the login url. You get redirected to onboarding.
 */
export function fedletToLoginHookRunBlock($transitions: TransitionService) {
    const navigationToLoginState: HookMatchCriteria = {
        to: 'authentication.login',
        // no state name: we are navigating from somewhere unknown
        from: state => !state.name
    };

    $transitions.onBefore(navigationToLoginState, abort, {priority: OnBeforePriorities.FEDLET_TO_LOGIN});
}

function abort(): boolean {
    return false;
}
