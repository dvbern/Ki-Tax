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
 * This component shows a Dialog with a title and a link. Nothing is returned
 */
@Component({
    selector: 'dv-ng-link-dialog',
    template: require('./dv-ng-link-dialog.template.html'),
})
export class DvNgLinkDialogComponent {

    title: string = '';
    link: string = '';

    constructor(
        private readonly dialogRef: MatDialogRef<DvNgLinkDialogComponent>,
        @Inject(MAT_DIALOG_DATA) data: any) {

        if (data) {
            this.title = data.title;
            this.link = data.link;
        }
    }

    close() {
        this.dialogRef.close();
    }
}
