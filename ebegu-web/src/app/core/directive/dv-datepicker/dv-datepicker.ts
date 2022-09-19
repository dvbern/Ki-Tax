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
import {DateUtil} from '../../../../utils/DateUtil';
import IAttributes = angular.IAttributes;
import ILogService = angular.ILogService;
import INgModelController = angular.INgModelController;
import ITranslateService = angular.translate.ITranslateService;

export class DVDatepicker implements IDirective {
    public restrict = 'E';
    public require: any = {ngModelCtrl: 'ngModel'};
    public scope = {};
    public controller = DatepickerController;
    public controllerAs = 'vm';
    public bindToController = {
        ngModel: '=',
        inputId: '@',
        ngRequired: '<',
        placeholder: '@',
        ngDisabled: '<',
        noFuture: '<?',
        dvOnBlur: '&?',
        dvMinDate: '<?', // Kann als String im Format allowedFormats oder als Moment angegeben werden
        dvMaxDate: '<?',  // Kann als String im Format allowedFormats oder als Moment angegeben werden
        dvGesuchsperiodeMinDate: '<?',
        dvGesuchsperiodeMaxDate: '<?'
    };
    public template = require('./dv-datepicker.html');

    /* constructor() { this.link = this.unboundLink.bind(this); }*/
    public static factory(): IDirectiveFactory {
        const directive = () => new DVDatepicker();
        // @ts-ignore
        directive.$inject = [];
        return directive;
    }
}

export class DatepickerController implements IController {
    public static $inject: string[] = ['$log', '$attrs', '$translate'];
    public static allowedFormats: string[] = ['D.M.YYYY', 'DD.MM.YYYY'];
    public static defaultFormat: string = 'DD.MM.YYYY';
    public date: Date;
    public ngModelCtrl: INgModelController;
    public dateRequired: boolean;
    public ngRequired: boolean;
    public placeholder: string;
    public dvOnBlur: () => void;
    public noFuture: boolean;
    public dvMinDate: any;
    public dvMaxDate: any;
    public dvGesuchsperiodeMinDate: any;
    public dvGesuchsperiodeMaxDate: any;

    public constructor(
        private readonly $log: ILogService,
        private readonly $attrs: IAttributes,
        private readonly $translate: ITranslateService
    ) {
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
    public $onChanges(changes: any): void {
        if (changes.ngRequired && !changes.ngRequired.isFirstChange()) {
            this.dateRequired = changes.ngRequired.currentValue;
        }

    }

    // eslint-disable-next-line
    public $onInit(): void {

        if (!this.ngModelCtrl) {
            return;
        }
        // Wenn kein Minimumdatum gesetzt ist, verwenden wir 01.01.1900 als Minimum
        if (this.dvMinDate === undefined) {
            this.dvMinDate = DateUtil.localDateToMoment('1900-01-01');
        }
        if (this.dvGesuchsperiodeMinDate === undefined) {
            this.dvGesuchsperiodeMinDate = DateUtil.localDateToMoment('1900-01-01');
        }
        if (this.noFuture === undefined) {
            this.noFuture = false;
        }

        // wenn kein Placeholder gesetzt wird wird der standardplaceholder verwendet. kann mit placeholder=""
        // ueberscrieben werden
        if (this.placeholder === undefined) {
            this.placeholder = this.$translate.instant('DATE_PLACEHOLDER');
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

        this.ngModelCtrl.$validators.moment = (modelValue, viewValue) => {
            // if not required and view value empty, it's ok...
            if (!this.dateRequired && !viewValue) {
                return true;
            }
            return this.getInputAsMoment(modelValue, viewValue).isValid();
        };
        // Validator fuer Minimal-Datum
        this.ngModelCtrl.$validators.dvMinDate = (modelValue, viewValue) => {
            let result = true;
            if (this.dvMinDate && viewValue) {
                const minDateAsMoment = moment(this.dvMinDate, DatepickerController.allowedFormats, true);
                if (minDateAsMoment.isValid()) {
                    const inputAsMoment = this.getInputAsMoment(modelValue, viewValue);
                    if (inputAsMoment && inputAsMoment.isBefore(minDateAsMoment)) {
                        result = false;
                    }
                } else {
                    this.$log.debug('min date is invalid', this.dvMinDate);
                }
            }
            return result;
        };
        if (this.noFuture) {
            this.ngModelCtrl.$validators.dvNoFutureDate = (modelValue, viewValue) => {
                let result = true;
                if (viewValue) {
                    const maxDateAsMoment = moment(moment.now());
                    const inputAsMoment = this.getInputAsMoment(modelValue, viewValue);
                    if (inputAsMoment && inputAsMoment.isAfter(maxDateAsMoment)) {
                        result = false;
                    }
                }
                return result;
            };
        }
        // Validator fuer Maximal-Datum
        this.ngModelCtrl.$validators.dvMaxDate = (modelValue, viewValue) => {
            let result = true;
            if (this.dvMaxDate && viewValue) {
                const maxDateAsMoment = moment(this.dvMaxDate, DatepickerController.allowedFormats, true);
                if (maxDateAsMoment.isValid()) {
                    const inputAsMoment = this.getInputAsMoment(modelValue, viewValue);
                    if (inputAsMoment && inputAsMoment.isAfter(maxDateAsMoment)) {
                        result = false;
                    }
                } else {
                    this.$log.debug('max date is invalid', this.dvMaxDate);
                }
            }
            return result;
        };
        // Validator fuer Daterange mit Min und Max Datum
        this.ngModelCtrl.$validators.dvGesuchsperiodeIsInDateRange = (modelValue, viewValue) => {
            let result = true;
            if (this.dvGesuchsperiodeMaxDate && this.dvGesuchsperiodeMinDate && viewValue) {
                const maxDateAsMoment = moment(this.dvGesuchsperiodeMaxDate, DatepickerController.allowedFormats, true);
                const minDateAsMoment = moment(this.dvGesuchsperiodeMinDate, DatepickerController.allowedFormats, true);
                if (maxDateAsMoment.isValid() && minDateAsMoment.isValid()) {
                    const inputAsMoment = this.getInputAsMoment(modelValue, viewValue);
                    if (inputAsMoment &&
                        (inputAsMoment.isAfter(maxDateAsMoment) || inputAsMoment.isBefore(minDateAsMoment))) {
                        result = false;
                    }
                } else {
                    this.$log.debug('max date and min date are invalid', this.dvMaxDate);
                }
            }
            return result;
        };
    }

    private getInputAsMoment(modelValue: any, viewValue: any): moment.Moment {
        const value = modelValue || DatepickerController.stringToMoment(viewValue);

        return moment(value, DatepickerController.allowedFormats, true);
    }

    public onBlur(): void {
        if (this.dvOnBlur) { // userdefined onBlur event
            this.dvOnBlur();
        }
        this.ngModelCtrl.$setTouched();
    }

    public updateModelValue(): void {
        this.ngModelCtrl.$setViewValue(this.date);
    }
}
