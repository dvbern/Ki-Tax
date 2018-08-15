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

import {Component, Input, OnChanges, SimpleChanges} from '@angular/core';
import {ValidationErrors} from '@angular/forms';

@Component({
    selector: 'dv-ng-error-messages',
    templateUrl: './dv-ng-error-messages.html',
    styleUrls: ['./dv-error-messages.less']
})
export class DvNgErrorMessages implements OnChanges {

    @Input() errorObject: ValidationErrors | null;
    @Input() show: boolean = false;
    @Input() inputid: string;

    public error: string = '';

    public ngOnChanges(changes: SimpleChanges): void {
        // when the errors change we need to update our error
        if (changes && changes.errorObject) {
            this.initError(changes.errorObject.currentValue);
        }
    }

    private initError(errors: ValidationErrors | null): void {
        if (!errors) {
            this.error = '';
            return;
        }

        const firstErroneousKey = Object.keys(errors)
            .find(key => errors[key] === true);

        this.error = firstErroneousKey ? firstErroneousKey : '';
    }
}
