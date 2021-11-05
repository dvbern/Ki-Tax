/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import {TSFinanzielleSituationSubStepName} from '../../models/enums/TSFinanzielleSituationSubStepName';
import {FinanzielleSituationLuzernService} from '../component/finanzielleSituation/luzern/finanzielle-situation-luzern.service';
import {FinanzielleSituationSubStepManager} from './finanzielleSituationSubStepManager';
import {GesuchModelManager} from './gesuchModelManager';

export class FinanzielleSituationSubStepManagerLuzern extends FinanzielleSituationSubStepManager {

    public constructor(gesuchModelManager: GesuchModelManager) {
        super(gesuchModelManager);
    }

    public getNextSubStepFinanzielleSituation(
        currentSubStep: TSFinanzielleSituationSubStepName,
    ): TSFinanzielleSituationSubStepName {
        if (TSFinanzielleSituationSubStepName.LUZERN_START === currentSubStep) {
            if (FinanzielleSituationLuzernService.finSitNeedsTwoAntragsteller(this.gesuchModelManager)) {
                return TSFinanzielleSituationSubStepName.LUZERN_GS2;
            }
            return TSFinanzielleSituationSubStepName.KEIN_WEITERER_SUBSTEP;
        }
        if (TSFinanzielleSituationSubStepName.LUZERN_GS2 === currentSubStep) {
            return TSFinanzielleSituationSubStepName.LUZERN_RESULTATE;
        }
        return TSFinanzielleSituationSubStepName.KEIN_WEITERER_SUBSTEP;
    }

    public getPreviousSubStepFinanzielleSituation(
        currentSubStep: TSFinanzielleSituationSubStepName,
    ): TSFinanzielleSituationSubStepName {
        if (TSFinanzielleSituationSubStepName.LUZERN_START === currentSubStep) {
            return TSFinanzielleSituationSubStepName.KEIN_WEITERER_SUBSTEP;
        }
        if (TSFinanzielleSituationSubStepName.LUZERN_GS2 === currentSubStep) {
            return TSFinanzielleSituationSubStepName.LUZERN_START;
        }
        if (TSFinanzielleSituationSubStepName.LUZERN_RESULTATE === currentSubStep) {
            return TSFinanzielleSituationSubStepName.LUZERN_GS2;
        }
        return TSFinanzielleSituationSubStepName.KEIN_WEITERER_SUBSTEP;
    }
}

