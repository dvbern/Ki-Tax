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

import ch.dvbern.ebegu.dto.personensuche.EWKPerson;
import ch.dvbern.ebegu.dto.personensuche.EWKResultat;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.enums.Geschlecht;
import ch.dvbern.ebegu.errors.PersonenSucheServiceBusinessException;
import ch.dvbern.ebegu.errors.PersonenSucheServiceException;

import javax.annotation.Nonnull;
import java.time.LocalDate;

/**
 * Serviceinterface welches die Methoden des EWK Service zur verfuegung stellt
 */
public interface GeresClient {

	/**
	 * Sucht eine Person im EWK, mit allen Angaben
	 */
	@Nonnull
	EWKResultat suchePersonMitFallbackOhneVorname(@Nonnull String name, @Nonnull String vorname, @Nonnull LocalDate geburtsdatum, @Nonnull Geschlecht geschlecht, Long bfsNummer) throws PersonenSucheServiceException, PersonenSucheServiceBusinessException;

	/**
	 * Sucht eine Person im EWK, mit allen Angaben, aber ohne bfsNummer
	 */
	@Nonnull
	EWKResultat suchePersonMitFallbackOhneVorname(@Nonnull String name, @Nonnull String vorname, @Nonnull LocalDate geburtsdatum, @Nonnull Geschlecht geschlecht) throws PersonenSucheServiceException, PersonenSucheServiceBusinessException;

	/**
	 * Sucht alle Personen in einem Haushalt
	 */
	@Nonnull
	EWKResultat suchePersonenInHaushalt(Long wohnungsId, Long gebaeudeId) throws PersonenSucheServiceException, PersonenSucheServiceBusinessException;

	@Nonnull
	EWKPerson suchePersonMitAhvNummerInGemeinde(Gesuchsteller gesuchsteller, Gemeinde gemeinde) throws PersonenSucheServiceException, PersonenSucheServiceBusinessException;

	String test() throws PersonenSucheServiceException;
}
