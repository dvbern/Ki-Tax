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
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngaben;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenAngebot;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenContainer;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenContainer_;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenKostenEinnahmen;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenNutzung;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenStammdaten;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.enums.gemeindeantrag.FerienbetreuungAngabenStatus;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
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

	@Nonnull
	@Override
	public FerienbetreuungAngabenContainer createFerienbetreuungAntrag(@Nonnull Gemeinde gemeinde, @Nonnull Gesuchsperiode gesuchsperiode) {
		FerienbetreuungAngabenContainer container = new FerienbetreuungAngabenContainer();
		container.setStatus(FerienbetreuungAngabenStatus.IN_BEARBEITUNG_GEMEINDE);
		container.setGemeinde(gemeinde);
		container.setGesuchsperiode(gesuchsperiode);
		container.setAngabenDeklaration(new FerienbetreuungAngaben());
		return persistence.persist(container);
	}

	@Nonnull
	@Override
	public void saveKommentar(@Nonnull String containerId, @Nonnull String kommentar) {
		FerienbetreuungAngabenContainer container =
			this.findFerienbetreuungAngabenContainer(containerId)
				.orElseThrow(() -> new EbeguEntityNotFoundException(
					"saveKommentar",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					containerId)
				);
		container.setInternerKommentar(kommentar);
		persistence.persist(container);
	}

	@Nonnull
	@Override
	public Optional<FerienbetreuungAngabenStammdaten> findFerienbetreuungAngabenStammdaten(@Nonnull String stammdatenId) {
		Objects.requireNonNull(stammdatenId, "id muss gesetzt sein");

		FerienbetreuungAngabenStammdaten stammdaten =
			persistence.find(FerienbetreuungAngabenStammdaten.class, stammdatenId);

		return Optional.ofNullable(stammdaten);
	}

	@Nonnull
	@Override
	public Optional<FerienbetreuungAngabenAngebot> findFerienbetreuungAngabenAngebot(@Nonnull String angebotId) {
		Objects.requireNonNull(angebotId, "id muss gesetzt sein");

		FerienbetreuungAngabenAngebot angebot =
			persistence.find(FerienbetreuungAngabenAngebot.class, angebotId);

		return Optional.ofNullable(angebot);
	}

	@Nonnull
	@Override
	public Optional<FerienbetreuungAngabenNutzung> findFerienbetreuungAngabenNutzung(@Nonnull String nutzungId) {
		Objects.requireNonNull(nutzungId, "id muss gesetzt sein");

		FerienbetreuungAngabenNutzung nutzung =
			persistence.find(FerienbetreuungAngabenNutzung.class, nutzungId);

		return Optional.ofNullable(nutzung);
	}

	@Nonnull
	@Override
	public Optional<FerienbetreuungAngabenKostenEinnahmen> findFerienbetreuungAngabenKostenEinnahmen(@Nonnull String kostenEinnahmenId) {
		Objects.requireNonNull(kostenEinnahmenId, "id muss gesetzt sein");

		FerienbetreuungAngabenKostenEinnahmen kostenEinnahmen =
			persistence.find(FerienbetreuungAngabenKostenEinnahmen.class, kostenEinnahmenId);

		return Optional.ofNullable(kostenEinnahmen);
	}

	@Nonnull
	@Override
	public FerienbetreuungAngabenStammdaten saveFerienbetreuungAngabenStammdaten(@Nonnull FerienbetreuungAngabenStammdaten stammdaten) {
		return persistence.merge(stammdaten);
	}

	@Nonnull
	@Override
	public FerienbetreuungAngabenAngebot saveFerienbetreuungAngabenAngebot(@Nonnull FerienbetreuungAngabenAngebot angebot) {
		return persistence.merge(angebot);
	}

	@Nonnull
	@Override
	public FerienbetreuungAngabenNutzung saveFerienbetreuungAngabenNutzung(@Nonnull FerienbetreuungAngabenNutzung nutzung) {
		return persistence.merge(nutzung);
	}

	@Nonnull
	@Override
	public FerienbetreuungAngabenKostenEinnahmen saveFerienbetreuungAngabenKostenEinnahmen(@Nonnull FerienbetreuungAngabenKostenEinnahmen kostenEinnahmen) {
		return persistence.merge(kostenEinnahmen);
	}
}


