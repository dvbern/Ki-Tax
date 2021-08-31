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

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.entities.AbstractEntity_;
import ch.dvbern.ebegu.entities.BetreuungMonitoring;
import ch.dvbern.ebegu.entities.BetreuungMonitoring_;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.lib.cdipersistence.Persistence;

/**
 * Service fuer BetreuungMonitoring
 */
@Stateless
@Local(BetreuungMonitoringService.class)
public class BetreuungMonitoringServiceBean extends AbstractBaseService implements BetreuungMonitoringService {

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Inject
	private Persistence persistence;

	@Nonnull
	@Override
	public Collection<BetreuungMonitoring> getAllBetreuungMonitoringInfos() {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<BetreuungMonitoring> query = cb.createQuery(BetreuungMonitoring.class);
		Root<BetreuungMonitoring> root = query.from(BetreuungMonitoring.class);
		query.orderBy(cb.desc(root.get(AbstractEntity_.timestampErstellt)));
		return persistence.getEntityManager().createQuery(query).setMaxResults(200).getResultList();
	}

	@Override
	@Nonnull
	public Collection<BetreuungMonitoring> getAllBetreuungMonitoringFuerRefNummer(@Nonnull String refNummer){
		return criteriaQueryHelper.getEntitiesByAttribute(BetreuungMonitoring.class, refNummer,
			BetreuungMonitoring_.refNummer);
	}

	@Override
	@Nonnull
	public BetreuungMonitoring saveBetreuungMonitoring(@Nonnull BetreuungMonitoring betreuungMonitoring){
		return persistence.persist(betreuungMonitoring);
	}
}
