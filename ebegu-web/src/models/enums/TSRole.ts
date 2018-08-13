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

export enum TSRole {
    SUPER_ADMIN = 'SUPER_ADMIN',
    ADMIN = 'ADMIN',
    SACHBEARBEITER_JA = 'SACHBEARBEITER_JA',
    SACHBEARBEITER_INSTITUTION = 'SACHBEARBEITER_INSTITUTION',
    SACHBEARBEITER_TRAEGERSCHAFT = 'SACHBEARBEITER_TRAEGERSCHAFT',
    GESUCHSTELLER = 'GESUCHSTELLER',
    JURIST = 'JURIST',
    REVISOR = 'REVISOR',
    STEUERAMT = 'STEUERAMT',
    ADMINISTRATOR_SCHULAMT = 'ADMINISTRATOR_SCHULAMT',
    SCHULAMT = 'SCHULAMT',
    ANONYMOUS = 'ANONYMOUS',
}

export function getTSRoleValues(): Array<TSRole> {
    return Object.values(TSRole);
}

export function getTSRoleValuesWithoutSuperAdmin(): Array<TSRole> {
    return getTSRoleValues().filter(value => value !== TSRole.SUPER_ADMIN);
}

export function rolePrefix(): string {
    return 'TSRole_';
}
