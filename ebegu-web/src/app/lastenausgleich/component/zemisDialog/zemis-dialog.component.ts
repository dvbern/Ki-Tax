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

import {Component, Inject, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {ZemisDialogDTO} from './zemisDialog.interface';

@Component({
    selector: 'zemis-dialog',
    templateUrl: 'zemis-dialog.template.html',
    styleUrls: ['zemis-dialog.component.less']
})
export class ZemisDialogComponent {

    public jahr: number;
    public file: File;
    public upload: boolean;

    @ViewChild(NgForm) private form: NgForm;

    public constructor(
        private readonly dialogRef: MatDialogRef<ZemisDialogComponent>,
        @Inject(MAT_DIALOG_DATA) data: any,
    ) {
        this.upload = data?.upload;
    }

    public cancel(): void {
        this.dialogRef.close();
    }

    public ok(): void {
        if (!this.form.valid) {
            return;
        }
        const output: ZemisDialogDTO = {
            jahr: this.jahr,
            file: this.file
        };
        this.dialogRef.close(output);
    }

    public handleFileInput(event: any): void {
        if (event.target.files.length === 0) {
            return;
        }
        if (event.target.files.length > 1) {
            throw new Error('too many files uploaded');
        }
        this.file = event.target.files[0];
    }
}
