/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

import {ChangeDetectionStrategy, ChangeDetectorRef, Component} from '@angular/core';
import {Transition} from '@uirouter/core';
import {TSWizardStepName} from '../../../../../models/enums/TSWizardStepName';
import {BerechnungsManager} from '../../../../service/berechnungsManager';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../../service/wizardStepManager';
import {FinanzielleSituationLuzernService} from '../../../finanzielleSituation/luzern/finanzielle-situation-luzern.service';
import {AbstractEinkommensverschlechterungResultat} from '../../AbstractEinkommensverschlechterungResultat';

@Component({
    selector: 'dv-einkommensverschlechterung-luzern-resultate-view',
    templateUrl: './einkommensverschlechterung-luzern-resultate-view.component.html',
    styleUrls: ['./einkommensverschlechterung-luzern-resultate-view.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class EinkommensverschlechterungLuzernResultateViewComponent extends AbstractEinkommensverschlechterungResultat {

    public constructor(
        public gesuchModelManager: GesuchModelManager,
        protected wizardStepManager: WizardStepManager,
        protected finSitLuService: FinanzielleSituationLuzernService,
        protected berechnungsManager: BerechnungsManager,
        protected ref: ChangeDetectorRef,
        protected readonly $transition$: Transition
    ) {
        super(gesuchModelManager,
            wizardStepManager,
            berechnungsManager,
            ref,
            TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG_LUZERN,
            $transition$);
    }
}
