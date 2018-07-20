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

import {Component, OnInit} from '@angular/core';
import {DvDialog} from '../../../core/directive/dv-dialog/dv-dialog';
import {OkDialogController} from '../../../gesuch/dialog/OkDialogController';
import {DailyBatchRS} from '../../service/dailyBatchRS.rest';
import {DatabaseMigrationRS} from '../../service/databaseMigrationRS.rest';

require('./batchjobTrigger.less');
let okDialogTempl = require('../../../gesuch/dialog/okDialogTemplate.html');
let linkDialogTempl = require('../../../gesuch/dialog/linkDialogTemplate.html');

@Component({
    selector: 'batchjob-trigger-view',
    template: require('./batchjobTriggerView.html'),
})
export class BatchjobTriggerViewComponent implements OnInit {

    constructor(private dvDialog: DvDialog, private databaseMigrationRS: DatabaseMigrationRS, private dailyBatchRS: DailyBatchRS) {
    }

    public ngOnInit(): void {
    }

    public processScript(script: string): void {
        this.databaseMigrationRS.processScript(script);
    }

    public runBatchCleanDownloadFiles(): void {
        this.dailyBatchRS.runBatchCleanDownloadFiles().then((response) => {
            let text: string = '';
            if (response) {
                text = 'CLEANDOWNLOADFILES_BATCH_EXECUTED_OK';
            } else {
                text = 'CLEANDOWNLOADFILES_EXECUTED_ERROR';
            }
            this.dvDialog.showDialog(okDialogTempl, OkDialogController, {
                title: text
            }).then(() => {
                //do nothing
            });
        });
    }

    public runBatchMahnungFristablauf(): void {
        this.dailyBatchRS.runBatchMahnungFristablauf().then((response) => {
            let text: string = '';
            if (response) {
                text = 'MAHNUNG_BATCH_EXECUTED_OK';
            } else {
                text = 'MAHNUNG_BATCH_EXECUTED_ERROR';
            }
            this.dvDialog.showDialog(okDialogTempl, OkDialogController, {
                title: text
            }).then(() => {
                //do nothing
            });
        });
    }
}
