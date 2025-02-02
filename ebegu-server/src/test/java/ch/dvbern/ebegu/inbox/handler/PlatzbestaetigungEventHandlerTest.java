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

package ch.dvbern.ebegu.inbox.handler;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.betreuung.BetreuungEinstellungen;
import ch.dvbern.ebegu.betreuung.BetreuungEinstellungenService;
import ch.dvbern.ebegu.entities.AbstractMahlzeitenPensum;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungsmitteilung;
import ch.dvbern.ebegu.entities.BetreuungsmitteilungPensum;
import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.ErweiterteBetreuung;
import ch.dvbern.ebegu.entities.ExternalClient;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.InstitutionExternalClient;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.betreuung.Betreuungsstatus;
import ch.dvbern.ebegu.enums.GesuchsperiodeStatus;
import ch.dvbern.ebegu.enums.MitteilungStatus;
import ch.dvbern.ebegu.enums.MitteilungTeilnehmerTyp;
import ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungImportForm.ImportForm;
import ch.dvbern.ebegu.inbox.handler.pensum.PensumMapperFactory;
import ch.dvbern.ebegu.inbox.services.BetreuungEventHelper;
import ch.dvbern.ebegu.services.ApplicationPropertyService;
import ch.dvbern.ebegu.services.BetreuungMonitoringService;
import ch.dvbern.ebegu.services.BetreuungService;
import ch.dvbern.ebegu.services.EinstellungService;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.services.MitteilungService;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.test.util.TestDataInstitutionStammdatenBuilder;
import ch.dvbern.ebegu.testfaelle.Testfall01_WaeltiDagmar;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.BetreuungEventDTO;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.ZeitabschnittDTO;
import ch.dvbern.kibon.exchange.commons.types.Zeiteinheit;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.MockType;
import org.easymock.TestSubject;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.junit.jupiter.params.provider.MethodSource;

import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_ENABLED;
import static ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungImportForm.ImportForm.MUTATIONS_MITTEILUNG;
import static ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungTestUtil.REF_NUMMER;
import static ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungTestUtil.createBetreuungEventDTO;
import static ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungTestUtil.createBetreuungMitteilung;
import static ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungTestUtil.createBetreuungsmitteilungPensum;
import static ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungTestUtil.createZeitabschnittDTO;
import static ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungTestUtil.failed;
import static ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungTestUtil.getSingleContainer;
import static ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungTestUtil.ignored;
import static ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungTestUtil.initGesuch;
import static ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungTestUtil.matches;
import static ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungTestUtil.unitTestPensumMapper;
import static ch.dvbern.ebegu.inbox.handler.pensum.PensumMappingUtil.GO_LIVE;
import static com.spotify.hamcrest.pojo.IsPojo.pojo;
import static java.util.Objects.requireNonNull;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.newCapture;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.Matchers.stringContainsInOrder;

@ExtendWith(EasyMockExtension.class)
public class PlatzbestaetigungEventHandlerTest extends EasyMockSupport {

	public static final String CLIENT_NAME = "foo";
	public static final LocalDateTime EVENT_TIME = LocalDateTime.now();

	@TestSubject
	private final PlatzbestaetigungEventHandler handler = new PlatzbestaetigungEventHandler();

	@Mock
	private BetreuungService betreuungService;

	@Mock
	private BetreuungEinstellungenService betreuungEinstellungenService;

	@Mock
	private EinstellungService einstellungService;

	@Mock
	private MitteilungService mitteilungService;

	@Mock
	private GemeindeService gemeindeService;

	@Mock
	private BetreuungEventHelper betreuungEventHelper;

	@Mock
	private InstitutionExternalClient institutionExternalClient;

	@Mock
	private PensumMapperFactory pensumMapperFactory;

	@Mock(MockType.NICE)
	private BetreuungMonitoringService betreuungMonitoringService;

	@Mock(MockType.NICE)
	private ApplicationPropertyService applicationPropertyService;

	private Gesuch gesuch_1GS = null;
	private Gesuchsperiode gesuchsperiode = null;
	private Gemeinde gemeinde = null;
	private EventMonitor eventMonitor = null;
	private Mandant mandant;

	@BeforeEach
	void setUp() {
		gesuchsperiode = TestDataUtil.createGesuchsperiodeXXYY(2020, 2021);
		gemeinde = TestDataUtil.createGemeindeParis();
		mandant = requireNonNull(gemeinde.getMandant());
		Testfall01_WaeltiDagmar testfall_1GS =
			new Testfall01_WaeltiDagmar(
				gesuchsperiode,
				false,
				gemeinde,
				new TestDataInstitutionStammdatenBuilder(gesuchsperiode));
		testfall_1GS.createFall();
		testfall_1GS.createGesuch(LocalDate.of(2016, Month.DECEMBER, 12));
		gesuch_1GS = testfall_1GS.fillInGesuch();
		eventMonitor = new EventMonitor(betreuungMonitoringService, EVENT_TIME, "fake", CLIENT_NAME);
	}

	@Test
	void testIsSame() {
		List<Betreuung> betreuungen = gesuch_1GS.extractAllBetreuungen();
		Betreuungsmitteilung betreuungsmitteilung = createBetreuungMitteilung(
			createBetreuungsmitteilungPensum(new DateRange(
				gesuchsperiode.getGueltigkeit().getGueltigAb(),
				gesuchsperiode.getGueltigkeit().getGueltigBis().withDayOfYear(31)
			))
		);

		assertThat(handler.isSame(betreuungsmitteilung, betreuungen.get(0)), is(true));
		//then with different Betreuung
		assertThat(handler.isSame(betreuungsmitteilung, betreuungen.get(1)), is(false));
	}

	@Nested
	class FailEventTest {

		/**
		 * Should use "Stornieren" API instead
		 */
		@Test
		void failWhenNoZeitabschnitte() {
			BetreuungEventDTO dto = createBetreuungEventDTO(defaultZeitabschnittDTO());
			dto.setZeitabschnitte(Collections.emptyList());

			testFailed(dto, "Es wurden keine Zeitabschnitte übergeben.");
		}

		@Test
		void failWhenNoBetreuungFound() {
			BetreuungEventDTO dto = createBetreuungEventDTO(defaultZeitabschnittDTO());

			expect(betreuungService.findBetreuungByReferenzNummer(dto.getRefnr(), false))
				.andReturn(Optional.empty());

			testFailed(dto, "Betreuung nicht gefunden.");
		}

		@ParameterizedTest
		@EnumSource(value = GesuchsperiodeStatus.class, names = "AKTIV", mode = Mode.EXCLUDE)
		void failWhenPeriodeNotAktiv(@Nonnull GesuchsperiodeStatus status) {
			BetreuungEventDTO dto = createBetreuungEventDTO(defaultZeitabschnittDTO());

			Betreuung betreuung = betreuungWithSingleContainer();
			betreuung.extractGesuchsperiode().setStatus(status);

			expectBetreuungFound(betreuung);

			testFailed(dto, "Die Gesuchsperiode ist nicht aktiv.");
		}

		@Test
		void failWhenBetreuungMutiertAfterEventTimestamp() {
			BetreuungEventDTO dto = createBetreuungEventDTO(defaultZeitabschnittDTO());
			Betreuung betreuung = betreuungWithSingleContainer();

			LocalDateTime eventTime = LocalDateTime.of(2020, 12, 1, 10, 1);
			LocalDateTime betreuungMutiertTime = eventTime.plusSeconds(1);
			betreuung.setTimestampMutiert(betreuungMutiertTime);
			String refnr = dto.getRefnr();

			expectBetreuungFound(betreuung);

			replayAll();

			EventMonitor monitor = new EventMonitor(betreuungMonitoringService, eventTime, refnr, CLIENT_NAME);

			Processing result = handler.attemptProcessing(monitor, dto);
			assertThat(result, failed("Die Betreuung wurde verändert, nachdem das BetreuungEvent generiert wurde."));
			verifyAll();
		}

		@Test
		void failWhenInvalidPensumInDays() {
			ZeitabschnittDTO zeitabschnittDTO = defaultZeitabschnittDTO();
			zeitabschnittDTO.setPensumUnit(Zeiteinheit.DAYS);
			BetreuungEventDTO dto = createBetreuungEventDTO(zeitabschnittDTO);
			Betreuung betreuung = betreuungWithSingleContainer();
			betreuung.getInstitutionStammdaten().setBetreuungsangebotTyp(BetreuungsangebotTyp.TAGESFAMILIEN);

			expectBetreuungFound(betreuung);

			testFailed(dto, "Eine Pensum in DAYS kann nur für ein Angebot in einer Kita angegeben werden.");
		}

		@Test
		void failWhenNoExternalClient() {
			BetreuungEventDTO dto = createBetreuungEventDTO(defaultZeitabschnittDTO());
			Betreuung betreuung = betreuungWithSingleContainer();

			expectBetreuungFound(betreuung);
			expect(betreuungEventHelper.getExternalClients(CLIENT_NAME, betreuung))
				.andReturn(new InstitutionExternalClients());
			expect(betreuungEventHelper.clientNotFoundFailure(CLIENT_NAME, betreuung))
				.andReturn(Processing.failure("Kein InstitutionExternalClient Namens ist der Institution zugewiesen"));

			replayAll();

			Processing result = handler.attemptProcessing(eventMonitor, dto);
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
		void failWhenClientGueltigkeitOutsidePeriode() {
			BetreuungEventDTO dto = createBetreuungEventDTO(defaultZeitabschnittDTO());
			Betreuung betreuung = betreuungWithSingleContainer();

			expectBetreuungFound(betreuung);
			mockClients(new DateRange(2022));

			testFailed(dto, "Der Client hat innerhalb der Periode keine Berechtigung.");
		}

		@Test
		void failWhenZeitabschnitteOverlap() {
			BetreuungEventDTO dto = createBetreuungEventDTO(
				createZeitabschnittDTO(LocalDate.of(2021, 1, 1), LocalDate.of(2021, 1, 10)),
				createZeitabschnittDTO(LocalDate.of(2021, 1, 10), LocalDate.of(2021, 1, 11))
			);
			Betreuung betreuung = betreuungWithSingleContainer();

			expectBetreuungFound(betreuung);
			mockClients(Constants.DEFAULT_GUELTIGKEIT);

			testFailed(dto, "Zeitabschnitte dürfen nicht überlappen.");
		}

		@Test
		void failWhenNoInstitutionGueltigkeitAndClientGueltigkeitOverlap() {
			LocalDate gueltigAb = LocalDate.of(2021, 5, 1);
			DateRange institutionGueltigkeit = new DateRange(gueltigAb, Constants.END_OF_TIME);

			BetreuungEventDTO dto = createBetreuungEventDTO(defaultZeitabschnittDTO());
			Betreuung betreuung = betreuungWithSingleContainer();
			betreuung.getInstitutionStammdaten().setGueltigkeit(institutionGueltigkeit);

			expectBetreuungFound(betreuung);
			mockClients(new DateRange(Constants.START_OF_TIME, gueltigAb.minusDays(1)));

			testFailed(dto, "Die Institution Gültigkeit überlappt nicht mit der Client Gültigkeit.");
		}

		@Test
		void failWhenNoZeitabschnittInInstitutionGueltigkeit() {
			LocalDate gueltigAb = LocalDate.of(2021, 5, 1);
			DateRange dtoGueltigkeit =
				new DateRange(gesuchsperiode.getGueltigkeit().getGueltigAb(), gueltigAb.minusDays(1));
			DateRange institutionGueltigkeit = new DateRange(gueltigAb, Constants.END_OF_TIME);

			BetreuungEventDTO dto = createBetreuungEventDTO(createZeitabschnittDTO(dtoGueltigkeit));
			Betreuung betreuung = betreuungWithSingleContainer();
			betreuung.getInstitutionStammdaten().setGueltigkeit(institutionGueltigkeit);

			expectBetreuungFound(betreuung);
			mockClients(Constants.DEFAULT_GUELTIGKEIT);

			testFailed(
				dto,
				"Kein Zeitabschnitt liegt innerhalb Client Gültigkeit & Periode & Institution Gültigkeit.");
		}

		@Test
		void failWhenNoZeitabschnittInClientGueltigkeit() {
			LocalDate gesuchsperiodeAb = gesuchsperiode.getGueltigkeit().getGueltigAb();
			LocalDate zeitabschnittBis = gesuchsperiodeAb.plusMonths(8).minusDays(1);

			BetreuungEventDTO dto = createBetreuungEventDTO(
				createZeitabschnittDTO(gesuchsperiodeAb, zeitabschnittBis)
			);
			Betreuung betreuung = betreuungWithSingleContainer();

			expectBetreuungFound(betreuung);
			mockClients(new DateRange(zeitabschnittBis.plusDays(1), Constants.END_OF_TIME));

			testFailed(
				dto,
				"Kein Zeitabschnitt liegt innerhalb Client Gültigkeit & Periode & Institution Gültigkeit.");
		}

		@ParameterizedTest
		@EnumSource(value = Betreuungsstatus.class,
			names = { "WARTEN", "VERFUEGT", "BESTAETIGT", "GESCHLOSSEN_OHNE_VERFUEGUNG", "STORNIERT" },
			mode = Mode.EXCLUDE)
		void failWhenInvalidBetreuungStatus(@Nonnull Betreuungsstatus status) {
			BetreuungEventDTO dto = createBetreuungEventDTO(defaultZeitabschnittDTO());
			Betreuung betreuung = betreuungWithSingleContainer();
			betreuung.setBetreuungsstatus(status);

			expectBetreuungWithoutOffeneBetreuungsmitteilung(betreuung);
			expect(betreuungEinstellungenService.getEinstellungen(betreuung))
				.andReturn(BetreuungEinstellungen.builder().build());
			mockClients(Constants.DEFAULT_GUELTIGKEIT);

			testFailed(dto, "Platzbestätigung oder Mutation nicht möglich.");
		}

		private void testFailed(@Nonnull BetreuungEventDTO dto, @Nonnull String message) {
			replayAll();

			Processing result = handler.attemptProcessing(eventMonitor, dto);
			assertThat(result, failed(message));
			verifyAll();
		}
	}

	@SuppressWarnings("TestMethodWithoutAssertion")
	@Nested
	class PlatzbestaetigungTest {

		private BetreuungEventDTO dto = null;
		private Betreuung betreuung = null;
		private DateRange clientGueltigkeit = Constants.DEFAULT_GUELTIGKEIT;
		private boolean zusaetzlicherGutscheinGemeindeEnabled = true;

		/**
		 * The default setup yields an automatic Platzbestaetigung in the most demanding case (Gemeinde with
		 * Mahlzeiten)
		 */
		@BeforeEach
		void setUp() {
			dto = createBetreuungEventDTO(defaultZeitabschnittDTO());
			dto.setGemeindeBfsNr(gemeinde.getBfsNummer());
			dto.setGemeindeName(gemeinde.getName());
			dto.getZeitabschnitte().forEach(z -> {
				z.setTarifProHauptmahlzeiten(BigDecimal.ZERO);
				z.setTarifProNebenmahlzeiten(BigDecimal.ZERO);
			});
			betreuung = betreuungWithSingleContainer();
			betreuung.getBetreuungspensumContainers().clear();
			ErweiterteBetreuung erweiterteBetreuung =
				requireNonNull(betreuung.getErweiterteBetreuungContainer().getErweiterteBetreuungJA());
			// the default test data already sets TRUE, but the actual default is NULL...
			erweiterteBetreuung.setBetreuungInGemeinde(null);

			clientGueltigkeit = Constants.DEFAULT_GUELTIGKEIT;
		}

		@Test
		void automaticPlatzbestaetigung() {
			expectPlatzBestaetigung();

			testProcessingSuccess();
		}

		@ParameterizedTest
		@MethodSource("ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungEventHandlerTest#provideSprachfoederungTestData")
		void automaticPlatzbestaetigungSprachfoederungTest(
			@Nullable Boolean sprachfoerderung,
			@Nonnull LocalDate aktivierungDatum,
			boolean isBestaetigt
		) {
			dto.setSprachfoerderungBestaetigt(sprachfoerderung);
			expectPlatzBestaetigung();
			expectBetreuungWithoutOffeneBetreuungsmitteilung(betreuung);
			expect(betreuungEinstellungenService.getEinstellungen(betreuung))
				.andReturn(BetreuungEinstellungen.builder().build());
			expectGetSchnittstelleSprachfoerderungAktivAb(aktivierungDatum);
			mockClients(clientGueltigkeit);
			withZusaetzlicherGutschein(zusaetzlicherGutscheinGemeindeEnabled);
			expect(pensumMapperFactory.createForPlatzbestaetigung(anyObject()))
				.andReturn(unitTestPensumMapper());

			replayAll();
			Processing result = handler.attemptProcessing(eventMonitor, dto);
			ErweiterteBetreuung erweiterteBetreuung = betreuung.getErweiterteBetreuungContainer().getErweiterteBetreuungJA();
			assertThat(requireNonNull(erweiterteBetreuung).isSprachfoerderungBestaetigt(), is(isBestaetigt));
			assertThat(result.isProcessingSuccess(), is(true));
			verifyAll();
		}

		@ParameterizedTest
		@EnumSource(value = AntragStatus.class,
			names = { "IN_BEARBEITUNG_GS", "IN_BEARBEITUNG_SOZIALDIENST" },
			mode = Mode.INCLUDE)
		void updatesPlatzbestaetigungWhenNotYetFreigegeben(@Nonnull AntragStatus antragStatus) {
			betreuung.setBetreuungsstatus(Betreuungsstatus.BESTAETIGT);
			Gesuch gesuch = betreuung.extractGesuch();
			gesuch.setStatus(antragStatus);

			expectPlatzBestaetigung();

			testProcessingSuccess();
		}

		/**
		 * Confirmation of {@code ErweiterteBeduerfnisse} when claim and response match
		 */
		@ParameterizedTest
		@CsvSource({
			"true, true, true",
			"true, false, false",
			"false, true, false",
			"false, false, true"
		})
		void setErweiterteBeduerfnisseBestaetigt(
			boolean erweiterteBeduerfnisseClaimed,
			boolean erweiterteBeduerfnisseConfirmed,
			boolean confirmation) {

			ErweiterteBetreuung erweiterteBetreuung =
				requireNonNull(betreuung.getErweiterteBetreuungContainer().getErweiterteBetreuungJA());

			erweiterteBetreuung.setErweiterteBeduerfnisse(erweiterteBeduerfnisseClaimed);

			dto.setAusserordentlicherBetreuungsaufwand(erweiterteBeduerfnisseConfirmed);

			expectPlatzBestaetigung();

			testProcessingSuccess();

			assertThat(erweiterteBetreuung.isErweiterteBeduerfnisseBestaetigt(), is(confirmation));
		}

		@Test
		void allowPensumInHoursForKita() {
			dto.getZeitabschnitte().get(0).setPensumUnit(Zeiteinheit.HOURS);
			betreuung.getInstitutionStammdaten().setBetreuungsangebotTyp(BetreuungsangebotTyp.KITA);

			expectPlatzBestaetigung();

			testProcessingSuccess();
		}

		@Test
		void ignoreErweiterteBeduerfnisseWhenNotClaimed() {
			// removes the claim
			betreuung.getErweiterteBetreuungContainer().setErweiterteBetreuungJA(null);
			// to be ignored
			dto.setAusserordentlicherBetreuungsaufwand(true);
			// disable ZusaetzlicherGutscheinGeminde, because it automatically creates ErweiterteBetreuung
			zusaetzlicherGutscheinGemeindeEnabled = false;

			expectPlatzBestaetigung();

			testProcessingSuccessWithoutErweiterteBetreuung();

			assertThat(betreuung.getErweiterteBetreuungContainer().getErweiterteBetreuungJA(), nullValue());
		}

		@Test
		void ignoreGemeindeWhenNotRequired() {
			zusaetzlicherGutscheinGemeindeEnabled = false;

			betreuung.getErweiterteBetreuungContainer().setErweiterteBetreuungJA(null);

			expectPlatzBestaetigung();

			testProcessingSuccessWithoutErweiterteBetreuung();

			assertThat(betreuung.getErweiterteBetreuungContainer().getErweiterteBetreuungJA(), nullValue());
		}

		@Test
		void requrieHumanInteractionWhenGemeindeMissing() {
			dto.setGemeindeName(null);
			dto.setGemeindeBfsNr(null);

			expectHumanConfirmation();

			testProcessingSuccess();
		}

		@ParameterizedTest
		@CsvSource(value = {
			// match by name
			"Foo, 1, Foo, null, true",
			// match by name case insensitive
			"Foo, 1, foo, null, true",
			// match by bfs number
			"Foo, 1, null, 1, true",
			// match by name or bfs number (name match, bfs mismatch)
			"Foo, 1, Foo, 2, true",
			// match by name or bfs number (name mismatch, bfs match)
			"Foo, 1, Bar, 1, true",
			// mismatch by name
			"Foo, 1, Bar, null, false",
			// mismatch by bfs number
			"Foo, 1, null, 2, false",
			// mismatch both
			"Foo, 1, Bar, 2, false",
		}, nullValues = "null")
		void matchGemeindeByNameOrBfsNumber(
			@Nonnull String kiBonName,
			@Nonnull Long kiBonBfsNumber,
			@Nullable String dtoName,
			@Nullable Long dtoBfsNumber,
			boolean betreuungInGemeinde) {

			gemeinde.setName(kiBonName);
			gemeinde.setBfsNummer(kiBonBfsNumber);

			dto.setGemeindeName(dtoName);
			dto.setGemeindeBfsNr(dtoBfsNumber);

			expectPlatzBestaetigung();

			ErweiterteBetreuung erweiterteBetreuung =
				requireNonNull(betreuung.getErweiterteBetreuungContainer().getErweiterteBetreuungJA());
			// the default test data already sets TRUE, but the actual default is NULL...
			erweiterteBetreuung.setBetreuungInGemeinde(null);

			testProcessingSuccess();

			assertThat(erweiterteBetreuung.getBetreuungInGemeinde(), is(betreuungInGemeinde));
		}

		@Nested
		class ZeitabschnitteTest {

			/**
			 * The automatic confirmation must be disabled, when the client is not permittet to set data for the entire
			 * periode. Otherwise it is not possible to augment the data with Zeitabschnitte from another client.
			 */
			@Test
			void requireHumanConfirmationWhenClientGueltigkeitIsNotCoveringEntirePeriod_withSomeOtherClientInPeriode() {
				LocalDate periodeAb = gesuchsperiode.getGueltigkeit().getGueltigAb();
				clientGueltigkeit = new DateRange(periodeAb.plusDays(1), Constants.END_OF_TIME);
				InstitutionExternalClient client2 = mockClient(new DateRange(Constants.START_OF_TIME, periodeAb), "client2");

				expectHumanConfirmation();
				expectBetreuungWithoutOffeneBetreuungsmitteilung(betreuung);
				expect(betreuungEinstellungenService.getEinstellungen(betreuung))
					.andReturn(BetreuungEinstellungen.builder().build());
				expectGetSchnittstelleSprachfoerderungAktivAb(LocalDate.of(2023, 1, 1));
				mockClients(clientGueltigkeit, List.of(client2));
				expect(pensumMapperFactory.createForPlatzbestaetigung(anyObject()))
					.andReturn(unitTestPensumMapper());
				withZusaetzlicherGutschein(zusaetzlicherGutscheinGemeindeEnabled);

				replayAll();

				Processing result = handler.attemptProcessing(eventMonitor, dto);
				assertThat(result.isProcessingSuccess(), is(true));
				verifyAll();
			}

			/**
			 * The automatic confirmation must be enabled, when the client is not permittet to set data for the entire
			 * periode but there is no other client.
			 */
			@Test
			void automaticWhenClientGueltigkeitIsNotCoveringEntirePeriod_whenNoOtherClientInPeriode() {

				clientGueltigkeit =
					new DateRange(gesuchsperiode.getGueltigkeit().getGueltigAb().plusDays(1), Constants.END_OF_TIME);

				expectPlatzBestaetigung();
				testProcessingSuccess();
			}

			@Test
			void splitDtoZeitabschnitteWithClientGueltigkeit() {
				clientGueltigkeit = new DateRange(LocalDate.of(2020, 12, 31), LocalDate.of(2021, 5, 31));

				// ZeitabschnittDTO's must be distinct, otherwise they get merged together to one large Zeitabschnitt.
				// -> set distinct Betreuungskosten
				// see also test #mergesIdenticalZeitabschnitte()
				AtomicInteger betreuungsKosten = new AtomicInteger(1234);

				ZeitabschnittDTO beforeGueltigAb =
					createZeitabschnittDTO(LocalDate.of(2020, 11, 1), LocalDate.of(2020, 11, 30));
				beforeGueltigAb.setBetreuungskosten(BigDecimal.valueOf(betreuungsKosten.incrementAndGet()));

				ZeitabschnittDTO overlapGueltigAb =
					createZeitabschnittDTO(LocalDate.of(2020, 12, 1), LocalDate.of(2021, 1, 31));
				overlapGueltigAb.setBetreuungskosten(BigDecimal.valueOf(betreuungsKosten.incrementAndGet()));

				ZeitabschnittDTO containedInGueltigkeit =
					createZeitabschnittDTO(LocalDate.of(2021, 2, 1), LocalDate.of(2021, 3, 31));
				containedInGueltigkeit.setBetreuungskosten(BigDecimal.valueOf(betreuungsKosten.incrementAndGet()));

				ZeitabschnittDTO overlapGueltigBis =
					createZeitabschnittDTO(LocalDate.of(2021, 4, 1), LocalDate.of(2021, 5, 31));
				overlapGueltigBis.setBetreuungskosten(BigDecimal.valueOf(betreuungsKosten.incrementAndGet()));

				ZeitabschnittDTO afterGueltigBis =
					createZeitabschnittDTO(LocalDate.of(2021, 7, 1), LocalDate.of(2021, 7, 31));
				afterGueltigBis.setBetreuungskosten(BigDecimal.valueOf(betreuungsKosten.incrementAndGet()));

				dto.setZeitabschnitte(Arrays.asList(
					beforeGueltigAb,
					overlapGueltigAb,
					containedInGueltigkeit,
					overlapGueltigBis,
					afterGueltigBis));

				expectPlatzBestaetigung();
				testProcessingSuccess();

				assertThat(betreuung.getBetreuungspensumContainers(), contains(
					container(matches(overlapGueltigAb, LocalDate.of(2020, 12, 31), LocalDate.of(2021, 1, 31))),
					container(matches(containedInGueltigkeit)),
					container(matches(overlapGueltigBis, LocalDate.of(2021, 4, 1), LocalDate.of(2021, 5, 31)))
				));
			}

			@Test
			void mergesIdenticalZeitabschnitte() {
				clientGueltigkeit = new DateRange(LocalDate.of(2020, 12, 31), LocalDate.of(2021, 5, 31));

				ZeitabschnittDTO beforeGueltigAb =
					createZeitabschnittDTO(LocalDate.of(2020, 11, 1), LocalDate.of(2020, 11, 30));

				ZeitabschnittDTO overlapGueltigAb =
					createZeitabschnittDTO(LocalDate.of(2020, 12, 1), LocalDate.of(2021, 1, 31));

				ZeitabschnittDTO containedInGueltigkeit =
					createZeitabschnittDTO(LocalDate.of(2021, 2, 1), LocalDate.of(2021, 3, 31));

				ZeitabschnittDTO overlapGueltigBis =
					createZeitabschnittDTO(LocalDate.of(2021, 4, 1), LocalDate.of(2021, 5, 31));

				ZeitabschnittDTO afterGueltigBis =
					createZeitabschnittDTO(LocalDate.of(2021, 7, 1), LocalDate.of(2021, 7, 31));

				dto.setZeitabschnitte(Arrays.asList(
					beforeGueltigAb,
					overlapGueltigAb,
					containedInGueltigkeit,
					overlapGueltigBis,
					afterGueltigBis));

				expectPlatzBestaetigung();
				testProcessingSuccess();

				assertThat(betreuung.getBetreuungspensumContainers(), contains(
					container(matches(containedInGueltigkeit, LocalDate.of(2020, 12, 31), LocalDate.of(2021, 5, 31)))
				));
			}

			/**
			 * In case there are already BetreuungspensumContainers, split them when necessary at ClientGueltigkeit and
			 * fill new Zeitabschnitte from Client.
			 */
			@Test
			void updateBetreuungpensumContainersWithDtoZeitabschnitte() {
				BetreuungspensumContainer original = addCompleteContainer(gesuchsperiode.getGueltigkeit());

				clientGueltigkeit = new DateRange(LocalDate.of(2020, 12, 15), LocalDate.of(2021, 5, 31));

				// "von" is before client gueltigAb -> must be ignored
				// "bis" is before client gueltigBis -> there must be a gap from "bis" to "gueltigBis"
				ZeitabschnittDTO z = createZeitabschnittDTO(LocalDate.of(2020, 11, 1), LocalDate.of(2021, 4, 30));
				dto.setZeitabschnitte(Collections.singletonList(z));

				expectPlatzBestaetigung();
				testProcessingSuccess();

				assertThat(betreuung.getBetreuungspensumContainers(), contains(
					container(matches(original, LocalDate.of(2020, 8, 1), LocalDate.of(2020, 12, 14))),
					container(matches(z, LocalDate.of(2020, 12, 15), LocalDate.of(2021, 4, 30))),
					container(matches(original, LocalDate.of(2021, 6, 1), LocalDate.of(2021, 7, 31)))
				));
			}

			@Test
			void preservesIncompleteContainerOutsideClientGueltigkeit() {
				BetreuungspensumContainer incomplete = addIncompleteContainer(gesuchsperiode.getGueltigkeit());

				clientGueltigkeit = new DateRange(LocalDate.of(2020, 12, 15), LocalDate.of(2021, 5, 31));

				ZeitabschnittDTO z = createZeitabschnittDTO(clientGueltigkeit);
				dto.setZeitabschnitte(Collections.singletonList(z));

				expectPlatzBestaetigung();
				testProcessingSuccess();

				assertThat(betreuung.getBetreuungspensumContainers(), contains(
					container(matches(incomplete, LocalDate.of(2020, 8, 1), LocalDate.of(2020, 12, 14))),
					container(matches(z, clientGueltigkeit)),
					container(matches(incomplete, LocalDate.of(2021, 6, 1), LocalDate.of(2021, 7, 31)))
				));
			}

			@Test
			void keepsEndOfTimeOfOriginalZeitabschnitt() {
				BetreuungspensumContainer original =
					addCompleteContainer(LocalDate.of(2020, 8, 1), Constants.END_OF_TIME);

				clientGueltigkeit = new DateRange(LocalDate.of(2021, 1, 1), LocalDate.of(2021, 5, 1));

				ZeitabschnittDTO z = createZeitabschnittDTO(clientGueltigkeit);
				dto.setZeitabschnitte(Collections.singletonList(z));

				expectPlatzBestaetigung();
				testProcessingSuccess();

				assertThat(betreuung.getBetreuungspensumContainers(), contains(
					container(matches(original, LocalDate.of(2020, 8, 1), LocalDate.of(2020, 12, 31))),
					container(matches(z, clientGueltigkeit)),
					container(matches(original, LocalDate.of(2021, 5, 2), Constants.END_OF_TIME))
				));
			}

			@Test
			void doesNotGeneratedCompletelyOutOfPeriodZeitabschnitt() {
				addCompleteContainer(LocalDate.of(2020, 8, 1), Constants.END_OF_TIME);

				clientGueltigkeit = new DateRange(LocalDate.of(2020, 8, 1), Constants.END_OF_TIME);

				DateRange gueltigkeit = betreuung.extractGesuchsperiode().getGueltigkeit();
				ZeitabschnittDTO z = createZeitabschnittDTO(gueltigkeit);
				dto.setZeitabschnitte(Collections.singletonList(z));

				expectPlatzBestaetigung();
				testProcessingSuccess();

				assertThat(betreuung.getBetreuungspensumContainers(), contains(
					container(matches(z, gueltigkeit))
				));
			}

			@Test
			void removesZeitabschnitteOutsidePeriode() {
				addCompleteContainer(LocalDate.of(2020, 8, 1), LocalDate.of(2021, 7, 31));
				addCompleteContainer(LocalDate.of(2021, 9, 1), Constants.END_OF_TIME);

				DateRange gueltigkeit = new DateRange(LocalDate.of(2021, 1, 1), LocalDate.of(2021, 7, 31));
				ZeitabschnittDTO z = createZeitabschnittDTO(gueltigkeit);
				dto.setZeitabschnitte(Collections.singletonList(z));

				expectPlatzBestaetigung();
				testProcessingSuccess();

				assertThat(betreuung.getBetreuungspensumContainers(), contains(
					container(matches(z, gueltigkeit))
				));
			}

			/**
			 * regression test: original implementation did set endOf 1st container to 2020-01-01 and created an
			 * overlap at that day.
			 */
			@Test
			void handlesOriginalBisEqualClientAb() {
				LocalDate edgeCase = LocalDate.of(2021, 1, 1);
				BetreuungspensumContainer original = addCompleteContainer(LocalDate.of(2020, 8, 1), edgeCase);

				clientGueltigkeit = new DateRange(edgeCase, LocalDate.of(2021, 5, 1));

				ZeitabschnittDTO z = createZeitabschnittDTO(clientGueltigkeit);
				dto.setZeitabschnitte(Collections.singletonList(z));

				expectPlatzBestaetigung();
				testProcessingSuccess();

				assertThat(betreuung.getBetreuungspensumContainers(), contains(
					container(matches(original, LocalDate.of(2020, 8, 1), LocalDate.of(2020, 12, 31))),
					container(matches(z, edgeCase, LocalDate.of(2021, 5, 1)))
				));
			}

			@Nonnull
			private Matcher<BetreuungspensumContainer> container(@Nonnull Matcher<AbstractMahlzeitenPensum> matcher) {
				return pojo(BetreuungspensumContainer.class)
					.where(BetreuungspensumContainer::getBetreuungspensumJA, matcher);
			}

			@Nonnull
			@CanIgnoreReturnValue
			private BetreuungspensumContainer addIncompleteContainer(@Nonnull DateRange range) {
				BetreuungspensumContainer container = addCompleteContainer(range);
				container.getBetreuungspensumJA().setVollstaendig(false);

				return container;
			}

			@Nonnull
			@CanIgnoreReturnValue
			private BetreuungspensumContainer addCompleteContainer(@Nonnull DateRange range) {
				return addCompleteContainer(range.getGueltigAb(), range.getGueltigBis());
			}

			@Nonnull
			@CanIgnoreReturnValue
			private BetreuungspensumContainer addCompleteContainer(@Nonnull LocalDate von, @Nonnull LocalDate bis) {
				BetreuungspensumContainer container = new BetreuungspensumContainer();
				Betreuungspensum pensum = new Betreuungspensum(new DateRange(von, bis));
				container.setBetreuungspensumJA(pensum);
				pensum.setVollstaendig(true);

				container.setBetreuung(betreuung);
				betreuung.getBetreuungspensumContainers().add(container);

				return container;
			}
		}

		private void testProcessingSuccess() {
			this.testProcessingSuccess(true);
		}

		private void testProcessingSuccessWithoutErweiterteBetreuung() {
			this.testProcessingSuccess(false);
		}

		private void testProcessingSuccess(boolean erweitereBetreuung) {
			expectBetreuungWithoutOffeneBetreuungsmitteilung(betreuung);
			expect(betreuungEinstellungenService.getEinstellungen(betreuung))
				.andReturn(BetreuungEinstellungen.builder().build());
			if (erweitereBetreuung) {
				expectGetSchnittstelleSprachfoerderungAktivAb(LocalDate.of(2023, 1, 1));
			}
			mockClients(clientGueltigkeit);
			withZusaetzlicherGutschein(zusaetzlicherGutscheinGemeindeEnabled);

			expect(pensumMapperFactory.createForPlatzbestaetigung(anyObject()))
				.andReturn(unitTestPensumMapper());

			replayAll();

			Processing result = handler.attemptProcessing(eventMonitor, dto);
			assertThat(result.isProcessingSuccess(), is(true));
			verifyAll();
		}

		private void expectGetSchnittstelleSprachfoerderungAktivAb(LocalDate aktivierungDatum) {
			expect(applicationPropertyService.getSchnittstelleSprachfoerderungAktivAb(mandant))
				.andReturn(aktivierungDatum);
		}

		private void withZusaetzlicherGutschein(boolean enabled) {
			expect(einstellungService
				.isEnabled(GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_ENABLED, betreuung))
				.andReturn(enabled);
		}

		private void expectPlatzBestaetigung() {
			expect(betreuungService.betreuungPlatzBestaetigen(betreuung, CLIENT_NAME))
				.andReturn(betreuung);
		}

		private void expectHumanConfirmation() {
			expect(betreuungService.saveBetreuung(betreuung, false, CLIENT_NAME))
				.andReturn(betreuung);
		}
	}

	@Nested
	class MutationsmeldungTest {

		private BetreuungEventDTO dto = null;
		private ZeitabschnittDTO zeitabschnittDTO = null;
		private Betreuung betreuung = null;
		private Betreuungspensum betreuungspensum = null;
		private DateRange clientGueltigkeit = Constants.DEFAULT_GUELTIGKEIT;

		/**
		 * The default setup yields an Mutationsmeldung
		 */
		@BeforeEach
		void setUp() {
			zeitabschnittDTO = createZeitabschnittDTO(GO_LIVE, gesuchsperiode.getGueltigkeit().getGueltigBis());

			zeitabschnittDTO.setTarifProHauptmahlzeiten(BigDecimal.ZERO);
			zeitabschnittDTO.setTarifProNebenmahlzeiten(BigDecimal.ZERO);
			dto = createBetreuungEventDTO(zeitabschnittDTO);
			dto.setGemeindeBfsNr(gemeinde.getBfsNummer());
			dto.setGemeindeName(gemeinde.getName());
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
		void createsNewBetreuungsmeldungWhenNoneExists() {
			Capture<Betreuungsmitteilung> capture = expectNewMitteilung();

			// limit to periode gueltigkeit (default is periodeAb to END_OF_TIME)
			betreuungspensum.setGueltigkeit(gesuchsperiode.getGueltigkeit());

			testProcessingSuccess();

			Dossier dossier = gesuch_1GS.getDossier();

			assertThat(capture.getValue(), pojo(Betreuungsmitteilung.class)
				.where(Betreuungsmitteilung::getDossier, sameInstance(dossier))
				.where(Betreuungsmitteilung::getSenderTyp, is(MitteilungTeilnehmerTyp.INSTITUTION))
				.where(Betreuungsmitteilung::getSender, notNullValue())
				.where(Betreuungsmitteilung::getEmpfaengerTyp, is(MitteilungTeilnehmerTyp.JUGENDAMT))
				.where(Betreuungsmitteilung::getEmpfaenger, sameInstance(dossier.getFall().getBesitzer()))
				.where(Betreuungsmitteilung::getMitteilungStatus, is(MitteilungStatus.NEU))
				.where(Betreuungsmitteilung::getSubject, is("Mutationsmeldung von foo"))
				.where(Betreuungsmitteilung::getBetreuung, sameInstance(betreuung))
				.where(Betreuungsmitteilung::getBetreuungspensen, contains(
					matches(betreuungspensum, betreuungspensum.getGueltigkeit().getGueltigAb(), GO_LIVE.minusDays(1)),
					matches(zeitabschnittDTO)
				))
			);
		}

		@Test
		void createsDefaultMessage() {
			Capture<Betreuungsmitteilung> capture = expectNewMitteilung();

			testProcessingSuccess();

			assertThat(capture.getValue().getMessage(), is("my test message"));
		}

		@Test
		void preservesOriginalGueltigkeit() {
			Capture<Betreuungsmitteilung> capture = expectNewMitteilung();
			clientGueltigkeit = new DateRange(Constants.START_OF_TIME, LocalDate.of(2021, 3, 31));

			testProcessingSuccess();

			assertThat(capture.getValue().getBetreuungspensen(), contains(
				matches(betreuungspensum, betreuungspensum.getGueltigkeit().getGueltigAb(), GO_LIVE.minusDays(1)),
				matches(zeitabschnittDTO, GO_LIVE, clientGueltigkeit.getGueltigBis()),
				matches(betreuungspensum, clientGueltigkeit.getGueltigBis().plusDays(1), Constants.END_OF_TIME)
			));
		}

		@Test
		void splitDtoZeitabschnitteWithClientGueltigkeit() {
			clientGueltigkeit = new DateRange(LocalDate.of(2021, 1, 15), LocalDate.of(2021, 5, 31));

			ZeitabschnittDTO beforeGueltigAb =
				createZeitabschnittDTO(LocalDate.of(2021, 1, 1), LocalDate.of(2021, 1, 14));

			ZeitabschnittDTO overlapGueltigAb =
				createZeitabschnittDTO(LocalDate.of(2021, 1, 15), LocalDate.of(2021, 1, 31));

			ZeitabschnittDTO containedInGueltigkeit =
				createZeitabschnittDTO(LocalDate.of(2021, 2, 1), LocalDate.of(2021, 3, 31));

			ZeitabschnittDTO overlapGueltigBis =
				createZeitabschnittDTO(LocalDate.of(2021, 4, 10), LocalDate.of(2021, 5, 31));

			ZeitabschnittDTO afterGueltigBis =
				createZeitabschnittDTO(LocalDate.of(2021, 7, 1), LocalDate.of(2021, 7, 31));

			dto.setZeitabschnitte(Arrays.asList(
				beforeGueltigAb,
				overlapGueltigAb,
				containedInGueltigkeit,
				overlapGueltigBis,
				afterGueltigBis));

			Capture<Betreuungsmitteilung> capture = expectNewMitteilung();

			testProcessingSuccess();

			assertThat(capture.getValue().getBetreuungspensen(), contains(
				matches(betreuungspensum, betreuungspensum.getGueltigkeit().getGueltigAb(), LocalDate.of(2021, 1, 14)),
				matches(overlapGueltigAb, clientGueltigkeit.getGueltigAb(), LocalDate.of(2021, 3, 31)),
				matches(overlapGueltigBis, LocalDate.of(2021, 4, 10), clientGueltigkeit.getGueltigBis()),
				matches(
					betreuungspensum,
					clientGueltigkeit.getGueltigBis().plusDays(1),
					betreuungspensum.getGueltigkeit().getGueltigBis())
			));
		}

		@Test
		void splitDtoZeitabschnitteWithInstitutionGueltigkeit() {
			DateRange institutionGueltigkeit = new DateRange(LocalDate.of(2021, 1, 15), LocalDate.of(2021, 5, 31));
			betreuung.getInstitutionStammdaten().setGueltigkeit(institutionGueltigkeit);

			ZeitabschnittDTO beforeGueltigAb =
				createZeitabschnittDTO(LocalDate.of(2021, 1, 1), LocalDate.of(2021, 1, 14));

			ZeitabschnittDTO overlapGueltigAb =
				createZeitabschnittDTO(LocalDate.of(2021, 1, 15), LocalDate.of(2021, 1, 31));

			ZeitabschnittDTO containedInGueltigkeit =
				createZeitabschnittDTO(LocalDate.of(2021, 2, 1), LocalDate.of(2021, 3, 31));
			containedInGueltigkeit.setBetreuungspensum(BigDecimal.valueOf(75));

			ZeitabschnittDTO overlapGueltigBis =
				createZeitabschnittDTO(LocalDate.of(2021, 4, 10), LocalDate.of(2021, 5, 31));

			ZeitabschnittDTO afterGueltigBis =
				createZeitabschnittDTO(LocalDate.of(2021, 7, 1), LocalDate.of(2021, 7, 31));

			dto.setZeitabschnitte(Arrays.asList(
				beforeGueltigAb,
				overlapGueltigAb,
				containedInGueltigkeit,
				overlapGueltigBis,
				afterGueltigBis));

			Capture<Betreuungsmitteilung> capture = expectNewMitteilung();

			testProcessingSuccess();

			assertThat(capture.getValue().getBetreuungspensen(), contains(
				matches(overlapGueltigAb, institutionGueltigkeit.getGueltigAb(), LocalDate.of(2021, 1, 31)),
				matches(containedInGueltigkeit, containedInGueltigkeit.getVon(), containedInGueltigkeit.getBis()),
				matches(overlapGueltigBis, LocalDate.of(2021, 4, 10), institutionGueltigkeit.getGueltigBis())
			));
		}

		@Test
		void ignoresZeitabschnitteBeforeGoLive() {
			ZeitabschnittDTO beforeGoLive =
				createZeitabschnittDTO(LocalDate.of(2020, 11, 1), LocalDate.of(2020, 12, 14));

			ZeitabschnittDTO overlapGueltigAb =
				createZeitabschnittDTO(LocalDate.of(2020, 12, 15), LocalDate.of(2021, 1, 31));

			dto.setZeitabschnitte(Arrays.asList(beforeGoLive, overlapGueltigAb));

			Capture<Betreuungsmitteilung> capture = expectNewMitteilung();

			testProcessingSuccess();

			assertThat(capture.getValue().getBetreuungspensen(), contains(
				matches(betreuungspensum, betreuungspensum.getGueltigkeit().getGueltigAb(), GO_LIVE.minusDays(1)),
				matches(overlapGueltigAb, LocalDate.of(2021, 1, 1), LocalDate.of(2021, 1, 31))
			));
		}

		@Test
		void createsNewBetreuungsmeldungBasedOnExistingBetreuungsmeldung() {
			DateRange periode = gesuchsperiode.getGueltigkeit();
			BetreuungsmitteilungPensum existingPensum = createBetreuungsmitteilungPensum(new DateRange(
				periode.getGueltigAb().plusMonths(1),
				periode.getGueltigBis()));
			existingPensum.setPensum(BigDecimal.valueOf(50));
			Betreuungsmitteilung existing = createBetreuungMitteilung(existingPensum);

			Capture<Betreuungsmitteilung> capture = expectNewMitteilung();
			testProcessingSuccess(existing);

			// betreuung has a pensum from 2020-08-01 to 2021-07-31
			// there is an existing Betreuungsmitteilung with a single pensum from 2020-09-01 to 2021-07-31
			// -> the new Betreuungsmitteilung should ignore August 2020, since the existing mutation did not define
			// anything in that range.

			assertThat(capture.getValue().getBetreuungspensen(), contains(
				matches(existingPensum, existingPensum.getGueltigkeit().getGueltigAb(), GO_LIVE.minusDays(1)),
				matches(zeitabschnittDTO, GO_LIVE, periode.getGueltigBis())
			));
		}

		@Test
		void createsNewBetreuungsmeldungBasedOnNewestOpenBetreuungsmeldung() {
			DateRange periode = gesuchsperiode.getGueltigkeit();

			LocalDateTime now = LocalDateTime.now();

			BetreuungsmitteilungPensum newestPensum = createBetreuungsmitteilungPensum(periode);
			newestPensum.setPensum(BigDecimal.valueOf(75));
			Betreuungsmitteilung newest = createBetreuungMitteilung(newestPensum);
			newest.setSentDatum(now.minusMinutes(1));

			BetreuungsmitteilungPensum otherOpenPensum = createBetreuungsmitteilungPensum(periode);
			otherOpenPensum.setPensum(BigDecimal.valueOf(70));
			Betreuungsmitteilung otherOpen = createBetreuungMitteilung(otherOpenPensum);
			otherOpen.setSentDatum(now.minusHours(1));

			BetreuungsmitteilungPensum completedPensum = createBetreuungsmitteilungPensum(periode);
			completedPensum.setPensum(BigDecimal.valueOf(60));
			Betreuungsmitteilung completed = createBetreuungMitteilung(completedPensum);
			completed.setApplied(true);

			Capture<Betreuungsmitteilung> capture = expectNewMitteilung();

			testProcessingSuccess(completed, otherOpen, newest);

			assertThat(capture.getValue().getBetreuungspensen(), contains(
				matches(newestPensum, periode.getGueltigAb(), GO_LIVE.minusDays(1)),
				matches(zeitabschnittDTO, GO_LIVE, periode.getGueltigBis())
			));
		}

		@Test
		void createsMutationAndPlatzbestaetigung() {
			DateRange periode = gesuchsperiode.getGueltigkeit();

			LocalDateTime now = LocalDateTime.now();

			// region setup platzbestätigung
			betreuung.setBetreuungsstatus(Betreuungsstatus.WARTEN);
			expect(einstellungService
				.isEnabled(GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_ENABLED, betreuung))
				.andReturn(false);

			expect(betreuungService.betreuungPlatzBestaetigen(betreuung, CLIENT_NAME))
				.andReturn(betreuung);
			betreuung.getErweiterteBetreuungContainer().setErweiterteBetreuungJA(null);

			expect(pensumMapperFactory.createForPlatzbestaetigung(anyObject()))
				.andReturn(unitTestPensumMapper());
			// endregion

			BetreuungsmitteilungPensum newestPensum = createBetreuungsmitteilungPensum(periode);
			newestPensum.setPensum(BigDecimal.valueOf(75));
			Betreuungsmitteilung newest = createBetreuungMitteilung(newestPensum);
			newest.setSentDatum(now.minusMinutes(1));
			newest.setBetreuung(betreuung);

			expectMutationsmeldung(newest);

			Capture<Betreuungsmitteilung> m1 = expectNewMitteilung();

			replayAll();

			Processing result = handler.attemptProcessing(eventMonitor, dto);
			assertThat((PlatzbestaetigungProcessing) result, pojo(PlatzbestaetigungProcessing.class)
				.where(PlatzbestaetigungProcessing::getProcessed, containsInAnyOrder(
					pojo(PlatzbestaetigungProcessing.class)
						.where(PlatzbestaetigungProcessing::getImportForm, is(ImportForm.PLATZBESTAETIGUNG))
						.where(PlatzbestaetigungProcessing::getState, is(ProcessingState.SUCCESS)),
					pojo(PlatzbestaetigungProcessing.class)
						.where(PlatzbestaetigungProcessing::getImportForm, is(MUTATIONS_MITTEILUNG))
						.where(PlatzbestaetigungProcessing::getState, is(ProcessingState.SUCCESS))
				))
			);

			assertThat(m1.getValue().getBetreuung(), is(betreuung));

			verifyAll();
		}

		@Test
		void createsMutationForNotYetFreigegebeneBetreuungWithVorgaenger() {
			betreuung.setBetreuungsstatus(Betreuungsstatus.WARTEN);
			// not yet freigegeben
			betreuung.extractGesuch().setStatus(AntragStatus.IN_BEARBEITUNG_GS);
			betreuung.setVorgaengerId("my-vorgaenger-id");

			expect(betreuungEinstellungenService.getEinstellungen(betreuung))
				.andReturn(BetreuungEinstellungen.builder().build());

			// region setup platzbestätigung
			betreuung.setBetreuungsstatus(Betreuungsstatus.WARTEN);
			expect(einstellungService
				.isEnabled(GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_ENABLED, betreuung))
				.andReturn(false);

			expect(betreuungService.betreuungPlatzBestaetigen(betreuung, CLIENT_NAME))
				.andReturn(betreuung);
			betreuung.getErweiterteBetreuungContainer().setErweiterteBetreuungJA(null);

			expect(pensumMapperFactory.createForPlatzbestaetigung(anyObject()))
				.andReturn(unitTestPensumMapper());
			// endregion

			Gesuch vorgaengerGesuch = initGesuch();
			vorgaengerGesuch.setDossier(gesuch_1GS.getDossier());
			Betreuung mutationsmeldungBetreuung = PlatzbestaetigungTestUtil.betreuungWithSingleContainer(vorgaengerGesuch, LocalDate.MIN, LocalDate.MAX);
			mutationsmeldungBetreuung.setId("my-vorgaenger-id");
			expect(betreuungService.findBetreuung(mutationsmeldungBetreuung.getId(), false))
				.andReturn(Optional.of(mutationsmeldungBetreuung));

			expectBetreuungFound(betreuung);

			expect(mitteilungService.findOffeneBetreuungsmitteilungenForBetreuung(betreuung))
				.andReturn(List.of());

			expect(mitteilungService.findOffeneBetreuungsmitteilungenForBetreuung(mutationsmeldungBetreuung))
				.andReturn(List.of());

			mockClients(clientGueltigkeit);

			ExternalClient mockClient = new ExternalClient();
			mockClient.setClientName(CLIENT_NAME);
			expect(institutionExternalClient.getExternalClient())
				.andStubReturn(mockClient);

			expectMutationsmeldungCreated(mutationsmeldungBetreuung);

			Capture<Betreuungsmitteilung> mitteilungCapture = expectNewMitteilung();

			replayAll();

			Processing result = handler.attemptProcessing(eventMonitor, dto);
			assertThat(result.isProcessingSuccess(), is(true));
			verifyAll();

			assertThat(mitteilungCapture.getValue().getBetreuung(), is(mutationsmeldungBetreuung));
		}

		@Test
		void ignoresBetreuungsmeldungWhenIdenticalToExistingBetreuung() {
			// limit to periode gueltigkeit (default is periodeAb to END_OF_TIME)
			betreuungspensum.setGueltigkeit(gesuchsperiode.getGueltigkeit());
			// make sure pensum is the same
			betreuungspensum.setPensum(zeitabschnittDTO.getBetreuungspensum());

			expectMutationsmeldung();

			replayAll();

			Processing result = handler.attemptProcessing(eventMonitor, dto);
			assertThat(
				(PlatzbestaetigungProcessing) result,
				ignored(MUTATIONS_MITTEILUNG, "Die Betreuungsmeldung und die Betreuung sind identisch.")
			);
			verifyAll();
		}

		@Test
		void ignoresBetreuungsmeldungWhenIdenticalToExistingBetreuungsmeldung() {
			BetreuungsmitteilungPensum betreuungsmitteilungPensum =
				createBetreuungsmitteilungPensum(gesuchsperiode.getGueltigkeit());
			Betreuungsmitteilung betreuungMitteilung = createBetreuungMitteilung(betreuungsmitteilungPensum);

			expectMutationsmeldung(betreuungMitteilung);

			replayAll();

			Processing result = handler.attemptProcessing(eventMonitor, dto);
			assertThat(
				(PlatzbestaetigungProcessing) result,
				ignored(MUTATIONS_MITTEILUNG, "Die Betreuungsmeldung ist identisch mit der neusten offenen Betreuungsmeldung.")
			);
			verifyAll();
		}

		@Test
		void createsBetreuungsmeldungWhenIdenticalToExistingBetreuungAndOpenBetreuungsmeldung() {
			DateRange periode = gesuchsperiode.getGueltigkeit();
			// limit to periode gueltigkeit (default is periodeAb to END_OF_TIME)
			betreuungspensum.setGueltigkeit(periode);
			// make sure pensum is the same
			betreuungspensum.setPensum(zeitabschnittDTO.getBetreuungspensum());

			Betreuungsmitteilung betreuungMitteilung = createBetreuungMitteilung(
				// existing Betreuungsmitteilung and new one are not equal due to different  gueltigkeit
				createBetreuungsmitteilungPensum(new DateRange(periode.getGueltigAb(), GO_LIVE))
			);

			Capture<Betreuungsmitteilung> capture = expectNewMitteilung();

			testProcessingSuccess(betreuungMitteilung);

			assertThat(capture.getValue().getBetreuungspensen(), contains(
				matches(betreuungspensum, periode)
			));
		}

		@Test
		void ignoresBetreuungsmeldungWhenClientGueltigkeitEndsBeforeGoLive() {
			clientGueltigkeit = new DateRange(2020);
			zeitabschnittDTO.setVon(clientGueltigkeit.getGueltigAb());
			zeitabschnittDTO.setBis(clientGueltigkeit.getGueltigBis());

			expectMutationsmeldung();

			replayAll();

			Processing result = handler.attemptProcessing(eventMonitor, dto);
			assertThat(
				(PlatzbestaetigungProcessing) result,
				ignored(MUTATIONS_MITTEILUNG, "Die Betreuungsmeldung und die Betreuung sind identisch.")
			);
			verifyAll();
		}

		/**
		 * regression test: when there is an existing Zeitabschnitt starting in client range but exceeding it, it
		 * should only be included once (after the client's gueltigkeit).
		 * <p>
		 * The setup requires to set distinct values to the Zeitabschnitte, here ensured with Betreuungskost, because
		 * otherwise the Zeitabschnitte merging creates a single Zeitabschnitt (and thus does not show the problem).
		 */
		@Test
		void shouldNotCreateOverlappingPensen() {
			LocalDate march = LocalDate.of(2021, 3, 1);
			clientGueltigkeit = new DateRange(march, LocalDate.of(2021, 6, 30));
			zeitabschnittDTO.setVon(march);
			zeitabschnittDTO.setBis(LocalDate.of(2021, 7, 2));
			zeitabschnittDTO.setBetreuungskosten(BigDecimal.valueOf(100));

			BetreuungsmitteilungPensum p1 =
				createBetreuungsmitteilungPensum(new DateRange(LocalDate.of(2021, 2, 1), LocalDate.of(2021, 2, 28)));
			p1.setMonatlicheBetreuungskosten(BigDecimal.valueOf(2000));
			BetreuungsmitteilungPensum p2 =
				createBetreuungsmitteilungPensum(new DateRange(march, LocalDate.of(2021, 7, 1)));
			p2.setMonatlicheBetreuungskosten(BigDecimal.valueOf(99));
			Betreuungsmitteilung existing = createBetreuungMitteilung(p1, p2);

			Capture<Betreuungsmitteilung> capture = expectNewMitteilung();

			testProcessingSuccess(existing);

			assertThat(capture.getValue().getBetreuungspensen(), contains(
				matches(p1, p1.getGueltigkeit()),
				matches(zeitabschnittDTO, new DateRange(march, clientGueltigkeit.getGueltigBis())),
				matches(p2, LocalDate.of(2021, 7, 1), LocalDate.of(2021, 7, 1))
			));
		}

		private void testProcessingSuccess(@Nonnull Betreuungsmitteilung... existing) {
			expectMutationsmeldung(existing);

			replayAll();

			Processing result = handler.attemptProcessing(eventMonitor, dto);
			assertThat(result.isProcessingSuccess(), is(true));
			verifyAll();
		}

		private void expectMutationsmeldung(@Nonnull Betreuungsmitteilung... existing) {
			expectBetreuungFound(betreuung);
			expect(betreuungEinstellungenService.getEinstellungen(betreuung))
				.andReturn(BetreuungEinstellungen.builder().build());

			expect(mitteilungService.findOffeneBetreuungsmitteilungenForBetreuung(betreuung))
				.andReturn(Arrays.asList(existing));

			mockClients(clientGueltigkeit);

			ExternalClient mockClient = new ExternalClient();
			mockClient.setClientName(CLIENT_NAME);
			expect(institutionExternalClient.getExternalClient())
				.andStubReturn(mockClient);

			expectMutationsmeldungCreated(betreuung);
		}

		private void expectMutationsmeldungCreated(Betreuung mutierteBetreuung) {
			Benutzer benutzer = mock(Benutzer.class);
			expect(betreuungEventHelper.getMutationsmeldungBenutzer(mutierteBetreuung)).andReturn(benutzer);
			expect(benutzer.getMandant()).andReturn(TestDataUtil.createMandant(MandantIdentifier.BERN)).anyTimes();

			expect(mitteilungService.isBetreuungGueltigForMutation(mutierteBetreuung)).andReturn(true);

			expect(mitteilungService.createNachrichtForMutationsmeldung(anyObject(), anyObject(), anyObject()))
				.andReturn("my test message");

			expect(gemeindeService.getGemeindeStammdatenByGemeindeId(gemeinde.getId()))
				.andReturn(Optional.of(TestDataUtil.createGemeindeStammdaten(gemeinde)));

			expect(pensumMapperFactory.createForBetreuungsmitteilung(anyObject()))
				.andReturn(unitTestPensumMapper());
		}

		@Nonnull
		private Capture<Betreuungsmitteilung> expectNewMitteilung() {
			Capture<Betreuungsmitteilung> captured = newCapture();
			//noinspection ConstantConditions
			mitteilungService.replaceOffeneBetreungsmitteilungenWithSameReferenzNummer(capture(captured), eq(REF_NUMMER));
			expectLastCall();

			return captured;
		}
	}

	private void expectBetreuungWithoutOffeneBetreuungsmitteilung(Betreuung betreuung) {
		expectBetreuungFound(betreuung);
		expect(mitteilungService.findOffeneBetreuungsmitteilungenForBetreuung(betreuung))
			.andReturn(Collections.emptyList());
	}

	private void expectBetreuungFound(@Nonnull Betreuung betreuung) {
		expect(betreuungService.findBetreuungByReferenzNummer(REF_NUMMER, false))
			.andReturn(Optional.of(betreuung));
	}

	private void mockClients(@Nonnull DateRange clientGueltigkeit) {
		mockClients(clientGueltigkeit, Collections.emptyList());
	}

	private void mockClients(@Nonnull DateRange clientGueltigkeit, Collection<InstitutionExternalClient> others) {
		institutionExternalClient = mockClient(clientGueltigkeit, CLIENT_NAME);

		expect(betreuungEventHelper.getExternalClients(eq(CLIENT_NAME), EasyMock.<Betreuung>anyObject()))
			.andReturn(new InstitutionExternalClients(institutionExternalClient, others));

		expect(institutionExternalClient.getGueltigkeit())
			.andStubReturn(clientGueltigkeit);
	}

	@Nonnull
	private InstitutionExternalClient mockClient(@Nonnull DateRange clientGueltigkeit, @Nonnull String clientName) {
		InstitutionExternalClient client = mock(InstitutionExternalClient.class);

		ExternalClient mockClient = new ExternalClient();
		mockClient.setClientName(clientName);
		expect(client.getExternalClient())
			.andStubReturn(mockClient);

		expect(client.getGueltigkeit())
			.andStubReturn(clientGueltigkeit);

		return client;
	}

	@Nonnull
	private ZeitabschnittDTO defaultZeitabschnittDTO() {
		return PlatzbestaetigungTestUtil.createZeitabschnittDTO(
			gesuchsperiode.getGueltigkeit().getGueltigAb(),
			gesuchsperiode.getGueltigkeit().getGueltigBis().withDayOfYear(31)
		);
	}

	@Nonnull
	private Betreuung betreuungWithSingleContainer() {
		return PlatzbestaetigungTestUtil.betreuungWithSingleContainer(gesuch_1GS);
	}

	private static Stream<Arguments> provideSprachfoederungTestData() {
		return Stream.of(
			Arguments.of(null, LocalDate.now().plusDays(1), true),
			Arguments.of(null, LocalDate.of(2023, 1, 15), false),
			Arguments.of(false, LocalDate.of(2023, 1, 15), false),
			Arguments.of(false, LocalDate.now().plusDays(1), false),
			Arguments.of(true, LocalDate.now().plusDays(1), true),
			Arguments.of(true, LocalDate.of(2023, 1, 15), true));
	}
}
