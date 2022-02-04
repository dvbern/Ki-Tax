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

import {NgForm} from '@angular/forms';
import {MatRadioChange} from '@angular/material/radio';
import {IPromise} from 'angular';
import {TSWizardStepName} from '../../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../../models/enums/TSWizardStepStatus';
import {TSFinanzielleSituationContainer} from '../../../../models/TSFinanzielleSituationContainer';
import {TSFinanzielleSituationSelbstdeklaration} from '../../../../models/TSFinanzielleSituationSelbstdeklaration';
import {TSFinanzModel} from '../../../../models/TSFinanzModel';
import {EbeguUtil} from '../../../../utils/EbeguUtil';
import {GesuchModelManager} from '../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../service/wizardStepManager';
import {AbstractGesuchViewX} from '../../abstractGesuchViewX';
import {FinanzielleSituationLuzernService} from './finanzielle-situation-luzern.service';

export abstract class AbstractFinSitLuzernView extends AbstractGesuchViewX<TSFinanzModel> {

    public readonly: boolean = false;

    protected constructor(
        protected gesuchModelManager: GesuchModelManager,
        protected wizardStepManager: WizardStepManager,
        protected gesuchstellerNumber: number,
        protected finSitLuService: FinanzielleSituationLuzernService = finSitLuService,
    ) {
        super(gesuchModelManager, wizardStepManager, TSWizardStepName.FINANZIELLE_SITUATION_LUZERN);
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
        this.getModel().finanzielleSituationJA.selbstdeklaration = new TSFinanzielleSituationSelbstdeklaration();
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
        if (newGemeinsameStek.value === true) {
            this.getModel().finanzielleSituationJA.selbstdeklaration = new TSFinanzielleSituationSelbstdeklaration();
        }
    }

    public alleinigeStekVorjahrChange(newAlleinigeStekVorjahr: MatRadioChange): void {
        if (newAlleinigeStekVorjahr.value === false && EbeguUtil.isNullOrFalse(this.getModel().finanzielleSituationJA.gemeinsameStekVorjahr)) {
            this.getModel().finanzielleSituationJA.veranlagt = undefined;
        }
        if (newAlleinigeStekVorjahr.value === true) {
            this.getModel().finanzielleSituationJA.selbstdeklaration = new TSFinanzielleSituationSelbstdeklaration();
        }
    }

    public veranlagtChange(newVeranlagt: MatRadioChange): void {
        if (newVeranlagt.value === true) {
            this.getModel().finanzielleSituationJA.selbstdeklaration = new TSFinanzielleSituationSelbstdeklaration();
        }
        if (newVeranlagt.value === false) {
            this.resetVeranlagungValues();
        }
    }

    private resetVeranlagungValues(): void {
        this.getModel().finanzielleSituationJA.steuerbaresEinkommen = undefined;
        this.getModel().finanzielleSituationJA.steuerbaresVermoegen = undefined;
        this.getModel().finanzielleSituationJA.abzuegeLiegenschaft = undefined;
        this.getModel().finanzielleSituationJA.geschaeftsverlust = undefined;
        this.getModel().finanzielleSituationJA.einkaeufeVorsorge = undefined;
        this.finSitLuService.calculateMassgebendesEinkommen(this.model);
    }

    public getYearForDeklaration(): number | string {
        const currentYear = this.getBasisjahrPlus1();
        const previousYear = this.getBasisjahr();
        if (this.getModel().finanzielleSituationJA.quellenbesteuert) {
            return previousYear;
        }
        if (EbeguUtil.isNotNullOrUndefined(this.getModel().finanzielleSituationJA.veranlagt)) {
            return previousYear;
        }
        if (EbeguUtil.isNotNullAndFalse(this.getModel().finanzielleSituationJA.gemeinsameStekVorjahr)
            || EbeguUtil.isNotNullAndFalse(this.getModel().finanzielleSituationJA.alleinigeStekVorjahr)) {
            return currentYear;
        }
        return '';
    }

    public abstract isGemeinsam(): boolean;

    public abstract getAntragstellerNummer(): number;

    public showInfomaFields(): boolean {
        return this.getAntragstellerNummer() === 1;
    }

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
        if (this.isGemeinsam()) {
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

    protected abstract save(onResult: Function): IPromise<TSFinanzielleSituationContainer>;

    public getAntragsteller2Name(): string {
        return this.gesuchModelManager.getGesuch().gesuchsteller2?.extractFullName();
    }

    /**
     * updates the Status of the Step depending on whether the Gesuch is a Mutation or not
     */
    protected updateWizardStepStatus(): IPromise<void> {
        return this.gesuchModelManager.getGesuch().isMutation() ?
            this.wizardStepManager.updateCurrentWizardStepStatusMutiert() :
            this.wizardStepManager.updateCurrentWizardStepStatusSafe(
                TSWizardStepName.FINANZIELLE_SITUATION_LUZERN,
                TSWizardStepStatus.OK);
    }
}
