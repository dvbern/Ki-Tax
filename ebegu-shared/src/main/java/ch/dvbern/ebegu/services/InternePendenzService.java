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

import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.InternePendenz;

public interface InternePendenzService {

	@Nonnull
	Optional<InternePendenz> findInternePendenz(@Nonnull String internePendenzId);

	/**
	 * Erstellt oder migriert die interne Pendenz
	 */
	@Nonnull
	InternePendenz updateInternePendenz(@Nonnull InternePendenz internePendenz);

	@Nonnull
	InternePendenz createInternePendenz(@Nonnull InternePendenz internePendenz);

	/**
	 * Gibt alle internen Pendenzen für das Gesuch zurück
	 * @return
	 */
	@Nonnull
	Collection<InternePendenz> findInternePendenzenForGesuch(@Nonnull Gesuch gesuch);

	/**
	 * Gibt die Anzahl der internen Pendenzen für das Gesuch zurück
	 */
	@Nonnull
	Long countInternePendenzenForGesuch(@Nonnull Gesuch gesuch);

	/**
	 * Löscht die interne Pendenz
	 */
	void deleteInternePendenz(@Nonnull InternePendenz internePendenz);
}
