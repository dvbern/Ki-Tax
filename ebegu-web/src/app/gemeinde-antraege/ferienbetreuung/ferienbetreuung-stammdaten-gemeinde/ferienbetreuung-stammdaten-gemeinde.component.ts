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
import {FormBuilder, Validators} from '@angular/forms';
import {MatDialog} from '@angular/material/dialog';
import {TranslateService} from '@ngx-translate/core';
import {UIRouterGlobals} from '@uirouter/core';
import {ibanValidator} from 'ngx-iban';
import {combineLatest} from 'rxjs';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {GemeindeRS} from '../../../../gesuch/service/gemeindeRS.rest';
import {TSFerienbetreuungAngabenContainer} from '../../../../models/gemeindeantrag/TSFerienbetreuungAngabenContainer';
import {TSFerienbetreuungAngabenStammdaten} from '../../../../models/gemeindeantrag/TSFerienbetreuungAngabenStammdaten';
import {TSAdresse} from '../../../../models/TSAdresse';
import {TSBfsGemeinde} from '../../../../models/TSBfsGemeinde';
import {EbeguUtil} from '../../../../utils/EbeguUtil';
import {CONSTANTS} from '../../../core/constants/CONSTANTS';
import {ErrorService} from '../../../core/errors/service/ErrorService';
import {LogFactory} from '../../../core/logging/LogFactory';
import {WizardStepXRS} from '../../../core/service/wizardStepXRS.rest';
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

    public formFreigebenTriggered: false;
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
            this.setupFormAndPermissions(this.stammdaten, principal);
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
            ],
            stammdatenKontaktpersonEmail: [
                stammdaten?.stammdatenKontaktpersonEmail,
            ],
            iban: [
                stammdaten?.iban,
            ],
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
            vermerkAuszahlung: [
                stammdaten?.vermerkAuszahlung,
            ],
        }, {
            updateOn: 'blur',
        });
    }

    protected enableFormValidation(): void {
        this.enableAdresseStammdatenValidation();
        this.form.get('stammdatenKontaktpersonVorname').setValidators([Validators.required]);
        this.form.get('stammdatenKontaktpersonNachname').setValidators([Validators.required]);
        this.form.get('stammdatenKontaktpersonTelefon')
            .setValidators([Validators.required, Validators.pattern(CONSTANTS.PATTERN_PHONE)]);
        this.form.get('stammdatenKontaktpersonEmail')
            .setValidators([Validators.required, Validators.pattern(CONSTANTS.PATTERN_EMAIL)]);
        this.enableKontoinhaberValidation();
    }

    private enableAdresseStammdatenValidation(): void {
        const group = this.form.get('stammdatenAdresse');
        group.get('organisation').setValidators([Validators.required]);
        group.get('strasse').setValidators([Validators.required]);
        group.get('plz').setValidators([Validators.required]);
        group.get('ort').setValidators([Validators.required]);
    }

    // We make a check for auszahlungsdaten and adresse if any of their fields are set
    private enableSaveValidations(): void {
        if (this.form.get('adresseKontoinhaber').dirty
            || this.form.get('kontoinhaber').dirty
            || this.form.get('iban').dirty) {
            this.enableKontoinhaberValidation();
        }

        if (this.form.get('stammdatenAdresse').dirty) {
            this.enableAdresseStammdatenValidation();
        }

        this.triggerFormValidation();
    }

    private enableKontoinhaberValidation(): void {
        this.form.get('kontoinhaber').setValidators([Validators.required]);
        this.form.get('adresseKontoinhaber').get('strasse').setValidators([Validators.required]);
        this.form.get('adresseKontoinhaber').get('plz').setValidators([Validators.required]);
        this.form.get('adresseKontoinhaber').get('ort').setValidators([Validators.required]);
        this.form.get('iban').setValidators([Validators.required, ibanValidator()]);
    }

    public save(): void {
        this.enableSaveValidations();
        if (!this.form.valid) {
            this.showValidierungFehlgeschlagenErrorMessage();
            return;
        }
        this.ferienbetreuungService.saveStammdaten(this.container.id, this.extractFormValues())
            .subscribe(() => {
                this.ferienbetreuungService.updateFerienbetreuungContainerStore(this.container.id);
                this.errorService.addMesageAsInfo(this.translate.instant('SPEICHERN_ERFOLGREICH'));
            }, err => {
                LOG.error(err);
                this.errorService.addMesageAsError(this.translate.instant('FERIENBETREUUNG_PERSIST_ERROR'));
            });
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
        this.stammdaten.iban = this.form.get('iban').value;
        this.stammdaten.kontoinhaber = this.form.get('kontoinhaber').value;

        const adresseKontoinhaber = new TSAdresse().from(this.form.get('adresseKontoinhaber').value);
        // Felder der Adresse sind required in Backend. Deshalb müssen entweder alle oder keine gesetzt sein.
        this.stammdaten.adresseKontoinhaber =
            (EbeguUtil.adresseValid(adresseKontoinhaber)) ? adresseKontoinhaber : null;

        this.stammdaten.vermerkAuszahlung = this.form.get('vermerkAuszahlung').value;
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
            .subscribe(() => this.handleSaveSuccess(), (error: any) => this.handleSaveError(error));
    }
}
