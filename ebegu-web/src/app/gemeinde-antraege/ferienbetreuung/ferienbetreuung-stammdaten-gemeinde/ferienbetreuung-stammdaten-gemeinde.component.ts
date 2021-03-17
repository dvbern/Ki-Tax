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
import {GemeindeRS} from '../../../../gesuch/service/gemeindeRS.rest';
import {TSFerienbetreuungAngaben} from '../../../../models/gemeindeantrag/TSFerienbetreuungAngaben';
import {TSBfsGemeinde} from '../../../../models/TSBfsGemeinde';
import {FerienbetreuungService} from '../services/ferienbetreuung.service';

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

    public constructor(
        private readonly ferienbetreuungService: FerienbetreuungService,
        private readonly fb: FormBuilder,
        private readonly cd: ChangeDetectorRef,
        private readonly gemeindeRS: GemeindeRS
    ) {
    }

    public ngOnInit(): void {
        this.ferienbetreuungService.getFerienbetreuungContainer()
            .subscribe(container => {
                const fbAngaben = container.angabenDeklaration;
                this.setupForm(fbAngaben);
                this.cd.markForCheck();
            });
        this.gemeindeRS.getAllBfsGemeinden().then(gemeinden => {
            this.bfsGemeinden = gemeinden;
            this.cd.markForCheck();
        });
    }

    private setupForm(angaben: TSFerienbetreuungAngaben): void {
        if (!angaben.stammdaten) {
            return;
        }
        const stammdaten = angaben.stammdaten;
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
                stammdaten?.stammdatenKontaktpersonTelefon
            ],
            stammdatenKontaktpersonEmail: [
                stammdaten?.stammdatenKontaktpersonEmail
            ],
            iban: [
                stammdaten?.iban
            ],
            kontoinhaber: [
                stammdaten?.kontoinhaber
            ],
            adresseKontoinhaber: [
                stammdaten?.adresseKontoinhaber
            ],
            vermerkAuszahlung: [
                stammdaten?.vermerkAuszahlung
            ],
        });
    }

    public onFormSubmit(): void {
        // TODO implement
    }
}
