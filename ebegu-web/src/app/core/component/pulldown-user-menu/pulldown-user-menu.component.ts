/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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
    Component,
    OnInit,
    ViewEncapsulation
} from '@angular/core';
import {StateService} from '@uirouter/core';
import * as Sentry from '@sentry/browser';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {TSRole} from '../../../../models/enums/TSRole';
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';
import {ApplicationPropertyRS} from '../../rest-services/applicationPropertyRS.rest';

@Component({
    selector: 'dv-pulldown-user-menu',
    templateUrl: './pulldown-user-menu.component.html',
    styleUrls: ['./pulldown-user-menu.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    encapsulation: ViewEncapsulation.None
})
export class PulldownUserMenuComponent implements OnInit {
    // Replace with Observable once ApplicationPropertyRS is migrated in KIBON-2963
    public multimandantAktiv: boolean = false;
    public frenchEnabled: boolean = false;
    public testfaelleEnabled: boolean = false;

    public constructor(
        private readonly authService: AuthServiceRS,
        private readonly applicationPropertyRS: ApplicationPropertyRS,
        private readonly state: StateService
    ) {}

    public ngOnInit(): void {
        this.initMandantSwitch();
        this.initFrenchEnabled();
        this.initTestFaelleEnabled().catch(e => Sentry.captureException(e));
    }

    public getFullName(): Observable<string> {
        return this.authService.principal$.pipe(
            map(principal => principal.getFullName())
        );
    }

    public getSuperAdminRoles(): ReadonlyArray<TSRole> {
        return TSRoleUtil.getSuperAdminRoles();
    }

    public getAllAdministratorRevisorRole(): ReadonlyArray<TSRole> {
        return TSRoleUtil.getAllAdministratorRevisorRole();
    }

    public getInstitutionProfilRoles(): ReadonlyArray<TSRole> {
        return TSRoleUtil.getInstitutionProfilRoles();
    }

    public getMandantRoles(): ReadonlyArray<TSRole> {
        return TSRoleUtil.getMandantRoles();
    }

    public getTraegerschaftId(): Observable<string> {
        return this.authService.principal$.pipe(
            map(principal => principal.currentBerechtigung?.traegerschaft?.id)
        );
    }

    public getAdministratorMandantRevisorRole(): ReadonlyArray<TSRole> {
        return TSRoleUtil.getAdministratorMandantRevisorRole();
    }

    public getAllRolesForSozialdienst(): ReadonlyArray<TSRole> {
        return TSRoleUtil.getAllRolesForSozialdienst();
    }

    private initMandantSwitch(): void {
        this.applicationPropertyRS
            .getPublicPropertiesCached()
            .then(publicAppConfig => publicAppConfig.mulitmandantAktiv)
            .then(active => {
                this.multimandantAktiv = active;
            });
    }

    private initFrenchEnabled(): void {
        this.applicationPropertyRS
            .getPublicPropertiesCached()
            .then(publicAppConfig => publicAppConfig.frenchEnabled)
            .then(enabled => {
                this.frenchEnabled = enabled;
            });
    }

    private async initTestFaelleEnabled(): Promise<void> {
        this.testfaelleEnabled =
            await this.applicationPropertyRS.isTestfaelleEnabled();
    }

    public logout(): void {
        this.state.go('authentication.login', {type: 'logout'});
    }
}
