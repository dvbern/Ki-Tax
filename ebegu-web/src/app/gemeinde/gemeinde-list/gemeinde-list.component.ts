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

import {
    AfterViewInit,
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    OnDestroy,
    OnInit,
    ViewChild
} from '@angular/core';
import {NgForm} from '@angular/forms';
import {MatDialog, MatSort, MatSortable, MatTableDataSource} from '@angular/material';
import {StateService} from '@uirouter/core';
import * as angular from 'angular';
import {Subject} from 'rxjs';
import {map, takeUntil} from 'rxjs/operators';
import AbstractAdminViewController from '../../../admin/abstractAdminView';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import GemeindeRS from '../../../gesuch/service/gemeindeRS.rest';
import TSGemeinde from '../../../models/TSGemeinde';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import ErrorService from '../../core/errors/service/ErrorService';
import {LogFactory} from '../../core/logging/LogFactory';

const LOG = LogFactory.createLog('GemeindeListComponent');

@Component({
    selector: 'dv-gemeinde-list',
    templateUrl: './gemeinde-list.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})

export class GemeindeListComponent extends AbstractAdminViewController implements OnInit, OnDestroy, AfterViewInit {

    displayedColumns: string[] = ['name', 'status'];
    gemeinde: TSGemeinde = undefined;
    dataSource: MatTableDataSource<TSGemeinde>;
    private readonly unsubscribe$ = new Subject<void>();
    controllerAs = 'vm';

    @ViewChild(NgForm) form: NgForm;
    @ViewChild(MatSort) sort: MatSort;

    constructor(private readonly gemeindeRS: GemeindeRS,
                private readonly $state: StateService,
                private readonly errorService: ErrorService,
                private readonly dialog: MatDialog,
                private readonly changeDetectorRef: ChangeDetectorRef,
                authServiceRS: AuthServiceRS) {

        super(authServiceRS);
    }

    public ngOnInit(): void {
        this.updateGemeindenList();
        this.sortTable();
    }

    public ngAfterViewInit(): void {
        this.dataSource.sort = this.sort;
    }

    public ngOnDestroy(): void {
        this.unsubscribe$.next();
        this.unsubscribe$.complete();
    }

    public updateGemeindenList(): void {
        this.gemeindeRS.getGemeindenForPrincipal$()
            .pipe(map(gemeinden => {
                    const dataSource = new MatTableDataSource(gemeinden);
                    return dataSource;
                }),
                takeUntil(this.unsubscribe$)
            )
            .subscribe(dataSource => {
                this.dataSource = dataSource;
                this.changeDetectorRef.markForCheck();
            });
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

    /**
     * @param gemeinde
     * @param event optinally this function can check if ctrl was clicked when opeing
     */
    public editGemeinde(gemeinde: TSGemeinde, event: any): void {
        if (gemeinde) {
            this.setSelectedGemeinde(gemeinde);
            this.$state.go('admin.addgemeinde', {gemeindeId: this.gemeinde.id});
        }
    }

    public addGemeinde(): void {
        this.$state.go('admin.addgemeinde', {gemeindeId: null});
    }


    isAccessible(): boolean {
        return this.authServiceRS.isOneOfRoles(TSRoleUtil.getMandantRoles());
    }

    public showNoContentMessage(): boolean {
        return !this.dataSource || this.dataSource.data.length === 0;
    }

}
