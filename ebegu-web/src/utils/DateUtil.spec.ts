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
import {LogLevel} from '../app/core/logging/log-level';
import {LogFactory} from '../app/core/logging/LogFactory';
import {DateUtil} from './DateUtil';

/* eslint-disable */
describe('dateUtil', () => {

    describe('localDateToMoment()', () => {
        it('should return null for invalid input', () => {
            // avoid log spam
            LogFactory.setModuleLevel('DateUtil', LogLevel.ERROR);
            expect(DateUtil.localDateTimeToMoment(undefined)).toEqual(undefined);
            expect(DateUtil.localDateTimeToMoment(null)).toEqual(undefined);
            expect(DateUtil.localDateTimeToMoment('')).toEqual(undefined);
            expect(DateUtil.localDateTimeToMoment('invalid format')).toEqual(undefined);
            expect(DateUtil.localDateTimeToMoment('1995-12-25')).toEqual(undefined);
            LogFactory.setModuleLevel('DateUtil', LogLevel.WARN);
        });

        it('should return a valid moment', () => {
            const actual = DateUtil.localDateTimeToMoment('1995-12-25T16:06:34.564');
            const expected = moment('1995-12-25T16:06:34.564', 'YYYY-MM-DDTHH:mm:ss.SSS', true);
            expect(expected.isSame(actual)).toBeTruthy();
        });
    });
    describe('compareDateTime()', () => {
        it('DATETIME: a date should be before b date', () => {
            const a = DateUtil.localDateTimeToMoment('1995-12-24T14:06:34.564');
            const b = DateUtil.localDateTimeToMoment('1995-12-24T16:06:34.564');
            expect(DateUtil.compareDateTime(a, b)).toBe(-1);
        });
        it('DATETIME: a date should be the same as b date', () => {
            const a = DateUtil.localDateTimeToMoment('1995-12-24T16:06:34.564');
            const b = DateUtil.localDateTimeToMoment('1995-12-24T16:06:34.564');
            expect(DateUtil.compareDateTime(a, b)).toBe(0);
        });
        it('DATETIME: a date should be after b date', () => {
            const a = DateUtil.localDateTimeToMoment('1995-12-24T18:06:34.564');
            const b = DateUtil.localDateTimeToMoment('1995-12-24T16:06:34.564');
            expect(DateUtil.compareDateTime(a, b)).toBe(1);
        });

        it('DATE: a date should be before b date', () => {
            const a = DateUtil.localDateToMoment('1995-12-23');
            const b = DateUtil.localDateToMoment('1995-12-24');
            expect(DateUtil.compareDateTime(a, b)).toBe(-1);
        });
        it('DATE: a date should be the same as b date', () => {
            const a = DateUtil.localDateToMoment('1995-12-24');
            const b = DateUtil.localDateToMoment('1995-12-24');
            expect(DateUtil.compareDateTime(a, b)).toBe(0);
        });
        it('DATE: a date should be after b date', () => {
            const a = DateUtil.localDateToMoment('1995-12-25');
            const b = DateUtil.localDateToMoment('1995-12-24');
            expect(DateUtil.compareDateTime(a, b)).toBe(1);
        });
    });
});
