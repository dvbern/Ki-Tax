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

import {Permission} from '../app/authorisation/Permission';
import {PERMISSIONS} from '../app/authorisation/Permissions';
import {getTSRoleValues, getTSRoleValuesWithoutSuperAdmin, rolePrefix, TSRole} from '../models/enums/TSRole';

/**
 * Hier findet man unterschiedliche Hilfsmethoden, um die Rollen von TSRole zu holen
 */
export class TSRoleUtil {

    public static getAllRolesButGesuchsteller(): Array<TSRole> {
        return TSRoleUtil.getAllRolesButAnonymous().filter(element => element !== TSRole.GESUCHSTELLER);
    }

    public static getAllRolesButSchulamtAndSuperAdmin(): Array<TSRole> {
        return getTSRoleValuesWithoutSuperAdmin()
            .filter(role => !TSRoleUtil.getSchulamtOnlyRoles().includes(role));
    }

    public static getAllRolesButSchulamtAndSuperAdminAndAnonymous(): Array<TSRole> {
        return this.getAllRolesButSchulamtAndSuperAdmin()
            .filter(role => role !== TSRole.ANONYMOUS);
    }

    public static getAllRolesForMenuAlleVerfuegungen(): Array<TSRole> {
        return TSRoleUtil.getAllRolesButAnonymous()
            .filter(element => element !== TSRole.SACHBEARBEITER_TS
                && element !== TSRole.ADMIN_TS
                && element !== TSRole.STEUERAMT);
    }

    public static getAllRolesForMenuAlleFaelle(): Array<TSRole> {
        return TSRoleUtil.getAllRolesButAnonymous()
            .filter(element => element !== TSRole.GESUCHSTELLER && element !== TSRole.STEUERAMT);
    }

    public static getAllRolesForZahlungen(): Array<TSRole> {
        return [
            TSRole.ADMIN_BG,
            TSRole.SUPER_ADMIN,
            TSRole.SACHBEARBEITER_BG,
            TSRole.ADMIN_GEMEINDE,
            TSRole.SACHBEARBEITER_GEMEINDE,
            TSRole.ADMIN_INSTITUTION,
            TSRole.SACHBEARBEITER_INSTITUTION,
            TSRole.ADMIN_TRAEGERSCHAFT,
            TSRole.SACHBEARBEITER_TRAEGERSCHAFT,
            TSRole.REVISOR,
            TSRole.JURIST,
            TSRole.ADMIN_MANDANT,
            TSRole.SACHBEARBEITER_MANDANT,
        ];
    }

    public static getAllRolesForStatistik(): Array<TSRole> {
        return [
            TSRole.ADMIN_BG,
            TSRole.SUPER_ADMIN,
            TSRole.SACHBEARBEITER_BG,
            TSRole.ADMIN_GEMEINDE,
            TSRole.SACHBEARBEITER_GEMEINDE,
            TSRole.ADMIN_TRAEGERSCHAFT,
            TSRole.SACHBEARBEITER_TRAEGERSCHAFT,
            TSRole.ADMIN_INSTITUTION,
            TSRole.SACHBEARBEITER_INSTITUTION,
            TSRole.ADMIN_TS,
            TSRole.SACHBEARBEITER_TS,
            TSRole.REVISOR,
            TSRole.ADMIN_MANDANT,
            TSRole.SACHBEARBEITER_MANDANT,
        ];
    }

    public static getAllRoles(): Array<TSRole> {
        return getTSRoleValues();
    }

    public static getAllRolesButAnonymous(): Array<TSRole> {
        return getTSRoleValues().filter(role => role !== TSRole.ANONYMOUS);
    }

    public static getSuperAdminRoles(): Array<TSRole> {
        return [TSRole.SUPER_ADMIN];
    }

    public static getAdministratorRoles(): Array<TSRole> {
        return [TSRole.SUPER_ADMIN, TSRole.ADMIN_BG, TSRole.ADMIN_GEMEINDE, TSRole.ADMIN_TS, TSRole.ADMIN_MANDANT];
    }

    public static getSchulamtAdministratorRoles(): Array<TSRole> {
        return [TSRole.SUPER_ADMIN, TSRole.ADMIN_TS, TSRole.ADMIN_GEMEINDE];
    }

    public static getJAAdministratorRoles(): Array<TSRole> {
        return [TSRole.SUPER_ADMIN, TSRole.ADMIN_BG, TSRole.ADMIN_GEMEINDE];
    }

    public static getTraegerschaftInstitutionRoles(): Array<TSRole> {
        return [
            TSRole.SUPER_ADMIN,
            TSRole.ADMIN_INSTITUTION,
            TSRole.SACHBEARBEITER_INSTITUTION,
            TSRole.ADMIN_TRAEGERSCHAFT,
            TSRole.SACHBEARBEITER_TRAEGERSCHAFT,
        ];
    }

    public static getTraegerschaftInstitutionSchulamtRoles(): Array<TSRole> {
        return [
            TSRole.SUPER_ADMIN,
            TSRole.ADMIN_INSTITUTION,
            TSRole.SACHBEARBEITER_INSTITUTION,
            TSRole.ADMIN_TRAEGERSCHAFT,
            TSRole.SACHBEARBEITER_TRAEGERSCHAFT,
            TSRole.SACHBEARBEITER_TS,
            TSRole.ADMIN_TS,
            TSRole.ADMIN_GEMEINDE,
        ];
    }

    public static getTraegerschaftRoles(): Array<TSRole> {
        return [TSRole.SUPER_ADMIN, TSRole.ADMIN_TRAEGERSCHAFT, TSRole.SACHBEARBEITER_TRAEGERSCHAFT];
    }

    public static getTraegerschaftInstitutionSteueramtOnlyRoles(): Array<TSRole> {
        return [
            TSRole.ADMIN_INSTITUTION,
            TSRole.SACHBEARBEITER_INSTITUTION,
            TSRole.ADMIN_TRAEGERSCHAFT,
            TSRole.SACHBEARBEITER_TRAEGERSCHAFT,
            TSRole.STEUERAMT,
        ];
    }

    public static getGesuchstellerJugendamtRoles(): Array<TSRole> {
        return [
            TSRole.SUPER_ADMIN,
            TSRole.GESUCHSTELLER,
            TSRole.SACHBEARBEITER_BG,
            TSRole.ADMIN_BG,
            TSRole.ADMIN_GEMEINDE,
            TSRole.SACHBEARBEITER_GEMEINDE,
        ];
    }

    public static getGesuchstellerJugendamtSchulamtRoles(): Array<TSRole> {
        return [
            TSRole.SUPER_ADMIN,
            TSRole.GESUCHSTELLER,
            TSRole.SACHBEARBEITER_BG,
            TSRole.ADMIN_BG,
            TSRole.ADMIN_GEMEINDE,
            TSRole.SACHBEARBEITER_GEMEINDE,
            TSRole.SACHBEARBEITER_TS,
            TSRole.ADMIN_TS,
        ];
    }

    public static getAdministratorJugendamtRole(): Array<TSRole> {
        return [
            TSRole.SUPER_ADMIN,
            TSRole.ADMIN_BG,
            TSRole.SACHBEARBEITER_BG,
            TSRole.ADMIN_GEMEINDE,
            TSRole.SACHBEARBEITER_GEMEINDE,
        ];
    }

    public static getAdministratorBgTsGemeindeRole(): Array<TSRole> {
        return [
            TSRole.SUPER_ADMIN,
            TSRole.ADMIN_BG,
            TSRole.ADMIN_TS,
            TSRole.ADMIN_GEMEINDE,
        ];
    }

    public static getAdministratorOrAmtRole(): Array<TSRole> {
        return [
            TSRole.SUPER_ADMIN,
            TSRole.ADMIN_BG,
            TSRole.SACHBEARBEITER_BG,
            TSRole.ADMIN_GEMEINDE,
            TSRole.SACHBEARBEITER_GEMEINDE,
            TSRole.ADMIN_TS,
            TSRole.SACHBEARBEITER_TS,
        ];
    }

    public static getAdministratorJugendamtSchulamtRoles(): Array<TSRole> {
        return TSRoleUtil.getAdministratorOrAmtRole();
    }

    public static getAdministratorRevisorRole(): Array<TSRole> {
        return [
            TSRole.SUPER_ADMIN,
            TSRole.ADMIN_BG,
            TSRole.ADMIN_GEMEINDE,
            TSRole.ADMIN_TS,
            TSRole.REVISOR,
            TSRole.ADMIN_MANDANT,
        ];
    }

    public static getAdministratorMandantRevisorRole(): Array<TSRole> {
        return [
            TSRole.SUPER_ADMIN,
            TSRole.ADMIN_BG,
            TSRole.ADMIN_GEMEINDE,
            TSRole.ADMIN_TS,
            TSRole.REVISOR,
            TSRole.ADMIN_MANDANT,
            TSRole.SACHBEARBEITER_MANDANT,
        ];
    }

    public static getAllAdministratorRevisorRole(): Array<TSRole> {
        return [
            TSRole.SUPER_ADMIN,
            TSRole.ADMIN_BG,
            TSRole.ADMIN_GEMEINDE,
            TSRole.ADMIN_TS,
            TSRole.REVISOR,
            TSRole.ADMIN_MANDANT,
            TSRole.ADMIN_INSTITUTION,
            TSRole.ADMIN_TRAEGERSCHAFT,
        ];
    }

    public static getAllAdminRoles(): Array<TSRole> {
        return [
            TSRole.SUPER_ADMIN,
            TSRole.ADMIN_BG,
            TSRole.ADMIN_GEMEINDE,
            TSRole.ADMIN_TS,
            TSRole.ADMIN_MANDANT,
            TSRole.ADMIN_INSTITUTION,
            TSRole.ADMIN_TRAEGERSCHAFT,
        ];
    }

    public static getGesuchstellerJugendamtSchulamtOtherAmtRoles(): Array<TSRole> {
        return [
            TSRole.SUPER_ADMIN,
            TSRole.ADMIN_BG,
            TSRole.SACHBEARBEITER_BG,
            TSRole.ADMIN_GEMEINDE,
            TSRole.SACHBEARBEITER_GEMEINDE,
            TSRole.JURIST,
            TSRole.REVISOR,
            TSRole.ADMIN_MANDANT,
            TSRole.SACHBEARBEITER_MANDANT,
            TSRole.GESUCHSTELLER,
            TSRole.ADMIN_TS,
            TSRole.SACHBEARBEITER_TS,
        ];
    }

    public static getGesuchstellerJugendamtOtherAmtRoles(): Array<TSRole> {
        return [
            TSRole.SUPER_ADMIN,
            TSRole.ADMIN_BG,
            TSRole.SACHBEARBEITER_BG,
            TSRole.ADMIN_GEMEINDE,
            TSRole.SACHBEARBEITER_GEMEINDE,
            TSRole.JURIST,
            TSRole.REVISOR,
            TSRole.ADMIN_MANDANT,
            TSRole.SACHBEARBEITER_MANDANT,
            TSRole.GESUCHSTELLER,
        ];
    }

    public static getMandantRoles(): Array<TSRole> {
        return [TSRole.SUPER_ADMIN, TSRole.ADMIN_MANDANT, TSRole.SACHBEARBEITER_MANDANT];
    }

    public static getJugendamtAndSchulamtRole(): Array<TSRole> {
        return [
            TSRole.SUPER_ADMIN,
            TSRole.ADMIN_BG,
            TSRole.SACHBEARBEITER_BG,
            TSRole.ADMIN_GEMEINDE,
            TSRole.SACHBEARBEITER_GEMEINDE,
            TSRole.JURIST,
            TSRole.REVISOR,
            TSRole.ADMIN_MANDANT,
            TSRole.SACHBEARBEITER_MANDANT,
            TSRole.ADMIN_TS,
            TSRole.SACHBEARBEITER_TS,
        ];
    }

    public static getAdministratorJugendamtSchulamtSteueramtRoles(): Array<TSRole> {
        return [
            TSRole.SUPER_ADMIN,
            TSRole.ADMIN_BG,
            TSRole.SACHBEARBEITER_BG,
            TSRole.ADMIN_GEMEINDE,
            TSRole.SACHBEARBEITER_GEMEINDE,
            TSRole.ADMIN_TS,
            TSRole.SACHBEARBEITER_TS,
            TSRole.STEUERAMT,
        ];
    }

    public static getAdministratorJugendamtSchulamtGesuchstellerRoles(): Array<TSRole> {
        return [
            TSRole.SUPER_ADMIN,
            TSRole.ADMIN_BG,
            TSRole.SACHBEARBEITER_BG,
            TSRole.ADMIN_GEMEINDE,
            TSRole.SACHBEARBEITER_GEMEINDE,
            TSRole.ADMIN_TS,
            TSRole.SACHBEARBEITER_TS,
            TSRole.GESUCHSTELLER,
        ];
    }

    public static getAllButAdministratorJugendamtRole(): Array<TSRole> {
        return TSRoleUtil.getAllRolesButAnonymous()
            .filter(element =>
                element !== TSRole.SACHBEARBEITER_BG &&
                element !== TSRole.ADMIN_BG &&
                element !== TSRole.SACHBEARBEITER_GEMEINDE &&
                element !== TSRole.ADMIN_GEMEINDE &&
                element !== TSRole.SUPER_ADMIN,
            );
    }

    public static getAllButAdministratorAmtRole(): Array<TSRole> {
        return getTSRoleValues()
            .filter(element =>
                element !== TSRole.SACHBEARBEITER_BG &&
                element !== TSRole.ADMIN_BG &&
                element !== TSRole.SACHBEARBEITER_GEMEINDE &&
                element !== TSRole.ADMIN_GEMEINDE &&
                element !== TSRole.SACHBEARBEITER_TS &&
                element !== TSRole.ADMIN_TS &&
                element !== TSRole.SUPER_ADMIN,
            );
    }

    public static getAllRolesButTraegerschaftInstitution(): Array<TSRole> {
        return TSRoleUtil.getAllRolesButAnonymous()
            .filter(element =>
                element !== TSRole.ADMIN_INSTITUTION
                && element !== TSRole.SACHBEARBEITER_INSTITUTION
                && element !== TSRole.ADMIN_TRAEGERSCHAFT
                && element !== TSRole.SACHBEARBEITER_TRAEGERSCHAFT,
            );
    }

    public static getAllRolesButTraegerschaftInstitutionSteueramt(): Array<TSRole> {
        return TSRoleUtil.getAllRolesButAnonymous()
            .filter(element =>
                element !== TSRole.ADMIN_INSTITUTION
                && element !== TSRole.SACHBEARBEITER_INSTITUTION
                && element !== TSRole.ADMIN_TRAEGERSCHAFT
                && element !== TSRole.SACHBEARBEITER_TRAEGERSCHAFT
                && element !== TSRole.STEUERAMT,
            );
    }

    public static getAllRolesButSteueramt(): Array<TSRole> {
        return TSRoleUtil.getAllRolesButAnonymous().filter(element => element !== TSRole.STEUERAMT);
    }

    public static getSchulamtRoles(): Array<TSRole> {
        return [
            TSRole.SUPER_ADMIN,
            TSRole.SACHBEARBEITER_TS,
            TSRole.ADMIN_TS,
            TSRole.ADMIN_GEMEINDE,
            TSRole.SACHBEARBEITER_GEMEINDE,
        ];
    }

    public static getSchulamtOnlyRoles(): Array<TSRole> {
        return [TSRole.SACHBEARBEITER_TS, TSRole.ADMIN_TS];
    }

    public static getGesuchstellerRoles(): Array<TSRole> {
        return [TSRole.SUPER_ADMIN, TSRole.GESUCHSTELLER];
    }

    public static getGesuchstellerOnlyRoles(): Array<TSRole> {
        return [TSRole.GESUCHSTELLER];
    }

    public static getSteueramtOnlyRoles(): Array<TSRole> {
        return [TSRole.STEUERAMT];
    }

    public static getSteueramtRoles(): Array<TSRole> {
        return [TSRole.SUPER_ADMIN, TSRole.STEUERAMT];
    }

    public static getReadOnlyRoles(): Array<TSRole> {
        return [TSRole.REVISOR, TSRole.JURIST, TSRole.STEUERAMT, TSRole.ADMIN_MANDANT, TSRole.SACHBEARBEITER_MANDANT];
    }

    public static getAllRolesForKommentarSpalte(): Array<TSRole> {
        return [
            TSRole.SUPER_ADMIN,
            TSRole.ADMIN_BG,
            TSRole.SACHBEARBEITER_BG,
            TSRole.ADMIN_GEMEINDE,
            TSRole.SACHBEARBEITER_GEMEINDE,
            TSRole.STEUERAMT,
            TSRole.ADMIN_TS,
            TSRole.SACHBEARBEITER_TS,
            TSRole.JURIST,
            TSRole.REVISOR,
            TSRole.ADMIN_MANDANT,
            TSRole.SACHBEARBEITER_MANDANT,
        ];
    }

    public static getGemeindeRoles(): TSRole[] {
        return PERMISSIONS[Permission.ROLE_GEMEINDE].concat(TSRole.SUPER_ADMIN);
    }

    public static getTraegerschaftInstitutionOnlyRoles(): TSRole[] {
        return PERMISSIONS[Permission.ROLE_INSTITUTION].concat(PERMISSIONS[Permission.ROLE_TRAEGERSCHAFT]);
    }

    public static isGemeindeRole(role: TSRole): boolean {
        return PERMISSIONS[Permission.ROLE_GEMEINDE].includes(role);
    }

    public static isInstitutionRole(role: TSRole): boolean {
        return PERMISSIONS[Permission.ROLE_INSTITUTION].includes(role);
    }

    public static isTraegerschaftRole(role: TSRole): boolean {
        return PERMISSIONS[Permission.ROLE_TRAEGERSCHAFT].includes(role);
    }

    public static translationKeyForRole(role: TSRole, gesuchstellerNone: boolean = false): string {
        return role === TSRole.GESUCHSTELLER && gesuchstellerNone ? rolePrefix() + 'NONE' : rolePrefix() + role;
    }
}
