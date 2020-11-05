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
import {MatDialog, MatDialogConfig, MatPaginator, MatSort, MatTableDataSource} from '@angular/material';
import {TranslateService} from '@ngx-translate/core';
import {StateService} from '@uirouter/core';
import {AbstractAdminViewController} from '../../../admin/abstractAdminView';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {TSBetreuungsangebotTyp} from '../../../models/enums/TSBetreuungsangebotTyp';
import {TSInstitutionStatus} from '../../../models/enums/TSInstitutionStatus';
import {TSRole} from '../../../models/enums/TSRole';
import {TSBerechtigung} from '../../../models/TSBerechtigung';
import {TSInstitution} from '../../../models/TSInstitution';
import {TSInstitutionListDTO} from '../../../models/TSInstitutionListDTO';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {DvNgRemoveDialogComponent} from '../../core/component/dv-ng-remove-dialog/dv-ng-remove-dialog.component';
import {Log, LogFactory} from '../../core/logging/LogFactory';
import {InstitutionRS} from '../../core/service/institutionRS.rest';

@Component({
    selector: 'dv-institution-list',
    templateUrl: './institution-list.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InstitutionListComponent extends AbstractAdminViewController implements OnInit, AfterViewInit {

    private readonly log: Log = LogFactory.createLog('InstitutionListComponent');

    public displayedColumns: string[] = [];
    public dataSource: MatTableDataSource<TSInstitutionListDTO>;

    @ViewChild(NgForm, { static: false }) public form: NgForm;
    @ViewChild(MatSort, { static: true }) public sort: MatSort;
    @ViewChild(MatPaginator, { static: true }) public paginator: MatPaginator;

    public constructor(
        private readonly institutionRS: InstitutionRS,
        private readonly dialog: MatDialog,
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
        this.institutionRS.getInstitutionenListDTOEditableForCurrentBenutzer()
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

    public removeInstitution(institution: any): void {
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
                    this.institutionRS.removeInstitution(institution.id).then(() => {
                        this.updateInstitutionenList();
                    });
                },
                () => {
                    this.log.error('error in observable. removeInstitution');
                }
            );
    }

    public createInstitutionBG(): void {
        this.goToAddInstitution({undefined});
    }

    public createInstitutionTS(): void {
        this.goToAddInstitution({
            betreuungsangebot: TSBetreuungsangebotTyp.TAGESSCHULE,
            betreuungsangebote: [TSBetreuungsangebotTyp.TAGESSCHULE]
        });
    }

    public createInstitutionFI(): void {
        this.goToAddInstitution({
            betreuungsangebot: TSBetreuungsangebotTyp.FERIENINSEL,
            betreuungsangebote: [TSBetreuungsangebotTyp.FERIENINSEL]
        });
    }

    private goToAddInstitution(params: any): void {
        this.$state.go('institution.add', params);
    }

    /**
     * Institutions in status EINGELADEN cannot be opened from the list. Only Exception: the InstitutionsAdmin for the
     * Institution in question can always open the Institution.
     */
    public openInstitution(institution: TSInstitution): void {
        if (this.hatBerechtigungEditieren(institution)) {
            this.$state.go('institution.edit', {
                institutionId: institution.id,
            });
        }
        return;
    }

    public hatBerechtigungEditieren(institution: TSInstitution): boolean {
        return institution.status !== TSInstitutionStatus.EINGELADEN
            || this.isCurrentUserAdminForInstitution(institution)
            || this.isSuperAdmin();
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

    public isCreateBGAllowed(): boolean {
        return this.authServiceRS.isOneOfRoles(TSRoleUtil.getMandantRoles());
    }

    public isCreateTSAllowed(): boolean {
        return this.authServiceRS.isOneOfRoles(TSRoleUtil.getGemeindeRoles())
            && this.authServiceRS.getPrincipal().mandant.angebotTS;
    }

    public isCreateFIAllowed(): boolean {
        return this.authServiceRS.isOneOfRoles(TSRoleUtil.getGemeindeRoles())
            && this.authServiceRS.getPrincipal().mandant.angebotFI;
    }

    public isDeleteAllowed(): boolean {
        return this.isSuperAdmin();
    }

    public showNoContentMessage(): boolean {
        return !this.dataSource || this.dataSource.data.length === 0;
    }

    private setDisplayedColumns(): void {
        this.displayedColumns = this.isDeleteAllowed()
            ? ['name', 'status', 'type', 'detail', 'remove']
            : ['name', 'status', 'type', 'detail'];
    }

    public isSuperAdmin(): boolean {
        return this.authServiceRS.isRole(TSRole.SUPER_ADMIN);
    }

    public doFilter = (value: string) => {
        if (this.dataSource) {
            this.dataSource.filter = value.trim().toLocaleLowerCase();
        }
    }

    public translateStatus(institution: TSInstitutionListDTO): string {
        const translatedStatus = this.translate.instant('INSTITUTION_STATUS_' + institution.status);
        const translatedCheck = institution.stammdatenCheckRequired
            ? this.translate.instant('INSTITUTION_STATUS_CHECK_REQUIRED')
            : '';
        return `${translatedStatus} ${translatedCheck}`;
    }
}
