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

import {ChangeDetectionStrategy, Component, Input, OnInit} from '@angular/core';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {TranslateService} from '@ngx-translate/core';
import {filter, mergeMap} from 'rxjs/operators';
import {DvNgConfirmDialogComponent} from '../../../core/component/dv-ng-confirm-dialog/dv-ng-confirm-dialog.component';
import {ErrorService} from '../../../core/errors/service/ErrorService';
import {LastenausgleichTSService} from '../../services/lastenausgleich-ts.service';

@Component({
    selector: 'dv-freigabe',
    templateUrl: './freigabe.component.html',
    styleUrls: ['./freigabe.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FreigabeComponent implements OnInit {

    @Input() public lastenausgleichID: string;

    public constructor(
        private readonly translate: TranslateService,
        private readonly errorService: ErrorService,
        private readonly latsService: LastenausgleichTSService,
        private readonly dialog: MatDialog,
    ) {
    }

    public ngOnInit(): void {
    }

    public freigeben(): void {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            frage: this.translate.instant('LATS_FRAGE_GEMEINDE_ANTRAG_FREIGABE'),
        };
        this.dialog.open(DvNgConfirmDialogComponent, dialogConfig)
            .afterClosed()
            .pipe(
                filter(result => !!result),
                mergeMap(() => this.latsService.getLATSAngabenGemeindeContainer()),
            )
            .subscribe(container => {
                this.latsService.latsGemeindeAntragFreigeben(container);
            });
    }
}
