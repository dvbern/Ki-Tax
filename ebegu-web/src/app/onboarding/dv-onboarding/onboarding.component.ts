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

import {ChangeDetectionStrategy, Component} from '@angular/core';
import {NgForm} from '@angular/forms';
import {StateService} from '@uirouter/core';
import {from, Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import GemeindeRS from '../../../gesuch/service/gemeindeRS.rest';
import TSGemeinde from '../../../models/TSGemeinde';
import {ApplicationPropertyRS} from '../../core/rest-services/applicationPropertyRS.rest';

@Component({
    selector: 'dv-onboarding',
    changeDetection: ChangeDetectionStrategy.OnPush,
    templateUrl: './onboarding.component.html',
    styleUrls: ['../onboarding.less', './onboarding.component.less'],
})
export class OnboardingComponent {
    public gemeinden$: Observable<TSGemeinde[]>;
    public gemeinde?: TSGemeinde;

    public isDummyMode$: Observable<boolean>;

    constructor(private readonly gemeindeRs: GemeindeRS,
                private readonly applicationPropertyRS: ApplicationPropertyRS,
                private readonly stateService: StateService,
    ) {
        this.gemeinden$ = from(this.gemeindeRs.getAllGemeinden())
            .pipe(map(gemeinden => gemeinden.sort((a, b) => a.name.localeCompare(b.name))));

        this.isDummyMode$ = from(this.applicationPropertyRS.isDummyMode());
    }

    public onSubmit(form: NgForm): void {
        if (!form.valid) {
            return;
        }

        this.stateService.go('onboarding.be-login', {gemeindeId: this.gemeinde.id});
    }
}
