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
import {TSAntragStatus} from './enums/TSAntragStatus';
import {TSAbstractMutableEntity} from './TSAbstractMutableEntity';
import TSBenutzer from './TSBenutzer';

export default class TSAntragStatusHistory extends TSAbstractMutableEntity {

    private _gesuchId: string;
    private _benutzer: TSBenutzer;
    private _timestampVon: moment.Moment;
    private _timestampBis: moment.Moment;
    private _status: TSAntragStatus;

    public constructor(gesuchId?: string,
                       benutzer?: TSBenutzer,
                       timestampVon?: moment.Moment,
                       timestampBis?: moment.Moment,
                       status?: TSAntragStatus) {
        super();
        this._gesuchId = gesuchId;
        this._benutzer = benutzer;
        this._timestampVon = timestampVon;
        this._timestampBis = timestampBis;
        this._status = status;
    }

    public get gesuchId(): string {
        return this._gesuchId;
    }

    public set gesuchId(value: string) {
        this._gesuchId = value;
    }

    public get benutzer(): TSBenutzer {
        return this._benutzer;
    }

    public set benutzer(value: TSBenutzer) {
        this._benutzer = value;
    }

    public get timestampVon(): moment.Moment {
        return this._timestampVon;
    }

    public set timestampVon(value: moment.Moment) {
        this._timestampVon = value;
    }

    public get timestampBis(): moment.Moment {
        return this._timestampBis;
    }

    public set timestampBis(value: moment.Moment) {
        this._timestampBis = value;
    }

    public get status(): TSAntragStatus {
        return this._status;
    }

    public set status(value: TSAntragStatus) {
        this._status = value;
    }
}
