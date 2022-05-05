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
import {TSFerienname} from './enums/TSFerienname';
import {TSAbstractMutableEntity} from './TSAbstractMutableEntity';
import {TSBelegungFerieninselTag} from './TSBelegungFerieninselTag';
import {TSFerieninselZeitraum} from './TSFerieninselZeitraum';
import {TSGesuchsperiode} from './TSGesuchsperiode';

export class TSFerieninselStammdaten extends TSAbstractMutableEntity {

    private _ferienname: TSFerienname;
    private _zeitraumList: TSFerieninselZeitraum[] = [];
    private _anmeldeschluss: moment.Moment;
    private _gesuchsperiode: TSGesuchsperiode;
    private _ferienActive: boolean;
    private _potenzielleFerieninselTageFuerBelegung: TSBelegungFerieninselTag[] = [];
    private _potenzielleFerieninselTageFuerBelegungMorgenmodul: TSBelegungFerieninselTag[] = [];

    public constructor() {
        super();
    }

    public get ferienname(): TSFerienname {
        return this._ferienname;
    }

    public set ferienname(value: TSFerienname) {
        this._ferienname = value;
    }

    public get zeitraumList(): TSFerieninselZeitraum[] {
        return this._zeitraumList;
    }

    public set zeitraumList(value: TSFerieninselZeitraum[]) {
        this._zeitraumList = value;
    }

    public get anmeldeschluss(): moment.Moment {
        return this._anmeldeschluss;
    }

    public set anmeldeschluss(value: moment.Moment) {
        this._anmeldeschluss = value;
    }

    public get gesuchsperiode(): TSGesuchsperiode {
        return this._gesuchsperiode;
    }

    public set gesuchsperiode(value: TSGesuchsperiode) {
        this._gesuchsperiode = value;
    }

    public get potenzielleFerieninselTageFuerBelegung(): TSBelegungFerieninselTag[] {
        return this._potenzielleFerieninselTageFuerBelegung;
    }

    public set potenzielleFerieninselTageFuerBelegung(value: TSBelegungFerieninselTag[]) {
        this._potenzielleFerieninselTageFuerBelegung = value;
    }

    public get potenzielleFerieninselTageFuerBelegungMorgenmodul(): TSBelegungFerieninselTag[] {
        return this._potenzielleFerieninselTageFuerBelegungMorgenmodul;
    }

    public set potenzielleFerieninselTageFuerBelegungMorgenmodul(value: TSBelegungFerieninselTag[]) {
        this._potenzielleFerieninselTageFuerBelegungMorgenmodul = value;
    }

    public get ferienActive(): boolean {
        return this._ferienActive;
    }

    public set ferienActive(value: boolean) {
        this._ferienActive = value;
    }
}
