/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

import {Component, OnInit, ChangeDetectionStrategy} from '@angular/core';
import {NgForm} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {from, Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import GemeindeRS from '../../../gesuch/service/gemeindeRS.rest';
import TSGemeinde from '../../../models/TSGemeinde';
import EbeguUtil from '../../../utils/EbeguUtil';
import {OnboardingPlaceholderService} from '../service/onboarding-placeholder.service';

@Component({
    selector: 'dv-onboarding-info-gem',
    templateUrl: './onboarding-info-gem.component.html',
    styleUrls: ['./onboarding-info-gem.component.less', '../onboarding.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class OnboardingInfoGemComponent implements OnInit {
    private readonly description1: string = 'ONBOARDING_GEM_DESC1';
    private readonly description2: string = 'ONBOARDING_GEM_DESC2';
    private readonly description3: string = 'ONBOARDING_GEM_DESC3';
    private readonly description4: string = 'ONBOARDING_GEM_DESC4';
    private readonly subjectText: string = 'ONBOARDING_MAIL_SUBJECT';
    private readonly emailBody: string = 'ONBOARDING_MAIL_GEM_BODY';
    private readonly emailEnd: string = 'ONBOARDING_MAIL_BODY_END';

    public testZugangBeantragen: boolean;
    public gemeinden$: Observable<TSGemeinde[]>;
    public gemeinde?: TSGemeinde;

    public constructor(private readonly onboardingPlaceholderService: OnboardingPlaceholderService,
                       private readonly translate: TranslateService,
                       private readonly gemeindeRS: GemeindeRS,
    ) {
        this.gemeinden$ = from(this.gemeindeRS.getAktiveGemeinden())
            .pipe(map(gemeinden => {
                gemeinden.sort(EbeguUtil.compareByName);

                return gemeinden;
            }));
    }

    public ngOnInit(): void {
        this.onboardingPlaceholderService.setDescription1(this.translate.instant(this.description1));
        this.onboardingPlaceholderService.setDescription2(this.translate.instant(this.description2));
        this.onboardingPlaceholderService.setDescription3(this.translate.instant(this.description3));
        this.onboardingPlaceholderService.setDescription4(this.translate.instant(this.description4));
    }

    public onSubmit(form: NgForm): void {
        if (!form.valid) {
            return;
        }
        const space = ' ';
        const punkt = '.';
        const mailto = 'mailto:support@kibon.ch&subject=';
        const emailBody = '&body=';
        const zeilenUmbruch = '%0D%0A%0D%0A';
        const bodyText: string = this.translate.instant(this.emailBody);
        const subject: string = this.translate.instant(this.subjectText);
        const body = bodyText + space + this.gemeinde.name + punkt;
        const endBody: string = this.translate.instant(this.emailEnd);
        window.location.href = mailto + subject + emailBody + body + zeilenUmbruch + endBody;
    }
}
