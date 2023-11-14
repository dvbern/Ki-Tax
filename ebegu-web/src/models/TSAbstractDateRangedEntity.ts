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

import {EbeguUtil} from '../utils/EbeguUtil';
import {TSAbstractMutableEntity} from './TSAbstractMutableEntity';
import {TSDateRange} from './types/TSDateRange';

export class TSAbstractDateRangedEntity extends TSAbstractMutableEntity {

    private _gueltigkeit: TSDateRange;

    public constructor(gueltigkeit?: TSDateRange) {
        super();
        this._gueltigkeit = gueltigkeit;
    }

    public get gueltigkeit(): TSDateRange {
        return this._gueltigkeit;
    }

    public set gueltigkeit(value: TSDateRange) {
        this._gueltigkeit = value;
    }

    public deepCopyTo(target: TSAbstractDateRangedEntity): TSAbstractDateRangedEntity {
        super.deepCopyTo(target);
        if (EbeguUtil.isNotNullOrUndefined(this.gueltigkeit)) {
            target.gueltigkeit = new TSDateRange(this.gueltigkeit.gueltigAb?.clone(), this.gueltigkeit.gueltigBis?.clone());
        }

        return target;
    }
}
