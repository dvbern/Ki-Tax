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
import {TSBetreuungsangebotTyp} from './enums/TSBetreuungsangebotTyp';
import TSGesuchsperiode from './TSGesuchsperiode';
import TSInstitution from './TSInstitution';

export default class TSPendenzBetreuung {

    private _betreuungsNummer: string;
    private _betreuungsId: string;
    private _gesuchId: string;
    private _kindId: string;
    private _name: string;
    private _vorname: string;
    private _geburtsdatum: moment.Moment;
    private _typ: string;
    private _gesuchsperiode: TSGesuchsperiode;
    private _eingangsdatum: moment.Moment;
    private _eingangsdatumSTV: moment.Moment;
    private _betreuungsangebotTyp: TSBetreuungsangebotTyp;
    private _institution: TSInstitution;
    private _gemeinde: string;

    public constructor(betreuungsNummer?: string,
                       betreuungsId?: string,
                       gesuchId?: string,
                       kindId?: string,
                       name?: string,
                       vorname?: string,
                       geburtsdatum?: moment.Moment,
                       typ?: string,
                       gesuchsperiode?: TSGesuchsperiode,
                       eingangsdatum?: moment.Moment,
                       eingangsdatumSTV?: moment.Moment,
                       betreuungsangebotTyp?: TSBetreuungsangebotTyp,
                       institution?: TSInstitution,
                       gemeinde?: string) {
        this._betreuungsNummer = betreuungsNummer;
        this._betreuungsId = betreuungsId;
        this._gesuchId = gesuchId;
        this._kindId = kindId;
        this._name = name;
        this._vorname = vorname;
        this._geburtsdatum = geburtsdatum;
        this._typ = typ;
        this._gesuchsperiode = gesuchsperiode;
        this._eingangsdatum = eingangsdatum;
        this._eingangsdatumSTV = eingangsdatumSTV;
        this._betreuungsangebotTyp = betreuungsangebotTyp;
        this._institution = institution;
        this._gemeinde = gemeinde;
    }

    public get betreuungsNummer(): string {
        return this._betreuungsNummer;
    }

    public set betreuungsNummer(value: string) {
        this._betreuungsNummer = value;
    }

    public get betreuungsId(): string {
        return this._betreuungsId;
    }

    public set betreuungsId(value: string) {
        this._betreuungsId = value;
    }

    public get gesuchId(): string {
        return this._gesuchId;
    }

    public set gesuchId(value: string) {
        this._gesuchId = value;
    }

    public get kindId(): string {
        return this._kindId;
    }

    public set kindId(value: string) {
        this._kindId = value;
    }

    public get name(): string {
        return this._name;
    }

    public set name(value: string) {
        this._name = value;
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

    public get typ(): string {
        return this._typ;
    }

    public set typ(value: string) {
        this._typ = value;
    }

    public get gesuchsperiode(): TSGesuchsperiode {
        return this._gesuchsperiode;
    }

    public set gesuchsperiode(value: TSGesuchsperiode) {
        this._gesuchsperiode = value;
    }

    public get eingangsdatum(): moment.Moment {
        return this._eingangsdatum;
    }

    public set eingangsdatum(value: moment.Moment) {
        this._eingangsdatum = value;
    }

    public get eingangsdatumSTV(): moment.Moment {
        return this._eingangsdatumSTV;
    }

    public set eingangsdatumSTV(value: moment.Moment) {
        this._eingangsdatumSTV = value;
    }

    public get betreuungsangebotTyp(): TSBetreuungsangebotTyp {
        return this._betreuungsangebotTyp;
    }

    public set betreuungsangebotTyp(value: TSBetreuungsangebotTyp) {
        this._betreuungsangebotTyp = value;
    }

    public get institution(): TSInstitution {
        return this._institution;
    }

    public set institution(value: TSInstitution) {
        this._institution = value;
    }

    public get gemeinde(): string {
        return this._gemeinde;
    }

    public set gemeinde(value: string) {
        this._gemeinde = value;
    }
}
