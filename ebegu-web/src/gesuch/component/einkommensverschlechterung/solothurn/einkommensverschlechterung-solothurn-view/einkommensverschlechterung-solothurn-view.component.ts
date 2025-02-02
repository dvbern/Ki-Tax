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

import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component
} from '@angular/core';
import {Transition} from '@uirouter/core';
import {IPromise} from 'angular';
import {TSFinanzielleSituationResultateDTO} from '../../../../../models/dto/TSFinanzielleSituationResultateDTO';
import {TSWizardStepName} from '../../../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../../../models/enums/TSWizardStepStatus';
import {TSEinkommensverschlechterungContainer} from '../../../../../models/TSEinkommensverschlechterungContainer';
import {TSFinanzModel} from '../../../../../models/TSFinanzModel';
import {BerechnungsManager} from '../../../../service/berechnungsManager';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../../service/wizardStepManager';
import {AbstractGesuchViewX} from '../../../abstractGesuchViewX';

@Component({
    selector: 'dv-einkommensverschlechterung-solothurn-view',
    templateUrl: './einkommensverschlechterung-solothurn-view.component.html',
    styleUrls: ['./einkommensverschlechterung-solothurn-view.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class EinkommensverschlechterungSolothurnViewComponent extends AbstractGesuchViewX<TSFinanzModel> {
    public readOnly: boolean = false;

    public constructor(
        public gesuchModelManager: GesuchModelManager,
        protected wizardStepManager: WizardStepManager,
        protected berechnungsManager: BerechnungsManager,
        private readonly $transition$: Transition,
        protected ref: ChangeDetectorRef
    ) {
        super(
            gesuchModelManager,
            wizardStepManager,
            TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG_SOLOTHURN
        );
        const parsedGesuchstelllerNum = parseInt(
            this.$transition$.params().gesuchstellerNumber,
            10
        );
        const parsedBasisJahrPlusNum = parseInt(
            this.$transition$.params().basisjahrPlus,
            10
        );
        this.gesuchModelManager.setGesuchstellerNumber(parsedGesuchstelllerNum);
        this.gesuchModelManager.setBasisJahrPlusNumber(parsedBasisJahrPlusNum);
        this.model = new TSFinanzModel(
            this.gesuchModelManager.getBasisjahr(),
            this.gesuchModelManager.isGesuchsteller2Required(),
            parsedGesuchstelllerNum,
            parsedBasisJahrPlusNum
        );
        this.model.copyEkvDataFromGesuch(this.gesuchModelManager.getGesuch());
        this.model.copyFinSitDataFromGesuch(
            this.gesuchModelManager.getGesuch()
        );
        this.model.initEinkommensverschlechterungContainer(
            parsedBasisJahrPlusNum,
            parsedGesuchstelllerNum
        );
        this.readOnly = this.gesuchModelManager.isGesuchReadonly();
        this.wizardStepManager.updateCurrentWizardStepStatusSafe(
            TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG_SOLOTHURN,
            TSWizardStepStatus.IN_BEARBEITUNG
        );
        this.onValueChangeFunction();
    }

    public save(
        onResult: (arg: any) => void
    ): IPromise<TSEinkommensverschlechterungContainer> {
        if (!this.isGesuchValid()) {
            onResult(undefined);
            return undefined;
        }

        if (!this.form.dirty) {
            // If there are no changes in form we don't need anything to update on Server and we could return the
            // promise immediately
            onResult(this.model.getEkvContToWorkWith());
            return Promise.resolve(this.model.getEkvContToWorkWith());
        }
        this.model.copyEkvSitDataToGesuch(this.gesuchModelManager.getGesuch());
        return this.gesuchModelManager
            .saveEinkommensverschlechterungContainer()
            .then(ekv => {
                onResult(ekv);
                return ekv;
            });
    }

    public onBruttoLohnChange = (): void => {
        if (!this.form?.controls.bruttolohn.valid) {
            return;
        }
        this.berechnungsManager
            .calculateEinkommensverschlechterungTemp(
                this.model,
                this.model.getBasisJahrPlus()
            )
            .then(() => this.ref.markForCheck());
    };

    public onValueChangeFunction = (): void => {
        if (!this.model.getEkvToWorkWith().nettoVermoegen) {
            return;
        }
        this.berechnungsManager
            .calculateEinkommensverschlechterungTemp(
                this.model,
                this.model.getBasisJahrPlus()
            )
            .then(() => this.ref.markForCheck());
    };

    private getResultate(): TSFinanzielleSituationResultateDTO {
        return this.model.getBasisJahrPlus() === 2
            ? this.berechnungsManager.einkommensverschlechterungResultateBjP2
            : this.berechnungsManager.einkommensverschlechterungResultateBjP1;
    }

    public getBruttolohnJahr(): number {
        return this.gesuchModelManager.getGesuchstellerNumber() === 1
            ? this.getResultate()?.bruttolohnJahrGS1
            : this.getResultate()?.bruttolohnJahrGS2;
    }

    public getMassgebendesEinkVorAbzFamGrGSX(): number {
        return this.gesuchModelManager.getGesuchstellerNumber() === 1
            ? this.getResultate()?.massgebendesEinkVorAbzFamGrGS1
            : this.getResultate()?.massgebendesEinkVorAbzFamGrGS2;
    }

    public getLabelExtralohn(): string {
        return this.model.getEkvToWorkWith_GS()?.extraLohn
            ? 'LABEL_13'
            : 'LABEL_12';
    }
}
