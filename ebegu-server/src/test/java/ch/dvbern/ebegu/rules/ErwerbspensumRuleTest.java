/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.rules;

import java.time.LocalDate;
import java.time.Month;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.ErwerbspensumContainer;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnittBemerkung;
import ch.dvbern.ebegu.enums.AntragTyp;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.enums.Taetigkeit;
import ch.dvbern.ebegu.rechner.AbstractBGRechnerTest;
import ch.dvbern.ebegu.test.TestDataUtil;
import org.junit.Test;

import static ch.dvbern.ebegu.util.Constants.ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests für ErwerbspensumRule
 */
public class ErwerbspensumRuleTest extends AbstractBGRechnerTest {

	@Test
	public void testKeinErwerbspensum() {
		Betreuung betreuung = createGesuch(true);

		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(0, result.get(0).getAnspruchberechtigtesPensum());
		final List<VerfuegungZeitabschnittBemerkung> bemerkungen = result.get(0).getVerfuegungZeitabschnittBemerkungList();
		assertNotNull(bemerkungen);
		assertFalse(bemerkungen.isEmpty());
	}

	@Test
	public void testNormalfallZweiGesuchsteller() {
		Betreuung betreuung = createGesuch(true);
		Gesuch gesuch = betreuung.extractGesuch();

		assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil
			.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 100));
		assertNotNull(gesuch.getGesuchsteller2());
		gesuch.getGesuchsteller2().addErwerbspensumContainer(TestDataUtil
			.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 40));

		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);
		assertNotNull(result);
		assertEquals(1, result.size());
		VerfuegungZeitabschnitt verfuegungZeitabschnitt = result.get(0);
		assertEquals(40 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, verfuegungZeitabschnitt.getAnspruchberechtigtesPensum());
		assertNotNull(verfuegungZeitabschnitt.getBemerkungenDTOList());
		assertEquals(2, verfuegungZeitabschnitt.getBemerkungenDTOList().uniqueSize());
		assertTrue(verfuegungZeitabschnitt.getBemerkungenDTOList().containsMsgKey(MsgKey.ERWERBSPENSUM_ANSPRUCH));
		assertTrue(verfuegungZeitabschnitt.getBemerkungenDTOList().containsMsgKey(MsgKey.VERFUEGUNG_MIT_ANSPRUCH));
	}

	@Test
	public void testNotAngebotJugendamtKleinkind() {
		Betreuung betreuung = createGesuch(true);
		AnmeldungTagesschule anmeldung = TestDataUtil.createAnmeldungTagesschuleWithModules(betreuung.getKind(), betreuung.extractGesuchsperiode());
		anmeldung.getInstitutionStammdaten().setBetreuungsangebotTyp(BetreuungsangebotTyp.TAGESSCHULE);
		Gesuch gesuch = betreuung.extractGesuch();

		assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil
			.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 100));
		assertNotNull(gesuch.getGesuchsteller2());
		gesuch.getGesuchsteller2().addErwerbspensumContainer(TestDataUtil
			.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 40));

		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(anmeldung);
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(100, result.get(0).getAnspruchberechtigtesPensum());
		List<VerfuegungZeitabschnittBemerkung> bemekungen = result.get(0).getVerfuegungZeitabschnittBemerkungList();
		assertNotNull(bemekungen);
		assertEquals(1, bemekungen.size());
		assertEquals("Betreuungsangebot Schulamt", bemekungen.get(0).getBemerkung());
	}

	@Test
	public void testNormalfallEinGesuchsteller() {
		Betreuung betreuung = createGesuch(false);
		Gesuch gesuch = betreuung.extractGesuch();

		assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil
			.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 60));

		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);
		assertNotNull(result);
		assertEquals(1, result.size());
		VerfuegungZeitabschnitt verfuegungZeitabschnitt = result.get(0);
		assertEquals(60 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, verfuegungZeitabschnitt.getAnspruchberechtigtesPensum());
		assertNotNull(verfuegungZeitabschnitt.getBemerkungenDTOList());
		assertEquals(2, verfuegungZeitabschnitt.getBemerkungenDTOList().uniqueSize());
		assertTrue(verfuegungZeitabschnitt.getBemerkungenDTOList().containsMsgKey(MsgKey.ERWERBSPENSUM_ANSPRUCH));
		assertTrue(verfuegungZeitabschnitt.getBemerkungenDTOList().containsMsgKey(MsgKey.VERFUEGUNG_MIT_ANSPRUCH));
	}

	@Test
	public void testNurEinErwerbspensumBeiZweiGesuchstellern() {
		Betreuung betreuung = createGesuch(true);
		Gesuch gesuch = betreuung.extractGesuch();

		assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil
			.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 80));

		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(0, result.get(0).getAnspruchberechtigtesPensum());
		final List<VerfuegungZeitabschnittBemerkung> bemerkungen = result.get(0).getVerfuegungZeitabschnittBemerkungList();
		assertNotNull(bemerkungen);
		assertFalse(bemerkungen.isEmpty());
	}

	@Test
	public void testMehrAls100ProzentBeiEinemGesuchsteller() {
		Betreuung betreuung = createGesuch(false);
		Gesuch gesuch = betreuung.extractGesuch();

		assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil
			.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 120));

		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(100, result.get(0).getAnspruchberechtigtesPensum());
		final List<VerfuegungZeitabschnittBemerkung> bemerkungen = result.get(0).getVerfuegungZeitabschnittBemerkungList();
		assertNotNull(bemerkungen);
		assertFalse(bemerkungen.isEmpty());
	}

	@Test
	public void testMehrAls100ProzentBeiBeidenGesuchstellern() {
		Betreuung betreuung = createGesuch(true);
		Gesuch gesuch = betreuung.extractGesuch();

		assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil
			.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 110));
		assertNotNull(gesuch.getGesuchsteller2());
		gesuch.getGesuchsteller2().addErwerbspensumContainer(TestDataUtil
			.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 110));

		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(100, result.get(0).getAnspruchberechtigtesPensum());
		assertBemerkungenContains(result.get(0).getVerfuegungZeitabschnittBemerkungList(), "Das totale Beschäftigungspensum pro Antragsteller/in kann 100% nicht übertreffen.");
	}

	@Test
	public void testFrom1GSTo2GSRechtzeitigEingereicht() {
		Betreuung betreuung = createGesuch(true);

		Gesuch gesuch = betreuung.extractGesuch();
		gesuch.setTyp(AntragTyp.MUTATION);
		gesuch.setEingangsdatum(LocalDate.of(TestDataUtil.PERIODE_JAHR_0, Month.DECEMBER, 26));

		from1GSTo2GS(betreuung, gesuch);

	}

	@Test
	public void testFrom1GSTo2GSSpaetEingereichtAberNiedrigerWert() {
		Betreuung betreuung = createGesuch(true);

		Gesuch gesuch = betreuung.extractGesuch();
		gesuch.setTyp(AntragTyp.MUTATION);
		gesuch.setEingangsdatum(LocalDate.of(TestDataUtil.PERIODE_JAHR_1, Month.MAY, 26));

		from1GSTo2GS(betreuung, gesuch);
	}

	private void from1GSTo2GS(Betreuung betreuung, Gesuch gesuch) {
		assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil
			.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 90));
		assertNotNull(gesuch.getGesuchsteller2());
		gesuch.getGesuchsteller2().addErwerbspensumContainer(TestDataUtil
			.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 80));

		assertNotNull(gesuch.getFamiliensituationContainer());
		gesuch.getFamiliensituationContainer().setFamiliensituationErstgesuch(new Familiensituation());

		final Familiensituation familiensituationErstgesuch = gesuch.extractFamiliensituationErstgesuch();
		assertNotNull(familiensituationErstgesuch);
		familiensituationErstgesuch.setFamilienstatus(EnumFamilienstatus.ALLEINERZIEHEND);

		final Familiensituation extractedFamiliensituation = gesuch.extractFamiliensituation();
		assertNotNull(extractedFamiliensituation);
		extractedFamiliensituation.setFamilienstatus(EnumFamilienstatus.VERHEIRATET);
		extractedFamiliensituation.setAenderungPer(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.MARCH, 26));

		betreuung.initVorgaengerVerfuegungen(null, null);
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculateInklAllgemeineRegeln(betreuung);


		assertNotNull(result);
		assertEquals(12, result.size());
		int i = 0;

		int anspruchAlleine = 100;
		int anspruchZuZweit = 90; // Zusammen 170% Pensum -> Anspruch 90%

		// Vor der Heirat ist das Erwerbspensum zu klein, als dass ein Anspruch resultieren könnte.
		assertZeitabschnitt(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.AUGUST, 1), 0, anspruchAlleine, 0);
		assertZeitabschnitt(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.SEPTEMBER, 1), 0, anspruchAlleine, 0);
		assertZeitabschnitt(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.OCTOBER, 1), 0, anspruchAlleine, 0);
		assertZeitabschnitt(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.NOVEMBER, 1), 0, anspruchAlleine, 0);
		assertZeitabschnitt(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.DECEMBER, 1), 0, anspruchAlleine, 0);
		assertZeitabschnitt(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.JANUARY, 1), 0, anspruchAlleine, 0);
		assertZeitabschnitt(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.FEBRUARY, 1), 0, anspruchAlleine, 0);
		assertZeitabschnitt(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.MARCH, 1), 0, anspruchAlleine, 0);
		// Heirat am 26.03.
		assertZeitabschnitt(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.APRIL, 1), 0, anspruchZuZweit, 0);
		assertZeitabschnitt(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.MAY, 1), 0, anspruchZuZweit, 0);
		assertZeitabschnitt(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.JUNE, 1), 0, anspruchZuZweit, 0);
		assertZeitabschnitt(result.get(i), LocalDate.of(BASISJAHR_PLUS_2, Month.JULY, 1), 0, anspruchZuZweit, 0);
	}

	@Test
	public void testFrom2GSTo1GSRechtzeitigEingereicht() {
		Betreuung betreuung = createGesuch(true);

		Gesuch gesuch = betreuung.extractGesuch();
		gesuch.setTyp(AntragTyp.MUTATION);
		gesuch.setEingangsdatum(LocalDate.of(2016, Month.DECEMBER, 26));

		assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil
			.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 90));
		assertNotNull(gesuch.getGesuchsteller2());
		gesuch.getGesuchsteller2().addErwerbspensumContainer(TestDataUtil
			.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 70));

		assertNotNull(gesuch.getFamiliensituationContainer());
		gesuch.getFamiliensituationContainer().setFamiliensituationErstgesuch(new Familiensituation());

		final Familiensituation familiensituationErstgesuch = gesuch.extractFamiliensituationErstgesuch();
		assertNotNull(familiensituationErstgesuch);
		familiensituationErstgesuch.setFamilienstatus(EnumFamilienstatus.VERHEIRATET);

		final Familiensituation extractedFamiliensituation = gesuch.extractFamiliensituation();
		assertNotNull(extractedFamiliensituation);
		extractedFamiliensituation.setFamilienstatus(EnumFamilienstatus.ALLEINERZIEHEND);
		extractedFamiliensituation.setAenderungPer(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.MARCH, 26));

		betreuung.initVorgaengerVerfuegungen(null, null);
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculateInklAllgemeineRegeln(betreuung);
		assertNotNull(result);
		assertEquals(12, result.size());
		int i = 0;

		int anspruchZuZweit = 60 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS;
		int anspruchAlleine = 100;

		// Vor der Scheidung haben sie zusammen 160% -> Anspruch 80%
		assertZeitabschnitt(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.AUGUST, 1), 0, anspruchZuZweit, 0);
		assertZeitabschnitt(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.SEPTEMBER, 1), 0, anspruchZuZweit, 0);
		assertZeitabschnitt(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.OCTOBER, 1), 0, anspruchZuZweit, 0);
		assertZeitabschnitt(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.NOVEMBER, 1), 0, anspruchZuZweit, 0);
		assertZeitabschnitt(result.get(i++), LocalDate.of(BASISJAHR_PLUS_1, Month.DECEMBER, 1), 0, anspruchZuZweit, 0);
		assertZeitabschnitt(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.JANUARY, 1), 0, anspruchZuZweit, 0);
		assertZeitabschnitt(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.FEBRUARY, 1), 0, anspruchZuZweit, 0);
		assertZeitabschnitt(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.MARCH, 1), 0, anspruchZuZweit, 0);
		// Scheidung am 26.03.
		assertZeitabschnitt(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.APRIL, 1), 0, anspruchAlleine, 0);
		assertZeitabschnitt(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.MAY, 1), 0, anspruchAlleine, 0);
		assertZeitabschnitt(result.get(i++), LocalDate.of(BASISJAHR_PLUS_2, Month.JUNE, 1), 0, anspruchAlleine, 0);
		assertZeitabschnitt(result.get(i), LocalDate.of(BASISJAHR_PLUS_2, Month.JULY, 1), 0, anspruchAlleine, 0);
	}

	/**
	 * das Pensum muss wie folgt abgerundet werden:
	 * X0 - X2 = X0
	 * X3 - X7 = Y0, wo Y=X+1
	 */
	@Test
	public void testRoundToFives() {
		Betreuung betreuung = createGesuch(false);
		Gesuch gesuch = betreuung.extractGesuch();

		assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().setErwerbspensenContainers(new HashSet<>());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil
			.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, -1));
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);
		assertEquals(0, result.get(0).getAnspruchberechtigtesPensum());

		gesuch.getGesuchsteller1().setErwerbspensenContainers(new HashSet<>());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil
			.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 0));
		List<VerfuegungZeitabschnitt> result2 = EbeguRuleTestsHelper.calculate(betreuung);
		assertEquals(0, result2.get(0).getAnspruchberechtigtesPensum());

		gesuch.getGesuchsteller1().setErwerbspensenContainers(new HashSet<>());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil
			.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 1));
		List<VerfuegungZeitabschnitt> result3 = EbeguRuleTestsHelper.calculate(betreuung);
		assertEquals(0, result3.get(0).getAnspruchberechtigtesPensum());

		gesuch.getGesuchsteller1().setErwerbspensenContainers(new HashSet<>());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil
			.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 50));
		List<VerfuegungZeitabschnitt> result4 = EbeguRuleTestsHelper.calculate(betreuung);
		assertEquals(50 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, result4.get(0).getAnspruchberechtigtesPensum());

		gesuch.getGesuchsteller1().setErwerbspensenContainers(new HashSet<>());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil
			.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 51));
		List<VerfuegungZeitabschnitt> result5 = EbeguRuleTestsHelper.calculate(betreuung);
		assertEquals(50 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, result5.get(0).getAnspruchberechtigtesPensum());

		gesuch.getGesuchsteller1().setErwerbspensenContainers(new HashSet<>());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil
			.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 54));
		List<VerfuegungZeitabschnitt> result6 = EbeguRuleTestsHelper.calculate(betreuung);
		assertEquals(55 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, result6.get(0).getAnspruchberechtigtesPensum());

		gesuch.getGesuchsteller1().setErwerbspensenContainers(new HashSet<>());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil
			.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 55));
		List<VerfuegungZeitabschnitt> result7 = EbeguRuleTestsHelper.calculate(betreuung);
		assertEquals(55 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, result7.get(0).getAnspruchberechtigtesPensum());

		//mit zuschlag
		gesuch.getGesuchsteller1().setErwerbspensenContainers(new HashSet<>());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil
			.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 55));
		List<VerfuegungZeitabschnitt> result8 = EbeguRuleTestsHelper.calculate(betreuung);
		assertEquals(55 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, result8.get(0).getAnspruchberechtigtesPensum());

		gesuch.getGesuchsteller1().setErwerbspensenContainers(new HashSet<>());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil
			.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 59));
		List<VerfuegungZeitabschnitt> result9 = EbeguRuleTestsHelper.calculate(betreuung);
		assertEquals(60 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, result9.get(0).getAnspruchberechtigtesPensum());

		gesuch.getGesuchsteller1().setErwerbspensenContainers(new HashSet<>());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil
			.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 99));
		List<VerfuegungZeitabschnitt> result10 = EbeguRuleTestsHelper.calculate(betreuung);
		assertEquals(100, result10.get(0).getAnspruchberechtigtesPensum());

		gesuch.getGesuchsteller1().setErwerbspensenContainers(new HashSet<>());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil
			.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 100));
		List<VerfuegungZeitabschnitt> result11 = EbeguRuleTestsHelper.calculate(betreuung);
		assertEquals(100, result11.get(0).getAnspruchberechtigtesPensum());

		gesuch.getGesuchsteller1().setErwerbspensenContainers(new HashSet<>());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil
			.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 101));
		List<VerfuegungZeitabschnitt> result12 = EbeguRuleTestsHelper.calculate(betreuung);
		assertEquals(100, result12.get(0).getAnspruchberechtigtesPensum());

	}

	@Test
	public void testZweiGesuchstellerAllMax() {
		Betreuung betreuung = createGesuch(true);
		Gesuch gesuch = betreuung.extractGesuch();

		assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil
			.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 115));
		assertNotNull(gesuch.getGesuchsteller2());
		gesuch.getGesuchsteller2().addErwerbspensumContainer(TestDataUtil
			.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 110));

		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(100, result.get(0).getAnspruchberechtigtesPensum());
		assertBemerkungenContains(result.get(0).getVerfuegungZeitabschnittBemerkungList(), "Das totale Beschäftigungspensum pro Antragsteller/in kann 100% nicht übertreffen.");
	}

	@Test
	public void testMinimaleErwerbspensen() {
		Betreuung betreuung = createGesuch(false);
		Gesuch gesuch = betreuung.extractGesuch();

		assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil
			.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 15));

		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);
		assertNotNull(result);
		assertEquals(1, result.size());
		final VerfuegungZeitabschnitt verfuegungZeitabschnitt = result.get(0);
		assertNotNull(verfuegungZeitabschnitt.getBgCalculationInputAsiv().getErwerbspensumGS1());
		assertEquals(15, verfuegungZeitabschnitt.getBgCalculationInputAsiv().getErwerbspensumGS1().intValue());
		assertEquals(0, verfuegungZeitabschnitt.getAnspruchberechtigtesPensum());
		assertFalse(verfuegungZeitabschnitt.getBgCalculationInputAsiv().isBezahltVollkosten());
		assertNotNull(verfuegungZeitabschnitt.getVerfuegungZeitabschnittBemerkungList());
		assertFalse(verfuegungZeitabschnitt.getVerfuegungZeitabschnittBemerkungList().isEmpty());
	}

	@Test
	public void testFreiwilligenarbeitAllePensenUeberMinimum() {
		// Beide Pensen sind einzeln schon gueltig. Gemeinde=Asiv+Gemeinde, Anspruch jeweils +20
		assertBerechnungenMitFreiwilligenarbeit(40, 20, 60, 80);
	}

	@Test
	public void testFreiwilligenarbeitTotalUnterMinimum() {
		// Beide Pensen sind einzeln zu tief, zusammen auch. Anspruch Asiv und Gemeinde 0
		assertBerechnungenMitFreiwilligenarbeit(5, 5, 0, 0);
	}

	@Test
	public void testFreiwilligenarbeitEinzelnUnterMinimum() {
		// Beide Pensen sind einzeln zu tief, zusammen aber wir der Anspruch erreicht. Anspruch Asiv=0, Gemeinde=Asiv+Gemeinde+20
		assertBerechnungenMitFreiwilligenarbeit(5, 15, 0, 40);
	}

	@Test
	public void testFreiwilligenarbeitAsivUnterMinimum() {
		// Pensum Asiv unter Minimum, zusammen mit Pensum Gemeinde wird es gueltig: Anspruch Asiv=0, Anspruch Gemeinde = Asiv+Gemeinde+20
		assertBerechnungenMitFreiwilligenarbeit(5, 20, 0, 45);
	}

	@Test
	public void testFreiwilligenarbeitGemeindeUnterMinimum() {
		// Pensum Gemeinde alleine unter Minimum -> keine speziellen Auswirkungen
		assertBerechnungenMitFreiwilligenarbeit(20, 5, 40, 45);
	}

	private void assertBerechnungenMitFreiwilligenarbeit(int pensumAngestellt, int pensumFreiwillig, int erwarteterAnspruchAsiv, int erwarteterAnspruchGemeinde) {
		Betreuung betreuung = createGesuch(false);
		Gesuch gesuch = betreuung.extractGesuch();

		assertNotNull(gesuch.getGesuchsteller1());
		ErwerbspensumContainer ewpAngestellt = TestDataUtil
			.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, pensumAngestellt);
		assertNotNull(ewpAngestellt.getErwerbspensumJA());
		ewpAngestellt.getErwerbspensumJA().setTaetigkeit(Taetigkeit.ANGESTELLT);
		gesuch.getGesuchsteller1().addErwerbspensumContainer(ewpAngestellt);

		ErwerbspensumContainer ewpFreiwilligenarbeit = TestDataUtil
			.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, pensumFreiwillig);
		assertNotNull(ewpFreiwilligenarbeit.getErwerbspensumJA());
		ewpFreiwilligenarbeit.getErwerbspensumJA().setTaetigkeit(Taetigkeit.FREIWILLIGENARBEIT);
		gesuch.getGesuchsteller1().addErwerbspensumContainer(ewpFreiwilligenarbeit);

		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung, EbeguRuleTestsHelper.getEinstellungenRulesParis(gesuch.getGesuchsperiode()));
		assertNotNull(result);
		assertEquals(1, result.size());
		final VerfuegungZeitabschnitt verfuegungZeitabschnitt = result.get(0);
		BGCalculationInput inputAsiv = verfuegungZeitabschnitt.getBgCalculationInputAsiv();
		BGCalculationInput inputGemeinde = verfuegungZeitabschnitt.getBgCalculationInputGemeinde();
		assertNotNull(inputAsiv.getErwerbspensumGS1());
		assertNotNull(inputGemeinde.getErwerbspensumGS1());
		assertEquals(pensumAngestellt, inputAsiv.getErwerbspensumGS1().intValue());
		assertEquals(erwarteterAnspruchAsiv, inputAsiv.getAnspruchspensumProzent());
		assertEquals(pensumAngestellt+pensumFreiwillig, inputGemeinde.getErwerbspensumGS1().intValue());
		assertEquals(erwarteterAnspruchGemeinde, inputGemeinde.getAnspruchspensumProzent());
	}

	private Betreuung createGesuch(final boolean gs2) {
		final Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(gs2);
		final Gesuch gesuch = betreuung.extractGesuch();

		TestDataUtil.createDefaultAdressenForGS(gesuch, gs2);

		return betreuung;
	}

	private void assertBemerkungenContains(List<VerfuegungZeitabschnittBemerkung> bemerkungenList, String bemerkung) {
		assertNotNull(bemerkungenList);
		assertNotNull(bemerkung);
		assertFalse(bemerkungenList.isEmpty());
		final List<String> bemerkungenStringList = bemerkungenList.stream()
			.map(VerfuegungZeitabschnittBemerkung::getBemerkung)
			.collect(Collectors.toList());
		assertTrue(bemerkungenStringList.contains(bemerkung));
	}

}
