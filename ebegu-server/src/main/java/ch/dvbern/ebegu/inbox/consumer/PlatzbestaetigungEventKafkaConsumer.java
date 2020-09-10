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

package ch.dvbern.ebegu.inbox.consumer;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.security.RunAs;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.enums.UserRoleName;
import ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungEventHandler;
import ch.dvbern.ebegu.kafka.MessageProcessor;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.BetreuungEventDTO;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
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
import static org.apache.kafka.clients.consumer.ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG;

@Startup
@Singleton
@RunAs(UserRoleName.SUPER_ADMIN)
public class PlatzbestaetigungEventKafkaConsumer {

	private static final Logger LOG = LoggerFactory.getLogger(PlatzbestaetigungEventKafkaConsumer.class);

	@Inject
	private EbeguConfiguration ebeguConfiguration;

	@Inject
	private PlatzbestaetigungEventHandler eventHandler;

	@Inject
	MessageProcessor processor;

	private Consumer<String, BetreuungEventDTO>  consumer;

	@PostConstruct
	public void startKafkaPlatzbestaetigungConsumer(){
		if (!ebeguConfiguration.getKafkaURL().isPresent()) {
			LOG.debug("Kafka URL not set, not consuming events.");
			return;
		}
		Properties props = new Properties();
		props.setProperty(BOOTSTRAP_SERVERS_CONFIG, ebeguConfiguration.getKafkaURL().get());
		props.setProperty(GROUP_ID_CONFIG,
			"kibon-platzbestaetigung-" + ebeguConfiguration.getKafkaPlatzbestaetigungGroupId());
		props.setProperty(AUTO_OFFSET_RESET_CONFIG, "earliest");
		props.setProperty(ENABLE_AUTO_COMMIT_CONFIG, "true");
		props.setProperty(AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
		props.setProperty(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		props.setProperty(VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());
		props.setProperty(SCHEMA_REGISTRY_URL_CONFIG, ebeguConfiguration.getSchemaRegistryURL());
		props.setProperty(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, "true");

	/*	KafkaConsumer<String, GenericRecord> consumer = new KafkaConsumer<>(props);
		consumer.subscribe(Collections.singletonList("PlatzbestaetigungBetreuungEvents"));*/
		consumer = new KafkaConsumer(props);
		consumer.subscribe(Arrays.asList("PlatzbestaetigungBetreuungEvents"));

	}

	@Schedule(info = "consume kafka events",second="*/10", minute = "*", hour = "*", persistent = true)
	public void workKafkaData(){
		try {
			//while(true){
				ConsumerRecords<String, BetreuungEventDTO> consumerRecordes =
					consumer.poll(Duration.ofMillis(5000));
				for (ConsumerRecord<String, BetreuungEventDTO> record : consumerRecordes) {
					LOG.info("BetreuungEvent received for Betreuung with refnr " + record.key());
					processor.process(record, eventHandler);
				}
			//}
		} catch (Exception e){
			LOG.error("There's a problem with the kafka Platzbestaetigung Consumer", e);
		}
	}

	@PreDestroy
	public void close() {
		consumer.close();
	}
}
