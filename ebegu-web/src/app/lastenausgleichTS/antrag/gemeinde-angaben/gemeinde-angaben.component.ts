/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {Subscription} from 'rxjs';
import {TSLastenausgleichTagesschuleAngabenGemeindeContainer} from '../../../../models/gemeindeantrag/TSLastenausgleichTagesschuleAngabenGemeindeContainer';
import {LastenausgleichTSService} from '../../services/lastenausgleich-ts.service';

@Component({
    selector: 'dv-gemeinde-angaben',
    templateUrl: './gemeinde-angaben.component.html',
    styleUrls: ['./gemeinde-angaben.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class GemeindeAngabenComponent implements OnInit {

    public lATSAngabenGemeindeContainer: TSLastenausgleichTagesschuleAngabenGemeindeContainer;
    public form: FormGroup;
    private subscription: Subscription;

    public constructor(
        private lastenausgleichTSService: LastenausgleichTSService
    ) {}

    public ngOnInit(): void {
        this.subscription = this.lastenausgleichTSService.getLATSAngabenGemeindeContainer()
            .subscribe(container => {
                this.lATSAngabenGemeindeContainer = container;
                this.initForm();
            });
    }

    public ngOnDestroy(): void {
        this.subscription.unsubscribe();
    }

    public onSubmit(): void {
        if (this.form.valid) {
            this.lATSAngabenGemeindeFuerInstitutionenFreigeben();
        }
    }

    private initForm(): void {
        this.form = new FormGroup({
            alleAngabenInKibonErfasst: new FormControl(
                this.lATSAngabenGemeindeContainer?.alleAngabenInKibonErfasst,
                Validators.required
            )
        });
    }

    private lATSAngabenGemeindeFuerInstitutionenFreigeben(): void {
        this.lATSAngabenGemeindeContainer.alleAngabenInKibonErfasst = this.form.get('alleAngabenInKibonErfasst').value;
        this.lastenausgleichTSService.lATSAngabenGemeindeFuerInstitutionenFreigeben(this.lATSAngabenGemeindeContainer);
    }

}
