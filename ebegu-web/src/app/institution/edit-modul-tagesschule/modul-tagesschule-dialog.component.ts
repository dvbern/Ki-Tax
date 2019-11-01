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

import {Component, Inject, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material';
import {getTSModulTagesschuleIntervallValues, TSModulTagesschuleIntervall} from '../../../models/enums/TSModulTagesschuleIntervall';
import {TSModulTagesschuleName} from '../../../models/enums/TSModulTagesschuleName';
import TSModulTagesschuleGroup from '../../../models/TSModulTagesschuleGroup';

@Component({
    selector: 'modul-tagesschule-dialog',
    templateUrl: './modul-tagesschule-dialog.template.html',
    styleUrls: ['./modul-tagesschule-dialog.component.less'],
})
export class ModulTagesschuleDialogComponent {

    @ViewChild(NgForm) public form: NgForm;

    public modulTagesschuleGroup: TSModulTagesschuleGroup;
    public noDaySelected: boolean = false;

    public patternHoursAndMinutes: '[0-9]{1,2}:[0-9]{1,2}';

    public constructor(
        private readonly dialogRef: MatDialogRef<ModulTagesschuleDialogComponent>,
        @Inject(MAT_DIALOG_DATA) data: any,
    ) {
        this.modulTagesschuleGroup = data.modulTagesschuleGroup;
    }

    public ngOnInit(): void {
        this.modulTagesschuleGroup.initializeTempModule();
    }

    public save(): void {
        this.modulTagesschuleGroup.applyTempModule();
        if (this.modulTagesschuleGroup.isValid()) {
            this.modulTagesschuleGroup.validated =  true;
            this.dialogRef.close(this.modulTagesschuleGroup);
        } else {
            if (this.modulTagesschuleGroup.module.length === 0) {
                this.noDaySelected = true;
            }
            this.ngOnInit();
        }
    }

    public close(): void {
        this.dialogRef.close();
    }

    public getModulTagesschuleIntervallOptions(): Array<TSModulTagesschuleIntervall> {
        return getTSModulTagesschuleIntervallValues();
    }

    public isModulErfassungDynamisch(): boolean {
        return TSModulTagesschuleName.DYNAMISCH === this.modulTagesschuleGroup.modulTagesschuleName;
    }
}
