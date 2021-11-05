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
import {FinanzielleSituationSubStepManager} from './finanzielleSituationSubStepManager';
import {GesuchModelManager} from './gesuchModelManager';

export class FinanzielleSituationSubStepManagerBernAsiv extends FinanzielleSituationSubStepManager {

    public constructor(gesuchModelManager: GesuchModelManager) {
        super(gesuchModelManager);
    }

    public getNextSubStepFinanzielleSituation(
        currentSubStep: TSFinanzielleSituationSubStepName,
    ): TSFinanzielleSituationSubStepName {
        if (TSFinanzielleSituationSubStepName.BERN_START === currentSubStep) {
            // (1) Sozialhilfe, sonst keine Details
            if (this.gesuchModelManager.isSozialhilfeBezuegerZeitraeumeRequired()) {
                return TSFinanzielleSituationSubStepName.BERN_SOZIALHILFE;
            }
            // (2) Normale FinSit Eingabe
            if (this.gesuchModelManager.isFinanzielleSituationEnabled()
                && this.gesuchModelManager.isFinanzielleSituationRequired()) {
                return TSFinanzielleSituationSubStepName.BERN_GS1;
            }
            // (3) Keine FinSit notwendig, aber auch keine Sozialhilfedetails (Gemeindeabhaengig)
            return TSFinanzielleSituationSubStepName.KEIN_WEITERER_SUBSTEP;
        }
        if (TSFinanzielleSituationSubStepName.BERN_GS1 === currentSubStep) {
            if (this.gesuchModelManager.getGesuchstellerNumber() === 1
                && this.gesuchModelManager.isGesuchsteller2Required()) {
                return TSFinanzielleSituationSubStepName.BERN_GS2;
            }
            return TSFinanzielleSituationSubStepName.BERN_RESULTATE;
        }
        if (TSFinanzielleSituationSubStepName.BERN_GS2 === currentSubStep) {
            return TSFinanzielleSituationSubStepName.BERN_RESULTATE;
        }
        if (TSFinanzielleSituationSubStepName.BERN_RESULTATE === currentSubStep) {
            return TSFinanzielleSituationSubStepName.KEIN_WEITERER_SUBSTEP;
        }
        if (TSFinanzielleSituationSubStepName.BERN_SOZIALHILFE === currentSubStep) {
            return TSFinanzielleSituationSubStepName.KEIN_WEITERER_SUBSTEP;
        }
        if (TSFinanzielleSituationSubStepName.BERN_SOZIALHILFE_DETAIL === currentSubStep) {
            return TSFinanzielleSituationSubStepName.BERN_SOZIALHILFE;
        }
        return TSFinanzielleSituationSubStepName.KEIN_WEITERER_SUBSTEP;
    }

    public getPreviousSubStepFinanzielleSituation(
        currentSubStep: TSFinanzielleSituationSubStepName,
    ): TSFinanzielleSituationSubStepName {
        if (TSFinanzielleSituationSubStepName.BERN_START === currentSubStep) {
            return TSFinanzielleSituationSubStepName.KEIN_WEITERER_SUBSTEP;
        }
        if (TSFinanzielleSituationSubStepName.BERN_GS1 === currentSubStep) {
            return TSFinanzielleSituationSubStepName.BERN_START;
        }
        if (TSFinanzielleSituationSubStepName.BERN_GS2 === currentSubStep) {
            return TSFinanzielleSituationSubStepName.BERN_GS1;
        }
        if (TSFinanzielleSituationSubStepName.BERN_RESULTATE === currentSubStep) {
            if ((this.gesuchModelManager.getGesuchstellerNumber() === 2)) {
                return TSFinanzielleSituationSubStepName.BERN_GS2;
            }
            return TSFinanzielleSituationSubStepName.BERN_GS1;
        }
        if (TSFinanzielleSituationSubStepName.BERN_SOZIALHILFE === currentSubStep) {
            return TSFinanzielleSituationSubStepName.BERN_START;
        }
        if (TSFinanzielleSituationSubStepName.BERN_SOZIALHILFE_DETAIL === currentSubStep) {
            return TSFinanzielleSituationSubStepName.BERN_SOZIALHILFE;
        }
        return TSFinanzielleSituationSubStepName.KEIN_WEITERER_SUBSTEP;
    }
}
