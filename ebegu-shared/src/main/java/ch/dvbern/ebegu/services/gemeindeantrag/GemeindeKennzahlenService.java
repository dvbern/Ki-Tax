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

import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.gemeindeantrag.gemeindekennzahlen.GemeindeKennzahlen;

/**
 * Service fuer Gemeindeantraege
 */
public interface GemeindeKennzahlenService {

	/**
	 * Erstellt für alle Gemeinden mit aktiviertem BG einen GemeindeKennzahlen-Antrag fuer die gewuenschte Periode.
	 */
	@Nonnull
	List<GemeindeKennzahlen> createGemeindeKennzahlen(@Nonnull Gesuchsperiode gesuchsperiode);

	@Nonnull
	Optional<GemeindeKennzahlen> findGemeindeKennzahlen(@Nonnull String id);

	/**
	 * Gibt alle GemeindeAntraege der Benutzerin zurück. Falls gesuchsperiode und/oder antragstyp mitgegeben werden
	 * wird entsprechend gefiltert.
	 */
	@Nonnull
	List<GemeindeKennzahlen> getGemeindeKennzahlen(
			@Nullable Gemeinde gemeinde,
			@Nullable Gesuchsperiode gesuchsperiode,
			@Nullable String status,
			@Nullable String timestampMutiert);

	@Nonnull GemeindeKennzahlen saveGemeindeKennzahlen(@Nonnull GemeindeKennzahlen gemeindeKennzahlen);

	@Nonnull GemeindeKennzahlen gemeindeKennzahlenAbschliessen(@Nonnull GemeindeKennzahlen gemeindeKennzahlen);

	@Nonnull GemeindeKennzahlen gemeindeKennzahlenZurueckAnGemeinde(@Nonnull GemeindeKennzahlen gemeindeKennzahlen);

	/**
	 * Löscht alle GemeindeKennzahlen-Anträge für die Gemeinde und Gesuchsperiode
	 */
	void deleteGemeindeKennzahlen(@Nonnull Gemeinde gemeinde, @Nonnull Gesuchsperiode gesuchsperiode);
}
