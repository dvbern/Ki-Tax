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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.AbstractDateRangedEntity_;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gemeinde_;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenContainer;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenContainer_;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.enums.gemeindeantrag.FerienbetreuungAngabenStatus;
import ch.dvbern.ebegu.services.AbstractBaseService;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.types.DateRange_;
import ch.dvbern.lib.cdipersistence.Persistence;

/**
 * Service fuer die Ferienbetreuungen
 */
@Stateless
@Local(FerienbetreuungService.class)
public class FerienbetreuungServiceBean extends AbstractBaseService
	implements FerienbetreuungService {

	@Inject
	private Persistence persistence;

	@Inject
	private PrincipalBean principal;

	@Nonnull
	@Override
	public List<FerienbetreuungAngabenContainer> getFerienbetreuungAntraege(
		@Nullable String gemeinde,
		@Nullable String periode,
		@Nullable String status
	) {
		Set<Gemeinde> gemeinden = principal.getBenutzer().extractGemeindenForUser();

		CriteriaBuilder cb = persistence.getCriteriaBuilder();
		CriteriaQuery<FerienbetreuungAngabenContainer> query = cb.createQuery(FerienbetreuungAngabenContainer.class);
		Root<FerienbetreuungAngabenContainer> root = query.from(FerienbetreuungAngabenContainer.class);

		if (!principal.isCallerInAnyOfRole(
			UserRole.SUPER_ADMIN,
			UserRole.ADMIN_MANDANT,
			UserRole.SACHBEARBEITER_MANDANT)) {
			Predicate gemeindeIn =
				root.get(FerienbetreuungAngabenContainer_.gemeinde).in(gemeinden);
			query.where(gemeindeIn);
		}

		if (gemeinde != null) {
			query.where(
				cb.equal(
					root.get(FerienbetreuungAngabenContainer_.gemeinde).get(Gemeinde_.name),
					gemeinde)
			);
		}
		if (periode != null) {
			String[] years = Arrays.stream(periode.split("/"))
				.map(year -> year.length() == 4 ? year : "20".concat(year))
				.collect(Collectors.toList())
				.toArray(String[]::new);
			Path<DateRange> dateRangePath =
				root.join(FerienbetreuungAngabenContainer_.gesuchsperiode, JoinType.INNER)
					.get(AbstractDateRangedEntity_.gueltigkeit);
			query.where(
				cb.and(
					cb.equal(cb.function("year", Integer.class, dateRangePath.get(DateRange_.gueltigAb)), years[0]),
					cb.equal(cb.function("year", Integer.class, dateRangePath.get(DateRange_.gueltigBis)), years[1])
				)
			);
		}
		if (status != null) {
			query.where(
				cb.equal(
					root.get(FerienbetreuungAngabenContainer_.status),
					FerienbetreuungAngabenStatus.valueOf(status))
			);
		}

		return persistence.getCriteriaResults(query);
	}

	@Nonnull
	@Override
	public Optional<FerienbetreuungAngabenContainer> findFerienbetreuungAngabenContainer(@Nonnull String containerId) {
		Objects.requireNonNull(containerId, "id muss gesetzt sein");

		FerienbetreuungAngabenContainer container =
			persistence.find(FerienbetreuungAngabenContainer.class, containerId);

		return Optional.ofNullable(container);
	}
}


