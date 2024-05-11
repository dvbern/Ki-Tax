/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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

import {TransitionService} from '@uirouter/angular';
import {HookMatchCriteria, HookResult, TargetState, Transition} from '@uirouter/core';
import {ApplicationPropertyRS} from '../../app/core/rest-services/applicationPropertyRS.rest';
import {AuthServiceRS} from '../../authentication/service/AuthServiceRS.rest';
import {OnBeforePriorities} from '../../authentication/state-hooks/onBefore/onBeforePriorities';
import {getRoleBasedTargetState} from '../../utils/AuthenticationUtil';
import {EbeguBetreuungAbweichungenState} from '../gesuch.route';

abortWhenAbweichungenNotEnabled.$inject = ['$transitions', 'ApplicationPropertyRS', 'AuthServiceRS'];

export function abweichungenEnabledHook(
    $transitions: TransitionService,
    applicationPropertyRS: ApplicationPropertyRS,
    authService: AuthServiceRS,
): void {
    const navigatesToAbweichungenCriteria: HookMatchCriteria = {
        to: state => state.name === new EbeguBetreuungAbweichungenState().name,
    };

    $transitions.onBefore(navigatesToAbweichungenCriteria,
        transition => abortWhenAbweichungenNotEnabled(transition, applicationPropertyRS, authService),
        {priority: OnBeforePriorities.CONFIGURATION});
}

function abortWhenAbweichungenNotEnabled(transition: Transition, applicationPropertyRS: ApplicationPropertyRS,
                                         authService: AuthServiceRS,
): HookResult {
    return applicationPropertyRS.getPublicPropertiesCached().then(publicProperties => {
        if (publicProperties.abweichungenEnabled) {
            return true;
        }
        return getRoleBasedTargetState(authService.getPrincipalRole(), transition.router.stateService);
    }) as Promise<TargetState | boolean>;
}
