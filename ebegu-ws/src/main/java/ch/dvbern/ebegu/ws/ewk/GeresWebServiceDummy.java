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

import ch.dvbern.ebegu.cdi.Dummy;
import ch.dvbern.ebegu.cdi.Geres;
import ch.dvbern.ebegu.dto.personensuche.EWKAdresse;
import ch.dvbern.ebegu.dto.personensuche.EWKPerson;
import ch.dvbern.ebegu.dto.personensuche.EWKResultat;
import ch.dvbern.ebegu.enums.Geschlecht;
import ch.dvbern.ebegu.errors.PersonenSucheServiceBusinessException;
import ch.dvbern.ebegu.errors.PersonenSucheServiceException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import javax.annotation.Nonnull;
import javax.enterprise.context.Dependent;
import java.security.SecureRandom;
import java.time.LocalDate;

/**
 * Dummy Implementation des EWK-Services
 */
@Dummy
@Geres
@Dependent
@SuppressFBWarnings
public class GeresWebServiceDummy implements IEWKWebService {

	@Nonnull
	@Override
	public EWKResultat suchePersonMitFallbackOhneVorname(@Nonnull String name, @Nonnull String vorname, @Nonnull LocalDate geburtsdatum, @Nonnull Geschlecht geschlecht, long bfsNummer) throws PersonenSucheServiceException {
		return suchePerson(name, vorname, geburtsdatum, geschlecht, bfsNummer);
	}

	@Nonnull
	@Override
	public EWKResultat suchePersonMitFallbackOhneVorname(@Nonnull String name, @Nonnull String vorname, @Nonnull LocalDate geburtsdatum, @Nonnull Geschlecht geschlecht) throws PersonenSucheServiceException {
		return suchePerson(name, vorname, geburtsdatum, geschlecht, null);
	}

	private EWKResultat suchePerson(String name, String vorname, LocalDate geburtsdatum, Geschlecht geschlecht, Long bfsNummer) {
		// todo homa review kibon-955 transform data from xml instead of fixed object
		EWKResultat ewkResultat = new EWKResultat();
		EWKPerson person = new EWKPerson();
		person.setPersonID(String.valueOf(new SecureRandom().nextInt()));
		person.setNachname(name == null ? "Muster" : name + ("-Muster"));
		person.setVorname(vorname == null ? "Max" : vorname);
		person.setGeburtsdatum(geburtsdatum == null ? LocalDate.now() : geburtsdatum);
		person.setGeschlecht(geschlecht == null ? geschlecht : Geschlecht.MAENNLICH);
		EWKAdresse adresse = new EWKAdresse();
		adresse.setWohnungsId(Long.valueOf(2));
		adresse.setGebaeudeId(Long.valueOf(2));
		adresse.setPostleitzahl("3006");
		adresse.setStrasse("Musterstrasse");
		adresse.setOrt("Bern");
		adresse.setGebiet("Bern ( " + (bfsNummer != null ? bfsNummer : "") + ')');
		person.setAdresse(adresse);
		ewkResultat.getPersonen().add(person);
		return ewkResultat;
	}

	@Nonnull
	@Override
	public EWKResultat suchePersonenInHaushalt(Long wohnungsId, Long gebaeudeId) throws PersonenSucheServiceException, PersonenSucheServiceBusinessException {
		return suchePerson("Muster", "Max", LocalDate.now(), Geschlecht.WEIBLICH, 351L);
	}


}
