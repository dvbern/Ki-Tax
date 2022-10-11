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
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {EbeguUtil} from '../../../../utils/EbeguUtil';

export interface DvMultiSelectDialogItem {
    item: any;
    selected: boolean;

    labelSelectFunction?(): string;
}

/**
 * This Dialog should be used for asking the user to remove an object
 */
@Component({
    selector: 'dv-ng-multi-select-dialog',
    templateUrl: './dv-ng-multi-select-dialog.template.html',
    styleUrls: ['./dv-ng-multi-select-dialog.component.less']
})
export class DvNgMultiSelectDialog {

    public title: string = '';
    public text: string = '';
    public selectOptions: DvMultiSelectDialogItem[];
    public allChecked: boolean;
    public someChecked: boolean;

    public constructor(
        private readonly dialogRef: MatDialogRef<DvNgMultiSelectDialog>,
        @Inject(MAT_DIALOG_DATA) data: any
    ) {
        if (data) {
            this.title = data.title;
            this.text = data.text;
            this.selectOptions = data.selectOptions;
        }
        this.updateCheckedFlags();
    }

    public ok(): void {
        this.dialogRef.close(this.selectOptions);
    }

    public cancel(): void {
        this.dialogRef.close();
    }

    public setAll(checked: boolean): void {
        this.selectOptions.forEach(option => {
            option.selected = checked;
        });
        this.allChecked = checked;
        this.someChecked = false;
    }

    public updateCheckedFlags(): void {
        this.allChecked =
            EbeguUtil.isNullOrUndefined(
                this.selectOptions.find(option => EbeguUtil.isNotNullAndFalse(option.selected)));
        this.someChecked =
            EbeguUtil.isNotNullOrUndefined(
                this.selectOptions.find(option => EbeguUtil.isNotNullAndTrue(option.selected))) &&
            EbeguUtil.isNotNullOrUndefined(
                this.selectOptions.find(option => EbeguUtil.isNotNullAndFalse(option.selected))
            );
    }
}
