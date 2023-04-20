/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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
import {
    FinanzielleSituationAppenzellService
} from '../component/finanzielleSituation/appenzell/finanzielle-situation-appenzell.service';
import {FinanzielleSituationSubStepManager} from './finanzielleSituationSubStepManager';
import {GesuchModelManager} from './gesuchModelManager';

export class FinanzielleSituationSubStepManagerAppenzell extends FinanzielleSituationSubStepManager {

    public constructor(
        gesuchModelManager: GesuchModelManager
    ) {
        super(gesuchModelManager);
    }

    public getNextSubStepFinanzielleSituation(
        currentSubStep: TSFinanzielleSituationSubStepName
    ): TSFinanzielleSituationSubStepName {
        if (currentSubStep === TSFinanzielleSituationSubStepName.APPENZELL_START
        && FinanzielleSituationAppenzellService.finSitNeedsTwoSeparateAntragsteller(this.gesuchModelManager.getGesuch())) {
            return TSFinanzielleSituationSubStepName.APPENZELL_GS2;
        }
        return TSFinanzielleSituationSubStepName.KEIN_WEITERER_SUBSTEP;
    }

    public getPreviousSubStepFinanzielleSituation(
        currentSubStep: TSFinanzielleSituationSubStepName
    ): TSFinanzielleSituationSubStepName {
        if (currentSubStep === TSFinanzielleSituationSubStepName.APPENZELL_GS2) {
            return TSFinanzielleSituationSubStepName.APPENZELL_START;
        }
        return TSFinanzielleSituationSubStepName.KEIN_WEITERER_SUBSTEP;
    }
}
