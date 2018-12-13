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
import {Transition} from '@uirouter/core';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';

@Component({
    selector: 'dv-login-info',
    templateUrl: './login-info.component.html',
    styleUrls: ['./login-info.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginInfoComponent implements OnInit{

    private portalAccountCreationLink: string;

    public constructor(public transition: Transition,
                       private readonly authService: AuthServiceRS,
                       private readonly cdRef: ChangeDetectorRef,
                       ) {
    }



    public ngOnInit(): void {
        this.loadPortalAccountCreationLink();
    }

    public goToLoginWithReturnToState(): void {
        const params = this.transition.params();
        const options = this.transition.options();

        this.transition.router.stateService.go('einladung.abschliessen', params, options);
    }

    public loadPortalAccountCreationLink(): void {
         this.authService.portalAccountCreationPageLink().then((result) => {
             this.portalAccountCreationLink = result;
             this.cdRef.markForCheck();
         })
     }
}
