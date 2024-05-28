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

import {ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit} from '@angular/core';
import {FormBuilder, Validators} from '@angular/forms';
import {MatDialog} from '@angular/material/dialog';
import {TranslateService} from '@ngx-translate/core';
import {UIRouterGlobals} from '@uirouter/core';
import {combineLatest, Observable, Subject} from 'rxjs';
import {takeUntil} from 'rxjs/operators';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {TSFerienbetreuungAngabenContainer} from '../../../../models/gemeindeantrag/TSFerienbetreuungAngabenContainer';
import {TSFerienbetreuungAngabenNutzung} from '../../../../models/gemeindeantrag/TSFerienbetreuungAngabenNutzung';
import {ErrorService} from '../../../core/errors/service/ErrorService';
import {LogFactory} from '../../../core/logging/LogFactory';
import {WizardStepXRS} from '../../../core/service/wizardStepXRS.rest';
import {numberValidator, ValidationType} from '../../../shared/validators/number-validator.directive';
import {UnsavedChangesService} from '../../services/unsaved-changes.service';
import {AbstractFerienbetreuungFormular} from '../abstract.ferienbetreuung-formular';
import {FerienbetreuungService} from '../services/ferienbetreuung.service';

const LOG = LogFactory.createLog('FerienbetreuungNutzungComponent');

@Component({
    selector: 'dv-ferienbetreuung-nutzung',
    templateUrl: './ferienbetreuung-nutzung.component.html',
    styleUrls: ['./ferienbetreuung-nutzung.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class FerienbetreuungNutzungComponent extends AbstractFerienbetreuungFormular implements OnInit, OnDestroy {

    private nutzung: TSFerienbetreuungAngabenNutzung;
    public vorgaenger$: Observable<TSFerienbetreuungAngabenContainer>;
    private readonly unsubscribe$ = new Subject();

    public form = this.fb.group({
        anzahlBetreuungstageKinderBern: [<null | number> null],
        betreuungstageKinderDieserGemeinde: [<null | number> null],
        betreuungstageKinderDieserGemeindeSonderschueler: [<null | number> null],
        davonBetreuungstageKinderAndererGemeinden: [<null | number> null],
        davonBetreuungstageKinderAndererGemeindenSonderschueler: [<null | number> null],
        anzahlBetreuteKinder: [<null | number> null],
        anzahlBetreuteKinderSonderschueler: [<null | number> null],
        anzahlBetreuteKinder1Zyklus: [<null | number> null],
        anzahlBetreuteKinder2Zyklus: [<null | number> null],
        anzahlBetreuteKinder3Zyklus: [<null | number> null]
    });

    public constructor(
        protected readonly errorService: ErrorService,
        protected readonly translate: TranslateService,
        protected readonly dialog: MatDialog,
        private readonly ferienbetreuungService: FerienbetreuungService,
        protected readonly cd: ChangeDetectorRef,
        protected readonly wizardRS: WizardStepXRS,
        protected readonly uiRouterGlobals: UIRouterGlobals,
        private readonly fb: FormBuilder,
        private readonly authService: AuthServiceRS,
        private readonly unsavedChangesService: UnsavedChangesService
    ) {
        super(errorService, translate, dialog, cd, wizardRS, uiRouterGlobals);
    }

    public ngOnInit(): void {
        combineLatest([
            this.ferienbetreuungService.getFerienbetreuungContainer(),
            this.authService.principal$
        ]).pipe(
            takeUntil(this.unsubscribe$)
        ).subscribe(([container, principal]) => {
            this.container = container;
            this.nutzung = container.isAtLeastInPruefungKantonOrZurueckgegeben() ?
                container.angabenKorrektur?.nutzung : container.angabenDeklaration?.nutzung;
            this.setupFormAndPermissions(container, this.nutzung, principal);
            this.unsavedChangesService.registerForm(this.form);
        }, error => {
            LOG.error(error);
        });
        this.vorgaenger$ = this.ferienbetreuungService.getFerienbetreuungVorgaengerContainer()
            .pipe(takeUntil(this.unsubscribe$));
    }

    public ngOnDestroy(): void {
        this.unsubscribe$.next();
    }

    public async onAbschliessen(): Promise<void> {
        if (await this.checkReadyForAbschliessen()) {
            this.writeBackFormValues();
            this.ferienbetreuungService.nutzungAbschliessen(this.container.id, this.nutzung)
                .subscribe(() => this.handleSaveSuccess(), error => this.handleSaveErrors(error));
        }
    }

    // Overwrite
    protected enableFormValidation(): void {
        this.form.controls.anzahlBetreuungstageKinderBern
            .setValidators([Validators.required, numberValidator(ValidationType.HALF)]);
        this.form.controls.betreuungstageKinderDieserGemeinde
            .setValidators([Validators.required, numberValidator(ValidationType.HALF)]);
        this.form.controls.betreuungstageKinderDieserGemeindeSonderschueler
            .setValidators([numberValidator(ValidationType.HALF)]);
        this.form.controls.davonBetreuungstageKinderAndererGemeinden
            .setValidators([Validators.required, numberValidator(ValidationType.HALF)]);
        this.form.controls.davonBetreuungstageKinderAndererGemeindenSonderschueler
            .setValidators([numberValidator(ValidationType.HALF)]);
        this.form.controls.anzahlBetreuteKinder.setValidators([numberValidator(ValidationType.INTEGER)]);
        this.form.controls.anzahlBetreuteKinderSonderschueler.setValidators([numberValidator(ValidationType.INTEGER)]);
        this.form.controls.anzahlBetreuteKinder1Zyklus.setValidators([numberValidator(ValidationType.INTEGER)]);
        this.form.controls.anzahlBetreuteKinder2Zyklus.setValidators([numberValidator(ValidationType.INTEGER)]);
        this.form.controls.anzahlBetreuteKinder3Zyklus.setValidators([numberValidator(ValidationType.INTEGER)]);
        this.enableSpecialBetreuungstageFormValidation();
    }

    private enableSpecialBetreuungstageFormValidation(): void {
        // betreuungstage
        this.form.controls.anzahlBetreuungstageKinderBern.setValidators(control => {
            const diff = this.form.value.anzahlBetreuungstageKinderBern -
                this.form.value.betreuungstageKinderDieserGemeinde -
                this.form.value.davonBetreuungstageKinderAndererGemeinden;
            if (diff !== 0) {
                return {
                    betreuungstageError: control.value
                };
            }
            return null;
        });
        // sonderschueler 1
        this.form.controls.betreuungstageKinderDieserGemeindeSonderschueler.setValidators(control => {
            const diff = this.form.value.betreuungstageKinderDieserGemeinde -
                this.form.value.betreuungstageKinderDieserGemeindeSonderschueler;
            if (diff < 0) {
                return {
                    sonderschuelerError: control.value
                };
            }
            return null;
        });
        // sonderschueler 2
        // eslint-disable-next-line
        this.form.controls.davonBetreuungstageKinderAndererGemeindenSonderschueler.setValidators(control => {
            const diff = this.form.value.davonBetreuungstageKinderAndererGemeinden -
                this.form.value.davonBetreuungstageKinderAndererGemeindenSonderschueler;
            if (diff < 0) {
                return {
                    sonderschuelerError: control.value
                };
            }
            return null;
        });
    }

    protected setupForm(nutzung: TSFerienbetreuungAngabenNutzung): void {
        if (!nutzung) {
            return;
        }
        this.form.patchValue({
            anzahlBetreuungstageKinderBern: nutzung.anzahlBetreuungstageKinderBern,
            anzahlBetreuteKinder1Zyklus: nutzung.anzahlBetreuteKinder1Zyklus,
            anzahlBetreuteKinder2Zyklus: nutzung.anzahlBetreuteKinder2Zyklus,
            anzahlBetreuteKinder3Zyklus: nutzung.anzahlBetreuteKinder3Zyklus,
            anzahlBetreuteKinder: nutzung.anzahlBetreuteKinder,
            anzahlBetreuteKinderSonderschueler: nutzung.anzahlBetreuteKinderSonderschueler,
            davonBetreuungstageKinderAndererGemeinden: nutzung.davonBetreuungstageKinderAndererGemeinden,
            davonBetreuungstageKinderAndererGemeindenSonderschueler:
                nutzung.davonBetreuungstageKinderAndererGemeindenSonderschueler,
            betreuungstageKinderDieserGemeindeSonderschueler: nutzung.betreuungstageKinderDieserGemeindeSonderschueler,
            betreuungstageKinderDieserGemeinde: nutzung.betreuungstageKinderDieserGemeinde,
        });
        this.setBasicValidation();
    }

    protected setBasicValidation(): void {
        this.removeAllValidators();

        this.form.controls.anzahlBetreuungstageKinderBern.setValidators(
            numberValidator(ValidationType.HALF)
        );
        this.form.controls.betreuungstageKinderDieserGemeinde.setValidators(
            numberValidator(ValidationType.HALF)
        );
        this.form.controls.betreuungstageKinderDieserGemeindeSonderschueler.setValidators(
            numberValidator(ValidationType.HALF)
        );
        this.form.controls.davonBetreuungstageKinderAndererGemeinden.setValidators(
            numberValidator(ValidationType.HALF)
        );
        this.form.controls.davonBetreuungstageKinderAndererGemeindenSonderschueler.setValidators(
            numberValidator(ValidationType.HALF)
        );
        this.form.controls.anzahlBetreuteKinder.setValidators(
            numberValidator(ValidationType.INTEGER)
        );
        this.form.controls.anzahlBetreuteKinderSonderschueler.setValidators(
            numberValidator(ValidationType.INTEGER)
        );
        this.form.controls.anzahlBetreuteKinder1Zyklus.setValidators(
            numberValidator(ValidationType.INTEGER)
        );
        this.form.controls.anzahlBetreuteKinder2Zyklus.setValidators(
            numberValidator(ValidationType.INTEGER)
        );
        this.form.controls.anzahlBetreuteKinder3Zyklus.setValidators(
            numberValidator(ValidationType.INTEGER)
        );
        this.triggerFormValidation();
    }

    public save(): void {
        this.formAbschliessenTriggered = false;
        this.setBasicValidation();
        if (!this.form.valid) {
            this.showValidierungFehlgeschlagenErrorMessage();
            return;
        }
        this.writeBackFormValues();
        this.ferienbetreuungService.saveNutzung(this.container.id, this.nutzung)
            .subscribe(() => {
                this.ferienbetreuungService.updateFerienbetreuungContainerStores(this.container.id);
                this.errorService.clearAll();
                this.errorService.addMesageAsInfo(this.translate.instant('SPEICHERN_ERFOLGREICH'));
            }, err => this.handleSaveErrors(err));
    }

    private writeBackFormValues(): void {
        this.nutzung.anzahlBetreuteKinder = this.form.getRawValue().anzahlBetreuteKinder;
        this.nutzung.anzahlBetreuteKinderSonderschueler = this.form.getRawValue().anzahlBetreuteKinderSonderschueler;
        this.nutzung.anzahlBetreuungstageKinderBern = this.form.getRawValue().anzahlBetreuungstageKinderBern;
        this.nutzung.anzahlBetreuteKinder1Zyklus = this.form.getRawValue().anzahlBetreuteKinder1Zyklus;
        this.nutzung.anzahlBetreuteKinder2Zyklus = this.form.getRawValue().anzahlBetreuteKinder2Zyklus;
        this.nutzung.anzahlBetreuteKinder3Zyklus = this.form.getRawValue().anzahlBetreuteKinder3Zyklus;
        this.nutzung.betreuungstageKinderDieserGemeinde = this.form.getRawValue().betreuungstageKinderDieserGemeinde;
        this.nutzung.betreuungstageKinderDieserGemeindeSonderschueler =
            this.form.getRawValue().betreuungstageKinderDieserGemeindeSonderschueler;
        this.nutzung.davonBetreuungstageKinderAndererGemeinden =
            this.form.getRawValue().davonBetreuungstageKinderAndererGemeinden;
        this.nutzung.davonBetreuungstageKinderAndererGemeindenSonderschueler =
            this.form.getRawValue().davonBetreuungstageKinderAndererGemeindenSonderschueler;
    }

    public onFalscheAngaben(): void {
        this.ferienbetreuungService.falscheAngabenNutzung(this.container.id, this.nutzung)
            .subscribe(() => this.handleSaveSuccess(), error => this.handleSaveErrors(error));
    }
}
