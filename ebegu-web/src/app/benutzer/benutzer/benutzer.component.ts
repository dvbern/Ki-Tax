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
import {TSRole} from '../../../models/enums/TSRole';
import TSBerechtigung from '../../../models/TSBerechtigung';
import TSBerechtigungHistory from '../../../models/TSBerechtigungHistory';
import TSInstitution from '../../../models/TSInstitution';
import {TSTraegerschaft} from '../../../models/TSTraegerschaft';
import TSBenutzer from '../../../models/TSBenutzer';
import {TSDateRange} from '../../../models/types/TSDateRange';
import DateUtil from '../../../utils/DateUtil';
import EbeguUtil from '../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {DvNgRemoveDialogComponent} from '../../core/component/dv-ng-remove-dialog/dv-ng-remove-dialog.component';
import {LogFactory} from '../../core/logging/LogFactory';
import {ApplicationPropertyRS} from '../../core/rest-services/applicationPropertyRS.rest';
import {InstitutionRS} from '../../core/service/institutionRS.rest';
import {TraegerschaftRS} from '../../core/service/traegerschaftRS.rest';
import UserRS from '../../core/service/userRS.rest';

const LOG = LogFactory.createLog('BenutzerComponent');

@Component({
    selector: 'dv-benutzer',
    templateUrl: './benutzer.component.html',
    styleUrls: ['./benutzer.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class BenutzerComponent implements OnInit {

    @ViewChild(NgForm) form: NgForm;

    TSRoleUtil = TSRoleUtil;

    tomorrow: moment.Moment = DateUtil.today().add(1, 'days');

    public selectedUser: TSBenutzer;
    public institutionenList: Array<TSInstitution> = [];
    public traegerschaftenList: Array<TSTraegerschaft> = [];

    public currentBerechtigung: TSBerechtigung;
    public futureBerechtigung: TSBerechtigung;
    public isDefaultVerantwortlicher: boolean = false;
    public isDisabled = true;

    private _berechtigungHistoryList: TSBerechtigungHistory[];

    constructor(private readonly $transition$: Transition,
                private readonly changeDetectorRef: ChangeDetectorRef,
                private readonly $state: StateService,
                private readonly translate: TranslateService,
                private readonly authServiceRS: AuthServiceRS,
                private readonly institutionRS: InstitutionRS,
                private readonly traegerschaftenRS: TraegerschaftRS,
                private readonly userRS: UserRS,
                private readonly applicationPropertyRS: ApplicationPropertyRS,
                private readonly dialog: MatDialog) {
    }

    public get berechtigungHistoryList(): TSBerechtigungHistory[] {
        return this._berechtigungHistoryList;
    }

    private static initInstitution(berechtigung: TSBerechtigung): void {
        if (berechtigung && !berechtigung.institution) {
            berechtigung.institution = new TSInstitution();
        }
    }

    private static initTraegerschaft(berechtigung: TSBerechtigung): void {
        if (berechtigung && !berechtigung.traegerschaft) {
            berechtigung.traegerschaft = new TSTraegerschaft();
        }
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
        this.updateInstitutionenList();
        this.updateTraegerschaftenList();
        const username: string = this.$transition$.params().benutzerId;

        if (!username) {
            return;
        }

        this.userRS.findBenutzer(username).then(result => {
            this.selectedUser = result;
            this.initSelectedUser();
            // Falls der Benutzer JA oder SCH Benutzer ist, muss geprüft werden, ob es sich um den
            // "Default-Verantwortlichen" des entsprechenden Amtes handelt
            if (TSRoleUtil.getAdministratorJugendamtRole().indexOf(this.currentBerechtigung.role) > -1) {
                this.applicationPropertyRS.getByName('DEFAULT_VERANTWORTLICHER_BG').then(defaultBenutzerJA => {
                    if (result.username.toLowerCase() === defaultBenutzerJA.value.toLowerCase()) {
                        this.isDefaultVerantwortlicher = true;
                    }
                    this.changeDetectorRef.markForCheck();
                });
            }
            if (TSRoleUtil.getSchulamtRoles().indexOf(this.currentBerechtigung.role) > -1) {
                this.applicationPropertyRS.getByName('DEFAULT_VERANTWORTLICHER_TS').then(defaultBenutzerSCH => {
                    if (result.username.toLowerCase() === defaultBenutzerSCH.value.toLowerCase()) {
                        this.isDefaultVerantwortlicher = true;
                    }
                    this.changeDetectorRef.markForCheck();
                });
            }
            this.changeDetectorRef.markForCheck();
        });
    }

    // noinspection JSMethodCanBeStatic
    public isTraegerschaftBerechtigung(berechtigung?: TSBerechtigung): boolean {
        return berechtigung &&
            (berechtigung.role === TSRole.ADMIN_TRAEGERSCHAFT
                || berechtigung.role === TSRole.SACHBEARBEITER_TRAEGERSCHAFT);
    }

    // noinspection JSMethodCanBeStatic
    public isInstitutionBerechtigung(berechtigung?: TSBerechtigung): boolean {
        return berechtigung &&
            (berechtigung.role === TSRole.ADMIN_INSTITUTION || berechtigung.role === TSRole.SACHBEARBEITER_INSTITUTION);
    }

    // noinspection JSMethodCanBeStatic
    public isGemeindeabhaengigeBerechtigung(berechtigung?: TSBerechtigung): boolean {
        return berechtigung &&
            TSRoleUtil.isGemeindeabhaengig(berechtigung.role);
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
                })
            )
            .subscribe(
                () => this.doSaveBenutzer(),
                err => LOG.error(err)
            );
    }

    public inactivateBenutzer(): void {
        if (this.isDisabled || this.form.valid) {
            this.userRS.inactivateBenutzer(this.selectedUser).then(changedUser => {
                this.selectedUser = changedUser;
                this.changeDetectorRef.markForCheck();
            });
        }
    }

    public reactivateBenutzer(): void {
        if (this.isDisabled || this.form.valid) {
            this.userRS.reactivateBenutzer(this.selectedUser).then(changedUser => {
                this.selectedUser = changedUser;
                this.changeDetectorRef.markForCheck();
            });
        }
    }

    public canAddBerechtigung(): boolean {
        return EbeguUtil.isNullOrUndefined(this.futureBerechtigung);
    }

    public addBerechtigung() {
        const berechtigung: TSBerechtigung = new TSBerechtigung();
        berechtigung.role = TSRole.GESUCHSTELLER;
        berechtigung.gueltigkeit = new TSDateRange();
        berechtigung.gueltigkeit.gueltigAb = this.tomorrow;
        this.futureBerechtigung = berechtigung;
        BenutzerComponent.initInstitution(this.futureBerechtigung);
        BenutzerComponent.initTraegerschaft(this.futureBerechtigung);
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

    private updateInstitutionenList(): void {
        this.institutionRS.getAllInstitutionen().then(response => {
            this.institutionenList = response.sort((a, b) => a.name.localeCompare(b.name));
            this.changeDetectorRef.markForCheck();
        });
    }

    private updateTraegerschaftenList(): void {
        this.traegerschaftenRS.getAllTraegerschaften().then(response => {
            this.traegerschaftenList = response.sort((a, b) => a.name.localeCompare(b.name));
            this.changeDetectorRef.markForCheck();
        });
    }

    private initSelectedUser(): void {
        this.currentBerechtigung = this.selectedUser.berechtigungen[0];
        this.futureBerechtigung = this.selectedUser.berechtigungen[1];
        BenutzerComponent.initInstitution(this.currentBerechtigung);
        BenutzerComponent.initInstitution(this.futureBerechtigung);
        BenutzerComponent.initTraegerschaft(this.currentBerechtigung);
        BenutzerComponent.initTraegerschaft(this.futureBerechtigung);
        this.userRS.getBerechtigungHistoriesForBenutzer(this.selectedUser.username).then(result => {
            this._berechtigungHistoryList = result;
            this.changeDetectorRef.markForCheck();
        });
    }

    private prepareForSave(berechtigung: TSBerechtigung): void {
        if (berechtigung) {
            if (!this.TSRoleUtil.isGemeindeabhaengig(berechtigung.role)) {
                berechtigung.gemeindeList = [];
            }
            if (!this.isInstitutionBerechtigung(berechtigung)) {
                berechtigung.institution = undefined;
            }
            if (!this.isTraegerschaftBerechtigung(berechtigung)) {
                berechtigung.traegerschaft = undefined;
            }
        }
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
        this.prepareForSave(this.currentBerechtigung);
        this.prepareForSave(this.futureBerechtigung);

        this.selectedUser.berechtigungen = [];
        this.selectedUser.berechtigungen.push(this.currentBerechtigung);
        if (this.futureBerechtigung) {
            this.selectedUser.berechtigungen.push(this.futureBerechtigung);
        }
        this.userRS.saveBenutzerBerechtigungen(this.selectedUser).then(() => {
            this.isDisabled = true;
            this.navigateBackToUsersList();
        }).catch(err => {
            LOG.error('Could not save Benutzer', err);
            this.initSelectedUser();
        });
    }

    private navigateBackToUsersList() {
        this.$state.go('admin.benutzerlist');
    }
}
