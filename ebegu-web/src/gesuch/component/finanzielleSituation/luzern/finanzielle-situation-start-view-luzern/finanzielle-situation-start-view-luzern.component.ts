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

import {ChangeDetectionStrategy, Component, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {IPromise} from 'angular';
import {TSFinanzielleSituationSubStepName} from '../../../../../models/enums/TSFinanzielleSituationSubStepName';
import {TSWizardStepName} from '../../../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../../../models/enums/TSWizardStepStatus';
import {TSFinanzielleSituationContainer} from '../../../../../models/TSFinanzielleSituationContainer';
import {TSGesuch} from '../../../../../models/TSGesuch';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../../service/wizardStepManager';
import {AbstractFinSitLuzernView} from '../AbstractFinSitLuzernView';
import {FinanzielleSituationLuzernService} from '../finanzielle-situation-luzern.service';
import {ResultatComponent} from '../resultat/resultat.component';

@Component({
    selector: 'dv-finanzielle-situation-start-view-luzern',
    templateUrl: '../finanzielle-situation-luzern.component.html',
    styleUrls: ['../finanzielle-situation-luzern.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FinanzielleSituationStartViewLuzernComponent extends AbstractFinSitLuzernView {

    @ViewChild(NgForm) private readonly form: NgForm;
    @ViewChild(ResultatComponent) private readonly resultatComponent: ResultatComponent;

    public constructor(
        protected gesuchModelManager: GesuchModelManager,
        protected wizardStepManager: WizardStepManager,
    ) {
        super(gesuchModelManager, wizardStepManager, 1);
        this.wizardStepManager.updateCurrentWizardStepStatusSafe(
            TSWizardStepName.FINANZIELLE_SITUATION_LUZERN,
            TSWizardStepStatus.IN_BEARBEITUNG);
    }

    public isGemeinsam(): boolean {
        // if we don't need two antragsteller for gesuch, this is the component for both antragsteller together
        return !FinanzielleSituationLuzernService.finSitNeedsTwoAntragsteller(this.gesuchModelManager);
    }

    public getAntragstellerNummer(): number {
        // this is always antragsteller 1. if we have two antragsteller, we have angaben-gesuchsteller-2 component
        return 1;
    }

    public getTrue(): any {
        return true;
    }

    public getSubStepIndex(): number {
        return 1;
    }

    public getSubStepName(): TSFinanzielleSituationSubStepName {
        return TSFinanzielleSituationSubStepName.LUZERN_START;
    }

    public prepareSave(onResult: Function): IPromise<TSFinanzielleSituationContainer> {
        if (!this.isGesuchValid(this.form)) {
            onResult(undefined);
            return undefined;
        }
        return this.save(onResult);
    }

    protected save(onResult: Function): angular.IPromise<TSFinanzielleSituationContainer> {
        this.model.copyFinSitDataToGesuch(this.gesuchModelManager.getGesuch());
        return this.gesuchModelManager.saveFinanzielleSituationStart()
            .then((gesuch: TSGesuch) => {
                if (this.isGemeinsam()) {
                    this.updateWizardStepStatus();
                }
                onResult(gesuch.gesuchsteller1.finanzielleSituationContainer);
                return gesuch.gesuchsteller1.finanzielleSituationContainer;
            }).catch(error => {
                throw(error);
            });
    }

    public notify(): void {
        if (this.showResultat()) {
            this.resultatComponent.calculate();
        }
    }
}
