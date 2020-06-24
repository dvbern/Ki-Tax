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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material';

/**
 * Dialog mit drei Buttons (2 Actions, 1 Abbrechen).
 * Titel, Text, sowie alle Button-Texte koennen ueberschrieben werden
 */
@Component({
    selector: 'dv-ng-three-button-dialog',
    templateUrl: './dv-ng-three-button-dialog.template.html',
    styleUrls: ['./dv-ng-three-button-dialog.less'],
})
export class DvNgThreeButtonDialogComponent {

    public title: string = '';
    public text: string = '';
    public actionOneButtonLabel: string = 'LABEL_OK';   // Default-Buttontext
    public actionTwoButtonLabel: string = 'LABEL_OK';   // Default-Buttontext
    public cancelButtonLabel: string = 'CANCEL';        // Default-Buttontext

    public constructor(
        private readonly dialogRef: MatDialogRef<DvNgThreeButtonDialogComponent>,
        @Inject(MAT_DIALOG_DATA) data: any,
    ) {
        if (!data) {
            return;
        }
        this.title = data.title;
        this.text = data.text;
        if (data.actionOneButtonLabel) {
            this.actionOneButtonLabel = data.actionOneButtonLabel;
        }
        if (data.actionTwoButtonLabel) {
            this.actionTwoButtonLabel = data.actionTwoButtonLabel;
        }
        if (data.cancelButtonLabel) {
            this.cancelButtonLabel = data.cancelButtonLabel;
        }
    }

    public ok(): void {
        this.dialogRef.close(true);
    }

    public actionOne(): void {
        this.dialogRef.close(1);
    }

    public actionTwo(): void {
        this.dialogRef.close(2);
    }

    public cancel(): void {
        this.dialogRef.close();
    }
}
