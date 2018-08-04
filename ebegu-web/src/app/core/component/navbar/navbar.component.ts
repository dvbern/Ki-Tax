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
import {from as fromPromise, Observable, of} from 'rxjs';
import {filter} from 'rxjs/operators';
import AuthServiceRS from '../../../../authentication/service/AuthServiceRS.rest';
import {INewFallStateParams} from '../../../../gesuch/gesuch.route';
import GemeindeRS from '../../../../gesuch/service/gemeindeRS.rest';
import {TSEingangsart} from '../../../../models/enums/TSEingangsart';
import {TSRole} from '../../../../models/enums/TSRole';
import TSGemeinde from '../../../../models/TSGemeinde';
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';
import {DvNgGemeindeDialogComponent} from '../dv-ng-gemeinde-dialog/dv-ng-gemeinde-dialog.component';

@Component({
    selector: 'dv-navbar',
    templateUrl: './navbar.component.html',
    styleUrls: ['./navbar.component.less']
})
export class NavbarComponent {

    TSRoleUtil: any = TSRoleUtil;

    constructor(private readonly authServiceRS: AuthServiceRS,
                private readonly dialog: MatDialog,
                private readonly $state: StateService,
                private readonly gemeindeRS: GemeindeRS) {
        console.log('foo');
    }

    public getGemeindeIDFromUser$(): Observable<string> {
        if (this.authServiceRS.getPrincipal().hasJustOneGemeinde()) {
            return of(this.authServiceRS.getPrincipal().extractCurrentGemeindeId());

        } else {
            const dialogConfig = new MatDialogConfig();
            dialogConfig.disableClose = false; // dialog is canceled by clicking outside
            dialogConfig.autoFocus = true;
            dialogConfig.data = {
                // tslint:disable-next-line:rxjs-finnish
                gemeindeList: this.getListOfGemeinden$()
            };

            return this.dialog.open(DvNgGemeindeDialogComponent, dialogConfig).afterClosed();
        }

    }

    /**
     * Fuer den SUPER_ADMIN muessen wir die gesamte Liste von Gemeinden zurueckgeben, da er zu keiner Gemeinde gehoert aber alles
     * machen darf. Fuer andere Benutzer geben wir die Liste von Gemeinden zurueck, zu denen er gehoert.
     */
    private getListOfGemeinden$(): Observable<TSGemeinde[]> {
        if (this.authServiceRS.isRole(TSRole.SUPER_ADMIN)) {
            return fromPromise(this.gemeindeRS.getAllGemeinden());
        } else {
            return of(this.authServiceRS.getPrincipal().extractCurrentGemeinden());
        }
    }

    public createNewFall(): void {
        this.getGemeindeIDFromUser$()
            .pipe(
                filter(gemeindeId => !!gemeindeId)
            )
            .subscribe(
                (gemeindeId) => {
                    const params: INewFallStateParams = {
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
