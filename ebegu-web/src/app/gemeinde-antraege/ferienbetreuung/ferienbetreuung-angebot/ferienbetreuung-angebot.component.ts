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
import {FormBuilder, ValidatorFn, Validators} from '@angular/forms';
import {MatDialog} from '@angular/material/dialog';
import {TranslateService} from '@ngx-translate/core';
import {UIRouterGlobals} from '@uirouter/core';
import {combineLatest, Subscription} from 'rxjs';
import {filter} from 'rxjs/operators';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {GemeindeRS} from '../../../../gesuch/service/gemeindeRS.rest';
import {FerienbetreuungAngabenStatus} from '../../../../models/enums/FerienbetreuungAngabenStatus';
import {TSFerienbetreuungAngabenAngebot} from '../../../../models/gemeindeantrag/TSFerienbetreuungAngabenAngebot';
import {TSAdresse} from '../../../../models/TSAdresse';
import {TSBfsGemeinde} from '../../../../models/TSBfsGemeinde';
import {EbeguUtil} from '../../../../utils/EbeguUtil';
import {ErrorService} from '../../../core/errors/service/ErrorService';
import {LogFactory} from '../../../core/logging/LogFactory';
import {WizardStepXRS} from '../../../core/service/wizardStepXRS.rest';
import {numberValidator, ValidationType} from '../../../shared/validators/number-validator.directive';
import {UnsavedChangesService} from '../../services/unsaved-changes.service';
import {AbstractFerienbetreuungFormular} from '../abstract.ferienbetreuung-formular';
import {FerienbetreuungService} from '../services/ferienbetreuung.service';

const LOG = LogFactory.createLog('FerienbetreuungAngebotComponent');

@Component({
    selector: 'dv-ferienbetreuung-angebot',
    templateUrl: './ferienbetreuung-angebot.component.html',
    styleUrls: ['./ferienbetreuung-angebot.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FerienbetreuungAngebotComponent extends AbstractFerienbetreuungFormular implements OnInit, OnDestroy {

    public formValidationTriggered = false;
    public bfsGemeinden: TSBfsGemeinde[];

    private angebot: TSFerienbetreuungAngabenAngebot;
    private subscription: Subscription;

    public constructor(
        protected readonly errorService: ErrorService,
        protected readonly translate: TranslateService,
        protected readonly dialog: MatDialog,
        protected readonly cd: ChangeDetectorRef,
        protected readonly wizardRS: WizardStepXRS,
        protected readonly uiRouterGlobals: UIRouterGlobals,
        private readonly fb: FormBuilder,
        private readonly gemeindeRS: GemeindeRS,
        private readonly ferienbetreuungService: FerienbetreuungService,
        private readonly authService: AuthServiceRS,
        private readonly unsavedChangesService: UnsavedChangesService
    ) {
        super(errorService, translate, dialog, cd, wizardRS, uiRouterGlobals);
    }

    public ngOnInit(): void {
        this.subscription = combineLatest([
                this.ferienbetreuungService.getFerienbetreuungContainer(),
                this.authService.principal$.pipe(filter(principal => !!principal)),
            ],
        ).subscribe(([container, principal]) => {
            this.container = container;
            this.angebot = container.isAtLeastInPruefungKanton() ?
                container.angabenKorrektur?.angebot : container.angabenDeklaration?.angebot;
            this.setupFormAndPermissions(container, this.angebot, principal);
            this.unsavedChangesService.registerForm(this.form);
        }, error => {
            LOG.error(error);
        });
        this.gemeindeRS.getAllBfsGemeinden().then(gemeinden => {
            this.bfsGemeinden = gemeinden;
            this.bfsGemeinden.sort((a, b) => a.name.localeCompare(b.name));
            this.cd.markForCheck();
        });
    }

    public ngOnDestroy(): void {
        this.subscription.unsubscribe();
    }

    protected setupForm(angebot: TSFerienbetreuungAngabenAngebot): void {
        if (!angebot) {
            return;
        }
        this.form = this.fb.group({
            id: [
                angebot?.id,
            ],
            version: [
                angebot?.version
            ],
            angebot: [
                angebot?.angebot,
            ],
            angebotAdresse: this.fb.group({
                kontaktpersonVorname: [
                    angebot?.angebotKontaktpersonVorname,
                ],
                kontaktpersonNachname: [
                    angebot?.angebotKontaktpersonNachname,
                ],
                strasse: [
                    angebot?.angebotAdresse?.strasse,
                ],
                hausnummer: [
                    angebot?.angebotAdresse?.hausnummer,
                ],
                plz: [
                    angebot?.angebotAdresse?.plz,
                ],
                ort: [
                    angebot?.angebotAdresse?.ort,
                ],
                zusatzzeile: [
                    angebot?.angebotAdresse?.zusatzzeile
                ],
                version: [
                    angebot?.angebotAdresse?.version
                ]
            }),
            anzahlFerienwochenHerbstferien: [
                angebot?.anzahlFerienwochenHerbstferien
            ],
            anzahlFerienwochenWinterferien: [
                angebot?.anzahlFerienwochenWinterferien,
            ],
            anzahlFerienwochenSportferien: [
                angebot?.anzahlFerienwochenSportferien,
            ],
            anzahlFerienwochenFruehlingsferien: [
                angebot?.anzahlFerienwochenFruehlingsferien,
            ],
            anzahlFerienwochenSommerferien: [
                angebot?.anzahlFerienwochenSommerferien,
            ],
            anzahlTage: [
                angebot?.anzahlTage,
            ],
            bemerkungenAnzahlFerienwochen: [
                angebot?.bemerkungenAnzahlFerienwochen
            ],
            anzahlStundenProBetreuungstag: [
                angebot?.anzahlStundenProBetreuungstag,
            ],
            betreuungErfolgtTagsueber: [
                angebot?.betreuungErfolgtTagsueber
            ],
            bemerkungenOeffnungszeiten: [
                angebot?.bemerkungenOeffnungszeiten
            ],
            finanziellBeteiligteGemeinden: [
                angebot?.finanziellBeteiligteGemeinden,
            ],
            gemeindeFuehrtAngebotSelber: [
                angebot?.gemeindeFuehrtAngebotSelber,
            ],
            gemeindeFuehrtAngebotInKooperation: [
                angebot?.gemeindeFuehrtAngebotInKooperation,
            ],
            gemeindeBeauftragtExterneAnbieter: [
                angebot?.gemeindeBeauftragtExterneAnbieter,
            ],
            angebotVereineUndPrivateIntegriert: [
                angebot?.angebotVereineUndPrivateIntegriert,
            ],
            bemerkungenKooperation: [
                angebot?.bemerkungenKooperation,
            ],
            leitungDurchPersonMitAusbildung: [
                angebot?.leitungDurchPersonMitAusbildung,
            ],
            betreuungDurchPersonenMitErfahrung: [
                angebot?.betreuungDurchPersonenMitErfahrung,
            ],
            anzahlKinderAngemessen: [
                angebot?.anzahlKinderAngemessen,
            ],
            betreuungsschluessel: [
                angebot?.betreuungsschluessel,
            ],
            bemerkungenPersonal: [
                angebot?.bemerkungenPersonal,
            ],
            fixerTarifKinderDerGemeinde: [
                angebot?.fixerTarifKinderDerGemeinde,
            ],
            einkommensabhaengigerTarifKinderDerGemeinde: [
                angebot?.einkommensabhaengigerTarifKinderDerGemeinde,
            ],
            tagesschuleTarifGiltFuerFerienbetreuung: [
                angebot?.tagesschuleTarifGiltFuerFerienbetreuung,
            ],
            ferienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet: [
                angebot?.ferienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet,
            ],
            kinderAusAnderenGemeindenZahlenAnderenTarif: [
                angebot?.kinderAusAnderenGemeindenZahlenAnderenTarif,
            ],
            bemerkungenTarifsystem: [
                angebot?.bemerkungenTarifsystem,
            ],
        });
        this.setBasicValidation();
    }

    protected setBasicValidation(): void {
        this.removeAllValidators();

        this.form.get('anzahlFerienwochenHerbstferien').setValidators(
            numberValidator(ValidationType.INTEGER)
        );
        this.form.get('anzahlFerienwochenWinterferien').setValidators(
            numberValidator(ValidationType.INTEGER)
        );
        this.form.get('anzahlFerienwochenFruehlingsferien').setValidators(
            numberValidator(ValidationType.INTEGER)
        );
        this.form.get('anzahlFerienwochenSommerferien').setValidators(
            numberValidator(ValidationType.INTEGER)
        );
        this.form.get('anzahlTage').setValidators(
            numberValidator(ValidationType.INTEGER)
        );
        this.form.get('anzahlStundenProBetreuungstag').setValidators(
            numberValidator(ValidationType.HALF)
        );

        this.enableAdressValidation();
        this.triggerFormValidation();
    }

    // overwrite
    protected enableFormValidation(): void {
        this.form.get('angebot').setValidators(Validators.required);
        this.enableAdressValidation();

        this.form.get('anzahlFerienwochenHerbstferien')
            .setValidators([Validators.required, numberValidator(ValidationType.INTEGER)]);
        this.form.get('anzahlFerienwochenWinterferien')
            .setValidators([Validators.required, numberValidator(ValidationType.INTEGER)]);
        this.form.get('anzahlFerienwochenFruehlingsferien')
            .setValidators([Validators.required, numberValidator(ValidationType.INTEGER)]);
        this.form.get('anzahlFerienwochenSommerferien')
            .setValidators([Validators.required, numberValidator(ValidationType.INTEGER)]);

        this.form.get('anzahlTage').setValidators([Validators.required, numberValidator(ValidationType.INTEGER)]);
        this.form.get('anzahlStundenProBetreuungstag')
            .setValidators([Validators.required, numberValidator(ValidationType.HALF)]);

        this.form.get('betreuungErfolgtTagsueber').setValidators(Validators.required);

        this.form.get('leitungDurchPersonMitAusbildung').setValidators(Validators.required);
        this.form.get('betreuungDurchPersonenMitErfahrung').setValidators(Validators.required);
        this.form.get('anzahlKinderAngemessen').setValidators(Validators.required);
        this.form.get('betreuungsschluessel')
            .setValidators([Validators.required]);
    }

    public save(): void {
        this.formAbschliessenTriggered = false;
        this.setBasicValidation();

        if (!this.form.valid) {
            this.showValidierungFehlgeschlagenErrorMessage();
            return;
        }
        this.ferienbetreuungService.saveAngebot(this.container.id, this.formToObject())
            .subscribe(() => {
                this.formValidationTriggered = false;
                this.ferienbetreuungService.updateFerienbetreuungContainerStore(this.container.id);
                this.errorService.clearAll();
                this.errorService.addMesageAsInfo(this.translate.instant('SPEICHERN_ERFOLGREICH'));
            }, err => this.handleSaveError(err));
    }

    private formToObject(): TSFerienbetreuungAngabenAngebot {
        this.angebot = this.form.value;
        this.angebot.angebotKontaktpersonVorname = this.form.value.angebotAdresse.kontaktpersonVorname;
        this.angebot.angebotKontaktpersonNachname = this.form.value.angebotAdresse.kontaktpersonNachname;
        const formAdresse = this.form.value.angebotAdresse;

        const adresse = new TSAdresse();
        Object.assign(adresse, formAdresse);
        // set only if adresse is valid
        this.angebot.angebotAdresse = (EbeguUtil.adresseValid(adresse)) ? adresse : null;

        return this.angebot;
    }

    public formularReadOnly(): boolean {
        return !(this.container?.status === FerienbetreuungAngabenStatus.IN_BEARBEITUNG_GEMEINDE ||
            this.container?.status === FerienbetreuungAngabenStatus.IN_PRUEFUNG_KANTON);
    }

    public async onAbschliessen(): Promise<void> {
        if (await this.checkReadyForAbschliessen()) {
            this.ferienbetreuungService.angebotAbschliessen(this.container.id, this.formToObject())
                .subscribe(() => this.handleSaveSuccess(), error => this.handleSaveError(error));
        }
    }

    public onFalscheAngaben(): void {
        this.ferienbetreuungService.falscheAngabenAngebot(this.container.id, this.angebot)
            .subscribe(() => this.handleSaveSuccess(), error => this.handleSaveError(error));
    }

    // tslint:disable-next-line:cognitive-complexity
    private adressValidValidator(): ValidatorFn {
        return control => {
            const strasse = control.get('strasse');
            const ort = control.get('ort');
            const plz = control.get('plz');
            const vorname = control.get('kontaktpersonVorname');
            const nachname = control.get('kontaktpersonNachname');

            let formErroneous = false;

            if (this.formAbschliessenTriggered ||
                (strasse.value || ort.value || plz.value || vorname.value || nachname.value)) {
                if (!strasse.value) {
                    strasse.setErrors({required: true});
                    formErroneous = true;
                }
                if (!ort.value) {
                    ort.setErrors({required: true});
                    formErroneous = true;
                }
                if (!plz.value) {
                    plz.setErrors({required: true});
                    formErroneous = true;
                }
                if (!vorname.value) {
                    vorname.setErrors({required: true});
                    formErroneous = true;
                }
                if (!nachname.value) {
                    nachname.setErrors({required: true});
                    formErroneous = true;
                }
            } else {
                strasse.setErrors(null);
                ort.setErrors(null);
                plz.setErrors(null);
                vorname.setErrors(null);
                nachname.setErrors(null);
            }
            return formErroneous ? {adressInvalid: true} : null;
        };
    }

    protected enableAdressValidation(): void {
        this.form.get('angebotAdresse').setValidators(this.adressValidValidator());
        this.form.get('angebotAdresse').markAllAsTouched();

        this.triggerFormValidation();
    }
}
