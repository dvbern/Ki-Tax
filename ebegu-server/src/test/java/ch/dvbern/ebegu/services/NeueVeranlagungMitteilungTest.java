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

package ch.dvbern.ebegu.services;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.ejb.EJBAccessException;
import javax.ejb.EJBTransactionRolledbackException;

import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.NeueVeranlagungsMitteilung;
import ch.dvbern.ebegu.entities.SteuerdatenResponse;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.Eingangsart;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.test.util.TestDataInstitutionStammdatenBuilder;
import ch.dvbern.ebegu.testfaelle.Testfall01_WaeltiDagmar;
import ch.dvbern.ebegu.testfaelle.Testfall04_WaltherLaura;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Es gibt schon eine MitteilungServiceBeanTest
 * Diese ist eine leichtere Version mit Mock anstatt Arquillian die betrifft nur die bearbeitung von neue Veranlagung
 * Mitteilungen
 */
@ExtendWith(EasyMockExtension.class)
public class NeueVeranlagungMitteilungTest extends EasyMockSupport {

	private Gesuchsperiode gesuchsperiode;
	private Dossier dossier;
	private Gemeinde gemeinde;
	private NeueVeranlagungsMitteilung neueVeranlagungsMitteilung;

	@SuppressWarnings("InstanceVariableMayNotBeInitialized")
	@Mock
	private GesuchService gesuchService;

	@SuppressWarnings("InstanceVariableMayNotBeInitialized")
	@Mock
	private Authorizer authorizer;

	@SuppressWarnings("InstanceVariableMayNotBeInitialized")
	@Mock
	private FinanzielleSituationService finanzielleSituationService;

	@SuppressWarnings("InstanceVariableMayNotBeInitialized")
	@Mock
	private Persistence persistence;

	@TestSubject
	private final MitteilungServiceBean mitteilungServiceBean = new MitteilungServiceBean();

	@BeforeEach
	public void setUp() {
		gesuchsperiode = TestDataUtil.createGesuchsperiodeXXYY(2020, 2021);
		dossier = TestDataUtil.createDefaultDossier();
		gemeinde = TestDataUtil.createGemeindeParis();
		neueVeranlagungsMitteilung = new NeueVeranlagungsMitteilung();

	}

	@ParameterizedTest
	@EnumSource(value = AntragStatus.class,
		names = { "FREIGEGEBEN", "FREIGABEQUITTUNG" },
		mode = Mode.INCLUDE)
	public void antragNochNichtFreigegeben(@Nonnull AntragStatus antragStatus) {
		SteuerdatenResponse steuerdatenResponse = new SteuerdatenResponse();
		Gesuch gesuch = TestDataUtil.createGesuch(dossier, gesuchsperiode, antragStatus);
		steuerdatenResponse.setKiBonAntragId(gesuch.getId());
		neueVeranlagungsMitteilung.setSteuerdatenResponse(steuerdatenResponse);
		expectGesuchFound(gesuch);
		replayAll();
		testExceptionWithErrorCode(ErrorCodeEnum.ERROR_NOCH_NICHT_FREIGEGEBENE_ANTRAG);
		verifyAll();
	}

	@Test
	public void existingOnlineAntragMutation() {
		SteuerdatenResponse steuerdatenResponse = new SteuerdatenResponse();
		Gesuch gesuch = TestDataUtil.createGesuch(dossier, gesuchsperiode, AntragStatus.VERFUEGEN);
		steuerdatenResponse.setKiBonAntragId(gesuch.getId());
		neueVeranlagungsMitteilung.setSteuerdatenResponse(steuerdatenResponse);
		expectGesuchFound(gesuch);
		expect(gesuchService.getNeustesGesuchFuerGesuch(gesuch)).andThrow(new EJBTransactionRolledbackException(
			"",
			new EJBAccessException()));
		replayAll();
		testExceptionWithErrorCode(ErrorCodeEnum.ERROR_EXISTING_ONLINE_MUTATION);
		verifyAll();
	}

	@Test
	public void gesuchGesperrt() {
		SteuerdatenResponse steuerdatenResponse = new SteuerdatenResponse();
		Gesuch gesuch = TestDataUtil.createGesuch(dossier, gesuchsperiode, AntragStatus.VERFUEGEN);
		gesuch.setGesperrtWegenBeschwerde(true);
		steuerdatenResponse.setKiBonAntragId(gesuch.getId());
		neueVeranlagungsMitteilung.setSteuerdatenResponse(steuerdatenResponse);
		expectGesuchFound(gesuch);
		expect(gesuchService.getNeustesGesuchFuerGesuch(gesuch)).andReturn(Optional.of(gesuch));
		replayAll();
		testExceptionWithErrorCode(ErrorCodeEnum.ERROR_MUTATIONSMELDUNG_FALL_GESPERRT);
		verifyAll();
	}

	@Test
	public void gesuchInStatusVerfuegen() {
		SteuerdatenResponse steuerdatenResponse = new SteuerdatenResponse();
		Gesuch gesuch = TestDataUtil.createGesuch(dossier, gesuchsperiode, AntragStatus.VERFUEGEN);
		steuerdatenResponse.setKiBonAntragId(gesuch.getId());
		neueVeranlagungsMitteilung.setSteuerdatenResponse(steuerdatenResponse);
		expectGesuchFound(gesuch);
		expect(gesuchService.getNeustesGesuchFuerGesuch(gesuch)).andReturn(Optional.of(gesuch));
		replayAll();
		testExceptionWithErrorCode(ErrorCodeEnum.ERROR_MUTATIONSMELDUNG_STATUS_VERFUEGEN);
		verifyAll();
	}

	@Test
	public void neueVeranlaungsMitteilung1GSSteuerresponseGemeinsamRejected() {
		SteuerdatenResponse steuerdatenResponse = new SteuerdatenResponse();
		steuerdatenResponse.setZpvNrAntragsteller(1000001);
		steuerdatenResponse.setZpvNrDossiertraeger(1000001);
		steuerdatenResponse.setZpvNrPartner(1000002);
		Gesuch gesuch = prepareGS1Fall(steuerdatenResponse);
		expectEverythingBisBearbeitung(gesuch);
		replayAll();
		testExceptionWithErrorCode(ErrorCodeEnum.ERROR_FIN_SIT_ALLEIN_NEUE_VERANLAGUNG_GEMEINSAM);
		verifyAll();
	}

	@Test
	public void neueVeranlaungsMitteilung2GSSteuerresponseGemeinsamRejected() {
		SteuerdatenResponse steuerdatenResponse = new SteuerdatenResponse();
		steuerdatenResponse.setZpvNrAntragsteller(1000001);
		steuerdatenResponse.setZpvNrDossiertraeger(1000001);
		Gesuch gesuch = prepareGemeinsamFall(steuerdatenResponse);
		expectEverythingBisBearbeitung(gesuch);
		replayAll();
		testExceptionWithErrorCode(ErrorCodeEnum.ERROR_FIN_SIT_GEMEINSAM_NEUE_VERANLAGUNG_ALLEIN);
		verifyAll();
	}

	@Test
	public void neueVeranlaungsMitteilung1GSOk() {
		SteuerdatenResponse steuerdatenResponse = new SteuerdatenResponse();
		steuerdatenResponse.setZpvNrAntragsteller(1000001);
		steuerdatenResponse.setZpvNrDossiertraeger(1000001);
		Gesuch gesuch = prepareGS1Fall(steuerdatenResponse);
		expectEverythingBisBearbeitung(gesuch);
		Objects.requireNonNull(gesuch.getGesuchsteller1());
		expect(finanzielleSituationService.saveFinanzielleSituation(
			anyObject(),
			anyObject())).andReturn(gesuch.getGesuchsteller1().getFinanzielleSituationContainer());
		expect(persistence.merge(neueVeranlagungsMitteilung)).andReturn(neueVeranlagungsMitteilung);
		replayAll();
		mitteilungServiceBean.neueVeranlagungssmitteilungBearbeiten(neueVeranlagungsMitteilung);
		verifyAll();
	}

	@Test
	public void neueVeranlaungsMitteilung2GSOK() {
		SteuerdatenResponse steuerdatenResponse = new SteuerdatenResponse();
		steuerdatenResponse.setZpvNrDossiertraeger(1000001);
		steuerdatenResponse.setZpvNrPartner(1000002);
		steuerdatenResponse.setZpvNrAntragsteller(1000001);
		Gesuch gesuch = prepareGemeinsamFall(steuerdatenResponse);
		expectEverythingBisBearbeitung(gesuch);
		Objects.requireNonNull(gesuch.getGesuchsteller1());
		Objects.requireNonNull(gesuch.getGesuchsteller2());
		expect(finanzielleSituationService.saveFinanzielleSituation(
			anyObject(),
			anyObject())).andReturn(gesuch.getGesuchsteller1().getFinanzielleSituationContainer());
		expect(finanzielleSituationService.saveFinanzielleSituation(
			anyObject(),
			anyObject())).andReturn(gesuch.getGesuchsteller2().getFinanzielleSituationContainer());
		expect(persistence.merge(neueVeranlagungsMitteilung)).andReturn(neueVeranlagungsMitteilung);
		replayAll();
		mitteilungServiceBean.neueVeranlagungssmitteilungBearbeiten(neueVeranlagungsMitteilung);
		verifyAll();
	}

	private void expectGesuchFound(Gesuch gesuch) {
		expect(gesuchService.findGesuch(gesuch.getId())).andReturn(Optional.of(gesuch));
		authorizer.checkReadAuthorizationMitteilung(neueVeranlagungsMitteilung);
		expectLastCall();
	}

	private void expectEverythingBisBearbeitung(Gesuch gesuch) {
		expectGesuchFound(gesuch);
		expect(gesuchService.getNeustesGesuchFuerGesuch(gesuch)).andReturn(Optional.of(gesuch));
		expect(gesuchService.createGesuch(anyObject())).andReturn(gesuch);
		authorizer.checkWriteAuthorization(anyObject(Gesuch.class));
		expectLastCall();
		authorizer.checkWriteAuthorization(gesuch);
		expectLastCall();
		authorizer.checkReadAuthorizationMitteilung(neueVeranlagungsMitteilung);
		expectLastCall();
	}

	private void testExceptionWithErrorCode(ErrorCodeEnum errorCodeEnum) {
		try {
			mitteilungServiceBean.neueVeranlagungssmitteilungBearbeiten(neueVeranlagungsMitteilung);
		} catch (EbeguRuntimeException e) {
			assertThat(e.getErrorCodeEnum(), is(errorCodeEnum));
		}
	}

	private Gesuch prepareGS1Fall(SteuerdatenResponse steuerdatenResponse) {
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
		Gesuch gesuch = testfall_1GS.fillInGesuch();
		Objects.requireNonNull(gesuch.getGesuchsteller1());
		Objects.requireNonNull(gesuch.getGesuchsteller1().getFinanzielleSituationContainer());
		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().setSteuerdatenZugriff(true);
		gesuch.setEingangsart(Eingangsart.ONLINE);
		gesuch.getDossier().getFall().setBesitzer(new Benutzer());
		gesuch.setStatus(AntragStatus.VERFUEGT);
		Objects.requireNonNull(gesuch.getDossier().getFall().getBesitzer());
		gesuch.getDossier().getFall().getBesitzer().setZpvNummer("1000001");
		Objects.requireNonNull(gesuch.getGesuchsteller1());
		Objects.requireNonNull(gesuch.getGesuchsteller1().getFinanzielleSituationContainer());
		gesuch.getGesuchsteller1()
			.getFinanzielleSituationContainer()
			.getFinanzielleSituationJA()
			.setSteuerdatenResponse(steuerdatenResponse);
		steuerdatenResponse.setKiBonAntragId(gesuch.getId());
		neueVeranlagungsMitteilung.setSteuerdatenResponse(steuerdatenResponse);
		return gesuch;
	}

	private Gesuch prepareGemeinsamFall(SteuerdatenResponse steuerdatenResponse) {
		List<InstitutionStammdaten> institutionStammdatenList = new ArrayList<>();
		institutionStammdatenList.add(TestDataUtil.createInstitutionStammdatenKitaWeissenstein());
		institutionStammdatenList.add(TestDataUtil.createInstitutionStammdatenKitaBruennen());
		Testfall04_WaltherLaura testfall_2GS =
			new Testfall04_WaltherLaura(
				gesuchsperiode,
				false,
				gemeinde,
				new TestDataInstitutionStammdatenBuilder(gesuchsperiode));
		testfall_2GS.createFall();
		testfall_2GS.createGesuch(LocalDate.of(2016, Month.DECEMBER, 12));
		Gesuch gesuch = testfall_2GS.fillInGesuch();
		gesuch.setEingangsart(Eingangsart.ONLINE);
		gesuch.getDossier().getFall().setBesitzer(new Benutzer());
		gesuch.setStatus(AntragStatus.VERFUEGT);
		Objects.requireNonNull(gesuch.getDossier().getFall().getBesitzer());
		gesuch.getDossier().getFall().getBesitzer().setZpvNummer("1000001");
		Objects.requireNonNull(gesuch.getGesuchsteller1());
		Objects.requireNonNull(gesuch.getGesuchsteller1().getFinanzielleSituationContainer());
		gesuch.getGesuchsteller1()
			.getFinanzielleSituationContainer()
			.getFinanzielleSituationJA()
			.setSteuerdatenResponse(steuerdatenResponse);
		gesuch.getGesuchsteller1()
			.getFinanzielleSituationContainer()
			.getFinanzielleSituationJA()
			.setSteuerdatenZugriff(true);
		Objects.requireNonNull(gesuch.getGesuchsteller2());
		Objects.requireNonNull(gesuch.getGesuchsteller2().getFinanzielleSituationContainer());
		gesuch.getGesuchsteller2()
			.getFinanzielleSituationContainer()
			.getFinanzielleSituationJA()
			.setSteuerdatenResponse(steuerdatenResponse);
		gesuch.getGesuchsteller2()
			.getFinanzielleSituationContainer()
			.getFinanzielleSituationJA()
			.setSteuerdatenZugriff(true);
		Objects.requireNonNull(gesuch.getFamiliensituationContainer());
		Objects.requireNonNull(gesuch.getFamiliensituationContainer().getFamiliensituationJA());
		gesuch.getFamiliensituationContainer().getFamiliensituationJA().setGemeinsameSteuererklaerung(true);
		steuerdatenResponse.setKiBonAntragId(gesuch.getId());
		neueVeranlagungsMitteilung.setSteuerdatenResponse(steuerdatenResponse);
		return gesuch;
	}

}
