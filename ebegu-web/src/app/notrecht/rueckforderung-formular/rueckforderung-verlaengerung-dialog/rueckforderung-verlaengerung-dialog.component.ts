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

import {Component, ChangeDetectionStrategy, ViewChild, Inject} from '@angular/core';
import {NgForm} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material';
import {TSRueckforderungFormular} from '../../../../models/TSRueckforderungFormular';
import {EbeguUtil} from '../../../../utils/EbeguUtil';
import {NotrechtRS} from '../../../core/service/notrechtRS.rest';

@Component({
    selector: 'dv-rueckforderung-verlaengerung-dialog',
    templateUrl: './rueckforderung-verlaengerung-dialog.component.html',
    styleUrls: ['./rueckforderung-verlaengerung-dialog.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class RueckforderungVerlaengerungDialogComponent {

    @ViewChild(NgForm, { static: true }) public form: NgForm;
    public rueckforderungFormular: TSRueckforderungFormular;

    public constructor(private readonly dialogRef: MatDialogRef<RueckforderungVerlaengerungDialogComponent>,
                       private readonly notrechtRS: NotrechtRS,
                       @Inject(MAT_DIALOG_DATA) data: any,
                       ) {
        this.rueckforderungFormular = data.rueckforderungFormular;
    }

    public save(): void {
        if (!this.form.valid) {
            EbeguUtil.selectFirstInvalid();
            return;
        }
        this.notrechtRS.saveRueckforderungFormularEinreicheFrist(this.rueckforderungFormular).then(
            modifiedRueckforderungFormular => this.dialogRef.close(modifiedRueckforderungFormular)
        );
    }

    public close(): void {
        this.dialogRef.close();
    }
}
