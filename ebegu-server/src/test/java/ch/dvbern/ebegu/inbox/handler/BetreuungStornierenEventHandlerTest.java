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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungsmitteilung;
import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.ErweiterteBetreuung;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.InstitutionExternalClient;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.GesuchsperiodeStatus;
import ch.dvbern.ebegu.enums.MitteilungStatus;
import ch.dvbern.ebegu.enums.MitteilungTeilnehmerTyp;
import ch.dvbern.ebegu.inbox.services.BetreuungEventHelper;
import ch.dvbern.ebegu.services.BetreuungMonitoringService;
import ch.dvbern.ebegu.services.BetreuungService;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.services.MitteilungService;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.testfaelle.Testfall01_WaeltiDagmar;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.ZeitabschnittDTO;
import org.easymock.Capture;
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

import static ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungTestUtil.failed;
import static ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungTestUtil.getSingleContainer;
import static com.spotify.hamcrest.pojo.IsPojo.pojo;
import static java.util.Objects.requireNonNull;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(EasyMockExtension.class)
public class BetreuungStornierenEventHandlerTest extends EasyMockSupport {

	public static final String CLIENT_NAME = "foo";
	public static final LocalDateTime EVENT_TIME = LocalDateTime.now();

	@TestSubject
	private final BetreuungStornierenEventHandler handler = new BetreuungStornierenEventHandler();

	@SuppressWarnings("InstanceVariableMayNotBeInitialized")
	@Mock
	private BetreuungService betreuungService;

	@SuppressWarnings("InstanceVariableMayNotBeInitialized")
	@Mock
	private MitteilungService mitteilungService;

	@SuppressWarnings("InstanceVariableMayNotBeInitialized")
	@Mock
	private GemeindeService gemeindeService;

	@SuppressWarnings("InstanceVariableMayNotBeInitialized")
	@Mock
	private BetreuungEventHelper betreuungEventHelper;

	@SuppressWarnings("InstanceVariableMayNotBeInitialized")
	@Mock(MockType.NICE)
	private BetreuungMonitoringService betreuungMonitoringService;

	private Gesuch gesuch_1GS = null;
	private Gesuchsperiode gesuchsperiode = null;
	private Gemeinde gemeinde = null;
	private String refNummer = "20.007305.002.1.3";
	private EventMonitor eventMonitor = null;

	@BeforeEach
	void setUp() {
		gesuchsperiode = TestDataUtil.createGesuchsperiodeXXYY(2020, 2021);
		gemeinde = TestDataUtil.createGemeindeParis();
		List<InstitutionStammdaten> institutionStammdatenList = new ArrayList<>();
		institutionStammdatenList.add(TestDataUtil.createInstitutionStammdatenKitaWeissenstein());
		institutionStammdatenList.add(TestDataUtil.createInstitutionStammdatenKitaBruennen());
		Testfall01_WaeltiDagmar testfall_1GS =
			new Testfall01_WaeltiDagmar(gesuchsperiode, institutionStammdatenList, false, gemeinde);
		testfall_1GS.createFall();
		testfall_1GS.createGesuch(LocalDate.of(2016, Month.DECEMBER, 12));
		gesuch_1GS = testfall_1GS.fillInGesuch();
		eventMonitor = new EventMonitor(betreuungMonitoringService, EVENT_TIME, refNummer, CLIENT_NAME);
	}

	@ParameterizedTest
	@EnumSource(value = Betreuungsstatus.class,
		names = { "VERFUEGT", "BESTAETIGT", "GESCHLOSSEN_OHNE_VERFUEGUNG" },
		mode = Mode.INCLUDE)
	void isMutationsMitteilungStatus(@Nonnull Betreuungsstatus status) {
		assertThat(handler.isMutationsMitteilungStatus(status), is(true));
	}

	@Nested
	class IgnoreEventTest {

		@Test
		void ignoreEventWhenNoBetreuungFound() {
			expect(betreuungService.findBetreuungByBGNummer(refNummer, false))
				.andReturn(Optional.empty());

			testIgnored("Betreuung nicht gefunden.");
		}

		@ParameterizedTest
		@EnumSource(value = GesuchsperiodeStatus.class, names = "AKTIV", mode = Mode.EXCLUDE)
		void ignoreEventWhenPeriodeNotAktiv(@Nonnull GesuchsperiodeStatus status) {
			Betreuung betreuung = betreuungWithSingleContainer();
			betreuung.extractGesuchsperiode().setStatus(status);

			expect(betreuungService.findBetreuungByBGNummer(refNummer, false))
				.andReturn(Optional.of(betreuung));

			testIgnored("Die Gesuchsperiode ist nicht aktiv.");
		}

		@Test
		void ignoreEventWhenBetreuungMutiertAfterEventTimestamp() {
			Betreuung betreuung = betreuungWithSingleContainer();

			LocalDateTime eventTime = LocalDateTime.of(2020, 12, 1, 10, 1);
			LocalDateTime betreuungMutiertTime = eventTime.plusSeconds(1);
			betreuung.setTimestampMutiert(betreuungMutiertTime);

			expect(betreuungService.findBetreuungByBGNummer(refNummer, false))
				.andReturn(Optional.of(betreuung));

			replayAll();

			EventMonitor monitor = new EventMonitor(betreuungMonitoringService, eventTime, refNummer, CLIENT_NAME);

			Processing result = handler.attemptProcessing(monitor);
			assertThat(result, failed("Die Betreuung wurde verändert, nachdem das BetreuungEvent generiert wurde."));
			verifyAll();
		}

		@Test
		void ignoreEventWhenNoExternalClient() {
			Betreuung betreuung = betreuungWithSingleContainer();

			expect(betreuungService.findBetreuungByBGNummer(refNummer, false))
				.andReturn(Optional.of(betreuung));
			expect(betreuungEventHelper.getExternalClient(CLIENT_NAME, betreuung))
				.andReturn(Optional.empty());
			expect(betreuungEventHelper.clientNotFoundFailure(CLIENT_NAME, betreuung))
				.andReturn(Processing.failure("Kein InstitutionExternalClient Namens ist der Institution zugewiesen"));

			replayAll();

			Processing result = handler.attemptProcessing(eventMonitor);
			assertThat(
				result,
				failed(stringContainsInOrder(
					"Kein InstitutionExternalClient Namens",
					"ist der Institution",
					"zugewiesen"))
			);
			verifyAll();
		}

		@Test
		void ignoreEventWhenClientGueltigkeitOutsidePeriode() {
			Betreuung betreuung = betreuungWithSingleContainer();

			expect(betreuungService.findBetreuungByBGNummer(refNummer, false))
				.andReturn(Optional.of(betreuung));
			mockClient(new DateRange(2022));

			testIgnored("Der Client hat innerhalb der Periode keinen Berechtigung.");
		}

		@ParameterizedTest
		@EnumSource(value = Betreuungsstatus.class,
			names = { "WARTEN", "BESTAETIGT", "VERFUEGT", "GESCHLOSSEN_OHNE_VERFUEGUNG" },
			mode = Mode.EXCLUDE)
		void ignoreWhenInvalidBetreuungStatus(@Nonnull Betreuungsstatus status) {
			Betreuung betreuung = betreuungWithSingleContainer();
			betreuung.setBetreuungsstatus(status);

			expect(betreuungService.findBetreuungByBGNummer(refNummer, false))
				.andReturn(Optional.of(betreuung));
			mockClient(Constants.DEFAULT_GUELTIGKEIT);

			replayAll();

			Processing result = handler.attemptProcessing(eventMonitor);
			assertThat(
				result,
				failed(stringContainsInOrder(
					"Die Betreuung befindet sich in einen Status in dem eine Stornierung nicht erlaubt ist."))
			);
			verifyAll();
		}

		private void testIgnored(@Nonnull String message) {
			replayAll();

			Processing result = handler.attemptProcessing(eventMonitor);
			assertThat(result, failed(message));
			verifyAll();
		}
	}

	@Nested
	class BetreuungStornierenTest {
		private ZeitabschnittDTO zeitabschnittDTO = null;
		private Betreuung betreuung = null;
		private Betreuungspensum betreuungspensum = null;
		private DateRange clientGueltigkeit = Constants.DEFAULT_GUELTIGKEIT;
		private boolean withMahlzeitenEnabled = true;

		/**
		 * The default setup yields an Mutationsmeldung
		 */
		@BeforeEach
		void setUp() {
			betreuung = betreuungWithSingleContainer();
			betreuung.setBetreuungsstatus(Betreuungsstatus.VERFUEGT);
			betreuungspensum = getSingleContainer(betreuung);
			// set a pensum different of default zeitabschnitt DTO, otherwiese pensen will be merged, making validation
			// of gueltigkeiten harder
			betreuungspensum.setPensum(BigDecimal.valueOf(50));
			ErweiterteBetreuung erweiterteBetreuung =
				requireNonNull(betreuung.getErweiterteBetreuungContainer().getErweiterteBetreuungJA());
			// the default test data already sets TRUE, but the actual default is NULL...
			erweiterteBetreuung.setBetreuungInGemeinde(null);

			clientGueltigkeit = Constants.DEFAULT_GUELTIGKEIT;
		}

		@Test
		void createsNewBetreuungstornierenmeldungWhenNoneExists() {
			Capture<Betreuungsmitteilung> capture = expectNewMitteilung();
			expectMutationsmeldung();
			testProcessingSuccess();

			Dossier dossier = gesuch_1GS.getDossier();

			assertThat(capture.getValue(), pojo(Betreuungsmitteilung.class)
				.where(Betreuungsmitteilung::getDossier, sameInstance(dossier))
				.where(Betreuungsmitteilung::getSenderTyp, is(MitteilungTeilnehmerTyp.INSTITUTION))
				.where(Betreuungsmitteilung::getSender, notNullValue())
				.where(Betreuungsmitteilung::getEmpfaengerTyp, is(MitteilungTeilnehmerTyp.JUGENDAMT))
				.where(Betreuungsmitteilung::getEmpfaenger, sameInstance(dossier.getFall().getBesitzer()))
				.where(Betreuungsmitteilung::getMitteilungStatus, is(MitteilungStatus.NEU))
				.where(Betreuungsmitteilung::getSubject, is("Mutationstornierungmeldung"))
				.where(Betreuungsmitteilung::isBetreuungStornieren, is(true))
				.where(Betreuungsmitteilung::getBetreuung, sameInstance(betreuung))
			);
		}

		@Test
		void createsDefaultMessage() {
			Capture<Betreuungsmitteilung> capture = expectNewMitteilung();

			expectMutationsmeldung();

			testProcessingSuccess();

			// ignoring the dates, because their formatting is not platform independent
			assertThat(capture.getValue().getMessage(), stringContainsInOrder(
				"Die Betreuung ", " wird storniert"));
		}

		@Test
		void betreuungDirektStornieren() {
			betreuung.setVorgaengerId("SIMULATED-MUTATION");
			betreuung.setBetreuungsstatus(Betreuungsstatus.WARTEN);

			expectDirektStornierung();

			testProcessingSuccess();

			assertEquals(betreuung.getBetreuungsstatus(), Betreuungsstatus.STORNIERT);
		}

		private void testProcessingSuccess() {

			replayAll();

			Processing result = handler.attemptProcessing(eventMonitor);
			assertThat(result.isProcessingSuccess(), is(true));
			verifyAll();
		}

		private void expectDirektStornierung() {
			expect(betreuungService.findBetreuungByBGNummer(refNummer, false))
				.andReturn(Optional.of(betreuung));

			mockClient(clientGueltigkeit);

			expect(betreuungService.saveBetreuung(betreuung, false, CLIENT_NAME))
				.andReturn(betreuung);
		}

		private void expectMutationsmeldung() {
			expect(betreuungService.findBetreuungByBGNummer(refNummer, false))
				.andReturn(Optional.of(betreuung));

			mockClient(clientGueltigkeit);

			expect(betreuungEventHelper.getMutationsmeldungBenutzer()).andReturn(new Benutzer());

			expect(gemeindeService.getGemeindeStammdatenByGemeindeId(gemeinde.getId()))
				.andReturn(Optional.of(TestDataUtil.createGemeindeStammdaten(gemeinde)));
		}

		@Nonnull
		private Capture<Betreuungsmitteilung> expectNewMitteilung() {
			Capture<Betreuungsmitteilung> captured = EasyMock.newCapture();
			//noinspection ConstantConditions
			mitteilungService.replaceBetreungsmitteilungen(EasyMock.capture(captured));
			expectLastCall();

			return captured;
		}
	}

	private void mockClient(@Nonnull DateRange clientGueltigkeit) {
		InstitutionExternalClient institutionExternalClient = mock(InstitutionExternalClient.class);

		expect(betreuungEventHelper.getExternalClient(eq(CLIENT_NAME), anyObject()))
			.andReturn(Optional.of(institutionExternalClient));

		expect(institutionExternalClient.getGueltigkeit())
			.andReturn(clientGueltigkeit);
	}

	@Nonnull
	private Betreuung betreuungWithSingleContainer() {
		return PlatzbestaetigungTestUtil.betreuungWithSingleContainer(gesuch_1GS);
	}
}

