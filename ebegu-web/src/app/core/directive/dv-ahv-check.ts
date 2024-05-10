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

import {IAugmentedJQuery, IDirective, IDirectiveFactory, IDirectiveLinkFn, IScope} from 'angular';
import {EbeguUtil} from '../../../utils/EbeguUtil';

export class DvAhvCheck implements IDirective {
    public static $inject = ['CONSTANTS'];

    public restrict = 'A';
    public require = 'ngModel';
    public length: number;
    public link: IDirectiveLinkFn;

    public constructor(CONSTANTS: any) {
        this.length = CONSTANTS.MAX_LENGTH;
        this.link = (_scope: IScope, _element: IAugmentedJQuery, _attrs, ctrl: any) => {
            if (!ctrl) {
                return;
            }

            ctrl.$validators.dvAhvCheck = (_modelValue: any, viewValue: any) => {
                if (EbeguUtil.isNullOrUndefined(viewValue)) {
                    return true;
                }
                const ahvlenght = 13;
                const START_DIGITS = '756';
                const digits = viewValue
                    .replace(/\./g, '')
                    .split('')
                    .map((digit: string) => parseInt(digit, 10));

                if (digits.length !== ahvlenght) {
                    return false;
                }

                const relevantDigits = digits.slice(0,12).reverse();

                const relevantDigitsSum = relevantDigits.reduce((total: number, next: number, index: number) => {
                    const multiplier = index % 2 === 0 ? 3 : 1;
                    return total + next * multiplier;
                }, 0);

                const relevantDigitsRounded = Math.ceil(relevantDigitsSum / 10) * 10;
                const calculatedCheckDigit = relevantDigitsRounded - relevantDigitsSum;
                const checkDigit = digits[12];

                const startDigits = viewValue.slice(0, 3);

                return checkDigit === calculatedCheckDigit && startDigits === START_DIGITS;
            };
        };
    }

    public static factory(): IDirectiveFactory {
        const directive = (CONSTANTS: any) => new DvAhvCheck(CONSTANTS);
        directive.$inject = ['CONSTANTS'];
        return directive;
    }
}
