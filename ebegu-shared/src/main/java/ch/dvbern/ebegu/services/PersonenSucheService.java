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

package ch.dvbern.ebegu.services;

import ch.dvbern.ebegu.dto.personensuche.EWKResultat;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.errors.PersonenSucheServiceBusinessException;
import ch.dvbern.ebegu.errors.PersonenSucheServiceException;

import javax.annotation.Nonnull;

/**
 * Service für die Personensuche
 */
public interface PersonenSucheService {

	/**
	 * Sucht die Personen eine Gesuchs innerhalb der Einwohnerkontrolle. Es wird wie folgt gesucht:
	 *
	 * Gesuchsteller 1 inkl. aller Personen im gleichen Haushalt
	 * Gsuchsteller 2
	 * Alle Kinder
	 *
	 * Zum schluss wird geschaut, dass alle Personen nur einmal vorkommen
	 *
	 */
	@Nonnull
	EWKResultat suchePersonen(@Nonnull Gesuch gesuch) throws PersonenSucheServiceException, PersonenSucheServiceBusinessException;
}


