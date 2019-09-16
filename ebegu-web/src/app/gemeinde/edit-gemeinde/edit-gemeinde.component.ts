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

import {ChangeDetectionStrategy, Component, OnInit, QueryList, ViewChildren} from '@angular/core';
import {NgForm} from '@angular/forms';
import {MatDialog, MatDialogConfig} from '@angular/material';
import {TranslateService} from '@ngx-translate/core';
import {StateService, Transition} from '@uirouter/core';
import {StateDeclaration} from '@uirouter/core/lib/state/interface';
import {from, Observable} from 'rxjs';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import GemeindeRS from '../../../gesuch/service/gemeindeRS.rest';
import {TSRole} from '../../../models/enums/TSRole';
import TSAdresse from '../../../models/TSAdresse';
import TSGemeinde from '../../../models/TSGemeinde';
import TSGemeindeStammdaten from '../../../models/TSGemeindeStammdaten';
import TSTextRessource from '../../../models/TSTextRessource';
import EbeguUtil from '../../../utils/EbeguUtil';
import {Permission} from '../../authorisation/Permission';
import {PERMISSIONS} from '../../authorisation/Permissions';
import {DvNgOkDialogComponent} from '../../core/component/dv-ng-ok-dialog/dv-ng-ok-dialog.component';
import ErrorService from '../../core/errors/service/ErrorService';
import {LogFactory} from '../../core/logging/LogFactory';

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
    private navigationSource: StateDeclaration;
    private gemeindeId: string;
    private fileToUpload: File;
    // this field will be true when the gemeinde_stammdaten don't yet exist i.e. when the gemeinde is being registered
    private isRegisteringGemeinde: boolean = false;
    public editMode: boolean = false;
    public tageschuleEnabledForMandant: boolean;
    public currentTab: number;
    public altBGAdresse: boolean;
    public altTSAdresse: boolean;

    public constructor(
        private readonly $transition$: Transition,
        private readonly $state: StateService,
        private readonly errorService: ErrorService,
        private readonly gemeindeRS: GemeindeRS,
        private readonly translate: TranslateService,
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

        this.tageschuleEnabledForMandant = this.authServiceRS.hasMandantAngebotTS();

        this.loadStammdaten();

        // initially display the first tab
        this.currentTab = 0;
    }

    private loadStammdaten(): void {
        this.stammdaten$ = from(
            this.gemeindeRS.getGemeindeStammdaten(this.gemeindeId).then(stammdaten => {
                this.initializeEmptyUnrequiredFields(stammdaten);
                if (EbeguUtil.isNullOrUndefined(stammdaten.adresse)) {
                    stammdaten.adresse = new TSAdresse();
                }
                if (stammdaten.gemeinde && stammdaten.gemeinde.betreuungsgutscheineStartdatum) {
                    this.beguStartStr = stammdaten.gemeinde.betreuungsgutscheineStartdatum.format('DD.MM.YYYY');
                }
                return stammdaten;
            }));
    }

    private initializeEmptyUnrequiredFields(stammdaten: TSGemeindeStammdaten): void {
        this.keineBeschwerdeAdresse = !stammdaten.beschwerdeAdresse;
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
        this.validateData(stammdaten).then(index => {
            if (index !== undefined) {
                this.currentTab = index;
                this.showSaveWarningDialog();
                return;
            }
            this.setViewMode();

            this.errorService.clearAll();

            this.setEmptyUnrequiredFieldsToUndefined(stammdaten);

            this.gemeindeRS.saveGemeindeStammdaten(stammdaten).then(() => {
                if (this.fileToUpload) {
                    this.persistLogo(this.fileToUpload);
                } else if (this.isRegisteringGemeinde) {
                    this.$state.go('welcome');
                    return;
                }
            });

            // Wir initisieren die Models neu, damit nach jedem Speichern weitereditiert werden kann
            // Da sonst eine Nullpointer kommt, wenn man die Checkboxen wieder anklickt!
            this.initializeEmptyUnrequiredFields(stammdaten);
        });
    }

    private setEmptyUnrequiredFieldsToUndefined(stammdaten: TSGemeindeStammdaten): void {
        if (this.keineBeschwerdeAdresse) {
            // Reset Beschwerdeadresse if not used
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
        if (stammdaten.standardRechtsmittelbelehrung) {
            // reset custom Rechtsmittelbelehrung if checkbox not checked
            stammdaten.rechtsmittelbelehrung = undefined;
        }
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

    private validateData(stammdaten: TSGemeindeStammdaten): Promise<number> {
        let errorIndex: any;

        if (!stammdaten.korrespondenzspracheDe && !stammdaten.korrespondenzspracheFr) {
            errorIndex = 0;
        }

        this.forms.forEach((form, index) => {
            // do not override the index of the first error found!
            if (!form.valid && errorIndex === undefined) {
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
    private showSaveWarningDialog(): void {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            title: this.translate.instant('GEMEINDE_TAB_SAVE_WARNING')
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
}
