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

import {Pipe, PipeTransform} from '@angular/core';

@Pipe({
    name: 'ebeguNumber'
})
export class EbeguNumberPipe implements PipeTransform {

    public static stringToNumber(str: string): number | undefined | null {
        if (str) {
            return Number(EbeguNumberPipe.formatFromNumberString(str));
        }
        return null;  // null zurueckgeben und nicht undefined denn sonst wird ein ng-parse error erzeugt
    }

    private static formatFromNumberString(numberString: string): string {
        return numberString.split('\'').join('').split(',').join('');
    }

    public static formatToNumberString(valueString: string): string {
        if (valueString !== null && valueString !== undefined) {
            const parts = valueString.split('.');
            parts[0] = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, '\'');
            return parts.join('.');
        }
        return valueString;
    }

    public static sanitizeInputString(input: string, float: boolean): string {
        let formattedInput = input;

        if (input) {
            let sign = '';
            if (input.indexOf('-') === 0) {
                // if negative allowed, get sign
                sign = '-';
                formattedInput = input.substr(1); // get just the number part
            }

            formattedInput = float ?
                EbeguNumberPipe.sanitizeFloatString(formattedInput, sign) :
                EbeguNumberPipe.sanitizeIntString(formattedInput, sign);
        }
        return formattedInput;
    }

    private static sanitizeFloatString(transformedInput: string, sign: string): string {
        // removes all chars that are not a digit or a point
        let result = transformedInput.replace(/([^0-9|.])+/g, '');
        if (result) {
            const pointIndex = result.indexOf('.');
            // only parse if there is either no floating point or the floating point is not at the end. Also dont parse
            // if 0 at end
            if (pointIndex === -1
                || (pointIndex !== (result.length - 1) && result.lastIndexOf('0') !== (result.length - 1))) {
                // parse to float to remove unwanted  digits like leading zeros and then back to string
                result = parseFloat(result).toString();
            }
        }
        result = sign + result; // add sign to raw number
        return result;
    }

    private static sanitizeIntString(transformedInput: string, sign: string): string {
        let result = transformedInput.replace(/\D+/g, ''); // removes all "not digit"
        if (result) {
            // parse to int to remove not wanted digits like leading zeros and then back to string
            result = parseInt(result, 10).toString();
        }
        result = sign + result; // add sign to raw number

        return result;
    }

    private static numberToString(num: number): string {
        if (num || num === 0) {
            return EbeguNumberPipe.formatToNumberString(num.toString());
        }
        return '';
    }

    public transform(num: number): string {
        return EbeguNumberPipe.numberToString(num);
    }
}
