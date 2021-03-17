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

import {ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {TSFerienbetreuungAngaben} from '../../../../models/gemeindeantrag/TSFerienbetreuungAngaben';
import {TSFerienbetreuungAngabenContainer} from '../../../../models/gemeindeantrag/TSFerienbetreuungAngabenContainer';
import {numberValidator, ValidationType} from '../../../shared/validators/number-validator.directive';
import {FerienbetreuungService} from '../services/ferienbetreuung.service';

@Component({
    selector: 'dv-ferienbetreuung-kosten-einnahmen',
    templateUrl: './ferienbetreuung-kosten-einnahmen.component.html',
    styleUrls: ['./ferienbetreuung-kosten-einnahmen.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class FerienbetreuungKostenEinnahmenComponent implements OnInit {

    public form: FormGroup;
    public container: TSFerienbetreuungAngabenContainer;

    public constructor(
        private readonly ferienbetreuungService: FerienbetreuungService,
        private readonly fb: FormBuilder,
        private readonly cd: ChangeDetectorRef
    ) {
    }

    public ngOnInit(): void {
        this.ferienbetreuungService.getFerienbetreuungContainer()
            .subscribe(container => {
                this.container = container;
                const fbAngaben = container.angabenDeklaration;
                this.setupForm(fbAngaben);
                this.cd.markForCheck();
            });
    }

    private setupForm(angaben: TSFerienbetreuungAngaben): void {
        if (!angaben.kostenEinnahmen) {
            return;
        }
        const nutzung = angaben.kostenEinnahmen;
        this.form = this.fb.group({
            personalkosten: [
                nutzung.personalkosten,
                numberValidator(ValidationType.INTEGER)
            ],
            personalkostenLeitungAdmin: [
                nutzung.personalkostenLeitungAdmin,
                numberValidator(ValidationType.INTEGER)
            ],
            sachkosten: [
                nutzung.sachkosten,
                numberValidator(ValidationType.INTEGER)
            ],
            verpflegungskosten: [
                nutzung.verpflegungskosten,
                numberValidator(ValidationType.INTEGER)
            ],
            weitereKosten: [
                nutzung.weitereKosten,
                numberValidator(ValidationType.INTEGER)
            ],
            bemerkungenKosten: [
                nutzung.bemerkungenKosten
            ],
            elterngebuehren: [
                nutzung.elterngebuehren,
                numberValidator(ValidationType.INTEGER)
            ],
            weitereEinnahmen: [
                nutzung.weitereEinnahmen,
                numberValidator(ValidationType.INTEGER)
            ],
        });
    }

    public onFormSubmit(): void {
        // TODO: implement
    }

}
