/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.services;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Gesuch;

/**
 * Service zum Simulieren einer neuen Verfügung für bereits verfügte Gesuche
 */
public interface SimulationService {
	/**
	 * Simuliert eine Neuberechnung der Betreuungsgutscheine für ein bereits verfügtes Gesuch.
	 * Darf nur im Devmode verwendet werden.
	 * Falls Unterschiede entdeckt werden, werden diese geloggt und als String zurückgegeben.
	 */
	String simulateNewVerfuegung(@Nonnull Gesuch gesuch);

}
