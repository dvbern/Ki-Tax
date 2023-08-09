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
import {FormBuilder, FormControl, FormGroup, ValidatorFn, Validators} from '@angular/forms';
import {MAT_DATE_FORMATS} from '@angular/material/core';
import {MatDatepicker} from '@angular/material/datepicker';
import {MatDialog} from '@angular/material/dialog';
import {TranslateService} from '@ngx-translate/core';
import {UIRouterGlobals} from '@uirouter/core';
import {Moment} from 'moment';
import * as moment from 'moment';
import {ibanValidator} from 'ngx-iban';
import {combineLatest, Subscription} from 'rxjs';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {GemeindeRS} from '../../../../gesuch/service/gemeindeRS.rest';
import {TSFerienbetreuungFormularStatus} from '../../../../models/enums/TSFerienbetreuungFormularStatus';
import {TSFerienbetreuungAngabenStammdaten} from '../../../../models/gemeindeantrag/TSFerienbetreuungAngabenStammdaten';
import {TSAdresse} from '../../../../models/TSAdresse';
import {TSBfsGemeinde} from '../../../../models/TSBfsGemeinde';
import {EbeguUtil} from '../../../../utils/EbeguUtil';
import {CONSTANTS} from '../../../core/constants/CONSTANTS';
import {ErrorService} from '../../../core/errors/service/ErrorService';
import {LogFactory} from '../../../core/logging/LogFactory';
import {WizardStepXRS} from '../../../core/service/wizardStepXRS.rest';
import {UnsavedChangesService} from '../../services/unsaved-changes.service';
import {AbstractFerienbetreuungFormular} from '../abstract.ferienbetreuung-formular';
import {FerienbetreuungService} from '../services/ferienbetreuung.service';

const LOG = LogFactory.createLog('FerienbetreuungStammdatenGemeindeComponent');

export const MY_FORMATS = {
    parse: {
        dateInput: 'MM/YYYY'
    },
    display: {
        dateInput: 'MM/YYYY',
        monthYearLabel: 'MMM YYYY',
        dateA11yLabel: 'LL',
        monthYearA11yLabel: 'MMMM YYYY'
    }
};

@Component({
    selector: 'dv-ferienbetreuung-stammdaten-gemeinde',
    templateUrl: './ferienbetreuung-stammdaten-gemeinde.component.html',
    styleUrls: ['./ferienbetreuung-stammdaten-gemeinde.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    providers: [
        {provide: MAT_DATE_FORMATS, useValue: MY_FORMATS}
    ]
})
export class FerienbetreuungStammdatenGemeindeComponent extends AbstractFerienbetreuungFormular implements OnInit, OnDestroy {

    public bfsGemeinden: TSBfsGemeinde[];

    private stammdaten: TSFerienbetreuungAngabenStammdaten;
    private subscription: Subscription;

    public date?: moment.Moment;

    public form = this.fb.group({
        traegerschaft: [<null | string> null],
        amAngebotBeteiligteGemeinden: [<null | string[]> null],
        seitWannFerienbetreuungen: [<null | Moment> null],
        stammdatenAdresse: this.fb.group({
            organisation: [<null | string> null],
            zusatzzeile: [<null | string> null],
            strasse: [<null | string> null],
            hausnummer: [<null | string> null],
            plz: [<null | string> null],
            ort: [<null | string> null],
        }),
        stammdatenKontaktpersonVorname: [<null | string> null],
        stammdatenKontaktpersonNachname: [<null | string> null],
        stammdatenKontaktpersonFunktion: [<null | string> null],
        stammdatenKontaktpersonTelefon: [<null | string> null],
        stammdatenKontaktpersonEmail: [<null | string> null],
        auszahlungsdaten: this.fb.group({
            kontoinhaber: [<null | string> null],
            adresseKontoinhaber: this.fb.group({
                strasse: [<null | string> null],
                hausnummer: [<null | string> null],
                ort: [<null | string> null],
                plz: [<null | string> null],
                zusatzzeile: [<null | string> null],
            }),
            iban: [
                <null | string> null,
                ibanValidator()
            ],
            vermerkAuszahlung: [<null | string> null]
        })
    });

    public constructor(
        protected readonly errorService: ErrorService,
        protected readonly translate: TranslateService,
        protected readonly cd: ChangeDetectorRef,
        protected readonly dialog: MatDialog,
        protected readonly uiRouterGlobals: UIRouterGlobals,
        protected readonly wizardRS: WizardStepXRS,
        private readonly ferienbetreuungService: FerienbetreuungService,
        private readonly fb: FormBuilder,
        private readonly gemeindeRS: GemeindeRS,
        private readonly authServiceRS: AuthServiceRS,
        private readonly unsavedChangesService: UnsavedChangesService
    ) {
        super(errorService, translate, dialog, cd, wizardRS, uiRouterGlobals);
    }

    public ngOnInit(): void {
        this.subscription = combineLatest([
            this.ferienbetreuungService.getFerienbetreuungContainer(),
            this.authServiceRS.principal$
        ]).subscribe(([container, principal]) => {
            this.container = container;
            this.stammdaten = container.isAtLeastInPruefungKantonOrZurueckgegeben() ?
                container.angabenKorrektur?.stammdaten : container.angabenDeklaration?.stammdaten;
            this.setupFormAndPermissions(container, this.stammdaten, principal);
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

    protected setupForm(stammdaten: TSFerienbetreuungAngabenStammdaten): void {
        if (!stammdaten) {
            return;
        }
        this.form.patchValue({
            amAngebotBeteiligteGemeinden: stammdaten?.amAngebotBeteiligteGemeinden,
            seitWannFerienbetreuungen: stammdaten?.seitWannFerienbetreuungen,
            traegerschaft: stammdaten?.traegerschaft,
            stammdatenKontaktpersonEmail: stammdaten?.stammdatenKontaktpersonEmail,
            stammdatenKontaktpersonNachname: stammdaten?.stammdatenKontaktpersonNachname,
            stammdatenKontaktpersonVorname: stammdaten?.stammdatenKontaktpersonVorname,
            stammdatenKontaktpersonTelefon: stammdaten?.stammdatenKontaktpersonTelefon,
            stammdatenKontaktpersonFunktion: stammdaten?.stammdatenKontaktpersonFunktion,
            auszahlungsdaten: {
                kontoinhaber: stammdaten?.kontoinhaber,
                vermerkAuszahlung: stammdaten?.vermerkAuszahlung,
                iban: stammdaten?.iban,
                adresseKontoinhaber: {
                    strasse: stammdaten?.adresseKontoinhaber?.strasse,
                    plz: stammdaten?.adresseKontoinhaber?.plz,
                    hausnummer: stammdaten?.adresseKontoinhaber?.hausnummer,
                    zusatzzeile: stammdaten?.adresseKontoinhaber?.zusatzzeile,
                    ort: stammdaten?.adresseKontoinhaber?.ort
                }
            },
            stammdatenAdresse: {
                strasse: stammdaten?.stammdatenAdresse?.strasse,
                plz: stammdaten?.stammdatenAdresse?.plz,
                hausnummer: stammdaten?.stammdatenAdresse?.hausnummer,
                zusatzzeile: stammdaten?.stammdatenAdresse?.zusatzzeile,
                ort: stammdaten?.stammdatenAdresse?.ort,
                organisation: stammdaten?.stammdatenAdresse?.organisation

            }
        });
        this.setBasicValidation();
    }

    protected setBasicValidation(): void {
        this.removeAllValidators();

        this.form.controls.stammdatenKontaktpersonTelefon.setValidators(
            Validators.pattern(CONSTANTS.PATTERN_PHONE)
        );
        this.form.controls.stammdatenKontaktpersonEmail.setValidators(
            Validators.pattern(CONSTANTS.PATTERN_EMAIL)
        );

        this.enableStammdatenAuszahlungValidation();
        this.triggerFormValidation();
    }

    // eslint-disable-next-line
    private adressValidValidator(): ValidatorFn {
        return (control: FormGroup<{
            strasse: FormControl<null | string>;
            ort: FormControl<null | string>;
            plz: FormControl<null | string>;
            organisation: FormControl<null | string>;
        }>)=> {
            const strasse = control.controls.strasse;
            const ort = control.controls.ort;
            const plz = control.controls.plz;
            const organisation = control.controls.organisation;

            let formErroneous = false;

            if (this.formAbschliessenTriggered || (strasse.value || ort.value || plz.value || organisation.value)) {
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
                if (!organisation.value) {
                    organisation.setErrors({required: true});
                    formErroneous = true;
                }
            } else {
                strasse.setErrors(null);
                ort.setErrors(null);
                plz.setErrors(null);
                organisation.setErrors(null);
            }
            return formErroneous ? {adressInvalid: true} : null;
        };
    }

    // eslint-disable-next-line
    private auszahlungsdatenValidation(): ValidatorFn {
        return (control: FormGroup<{
            kontoinhaber: FormControl<null | string>;
            iban: FormControl<null | string>;
            adresseKontoinhaber: FormGroup<{
                strasse: FormControl<null | string>;
                hausnummer: FormControl<null | string>;
                ort: FormControl<null | string>;
                plz: FormControl<null | string>;
                zusatzzeile: FormControl<null | string>;
            }>;
        }>) => {
            const kontoinhaber = control.controls.kontoinhaber;
            const strasse = control.controls.adresseKontoinhaber.controls.strasse;
            const plz = control.controls.adresseKontoinhaber.controls.plz;
            const ort = control.controls.adresseKontoinhaber.controls.ort;
            const iban = control.controls.iban;

            let formErroneous = false;

            if (this.formAbschliessenTriggered ||
                (strasse.value || ort.value || plz.value || kontoinhaber.value || iban.value)) {
                if (!strasse.value) {
                    strasse.setErrors({required: true});
                    formErroneous = true;
                }
                if (!kontoinhaber.value) {
                    kontoinhaber.setErrors({required: true});
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
                if (!iban.value) {
                    iban.setErrors({required: true});
                    formErroneous = true;
                }
            } else {
                strasse.setErrors(null);
                ort.setErrors(null);
                plz.setErrors(null);
                kontoinhaber.setErrors(null);
                iban.setErrors(null);
            }
            return formErroneous ? {adressInvalid: true} : null;
        };
    }

    protected enableFormValidation(): void {
        this.enableStammdatenAuszahlungValidation();
        this.form.controls.stammdatenKontaktpersonVorname.setValidators([Validators.required]);
        this.form.controls.stammdatenKontaktpersonNachname.setValidators([Validators.required]);
        this.form.controls.stammdatenKontaktpersonTelefon.setValidators([Validators.required, Validators.pattern(CONSTANTS.PATTERN_PHONE)]);
        this.form.controls.stammdatenKontaktpersonEmail.setValidators([Validators.required, Validators.pattern(CONSTANTS.PATTERN_EMAIL)]);
    }

    public save(): void {
        this.formAbschliessenTriggered = false;
        this.setBasicValidation();

        if (!this.form.valid) {
            this.showValidierungFehlgeschlagenErrorMessage();
            return;
        }
        this.ferienbetreuungService.saveStammdaten(this.container.id, this.extractFormValues())
            .subscribe(() => {
                this.formValidationTriggered = false;
                this.ferienbetreuungService.updateFerienbetreuungContainerStores(this.container.id);
                this.errorService.clearAll();
                this.errorService.addMesageAsInfo(this.translate.instant('SPEICHERN_ERFOLGREICH'));
            }, err => this.handleSaveErrors(err));
    }

    private extractFormValues(): TSFerienbetreuungAngabenStammdaten {
        this.stammdaten.amAngebotBeteiligteGemeinden = this.form.value.amAngebotBeteiligteGemeinden;
        this.stammdaten.seitWannFerienbetreuungen = this.form.value.seitWannFerienbetreuungen;
        this.stammdaten.traegerschaft = this.form.value.traegerschaft;

        const adresse = new TSAdresse().from(this.form.value.stammdatenAdresse);
        adresse.id = this.stammdaten.stammdatenAdresse?.id;
        adresse.version = this.stammdaten.stammdatenAdresse?.version;
        // Felder der Adresse sind required in Backend. Deshalb müssen entweder alle oder keine gesetzt sein.
        this.stammdaten.stammdatenAdresse = (EbeguUtil.adresseValid(adresse)) ? adresse : null;

        this.stammdaten.stammdatenKontaktpersonVorname = this.form.value.stammdatenKontaktpersonVorname;
        this.stammdaten.stammdatenKontaktpersonNachname = this.form.value.stammdatenKontaktpersonNachname;
        this.stammdaten.stammdatenKontaktpersonFunktion = this.form.value.stammdatenKontaktpersonFunktion;
        this.stammdaten.stammdatenKontaktpersonTelefon = this.form.value.stammdatenKontaktpersonTelefon;
        this.stammdaten.stammdatenKontaktpersonEmail = this.form.value.stammdatenKontaktpersonEmail;
        this.stammdaten.iban = this.form.controls.auszahlungsdaten.value.iban.toUpperCase();
        this.stammdaten.kontoinhaber = this.form.value.auszahlungsdaten.kontoinhaber;

        const adresseKontoinhaber = new TSAdresse().from(this.form.value.auszahlungsdaten.adresseKontoinhaber);
        adresseKontoinhaber.id = this.stammdaten.adresseKontoinhaber?.id;
        adresseKontoinhaber.version = this.stammdaten.adresseKontoinhaber?.version;
        // Felder der Adresse sind required in Backend. Deshalb müssen entweder alle oder keine gesetzt sein.
        this.stammdaten.adresseKontoinhaber =
            (EbeguUtil.adresseValid(adresseKontoinhaber)) ? adresseKontoinhaber : null;

        this.stammdaten.vermerkAuszahlung = this.form.value.auszahlungsdaten.vermerkAuszahlung;
        return this.stammdaten;
    }

    public fillAdress(): void {
        const gemeinde = this.container.gemeinde;
        this.gemeindeRS.getGemeindeStammdaten(gemeinde.id).then(stammdaten => {
            const adresse = stammdaten.extractTsAdresse();
            this.form.controls.stammdatenAdresse.controls.organisation.setValue(adresse?.organisation);
            this.form.controls.stammdatenAdresse.controls.strasse.setValue(adresse?.strasse);
            this.form.controls.stammdatenAdresse.controls.hausnummer.setValue(adresse?.hausnummer);
            this.form.controls.stammdatenAdresse.controls.plz.setValue(adresse?.plz);
            this.form.controls.stammdatenAdresse.controls.ort.setValue(adresse.ort);
        }, err => {
            this.errorService.addMesageAsError(this.translate.instant('FERIENBETREUUNG_FEHLER_ABRUF_INFORMATIONEN'));
            LOG.error(err);
        });
    }

    public fillBenutzer(): void {
        const benutzer = this.authServiceRS.getPrincipal();
        this.form.controls.stammdatenKontaktpersonVorname.setValue(benutzer.vorname);
        this.form.controls.stammdatenKontaktpersonNachname.setValue(benutzer.nachname);
        this.form.controls.stammdatenKontaktpersonEmail.setValue(benutzer.email);
    }

    public async onAbschliessen(): Promise<void> {
        if (await this.checkReadyForAbschliessen()) {
            this.ferienbetreuungService.stammdatenAbschliessen(this.container.id, this.extractFormValues())
                .subscribe(() => this.handleSaveSuccess(), error => this.handleSaveErrors(error));
        }
    }

    public onFalscheAngaben(): void {
        this.ferienbetreuungService.falscheAngabenStammdaten(this.container.id, this.extractFormValues())
            .subscribe(() => this.handleSaveSuccess(), error => this.handleSaveErrors(error));
    }

    private enableStammdatenAuszahlungValidation(): void {
        this.form.controls.stammdatenAdresse.setValidators(this.adressValidValidator());
        this.form.controls.stammdatenAdresse.markAllAsTouched();
        this.form.controls.stammdatenAdresse.controls.organisation.markAllAsTouched();
        this.form.controls.auszahlungsdaten.setValidators(this.auszahlungsdatenValidation());
        this.form.controls.auszahlungsdaten.markAllAsTouched();

        this.triggerFormValidation();
    }

    public fillActionsVisible(): boolean {
        return this.stammdaten?.status === TSFerienbetreuungFormularStatus.IN_BEARBEITUNG_GEMEINDE;
    }

    public chosenYearHandler(normalizedYear: moment.Moment): void {
        const control = this.form.controls.seitWannFerienbetreuungen;
        const ctrlValue = control.value || moment();
        ctrlValue.year(normalizedYear.year());
        control.setValue(ctrlValue);
    }

    public chosenMonthHandler(normalizedMonth: moment.Moment, datepicker: MatDatepicker<moment.Moment>): void {
        const control = this.form.controls.seitWannFerienbetreuungen;
        const ctrlValue = control.value || moment();
        ctrlValue.month(normalizedMonth.month());
        control.setValue(ctrlValue.startOf('month'));
        datepicker.close();
    }
}
