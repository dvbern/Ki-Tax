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
import {TSFamiliensituation} from './TSFamiliensituation';

export class TSFamiliensituationContainer extends TSAbstractMutableEntity {

    private _familiensituationJA: TSFamiliensituation;
    private _familiensituationGS: TSFamiliensituation;
    private _familiensituationErstgesuch: TSFamiliensituation;

    public constructor() {
        super();
    }

    public get familiensituationJA(): TSFamiliensituation {
        return this._familiensituationJA;
    }

    public set familiensituationJA(value: TSFamiliensituation) {
        this._familiensituationJA = value;
    }

    public get familiensituationGS(): TSFamiliensituation {
        return this._familiensituationGS;
    }

    public set familiensituationGS(value: TSFamiliensituation) {
        this._familiensituationGS = value;
    }

    public get familiensituationErstgesuch(): TSFamiliensituation {
        return this._familiensituationErstgesuch;
    }

    public set familiensituationErstgesuch(value: TSFamiliensituation) {
        this._familiensituationErstgesuch = value;
    }
}
