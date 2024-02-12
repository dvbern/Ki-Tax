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

import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef as MatDialogRef} from '@angular/material/dialog';
import {TranslateService} from '@ngx-translate/core';
import {TSTraegerschaft} from '../../../../models/TSTraegerschaft';

@Component({
    selector: 'dv-ng-select-tragerschaft-email-dialog',
    templateUrl: './dv-ng-select-traegerschaft-email-dialog.template.html'
})
export class DvNgSelectTraegerschaftEmailDialogComponent {

    public adminMails: string[];
    public selectedMail: string;

    public constructor(
        private readonly dialogRef: MatDialogRef<DvNgSelectTraegerschaftEmailDialogComponent>,
        private readonly translate: TranslateService,
        @Inject(MAT_DIALOG_DATA) private readonly data: any
    ) {
        this.adminMails = data;
    }

    public save(): void {
        this.dialogRef.close({
            selectedEmail: this.selectedMail
        });
    }

    public close(): void {
        this.dialogRef.close();
    }
}
