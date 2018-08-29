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

import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import {getTSRoleValues, getTSRoleValuesWithoutSuperAdmin, TSRole} from '../../../models/enums/TSRole';
import TSBerechtigung from '../../../models/TSBerechtigung';
import TSInstitution from '../../../models/TSInstitution';
import {TSTraegerschaft} from '../../../models/TSTraegerschaft';
import EbeguUtil from '../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {InstitutionRS} from '../../core/service/institutionRS.rest';
import {TraegerschaftRS} from '../../core/service/traegerschaftRS.rest';

@Component({
    selector: 'dv-benutzer',
    templateUrl: './benutzer.component.html',
    styleUrls: ['./benutzer.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class BenutzerComponent implements OnInit {

    public institutionenList: Array<TSInstitution> = [];
    public traegerschaftenList: Array<TSTraegerschaft> = [];

    constructor(private readonly $translate: TranslateService,
                private readonly authServiceRS: AuthServiceRS,
                private readonly institutionRS: InstitutionRS,
                private readonly traegerschaftenRS: TraegerschaftRS) {
    }

    ngOnInit() {
        this.updateInstitutionenList();
        this.updateTraegerschaftenList();
    }

    public getRolesWithTranslations(): Array<{ role: TSRole; translated: string }> {
        return this.getRollen().map(role => ({role, translated: this.$translate.instant(`TSRole_${role}`)}));
    }

    public trackByRole(_i: number, role: string): string {
        return role;
    }

    public isInstitutionBerechtigung(berechtigung: TSBerechtigung): boolean {
        return berechtigung &&
            (berechtigung.role === TSRole.ADMIN_INSTITUTION || berechtigung.role === TSRole.SACHBEARBEITER_INSTITUTION);
    }

    public trackByInstitution(_i: number, institution: TSInstitution): string {
        return institution.id;
    }

    public isTraegerschaftBerechtigung(berechtigung: TSBerechtigung): boolean {
        return berechtigung &&
            (berechtigung.role === TSRole.ADMIN_TRAEGERSCHAFT
                || berechtigung.role === TSRole.SACHBEARBEITER_TRAEGERSCHAFT);
    }

    public trackByTraegerschaft(_i: number, traegerschaft: TSTraegerschaft): string {
        return traegerschaft.id;
    }

    private getRollen(): Array<TSRole> {
        if (EbeguUtil.isTagesschulangebotEnabled()) {
            return this.authServiceRS.isRole(TSRole.SUPER_ADMIN)
                ? getTSRoleValues()
                : getTSRoleValuesWithoutSuperAdmin();
        } else {
            return this.authServiceRS.isRole(TSRole.SUPER_ADMIN)
                ? TSRoleUtil.getAllRolesButSchulamt()
                : TSRoleUtil.getAllRolesButSchulamtAndSuperAdmin();
        }
    }

    private updateInstitutionenList(): void {
        this.institutionRS.getAllInstitutionen().then(response => {
            this.institutionenList = response.sort((a, b) => a.name.localeCompare(b.name));
        });
    }

    private updateTraegerschaftenList(): void {
        this.traegerschaftenRS.getAllTraegerschaften().then(response => {
            this.traegerschaftenList = response.sort((a, b) => a.name.localeCompare(b.name));
        });
    }

}
