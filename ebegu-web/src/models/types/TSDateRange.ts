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

export class TSDateRange {
    private _gueltigAb: moment.Moment;
    private _gueltigBis: moment.Moment;

    public constructor(gueltigAb?: moment.Moment, gueltigBis?: moment.Moment) {
        this._gueltigAb = gueltigAb;
        this._gueltigBis = gueltigBis;
    }

    public get gueltigAb(): moment.Moment {
        return this._gueltigAb;
    }

    public set gueltigAb(value: moment.Moment) {
        this._gueltigAb = value;
    }

    public get gueltigBis(): moment.Moment {
        return this._gueltigBis;
    }

    public set gueltigBis(value: moment.Moment) {
        this._gueltigBis = value;
    }

    /**
     * Returns true if the given date is in the daterange of the current TSDateRange object.
     * e.g. DateRange 1.8.2023 - 31.7.2023
     * * Date 15.8.2023 is in Date Range
     * * Date 1.8.2023 is in Date Range
     * * Date 31.7.2023 is in Date Range
     */
    public isInDateRange(date: moment.Moment): boolean {
        return (
            date.isSameOrBefore(this.gueltigBis) &&
            date.isSameOrAfter(this.gueltigAb)
        );
    }

    public contains(other: TSDateRange): boolean {
        if (!this.gueltigBis || !other.gueltigBis) {
            return other.gueltigAb.isSameOrAfter(this.gueltigAb);
        }
        return (
            other.gueltigAb.isSameOrAfter(this.gueltigAb) &&
            other.gueltigBis.isSameOrBefore(this.gueltigBis)
        );
    }
}
