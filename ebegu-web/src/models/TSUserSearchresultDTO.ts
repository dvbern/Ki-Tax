/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
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

import TSBenutzer from './TSBenutzer';

export default class TSUserSearchresultDTO {

    private _userDTOs: Array<TSBenutzer>;
    private _totalResultSize: number;

    public constructor(userDTOs?: Array<TSBenutzer>, totalResultSize?: number) {
        this._userDTOs = userDTOs;
        this._totalResultSize = totalResultSize;
    }

    public get userDTOs(): Array<TSBenutzer> {
        return this._userDTOs;
    }

    public set userDTOs(value: Array<TSBenutzer>) {
        this._userDTOs = value;
    }

    public get totalResultSize(): number {
        return this._totalResultSize;
    }

    public set totalResultSize(value: number) {
        this._totalResultSize = value;
    }
}
