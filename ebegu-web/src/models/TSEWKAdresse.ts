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

/**
 * DTO für eine Adresse aus dem EWK
 */
export class TSEWKAdresse extends TSAbstractMutableEntity {
    private _adresszusatz1: string;
    private _adresszusatz2: string;
    private _hausnummer: string;
    private _wohnungsnummer: string;
    private _strasse: string;
    private _postleitzahl: string;
    private _ort: string;
    private _gebiet: string;

    public constructor(
        adresszusatz1?: string,
        adresszusatz2?: string,
        hausnummer?: string,
        wohnungsnummer?: string,
        strasse?: string,
        postleitzahl?: string,
        ort?: string,
        gebiet?: string
    ) {
        super();
        this._adresszusatz1 = adresszusatz1;
        this._adresszusatz2 = adresszusatz2;
        this._hausnummer = hausnummer;
        this._wohnungsnummer = wohnungsnummer;
        this._strasse = strasse;
        this._postleitzahl = postleitzahl;
        this._ort = ort;
        this._gebiet = gebiet;
    }

    public get adresszusatz1(): string {
        return this._adresszusatz1;
    }

    public set adresszusatz1(value: string) {
        this._adresszusatz1 = value;
    }

    public get adresszusatz2(): string {
        return this._adresszusatz2;
    }

    public set adresszusatz2(value: string) {
        this._adresszusatz2 = value;
    }

    public get hausnummer(): string {
        return this._hausnummer;
    }

    public set hausnummer(value: string) {
        this._hausnummer = value;
    }

    public get wohnungsnummer(): string {
        return this._wohnungsnummer;
    }

    public set wohnungsnummer(value: string) {
        this._wohnungsnummer = value;
    }

    public get strasse(): string {
        return this._strasse;
    }

    public set strasse(value: string) {
        this._strasse = value;
    }

    public get postleitzahl(): string {
        return this._postleitzahl;
    }

    public set postleitzahl(value: string) {
        this._postleitzahl = value;
    }

    public get ort(): string {
        return this._ort;
    }

    public set ort(value: string) {
        this._ort = value;
    }

    public get gebiet(): string {
        return this._gebiet;
    }

    public set gebiet(value: string) {
        this._gebiet = value;
    }

    public getShortDescription(): string {
        return `${this.strasse} ${this.hausnummer}, ${this.postleitzahl} ${this.ort}`;
    }
}
