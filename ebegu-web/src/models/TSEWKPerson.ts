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
import {TSGeschlecht} from './enums/TSGeschlecht';
import {TSAbstractMutableEntity} from './TSAbstractMutableEntity';
import TSEWKAdresse from './TSEWKAdresse';
import TSEWKBeziehung from './TSEWKBeziehung';
import TSEWKEinwohnercode from './TSEWKEinwohnercode';

/**
 * DTO für eine Person aus dem EWK
 */
export default class TSEWKPerson extends TSAbstractMutableEntity {

    private _personID: string;
    private _einwohnercodes: Array<TSEWKEinwohnercode>;
    private _nachname: string;
    private _ledigname: string;
    private _vorname: string;
    private _rufname: string;
    private _geburtsdatum: moment.Moment;
    private _zuzugsdatum: moment.Moment;
    private _nationalitaet: string;
    private _zivilstand: string;
    private _zivilstandTxt: string;
    private _zivilstandsdatum: moment.Moment;
    private _geschlecht: TSGeschlecht;
    private _bewilligungsart: string;
    private _bewilligungsartTxt: string;
    private _bewilligungBis: moment.Moment;
    private _adressen: Array<TSEWKAdresse>;
    private _beziehungen: Array<TSEWKBeziehung>;

    public constructor(personID?: string,
                       einwohnercodes?: Array<TSEWKEinwohnercode>,
                       nachname?: string,
                       ledigname?: string,
                       vorname?: string,
                       rufname?: string,
                       geburtsdatum?: moment.Moment,
                       zuzugsdatum?: moment.Moment,
                       nationalitaet?: string,
                       zivilstand?: string,
                       zivilstandTxt?: string,
                       zivilstandsdatum?: moment.Moment,
                       geschlecht?: TSGeschlecht,
                       bewilligungsart?: string,
                       bewilligungsartTxt?: string,
                       bewilligungBis?: moment.Moment,
                       adressen?: Array<TSEWKAdresse>,
                       beziehungen?: Array<TSEWKBeziehung>) {
        super();
        this._personID = personID;
        this._einwohnercodes = einwohnercodes;
        this._nachname = nachname;
        this._ledigname = ledigname;
        this._vorname = vorname;
        this._rufname = rufname;
        this._geburtsdatum = geburtsdatum;
        this._zuzugsdatum = zuzugsdatum;
        this._nationalitaet = nationalitaet;
        this._zivilstand = zivilstand;
        this._zivilstandTxt = zivilstandTxt;
        this._zivilstandsdatum = zivilstandsdatum;
        this._geschlecht = geschlecht;
        this._bewilligungsart = bewilligungsart;
        this._bewilligungsartTxt = bewilligungsartTxt;
        this._bewilligungBis = bewilligungBis;
        this._adressen = adressen;
        this._beziehungen = beziehungen;
    }

    public get personID(): string {
        return this._personID;
    }

    public set personID(value: string) {
        this._personID = value;
    }

    public get einwohnercodes(): Array<TSEWKEinwohnercode> {
        return this._einwohnercodes;
    }

    public set einwohnercodes(value: Array<TSEWKEinwohnercode>) {
        this._einwohnercodes = value;
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

    public get zuzugsdatum(): moment.Moment {
        return this._zuzugsdatum;
    }

    public set zuzugsdatum(value: moment.Moment) {
        this._zuzugsdatum = value;
    }

    public get nationalitaet(): string {
        return this._nationalitaet;
    }

    public set nationalitaet(value: string) {
        this._nationalitaet = value;
    }

    public get zivilstand(): string {
        return this._zivilstand;
    }

    public set zivilstand(value: string) {
        this._zivilstand = value;
    }

    public get zivilstandTxt(): string {
        return this._zivilstandTxt;
    }

    public set zivilstandTxt(value: string) {
        this._zivilstandTxt = value;
    }

    public get zivilstandsdatum(): moment.Moment {
        return this._zivilstandsdatum;
    }

    public set zivilstandsdatum(value: moment.Moment) {
        this._zivilstandsdatum = value;
    }

    public get geschlecht(): TSGeschlecht {
        return this._geschlecht;
    }

    public set geschlecht(value: TSGeschlecht) {
        this._geschlecht = value;
    }

    public get bewilligungsart(): string {
        return this._bewilligungsart;
    }

    public set bewilligungsart(value: string) {
        this._bewilligungsart = value;
    }

    public get bewilligungsartTxt(): string {
        return this._bewilligungsartTxt;
    }

    public set bewilligungsartTxt(value: string) {
        this._bewilligungsartTxt = value;
    }

    public get bewilligungBis(): moment.Moment {
        return this._bewilligungBis;
    }

    public set bewilligungBis(value: moment.Moment) {
        this._bewilligungBis = value;
    }

    public get adressen(): Array<TSEWKAdresse> {
        return this._adressen;
    }

    public set adressen(value: Array<TSEWKAdresse>) {
        this._adressen = value;
    }

    public get beziehungen(): Array<TSEWKBeziehung> {
        return this._beziehungen;
    }

    public set beziehungen(value: Array<TSEWKBeziehung>) {
        this._beziehungen = value;
    }

    public getWohnadresse(): TSEWKAdresse {
        for (const adresse of this.adressen) {
            if (adresse.adresstyp === '1') {
                return adresse;
            }
        }
        return undefined;
    }

    public getShortDescription(): string {
        let description: string;
        description = this.vorname + ' ';
        description += this.nachname + ', ';
        description += `${this.geburtsdatum.format('DD.MM.YYYY')}, `;
        if (this.getWohnadresse()) {
            description += this.getWohnadresse().getShortDescription();
        }
        return description;
    }
}
