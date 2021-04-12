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
import ch.dvbern.ebegu.entities.gemeindeantrag.GemeindeAntrag;
import ch.dvbern.ebegu.enums.gemeindeantrag.GemeindeAntragTyp;

/**
 * Service fuer Gemeindeantraege
 */
public interface GemeindeAntragService {

	/**
	 * Erstellt fuer jede aktive Gemeinde einen Gemeindeantrag des gewuenschten Typs fuer die gewuenschte Periode.
	 */
	@Nonnull
	List<GemeindeAntrag> createAllGemeindeAntraege(@Nonnull Gesuchsperiode gesuchsperiode, @Nonnull GemeindeAntragTyp typ);

	/**
	 * Erstellt für die angegebene Gemeinden einen Gemeindeantrag des gewuenschten Typs fuer die gewuenschte Periode.
	 */
	@Nonnull
	GemeindeAntrag createGemeindeAntrag(@Nonnull Gemeinde gemeinde, @Nonnull Gesuchsperiode gesuchsperiode, @Nonnull GemeindeAntragTyp gemeindeAntragTyp);

	/**
	 * Gibt alle GemeindeAntraege der Benutzerin zurück. Falls gesuchsperiode und/oder antragstyp mitgegeben werden
	 * wird entsprechend gefiltert.
	 */
	@Nonnull
	List<? extends GemeindeAntrag> getGemeindeAntraege(
		@Nullable String gemeinde,
		@Nullable String periode,
		@Nullable String typ,
		@Nullable String status,
		@Nullable String timestampMutiert);

	@Nonnull
	List<GemeindeAntrag> getGemeindeAntraege(
		@Nullable String gemeindeId,
		@Nullable String periodeId,
		@Nullable String status,
		@Nullable String timestampMutiert);

	@Nonnull
	Optional<? extends GemeindeAntrag> findGemeindeAntrag(@Nonnull GemeindeAntragTyp typ, @Nonnull String gemeindeAntragId);

	void deleteGemeindeAntraege(@Nonnull Gesuchsperiode gesuchsperiode, @Nonnull GemeindeAntragTyp gemeindeAntragTyp);
}
