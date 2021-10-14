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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {Platform} from '@angular/cdk/platform';
import {Component, OnInit, ChangeDetectionStrategy} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {TranslateService} from '@ngx-translate/core';
import {DvNgOkDialogComponent} from '../../../core/component/dv-ng-ok-dialog/dv-ng-ok-dialog.component';

@Component({
    selector: 'dv-ie-deprecation-warning',
    templateUrl: './ie-deprecation-warning.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class IeDeprecationWarningComponent implements OnInit {

    public constructor(
        private readonly platform: Platform,
        private readonly dialog: MatDialog,
        private readonly translate: TranslateService,
    ) {
    }

    public ngOnInit(): void {
        const dialogOptions = {
            data: {
                title: this.translate.instant('IE_DEPRECATION_WARNING'),
            },
        };
        // trident is the rendering engine for IE
        if (this.platform.TRIDENT) {
            this.dialog.open(DvNgOkDialogComponent, dialogOptions);
        }
    }

}
