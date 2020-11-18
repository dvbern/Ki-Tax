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
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import {KiBonGuidedTourService} from '../../app/kibonTour/service/KiBonGuidedTourService';
import {AuthServiceRS} from '../../authentication/service/AuthServiceRS.rest';
import {DvNgSupportDialogComponent} from './dv-ng-support-dialog.component';
import {TSRoleUtil} from '../../utils/TSRoleUtil';

/**
 * This component shows a Help Dialog with all contact details and a Link to the user manual
 */
@Component({
    selector: 'dv-ng-help-dialog',
    templateUrl: './dv-ng-help-dialog.template.html',
})
export class DvNgHelpDialogComponent {

    public hasRoleGemeinde: boolean = false;
    public hasRoleInstitution: boolean = false;
    public hasRoleMandant: boolean = false;

    public constructor(
        private readonly dialogRef: MatDialogRef<DvNgHelpDialogComponent>,
        private readonly dialogSupport: MatDialog,
        private readonly kibonGuidedTourService: KiBonGuidedTourService,
        private readonly authServiceRS: AuthServiceRS,
    ) {
        this.hasRoleGemeinde = this.isGemeinde();
        this.hasRoleInstitution = this.isInstitution();
        this.hasRoleMandant = this.isMandant();
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

    private isGemeinde(): boolean {
        return this.authServiceRS.isOneOfRoles(TSRoleUtil.getGemeindeRoles().concat(TSRoleUtil.getMandantRoles()));
    }

    private isInstitution(): boolean {
        return this.authServiceRS.isOneOfRoles(TSRoleUtil.getInstitutionRoles().concat(TSRoleUtil.getMandantRoles()));
    }

    private isMandant(): boolean {
        return this.authServiceRS.isOneOfRoles(TSRoleUtil.getMandantRoles());
    }
}
