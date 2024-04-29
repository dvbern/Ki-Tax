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

package ch.dvbern.ebegu.services;

import ch.dvbern.ebegu.dto.JaxFreigabeDTO;
import ch.dvbern.ebegu.entities.*;
import ch.dvbern.ebegu.enums.Eingangsart;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.test.util.TestDataInstitutionStammdatenBuilder;
import ch.dvbern.ebegu.testfaelle.Testfall01_WaeltiDagmar;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(EasyMockExtension.class)
class VerantwortlicheServiceTest extends EasyMockSupport {

	public static final String DOSSIER_ID = "dossier";
	@Mock
	private BenutzerService benutzerService;

	@Mock
	private DossierService dossierService;

	@Mock
	private GemeindeService gemeindeService;

	@TestSubject
	private VerantwortlicheService testee;

	@Test
	void testOnlyIfNotSetFalsePersistFalse() {
		// given
		var gesuch = getGesuch();

		var benutzerBg = TestDataUtil.createBenutzerSCH();
		benutzerBg.setRole(UserRole.ADMIN_GEMEINDE);
		var benutzerTs = TestDataUtil.createBenutzerSCH();

		Dossier dossierMock = mock(Dossier.class);
		expect(dossierMock.getVerantwortlicherBG()).andReturn(null);
		dossierMock.setVerantwortlicherBG(benutzerBg);
		expect(dossierMock.getVerantwortlicherTS()).andReturn(null);
		dossierMock.setVerantwortlicherTS(benutzerTs);
		gesuch.setDossier(dossierMock);

		replayAll();

		// when
		testee.setVerantwortliche(benutzerBg, benutzerTs, gesuch, false, false);

		// then
		verifyAll();
	}

	private static Gesuch getGesuch() {
		Mandant mandant = new Mandant();
		mandant.setMandantIdentifier(MandantIdentifier.SCHWYZ);

		Gesuchsperiode gesuchsperiode = TestDataUtil.createGesuchsperiode1718(mandant);
		Gemeinde gemeinde = TestDataUtil.createGemeindeLondon(mandant);

		TestDataInstitutionStammdatenBuilder institution = new TestDataInstitutionStammdatenBuilder(gesuchsperiode);
		var testFall = new Testfall01_WaeltiDagmar(gesuchsperiode, true, gemeinde, institution);
		testFall.createGesuch(gesuchsperiode.getDatumAktiviert());
		testFall.fillInGesuch();
		var gesuch = testFall.getGesuch();

		gesuch.getKindContainers().forEach(kindContainer -> kindContainer.setKindNummer(testFall.getFall().getNextNumberKind()));

		var kind = Optional.ofNullable(gesuch.extractKindFromKindNumber(1)).orElseThrow();
		var anmeldungTagesschuleWithModules = TestDataUtil.createAnmeldungTagesschuleWithModules(kind, gesuchsperiode);
		kind.setAnmeldungenTagesschule(Set.of(anmeldungTagesschuleWithModules));
		return gesuch;
	}

	@Test
	void testOnlyIfNotSetFalsePersistTrue() {
		// given
		var gesuch = getGesuch();

		var benutzerBg = TestDataUtil.createBenutzerSCH();
		benutzerBg.setRole(UserRole.ADMIN_GEMEINDE);
		var benutzerTs = TestDataUtil.createBenutzerSCH();

		Dossier dossierMock = mock(Dossier.class);
		expect(dossierMock.getId()).andReturn(DOSSIER_ID).times(2);
		expect(dossierMock.getVerantwortlicherBG()).andReturn(null);
		dossierMock.setVerantwortlicherBG(benutzerBg);
		expect(dossierMock.getVerantwortlicherTS()).andReturn(null);
		dossierMock.setVerantwortlicherTS(benutzerTs);
		gesuch.setDossier(dossierMock);

		expect(dossierService.setVerantwortlicherBG(DOSSIER_ID, benutzerBg)).andReturn(null);
		expect(dossierService.setVerantwortlicherTS(DOSSIER_ID, benutzerTs)).andReturn(null);

		replayAll();

		// when
		testee.setVerantwortliche(benutzerBg, benutzerTs, gesuch, false, true);

		// then
		verifyAll();
	}

	@Test
	void testOnlyIfNotSetTruePersistTrue() {
		// given
		var gesuch = getGesuch();

		var benutzerBg = TestDataUtil.createBenutzerSCH();
		benutzerBg.setRole(UserRole.ADMIN_GEMEINDE);
		var benutzerTs = TestDataUtil.createBenutzerSCH();

		Dossier dossierMock = mock(Dossier.class);
		expect(dossierMock.getVerantwortlicherBG()).andReturn(benutzerBg);
		expect(dossierMock.getVerantwortlicherTS()).andReturn(benutzerTs);
		gesuch.setDossier(dossierMock);

		replayAll();

		// when
		testee.setVerantwortliche(benutzerBg, benutzerTs, gesuch, true, true);

		// then
		verifyAll();
	}

	@Test
	void testOnlyIfNotSetTruePersistFalse() {
		// given
		var gesuch = getGesuch();

		var benutzerBg = TestDataUtil.createBenutzerSCH();
		benutzerBg.setRole(UserRole.ADMIN_GEMEINDE);
		var benutzerTs = TestDataUtil.createBenutzerSCH();

		Dossier dossierMock = mock(Dossier.class);
		expect(dossierMock.getVerantwortlicherBG()).andReturn(benutzerBg);
		expect(dossierMock.getVerantwortlicherTS()).andReturn(benutzerTs);
		gesuch.setDossier(dossierMock);

		replayAll();

		// when
		testee.setVerantwortliche(benutzerBg, benutzerTs, gesuch, true, false);

		// then
		verifyAll();
	}

	@Test
	void mustNotDoAnythingIfBenutzerNull() {
		// given
		var gesuch = getGesuch();

		Dossier dossierMock = mock(Dossier.class);
		gesuch.setDossier(dossierMock);

		replayAll();

		// when
		testee.setVerantwortliche(null, null, gesuch, true, false);

		// then
		verifyAll();
	}

	@Test
	void mustNotDoAnythingIfBenutzerRolesWrong() {
		// given
		var gesuch = getGesuch();

		var benutzer1 = TestDataUtil.createDefaultBenutzer();
		benutzer1.setRole(UserRole.GESUCHSTELLER);
		var benutzer2 = TestDataUtil.createDefaultBenutzer();
		benutzer2.setRole(UserRole.GESUCHSTELLER);

		Dossier dossierMock = mock(Dossier.class);
		gesuch.setDossier(dossierMock);

		replayAll();

		// when
		testee.setVerantwortliche(benutzer1, benutzer2, gesuch, true, false);

		// then
		verifyAll();
	}

	@Test
	void testUpdateVerantwortlicheGesuchNoUsers() {
		// given
		var gesuch = getGesuch();

		var benutzer1 = TestDataUtil.createDefaultBenutzer();
		benutzer1.setRole(UserRole.GESUCHSTELLER);
		var benutzer2 = TestDataUtil.createDefaultBenutzer();
		benutzer2.setRole(UserRole.GESUCHSTELLER);

		Dossier dossierMock = mock(Dossier.class);
		gesuch.setDossier(dossierMock);
		var freigabeDTO = JaxFreigabeDTO.builder().build();

		replayAll();

		// when
		testee.updateVerantwortliche(gesuch.getId(), freigabeDTO, gesuch);

		// then
		verifyAll();
	}

	@Test
	void testUpdateVerantwortlicheGesuchWithUsersIfNotMutation() {
		// given
		var gesuch = getGesuch();

		var benutzerBg = TestDataUtil.createBenutzerSCH();
		benutzerBg.setRole(UserRole.ADMIN_GEMEINDE);
		var benutzerTs = TestDataUtil.createBenutzerSCH();

		Dossier dossierMock = mock(Dossier.class);
		expect(dossierMock.getGemeinde()).andReturn(gesuch.getDossier().getGemeinde()).times(2);
		expect(dossierMock.getVerantwortlicherBG()).andReturn(benutzerBg);
		dossierMock.setVerantwortlicherBG(benutzerBg);
		expect(dossierMock.getVerantwortlicherTS()).andReturn(benutzerTs);
		dossierMock.setVerantwortlicherTS(benutzerTs);
		gesuch.setDossier(dossierMock);
		var freigabeDTO =
			JaxFreigabeDTO.builder().usernameSCH(benutzerTs.getUsername()).usernameJA(benutzerBg.getUsername()).build();

		expect(benutzerService.findBenutzer(
			benutzerBg.getUsername(),
			gesuch.getGesuchsperiode().getMandant())).andReturn(Optional.of(benutzerBg));
		expect(benutzerService.findBenutzer(
			benutzerTs.getUsername(),
			gesuch.getGesuchsperiode().getMandant())).andReturn(Optional.of(benutzerTs));

		replayAll();

		// when
		testee.updateVerantwortliche(gesuch.getId(), freigabeDTO, gesuch);

		// then
		verifyAll();
	}

	@ParameterizedTest
	@MethodSource("updateVerantwortlicheNeededSource")
	void testUpdateVerantwortlicheNeeded(
		Eingangsart eingangsart, boolean isSchulamtAnmeldungAusgeloest, boolean isNew, boolean expectedResult) {
		assertThat(
			testee.updateVerantwortlicheNeeded(eingangsart, isSchulamtAnmeldungAusgeloest, isNew),
			Matchers.is(expectedResult));
	}

	public static Stream<Arguments> updateVerantwortlicheNeededSource() {
		return Stream.of(
			Arguments.of(Eingangsart.ONLINE, false, true, false),
			Arguments.of(Eingangsart.ONLINE, true, true, true),
			Arguments.of(Eingangsart.PAPIER, true, true, true),
			Arguments.of(Eingangsart.ONLINE, true, false, false)
		);
	}

}
