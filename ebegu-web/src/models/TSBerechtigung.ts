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

import {TSRoleUtil} from '../utils/TSRoleUtil';
import {TSAmt} from './enums/TSAmt';
import {TSRole} from './enums/TSRole';
import {TSAbstractDateRangedEntity} from './TSAbstractDateRangedEntity';
import TSGemeinde from './TSGemeinde';
import TSInstitution from './TSInstitution';
import {TSTraegerschaft} from './TSTraegerschaft';
import {TSDateRange} from './types/TSDateRange';

export default class TSBerechtigung extends TSAbstractDateRangedEntity {

    private _traegerschaft?: TSTraegerschaft;
    private _institution?: TSInstitution;
    private _role: TSRole;
    private _gemeindeList: Array<TSGemeinde> = [];

    public constructor(
        gueltigkeit?: TSDateRange,
        role?: TSRole,
        traegerschaft?: TSTraegerschaft,
        institution?: TSInstitution,
    ) {
        super(gueltigkeit);
        this._role = role;
        this._traegerschaft = traegerschaft;
        this._institution = institution;
    }

    public get role(): TSRole {
        return this._role;
    }

    public set role(value: TSRole) {
        this._role = value;
    }

    public get traegerschaft(): TSTraegerschaft {
        return this._traegerschaft;
    }

    public set traegerschaft(value: TSTraegerschaft) {
        this._traegerschaft = value;
    }

    public get institution(): TSInstitution {
        return this._institution;
    }

    public set institution(value: TSInstitution) {
        this._institution = value;
    }

    public get gemeindeList(): Array<TSGemeinde> {
        return this._gemeindeList;
    }

    public set gemeindeList(value: Array<TSGemeinde>) {
        this._gemeindeList = value;
    }

    /**
     * Diese Methode wird im Client gebraucht, weil das Amt in der Cookie nicht gespeichert wird. Das Amt in der Cookie
     * zu speichern waere auch keine gute Loesung, da es da nicht hingehoert. Normalerweise wird das Amt aber im Server
     * gesetzt und zum Client geschickt. Diese Methode wird nur verwendet, wenn der User aus der Cookie geholt wird.
     *
     * ACHTUNG Diese Logik existiert auch im Server UserRole. Aenderungen muessen in beiden Orten gemacht werden.
     */
    public analyseAmt(): TSAmt {
        switch (this.role) {
            case TSRole.SACHBEARBEITER_BG:
            case TSRole.ADMIN_BG:
            case TSRole.SUPER_ADMIN:
                return TSAmt.JUGENDAMT;
            case TSRole.SACHBEARBEITER_TS:
            case TSRole.ADMIN_TS:
                return TSAmt.SCHULAMT;
            case TSRole.ADMIN_GEMEINDE:
            case TSRole.SACHBEARBEITER_GEMEINDE:
                return TSAmt.GEMEINDE;
            default:
                return TSAmt.NONE;
        }
    }

    public hasGemeindeRole(): boolean {
        return TSRoleUtil.isGemeindeRole(this.role);
    }

    public hasInstitutionRole(): boolean {
        return TSRoleUtil.isInstitutionRole(this.role);
    }

    public hasTraegerschaftRole(): boolean {
        return TSRoleUtil.isTraegerschaftRole(this.role);
    }

    public isSuperadmin(): boolean {
        return TSRoleUtil.getSuperAdminRoles().includes(this.role);
    }

    public prepareForSave(): void {
        if (!this.hasGemeindeRole()) {
            this.gemeindeList = [];
        }
        if (!this.hasInstitutionRole()) {
            this.institution = undefined;
        }
        if (!this.hasTraegerschaftRole()) {
            this.traegerschaft = undefined;
        }
    }
}
