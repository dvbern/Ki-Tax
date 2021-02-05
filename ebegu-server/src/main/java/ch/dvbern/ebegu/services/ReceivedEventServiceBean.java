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

package ch.dvbern.ebegu.services;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.entities.ReceivedEvent;
import ch.dvbern.ebegu.entities.ReceivedEvent_;
import ch.dvbern.lib.cdipersistence.Persistence;

@Stateless
@Local(ReceivedEventService.class)
public class ReceivedEventServiceBean implements ReceivedEventService {

	@Inject
	private Persistence persistence;

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public boolean saveReceivedEvent(@Nonnull ReceivedEvent event) {
		Optional<ReceivedEvent> receivedEventOpt = findByEventId(event.getEventId());
		if(receivedEventOpt.isPresent()){
			return false;
		}
		persistence.persist(event);
		return true;
	}

	private Optional<ReceivedEvent> findByEventId(String eventId){
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<ReceivedEvent> query = cb.createQuery(ReceivedEvent.class);
		Root<ReceivedEvent> root = query.from(ReceivedEvent.class);
		Predicate predicateEventId = cb.equal(root.get(ReceivedEvent_.eventId), eventId);
		query.where(predicateEventId);
		return Optional.ofNullable(persistence.getCriteriaSingleResult(query));
	}
}
