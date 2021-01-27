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

import {ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {Subscription} from 'rxjs';
import {TSLastenausgleichTagesschuleAngabenGemeindeStatus} from '../../../../models/enums/TSLastenausgleichTagesschuleAngabenGemeindeStatus';
import {TSLastenausgleichTagesschuleAngabenGemeindeContainer} from '../../../../models/gemeindeantrag/TSLastenausgleichTagesschuleAngabenGemeindeContainer';
import {LogFactory} from '../../../core/logging/LogFactory';
import {LastenausgleichTSService} from '../../services/lastenausgleich-ts.service';

const LOG = LogFactory.createLog('GemeindeAngabenComponent');

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
        private readonly lastenausgleichTSService: LastenausgleichTSService,
        private readonly ref: ChangeDetectorRef
    ) {}

    public ngOnInit(): void {
        this.subscription = this.lastenausgleichTSService.getLATSAngabenGemeindeContainer()
            .subscribe(container => {
                this.lATSAngabenGemeindeContainer = container;
                this.initForm();
                this.ref.markForCheck();
            }, err => LOG.error(err));
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

    public showAntragErstellen(): boolean {
        return this.lATSAngabenGemeindeContainer?.status === TSLastenausgleichTagesschuleAngabenGemeindeStatus.NEU;
    }
}
