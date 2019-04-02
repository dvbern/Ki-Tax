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
import {MatDialogRef} from '@angular/material';
import TSSupportAnfrage from '../../models/TSSupportAnfrage';
import SupportRS from '../service/supportRS.rest';

/**
 * This component shows a dialog to send a request of support
 */
@Component({
    selector: 'dv-ng-support-dialog',
    templateUrl: './dv-ng-support-dialog.template.html',
})
export class DvNgSupportDialogComponent {

    private readonly beschreibung: string;

    public constructor(
        private readonly dialogRef: MatDialogRef<DvNgSupportDialogComponent>,
        private readonly supportRS: SupportRS
    ) {
    }

    public send(): void {
        const uuidv4 = require('uuid/v4');
        let anfrage: TSSupportAnfrage = new TSSupportAnfrage();
        anfrage.id = uuidv4();
        anfrage.beschreibung = this.beschreibung;
        this.dialogRef.close();
        this.supportRS.sendSupportAnfrage(anfrage);
    }
}
