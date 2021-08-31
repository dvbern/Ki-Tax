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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.BelegungTagesschuleModul;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.BetreuungMonitoring;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.InstitutionExternalClient;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.GesuchsperiodeStatus;
import ch.dvbern.ebegu.inbox.services.BetreuungEventHelper;
import ch.dvbern.ebegu.services.BetreuungMonitoringService;
import ch.dvbern.ebegu.services.BetreuungService;
import ch.dvbern.ebegu.services.ModulTagesschuleService;
import ch.dvbern.ebegu.services.VerfuegungService;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.kibon.exchange.commons.tagesschulen.ModulAuswahlDTO;
import ch.dvbern.kibon.exchange.commons.tagesschulen.TagesschuleBestaetigungEventDTO;
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
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.stringContainsInOrder;

@ExtendWith(EasyMockExtension.class)
public class AnmeldungBestaetigungEventHandlerTest extends EasyMockSupport {

	public static final String CLIENT_NAME = "foo";
	public static final LocalDateTime EVENT_TIME = LocalDateTime.now();

	@TestSubject
	private final AnmeldungBestaetigungEventHandler anmeldungBestaetigungEventHandler = new AnmeldungBestaetigungEventHandler();

	@SuppressWarnings("InstanceVariableMayNotBeInitialized")
	@Mock
	private BetreuungService betreuungService;

	@SuppressWarnings("InstanceVariableMayNotBeInitialized")
	@Mock
	private BetreuungEventHelper betreuungEventHelper;

	@SuppressWarnings("InstanceVariableMayNotBeInitialized")
	@Mock
	private ModulTagesschuleService modulTagesschuleService;

	@SuppressWarnings("InstanceVariableMayNotBeInitialized")
	@Mock
	private VerfuegungService verfuegungService;

	@SuppressWarnings("InstanceVariableMayNotBeInitialized")
	@Mock(MockType.NICE)
	private BetreuungMonitoringService betreuungMonitoringService;

	private Gesuch gesuch_1GS = null;
	private Gesuchsperiode gesuchsperiode = null;

	private AnmeldungTagesschule anmeldungTagesschule;
	private TagesschuleBestaetigungEventDTO tagesschuleBestaetigungEventDTO;

	@BeforeEach
	void setUp() {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(false);
		gesuchsperiode = betreuung.extractGesuchsperiode();
		anmeldungTagesschule = TestDataUtil.createAnmeldungTagesschuleWithModules(betreuung.getKind(), gesuchsperiode);
		anmeldungTagesschule.extractGesuch().setStatus(AntragStatus.IN_BEARBEITUNG_JA);
		tagesschuleBestaetigungEventDTO = AnmeldungTestUtil.createTagesschuleBestaetigungEventDTO();
	}

	@ParameterizedTest
	@EnumSource(value = AntragStatus.class,
		names = { "VERFUEGT", "VERFUEGEN", "KEIN_KONTINGENT", "NUR_SCHULAMT", "BESCHWERDE_HAENGIG",
			"PRUEFUNG_STV", "IN_BEARBEITUNG_STV", "GEPRUEFT_STV", "KEIN_ANGEBOT" },
		mode = Mode.INCLUDE)
	void isAnmeldungDirektUebernehmen(@Nonnull AntragStatus status) {
		assertThat(anmeldungBestaetigungEventHandler.anmeldungDirektUebernehmen(status), is(true));
	}


	@Nested
	class IgnoreEventTest {

		@Test
		void ignoreEventWhenNoAnmeldungFound() {
			expect(betreuungService.findAnmeldungenTagesschuleByBGNummer(tagesschuleBestaetigungEventDTO.getRefnr()))
				.andReturn(Optional.empty());

			testIgnored(tagesschuleBestaetigungEventDTO,"AnmeldungTagessschule nicht gefunden.");
		}

		@ParameterizedTest
		@EnumSource(value = GesuchsperiodeStatus.class, names = "AKTIV", mode = Mode.EXCLUDE)
		void ignoreEventWhenPeriodeNotAktiv(@Nonnull GesuchsperiodeStatus status) {
			anmeldungTagesschule.extractGesuchsperiode().setStatus(status);

			expect(betreuungService.findAnmeldungenTagesschuleByBGNummer(tagesschuleBestaetigungEventDTO.getRefnr()))
				.andReturn(Optional.of(anmeldungTagesschule));

			testIgnored(tagesschuleBestaetigungEventDTO,"Die Gesuchsperiode ist nicht aktiv.");
		}

		@Test
		void ignoreEventWhenBetreuungMutiertAfterEventTimestamp() {

			LocalDateTime anmeldungMutiertTime = EVENT_TIME.plusSeconds(1);
			anmeldungTagesschule.setTimestampMutiert(anmeldungMutiertTime);

			expect(betreuungService.findAnmeldungenTagesschuleByBGNummer(tagesschuleBestaetigungEventDTO.getRefnr()))
				.andReturn(Optional.of(anmeldungTagesschule));

			testIgnored(tagesschuleBestaetigungEventDTO, "Die AnmeldungTagesschule wurde verändert, nachdem das AnmeldungTagesschuleEvent generiert wurde.");
		}

		@Test
		void ignoreEventWhenNoExternalClient() {

			expect(betreuungService.findAnmeldungenTagesschuleByBGNummer(tagesschuleBestaetigungEventDTO.getRefnr()))
				.andReturn(Optional.of(anmeldungTagesschule));
			expect(betreuungEventHelper.getExternalClient(CLIENT_NAME, anmeldungTagesschule))
				.andReturn(Optional.empty());
			expect(betreuungEventHelper.clientNotFoundFailure(CLIENT_NAME, anmeldungTagesschule))
				.andReturn(Processing.failure("Kein InstitutionExternalClient Namens ist der Institution zugewiesen"));


			testIgnored(tagesschuleBestaetigungEventDTO, "Kein InstitutionExternalClient Namens ist der Institution zugewiesen");
		}

		@Test
		void ignoreEventWhenClientGueltigkeitOutsidePeriode() {

			expect(betreuungService.findAnmeldungenTagesschuleByBGNummer(tagesschuleBestaetigungEventDTO.getRefnr()))
				.andReturn(Optional.of(anmeldungTagesschule));
			mockClient(new DateRange(2022));

			testIgnored(tagesschuleBestaetigungEventDTO,"Der Client hat innerhalb der Periode keinen Berechtigung.");
		}

		@ParameterizedTest
		@EnumSource(value = Betreuungsstatus.class,
			names = { "SCHULAMT_ANMELDUNG_AUSGELOEST" },
			mode = Mode.EXCLUDE)
		void ignoreWhenInvalidBetreuungStatus(@Nonnull Betreuungsstatus status) {
			anmeldungTagesschule.setBetreuungsstatus(status);

			expect(betreuungService.findAnmeldungenTagesschuleByBGNummer(tagesschuleBestaetigungEventDTO.getRefnr()))
				.andReturn(Optional.of(anmeldungTagesschule));
			mockClient(Constants.DEFAULT_GUELTIGKEIT);

			testIgnored(tagesschuleBestaetigungEventDTO,"Die AnmeldungTagesschule hat einen ungültigen Status: " + status);
		}

		@Test
		void ignoreEventWhenKeineBelegungTagesschule() {
			anmeldungTagesschule.setBelegungTagesschule(null);

			expect(betreuungService.findAnmeldungenTagesschuleByBGNummer(tagesschuleBestaetigungEventDTO.getRefnr()))
				.andReturn(Optional.of(anmeldungTagesschule));

			mockClient(Constants.DEFAULT_GUELTIGKEIT);

			testIgnored(tagesschuleBestaetigungEventDTO,"Anmeldung hat einen Datenproblem, keine BelegungTagesschule");
		}

		@Test
		void ignoreEventWhenKeineModule() {
			expect(betreuungService.findAnmeldungenTagesschuleByBGNummer(tagesschuleBestaetigungEventDTO.getRefnr()))
				.andReturn(Optional.of(anmeldungTagesschule));

			mockClient(Constants.DEFAULT_GUELTIGKEIT);

			testIgnored(tagesschuleBestaetigungEventDTO,"TagesschuleBestaetigungEventDTO hat keine Module");
		}


		private void testIgnored(@Nonnull TagesschuleBestaetigungEventDTO dto, @Nonnull String message) {
			replayAll();

			Processing result = anmeldungBestaetigungEventHandler.attemptProcessing(EVENT_TIME, dto, CLIENT_NAME);
			assertThat(result, failed(message));
			verifyAll();
		}
	}

	@Nested
	class AnmeldungBestaetigungTest {

		@Test
		void testAnmeldungAkzeptiert() {
			List<ModulAuswahlDTO> modulAuswahlDTOList = prepareStandardCallAndModulAuswahlList();

			tagesschuleBestaetigungEventDTO.setModule(modulAuswahlDTOList);

			expect(betreuungService.anmeldungSchulamtModuleAkzeptieren(anmeldungTagesschule)).andReturn(anmeldungTagesschule);

			testSuccess(tagesschuleBestaetigungEventDTO);
		}

		@Test
		void testAnmeldungUebernhemen() {
			anmeldungTagesschule.extractGesuch().setStatus(AntragStatus.VERFUEGT);

			List<ModulAuswahlDTO> modulAuswahlDTOList = prepareStandardCallAndModulAuswahlList();

			tagesschuleBestaetigungEventDTO.setModule(modulAuswahlDTOList);

			expect(verfuegungService.anmeldungTagesschuleUebernehmen(anmeldungTagesschule)).andReturn(anmeldungTagesschule);

			testSuccess(tagesschuleBestaetigungEventDTO);
		}

		@Test
		void testModuleNichtGefunden() {
			List<ModulAuswahlDTO> modulAuswahlDTOList = prepareStandardCallAndModulAuswahlList();
			modulAuswahlDTOList.get(0).setWeekday(7);

			tagesschuleBestaetigungEventDTO.setModule(modulAuswahlDTOList);

			expect(betreuungService.anmeldungSchulamtModuleAkzeptieren(anmeldungTagesschule)).andReturn(anmeldungTagesschule);

			Capture<BetreuungMonitoring> capture = expectNewBetreuungMonitoringLog();

			testSuccess(tagesschuleBestaetigungEventDTO);

			assertThat(capture.getValue().getInfoText(), stringContainsInOrder(
				"Tagesschuleanmeldung einen Modul wurde nicht gefunden fuer die ModulTagesschuleGroup: ", modulAuswahlDTOList.get(0).getModulId()));
		}

		@Test
		void testModulGroupNichtGefunden() {
			List<ModulAuswahlDTO> modulAuswahlDTOList = new ArrayList<>();

			Objects.requireNonNull(anmeldungTagesschule.getBelegungTagesschule());

			anmeldungTagesschule.getBelegungTagesschule().getBelegungTagesschuleModule().forEach(
				belegungTagesschuleModul -> {
					modulAuswahlDTOList.add(AnmeldungTestUtil.createModulAuswahlDTO(belegungTagesschuleModul.getModulTagesschule().getModulTagesschuleGroup().getId(), belegungTagesschuleModul.getModulTagesschule().getWochentag().getValue()));
					expect(modulTagesschuleService.findModulTagesschuleGroup(belegungTagesschuleModul.getModulTagesschule().getModulTagesschuleGroup().getId())).andReturn(Optional.empty());
				}
			);

			tagesschuleBestaetigungEventDTO.setModule(modulAuswahlDTOList);

			expect(betreuungService.anmeldungSchulamtModuleAkzeptieren(anmeldungTagesschule)).andReturn(anmeldungTagesschule);

			Capture<BetreuungMonitoring> capture = expectNewBetreuungMonitoringLog();

			testSuccess(tagesschuleBestaetigungEventDTO);

			assertThat(capture.getValue().getInfoText(), stringContainsInOrder(
				"Tagesschuleanmeldung einen ModulTagesschuleGroup wurde nicht gefunden: ", modulAuswahlDTOList.get(0).getModulId()));
		}

		@Test
		void testModulWeggenommen() {
			List<ModulAuswahlDTO> modulAuswahlDTOList = new ArrayList<>();

			Objects.requireNonNull(anmeldungTagesschule.getBelegungTagesschule());

			AtomicBoolean firstCall = new AtomicBoolean(true);
			anmeldungTagesschule.getBelegungTagesschule().getBelegungTagesschuleModule().forEach(
				belegungTagesschuleModul -> {
					modulAuswahlDTOList.add(AnmeldungTestUtil.createModulAuswahlDTO(belegungTagesschuleModul.getModulTagesschule().getModulTagesschuleGroup().getId(), belegungTagesschuleModul.getModulTagesschule().getWochentag().getValue()));
					if(firstCall.get()) {
						firstCall.set(false);
					}
					else {
						expect(modulTagesschuleService.findModulTagesschuleGroup(belegungTagesschuleModul.getModulTagesschule().getModulTagesschuleGroup().getId())).andReturn(Optional.of(belegungTagesschuleModul.getModulTagesschule().getModulTagesschuleGroup()));
					}
				}
			);
			modulAuswahlDTOList.remove(0);
			tagesschuleBestaetigungEventDTO.setModule(modulAuswahlDTOList);

			expect(betreuungService.anmeldungSchulamtModuleAkzeptieren(anmeldungTagesschule)).andReturn(anmeldungTagesschule);

			testSuccess(tagesschuleBestaetigungEventDTO);

			assertThat(anmeldungTagesschule.getBelegungTagesschule().getBelegungTagesschuleModule().size(), is(3));
		}

		@Test
		void testModulAddiert() {
			Objects.requireNonNull(anmeldungTagesschule.getBelegungTagesschule());

			Optional<BelegungTagesschuleModul> belegungTagesschuleModulToRemove = anmeldungTagesschule.getBelegungTagesschule().getBelegungTagesschuleModule().stream().findAny();
			anmeldungTagesschule.getBelegungTagesschule().getBelegungTagesschuleModule().remove(belegungTagesschuleModulToRemove.get());
			List<ModulAuswahlDTO> modulAuswahlDTOList = prepareStandardCallAndModulAuswahlList();
			modulAuswahlDTOList.add(AnmeldungTestUtil.createModulAuswahlDTO(belegungTagesschuleModulToRemove.get().getModulTagesschule().getModulTagesschuleGroup().getId(), belegungTagesschuleModulToRemove.get().getModulTagesschule().getWochentag().getValue()));
			expect(modulTagesschuleService.findModulTagesschuleGroup(belegungTagesschuleModulToRemove.get().getModulTagesschule().getModulTagesschuleGroup().getId())).andReturn(Optional.of(belegungTagesschuleModulToRemove.get().getModulTagesschule().getModulTagesschuleGroup()));

			tagesschuleBestaetigungEventDTO.setModule(modulAuswahlDTOList);

			expect(betreuungService.anmeldungSchulamtModuleAkzeptieren(anmeldungTagesschule)).andReturn(anmeldungTagesschule);

			testSuccess(tagesschuleBestaetigungEventDTO);

			assertThat(anmeldungTagesschule.getBelegungTagesschule().getBelegungTagesschuleModule().size(), is(4));
		}

		private List<ModulAuswahlDTO> prepareStandardCallAndModulAuswahlList(){
			List<ModulAuswahlDTO> modulAuswahlDTOList = new ArrayList<>();
			Objects.requireNonNull(anmeldungTagesschule.getBelegungTagesschule());
			anmeldungTagesschule.getBelegungTagesschule().getBelegungTagesschuleModule().forEach(
				belegungTagesschuleModul -> {
					modulAuswahlDTOList.add(AnmeldungTestUtil.createModulAuswahlDTO(belegungTagesschuleModul.getModulTagesschule().getModulTagesschuleGroup().getId(), belegungTagesschuleModul.getModulTagesschule().getWochentag().getValue()));
					expect(modulTagesschuleService.findModulTagesschuleGroup(belegungTagesschuleModul.getModulTagesschule().getModulTagesschuleGroup().getId())).andReturn(Optional.of(belegungTagesschuleModul.getModulTagesschule().getModulTagesschuleGroup()));
				}
			);
			return modulAuswahlDTOList;
		}

		private void testSuccess(@Nonnull TagesschuleBestaetigungEventDTO dto) {
			expect(betreuungService.findAnmeldungenTagesschuleByBGNummer(tagesschuleBestaetigungEventDTO.getRefnr()))
				.andReturn(Optional.of(anmeldungTagesschule));
			mockClient(Constants.DEFAULT_GUELTIGKEIT);
			replayAll();
			Processing result = anmeldungBestaetigungEventHandler.attemptProcessing(EVENT_TIME, tagesschuleBestaetigungEventDTO, CLIENT_NAME);
			assertThat(result.isProcessingSuccess(), is(true));
			verifyAll();
		}

		@Nonnull
		private Capture<BetreuungMonitoring> expectNewBetreuungMonitoringLog() {
			Capture<BetreuungMonitoring> captured = EasyMock.newCapture();
			//noinspection ConstantConditions
			expect(betreuungMonitoringService.saveBetreuungMonitoring(EasyMock.capture(captured))).andReturn(EasyMock.capture(captured));
			expectLastCall();
			return captured;
		}

	}

	private void mockClient(@Nonnull DateRange clientGueltigkeit) {
		InstitutionExternalClient institutionExternalClient = mock(InstitutionExternalClient.class);

		expect(betreuungEventHelper.getExternalClient(eq(CLIENT_NAME), EasyMock.<AnmeldungTagesschule> anyObject()))
			.andReturn(Optional.of(institutionExternalClient));

		expect(institutionExternalClient.getGueltigkeit())
			.andReturn(clientGueltigkeit);
	}
}
