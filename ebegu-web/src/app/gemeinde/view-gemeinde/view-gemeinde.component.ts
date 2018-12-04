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
import {from, Observable} from 'rxjs';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import GemeindeRS from '../../../gesuch/service/gemeindeRS.rest';
import {TSRole} from '../../../models/enums/TSRole';
import TSGemeinde from '../../../models/TSGemeinde';
import TSGemeindeStammdaten from '../../../models/TSGemeindeStammdaten';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {Permission} from '../../authorisation/Permission';
import {PERMISSIONS} from '../../authorisation/Permissions';

@Component({
    selector: 'dv-view-gemeinde',
    templateUrl: './view-gemeinde.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ViewGemeindeComponent implements OnInit {
    @ViewChild(NgForm) public form: NgForm;

    public stammdaten$: Observable<TSGemeindeStammdaten>;
    public keineBeschwerdeAdresse: boolean;
    public korrespondenzsprache: string;
    private gemeindeId: string;

    public constructor(
        private readonly $transition$: Transition,
        private readonly $state: StateService,
        private readonly translate: TranslateService,
        private readonly gemeindeRS: GemeindeRS,
        private readonly authServiceRS: AuthServiceRS,
    ) {
    }

    public ngOnInit(): void {
        this.gemeindeId = this.$transition$.params().gemeindeId;
        if (!this.gemeindeId) {
            return;
        }
        this.stammdaten$ = from(
            this.gemeindeRS.getGemeindeStammdaten(this.gemeindeId).then(stammdaten => {
                this.initStrings(stammdaten);
                this.keineBeschwerdeAdresse = !stammdaten.beschwerdeAdresse;
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

    public editGemeindeStammdaten(): void {
        this.$state.go('gemeinde.edit', {gemeindeId: this.gemeindeId});
    }

    public isStammdatenEditable(): boolean {
        return this.authServiceRS.isOneOfRoles(TSRoleUtil.getAdministratorBgTsGemeindeRole());
    }

    private initStrings(stammdaten: TSGemeindeStammdaten): void {
        const languages: string[] = [];
        if (stammdaten.korrespondenzspracheDe) {
            languages.push(this.translate.instant('DEUTSCH'));
        }
        if (stammdaten.korrespondenzspracheFr) {
            languages.push(this.translate.instant('FRANZOESISCH'));
        }
        this.korrespondenzsprache = languages.join(', ');
    }

    public cancel(): void {
        this.navigateBack();
    }

    private navigateBack(): void {
        this.$state.go('gemeinde.list');
    }
}
