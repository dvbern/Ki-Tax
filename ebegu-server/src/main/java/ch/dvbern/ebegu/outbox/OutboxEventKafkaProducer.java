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

import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.annotation.Nonnull;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;

import static ch.dvbern.kibon.exchange.commons.ObjectMapperUtil.MESSAGE_HEADER_EVENT_ID;
import static ch.dvbern.kibon.exchange.commons.ObjectMapperUtil.MESSAGE_HEADER_EVENT_TYPE;
import static com.google.common.base.Preconditions.checkNotNull;

@Stateless
public class OutboxEventKafkaProducer {

	@PersistenceContext(unitName = "ebeguPersistenceUnit")
	private EntityManager entityManager;

	/**
	 * Uses polling to detect new OutboxEvents, fetches them from the database and transmits them to Kafka.
	 * If everything succeds, the OutboxEvents are deleted from the database.
	 */
	@Schedule(info = "publish outbox events", minute = "*", hour = "*", persistent = true)
	public void publishEvents() {

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<OutboxEvent> query = cb.createQuery(OutboxEvent.class);
		query.from(OutboxEvent.class);

		List<OutboxEvent> events = entityManager.createQuery(query)
			// lock until we are done
			.setLockMode(LockModeType.PESSIMISTIC_WRITE)
			.getResultList();

		if (events.isEmpty()) {
			return;
		}

		// todo make configurable
		Properties props = new Properties();
		props.setProperty("bootstrap.servers", "localhost:9092");
		props.setProperty("acks", "all");
		props.setProperty("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		props.setProperty("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");

		Producer<String, byte[]> producer = new KafkaProducer<>(props);

		events.stream()
			.map(this::toProducerRecord)
			.forEach(producer::send);

		producer.close();

		events.forEach(entityManager::remove);
	}

	@Nonnull
	private ProducerRecord<String, byte[]> toProducerRecord(@Nonnull OutboxEvent outboxEvent) {
		String key = outboxEvent.getAggregateId();
		String topic = outboxEvent.getAggregateType() + "Events";
		String eventId = outboxEvent.getId();
		String eventType = outboxEvent.getType();
		byte[] payload = outboxEvent.getPayload();

		// adding some metadata
		Iterable<Header> headers = Arrays.asList(
			new RecordHeader(MESSAGE_HEADER_EVENT_ID, eventId.getBytes(StandardCharsets.UTF_8)),
			new RecordHeader(MESSAGE_HEADER_EVENT_TYPE, eventType.getBytes(StandardCharsets.UTF_8))
		);

		long timestamp = checkNotNull(outboxEvent.getTimestampErstellt())
			.atZone(ZoneId.systemDefault())
			.toInstant()
			.toEpochMilli();

		return new ProducerRecord<>(topic, null, timestamp, key, payload, headers);
	}
}
