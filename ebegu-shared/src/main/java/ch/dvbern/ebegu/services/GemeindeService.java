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

import ch.dvbern.ebegu.entities.BfsGemeinde;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Mandant;

/**
 * Service zum Verwalten von Gemeinden
 */
public interface GemeindeService {

	/**
	 * Speichert die Gemeinde neu in der DB falls der Key noch nicht existiert.
	 * @param gemeinde Die Gemeinde
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
	 * Gibt die GemeindeStammdaten anhand ihrer Id zurück
	 */
	@Nonnull
	Optional<GemeindeStammdaten> getGemeindeStammdaten(@Nonnull String id);

	/**
	 * Gibt die GemeindeStammdaten der jeweiligen Gemeinde zurück
	 */
	@Nonnull
	Optional<GemeindeStammdaten> getGemeindeStammdatenByGemeindeId(@Nonnull String gemeindeId);

	/**
	 * Speichert die GemeindeStammdaten neu in der DB falls der Key noch nicht existiert.
	 * @param stammdaten Die GemeindeStammdaten
	 */
	@Nonnull
	GemeindeStammdaten saveGemeindeStammdaten(@Nonnull GemeindeStammdaten stammdaten);

	/**
	 * Updates the logo of the given Gemeinde wth the given content
	 */
	@Nonnull
	GemeindeStammdaten uploadLogo(
		@Nonnull String gemeindeId,
		@Nonnull byte[] content,
		@Nonnull String name,
		@Nonnull String type);

	/**
	 * Gibt eine Liste aller BFS Gemeinden dieses Mandanten zurueck, welche noch nicht fuer KiBon registriert sind.
	 */
	@Nonnull
	Collection<BfsGemeinde> getUnregisteredBfsGemeinden(@Nonnull Mandant mandant);

	/**
	 * Gibt den zur BFS-Nummer gehoerenden Verbund zurueck.
	 */
	@Nonnull
	Optional<Gemeinde> findRegistredGemeindeVerbundIfExist(@Nonnull Long gemeindeBfsNummer);
}
