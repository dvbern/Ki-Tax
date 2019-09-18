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

package ch.dvbern.ebegu.services;

import java.util.Arrays;
import java.util.Collections;

import javax.annotation.Nonnull;
import javax.enterprise.event.Event;

import ch.dvbern.ebegu.entities.ExternalClient;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.enums.ExternalClientType;
import ch.dvbern.ebegu.outbox.ExportedEvent;
import ch.dvbern.ebegu.outbox.institutionclient.AbstractInstitutionClientEvent;
import ch.dvbern.ebegu.outbox.institutionclient.InstitutionClientAddedEvent;
import ch.dvbern.ebegu.outbox.institutionclient.InstitutionClientEventConverter;
import ch.dvbern.ebegu.outbox.institutionclient.InstitutionClientRemovedEvent;
import com.google.common.collect.Sets;
import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

@RunWith(EasyMockRunner.class)
public class InstitutionServiceBeanTest {

	private static final byte[] MOCK_BYTES = new byte[0];

	@TestSubject
	private final InstitutionServiceBean institutionService = new InstitutionServiceBean();

	@SuppressWarnings({ "unused", "InstanceVariableMayNotBeInitialized" })
	@Mock
	private InstitutionClientEventConverter institutionClientEventConverter;

	@SuppressWarnings({ "unused", "InstanceVariableMayNotBeInitialized" })
	@Mock
	private Event<ExportedEvent> exportedEvent;

	@Nonnull
	private final ExternalClient client1 = new ExternalClient("1", ExternalClientType.EXCHANGE_SERVICE_USER);
	@Nonnull
	private final ExternalClient client2 = new ExternalClient("2", ExternalClientType.EXCHANGE_SERVICE_USER);
	@Nonnull
	private final ExternalClient client3 = new ExternalClient("3", ExternalClientType.EXCHANGE_SERVICE_USER);

	@Test
	public void testSaveExternalClients_shouldRemoveAll() {
		Institution institution = createInstitution();

		expectRemoval(institution.getId(), client1);
		expectRemoval(institution.getId(), client2);

		EasyMock.replay(institutionClientEventConverter, exportedEvent);

		institutionService.saveExternalClients(institution, Collections.emptyList());

		assertThat(institution.getExternalClients(), is(empty()));

		EasyMock.verify(institutionClientEventConverter, exportedEvent);
	}

	@Test
	public void testSaveExternalClients_shouldAddClient3() {
		Institution institution = createInstitution();

		expectAddition(institution.getId(), client3);

		EasyMock.replay(institutionClientEventConverter, exportedEvent);

		institutionService.saveExternalClients(institution, Arrays.asList(client1, client2, client3));

		assertThat(institution.getExternalClients(), is(containsInAnyOrder(client1, client2, client3)));

		EasyMock.verify(institutionClientEventConverter, exportedEvent);
	}

	@Test
	public void testSaveExternalClients_shouldRemoveClient2AndAddClient3() {
		Institution institution = createInstitution();

		expectRemoval(institution.getId(), client2);
		expectAddition(institution.getId(), client3);

		EasyMock.replay(institutionClientEventConverter, exportedEvent);

		institutionService.saveExternalClients(institution, Arrays.asList(client1, client3));

		assertThat(institution.getExternalClients(), is(containsInAnyOrder(client1, client3)));

		EasyMock.verify(institutionClientEventConverter, exportedEvent);
	}

	@Test
	public void testSaveExternalClients_shouldNotFireEventsWhenNothingchanges() {
		Institution institution = createInstitution();

		EasyMock.replay(institutionClientEventConverter, exportedEvent);

		institutionService.saveExternalClients(institution, Arrays.asList(client1, client2));

		assertThat(institution.getExternalClients(), is(containsInAnyOrder(client1, client2)));

		EasyMock.verify(institutionClientEventConverter, exportedEvent);
	}

	@Nonnull
	private Institution createInstitution() {
		Institution institution = new Institution();

		institution.setExternalClients(Sets.newHashSet(client1, client2));

		return institution;
	}

	private void expectRemoval(@Nonnull String institutionId, @Nonnull ExternalClient client) {
		InstitutionClientRemovedEvent event = new InstitutionClientRemovedEvent(institutionId, MOCK_BYTES);

		EasyMock.expect(institutionClientEventConverter.clientRemovedEventOf(institutionId, client))
			.andReturn(event);

		expectEvent(event);
	}

	private void expectAddition(@Nonnull String institutionId, @Nonnull ExternalClient client) {
		InstitutionClientAddedEvent event = new InstitutionClientAddedEvent(institutionId, MOCK_BYTES);

		EasyMock.expect(institutionClientEventConverter.clientAddedEventOf(institutionId, client))
			.andReturn(event);

		expectEvent(event);
	}

	private void expectEvent(@Nonnull AbstractInstitutionClientEvent event) {
		exportedEvent.fire(event);

		EasyMock.expectLastCall().once();
	}
}
