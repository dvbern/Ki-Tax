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

import {Component, ChangeDetectionStrategy} from '@angular/core';
import {MatDialog, MatDialogConfig} from '@angular/material';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {TSRole} from '../../../models/enums/TSRole';
import {TSRueckforderungMitteilung} from '../../../models/TSRueckforderungMitteilung';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {NotrechtRS} from '../../core/service/notrechtRS.rest';
import {SendNotrechtMitteilungComponent} from '../send-notrecht-mitteilung/send-notrecht-mitteilung.component';

@Component({
    selector: 'dv-notrecht',
    templateUrl: './notrecht.component.html',
    styleUrls: ['./notrecht.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class NotrechtComponent  {

    private readonly panelClass = 'dv-mat-dialog-send-notrecht-mitteilung';
    private tempSavedMitteilung: TSRueckforderungMitteilung;

    public constructor(
        private readonly notrechtRS: NotrechtRS,
        private readonly authServiceRS: AuthServiceRS,
        private readonly dialog: MatDialog,
    ) {
    }

    public initializeRueckforderungFormulare(): void {
        this.notrechtRS.initializeRueckforderungFormulare().then(result => {
            console.log(result); // man sollte vermeiden console.log and logger verwenden
        });
    }

    public isSuperAdmin(): boolean {
        return this.authServiceRS.isRole(TSRole.SUPER_ADMIN);
    }

    public sendMitteilung(): void {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {mitteilung: this.tempSavedMitteilung};
        dialogConfig.panelClass = this.panelClass;
        // Bei Ok erhalten wir die Mitteilung, die gesendet werden soll, sonst nichts
        this.dialog.open(SendNotrechtMitteilungComponent, dialogConfig).afterClosed().toPromise().then(result => {
            if (EbeguUtil.isNullOrUndefined(result) || EbeguUtil.isNullOrUndefined(result.mitteilung)) {
                return;
            }
            if (result.send) {
                console.log(result.mitteilung);
                return;
            }
            // Mitteilung wurde nicht gesendet, deshalb wird sie zwischengespeichert um sie allenfalls später wieder
            // zu öffnen
            this.tempSavedMitteilung = result.mitteilung;
        });
    }
}
