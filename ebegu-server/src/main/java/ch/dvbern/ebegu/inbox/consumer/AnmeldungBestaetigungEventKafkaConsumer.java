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

package ch.dvbern.ebegu.inbox.consumer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import javax.annotation.Nonnull;
import javax.annotation.PreDestroy;
import javax.annotation.security.RunAs;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.enums.UserRoleName;
import ch.dvbern.ebegu.inbox.handler.AnmeldungBestaetigungEventHandler;
import ch.dvbern.ebegu.kafka.MessageProcessor;
import ch.dvbern.kibon.exchange.commons.tagesschulen.TagesschuleBestaetigungEventDTO;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;

@Startup
@Singleton
@RunAs(UserRoleName.SUPER_ADMIN)
public class AnmeldungBestaetigungEventKafkaConsumer {

	private static final Logger LOG = LoggerFactory.getLogger(AnmeldungBestaetigungEventKafkaConsumer.class);

	@Inject
	private EbeguConfiguration ebeguConfiguration;

	@Inject
	private AnmeldungBestaetigungEventHandler eventHandler;

	@Inject
	private MessageProcessor processor;

	private Consumer<String, TagesschuleBestaetigungEventDTO> consumer = null;

	private void startKafkaAnmeldungBestaetigungConsumer() {
		if (ebeguConfiguration.getKafkaURL().isEmpty()
			|| !ebeguConfiguration.isAnmeldungTagesschuleApiEnabled()
			|| !ebeguConfiguration.isKafkaConsumerEnabled()) {
			LOG.debug("Kafka URL not set or Anmeldung Api is not enabled, not consuming events.");
			return;
		}
		Properties props = new Properties();
		props.setProperty(BOOTSTRAP_SERVERS_CONFIG, ebeguConfiguration.getKafkaURL().get());
		String groupId = ebeguConfiguration.getKafkaConsumerGroupId();
		props.setProperty(GROUP_ID_CONFIG, "kibon-anmeldungbestaetigung-" + groupId);
		props.setProperty(AUTO_OFFSET_RESET_CONFIG, "earliest");
		props.setProperty(ENABLE_AUTO_COMMIT_CONFIG, "false");
		props.setProperty(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		props.setProperty(VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());
		props.setProperty(SCHEMA_REGISTRY_URL_CONFIG, ebeguConfiguration.getSchemaRegistryURL());
		props.setProperty(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, "true");

		consumer = new KafkaConsumer<>(props);
		consumer.subscribe(Collections.singletonList("AnmeldungBestaetigungEvents"));

	}

	@Schedule(info = "consume kafka events", second = "*/12", minute = "*", hour = "*", persistent = false)
	public void runAnmeldungBestaetigungConsumer() {
		try {
			if (consumer == null) {
				startKafkaAnmeldungBestaetigungConsumer();
				return;
			}

			ConsumerRecords<String, TagesschuleBestaetigungEventDTO> consumerRecordes =
				consumer.poll(Duration.ofMillis(5000));
			consumerRecordes.forEach(this::process);
			consumer.commitSync();
		} catch (Exception e) {
			LOG.error("There's a problem with the kafka Anmeldungbestaetigung Consumer", e);
		}
	}

	private void process(@Nonnull ConsumerRecord<String, TagesschuleBestaetigungEventDTO> record) {
		LOG.info("TagesschuleBestaetigungEvent received for Tagesschule Anmeldung with refnr {}", record.key());
		processor.process(record, eventHandler);
	}

	@PreDestroy
	public void close() {
		// Beim Herunterfahren des Servers ist der consumer scheinbar schon null
		if (consumer != null) {
			consumer.close();
		}
	}
}
