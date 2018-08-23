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
import {Observable} from 'rxjs';
import TSGemeinde from '../../../../models/TSGemeinde';

/**
 * Component fuer den GemeindeDialog. In einem Select muss der Benutzer die Gemeinde auswaehlen.
 * Keine Gemeinde wird by default ausgewaehlt, damit der Benutzer nicht aus Versehen die falsche Gemeinde auswaehlt.
 * Die GemeindeListe wird von aussen gegeben, damit dieser Component von nichts abhaengt. Die ausgewaehlte Gemeinde
 * wird dann beim Close() zurueckgegeben
 */
@Component({
    selector: 'dv-ng-gemeinde-dialog',
    templateUrl: './dv-ng-gemeinde-dialog.template.html',
})
export class DvNgGemeindeDialogComponent {

    selectedGemeinde: TSGemeinde;
    gemeindeList: TSGemeinde[];

    constructor(
        private readonly dialogRef: MatDialogRef<DvNgGemeindeDialogComponent>,
        @Inject(MAT_DIALOG_DATA) data: any) {

        this.gemeindeList = data.gemeindeList;
    }

    save() {
        this.dialogRef.close(this.selectedGemeinde ? this.selectedGemeinde.id : undefined);
    }

    close() {
        this.dialogRef.close();
    }
}
