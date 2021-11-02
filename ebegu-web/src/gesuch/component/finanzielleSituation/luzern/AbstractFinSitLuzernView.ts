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
import {GesuchModelManager} from '../../../service/gesuchModelManager';
import {AbstractGesuchViewX} from '../../abstractGesuchViewX';

export abstract class AbstractFinSitLuzernView extends AbstractGesuchViewX {

    public form: FormGroup;

    public constructor(
        protected gesuchModelManager: GesuchModelManager,
        private readonly fb: FormBuilder,
    ) {
        super(gesuchModelManager);
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
        if (this.form.controls.veranlagt.value === true) {
            return currentYear;
        }
        if (this.form.controls.veranlagt.value === false) {
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
}
