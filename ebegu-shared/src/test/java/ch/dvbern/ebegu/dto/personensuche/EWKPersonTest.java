/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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
 *
 */

package ch.dvbern.ebegu.dto.personensuche;

import java.util.HashSet;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;

class EWKPersonTest {
	@Test
	void personIDMustBeRelevantForEqualsAndHashcode() {
		EWKPerson ewkPerson = new EWKPerson();
		ewkPerson.setPersonID("id");
		ewkPerson.setNachname("nachname");
		ewkPerson.setVorname("vorname");

		EWKPerson ewkPerson2 = new EWKPerson();
		ewkPerson2.setPersonID("id");
		ewkPerson.setNachname("nachname2");
		ewkPerson.setVorname("vorname2");

		assertThat(ewkPerson.hashCode(), Matchers.is(ewkPerson2.hashCode()));
		assertThat(ewkPerson, Matchers.equalTo(ewkPerson2));

		HashSet<EWKPerson> ewkPeople = new HashSet<>();
		ewkPeople.add(ewkPerson);

		assertThat(ewkPeople.add(ewkPerson2), Matchers.is(false));
	}
}
