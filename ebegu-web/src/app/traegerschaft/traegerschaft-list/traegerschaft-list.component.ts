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

import {AfterViewInit, ChangeDetectionStrategy, Component, Input, OnInit, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {MatDialog, MatDialogConfig, MatSort, MatTableDataSource} from '@angular/material';
import {StateService} from '@uirouter/core';
import AbstractAdminViewController from '../../../admin/abstractAdminView';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import {TSTraegerschaft} from '../../../models/TSTraegerschaft';
import EbeguUtil from '../../../utils/EbeguUtil';
import {DvNgRemoveDialogComponent} from '../../core/component/dv-ng-remove-dialog/dv-ng-remove-dialog.component';
import ErrorService from '../../core/errors/service/ErrorService';
import {TraegerschaftRS} from '../../core/service/traegerschaftRS.rest';

@Component({
  selector: 'dv-traegerschaft-list',
  templateUrl: './traegerschaft-list.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TraegerschaftListComponent extends AbstractAdminViewController implements OnInit, AfterViewInit {

    @Input() public traegerschaften: TSTraegerschaft[];

    public displayedColumns: string[] = ['name', 'remove'];
    public dataSource: MatTableDataSource<TSTraegerschaft>;

    @ViewChild(NgForm) public form: NgForm;
    @ViewChild(MatSort) public sort: MatSort;

    public constructor(
        private readonly traegerschaftRS: TraegerschaftRS,
        private readonly dialog: MatDialog,
        authServiceRS: AuthServiceRS,
        private readonly $state: StateService,
    ) {

        super(authServiceRS);
    }

    public ngOnInit(): void {
        this.dataSource = new MatTableDataSource(this.traegerschaften);
        this.sortTable();
    }

    /**
     * It sorts the table by default using the variable sort.
     */
    private sortTable(): void {
        this.sort.sort({
                id: 'name',
                start: 'asc',
                disableClear: false,
            },
        );
    }

    public openTraegerschaft(selected: TSTraegerschaft): void {
        if (this.authServiceRS.isOneOfRoles(this.TSRoleUtil.getAdministratorBgTsGemeindeRole())) {
            this.$state.go('traegerschaft.edit', {traegerschaftId: selected.id});
        }
    }

    public ngAfterViewInit(): void {
        this.dataSource.sort = this.sort;
    }

    public removeTraegerschaft(traegerschaft: any): void {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            title: 'LOESCHEN_DIALOG_TITLE',
        };

        this.dialog.open(DvNgRemoveDialogComponent, dialogConfig).afterClosed()
            .subscribe(userAccepted => {   // User confirmed removal
                if (!userAccepted) {
                    return;
                }
                this.traegerschaftRS.removeTraegerschaft(traegerschaft.id).then(() => {
                    const index = EbeguUtil.getIndexOfElementwithID(traegerschaft, this.traegerschaften);
                    if (index > -1) {
                        this.traegerschaften.splice(index, 1);
                        this.refreshTraegerschaftenList();
                    }
                });
            });
    }

    public addTraegerschaft(): void {
        this.$state.go('traegerschaft.add');
    }

    /**
     * To refresh the traegerschaftenlist we need to refresh the MatTableDataSource with the new list of
     * Traegerschaften.
     */
    private refreshTraegerschaftenList(): void {
        this.dataSource.data = this.traegerschaften;
    }

    public showNoContentMessage(): boolean {
        return !this.dataSource || this.dataSource.data.length === 0;
    }
}
