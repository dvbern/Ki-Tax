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
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    OnInit,
    QueryList,
    ViewChild,
    ViewChildren,
} from '@angular/core';
import {NgForm} from '@angular/forms';
import {MatCheckboxChange} from '@angular/material/checkbox';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {TranslateService} from '@ngx-translate/core';
import {StateService, Transition} from '@uirouter/core';
import {IPromise} from 'angular';
import * as moment from 'moment';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {isJugendamt, TSBetreuungsangebotTyp} from '../../../models/enums/TSBetreuungsangebotTyp';
import {TSInstitutionStatus} from '../../../models/enums/TSInstitutionStatus';
import {TSRole} from '../../../models/enums/TSRole';
import {TSAdresse} from '../../../models/TSAdresse';
import {TSExternalClient} from '../../../models/TSExternalClient';
import {TSInstitution} from '../../../models/TSInstitution';
import {TSInstitutionExternalClient} from '../../../models/TSInstitutionExternalClient';
import {TSInstitutionExternalClientAssignment} from '../../../models/TSInstitutionExternalClientAssignment';
import {TSInstitutionStammdaten} from '../../../models/TSInstitutionStammdaten';
import {TSInstitutionUpdate} from '../../../models/TSInstitutionUpdate';
import {TSMandant} from '../../../models/TSMandant';
import {TSTraegerschaft} from '../../../models/TSTraegerschaft';
import {TSDateRange} from '../../../models/types/TSDateRange';
import {DateUtil} from '../../../utils/DateUtil';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {Permission} from '../../authorisation/Permission';
import {PERMISSIONS} from '../../authorisation/Permissions';
import {DvNgConfirmDialogComponent} from '../../core/component/dv-ng-confirm-dialog/dv-ng-confirm-dialog.component';
import {CONSTANTS} from '../../core/constants/CONSTANTS';
import {ErrorService} from '../../core/errors/service/ErrorService';
import {InstitutionRS} from '../../core/service/institutionRS.rest';
import {InstitutionStammdatenRS} from '../../core/service/institutionStammdatenRS.rest';
import {TraegerschaftRS} from '../../core/service/traegerschaftRS.rest';
import {EditInstitutionBetreuungsgutscheineComponent} from '../edit-institution-betreuungsgutscheine/edit-institution-betreuungsgutscheine.component';
import {EditInstitutionTagesschuleComponent} from '../edit-institution-tagesschule/edit-institution-tagesschule.component';

@Component({
    selector: 'dv-edit-institution',
    templateUrl: './edit-institution.component.html',
    styleUrls: ['./edit-institution.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})

export class EditInstitutionComponent implements OnInit {
    public readonly CONSTANTS: any = CONSTANTS;

    @ViewChildren(NgForm) public forms: QueryList<NgForm>;
    public readonly tomorrow: moment.Moment = DateUtil.today().add(1, 'days');

    public traegerschaftenList: TSTraegerschaft[];
    public stammdaten: TSInstitutionStammdaten;
    public externalClients?: TSInstitutionExternalClientAssignment;
    public isCheckRequired: boolean = false;
    public editMode: boolean;
    // maping from TSExternalClient.id to TSDateRange, caching so users can readd removed clients without losing date
    // range
    private assignedClientGueltigkeitCache: Map<string, TSDateRange>;

    @ViewChild(EditInstitutionBetreuungsgutscheineComponent)
    private readonly componentBetreuungsgutscheine: EditInstitutionBetreuungsgutscheineComponent;

    @ViewChild(EditInstitutionTagesschuleComponent)
    private readonly componentTagesschule: EditInstitutionTagesschuleComponent;

    private isRegisteringInstitution: boolean = false;
    private initiallyAssignedClients: TSInstitutionExternalClient[];
    public ebeguUtil = EbeguUtil;
    public allPossibleClients: TSExternalClient[];

    public constructor(
        private readonly $transition$: Transition,
        private readonly $state: StateService,
        private readonly errorService: ErrorService,
        private readonly institutionRS: InstitutionRS,
        private readonly institutionStammdatenRS: InstitutionStammdatenRS,
        private readonly authServiceRS: AuthServiceRS,
        private readonly changeDetectorRef: ChangeDetectorRef,
        private readonly translate: TranslateService,
        private readonly traegerschaftRS: TraegerschaftRS,
        private readonly dialog: MatDialog,
    ) {
    }

    private static createInstitutionStammdaten(institution: TSInstitution): TSInstitutionStammdaten {
        const stammdaten = new TSInstitutionStammdaten();
        stammdaten.adresse = new TSAdresse();
        stammdaten.institution = institution;
        stammdaten.gueltigkeit = new TSDateRange();
        stammdaten.gueltigkeit.gueltigAb = moment();

        return stammdaten;
    }

    public ngOnInit(): void {
        const institutionId = this.$transition$.params().institutionId;
        if (!institutionId) {
            return;
        }
        this.isRegisteringInstitution = this.$transition$.params().isRegistering;
        this.editMode = this.$transition$.params().editMode;

        this.traegerschaftRS.getAllActiveTraegerschaften().then(allTraegerschaften => {
            this.traegerschaftenList = allTraegerschaften;
        });

        this.fetchInstitutionAndStammdaten(institutionId);
        this.fetchExternalClients(institutionId);
    }

    private fetchExternalClients(institutionId: string): void {
        this.institutionRS.getExternalClients(institutionId)
            .then(externalClients => this.initExternalClients(externalClients));
    }

    private initExternalClients(externalClients: TSInstitutionExternalClientAssignment): void {
        this.externalClients = externalClients;
        // Store a copy of the assignedClients, such that we can later determine whetere we should PUT and update
        this.initiallyAssignedClients = EbeguUtil.copyArrayWithoutReference(this.externalClients.assignedClients);
        this.assignedClientGueltigkeitCache = new Map<string, TSDateRange>();
        for (const client of this.initiallyAssignedClients) {
            this.assignedClientGueltigkeitCache.set(client.externalClient.id, client.gueltigkeit);
        }
        this.allPossibleClients = [].concat(externalClients.assignedClients.map(eC => eC.externalClient))
            .concat(externalClients.availableClients)
            .sort((a: TSExternalClient, b: TSExternalClient) => {
                if (a.clientName.toLowerCase() < b.clientName.toLowerCase()) {
                    return -1;
                }
                if (a.clientName.toLowerCase() > b.clientName.toLowerCase()) {
                    return 1;
                }
                return 0;
            });
        this.changeDetectorRef.markForCheck();
    }

    private fetchInstitutionAndStammdaten(institutionId: string): void {
        this.institutionStammdatenRS.fetchInstitutionStammdatenByInstitution(institutionId)
            .then(optionalStammdaten => this.getOrCreateStammdaten(institutionId, optionalStammdaten))
            .then(stammdaten => this.initModel(stammdaten));
    }

    private getOrCreateStammdaten(
        institutionId: string,
        optionalStammdaten?: TSInstitutionStammdaten | null,
    ): IPromise<TSInstitutionStammdaten> {

        if (optionalStammdaten) {
            return Promise.resolve(optionalStammdaten);
        }

        return this.institutionRS.findInstitution(institutionId).then(institution => {
            return EditInstitutionComponent.createInstitutionStammdaten(institution);
        });
    }

    private initModel(stammdaten: TSInstitutionStammdaten): void {
        this.stammdaten = stammdaten;
        this.isCheckRequired = stammdaten.institution.stammdatenCheckRequired;
        this.editMode = (stammdaten.institution.status === TSInstitutionStatus.EINGELADEN || this.editMode);
        this.changeDetectorRef.markForCheck();
    }

    public getMitarbeiterVisibleRoles(): TSRole[] {
        const allowedRoles = PERMISSIONS[Permission.ROLE_INSTITUTION].concat(TSRole.SUPER_ADMIN);
        return allowedRoles;
    }

    public isStammdatenEditable(): boolean {
        return this.authServiceRS.isOneOfRoles(TSRoleUtil.getInstitutionProfilEditRoles());
    }

    public isDateStartEndDisabled(): boolean {
        if (this.isBetreuungsgutschein()) {
            return !this.isSuperAdmin();
        }
        return false;
    }

    public isSuperAdmin(): boolean {
        return this.authServiceRS.isRole(TSRole.SUPER_ADMIN);
    }

    public isRegistering(): boolean {
        return this.isRegisteringInstitution;
    }

    public getHeaderPreTitle(): string {
        let result = '';
        if (this.stammdaten.institution.traegerschaft) {
            result += this.stammdaten.institution.traegerschaft.name + ' - ';
        }
        result += this.translate.instant(this.stammdaten.betreuungsangebotTyp);
        return result;
    }

    public onSubmit(): void {
        if (this.editMode) {
            this.persistStammdaten();
            return;
        }
        this.forms.forEach(form => {
            form.resetForm(form.value);
        });
        this.editMode = true;
    }

    public submitButtonLabel(): string {
        if (this.editMode) {
            return this.translate.instant('INSTITUTION_SPEICHERN');
        }
        return this.translate.instant('INSTITUTION_EDIT');
    }

    public cancel(): void {
        if (this.editMode) {
            this.ngOnInit();
            this.editMode = false;
            return;
        }
        this.navigateBack();
    }

    private persistStammdaten(): void {
        let valid = true;
        this.forms.forEach(form => {
            if (!form.valid) {
                valid = false;
            }
        });

        if (!valid) {
            EbeguUtil.selectFirstInvalid();
            return;
        }
        this.errorService.clearAll();
        if (this.componentTagesschule
            && !this.componentTagesschule.institutionStammdatenTagesschuleValid()) {
            this.errorService.addMesageAsError(this.translate.instant('ERROR_MODULE_INVALID'));
            return;
        }

        if (this.stammdaten.telefon === '') { // Prevent phone regex error in case of empty string
            this.stammdaten.telefon = null;
        }

        // PrePersist auch auf den Child-Komponenten aufrufen
        if (this.componentBetreuungsgutscheine) {
            this.componentBetreuungsgutscheine.onPrePersist();
        }
        if (this.componentTagesschule) {
            this.componentTagesschule.onPrePersist();
        }

        const updateModel = new TSInstitutionUpdate();
        updateModel.name = this.stammdaten.institution.name;
        updateModel.traegerschaftId = this.getTraegerschaftsUpdate();
        updateModel.institutionExternalClients = this.getExternalClientsUpdate();
        updateModel.stammdaten = this.stammdaten;

        this.updateInstitution(updateModel);
    }

    private updateInstitution(updateModel: TSInstitutionUpdate): void {
        if (this.stammdaten.institutionStammdatenBetreuungsgutscheine) {
            this.stammdaten.institutionStammdatenBetreuungsgutscheine.iban =
                this.stammdaten.institutionStammdatenBetreuungsgutscheine?.iban?.toLocaleUpperCase();
        }
        if (!this.isSameInstitutionClient(this.externalClients.assignedClients,
            this.initiallyAssignedClients, false) && this.externalClients.assignedClients.length > 0) {
            let drittanwendungen = '';
            this.externalClients.assignedClients.filter(assignedClient =>
                this.initiallyAssignedClients.indexOf(assignedClient) < 0,
            ).forEach(assignedClient =>
                drittanwendungen.length > 0 ?
                    drittanwendungen += ', ' + assignedClient.externalClient.clientName
                    : drittanwendungen = assignedClient.externalClient.clientName,
            );
            // show warning popup only when added client
            if (drittanwendungen.length > 0) {
                const dialogConfig = new MatDialogConfig();
                dialogConfig.data = {
                    frage: this.translate.instant('INSTITUTION_DRITTANWENDUNG_WARNUNG', {
                        NAME_DRITTANWENDUNGEN: drittanwendungen,
                    }),
                };
                this.dialog.open(DvNgConfirmDialogComponent, dialogConfig).afterClosed()
                    .subscribe(answer => {
                            if (answer !== true) {
                                return;
                            }
                            this.institutionRS.updateInstitution(this.stammdaten.institution.id, updateModel)
                                .then(stammdaten => this.setValuesAfterSave(stammdaten));
                        },
                        () => {
                        });
            } else {
                this.institutionRS.updateInstitution(this.stammdaten.institution.id, updateModel)
                    .then(stammdaten => this.setValuesAfterSave(stammdaten));
            }
        } else {
            this.institutionRS.updateInstitution(this.stammdaten.institution.id, updateModel)
                .then(stammdaten => this.setValuesAfterSave(stammdaten));
        }
    }

    private getExternalClientsUpdate(): TSInstitutionExternalClient[] | null {
        const assignedClients = this.externalClients.assignedClients;

        if (this.isSameInstitutionClient(assignedClients, this.initiallyAssignedClients, true)) {
            // no backed update necessary
            return null;
        }

        return assignedClients;
    }

    /**
     * Compares two InstitutionExternalClient array and returns TRUE when both arrays contain the same objects and
     * values
     */
    private isSameInstitutionClient(
        a: TSInstitutionExternalClient[],
        b: TSInstitutionExternalClient[],
        checkGueltigkeit: boolean,
    ): boolean {
        if (a.length !== b.length) {
            return false;
        }

        const aSorted = a.concat().sort();
        const bSorted = b.concat().sort();
        if (checkGueltigkeit) {
            return aSorted.every((value, index) => this.isEquivalent(bSorted[index], value));
        }
        return aSorted.every((value, index) => bSorted[index].externalClient.id === value.externalClient.id);
    }

    /**
     * In order to compare two Institution External Client, we check if the Client id's are the same and if the
     * Gueltigkeit is the same
     */
    private isEquivalent(a: TSInstitutionExternalClient, b: TSInstitutionExternalClient): boolean {
        return a.externalClient.id === b.externalClient.id
            && ((EbeguUtil.isNotNullOrUndefined(a.gueltigkeit.gueltigAb) && EbeguUtil.isNotNullOrUndefined(b.gueltigkeit.gueltigAb)
                && a.gueltigkeit.gueltigAb.isSame(b.gueltigkeit.gueltigAb))
                || (EbeguUtil.isNullOrUndefined(a.gueltigkeit.gueltigAb) && EbeguUtil.isNullOrUndefined(b.gueltigkeit.gueltigAb)))
            && ((EbeguUtil.isNotNullOrUndefined(a.gueltigkeit.gueltigBis) && EbeguUtil.isNotNullOrUndefined(b.gueltigkeit.gueltigBis)
                && a.gueltigkeit.gueltigBis.isSame(b.gueltigkeit.gueltigBis))
                || (EbeguUtil.isNullOrUndefined(a.gueltigkeit.gueltigBis) && EbeguUtil.isNullOrUndefined(b.gueltigkeit.gueltigBis)));
    }

    private getTraegerschaftsUpdate(): string | null {
        const traegerschaft = this.stammdaten.institution.traegerschaft;

        return traegerschaft ? traegerschaft.id : null;
    }

    private setValuesAfterSave(stammdaten: TSInstitutionStammdaten): void {
        this.editMode = false;
        if (this.navigateToWelcomesite()) {
            return;
        }
        // if we don't navigate away we refresh all data
        this.fetchExternalClients(stammdaten.institution.id);
        this.initModel(stammdaten);
    }

    private navigateBack(): void {
        this.$state.go('institution.list');
    }

    private navigateToWelcomesite(): boolean {
        if (this.isRegisteringInstitution) {
            this.$state.go('welcome');
            return true;
        }

        return false;
    }

    public getGueltigkeitTodisplay(): string {
        const date = DateUtil.momentToLocalDateFormat(this.stammdaten.gueltigkeit.gueltigAb, CONSTANTS.DATE_FORMAT);

        return `${this.translate.instant('AB')} ${date} ${this.getBisDateIfSet()}`;
    }

    private getBisDateIfSet(): string {
        if (!this.stammdaten.gueltigkeit.gueltigBis) {
            return '';
        }

        const date = DateUtil.momentToLocalDateFormat(this.stammdaten.gueltigkeit.gueltigBis, CONSTANTS.DATE_FORMAT);
        if (date !== CONSTANTS.END_OF_TIME_STRING) {
            return `${this.translate.instant('BIS')} ${date}`;
        }

        return '';
    }

    public compareTraegerschaft(b1: TSTraegerschaft, b2: TSTraegerschaft): boolean {
        return b1 && b2 ? b1.id === b2.id : b1 === b2;
    }

    public getPlaceholderForOeffnungszeiten(): string {
        return this.translate.instant('INSTITUTION_OEFFNUNGSZEITEN_PLACEHOLDER');
    }

    public deactivateStammdatenCheckRequired(): void {
        this.institutionRS.deactivateStammdatenCheckRequired(this.stammdaten.institution.id)
            .then(() => this.navigateBack());
    }

    public isCheckRequiredEnabled(): boolean {
        return this.isCheckRequired && !this.editMode && this.isStammdatenEditable();
    }

    public isBetreuungsgutschein(): boolean {
        return isJugendamt(this.stammdaten.betreuungsangebotTyp);
    }

    public isTagesschule(): boolean {
        return this.stammdaten.betreuungsangebotTyp === TSBetreuungsangebotTyp.TAGESSCHULE;
    }

    public isFerieninsel(): boolean {
        return this.stammdaten.betreuungsangebotTyp === TSBetreuungsangebotTyp.FERIENINSEL;
    }

    public traegerschaftId(traegerschaft: TSTraegerschaft): string {
        return traegerschaft.id;
    }

    public getMinStartDate(): Date {
        if (this.isFerieninsel() || this.isTagesschule()) {
            return TSMandant.earliestDateOfTSAnmeldung.toDate();
        }
        return new Date(0);
    }

    public getGueltigAbDate(date: moment.Moment): string {
        if (!date || !date.isValid()) {
            return '';
        }
        const formatedDate = DateUtil.momentToLocalDateFormat(date, CONSTANTS.DATE_FORMAT);
        return `${this.translate.instant('AB')} ${formatedDate}`;
    }

    public getGueltigBisDate(date: moment.Moment): string {
        if (!date || !date.isValid()) {
            return '';
        }
        const formatedDate = DateUtil.momentToLocalDateFormat(date, CONSTANTS.DATE_FORMAT);
        if (formatedDate !== CONSTANTS.END_OF_TIME_STRING) {
            return `${this.translate.instant('BIS')} ${formatedDate}`;
        }
        return '';
    }

    public dateAbChange($event: moment.Moment | null, assignedClient: TSInstitutionExternalClient): void {
        assignedClient.gueltigkeit.gueltigAb = $event ? $event.startOf('month') : null;
        this.updateClientGueltigkeitCache(assignedClient);
    }

    public dateBisChange($event: moment.Moment | null, assignedClient: TSInstitutionExternalClient): void {
        assignedClient.gueltigkeit.gueltigBis = $event ? $event.endOf('month') : null;
        this.updateClientGueltigkeitCache(assignedClient);
    }

    private updateClientGueltigkeitCache(client: TSInstitutionExternalClient): void {
        if (this.assignedClientGueltigkeitCache.has(client.externalClient.id)) {
            this.assignedClientGueltigkeitCache.set(client.externalClient.id, client.gueltigkeit);
        }
    }

    public changeAssignmentClient(changeEvent: MatCheckboxChange, client: TSExternalClient): void {
        if (changeEvent.checked) {
            if (!this.isClientAssigned(client)) {
                const newInstitutionClient = new TSInstitutionExternalClient(client);
                // check cache for existing gueltigkeit
                if (this.assignedClientGueltigkeitCache.has(client.id)) {
                    newInstitutionClient.gueltigkeit = this.assignedClientGueltigkeitCache.get(client.id);
                } else {
                    this.assignedClientGueltigkeitCache.set(client.id, newInstitutionClient.gueltigkeit);
                }
                this.externalClients.assignedClients.push(newInstitutionClient);
            }
        } else {
            const idx = this.externalClients.assignedClients.findIndex(c => c.externalClient.id === client.id);
            if (idx >= 0) {
                this.externalClients.assignedClients.splice(idx, 1);
            }
        }
    }

    public isClientAssigned(client: TSExternalClient): boolean {
        return !!this.externalClients.assignedClients.find(c => c.externalClient.id === client.id);
    }

    public getAssignedInstitutionClient(client: TSExternalClient): TSInstitutionExternalClient {
        return this.externalClients.assignedClients
            .find(assignedClient => assignedClient.externalClient.id === client.id);
    }

    public getSortedAssignedClients(): TSInstitutionExternalClient[] {
        return this.externalClients.assignedClients.sort((
            a: TSInstitutionExternalClient,
            b: TSInstitutionExternalClient,
        ) => {
            if (a.externalClient.clientName < b.externalClient.clientName) {
                return -1;
            }
            if (a.externalClient.clientName > b.externalClient.clientName) {
                return 1;
            }
            return 0;
        });
    }
}
