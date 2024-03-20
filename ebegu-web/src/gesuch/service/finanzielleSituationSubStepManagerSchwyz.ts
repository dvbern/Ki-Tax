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

import {Moment} from 'moment';
import {TSFinanzielleSituationSubStepName} from '../../models/enums/TSFinanzielleSituationSubStepName';
import {FinanzielleSituationSubStepManager} from './finanzielleSituationSubStepManager';
import {GesuchModelManager} from './gesuchModelManager';

export class FinanzielleSituationSubStepManagerSchwyz extends FinanzielleSituationSubStepManager {

    public constructor(
        gesuchModelManager: GesuchModelManager,
    ) {
        super(gesuchModelManager);
    }

    public getNextSubStepFinanzielleSituation(
        currentSubStep: TSFinanzielleSituationSubStepName,
    ): TSFinanzielleSituationSubStepName {
        if (currentSubStep === TSFinanzielleSituationSubStepName.SCHWYZ_START
            && this.hasSecondGesuchsteller()
            && !this.gesuchModelManager.getGesuch().extractFamiliensituation().gemeinsameSteuererklaerung) {
            return TSFinanzielleSituationSubStepName.APPENZELL_GS2;
        }
        if (currentSubStep === TSFinanzielleSituationSubStepName.SCHWYZ_GS1) {
            return TSFinanzielleSituationSubStepName.SCHWYZ_GS2;
        }
        return TSFinanzielleSituationSubStepName.KEIN_WEITERER_SUBSTEP;
    }

    public getPreviousSubStepFinanzielleSituation(
        currentSubStep: TSFinanzielleSituationSubStepName,
    ): TSFinanzielleSituationSubStepName {
        if (currentSubStep === TSFinanzielleSituationSubStepName.SCHWYZ_GS2) {
            return TSFinanzielleSituationSubStepName.SCHWYZ_GS2;
        }
        if (currentSubStep === TSFinanzielleSituationSubStepName.SCHWYZ_GS1) {
            return TSFinanzielleSituationSubStepName.SCHWYZ_START;
        }
        return TSFinanzielleSituationSubStepName.KEIN_WEITERER_SUBSTEP;
    }

    private hasSecondGesuchsteller(): boolean {
        const endOfPeriode: Moment = this.gesuchModelManager.getGesuchsperiode().gueltigkeit.gueltigBis;
        return this.gesuchModelManager.getFamiliensituation().hasSecondGesuchsteller(endOfPeriode);
    }
}
