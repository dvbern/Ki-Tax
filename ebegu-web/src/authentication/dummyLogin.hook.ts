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
import {ApplicationPropertyRS} from '../app/core/rest-services/applicationPropertyRS.rest';
import {OnBeforePriorities} from './onBeforePriorities';

dummyLoginHookRunBlock.$inject = ['$transitions'];

/**
 * This file contains a Transition Hook which protects a
 * route that requires DummyLogin.
 *
 * This hook aborts a transition when the the application is not running in dummy mode
 */
export function dummyLoginHookRunBlock($transitions: TransitionService) {
    // Matches all states the require dummy login
    const requiresDummyLogin: HookMatchCriteria = {
        to: (state) => {
            return state.data && state.data.requiresDummyLogin;
        },
    };

    $transitions.onBefore(requiresDummyLogin, checkDummyLogin, {priority: OnBeforePriorities.DUMMY_LOGIN});
}

function checkDummyLogin(transition: Transition): HookResult {
    const applicationPropertyRS: ApplicationPropertyRS = transition.injector().get('ApplicationPropertyRS');

    return applicationPropertyRS.isDummyMode() as Promise<boolean>;
}
