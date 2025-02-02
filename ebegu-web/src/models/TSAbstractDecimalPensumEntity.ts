/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {TSPensumUnits} from './enums/TSPensumUnits';
import {TSAbstractDateRangedEntity} from './TSAbstractDateRangedEntity';
import {TSEingewoehnung} from './TSEingewoehnung';

export class TSAbstractDecimalPensumEntity extends TSAbstractDateRangedEntity {
    private _unitForDisplay: TSPensumUnits;
    private _pensum: number;
    private _monatlicheBetreuungskosten: number;
    private _stuendlicheVollkosten: number;
    private _betreuteTage: number;
    private _eingewoehnung: TSEingewoehnung;
    // Transient field used for calculations. Not sent to server
    private _hasEingewoehnung: boolean;

    public get unitForDisplay(): TSPensumUnits {
        return this._unitForDisplay;
    }

    public set unitForDisplay(value: TSPensumUnits) {
        this._unitForDisplay = value;
    }

    public get pensum(): number {
        return this._pensum;
    }

    public set pensum(value: number) {
        this._pensum = value;
    }

    public get monatlicheBetreuungskosten(): number {
        return this._monatlicheBetreuungskosten;
    }

    public set monatlicheBetreuungskosten(value: number) {
        this._monatlicheBetreuungskosten = value;
    }

    public get stuendlicheVollkosten(): number {
        return this._stuendlicheVollkosten;
    }

    public set stuendlicheVollkosten(value: number) {
        this._stuendlicheVollkosten = value;
    }

    public get betreuteTage(): number {
        return this._betreuteTage;
    }

    public set betreuteTage(value: number) {
        this._betreuteTage = value;
    }

    public get eingewoehnung(): TSEingewoehnung {
        return this._eingewoehnung;
    }

    public set eingewoehnung(value: TSEingewoehnung) {
        this._eingewoehnung = value;
    }

    public get hasEingewoehnungs(): boolean {
        return this._hasEingewoehnung;
    }

    public set hasEingewoehnung(value: boolean) {
        this._hasEingewoehnung = value;
    }
}
