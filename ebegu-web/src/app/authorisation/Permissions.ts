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

import {TSRole} from '../../models/enums/TSRole';
import {Permission} from './Permission';

export const PERMISSIONS: { [k in Permission]: ReadonlyArray<TSRole> } = {
    [Permission.BENUTZER_EINLADEN]: [
        TSRole.SUPER_ADMIN,
        TSRole.ADMIN_MANDANT,
        TSRole.ADMIN_GEMEINDE,
        TSRole.ADMIN_BG,
        TSRole.ADMIN_TS,
        TSRole.ADMIN_INSTITUTION,
        TSRole.ADMIN_TRAEGERSCHAFT,
    ],
    [Permission.ROLE_TRAEGERSCHAFT]: [
        TSRole.ADMIN_TRAEGERSCHAFT,
        TSRole.SACHBEARBEITER_TRAEGERSCHAFT,
    ],
    [Permission.ROLE_INSTITUTION]: [
        TSRole.ADMIN_INSTITUTION,
        TSRole.SACHBEARBEITER_INSTITUTION,
    ],
    [Permission.ROLE_GEMEINDE]: [
        TSRole.ADMIN_GEMEINDE,
        TSRole.SACHBEARBEITER_GEMEINDE,
        TSRole.ADMIN_BG,
        TSRole.SACHBEARBEITER_BG,
        TSRole.ADMIN_TS,
        TSRole.SACHBEARBEITER_TS,
        TSRole.JURIST,
        TSRole.REVISOR,
        TSRole.STEUERAMT,
    ],
    [Permission.BENUTZER_EINLADEN_AS_GEMEINDE]: [
        TSRole.ADMIN_GEMEINDE,
        TSRole.SACHBEARBEITER_GEMEINDE,
        TSRole.ADMIN_BG,
        TSRole.SACHBEARBEITER_BG,
        TSRole.ADMIN_TS,
        TSRole.SACHBEARBEITER_TS,
        TSRole.JURIST,
        TSRole.REVISOR,
        TSRole.STEUERAMT,
        TSRole.ADMIN_INSTITUTION,
        TSRole.SACHBEARBEITER_INSTITUTION,
    ],
    [Permission.ROLE_BG]: [
        // todo This Permission should be removed in KIBON version 2. The permission Permission.ROLE_GEMEINDE should be
        // used instead
        TSRole.ADMIN_BG,
        TSRole.SACHBEARBEITER_BG,
        TSRole.JURIST,
        TSRole.REVISOR,
        TSRole.STEUERAMT,
    ],
    [Permission.ROLE_TS]: [
        // todo This Permission should be removed in KIBON version 2. The permission Permission.ROLE_GEMEINDE should be
        // used instead
        TSRole.ADMIN_TS,
        TSRole.SACHBEARBEITER_TS,
        TSRole.JURIST,
        TSRole.REVISOR,
        TSRole.STEUERAMT,
    ],
    [Permission.ROLE_MANDANT]: [
        TSRole.ADMIN_MANDANT,
        TSRole.SACHBEARBEITER_MANDANT,
    ],
    [Permission.ROLE_SOZIALDIENST]: [
        TSRole.ADMIN_SOZIALDIENST,
        TSRole.SACHBEARBEITER_SOZIALDIENST,
    ]
};
