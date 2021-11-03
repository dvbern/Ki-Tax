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
import {LogFactory} from '../../core/logging/LogFactory';
import {ApplicationPropertyRS} from '../../core/rest-services/applicationPropertyRS.rest';
import {OnboardingPlaceholderService} from '../service/onboarding-placeholder.service';

const LOG = LogFactory.createLog('OnboardingMainComponent');

@Component({
    selector: 'dv-onboarding-main',
    templateUrl: './onboarding-main.component.html',
    styleUrls: ['./onboarding-main.component.less', '../onboarding.less'],
})
export class OnboardingMainComponent {
    public description1: string = 'ONBOARDING_MAIN_DESC1';
    public description2: string = 'ONBOARDING_MAIN_DESC2';
    public description3: string = 'ONBOARDING_MAIN_DESC3';
    public description4: string = 'ONBOARDING_MAIN_DESC4';
    public splittedScreen: boolean = true;
    public logoFileNameWhite: string;

    public constructor(
        private readonly onboardingPlaceholderService: OnboardingPlaceholderService,
        private readonly applicationPropertyRS: ApplicationPropertyRS
    ) {
        this.onboardingPlaceholderService.description1$.subscribe(updatedDescription1 => {
            this.description1 = updatedDescription1;
        }, err => LOG.error(err));
        this.onboardingPlaceholderService.description2$.subscribe(updatedDescription2 => {
            this.description2 = updatedDescription2;
        }, err => LOG.error(err));
        this.onboardingPlaceholderService.description3$.subscribe(updatedDescription3 => {
            this.description3 = updatedDescription3;
        }, err => LOG.error(err));
        this.onboardingPlaceholderService.description4$.subscribe(updatedDescription4 => {
            this.description4 = updatedDescription4;
        }, err => LOG.error(err));
        this.onboardingPlaceholderService.splittedScreen$.subscribe(updatedSplittedScreen => {
            this.splittedScreen = updatedSplittedScreen;
        }, err => LOG.error(err));
        this.applicationPropertyRS.getPublicPropertiesCached().then(res => {
            this.logoFileNameWhite = res.logoFileNameWhite;
        });
    }

    public getLogoWhiteUrl(): string {
        return `url(\'assets/images/${this.logoFileNameWhite}\')`;
    }
}
