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

import {ChangeDetectionStrategy, Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {ControlContainer, NgForm} from '@angular/forms';
import * as moment from 'moment';

@Component({
    selector: 'dv-date-picker-x',
    templateUrl: './dv-date-picker-x.component.html',
    changeDetection: ChangeDetectionStrategy.Default,
    styleUrls: ['dv-date-picker-x.component.less'],
    viewProviders: [ { provide: ControlContainer, useExisting: NgForm } ]
})
export class DvDatePickerXComponent implements OnInit {

    // store if picker is touched to set css class for validation
    public touched = false;

    @Input()
    public label: string;

    @Input()
    public date: moment.Moment;

    @Input()
    public minDate: moment.Moment;

    @Input()
    public maxDate: moment.Moment;

    @Output()
    public readonly dateChange: EventEmitter<moment.Moment> = new EventEmitter<moment.Moment>();

    @Input()
    public readonly required: boolean;

    public randId = Math.round(Math.random() * 1000);

    public constructor() {
    }

    public ngOnInit(): void {
    }

    public emit(): void {
        this.dateChange.emit(this.date);
    }
}
