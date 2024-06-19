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

import {
    IAugmentedJQuery,
    IController,
    IDirective,
    IDirectiveFactory,
    INgModelController
} from 'angular';
import {EbeguUtil} from '../../../../utils/EbeguUtil';
import ITimeoutService = angular.ITimeoutService;

declare let require: any;
declare let angular: any;

export class DVValueinput implements IDirective {
    public restrict = 'E';
    public require = {ngModelCtrl: 'ngModel', dvValueInputCtrl: 'dvValueinput'};
    public scope = {};
    public controller = ValueinputController;
    public controllerAs = 'vm';
    public bindToController = {
        ngModel: '=',
        inputId: '@',
        ngRequired: '<',
        ngDisabled: '<',
        allowNegative: '<',
        float: '<',
        fixedDecimals: '@',
        dvOnBlur: '&?',
        inputName: '@?'
    };
    public template = require('./dv-valueinput.html');

    public static factory(): IDirectiveFactory {
        const directive = () => new DVValueinput();
        // @ts-ignore
        directive.$inject = [];
        return directive;
    }
}

export class ValueinputController implements IController {
    public static $inject: string[] = ['$timeout'];

    public valueinput: string;
    public ngModelCtrl: INgModelController;
    public valueRequired: boolean;
    public ngRequired: boolean;
    public allowNegative: boolean;
    public float: boolean;
    public fixedDecimals: number;
    public dvOnBlur: () => void;

    public constructor(private readonly $timeout: ITimeoutService) {}

    private static numberToString(num: number): string {
        if (num || num === 0) {
            return ValueinputController.formatToNumberString(num.toString());
        }
        return '';
    }

    private static stringToNumber(str: string): number | undefined | null {
        if (str) {
            return Number(ValueinputController.formatFromNumberString(str));
        }
        return null; // null zurueckgeben und nicht undefined denn sonst wird ein ng-parse error erzeugt
    }

    private static formatToNumberString(valueString: string): string {
        if (valueString !== null && valueString !== undefined) {
            const parts = valueString.split('.');
            parts[0] = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, "'");
            return parts.join('.');
        }
        return valueString;
    }

    private static formatFromNumberString(numberString: string): string {
        return numberString.split("'").join('').split(',').join('');
    }

    // beispiel wie man auf changes eines attributes von aussen reagieren kann
    public $onChanges(changes: any): void {
        if (changes.ngRequired && !changes.ngRequired.isFirstChange()) {
            this.valueRequired = changes.ngRequired.currentValue;
        }
    }

    public $onInit(): void {
        if (!this.ngModelCtrl) {
            return;
        }

        if (this.ngRequired) {
            this.valueRequired = this.ngRequired;
        }

        if (!this.allowNegative) {
            this.allowNegative = false;
        }

        if (!this.float) {
            this.float = false;
        }

        this.ngModelCtrl.$render = () => {
            this.valueinput = this.ngModelCtrl.$viewValue;
        };
        this.ngModelCtrl.$formatters.unshift(
            ValueinputController.numberToString
        );
        this.ngModelCtrl.$parsers.push(ValueinputController.stringToNumber);

        this.ngModelCtrl.$validators.valueinput = (modelValue, viewValue) => {
            // if not required and view value empty, it's ok...
            if (!this.valueRequired && !viewValue) {
                return true;
            }

            const value =
                modelValue || ValueinputController.stringToNumber(viewValue);
            const maxValue = 999999999999;

            return !isNaN(Number(value)) &&
                Number(value) < maxValue &&
                this.allowNegative
                ? true
                : Number(value) >= 0;
        };
    }

    /**
     * on blur setzen wir den formatierten "string" ins feld
     */
    public updateModelValueBlur(): void {
        this.updateModelValue();
        this.ngModelCtrl.$setTouched();
    }

    /**
     * onFocus schreiben wir den string als zahl ins feld und setzen den cursor ans ende des inputs
     */
    public handleFocus(event: any): void {
        this.valueinput = this.sanitizeInputString();
        if (!event) {
            return;
        }

        const angEle: IAugmentedJQuery = angular.element(event.target);
        const element: any = angEle[0];
        this.$timeout(() => {
            // If this function exists...
            if (element.setSelectionRange) {
                // ... then use it
                const range = 999999;
                element.setSelectionRange(range, range);
            } else {
                // ... otherwise replace the contents with itself
                // (Doesn't work in Google Chrome)
                element.val(element.val());
            }
        });
    }

    public updateModelValue(): void {
        // set the number as formatted string to the model
        if (this.valueinput) {
            // if a number of fixed decimals are requested make the transformation on blur
            if (this.float && !isNaN(this.fixedDecimals)) {
                this.valueinput = parseFloat(this.valueinput).toFixed(
                    this.fixedDecimals
                );
            }
            const valueString = ValueinputController.formatFromNumberString(
                this.valueinput
            );
            this.valueinput =
                ValueinputController.formatToNumberString(valueString);
        }
        this.ngModelCtrl.$setViewValue(this.valueinput);
        if (this.dvOnBlur) {
            // userdefined onBlur event
            this.dvOnBlur();
        }
    }

    public removeNotDigits(): void {
        const transformedInput = this.sanitizeInputString();

        // neuen wert ins model schreiben
        if (
            EbeguUtil.isNotNullOrUndefined(transformedInput) &&
            transformedInput !== this.ngModelCtrl.$viewValue
        ) {
            // setting the new raw number into the invisible parentmodel
            this.ngModelCtrl.$setViewValue(
                ValueinputController.formatToNumberString(transformedInput)
            );
            this.ngModelCtrl.$render();
        }
        if (this.valueinput !== transformedInput) {
            this.valueinput = transformedInput;
        }
    }

    private sanitizeInputString(): string {
        let transformedInput = this.valueinput;
        if (this.valueinput) {
            let sign = '';
            if (
                this.allowNegative &&
                this.valueinput &&
                this.valueinput.indexOf('-') === 0
            ) {
                // if negative allowed, get sign
                sign = '-';
                transformedInput = transformedInput.substr(1); // get just the number part
            }

            transformedInput = this.float
                ? this.sanitizeFloatString(transformedInput, sign)
                : this.sanitizeIntString(transformedInput, sign);
        }
        return transformedInput;
    }

    private sanitizeFloatString(
        transformedInput: string,
        sign: string
    ): string {
        // removes all chars that are not a digit or a point
        let result = transformedInput.replace(/([^0-9|.])+/g, '');
        if (result) {
            const pointIndex = result.indexOf('.');
            // only parse if there is either no floating point or the floating point is not at the end. Also dont parse
            // if 0 at end
            if (
                pointIndex === -1 ||
                (pointIndex !== result.length - 1 &&
                    result.lastIndexOf('0') !== result.length - 1)
            ) {
                // parse to float to remove unwanted  digits like leading zeros and then back to string
                result = parseFloat(result).toString();
            }
        }
        result = sign + result; // add sign to raw number
        return result;
    }

    private sanitizeIntString(transformedInput: string, sign: string): string {
        let result = transformedInput.replace(/\D+/g, ''); // removes all "not digit"
        if (result) {
            // parse to int to remove not wanted digits like leading zeros and then back to string
            result = parseInt(result, 10).toString();
        }
        result = sign + result; // add sign to raw number

        return result;
    }
}
