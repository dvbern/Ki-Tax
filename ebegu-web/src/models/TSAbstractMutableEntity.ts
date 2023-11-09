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

import {TSAbstractEntity} from './TSAbstractEntity';

export class TSAbstractMutableEntity extends TSAbstractEntity {

    private _vorgaengerId: string;

    public get vorgaengerId(): string {
        return this._vorgaengerId;
    }

    public set vorgaengerId(value: string) {
        this._vorgaengerId = value;
    }

    public hasVorgaenger(): boolean {
        return this.vorgaengerId !== null && this.vorgaengerId !== undefined;
    }

    public deepCopyTo(target: TSAbstractMutableEntity): TSAbstractMutableEntity {
        super.deepCopyTo(target);
        target.vorgaengerId = this.vorgaengerId;
        return target;
    }
}
