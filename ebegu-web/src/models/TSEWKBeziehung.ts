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
import TSEWKAdresse from './TSEWKAdresse';

/**
 * DTO für eine Beziehung aus dem EWK
 */
export default class TSEWKBeziehung extends TSAbstractMutableEntity {

    private _beziehungstyp: string;
    private _beziehungstypTxt: string;
    private _personID: string;
    private _nachname: string;
    private _ledigname: string;
    private _vorname: string;
    private _rufname: string;
    private _geburtsdatum: moment.Moment;
    private _adresse: TSEWKAdresse;

    public constructor(beziehungstyp?: string, beziehungstypTxt?: string, personID?: string, nachname?: string, ledigname?: string,
                       vorname?: string, rufname?: string, geburtsdatum?: moment.Moment, adresse?: TSEWKAdresse) {
        super();
        this._beziehungstyp = beziehungstyp;
        this._beziehungstypTxt = beziehungstypTxt;
        this._personID = personID;
        this._nachname = nachname;
        this._ledigname = ledigname;
        this._vorname = vorname;
        this._rufname = rufname;
        this._geburtsdatum = geburtsdatum;
        this._adresse = adresse;
    }

    public get beziehungstyp(): string {
        return this._beziehungstyp;
    }

    public set beziehungstyp(value: string) {
        this._beziehungstyp = value;
    }

    public get beziehungstypTxt(): string {
        return this._beziehungstypTxt;
    }

    public set beziehungstypTxt(value: string) {
        this._beziehungstypTxt = value;
    }

    public get personID(): string {
        return this._personID;
    }

    public set personID(value: string) {
        this._personID = value;
    }

    public get nachname(): string {
        return this._nachname;
    }

    public set nachname(value: string) {
        this._nachname = value;
    }

    public get ledigname(): string {
        return this._ledigname;
    }

    public set ledigname(value: string) {
        this._ledigname = value;
    }

    public get vorname(): string {
        return this._vorname;
    }

    public set vorname(value: string) {
        this._vorname = value;
    }

    public get rufname(): string {
        return this._rufname;
    }

    public set rufname(value: string) {
        this._rufname = value;
    }

    public get geburtsdatum(): moment.Moment {
        return this._geburtsdatum;
    }

    public set geburtsdatum(value: moment.Moment) {
        this._geburtsdatum = value;
    }

    public get adresse(): TSEWKAdresse {
        return this._adresse;
    }

    public set adresse(value: TSEWKAdresse) {
        this._adresse = value;
    }
}
