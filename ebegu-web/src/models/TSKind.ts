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
import {TSEinschulungTyp} from './enums/TSEinschulungTyp';
import {TSGeschlecht} from './enums/TSGeschlecht';
import {TSKinderabzug} from './enums/TSKinderabzug';
import TSAbstractPersonEntity from './TSAbstractPersonEntity';
import {TSPensumFachstelle} from './TSPensumFachstelle';

export default class TSKind extends TSAbstractPersonEntity {

    private _kinderabzug: TSKinderabzug;
    private _familienErgaenzendeBetreuung: boolean;
    private _mutterspracheDeutsch: boolean;
    private _einschulungTyp: TSEinschulungTyp;
    private _pensumFachstelle: TSPensumFachstelle;

    public constructor(
        vorname?: string,
        nachname?: string,
        geburtsdatum?: moment.Moment,
        geschlecht?: TSGeschlecht,
        kinderabzug?: TSKinderabzug,
        familienErgaenzendeBetreuung?: boolean,
        mutterspracheDeutsch?: boolean,
        pensumFachstelle?: TSPensumFachstelle,
        einschulungTyp?: TSEinschulungTyp,
    ) {

        super(vorname, nachname, geburtsdatum, geschlecht);
        this._kinderabzug = kinderabzug;
        this._familienErgaenzendeBetreuung = familienErgaenzendeBetreuung;
        this._mutterspracheDeutsch = mutterspracheDeutsch;
        this._einschulungTyp = einschulungTyp;
        this._pensumFachstelle = pensumFachstelle;
    }

    public get kinderabzug(): TSKinderabzug {
        return this._kinderabzug;
    }

    public set kinderabzug(value: TSKinderabzug) {
        this._kinderabzug = value;
    }

    public get familienErgaenzendeBetreuung(): boolean {
        return this._familienErgaenzendeBetreuung;
    }

    public set familienErgaenzendeBetreuung(value: boolean) {
        this._familienErgaenzendeBetreuung = value;
    }

    public get mutterspracheDeutsch(): boolean {
        return this._mutterspracheDeutsch;
    }

    public set mutterspracheDeutsch(value: boolean) {
        this._mutterspracheDeutsch = value;
    }

    public get pensumFachstelle(): TSPensumFachstelle {
        return this._pensumFachstelle;
    }

    public set pensumFachstelle(value: TSPensumFachstelle) {
        this._pensumFachstelle = value;
    }

    public get einschulungTyp(): TSEinschulungTyp {
        return this._einschulungTyp;
    }

    public set einschulungTyp(value: TSEinschulungTyp) {
        this._einschulungTyp = value;
    }
}
