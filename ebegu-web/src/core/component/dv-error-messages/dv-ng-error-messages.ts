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

import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';

require('./dv-error-messages.less');

@Component({
    selector: 'dv-ng-error-messages',
    template: require('./dv-ng-error-messages.html'),
})
export class DvNgErrorMessages implements OnInit, OnChanges {

    @Input() errorObject: any;
    @Input() inputid: string;

    public errorsList: string[] = [];

    constructor() {}

    ngOnInit() {
        this.getErrors();
    }

    public getErrors() {
        this.errorsList = []; // always start with an empty list
        if (this.errorObject && this.errorObject.currentValue) {
            Object.keys(this.errorObject.currentValue)
                .filter(key => this.errorObject.currentValue[key] === true)
                .forEach(key => {
                        this.errorsList.push(key);
                    }
                );
        }
    }

    public ngOnChanges(changes: SimpleChanges): void {
        // when the errors change we need to update our errorsList
        if (changes && changes.errorObject) {
            this.errorObject = changes.errorObject;
            this.getErrors();
        }
    }
}
