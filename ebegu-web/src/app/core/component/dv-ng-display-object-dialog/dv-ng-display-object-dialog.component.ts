/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

/**
 * This Dialog should be used to display all key/values of an object in a dev environment
 */
@Component({
    selector: 'dv-ng-display-object-dialog',
    templateUrl: './dv-ng-display-object-dialog.template.html',
})
export class DvNgDisplayObjectDialogComponent {

    public object: Object = {};

    public constructor(
        private readonly dialogRef: MatDialogRef<DvNgDisplayObjectDialogComponent>,
        @Inject(MAT_DIALOG_DATA) data: any,
    ) {
        if (data) {
            this.object = data.object;
        }
    }

    public ok(): void {
        this.dialogRef.close(true);
    }
}
