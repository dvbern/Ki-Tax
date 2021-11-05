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

import {FormBuilder, FormGroup} from '@angular/forms';
import {TSWizardStepName} from '../../../../models/enums/TSWizardStepName';
import {EbeguUtil} from '../../../../utils/EbeguUtil';
import {GesuchModelManager} from '../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../service/wizardStepManager';
import {AbstractGesuchViewX} from '../../abstractGesuchViewX';

export abstract class AbstractFinSitLuzernView extends AbstractGesuchViewX {

    public form: FormGroup;

    public constructor(
        protected gesuchModelManager: GesuchModelManager,
        protected wizardStepManager: WizardStepManager,
        private readonly fb: FormBuilder
    ) {
        super(gesuchModelManager, wizardStepManager, TSWizardStepName.FINANZIELLE_SITUATION_LUZERN);
        this.setupForm();
    }

    private setupForm(): void {
        this.form = this.fb.group({
            quellenbesteuert: null,
            gemeinsameStekVorjahr: null,
            alleinigeStekVorjahr: null,
            veranlagt: null
        });
    }

    public showSelbstdeklaration(): boolean {
        return this.form.controls.quellenbesteuert.value === true
            || this.form.controls.gemeinsameStekVorjahr.value === false
            || this.form.controls.alleinigeStekVorjahr.value === false
            || this.form.controls.veranlagt.value === false;
    }

    public showVeranlagung(): boolean {
        return this.form.controls.veranlagt.value === true;
    }

    public gemeinsameStekVisible(): boolean {
        return this.isGemeinsam() && this.form.controls.quellenbesteuert.value === false;
    }

    public alleinigeStekVisible(): boolean {
        return !this.isGemeinsam() && this.form.controls.quellenbesteuert.value === false;
    }

    public veranlagtVisible(): boolean {
        return this.form.controls.gemeinsameStekVorjahr.value === true
        || this.form.controls.alleinigeStekVorjahr.value === true;
    }

    public getYearForDeklaration(): number {
        const currentYear = this.getBasisjahrPlus1();
        const previousYear = this.getBasisjahr();
        if (this.form.controls.quellenbesteuert.value === true) {
            return previousYear;
        }
        if (EbeguUtil.isNotNullOrUndefined(this.form.controls.veranlagt.value)) {
            return previousYear;
        }
        if (this.form.controls.gemeinsameStekVorjahr.value === false
            || this.form.controls.alleinigeStekVorjahr.value === false) {
            return currentYear;
        }
        throw new Error('Dieser Fall ist nicht abgedeckt: ' + JSON.stringify(this.form.value));
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

    // must return a promise to make dv-navigation work
    public abstract save(): Promise<any>;
}
