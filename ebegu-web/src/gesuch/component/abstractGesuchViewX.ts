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

import {AfterViewInit, Directive} from '@angular/core';
import {isAtLeastFreigegeben} from '../../models/enums/TSAntragStatus';
import {TSEingangsart} from '../../models/enums/TSEingangsart';
import {TSFinanzielleSituationTyp} from '../../models/enums/TSFinanzielleSituationTyp';
import {TSWizardStepName} from '../../models/enums/TSWizardStepName';
import {TSAbstractFinanzielleSituation} from '../../models/TSAbstractFinanzielleSituation';
import {TSGesuch} from '../../models/TSGesuch';
import {EbeguUtil} from '../../utils/EbeguUtil';
import {GesuchModelManager} from '../service/gesuchModelManager';
import {WizardStepManager} from '../service/wizardStepManager';

@Directive()
export class AbstractGesuchViewX<T> implements AfterViewInit {

    private _model: T;

    public constructor(
        protected gesuchModelManager: GesuchModelManager,
        protected wizardStepManager: WizardStepManager,
        protected stepName: TSWizardStepName
    ) {
        this.wizardStepManager.setCurrentStep(stepName);
    }

    // reset transitionInProgress after new form is loaded
    public ngAfterViewInit(): void {
        this.wizardStepManager.isTransitionInProgress = false;
    }

    public getBasisjahr(): number | undefined {
        if (this.gesuchModelManager && this.gesuchModelManager.getBasisjahr()) {
            return this.gesuchModelManager.getBasisjahr();
        }
        return undefined;
    }

    public getBasisjahrMinus1(): number | undefined {
        return this.getBasisjahrMinus(1);
    }

    public getBasisjahrMinus2(): number | undefined {
        return this.getBasisjahrMinus(2);
    }

    private getBasisjahrMinus(nbr: number): number | undefined {
        if (this.gesuchModelManager && this.gesuchModelManager.getBasisjahr()) {
            return this.gesuchModelManager.getBasisjahr() - nbr;
        }
        return undefined;
    }

    public getBasisjahrPlus1(): number | undefined {
        return this.getBasisjahrPlus(1);
    }

    public getBasisjahrPlus2(): number | undefined {
        return this.getBasisjahrPlus(2);
    }

    private getBasisjahrPlus(nbr: number): number | undefined {
        if (this.gesuchModelManager && this.gesuchModelManager.getBasisjahrPlus(nbr)) {
            return this.gesuchModelManager.getBasisjahrPlus(nbr);
        }
        return undefined;
    }

    protected getGesuch(): TSGesuch {
        return this.gesuchModelManager.getGesuch();
    }

    public extractFullNameGS1(): string {
        return this.getGesuch() && this.getGesuch().gesuchsteller1
            ? this.getGesuch().gesuchsteller1.extractFullName()
            : '';
    }

    public extractFullNameGS2(): string {
        return this.getGesuch() && this.getGesuch().gesuchsteller2
            ? this.getGesuch().gesuchsteller2.extractFullName()
            : '';
    }

    public get model(): T {
        return this._model;
    }

    public set model(value: T) {
        this._model = value;
    }

    public isKorrekturModusJugendamtOrFreigegeben(): boolean {
        return this.gesuchModelManager.getGesuch()
            && isAtLeastFreigegeben(this.gesuchModelManager.getGesuch().status)
            && (TSEingangsart.ONLINE === this.gesuchModelManager.getGesuch().eingangsart);
    }

    public isFKJV(): boolean {
        return EbeguUtil.isNotNullOrUndefined(this.getGesuch()) && this.getGesuch().finSitTyp === TSFinanzielleSituationTyp.BERN_FKJV;
    }

    public showBisher(abstractFinanzielleSituation: TSAbstractFinanzielleSituation): boolean {
        return (EbeguUtil.isNotNullOrUndefined(abstractFinanzielleSituation))
            && this.isKorrekturModusJugendamtOrFreigegeben();
    }
}
