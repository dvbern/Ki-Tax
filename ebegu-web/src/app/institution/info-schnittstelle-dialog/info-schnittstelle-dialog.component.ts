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

import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {TSModulTagesschuleGroup} from '../../../models/TSModulTagesschuleGroup';

@Component({
    selector: 'modul-tagesschule-dialog',
    templateUrl: './info-schnittstelle-dialog.template.html',
    styleUrls: ['./info-schnittstelle-dialog.component.less'],
})
export class InfoSchnittstelleDialogComponent {

    public modulTagesschuleGroup: TSModulTagesschuleGroup;

    public constructor(
        private readonly dialogRef: MatDialogRef<InfoSchnittstelleDialogComponent>,
        @Inject(MAT_DIALOG_DATA) data: any,
    ) {
        this.modulTagesschuleGroup = data.modulTagesschuleGroup;
    }

    public ngOnInit(): void {
    }

    public close(): void {
        this.dialogRef.close();
    }
}
