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

import {TSAmt} from './enums/TSAmt';
import {rolePrefix, TSRole} from './enums/TSRole';
import {TSAbstractDateRangedEntity} from './TSAbstractDateRangedEntity';
import TSGemeinde from './TSGemeinde';
import TSInstitution from './TSInstitution';
import {TSTraegerschaft} from './TSTraegerschaft';
import {TSDateRange} from './types/TSDateRange';

export default class TSBerechtigung extends TSAbstractDateRangedEntity {

    private _traegerschaft: TSTraegerschaft;
    private _institution: TSInstitution;
    private _role: TSRole;
    private _gemeindeList: Array<TSGemeinde> = [];

    private _enabled: boolean; // Wird nicht zum Server gemappt, nur zur Anzeige im GUI

    constructor(gueltigkeit?: TSDateRange, role?: TSRole, traegerschaft?: TSTraegerschaft, institution?: TSInstitution) {
        super(gueltigkeit);
        this._role = role;
        this._traegerschaft = traegerschaft;
        this._institution = institution;
    }

    get role(): TSRole {
        return this._role;
    }

    set role(value: TSRole) {
        this._role = value;
    }

    get traegerschaft(): TSTraegerschaft {
        return this._traegerschaft;
    }

    set traegerschaft(value: TSTraegerschaft) {
        this._traegerschaft = value;
    }

    get institution(): TSInstitution {
        return this._institution;
    }

    set institution(value: TSInstitution) {
        this._institution = value;
    }

    get gemeindeList(): Array<TSGemeinde> {
        return this._gemeindeList;
    }

    set gemeindeList(value: Array<TSGemeinde>) {
        this._gemeindeList = value;
    }

    get enabled(): boolean {
        return this._enabled;
    }

    set enabled(value: boolean) {
        this._enabled = value;
    }

    getRoleKey(): string {
        return rolePrefix() + this.role;
    }

    /**
     * Diese Methode wird im Client gebraucht, weil das Amt in der Cookie nicht gespeichert wird. Das Amt in der Cookie zu speichern
     * waere auch keine gute Loesung, da es da nicht hingehoert. Normalerweise wird das Amt aber im Server gesetzt und zum Client geschickt.
     * Diese Methode wird nur verwendet, wenn der User aus der Cookie geholt wird.
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
}
