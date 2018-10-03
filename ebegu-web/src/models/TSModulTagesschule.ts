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

import * as moment from 'moment';
import {TSDayOfWeek} from './enums/TSDayOfWeek';
import {TSModulTagesschuleName} from './enums/TSModulTagesschuleName';
import {TSAbstractMutableEntity} from './TSAbstractMutableEntity';

export default class TSModulTagesschule extends TSAbstractMutableEntity {

    private _wochentag: TSDayOfWeek;
    private _modulTagesschuleName: TSModulTagesschuleName;
    private _zeitVon: moment.Moment;
    private _zeitBis: moment.Moment;

    private _angemeldet: boolean; // Transient, wird nicht auf Server synchronisiert, bzw. nur die mit angemeldet=true

    public constructor(
        wochentag?: TSDayOfWeek,
        modulTagesschuleName?: TSModulTagesschuleName,
        zeitVon?: moment.Moment,
        zeitBis?: moment.Moment,
    ) {
        super();
        this._wochentag = wochentag;
        this._modulTagesschuleName = modulTagesschuleName;
        this._zeitVon = zeitVon;
        this._zeitBis = zeitBis;
    }

    public get wochentag(): TSDayOfWeek {
        return this._wochentag;
    }

    public set wochentag(value: TSDayOfWeek) {
        this._wochentag = value;
    }

    public get modulTagesschuleName(): TSModulTagesschuleName {
        return this._modulTagesschuleName;
    }

    public set modulTagesschuleName(value: TSModulTagesschuleName) {
        this._modulTagesschuleName = value;
    }

    public get zeitVon(): moment.Moment {
        return this._zeitVon;
    }

    public set zeitVon(value: moment.Moment) {
        this._zeitVon = value;
    }

    public get zeitBis(): moment.Moment {
        return this._zeitBis;
    }

    public set zeitBis(value: moment.Moment) {
        this._zeitBis = value;
    }

    public get angemeldet(): boolean {
        return this._angemeldet;
    }

    public set angemeldet(value: boolean) {
        this._angemeldet = value;
    }

    /**
     * Prueft ob beide Module gleich sind. Sie sind glech wenn wochentag und modulTagesschuleName gleich sind.
     * Die ZeitVon und ZeitBis spielt keine Rolle in diesem Fall, da so Module unterschiedlichen Institutionen
     * verglichen werden koennen.
     */
    public isSameModul(instModul: TSModulTagesschule): boolean {
        return instModul
            && this.wochentag === instModul.wochentag
            && this.modulTagesschuleName === instModul.modulTagesschuleName;
    }
}
