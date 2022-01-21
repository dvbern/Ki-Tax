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

import {TSSteuerdatenAnfrageStatus} from './enums/TSSteuerdatenAnfrageStatus';
import {TSAbstractMutableEntity} from './TSAbstractMutableEntity';

export class TSAbstractFinanzielleSituation extends TSAbstractMutableEntity {
    private _nettolohn: number;
    private _familienzulage: number;
    private _ersatzeinkommen: number;
    private _erhalteneAlimente: number;
    private _bruttovermoegen: number;
    private _schulden: number;
    private _geschaeftsgewinnBasisjahr: number;
    private _geleisteteAlimente: number;
    private _steuerbaresEinkommen: number;
    private _steuerbaresVermoegen: number;
    private _abzuegeLiegenschaft: number;
    private _geschaeftsverlust: number;
    private _einkaeufeVorsorge: number;
    private _steuerdatenAbfrageStatus: TSSteuerdatenAnfrageStatus;

    public constructor() {
        super();
    }

    public get nettolohn(): number {
        return this._nettolohn;
    }

    public set nettolohn(value: number) {
        this._nettolohn = value;
    }

    public get familienzulage(): number {
        return this._familienzulage;
    }

    public set familienzulage(value: number) {
        this._familienzulage = value;
    }

    public get ersatzeinkommen(): number {
        return this._ersatzeinkommen;
    }

    public set ersatzeinkommen(value: number) {
        this._ersatzeinkommen = value;
    }

    public get erhalteneAlimente(): number {
        return this._erhalteneAlimente;
    }

    public set erhalteneAlimente(value: number) {
        this._erhalteneAlimente = value;
    }

    public get bruttovermoegen(): number {
        return this._bruttovermoegen;
    }

    public set bruttovermoegen(value: number) {
        this._bruttovermoegen = value;
    }

    public get schulden(): number {
        return this._schulden;
    }

    public set schulden(value: number) {
        this._schulden = value;
    }

    public get geschaeftsgewinnBasisjahr(): number {
        return this._geschaeftsgewinnBasisjahr;
    }

    public set geschaeftsgewinnBasisjahr(value: number) {
        this._geschaeftsgewinnBasisjahr = value;
    }

    public get geleisteteAlimente(): number {
        return this._geleisteteAlimente;
    }

    public set geleisteteAlimente(value: number) {
        this._geleisteteAlimente = value;
    }
    public get steuerbaresEinkommen(): number {
        return this._steuerbaresEinkommen;
    }

    public set steuerbaresEinkommen(value: number) {
        this._steuerbaresEinkommen = value;
    }

    public get steuerbaresVermoegen(): number {
        return this._steuerbaresVermoegen;
    }

    public set steuerbaresVermoegen(value: number) {
        this._steuerbaresVermoegen = value;
    }

    public get abzuegeLiegenschaft(): number {
        return this._abzuegeLiegenschaft;
    }

    public set abzuegeLiegenschaft(value: number) {
        this._abzuegeLiegenschaft = value;
    }

    public get geschaeftsverlust(): number {
        return this._geschaeftsverlust;
    }

    public set geschaeftsverlust(value: number) {
        this._geschaeftsverlust = value;
    }

    public get einkaeufeVorsorge(): number {
        return this._einkaeufeVorsorge;
    }

    public set einkaeufeVorsorge(value: number) {
        this._einkaeufeVorsorge = value;
    }

    public get steuerdatenAbfrageStatus(): TSSteuerdatenAnfrageStatus {
        return this._steuerdatenAbfrageStatus;
    }

    public set steuerdatenAbfrageStatus(value: TSSteuerdatenAnfrageStatus) {
        this._steuerdatenAbfrageStatus = value;
    }
}
