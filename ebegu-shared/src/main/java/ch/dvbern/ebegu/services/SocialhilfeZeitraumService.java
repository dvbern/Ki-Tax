/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.validation.Valid;

import ch.dvbern.ebegu.entities.SocialhilfeZeitraumContainer;

public interface SocialhilfeZeitraumService {

	/**
	 * Speichert die SocialhilfeZeitraum neu in der DB falls der Key noch nicht existiert.
	 *
	 * @param erwerbspensumContainer Das SocialhilfeZeitraum das gespeichert werden soll
	 */
	@Nonnull
	SocialhilfeZeitraumContainer saveSocialhilfeZeitraum(@Valid @Nonnull SocialhilfeZeitraumContainer socialhilfeZeitraumContainer);

	/**
	 * @param key PK (id) des SocialhilfeZeitraumContainers
	 * @return Optional mit dem  SocialhilfeZeitraumContainers mit fuer den gegebenen Key
	 */
	@Nonnull
	Optional<SocialhilfeZeitraumContainer> findSocialhilfeZeitraum(@Nonnull String key);

	/**
	 * entfernt eine Erwerbspensum aus der Databse
	 *
	 * @param erwerbspensumContainerID der Entfernt werden soll
	 */
	void removeSocialhilfeZeitraum(@Nonnull String socialhilfeZeitraumContainerID);
}
