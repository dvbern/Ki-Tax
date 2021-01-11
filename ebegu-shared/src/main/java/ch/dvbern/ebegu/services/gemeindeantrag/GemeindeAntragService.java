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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
	List<GemeindeAntrag> createGemeindeAntrag(@Nonnull Gesuchsperiode gesuchsperiode, @Nonnull GemeindeAntragTyp typ);

	/**
	 * Gibt alle GemeindeAntraege der Benutzerin zurück. Falls gesuchsperiode und/oder antragstyp mitgegeben werden
	 * wird entsprechend gefiltert.
	 */
	@Nonnull
	List<? extends GemeindeAntrag> getGemeindeAntraege();

	@Nonnull
	List<? extends GemeindeAntrag> getGemeindeAntraege(
		@Nullable String gemeinde,
		@Nullable String periode,
		@Nullable String typ,
		@Nullable String status);

	@Nonnull
	List<GemeindeAntrag> getGemeindeAntraege(@Nonnull Gesuchsperiode gesuchsperiode);

	@Nonnull
	List<GemeindeAntrag> getGemeindeAntraege(@Nonnull GemeindeAntragTyp typ);

	@Nonnull
	List<GemeindeAntrag> getGemeindeAntraege(@Nonnull Gesuchsperiode gesuchsperiode, @Nonnull GemeindeAntragTyp typ);
}
