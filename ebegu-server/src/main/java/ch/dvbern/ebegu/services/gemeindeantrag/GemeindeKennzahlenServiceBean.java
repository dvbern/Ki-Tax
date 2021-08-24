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

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
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
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.gemeindeantrag.gemeindekennzahlen.GemeindeKennzahlen;
import ch.dvbern.ebegu.entities.gemeindeantrag.gemeindekennzahlen.GemeindeKennzahlenStatus;
import ch.dvbern.ebegu.entities.gemeindeantrag.gemeindekennzahlen.GemeindeKennzahlen_;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.services.AbstractBaseService;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service fuer Gemeindeantraege
 */
@Stateless
@Local(GemeindeKennzahlenService.class)
public class GemeindeKennzahlenServiceBean extends AbstractBaseService implements GemeindeKennzahlenService {

	private static final String ID_MUSS_GESETZT_SEIN = "id muss gesetzt sein";

	@Inject
	private PrincipalBean principal;

	@Inject
	private Persistence persistence;

	@Inject
	private GemeindeService gemeindeService;

	private static final Logger LOG = LoggerFactory.getLogger(GemeindeKennzahlenServiceBean.class);

	@Nonnull
	@Override
	public List<GemeindeKennzahlen> createGemeindeKennzahlen(
			@Nonnull Gesuchsperiode gesuchsperiode) {
		return gemeindeService.getAktiveGemeinden()
				.stream()
				.filter(Gemeinde::isAngebotBG)
				.filter(gemeinde -> !antragAlreadyExisting(gemeinde, gesuchsperiode))
				.map(gemeinde -> {
					GemeindeKennzahlen gemeindeKennzahlen = new GemeindeKennzahlen();
					gemeindeKennzahlen.setGemeinde(gemeinde);
					gemeindeKennzahlen.setGesuchsperiode(gesuchsperiode);
					gemeindeKennzahlen.setStatus(GemeindeKennzahlenStatus.IN_BEARBEITUNG_GEMEINDE);
					return persistence.persist(gemeindeKennzahlen);
				}).collect(Collectors.toList());
	}

	private boolean antragAlreadyExisting(Gemeinde gemeinde, Gesuchsperiode gesuchsperiode) {
		boolean hasAntrag =  !getGemeindeKennzahlen(gemeinde, gesuchsperiode, null, null).isEmpty();
		if (hasAntrag) {
			LOG.info("Gemeinde {} already has an antrag in GS {}", gemeinde.getName(), gesuchsperiode.getGesuchsperiodeString());
		}
		return hasAntrag;
	}

	@Nonnull
	@Override
	public Optional<GemeindeKennzahlen> findGemeindeAntrag(@Nonnull String id) {
		Objects.requireNonNull(id, ID_MUSS_GESETZT_SEIN);

		GemeindeKennzahlen gemeindeKennzahlen = persistence.find(GemeindeKennzahlen.class, id);

		return Optional.ofNullable(gemeindeKennzahlen);
	}

	@Nonnull
	@Override
	public List<GemeindeKennzahlen> getGemeindeKennzahlen(
			@Nullable Gemeinde gemeinde,
			@Nullable Gesuchsperiode gesuchsperiode,
			@Nullable String status,
			@Nullable String timestampMutiert) {

		Set<Gemeinde> gemeinden = principal.getBenutzer().extractGemeindenForUser();

		Set<Predicate> predicates = new HashSet<>();
		CriteriaBuilder cb = persistence.getCriteriaBuilder();
		CriteriaQuery<GemeindeKennzahlen> query = cb.createQuery(GemeindeKennzahlen.class);
		Root<GemeindeKennzahlen> root = query.from(GemeindeKennzahlen.class);

		if (!principal.isCallerInAnyOfRole(
				UserRole.SUPER_ADMIN,
				UserRole.ADMIN_MANDANT,
				UserRole.SACHBEARBEITER_MANDANT)) {
			Predicate gemeindeIn =
					root.get(GemeindeKennzahlen_.gemeinde).in(gemeinden);
			predicates.add(gemeindeIn);
		}

		if (gemeinde != null) {
			predicates.add(createGemeindePredicate(cb, root, gemeinde));
		}

		if (gesuchsperiode != null) {
			predicates.add(createGesuchsperiodePredicate(cb, root, gesuchsperiode));
		}

		if (status != null) {
			predicates.add(createStatusPredicate(cb, root, status));
		}

		if (timestampMutiert != null) {
			predicates.add(createTimestampMutiertPredicate(cb, root, timestampMutiert));
		}

		Predicate[] predicatesArray = new Predicate[predicates.size()];
		query.where(predicates.toArray(predicatesArray));

		return persistence.getCriteriaResults(query);
	}

	private Predicate createGemeindePredicate(
			CriteriaBuilder cb,
			Root<GemeindeKennzahlen> root,
			Gemeinde gemeinde) {
		return cb.equal(
				root.get(GemeindeKennzahlen_.gemeinde),
				gemeinde
		);
	}

	private Predicate createGesuchsperiodePredicate(
			CriteriaBuilder cb,
			Root<GemeindeKennzahlen> root,
			Gesuchsperiode gesuchsperiode) {
		return cb.equal(
				root.get(GemeindeKennzahlen_.gesuchsperiode),
				gesuchsperiode
		);
	}

	private Predicate createStatusPredicate(CriteriaBuilder cb, Root<GemeindeKennzahlen> root, String status) {
		return cb.equal(
				root.get(GemeindeKennzahlen_.status),
				status
		);
	}

	private Predicate createTimestampMutiertPredicate(
			CriteriaBuilder cb,
			Root<GemeindeKennzahlen> root,
			String timestampMutiert) {

		Predicate timestampMutiertPredicate;
		try {
			// Wir wollen ohne Zeit vergleichen
			Expression<LocalDate> timestampAsLocalDate =
					root.get(GemeindeKennzahlen_.timestampMutiert).as(LocalDate.class);
			LocalDate searchDate = LocalDate.parse(timestampMutiert, Constants.DATE_FORMATTER);
			timestampMutiertPredicate = cb.equal(timestampAsLocalDate, searchDate);
		} catch (DateTimeParseException e) {
			// no valid date. we return false, since no antrag should be found
			timestampMutiertPredicate = cb.disjunction();
		}
		return timestampMutiertPredicate;
	}

	@Override
	public void deleteGemeindeKennzahlen(
			@Nonnull Gemeinde gemeinde, @Nonnull Gesuchsperiode gesuchsperiode) {
		getGemeindeKennzahlen(gemeinde, gesuchsperiode, null, null)
				.forEach(gemeindeKennzahlen -> {
					persistence.remove(gemeindeKennzahlen);
					LOG.warn(
							"Removed GemeindeKennzahlen for Gemeinde {} in GS {}",
							gemeinde.getName(),
							gesuchsperiode.getGesuchsperiodeString());
				});
	}
}


