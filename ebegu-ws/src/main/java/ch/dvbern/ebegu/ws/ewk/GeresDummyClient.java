/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.ws.ewk;

import java.security.SecureRandom;
import java.time.LocalDate;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.personensuche.EWKAdresse;
import ch.dvbern.ebegu.dto.personensuche.EWKBeziehung;
import ch.dvbern.ebegu.dto.personensuche.EWKPerson;
import ch.dvbern.ebegu.dto.personensuche.EWKResultat;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.enums.Geschlecht;
import ch.dvbern.ebegu.errors.PersonenSucheServiceBusinessException;
import ch.dvbern.ebegu.errors.PersonenSucheServiceException;

public class GeresDummyClient implements GeresClient {

	private final SecureRandom secureRandom = new SecureRandom();

	@Nonnull
	@Override
	public EWKResultat suchePersonMitFallbackOhneVorname(@Nonnull String name, @Nonnull String vorname, @Nonnull LocalDate geburtsdatum, @Nonnull Geschlecht geschlecht, Long bfsNummer) {
		return suchePerson(name, vorname, geburtsdatum, geschlecht, bfsNummer);
	}

	@Nonnull
	@Override
	public EWKResultat suchePersonMitFallbackOhneVorname(@Nonnull String name, @Nonnull String vorname, @Nonnull LocalDate geburtsdatum, @Nonnull Geschlecht geschlecht) {
		return suchePerson(name, vorname, geburtsdatum, geschlecht, null);
	}

	private EWKResultat suchePerson(String name, String vorname, LocalDate geburtsdatum, Geschlecht geschlecht, Long bfsNummer) {
		EWKResultat ewkResultat = new EWKResultat();
		EWKPerson person = new EWKPerson();
		person.setPersonID(String.valueOf(secureRandom.nextInt(1000)));
		person.setNachname(name == null ? "Muster" : name + ("-Muster"));
		person.setVorname(vorname == null ? "Max" : vorname);
		person.setGeburtsdatum(geburtsdatum == null ? LocalDate.now().minusYears(secureRandom.nextInt(50)) : geburtsdatum);
		person.setGeschlecht(geschlecht == null ? Geschlecht.MAENNLICH : geschlecht);
		EWKAdresse adresse = new EWKAdresse();
		adresse.setWohnungsId(2L);
		adresse.setGebaeudeId(2L);
		adresse.setPostleitzahl("3006");
		adresse.setStrasse("Musterstrasse");
		adresse.setOrt("Bern");
		adresse.setGebiet("Bern ( " + (bfsNummer != null ? bfsNummer : "") + ')');
		person.setAdresse(adresse);
		person.setGesuchsteller(secureRandom.nextInt(100) % 3 == 0);
		person.setHaushalt(secureRandom.nextInt(100) % 2 == 0);
		person.setNichtGefunden(secureRandom.nextInt(100) % 5 == 0);
		EWKBeziehung e = new EWKBeziehung();
		if (secureRandom.nextInt(100) % 3 == 0) {

			e.setAdresse(adresse);
		}
		e.setNachname("Muster");
		e.setVorname("Vater");
		int i = secureRandom.nextInt(11) + 1;
		e.setBeziehungstyp("EWK_BEZIEHUNG_" + i);
		if (secureRandom.nextInt(100) % 2 == 0) {
			person.getBeziehungen().add(e);
		}
		ewkResultat.getPersonen().add(person);
		return ewkResultat;
	}

	@Nonnull
	@Override
	public EWKResultat suchePersonenInHaushalt(Long wohnungsId, Long gebaeudeId) throws PersonenSucheServiceException, PersonenSucheServiceBusinessException {
		return suchePerson("Chambre", "Max", LocalDate.parse("2021-12-11"), Geschlecht.WEIBLICH, 351L);
	}

	@Nonnull
	@Override
	public EWKPerson suchePersonMitAhvNummer(Gesuchsteller gesuchsteller) {
		return suchePerson(gesuchsteller.getNachname(), gesuchsteller.getVorname(), LocalDate.now(), Geschlecht.MAENNLICH, null).getPersonen().get(0);
	}

	@Override
	public String test() throws PersonenSucheServiceException {
		return "TestResponse by GeresDummyClient";
	}
}
