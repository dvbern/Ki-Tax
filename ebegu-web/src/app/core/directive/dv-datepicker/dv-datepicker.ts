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

import {IDirective, IDirectiveFactory} from 'angular';
import * as moment from 'moment';
import DateUtil from '../../../../utils/DateUtil';
import IAttributes = angular.IAttributes;
import ILogService = angular.ILogService;
import INgModelController = angular.INgModelController;

const template = require('./dv-datepicker.html');

export class DVDatepicker implements IDirective {
    restrict = 'E';
    require: any = {ngModelCtrl: 'ngModel'};
    scope = {};
    controller = DatepickerController;
    controllerAs = 'vm';
    bindToController = {
        ngModel: '=',
        inputId: '@',
        ngRequired: '<',
        placeholder: '@',
        ngDisabled: '<',
        noFuture: '<?',
        dvOnBlur: '&?',
        dvMinDate: '<?', // Kann als String im Format allowedFormats oder als Moment angegeben werden
        dvMaxDate: '<?'  // Kann als String im Format allowedFormats oder als Moment angegeben werden
    };
    template = template;

    /* constructor() { this.link = this.unboundLink.bind(this); }*/
    static factory(): IDirectiveFactory {
        const directive = () => new DVDatepicker();
        directive.$inject = [];
        return directive;
    }
}

export class DatepickerController {
    static $inject: string[] = ['$log', '$attrs'];
    static allowedFormats: string[] = ['D.M.YYYY', 'DD.MM.YYYY'];
    static defaultFormat: string = 'DD.MM.YYYY';
    date: Date;
    ngModelCtrl: INgModelController;
    dateRequired: boolean;
    ngRequired: boolean;
    placeholder: string;
    dvOnBlur: () => void;
    dvMinDate: any;
    dvMaxDate: any;

    constructor(private readonly $log: ILogService, private readonly $attrs: IAttributes) {
    }

    private static momentToString(mom: moment.Moment): string {
        if (mom && mom.isValid()) {
            return mom.format(DatepickerController.defaultFormat);
        }
        return '';
    }

    private static stringToMoment(date: string): any {
        if (moment(date, DatepickerController.allowedFormats, true).isValid()) {
            return moment(date, DatepickerController.allowedFormats, true);
        }
        return null;
    }

    // beispiel wie man auf changes eines attributes von aussen reagieren kann
    $onChanges(changes: any) {
        if (changes.ngRequired && !changes.ngRequired.isFirstChange()) {
            this.dateRequired = changes.ngRequired.currentValue;
        }

    }

    //wird von angular aufgerufen
    $onInit() {

        if (!this.ngModelCtrl) {
            return;
        }
        // Wenn kein Minimumdatum gesetzt ist, verwenden wir 01.01.1900 als Minimum
        if (this.dvMinDate === undefined) {
            this.dvMinDate = DateUtil.localDateToMoment('1900-01-01');
        }
        const noFuture = 'noFuture' in this.$attrs;
        //wenn kein Placeholder gesetzt wird wird der standardplaceholder verwendet. kann mit placeholder=""
        // ueberscrieben werden
        if (this.placeholder === undefined) {
            this.placeholder = 'tt.mm.jjjj';
        } else if (this.placeholder === '') {
            this.placeholder = undefined;
        }

        if (this.ngRequired) {
            this.dateRequired = this.ngRequired;
        }

        this.ngModelCtrl.$render = () => {
            this.date = this.ngModelCtrl.$viewValue;
        };
        this.ngModelCtrl.$formatters.unshift(DatepickerController.momentToString);
        this.ngModelCtrl.$parsers.push(DatepickerController.stringToMoment);

        this.ngModelCtrl.$validators['moment'] = (modelValue: any, viewValue: any) => {
            // if not required and view value empty, it's ok...
            if (!this.dateRequired && !viewValue) {
                return true;
            }
            return this.getInputAsMoment(modelValue, viewValue).isValid();
        };
        // Validator fuer Minimal-Datum
        this.ngModelCtrl.$validators['dvMinDate'] = (modelValue: any, viewValue: any) => {
            let result: boolean = true;
            if (this.dvMinDate && viewValue) {
                const minDateAsMoment: moment.Moment = moment(this.dvMinDate, DatepickerController.allowedFormats, true);
                if (minDateAsMoment.isValid()) {
                    const inputAsMoment: moment.Moment = this.getInputAsMoment(modelValue, viewValue);
                    if (inputAsMoment && inputAsMoment.isBefore(minDateAsMoment)) {
                        result = false;
                    }
                } else {
                    this.$log.debug('min date is invalid', this.dvMinDate);
                }
            }
            return result;
        };
        if (noFuture) {
            this.ngModelCtrl.$validators['dvNoFutureDate'] = (modelValue: any, viewValue: any) => {
                let result: boolean = true;
                if (viewValue) {
                    const maxDateAsMoment: moment.Moment = moment(moment.now());
                    const inputAsMoment: moment.Moment = this.getInputAsMoment(modelValue, viewValue);
                    if (inputAsMoment && inputAsMoment.isAfter(maxDateAsMoment)) {
                        result = false;
                    }
                }
                return result;
            };
        }
        // Validator fuer Maximal-Datum
        this.ngModelCtrl.$validators['dvMaxDate'] = (modelValue: any, viewValue: any) => {
            let result: boolean = true;
            if (this.dvMaxDate && viewValue) {
                const maxDateAsMoment: moment.Moment = moment(this.dvMaxDate, DatepickerController.allowedFormats, true);
                if (maxDateAsMoment.isValid()) {
                    const inputAsMoment: moment.Moment = this.getInputAsMoment(modelValue, viewValue);
                    if (inputAsMoment && inputAsMoment.isAfter(maxDateAsMoment)) {
                        result = false;
                    }
                } else {
                    this.$log.debug('max date is invalid', this.dvMaxDate);
                }
            }
            return result;
        };
    }

    private getInputAsMoment(modelValue: any, viewValue: any): moment.Moment {
        const value = modelValue || DatepickerController.stringToMoment(viewValue);
        const inputdate: moment.Moment = moment(value, DatepickerController.allowedFormats, true);
        return inputdate;
    }

    onBlur() {
        if (this.dvOnBlur) { // userdefined onBlur event
            this.dvOnBlur();
        }
        this.ngModelCtrl.$setTouched();
    }

    updateModelValue() {
        this.ngModelCtrl.$setViewValue(this.date);
    }
}
