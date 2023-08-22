/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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

import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';

/**
 * This Dialog should be used for asking the user to confirm an action
 */
@Component({
    selector: 'dv-ng-confirm-dialog',
    templateUrl: './dv-ng-cancel-dialog.template.html'
})
export class DvNgCancelDialogComponent {

    public frage: string = '';

    public constructor(
        private readonly dialogRef: MatDialogRef<DvNgCancelDialogComponent>,
        @Inject(MAT_DIALOG_DATA) private readonly data: any
    ) {
        if (data) {
            this.frage = data.frage;
        }
    }

    public ok(): void {
        this.dialogRef.close(true);
    }

    public cancel(): void {
        this.dialogRef.close();
    }
}
