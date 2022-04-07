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

import {ChangeDetectionStrategy, ChangeDetectorRef, Component, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
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

@Component({
    selector: 'dv-einkommensverschlechterung-resultate-view',
    templateUrl: './einkommensverschlechterung-resultate-view.component.html',
    styleUrls: ['./einkommensverschlechterung-resultate-view.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EinkommensverschlechterungResultateViewComponent extends AbstractEinkommensverschlechterungResultat {

    @ViewChild(NgForm) private readonly form: NgForm;

    public resultatBasisjahr?: TSFinanzielleSituationResultateDTO;
    public resultatProzent: string;
    public readOnly: boolean = false;

    public constructor(
        public gesuchModelManager: GesuchModelManager,
        protected wizardStepManager: WizardStepManager,
        protected berechnungsManager: BerechnungsManager,
        protected ref: ChangeDetectorRef,
        protected readonly $transition$: Transition,
        private readonly errorService: ErrorService,
    ) {
        super(gesuchModelManager,
            wizardStepManager,
            berechnungsManager,
            ref,
            TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG,
            $transition$);
        this.readOnly = this.gesuchModelManager.isGesuchReadonly();
    }

    public showGS2(): boolean {
        return this.model.isGesuchsteller2Required();
    }

    public showResult(): boolean {
        if (this.model.getBasisJahrPlus() !== 1) {
            return true;
        }

        const infoContainer = this.model.einkommensverschlechterungInfoContainer;
        const ekvFuerBasisJahrPlus = infoContainer.einkommensverschlechterungInfoJA.ekvFuerBasisJahrPlus1;

        return ekvFuerBasisJahrPlus && ekvFuerBasisJahrPlus === true;
    }

    public save(onResult: Function): IPromise<any> {
        if (!this.isGesuchValid()) {
            onResult(undefined);
            return undefined;
        }

        if (!this.form.dirty) {
            // If there are no changes in form we don't need anything to update on Server and we could return the
            // promise immediately
            // Update wizardStepStatus also if the form is empty and not dirty
            return this.updateStatus(false).then(
                onResult(true),
            );
        }

        this.model.copyEkvSitDataToGesuch(this.gesuchModelManager.getGesuch());
        this.errorService.clearAll();

        if (!this.gesuchModelManager.getGesuch().gesuchsteller1) {
            onResult(undefined);
            return undefined;
        }

        this.gesuchModelManager.setGesuchstellerNumber(1);
        if (this.gesuchModelManager.getGesuch().gesuchsteller2) {
            return this.gesuchModelManager.saveEinkommensverschlechterungContainer().then(() => {
                this.gesuchModelManager.setGesuchstellerNumber(2);
                return this.gesuchModelManager.saveEinkommensverschlechterungContainer().then(() => {
                    return this.updateStatus(true).then(
                        onResult(true),
                    );
                });
            });
        }
        // tslint:disable-next-line:no-identical-functions
        return this.gesuchModelManager.saveEinkommensverschlechterungContainer().then(() => {
            return this.updateStatus(true).then(
                onResult(true),
            );
        });
    }

    public onValueChangeFunction = (): void => {
        this.calculate();
    }

    public getEinkommensverschlechterungContainerGS1(): TSEinkommensverschlechterungContainer {
        return this.model.einkommensverschlechterungContainerGS1;
    }

    // tslint:disable-next-line:naming-convention
    public getEinkommensverschlechterungGS1_GS(): TSEinkommensverschlechterung {
        return this.model.getBasisJahrPlus() === 2 ?
            this.getEinkommensverschlechterungContainerGS1().ekvGSBasisJahrPlus2 :
            this.getEinkommensverschlechterungContainerGS1().ekvGSBasisJahrPlus1;
    }

    // tslint:disable-next-line:naming-convention
    public getEinkommensverschlechterungGS1_JA(): TSEinkommensverschlechterung {
        return this.model.getBasisJahrPlus() === 2 ?
            this.getEinkommensverschlechterungContainerGS1().ekvJABasisJahrPlus2 :
            this.getEinkommensverschlechterungContainerGS1().ekvJABasisJahrPlus1;
    }

    public getEinkommensverschlechterungContainerGS2(): TSEinkommensverschlechterungContainer {
        return this.model.einkommensverschlechterungContainerGS2;
    }

    // tslint:disable-next-line:naming-convention
    public getEinkommensverschlechterungGS2_GS(): TSEinkommensverschlechterung {
        return this.model.getBasisJahrPlus() === 2 ?
            this.getEinkommensverschlechterungContainerGS2().ekvGSBasisJahrPlus2 :
            this.getEinkommensverschlechterungContainerGS2().ekvGSBasisJahrPlus1;
    }

    // tslint:disable-next-line:naming-convention
    public getEinkommensverschlechterungGS2_JA(): TSEinkommensverschlechterung {
        return this.model.getBasisJahrPlus() === 2 ?
            this.getEinkommensverschlechterungContainerGS2().ekvJABasisJahrPlus2 :
            this.getEinkommensverschlechterungContainerGS2().ekvJABasisJahrPlus1;
    }

    private isGesuchValid(): boolean {
        if (!this.form.valid) {
            for (const control in this.form.controls) {
                if (EbeguUtil.isNotNullOrUndefined(this.form.controls[control])) {
                    this.form.controls[control].markAsTouched({onlySelf: true});
                }
            }
            EbeguUtil.selectFirstInvalid();
        }

        return this.form.valid;
    }

    /**
     * Hier wird der Status von WizardStep auf OK (MUTIERT fuer Mutationen) aktualisiert aber nur wenn es die letzt
     * Seite EVResultate gespeichert wird. Sonst liefern wir einfach den aktuellen GS als Promise zurueck.
     */
    private updateStatus(changes: boolean): IPromise<any> {
        if (this.isLastEinkVersStep()) {
            if (this.gesuchModelManager.getGesuch().isMutation()) {
                if (this.wizardStepManager.getCurrentStep().wizardStepStatus === TSWizardStepStatus.NOK || changes) {
                    this.wizardStepManager.updateCurrentWizardStepStatusMutiert();
                }
            } else {
                return this.wizardStepManager.updateCurrentWizardStepStatusSafe(
                    TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG,
                    TSWizardStepStatus.OK);
            }
        }
        // wenn nichts gespeichert einfach den aktuellen GS zurueckgeben
        return Promise.resolve(this.gesuchModelManager.getStammdatenToWorkWith());
    }

    /**
     * Prueft ob es die letzte Seite von EVResultate ist. Es ist die letzte Seite wenn es zum letzten EV-Jahr gehoert
     */
    private isLastEinkVersStep(): boolean {
        // Letztes Jahr haengt von den eingegebenen Daten ab
        const info = this.gesuchModelManager.getGesuch().extractEinkommensverschlechterungInfo();

        return info.ekvFuerBasisJahrPlus2 && this.gesuchModelManager.basisJahrPlusNumber === 2
            || !info.ekvFuerBasisJahrPlus2 && this.gesuchModelManager.basisJahrPlusNumber === 1;
    }

    public getBruttovermoegenTooltipLabel(): string {
        if (this.isFKJV()) {
            return 'FINANZIELLE_SITUATION_VERMOEGEN_HELP_FKJV';
        }
        return 'FINANZIELLE_SITUATION_VERMOEGEN_HELP';
    }
}
