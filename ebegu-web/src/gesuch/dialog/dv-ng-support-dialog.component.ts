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

import {Component} from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import {TSSupportAnfrage} from '../../models/TSSupportAnfrage';
import {EbeguUtil} from '../../utils/EbeguUtil';
import {SupportRS} from '../service/supportRS.rest';

/**
 * This component shows a dialog to send a request of support
 */
@Component({
    selector: 'dv-ng-support-dialog',
    templateUrl: './dv-ng-support-dialog.template.html',
})
export class DvNgSupportDialogComponent {

    public beschreibung: string;
    public readonly idLength = 20;

    public constructor(
        private readonly dialogRef: MatDialogRef<DvNgSupportDialogComponent>,
        private readonly supportRS: SupportRS
    ) {
    }

    public send(): void {
        const anfrage = new TSSupportAnfrage();
        anfrage.id = EbeguUtil.generateRandomName(this.idLength);
        anfrage.beschreibung = this.beschreibung;
        this.dialogRef.close();
        this.supportRS.sendSupportAnfrage(anfrage);
    }
}
