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

import {ChangeDetectionStrategy, ChangeDetectorRef, Component} from '@angular/core';
import {Transition} from '@uirouter/core';
import {LogFactory} from '../../../../../app/core/logging/LogFactory';
import {TSFinanzielleSituationResultateDTO} from '../../../../../models/dto/TSFinanzielleSituationResultateDTO';
import {TSFinanzModel} from '../../../../../models/TSFinanzModel';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../../service/wizardStepManager';
import {FinanzielleSituationLuzernService} from '../../../finanzielleSituation/luzern/finanzielle-situation-luzern.service';
import {AbstractEKVLuzernView} from '../AbstractEKVLuzernView';

const LOG = LogFactory.createLog('EinkommensverschlechterungLuzernResultateViewComponent');

@Component({
    selector: 'dv-einkommensverschlechterung-luzern-resultate-view',
    templateUrl: './einkommensverschlechterung-luzern-resultate-view.component.html',
    styleUrls: ['./einkommensverschlechterung-luzern-resultate-view.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EinkommensverschlechterungLuzernResultateViewComponent extends AbstractEKVLuzernView {

    public resultatBasisjahr?: TSFinanzielleSituationResultateDTO;
    public resultatProzent: string;

    public resultate?: TSFinanzielleSituationResultateDTO;

    public constructor(
        protected gesuchModelManager: GesuchModelManager,
        protected wizardStepManager: WizardStepManager,
        protected finSitLuService: FinanzielleSituationLuzernService,
        protected ref: ChangeDetectorRef,
        private readonly $transition$: Transition,
    ) {
        super(gesuchModelManager, wizardStepManager);
        const parsedBasisJahrPlusNum = parseInt(this.$transition$.params().basisjahrPlus, 10);
        this.model = new TSFinanzModel(this.gesuchModelManager.getBasisjahr(),
            this.gesuchModelManager.isGesuchsteller2Required(),
            null,
            parsedBasisJahrPlusNum);
        this.model.copyEkvDataFromGesuch(this.gesuchModelManager.getGesuch());
        this.model.copyFinSitDataFromGesuch(this.gesuchModelManager.getGesuch());
        this.gesuchModelManager.setBasisJahrPlusNumber(parsedBasisJahrPlusNum);
        this.finSitLuService.massgebendesEinkommenStore.subscribe(resultate => {
                this.resultate = resultate;
                this.resultatProzent = this.calculateVeraenderung();
                this.ref.markForCheck();
            }, error => LOG.error(error),
        );
        this.calculate();
        this.resultatBasisjahr = null;
        this.calculateResultateVorjahr();
    }

    public calculate(): void {
        if (!this.model || !this.model.getBasisJahrPlus()) {
            console.log('No gesuch and Basisjahr to calculate');
            return;
        }
        this.finSitLuService.calculateEinkommensverschlechterung(this.model, this.model.getBasisJahrPlus());
    }

    public calculateVeraenderung(): string {
        if (this.resultatBasisjahr) {
            const resultatJahrPlus1 = this.finSitLuService.getResultate(this.model);
            if (resultatJahrPlus1) {
                this.finSitLuService.calculateProzentualeDifferenz(
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
        this.finSitLuService.calculateResultateVorjahr(this.model).then(resultatVorjahr => {
            this.resultatBasisjahr = resultatVorjahr;
            this.resultatProzent = this.calculateVeraenderung();
            this.ref.markForCheck();
        });
    }
}
