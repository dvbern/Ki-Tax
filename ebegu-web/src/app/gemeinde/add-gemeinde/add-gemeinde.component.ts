/*
 * AGPL File-Header
 *
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

import {ChangeDetectionStrategy, Component, OnInit, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {MatDialog} from '@angular/material';
import {StateService, Transition} from '@uirouter/core';
import GemeindeRS from '../../../gesuch/service/gemeindeRS.rest';
import {TSGemeindeStatus} from '../../../models/enums/TSGemeindeStatus';
import TSGemeinde from '../../../models/TSGemeinde';
import {LogFactory} from '../../core/logging/LogFactory';

const LOG = LogFactory.createLog('AddGemeindeComponent');

@Component({
    selector: 'add-gemeinde',
    templateUrl: './add-gemeinde.component.html',
    styleUrls: ['./add-gemeinde.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class AddGemeindeComponent implements OnInit {

    @ViewChild(NgForm) form: NgForm;

    selectedGemeinde: TSGemeinde = undefined;

    isDisabled = false;

    constructor(private readonly $transition$: Transition,
                private readonly $state: StateService,
                private readonly gemeindeRS: GemeindeRS,
                private readonly dialog: MatDialog) {
    }

    public ngOnInit(): void {
        const gemeindeId: string = this.$transition$.params().gemeindeId;
        if (gemeindeId) { // edit
            this.gemeindeRS.findGemeinde(gemeindeId).then((result) => {
                this.selectedGemeinde = result;
            });
        } else { // add
            this.selectedGemeinde = new TSGemeinde();
            this.selectedGemeinde.status = TSGemeindeStatus.EINGELADEN;

        }
    }

    public cancel(): void {
        this.navigateBack();
    }

    private navigateBack() {
        this.$state.go('admin.gemeindelist');
    }
}
