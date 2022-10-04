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
import {TSEinkommensverschlechterung} from './TSEinkommensverschlechterung';

export class TSEinkommensverschlechterungContainer extends TSAbstractMutableEntity {

    private _ekvGSBasisJahrPlus1: TSEinkommensverschlechterung;
    private _ekvGSBasisJahrPlus2: TSEinkommensverschlechterung;
    private _ekvJABasisJahrPlus1: TSEinkommensverschlechterung;
    private _ekvJABasisJahrPlus2: TSEinkommensverschlechterung;

    public constructor(
        ekvGSBasisJahrPlus1?: TSEinkommensverschlechterung,
        ekvGSBasisJahrPlus2?: TSEinkommensverschlechterung,
        ekvJABasisJahrPlus1?: TSEinkommensverschlechterung,
        ekvJABasisJahrPlus2?: TSEinkommensverschlechterung
    ) {
        super();
        this._ekvGSBasisJahrPlus1 = ekvGSBasisJahrPlus1;
        this._ekvGSBasisJahrPlus2 = ekvGSBasisJahrPlus2;
        this._ekvJABasisJahrPlus1 = ekvJABasisJahrPlus1;
        this._ekvJABasisJahrPlus2 = ekvJABasisJahrPlus2;
    }

    public get ekvGSBasisJahrPlus1(): TSEinkommensverschlechterung {
        return this._ekvGSBasisJahrPlus1;
    }

    public set ekvGSBasisJahrPlus1(value: TSEinkommensverschlechterung) {
        this._ekvGSBasisJahrPlus1 = value;
    }

    public get ekvGSBasisJahrPlus2(): TSEinkommensverschlechterung {
        return this._ekvGSBasisJahrPlus2;
    }

    public set ekvGSBasisJahrPlus2(value: TSEinkommensverschlechterung) {
        this._ekvGSBasisJahrPlus2 = value;
    }

    public get ekvJABasisJahrPlus1(): TSEinkommensverschlechterung {
        return this._ekvJABasisJahrPlus1;
    }

    public set ekvJABasisJahrPlus1(value: TSEinkommensverschlechterung) {
        this._ekvJABasisJahrPlus1 = value;
    }

    public get ekvJABasisJahrPlus2(): TSEinkommensverschlechterung {
        return this._ekvJABasisJahrPlus2;
    }

    public set ekvJABasisJahrPlus2(value: TSEinkommensverschlechterung) {
        this._ekvJABasisJahrPlus2 = value;
    }

    public isEmpty(): boolean {
        return !this._ekvJABasisJahrPlus1 && !this._ekvJABasisJahrPlus2;
    }
}
