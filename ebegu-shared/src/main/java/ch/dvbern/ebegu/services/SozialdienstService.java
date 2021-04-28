/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

import ch.dvbern.ebegu.entities.sozialdienst.Sozialdienst;
import ch.dvbern.ebegu.entities.sozialdienst.SozialdienstFall;
import ch.dvbern.ebegu.entities.sozialdienst.SozialdienstStammdaten;

public interface SozialdienstService {

	/**
	 * Speichert die Sozialdienst neu in der DB falls der Key noch nicht existiert.
	 */
	@Nonnull
	Sozialdienst saveSozialdienst(@Nonnull Sozialdienst sozialdienst);

	/**
	 * Creates a new Sozialdienst. Name and BSFNummer must be unique. If they already exist an Exception will be thrown
	 */
	@Nonnull
	Sozialdienst createSozialdienst(@Nonnull String adminMail, @Nonnull Sozialdienst sozialdienst);

	/**
	 * Gibt die Sozialdienst mit der uebergebenen ID zurueck.
	 */
	@Nonnull
	Optional<Sozialdienst> findSozialdienst(@Nonnull String id);

	/**
	 * Gibt alle Sozialdienst zurück
	 */
	@Nonnull
	Collection<Sozialdienst> getAllSozialdienste();

	/**
	 * Gibt die Stammdaten zurück
	 */
	@Nonnull
	Optional<SozialdienstStammdaten> getSozialdienstStammdaten(@Nonnull String id);

	/**
	 * Gibt die Stammdaten von dieser Sozialdienst zurück
	 */
	@Nonnull
	Optional<SozialdienstStammdaten> getSozialdienstStammdatenBySozialdienstId(@Nonnull String sozialdienstId);

	/**
	 * Speichert die Stammdaten
	 */
	@Nonnull
	SozialdienstStammdaten saveSozialdienstStammdaten(@Nonnull SozialdienstStammdaten stammdaten);

	/**
	 * Gibt der Sozialdienst Fall zurück
	 * @param id
	 * @return
	 */
	@Nonnull
	Optional<SozialdienstFall> findSozialdienstFall(@Nonnull String id);
}
