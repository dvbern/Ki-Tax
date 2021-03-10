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

import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {TranslateService} from '@ngx-translate/core';

/**
 * Component fuer den GemeindeDialog. In einem Select muss der Benutzer die Gemeinde auswaehlen.
 * Keine Gemeinde wird by default ausgewaehlt, damit der Benutzer nicht aus Versehen die falsche Gemeinde auswaehlt.
 * Die GemeindeListe wird von aussen gegeben, damit dieser Component von nichts abhaengt. Die ausgewaehlte Gemeinde
 * wird dann beim Close() zurueckgegeben
 */
@Component({
    selector: 'dv-ng-gemeinde-dialog',
    templateUrl: './dv-ng-gesuchsteller-dialog.template.html',
})
export class DvNgGesuchstellerDialogComponent {

    public emailAdresse: string;
    public administratorRolle: string;
    public gesuchstellerName: string;
    public question: string;
    public confirmationText: string;
    public confirmGesuchDelete: boolean;

    public constructor(
        private readonly dialogRef: MatDialogRef<DvNgGesuchstellerDialogComponent>,
        private readonly translate: TranslateService,
        @Inject(MAT_DIALOG_DATA) data: any,
    ) {
        this.emailAdresse = data.emailAdresse;
        this.administratorRolle = this.translate.instant(data.administratorRolle);
        this.gesuchstellerName = data.gesuchstellerName;
        this.confirmGesuchDelete = false;
        this.question = this.translate.instant('GESUCHSTELLER_HOEHRE_ROLLE_EINLADEN_FRAGE', {
            emailAdresse: this.emailAdresse,
            administratorRolle: this.administratorRolle,
        });
        this.confirmationText = this.translate.instant('GESUCHSTELLER_DIALOG_CONFIRM_DELETE', {
            gesuchstellerName: this.gesuchstellerName,
        });
    }

    public save(): void {
        this.dialogRef.close(this.confirmGesuchDelete);
    }

    public close(): void {
        this.dialogRef.close(false);
    }
}
