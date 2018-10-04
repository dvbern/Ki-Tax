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

import {Component} from '@angular/core';
import {Transition} from '@uirouter/core';

@Component({
    selector: 'dv-onboarding-be-login',
    templateUrl: './onboarding-be-login.component.html',
    styleUrls: ['../onboarding.less', './onboarding-be-login.component.less'],
})
export class OnboardingBeLoginComponent {

    public readonly gemeindeId: string;

    public constructor(private readonly transition: Transition,
    ) {
        this.gemeindeId = this.transition.params().gemeindeId;
    }
}
