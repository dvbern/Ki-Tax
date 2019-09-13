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

@Component({
    selector: 'dv-onboarding-main',
    templateUrl: './onboarding-main.component.html',
    styleUrls: ['./onboarding-main.component.less', '../onboarding.less'],
})
export class OnboardingMainComponent {
    placeholder1 = 'ONBOARDING_MAIN_PH1';
    description1 = 'ONBOARDING_MAIN_DESC1';
    placeholder2 = 'ONBOARDING_MAIN_PH2';
    description2 = 'ONBOARDING_MAIN_DESC2';
    placeholder3 = 'ONBOARDING_MAIN_PH3';
    description3 = 'ONBOARDING_MAIN_DESC3';
    placeholder4 = 'ONBOARDING_MAIN_PH4';
    description4 = 'ONBOARDING_MAIN_DESC4';

}
