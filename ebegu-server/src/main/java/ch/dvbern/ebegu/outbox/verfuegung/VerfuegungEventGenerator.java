/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.outbox.verfuegung;

import java.util.List;

import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.entities.AbstractPlatz_;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.Verfuegung_;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.outbox.ExportedEvent;
import ch.dvbern.lib.cdipersistence.Persistence;

@Stateless
public class VerfuegungEventGenerator {

	@Inject
	private Persistence persistence;

	@Inject
	private Event<ExportedEvent> event;

	@Inject
	private VerfuegungEventConverter verfuegungEventConverter;

	/**
	 * Each new Verfuegung is published to Kafka via the outbox event system. However, there are already Verfuegungn
	 * in the database which have not been published, because the outbox event system has been added later. Thus,
	 * fetch all these Verfuegungen and publish them once.
	 */
	@Schedule(info = "Migration-aid, pushes already existing Verfuegungen to outbox", hour = "5", persistent = true)
	public void publishExistingVerfuegungen() {
		CriteriaBuilder cb = persistence.getCriteriaBuilder();
		CriteriaQuery<Verfuegung> query = cb.createQuery(Verfuegung.class);
		Root<Verfuegung> root = query.from(Verfuegung.class);
		Path<Betreuung> betreuungPath = root.get(Verfuegung_.betreuung);

		ParameterExpression<Betreuungsstatus> statusParam = cb.parameter(Betreuungsstatus.class);
		Predicate isVerfuegt = cb.equal(betreuungPath.get(AbstractPlatz_.betreuungsstatus), statusParam);

		Predicate isGueltig = cb.isTrue(betreuungPath.get(AbstractPlatz_.gueltig));

		Predicate isNotPublished = cb.isFalse(root.get(Verfuegung_.eventPublished));

		query.where(isGueltig, isNotPublished, isVerfuegt);

		List<Verfuegung> verfuegungen = persistence.getEntityManager().createQuery(query)
			.setParameter(statusParam, Betreuungsstatus.VERFUEGT)
			.getResultList();

		verfuegungen.forEach(verfuegung -> {
			event.fire(verfuegungEventConverter.of(verfuegung));

			verfuegung.setSkipPreUpdate(true);
			verfuegung.setEventPublished(true);
			persistence.merge(verfuegung);
		});
	}
}
