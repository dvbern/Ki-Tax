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

import {StateService} from '@uirouter/core';
import {TSRole} from '../models/enums/TSRole';
import {TSRoleUtil} from './TSRoleUtil';

export class NavigationUtil {

    public static navigateToStartsiteOfGesuchForRole(role: TSRole, state: StateService, gesuchID: string): void {
        if (TSRoleUtil.getTraegerschaftInstitutionOnlyRoles().includes(role)) {
            state.go('gesuch.betreuungen', {
                gesuchId: gesuchID,
            });
        } else if (role === TSRole.STEUERAMT) {
            state.go('gesuch.familiensituation', {
                gesuchId: gesuchID,
            });
        } else if (TSRoleUtil.isSozialdienstRole(role)) {
            state.go('gesuch.sozialdienstfallcreation', {
                gesuchId: gesuchID,
            });
        } else if (role === TSRole.ADMIN_FERIENBETREUUNG || role === TSRole.SACHBEARBEITER_FERIENBETREUUNG) {
            state.go('gemeindeantrage.view');
        } else {
            state.go('gesuch.fallcreation', {
                gesuchId: gesuchID,
            });
        }
    }
}
