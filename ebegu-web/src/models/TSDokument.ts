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
import TSBenutzer from './TSBenutzer';
import TSFile from './TSFile';

export default class TSDokument extends TSFile {

    private _timestampUpload: moment.Moment;
    private _userUploaded: TSBenutzer;

    public constructor(timestampUpload?: moment.Moment, userUploaded?: TSBenutzer) {
        super();
        this._timestampUpload = timestampUpload;
        this._userUploaded = userUploaded;
    }

    public get timestampUpload(): moment.Moment {
        return this._timestampUpload;
    }

    public set timestampUpload(value: moment.Moment) {
        this._timestampUpload = value;
    }

    public get userUploaded(): TSBenutzer {
        return this._userUploaded;
    }

    public set userUploaded(value: TSBenutzer) {
        this._userUploaded = value;
    }
}
