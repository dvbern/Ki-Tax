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
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import {DvNgOkDialogComponent} from '../../../app/core/component/dv-ng-ok-dialog/dv-ng-ok-dialog.component';
import {DailyBatchRS} from '../../service/dailyBatchRS.rest';
import {DatabaseMigrationRS} from '../../service/databaseMigrationRS.rest';

@Component({
    selector: 'dv-batchjob-trigger-view',
    templateUrl: './batchjobTriggerView.html',
    styleUrls: ['./batchjobTrigger.less'],
})
export class BatchjobTriggerViewComponent {

    public constructor(
        private readonly dialog: MatDialog,
        private readonly databaseMigrationRS: DatabaseMigrationRS,
        private readonly dailyBatchRS: DailyBatchRS,
    ) {
    }

    public processScript(script: string): void {
        this.databaseMigrationRS.processScript(script);
    }

    public runBatchCleanDownloadFiles(): void {
        this.dailyBatchRS.runBatchCleanDownloadFiles().then(response => {
            const title = response ? 'CLEANDOWNLOADFILES_BATCH_EXECUTED_OK' : 'CLEANDOWNLOADFILES_EXECUTED_ERROR';
            this.createAndOpenDialog(title);
        });
    }

    public runBatchMahnungFristablauf(): void {
        this.dailyBatchRS.runBatchMahnungFristablauf().then(response => {
            const title = response ? 'MAHNUNG_BATCH_EXECUTED_OK' : 'MAHNUNG_BATCH_EXECUTED_ERROR';
            this.createAndOpenDialog(title);
        });
    }

    public runBatchUpdateGemeindeForBGInstitutionen(): void {
        this.dailyBatchRS.runBatchUpdateGemeindeForBGInstitutionen().then(response => {
            const title = response ? 'Gemeinden erfolgreich aktualisiert' : 'Fehler beim aktualisieren der Gemeinden';
            this.createAndOpenDialog(title);
        });
    }

    private createAndOpenDialog(title: string): void {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {title};

        this.dialog.open(DvNgOkDialogComponent, dialogConfig);
    }
}
