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

import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenInstitution;

public class Testantrag_LastenausgleichTagesschuleAngabenInstitution extends LastenausgleichTagesschuleAngabenInstitution {

	public Testantrag_LastenausgleichTagesschuleAngabenInstitution() {
		// A
		setLehrbetrieb(true);
		// B
		this.setAnzahlEingeschriebeneKinder(new BigDecimal(25));
		this.setAnzahlEingeschriebeneKinderKindergarten(new BigDecimal(10));
		this.setAnzahlEingeschriebeneKinderSekundarstufe(new BigDecimal(10));
		this.setAnzahlEingeschriebeneKinderPrimarstufe(new BigDecimal(5));
		this.setAnzahlEingeschriebeneKinderMitBesonderenBeduerfnissen(new BigDecimal(5));
		this.setDurchschnittKinderProTagFruehbetreuung(new BigDecimal(5));
		this.setDurchschnittKinderProTagMittag(new BigDecimal(5));
		this.setDurchschnittKinderProTagNachmittag1(new BigDecimal(5));
		this.setDurchschnittKinderProTagNachmittag2(new BigDecimal(5));
		this.setBetreuungsstundenEinschliesslichBesondereBeduerfnisse(new BigDecimal(150));
		// C
		this.setSchuleAufBasisOrganisatorischesKonzept(true);
		this.setSchuleAufBasisPaedagogischesKonzept(true);
		this.setRaeumlicheVoraussetzungenEingehalten(true);
		this.setBetreuungsverhaeltnisEingehalten(true);
		this.setErnaehrungsGrundsaetzeEingehalten(true);
	}
}
