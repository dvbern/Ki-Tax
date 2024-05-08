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

package ch.dvbern.ebegu.inbox.handler;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.InstitutionExternalClient;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.GesuchsperiodeStatus;
import ch.dvbern.ebegu.inbox.services.BetreuungEventHelper;
import ch.dvbern.ebegu.services.BetreuungMonitoringService;
import ch.dvbern.ebegu.services.BetreuungService;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import org.easymock.EasyMock;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.MockType;
import org.easymock.TestSubject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;

import static ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungTestUtil.REF_NUMMER;
import static ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungTestUtil.failed;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(EasyMockExtension.class)
public class AnmeldungAblehnenEventHandlerTest extends EasyMockSupport {
	public static final String CLIENT_NAME = "foo";
	public static final LocalDateTime EVENT_TIME = LocalDateTime.now();

	@TestSubject
	private final AnmeldungAblehnenEventHandler anmeldungAblehnenEventHandler =
		new AnmeldungAblehnenEventHandler();

	@SuppressWarnings("InstanceVariableMayNotBeInitialized")
	@Mock
	private BetreuungService betreuungService;

	@SuppressWarnings("InstanceVariableMayNotBeInitialized")
	@Mock
	private BetreuungEventHelper betreuungEventHelper;

	@SuppressWarnings("InstanceVariableMayNotBeInitialized")
	@Mock
	private GemeindeService gemeindeService;

	@SuppressWarnings("InstanceVariableMayNotBeInitialized")
	@Mock(MockType.NICE)
	private BetreuungMonitoringService betreuungMonitoringService;

	private AnmeldungTagesschule anmeldungTagesschule;
	private EventMonitor eventMonitor = null;
	private Mandant mandant;

	@BeforeEach
	void setUp() {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(false);
		Gesuchsperiode gesuchsperiode = betreuung.extractGesuchsperiode();
		mandant = gesuchsperiode.getMandant();
		anmeldungTagesschule = TestDataUtil.createAnmeldungTagesschuleWithModules(betreuung.getKind(), gesuchsperiode);
		anmeldungTagesschule.extractGesuch().setStatus(AntragStatus.IN_BEARBEITUNG_JA);
		eventMonitor = new EventMonitor(betreuungMonitoringService, EVENT_TIME, REF_NUMMER, CLIENT_NAME);
	}

	@ParameterizedTest
	@EnumSource(value = Betreuungsstatus.class,
		names = { "SCHULAMT_ANMELDUNG_AUSGELOEST", "SCHULAMT_FALSCHE_INSTITUTION" },
		mode = Mode.INCLUDE)
	void isAblehnungErblaubt(@Nonnull Betreuungsstatus status) {
		assertThat(anmeldungAblehnenEventHandler.isAblehnungErlaubtStatus(status), is(true));
	}

	@Nested
	class IgnoreEventTest {

		@Test
		void ignoreEventWhenNoAnmeldungFound() {
			expect(betreuungService.findAnmeldungenTagesschuleByReferenzNummer(eventMonitor.getRefnr()))
				.andReturn(Optional.empty());

			testIgnored("AnmeldungTagesschule nicht gefunden.");
		}

		@ParameterizedTest
		@EnumSource(value = GesuchsperiodeStatus.class, names = "AKTIV", mode = Mode.EXCLUDE)
		void ignoreEventWhenPeriodeNotAktiv(@Nonnull GesuchsperiodeStatus status) {
			anmeldungTagesschule.extractGesuchsperiode().setStatus(status);

			expect(betreuungService.findAnmeldungenTagesschuleByReferenzNummer(eventMonitor.getRefnr()))
				.andReturn(Optional.of(anmeldungTagesschule));

			testIgnored("Die Gesuchsperiode ist nicht aktiv.");
		}

		@Test
		void ignoreEventWhenAnmeldungMutiertAfterEventTimestamp() {

			LocalDateTime anmeldungMutiertTime = EVENT_TIME.plusSeconds(1);
			anmeldungTagesschule.setTimestampMutiert(anmeldungMutiertTime);

			expect(betreuungService.findAnmeldungenTagesschuleByReferenzNummer(eventMonitor.getRefnr()))
				.andReturn(Optional.of(anmeldungTagesschule));

			testIgnored(
				"Die AnmeldungTagesschule wurde verändert, nachdem das AblehnungEvent generiert wurde.");
		}

		@Test
		void ignoreEventWhenNoExternalClient() {

			expect(betreuungService.findAnmeldungenTagesschuleByReferenzNummer(eventMonitor.getRefnr()))
				.andReturn(Optional.of(anmeldungTagesschule));
			expect(betreuungEventHelper.getExternalClients(CLIENT_NAME, anmeldungTagesschule))
				.andReturn(new InstitutionExternalClients(null, Collections.emptyList()));
			expect(betreuungEventHelper.clientNotFoundFailure(CLIENT_NAME, anmeldungTagesschule))
				.andReturn(Processing.failure("Kein InstitutionExternalClient Namens ist der Institution zugewiesen"));

			testIgnored(
				"Kein InstitutionExternalClient Namens ist der Institution zugewiesen");
		}

		@Test
		void ignoreEventWhenClientGueltigkeitOutsidePeriode() {

			expect(betreuungService.findAnmeldungenTagesschuleByReferenzNummer(eventMonitor.getRefnr()))
				.andReturn(Optional.of(anmeldungTagesschule));
			mockClient(new DateRange(2022));

			testIgnored("Der Client hat innerhalb der Periode keine Berechtigung.");
		}

		@ParameterizedTest
		@EnumSource(value = Betreuungsstatus.class,
			names = { "SCHULAMT_ANMELDUNG_AUSGELOEST", "SCHULAMT_FALSCHE_INSTITUTION" },
			mode = Mode.EXCLUDE)
		void ignoreWhenInvalidBetreuungStatus(@Nonnull Betreuungsstatus status) {
			anmeldungTagesschule.setBetreuungsstatus(status);

			expect(betreuungService.findAnmeldungenTagesschuleByReferenzNummer(eventMonitor.getRefnr()))
				.andReturn(Optional.of(anmeldungTagesschule));
			mockClient(Constants.DEFAULT_GUELTIGKEIT);

			testIgnored(
				"Die AnmeldungTagesschule hat einen ungültigen Status: " + status);
		}

		private void testIgnored(@Nonnull String message) {
			replayAll();

			Processing result = anmeldungAblehnenEventHandler.attemptProcessing(eventMonitor);
			assertThat(result, failed(message));
			verifyAll();
		}
	}

	@Nested
	class AnmeldungAblehnenTest {

		@Test
		void testAnmeldungAkzeptiert() {
			expect(betreuungService.anmeldungSchulamtAblehnen(anmeldungTagesschule))
				.andReturn(anmeldungTagesschule);

			expect(betreuungService.findAnmeldungenTagesschuleByReferenzNummer(eventMonitor.getRefnr()))
				.andReturn(Optional.of(anmeldungTagesschule));
			mockClient(Constants.DEFAULT_GUELTIGKEIT);
			replayAll();
			Processing result = anmeldungAblehnenEventHandler.attemptProcessing(eventMonitor);
			assertThat(result.isProcessingSuccess(), is(true));
			verifyAll();
		}
	}

	private void mockClient(@Nonnull DateRange clientGueltigkeit) {
		InstitutionExternalClient institutionExternalClient = mock(InstitutionExternalClient.class);

		expect(betreuungEventHelper.getExternalClients(eq(CLIENT_NAME), EasyMock.<AnmeldungTagesschule>anyObject()))
			.andReturn(new InstitutionExternalClients(institutionExternalClient, Collections.emptyList()));

		expect(institutionExternalClient.getGueltigkeit())
			.andReturn(clientGueltigkeit);
	}

}
