/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.services;

import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.entities.BelegungFerieninselTag;
import ch.dvbern.ebegu.entities.EinstellungenFerieninsel;
import ch.dvbern.ebegu.entities.EinstellungenFerieninsel_;
import ch.dvbern.ebegu.entities.GemeindeStammdatenGesuchsperiode;
import ch.dvbern.ebegu.entities.GemeindeStammdatenGesuchsperiodeFerieninsel;
import ch.dvbern.ebegu.entities.GemeindeStammdatenGesuchsperiodeFerieninselZeitraum;
import ch.dvbern.ebegu.entities.GemeindeStammdatenGesuchsperiodeFerieninsel_;
import ch.dvbern.ebegu.entities.GemeindeStammdatenGesuchsperiode_;
import ch.dvbern.ebegu.entities.Gemeinde_;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Gesuchsperiode_;
import ch.dvbern.ebegu.enums.Ferienname;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.util.DateUtil;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.apache.commons.lang3.StringUtils;

/**
 * Service zum Verwalten von Ferieninsel-Stammdaten
 */
@Stateless
@Local(FerieninselStammdatenService.class)
public class FerieninselStammdatenServiceBean extends AbstractBaseService implements FerieninselStammdatenService {

	@Inject
	private Persistence persistence;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;


	@Nonnull
	@Override
	public List<GemeindeStammdatenGesuchsperiodeFerieninsel> findGesuchsperiodeFerieninselByGemeindeAndPeriode(@Nullable String gemeindeId,
		@Nonnull String gesuchsperiodeId) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<GemeindeStammdatenGesuchsperiodeFerieninsel> query =
			cb.createQuery(GemeindeStammdatenGesuchsperiodeFerieninsel.class);
		Root<GemeindeStammdatenGesuchsperiodeFerieninsel> root = query.from(GemeindeStammdatenGesuchsperiodeFerieninsel.class);
		Join<GemeindeStammdatenGesuchsperiodeFerieninsel, GemeindeStammdatenGesuchsperiode> joinStammdaten =
			root.join(GemeindeStammdatenGesuchsperiodeFerieninsel_.gemeindeStammdatenGesuchsperiode);
		query.select(root);
		Predicate predicateGesuchsperiode =
			cb.equal(joinStammdaten.get(GemeindeStammdatenGesuchsperiode_.gesuchsperiode).get(Gesuchsperiode_.id),
			gesuchsperiodeId);
		if (StringUtils.isNotBlank(gemeindeId)) {
			Predicate gemeindePredicate = cb.equal(joinStammdaten.get(GemeindeStammdatenGesuchsperiode_.gemeinde).get(Gemeinde_.id),
				gemeindeId);

			query.where(predicateGesuchsperiode, gemeindePredicate);
		} else {
			query.where(predicateGesuchsperiode);
		}
		return persistence.getCriteriaResults(query);
	}

	@Override
	public void initFerieninselStammdaten(@Nonnull GemeindeStammdatenGesuchsperiode gemeindeStammdatenGesuchsperiode) {
		GemeindeStammdatenGesuchsperiodeFerieninsel herbstferien = new GemeindeStammdatenGesuchsperiodeFerieninsel();
		herbstferien.setFerienname(Ferienname.HERBSTFERIEN);
		herbstferien.setGemeindeStammdatenGesuchsperiode(gemeindeStammdatenGesuchsperiode);
		persistence.persist(herbstferien);

		GemeindeStammdatenGesuchsperiodeFerieninsel sportferien = new GemeindeStammdatenGesuchsperiodeFerieninsel();
		sportferien.setFerienname(Ferienname.SPORTFERIEN);
		sportferien.setGemeindeStammdatenGesuchsperiode(gemeindeStammdatenGesuchsperiode);
		persistence.persist(sportferien);

		GemeindeStammdatenGesuchsperiodeFerieninsel fruehlingsFerien = new GemeindeStammdatenGesuchsperiodeFerieninsel();
		fruehlingsFerien.setFerienname(Ferienname.FRUEHLINGSFERIEN);
		fruehlingsFerien.setGemeindeStammdatenGesuchsperiode(gemeindeStammdatenGesuchsperiode);
		persistence.persist(fruehlingsFerien);

		GemeindeStammdatenGesuchsperiodeFerieninsel sommerferien = new GemeindeStammdatenGesuchsperiodeFerieninsel();
		sommerferien.setFerienname(Ferienname.SOMMERFERIEN);
		sommerferien.setGemeindeStammdatenGesuchsperiode(gemeindeStammdatenGesuchsperiode);
		persistence.persist(sommerferien);
	}

	@Override
	public void removeFerieninselStammdaten(@Nonnull String gemeindeStammdatenGesuchsperiodeFerieninselId) {
		persistence.remove(GemeindeStammdatenGesuchsperiodeFerieninsel.class, gemeindeStammdatenGesuchsperiodeFerieninselId);
	}

	@Nonnull
	@Override
	public GemeindeStammdatenGesuchsperiodeFerieninsel saveFerieninselStammdaten(
		@Nonnull GemeindeStammdatenGesuchsperiodeFerieninsel ferieninselStammdaten
	) {
		return persistence.merge(ferieninselStammdaten);
	}

	@Nonnull
	@Override
	public Optional<GemeindeStammdatenGesuchsperiodeFerieninsel> findFerieninselStammdaten(@Nonnull String ferieninselStammdatenId) {
		Objects.requireNonNull(ferieninselStammdatenId, "ferieninselStammdatenId muss gesetzt sein");
		GemeindeStammdatenGesuchsperiodeFerieninsel ferieninselStammdaten = persistence.find(GemeindeStammdatenGesuchsperiodeFerieninsel.class, ferieninselStammdatenId);
		return Optional.ofNullable(ferieninselStammdaten);
	}

	@Override
	public void copyEinstellungenFerieninselToNewGesuchsperiode(
		@Nonnull Gesuchsperiode gesuchsperiodeToCreate,
		@Nonnull Gesuchsperiode lastGesuchsperiode
	) {
		Collection<EinstellungenFerieninsel> lastEinstellungenFerieninsel =
			findEinstellungenFerieninselByGesuchsperiode(lastGesuchsperiode);
		lastEinstellungenFerieninsel.forEach(lastEinstellung -> {
			EinstellungenFerieninsel newEinstellung = lastEinstellung.copyForGesuchsperiode(gesuchsperiodeToCreate);
			persistence.merge(newEinstellung);
		});
	}

	@Override
	public Collection<EinstellungenFerieninsel> findEinstellungenFerieninselByGesuchsperiode(@Nonnull Gesuchsperiode gesuchsperiode) {
		return
			criteriaQueryHelper.getEntitiesByAttribute(
				EinstellungenFerieninsel.class, gesuchsperiode, EinstellungenFerieninsel_.gesuchsperiode);
	}

	@Nonnull
	@Override
	public Optional<GemeindeStammdatenGesuchsperiodeFerieninsel> findFerieninselStammdatenForGesuchsperiodeAndFerienname(
			@Nonnull String gemeindeId, @Nonnull String gesuchsperiodeId, @Nonnull Ferienname ferienname) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<GemeindeStammdatenGesuchsperiodeFerieninsel> query = cb.createQuery(GemeindeStammdatenGesuchsperiodeFerieninsel.class);
		Root<GemeindeStammdatenGesuchsperiodeFerieninsel> root = query.from(GemeindeStammdatenGesuchsperiodeFerieninsel.class);
		query.select(root);

		Join<GemeindeStammdatenGesuchsperiodeFerieninsel, GemeindeStammdatenGesuchsperiode> gesuchsperiodeJoin =
			root.join(GemeindeStammdatenGesuchsperiodeFerieninsel_.gemeindeStammdatenGesuchsperiode, JoinType.LEFT);

		Predicate predicateGesuchsperiode =
			cb.equal(gesuchsperiodeJoin.get(GemeindeStammdatenGesuchsperiode_.gesuchsperiode).get(Gesuchsperiode_.id),
				gesuchsperiodeId);

		Predicate predicateGemeinde =
			cb.equal(gesuchsperiodeJoin.get(GemeindeStammdatenGesuchsperiode_.gemeinde).get(Gemeinde_.id),
				gemeindeId);

		Predicate predicateFerienname = cb.equal(root.get(GemeindeStammdatenGesuchsperiodeFerieninsel_.ferienname), ferienname);

		query.where(predicateGesuchsperiode, predicateGemeinde, predicateFerienname);
		query.orderBy(cb.asc(root.get(GemeindeStammdatenGesuchsperiodeFerieninsel_.ferienname)));
		GemeindeStammdatenGesuchsperiodeFerieninsel fiStammdatenOrNull = persistence.getCriteriaSingleResult(query);
		return Optional.ofNullable(fiStammdatenOrNull);
	}

	@Nonnull
	@Override
	public List<BelegungFerieninselTag> getPossibleFerieninselTage(@Nonnull GemeindeStammdatenGesuchsperiodeFerieninsel ferieninselStammdaten) {
		List<BelegungFerieninselTag> potentielleFerieninselTage = new LinkedList<>();
		for (GemeindeStammdatenGesuchsperiodeFerieninselZeitraum ferieninselZeitraum : ferieninselStammdaten.getZeitraumList()) {
			potentielleFerieninselTage.addAll(getPossibleFerieninselTageForZeitraum(ferieninselZeitraum));
		}
		return potentielleFerieninselTage;
	}

	private List<BelegungFerieninselTag> getPossibleFerieninselTageForZeitraum(GemeindeStammdatenGesuchsperiodeFerieninselZeitraum zeitraum) {
		List<BelegungFerieninselTag> potentielleFerieninselTage = new LinkedList<>();
		LocalDate currentDate = zeitraum.getGueltigkeit().getGueltigAb();
		while (!currentDate.isAfter(zeitraum.getGueltigkeit().getGueltigBis())) {
			if (!DateUtil.isWeekend(currentDate) && !DateUtil.isHoliday(currentDate)) {
				BelegungFerieninselTag belegungTag = new BelegungFerieninselTag();
				belegungTag.setTag(currentDate);
				potentielleFerieninselTage.add(belegungTag);
			}
			currentDate = currentDate.plusDays(1);
		}
		return potentielleFerieninselTage;
	}
}
