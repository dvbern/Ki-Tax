/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import * as moment from 'moment';
import {TSAntragStatus} from '../enums/TSAntragStatus';
import {TSAbstractEntity} from '../TSAbstractEntity';
import {TSBenutzer} from '../TSBenutzer';

export class TSLastenausgleichTagesschulenStatusHistory extends TSAbstractEntity {

    private _containerId: string;
    private _benutzer: TSBenutzer;
    private _timestampVon: moment.Moment;
    private _timestampBis: moment.Moment;
    private _status: TSAntragStatus;

    public constructor() {
        super();
    }

    public get containerId(): string {
        return this._containerId;
    }

    public set containerId(value: string) {
        this._containerId = value;
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
