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

import {ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {MatDialog, MatDialogConfig} from '@angular/material';
import {TranslateService} from '@ngx-translate/core';
import {StateService, Transition} from '@uirouter/core';
import * as moment from 'moment';
import {of} from 'rxjs';
import {filter, mergeMap} from 'rxjs/operators';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import {TSBenutzerStatus} from '../../../models/enums/TSBenutzerStatus';
import {TSRole} from '../../../models/enums/TSRole';
import TSBenutzer from '../../../models/TSBenutzer';
import TSBerechtigung from '../../../models/TSBerechtigung';
import TSBerechtigungHistory from '../../../models/TSBerechtigungHistory';
import {TSDateRange} from '../../../models/types/TSDateRange';
import DateUtil from '../../../utils/DateUtil';
import EbeguUtil from '../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {Permission} from '../../authorisation/Permission';
import {PERMISSIONS} from '../../authorisation/Permissions';
import {DvNgRemoveDialogComponent} from '../../core/component/dv-ng-remove-dialog/dv-ng-remove-dialog.component';
import {LogFactory} from '../../core/logging/LogFactory';
import BenutzerRS from '../../core/service/benutzerRS.rest';

const LOG = LogFactory.createLog('BenutzerComponent');

@Component({
    selector: 'dv-benutzer',
    templateUrl: './benutzer.component.html',
    styleUrls: ['./benutzer.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BenutzerComponent implements OnInit {

    @ViewChild(NgForm) private readonly form: NgForm;

    public readonly TSRoleUtil = TSRoleUtil;
    public readonly TSBenutzerStatus = TSBenutzerStatus;

    public tomorrow: moment.Moment = DateUtil.today().add(1, 'days');

    public selectedUser: TSBenutzer;

    public currentBerechtigung: TSBerechtigung;
    public futureBerechtigung?: TSBerechtigung;
    public isDefaultVerantwortlicher: boolean = false;
    public isDisabled = true;

    private _berechtigungHistoryList: TSBerechtigungHistory[];

    public constructor(
        private readonly $transition$: Transition,
        private readonly changeDetectorRef: ChangeDetectorRef,
        private readonly $state: StateService,
        private readonly translate: TranslateService,
        private readonly authServiceRS: AuthServiceRS,
        private readonly benutzerRS: BenutzerRS,
        private readonly dialog: MatDialog,
    ) {
    }

    public get berechtigungHistoryList(): TSBerechtigungHistory[] {
        return this._berechtigungHistoryList;
    }

    // noinspection JSMethodCanBeStatic
    /**
     * Anonymous doesn't give any useful information to the user. For this reason we show system instead of anonymous
     */
    public getGeaendertDurch(role: TSBerechtigungHistory): string {
        if (role.userErstellt === 'anonymous') {
            return 'system';
        }

        return role.userErstellt;
    }

    public ngOnInit(): void {
        const username: string = this.$transition$.params().benutzerId;

        if (!username) {
            return;
        }

        this.benutzerRS.findBenutzer(username).then(result => {
            this.selectedUser = result;
            this.initSelectedUser();
            // Falls der Benutzer JA oder SCH Benutzer ist, muss geprüft werden, ob es sich um den
            // "Default-Verantwortlichen" des entsprechenden Amtes handelt
            if (PERMISSIONS[Permission.ROLE_GEMEINDE].indexOf(this.currentBerechtigung.role) > -1) {
                this.benutzerRS.isBenutzerDefaultBenutzerOfAnyGemeinde(this.selectedUser.username).then(isDefaultUser => {
                    this.isDefaultVerantwortlicher = (isDefaultUser === true);
                });
            }
            this.changeDetectorRef.markForCheck();
        });
    }

    public getBerechtigungHistoryDescription(history: TSBerechtigungHistory): string {
        const role = this.getTranslatedRole(history.role);
        const details = history.getDescription();

        return EbeguUtil.isEmptyStringNullOrUndefined(details) ? role : `${role} (${details})`;
    }

    public saveBenutzerBerechtigungen(): void {
        if (!this.form.valid) {
            return;
        }

        if (!this.isMoreThanGesuchstellerRole()) {
            this.doSaveBenutzer();

            return;
        }

        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            title: 'BENUTZER_ROLLENZUWEISUNG_CONFIRMATION_TITLE',
            text: 'BENUTZER_ROLLENZUWEISUNG_CONFIRMATION_TEXT',
        };

        const isAdminRole = this.isAdminRole();

        this.dialog.open(DvNgRemoveDialogComponent, dialogConfig)
            .afterClosed()
            .pipe(
                filter(userAccepted => !!userAccepted),
                mergeMap(() => {
                    if (!isAdminRole) {
                        return of(undefined);
                    }

                    const adminDialogConfig = new MatDialogConfig();
                    adminDialogConfig.data = {
                        title: 'BENUTZER_ROLLENZUWEISUNG_CONFIRMATION_ADMIN_TITLE',
                        text: 'BENUTZER_ROLLENZUWEISUNG_CONFIRMATION_ADMIN_TEXT',
                    };

                    return this.dialog.open(DvNgRemoveDialogComponent, adminDialogConfig)
                        .afterClosed()
                        .pipe(filter(userAccepted => !!userAccepted));
                }),
            )
            .subscribe(
                () => this.doSaveBenutzer(),
                err => LOG.error(err),
            );
    }

    public inactivateBenutzer(): void {
        if (!(this.isDisabled || this.form.valid)) {
            return;
        }

        this.benutzerRS.inactivateBenutzer(this.selectedUser).then(changedUser => {
            this.selectedUser = changedUser;
            this.changeDetectorRef.markForCheck();
        });
    }

    public reactivateBenutzer(): void {
        if (!(this.isDisabled || this.form.valid)) {
            return;
        }

        this.benutzerRS.reactivateBenutzer(this.selectedUser).then(changedUser => {
            this.selectedUser = changedUser;
            this.changeDetectorRef.markForCheck();
        });
    }

    public canAddBerechtigung(): boolean {
        return EbeguUtil.isNullOrUndefined(this.futureBerechtigung);
    }

    public addBerechtigung(): void {
        const berechtigung = new TSBerechtigung();
        berechtigung.role = TSRole.GESUCHSTELLER;
        berechtigung.gueltigkeit = new TSDateRange();
        berechtigung.gueltigkeit.gueltigAb = this.tomorrow;
        this.futureBerechtigung = berechtigung;
    }

    public enableBenutzer(): void {
        this.isDisabled = false;
    }

    public removeBerechtigung(): void {
        this.futureBerechtigung = undefined;
    }

    public cancel(): void {
        this.navigateBackToUsersList();
    }

    private getTranslatedRole(role: TSRole): string {
        return this.translate.instant(TSRoleUtil.translationKeyForRole(role, true));
    }

    private initSelectedUser(): void {
        this.currentBerechtigung = this.selectedUser.berechtigungen[0];
        this.futureBerechtigung = this.selectedUser.berechtigungen[1];
        this.benutzerRS.getBerechtigungHistoriesForBenutzer(this.selectedUser.username).then(result => {
            this._berechtigungHistoryList = result;
            this.changeDetectorRef.markForCheck();
        });
    }

    private isAdminRole(): boolean {
        return this.isAtLeastOneRoleInList(TSRoleUtil.getAdministratorRoles());
    }

    private isMoreThanGesuchstellerRole(): boolean {
        return this.isAtLeastOneRoleInList(TSRoleUtil.getAllRolesButGesuchsteller());
    }

    public isSuperAdmin(): boolean {
        return this.authServiceRS.isRole(TSRole.SUPER_ADMIN);
    }

    private isAtLeastOneRoleInList(rolesToCheck: Array<TSRole>): boolean {
        // Es muessen alle vorhandenen Rollen geprueft werden
        if (rolesToCheck.indexOf(this.currentBerechtigung.role) > -1) {
            return true;
        }

        return this.futureBerechtigung && rolesToCheck.indexOf(this.futureBerechtigung.role) > -1;
    }

    private doSaveBenutzer(): void {
        this.selectedUser.berechtigungen = [];

        this.currentBerechtigung.prepareForSave();
        this.selectedUser.berechtigungen.push(this.currentBerechtigung);

        if (this.futureBerechtigung) {
            this.futureBerechtigung.prepareForSave();
            this.selectedUser.berechtigungen.push(this.futureBerechtigung);
        }

        this.benutzerRS.saveBenutzerBerechtigungen(this.selectedUser).then(() => {
            this.isDisabled = true;
            this.navigateBackToUsersList();
        }).catch(err => {
            LOG.error('Could not save Benutzer', err);
            this.initSelectedUser();
        });
    }

    private navigateBackToUsersList(): void {
        this.$state.go('admin.benutzerlist');
    }
}
