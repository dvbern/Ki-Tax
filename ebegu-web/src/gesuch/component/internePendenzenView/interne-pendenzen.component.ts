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
import {ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {LogFactory} from '../../../app/core/logging/LogFactory';
import {TSInternePendenz} from '../../../models/TSInternePendenz';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {GesuchModelManager} from '../../service/gesuchModelManager';
import {InternePendenzDialogComponent} from './interne-pendenz-dialog/interne-pendenz-dialog.component';
import {InternePendenzenRS} from './internePendenzenRS.rest';

const LOG = LogFactory.createLog('InternePendenzenComponent');

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
        private readonly dialog: MatDialog,
        private readonly internePendenzenRS: InternePendenzenRS,
        private readonly cd: ChangeDetectorRef,
        private readonly gesuchModelManager: GesuchModelManager,
    ) {
    }

    public ngOnInit(): void {
        this.getPendenzen();
    }

    public getPendenzen(): void {
        this.internePendenzenRS.findInternePendenzenForGesuch(this.gesuchModelManager.getGesuch())
            .subscribe(internePendenzen => {
                this.internePendenzen = internePendenzen;
                this.cd.markForCheck();
            }, error => this.handleError(error));
    }

    public async addInternePendenz(): Promise<void> {
        let newPendenz = new TSInternePendenz();
        newPendenz.gesuch = this.gesuchModelManager.getGesuch();
        newPendenz = await this.openPendenzDialog(newPendenz);
        if (EbeguUtil.isNullOrUndefined(newPendenz)) {
            return;
        }
        this.internePendenzenRS.createInternePendenz(newPendenz)
            .subscribe(savedPendenz => {
                // concat to change ref of element and trigger changeDetection of child
                this.internePendenzen = this.internePendenzen.concat([savedPendenz]);
                this.cd.markForCheck();
            }, error => this.handleError(error));
    }

    public async editPendenz(pendenz: TSInternePendenz): Promise<void> {
        const editedPendenz = await this.openPendenzDialog(pendenz);
        if (EbeguUtil.isNullOrUndefined(editedPendenz)) {
            return;
        }
        this.internePendenzenRS.updateInternePendenz(editedPendenz)
            .subscribe(savedPendenz => {
                this.internePendenzen = this.internePendenzen.filter(p => {
                    return p.id !== editedPendenz.id;
                });
                this.internePendenzen.push(savedPendenz);
                this.cd.markForCheck();
            }, error => this.handleError(error));
    }

    public deletePendenz(deletedPendenz: TSInternePendenz): void {
        this.internePendenzenRS.deleteInternePendenz(deletedPendenz)
            .subscribe(() => {
                this.internePendenzen = this.internePendenzen.filter(p => {
                    return p.id !== deletedPendenz.id;
                });
                this.cd.markForCheck();
            }, error => this.handleError(error));
    }

    private openPendenzDialog(internePendenz: TSInternePendenz): Promise<TSInternePendenz> {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {internePendenz};
        dialogConfig.panelClass = 'interne-pendenzen-dialog';
        return this.dialog.open(InternePendenzDialogComponent, dialogConfig).afterClosed().toPromise();
    }

    public navigateBack(): void {
        this.location.back();
    }

    private handleError(error: Error): void {
        LOG.error(error);
    }
}
