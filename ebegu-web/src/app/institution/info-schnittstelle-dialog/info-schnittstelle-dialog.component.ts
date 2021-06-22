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

import {Clipboard} from '@angular/cdk/clipboard';
import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {TSInstitution} from '../../../models/TSInstitution';
import {TSModulTagesschuleGroup} from '../../../models/TSModulTagesschuleGroup';

@Component({
    selector: 'modul-tagesschule-dialog',
    templateUrl: './info-schnittstelle-dialog.template.html',
    styleUrls: ['./info-schnittstelle-dialog.component.less'],
})
export class InfoSchnittstelleDialogComponent {

    public modulTagesschuleGroup: TSModulTagesschuleGroup;
    public institution: TSInstitution;
    public copied: any = {};

    private readonly resetCopiedAfterMS = 1000;

    public constructor(
        private readonly dialogRef: MatDialogRef<InfoSchnittstelleDialogComponent>,
        @Inject(MAT_DIALOG_DATA) data: any,
        private clipboard: Clipboard
    ) {
        this.modulTagesschuleGroup = data.modulTagesschuleGroup;
        this.institution = data.institution;
    }

    public ngOnInit(): void {
    }

    public copyToClipboard(text: string, key: string): void {
        this.clipboard.copy(text);
        this.copied[key] = true;
        setTimeout(() => this.copied[key] = false, this.resetCopiedAfterMS);
    }

    public close(): void {
        this.dialogRef.close();
    }
}
