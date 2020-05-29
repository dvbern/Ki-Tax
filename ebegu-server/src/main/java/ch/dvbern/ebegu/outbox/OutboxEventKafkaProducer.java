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
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.entities.AbstractEntity_;
import ch.dvbern.kibon.exchange.commons.util.AvroConverter;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.kibon.exchange.commons.util.EventUtil.MESSAGE_HEADER_EVENT_ID;
import static ch.dvbern.kibon.exchange.commons.util.EventUtil.MESSAGE_HEADER_EVENT_TYPE;
import static com.google.common.base.Preconditions.checkNotNull;
import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.ACKS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;

@Stateless
public class OutboxEventKafkaProducer {

	private static final Logger LOG = LoggerFactory.getLogger(OutboxEventKafkaProducer.class);

	@PersistenceContext(unitName = "ebeguPersistenceUnit")
	private EntityManager entityManager;

	@Inject
	private EbeguConfiguration ebeguConfiguration;

	/**
	 * Uses polling to detect new OutboxEvents, fetches them from the database and transmits them to Kafka.
	 * If everything succeds, the OutboxEvents are deleted from the database.
	 */
	@Schedule(info = "publish outbox events", minute = "*", hour = "*", persistent = true)
	public void publishEvents() {
		if (!ebeguConfiguration.getKafkaURL().isPresent()) {
			LOG.debug("Kafka URL not set, not publishing events.");
			return;
		}

		try {
			CriteriaBuilder cb = entityManager.getCriteriaBuilder();
			CriteriaQuery<OutboxEvent> query = cb.createQuery(OutboxEvent.class);
			Root<OutboxEvent> root = query.from(OutboxEvent.class);

			query.orderBy(cb.asc(root.get(AbstractEntity_.timestampErstellt)));

			List<OutboxEvent> events = entityManager.createQuery(query)
				// lock until we are done
				.setLockMode(LockModeType.PESSIMISTIC_WRITE)
				.getResultList();

			if (events.isEmpty()) {
				LOG.debug("No OutboxEvents to publish.");
				return;
			}

			LOG.info("Going to publish {} OutboxEvents", events.size());

			Properties props = new Properties();
			props.setProperty(BOOTSTRAP_SERVERS_CONFIG, ebeguConfiguration.getKafkaURL().get());
			props.setProperty(ACKS_CONFIG, "all");
			props.setProperty(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
			props.setProperty(VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
			props.setProperty(SCHEMA_REGISTRY_URL_CONFIG, ebeguConfiguration.getSchemaRegistryURL());

			Producer<String, GenericRecord> producer = new KafkaProducer<>(props);

			events.stream()
				.map(this::toProducerRecord)
				.forEach(producer::send);

			producer.close();

			events.forEach(entityManager::remove);

		} catch (RuntimeException e) {
			// When a timer fails, it's called again sometime later. If that timer fails as well, the schedule is
			// cancelled: https://stackoverflow.com/a/10598938
			LOG.error("Kafka export failed", e);
		}
	}

	/**
	 * There are three approaches I considered for OutboxEvent AVRO payload with schema registry:
	 * <ol>
	 *    <li>Use KafkaAvroSerializer when creating an OutboxEvent.<br>
	 *        Downside: the business transaction requires a running schema registry.</li>
	 *    <li>Write a custom KafkaAvroSerializer, which does the schema registration & caching and just forwards the
	 *    already serialized avro binary payload.<br>
	 *        Downside: must keep track of API changes of the schema registry.</li>
	 *    <li>Deserialize the avro payload and use KafkaAvroSerializer in a standard fashion.<br>
	 *        Downside: performance penalty from redundant deserialization followed by another serialization</li>
	 * </ol>
	 * <p>
	 * The last approach was chosen, because it is the most stable solution and as long as we are not dealing with
	 * lots and lots of events the pefromance penalty should be negligible.
	 * </p>
	 */
	@Nonnull
	private ProducerRecord<String, GenericRecord> toProducerRecord(@Nonnull OutboxEvent outboxEvent) {
		String key = outboxEvent.getAggregateId();
		String topic = outboxEvent.getAggregateType() + "Events";
		String eventId = outboxEvent.getId();
		String eventType = outboxEvent.getType();
		byte[] payload = outboxEvent.getPayload();

		GenericRecord specificRecordBase = AvroConverter.fromAvroBinaryGeneric(outboxEvent.getAvroSchema(), payload);

		// adding some metadata
		Iterable<Header> headers = Arrays.asList(
			new RecordHeader(MESSAGE_HEADER_EVENT_ID, eventId.getBytes(StandardCharsets.UTF_8)),
			new RecordHeader(MESSAGE_HEADER_EVENT_TYPE, eventType.getBytes(StandardCharsets.UTF_8))
		);

		long timestamp = checkNotNull(outboxEvent.getTimestampErstellt())
			.atZone(ZoneId.systemDefault())
			.toInstant()
			.toEpochMilli();

		return new ProducerRecord<>(topic, null, timestamp, key, specificRecordBase, headers);
	}
}
