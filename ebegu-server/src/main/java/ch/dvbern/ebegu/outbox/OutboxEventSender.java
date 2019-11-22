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

package ch.dvbern.ebegu.outbox;

import javax.ejb.Stateless;
import javax.enterprise.event.Observes;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
public class OutboxEventSender {

	private static final Logger LOG = LoggerFactory.getLogger(OutboxEventSender.class);

	@PersistenceContext(unitName = "ebeguPersistenceUnit")
	private EntityManager entityManager;

	/**
	 * persists an ExportedEvent in the OutboxEvent table, such that the publisher can read the event and send it to
	 * Kafka.
	 */
	public void onExportedEvent(@Observes ExportedEvent event) {
		LOG.info("ExportedEvent {}", event);
		OutboxEvent outboxEvent = new OutboxEvent(
			event.getAggregateType(),
			event.getAggregateId(),
			event.getType(),
			event.getPayload(),
			event.getSchema()
		);

		entityManager.persist(outboxEvent);
	}
}
