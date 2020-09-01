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

import java.util.Collection;
import java.util.Optional;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Adresse;

/**
 * Service zum Verwalten von Personen Adressen
 */
public interface AdresseService {

	/**
	 * Speichert die Adresse neu in der DB falls der Key noch nicht existiert.
	 *
	 * @param adresse Die Adresse als DTO
	 */
	@Nonnull
	Adresse createAdresse(@Nonnull Adresse adresse);

	/**
	 * Aktualisiert die Adresse in der DB.
	 *
	 * @param adresse Die Adresse als DTO
	 */
	@Nonnull
	Adresse updateAdresse(@Nonnull Adresse adresse);

	/**
	 * @param key PK (id) der Adresse
	 * @return Adresse mit dem gegebenen key oder null falls nicht vorhanden
	 */
	@Nonnull
	Optional<Adresse> findAdresse(@Nonnull String key);

	/**
	 * @return Liste aller Adressen aus der DB
	 */
	@Nonnull
	Collection<Adresse> getAllAdressen();

	/**
	 * Aktuealisiert mit Hilfe der GeoAdmin API die Gemeinde und BFS Nummer der Adresse
	 */
	void updateGemeindeAndBFS(Adresse adresse);
}
