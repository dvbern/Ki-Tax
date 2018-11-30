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
import {TranslateService} from '@ngx-translate/core';
import {StateService, Transition} from '@uirouter/core';
import {StateDeclaration} from '@uirouter/core/lib/state/interface';
import {from, Observable} from 'rxjs';
import GemeindeRS from '../../../gesuch/service/gemeindeRS.rest';
import {TSRole} from '../../../models/enums/TSRole';
import TSAdresse from '../../../models/TSAdresse';
import TSBenutzer from '../../../models/TSBenutzer';
import TSGemeinde from '../../../models/TSGemeinde';
import TSGemeindeStammdaten from '../../../models/TSGemeindeStammdaten';
import {Permission} from '../../authorisation/Permission';
import {PERMISSIONS} from '../../authorisation/Permissions';
import ErrorService from '../../core/errors/service/ErrorService';

@Component({
    selector: 'dv-edit-gemeinde',
    templateUrl: './edit-gemeinde.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EditGemeindeComponent implements OnInit {
    @ViewChild(NgForm) public form: NgForm;

    public stammdaten$: Observable<TSGemeindeStammdaten>;
    public keineBeschwerdeAdresse: boolean;
    private navigationSource: StateDeclaration;
    private gemeindeId: string;
    private fileToUpload: File;

    public constructor(
        private readonly $transition$: Transition,
        private readonly $state: StateService,
        private readonly errorService: ErrorService,
        private readonly gemeindeRS: GemeindeRS,
        private readonly translate: TranslateService,
    ) {
    }

    public ngOnInit(): void {
        this.navigationSource = this.$transition$.from();
        this.gemeindeId = this.$transition$.params().gemeindeId;
        if (!this.gemeindeId) {
            return;
        }
        this.stammdaten$ = from(
            this.gemeindeRS.getGemeindeStammdaten(this.gemeindeId).then(stammdaten => {
                this.keineBeschwerdeAdresse = !stammdaten.beschwerdeAdresse;
                if (stammdaten.adresse === undefined) {
                    stammdaten.adresse = new TSAdresse();
                }
                if (stammdaten.beschwerdeAdresse === undefined) {
                    stammdaten.beschwerdeAdresse = new TSAdresse();
                }
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
        const allowedRoles = PERMISSIONS[Permission.ROLE_GEMEINDE];
        allowedRoles.push(TSRole.SUPER_ADMIN);
        return allowedRoles;
    }

    public cancel(): void {
        this.navigateBack();
    }

    public persistGemeindeStammdaten(stammdaten: TSGemeindeStammdaten): void {
        if (!this.validateData(stammdaten)) {
            return;
        }
        this.errorService.clearAll();
        if (this.keineBeschwerdeAdresse) {
            // Reset Beschwerdeadresse if not used
            stammdaten.beschwerdeAdresse = undefined;
        }
        this.gemeindeRS.saveGemeindeStammdaten(stammdaten).then(() => {
            if (this.fileToUpload) {
                this.persistLogo(this.fileToUpload, true);
            } else {
                this.navigateBack();
            }
        });
    }

    private persistLogo(file: File, navigateBack: boolean): void {
        this.gemeindeRS.uploadLogoImage(this.gemeindeId, file).then(() => {
            if (navigateBack) {
                this.navigateBack();
            }
        }, () => {
            this.errorService.clearAll();
            this.errorService.addMesageAsError(this.translate.instant('GEMEINDE_LOGO_ZU_GROSS'));
        });
    }

    public collectLogoChange(file: File, stammdaten: TSGemeindeStammdaten): void {
        if (!file) {
            return;
        }
        if (!stammdaten || stammdaten.isNew()) {
            // upload later if the stammdaten are new, because if the object doesn't exist yet we will get an error
            this.fileToUpload = file;
            return;
        }
        this.persistLogo(file, false);
        this.fileToUpload = undefined;
    }

    private validateData(stammdaten: TSGemeindeStammdaten): boolean {
        return (stammdaten.korrespondenzspracheDe || stammdaten.korrespondenzspracheFr)
            && this.form.valid;
    }

    public compareBenutzer(b1: TSBenutzer, b2: TSBenutzer): boolean {
        return b1 && b2 ? b1.username === b2.username : b1 === b2;
    }

    private navigateBack(): void {
        if (!this.navigationSource.name) {
            this.$state.go('gemeinde.list');
            return;
        }

        const redirectTo = this.navigationSource.name === 'einladung.abschliessen'
            ? 'gemeinde.view'
            : this.navigationSource;

        this.$state.go(redirectTo, {gemeindeId: this.gemeindeId});
    }
}
