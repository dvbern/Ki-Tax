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

import {TSAbstractMutableEntity} from './TSAbstractMutableEntity';

/* eslint-disable @typescript-eslint/naming-convention, no-underscore-dangle, id-blacklist, id-match */
export class TSEinkommensverschlechterungInfo extends TSAbstractMutableEntity {

    private _einkommensverschlechterung: boolean = false;

    private _ekvFuerBasisJahrPlus1: boolean;
    private _ekvFuerBasisJahrPlus2: boolean;

    private _ekvBasisJahrPlus1Annulliert: boolean = false;
    private _ekvBasisJahrPlus2Annulliert: boolean = false;

    public get einkommensverschlechterung(): boolean {
        return this._einkommensverschlechterung;
    }

    public set einkommensverschlechterung(value: boolean) {
        this._einkommensverschlechterung = value;
    }

    public get ekvFuerBasisJahrPlus1(): boolean {
        return this._ekvFuerBasisJahrPlus1;
    }

    public set ekvFuerBasisJahrPlus1(value: boolean) {
        this._ekvFuerBasisJahrPlus1 = value;
    }

    public get ekvFuerBasisJahrPlus2(): boolean {
        return this._ekvFuerBasisJahrPlus2;
    }

    public set ekvFuerBasisJahrPlus2(value: boolean) {
        this._ekvFuerBasisJahrPlus2 = value;
    }

    public get ekvBasisJahrPlus1Annulliert(): boolean {
        return this._ekvBasisJahrPlus1Annulliert;
    }

    public set ekvBasisJahrPlus1Annulliert(value: boolean) {
        this._ekvBasisJahrPlus1Annulliert = value;
    }

    public get ekvBasisJahrPlus2Annulliert(): boolean {
        return this._ekvBasisJahrPlus2Annulliert;
    }

    public set ekvBasisJahrPlus2Annulliert(value: boolean) {
        this._ekvBasisJahrPlus2Annulliert = value;
    }
}
