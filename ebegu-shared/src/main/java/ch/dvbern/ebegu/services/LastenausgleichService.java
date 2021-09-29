/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Lastenausgleich;
import ch.dvbern.ebegu.entities.LastenausgleichGrundlagen;
import ch.dvbern.ebegu.entities.Mandant;

/**
 * Service fuer den Lastenausgleich
 */
public interface LastenausgleichService {

	/**
	 * Gibt alle vorhandenen Lastenausgleiche zurück.
	 */
	@Nonnull
	Collection<Lastenausgleich> getAllLastenausgleiche();

	/**
	 * Gibt alle Lastenausgleiche für die Gemeinden zurück. Die LastenausgleichDetails mit Gemeinden, die nicht in
	 * dieser Liste sind werden entfernt und das Gesamt des Lastenausgleichs entsprechend neu berechnet, aber nicht
	 * persistiert.
	 */
	@Nonnull
	Collection<Lastenausgleich> getLastenausgleicheForGemeinden(@Nonnull Set<Gemeinde> gemeinden);

	/**
	 * Berechnet einen Lastenausgleich fuer das uebergebene Jahr. Die Kosten pro 100% Platz werden als
	 * LastenausgleichGrundlagen gespeichert.
	 * Der Lastenausgleich kann pro Jahr nur einmal erstellt werden, auch die Grundlagen duerfen nicht mehr geaendert
	 * werden.
	 * Es werden auch rueckwirkende Korrekturen vorgenommen und zwar fuer die letzten 10 Jahre
	 */
	@Nonnull
	Lastenausgleich createLastenausgleich(
			int jahr,
			@Nonnull BigDecimal selbstbehaltPro100ProzentPlatz,
			Mandant mandant);

	/**
	 * Sucht den Lastenausgleich des uebergebenen Jahres, falls vorhanden
	 */
	@Nonnull
	Lastenausgleich findLastenausgleich(@Nonnull String lastenausgleichId);

	/**
	 * Sucht die LastenausgleichGrundlagen des uebergebenen Jahres, falls vorhanden
	 */
	@Nonnull
	Optional<LastenausgleichGrundlagen> findLastenausgleichGrundlagen(int jahr);

	/**
	 * Löscht den Lastenausgleich
	 */
	void removeLastenausgleich(@Nonnull String lastenausgleichId);
}
