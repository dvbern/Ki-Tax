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

import {TSInstitutionStatus} from './enums/TSInstitutionStatus';
import {TSAbstractMutableEntity} from './TSAbstractMutableEntity';
import {TSMandant} from './TSMandant';
import {TSTraegerschaft} from './TSTraegerschaft';

export default class TSInstitution extends TSAbstractMutableEntity {
    private _name: string;
    private _traegerschaft: TSTraegerschaft;
    private _mandant: TSMandant;
    private _mail: string;
    private _status: TSInstitutionStatus;

    public constructor(name?: string, tragerschaft?: TSTraegerschaft, mandant?: TSMandant, mail?: string, status?: TSInstitutionStatus) {
        super();
        this._name = name;
        this._traegerschaft = tragerschaft;
        this._mandant = mandant;
        this._mail = mail;
        this._status = status;
    }

    public get name(): string {
        return this._name;
    }

    public set name(value: string) {
        this._name = value;
    }

    public get traegerschaft(): TSTraegerschaft {
        return this._traegerschaft;
    }

    public set traegerschaft(value: TSTraegerschaft) {
        this._traegerschaft = value;
    }

    public get mandant(): TSMandant {
        return this._mandant;
    }

    public set mandant(value: TSMandant) {
        this._mandant = value;
    }

    public get mail(): string {
        return this._mail;
    }

    public set mail(value: string) {
        this._mail = value;
    }

    public get status(): TSInstitutionStatus {
        return this._status;
    }

    public set status(value: TSInstitutionStatus) {
        this._status = value;
    }
}
