/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

import {Injectable} from '@angular/core';
import {IPromise} from 'angular';
import {Observable, ReplaySubject} from 'rxjs';
import {map} from 'rxjs/operators';
import {TSWizardStepXTyp} from '../../../models/enums/TSWizardStepXTyp';
import {TSWizardStepX} from '../../../models/TSWizardStepX';
import {TSDemoFeature} from '../directive/dv-hide-feature/TSDemoFeature';
import {LogFactory} from '../logging/LogFactory';
import {ApplicationPropertyRS} from '../rest-services/applicationPropertyRS.rest';

@Injectable({
    providedIn: 'root'
})
export class DemoFeatureRS {
    public constructor(
        private readonly applicationPropertyRS: ApplicationPropertyRS
    ) {}

    public isDemoFeatureAllowed(
        dvDemoFeature: TSDemoFeature
    ): IPromise<boolean> {
        return this.applicationPropertyRS
            .getActivatedDemoFeatures()
            .then(allowedElement =>
                allowedElement.includes(dvDemoFeature.valueOf())
            );
    }
}
