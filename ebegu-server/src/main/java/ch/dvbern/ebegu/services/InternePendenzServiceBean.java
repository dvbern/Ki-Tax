/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Dossier_;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Fall_;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuch_;
import ch.dvbern.ebegu.entities.InternePendenz;
import ch.dvbern.ebegu.entities.InternePendenz_;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.lib.cdipersistence.Persistence;

/**
 * Service fuer interne Pendenzen
 */
@Stateless
@Local(InternePendenzService.class)
public class InternePendenzServiceBean extends AbstractBaseService implements InternePendenzService {

	@Inject
	private Persistence persistence;

	@Inject
	private Authorizer authorizer;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Inject
	private PrincipalBean principalBean;

	@Nonnull
	@Override
	public Optional<InternePendenz> findInternePendenz(@Nonnull String internePendenzId) {
		Objects.requireNonNull(internePendenzId);

		Optional<InternePendenz> internePendenz = Optional.ofNullable(persistence.find(InternePendenz.class, internePendenzId));
		internePendenz.ifPresent(pendenz -> authorizer.checkReadAuthorization(pendenz));
		return internePendenz;
	}

	@Nonnull
	@Override
	public InternePendenz updateInternePendenz(@Nonnull InternePendenz internePendenz) {
		Objects.requireNonNull(internePendenz);
		Objects.requireNonNull(internePendenz.getId());

		authorizer.checkWriteAuthorization(internePendenz);
		return persistence.merge(internePendenz);
	}

	@Nonnull
	@Override
	public InternePendenz createInternePendenz(@Nonnull InternePendenz internePendenz) {
		Objects.requireNonNull(internePendenz);
		internePendenz.getGesuch().setInternePendenz(true);
		return persistence.persist(internePendenz);
	}

	@Nonnull
	@Override
	public Collection<InternePendenz> findInternePendenzenForGesuch(@Nonnull Gesuch gesuch) {
		Objects.requireNonNull(gesuch);
		Objects.requireNonNull(gesuch.getId());

		authorizer.checkReadAuthorization(gesuch);
		return criteriaQueryHelper.getEntitiesByAttribute(InternePendenz.class, gesuch, InternePendenz_.gesuch);
	}

	@Nonnull
	@Override
	public Long countInternePendenzenForGesuch(@Nonnull Gesuch gesuch) {
		Objects.requireNonNull(gesuch);
		Objects.requireNonNull(gesuch.getId());

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Long> query = cb.createQuery(Long.class);
		Root<InternePendenz> root = query.from(InternePendenz.class);

		List<Predicate> predicates = new ArrayList<>();

		Predicate predicateNichtErledigt = cb.equal(root.get(InternePendenz_.erledigt), false);
		predicates.add(predicateNichtErledigt);
		Predicate predicateGesuch = cb.equal(root.get(InternePendenz_.gesuch), gesuch);
		predicates.add(predicateGesuch);

		query.select(cb.countDistinct(root));
		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));
		return persistence.getCriteriaSingleResult(query);
	}

	@Override
	public void deleteInternePendenz(@Nonnull InternePendenz internePendenz) {
		Objects.requireNonNull(internePendenz);

		authorizer.checkWriteAuthorization(internePendenz);
		persistence.remove(internePendenz);
	}

	@Nonnull
	@Override
	public Collection<InternePendenz> findAlleAbgelaufendeInternePendenzen() {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<InternePendenz> query = cb.createQuery(InternePendenz.class);
		Root<InternePendenz> root = query.from(InternePendenz.class);
		Join<InternePendenz, Gesuch> gesuchJoin = root.join(InternePendenz_.gesuch);
		Join<Gesuch, Dossier> dossierJoin = gesuchJoin.join(Gesuch_.dossier);
		Join<Dossier, Fall> fallJoin = dossierJoin.join(Dossier_.fall);


		List<Predicate> predicates = new ArrayList<>();

		Predicate predicateNichtErledigt = cb.equal(root.get(InternePendenz_.erledigt), false);
		predicates.add(predicateNichtErledigt);

		Predicate predicateMandant = cb.equal(fallJoin.get(Fall_.mandant), principalBean.getMandant());
		predicates.add(predicateMandant);

		Predicate predicateAbgelaufen = cb.lessThanOrEqualTo(root.get(InternePendenz_.termin), LocalDate.now());
		predicates.add(predicateAbgelaufen);
		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));
		return persistence.getCriteriaResults(query);
	}

	@Nonnull
	@Override
	public boolean hasGesuchAbgelaufeneInternePendenzen(@Nonnull Gesuch gesuch) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<InternePendenz> query = cb.createQuery(InternePendenz.class);
		Root<InternePendenz> root = query.from(InternePendenz.class);

		List<Predicate> predicates = new ArrayList<>();

		Predicate predicateNichtErledigt = cb.equal(root.get(InternePendenz_.erledigt), false);
		predicates.add(predicateNichtErledigt);

		Predicate predicateAbgelaufen = cb.lessThanOrEqualTo(root.get(InternePendenz_.termin), LocalDate.now());
		predicates.add(predicateAbgelaufen);

		Predicate predicateGesuch = cb.equal(root.get(InternePendenz_.gesuch), gesuch);
		predicates.add(predicateGesuch);

		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));
		return persistence.getCriteriaResults(query).stream().count() > 0;
	}

	@Override
	public void deleteAllInternePendenz(@Nonnull Gesuch gesuch) {
		findInternePendenzenForGesuch(gesuch).forEach(
			this::deleteInternePendenz
		);
	}
}
