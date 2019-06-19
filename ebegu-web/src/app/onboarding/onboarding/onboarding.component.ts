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

import {ChangeDetectionStrategy, Component, Input} from '@angular/core';
import {NgForm} from '@angular/forms';
import {StateService} from '@uirouter/core';
import {from, Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import GemeindeRS from '../../../gesuch/service/gemeindeRS.rest';
import TSGemeinde from '../../../models/TSGemeinde';
import EbeguUtil from '../../../utils/EbeguUtil';
import {ApplicationPropertyRS} from '../../core/rest-services/applicationPropertyRS.rest';

@Component({
    selector: 'dv-onboarding',
    changeDetection: ChangeDetectionStrategy.OnPush,
    templateUrl: './onboarding.component.html',
    styleUrls: ['./onboarding.component.less', '../onboarding.less'],
})
export class OnboardingComponent {

    @Input() public nextState: string = 'onboarding.be-login';
    @Input() public showLogin: boolean = true;

    public gemeinden$: Observable<TSGemeinde[]>;
    public gemeinde?: TSGemeinde;

    public isDummyMode$: Observable<boolean>;

    public constructor(
        private readonly gemeindeRS: GemeindeRS,
        private readonly applicationPropertyRS: ApplicationPropertyRS,
        private readonly stateService: StateService,
    ) {
        this.gemeinden$ = from(this.gemeindeRS.getAktiveGemeinden())
            .pipe(map(gemeinden => {
                gemeinden.sort(EbeguUtil.compareByName);

                return gemeinden;
            }));

        this.isDummyMode$ = from(this.applicationPropertyRS.isDummyMode());
    }

    public onSubmit(form: NgForm): void {
        if (!form.valid) {
            return;
        }

        this.stateService.go(this.nextState, {gemeindeId: this.gemeinde.id});
    }
}