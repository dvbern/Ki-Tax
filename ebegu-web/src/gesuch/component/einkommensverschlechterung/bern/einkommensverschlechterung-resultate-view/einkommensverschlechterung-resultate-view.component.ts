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
    Component,
    ViewChild
} from '@angular/core';
import {Transition} from '@uirouter/core';
import {IPromise} from 'angular';
import {ErrorService} from '../../../../../app/core/errors/service/ErrorService';
import {TSFinanzielleSituationResultateDTO} from '../../../../../models/dto/TSFinanzielleSituationResultateDTO';
import {TSWizardStepName} from '../../../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../../../models/enums/TSWizardStepStatus';
import {TSEinkommensverschlechterung} from '../../../../../models/TSEinkommensverschlechterung';
import {TSEinkommensverschlechterungContainer} from '../../../../../models/TSEinkommensverschlechterungContainer';
import {EbeguUtil} from '../../../../../utils/EbeguUtil';
import {BerechnungsManager} from '../../../../service/berechnungsManager';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../../service/wizardStepManager';
import {AbstractEinkommensverschlechterungResultat} from '../../AbstractEinkommensverschlechterungResultat';
import {EinstellungRS} from '../../../../../admin/service/einstellungRS.rest';

@Component({
    selector: 'dv-einkommensverschlechterung-resultate-view',
    templateUrl: './einkommensverschlechterung-resultate-view.component.html',
    styleUrls: ['./einkommensverschlechterung-resultate-view.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class EinkommensverschlechterungResultateViewComponent extends AbstractEinkommensverschlechterungResultat {
    public resultatBasisjahr?: TSFinanzielleSituationResultateDTO;
    public resultatProzent: string;
    public readOnly: boolean = false;

    public constructor(
        public gesuchModelManager: GesuchModelManager,
        protected wizardStepManager: WizardStepManager,
        protected berechnungsManager: BerechnungsManager,
        protected ref: ChangeDetectorRef,
        protected readonly einstellungRS: EinstellungRS,
        protected readonly $transition$: Transition,
        private readonly errorService: ErrorService
    ) {
        super(
            gesuchModelManager,
            wizardStepManager,
            berechnungsManager,
            ref,
            TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG,
            einstellungRS,
            $transition$
        );
        this.readOnly = this.gesuchModelManager.isGesuchReadonly();
    }

    public showGS2(): boolean {
        return this.model.isGesuchsteller2Required();
    }

    public showResult(): boolean {
        if (this.model.getBasisJahrPlus() !== 1) {
            return true;
        }

        const infoContainer =
            this.model.einkommensverschlechterungInfoContainer;
        const ekvFuerBasisJahrPlus =
            infoContainer.einkommensverschlechterungInfoJA
                .ekvFuerBasisJahrPlus1;

        return ekvFuerBasisJahrPlus && ekvFuerBasisJahrPlus;
    }

    public save(onResult: (arg: any) => any): IPromise<any> {
        if (!this.isGesuchValid()) {
            onResult(undefined);
            return undefined;
        }

        if (!this.form.dirty) {
            // If there are no changes in form we don't need anything to update on Server and we could return the
            // promise immediately
            // Update wizardStepStatus also if the form is empty and not dirty
            return this.updateStatus(false).then(onResult(true));
        }

        this.model.copyEkvSitDataToGesuch(this.gesuchModelManager.getGesuch());
        this.errorService.clearAll();

        if (!this.gesuchModelManager.getGesuch().gesuchsteller1) {
            onResult(undefined);
            return undefined;
        }

        this.gesuchModelManager.setGesuchstellerNumber(1);
        if (this.gesuchModelManager.getGesuch().gesuchsteller2) {
            return this.gesuchModelManager
                .saveEinkommensverschlechterungContainer()
                .then(() => {
                    this.gesuchModelManager.setGesuchstellerNumber(2);
                    return this.gesuchModelManager
                        .saveEinkommensverschlechterungContainer()
                        .then(() =>
                            this.updateStatus(true).then(onResult(true))
                        );
                });
        }
        // eslint-disable-next-line
        return this.gesuchModelManager
            .saveEinkommensverschlechterungContainer()
            .then(() => {
                return this.updateStatus(true).then(onResult(true));
            });
    }

    public onValueChangeFunction = (): void => {
        this.calculate();
    };

    public getEinkommensverschlechterungContainerGS1(): TSEinkommensverschlechterungContainer {
        return this.model.einkommensverschlechterungContainerGS1;
    }

    // eslint-disable-next-line
    public getEinkommensverschlechterungGS1_GS(): TSEinkommensverschlechterung {
        return this.model.getBasisJahrPlus() === 2
            ? this.getEinkommensverschlechterungContainerGS1()
                  .ekvGSBasisJahrPlus2
            : this.getEinkommensverschlechterungContainerGS1()
                  .ekvGSBasisJahrPlus1;
    }

    // eslint-disable-next-line
    public getEinkommensverschlechterungGS1_JA(): TSEinkommensverschlechterung {
        return this.model.getBasisJahrPlus() === 2
            ? this.getEinkommensverschlechterungContainerGS1()
                  .ekvJABasisJahrPlus2
            : this.getEinkommensverschlechterungContainerGS1()
                  .ekvJABasisJahrPlus1;
    }

    public getEinkommensverschlechterungContainerGS2(): TSEinkommensverschlechterungContainer {
        return this.model.einkommensverschlechterungContainerGS2;
    }

    // eslint-disable-next-line
    public getEinkommensverschlechterungGS2_GS(): TSEinkommensverschlechterung {
        return this.model.getBasisJahrPlus() === 2
            ? this.getEinkommensverschlechterungContainerGS2()
                  .ekvGSBasisJahrPlus2
            : this.getEinkommensverschlechterungContainerGS2()
                  .ekvGSBasisJahrPlus1;
    }

    // eslint-disable-next-line
    public getEinkommensverschlechterungGS2_JA(): TSEinkommensverschlechterung {
        return this.model.getBasisJahrPlus() === 2
            ? this.getEinkommensverschlechterungContainerGS2()
                  .ekvJABasisJahrPlus2
            : this.getEinkommensverschlechterungContainerGS2()
                  .ekvJABasisJahrPlus1;
    }

    public getBruttovermoegenTooltipLabel(): string {
        if (this.isFKJV()) {
            return 'FINANZIELLE_SITUATION_VERMOEGEN_HELP_FKJV';
        }
        return 'FINANZIELLE_SITUATION_VERMOEGEN_HELP';
    }
}
