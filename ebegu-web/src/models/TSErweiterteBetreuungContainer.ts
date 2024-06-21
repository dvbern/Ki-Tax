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
import {TSErweiterteBetreuung} from './TSErweiterteBetreuung';

export class TSErweiterteBetreuungContainer extends TSAbstractMutableEntity {
    private _erweiterteBetreuungGS: TSErweiterteBetreuung;
    private _erweiterteBetreuungJA: TSErweiterteBetreuung;

    public constructor(
        erweiterteBetreuungGS?: TSErweiterteBetreuung,
        erweiterteBetreuungJA?: TSErweiterteBetreuung
    ) {
        super();
        this._erweiterteBetreuungGS = erweiterteBetreuungGS;
        this._erweiterteBetreuungJA = erweiterteBetreuungJA;
    }

    public get erweiterteBetreuungGS(): TSErweiterteBetreuung {
        return this._erweiterteBetreuungGS;
    }

    public set erweiterteBetreuungGS(value: TSErweiterteBetreuung) {
        this._erweiterteBetreuungGS = value;
    }

    public get erweiterteBetreuungJA(): TSErweiterteBetreuung {
        return this._erweiterteBetreuungJA;
    }

    public set erweiterteBetreuungJA(value: TSErweiterteBetreuung) {
        this._erweiterteBetreuungJA = value;
    }
}
