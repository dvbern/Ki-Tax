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

import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material';
import {TSEinstellungenTagesschule} from '../../../../models/TSEinstellungenTagesschule';
import {TSInstitution} from '../../../../models/TSInstitution';
import {TSInstitutionStammdaten} from '../../../../models/TSInstitutionStammdaten';
import {InstitutionStammdatenRS} from '../../../core/service/institutionStammdatenRS.rest';

@Component({
    selector: 'dv-ng-gemeinde-dialog',
    templateUrl: './dialog-import-from-other-institution.template.html',
    styleUrls: ['./dialog-import-from-other-institution.component.less'],
})
export class DialogImportFromOtherInstitution {

    public selectedInstitution: TSInstitution;
    public institutionList: TSInstitution[];
    public selectedEinstellungTagesschule: TSEinstellungenTagesschule;
    public einstellungenTagesschule: TSEinstellungenTagesschule[];

    public constructor(
        private readonly dialogRef: MatDialogRef<DialogImportFromOtherInstitution>,
        private readonly institutionStammdatenRS: InstitutionStammdatenRS,
        @Inject(MAT_DIALOG_DATA) data: any,
    ) {
        this.institutionList = data.institutionList;
    }

    private getInstitutionStammdaten(institutionId: string): void {
        this.institutionStammdatenRS.fetchInstitutionStammdatenByInstitution(institutionId).then((institutionStammdaten: TSInstitutionStammdaten) => {
            this.einstellungenTagesschule = institutionStammdaten.institutionStammdatenTagesschule
                .einstellungenTagesschule;
        });
    }

    public save(): void {
        this.dialogRef.close(this.selectedEinstellungTagesschule ?
            this.selectedEinstellungTagesschule.modulTagesschuleGroups : undefined);
    }

    public close(): void {
        this.dialogRef.close();
    }
}
