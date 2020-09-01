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

package ch.dvbern.ebegu.inbox;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.config.EbeguConfiguration;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.CommitFailedException;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG;

@Stateless
public class InboxEventKafkaConsumer {

	private static final Logger LOG = LoggerFactory.getLogger(InboxEventKafkaConsumer.class);

	@Inject
	private EbeguConfiguration ebeguConfiguration;

	//@Schedule(info = "consume kafka events", minute = "*", hour = "*", persistent = true)
	public void consumeEvents() throws InterruptedException {
		if (!ebeguConfiguration.getKafkaURL().isPresent()) {
			LOG.debug("Kafka URL not set, not consuming events.");
			return;
		}
		Properties props = new Properties();
		props.setProperty(BOOTSTRAP_SERVERS_CONFIG, ebeguConfiguration.getKafkaURL().get());
		props.setProperty(GROUP_ID_CONFIG, "kibon-platzbestaetigung-group");
		props.setProperty(AUTO_OFFSET_RESET_CONFIG, "earliest");
		props.setProperty(ENABLE_AUTO_COMMIT_CONFIG, "false");
		//props.setProperty(ENABLE_AUTO_COMMIT_CONFIG, "true");
		//props.setProperty(AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
		props.setProperty(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		props.setProperty(VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());
		props.setProperty(SCHEMA_REGISTRY_URL_CONFIG, ebeguConfiguration.getSchemaRegistryURL());

	/*	KafkaConsumer<String, GenericRecord> consumer = new KafkaConsumer<>(props);
		consumer.subscribe(Collections.singletonList("PlatzbestaetigungBetreuungEvents"));*/
		final Consumer<String, GenericRecord> consumer = new KafkaConsumer(props);
		consumer.subscribe(Arrays.asList("PlatzbestaetigungBetreuungEvents"));
		//minimum 5 Sekunden sodass der Kafka Consumer sich richtig verbunden kann und seine Offset und Group id
		// verknuepfen kann
		ConsumerRecords<String, GenericRecord> consumerRecords =
			consumer.poll(Duration.ofMillis(1000));
		Thread.sleep(10000);
		//der zweite Anruf nur liefer Daten die erste startet das ganze registrierung usw...
		ConsumerRecords<String, GenericRecord> consumerRecordes =
			consumer.poll(Duration.ofMillis(5000));

		try {
			for (ConsumerRecord<String, GenericRecord> record : consumerRecordes) {
				//TODO work the item
				//First check if there is a betreuung corresponding with the key
				//then check if all the data are there
				//false yes automatic change state
				//false no just fill what can be filled
				LOG.info("Key: " + record.key() + ", Value:" + record.value());
				LOG.info("Partition:" + record.partition() + ",Offset:" + record.offset());
			}
			//der Offset muss sofort commited werden, als wenn die bearbeitung von die Daten mehr als 1 Sekunde
			// dauert, wird dann eine 'rebalancing' in kafka kommen und man abholt bei den naechsten Lauf wieder
			// dieslbe Daten aber sollte nur nach 5 minuten sein
			consumer.commitSync();
		} catch (CommitFailedException e) {
			LOG.error("commit failed", e);
		} finally {
			//consumer.unsubscribe();
			consumer.close();
		}
	}
}
