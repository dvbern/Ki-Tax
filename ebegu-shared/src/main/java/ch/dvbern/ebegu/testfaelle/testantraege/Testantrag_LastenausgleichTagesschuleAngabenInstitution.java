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

public class Testantrag_LastenausgleichTagesschuleAngabenInstitution {

	private LastenausgleichTagesschuleAngabenInstitution angabenInstitution;

	public Testantrag_LastenausgleichTagesschuleAngabenInstitution() {
		this.angabenInstitution = new LastenausgleichTagesschuleAngabenInstitution();
		// A
		this.angabenInstitution.setLehrbetrieb(true);
		// B
		this.angabenInstitution.setAnzahlEingeschriebeneKinder(new BigDecimal(25));
		this.angabenInstitution.setAnzahlEingeschriebeneKinderKindergarten(new BigDecimal(10));
		this.angabenInstitution.setAnzahlEingeschriebeneKinderSekundarstufe(new BigDecimal(10));
		this.angabenInstitution.setAnzahlEingeschriebeneKinderPrimarstufe(new BigDecimal(5));
		this.angabenInstitution.setAnzahlEingeschriebeneKinderMitBesonderenBeduerfnissen(new BigDecimal(5));
		this.angabenInstitution.setDurchschnittKinderProTagFruehbetreuung(new BigDecimal(5));
		this.angabenInstitution.setDurchschnittKinderProTagMittag(new BigDecimal(5));
		this.angabenInstitution.setDurchschnittKinderProTagNachmittag1(new BigDecimal(5));
		this.angabenInstitution.setDurchschnittKinderProTagNachmittag2(new BigDecimal(5));
		this.angabenInstitution.setBetreuungsstundenEinschliesslichBesondereBeduerfnisse(new BigDecimal(150));
		// C
		this.angabenInstitution.setSchuleAufBasisOrganisatorischesKonzept(true);
		this.angabenInstitution.setSchuleAufBasisPaedagogischesKonzept(true);
		this.angabenInstitution.setRaeumlicheVoraussetzungenEingehalten(true);
		this.angabenInstitution.setBetreuungsverhaeltnisEingehalten(true);
		this.angabenInstitution.setErnaehrungsGrundsaetzeEingehalten(true);
	}

	public LastenausgleichTagesschuleAngabenInstitution getAngabenInstitution() {
		return angabenInstitution;
	}
}
