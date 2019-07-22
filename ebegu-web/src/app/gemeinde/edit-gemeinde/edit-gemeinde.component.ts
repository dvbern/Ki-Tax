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

import {ChangeDetectionStrategy, Component, OnInit, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {MatDialog, MatDialogConfig} from '@angular/material';
import {TranslateService} from '@ngx-translate/core';
import {StateService, Transition} from '@uirouter/core';
import {StateDeclaration} from '@uirouter/core/lib/state/interface';
import {from, Observable} from 'rxjs';
import {EinstellungRS} from '../../../admin/service/einstellungRS.rest';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import GemeindeRS from '../../../gesuch/service/gemeindeRS.rest';
import {TSRole} from '../../../models/enums/TSRole';
import TSAdresse from '../../../models/TSAdresse';
import TSGemeinde from '../../../models/TSGemeinde';
import TSGemeindeStammdaten from '../../../models/TSGemeindeStammdaten';
import TSTextRessource from '../../../models/TSTextRessource';
import {Permission} from '../../authorisation/Permission';
import {PERMISSIONS} from '../../authorisation/Permissions';
import {DvNgOkDialogComponent} from '../../core/component/dv-ng-ok-dialog/dv-ng-ok-dialog.component';
import ErrorService from '../../core/errors/service/ErrorService';

@Component({
    selector: 'dv-edit-gemeinde',
    templateUrl: './edit-gemeinde.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    styleUrls: ['./edit-gemeinde.component.less'],
})
export class EditGemeindeComponent implements OnInit {
    @ViewChild(NgForm) public form: NgForm;

    public stammdaten$: Observable<TSGemeindeStammdaten>;
    public keineBeschwerdeAdresse: boolean;
    public beguStartStr: string;
    private navigationSource: StateDeclaration;
    private gemeindeId: string;
    private fileToUpload: File;
    // this field will be true when the gemeinde_stammdaten don't yet exist i.e. when the gemeinde is being registered
    private isRegisteringGemeinde: boolean = false;
    private hasTS$: Observable<boolean>;
    private hasFI$: Observable<boolean>;
    private hasBG$: Observable<boolean>;
    public editMode: boolean = false;

    public constructor(
        private readonly $transition$: Transition,
        private readonly $state: StateService,
        private readonly errorService: ErrorService,
        private readonly gemeindeRS: GemeindeRS,
        private readonly translate: TranslateService,
        private readonly einstellungRS: EinstellungRS,
        private readonly authServiceRS: AuthServiceRS,
        private readonly dialog: MatDialog,
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

        this.stammdaten$ = from(
            this.gemeindeRS.getGemeindeStammdaten(this.gemeindeId).then(stammdaten => {
                this.keineBeschwerdeAdresse = !stammdaten.beschwerdeAdresse;
                if (stammdaten.adresse === undefined) {
                    stammdaten.adresse = new TSAdresse();
                }
                if (stammdaten.beschwerdeAdresse === undefined) {
                    stammdaten.beschwerdeAdresse = new TSAdresse();
                }
                if (!stammdaten.rechtsmittelbelehrung) {
                    stammdaten.rechtsmittelbelehrung = new TSTextRessource();
                }
                if (stammdaten.gemeinde && stammdaten.gemeinde.betreuungsgutscheineStartdatum) {
                    this.beguStartStr = stammdaten.gemeinde.betreuungsgutscheineStartdatum.format('DD.MM.YYYY');
                }

                this.getEinstellungenFromGemeinde();

                return stammdaten;
            }));
    }

    public getHeaderTitle(gemeinde: TSGemeinde): string {
        if (!gemeinde) {
            return '';
        }
        return `${this.translate.instant('GEMEINDE')} ${gemeinde.name}`;
    }

    public getLogoImageUrl(gemeinde: TSGemeinde): string {
        return this.gemeindeRS.getLogoUrl(gemeinde.id);
    }

    public getMitarbeiterVisibleRoles(): TSRole[] {
        const allowedRoles = PERMISSIONS[Permission.ROLE_GEMEINDE].concat(TSRole.SUPER_ADMIN);
        return allowedRoles;
    }

    public cancel(): void {
        this.navigateBack();
    }

    public persistGemeindeStammdaten(stammdaten: TSGemeindeStammdaten): void {
        if (!this.validateData(stammdaten)) {
            this.showSaveWarningDialog();
            return;
        }
        this.setViewMode();

        this.errorService.clearAll();
        if (this.keineBeschwerdeAdresse) {
            // Reset Beschwerdeadresse if not used
            stammdaten.beschwerdeAdresse = undefined;
        }
        if (stammdaten.standardRechtsmittelbelehrung) {
            // reset custom Rechtsmittelbelehrung if checkbox not checked
            stammdaten.rechtsmittelbelehrung = undefined;
        }

        this.gemeindeRS.saveGemeindeStammdaten(stammdaten).then(() => {
            if (this.fileToUpload) {
                this.persistLogo(this.fileToUpload);
            } else if (this.isRegisteringGemeinde) {
                this.$state.go('welcome');
                return;
            }
        });
    }

    private persistLogo(file: File): void {
        this.gemeindeRS.uploadLogoImage(this.gemeindeId, file).then(
            () => {
                this.navigateBack();
            },
            () => {
                this.errorService.clearAll();
                this.errorService.addMesageAsError(this.translate.instant('GEMEINDE_LOGO_ZU_GROSS'));
        });
    }

    public collectLogoChange(file: File): void {
        if (!file) {
            return;
        }
        this.fileToUpload = file;
    }

    private validateData(stammdaten: TSGemeindeStammdaten): boolean {
        return (stammdaten.korrespondenzspracheDe || stammdaten.korrespondenzspracheFr)
            && this.form.valid;
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

    private getEinstellungenFromGemeinde(): void {
        this.hasTS$ = from(this.einstellungRS.hasTagesschuleInAnyGesuchsperiode(
            this.gemeindeId,
        ).then(response => response));

        this.hasFI$ = from(this.einstellungRS.hasFerieninselInAnyGesuchsperiode(
            this.gemeindeId,
        ).then(response => response));

        this.hasBG$ = from(this.einstellungRS.hasBetreuungsgutscheineInAnyGesuchsperiode(
            this.gemeindeId,
        ).then(response => response));
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
    private showSaveWarningDialog(): void {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            title: this.translate.instant('GEMEINDE_TAB_SAVE_WARNING')
        };

        this.dialog.open(DvNgOkDialogComponent, dialogConfig).afterClosed();
    }
}
