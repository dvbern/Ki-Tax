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

import {IController, IDirective, IDirectiveFactory} from 'angular';
import * as moment from 'moment';
import DateUtil from '../../../../utils/DateUtil';
import IAttributes = angular.IAttributes;
import ILogService = angular.ILogService;
import INgModelController = angular.INgModelController;

export class DVTimepicker implements IDirective {
    restrict = 'E';
    require: any = {ngModelCtrl: 'ngModel'};
    scope = {};
    controller = TimepickerController;
    controllerAs = 'vm';
    bindToController = {
        ngModel: '=',
        inputId: '@',
        ngRequired: '<',
        placeholder: '@',
        ngDisabled: '<',
        noFuture: '<?',
        dvOnBlur: '&?',
        dvMinDateTime: '<?', // Kann als String im Format allowedFormats oder als Moment angegeben werden
        dvMaxDateTime: '<?'  // Kann als String im Format allowedFormats oder als Moment angegeben werden
    };
    template = require('./dv-timepicker.html');

    /* constructor() { this.link = this.unboundLink.bind(this); }*/
    static factory(): IDirectiveFactory {
        const directive = () => new DVTimepicker();
        directive.$inject = [];
        return directive;
    }
}

export class TimepickerController implements IController {
    static $inject: string[] = ['$log', '$attrs'];
    static allowedFormats: string[] = ['HH:mm:ss', 'HH:mm'];
    static defaultFormat: string = 'HH:mm';
    dateTime: Date;
    ngModelCtrl: INgModelController;
    dateTimeRequired: boolean;
    ngRequired: boolean;
    placeholder: string;
    dvOnBlur: () => void;
    dvMinDateTime: any;
    dvMaxDateTime: any;

    constructor(private readonly $log: ILogService, private readonly $attrs: IAttributes) {
    }

    private static momentToString(mom: moment.Moment): string {
        if (mom && mom.isValid()) {
            return mom.format(TimepickerController.defaultFormat);
        }
        return '';
    }

    private static stringToMoment(dateTime: string): any {
        if (moment(dateTime, TimepickerController.allowedFormats, true).isValid()) {
            return moment(dateTime, TimepickerController.allowedFormats, true);
        }
        return null;
    }

    // beispiel wie man auf changes eines attributes von aussen reagieren kann
    $onChanges(changes: any) {
        if (changes.ngRequired && !changes.ngRequired.isFirstChange()) {
            this.dateTimeRequired = changes.ngRequired.currentValue;
        }

    }

    //wird von angular aufgerufen
    $onInit() {

        if (!this.ngModelCtrl) {
            return;
        }
        // Wenn kein Minimumdatum gesetzt ist, verwenden wir 01.01.1900 als Minimum
        if (this.dvMinDateTime === undefined) {
            this.dvMinDateTime = DateUtil.localDateToMoment('1900-01-01 00:00');
        }
        const noFuture = 'noFuture' in this.$attrs;
        //wenn kein Placeholder gesetzt wird wird der standardplaceholder verwendet. kann mit placeholder=""
        // ueberscrieben werden
        if (this.placeholder === undefined) {
            this.placeholder = 'hh:mm';
        } else if (this.placeholder === '') {
            this.placeholder = undefined;
        }

        if (this.ngRequired) {
            this.dateTimeRequired = this.ngRequired;
        }

        this.ngModelCtrl.$render = () => {
            this.dateTime = this.ngModelCtrl.$viewValue;
        };
        this.ngModelCtrl.$formatters.unshift(TimepickerController.momentToString);
        this.ngModelCtrl.$parsers.push(TimepickerController.stringToMoment);

        this.ngModelCtrl.$validators['moment'] = (modelValue: any, viewValue: any) => {
            // if not required and view value empty, it's ok...
            if (!this.dateTimeRequired && !viewValue) {
                return true;
            }
            return this.getInputAsMoment(modelValue, viewValue).isValid();
        };
        // Validator fuer Minimal-Datum
        this.ngModelCtrl.$validators['dvMinDateTime'] = (modelValue: any, viewValue: any) => {
            let result: boolean = true;
            if (this.dvMinDateTime && viewValue) {
                const minDateTimeAsMoment: moment.Moment = moment(this.dvMinDateTime, TimepickerController.allowedFormats, true);
                if (minDateTimeAsMoment.isValid()) {
                    const inputAsMoment: moment.Moment = this.getInputAsMoment(modelValue, viewValue);
                    if (inputAsMoment && inputAsMoment.isBefore(minDateTimeAsMoment)) {
                        result = false;
                    }
                } else {
                    this.$log.debug('min time is invalid', this.dvMinDateTime);
                }
            }
            return result;
        };
        if (noFuture) {
            this.ngModelCtrl.$validators['dvNoFutureDateTime'] = (modelValue: any, viewValue: any) => {
                let result: boolean = true;
                if (viewValue) {
                    const maxDateTimeAsMoment: moment.Moment = moment(moment.now());
                    const inputAsMoment: moment.Moment = this.getInputAsMoment(modelValue, viewValue);
                    if (inputAsMoment && inputAsMoment.isAfter(maxDateTimeAsMoment)) {
                        result = false;
                    }
                }
                return result;
            };
        }
        // Validator fuer Maximal-Datum
        this.ngModelCtrl.$validators['dvMaxDateTime'] = (modelValue: any, viewValue: any) => {
            let result: boolean = true;
            if (this.dvMaxDateTime && viewValue) {
                const maxDateTimeAsMoment: moment.Moment = moment(this.dvMaxDateTime, TimepickerController.allowedFormats, true);
                if (maxDateTimeAsMoment.isValid()) {
                    const inputAsMoment: moment.Moment = this.getInputAsMoment(modelValue, viewValue);
                    if (inputAsMoment && inputAsMoment.isAfter(maxDateTimeAsMoment)) {
                        result = false;
                    }
                } else {
                    this.$log.debug('max time is invalid', this.dvMaxDateTime);
                }
            }
            return result;
        };
    }

    private getInputAsMoment(modelValue: any, viewValue: any): moment.Moment {
        const value = modelValue || TimepickerController.stringToMoment(viewValue);
        const inputdateTime: moment.Moment = moment(value, TimepickerController.allowedFormats, true);
        return inputdateTime;
    }

    onBlur() {
        if (this.dvOnBlur) { // userdefined onBlur event
            this.dvOnBlur();
        }
        this.ngModelCtrl.$setTouched();
    }

    updateTimeModelValue() {
        this.ngModelCtrl.$setViewValue(this.dateTime);
    }
}
