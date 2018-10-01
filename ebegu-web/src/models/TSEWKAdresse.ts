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
import * as moment from 'moment';

/**
 * DTO für eine Adresse aus dem EWK
 */
export default class TSEWKAdresse extends TSAbstractMutableEntity {

    private _adresstyp: string;
    private _adresstypTxt: string;
    private _gueltigVon: moment.Moment;
    private _gueltigBis: moment.Moment;
    private _coName: string;
    private _postfach: string;
    private _bfSGemeinde: string;
    private _strasse: string;
    private _hausnummer: string;
    private _postleitzahl: string;
    private _ort: string;
    private _kanton: string;
    private _land: string;

    public constructor(adresstyp?: string, adresstypTxt?: string, gueltigVon?: moment.Moment, gueltigBis?: moment.Moment,
                       coName?: string, postfach?: string, bfSGemeinde?: string, strasse?: string, hausnummer?: string,
                       postleitzahl?: string, ort?: string, kanton?: string, land?: string) {
        super();
        this._adresstyp = adresstyp;
        this._adresstypTxt = adresstypTxt;
        this._gueltigVon = gueltigVon;
        this._gueltigBis = gueltigBis;
        this._coName = coName;
        this._postfach = postfach;
        this._bfSGemeinde = bfSGemeinde;
        this._strasse = strasse;
        this._hausnummer = hausnummer;
        this._postleitzahl = postleitzahl;
        this._ort = ort;
        this._kanton = kanton;
        this._land = land;
    }

    public get adresstyp(): string {
        return this._adresstyp;
    }

    public set adresstyp(value: string) {
        this._adresstyp = value;
    }

    public get adresstypTxt(): string {
        return this._adresstypTxt;
    }

    public set adresstypTxt(value: string) {
        this._adresstypTxt = value;
    }

    public get gueltigVon(): moment.Moment {
        return this._gueltigVon;
    }

    public set gueltigVon(value: moment.Moment) {
        this._gueltigVon = value;
    }

    public get gueltigBis(): moment.Moment {
        return this._gueltigBis;
    }

    public set gueltigBis(value: moment.Moment) {
        this._gueltigBis = value;
    }

    public get coName(): string {
        return this._coName;
    }

    public set coName(value: string) {
        this._coName = value;
    }

    public get postfach(): string {
        return this._postfach;
    }

    public set postfach(value: string) {
        this._postfach = value;
    }

    public get bfSGemeinde(): string {
        return this._bfSGemeinde;
    }

    public set bfSGemeinde(value: string) {
        this._bfSGemeinde = value;
    }

    public get strasse(): string {
        return this._strasse;
    }

    public set strasse(value: string) {
        this._strasse = value;
    }

    public get hausnummer(): string {
        return this._hausnummer;
    }

    public set hausnummer(value: string) {
        this._hausnummer = value;
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

    public get kanton(): string {
        return this._kanton;
    }

    public set kanton(value: string) {
        this._kanton = value;
    }

    public get land(): string {
        return this._land;
    }

    public set land(value: string) {
        this._land = value;
    }

    public getShortDescription(): string {
        return this.strasse + ' ' + this.hausnummer + ', ' + this.postleitzahl + ' ' + this.ort;
    }
}
