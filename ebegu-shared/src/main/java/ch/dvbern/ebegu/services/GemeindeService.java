/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.services;

import java.util.Collection;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;

/**
 * Service zum Verwalten von Gemeinden
 */
public interface GemeindeService {

	/**
	 * Speichert die Gemeinde neu in der DB falls der Key noch nicht existiert.
	 *
	 * @param gemeinde Die Gemeinde als DTO
	 */
	@Nonnull
	Gemeinde saveGemeinde(@Nonnull Gemeinde gemeinde);

	/**
	 * Creates a new Gemeinde. Name and BSFNummer must be unique. If they already exist an Exception will be thrown
	 */
	@Nonnull
	Gemeinde createGemeinde(@Nonnull Gemeinde gemeinde);

	/**
	 * Gibt die Gemeinde mit der uebergebenen ID zurueck.
	 */
	@Nonnull
	Optional<Gemeinde> findGemeinde(@Nonnull String id);

	/**
	 * Sucht eine Gemeinde anhand des Namens.
	 */
	@Nonnull
	Optional<Gemeinde> findGemeindeByName(@Nonnull String name);

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

	/**
	 * Gibt die GemeindeStammdaten der jeweiligen Gemeinde zurück
	 */
	@Nullable
	GemeindeStammdaten getGemeindeStammdaten(@Nonnull String gemeindeId);
}
