/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
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
import {MatDialog, MatDialogConfig} from '@angular/material';
import {StateService} from '@uirouter/core';
import {IOnDestroy} from 'angular';
import {Observable} from 'rxjs/Observable';
import {of} from 'rxjs/observable/of';
import {filter} from 'rxjs/operators';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import {INewFallStateParams} from '../../../gesuch/gesuch.route';
import {TSEingangsart} from '../../../models/enums/TSEingangsart';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {DvNgGemeindeDialogComponent} from '../dv-ng-gemeinde-dialog/dv-ng-gemeinde-dialog.component';

require('./dv-ng-navbar.less');

@Component({
    selector: 'dv-ng-topmenu',
    template: require('./dv-ng-navbar.html'),
})
export class DvNgNavbar implements IOnDestroy {

    TSRoleUtil: any = TSRoleUtil;

    constructor(private authServiceRS: AuthServiceRS, private dialog: MatDialog,
                private $state: StateService) {
    }

    public $onDestroy(): void {
        // todo ?????
    }

    public getGemeindeIDFromUser(): Observable<string> {
        if (this.authServiceRS.getPrincipal().hasJustOneGemeinde()) {
            // TODO return this.authServiceRS.getPrincipal().extractCurrentGemeinde().id;
            // return '80a8e496-b73c-4a4a-a163-a0b2caf76487'; // ostermundigen
            return of('ea02b313-e7c3-4b26-9ef7-e413f4046db2'); // bern

        } else {
            const dialogConfig = new MatDialogConfig();
            dialogConfig.disableClose = false; // dialog is canceled by clicking outside
            dialogConfig.autoFocus = true;

            return this.dialog.open(DvNgGemeindeDialogComponent, dialogConfig).afterClosed();
        }

    }

    public createNewFall(): void {
        this.getGemeindeIDFromUser()
            .pipe(
                filter(gemeindeId => !!gemeindeId)
            )
            .subscribe(
                (gemeindeId) => {
                    let params: INewFallStateParams = {
                        gesuchsperiodeId: null,
                        createMutation: null,
                        createNew: 'true',
                        gesuchId: null,
                        dossierId: null,
                        gemeindeId: gemeindeId,
                        eingangsart: TSEingangsart.PAPIER,
                    };
                    this.$state.go('gesuch.fallcreation', params);
                }
            );
    }
}
