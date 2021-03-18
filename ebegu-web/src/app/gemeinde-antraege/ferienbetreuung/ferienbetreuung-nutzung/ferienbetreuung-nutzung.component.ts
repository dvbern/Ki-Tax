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
import {TranslateService} from '@ngx-translate/core';
import {TSFerienbetreuungAngabenContainer} from '../../../../models/gemeindeantrag/TSFerienbetreuungAngabenContainer';
import {TSFerienbetreuungAngabenNutzung} from '../../../../models/gemeindeantrag/TSFerienbetreuungAngabenNutzung';
import {ErrorService} from '../../../core/errors/service/ErrorService';
import {LogFactory} from '../../../core/logging/LogFactory';
import {numberValidator, ValidationType} from '../../../shared/validators/number-validator.directive';
import {FerienbetreuungService} from '../services/ferienbetreuung.service';

const LOG = LogFactory.createLog('FerienbetreuungNutzungComponent');

@Component({
    selector: 'dv-ferienbetreuung-nutzung',
    templateUrl: './ferienbetreuung-nutzung.component.html',
    styleUrls: ['./ferienbetreuung-nutzung.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class FerienbetreuungNutzungComponent implements OnInit {

    public form: FormGroup;

    private nutzung: TSFerienbetreuungAngabenNutzung;
    private container: TSFerienbetreuungAngabenContainer;

    public constructor(
        private readonly ferienbetreuungService: FerienbetreuungService,
        private readonly fb: FormBuilder,
        private readonly cd: ChangeDetectorRef,
        private readonly errorService: ErrorService,
        private readonly translate: TranslateService
    ) {
    }

    public ngOnInit(): void {
        this.ferienbetreuungService.getFerienbetreuungContainer()
            .subscribe(container => {
                this.container = container;
                this.nutzung = container.angabenDeklaration?.nutzung;
                this.setupForm(this.nutzung);
                this.cd.markForCheck();
            });
    }

    private setupForm(nutzung: TSFerienbetreuungAngabenNutzung): void {
        if (!nutzung) {
            return;
        }
        this.form = this.fb.group({
            anzahlBetreuungstageKinderBern: [
                nutzung.anzahlBetreuungstageKinderBern,
                numberValidator(ValidationType.HALF)
            ],
            betreuungstageKinderDieserGemeinde: [
                nutzung.betreuungstageKinderDieserGemeinde,
                numberValidator(ValidationType.HALF)
            ],
            betreuungstageKinderDieserGemeindeSonderschueler: [
                nutzung.betreuungstageKinderDieserGemeindeSonderschueler,
                numberValidator(ValidationType.HALF)
            ],
            davonBetreuungstageKinderAndererGemeinden: [
                nutzung.davonBetreuungstageKinderAndererGemeinden,
                numberValidator(ValidationType.HALF)
            ],
            davonBetreuungstageKinderAndererGemeindenSonderschueler: [
                nutzung.davonBetreuungstageKinderAndererGemeindenSonderschueler,
                numberValidator(ValidationType.HALF)
            ],
            anzahlBetreuteKinder: [
                nutzung.anzahlBetreuteKinder,
                numberValidator(ValidationType.INTEGER)
            ],
            anzahlBetreuteKinderSonderschueler: [
                nutzung.anzahlBetreuteKinderSonderschueler,
                numberValidator(ValidationType.INTEGER)
            ],
            anzahlBetreuteKinder1Zyklus: [
                nutzung.anzahlBetreuteKinder1Zyklus,
                numberValidator(ValidationType.INTEGER)
            ],
            anzahlBetreuteKinder2Zyklus: [
                nutzung.anzahlBetreuteKinder2Zyklus,
                numberValidator(ValidationType.INTEGER)
            ],
            anzahlBetreuteKinder3Zyklus: [
                nutzung.anzahlBetreuteKinder3Zyklus,
                numberValidator(ValidationType.INTEGER)
            ],
        });
    }

    public save(): void {
        this.ferienbetreuungService.saveNutzung(this.container.id, this.extractFormValues())
            .subscribe(() => {
                this.ferienbetreuungService.updateFerienbetreuungContainerStore(this.container.id);
                this.errorService.addMesageAsInfo(this.translate.instant('SPEICHERN_ERFOLGREICH'));
            }, err => {
                LOG.error(err);
                this.errorService.addMesageAsError(this.translate.instant('FERIENBETREUUNG_PERSIST_ERROR'));
            });
    }

    private extractFormValues(): TSFerienbetreuungAngabenNutzung {
        this.nutzung.anzahlBetreuungstageKinderBern = this.form.controls.anzahlBetreuungstageKinderBern.value;
        this.nutzung.betreuungstageKinderDieserGemeinde = this.form.controls.betreuungstageKinderDieserGemeinde.value;
        this.nutzung.betreuungstageKinderDieserGemeindeSonderschueler =
            this.form.controls.betreuungstageKinderDieserGemeindeSonderschueler.value;
        this.nutzung.davonBetreuungstageKinderAndererGemeinden =
            this.form.controls.davonBetreuungstageKinderAndererGemeinden.value;
        this.nutzung.davonBetreuungstageKinderAndererGemeindenSonderschueler =
            this.form.controls.davonBetreuungstageKinderAndererGemeindenSonderschueler.value;
        this.nutzung.anzahlBetreuteKinder = this.form.controls.anzahlBetreuteKinder.value;
        this.nutzung.anzahlBetreuteKinderSonderschueler = this.form.controls.anzahlBetreuteKinderSonderschueler.value;
        this.nutzung.anzahlBetreuteKinder1Zyklus = this.form.controls.anzahlBetreuteKinder1Zyklus.value;
        this.nutzung.anzahlBetreuteKinder2Zyklus = this.form.controls.anzahlBetreuteKinder2Zyklus.value;
        this.nutzung.anzahlBetreuteKinder3Zyklus = this.form.controls.anzahlBetreuteKinder3Zyklus.value;
        return this.nutzung;
    }
}
