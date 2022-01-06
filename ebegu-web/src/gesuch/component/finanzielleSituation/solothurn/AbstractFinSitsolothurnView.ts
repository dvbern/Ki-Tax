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

import {NgForm} from '@angular/forms';
import {MatRadioChange} from '@angular/material/radio';
import {IPromise} from 'angular';
import {TSWizardStepName} from '../../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../../models/enums/TSWizardStepStatus';
import {TSFinanzielleSituation} from '../../../../models/TSFinanzielleSituation';
import {TSFinanzielleSituationContainer} from '../../../../models/TSFinanzielleSituationContainer';
import {TSFinanzModel} from '../../../../models/TSFinanzModel';
import {EbeguUtil} from '../../../../utils/EbeguUtil';
import {GesuchModelManager} from '../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../service/wizardStepManager';
import {AbstractGesuchViewX} from '../../abstractGesuchViewX';

export abstract class AbstractFinSitsolothurnView extends AbstractGesuchViewX<TSFinanzModel> {

    public readonly: boolean = false;

    protected constructor(
        protected gesuchModelManager: GesuchModelManager,
        protected wizardStepManager: WizardStepManager,
        protected gesuchstellerNumber: number,
    ) {
        super(gesuchModelManager, wizardStepManager, TSWizardStepName.FINANZIELLE_SITUATION_SOLOTHURN);
        this.model = new TSFinanzModel(this.gesuchModelManager.getBasisjahr(),
            this.gesuchModelManager.isGesuchsteller2Required(),
            gesuchstellerNumber);
        this.model.copyFinSitDataFromGesuch(this.gesuchModelManager.getGesuch());
        this.setupForm();
    }

    private setupForm(): void {
        if (!this.getModel().finanzielleSituationJA.isNew()) {
            return;
        }
        this.getModel().finanzielleSituationJA.quellenbesteuert = undefined;
        this.getModel().finanzielleSituationJA.gemeinsameStekVorjahr = undefined;
        this.getModel().finanzielleSituationJA.alleinigeStekVorjahr = undefined;
        this.getModel().finanzielleSituationJA.veranlagt = undefined;
    }

    public showSelbstdeklaration(): boolean {
        return EbeguUtil.isNotNullAndTrue(this.getModel().finanzielleSituationJA.quellenbesteuert)
            || EbeguUtil.isNotNullAndFalse(this.getModel().finanzielleSituationJA.gemeinsameStekVorjahr)
            || EbeguUtil.isNotNullAndFalse(this.getModel().finanzielleSituationJA.alleinigeStekVorjahr)
            || EbeguUtil.isNotNullAndFalse(this.getModel().finanzielleSituationJA.veranlagt);
    }

    public showVeranlagung(): boolean {
        return EbeguUtil.isNotNullAndTrue(this.getModel().finanzielleSituationJA.veranlagt);
    }

    public showResultat(): boolean {
        return !this.gesuchModelManager.isGesuchsteller2Required();
    }

    public quellenBesteuertChange(newQuellenBesteuert: MatRadioChange): void {
        if (newQuellenBesteuert.value === false) {
            return;
        }
        this.getModel().finanzielleSituationJA.gemeinsameStekVorjahr = undefined;
        this.getModel().finanzielleSituationJA.alleinigeStekVorjahr = undefined;
        this.getModel().finanzielleSituationJA.veranlagt = undefined;
    }

    public gemeinsameStekVisible(): boolean {
        return this.isGemeinsam() && EbeguUtil.isNotNullAndFalse(this.getModel().finanzielleSituationJA.quellenbesteuert);
    }

    public alleinigeStekVisible(): boolean {
        return !this.isGemeinsam() && EbeguUtil.isNotNullAndFalse(this.getModel().finanzielleSituationJA.quellenbesteuert);
    }

    public veranlagtVisible(): boolean {
        return EbeguUtil.isNotNullAndTrue(this.getModel().finanzielleSituationJA.gemeinsameStekVorjahr)
            || EbeguUtil.isNotNullAndTrue(this.getModel().finanzielleSituationJA.alleinigeStekVorjahr);
    }

    public gemeinsameStekChange(newGemeinsameStek: MatRadioChange): void {
        if (newGemeinsameStek.value === false && EbeguUtil.isNullOrFalse(this.getModel().finanzielleSituationJA.alleinigeStekVorjahr)) {
            this.getModel().finanzielleSituationJA.veranlagt = undefined;
        }
    }

    public alleinigeStekVorjahrChange(newAlleinigeStekVorjahr: MatRadioChange): void {
        if (newAlleinigeStekVorjahr.value === false && EbeguUtil.isNullOrFalse(this.getModel().finanzielleSituationJA.gemeinsameStekVorjahr)) {
            this.getModel().finanzielleSituationJA.veranlagt = undefined;
        }
    }

    public getYearForDeklaration(): number | string {
        const currentYear = this.getBasisjahrPlus1();
        const previousYear = this.getBasisjahr();
        if (this.getModel().finanzielleSituationJA.gemeinsameStekVorjahr) {
            return previousYear;
        }
        return currentYear;
    }

    public abstract isGemeinsam(): boolean;

    public abstract getAntragstellerNummer(): number;

    public hasPrevious(): boolean {
        return true;
    }

    public hasNext(): boolean {
        return true;
    }

    public abstract getSubStepIndex(): number;

    public abstract getSubStepName(): string;

    public abstract prepareSave(onResult: Function): IPromise<TSFinanzielleSituationContainer>;

    public getAntragstellerNameForCurrentStep(): string {
        if (this.getAntragstellerNummer() === 0) {
            return '';
        }
        if (this.getAntragstellerNummer() === 1) {
            return this.gesuchModelManager.getGesuch().gesuchsteller1.extractFullName();
        }
        if (this.getAntragstellerNummer() === 2) {
            try {
                return this.gesuchModelManager.getGesuch().gesuchsteller2.extractFullName();
            } catch (error) {
                // Gesuchsteller has not yet filled in Form for Antragsteller 2
                return '';
            }
        }
        return '';
    }

    public getModel(): TSFinanzielleSituationContainer {
        return this.model.getFiSiConToWorkWith();
    }

    public isGesuchValid(form: NgForm): boolean {
        if (!form.valid) {
            for (const control in form.controls) {
                if (EbeguUtil.isNotNullOrUndefined(form.controls[control])) {
                    form.controls[control].markAsTouched({onlySelf: true});
                }
            }
            EbeguUtil.selectFirstInvalid();
        }
        return form.valid;
    }

    public abstract notify(): void;

    protected save(onResult: Function): Promise<TSFinanzielleSituationContainer> {
        this.model.copyFinSitDataToGesuch(this.gesuchModelManager.getGesuch());
        return this.gesuchModelManager.saveFinanzielleSituation()
            .then(async (finanzielleSituationContainer: TSFinanzielleSituationContainer) => {
                if (!this.isGemeinsam() || this.getAntragstellerNummer() === 2) {
                    await this.updateWizardStepStatus();
                }
                onResult(finanzielleSituationContainer);
                return finanzielleSituationContainer;
            }).catch(error => {
                throw(error);
            }) as Promise<TSFinanzielleSituationContainer>;
    }

    /**
     * updates the Status of the Step depending on whether the Gesuch is a Mutation or not
     */
    protected updateWizardStepStatus(): Promise<void> {
        return this.gesuchModelManager.getGesuch().isMutation() ?
            this.wizardStepManager.updateCurrentWizardStepStatusMutiert() as Promise<void> :
            this.wizardStepManager.updateCurrentWizardStepStatusSafe(
                TSWizardStepName.FINANZIELLE_SITUATION_SOLOTHURN,
                TSWizardStepStatus.OK) as Promise<void>;
    }

    public hasSteuerveranlagungErhalten(): boolean {
        return this.getModel().finanzielleSituationJA.steuerveranlagungErhalten;
    }

    public isSteuerveranlagungGemeinsam(): boolean {
        if (this.gesuchstellerNumber === 2) {
            // this is only saved on the primary GS for Solothurn
            return this.getGesuch().gesuchsteller1.finanzielleSituationContainer.finanzielleSituationJA.gemeinsameStekVorjahr;
        }
        return this.getModel().finanzielleSituationJA.gemeinsameStekVorjahr;
    }

    protected resetVeranlagungSolothurn(): void {
        this.getFinanzielleSituationJA().abzuegeKinderAusbildung = null;
        this.getFinanzielleSituationJA().nettolohn = null;
        this.getFinanzielleSituationJA().unterhaltsBeitraege = null;
        this.getFinanzielleSituationJA().steuerbaresVermoegen = null;
    }

    protected resetVeranlagungSolothurnGS2(): void {
        this.getFinanzielleSituationJAGS2().abzuegeKinderAusbildung = null;
        this.getFinanzielleSituationJAGS2().nettolohn = null;
        this.getFinanzielleSituationJAGS2().unterhaltsBeitraege = null;
        this.getFinanzielleSituationJAGS2().steuerbaresVermoegen = null;
    }

    protected resetBruttoLohn(): void {
        this.getFinanzielleSituationJA().bruttoLohn = null;
    }

    private getFinanzielleSituationJA(): TSFinanzielleSituation {
        return this.getModel().finanzielleSituationJA;
    }

    protected resetBruttoLohnGS2(): void {
        this.getFinanzielleSituationJAGS2().bruttoLohn = null;
    }

    private getFinanzielleSituationJAGS2(): TSFinanzielleSituation {
        return this.model.finanzielleSituationContainerGS2.finanzielleSituationJA;
    }

    public abstract steuerveranlagungErhaltenChange(steuerveranlagungErhalten: boolean): void;
}
