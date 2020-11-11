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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {Component, EventEmitter, Input, Output} from '@angular/core';
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import {DvNgMitteilungDelegationDialogComponent} from '../dv-ng-mitteilung-delegation-dialog/dv-ng-mitteilung-delegation-dialog.component';
import {LogFactory} from '../../logging/LogFactory';

const LOG = LogFactory.createLog('DvMitteilungDelegationComponent');

@Component({
    selector: 'dv-mitteilung-delegation',
    templateUrl: './dv-mitteilung-delegation.html',
})
export class DvMitteilungDelegationComponent {

    @Input() public mitteilungId: string;
    @Input() public gemeindeId: string;
    @Output() public readonly valueChange  = new EventEmitter<undefined>();

    public constructor(private readonly dialog: MatDialog) {
    }

    public showDialog(): void {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            mitteilungId: this.mitteilungId,
            gemeindeId: this.gemeindeId
        };
        this.dialog.open(DvNgMitteilungDelegationDialogComponent, dialogConfig).afterClosed().subscribe(result => {
            if (result) {
                this.valueChange.emit();
            }
        }, err => LOG.error(err), );
    }
}
