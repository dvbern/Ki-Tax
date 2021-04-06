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
import {TranslateService} from '@ngx-translate/core';
import {GemeindeRS} from '../../../../gesuch/service/gemeindeRS.rest';
import {TSFerienbetreuungAngabenAngebot} from '../../../../models/gemeindeantrag/TSFerienbetreuungAngabenAngebot';
import {TSFerienbetreuungAngabenContainer} from '../../../../models/gemeindeantrag/TSFerienbetreuungAngabenContainer';
import {TSAdresse} from '../../../../models/TSAdresse';
import {TSBfsGemeinde} from '../../../../models/TSBfsGemeinde';
import {EbeguUtil} from '../../../../utils/EbeguUtil';
import {ErrorService} from '../../../core/errors/service/ErrorService';
import {LogFactory} from '../../../core/logging/LogFactory';
import {numberValidator, ValidationType} from '../../../shared/validators/number-validator.directive';
import {FerienbetreuungService} from '../services/ferienbetreuung.service';

const LOG = LogFactory.createLog('FerienbetreuungAngebotComponent');

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

    private angebot: TSFerienbetreuungAngabenAngebot;
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
                this.angebot = container.angabenDeklaration?.angebot;
                this.setupForm(this.angebot);
                this.cd.markForCheck();
            }, error => {
                LOG.error(error);
            });
        this.gemeindeRS.getAllBfsGemeinden().then(gemeinden => {
            this.bfsGemeinden = gemeinden;
            this.cd.markForCheck();
        });
    }

    private setupForm(angebot: TSFerienbetreuungAngabenAngebot): void {
        if (!angebot) {
            return;
        }
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
                angebot?.bemerkungenAnzahlFerienwochen
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

    public save(): void {
        this.ferienbetreuungService.saveAngebot(this.container.id, this.extractFormValues())
            .subscribe(() => {
                this.ferienbetreuungService.updateFerienbetreuungContainerStore(this.container.id);
                this.errorService.addMesageAsInfo(this.translate.instant('SPEICHERN_ERFOLGREICH'));
            }, err => {
                LOG.error(err);
                this.errorService.addMesageAsError(this.translate.instant('FERIENBETREUUNG_PERSIST_ERROR'));
            });
    }

    private extractFormValues(): TSFerienbetreuungAngabenAngebot {
        this.angebot.angebot = this.form.get('angebot').value;
        this.angebot.angebotKontaktpersonVorname = this.form.get('angebotKontaktpersonVorname').value;
        this.angebot.angebotKontaktpersonNachname = this.form.get('angebotKontaktpersonNachname').value;

        const adresse = new TSAdresse();
        adresse.strasse = this.form.get('angebotStrasse').value;
        adresse.hausnummer = this.form.get('angebotNr').value;
        adresse.plz = this.form.get('angebotPlz').value;
        adresse.ort = this.form.get('angebotOrt').value;
        // set only if adresse is valid
        this.angebot.angebotAdresse = (EbeguUtil.adresseValid(adresse)) ? adresse : null;

        this.angebot.anzahlFerienwochenHerbstferien = this.form.get('anzahlFerienwochenHerbstferien').value;
        this.angebot.anzahlFerienwochenWinterferien = this.form.get('anzahlFerienwochenWinterferien').value;
        this.angebot.anzahlFerienwochenFruehlingsferien = this.form.get('anzahlFerienwochenFruehlingsferien').value;
        this.angebot.anzahlFerienwochenSommerferien = this.form.get('anzahlFerienwochenSommerferien').value;
        this.angebot.anzahlTage = this.form.get('anzahlTage').value;
        this.angebot.bemerkungenAnzahlFerienwochen = this.form.get('bemerkungenAnzahlFerienwochen').value;
        this.angebot.anzahlStundenProBetreuungstag = this.form.get('anzahlStundenProBetreuungstag').value;
        this.angebot.betreuungErfolgtTagsueber = this.form.get('betreuungErfolgtTagsueber').value;
        this.angebot.bemerkungenOeffnungszeiten = this.form.get('bemerkungenOeffnungszeiten').value;
        this.angebot.finanziellBeteiligteGemeinden = this.form.get('finanziellBeteiligteGemeinden').value;
        this.angebot.gemeindeFuehrtAngebotSelber = this.form.get('gemeindeFuehrtAngebotSelber').value;
        this.angebot.gemeindeBeauftragtExterneAnbieter = this.form.get('gemeindeBeauftragtExterneAnbieter').value;
        this.angebot.angebotVereineUndPrivateIntegriert = this.form.get('angebotVereineUndPrivateIntegriert').value;
        this.angebot.bemerkungenKooperation = this.form.get('bemerkungenKooperation').value;
        this.angebot.leitungDurchPersonMitAusbildung = this.form.get('leitungDurchPersonMitAusbildung').value;
        this.angebot.betreuungDurchPersonenMitErfahrung = this.form.get('betreuungDurchPersonenMitErfahrung').value;
        this.angebot.anzahlKinderAngemessen = this.form.get('anzahlKinderAngemessen').value;
        this.angebot.betreuungsschluessel = this.form.get('betreuungsschluessel').value;
        this.angebot.bemerkungenPersonal = this.form.get('bemerkungenPersonal').value;
        this.angebot.fixerTarifKinderDerGemeinde = this.form.get('fixerTarifKinderDerGemeinde').value;
        this.angebot.einkommensabhaengigerTarifKinderDerGemeinde =
            this.form.get('einkommensabhaengigerTarifKinderDerGemeinde').value;
        this.angebot.tagesschuleTarifGiltFuerFerienbetreuung =
            this.form.get('tagesschuleTarifGiltFuerFerienbetreuung').value;
        this.angebot.ferienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet = this.form.get('ferienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet').value;
        this.angebot.kinderAusAnderenGemeindenZahlenAnderenTarif = this.form.get('kinderAusAnderenGemeindenZahlenAnderenTarif').value;
        this.angebot.bemerkungenTarifsystem = this.form.get('bemerkungenTarifsystem').value;
        return this.angebot;
    }
}
