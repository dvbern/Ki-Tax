import {ChangeDetectionStrategy, Component, EventEmitter, Input, Output} from '@angular/core';
import {ControlContainer, NgForm} from '@angular/forms';
import {MAT_DATE_FORMATS, MatDatepicker} from '@angular/material';
import * as moment from 'moment';

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
    viewProviders: [{provide: ControlContainer, useExisting: NgForm}],
    providers: [
        {provide: MAT_DATE_FORMATS, useValue: MY_FORMATS},
    ],
})
export class DvMonthPickerComponent {

    @Input() public date?: moment.Moment;
    @Input() public readonly required: boolean = false;

    @Output()
    public readonly dateChange = new EventEmitter<moment.Moment | null>();

    public inputId = `dv-month-picker-${nextId++}`;

    public constructor(public readonly form: NgForm) {
    }

    public chosenYearHandler(normalizedYear: moment.Moment): void {
        const control = this.form.controls[this.inputId];
        const ctrlValue = control.value || moment();
        ctrlValue.year(normalizedYear.year());
        control.setValue(ctrlValue);
    }

    public chosenMonthHandler(normalizedMonth: moment.Moment, datepicker: MatDatepicker<moment.Moment>): void {
        const control = this.form.controls[this.inputId];

        const ctrlValue = control.value || moment();
        ctrlValue.month(normalizedMonth.month());
        control.setValue(ctrlValue);
        datepicker.close();
    }

    public onChange(): void {
        const value = this.form.controls[this.inputId].value;
        const emitValue = moment.isMoment(value) ? value : null;
        this.dateChange.emit(emitValue);
    }
}
