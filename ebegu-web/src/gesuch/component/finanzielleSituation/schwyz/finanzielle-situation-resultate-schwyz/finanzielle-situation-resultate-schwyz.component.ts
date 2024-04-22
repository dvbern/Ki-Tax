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
 */

import {ChangeDetectionStrategy, ChangeDetectorRef, Component} from '@angular/core';
import {LogFactory} from '../../../../../app/core/logging/LogFactory';
import {TSFinanzielleSituationResultateDTO} from '../../../../../models/dto/TSFinanzielleSituationResultateDTO';
import {TSFinanzielleSituationSubStepName} from '../../../../../models/enums/TSFinanzielleSituationSubStepName';
import {TSWizardStepName} from '../../../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../../../models/enums/TSWizardStepStatus';
import {TSFinanzModel} from '../../../../../models/TSFinanzModel';
import {EbeguUtil} from '../../../../../utils/EbeguUtil';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../../service/wizardStepManager';
import {AbstractGesuchViewX} from '../../../abstractGesuchViewX';
import {FinanzielleSituationSchwyzService} from '../finanzielle-situation-schwyz.service';

const LOG = LogFactory.createLog('FinanzielleSituationResultateSchwyzComponent');

@Component({
    selector: 'dv-finanzielle-situation-resultate-schwyz',
    templateUrl: './finanzielle-situation-resultate-schwyz.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FinanzielleSituationResultateSchwyzComponent extends AbstractGesuchViewX<TSFinanzModel> {
    public resultate?: TSFinanzielleSituationResultateDTO;

    public constructor(
        protected ref: ChangeDetectorRef,
        protected readonly gesuchmodelManager: GesuchModelManager,
        protected readonly wizardstepManager: WizardStepManager,
        private readonly finanzielleSituationSchwyzService: FinanzielleSituationSchwyzService,
    ) {
        super(gesuchmodelManager, wizardstepManager, TSWizardStepName.FINANZIELLE_SITUATION_SCHWYZ);
        this.model =
            new TSFinanzModel(this.gesuchModelManager.getBasisjahr(), this.gesuchModelManager.isGesuchsteller2Required(), 1);
        this.model.copyFinSitDataFromGesuch(this.gesuchModelManager.getGesuch());

        this.calculate();
    }

    public isNotNullOrUndefined(toCheck: any): boolean {
        return EbeguUtil.isNotNullOrUndefined(toCheck);
    }

    public async navigate(onResult: (arg: any) => any): Promise<void> {
        await this.updateWizardStepStatus();
        onResult(true);
    }

    public getSubStepName(): TSFinanzielleSituationSubStepName {
        return TSFinanzielleSituationSubStepName.SCHWYZ_RESULTATE;
    }

    public calculate(): void {
        this.finanzielleSituationSchwyzService.massgebendesEinkommenStore.subscribe(resultate => {
                this.resultate = resultate;
                this.ref.markForCheck();
            }, error => LOG.error(error),
        );
        this.finanzielleSituationSchwyzService.calculateMassgebendesEinkommen(this.model);
    }

    private updateWizardStepStatus(): Promise<void> {
        return this.gesuchModelManager.getGesuch().isMutation() ?
            this.wizardStepManager.updateCurrentWizardStepStatusMutiert() as Promise<void> :
            this.wizardStepManager.updateCurrentWizardStepStatusSafe(
                TSWizardStepName.FINANZIELLE_SITUATION_SCHWYZ,
                TSWizardStepStatus.OK) as Promise<void>;
    }
}
