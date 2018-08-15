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
import TSAbstractPersonEntity from './TSAbstractPersonEntity';
import {TSGeschlecht} from './enums/TSGeschlecht';
import * as moment from 'moment';

export default class TSGesuchsteller extends TSAbstractPersonEntity {

    private _mail: string;
    private _mobile: string;
    private _telefon: string;
    private _telefonAusland: string;
    private _diplomatenstatus: boolean;
    private _ewkPersonId: string;
    private _ewkAbfrageDatum: moment.Moment;
    private _korrespondenzSprache: TSSprache;

    constructor(vorname?: string, nachname?: string, geburtsdatum?: moment.Moment, geschlecht?: TSGeschlecht,
                email?: string, mobile?: string, telefon?: string, telefonAusland?: string,
                diplomatenstatus?: boolean, ewkPersonId?: string, ewkAbfrageDatum?: moment.Moment, korrespondenzSprache?: TSSprache) {
        super(vorname, nachname, geburtsdatum, geschlecht);
        this._mail = email;
        this._mobile = mobile;
        this._telefon = telefon;
        this._telefonAusland = telefonAusland;
        this._diplomatenstatus = diplomatenstatus;
        this._ewkPersonId = ewkPersonId;
        this._ewkAbfrageDatum = ewkAbfrageDatum;
        this._korrespondenzSprache = korrespondenzSprache;
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

    get diplomatenstatus(): boolean {
        return this._diplomatenstatus;
    }

    set diplomatenstatus(value: boolean) {
        this._diplomatenstatus = value;
    }

    get ewkPersonId(): string {
        return this._ewkPersonId;
    }

    set ewkPersonId(value: string) {
        this._ewkPersonId = value;
    }

    get ewkAbfrageDatum(): moment.Moment {
        return this._ewkAbfrageDatum;
    }

    set ewkAbfrageDatum(value: moment.Moment) {
        this._ewkAbfrageDatum = value;
    }

    get korrespondenzSprache(): TSSprache {
        return this._korrespondenzSprache;
    }

    set korrespondenzSprache(value: TSSprache) {
        this._korrespondenzSprache = value;
    }

    public getPhone(): string {
        if (this.mobile) {
            return this.mobile;
        } else if (this.telefon) {
            return this.telefon;
        } else {
            return '';
        }
    }
}

