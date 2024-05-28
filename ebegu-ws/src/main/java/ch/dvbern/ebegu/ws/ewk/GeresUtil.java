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

package ch.dvbern.ebegu.ws.ewk;

import java.util.function.Predicate;

import ch.dvbern.ebegu.dto.personensuche.EWKPerson;
import ch.dvbern.ebegu.entities.AbstractPersonEntity;

public final class GeresUtil {
	private GeresUtil() {
	}

	public static EWKPerson createNotFoundPerson(AbstractPersonEntity personEntity) {
		EWKPerson person = new EWKPerson();
		person.setPersonID(personEntity.getId());
		person.setNachname(personEntity.getNachname());
		person.setVorname(personEntity.getVorname());
		person.setGeburtsdatum(personEntity.getGeburtsdatum());
		person.setGeschlecht(personEntity.getGeschlecht());
		person.setNichtGefunden(true);
		return person;
	}

	public static Predicate<EWKPerson> matches(AbstractPersonEntity personEntity) {
		return ewkPerson -> ewkPerson.getGeburtsdatum() != null &&
			personEntity.getGeburtsdatum().equals(ewkPerson.getGeburtsdatum()) &&
			personEntity.getNachname().equals(ewkPerson.getNachname()) &&
			personEntity.getVorname().equals(ewkPerson.getVorname());
	}
}
