/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
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

import {Component} from '@angular/core';
import {MatDialog, MatDialogRef} from '@angular/material';
import {DownloadRS} from '../../app/core/service/downloadRS.rest';
import TSDownloadFile from '../../models/TSDownloadFile';
import {DvNgSupportDialogComponent} from './dv-ng-support-dialog.component';
import {KiBonGuidedTourService} from "../../app/kibonTour/service/KiBonGuidedTourService";
import {ITourParams} from "../gesuch.route";
import {navigateToStartPageForRoleWithParams} from "../../utils/AuthenticationUtil";

/**
 * This component shows a Help Dialog with all contact details and a Link to the user manual
 */
@Component({
    selector: 'dv-ng-help-dialog',
    templateUrl: './dv-ng-help-dialog.template.html',
})
export class DvNgHelpDialogComponent {

    public constructor(
        private readonly dialogRef: MatDialogRef<DvNgHelpDialogComponent>,
        private readonly dialogSupport: MatDialog,
        private readonly downloadRS: DownloadRS,
        private readonly kibonGuidedTourService: KiBonGuidedTourService,
    ) {
    }

    public close(): void {
        this.dialogRef.close();
    }

    public openSupportanfrage(): void {
        this.close();
        this.dialogSupport.open(DvNgSupportDialogComponent);
    }

    public startTour(): void {
        this.close();
        this.kibonGuidedTourService.emit();
    }

}
