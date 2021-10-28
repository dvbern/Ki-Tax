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

import {TSWizardSubStepName} from '../../models/enums/TSWizardSubStepName';
import {FinanzielleSituationSubStepManager} from './finanzielleSituationSubStepManager';
import {GesuchModelManager} from './gesuchModelManager';

export class FinanzielleSituationSubStepManagerBernAsiv extends FinanzielleSituationSubStepManager {

    public constructor(gesuchModelManager: GesuchModelManager) {
        super(gesuchModelManager);
    }

    public getNextSubStepFinanzielleSituation(
        currentSubStep: TSWizardSubStepName,
    ): TSWizardSubStepName {
        if (TSWizardSubStepName.FINANZIELLE_SITUATION_START === currentSubStep) {
            // (1) Sozialhilfe, sonst keine Details
            if (this.gesuchModelManager.isSozialhilfeBezuegerZeitraeumeRequired()) {
                return TSWizardSubStepName.FINANZIELLE_SITUATION_SOZIALHILFE;
            }
            // (2) Normale FinSit Eingabe
            if (this.gesuchModelManager.isFinanzielleSituationEnabled()
                && this.gesuchModelManager.isFinanzielleSituationRequired()) {
                return TSWizardSubStepName.FINANZIELLE_SITUATON_GS1;
            }
            // (3) Keine FinSit notwendig, aber auch keine Sozialhilfedetails (Gemeindeabhaengig)
            return TSWizardSubStepName.KEIN_WEITERER_SUBSTEP;
        }
        if (TSWizardSubStepName.FINANZIELLE_SITUATON_GS1 === currentSubStep) {
            if (this.gesuchModelManager.getGesuchstellerNumber() === 1
                && this.gesuchModelManager.isGesuchsteller2Required()) {
                return TSWizardSubStepName.FINANZIELLE_SITUATON_GS2;
            }
            return TSWizardSubStepName.FINANZIELLE_SITUATION_RESULTATE;
        }
        if (TSWizardSubStepName.FINANZIELLE_SITUATON_GS2 === currentSubStep) {
            return TSWizardSubStepName.FINANZIELLE_SITUATION_RESULTATE;
        }
        if (TSWizardSubStepName.FINANZIELLE_SITUATION_RESULTATE === currentSubStep) {
            return TSWizardSubStepName.KEIN_WEITERER_SUBSTEP;
        }
        if (TSWizardSubStepName.FINANZIELLE_SITUATION_SOZIALHILFE === currentSubStep) {
            return TSWizardSubStepName.KEIN_WEITERER_SUBSTEP;
        }
        if (TSWizardSubStepName.FINANZIELLE_SITUATION_SOZIALHILFE_DETAIL === currentSubStep) {
            return TSWizardSubStepName.FINANZIELLE_SITUATION_SOZIALHILFE;
        }
        return TSWizardSubStepName.KEIN_WEITERER_SUBSTEP;
    }

    public getPreviousSubStepFinanzielleSituation(
        currentSubStep: TSWizardSubStepName,
    ): TSWizardSubStepName {
        if (TSWizardSubStepName.FINANZIELLE_SITUATION_START === currentSubStep) {
            return TSWizardSubStepName.KEIN_WEITERER_SUBSTEP;
        }
        if (TSWizardSubStepName.FINANZIELLE_SITUATON_GS1 === currentSubStep) {
            return TSWizardSubStepName.FINANZIELLE_SITUATION_START;
        }
        if (TSWizardSubStepName.FINANZIELLE_SITUATON_GS2 === currentSubStep) {
            return TSWizardSubStepName.FINANZIELLE_SITUATON_GS1;
        }
        if (TSWizardSubStepName.FINANZIELLE_SITUATION_RESULTATE === currentSubStep) {
            if ((this.gesuchModelManager.getGesuchstellerNumber() === 2)) {
                return TSWizardSubStepName.FINANZIELLE_SITUATON_GS2;
            }
            return TSWizardSubStepName.FINANZIELLE_SITUATON_GS1;
        }
        if (TSWizardSubStepName.FINANZIELLE_SITUATION_SOZIALHILFE === currentSubStep) {
            return TSWizardSubStepName.FINANZIELLE_SITUATION_START;
        }
        if (TSWizardSubStepName.FINANZIELLE_SITUATION_SOZIALHILFE_DETAIL === currentSubStep) {
            return TSWizardSubStepName.FINANZIELLE_SITUATION_SOZIALHILFE;
        }
        return TSWizardSubStepName.KEIN_WEITERER_SUBSTEP;
    }
}

