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
import {FormBuilder, ValidatorFn, Validators} from '@angular/forms';
import {MatDialog} from '@angular/material/dialog';
import {TranslateService} from '@ngx-translate/core';
import {UIRouterGlobals} from '@uirouter/core';
import {ibanValidator} from 'ngx-iban';
import {combineLatest} from 'rxjs';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {GemeindeRS} from '../../../../gesuch/service/gemeindeRS.rest';
import {TSFerienbetreuungFormularStatus} from '../../../../models/enums/TSFerienbetreuungFormularStatus';
import {TSFerienbetreuungAngabenContainer} from '../../../../models/gemeindeantrag/TSFerienbetreuungAngabenContainer';
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

@Component({
    selector: 'dv-ferienbetreuung-stammdaten-gemeinde',
    templateUrl: './ferienbetreuung-stammdaten-gemeinde.component.html',
    styleUrls: ['./ferienbetreuung-stammdaten-gemeinde.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FerienbetreuungStammdatenGemeindeComponent extends AbstractFerienbetreuungFormular implements OnInit {

    public bfsGemeinden: TSBfsGemeinde[];

    private stammdaten: TSFerienbetreuungAngabenStammdaten;
    private container: TSFerienbetreuungAngabenContainer;

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
        combineLatest([
            this.ferienbetreuungService.getFerienbetreuungContainer(),
            this.authServiceRS.principal$,
        ]).subscribe(([container, principal]) => {
            this.container = container;
            this.stammdaten = container.angabenDeklaration?.stammdaten;
            this.setupFormAndPermissions(container, this.stammdaten, principal);
            this.unsavedChangesService.registerForm(this.form);
        }, error => {
            LOG.error(error);
        });
        this.gemeindeRS.getAllBfsGemeinden().then(gemeinden => {
            this.bfsGemeinden = gemeinden;
            this.cd.markForCheck();
        });
    }

    protected setupForm(stammdaten: TSFerienbetreuungAngabenStammdaten): void {
        if (!stammdaten) {
            return;
        }
        this.form = this.fb.group({
            version: [
                stammdaten?.version
            ],
            traegerschaft: [
                stammdaten?.traegerschaft,
            ],
            amAngebotBeteiligteGemeinden: [
                stammdaten?.amAngebotBeteiligteGemeinden,
            ],
            seitWannFerienbetreuungen: [
                stammdaten?.seitWannFerienbetreuungen,
            ],
            stammdatenAdresse: this.fb.group({
                organisation: [
                    stammdaten?.stammdatenAdresse?.organisation,
                ],
                zusatz: [
                    stammdaten?.stammdatenAdresse?.zusatzzeile,
                ],
                strasse: [
                    stammdaten?.stammdatenAdresse?.strasse,
                ],
                hausnummer: [
                    stammdaten?.stammdatenAdresse?.hausnummer,
                ],
                plz: [
                    stammdaten?.stammdatenAdresse?.plz,
                ],
                ort: [
                    stammdaten?.stammdatenAdresse?.ort,
                ],
            }),
            stammdatenKontaktpersonVorname: [
                stammdaten?.stammdatenKontaktpersonVorname,
            ],
            stammdatenKontaktpersonNachname: [
                stammdaten?.stammdatenKontaktpersonNachname,
            ],
            stammdatenKontaktpersonFunktion: [
                stammdaten?.stammdatenKontaktpersonFunktion,
            ],
            stammdatenKontaktpersonTelefon: [
                stammdaten?.stammdatenKontaktpersonTelefon,
                Validators.pattern(CONSTANTS.PATTERN_PHONE)
            ],
            stammdatenKontaktpersonEmail: [
                stammdaten?.stammdatenKontaktpersonEmail,
                Validators.pattern(CONSTANTS.PATTERN_EMAIL)
            ],
            auszahlungsdaten: this.fb.group({
                kontoinhaber: [
                    stammdaten?.kontoinhaber,
                ],
                adresseKontoinhaber: this.fb.group({
                    strasse: [
                        stammdaten?.adresseKontoinhaber?.strasse,
                    ],
                    hausnummer: [
                        stammdaten?.adresseKontoinhaber?.hausnummer,
                    ],
                    ort: [
                        stammdaten?.adresseKontoinhaber?.ort,
                    ],
                    plz: [
                        stammdaten?.adresseKontoinhaber?.plz,
                    ],
                }),
                iban: [
                    stammdaten?.iban,
                    ibanValidator(),
                ],
                vermerkAuszahlung: [
                    stammdaten?.vermerkAuszahlung,
                ],
            }),
        });
        this.enableStammdatenAuszahlungValidation();
    }

    // tslint:disable-next-line:cognitive-complexity
    private adressValidValidator(): ValidatorFn {
        return control => {
            const strasse = control.get('strasse');
            const ort = control.get('ort');
            const plz = control.get('plz');
            const organisation = control.get('organisation');

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

    // tslint:disable-next-line:cognitive-complexity
    private auszahlungsdatenValidation(): ValidatorFn {
        return control => {
            const kontoinhaber = control.get('kontoinhaber');
            const strasse = control.get('adresseKontoinhaber').get('strasse');
            const plz = control.get('adresseKontoinhaber').get('plz');
            const ort = control.get('adresseKontoinhaber').get('ort');
            const iban = control.get('iban');

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
        this.form.get('stammdatenKontaktpersonVorname').setValidators([Validators.required]);
        this.form.get('stammdatenKontaktpersonNachname').setValidators([Validators.required]);
        this.form.get('stammdatenKontaktpersonTelefon')
            .setValidators([Validators.required, Validators.pattern(CONSTANTS.PATTERN_PHONE)]);
        this.form.get('stammdatenKontaktpersonEmail')
            .setValidators([Validators.required, Validators.pattern(CONSTANTS.PATTERN_EMAIL)]);
    }

    public save(): void {
        this.enableStammdatenAuszahlungValidation();

        if (!this.form.valid) {
            this.showValidierungFehlgeschlagenErrorMessage();
            return;
        }
        this.ferienbetreuungService.saveStammdaten(this.container.id, this.extractFormValues())
            .subscribe(() => {
                this.formValidationTriggered = false;
                this.ferienbetreuungService.updateFerienbetreuungContainerStore(this.container.id);
                this.errorService.clearAll();
                this.errorService.addMesageAsInfo(this.translate.instant('SPEICHERN_ERFOLGREICH'));
            }, err => this.handleSaveError(err));
    }

    private extractFormValues(): TSFerienbetreuungAngabenStammdaten {
        this.stammdaten.amAngebotBeteiligteGemeinden = this.form.get('amAngebotBeteiligteGemeinden').value;
        this.stammdaten.seitWannFerienbetreuungen = this.form.get('seitWannFerienbetreuungen').value;
        this.stammdaten.traegerschaft = this.form.get('traegerschaft').value;

        const adresse = new TSAdresse().from(this.form.get('stammdatenAdresse').value);
        // Felder der Adresse sind required in Backend. Deshalb müssen entweder alle oder keine gesetzt sein.
        this.stammdaten.stammdatenAdresse = (EbeguUtil.adresseValid(adresse)) ? adresse : null;

        this.stammdaten.stammdatenKontaktpersonVorname = this.form.get('stammdatenKontaktpersonVorname').value;
        this.stammdaten.stammdatenKontaktpersonNachname = this.form.get('stammdatenKontaktpersonNachname').value;
        this.stammdaten.stammdatenKontaktpersonFunktion = this.form.get('stammdatenKontaktpersonFunktion').value;
        this.stammdaten.stammdatenKontaktpersonTelefon = this.form.get('stammdatenKontaktpersonTelefon').value;
        this.stammdaten.stammdatenKontaktpersonEmail = this.form.get('stammdatenKontaktpersonEmail').value;
        this.stammdaten.iban = this.form.get('auszahlungsdaten').get('iban').value;
        this.stammdaten.kontoinhaber = this.form.get('auszahlungsdaten').get('kontoinhaber').value;

        const adresseKontoinhaber = new TSAdresse().from(this.form.get('auszahlungsdaten')
            .get('adresseKontoinhaber').value);
        // Felder der Adresse sind required in Backend. Deshalb müssen entweder alle oder keine gesetzt sein.
        this.stammdaten.adresseKontoinhaber =
            (EbeguUtil.adresseValid(adresseKontoinhaber)) ? adresseKontoinhaber : null;

        this.stammdaten.vermerkAuszahlung = this.form.get('auszahlungsdaten').get('vermerkAuszahlung').value;
        return this.stammdaten;
    }

    public fillAdress(): void {
        const gemeinde = this.container.gemeinde;
        this.gemeindeRS.getGemeindeStammdaten(gemeinde.id).then(stammdaten => {
            const adresse = stammdaten.extractTsAdresse();
            this.form.get('stammdatenAdresse').get('organisation').setValue(adresse?.organisation);
            this.form.get('stammdatenAdresse').get('strasse').setValue(adresse?.strasse);
            this.form.get('stammdatenAdresse').get('hausnummer').setValue(adresse?.hausnummer);
            this.form.get('stammdatenAdresse').get('plz').setValue(adresse?.plz);
            this.form.get('stammdatenAdresse').get('ort').setValue(adresse.ort);
        }, err => {
            this.errorService.addMesageAsError(this.translate.instant('FERIENBETREUUNG_FEHLER_ABRUF_INFORMATIONEN'));
            LOG.error(err);
        });
    }

    public fillBenutzer(): void {
        const benutzer = this.authServiceRS.getPrincipal();
        this.form.get('stammdatenKontaktpersonVorname').setValue(benutzer.vorname);
        this.form.get('stammdatenKontaktpersonNachname').setValue(benutzer.nachname);
        this.form.get('stammdatenKontaktpersonEmail').setValue(benutzer.email);
    }

    public async onAbschliessen(): Promise<void> {
        if (await this.checkReadyForAbschliessen()) {
            this.ferienbetreuungService.stammdatenAbschliessen(this.container.id, this.extractFormValues())
                .subscribe(() => this.handleSaveSuccess(), error => this.handleSaveError(error));
        }
    }

    public onFalscheAngaben(): void {
        this.ferienbetreuungService.falscheAngabenStammdaten(this.container.id, this.extractFormValues())
            .subscribe(() => this.handleSaveSuccess(), error => this.handleSaveError(error));
    }

    private enableStammdatenAuszahlungValidation(): void {
        this.form.get('stammdatenAdresse').setValidators(this.adressValidValidator());
        this.form.get('stammdatenAdresse').markAllAsTouched();
        this.form.get('stammdatenAdresse').get('organisation').markAllAsTouched();
        this.form.get('auszahlungsdaten').setValidators(this.auszahlungsdatenValidation());
        this.form.get('auszahlungsdaten').markAllAsTouched();

        this.triggerFormValidation();
    }

    public fillActionsVisible(): boolean {
        return this.stammdaten?.status === TSFerienbetreuungFormularStatus.IN_BEARBEITUNG_GEMEINDE;
    }
}
