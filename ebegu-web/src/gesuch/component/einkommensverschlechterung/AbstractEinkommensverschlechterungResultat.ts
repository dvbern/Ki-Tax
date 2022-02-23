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
import {ChangeDetectorRef} from '@angular/core';
import {Transition} from '@uirouter/core';
import {TSFinanzielleSituationResultateDTO} from '../../../models/dto/TSFinanzielleSituationResultateDTO';
import {TSWizardStepName} from '../../../models/enums/TSWizardStepName';
import {TSFinanzModel} from '../../../models/TSFinanzModel';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {BerechnungsManager} from '../../service/berechnungsManager';
import {GesuchModelManager} from '../../service/gesuchModelManager';
import {WizardStepManager} from '../../service/wizardStepManager';
import {AbstractGesuchViewX} from '../abstractGesuchViewX';
import {EKVViewUtil} from './EKVViewUtil';

export abstract class AbstractEinkommensverschlechterungResultat extends AbstractGesuchViewX<TSFinanzModel> {
    public resultatBasisjahr?: TSFinanzielleSituationResultateDTO;
    public resultatProzent: string;

    public constructor(
        public gesuchModelManager: GesuchModelManager,
        protected wizardStepManager: WizardStepManager,
        protected berechnungsManager: BerechnungsManager,
        protected ref: ChangeDetectorRef,
        protected stepName: TSWizardStepName,
        protected readonly $transition$: Transition,
    ) {
        super(gesuchModelManager, wizardStepManager, stepName);
        const parsedBasisJahrPlusNum = parseInt(this.$transition$.params().basisjahrPlus, 10);
        this.model = new TSFinanzModel(this.gesuchModelManager.getBasisjahr(),
            this.gesuchModelManager.isGesuchsteller2Required(),
            null,
            parsedBasisJahrPlusNum);
        this.model.copyEkvDataFromGesuch(this.gesuchModelManager.getGesuch());
        this.model.copyFinSitDataFromGesuch(this.gesuchModelManager.getGesuch());
        this.gesuchModelManager.setBasisJahrPlusNumber(parsedBasisJahrPlusNum);
        this.calculate();
        this.resultatBasisjahr = null;
        this.calculateResultateVorjahr();
    }

    public calculate(): void {
        if (!this.model || !this.model.getBasisJahrPlus()) {
            console.log('No gesuch and Basisjahr to calculate');
            return;
        }
        this.berechnungsManager.calculateEinkommensverschlechterungTemp(this.model, this.model.getBasisJahrPlus())
            .then(() => {
                this.resultatProzent = this.calculateVeraenderung();
            });
    }

    public calculateVeraenderung(): string {
        if (EbeguUtil.isNotNullOrUndefined(this.resultatBasisjahr)) {
            const resultatJahrPlus1 = this.getResultate();
            if (EbeguUtil.isNotNullOrUndefined(resultatJahrPlus1)) {
                this.berechnungsManager.calculateProzentualeDifferenz(
                    this.resultatBasisjahr.massgebendesEinkVorAbzFamGr, resultatJahrPlus1.massgebendesEinkVorAbzFamGr)
                    .then(abweichungInProzentZumVorjahr => {
                        this.resultatProzent = abweichungInProzentZumVorjahr;
                        this.ref.markForCheck();
                        return abweichungInProzentZumVorjahr;
                    });
            }
        }
        return '';
    }

    public calculateResultateVorjahr(): void {
        this.berechnungsManager.calculateFinanzielleSituationTemp(this.model).then(resultatVorjahr => {
            this.resultatBasisjahr = resultatVorjahr;
            this.resultatProzent = this.calculateVeraenderung();
            this.ref.markForCheck();
        });
    }

    public getResultate(): TSFinanzielleSituationResultateDTO {
        return this.model.getBasisJahrPlus() === 2 ?
            this.berechnungsManager.einkommensverschlechterungResultateBjP2 :
            this.berechnungsManager.einkommensverschlechterungResultateBjP1;
    }

    public hasSecondAntragstellende(): boolean {
        return EbeguUtil.isNotNullOrUndefined(this.gesuchModelManager.getGesuch().gesuchsteller2);
    }

    public getGemeinsameFullname(): string {
        return EKVViewUtil.getGemeinsameFullname(this.gesuchModelManager);
    }

    public getAntragsteller1Name(): string {
        return EKVViewUtil.getAntragsteller1Name(this.gesuchModelManager);
    }
}
