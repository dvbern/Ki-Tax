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

import {Component, EventEmitter, Input, Output} from '@angular/core';
import {MatDialog, MatDialogConfig} from '@angular/material';
import {DvNgMitteilungDelegationDialogComponent} from "../dv-ng-mitteilung-delegation-dialog/dv-ng-mitteilung-delegation-dialog.component";

@Component({
    selector: 'dv-mitteilung-delegation',
    templateUrl: './dv-mitteilung-delegation.html',
})
export class DvMitteilungDelegationComponent {

    @Input() public mitteilungId: string;
    @Input() public gemeindeId: string;
    @Output() valueChange  = new EventEmitter();

    public constructor(private readonly dialog: MatDialog) {
    }

    public showDialog(): void {
        let dialogConfig: MatDialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            mitteilungId: this.mitteilungId,
            gemeindeId: this.gemeindeId
        };
        this.dialog.open(DvNgMitteilungDelegationDialogComponent, dialogConfig).afterClosed().subscribe(result=>{
            if (result) {
                this.valueChange.emit();
            }
        });
    }
}
