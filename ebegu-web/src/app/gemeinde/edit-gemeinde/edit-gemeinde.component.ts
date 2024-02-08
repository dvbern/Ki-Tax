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

import {ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit, QueryList, ViewChildren} from '@angular/core';
import {NgForm} from '@angular/forms';
import {MatLegacyDialog as MatDialog, MatLegacyDialogConfig as MatDialogConfig} from '@angular/material/legacy-dialog';
import {TranslateService} from '@ngx-translate/core';
import {StateService, Transition} from '@uirouter/core';
import {StateDeclaration} from '@uirouter/core/lib/state/interface';
import {IPromise} from 'angular';
import {Moment} from 'moment';
import {from, Observable} from 'rxjs';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {GemeindeRS} from '../../../gesuch/service/gemeindeRS.rest';
import {TSRole} from '../../../models/enums/TSRole';
import {TSAdresse} from '../../../models/TSAdresse';
import {TSExternalClient} from '../../../models/TSExternalClient';
import {TSExternalClientAssignment} from '../../../models/TSExternalClientAssignment';
import {TSGemeinde} from '../../../models/TSGemeinde';
import {TSGemeindeStammdaten} from '../../../models/TSGemeindeStammdaten';
import {TSTextRessource} from '../../../models/TSTextRessource';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {Permission} from '../../authorisation/Permission';
import {PERMISSIONS} from '../../authorisation/Permissions';
import {DvNgConfirmDialogComponent} from '../../core/component/dv-ng-confirm-dialog/dv-ng-confirm-dialog.component';
import {DvNgOkDialogComponent} from '../../core/component/dv-ng-ok-dialog/dv-ng-ok-dialog.component';
import {ErrorService} from '../../core/errors/service/ErrorService';
import {LogFactory} from '../../core/logging/LogFactory';
import {ApplicationPropertyRS} from '../../core/rest-services/applicationPropertyRS.rest';
import {GemeindeWarningService} from '../gemeinde-warning-service/gemeinde-warning.service';

const LOG = LogFactory.createLog('EditGemeindeComponent');

@Component({
    selector: 'dv-edit-gemeinde',
    templateUrl: './edit-gemeinde.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    styleUrls: ['./edit-gemeinde.component.less'],
})
export class EditGemeindeComponent implements OnInit {
    @ViewChildren(NgForm) public forms: QueryList<NgForm>;

    public stammdaten$: Observable<TSGemeindeStammdaten>;
    public keineBeschwerdeAdresse: boolean;
    public beguStartStr: string;
    public tsAnmeldungenStartStr: string;
    public fiAnmeldungenStartStr: string;
    private navigationSource: StateDeclaration;
    public gemeindeId: string;
    private fileToUpload: File;
    private altFileToUpload: File;
    // this field will be true when the gemeinde_stammdaten don't yet exist i.e. when the gemeinde is being registered
    private isRegisteringGemeinde: boolean = false;
    public editMode: boolean = false;
    public tageschuleEnabledForMandant: boolean;
    public tfoEnabledForMandant: boolean;
    public currentTab: number;
    public altBGAdresse: boolean;
    public altTSAdresse: boolean;
    public initialBGValue: boolean;
    public initialTSValue: boolean;
    public initialFIValue: boolean;
    public beguStartDatum: Moment;
    public tsAnmeldungenStartDatum: Moment;
    public fiAnmeldungenStartDatum: Moment;
    private readonly startDatumFormat = 'DD.MM.YYYY';
    private initiallyAssignedClients: TSExternalClient[];
    public externalClients: TSExternalClientAssignment;
    public usernameScolaris: string;
    public gemeindeList$: Observable<TSGemeinde[]>;

    public constructor(
        private readonly $transition$: Transition,
        private readonly $state: StateService,
        private readonly errorService: ErrorService,
        private readonly gemeindeRS: GemeindeRS,
        private readonly translate: TranslateService,
        private readonly authServiceRS: AuthServiceRS,
        private readonly dialog: MatDialog,
        private readonly changeDetectorRef: ChangeDetectorRef,
        private readonly gemeindeWarningService: GemeindeWarningService,
        private readonly applicationPropertyRS: ApplicationPropertyRS,
    ) {
    }

    public ngOnInit(): void {
        this.gemeindeId = this.$transition$.params().gemeindeId;
        if (!this.gemeindeId) {
            return;
        }

        this.navigationSource = this.$transition$.from();
        if (this.navigationSource.name === 'einladung.abschliessen') {
            this.editMode = true;
        }

        this.isRegisteringGemeinde = this.$transition$.params().isRegistering;

        this.applicationPropertyRS.getPublicPropertiesCached().then(res => {
            this.tageschuleEnabledForMandant = res.angebotTSActivated;
            this.tfoEnabledForMandant = res.angebotTFOActivated;
        });

        this.loadGemeindenList();

        this.loadStammdaten();

        // initially display the first tab
        this.currentTab = 0;
    }

    private fetchExternalClients(gemeindeId: string): void {
        this.gemeindeRS.getExternalClients(gemeindeId)
            .then(externalClients => this.initExternalClients(externalClients));
    }

    private initExternalClients(externalClients: TSExternalClientAssignment): void {
        this.externalClients = externalClients;
        // Store a copy of the assignedClients, such that we can later determine whetere we should PUT and update
        this.initiallyAssignedClients = [...externalClients.assignedClients];
    }

    private loadStammdaten(): void {
        this.stammdaten$ = from(
            this.gemeindeRS.getGemeindeStammdaten(this.gemeindeId).then(stammdaten => {
                this.initializeEmptyUnrequiredFields(stammdaten);
                this.fetchExternalClients(this.gemeindeId);
                if (EbeguUtil.isNullOrUndefined(stammdaten.adresse)) {
                    stammdaten.adresse = new TSAdresse();
                }
                if (stammdaten.gemeinde && stammdaten.gemeinde.betreuungsgutscheineStartdatum) {
                    this.beguStartDatum = stammdaten.gemeinde.betreuungsgutscheineStartdatum;
                    this.beguStartStr = this.beguStartDatum.format(this.startDatumFormat);
                }
                if (stammdaten.gemeinde && stammdaten.gemeinde.tagesschulanmeldungenStartdatum) {
                    this.tsAnmeldungenStartDatum = stammdaten.gemeinde.tagesschulanmeldungenStartdatum;
                    this.tsAnmeldungenStartStr = this.tsAnmeldungenStartDatum.format(this.startDatumFormat);
                }
                if (stammdaten.gemeinde && stammdaten.gemeinde.ferieninselanmeldungenStartdatum) {
                    this.fiAnmeldungenStartDatum = stammdaten.gemeinde.ferieninselanmeldungenStartdatum;
                    this.fiAnmeldungenStartStr = this.fiAnmeldungenStartDatum.format(this.startDatumFormat);
                }

                this.initialFIValue = stammdaten.gemeinde.angebotFI;

                if (EbeguUtil.isNotNullOrUndefined(stammdaten.usernameScolaris)) {
                    this.usernameScolaris = stammdaten.usernameScolaris;
                }
                this.gemeindeWarningService.init(stammdaten.konfigurationsListe);

                return stammdaten;
            }));

        this.stammdaten$.subscribe(stammdaten => {
                this.initialBGValue = stammdaten.gemeinde.angebotBG;
                this.initialTSValue = stammdaten.gemeinde.angebotTS;
                this.initialFIValue = stammdaten.gemeinde.angebotFI;
            },
            err => {
                LOG.error(err);
            });
    }

    private loadGemeindenList(): void {
        this.gemeindeList$ = from(
            this.gemeindeRS.getAktiveGueltigeGemeinden()
                .then(response => response.filter(gemeinde => gemeinde.id !== this.gemeindeId)
                    .sort((a, b) => a.name.localeCompare(b.name))));

    }

    private initializeEmptyUnrequiredFields(stammdaten: TSGemeindeStammdaten): void {
        // wenn die Stammdaten noch nicht definiert sind, ist der checkbox fur beschwerde Adresse unselektiert
        this.keineBeschwerdeAdresse = (EbeguUtil.isNullOrUndefined(stammdaten.adresse)
            || EbeguUtil.isEmptyStringNullOrUndefined(stammdaten.adresse.strasse))
            ? false
            : !stammdaten.beschwerdeAdresse;
        if (EbeguUtil.isNullOrUndefined(stammdaten.beschwerdeAdresse)) {
            stammdaten.beschwerdeAdresse = new TSAdresse();
        }
        if (EbeguUtil.isNullOrUndefined(stammdaten.bgAdresse)) {
            this.altBGAdresse = false;
            stammdaten.bgAdresse = new TSAdresse();
        } else {
            this.altBGAdresse = true;
        }
        if (EbeguUtil.isNullOrUndefined(stammdaten.tsAdresse)) {
            this.altTSAdresse = false;
            stammdaten.tsAdresse = new TSAdresse();
        } else {
            this.altTSAdresse = true;
        }
        if (!stammdaten.rechtsmittelbelehrung) {
            stammdaten.rechtsmittelbelehrung = new TSTextRessource();
        }
    }

    public getHeaderTitle(gemeinde: TSGemeinde): string {
        if (!gemeinde) {
            return '';
        }
        return gemeinde.besondereVolksschule ?
            `${this.translate.instant('BESONDERE_VOLKSSCHULE')} ${gemeinde.name}` :
            `${this.translate.instant('GEMEINDE')} ${gemeinde.name}`;
    }

    public getLogoImageUrl(gemeinde: TSGemeinde): string {
        return this.gemeindeRS.getLogoUrl(gemeinde.id);
    }

    public getAlternativeLogoImageUrl(gemeinde: TSGemeinde): string {
        return this.gemeindeRS.getAlternativeLogoUrl(gemeinde.id);
    }

    public getMitarbeiterVisibleRoles(): TSRole[] {
        const allowedRoles = PERMISSIONS[Permission.ROLE_GEMEINDE].concat(TSRole.SUPER_ADMIN);
        return allowedRoles;
    }

    public cancel(): void {
        this.navigateBack();
    }

    public async persistGemeindeStammdaten(stammdaten: TSGemeindeStammdaten): Promise<void> {
        const index = await this.validateData(stammdaten);
        if (index !== undefined) {
            this.currentTab = index;
            this.showSaveWarningDialog();
            return;
        }

        if (this.gemeindeWarningService.showWarning(stammdaten.konfigurationsListe)) {
            const saveDangerous = await this.showDangerousSaveDialog();
            if (!saveDangerous) {
                return;
            }
        }

        this.errorService.clearAll();
        this.setEmptyUnrequiredFieldsToUndefined(stammdaten);
        stammdaten.iban = stammdaten.iban?.toLocaleUpperCase();
        stammdaten.externalClients = this.externalClients.assignedClients.map(client => client.id);
        stammdaten.usernameScolaris = EbeguUtil.isEmptyStringNullOrUndefined(this.usernameScolaris)
            ? undefined
            : this.usernameScolaris;
        // falls Gutschein selber ausgestellt sollen keine Gemeinde Ausgabestelle definiert werden
        if (stammdaten.gutscheinSelberAusgestellt) {
            stammdaten.gemeindeAusgabestelle = undefined;
        }

        try {
            const savedStammdaten = await this.gemeindeRS.saveGemeindeStammdaten(stammdaten);
            const hasAlternativeTagesschuleFileToUpload =
                this.altFileToUpload && stammdaten.gemeindeStammdatenKorrespondenz.hasAlternativeLogoTagesschule;
            if (this.fileToUpload) {
                await this.persistLogo(this.fileToUpload, !hasAlternativeTagesschuleFileToUpload);
            }

            if (hasAlternativeTagesschuleFileToUpload) {
                await this.persistAlternativeLogoTagesschule(this.altFileToUpload);
            } else if (this.isRegisteringGemeinde) {
                this.$state.go('welcome');
                return;
            }

            if (savedStammdaten.gemeindeStammdatenKorrespondenz.hasAlternativeLogoTagesschule
                && !stammdaten.gemeindeStammdatenKorrespondenz.hasAlternativeLogoTagesschule) {
                await this.gemeindeRS.deleteAlternativeLogoTagesschule(this.gemeindeId);
            }

            if (this.authServiceRS.isOneOfRoles(TSRoleUtil.getMandantRoles())) {
                await this.gemeindeRS.updateAngebote(stammdaten.gemeinde);
                this.updateExternalClients();
            }

            this.loadStammdaten();
            this.setViewMode();

        } catch (err) {
            this.setEditMode();
        } finally {
            this.changeDetectorRef.detectChanges();
        }

        // Wir initisieren die Models neu, damit nach jedem Speichern weitereditiert werden kann
        // Da sonst eine Nullpointer kommt, wenn man die Checkboxen wieder anklickt!
        this.initializeEmptyUnrequiredFields(stammdaten);
    }

    private updateExternalClients(): void {
        // the removed assigned clients need to be added in the available clients
        this.initiallyAssignedClients.forEach(initiallyAssignedClient => {
            if (this.externalClients.assignedClients.length === 0 || this.externalClients.assignedClients.filter(
                assignedClient => assignedClient.id === initiallyAssignedClient.id).length === 0) {
                this.externalClients.availableClients.push(initiallyAssignedClient);
            }
        });
        this.initiallyAssignedClients = this.externalClients.assignedClients;
        // the newly added assigned clients need to be removed from the available clients
        this.externalClients.assignedClients.forEach(assignedClient => {
            if (this.externalClients.availableClients.filter(
                availableClient => availableClient.id === assignedClient.id).length > 0) {
                const clientIndex = this.externalClients.availableClients.indexOf(assignedClient);
                this.externalClients.availableClients.splice(clientIndex, 1);
            }
        });
    }

    private setEmptyUnrequiredFieldsToUndefined(stammdaten: TSGemeindeStammdaten): void {
        if (this.keineBeschwerdeAdresse || !stammdaten.standardRechtsmittelbelehrung) {
            // Reset Beschwerdeadresse if not used:
            // Wenn nicht angewählt, oder wenn nicht Standard-Rechtsmittelbelehrung
            stammdaten.beschwerdeAdresse = undefined;

        }
        if (!this.altBGAdresse) {
            // Reset BGAdresse if not used
            stammdaten.bgAdresse = undefined;
        }
        if (!this.altTSAdresse) {
            // Reset BGAdresse if not used
            stammdaten.tsAdresse = undefined;
        }
        if (!this.usernameScolaris) {
            stammdaten.usernameScolaris = undefined;
        }
        if (stammdaten.standardRechtsmittelbelehrung) {
            // reset custom Rechtsmittelbelehrung if checkbox not checked
            stammdaten.rechtsmittelbelehrung = undefined;
        }
    }

    private persistLogo(file: File, navigateBack: boolean): IPromise<void> {
        return this.gemeindeRS.uploadLogoImage(this.gemeindeId, file).then(
            () => {
                if (navigateBack) {
                    this.navigateBack();
                }
            },
            () => {
                this.errorService.clearAll();
                this.errorService.addMesageAsError(this.translate.instant('GEMEINDE_LOGO_ZU_GROSS'));
            });
    }

    private persistAlternativeLogoTagesschule(file: File): IPromise<void> {
        return this.gemeindeRS.uploadAlternativeLogoTagesschule(this.gemeindeId, file).then(
            () => {
                this.navigateBack();
            },
            () => {
                this.errorService.clearAll();
                this.errorService.addMesageAsError(this.translate.instant('TAGESSCHUL_LOGO_ZU_GROSS'));
            });
    }

    public collectLogoChange(file: File): void {
        if (!file) {
            return;
        }
        this.fileToUpload = file;
    }

    public altCollectLogoChange(file: File): void {
        if (!file) {
            return;
        }
        this.altFileToUpload = file;
    }

    private validateData(stammdaten: TSGemeindeStammdaten): Promise<number> {
        let errorIndex: any;

        if (!stammdaten.korrespondenzspracheDe && !stammdaten.korrespondenzspracheFr) {
            errorIndex = 0;
        }
        const hasAngebot = stammdaten.gemeinde.isAtLeastOneAngebotSelected();
        if (!hasAngebot) {
            errorIndex = 0;
        }
        this.forms.forEach((form, index) => {
            // do not override the index of the first error found!
            if (!form.disabled && !form.valid && errorIndex === undefined) {
                errorIndex = index - 1;
            }
        });

        return Promise.resolve(errorIndex);
    }

    private navigateBack(): void {
        if (this.isRegisteringGemeinde) {
            this.$state.go('welcome');
            return;
        }

        if (!this.navigationSource.name) {
            this.$state.go('gemeinde.list');
            return;
        }

        const redirectTo = this.navigationSource.name === 'einladung.abschliessen'
            ? 'gemeinde.edit'
            : this.navigationSource;

        this.$state.go(redirectTo, {gemeindeId: this.gemeindeId});
    }

    public isRegistering(): boolean {
        return this.isRegisteringGemeinde;
    }

    public setEditMode(): void {
        this.editMode = true;
    }

    private setViewMode(): void {
        this.editMode = false;
    }

    public editModeForBG(): boolean {
        if (this.authServiceRS.isOneOfRoles([TSRole.ADMIN_BG, TSRole.ADMIN_GEMEINDE, TSRole.SUPER_ADMIN])) {
            return this.editMode;
        }
        return false;
    }

    public editModeForTSFI(): boolean {
        if (this.authServiceRS.isOneOfRoles([TSRole.ADMIN_TS, TSRole.ADMIN_GEMEINDE, TSRole.SUPER_ADMIN])) {
            return this.editMode;
        }
        return false;
    }

    /**
     * Because we only have one button to save the complete formular (i.e. all tabs) we show a dialog indicating that
     * the form contains errors. This is useful in case the user has a correct tab open but the error is in another
     * tab that she doesn't see.
     */
    private async showSaveWarningDialog(): Promise<void> {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            title: this.translate.instant('GEMEINDE_TAB_SAVE_WARNING'),
        };
        this.dialog
            .open(DvNgOkDialogComponent, dialogConfig)
            .afterClosed().subscribe(
            () => {
                EbeguUtil.selectFirstInvalid();
            },
            err => {
                LOG.error(err);
            });
    }

    private async showDangerousSaveDialog(): Promise<boolean> {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            frage: this.translate.instant('GEMEINDE_DANGEROUS_SAVING'),
        };
        return this.dialog
            .open(DvNgConfirmDialogComponent, dialogConfig)
            .afterClosed().toPromise();
    }

    public isGemeindeEditable(): boolean {
        return this.authServiceRS.isOneOfRoles(TSRoleUtil.getAdministratorBgTsGemeindeOrMandantRole());
    }

    public isSuperAdmin(): boolean {
        return this.authServiceRS.isRole(TSRole.SUPER_ADMIN);
    }
}
