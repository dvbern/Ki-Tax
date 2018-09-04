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

import * as angular from 'angular';
import {AfterViewInit, Component, Input, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {MatDialog, MatDialogConfig, MatSort, MatSortable, MatTableDataSource} from '@angular/material';
import {Subject} from 'rxjs/index';
import {takeUntil} from 'rxjs/operators';
import {DvNgRemoveDialogComponent} from '../../../app/core/component/dv-ng-remove-dialog/dv-ng-remove-dialog.component';
import ErrorService from '../../../app/core/errors/service/ErrorService';
import {LogFactory} from '../../../app/core/logging/LogFactory';
import TSGemeinde from '../../../models/TSGemeinde';
import GemeindeRS from '../../../gesuch/service/gemeindeRS.rest';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import EbeguUtil from '../../../utils/EbeguUtil';
import AbstractAdminViewController from '../../abstractAdminView';

const LOG = LogFactory.createLog('GemeindenViewComponent');

@Component({
    selector: 'dv-gemeinden-view',
    templateUrl: './gemeindenView.html',
    styleUrls: ['./gemeindenView.less']
})

export class GemeindenViewComponent extends AbstractAdminViewController implements OnInit, OnDestroy, AfterViewInit {

    displayedColumns: string[] = ['name', 'status'];
    gemeinde: TSGemeinde = undefined;
    gemeindenList: Array<TSGemeinde>;
    dataSource: MatTableDataSource<TSGemeinde>;
    private readonly unsubscribe$ = new Subject<void>();

    @ViewChild(NgForm) form: NgForm;
    @ViewChild(MatSort) sort: MatSort;

    constructor(private readonly gemeindeRS: GemeindeRS,
                private readonly errorService: ErrorService,
                private readonly dialog: MatDialog,
                authServiceRS: AuthServiceRS) {

        super(authServiceRS);

        this.updateGemeindenList();
    }

    public ngOnInit(): void {
        this.sortTable();
    }

    public ngAfterViewInit(): void {
        this.dataSource.sort = this.sort;
    }

    public ngOnDestroy(): void {
        this.unsubscribe$.next();
        this.unsubscribe$.complete();
    }

    private updateGemeindenList(): void {
        this.gemeindeRS.getGemeindenForPrincipal$()
            .pipe(takeUntil(this.unsubscribe$))
            .subscribe(gemeinden => {
                    this.dataSource = new MatTableDataSource(gemeinden);
                },
                err => LOG.error(err)
            );
    }

    /**
     * It sorts the table by default using the variable sort.
     */
    private sortTable() {
        this.sort.sort(<MatSortable>{
                id: 'name',
                start: 'asc'
            }
        );
    }

    setSelectedGemeinde(selected: TSGemeinde): void {
        this.gemeinde = angular.copy(selected);
    }

    public showNoContentMessage(): boolean {
        return !this.dataSource || this.dataSource.data.length === 0;
    }
}
