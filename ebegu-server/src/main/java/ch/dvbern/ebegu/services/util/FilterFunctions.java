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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Benutzer_;
import ch.dvbern.ebegu.entities.Berechtigung;
import ch.dvbern.ebegu.entities.Berechtigung_;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.Institution_;
import ch.dvbern.ebegu.entities.Traegerschaft;
import ch.dvbern.ebegu.enums.RollenAbhaengigkeit;
import ch.dvbern.ebegu.enums.UserRole;

public final class FilterFunctions {

	private FilterFunctions() {
		// util
	}

	public static void setGemeindeFilterForCurrentUser(
		@Nonnull Benutzer currentBenutzer,
		@Nonnull Join<?, Gemeinde> joinGemeinde,
		@Nonnull List<Predicate> predicates) {

		if (currentBenutzer.getCurrentBerechtigung().getRole().isRoleGemeindeabhaengig()) {
			Collection<Gemeinde> gemeindenForBenutzer = currentBenutzer.extractGemeindenForUser();
			Predicate inGemeinde = joinGemeinde.in(gemeindenForBenutzer);
			predicates.add(inGemeinde);
		}
	}

	public static void setRoleFilterForCurrentUser(
		@Nonnull Benutzer currentBenutzer,
		@Nonnull Join<Benutzer, Berechtigung> joinBerechtigung,
		@Nonnull List<Predicate> predicates) {

		final UserRole role = currentBenutzer.getCurrentBerechtigung().getRole();

		if (role == UserRole.ADMIN_MANDANT) {
			predicates.add(joinBerechtigung.get(Berechtigung_.role).in(UserRole.getMandantRoles()));
			return;
		}

		final RollenAbhaengigkeit abhaengigkeit = role.getRollenAbhaengigkeit();
		switch (abhaengigkeit) {
		case GEMEINDE:
		case INSTITUTION:
			predicates.add(joinBerechtigung.get(Berechtigung_.role)
				.in(UserRole.getRolesByAbhaengigkeit(abhaengigkeit)));
			return;
		case TRAEGERSCHAFT:
			predicates.add(joinBerechtigung.get(Berechtigung_.role)
				.in(UserRole.getRolesByAbhaengigkeiten(
					Arrays.asList(RollenAbhaengigkeit.INSTITUTION, RollenAbhaengigkeit.TRAEGERSCHAFT))
				));
			return;
		default:
			return;
		}
	}

	public static void setInstitutionFilterForCurrentUser(
		@Nonnull Benutzer currentBenutzer,
		@Nonnull Join<Benutzer, Berechtigung> joinCurrentBerechtigung,
		@Nonnull CriteriaBuilder cb,
		@Nonnull List<Predicate> predicates) {

		final Institution userInstitution = currentBenutzer.getCurrentBerechtigung().getInstitution();
		Objects.requireNonNull(userInstitution);

		Predicate sameInstitution = cb.equal(joinCurrentBerechtigung.get(Berechtigung_.institution), userInstitution);
		predicates.add(sameInstitution);
	}

	public static void setGemeindeFilterForCurrentFerienbetreuungUser(
		@Nonnull Benutzer currentBenutzer,
		@Nonnull Join<Benutzer, Berechtigung> joinCurrentBerechtigung,
		@Nonnull CriteriaBuilder cb,
		@Nonnull List<Predicate> predicates) {

		Predicate isFerienbetreuungRole = joinCurrentBerechtigung.get(Berechtigung_.role)
			.in(UserRole.ADMIN_FERIENBETREUUNG, UserRole.SACHBEARBEITER_FERIENBETREUUNG);

		predicates.add(isFerienbetreuungRole);
	}

	public static void setTraegerschaftFilterForCurrentUser(
		@Nonnull Benutzer currentBenutzer,
		@Nonnull Join<Benutzer, Berechtigung> joinCurrentBerechtigung,
		@Nonnull CriteriaBuilder cb,
		@Nonnull List<Predicate> predicates) {

		final Traegerschaft userTraegerschaft = currentBenutzer.getCurrentBerechtigung().getTraegerschaft();
		Objects.requireNonNull(userTraegerschaft);

		Predicate sameTraegerschaft =
			cb.equal(joinCurrentBerechtigung.get(Berechtigung_.traegerschaft), userTraegerschaft);
		Predicate institutionOfTraegerschaft = cb.equal(
			joinCurrentBerechtigung.get(Berechtigung_.institution).get(Institution_.traegerschaft), userTraegerschaft);

		predicates.add(cb.or(sameTraegerschaft, institutionOfTraegerschaft));
	}

	public static void setMandantFilterForCurrentUser(
		@Nonnull Benutzer currentBenutzer,
		@Nonnull Root<Benutzer> root,
		@Nonnull CriteriaBuilder cb,
		@Nonnull List<Predicate> predicates
	) {
		final UserRole role = currentBenutzer.getCurrentBerechtigung().getRole();

		// Alle ausser Superadmin duerfen nur Benutzer ihres Mandanten sehen
		if (role != UserRole.SUPER_ADMIN) {
			predicates.add(cb.equal(root.get(Benutzer_.mandant), currentBenutzer.getMandant()));
		}
	}

	public static void setSuperAdminFilterForCurrentUser(
		@Nonnull Benutzer currentBenutzer,
		@Nonnull Join<Benutzer, Berechtigung> joinBerechtigungen,
		@Nonnull List<Predicate> predicates
	) {

		final UserRole role = currentBenutzer.getCurrentBerechtigung().getRole();

		// Alle ausser Superadmin duerfen keine Superadmins sehen
		if (role != UserRole.SUPER_ADMIN) {
			Predicate predicateRoleNotSuperadmin =
				joinBerechtigungen.get(Berechtigung_.role).in(UserRole.SUPER_ADMIN).not();
			predicates.add(predicateRoleNotSuperadmin);
		}
	}
}
