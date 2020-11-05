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
import {MatDialogRef, MAT_DIALOG_DATA} from '@angular/material';
import {TranslateService} from '@ngx-translate/core';
import {TSRueckforderungStatus} from '../../../models/enums/TSRueckforderungStatus';
import {TSRueckforderungMitteilung} from '../../../models/TSRueckforderungMitteilung';

@Component({
    selector: 'modul-tagesschule-dialog',
    templateUrl: './send-notrecht-mitteilung.template.html',
    styleUrls: ['./send-notrecht-mitteilung.component.less'],
})
export class SendNotrechtMitteilungComponent {

    @ViewChild(NgForm, { static: true }) public form: NgForm;

    public mitteilung: TSRueckforderungMitteilung;
    public isEinladung: boolean;
    public statusToSendMitteilung: TSRueckforderungStatus[];

    public constructor(
        private readonly dialogRef: MatDialogRef<SendNotrechtMitteilungComponent>,
        private readonly translate: TranslateService,
        @Inject(MAT_DIALOG_DATA) data: any
    ) {
        this.isEinladung = data.isEinladung;
    }

    public ngOnInit(): void {
        if (this.mitteilung) {
            return;
        }
        this.mitteilung = new TSRueckforderungMitteilung();
    }

    public save(): void {
        if (this.isValid()) {
            this.dialogRef.close({
                mitteilung: this.mitteilung,
                statusToSendMitteilung: this.statusToSendMitteilung,
            });
            return;
        }
        this.ngOnInit();
    }

    public isValid(): boolean {
        return this.form.valid;
    }

    public close(): void {
        this.dialogRef.close();
    }

    public getAllRueckforderungFormularStatus(): string[] {
        return Object.keys(TSRueckforderungStatus);
    }

    public translateRueckforderungStatus(status: string): string {
        return this.translate.instant(`RUECKFORDERUNG_STATUS_${status}`);
    }
}
