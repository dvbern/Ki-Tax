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

import {MatRadioChange} from '@angular/material/radio';
import {IPromise} from 'angular';
import {TSWizardStepName} from '../../../../models/enums/TSWizardStepName';
import {TSFinanzielleSituationContainer} from '../../../../models/TSFinanzielleSituationContainer';
import {TSFinanzModel} from '../../../../models/TSFinanzModel';
import {EbeguUtil} from '../../../../utils/EbeguUtil';
import {GesuchModelManager} from '../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../service/wizardStepManager';
import {AbstractGesuchViewX} from '../../abstractGesuchViewX';

export abstract class AbstractFinSitLuzernView extends AbstractGesuchViewX<TSFinanzModel> {

    public readonly: boolean = false;

    protected constructor(
        protected gesuchModelManager: GesuchModelManager,
        protected wizardStepManager: WizardStepManager,
    ) {
        super(gesuchModelManager, wizardStepManager, TSWizardStepName.FINANZIELLE_SITUATION_LUZERN);
        this.model = new TSFinanzModel(this.gesuchModelManager.getBasisjahr(),
            this.gesuchModelManager.isGesuchsteller2Required(),
            null);
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
        return this.getModel().finanzielleSituationJA.quellenbesteuert
            || this.getModel().finanzielleSituationJA.gemeinsameStekVorjahr
            || this.getModel().finanzielleSituationJA.alleinigeStekVorjahr
            || this.getModel().finanzielleSituationJA.veranlagt;
    }

    public showVeranlagung(): boolean {
        return this.getModel().finanzielleSituationJA.veranlagt;
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
        return this.isGemeinsam() && this.getModel().finanzielleSituationJA.quellenbesteuert;
    }

    public alleinigeStekVisible(): boolean {
        return !this.isGemeinsam() && this.getModel().finanzielleSituationJA.quellenbesteuert;
    }

    public veranlagtVisible(): boolean {
        return this.model.gemeinsameSteuererklaerung
            || this.getModel().finanzielleSituationJA.alleinigeStekVorjahr;
    }

    public gemeinsameStekChange(newGemeinsameStek: MatRadioChange): void {
        if (newGemeinsameStek.value === false && !this.getModel().finanzielleSituationJA.alleinigeStekVorjahr) {
            this.getModel().finanzielleSituationJA.veranlagt = undefined;
        }
    }

    public alleinigeStekVorjahrChange(newAlleinigeStekVorjahr: MatRadioChange): void {
        if (newAlleinigeStekVorjahr.value === false && this.getModel().finanzielleSituationJA.gemeinsameStekVorjahr) {
            this.getModel().finanzielleSituationJA.veranlagt = undefined;
        }
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
        if (this.getModel().finanzielleSituationJA.gemeinsameStekVorjahr
            || this.getModel().finanzielleSituationJA.alleinigeStekVorjahr) {
            return currentYear;
        }
        return '';
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

    protected save(): IPromise<TSFinanzielleSituationContainer> {
        this.model.copyFinSitDataToGesuch(this.gesuchModelManager.getGesuch());
        return this.gesuchModelManager.saveFinanzielleSituation()
            .then((finanzielleSituationContainer: TSFinanzielleSituationContainer) => {
                return finanzielleSituationContainer;
            }).catch(error => {
                throw(error);
            });
    }
}
