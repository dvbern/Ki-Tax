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

package ch.dvbern.ebegu.entities.gemeindeantrag;

import java.time.LocalDateTime;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.gemeindeantrag.GemeindeAntragTyp;

/**
 * Ein GemeindeAntrag ist ein generisches Konstrukt, welches verschiedene Antraege in einer Tabelle zusammenfassen soll.
 * Dieses Interface beschreibt die Mindestanforderungen fuer ein solches Ding.
 */
public interface GemeindeAntrag {

	@Nonnull
	String getId();

	long getVersion();

	@Nullable
	LocalDateTime getTimestampErstellt();

	@Nullable
	LocalDateTime getTimestampMutiert();

	@Nonnull
	GemeindeAntragTyp getGemeindeAntragTyp();

	@Nonnull
	Gemeinde getGemeinde();

	@Nonnull
	Gesuchsperiode getGesuchsperiode();

	@Nonnull
	String getStatusString();

	boolean isAntragAbgeschlossen();
}
