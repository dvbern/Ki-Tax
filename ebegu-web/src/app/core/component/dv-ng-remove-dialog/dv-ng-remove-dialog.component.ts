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

import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material';

/**
 * This Dialog should be used for asking the user to remove an object
 */
@Component({
    selector: 'dv-ng-remove-dialog',
    templateUrl: './dv-ng-remove-dialog.template.html',
})
export class DvNgRemoveDialogComponent {

    title: string = '';
    text: string = '';

    constructor(
        private readonly dialogRef: MatDialogRef<DvNgRemoveDialogComponent>,
        @Inject(MAT_DIALOG_DATA) data: any) {

        if (data) {
            this.title = data.title;
            this.text = data.text;
        }
    }

    ok() {
        this.dialogRef.close(true);
    }

    cancel() {
        this.dialogRef.close();
    }
}
