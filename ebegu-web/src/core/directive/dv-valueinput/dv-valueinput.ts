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

import {IAugmentedJQuery, IDirective, IDirectiveFactory, IDirectiveLinkFn, INgModelController} from 'angular';
import ITimeoutService = angular.ITimeoutService;
declare let require: any;
declare let angular: any;
const template = require('./dv-valueinput.html');

export class DVValueinput implements IDirective {
    restrict = 'E';
    require: any = {ngModelCtrl: 'ngModel', dvValueInputCtrl: 'dvValueinput'};
    scope = {};
    controller = ValueinputController;
    controllerAs = 'vm';
    bindToController = {
        ngModel: '=',
        inputId: '@',
        ngRequired: '<',
        ngDisabled: '<',
        allowNegative: '<',
        float: '<',
        fixedDecimals: '@',
        dvOnBlur: '&?',
        inputName: '@?',
    };
    template = template;
    link: IDirectiveLinkFn;

    constructor() {

    }

    static factory(): IDirectiveFactory {
        const directive = () => new DVValueinput();
        directive.$inject = [];
        return directive;
    }
}
export class ValueinputController {

    static $inject: string[] = ['$timeout'];
    valueinput: string;
    ngModelCtrl: INgModelController;
    valueRequired: boolean;
    ngRequired: boolean;
    allowNegative: boolean;
    float: boolean;
    fixedDecimals: number;
    dvOnBlur: () => void;
    constructor(private readonly $timeout: ITimeoutService) {
    }

    private static numberToString(num: number): string {
        if (num || num === 0) {
            return ValueinputController.formatToNumberString(num.toString());
        }
        return '';
    }

    private static stringToNumber(string: string): number | undefined  | null {
        if (string) {
            return Number(ValueinputController.formatFromNumberString(string));
        }
        return null;  // null zurueckgeben und nicht undefined denn sonst wird ein ng-parse error erzeugt
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
    $onChanges(changes: any) {
        if (changes.ngRequired && !changes.ngRequired.isFirstChange()) {
            this.valueRequired = changes.ngRequired.currentValue;
        }
    }

    //wird von angular aufgerufen
    $onInit() {
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
        this.ngModelCtrl.$formatters.unshift(ValueinputController.numberToString);
        this.ngModelCtrl.$parsers.push(ValueinputController.stringToNumber);

        this.ngModelCtrl.$validators['valueinput'] = (modelValue: any, viewValue: any) => {
            // if not required and view value empty, it's ok...
            if (!this.valueRequired && !viewValue) {
                return true;
            }

            const value = modelValue || ValueinputController.stringToNumber(viewValue);

            return !isNaN(Number(value)) && (Number(value) < 999999999999) && this.allowNegative ? true : Number(value) >= 0;
        };
    }

    /**
     * on blur setzen wir den formatierten "string" ins feld
     */
    public updateModelValueBlur() {
        this.updateModelValue();
        this.ngModelCtrl.$setTouched();
    }

    /**
     * onFocus schreiben wir den string als zahl ins feld und setzen den cursor ans ende des inputs
     * @param event
     */
    public handleFocus(event: any) {

        this.valueinput = this.sanitizeInputString();
        if (event) {
            const angEle: IAugmentedJQuery = angular.element(event.target); //read raw html element

            const element: any = angEle[0];
            this.$timeout(() => {
                // If this function exists...
                if (element.setSelectionRange) {
                    // ... then use it
                    element.setSelectionRange(999999, 999999);
                } else {
                    // ... otherwise replace the contents with itself
                    // (Doesn't work in Google Chrome)
                    element.val(element.val());
                }
            });
        }

    }

    updateModelValue() {
        //set the number as formatted string to the model
        if (this.valueinput) {
            //if a number of fixed decimals are requested make the transformation on blur
            if (this.float === true && !isNaN(this.fixedDecimals)) {
                this.valueinput = parseFloat(this.valueinput).toFixed(this.fixedDecimals);
            }
            this.valueinput = ValueinputController.formatToNumberString(ValueinputController.formatFromNumberString(this.valueinput));

        }
        this.ngModelCtrl.$setViewValue(this.valueinput);
        if (this.dvOnBlur) { // userdefined onBlur event
            this.dvOnBlur();
        }

    }

    public removeNotDigits(): void {
        const transformedInput = this.sanitizeInputString();

        //neuen wert ins model schreiben
        if (transformedInput && transformedInput !== this.ngModelCtrl.$viewValue) {
            //setting the new raw number into the invisible parentmodel
            this.ngModelCtrl.$setViewValue(ValueinputController.formatToNumberString(transformedInput));
            this.ngModelCtrl.$render();
        }
        if (this.valueinput !== transformedInput) {
            this.valueinput = transformedInput;
        }
    }

    private sanitizeInputString() {
        let transformedInput = this.valueinput;
        if (transformedInput) {
            let sign: string = '';
            if (this.allowNegative === true && this.valueinput && this.valueinput.indexOf('-') === 0) { // if negative allowed, get sign
                sign = '-';
                transformedInput.substr(1); // get just the number part
            }
            if (!this.float) {
                transformedInput = this.sanitizeIntString(transformedInput, sign);
            } else {
                transformedInput = this.sanitizeFloatString(transformedInput, sign);
            }
        }
        return transformedInput;
    }

    private sanitizeFloatString(transformedInput: string, sign: string) {
        // removes all chars that are not a digit or a point
        transformedInput = transformedInput.replace(/([^0-9|\.])+/g, '');
        if (transformedInput) {
            const pointIndex = transformedInput.indexOf('.');
            //only parse if there is either no floating point or the floating point is not at the end. Also dont parse
            // if 0 at end
            if (pointIndex === -1 || (pointIndex !== (transformedInput.length - 1) && transformedInput.lastIndexOf('0') !== (transformedInput.length - 1) )) {
                // parse to float to remove unwanted  digits like leading zeros and then back to string
                transformedInput = parseFloat(transformedInput).toString();
            }
        }
        transformedInput = sign + transformedInput; // add sign to raw number
        return transformedInput;
    }

    private sanitizeIntString(transformedInput: string, sign: string) {
        transformedInput = transformedInput.replace(/\D+/g, ''); // removes all "not digit"
        if (transformedInput) {
            // parse to int to remove not wanted digits like leading zeros and then back to string
            transformedInput = parseInt(transformedInput).toString();
        }
        transformedInput = sign + transformedInput; // add sign to raw number
        return transformedInput;
    }

}
