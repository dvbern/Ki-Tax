/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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
 *
 */

package ch.dvbern.ebegu.gesuch.freigabe;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import ch.dvbern.ebegu.dto.JaxFreigabeDTO;
import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.WizardStepName;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.services.AntragStatusHistoryService;
import ch.dvbern.ebegu.services.Authorizer;
import ch.dvbern.ebegu.services.BetreuungService;
import ch.dvbern.ebegu.services.EinstellungService;
import ch.dvbern.ebegu.services.VerantwortlicheService;
import ch.dvbern.ebegu.services.WizardStepService;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.test.util.TestDataInstitutionStammdatenBuilder;
import ch.dvbern.ebegu.testfaelle.Testfall01_WaeltiDagmar;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(EasyMockExtension.class)
class FreigabeServiceTest extends EasyMockSupport {

	@Mock
	private Persistence persistence;

	@Mock
	private Authorizer authorizer;

	@Mock
	private BetreuungService betreuungService;

	@Mock
	private WizardStepService wizardStepService;

	@Mock
	private AntragStatusHistoryService antragStatusHistoryService;

	@Mock
	private VerantwortlicheService verantwortlicheService;

	@Mock
	private GesuchValidatorService gesuchValidationService;

	@Mock
	private EinstellungService einstellungService;

	@TestSubject
	private FreigabeService testee;

	@ParameterizedTest
	@MethodSource("invalidOnlinefreigabeCases")
	void mustThrowErrorIfOnlineFreigabeAndUserConfirmationIsMissing(JaxFreigabeDTO freigabeDTO) {
		// given
		Mandant mandant = new Mandant();
		mandant.setMandantIdentifier(MandantIdentifier.SCHWYZ);

		Gesuchsperiode gesuchsperiode = TestDataUtil.createGesuchsperiode1718(mandant);
		Gemeinde gemeinde = TestDataUtil.createGemeindeLondon(mandant);

		TestDataInstitutionStammdatenBuilder institution = new TestDataInstitutionStammdatenBuilder(gesuchsperiode);
		var testFall = new Testfall01_WaeltiDagmar(gesuchsperiode, true, gemeinde, institution);
		testFall.createGesuch(gesuchsperiode.getDatumAktiviert());
		testFall.fillInGesuch();

		Gesuch gesuch = testFall.getGesuch();
		expect(persistence.find(Gesuch.class, gesuch.getId())).andReturn(gesuch);
		expect(einstellungService.getEinstellungByMandant(EinstellungKey.GESUCHFREIGABE_ONLINE, gesuchsperiode)).andReturn(
			Optional.of(new Einstellung(EinstellungKey.GESUCHFREIGABE_ONLINE, "true", gesuchsperiode, mandant)));

		replayAll();

		// when
		// then
		assertThrows(EbeguRuntimeException.class, () -> testee.antragFreigeben(gesuch.getId(), freigabeDTO));
		verifyAll();
	}

	static Stream<Arguments> invalidOnlinefreigabeCases() {
		return Stream.of(
			Arguments.of(JaxFreigabeDTO.builder().build()),
			Arguments.of(JaxFreigabeDTO.builder().userConfirmedCorrectness(false).build()));
	}

	@ParameterizedTest
	@MethodSource("invalidGesuchStatusForFreigabe")
	void mustThrowErrorForCertainGesuchStatus(AntragStatus antragStatus) {
		// given
		Mandant mandant = new Mandant();
		mandant.setMandantIdentifier(MandantIdentifier.SCHWYZ);

		Gesuchsperiode gesuchsperiode = TestDataUtil.createGesuchsperiode1718(mandant);
		Gemeinde gemeinde = TestDataUtil.createGemeindeLondon(mandant);

		TestDataInstitutionStammdatenBuilder institution = new TestDataInstitutionStammdatenBuilder(gesuchsperiode);
		var testFall = new Testfall01_WaeltiDagmar(gesuchsperiode, true, gemeinde, institution);
		testFall.createGesuch(gesuchsperiode.getDatumAktiviert());
		testFall.fillInGesuch();

		Gesuch gesuch = testFall.getGesuch();
		gesuch.setStatus(antragStatus);
		expect(persistence.find(Gesuch.class, gesuch.getId())).andReturn(gesuch);
		expect(einstellungService.getEinstellungByMandant(EinstellungKey.GESUCHFREIGABE_ONLINE, gesuchsperiode)).andReturn(
			Optional.empty());
		gesuchValidationService.validateGesuchComplete(gesuch);

		var freigabeDTO = JaxFreigabeDTO.builder().build();

		replayAll();

		// when
		// then
		assertThrows(EbeguRuntimeException.class, () -> testee.antragFreigeben(gesuch.getId(), freigabeDTO));
		verifyAll();
	}

	static Stream<Arguments> invalidGesuchStatusForFreigabe() {
		var set = EnumSet.allOf(AntragStatus.class);
		set.removeAll(EnumSet.of(AntragStatus.FREIGABEQUITTUNG, AntragStatus.IN_BEARBEITUNG_SOZIALDIENST, AntragStatus.IN_BEARBEITUNG_GS));
		return set.stream().map(Arguments::of);
	}

	@Test
	void mustDoFreigabeStuff() {
		// given
		Mandant mandant = new Mandant();
		mandant.setMandantIdentifier(MandantIdentifier.SCHWYZ);
		Gesuchsperiode gesuchsperiode = TestDataUtil.createGesuchsperiode1718(mandant);
		Gemeinde gemeinde = TestDataUtil.createGemeindeLondon(mandant);
		TestDataInstitutionStammdatenBuilder institution = new TestDataInstitutionStammdatenBuilder(gesuchsperiode);
		var testFall = new Testfall01_WaeltiDagmar(gesuchsperiode, true, gemeinde, institution);
		testFall.createGesuch(gesuchsperiode.getDatumAktiviert());
		testFall.fillInGesuch();
		Gesuch gesuch = testFall.getGesuch();
		gesuch.getKindContainers().forEach(kindContainer -> kindContainer.setKindNummer(testFall.getFall().getNextNumberKind()));		;
		var kind = Optional.ofNullable(gesuch.extractKindFromKindNumber(1)).orElseThrow();
		var anmeldungTagesschuleWithModules =
			TestDataUtil.createAnmeldungTagesschuleWithModules(kind, gesuchsperiode);
		kind.setAnmeldungenTagesschule(Set.of(anmeldungTagesschuleWithModules));

		var freigabeDTO = JaxFreigabeDTO.builder().userConfirmedCorrectness(true).build();

		expect(persistence.find(Gesuch.class, gesuch.getId())).andReturn(gesuch);
		expect(einstellungService.getEinstellungByMandant(EinstellungKey.GESUCHFREIGABE_ONLINE, gesuchsperiode)).andReturn(
			Optional.of(new Einstellung(EinstellungKey.GESUCHFREIGABE_ONLINE, "true", gesuchsperiode, mandant)));
		gesuchValidationService.validateGesuchComplete(gesuch);
		authorizer.checkWriteAuthorization(gesuch);
		wizardStepService.setWizardStepOkay(gesuch.getId(), WizardStepName.FREIGABE);
		verantwortlicheService.updateVerantwortliche(gesuch.getId(), freigabeDTO, gesuch);
		expect(persistence.merge(gesuch)).andReturn(gesuch);
		expect(antragStatusHistoryService.saveStatusChange(gesuch, null)).andReturn(null);
		betreuungService.fireAnmeldungTagesschuleAddedEvent(anmeldungTagesschuleWithModules);

		replayAll();

		// when
		testee.antragFreigeben(gesuch.getId(), freigabeDTO);

		// then
		verifyAll();
	}
}
