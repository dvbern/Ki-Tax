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

import {TSWizardStepName} from '../../../../models/enums/TSWizardStepName';
import {TSFinanzModel} from '../../../../models/TSFinanzModel';
import {EbeguUtil} from '../../../../utils/EbeguUtil';
import {GesuchModelManager} from '../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../service/wizardStepManager';
import {AbstractGesuchViewX} from '../../abstractGesuchViewX';
import {FinanzielleSituationLuzernService} from '../../finanzielleSituation/luzern/finanzielle-situation-luzern.service';

export abstract class AbstractEKVLuzernView extends AbstractGesuchViewX<TSFinanzModel> {

    protected constructor(
        protected gesuchModelManager: GesuchModelManager,
        protected wizardStepManager: WizardStepManager,
    ) {
        super(gesuchModelManager, wizardStepManager, TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG_LUZERN);
    }

    public isGemeinsam(): boolean {
        // if we don't need two separate antragsteller for gesuch, this is the component for both antragsteller together
        // or only for the single antragsteller
        return !FinanzielleSituationLuzernService.finSitNeedsTwoSeparateAntragsteller(this.gesuchModelManager)
            && EbeguUtil.isNotNullOrUndefined(this.gesuchModelManager.getGesuch().gesuchsteller2);
    }

    public getGemeinsameFullname(): string {
        return `${this.gesuchModelManager.getGesuch().gesuchsteller1?.extractFullName()}
         + ${this.gesuchModelManager.getGesuch().gesuchsteller2?.extractFullName()}`;
    }
}
