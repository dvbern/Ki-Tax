/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

import {Component, Inject, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {TSGemeinde} from '../../../../models/TSGemeinde';
import {TSGesuchsperiode} from '../../../../models/TSGesuchsperiode';
import {EbeguUtil} from '../../../../utils/EbeguUtil';

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

    public selectedGemeinde: TSGemeinde;
    public gemeindeList: TSGemeinde[];

    public selectedGesuchsperiode: TSGesuchsperiode;
    public gesuchsperiodeList: TSGesuchsperiode[];

    public constructor(
        private readonly dialogRef: MatDialogRef<DvNgGemeindeDialogComponent>,
        @Inject(MAT_DIALOG_DATA) data: any,
    ) {

        this.gemeindeList = data.gemeindeList;
        this.gesuchsperiodeList = data.gesuchsperiodeList;
    }

    public save(): void {
        if (!this.isValid()) {
            return;
        }
        this.dialogRef.close({
            gemeindeId: this.selectedGemeinde ? this.selectedGemeinde.id : undefined,
            gesuchsperiodeId: this.selectedGesuchsperiode ? this.selectedGesuchsperiode.id : undefined,
        });
    }

    public isValid(): boolean {
        return EbeguUtil.isNotNullOrUndefined(this.selectedGemeinde) &&
            ((EbeguUtil.isNotNullOrUndefined(this.gesuchsperiodeList)
                && EbeguUtil.isNotNullOrUndefined(this.selectedGesuchsperiode))
                || EbeguUtil.isNullOrUndefined(this.gesuchsperiodeList));
    }

    public close(): void {
        this.dialogRef.close();
    }
}
