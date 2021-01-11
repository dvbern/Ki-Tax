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

package ch.dvbern.ebegu.services.gemeindeantrag;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.gemeindeantrag.GemeindeAntrag;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeinde;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeContainer;

/**
 * Service fuer den Lastenausgleich der Tagesschulen
 */
public interface LastenausgleichTagesschuleAngabenGemeindeService {

	/**
	 * Erstellt fuer jede aktive Gemeinde einen LastenausgleichTagesschule fuer die angegebene Periode
	 */
	@Nonnull
	List<? extends GemeindeAntrag> createLastenausgleichTagesschuleGemeinde(
		@Nonnull Gesuchsperiode gesuchsperiode);

	/**
	 * Sucht den LastenausgleichTagesschuleAngabenGemeindeContainer mit der uebergebenen ID
	 */
	@Nonnull
	Optional<LastenausgleichTagesschuleAngabenGemeindeContainer> findLastenausgleichTagesschuleAngabenGemeindeContainer(
		@Nonnull String id);

	/**
	 * Speichert den LastenausgleichTagesschule, ohne Eintrag in die StatusHistory-Tabelle
	 */
	@Nonnull
	LastenausgleichTagesschuleAngabenGemeindeContainer saveLastenausgleichTagesschuleGemeinde(
		@Nonnull LastenausgleichTagesschuleAngabenGemeindeContainer fallContainer);

	/**
	 * Gibt den LastenausgleichTagesschuleAngabenGemeindeContainer frei fuer die Bearbeitung durch die Institutionen.
	 * Der Status wird von OFFEN auf IN_BEARBEITUNG_GEMEINDE gesetzt.
	 */
	@Nonnull
	LastenausgleichTagesschuleAngabenGemeindeContainer lastenausgleichTagesschuleGemeindeFuerInstitutionenFreigeben(
		@Nonnull LastenausgleichTagesschuleAngabenGemeindeContainer fallContainer);

	/**
	 * Reicht den Lastenausgleich ein, inkl. kopieren der Daten vom Korrektur- in den Deklarations-Container,
	 * falls die Vorbedingungen dazu erfuellt sind.
	 */
	@Nonnull
	LastenausgleichTagesschuleAngabenGemeindeContainer lastenausgleichTagesschuleGemeindeEinreichen(
		@Nonnull LastenausgleichTagesschuleAngabenGemeindeContainer fallContainer);

	/**
	 * Gibt alle Lastenausgleiche der Tagesschulen für die Benutzerin zurück
	 * @return
	 */
	@Nonnull
	List<LastenausgleichTagesschuleAngabenGemeindeContainer> getAllLastenausgleicheTagesschulen();

	/**
	 * Gibt die gefilterten Lastenausgleiche der Tagesschulen für die Benutzerin zurück
	 * @return
	 */
	@Nonnull
	List<LastenausgleichTagesschuleAngabenGemeindeContainer> getLastenausgleicheTagesschulen(
		@Nullable String gemeinde,
		@Nullable String periode,
		@Nullable String status);
}
