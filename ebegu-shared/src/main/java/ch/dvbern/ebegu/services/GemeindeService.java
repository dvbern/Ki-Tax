/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
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

import java.util.Collection;
import java.util.Optional;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Gemeinde;

/**
 * Service zum Verwalten von Gemeinden
 */
public interface GemeindeService {

	/**
	 * Gibt die Gemeinde mit der uebergebenen ID zurueck.
	 */
	@Nonnull
	Optional<Gemeinde> findGemeinde(@Nonnull String id);

	/**
	 * Gibt die erste (und aktuell einzige) Gemeinde aus der DB zurueck
	 */
	@Nonnull
	Gemeinde getFirst();

	/**
	 * Gibt alle Gemeinden zurück
	 */
	@Nonnull
	Collection<Gemeinde> getAllGemeinden();

	/**
	 * Gibt alle Gemeinden im Status "AKTIV" zurueck.
	 */
	@Nonnull
	Collection<Gemeinde> getAktiveGemeinden();
}
