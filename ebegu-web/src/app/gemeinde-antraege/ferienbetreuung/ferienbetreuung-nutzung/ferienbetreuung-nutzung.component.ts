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
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FerienbetreuungNutzungComponent extends AbstractFerienbetreuungFormular implements OnInit, OnDestroy {

    private nutzung: TSFerienbetreuungAngabenNutzung;
    public vorgaenger$: Observable<TSFerienbetreuungAngabenContainer>;
    private unsubscribe$ = new Subject();

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
        private readonly unsavedChangesService: UnsavedChangesService,
    ) {
        super(errorService, translate, dialog, cd, wizardRS, uiRouterGlobals);
    }

    public ngOnInit(): void {
        combineLatest([
            this.ferienbetreuungService.getFerienbetreuungContainer(),
            this.authService.principal$,
        ]).pipe(
            takeUntil(this.unsubscribe$)
        ).subscribe(([container, principal]) => {
            this.container = container;
            this.nutzung = container.isAtLeastInPruefungKanton() ?
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
            this.ferienbetreuungService.nutzungAbschliessen(this.container.id, this.form.value)
                .subscribe(() => this.handleSaveSuccess(), error => this.handleSaveErrors(error));
        }
    }

    // Overwrite
    protected enableFormValidation(): void {
        this.form.get('anzahlBetreuungstageKinderBern')
            .setValidators([Validators.required, numberValidator(ValidationType.HALF)]);
        this.form.get('betreuungstageKinderDieserGemeinde')
            .setValidators([Validators.required, numberValidator(ValidationType.HALF)]);
        this.form.get('betreuungstageKinderDieserGemeindeSonderschueler')
            .setValidators([numberValidator(ValidationType.HALF)]);
        this.form.get('davonBetreuungstageKinderAndererGemeinden')
            .setValidators([Validators.required, numberValidator(ValidationType.HALF)]);
        this.form.get('davonBetreuungstageKinderAndererGemeindenSonderschueler')
            .setValidators([numberValidator(ValidationType.HALF)]);
        this.form.get('anzahlBetreuteKinder').setValidators([numberValidator(ValidationType.INTEGER)]);
        this.form.get('anzahlBetreuteKinderSonderschueler').setValidators([numberValidator(ValidationType.INTEGER)]);
        this.form.get('anzahlBetreuteKinder1Zyklus').setValidators([numberValidator(ValidationType.INTEGER)]);
        this.form.get('anzahlBetreuteKinder2Zyklus').setValidators([numberValidator(ValidationType.INTEGER)]);
        this.form.get('anzahlBetreuteKinder3Zyklus').setValidators([numberValidator(ValidationType.INTEGER)]);
        this.enableSpecialBetreuungstageFormValidation();
    }

    private enableSpecialBetreuungstageFormValidation(): void {
        // betreuungstage
        this.form.get('anzahlBetreuungstageKinderBern').setValidators(control => {
            const diff = parseFloat(this.form.get('anzahlBetreuungstageKinderBern').value) -
                parseFloat(this.form.get('betreuungstageKinderDieserGemeinde').value) -
                parseFloat(this.form.get('davonBetreuungstageKinderAndererGemeinden').value);
            if (diff !== 0) {
                return {
                    betreuungstageError: control.value,
                };
            }
            return null;
        });
        // sonderschueler 1
        this.form.get('betreuungstageKinderDieserGemeindeSonderschueler').setValidators(control => {
            const diff = parseFloat(this.form.get('betreuungstageKinderDieserGemeinde').value) -
                parseFloat(this.form.get('betreuungstageKinderDieserGemeindeSonderschueler').value);
            if (diff < 0) {
                return {
                    sonderschuelerError: control.value,
                };
            }
            return null;
        });
        // sonderschueler 2
        // tslint:disable-next-line:no-identical-functions
        this.form.get('davonBetreuungstageKinderAndererGemeindenSonderschueler').setValidators(control => {
            const diff = parseFloat(this.form.get('davonBetreuungstageKinderAndererGemeinden').value) -
                parseFloat(this.form.get('davonBetreuungstageKinderAndererGemeindenSonderschueler').value);
            if (diff < 0) {
                return {
                    sonderschuelerError: control.value,
                };
            }
            return null;
        });
    }

    protected setupForm(nutzung: TSFerienbetreuungAngabenNutzung): void {
        if (!nutzung) {
            return;
        }
        this.form = this.fb.group({
            id: [nutzung.id],
            version: [
                nutzung.version,
            ],
            anzahlBetreuungstageKinderBern: [
                nutzung.anzahlBetreuungstageKinderBern,
            ],
            betreuungstageKinderDieserGemeinde: [
                nutzung.betreuungstageKinderDieserGemeinde,
            ],
            betreuungstageKinderDieserGemeindeSonderschueler: [
                nutzung.betreuungstageKinderDieserGemeindeSonderschueler,
            ],
            davonBetreuungstageKinderAndererGemeinden: [
                nutzung.davonBetreuungstageKinderAndererGemeinden,
            ],
            davonBetreuungstageKinderAndererGemeindenSonderschueler: [
                nutzung.davonBetreuungstageKinderAndererGemeindenSonderschueler,
            ],
            anzahlBetreuteKinder: [
                nutzung.anzahlBetreuteKinder,
            ],
            anzahlBetreuteKinderSonderschueler: [
                nutzung.anzahlBetreuteKinderSonderschueler,
            ],
            anzahlBetreuteKinder1Zyklus: [
                nutzung.anzahlBetreuteKinder1Zyklus,
            ],
            anzahlBetreuteKinder2Zyklus: [
                nutzung.anzahlBetreuteKinder2Zyklus,
            ],
            anzahlBetreuteKinder3Zyklus: [
                nutzung.anzahlBetreuteKinder3Zyklus,
            ],
        });
        this.setBasicValidation();
    }

    protected setBasicValidation(): void {
        this.removeAllValidators();

        this.form.get('anzahlBetreuungstageKinderBern').setValidators(
            numberValidator(ValidationType.HALF)
        );
        this.form.get('betreuungstageKinderDieserGemeinde').setValidators(
            numberValidator(ValidationType.HALF)
        );
        this.form.get('betreuungstageKinderDieserGemeindeSonderschueler').setValidators(
            numberValidator(ValidationType.HALF)
        );
        this.form.get('davonBetreuungstageKinderAndererGemeinden').setValidators(
            numberValidator(ValidationType.HALF)
        );
        this.form.get('davonBetreuungstageKinderAndererGemeindenSonderschueler').setValidators(
            numberValidator(ValidationType.HALF)
        );
        this.form.get('anzahlBetreuteKinder').setValidators(
            numberValidator(ValidationType.INTEGER)
        );
        this.form.get('anzahlBetreuteKinderSonderschueler').setValidators(
            numberValidator(ValidationType.INTEGER)
        );
        this.form.get('anzahlBetreuteKinder1Zyklus').setValidators(
            numberValidator(ValidationType.INTEGER)
        );
        this.form.get('anzahlBetreuteKinder2Zyklus').setValidators(
            numberValidator(ValidationType.INTEGER)
        );
        this.form.get('anzahlBetreuteKinder3Zyklus').setValidators(
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
        this.ferienbetreuungService.saveNutzung(this.container.id, this.form.value)
            .subscribe(() => {
                this.ferienbetreuungService.updateFerienbetreuungContainerStores(this.container.id);
                this.errorService.clearAll();
                this.errorService.addMesageAsInfo(this.translate.instant('SPEICHERN_ERFOLGREICH'));
            }, err => this.handleSaveErrors(err));
    }

    public onFalscheAngaben(): void {
        this.ferienbetreuungService.falscheAngabenNutzung(this.container.id, this.nutzung)
            .subscribe(() => this.handleSaveSuccess(), error => this.handleSaveErrors(error));
    }
}
