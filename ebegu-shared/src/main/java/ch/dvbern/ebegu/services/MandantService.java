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

import java.util.Optional;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Mandant;

/**
 * Service fuer Mandant
 */
public interface MandantService {

	/**
	 * @param id PK (id) des Mandanten
	 * @return Mandant mit dem gegebenen key oder null falls nicht vorhanden
	 */
	@Nonnull
	Optional<Mandant> findMandant(@Nonnull final String id);

	/**
	 * Gibt den ersten (und aktuell einzigen) Mandanten aus der DB zurueck
	 */
	@Nonnull
	Mandant getFirst();

	@Nonnull
	Optional<Mandant> findMandantByName(@Nonnull String name);

	@Nonnull
	Mandant getDefaultMandant();
}
