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

import {ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnInit} from '@angular/core';
import {Observable, Subscription} from 'rxjs';
import {LogFactory} from '../../../core/logging/LogFactory';

const LOG = LogFactory.createLog('SavingInfo');

@Component({
    selector: 'dv-saving-info',
    templateUrl: './saving-info.component.html',
    styleUrls: ['./saving-info.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class SavingInfo implements OnInit {

    private static readonly HIDE_SAVED_AFTER_MS = 3000;

    @Input() private readonly saving$: Observable<boolean>;
    public showSaving: boolean;
    public showSaved: boolean;
    private subscription: Subscription;

    public constructor(
        private readonly ref: ChangeDetectorRef
    ) {
    }

    public ngOnInit(): void {
        this.subscription = this.saving$.subscribe(saving => {
            if (saving) {
                this.showSavingInfo();
            } else {
                this.hideSavingInfo();
            }
            this.ref.markForCheck();
        }, error => LOG.error(error));
    }

    private showSavingInfo(): void {
        this.showSaving = true;
    }

    private hideSavingInfo(): void {
        this.showSaving = false;
        this.showSaved = true;
        setTimeout(() => {
            this.showSaved = false;
            this.ref.markForCheck();
        }, SavingInfo.HIDE_SAVED_AFTER_MS);
    }
}
