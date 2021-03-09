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
import {TSSozialdienstFall} from './sozialdienst/TSSozialdienstFall';
import {TSAbstractMutableEntity} from './TSAbstractMutableEntity';
import {TSBenutzer} from './TSBenutzer';

export class TSFall extends TSAbstractMutableEntity {

    private _fallNummer: number;
    private _nextNumberKind: number;
    private _besitzer: TSBenutzer;
    private _sozialdienstFall: TSSozialdienstFall;

    public constructor(fallNummer?: number, nextNumberKind?: number, besitzer?: TSBenutzer) {
        super();
        this._fallNummer = fallNummer;
        this._nextNumberKind = nextNumberKind;
        this._besitzer = besitzer;
    }

    public get fallNummer(): number {
        return this._fallNummer;
    }

    public set fallNummer(value: number) {
        this._fallNummer = value;
    }

    public get nextNumberKind(): number {
        return this._nextNumberKind;
    }

    public set nextNumberKind(value: number) {
        this._nextNumberKind = value;
    }

    public get besitzer(): TSBenutzer {
        return this._besitzer;
    }

    public set besitzer(value: TSBenutzer) {
        this._besitzer = value;
    }

    public get sozialdienstFall(): TSSozialdienstFall {
        return this._sozialdienstFall;
    }

    public set sozialdienstFall(value: TSSozialdienstFall) {
        this._sozialdienstFall = value;
    }
}
