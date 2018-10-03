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

import {AfterViewInit, Component, Input, OnInit, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {MatDialog, MatDialogConfig, MatSort, MatTableDataSource} from '@angular/material';
import * as angular from 'angular';
import {DvNgRemoveDialogComponent} from '../../../app/core/component/dv-ng-remove-dialog/dv-ng-remove-dialog.component';
import ErrorService from '../../../app/core/errors/service/ErrorService';
import {TraegerschaftRS} from '../../../app/core/service/traegerschaftRS.rest';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import {TSTraegerschaft} from '../../../models/TSTraegerschaft';
import EbeguUtil from '../../../utils/EbeguUtil';
import AbstractAdminViewController from '../../abstractAdminView';

@Component({
    selector: 'dv-traegerschaft-view',
    templateUrl: './traegerschaftView.html',
    styleUrls: ['./traegerschaftView.less']
})
export class TraegerschaftViewComponent extends AbstractAdminViewController implements OnInit, AfterViewInit {

    @Input() public traegerschaften: TSTraegerschaft[];

    public displayedColumns: string[] = ['name', 'remove'];
    public traegerschaft: TSTraegerschaft = undefined;
    public dataSource: MatTableDataSource<TSTraegerschaft>;

    @ViewChild(NgForm) public form: NgForm;
    @ViewChild(MatSort) public sort: MatSort;

    public constructor(private readonly traegerschaftRS: TraegerschaftRS,
                       private readonly errorService: ErrorService,
                       private readonly dialog: MatDialog,
                       authServiceRS: AuthServiceRS) {

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
            }
        );
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

                this.traegerschaft = undefined;
                this.traegerschaftRS.removeTraegerschaft(traegerschaft.id).then(() => {
                    const index = EbeguUtil.getIndexOfElementwithID(traegerschaft, this.traegerschaften);
                    if (index > -1) {
                        this.traegerschaften.splice(index, 1);
                        this.refreshTraegerschaftenList();
                    }
                });
            });
    }

    public createTraegerschaft(): void {
        this.traegerschaft = new TSTraegerschaft();
        this.traegerschaft.active = true;
    }

    public saveTraegerschaft(): void {
        if (!this.form.valid) {
            return;
        }

        this.errorService.clearAll();
        const newTraegerschaft = this.traegerschaft.isNew();
        this.traegerschaftRS.createTraegerschaft(this.traegerschaft).then((traegerschaft: TSTraegerschaft) => {
            if (newTraegerschaft) {
                this.traegerschaften.push(traegerschaft);
            } else {
                const index = EbeguUtil.getIndexOfElementwithID(traegerschaft, this.traegerschaften);
                if (index > -1) {
                    this.traegerschaften[index] = traegerschaft;
                    EbeguUtil.handleSmarttablesUpdateBug(this.traegerschaften);
                }
            }
            this.refreshTraegerschaftenList();
            this.traegerschaft = undefined;
        });
    }

    /**
     * To refresh the traegerschaftenlist we need to refresh the MatTableDataSource with the new list of
     * Traegerschaften.
     */
    private refreshTraegerschaftenList(): void {
        this.dataSource.data = this.traegerschaften;
    }

    public cancelTraegerschaft(): void {
        this.traegerschaft = undefined;
    }

    public setSelectedTraegerschaft(selected: TSTraegerschaft): void {
        this.traegerschaft = angular.copy(selected);
    }

    public showNoContentMessage(): boolean {
        return !this.dataSource || this.dataSource.data.length === 0;
    }
}
