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
import {TSEWKAdresse} from './TSEWKAdresse';
import {TSEWKBeziehung} from './TSEWKBeziehung';

/**
 * DTO für eine Person aus dem EWK
 */
export class TSEWKPerson extends TSAbstractMutableEntity {

    private _personID: string;
    private _nachname: string;
    private _vorname: string;
    private _geburtsdatum: moment.Moment;
    private _zuzugsdatum: moment.Moment;
    private _wegzugsdatum: moment.Moment;
    private _zivilstand: string;
    private _zivilstandsdatum: moment.Moment;
    private _geschlecht: TSGeschlecht;
    private _adresse: TSEWKAdresse;
    private _kind: boolean;
    private _gesuchsteller: boolean;
    private _beziehungen: Array<TSEWKBeziehung>;
    private _nichtGefunden: boolean;
    private  _haushalt: boolean;

    public constructor(
        personID?: string,
        nachname?: string,
        vorname?: string,
        geburtsdatum?: moment.Moment,
        zuzugsdatum?: moment.Moment,
        wegzugsdatum?: moment.Moment,
        zivilstand?: string,
        zivilstandsdatum?: moment.Moment,
        geschlecht?: TSGeschlecht,
        adresse?: TSEWKAdresse,
        beziehungen?: Array<TSEWKBeziehung>,
        kind: boolean = false,
        gesuchsteller: boolean = false,
        nichtGefunden: boolean = false,
        haushalt: boolean = false
    ) {
        super();
        this._personID = personID;
        this._nachname = nachname;
        this._vorname = vorname;
        this._geburtsdatum = geburtsdatum;
        this._zuzugsdatum = zuzugsdatum;
        this._wegzugsdatum = wegzugsdatum;
        this._zivilstand = zivilstand;
        this._zivilstandsdatum = zivilstandsdatum;
        this._geschlecht = geschlecht;
        this._adresse = adresse;
        this._beziehungen = beziehungen;
        this._kind = kind;
        this._gesuchsteller = gesuchsteller;
        this._haushalt = haushalt;
        this._nichtGefunden = nichtGefunden;
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

    public get vorname(): string {
        return this._vorname;
    }

    public set vorname(value: string) {
        this._vorname = value;
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

    public get wegzugsdatum(): moment.Moment {
        return this._wegzugsdatum;
    }

    public set wegzugsdatum(value: moment.Moment) {
        this._wegzugsdatum = value;
    }

    public get zivilstand(): string {
        return this._zivilstand;
    }

    public set zivilstand(value: string) {
        this._zivilstand = value;
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

    public get adresse(): TSEWKAdresse {
        return this._adresse;
    }

    public set adresse(value: TSEWKAdresse) {
        this._adresse = value;
    }

    public get beziehungen(): Array<TSEWKBeziehung> {
        return this._beziehungen;
    }

    public set beziehungen(value: Array<TSEWKBeziehung>) {
        this._beziehungen = value;
    }

    public get kind(): boolean {
        return this._kind;
    }

    public set kind(value: boolean) {
        this._kind = value;
    }

    public get gesuchsteller(): boolean {
        return this._gesuchsteller;
    }

    public set gesuchsteller(value: boolean) {
        this._gesuchsteller = value;
    }

    public get haushalt(): boolean {
        return this._haushalt;
    }

    public set haushalt(value: boolean) {
        this._haushalt = value;
    }

    public get nichtGefunden(): boolean {
        return this._nichtGefunden;
    }

    public set nichtGefunden(value: boolean) {
        this._nichtGefunden = value;
    }

    public getShortDescription(): string {
        let description: string;
        description = `${this.vorname  } `;
        description += `${this.nachname  }, `;
        if(this.geburtsdatum) {
            description += `${this.geburtsdatum?.format('DD.MM.YYYY')}, `;
        }
        if (this.adresse) {
            description += this.adresse.ort;
        }
        return description;
    }
}
