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

import ch.dvbern.ebegu.entities.SozialhilfeZeitraumContainer;

public interface SozialhilfeZeitraumService {

	/**
	 * Speichert die SozialhilfeZeitraum neu in der DB falls der Key noch nicht existiert.
	 *
	 * @param erwerbspensumContainer Das SozialhilfeZeitraum das gespeichert werden soll
	 */
	@Nonnull
	SozialhilfeZeitraumContainer saveSozialhilfeZeitraum(@Valid @Nonnull SozialhilfeZeitraumContainer sozialhilfeZeitraumContainer);

	/**
	 * @param key PK (id) des SozialhilfeZeitraumContainers
	 * @return Optional mit dem  SozialhilfeZeitraumContainers mit fuer den gegebenen Key
	 */
	@Nonnull
	Optional<SozialhilfeZeitraumContainer> findSozialhilfeZeitraum(@Nonnull String key);

	/**
	 * entfernt eine Erwerbspensum aus der Databse
	 *
	 * @param erwerbspensumContainerID der Entfernt werden soll
	 */
	void removeSozialhilfeZeitraum(@Nonnull String sozialhilfeZeitraumContainerID);
}
