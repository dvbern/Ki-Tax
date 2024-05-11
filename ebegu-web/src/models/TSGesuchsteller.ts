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

import {TSSprache} from './enums/TSSprache';
import {TSAbstractPersonEntity} from './TSAbstractPersonEntity';

export class TSGesuchsteller extends TSAbstractPersonEntity {

    private _mail: string;
    private _mobile: string;
    private _telefon: string;
    private _telefonAusland: string;
    private _diplomatenstatus: boolean;
    private _korrespondenzSprache: TSSprache;
    private _notfallnummer: string;
    private _sozialversicherungsnummer: string;

    public constructor() {
        super();
    }

    public get mail(): string {
        return this._mail;
    }

    public set mail(value: string) {
        this._mail = value;
    }

    public get mobile(): string {
        return this._mobile;
    }

    public set mobile(value: string) {
        this._mobile = value;
    }

    public get telefon(): string {
        return this._telefon;
    }

    public set telefon(value: string) {
        this._telefon = value;
    }

    public get telefonAusland(): string {
        return this._telefonAusland;
    }

    public set telefonAusland(value: string) {
        this._telefonAusland = value;
    }

    public get diplomatenstatus(): boolean {
        return this._diplomatenstatus;
    }

    public set diplomatenstatus(value: boolean) {
        this._diplomatenstatus = value;
    }

    public get korrespondenzSprache(): TSSprache {
        return this._korrespondenzSprache;
    }

    public set korrespondenzSprache(value: TSSprache) {
        this._korrespondenzSprache = value;
    }

    public get notfallnummer(): string {
        return this._notfallnummer;
    }

    public set notfallnummer(value: string) {
        this._notfallnummer = value;
    }

    public getPhone(): string {
        if (this.mobile) {
            return this.mobile;
        }
        if (this.telefon) {
            return this.telefon;
        }
        return '';
    }

    public get sozialversicherungsnummer(): string {
        return this._sozialversicherungsnummer;
    }

    public set sozialversicherungsnummer(value: string) {
        this._sozialversicherungsnummer = value;
    }
}
