import {Component, ChangeDetectionStrategy, Input, EventEmitter, Output} from '@angular/core';
import {NgForm} from '@angular/forms';
import {DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE} from '@angular/material';
import {MAT_MOMENT_DATE_ADAPTER_OPTIONS, MomentDateAdapter} from '@angular/material-moment-adapter';
import * as moment from 'moment';
import {DateUtil} from '../../../../utils/DateUtil';
import {EbeguUtil} from '../../../../utils/EbeguUtil';

export const MY_FORMATS = {
    parse: {
        dateInput: 'MM/YYYY',
    },
    display: {
        dateInput: 'MM/YYYY',
        monthYearLabel: 'MMM YYYY',
        dateA11yLabel: 'LL',
        monthYearA11yLabel: 'MMMM YYYY',
    },
};

let nextId = 0;

@Component({
    selector: 'dv-month-picker',
    templateUrl: './dv-month-picker.component.html',
    styleUrls: ['./dv-month-picker.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    providers: [
        // `MomentDateAdapter` can be automatically provided by importing `MomentDateModule` in your
        // application's root module. We provide it at the component level here, due to limitations of
        // our example generation script.
        {
            provide: DateAdapter,
            useClass: MomentDateAdapter,
            deps: [MAT_DATE_LOCALE, MAT_MOMENT_DATE_ADAPTER_OPTIONS],
        },

        {provide: MAT_DATE_FORMATS, useValue: MY_FORMATS},
    ],
})
export class DvMonthPickerComponent {

    private _date: moment.Moment;
    @Output() public readonly dateChange: EventEmitter<moment.Moment> = new EventEmitter();

    public inputId = `dv-month-picker-${nextId++}`;

    public constructor(public readonly form: NgForm) {
    }

    public chosenYearHandler(normalizedYear: moment.Moment): void {
        if (EbeguUtil.isNullOrUndefined(this._date)) {
            this._date = DateUtil.localDateToMoment('2000-01-01');
        }
        const ctrlValue = this._date;
        this._date = moment({
            year: normalizedYear.year(), month: ctrlValue.month(), day: ctrlValue.day(),
        });
    }

    public chosenMonthHandler(
        normalizedMonth: moment.Moment,
        datepicker?: any,
    ): void {
        const ctrlValue = this._date;
        this._date = moment({
            year: ctrlValue.year(), month: normalizedMonth.month(), day: ctrlValue.day(),
        });
        datepicker.close();
        this.dateChange.emit(this._date);
    }

    @Input()
    public get date(): moment.Moment {
        return this._date;
    }

    // noinspection JSUnusedGlobalSymbols
    public set date(value: moment.Moment) {
        this._date = value;
        this.dateChange.emit(value);
    }
}
