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
import {numberValidator, ValidationType} from '../../../shared/validators/number-validator.directive';
import {FerienbetreuungService} from '../services/ferienbetreuung.service';

@Component({
    selector: 'dv-ferienbetreuung-angebot',
    templateUrl: './ferienbetreuung-angebot.component.html',
    styleUrls: ['./ferienbetreuung-angebot.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class FerienbetreuungAngebotComponent implements OnInit {

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
        if (!angaben.angebot) {
            return;
        }
        const angebot = angaben.angebot;
        this.form = this.fb.group({
            angebot: [
                angebot?.angebot
            ],
            angebotKontaktpersonVorname: [
                angebot?.angebotKontaktpersonVorname
            ],
            angebotKontaktpersonNachname: [
                angebot?.angebotKontaktpersonNachname
            ],
            angebotStrasse: [
                angebot?.angebotAdresse?.strasse
            ],
            angebotNr: [
                angebot?.angebotAdresse?.hausnummer
            ],
            angebotPlz: [
                angebot?.angebotAdresse?.plz
            ],
            angebotOrt: [
                angebot?.angebotAdresse?.ort
            ],
            anzahlFerienwochenHerbstferien: [
                angebot?.anzahlFerienwochenHerbstferien,
                numberValidator(ValidationType.INTEGER)
            ],
            anzahlFerienwochenWinterferien: [
                angebot?.anzahlFerienwochenWinterferien,
                numberValidator(ValidationType.INTEGER)
            ],
            anzahlFerienwochenFruehlingsferien: [
                angebot?.anzahlFerienwochenFruehlingsferien,
                numberValidator(ValidationType.INTEGER)
            ],
            anzahlFerienwochenSommerferien: [
                angebot?.anzahlFerienwochenSommerferien,
                numberValidator(ValidationType.INTEGER)
            ],
            anzahlTage: [
                angebot?.anzahlTage,
                numberValidator(ValidationType.INTEGER)
            ],
            bemerkungenAnzahlFerienwochen: [
                // TODO: add this property
            ],
            anzahlStundenProBetreuungstag: [
                angebot?.anzahlStundenProBetreuungstag
            ],
            betreuungErfolgtTagsueber: [
                angebot?.betreuungErfolgtTagsueber
            ],
            bemerkungenOeffnungszeiten: [
                angebot?.bemerkungenOeffnungszeiten
            ],
            finanziellBeteiligteGemeinden: [
                angebot?.finanziellBeteiligteGemeinden
            ],
            gemeindeFuehrtAngebotSelber: [
                angebot?.gemeindeFuehrtAngebotSelber
            ],
            gemeindeBeauftragtExterneAnbieter: [
                angebot?.gemeindeBeauftragtExterneAnbieter
            ],
            angebotVereineUndPrivateIntegriert: [
                angebot?.angebotVereineUndPrivateIntegriert
            ],
            bemerkungenKooperation: [
                angebot?.bemerkungenKooperation
            ],
            leitungDurchPersonMitAusbildung: [
                angebot?.leitungDurchPersonMitAusbildung
            ],
            betreuungDurchPersonenMitErfahrung: [
                angebot?.betreuungDurchPersonenMitErfahrung
            ],
            anzahlKinderAngemessen: [
                angebot?.anzahlKinderAngemessen
            ],
            betreuungsschluessel: [
                angebot?.betreuungsschluessel
            ],
            bemerkungenPersonal: [
                angebot?.bemerkungenPersonal
            ],
            fixerTarifKinderDerGemeinde: [
                angebot?.fixerTarifKinderDerGemeinde
            ],
            einkommensabhaengigerTarifKinderDerGemeinde: [
                angebot?.einkommensabhaengigerTarifKinderDerGemeinde
            ],
            tagesschuleTarifGiltFuerFerienbetreuung: [
                angebot?.tagesschuleTarifGiltFuerFerienbetreuung
            ],
            ferienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet: [
                angebot?.ferienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet
            ],
            kinderAusAnderenGemeindenZahlenAnderenTarif: [
                angebot?.kinderAusAnderenGemeindenZahlenAnderenTarif
            ],
            bemerkungenTarifsystem: [
                angebot?.bemerkungenTarifsystem
            ],
        }, {
            updateOn: 'blur'
        });
    }

    public onFormSubmit(): void {
        // todo
    }
}
