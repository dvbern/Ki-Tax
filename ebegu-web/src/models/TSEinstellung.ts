/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

import {TSEinstellungKey} from './enums/TSEinstellungKey';
import {TSAbstractDateRangedEntity} from './TSAbstractDateRangedEntity';
import {TSDateRange} from './types/TSDateRange';

export default class TSEinstellung extends TSAbstractDateRangedEntity {

    private _key: TSEinstellungKey;
    private _value: string;
    private _description: string;

    constructor(key?: TSEinstellungKey, value?: string, description?: string, gueltigkeit?: TSDateRange) {
        super(gueltigkeit);
        this._key = key;
        this._value = value;
        this._description = description;
    }

    public get key(): TSEinstellungKey {
        return this._key;
    }

    public set key(value: TSEinstellungKey) {
        this._key = value;
    }

    public get value(): string {
        return this._value;
    }

    public set value(value: string) {
        this._value = value;
    }

    public get description(): string {
        return this._description;
    }

    public set description(value: string) {
        this._description = value;
    }
}
