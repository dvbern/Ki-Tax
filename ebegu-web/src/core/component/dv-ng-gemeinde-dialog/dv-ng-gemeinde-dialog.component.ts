/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
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

import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material';

@Component({
    selector: 'dv-ng-gemeinde-dialog',
    template: require('./dv-ng-gemeinde-dialog.component.html'),
})
export class DvNgGemeindeDialogComponent implements OnInit {

    // form: FormGroup;

    constructor(
        // private fb: FormBuilder,
        private dialogRef: MatDialogRef<DvNgGemeindeDialogComponent>,
        @Inject(MAT_DIALOG_DATA) data: any) {

    }

    ngOnInit() {
        // this.form = fb.group({
        //     description: [description, []],
        //     ...
        // });
    }

    save() {
        // this.dialogRef.close(this.form.gemeinde);
        this.dialogRef.close('80a8e496-b73c-4a4a-a163-a0b2caf76487'); // ostermundigen
        // this.dialogRef.close('ea02b313-e7c3-4b26-9ef7-e413f4046db2'); // bern
    }

    close() {
        this.dialogRef.close();
    }
}
