/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import * as moment from 'moment';
import {TSRueckforderungDokumentTyp} from './enums/TSRueckforderungDokumentTyp';

import {TSUploadFile} from './TSUploadFile';

export class TSRueckforderungDokument extends TSUploadFile {

    private _rueckforderungDokumentTyp: TSRueckforderungDokumentTyp;

    public constructor(timestampUpload?: moment.Moment, rueckforderungDokumentTyp?: TSRueckforderungDokumentTyp) {
        super(timestampUpload);
        this._rueckforderungDokumentTyp = rueckforderungDokumentTyp;
    }

    public get rueckforderungDokumentTyp(): TSRueckforderungDokumentTyp {
        return this._rueckforderungDokumentTyp;
    }

    public set rueckforderungDokumentTyp(value: TSRueckforderungDokumentTyp) {
        this._rueckforderungDokumentTyp = value;
    }
}
