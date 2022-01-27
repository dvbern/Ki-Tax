/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

import {EbeguNumberPipe} from './ebegu-number.pipe';

describe('EbeguDatePipe', () => {
    it('create an instance', () => {
        const pipe = new EbeguNumberPipe();
        expect(pipe).toBeTruthy();
    });
    it('should transform number into string of format "#\'###"', () => {
        const pipe = new EbeguNumberPipe();
        const num = 1000;
        expect(pipe.transform(num)).toBe('1\'000');
    });
    it('should transform string of format "####" into string of format "#\'###"', () => {
        expect(EbeguNumberPipe.formatToNumberString('1000')).toBe('1\'000');
        expect(EbeguNumberPipe.formatToNumberString('16')).toBe('16');
        expect(EbeguNumberPipe.formatToNumberString('15762')).toBe('15\'762');
        expect(EbeguNumberPipe.formatToNumberString('-762487')).toBe('-762\'487');
        expect(EbeguNumberPipe.formatToNumberString('-163.50')).toBe('-163.50');
        expect(EbeguNumberPipe.formatToNumberString('-47851.48')).toBe('-47\'851.48');
    });
    it('should remove not nummeric chars from string', () => {
        expect(EbeguNumberPipe.sanitizeInputString('1\'000', true)).toBe('1000');
        expect(EbeguNumberPipe.sanitizeInputString('16a', true)).toBe('16');
        expect(EbeguNumberPipe.sanitizeInputString('157-62', true)).toBe('15762');
        expect(EbeguNumberPipe.sanitizeInputString('-762487', true)).toBe('-762487');
        expect(EbeguNumberPipe.sanitizeInputString('a163.50', true)).toBe('163.50');
        expect(EbeguNumberPipe.sanitizeInputString('-47851.48', true)).toBe('-47851.48');
    });
    it('should remove not nummeric chars from string no floating point allowed', () => {
        expect(EbeguNumberPipe.sanitizeInputString('1\'000', false)).toBe('1000');
        expect(EbeguNumberPipe.sanitizeInputString('16a', false)).toBe('16');
        expect(EbeguNumberPipe.sanitizeInputString('157-62', false)).toBe('15762');
        expect(EbeguNumberPipe.sanitizeInputString('-762487', false)).toBe('-762487');
        expect(EbeguNumberPipe.sanitizeInputString('a163.50', false)).toBe('16350');
        expect(EbeguNumberPipe.sanitizeInputString('-4785148', false)).toBe('-4785148');
    });
});
