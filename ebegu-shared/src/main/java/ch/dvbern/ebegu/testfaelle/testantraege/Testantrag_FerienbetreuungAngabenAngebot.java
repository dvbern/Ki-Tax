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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.testfaelle.testantraege;

import java.math.BigDecimal;

import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenAngebot;
import ch.dvbern.ebegu.enums.gemeindeantrag.FerienbetreuungAngabenStatus;
import ch.dvbern.ebegu.enums.gemeindeantrag.FerienbetreuungFormularStatus;

public class Testantrag_FerienbetreuungAngabenAngebot {

	private FerienbetreuungAngabenAngebot angebot;

	public Testantrag_FerienbetreuungAngabenAngebot(FerienbetreuungAngabenStatus status) {
		this.angebot = new FerienbetreuungAngabenAngebot();
		this.angebot.setAngebot("Ferienbetreuung");
		this.angebot.setAngebotKontaktpersonVorname("Lena");
		this.angebot.setAngebotKontaktpersonNachname("Musterfrau");
		Adresse testAdresse = new Adresse();
		testAdresse.setStrasse("Teststrasse");
		testAdresse.setPlz("3000");
		testAdresse.setOrt("Bern");
		this.angebot.setAngebotAdresse(testAdresse);
		this.angebot.setAnzahlFerienwochenFruehlingsferien(new BigDecimal(2));
		this.angebot.setAnzahlFerienwochenSommerferien(new BigDecimal(5));
		this.angebot.setAnzahlFerienwochenHerbstferien(new BigDecimal(3));
		this.angebot.setAnzahlFerienwochenWinterferien(new BigDecimal(2));
		this.angebot.setAnzahlTage(new BigDecimal(2));

		this.angebot.setLeitungDurchPersonMitAusbildung(true);
		this.angebot.setAnzahlKinderAngemessen(true);
		this.angebot.setBetreuungsschluessel(new BigDecimal(25));

		this.angebot.setAnzahlStundenProBetreuungstag(new BigDecimal(8));
		this.angebot.setBetreuungDurchPersonenMitErfahrung(true);
		this.angebot.setBetreuungErfolgtTagsueber(true);

		if(status == FerienbetreuungAngabenStatus.IN_BEARBEITUNG_GEMEINDE) {
			this.angebot.setStatus(FerienbetreuungFormularStatus.IN_BEARBEITUNG_GEMEINDE);
		} else {
			this.angebot.setStatus(FerienbetreuungFormularStatus.IN_PRUEFUNG_KANTON);
		}
	}

	public FerienbetreuungAngabenAngebot getAngebot() {
		return angebot;
	}
}
