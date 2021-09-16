/*
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

import {ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnInit, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {StateService} from '@uirouter/core';
import {from, Observable} from 'rxjs';
import {AbstractAdminViewController} from '../../../admin/abstractAdminView';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {TSRole} from '../../../models/enums/TSRole';
import {TSTraegerschaft} from '../../../models/TSTraegerschaft';
import {DvNgRemoveDialogComponent} from '../../core/component/dv-ng-remove-dialog/dv-ng-remove-dialog.component';
import {Log, LogFactory} from '../../core/logging/LogFactory';
import {TraegerschaftRS} from '../../core/service/traegerschaftRS.rest';
import {DVEntitaetListItem} from '../../shared/interfaces/DVEntitaetListItem';

@Component({
  selector: 'dv-traegerschaft-list',
  templateUrl: './traegerschaft-list.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TraegerschaftListComponent extends AbstractAdminViewController implements OnInit {

    private readonly log: Log = LogFactory.createLog('TraegerschaftListComponent');

    @Input() public traegerschaften: TSTraegerschaft[];

    public hiddenDVTableColumns = [''];

    public antragList$: Observable<DVEntitaetListItem[]>;

    @ViewChild(NgForm) public form: NgForm;

    public constructor(
        private readonly traegerschaftRS: TraegerschaftRS,
        private readonly dialog: MatDialog,
        authServiceRS: AuthServiceRS,
        private readonly $state: StateService,
        private readonly cd: ChangeDetectorRef,
    ) {

        super(authServiceRS);
    }

    public ngOnInit(): void {
        this.setDisplayedColumns();
        this.loadData();
    }

    public loadData(): void {
        const readDeleteAllowed = this.isReadDeleteAllowed();
        this.antragList$ = from(this.traegerschaftRS.getAllActiveTraegerschaften()
            .then(traegerschaftList => {
                const entitaetListItems: DVEntitaetListItem[] = [];
                traegerschaftList.forEach(
                    traegerschaft => {
                        const dvListItem = {
                            id: traegerschaft.id,
                            name: traegerschaft.name,
                            institutionCount: traegerschaft.institutionCount,
                            canEdit: readDeleteAllowed,
                            canRemove: readDeleteAllowed,
                        };
                        entitaetListItems.push(dvListItem);
                    },
                );
                this.cd.markForCheck();
                return entitaetListItems;
            }));
    }

    public isReadDeleteAllowed(): boolean {
        return this.isSuperAdmin();
    }

    public isSuperAdmin(): boolean {
        return this.authServiceRS.isRole(TSRole.SUPER_ADMIN);
    }

    public openTraegerschaft(id: string): void {
        if (this.authServiceRS.isOneOfRoles(this.TSRoleUtil.getMandantRoles())) {
            this.$state.go('traegerschaft.edit', {traegerschaftId: id});
        }
    }

    public removeTraegerschaft(id: string): void {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            title: 'LOESCHEN_DIALOG_TITLE',
        };

        this.dialog.open(DvNgRemoveDialogComponent, dialogConfig).afterClosed()
            .subscribe(
                userAccepted => {   // User confirmed removal
                    if (!userAccepted) {
                        return;
                    }
                    this.traegerschaftRS.removeTraegerschaft(id).then(() => {
                        this.loadData();
                    });
                },
                () => {
                    this.log.error('error has occurred while closing the remove dialog for Traegerschaft');
                }
            );
    }

    public addTraegerschaft(): void {
        this.$state.go('traegerschaft.add');
    }

    private setDisplayedColumns(): void {
        this.hiddenDVTableColumns = this.isReadDeleteAllowed()
            ? ['status', 'type', ]
            : ['status', 'type', 'remove'];
    }
}
