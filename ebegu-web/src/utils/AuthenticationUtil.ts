/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
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

import {StateService, TargetState, TransitionPromise} from '@uirouter/core';
import {TSRole} from '../models/enums/TSRole';

/**
 *  Navigiert basierend auf der Rolle zu einer anderen Startseite
 */
export function navigateToStartPageForRoleWithParams(currentRole: TSRole, $state: StateService, params: any): TransitionPromise {
    return $state.go(getRoleBasedTargetState(currentRole, $state).$state(), params);
}

export function navigateToStartPageForRole(currentRole: TSRole, $state: StateService): TransitionPromise {
    return $state.go(getRoleBasedTargetState(currentRole, $state).$state());
}

export function getRoleBasedTargetState(currentRole: TSRole, $state: StateService): TargetState {
    const faelle = 'faelle.list';
    const pendenzen = 'pendenzen.list-view';
    const pendenzenBetreuung = 'pendenzenBetreuungen.list-view';

    const stateByRole: { [key in TSRole]: string } = {
        [TSRole.SUPER_ADMIN]: faelle,
        [TSRole.ADMIN_BG]: pendenzen,
        [TSRole.SACHBEARBEITER_BG]: pendenzen,
        [TSRole.ADMIN_GEMEINDE]: pendenzen,
        [TSRole.SACHBEARBEITER_GEMEINDE]: pendenzen,
        [TSRole.ADMIN_INSTITUTION]: pendenzenBetreuung,
        [TSRole.SACHBEARBEITER_INSTITUTION]: pendenzenBetreuung,
        [TSRole.ADMIN_TRAEGERSCHAFT]: pendenzenBetreuung,
        [TSRole.SACHBEARBEITER_TRAEGERSCHAFT]: pendenzenBetreuung,
        [TSRole.GESUCHSTELLER]: 'gesuchsteller.dashboard',
        [TSRole.JURIST]: faelle,
        [TSRole.REVISOR]: faelle,
        [TSRole.STEUERAMT]: 'pendenzenSteueramt.list-view',
        [TSRole.ADMIN_TS]: pendenzen,
        [TSRole.SACHBEARBEITER_TS]: pendenzen,
        [TSRole.ADMIN_MANDANT]: faelle,
        [TSRole.SACHBEARBEITER_MANDANT]: faelle,
        [TSRole.ANONYMOUS]: 'onboarding.anmeldung',
        [TSRole.ADMIN_SOZIALDIENST]: 'pendenzen',
        [TSRole.SACHBEARBEITER_SOZIALDIENST]: 'pendenzen',
        [TSRole.ADMIN_FERIENBETREUUNG]: 'GEMEINDE_ANTRAEGE',
        [TSRole.SACHBEARBEITER_FERIENBETREUUNG]: 'GEMEINDE_ANTRAEGE'
    };

    return $state.target(stateByRole[currentRole]);
}

export function returnToOriginalState($state: StateService, returnTo: TargetState): TransitionPromise {
    const state = returnTo.state();
    const params = returnTo.params();
    const options = {...returnTo.options(), ...{reload: true}};

    return $state.go(state, params, options);
}
