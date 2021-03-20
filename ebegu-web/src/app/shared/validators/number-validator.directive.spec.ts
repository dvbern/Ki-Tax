/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import {FormControl} from '@angular/forms';
import {numberValidator, ValidationType} from './number-validator.directive';

describe('numberValidator', () => {
    it('should return an error if 1.5 ist tested for an integer', () => {
        const test = numberValidator(ValidationType.INTEGER)(new FormControl('1.5'));
        expect(test).toEqual({isNotInteger: {value: '1.5'}});
    });
    it('should NOT return an error if 2 ist tested for an integer', () => {
        const test = numberValidator(ValidationType.INTEGER)(new FormControl('2'));
        expect(test).toEqual(null);
    });
    it('should return an error if 1.2 ist tested for half number', () => {
        const test = numberValidator(ValidationType.HALF)(new FormControl('1.2'));
        expect(test).toEqual({isNotHalf: {value: '1.2'}});
    });
    it('should NOT return an error if 1.5 ist tested for half number', () => {
        const test = numberValidator(ValidationType.HALF)(new FormControl('1.5'));
        expect(test).toEqual(null);
    });
    it('should return an error if "abc" ist tested for integer', () => {
        const test = numberValidator(ValidationType.INTEGER)(new FormControl('abc'));
        expect(test).toEqual({isNotInteger: {value: 'abc'}});
    });
    it('should return an error if "abc" ist tested for half number', () => {
        const test = numberValidator(ValidationType.HALF)(new FormControl('abc'));
        expect(test).toEqual({isNotHalf: {value: 'abc'}});
    });
});
