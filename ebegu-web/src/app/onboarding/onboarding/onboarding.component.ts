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

import {Component, Input, OnInit} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {from, Observable} from 'rxjs';
import {ApplicationPropertyRS} from '../../core/rest-services/applicationPropertyRS.rest';
import {OnboardingPlaceholderService} from '../service/onboarding-placeholder.service';

@Component({
    selector: 'dv-onboarding',
    templateUrl: './onboarding.component.html',
    styleUrls: ['./onboarding.component.less', '../onboarding.less'],
})
export class OnboardingComponent implements OnInit {

    @Input() public showLogin: boolean = true;

    private readonly description1: string = 'ONBOARDING_MAIN_DESC1';
    private readonly description2: string = 'ONBOARDING_MAIN_DESC2';
    private readonly description3: string = 'ONBOARDING_MAIN_DESC3';
    private readonly description4: string = 'ONBOARDING_MAIN_DESC4';
    public isDummyMode$: Observable<boolean>;

    public constructor(
        private readonly applicationPropertyRS: ApplicationPropertyRS,
        private readonly onboardingPlaceholderService: OnboardingPlaceholderService,
        private readonly translate: TranslateService,
    ) {
        this.isDummyMode$ = from(this.applicationPropertyRS.isDummyMode());

    }

    public ngOnInit(): void {
        this.onboardingPlaceholderService.setDescription1(this.translate.instant(this.description1));
        this.onboardingPlaceholderService.setDescription2(this.translate.instant(this.description2));
        this.onboardingPlaceholderService.setDescription3(this.translate.instant(this.description3));
        this.onboardingPlaceholderService.setDescription4(this.translate.instant(this.description4));
    }
}
