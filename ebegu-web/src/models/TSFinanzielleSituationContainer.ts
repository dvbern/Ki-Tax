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
import {TSFinanzielleSituation} from './TSFinanzielleSituation';

export class TSFinanzielleSituationContainer extends TSAbstractMutableEntity {

    private _jahr: number;
    private _finanzielleSituationGS: TSFinanzielleSituation;
    private _finanzielleSituationJA: TSFinanzielleSituation;

    public constructor(
        jahr?: number,
        finanzielleSituationGS?: TSFinanzielleSituation,
        finanzielleSituationJA?: TSFinanzielleSituation
    ) {
        super();
        this._jahr = jahr;
        this._finanzielleSituationGS = finanzielleSituationGS;
        this._finanzielleSituationJA = finanzielleSituationJA;
    }

    public get jahr(): number {
        return this._jahr;
    }

    public set jahr(value: number) {
        this._jahr = value;
    }

    public get finanzielleSituationGS(): TSFinanzielleSituation {
        return this._finanzielleSituationGS;
    }

    public set finanzielleSituationGS(value: TSFinanzielleSituation) {
        this._finanzielleSituationGS = value;
    }

    public get finanzielleSituationJA(): TSFinanzielleSituation {
        return this._finanzielleSituationJA;
    }

    public set finanzielleSituationJA(value: TSFinanzielleSituation) {
        this._finanzielleSituationJA = value;
    }
}
