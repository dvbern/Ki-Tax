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

    constructor(gueltigAb?: moment.Moment, gueltigBis?: moment.Moment) {
        this._gueltigAb = gueltigAb;
        this._gueltigBis = gueltigBis;
    }

    get gueltigAb(): moment.Moment {
        return this._gueltigAb;
    }

    set gueltigAb(value: moment.Moment) {
        this._gueltigAb = value;
    }

    get gueltigBis(): moment.Moment {
        return this._gueltigBis;
    }

    set gueltigBis(value: moment.Moment) {
        this._gueltigBis = value;
    }

    /**
     * Returns true if the given date is in the daterange of the current TSDateRange object.
     */
    public isInDateRange(date: moment.Moment): boolean {
        return date.isBefore(this.gueltigBis) && date.isAfter(this.gueltigAb);
    }
}
