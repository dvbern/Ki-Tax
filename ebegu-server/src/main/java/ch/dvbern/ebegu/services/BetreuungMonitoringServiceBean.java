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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.entities.AbstractEntity_;
import ch.dvbern.ebegu.entities.BetreuungMonitoring;
import ch.dvbern.ebegu.entities.BetreuungMonitoring_;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.services.util.SearchUtil;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.apache.commons.lang3.StringUtils;

/**
 * Service fuer BetreuungMonitoring
 */
@Stateless
@Local(BetreuungMonitoringService.class)
public class BetreuungMonitoringServiceBean extends AbstractBaseService implements BetreuungMonitoringService {

	@Inject
	private Persistence persistence;

	@Override
	@Nonnull
	public Collection<BetreuungMonitoring> getAllBetreuungMonitoringBeiCriteria(
		@Nullable String refNummer,
		@Nullable String benutzer){
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<BetreuungMonitoring> query = cb.createQuery(BetreuungMonitoring.class);
		Root<BetreuungMonitoring> root = query.from(BetreuungMonitoring.class);
		List<Predicate> predicates = new ArrayList<>();
		if(!StringUtils.isEmpty(refNummer)) {
			Predicate refNummerPredicate = cb.equal(root.get(BetreuungMonitoring_.refNummer), refNummer);
			predicates.add(refNummerPredicate);
		}
		if(!StringUtils.isEmpty(benutzer)) {
			Predicate benutzendePredicate = cb.like(root.get(BetreuungMonitoring_.benutzer),
				SearchUtil.withWildcards(benutzer));
			predicates.add(benutzendePredicate);
		}
		query.orderBy(cb.desc(root.get(AbstractEntity_.timestampErstellt)));
		if (predicates.size() > 0) {
			query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));
			TypedQuery<BetreuungMonitoring> typedQuery = persistence.getEntityManager().createQuery(query);
			// we only want no restriction if the referenznummer is given, it can be a lot of result by benutzer
			if (!StringUtils.isEmpty(benutzer)) {
				typedQuery.setMaxResults(200);
			}
			return typedQuery.getResultList();
		}

		return persistence.getEntityManager().createQuery(query).setMaxResults(200).getResultList();
	}

	@Override
	@Nonnull
	public BetreuungMonitoring saveBetreuungMonitoring(@Nonnull BetreuungMonitoring betreuungMonitoring){
		return persistence.persist(betreuungMonitoring);
	}
}
