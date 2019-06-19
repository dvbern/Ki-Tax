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

import {AfterViewInit, ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {MatPaginator, MatSort, MatTableDataSource} from '@angular/material';
import {TranslateService} from '@ngx-translate/core';
import {StateService} from '@uirouter/core';
import AbstractAdminViewController from '../../../admin/abstractAdminView';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import {TSInstitutionStatus} from '../../../models/enums/TSInstitutionStatus';
import {TSRole} from '../../../models/enums/TSRole';
import TSBerechtigung from '../../../models/TSBerechtigung';
import TSInstitution from '../../../models/TSInstitution';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {InstitutionRS} from '../../core/service/institutionRS.rest';

@Component({
    selector: 'dv-institution-list',
    templateUrl: './institution-list.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InstitutionListComponent extends AbstractAdminViewController implements OnInit, AfterViewInit {

    public displayedColumns: string[] = [];
    public institutionen: TSInstitution[];
    public dataSource: MatTableDataSource<TSInstitution>;

    @ViewChild(NgForm) public form: NgForm;
    @ViewChild(MatSort) public sort: MatSort;
    @ViewChild(MatPaginator) public paginator: MatPaginator;

    public constructor(
        private readonly institutionRS: InstitutionRS,
        private readonly changeDetectorRef: ChangeDetectorRef,
        private readonly $state: StateService,
        authServiceRS: AuthServiceRS,
        private readonly translate: TranslateService,
    ) {
        super(authServiceRS);
    }

    public ngOnInit(): void {
        this.updateInstitutionenList();
        this.sortTable();
        this.setDisplayedColumns();
    }

    public ngAfterViewInit(): void {
        if (!this.dataSource) {
            return;
        }
        this.dataSource.sort = this.sort;
        this.dataSource.paginator = this.paginator;
    }

    public updateInstitutionenList(): void {
        this.institutionRS.getInstitutionenForCurrentBenutzer()
            .then(insti => {
                this.dataSource = new MatTableDataSource(insti);
                this.dataSource.paginator = this.paginator;
                this.changeDetectorRef.markForCheck();
                this.dataSource.sort = this.sort;
            });
    }

    private sortTable(): void {
        this.sort.sort({
                id: 'name',
                start: 'asc',
                disableClear: false,
            },
        );
    }

    public createInstitution(): void {
        this.$state.go('institution.add');
    }

    /**
     * Institutions in status EINGELADEN cannot be opened from the list. Only Exception: the InstitutionsAdmin for the
     * Institution in question can always open the Institution.
     */
    public openInstitution(institution: TSInstitution): void {
        if (institution.status !== TSInstitutionStatus.EINGELADEN
            || this.isCurrentUserAdminForInstitution(institution)
            || this.isSuperAdmin()
        ) {
            this.$state.go('institution.edit', {
                institutionId: institution.id,
            });
        }
        return;
    }

    private isCurrentUserAdminForInstitution(institution: TSInstitution): boolean {
        const currentBerechtigung = this.authServiceRS.getPrincipal().currentBerechtigung;
        if (currentBerechtigung) {
            return this.isCurrentUserTraegerschaftAdminOfSelectedInstitution(institution, currentBerechtigung)
                || this.isCurrentUserInstitutionAdminOfSelectedInstitution(institution, currentBerechtigung);
        }
        return false;
    }

    private isCurrentUserTraegerschaftAdminOfSelectedInstitution(
        institution: TSInstitution,
        currentBerechtigung: TSBerechtigung,
    ): boolean {
        return currentBerechtigung.role === TSRole.ADMIN_TRAEGERSCHAFT
            && (currentBerechtigung.traegerschaft && institution.traegerschaft
                && currentBerechtigung.traegerschaft.id === institution.traegerschaft.id);
    }

    private isCurrentUserInstitutionAdminOfSelectedInstitution(
        institution: TSInstitution,
        currentBerechtigung: TSBerechtigung,
    ): boolean {
        return currentBerechtigung.role === TSRole.ADMIN_INSTITUTION
            && (currentBerechtigung.institution
                && currentBerechtigung.institution.id === institution.id);
    }

    public isCreateAllowed(): boolean {
        return this.authServiceRS.isOneOfRoles(TSRoleUtil.getMandantRoles());
    }

    public showNoContentMessage(): boolean {
        return !this.dataSource || this.dataSource.data.length === 0;
    }

    private setDisplayedColumns(): void {
        this.displayedColumns = ['name', 'status', 'detail'];
    }

    public isSuperAdmin(): boolean {
        return this.authServiceRS.isRole(TSRole.SUPER_ADMIN);
    }

    public doFilter = (value: string) => {
        this.dataSource.filter = value.trim().toLocaleLowerCase();
    }

    public translateStatus(institution: TSInstitution): string {
        const translatedStatus = this.translate.instant('INSTITUTION_STATUS_' + institution.status);
        const translatedCheck = institution.stammdatenCheckRequired
            ? this.translate.instant('INSTITUTION_STATUS_CHECK_REQUIRED')
            : '';
        return `${translatedStatus} ${translatedCheck}`;
    }
}