/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.services.util;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;

import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Gemeinde;

public final class FilterFunctions {

	public static void getGemeindeFilterForCurrentUser(
		@Nonnull Benutzer currentBenutzer,
		@Nonnull Join<?, Gemeinde> joinGemeinde,
		@Nonnull List<Predicate> predicates) {

		if (currentBenutzer.getCurrentBerechtigung().getRole().isRoleGemeindeabhaengig()) {
			Collection<Gemeinde> gemeindenFOrBenutzer = currentBenutzer.extractGemeindenForUser();
			Predicate inGemeinde = joinGemeinde.in(gemeindenFOrBenutzer);
			predicates.add(inGemeinde);
		}
	}
}
