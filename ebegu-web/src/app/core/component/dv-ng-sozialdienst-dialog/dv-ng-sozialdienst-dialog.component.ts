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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {Component, Inject} from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import {TSSozialdienst} from '../../../../models/sozialdienst/TSSozialdienst';

/**
 * Component fuer den GemeindeDialog. In einem Select muss der Benutzer die Gemeinde auswaehlen.
 * Keine Gemeinde wird by default ausgewaehlt, damit der Benutzer nicht aus Versehen die falsche Gemeinde auswaehlt.
 * Die GemeindeListe wird von aussen gegeben, damit dieser Component von nichts abhaengt. Die ausgewaehlte Gemeinde
 * wird dann beim Close() zurueckgegeben
 */
@Component({
    selector: 'dv-ng-sozialdienst-dialog',
    templateUrl: './dv-ng-sozialdienst-dialog.template.html',
})
export class DvNgSozialdienstDialogComponent {

    public selectedSozialdienst: TSSozialdienst;
    public sozialdienstList: TSSozialdienst[];

    public constructor(
        private readonly dialogRef: MatDialogRef<DvNgSozialdienstDialogComponent>,
        @Inject(MAT_DIALOG_DATA) data: any,
    ) {

        this.sozialdienstList = data.sozialdienstList;
    }

    public save(): void {
        this.dialogRef.close(this.selectedSozialdienst ? this.selectedSozialdienst.id : undefined);
    }

    public close(): void {
        this.dialogRef.close();
    }
}
