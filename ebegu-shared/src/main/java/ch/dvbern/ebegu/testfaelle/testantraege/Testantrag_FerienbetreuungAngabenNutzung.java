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

import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenNutzung;
import ch.dvbern.ebegu.enums.gemeindeantrag.FerienbetreuungAngabenStatus;
import ch.dvbern.ebegu.enums.gemeindeantrag.FerienbetreuungFormularStatus;

public class Testantrag_FerienbetreuungAngabenNutzung {

	private FerienbetreuungAngabenNutzung nutzung;

	public Testantrag_FerienbetreuungAngabenNutzung(FerienbetreuungAngabenStatus status) {
		this.nutzung = new FerienbetreuungAngabenNutzung();

		this.nutzung.setAnzahlBetreuungstageKinderBern(new BigDecimal(16));
		this.nutzung.setBetreuungstageKinderDieserGemeinde(new BigDecimal(10));
		this.nutzung.setBetreuungstageKinderDieserGemeindeSonderschueler(new BigDecimal(3));
		this.nutzung.setDavonBetreuungstageKinderAndererGemeinden(new BigDecimal(6));
		this.nutzung.setDavonBetreuungstageKinderAndererGemeindenSonderschueler(new BigDecimal(3));

		this.nutzung.setAnzahlBetreuteKinder(new BigDecimal(26));
		this.nutzung.setAnzahlBetreuteKinderSonderschueler(new BigDecimal(10));
		this.nutzung.setAnzahlBetreuteKinder1Zyklus(new BigDecimal(10));
		this.nutzung.setAnzahlBetreuteKinder2Zyklus(new BigDecimal(10));
		this.nutzung.setAnzahlBetreuteKinder3Zyklus(new BigDecimal(10));

		if(status == FerienbetreuungAngabenStatus.IN_BEARBEITUNG_GEMEINDE) {
			this.nutzung.setStatus(FerienbetreuungFormularStatus.IN_BEARBEITUNG_GEMEINDE);
		} else {
			this.nutzung.setStatus(FerienbetreuungFormularStatus.IN_PRUEFUNG_KANTON);
		}
	}

	public FerienbetreuungAngabenNutzung getNutzung() {
		return nutzung;
	}
}
