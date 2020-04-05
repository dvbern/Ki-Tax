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

package ch.dvbern.ebegu.rules;

import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;

public abstract class AbstractAbschlussRule {

	protected List<VerfuegungZeitabschnitt> executeIfApplicable(@Nonnull AbstractPlatz platz, @Nonnull List<VerfuegungZeitabschnitt> zeitabschnitte) {
		if (isApplicableForAngebot(platz)) {
			return execute(platz, zeitabschnitte);
		}
		return zeitabschnitte;
	}

	@Nonnull
	protected abstract List<VerfuegungZeitabschnitt> execute(@Nonnull AbstractPlatz platz, @Nonnull List<VerfuegungZeitabschnitt> zeitabschnitte);

	/**
	 * @param platz (Betreuung, Tageschhulplatz etc)
	 * @return true wenn die Regel anwendbar ist
	 */
	private boolean isApplicableForAngebot(@Nonnull AbstractPlatz platz) {
		Objects.requireNonNull(platz);
		Objects.requireNonNull(platz.getBetreuungsangebotTyp());
		return getApplicableAngebotTypes().contains(platz.getBetreuungsangebotTyp());
	}

	protected abstract List<BetreuungsangebotTyp> getApplicableAngebotTypes();

}
