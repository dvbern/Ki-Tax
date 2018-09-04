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
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import {rolePrefix, TSRole} from '../../../models/enums/TSRole';
import TSBerechtigung from '../../../models/TSBerechtigung';
import TSBerechtigungHistory from '../../../models/TSBerechtigungHistory';
import TSInstitution from '../../../models/TSInstitution';
import {TSTraegerschaft} from '../../../models/TSTraegerschaft';
import TSUser from '../../../models/TSUser';
import {TSDateRange} from '../../../models/types/TSDateRange';
import DateUtil from '../../../utils/DateUtil';
import EbeguUtil from '../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {DvNgRemoveDialogComponent} from '../../core/component/dv-ng-remove-dialog/dv-ng-remove-dialog.component';
import {Log, LogFactory} from '../../core/logging/LogFactory';
import {ApplicationPropertyRS} from '../../core/rest-services/applicationPropertyRS.rest';
import {InstitutionRS} from '../../core/service/institutionRS.rest';
import {TraegerschaftRS} from '../../core/service/traegerschaftRS.rest';
import UserRS from '../../core/service/userRS.rest';

@Component({
    selector: 'dv-benutzer',
    templateUrl: './benutzer.component.html',
    styleUrls: ['./benutzer.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class BenutzerComponent implements OnInit {

    private readonly log: Log = LogFactory.createLog('BenutzerComponent');

    public institutionenList: Array<TSInstitution> = [];
    public traegerschaftenList: Array<TSTraegerschaft> = [];

    @ViewChild(NgForm) form: NgForm;
    TSRoleUtil = TSRoleUtil;
    tomorrow: moment.Moment = DateUtil.today().add(1, 'days');
    selectedUser: TSUser;

    private _currentBerechtigung: TSBerechtigung;
    private _futureBerechtigung: TSBerechtigung;
    private _berechtigungHistoryList: TSBerechtigungHistory[];
    private _isDefaultVerantwortlicher: boolean = false;

    constructor(private readonly $transition$: Transition,
                private readonly changeDetectorRef: ChangeDetectorRef,
                private readonly $state: StateService,
                public readonly translate: TranslateService,
                private readonly authServiceRS: AuthServiceRS,
                private readonly institutionRS: InstitutionRS,
                private readonly traegerschaftenRS: TraegerschaftRS,
                private readonly userRS: UserRS,
                private readonly applicationPropertyRS: ApplicationPropertyRS,
                private readonly dialog: MatDialog) {
    }

    /**
     * Anonymous doesn't give any useful information to the user. For this reason we show system instead of anonymous
     */
    public getGeaendertDurch(role: TSBerechtigungHistory): string {
        if (role.userErstellt === 'anonymous') {
            return 'system';
        }
        return role.userErstellt;
    }

    ngOnInit() {
        this.updateInstitutionenList();
        this.updateTraegerschaftenList();
        const username: string = this.$transition$.params().benutzerId;
        if (username) {
            this.userRS.findBenutzer(username).then((result) => {
                this.selectedUser = result;
                this.initSelectedUser();
                // Falls der Benutzer JA oder SCH Benutzer ist, muss geprüft werden, ob es sich um den
                // "Default-Verantwortlichen" des entsprechenden Amtes handelt
                if (TSRoleUtil.getAdministratorJugendamtRole().indexOf(this.currentBerechtigung.role) > -1) {
                    this.applicationPropertyRS.getByName('DEFAULT_VERANTWORTLICHER_BG').then(defaultBenutzerJA => {
                        if (result.username.toLowerCase() === defaultBenutzerJA.value.toLowerCase()) {
                            this._isDefaultVerantwortlicher = true;
                        }
                        this.changeDetectorRef.markForCheck();
                    });
                }
                if (TSRoleUtil.getSchulamtRoles().indexOf(this.currentBerechtigung.role) > -1) {
                    this.applicationPropertyRS.getByName('DEFAULT_VERANTWORTLICHER_TS').then(defaultBenutzerSCH => {
                        if (result.username.toLowerCase() === defaultBenutzerSCH.value.toLowerCase()) {
                            this._isDefaultVerantwortlicher = true;
                        }
                        this.changeDetectorRef.markForCheck();
                    });
                }
                this.changeDetectorRef.markForCheck();
            });
        }
    }

    public isTraegerschaftBerechtigung(berechtigung: TSBerechtigung): boolean {
        return berechtigung &&
            (berechtigung.role === TSRole.ADMIN_TRAEGERSCHAFT
                || berechtigung.role === TSRole.SACHBEARBEITER_TRAEGERSCHAFT);
    }

    public isInstitutionBerechtigung(berechtigung: TSBerechtigung): boolean {
        console.log('berechtigung', berechtigung);
        return berechtigung &&
            (berechtigung.role === TSRole.ADMIN_INSTITUTION || berechtigung.role === TSRole.SACHBEARBEITER_INSTITUTION);
    }

    public trackByRole(_i: number, role: string): string {
        return role;
    }

    public getRolesWithTranslations(): Array<{ role: TSRole; translated: string }> {
        return this.getRollen().map(role => ({role, translated: this.translate.instant(`TSRole_${role}`)}));
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
        this._currentBerechtigung = this.selectedUser.berechtigungen[0];
        this._futureBerechtigung = this.selectedUser.berechtigungen[1];
        this.initInstitution(this._currentBerechtigung);
        this.initInstitution(this._futureBerechtigung);
        this.initTraegerschaft(this._currentBerechtigung);
        this.initTraegerschaft(this._futureBerechtigung);
        this.userRS.getBerechtigungHistoriesForBenutzer(this.selectedUser.username).then((result) => {
            this._berechtigungHistoryList = result;
            this.changeDetectorRef.markForCheck();
        });
    }

    private initInstitution(berechtigung: TSBerechtigung): void {
        if (berechtigung && !berechtigung.institution) {
            berechtigung.institution = new TSInstitution();
        }
    }

    private initTraegerschaft(berechtigung: TSBerechtigung): void {
        if (berechtigung && !berechtigung.traegerschaft) {
            berechtigung.traegerschaft = new TSTraegerschaft();
        }
    }

    public getRollen(): Array<TSRole> {
        if (EbeguUtil.isTagesschulangebotEnabled()) {
            return this.authServiceRS.isRole(TSRole.SUPER_ADMIN)
                ? TSRoleUtil.getAllRolesButAnonymous()
                : TSRoleUtil.getAllRolesButSuperAdminAndAnonymous();
        } else {
            return this.authServiceRS.isRole(TSRole.SUPER_ADMIN)
                ? TSRoleUtil.getAllRolesButSchulamtAndAnonymous()
                : TSRoleUtil.getAllRolesButSchulamtAndSuperAdminAndAnonymous();
        }
    }

    public getTranslatedRole(role: TSRole): string {
        if (role === TSRole.GESUCHSTELLER) {
            return this.translate.instant(rolePrefix() + 'NONE');
        }
        return this.translate.instant(rolePrefix() + role);
    }

    saveBenutzerBerechtigungen(): void {
        if (this.form.valid) {
            this.prepareForSave(this.currentBerechtigung);
            this.prepareForSave(this.futureBerechtigung);
            if (this.isMoreThanGesuchstellerRole()) {
                const dialogConfig = new MatDialogConfig();
                dialogConfig.data = {
                    title: 'BENUTZER_ROLLENZUWEISUNG_CONFIRMATION_TITLE',
                    text: 'BENUTZER_ROLLENZUWEISUNG_CONFIRMATION_TEXT',
                };

                this.dialog.open(DvNgRemoveDialogComponent, dialogConfig)
                    .afterClosed()
                    .subscribe(userAccepted => {
                        if (userAccepted) {
                            if (this.isAdminRole()) {
                                const adminDialogConfig = new MatDialogConfig();
                                adminDialogConfig.data = {
                                    title: 'BENUTZER_ROLLENZUWEISUNG_CONFIRMATION_ADMIN_TITLE',
                                    text: 'BENUTZER_ROLLENZUWEISUNG_CONFIRMATION_ADMIN_TEXT',
                                };

                                this.dialog.open(DvNgRemoveDialogComponent, adminDialogConfig)
                                    .afterClosed()
                                    .subscribe(userAccepted => {
                                        if (userAccepted) {
                                            this.doSaveBenutzer();
                                        }
                                    });
                            } else {
                                this.doSaveBenutzer();
                            }
                        }
                    });
            } else {
                this.doSaveBenutzer();
            }
        }
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

    private isAdminRole() {
        return this.isAtLeastOneRoleInList(TSRoleUtil.getAdministratorRoles());
    }

    private isMoreThanGesuchstellerRole() {
        return this.isAtLeastOneRoleInList(TSRoleUtil.getAllRolesButGesuchsteller());
    }

    private isAtLeastOneRoleInList(rolesToCheck: Array<TSRole>): boolean {
        // Es muessen alle vorhandenen Rollen geprueft werden
        if (rolesToCheck.indexOf(this.currentBerechtigung.role) > -1) {
            return true;
        }
        if (this.futureBerechtigung && rolesToCheck.indexOf(this.futureBerechtigung.role) > -1) {
            return true;
        }
        return false;
    }

    private doSaveBenutzer(): void {
        this.selectedUser.berechtigungen = [];
        this.selectedUser.berechtigungen.push(this._currentBerechtigung);
        if (this._futureBerechtigung) {
            this.selectedUser.berechtigungen.push(this._futureBerechtigung);
        }
        this.userRS.saveBenutzerBerechtigungen(this.selectedUser).then(() => {
            this.navigateBackToUsersList();
        }).catch((err) => {
            this.log.error('Could not save Benutzer', err);
            this.initSelectedUser();
        });
    }

    inactivateBenutzer(): void {
        if (this.form.valid) {
            this.userRS.inactivateBenutzer(this.selectedUser).then(changedUser => {
                this.selectedUser = changedUser;
                this.changeDetectorRef.markForCheck();
            });
        }
    }

    reactivateBenutzer(): void {
        if (this.form.valid) {
            this.userRS.reactivateBenutzer(this.selectedUser).then(changedUser => {
                this.selectedUser = changedUser;
                this.changeDetectorRef.markForCheck();
            });
        }
    }

    canAddBerechtigung(): boolean {
        return EbeguUtil.isNullOrUndefined(this.futureBerechtigung);
    }

    addBerechtigung() {
        const berechtigung: TSBerechtigung = new TSBerechtigung();
        berechtigung.role = TSRole.GESUCHSTELLER;
        berechtigung.gueltigkeit = new TSDateRange();
        berechtigung.gueltigkeit.gueltigAb = this.tomorrow;
        berechtigung.enabled = true;
        this._futureBerechtigung = berechtigung;
        this.initInstitution(this._futureBerechtigung);
        this.initTraegerschaft(this._futureBerechtigung);
    }

    enableBerechtigung(berechtigung: TSBerechtigung): void {
        berechtigung.enabled = true;
    }

    removeBerechtigung(berechtigung: TSBerechtigung): void {
        this._futureBerechtigung = undefined;
    }

    cancel(): void {
        this.navigateBackToUsersList();
    }

    private navigateBackToUsersList() {
        this.$state.go('admin.benutzerlist');
    }

    public isBerechtigungEnabled(berechtigung: TSBerechtigung): boolean {
        return berechtigung && berechtigung.enabled;
    }

    public get currentBerechtigung(): TSBerechtigung {
        return this._currentBerechtigung;
    }

    public get futureBerechtigung(): TSBerechtigung {
        return this._futureBerechtigung;
    }

    public get isDefaultVerantwortlicher(): boolean {
        return this._isDefaultVerantwortlicher;
    }

    public set isDefaultVerantwortlicher(value: boolean) {
        this._isDefaultVerantwortlicher = value;
    }

    public get berechtigungHistoryList(): TSBerechtigungHistory[] {
        return this._berechtigungHistoryList;
    }
}
