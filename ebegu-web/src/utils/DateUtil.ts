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
import {CONSTANTS} from '../app/core/constants/CONSTANTS';
import {LogFactory} from '../app/core/logging/LogFactory';
import Moment = moment.Moment; // kann das über ein anderes Import Format gelöst werden (import ... from 'moment')?

const LOG = LogFactory.createLog('DateUtil');

export class DateUtil {

    /**
     * @param localDateTimeString string with format YYYY-MM-DDTHH:mm:ss.SSS
     */
    public static localDateTimeToMoment(localDateTimeString: string): Moment | undefined {
        // cannot use EbeguUtil. cycle dependency
        if (localDateTimeString === null || localDateTimeString === undefined) {
            return undefined;
        }
        const formats = ['YYYY-MM-DDTHH:mm:ss.SSS', 'YYYY-MM-DDTHH:mm:ss', 'YYYY-MM-DDTHH:mm:ss.SSSZ'];
        const theMoment = moment(localDateTimeString, formats, true);
        if (!theMoment.isValid()) {
            LOG.warn('Trying to parse an invalid date to moment', theMoment);

            return undefined;
        }

        return theMoment;
    }

    /**
     * Calls momentToLocalDateFormat with the format by default 'YYYY-MM-DD'
     */
    public static momentToLocalDate(aMoment: Moment): string {
        if (aMoment && aMoment.isValid()) {
            return DateUtil.momentToLocalDateFormat(aMoment, 'YYYY-MM-DD');
        }
        return (aMoment as any);
    }

    /**
     * @param aMoment time instance
     * @param format the format
     * @returns a Date (YYYY-MM-DD) representation of the given moment. NULL when aMoment is invalid
     */
    public static momentToLocalDateFormat(aMoment: Moment, format?: string): string | undefined {
        if (!aMoment) {
            return undefined;
        }

        return moment(aMoment).startOf('day').format(format);
    }

    /**
     * @param aMoment time instance
     * @param format format for the time
     * @returns a Date (YYYY-MM-DD) representation of the given moment. undefined when aMoment is invalid
     */
    public static momentToLocalDateTimeFormat(aMoment: Moment, format: string): string | undefined {
        if (!aMoment) {
            return undefined;
        }

        return moment(aMoment).format(format);
    }

    /**
     * @param aMoment time instance
     * @returns a Date (YYYY-MM-DD) representation of the given moment. NULL when aMoment is invalid
     */
    public static momentToLocalDateTime(aMoment: Moment): string | undefined {
        return DateUtil.momentToLocalDateTimeFormat(aMoment, 'YYYY-MM-DDTHH:mm:ss.SSS');
    }

    /**
     * @param localDateString string with format YYYY-MM-DD
     */
    public static localDateToMoment(localDateString: string): Moment | undefined {
        const theMoment = moment(localDateString, 'YYYY-MM-DD', true);

        return theMoment.isValid() ? theMoment : undefined;
    }

    public static today(): Moment {
        return moment().startOf('day');
    }

    public static now(): Moment {
        return moment();
    }

    public static currentYear(): number {
        return moment().year();
    }

    /**
     * Vergleicht 2 Moments. Date und Time werden beruecksichtigt.
     */
    public static compareDateTime(a: Moment, b: Moment): number {
        if (a.isBefore(b)) {
            return -1;
        }

        if (a.isSame(b)) {
            return 0;
        }

        return 1;
    }

    public static calculatePeriodenStartdatumString(startdatum: Moment): string {
        // wenn nach 1.8. -> periode = year / year + 1
        // wenn vor 1.8. -> periode = year -1 / year
        const year = startdatum.month() > 6 ? startdatum.year() : startdatum.year() - 1;

        return `${year} / ${year + 1}`;
    }

    public static momentToHoursAndMinutes(date: Moment): string {
        return date.format('HH.mm');
    }

    public static hoursAndMinutesToMoment(hoursAndMinutes: any): Moment {
        const split = hoursAndMinutes.split(':');
        const hours = split[0];
        const minutes = split[1];
        return moment({hour: hours, minute: minutes});
    }

    public static endOfTime(): Moment {
        return moment(CONSTANTS.END_OF_TIME_STRING, 'DD.MM.YYYY');
    }
}
