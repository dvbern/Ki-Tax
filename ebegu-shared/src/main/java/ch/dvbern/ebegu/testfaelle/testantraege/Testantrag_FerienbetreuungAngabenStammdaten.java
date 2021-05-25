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

import java.time.LocalDate;

import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.Auszahlungsdaten;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenStammdaten;
import ch.dvbern.ebegu.enums.gemeindeantrag.FerienbetreuungAngabenStatus;
import ch.dvbern.ebegu.enums.gemeindeantrag.FerienbetreuungFormularStatus;
import ch.dvbern.oss.lib.beanvalidation.embeddables.IBAN;

public class Testantrag_FerienbetreuungAngabenStammdaten {

	private FerienbetreuungAngabenStammdaten stammdaten;

	public Testantrag_FerienbetreuungAngabenStammdaten(FerienbetreuungAngabenStatus status) {
		this.stammdaten = new FerienbetreuungAngabenStammdaten();

		this.stammdaten.setSeitWannFerienbetreuungen(LocalDate.now());
		Adresse stammdatenAdresse = new Adresse();
		stammdatenAdresse.setStrasse("Stammdatenstrasse");
		stammdatenAdresse.setPlz("3000");
		stammdatenAdresse.setOrt("Bern");
		stammdatenAdresse.setOrganisation("Gemeinde");
		this.stammdaten.setStammdatenAdresse(stammdatenAdresse);
		this.stammdaten.setStammdatenKontaktpersonVorname("Sloane");
		this.stammdaten.setStammdatenKontaktpersonNachname("Mmoh");
		this.stammdaten.setStammdatenKontaktpersonFunktion("Vorstehende");
		this.stammdaten.setStammdatenKontaktpersonTelefon("0799999999");
		this.stammdaten.setStammdatenKontaktpersonEmail("testdaten-fb@mailbucket.dvbern.ch");

		Auszahlungsdaten auszahlungsdaten = new Auszahlungsdaten();
		auszahlungsdaten.setIban(new IBAN("CH93 0076 2011 6238 5295 7"));
		auszahlungsdaten.setKontoinhaber("Sloane Mmoh");
		Adresse kontoinhabendeAdresse = new Adresse();
		kontoinhabendeAdresse.setStrasse("Stammdatenstrasse");
		kontoinhabendeAdresse.setPlz("3000");
		kontoinhabendeAdresse.setOrt("Bern");
		auszahlungsdaten.setAdresseKontoinhaber(kontoinhabendeAdresse);
		this.stammdaten.setAuszahlungsdaten(auszahlungsdaten);

		if(status == FerienbetreuungAngabenStatus.IN_BEARBEITUNG_GEMEINDE) {
			this.stammdaten.setStatus(FerienbetreuungFormularStatus.IN_BEARBEITUNG_GEMEINDE);
		} else {
			this.stammdaten.setStatus(FerienbetreuungFormularStatus.ABGESCHLOSSEN);
		}

	}

	public FerienbetreuungAngabenStammdaten getStammdaten() {
		return stammdaten;
	}
}
