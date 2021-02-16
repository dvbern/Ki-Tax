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

import {EbeguUtil} from '../utils/EbeguUtil';
import {TSBenutzerStatus} from './enums/TSBenutzerStatus';
import {TSRole} from './enums/TSRole';
import {TSSozialdienst} from './sozialdienst/TSSozialdienst';
import {TSAbstractDateRangedEntity} from './TSAbstractDateRangedEntity';
import {TSInstitution} from './TSInstitution';
import {TSTraegerschaft} from './TSTraegerschaft';

export class TSBerechtigungHistory extends TSAbstractDateRangedEntity {

    private _userErstellt: string;
    private _username: string;
    private _role: TSRole;
    private _traegerschaft: TSTraegerschaft;
    private _institution: TSInstitution;
    private _gemeinden: string;
    private _status: TSBenutzerStatus;
    private _geloescht: boolean;
    private _sozialdienst: TSSozialdienst;

    public get userErstellt(): string {
        return this._userErstellt;
    }

    public set userErstellt(value: string) {
        this._userErstellt = value;
    }

    public get username(): string {
        return this._username;
    }

    public set username(value: string) {
        this._username = value;
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

    public get gemeinden(): string {
        return this._gemeinden;
    }

    public set gemeinden(value: string) {
        this._gemeinden = value;
    }

    public get status(): TSBenutzerStatus {
        return this._status;
    }

    public set status(value: TSBenutzerStatus) {
        this._status = value;
    }

    public get geloescht(): boolean {
        return this._geloescht;
    }

    public set geloescht(value: boolean) {
        this._geloescht = value;
    }

    public get sozialdienst(): TSSozialdienst {
        return this._sozialdienst;
    }

    public set sozialdienst(value: TSSozialdienst) {
        this._sozialdienst = value;
    }

    public getDescription(): string | null {
        if (this.institution) {
            return this.institution.name;
        }
        if (this.traegerschaft) {
            return this.traegerschaft.name;
        }
        if (!EbeguUtil.isEmptyStringNullOrUndefined(this.gemeinden)) {
            return this.gemeinden;
        }

        return null;
    }
}
