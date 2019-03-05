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

import TSAbstractFinanzielleSituation from './TSAbstractFinanzielleSituation';

export default class TSEinkommensverschlechterung extends TSAbstractFinanzielleSituation {

    private _nettolohnJan: number;
    private _nettolohnFeb: number;
    private _nettolohnMrz: number;
    private _nettolohnApr: number;
    private _nettolohnMai: number;
    private _nettolohnJun: number;
    private _nettolohnJul: number;
    private _nettolohnAug: number;
    private _nettolohnSep: number;
    private _nettolohnOkt: number;
    private _nettolohnNov: number;
    private _nettolohnDez: number;
    private _nettolohnZus: number;
    private _geschaeftsgewinnBasisjahrMinus1: number;

    public constructor() {
        super();
    }

    public get nettolohnJan(): number {
        return this._nettolohnJan;
    }

    public set nettolohnJan(value: number) {
        this._nettolohnJan = value;
    }

    public get nettolohnFeb(): number {
        return this._nettolohnFeb;
    }

    public set nettolohnFeb(value: number) {
        this._nettolohnFeb = value;
    }

    public get nettolohnMrz(): number {
        return this._nettolohnMrz;
    }

    public set nettolohnMrz(value: number) {
        this._nettolohnMrz = value;
    }

    public get nettolohnApr(): number {
        return this._nettolohnApr;
    }

    public set nettolohnApr(value: number) {
        this._nettolohnApr = value;
    }

    public get nettolohnMai(): number {
        return this._nettolohnMai;
    }

    public set nettolohnMai(value: number) {
        this._nettolohnMai = value;
    }

    public get nettolohnJun(): number {
        return this._nettolohnJun;
    }

    public set nettolohnJun(value: number) {
        this._nettolohnJun = value;
    }

    public get nettolohnJul(): number {
        return this._nettolohnJul;
    }

    public set nettolohnJul(value: number) {
        this._nettolohnJul = value;
    }

    public get nettolohnAug(): number {
        return this._nettolohnAug;
    }

    public set nettolohnAug(value: number) {
        this._nettolohnAug = value;
    }

    public get nettolohnSep(): number {
        return this._nettolohnSep;
    }

    public set nettolohnSep(value: number) {
        this._nettolohnSep = value;
    }

    public get nettolohnOkt(): number {
        return this._nettolohnOkt;
    }

    public set nettolohnOkt(value: number) {
        this._nettolohnOkt = value;
    }

    public get nettolohnNov(): number {
        return this._nettolohnNov;
    }

    public set nettolohnNov(value: number) {
        this._nettolohnNov = value;
    }

    public get nettolohnDez(): number {
        return this._nettolohnDez;
    }

    public set nettolohnDez(value: number) {
        this._nettolohnDez = value;
    }

    public get nettolohnZus(): number {
        return this._nettolohnZus;
    }

    public set nettolohnZus(value: number) {
        this._nettolohnZus = value;
    }

    public get geschaeftsgewinnBasisjahrMinus1(): number {
        return this._geschaeftsgewinnBasisjahrMinus1;
    }

    public set geschaeftsgewinnBasisjahrMinus1(value: number) {
        this._geschaeftsgewinnBasisjahrMinus1 = value;
    }
}
