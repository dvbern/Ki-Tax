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
import javax.validation.Valid;

import ch.dvbern.ebegu.entities.ErwerbspensumContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.UnbezahlterUrlaub;

/**
 * Service zum Verwalten von Erwerbspensen
 */
public interface ErwerbspensumService {

	/**
	 * Speichert die Erwerbspensen neu in der DB falls der Key noch nicht existiert.
	 *
	 * @param erwerbspensumContainer Das Erwerbspensum das gespeichert werden soll
	 */
	@Nonnull
	ErwerbspensumContainer saveErwerbspensum(@Valid @Nonnull ErwerbspensumContainer erwerbspensumContainer, Gesuch gesuch);

	/**
	 * @param key PK (id) des ErwerbspensumContainers
	 * @return Optional mit dem  ErwerbspensumContainers mit fuer den gegebenen Key
	 */
	@Nonnull
	Optional<ErwerbspensumContainer> findErwerbspensum(@Nonnull String key);

	/**
	 * entfernt eine Erwerbspensum aus der Databse
	 *
	 * @param erwerbspensumContainerID der Entfernt werden soll
	 */
	void removeErwerbspensum(@Nonnull String erwerbspensumContainerID, Gesuch gesuch);

	/**
	 * Gibt zurück, ob fuer das uebergebene Gesuch ein Erwerbspensum erfasst werden muss.
	 * Ein Erwerbspensum muss fuer alle Gesuchsteller erfasst werden wenn es keine Fachstelle erfasst wurde und es
	 * kein Angebot des Types Tagesschule, TAGI oder Tageseltern für Schulkind gibt
	 */
	boolean isErwerbspensumRequired(@Nonnull Gesuch gesuch);

	@Nonnull
	Optional<UnbezahlterUrlaub> findUnbezahlterUrlaub(@Nonnull String unbezahlterUrlaubId);
}
