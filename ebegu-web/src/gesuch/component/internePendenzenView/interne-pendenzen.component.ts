/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

import {Location} from '@angular/common';
import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {TSInternePendenz} from '../../../models/TSInternePendenz';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {InternePendenzDialogComponent} from './interne-pendenz-dialog/interne-pendenz-dialog.component';

@Component({
    selector: 'interne-pendenzen-view',
    templateUrl: './interne-pendenzen.component.html',
    styleUrls: ['./interne-pendenzen.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class InternePendenzenComponent implements OnInit {

    public internePendenzen: TSInternePendenz[] = [];

    public constructor(
        private readonly location: Location,
        private dialog: MatDialog
    ) {
    }

    public ngOnInit(): void {
    }

    public async addInternePendenz(): Promise<void> {
        let newPendenz = new TSInternePendenz();
        newPendenz = await this.openPendenzDialog(newPendenz);
        if (EbeguUtil.isNotNullOrUndefined(newPendenz)) {
            this.internePendenzen.push(newPendenz);
        }
    }

    private openPendenzDialog(internePendenz: TSInternePendenz): Promise<TSInternePendenz> {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {internePendenz};
        return this.dialog.open(InternePendenzDialogComponent, dialogConfig).afterClosed().toPromise();
    }

    public navigateBack(): void {
        this.location.back();
    }
}
