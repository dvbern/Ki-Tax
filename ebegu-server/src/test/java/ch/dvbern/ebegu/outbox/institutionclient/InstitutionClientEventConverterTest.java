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

package ch.dvbern.ebegu.outbox.institutionclient;

import java.io.IOException;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.ExternalClient;
import ch.dvbern.ebegu.enums.ExternalClientType;
import ch.dvbern.ebegu.outbox.ExportedEvent;
import ch.dvbern.kibon.exchange.commons.util.ObjectMapperUtil;
import com.fasterxml.jackson.databind.JsonNode;
import org.hamcrest.Matcher;
import org.junit.Test;

import static com.spotify.hamcrest.jackson.JsonMatchers.jsonObject;
import static com.spotify.hamcrest.jackson.JsonMatchers.jsonText;
import static com.spotify.hamcrest.pojo.IsPojo.pojo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class InstitutionClientEventConverterTest {

	@Nonnull
	private final InstitutionClientEventConverter converter = new InstitutionClientEventConverter();

	private static final ExternalClient CLIENT = new ExternalClient("foo", ExternalClientType.EXCHANGE_SERVICE_USER);
	private static final String INSTITUTION_ID = "1";

	@Test
	public void testAddedEvent() throws Exception {
		InstitutionClientAddedEvent event = converter.clientAddedEventOf(INSTITUTION_ID, CLIENT);

		assertThat(event, exportedEventMatcher("ClientAdded"));

		verifyPayload(event);
	}

	@Test
	public void testRemovedEvent() throws Exception {
		InstitutionClientRemovedEvent event = converter.clientRemovedEventOf(INSTITUTION_ID, CLIENT);

		assertThat(event, exportedEventMatcher("ClientRemoved"));

		verifyPayload(event);
	}

	@Nonnull
	private Matcher<ExportedEvent> exportedEventMatcher(@Nonnull String expectedType) {
		return is(pojo(ExportedEvent.class)
			.where(ExportedEvent::getAggregateId, is(INSTITUTION_ID))
			.where(ExportedEvent::getAggregateType, is("InstitutionClient"))
			.where(ExportedEvent::getType, is(expectedType)));
	}

	private void verifyPayload(@Nonnull ExportedEvent event) throws IOException {
		JsonNode jsonNode = ObjectMapperUtil.MAPPER.readTree(event.getPayload());

		assertThat(jsonNode, is(jsonObject()
			.where("institutionId", is(jsonText(INSTITUTION_ID)))
			.where("clientName", is(jsonText(CLIENT.getClientName())))
			.where("clientType", is(jsonText((CLIENT.getType().name()))))
		));
	}
}
