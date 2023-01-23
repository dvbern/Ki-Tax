/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;

import ch.dvbern.ebegu.dto.FinanzielleSituationResultateDTO;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.NeueVeranlagungsMitteilung;
import ch.dvbern.ebegu.entities.SteuerdatenResponse;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.Eingangsart;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.SteuerdatenAnfrageStatus;
import ch.dvbern.ebegu.nesko.handler.KibonAnfrageContext;
import ch.dvbern.ebegu.nesko.handler.KibonAnfrageHandler;
import ch.dvbern.ebegu.services.EinstellungService;
import ch.dvbern.ebegu.services.FinanzielleSituationService;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.services.MitteilungService;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.test.util.TestDataInstitutionStammdatenBuilder;
import ch.dvbern.ebegu.testfaelle.Testfall01_WaeltiDagmar;
import ch.dvbern.kibon.exchange.commons.neskovanp.NeueVeranlagungEventDTO;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.hibernate.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;

import static ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungTestUtil.failed;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.stringContainsInOrder;

@ExtendWith(EasyMockExtension.class)
public class NeueVeranlagungEventHandlerTest extends EasyMockSupport {

	private NeueVeranlagungEventDTO dto;
	private Gesuch gesuch_1GS = null;
	private String zpvNummer = "1000001";
	private SteuerdatenResponse steuerdatenResponse;
	private KibonAnfrageContext kibonAnfrageContext;

	@SuppressWarnings("InstanceVariableMayNotBeInitialized")
	@Mock
	private MitteilungService mitteilungService;

	@SuppressWarnings("InstanceVariableMayNotBeInitialized")
	@Mock
	private GesuchService gesuchService;

	@SuppressWarnings("InstanceVariableMayNotBeInitialized")
	@Mock
	private KibonAnfrageHandler kibonAnfrageHandler;

	@SuppressWarnings("InstanceVariableMayNotBeInitialized")
	@Mock
	private FinanzielleSituationService finanzielleSituationService;

	@SuppressWarnings("InstanceVariableMayNotBeInitialized")
	@Mock
	private EinstellungService einstellungService;

	@SuppressWarnings("InstanceVariableMayNotBeInitialized")
	@Mock
	private GemeindeService gemeindeService;

	@SuppressWarnings("InstanceVariableMayNotBeInitialized")
	@Mock
	private Persistence persistence;

	@TestSubject
	private final NeueVeranlagungEventHandler handler = new NeueVeranlagungEventHandler();

	@BeforeEach
	void setUp() {
		Gesuchsperiode gesuchsperiode = TestDataUtil.createGesuchsperiodeXXYY(2020, 2021);
		Gemeinde gemeinde = TestDataUtil.createGemeindeParis();
		List<InstitutionStammdaten> institutionStammdatenList = new ArrayList<>();
		institutionStammdatenList.add(TestDataUtil.createInstitutionStammdatenKitaWeissenstein());
		institutionStammdatenList.add(TestDataUtil.createInstitutionStammdatenKitaBruennen());
		Testfall01_WaeltiDagmar testfall_1GS =
			new Testfall01_WaeltiDagmar(
				gesuchsperiode,
				false,
				gemeinde,
				new TestDataInstitutionStammdatenBuilder(gesuchsperiode));
		testfall_1GS.createFall();
		testfall_1GS.createGesuch(LocalDate.of(2016, Month.DECEMBER, 12));
		gesuch_1GS = testfall_1GS.fillInGesuch();
		gesuch_1GS.setEingangsart(Eingangsart.ONLINE);
		gesuch_1GS.getDossier().getFall().setBesitzer(new Benutzer());
		Objects.requireNonNull(gesuch_1GS.getDossier().getFall().getBesitzer());
		gesuch_1GS.getDossier().getFall().getBesitzer().setZpvNummer(zpvNummer);
		dto = new NeueVeranlagungEventDTO();
		dto.setZpvNummer(1000001);
		Objects.requireNonNull(gesuch_1GS.getGesuchsteller1());
		dto.setGeburtsdatum(gesuch_1GS.getGesuchsteller1().getGesuchstellerJA().getGeburtsdatum());
		dto.setGesuchsperiodeBeginnJahr(gesuchsperiode.getBasisJahrPlus1());
		dto.setKibonAntragId(gesuch_1GS.getId());
		steuerdatenResponse = NeueVeranlagungTestUtil.createSteuerdatenResponseAleine(dto);
		Objects.requireNonNull(gesuch_1GS.getGesuchsteller1()
			.getFinanzielleSituationContainer());
		gesuch_1GS.getGesuchsteller1()
			.getFinanzielleSituationContainer()
			.getFinanzielleSituationJA()
			.setSteuerdatenResponse(steuerdatenResponse);

		kibonAnfrageContext = NeueVeranlagungTestUtil.initKibonAnfrageContext(gesuch_1GS);
	}

	@Test
	void gesuchIdUnbekannt() {
		expectGesuchNotFound();

		testIgnored("Kein Gesuch für Key gefunden. Key: ");
	}

	@ParameterizedTest
	@EnumSource(value = AntragStatus.class,
		names = { "IN_BEARBEITUNG_GS", "IN_BEARBEITUNG_SOZIALDIENST", "FREIGABEQUITTUNG" },
		mode = Mode.INCLUDE)
	void gesuchNochNichtFreigegeben(@Nonnull AntragStatus status) {
		gesuch_1GS.setStatus(status);
		expectGesuchFound();
		testIgnored("Gesuch ist noch nicht freigegeben");
	}

	@Test
	void steuerAbfrageNichtErfolgreich() {
		expectGesuchFound();
		expect(finanzielleSituationService.calculateResultate(anyObject())).andReturn(new FinanzielleSituationResultateDTO());
		expect(kibonAnfrageHandler.handleKibonAnfrage(anyObject(), eq(false))).andReturn(kibonAnfrageContext);
		testIgnored("Keine neue Veranlagung gefunden");
	}

	@Test
	void steuerdatenResponseNichtRechtskraeftig() {
		kibonAnfrageContext.setSteuerdatenAnfrageStatus(SteuerdatenAnfrageStatus.PROVISORISCH);
		expectGesuchFound();
		expect(finanzielleSituationService.calculateResultate(anyObject())).andReturn(new FinanzielleSituationResultateDTO());
		expect(kibonAnfrageHandler.handleKibonAnfrage(anyObject(), eq(false))).andReturn(kibonAnfrageContext);
		testIgnored("Die neue Veranlagung ist noch nicht Rechtskraeftig");
	}

	@Test
	void finSitUnterschiedGleich() {
		kibonAnfrageContext.setSteuerdatenAnfrageStatus(SteuerdatenAnfrageStatus.RECHTSKRAEFTIG);
		expectGesuchFound();
		Einstellung einstellung = findEinstellungMinUnterschied();
		expect(einstellung.getValueAsBigDecimal()).andReturn(new BigDecimal(50));
		expect(finanzielleSituationService.calculateResultate(anyObject())).andReturn(new FinanzielleSituationResultateDTO());
		expect(kibonAnfrageHandler.handleKibonAnfrage(anyObject(), eq(false))).andReturn(kibonAnfrageContext);
		expect(finanzielleSituationService.calculateResultate(anyObject())).andReturn(new FinanzielleSituationResultateDTO());
		testIgnored("Keine Meldung erstellt. Das massgebende Einkommen hat sich um 0 Franken verändert. Der konfigurierte Schwellenwert zur Benachrichtigung liegt bei 50 Franken");
	}

	@Test
	void finSitUnterschiedMehrAberNichtGenuegen() {
		expectEverythingUntilCompare();
		Einstellung einstellung = findEinstellungMinUnterschied();
		expect(einstellung.getValueAsBigDecimal()).andReturn(new BigDecimal(60));
		testIgnored("Keine Meldung erstellt. Das massgebende Einkommen hat sich um 60 Franken verändert."
			+ " Der konfigurierte Schwellenwert zur Benachrichtigung liegt bei 60 Franken");
	}

	@Test
	void createsNeueVeranlagungMitteilung() {
		expectEverythingUntilCompare();
		Einstellung einstellung = findEinstellungMinUnterschied();
		expect(einstellung.getValueAsBigDecimal()).andReturn(new BigDecimal(50));
		GemeindeStammdaten gemeindeStammdaten = new GemeindeStammdaten();
		expect(gemeindeService.getGemeindeStammdatenByGemeindeId(anyObject())).andReturn(Optional.of(gemeindeStammdaten));
		expect(mitteilungService.sendNeueVeranlagungsmitteilung(anyObject())).andReturn(new NeueVeranlagungsMitteilung());
		testProcessingSuccess();
	}

	@Test
	void createsNeueVeranlagungMitteilungWhenZugunstenAntragsteller() {
		//Einkommen sinkt um 1 CHF
		expectEverythingUntilCompare(BigDecimal.valueOf(99999));
		Einstellung einstellung = findEinstellungMinUnterschied();
		expect(einstellung.getValueAsBigDecimal()).andReturn(new BigDecimal(70));
		GemeindeStammdaten gemeindeStammdaten = new GemeindeStammdaten();
		expect(gemeindeService.getGemeindeStammdatenByGemeindeId(anyObject())).andReturn(Optional.of(gemeindeStammdaten));
		expect(mitteilungService.sendNeueVeranlagungsmitteilung(anyObject())).andReturn(new NeueVeranlagungsMitteilung());
		testProcessingSuccess();
	}

	@Test
	void createsNeueVeranalgungMitteilungWhenGesuchMarkiert() {
		//Einkommen bleibt gleich
		expectEverythingUntilCompare(BigDecimal.valueOf(100000));
		// gesuch ist markiert
		kibonAnfrageContext.getGesuch().setMarkiertFuerKontroll(true);
		Einstellung einstellung = findEinstellungMinUnterschied();
		expect(einstellung.getValueAsBigDecimal()).andReturn(new BigDecimal(70));
		GemeindeStammdaten gemeindeStammdaten = new GemeindeStammdaten();
		expect(gemeindeService.getGemeindeStammdatenByGemeindeId(anyObject())).andReturn(Optional.of(gemeindeStammdaten));
		expect(mitteilungService.sendNeueVeranlagungsmitteilung(anyObject())).andReturn(new NeueVeranlagungsMitteilung());
		testProcessingSuccess();
	}

	private void testIgnored(@Nonnull String message) {
		replayAll();

		Processing result = handler.attemptProcessing(gesuch_1GS.getId(), dto);
		assertThat(result, failed(stringContainsInOrder(message)));
		verifyAll();
	}

	private void expectGesuchNotFound() {
		expect(gesuchService.findGesuch(gesuch_1GS.getId())).andReturn(Optional.empty());
	}

	private void expectGesuchFound() {
		expect(gesuchService.findGesuch(gesuch_1GS.getId())).andReturn(Optional.of(gesuch_1GS));
		EntityManager em =createMock(EntityManager.class);
		expect(persistence.getEntityManager()).andReturn(em);
		Session session = createMock(Session.class);
		expect(em.unwrap(Session.class)).andStubReturn(session);
		session.evict(gesuch_1GS);
	}

	private void expectEverythingUntilCompare() {
		expectEverythingUntilCompare(BigDecimal.valueOf(100060));
	}

	private void expectEverythingUntilCompare(BigDecimal einkommenNeu) {
		kibonAnfrageContext.setSteuerdatenAnfrageStatus(SteuerdatenAnfrageStatus.RECHTSKRAEFTIG);
		kibonAnfrageContext.setSteuerdatenResponse(steuerdatenResponse);
		FinanzielleSituationResultateDTO finanzielleSituationResultateDTOOrig = new FinanzielleSituationResultateDTO();
		finanzielleSituationResultateDTOOrig.setMassgebendesEinkVorAbzFamGr(new BigDecimal(100000));
		FinanzielleSituationResultateDTO finanzielleSituationResultateDTONeu = new FinanzielleSituationResultateDTO();
		finanzielleSituationResultateDTONeu.setMassgebendesEinkVorAbzFamGr(einkommenNeu);
		expectGesuchFound();
		expect(finanzielleSituationService.calculateResultate(anyObject())).andReturn(finanzielleSituationResultateDTOOrig);
		expect(kibonAnfrageHandler.handleKibonAnfrage(anyObject(), eq(false))).andReturn(kibonAnfrageContext);
		expect(finanzielleSituationService.calculateResultate(anyObject())).andReturn(finanzielleSituationResultateDTONeu);
	}

	private Einstellung findEinstellungMinUnterschied() {
		Einstellung einstellung = mock(Einstellung.class);
		List<Einstellung> einstellungs = new ArrayList<>();
		einstellungs.add(einstellung);
		expect(einstellungService.findEinstellungen(EinstellungKey.VERANLAGUNG_MIN_UNTERSCHIED_MASSGEBENDESEINK, gesuch_1GS.getGesuchsperiode()))
			.andReturn(einstellungs);
		return einstellung;
	}

	private void testProcessingSuccess() {
		replayAll();
		Processing result = handler.attemptProcessing(gesuch_1GS.getId(), dto);
		assertThat(result.isProcessingSuccess(), is(true));
		verifyAll();
	}
}
