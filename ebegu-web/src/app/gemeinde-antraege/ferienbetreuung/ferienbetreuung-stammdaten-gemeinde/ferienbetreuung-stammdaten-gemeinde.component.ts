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
import {TranslateService} from '@ngx-translate/core';
import {ibanValidator} from 'ngx-iban';
import {GemeindeRS} from '../../../../gesuch/service/gemeindeRS.rest';
import {TSFerienbetreuungAngabenContainer} from '../../../../models/gemeindeantrag/TSFerienbetreuungAngabenContainer';
import {TSFerienbetreuungAngabenStammdaten} from '../../../../models/gemeindeantrag/TSFerienbetreuungAngabenStammdaten';
import {TSAdresse} from '../../../../models/TSAdresse';
import {TSBfsGemeinde} from '../../../../models/TSBfsGemeinde';
import {EbeguUtil} from '../../../../utils/EbeguUtil';
import {CONSTANTS} from '../../../core/constants/CONSTANTS';
import {ErrorService} from '../../../core/errors/service/ErrorService';
import {LogFactory} from '../../../core/logging/LogFactory';
import {FerienbetreuungService} from '../services/ferienbetreuung.service';

const LOG = LogFactory.createLog('FerienbetreuungStammdatenGemeindeComponent');

@Component({
    selector: 'dv-ferienbetreuung-stammdaten-gemeinde',
    templateUrl: './ferienbetreuung-stammdaten-gemeinde.component.html',
    styleUrls: ['./ferienbetreuung-stammdaten-gemeinde.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class FerienbetreuungStammdatenGemeindeComponent implements OnInit {

    public form: FormGroup;
    public formFreigebenTriggered: false;
    public bfsGemeinden: TSBfsGemeinde[];

    private stammdaten: TSFerienbetreuungAngabenStammdaten;
    private container: TSFerienbetreuungAngabenContainer;

    public constructor(
        private readonly ferienbetreuungService: FerienbetreuungService,
        private readonly fb: FormBuilder,
        private readonly cd: ChangeDetectorRef,
        private readonly gemeindeRS: GemeindeRS,
        private readonly errorService: ErrorService,
        private readonly translate: TranslateService
    ) {
    }

    public ngOnInit(): void {
        this.ferienbetreuungService.getFerienbetreuungContainer()
            .subscribe(container => {
                this.container = container;
                this.stammdaten = container.angabenDeklaration?.stammdaten;
                this.setupForm(this.stammdaten);
                this.cd.markForCheck();
            }, error => {
                LOG.error(error);
            });
        this.gemeindeRS.getAllBfsGemeinden().then(gemeinden => {
            this.bfsGemeinden = gemeinden;
            this.cd.markForCheck();
        });
    }

    private setupForm(stammdaten: TSFerienbetreuungAngabenStammdaten): void {
        if (!stammdaten) {
            return;
        }
        this.form = this.fb.group({
            traegerschaft: [
                stammdaten?.traegerschaft
            ],
            amAngebotBeteiligteGemeinden: [
                stammdaten?.amAngebotBeteiligteGemeinden
            ],
            seitWannFerienbetreuungen: [
                stammdaten?.seitWannFerienbetreuungen
            ],
            stammdatenAdresseAnschrift: [
                stammdaten?.stammdatenAdresse?.organisation
            ],
            stammdatenAdresseZusatz: [
                stammdaten?.stammdatenAdresse?.zusatzzeile
            ],
            stammdatenAdresseStrasse: [
                stammdaten?.stammdatenAdresse?.strasse
            ],
            stammdatenAdresseNr: [
                stammdaten?.stammdatenAdresse?.hausnummer
            ],
            stammdatenAdressePlz: [
                stammdaten?.stammdatenAdresse?.plz
            ],
            stammdatenAdresseOrt: [
                stammdaten?.stammdatenAdresse?.ort
            ],
            stammdatenKontaktpersonVorname: [
                stammdaten?.stammdatenKontaktpersonVorname
            ],
            stammdatenKontaktpersonNachname: [
                stammdaten?.stammdatenKontaktpersonNachname
            ],
            stammdatenKontaktpersonFunktion: [
                stammdaten?.stammdatenKontaktpersonFunktion
            ],
            stammdatenKontaktpersonTelefon: [
                stammdaten?.stammdatenKontaktpersonTelefon,
                Validators.pattern(CONSTANTS.PATTERN_PHONE)
            ],
            stammdatenKontaktpersonEmail: [
                stammdaten?.stammdatenKontaktpersonEmail,
                Validators.pattern(CONSTANTS.PATTERN_EMAIL),
            ],
            iban: [
                stammdaten?.iban,
                ibanValidator()
            ],
            kontoinhaber: [
                stammdaten?.kontoinhaber
            ],
            adresseKontoinhaberStrasse: [
                stammdaten?.adresseKontoinhaber?.strasse
            ],
            adresseKontoinhaberNr: [
                stammdaten?.adresseKontoinhaber?.hausnummer
            ],
            adresseKontoinhaberOrt: [
                stammdaten?.adresseKontoinhaber?.ort
            ],
            adresseKontoinhaberPlz: [
                stammdaten?.adresseKontoinhaber?.plz
            ],
            vermerkAuszahlung: [
                stammdaten?.vermerkAuszahlung
            ],
        }, {
            updateOn: 'blur'
        });
    }

    public save(): void {
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

        const adresse = new TSAdresse();
        adresse.organisation = this.form.get('stammdatenAdresseAnschrift').value;
        adresse.zusatzzeile = this.form.get('stammdatenAdresseZusatz').value;
        adresse.strasse = this.form.get('stammdatenAdresseStrasse').value;
        adresse.hausnummer = this.form.get('stammdatenAdresseNr').value;
        adresse.plz = this.form.get('stammdatenAdressePlz').value;
        adresse.ort = this.form.get('stammdatenAdresseOrt').value;
        // Felder der Adresse sind required in Backend. Deshalb müssen entweder alle oder keine gesetzt sein.
        this.stammdaten.stammdatenAdresse = (EbeguUtil.adresseValid(adresse)) ? adresse : null;

        this.stammdaten.stammdatenKontaktpersonVorname = this.form.get('stammdatenKontaktpersonVorname').value;
        this.stammdaten.stammdatenKontaktpersonNachname = this.form.get('stammdatenKontaktpersonNachname').value;
        this.stammdaten.stammdatenKontaktpersonFunktion = this.form.get('stammdatenKontaktpersonFunktion').value;
        this.stammdaten.stammdatenKontaktpersonTelefon = this.form.get('stammdatenKontaktpersonTelefon').value;
        this.stammdaten.stammdatenKontaktpersonEmail = this.form.get('stammdatenKontaktpersonEmail').value;
        this.stammdaten.iban = this.form.get('iban').value;
        this.stammdaten.kontoinhaber = this.form.get('kontoinhaber').value;

        const adresseKontoinhaber = new TSAdresse();
        adresseKontoinhaber.strasse = this.form.get('adresseKontoinhaberStrasse').value;
        adresseKontoinhaber.hausnummer = this.form.get('adresseKontoinhaberNr').value;
        adresseKontoinhaber.plz = this.form.get('adresseKontoinhaberOrt').value;
        adresseKontoinhaber.ort = this.form.get('adresseKontoinhaberPlz').value;
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
            this.form.get('stammdatenAdresseAnschrift').setValue(adresse?.organisation);
            this.form.get('stammdatenAdresseStrasse').setValue(adresse?.strasse);
            this.form.get('stammdatenAdresseNr').setValue(adresse?.hausnummer);
            this.form.get('stammdatenAdressePlz').setValue(adresse?.plz);
            this.form.get('stammdatenAdresseOrt').setValue(adresse.ort);
        }, err => {
            this.errorService.addMesageAsError(this.translate.instant('FERIENBETREUUNG_FEHLER_ABRUF_INFORMATIONEN'));
            LOG.error(err);
        });
    }
}
