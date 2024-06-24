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

package ch.dvbern.ebegu.services.personensuche;

import ch.dvbern.ebegu.dto.personensuche.EWKAdresse;
import ch.dvbern.ebegu.dto.personensuche.EWKBeziehung;
import ch.dvbern.ebegu.dto.personensuche.EWKPerson;
import ch.dvbern.ebegu.entities.AbstractPersonEntity;
import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.Kind;

public final class GeresTestUtil {

	private GeresTestUtil() {
	}


	public static EWKPerson ewkPersonFromEntity(AbstractPersonEntity personEntity) {
		EWKPerson ewkPerson = new EWKPerson();
		ewkPerson.setPersonID(personEntity.getId());
		ewkPerson.setVorname(personEntity.getVorname());
		ewkPerson.setNachname(personEntity.getNachname());
		ewkPerson.setGeburtsdatum(personEntity.getGeburtsdatum());
		ewkPerson.setGeschlecht(personEntity.getGeschlecht());
		return ewkPerson;
	}

	public static EWKAdresse ewkAdresseFromEntity(Adresse adresse, Long gebaeudeId, Long wohnungsId) {
		EWKAdresse ewkAdresse = new EWKAdresse();
		ewkAdresse.setGebaeudeId(gebaeudeId);
		ewkAdresse.setWohnungsId(wohnungsId);
		ewkAdresse.setPostleitzahl(adresse.getPlz());
		ewkAdresse.setHausnummer(adresse.getHausnummer());
		ewkAdresse.setOrt(adresse.getOrt());
		return ewkAdresse;
	}
}
