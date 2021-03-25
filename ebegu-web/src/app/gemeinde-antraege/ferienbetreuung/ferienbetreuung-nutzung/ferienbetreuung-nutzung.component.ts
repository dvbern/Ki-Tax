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
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {TranslateService} from '@ngx-translate/core';
import {UIRouterGlobals} from '@uirouter/core';
import {combineLatest, Subject} from 'rxjs';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {TSWizardStepXTyp} from '../../../../models/enums/TSWizardStepXTyp';
import {TSFerienbetreuungAngabenContainer} from '../../../../models/gemeindeantrag/TSFerienbetreuungAngabenContainer';
import {TSFerienbetreuungAngabenNutzung} from '../../../../models/gemeindeantrag/TSFerienbetreuungAngabenNutzung';
import {DvNgConfirmDialogComponent} from '../../../core/component/dv-ng-confirm-dialog/dv-ng-confirm-dialog.component';
import {ErrorService} from '../../../core/errors/service/ErrorService';
import {LogFactory} from '../../../core/logging/LogFactory';
import {WizardStepXRS} from '../../../core/service/wizardStepXRS.rest';
import {numberValidator, ValidationType} from '../../../shared/validators/number-validator.directive';
import {FerienbetreuungService} from '../services/ferienbetreuung.service';

const LOG = LogFactory.createLog('FerienbetreuungNutzungComponent');

@Component({
    selector: 'dv-ferienbetreuung-nutzung',
    templateUrl: './ferienbetreuung-nutzung.component.html',
    styleUrls: ['./ferienbetreuung-nutzung.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FerienbetreuungNutzungComponent implements OnInit {

    public form: FormGroup;

    private nutzung: TSFerienbetreuungAngabenNutzung;
    private container: TSFerienbetreuungAngabenContainer;
    private formFreigebenTriggered = false;
    private readonly WIZARD_TYPE = TSWizardStepXTyp.FERIENBETREUUNG;

    public readonly canSeeSave: Subject<boolean> = new Subject();
    public readonly canSeeAbschliessen: Subject<boolean> = new Subject();

    public constructor(
        private readonly ferienbetreuungService: FerienbetreuungService,
        private readonly fb: FormBuilder,
        private readonly cd: ChangeDetectorRef,
        private readonly errorService: ErrorService,
        private readonly translate: TranslateService,
        private readonly dialog: MatDialog,
        private readonly wizardRS: WizardStepXRS,
        private readonly uiRouterGlobals: UIRouterGlobals,
        private readonly authService: AuthServiceRS
    ) {
    }

    public ngOnInit(): void {
        combineLatest([
            this.ferienbetreuungService.getFerienbetreuungContainer(),
            this.authService.principal$,
        ]).subscribe(([container, principal]) => {
                this.container = container;
                this.nutzung = container.angabenDeklaration?.nutzung;
                this.canSeeSave.next(true);
                this.canSeeAbschliessen.next(true);
                this.setupForm(this.nutzung);
                this.cd.markForCheck();
            }, error => {
                LOG.error(error);
            });
    }


    public async onAbschliessen(): Promise<void> {
        this.triggerFormValidation();

        if (!this.form.valid) {
            this.showValidierungFehlgeschlagenErrorMessage();
            return;
        }
        if (!await this.confirmDialog('FRAGE_FORMULAR_ABSCHLIESSEN')) {
            return;
        }

        this.ferienbetreuungService.angebotAbschliessen(this.container.id, this.form.value)
            .subscribe(() => this.handleSaveSuccess(), error => this.handleSaveError(error));
    }

    private handleSaveSuccess(): void {
        this.wizardRS.updateSteps(this.WIZARD_TYPE, this.uiRouterGlobals.params.id);
    }

    private handleSaveError(error: any): void {
        if (error.error?.includes('Not all required properties are set')) {
            this.triggerFormValidation();
            this.showValidierungFehlgeschlagenErrorMessage();
        } else {
            this.errorService.addMesageAsError(this.translate.instant('SAVE_ERROR'));
        }
    }

    private showValidierungFehlgeschlagenErrorMessage(): void {
        this.errorService.addMesageAsError(
            this.translate.instant('LATS_GEMEINDE_VALIDIERUNG_FEHLGESCHLAGEN'),
        );
    }

    private confirmDialog(frageKey: string): Promise<boolean> {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            frage: this.translate.instant(frageKey),
        };
        return this.dialog.open(DvNgConfirmDialogComponent, dialogConfig)
            .afterClosed()
            .toPromise();
    }

    private triggerFormValidation(): void {
        this.enableFormValidation();
        this.formFreigebenTriggered = true;
        for (const key in this.form.controls) {
            if (this.form.get(key) !== null) {
                this.form.get(key).markAsTouched();
                this.form.get(key).updateValueAndValidity();
            }
        }
        this.form.updateValueAndValidity();
    }

    private enableFormValidation(): void {
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
    }

    private setupForm(nutzung: TSFerienbetreuungAngabenNutzung): void {
        if (!nutzung) {
            return;
        }
        this.form = this.fb.group({
            id: [nutzung.id],
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
    }

    public save(): void {
        this.ferienbetreuungService.saveNutzung(this.container.id, this.form.value)
            .subscribe(() => {
                this.ferienbetreuungService.updateFerienbetreuungContainerStore(this.container.id);
                this.errorService.addMesageAsInfo(this.translate.instant('SPEICHERN_ERFOLGREICH'));
            }, err => {
                LOG.error(err);
                this.errorService.addMesageAsError(this.translate.instant('FERIENBETREUUNG_PERSIST_ERROR'));
            });
    }

}
