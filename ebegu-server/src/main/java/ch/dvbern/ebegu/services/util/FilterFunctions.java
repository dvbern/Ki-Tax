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
