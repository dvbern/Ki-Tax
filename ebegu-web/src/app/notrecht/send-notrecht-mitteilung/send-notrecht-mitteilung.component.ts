/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

import {Component, Inject, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {MatDialogRef} from '@angular/material';
import {MAT_DIALOG_DATA} from '@angular/material/dialog';
import {TSRueckforderungStatus} from '../../../models/enums/TSRueckforderungStatus';
import {TSRueckforderungMitteilung} from '../../../models/TSRueckforderungMitteilung';

@Component({
    selector: 'modul-tagesschule-dialog',
    templateUrl: './send-notrecht-mitteilung.template.html',
    styleUrls: ['./send-notrecht-mitteilung.component.less'],
})
export class SendNotrechtMitteilungComponent {

    @ViewChild(NgForm) public form: NgForm;

    public mitteilung: TSRueckforderungMitteilung;

    public constructor(
        private readonly dialogRef: MatDialogRef<SendNotrechtMitteilungComponent>,
        @Inject(MAT_DIALOG_DATA) data: any
    ) {
        this.mitteilung = data.mitteilung;
    }

    public ngOnInit(): void {
        if (!this.mitteilung) {
            this.mitteilung = new TSRueckforderungMitteilung();
        }
    }

    public save(): void {
        if (this.isValid()) {
            this.dialogRef.close({
                mitteilung: this.mitteilung,
                send: true
            });
            return;
        }
        this.ngOnInit();
    }

    public isValid(): boolean {
        return this.form.valid;
    }

    // wir geben die Mitteilung trotzdem zurück, damit sie in beim Erneuten Öffnen wieder erscheinen kann
    public close(): void {
        this.dialogRef.close({
            mitteilung: this.mitteilung,
            send: false
        });
    }

    public getAllRueckforderungFormularStatus(): string[] {
        return Object.keys(TSRueckforderungStatus);
    }
}
