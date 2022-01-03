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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {TSFinanzielleSituationSubStepName} from '../../models/enums/TSFinanzielleSituationSubStepName';
import {FinanzielleSituationSolothurnService} from '../component/finanzielleSituation/solothurn/finanzielle-situation-solothurn.service';
import {FinanzielleSituationSubStepManager} from './finanzielleSituationSubStepManager';
import {GesuchModelManager} from './gesuchModelManager';

export class FinanzielleSituationSubStepManagerSolothurn extends FinanzielleSituationSubStepManager {

    public constructor(gesuchModelManager: GesuchModelManager) {
        super(gesuchModelManager);
    }

    public getNextSubStepFinanzielleSituation(
        currentSubStep: TSFinanzielleSituationSubStepName,
    ): TSFinanzielleSituationSubStepName {
        if (currentSubStep === TSFinanzielleSituationSubStepName.SOLOTHURN_START &&
            FinanzielleSituationSolothurnService.finSitNeedsTwoAntragsteller(this.gesuchModelManager)) {
            return TSFinanzielleSituationSubStepName.SOLOTHURN_GS1;
        }
        if (currentSubStep === TSFinanzielleSituationSubStepName.SOLOTHURN_GS1) {
            return TSFinanzielleSituationSubStepName.SOLOTHURN_GS2;
        }
        return TSFinanzielleSituationSubStepName.KEIN_WEITERER_SUBSTEP;
    }

    public getPreviousSubStepFinanzielleSituation(
        currentSubStep: TSFinanzielleSituationSubStepName,
    ): TSFinanzielleSituationSubStepName {
        if (currentSubStep === TSFinanzielleSituationSubStepName.SOLOTHURN_GS2) {
            return TSFinanzielleSituationSubStepName.SOLOTHURN_GS1;
        }
        if (currentSubStep === TSFinanzielleSituationSubStepName.SOLOTHURN_GS1) {
            return TSFinanzielleSituationSubStepName.SOLOTHURN_START;
        }
        return TSFinanzielleSituationSubStepName.KEIN_WEITERER_SUBSTEP;
    }
}
