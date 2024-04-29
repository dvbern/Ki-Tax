/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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
 *
 */

import {ChangeDetectionStrategy, Component} from '@angular/core';
import {isAtLeastFreigegeben} from '../../../../models/enums/TSAntragStatus';
import {TSWizardStepName} from '../../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../../models/enums/TSWizardStepStatus';
import {TSFreigabe} from '../../../../models/TSFreigabe';
import {AbstractGesuchViewX} from '../../../component/abstractGesuchViewX';
import {GesuchModelManager} from '../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../service/wizardStepManager';

interface Model {
    userConfirmedCorrectness: boolean;
}

const STEP_NAME = TSWizardStepName.FREIGABE;

@Component({
    templateUrl: './online-freigabe.component.html',
    selector: 'dv-online-freigabe',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class OnlineFreigabeComponent extends AbstractGesuchViewX<Model> {
    public freigegeben: boolean;

    public constructor(
        gesuchModelManager: GesuchModelManager,
        wizardStepManager: WizardStepManager,
    ) {
        super(gesuchModelManager, wizardStepManager, STEP_NAME);

        const unbesucht = wizardStepManager.getStepByName(STEP_NAME).wizardStepStatus === TSWizardStepStatus.UNBESUCHT;
        this.freigegeben = isAtLeastFreigegeben(gesuchModelManager.getGesuch().status);
        this.model = {userConfirmedCorrectness: this.freigegeben};

        if (!this.freigegeben && unbesucht) {
            this.wizardStepManager.updateCurrentWizardStepStatusSafe(
                STEP_NAME,
                TSWizardStepStatus.IN_BEARBEITUNG);
        } else if (this.freigegeben) {
            this.wizardStepManager.updateCurrentWizardStepStatusSafe(
                STEP_NAME,
                TSWizardStepStatus.OK);
        }
    }

    public async freigeben(): Promise<unknown> {
        if (!this.model.userConfirmedCorrectness) {
            return null;
        }
        const freigabeDto = new TSFreigabe(null, null, this.model.userConfirmedCorrectness);
        try {
            await this.gesuchModelManager.antragFreigeben(this.gesuchModelManager.getGesuch().id, freigabeDto);
            return this.wizardStepManager.updateCurrentWizardStepStatusSafe(
                STEP_NAME,
                TSWizardStepStatus.OK);
        } catch (e) {
            return this.wizardStepManager.updateCurrentWizardStepStatusSafe(
                STEP_NAME,
                TSWizardStepStatus.NOK);
        }
    }

    public freigebenButtonDisabled() {
        return !this.model.userConfirmedCorrectness || this.freigegeben;
    }
}
