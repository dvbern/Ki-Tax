/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {ChangeDetectionStrategy, Component, Input, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {getTSModulTagesschuleIntervallValues, TSModulTagesschuleIntervall} from '../../../models/enums/TSModulTagesschuleIntervall';
import TSModulTagesschuleGroup from '../../../models/TSModulTagesschuleGroup';

@Component({
    selector: 'dv-edit-modul-tagesschule',
    templateUrl: './edit-modul-tagesschule.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EditModulTagesschuleComponent {

    @ViewChild(NgForm) public form: NgForm;

    @Input() public modulTagesschuleGroup: TSModulTagesschuleGroup = undefined;

    public constructor(
    ) {
    }

    public getModulTagesschuleIntervallOptions(): Array<TSModulTagesschuleIntervall> {
        return getTSModulTagesschuleIntervallValues();
    }
}
