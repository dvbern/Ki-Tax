/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    OnDestroy,
    OnInit
} from '@angular/core';
import {FormBuilder, Validators} from '@angular/forms';
import {MatDialog} from '@angular/material/dialog';
import {TranslateService} from '@ngx-translate/core';
import {UIRouterGlobals} from '@uirouter/core';
import {combineLatest, Observable, Subject} from 'rxjs';
import {takeUntil} from 'rxjs/operators';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {TSFerienbetreuungAngaben} from '../../../../models/gemeindeantrag/TSFerienbetreuungAngaben';
import {TSFerienbetreuungAngabenContainer} from '../../../../models/gemeindeantrag/TSFerienbetreuungAngabenContainer';
import {TSFerienbetreuungAngabenKostenEinnahmen} from '../../../../models/gemeindeantrag/TSFerienbetreuungAngabenKostenEinnahmen';
import {ErrorService} from '../../../core/errors/service/ErrorService';
import {LogFactory} from '../../../core/logging/LogFactory';
import {WizardStepXRS} from '../../../core/service/wizardStepXRS.rest';
import {
    numberValidator,
    ValidationType
} from '../../../shared/validators/number-validator.directive';
import {UnsavedChangesService} from '../../services/unsaved-changes.service';
import {AbstractFerienbetreuungFormular} from '../abstract.ferienbetreuung-formular';
import {FerienbetreuungService} from '../services/ferienbetreuung.service';

const LOG = LogFactory.createLog('FerienbetreuungKostenEinnahmenComponent');

@Component({
    selector: 'dv-ferienbetreuung-kosten-einnahmen',
    templateUrl: './ferienbetreuung-kosten-einnahmen.component.html',
    styleUrls: ['./ferienbetreuung-kosten-einnahmen.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class FerienbetreuungKostenEinnahmenComponent
    extends AbstractFerienbetreuungFormular
    implements OnInit, OnDestroy
{
    private kostenEinnahmen: TSFerienbetreuungAngabenKostenEinnahmen;
    private readonly unsubscribe$ = new Subject();
    public vorgaenger$: Observable<TSFerienbetreuungAngabenContainer>;
    public isDelegationsmodell: boolean = false;

    public form = this.fb.group({
        personalkosten: [<null | number>null],
        personalkostenLeitungAdmin: [<null | number>null],
        sachkosten: [<null | number>null],
        weitereKosten: [<null | number>null],
        verpflegungskosten: [<null | number>null],
        bemerkungenKosten: [<null | string>null],
        elterngebuehren: [<null | number>null],
        weitereEinnahmen: [<null | number>null],
        sockelbeitrag: [<null | number>null],
        beitraegeNachAnmeldungen: [<null | number>null],
        vorfinanzierteKantonsbeitraege: [<null | number>null],
        eigenleistungenGemeinde: [<null | number>null]
    });

    public constructor(
        protected readonly cd: ChangeDetectorRef,
        protected readonly errorService: ErrorService,
        protected readonly translate: TranslateService,
        protected readonly dialog: MatDialog,
        protected readonly wizardRS: WizardStepXRS,
        protected readonly uiRouterGlobals: UIRouterGlobals,
        private readonly ferienbetreuungService: FerienbetreuungService,
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
        ])
            .pipe(takeUntil(this.unsubscribe$))
            .subscribe(
                ([container, principal]) => {
                    this.container = container;
                    const angaben =
                        container.isAtLeastInPruefungKantonOrZurueckgegeben()
                            ? container.angabenKorrektur
                            : container.angabenDeklaration;
                    this.kostenEinnahmen = angaben?.kostenEinnahmen;
                    this.isDelegationsmodell = angaben?.isDelegationsmodell();
                    this.setupFormAndPermissions(
                        container,
                        this.kostenEinnahmen,
                        principal
                    );
                    this.unsavedChangesService.registerForm(this.form);
                },
                error => {
                    LOG.error(error);
                }
            );
        this.vorgaenger$ = this.ferienbetreuungService
            .getFerienbetreuungVorgaengerContainer()
            .pipe(takeUntil(this.unsubscribe$));
    }

    public ngOnDestroy(): void {
        this.unsubscribe$.next();
    }

    protected setupForm(
        kostenEinnahmen: TSFerienbetreuungAngabenKostenEinnahmen
    ): void {
        this.form.patchValue({
            personalkosten: kostenEinnahmen.personalkosten,
            beitraegeNachAnmeldungen: kostenEinnahmen.beitraegeNachAnmeldungen,
            weitereEinnahmen: kostenEinnahmen.weitereEinnahmen,
            sachkosten: kostenEinnahmen.sachkosten,
            sockelbeitrag: kostenEinnahmen.sockelbeitrag,
            personalkostenLeitungAdmin:
                kostenEinnahmen.personalkostenLeitungAdmin,
            vorfinanzierteKantonsbeitraege:
                kostenEinnahmen.vorfinanzierteKantonsbeitraege,
            elterngebuehren: kostenEinnahmen.elterngebuehren,
            eigenleistungenGemeinde: kostenEinnahmen.eigenleistungenGemeinde,
            bemerkungenKosten: kostenEinnahmen.bemerkungenKosten,
            verpflegungskosten: kostenEinnahmen.verpflegungskosten,
            weitereKosten: kostenEinnahmen.weitereKosten
        });
        this.setBasicValidation();
    }

    protected setBasicValidation(): void {
        this.removeAllValidators();

        this.form.controls.personalkosten.setValidators(
            numberValidator(ValidationType.INTEGER)
        );
        this.form.controls.personalkostenLeitungAdmin.setValidators(
            numberValidator(ValidationType.INTEGER)
        );
        this.form.controls.sachkosten.setValidators(
            numberValidator(ValidationType.INTEGER)
        );
        this.form.controls.verpflegungskosten.setValidators(
            numberValidator(ValidationType.INTEGER)
        );
        this.form.controls.weitereKosten.setValidators(
            numberValidator(ValidationType.INTEGER)
        );
        this.form.controls.elterngebuehren.setValidators(
            numberValidator(ValidationType.INTEGER)
        );
        this.form.controls.weitereEinnahmen.setValidators(
            numberValidator(ValidationType.INTEGER)
        );
        this.form.controls.sockelbeitrag.setValidators(
            numberValidator(ValidationType.INTEGER)
        );
        this.form.controls.beitraegeNachAnmeldungen.setValidators(
            numberValidator(ValidationType.INTEGER)
        );
        this.form.controls.vorfinanzierteKantonsbeitraege.setValidators(
            numberValidator(ValidationType.INTEGER)
        );
        this.form.controls.eigenleistungenGemeinde.setValidators(
            numberValidator(ValidationType.INTEGER)
        );
        this.triggerFormValidation();
    }

    protected enableFormValidation(): void {
        this.form.controls.personalkosten.setValidators([
            Validators.required,
            numberValidator(ValidationType.INTEGER)
        ]);
        this.form.controls.personalkostenLeitungAdmin.setValidators([
            numberValidator(ValidationType.INTEGER)
        ]);
        this.form.controls.sachkosten.setValidators([
            Validators.required,
            numberValidator(ValidationType.INTEGER)
        ]);
        this.form.controls.verpflegungskosten.setValidators([
            numberValidator(ValidationType.INTEGER)
        ]);
        this.form.controls.weitereKosten.setValidators([
            numberValidator(ValidationType.INTEGER)
        ]);
        this.form.controls.elterngebuehren.setValidators([
            Validators.required,
            numberValidator(ValidationType.INTEGER)
        ]);
        this.form.controls.weitereEinnahmen.setValidators([
            Validators.required,
            numberValidator(ValidationType.INTEGER)
        ]);
        if (this.isDelegationsmodell) {
            this.form.controls.sockelbeitrag.setValidators([
                Validators.required,
                numberValidator(ValidationType.INTEGER)
            ]);
            this.form.controls.beitraegeNachAnmeldungen.setValidators([
                Validators.required,
                numberValidator(ValidationType.INTEGER)
            ]);
            this.form.controls.vorfinanzierteKantonsbeitraege.setValidators([
                Validators.required,
                numberValidator(ValidationType.INTEGER)
            ]);
            this.form.controls.eigenleistungenGemeinde.setValidators([
                Validators.required,
                numberValidator(ValidationType.INTEGER)
            ]);
        }
    }

    public save(): void {
        this.formAbschliessenTriggered = false;
        this.setBasicValidation();
        if (!this.form.valid) {
            this.showValidierungFehlgeschlagenErrorMessage();
            return;
        }
        this.ferienbetreuungService
            .saveBerechnung(
                this.container.id,
                this.getAngabenForStatus().berechnungen
            )
            .subscribe(() => {
                this.ferienbetreuungService
                    .saveKostenEinnahmen(
                        this.container.id,
                        this.extractFormValues()
                    )
                    .subscribe(
                        () => {
                            this.ferienbetreuungService.updateFerienbetreuungContainerStores(
                                this.container.id
                            );
                            this.errorService.clearAll();
                            this.errorService.addMesageAsInfo(
                                this.translate.instant('SPEICHERN_ERFOLGREICH')
                            );
                        },
                        err => this.handleSaveErrors(err)
                    );
            });
    }

    private getAngabenForStatus(): TSFerienbetreuungAngaben {
        return this.container?.isAtLeastInPruefungKantonOrZurueckgegeben()
            ? this.container?.angabenKorrektur
            : this.container?.angabenDeklaration;
    }

    public async onAbschliessen(): Promise<void> {
        if (await this.checkReadyForAbschliessen()) {
            this.ferienbetreuungService
                .saveBerechnung(
                    this.container.id,
                    this.getAngabenForStatus().berechnungen
                )
                .subscribe(
                    () =>
                        this.ferienbetreuungService
                            .kostenEinnahmenAbschliessen(
                                this.container.id,
                                Object.assign(
                                    this.getAngabenForStatus().kostenEinnahmen,
                                    this.form.value
                                )
                            )
                            .subscribe(
                                () => this.handleSaveSuccess(),
                                error => this.handleSaveErrors(error)
                            ),
                    error => this.handleSaveErrors(error)
                );
        }
    }

    private extractFormValues(): TSFerienbetreuungAngabenKostenEinnahmen {
        this.kostenEinnahmen.personalkosten = this.form.value.personalkosten;
        this.kostenEinnahmen.personalkostenLeitungAdmin =
            this.form.value.personalkostenLeitungAdmin;
        this.kostenEinnahmen.sachkosten = this.form.value.sachkosten;
        this.kostenEinnahmen.verpflegungskosten =
            this.form.value.verpflegungskosten;
        this.kostenEinnahmen.weitereKosten = this.form.value.weitereKosten;
        this.kostenEinnahmen.bemerkungenKosten =
            this.form.value.bemerkungenKosten;
        this.kostenEinnahmen.elterngebuehren = this.form.value.elterngebuehren;
        this.kostenEinnahmen.weitereEinnahmen =
            this.form.value.weitereEinnahmen;
        this.kostenEinnahmen.sockelbeitrag = this.form.value.sockelbeitrag;
        this.kostenEinnahmen.beitraegeNachAnmeldungen =
            this.form.value.beitraegeNachAnmeldungen;
        this.kostenEinnahmen.vorfinanzierteKantonsbeitraege =
            this.form.value.vorfinanzierteKantonsbeitraege;
        this.kostenEinnahmen.eigenleistungenGemeinde =
            this.form.value.eigenleistungenGemeinde;
        return this.kostenEinnahmen;
    }

    public onFalscheAngaben(): void {
        this.ferienbetreuungService
            .falscheAngabenKostenEinnahmen(
                this.container.id,
                this.kostenEinnahmen
            )
            .subscribe(
                () => this.handleSaveSuccess(),
                error => this.handleSaveErrors(error)
            );
    }
}
