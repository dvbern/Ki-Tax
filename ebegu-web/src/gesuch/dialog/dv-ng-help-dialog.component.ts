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

import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material';
import {DownloadRS} from '../../core/service/downloadRS.rest';
import TSDownloadFile from '../../models/TSDownloadFile';

/**
 * This component shows a Help Dialog with all contact details and a Link to the user manual
 */
@Component({
    selector: 'dv-ng-help-dialog',
    template: require('./dv-ng-help-dialog.template.html'),
})
export class DvNgHelpDialogComponent {

    constructor(
        private readonly dialogRef: MatDialogRef<DvNgHelpDialogComponent>,
        private readonly downloadRS: DownloadRS,
        @Inject(MAT_DIALOG_DATA) data: any) {
    }

    close() {
        this.dialogRef.close();
    }

    public download(): void {
        const win: Window = this.downloadRS.prepareDownloadWindow();
        this.downloadRS.getAccessTokenBenutzerhandbuch()
            .then((downloadFile: TSDownloadFile) => {
                this.downloadRS.startDownload(downloadFile.accessToken, downloadFile.filename, false, win);
            })
            .catch(() => {
                win.close();
            });
    }
}
