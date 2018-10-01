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
import TSGesuch from './TSGesuch';
import {TSMahnungTyp} from './enums/TSMahnungTyp';
import * as moment from 'moment';

export default class TSMahnung extends TSAbstractMutableEntity {

    private _gesuch: TSGesuch;
    private _mahnungTyp: TSMahnungTyp;
    private _datumFristablauf: moment.Moment;
    private _bemerkungen: string;
    private _timestampAbgeschlossen: moment.Moment;
    private _abgelaufen: boolean;

    public constructor(gesuch?: TSGesuch, mahnungTyp?: TSMahnungTyp, datumFristablauf?: moment.Moment, bemerkungen?: string,
                       timestampAbgeschlossen?: moment.Moment, abgelaufen?: boolean) {
        super();
        this._gesuch = gesuch;
        this._mahnungTyp = mahnungTyp;
        this._datumFristablauf = datumFristablauf;
        this._bemerkungen = bemerkungen;
        this._timestampAbgeschlossen = timestampAbgeschlossen;
        this._abgelaufen = abgelaufen;
    }

    public get gesuch(): TSGesuch {
        return this._gesuch;
    }

    public set gesuch(value: TSGesuch) {
        this._gesuch = value;
    }

    public get mahnungTyp(): TSMahnungTyp {
        return this._mahnungTyp;
    }

    public set mahnungTyp(value: TSMahnungTyp) {
        this._mahnungTyp = value;
    }

    public get datumFristablauf(): moment.Moment {
        return this._datumFristablauf;
    }

    public set datumFristablauf(value: moment.Moment) {
        this._datumFristablauf = value;
    }

    public get bemerkungen(): string {
        return this._bemerkungen;
    }

    public set bemerkungen(value: string) {
        this._bemerkungen = value;
    }

    public get timestampAbgeschlossen(): moment.Moment {
        return this._timestampAbgeschlossen;
    }

    public set timestampAbgeschlossen(value: moment.Moment) {
        this._timestampAbgeschlossen = value;
    }

    public get abgelaufen(): boolean {
        return this._abgelaufen;
    }

    public set abgelaufen(value: boolean) {
        this._abgelaufen = value;
    }
}
