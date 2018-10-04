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

export default class TSFile extends TSAbstractMutableEntity {

    private _filename: string;

    private _filepfad: string;

    private _filesize: string;

    public get filename(): string {
        return this._filename;
    }

    public set filename(value: string) {
        this._filename = value;
    }

    public get filepfad(): string {
        return this._filepfad;
    }

    public set filepfad(value: string) {
        this._filepfad = value;
    }

    public get filesize(): string {
        return this._filesize;
    }

    public set filesize(value: string) {
        this._filesize = value;
    }
}
