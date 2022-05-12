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

import {ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {StateService} from '@uirouter/core';
import {from, Observable} from 'rxjs';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {ITourParams} from '../../../gesuch/gesuch.route';
import {navigateToStartPageForRole, navigateToStartPageForRoleWithParams} from '../../../utils/AuthenticationUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {ApplicationPropertyRS} from '../../core/rest-services/applicationPropertyRS.rest';
import {KiBonGuidedTourService} from '../../kibonTour/service/KiBonGuidedTourService';

@Component({
    selector: 'dv-welcome-main',
    templateUrl: './welcome-main.component.html',
    styleUrls: ['./welcome-main.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WelcomeMainComponent implements OnInit {

    private logoUrl: string;

    public constructor(
        private readonly authServiceRs: AuthServiceRS,
        private readonly $state: StateService,
        private readonly kibonGuidedTourService: KiBonGuidedTourService,
        private readonly applicationPropertyRS: ApplicationPropertyRS,
        private readonly cd: ChangeDetectorRef
    ) {

    }

    public ngOnInit(): void {
        this.applicationPropertyRS.getPublicPropertiesCached()
            .then(res => {
                this.logoUrl = `url("assets/images/${res.logoFileName}")`;
                this.cd.markForCheck();
            });
    }

    public navigateToStartPage(): void {
        const params: ITourParams = {
            tourType: 'startTour',
        };
        navigateToStartPageForRoleWithParams(this.authServiceRs.getPrincipal().getCurrentRole(), this.$state, params);
        this.kibonGuidedTourService.emit();
    }

    public cancel(): void {
        navigateToStartPageForRole(this.authServiceRs.getPrincipal().getCurrentRole(), this.$state);
    }

    public isNotSozialdienstRole(): boolean {
        return !this.authServiceRs.isOneOfRoles(TSRoleUtil.getSozialdienstRolle());
    }
}
