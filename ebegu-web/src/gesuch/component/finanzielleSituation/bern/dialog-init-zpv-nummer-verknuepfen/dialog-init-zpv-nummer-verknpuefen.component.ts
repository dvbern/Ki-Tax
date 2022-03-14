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

import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {GesuchstellerRS} from '../../../../../app/core/service/gesuchstellerRS.rest';
import {TSGesuchsteller} from '../../../../../models/TSGesuchsteller';

@Component({
    selector: 'dv-ng-zpv-nummmer-verknuepfen-dialog',
    templateUrl: './dialog-init-zpv-nummer-verknpuefen.template.html',
    styleUrls: ['./dialog-init-zpv-nummer-verknpuefen.component.less'],
})
export class DialogInitZPVNummerVerknuepfen implements OnInit {

    private readonly gs2: TSGesuchsteller;
    public email: string;

    public constructor(
        private readonly dialogRef: MatDialogRef<DialogInitZPVNummerVerknuepfen>,
        private readonly gesuchstellerRS: GesuchstellerRS,
        @Inject(MAT_DIALOG_DATA) data: any,
    ) {
        this.gs2 = data.gs2;
    }

    public ngOnInit(): void {
    }

    public save(): void {
        this.gesuchstellerRS.initGS2ZPVNr(this.email, this.gs2).then(() => this.dialogRef.close());
    }

    public close(): void {
        this.dialogRef.close();
    }
}
