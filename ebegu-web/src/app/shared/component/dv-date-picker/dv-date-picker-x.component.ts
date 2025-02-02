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

import {
    ChangeDetectionStrategy,
    Component,
    EventEmitter,
    Input,
    Output
} from '@angular/core';
import {ControlContainer, NgForm} from '@angular/forms';
import * as moment from 'moment';
import {EbeguUtil} from '../../../../utils/EbeguUtil';

@Component({
    selector: 'dv-date-picker-x',
    templateUrl: './dv-date-picker-x.component.html',
    changeDetection: ChangeDetectionStrategy.Default,
    styleUrls: ['dv-date-picker-x.component.less'],
    viewProviders: [{provide: ControlContainer, useExisting: NgForm}]
})
export class DvDatePickerXComponent {
    @Input()
    public label: string;

    @Input()
    public date: moment.Moment;

    @Input()
    public minDate: moment.Moment;

    @Input()
    public maxDate: moment.Moment;

    /**
     * Whether the mat-toggle for opening the calender is enabled. Defaults to true
     */
    @Input()
    public datePickerEnabled: boolean = true;

    /**
     * Custom id to be used as id for the input field. Will also be used for the label.for attribute if a label is
     * provided
     */
    @Input()
    public inputId: string;

    @Input()
    public disabled: boolean = false;

    @Output()
    public readonly dateChange: EventEmitter<moment.Moment> =
        new EventEmitter<moment.Moment>();

    @Input()
    public readonly required: boolean;

    public randId = EbeguUtil.generateRandomName(10);

    public emit(): void {
        this.dateChange.emit(this.date);
    }
}
