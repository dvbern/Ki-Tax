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

import {TSAbstractFinanzielleSituation} from './TSAbstractFinanzielleSituation';

export class TSEinkommensverschlechterung extends TSAbstractFinanzielleSituation {
    private _bruttolohnAbrechnung1: number;
    private _bruttolohnAbrechnung2: number;
    private _bruttolohnAbrechnung3: number;
    private _extraLohn: boolean;

    public constructor() {
        super();
    }

    public get bruttolohnAbrechnung1(): number {
        return this._bruttolohnAbrechnung1;
    }

    public set bruttolohnAbrechnung1(value: number) {
        this._bruttolohnAbrechnung1 = value;
    }

    public get bruttolohnAbrechnung2(): number {
        return this._bruttolohnAbrechnung2;
    }

    public set bruttolohnAbrechnung2(value: number) {
        this._bruttolohnAbrechnung2 = value;
    }

    public get bruttolohnAbrechnung3(): number {
        return this._bruttolohnAbrechnung3;
    }

    public set bruttolohnAbrechnung3(value: number) {
        this._bruttolohnAbrechnung3 = value;
    }

    public get extraLohn(): boolean {
        return this._extraLohn;
    }

    public set extraLohn(value: boolean) {
        this._extraLohn = value;
    }
}
