/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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
import {TSGesuchsperiodeStatus} from '../models/enums/TSGesuchsperiodeStatus';
import {TSAbstractEntity} from '../models/TSAbstractEntity';
import {TSFall} from '../models/TSFall';
import {TSGemeinde} from '../models/TSGemeinde';
import {TSGesuchsperiode} from '../models/TSGesuchsperiode';
import {TSDateRange} from '../models/types/TSDateRange';
import {EbeguUtil} from './EbeguUtil';
import {MathUtil} from './MathUtil';
import {TestDataUtil} from './TestDataUtil.spec';
import IProvideService = angular.auto.IProvideService;

/* eslint-disable no-magic-numbers */
describe('MathUtil', () => {
    const baseValue = 192526.95;
    const toSubtract = 62534.95;
    const toAdd = 0.95;

    describe('subtractFloatPrecisionSafe', () => {
        it('should subtract two non-binary representable values with precision 0', () => {
            expect(MathUtil.subtractFloatPrecisionSafe(baseValue, toSubtract, 0)).toBe(129992);
        });
        it('should subtract two non-binary representable values with precision 1', () => {
            expect(MathUtil.subtractFloatPrecisionSafe(baseValue, toSubtract, 1)).toBe(129992.0);
        });
        it('should subtract two non-binary representable values with precision 2', () => {
            expect(MathUtil.subtractFloatPrecisionSafe(baseValue, toSubtract, 2)).toBe(129992.00);
        });

        it('should be chainable', () => {
            const result = MathUtil.subtractFloatPrecisionSafe(
                MathUtil.subtractFloatPrecisionSafe(192526.95, 0.5),
                62534.45,
            );
            expect(result).toBe(129992.00);
        });
    });

    describe('subtractArrayFloatPrecisionSafe', () => {
        it('should subtract array of 1 value with precision 0', () => {
            expect(MathUtil.subtractArrayFloatPrecisionSafe(
                baseValue,
                [toSubtract],
                0,
            )).toBe(129992);
        });
        it('should subtract array of 1 value with precision 1', () => {
            expect(MathUtil.subtractArrayFloatPrecisionSafe(
                baseValue,
                [toSubtract],
                0,
            )).toBe(129992.0);
        });
        it('should subtract array of 2 values with default precision 2', () => {
            expect(MathUtil.subtractArrayFloatPrecisionSafe(
                baseValue,
                [0.5, 62534.45]
            )).toBe(129992.00);
        });
    });

    describe('addFloatPrecisionSafe', () => {
        it('should add two non-binary representable values with precision 0', () => {
            expect(MathUtil.addFloatPrecisionSafe(baseValue, toAdd, 0)).toBe(192528);
        });
        it('should add two non-binary representable values with precision 1', () => {
            expect(MathUtil.addFloatPrecisionSafe(baseValue, toAdd, 1)).toBe(192527.9);
        });
        it('should add two non-binary representable values with precision 2', () => {
            expect(MathUtil.addFloatPrecisionSafe(baseValue, toAdd, 2)).toBe(192527.90);
        });

        it('should be chainable', () => {
            const result = MathUtil.addFloatPrecisionSafe(
                MathUtil.addFloatPrecisionSafe(baseValue, 0.5),
                0.45,
            );
            expect(result).toBe(192527.90);
        });
    });

    describe('addArrayFloatPrecisionSafe', () => {
        it('should add array of 1 value with precision 0', () => {
            expect(MathUtil.addArrayFloatPrecisionSafe(
                baseValue,
                [toAdd],
                0,
            )).toBe(192528);
        });
        it('should add array of 1 value with precision 1', () => {
            expect(MathUtil.addArrayFloatPrecisionSafe(
                baseValue,
                [toAdd],
                0,
            )).toBe(192528);
        });
        it('should add array of 2 values with default precision 2', () => {
            expect(MathUtil.addArrayFloatPrecisionSafe(
                baseValue,
                [0.50, 0.45]
            )).toBe(192527.90);
        });
    });
});
