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
import {TSFinanzielleSituationResultateDTO} from '../../../../models/dto/TSFinanzielleSituationResultateDTO';
import {TSTaetigkeit} from '../../../../models/enums/TSTaetigkeit';
import {TSWizardStepName} from '../../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../../models/enums/TSWizardStepStatus';
import {TSFinanzielleSituation} from '../../../../models/TSFinanzielleSituation';
import {TSFinanzielleSituationContainer} from '../../../../models/TSFinanzielleSituationContainer';
import {TSFinanzModel} from '../../../../models/TSFinanzModel';
import {EbeguUtil} from '../../../../utils/EbeguUtil';
import {GesuchModelManager} from '../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../service/wizardStepManager';
import {AbstractGesuchViewX} from '../../abstractGesuchViewX';
import {FinanzielleSituationSolothurnService} from './finanzielle-situation-solothurn.service';

export abstract class AbstractFinSitsolothurnView extends AbstractGesuchViewX<TSFinanzModel> {

    public readonly: boolean = false;
    public finanzielleSituationResultate?: TSFinanzielleSituationResultateDTO;

    public constructor(
        protected gesuchModelManager: GesuchModelManager,
        protected wizardStepManager: WizardStepManager,
        protected readonly finSitSoService: FinanzielleSituationSolothurnService,
        protected gesuchstellerNumber: number,
    ) {
        super(gesuchModelManager, wizardStepManager, TSWizardStepName.FINANZIELLE_SITUATION_SOLOTHURN);
        this.model = new TSFinanzModel(this.gesuchModelManager.getBasisjahr(),
            this.gesuchModelManager.isGesuchsteller2Required(),
            gesuchstellerNumber);
        this.model.copyFinSitDataFromGesuch(this.gesuchModelManager.getGesuch());
        this.setupForm();
        this.calculateMassgebendesEinkommen();
        this.gesuchModelManager.setGesuchstellerNumber(gesuchstellerNumber);
    }

    private setupForm(): void {
        if (!this.getModel().finanzielleSituationJA.isNew()) {
            return;
        }
        this.getModel().finanzielleSituationJA.quellenbesteuert = undefined;
        this.getModel().finanzielleSituationJA.veranlagt = undefined;
    }

    public showSelbstdeklaration(): boolean {
        return EbeguUtil.isNotNullAndTrue(this.getModel().finanzielleSituationJA.quellenbesteuert)
            || EbeguUtil.isNotNullAndFalse(this.model.gemeinsameSteuererklaerung)
            || EbeguUtil.isNotNullAndFalse(this.getModel().finanzielleSituationJA.alleinigeStekVorjahr)
            || EbeguUtil.isNotNullAndFalse(this.getModel().finanzielleSituationJA.veranlagt);
    }

    public showVeranlagung(): boolean {
        return EbeguUtil.isNotNullAndTrue(this.getModel().finanzielleSituationJA.veranlagt);
    }

    public showResultat(): boolean {
        return !this.gesuchModelManager.isGesuchsteller2Required();
    }

    public gemeinsameStekVisible(): boolean {
        return this.isGemeinsam() && EbeguUtil.isNotNullAndFalse(this.getModel().finanzielleSituationJA.quellenbesteuert);
    }

    public alleinigeStekVisible(): boolean {
        return !this.isGemeinsam() && EbeguUtil.isNotNullAndFalse(this.getModel().finanzielleSituationJA.quellenbesteuert);
    }

    public gemeinsameStekChange(newGemeinsameStek: MatRadioChange): void {
        if (newGemeinsameStek.value === false && EbeguUtil.isNullOrFalse(this.getModel().finanzielleSituationJA.alleinigeStekVorjahr)) {
            this.getModel().finanzielleSituationJA.veranlagt = undefined;
        }
    }

    public alleinigeStekVorjahrChange(newAlleinigeStekVorjahr: MatRadioChange): void {
        if (newAlleinigeStekVorjahr.value === false && EbeguUtil.isNullOrFalse(this.model.gemeinsameSteuererklaerung)) {
            this.getModel().finanzielleSituationJA.veranlagt = undefined;
        }
    }

    public getYearForDeklaration(): number | string {
        const currentYear = this.getBasisjahrPlus1();
        const previousYear = this.getBasisjahr();
        if (this.model.gemeinsameSteuererklaerung) {
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
        const status = this.isFinSitOk() ? TSWizardStepStatus.OK : TSWizardStepStatus.NOK;
        return this.gesuchModelManager.getGesuch().isMutation() ?
            this.wizardStepManager.updateCurrentWizardStepStatusMutiert() as Promise<void> :
            this.wizardStepManager.updateCurrentWizardStepStatusSafe(
                TSWizardStepName.FINANZIELLE_SITUATION_SOLOTHURN,
                status) as Promise<void>;
    }

    private isFinSitOk(): boolean {
        return this.isGemeinsam() ? this.isGs2Ok() : this.isStartOk();
    }

    private isGs2Ok(): boolean {
        const gs2 = this.getGesuch().gesuchsteller2.finanzielleSituationContainer.finanzielleSituationJA;
        const isGs2Ok = gs2.steuerveranlagungErhalten ?
            EbeguUtil.areAllNotNullOrUndefined(gs2.nettolohn,
                gs2.unterhaltsBeitraege,
                gs2.abzuegeKinderAusbildung,
                gs2.steuerbaresVermoegen) :
            EbeguUtil.isNotNullOrUndefined(gs2.bruttoLohn);
        return isGs2Ok;
    }

    private isStartOk(): boolean {
        const finanzielleSituationJA = this.getModel().finanzielleSituationJA;
        const isStartOk = finanzielleSituationJA.steuerveranlagungErhalten ?
            EbeguUtil.areAllNotNullOrUndefined(
                finanzielleSituationJA.nettolohn,
                finanzielleSituationJA.unterhaltsBeitraege,
                finanzielleSituationJA.abzuegeKinderAusbildung,
                finanzielleSituationJA.steuerbaresVermoegen,
            ) :
            EbeguUtil.isNotNullOrUndefined(finanzielleSituationJA.bruttoLohn);
        return isStartOk;
    }

    public hasSteuerveranlagungErhalten(): boolean {
        if (this.gesuchstellerNumber === 2 && this.isSteuerveranlagungGemeinsam()) {
            // this is only saved on the primary GS for Solothurn
            return this.getGesuch().gesuchsteller1.finanzielleSituationContainer.finanzielleSituationJA.steuerveranlagungErhalten;
        }
        return this.getModel().finanzielleSituationJA.steuerveranlagungErhalten;
    }

    public isSteuerveranlagungGemeinsam(): boolean {
        return this.model.gemeinsameSteuererklaerung;
    }

    protected resetVeranlagungSolothurn(): void {
        this.getFinanzielleSituationJA().abzuegeKinderAusbildung = null;
        this.getFinanzielleSituationJA().nettolohn = null;
        this.getFinanzielleSituationJA().unterhaltsBeitraege = null;
        this.getFinanzielleSituationJA().steuerbaresVermoegen = null;
        this.calculateMassgebendesEinkommen();
    }

    protected resetVeranlagungSolothurnGS2(): void {
        this.getFinanzielleSituationJAGS2().abzuegeKinderAusbildung = null;
        this.getFinanzielleSituationJAGS2().nettolohn = null;
        this.getFinanzielleSituationJAGS2().unterhaltsBeitraege = null;
        this.getFinanzielleSituationJAGS2().steuerbaresVermoegen = null;
        this.calculateMassgebendesEinkommen();
    }

    protected resetBruttoLohn(): void {
        this.getFinanzielleSituationJA().bruttoLohn = null;
        this.calculateMassgebendesEinkommen();
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

    public onValueChangeFunction = (): void => {
        this.calculateMassgebendesEinkommen();
    }

    protected calculateMassgebendesEinkommen(): void {
        this.finSitSoService.calculateMassgebendesEinkommen(this.model);
    }

    public abstract steuerveranlagungErhaltenChange(steuerveranlagungErhalten: boolean): void;

    public isSelbstraendig(): boolean {
        if (this.getAntragstellerNummer() === 1) {
            return this.gesuchModelManager.getGesuch().gesuchsteller1.erwerbspensenContainer.filter(
                erwerbspensum => erwerbspensum.erwerbspensumJA.taetigkeit === TSTaetigkeit.SELBSTAENDIG,
            ).length > 0;
        }
        if (this.getAntragstellerNummer() === 2) {
            try {
                return this.gesuchModelManager.getGesuch().gesuchsteller2.erwerbspensenContainer.filter(
                    erwerbspensum => erwerbspensum.erwerbspensumJA.taetigkeit === TSTaetigkeit.SELBSTAENDIG,
                ).length > 0;
            } catch (error) {
                // Gesuchsteller has not yet filled in Form for Antragsteller 2
                return false;
            }
        }
        return false;
    }
}
