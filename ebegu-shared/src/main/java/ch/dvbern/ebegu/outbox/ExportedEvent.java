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

import javax.annotation.Nonnull;

import org.apache.avro.Schema;

public interface ExportedEvent {

	/**
	 * @return <p>the type of the aggregate root to which a given event is related; the idea being, leaning on the same
	 * concept of domain-driven design, that exported events should refer to an aggregate ("a cluster of domain
	 * objects that can be treated as a single unit"), where the aggregate root provides the sole entry point for
	 * accessing any of the entities within the aggregate. This could for instance be "purchase order" or "customer"
	 * .</p>
	 *
	 * <p>This value will be used to route events to corresponding topics in Kafka, so there’d be a topic for all
	 * events related to purchase orders, one topic for all customer-related events etc. Note that also events
	 * pertaining to a child entity contained within one such aggregate should use that same type. So e.g. an event
	 * representing the cancelation of an individual order line (which is part of the purchase order aggregate) should
	 * also use the type of its aggregate root, "order", ensuring that also this event will go into the "order" Kafka
	 * topic.</p>
	 */
	@Nonnull
	String getAggregateType();

	/**
	 * @return <p>the id of the aggregate root that is affected by a given event; this could for instance be the id
	 * of a purchase order or a customer id; Similar to the aggregate type, events pertaining to a sub-entity
	 * contained within an aggregate should use the id of the containing aggregate root, e.g. the purchase order id
	 * for an order line cancelation event. This id will be used as the key for Kafka messages later on. That way, all
	 * events pertaining to one aggregate root or any of its contained sub-entities will go into the same partition of
	 * that Kafka topic, which ensures that consumers of that topic will consume all the events related to one and the
	 * same aggregate in the exact order as they were produced.</p>
	 */
	@Nonnull
	String getAggregateId();

	/**
	 * @return the type of event, e.g. "Order Created" or "Order Line Canceled". Allows consumers to trigger suitable
	 * event handlers.
	 */
	@Nonnull
	String getType();

	/**
	 * @return a (binary) Avro structure with the actual event contents, e.g. containing a purchase order, information
	 * about the purchaser, contained order lines, their price etc.
	 */
	@Nonnull
	byte[] getPayload();

	/**
	 * @return the Avro schma for the generated payload.
	 */
	@Nonnull
	Schema getSchema();
}
