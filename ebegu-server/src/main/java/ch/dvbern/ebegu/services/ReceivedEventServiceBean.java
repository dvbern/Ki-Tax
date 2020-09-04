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
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.ReceivedEvent;
import ch.dvbern.ebegu.entities.ReceivedEvent_;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.lib.cdipersistence.Persistence;

@Stateless
@Local(ReceivedEventService.class)
public class ReceivedEventServiceBean implements ReceivedEventService {

	@Inject
	private Persistence persistence;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Override
	public boolean saveReceivedEvent(@Nonnull ReceivedEvent event) {
		Optional<ReceivedEvent> receivedEventOpt = findByEventId(event.getEventId());
		if(receivedEventOpt.isPresent()){
			return false;
		}
		persistence.persist(event);
		return true;
	}

	private Optional<ReceivedEvent> findByEventId(String eventId){
		return criteriaQueryHelper.getEntityByUniqueAttribute(ReceivedEvent.class, eventId, ReceivedEvent_.eventId);
	}
}
