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

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.AntragTyp;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.EnumGesuchstellerKardinalitaet;
import ch.dvbern.ebegu.test.TestDataUtil;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests für ErwerbspensumRule
 */
public class ErwerbspensumRuleTest {

	@Test
	public void testKeinErwerbspensum() {
		Betreuung betreuung = createGesuch(true);

		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(0, result.get(0).getAnspruchberechtigtesPensum());
		final String bemerkungen = result.get(0).getBemerkungen();
		assertNotNull(bemerkungen);
		assertFalse(bemerkungen.isEmpty());
		assertTrue(bemerkungen.contains(RuleKey.ERWERBSPENSUM.name()));
	}

	@Test
	public void testNormalfallZweiGesuchsteller() {
		Betreuung betreuung = createGesuch(true);
		Gesuch gesuch = betreuung.extractGesuch();

		assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 100));
		assertNotNull(gesuch.getGesuchsteller2());
		gesuch.getGesuchsteller2().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 40));

		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(40, result.get(0).getAnspruchberechtigtesPensum());
		final String bemerkungen = result.get(0).getBemerkungen();
		assertNotNull(bemerkungen);
		assertTrue(bemerkungen.isEmpty());
	}

	@Test
	public void testNotAngebotJugendamtKleinkind() {
		Betreuung betreuung = createGesuch(true);
		betreuung.getInstitutionStammdaten().setBetreuungsangebotTyp(BetreuungsangebotTyp.TAGESSCHULE);
		Gesuch gesuch = betreuung.extractGesuch();

		assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 100));
		assertNotNull(gesuch.getGesuchsteller2());
		gesuch.getGesuchsteller2().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 40));


		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(0, result.get(0).getAnspruchberechtigtesPensum());
		assertEquals("BETREUUNGSANGEBOT_TYP: Betreuungsangebot Schulamt", result.get(0).getBemerkungen());
	}

	@Test
	public void testNormalfallEinGesuchsteller() {
		Betreuung betreuung = createGesuch(false);
		Gesuch gesuch = betreuung.extractGesuch();

		assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 60));

		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(60, result.get(0).getAnspruchberechtigtesPensum());
		final String bemerkungen = result.get(0).getBemerkungen();
		assertNotNull(bemerkungen);
		assertTrue(bemerkungen.isEmpty());
	}

	@Test
	public void testNurEinErwerbspensumBeiZweiGesuchstellern() {
		Betreuung betreuung = createGesuch(true);
		Gesuch gesuch = betreuung.extractGesuch();

		assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 80));

		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(0, result.get(0).getAnspruchberechtigtesPensum());
		final String bemerkungen = result.get(0).getBemerkungen();
		assertNotNull(bemerkungen);
		assertFalse(bemerkungen.isEmpty());
	}

	@Test
	public void testMehrAls100ProzentBeiEinemGesuchsteller() {
		Betreuung betreuung = createGesuch(false);
		Gesuch gesuch = betreuung.extractGesuch();

		assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 120));


		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(100, result.get(0).getAnspruchberechtigtesPensum());
		final String bemerkungen = result.get(0).getBemerkungen();
		assertNotNull(bemerkungen);
		assertFalse(bemerkungen.isEmpty());
	}

	@Test
	public void testMehrAls100ProzentBeiBeidenGesuchstellern() {
		Betreuung betreuung = createGesuch(true);
		Gesuch gesuch = betreuung.extractGesuch();

		assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 110));
		assertNotNull(gesuch.getGesuchsteller2());
		gesuch.getGesuchsteller2().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 110));


		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(100, result.get(0).getAnspruchberechtigtesPensum());
		final String bemerkungen = result.get(0).getBemerkungen();
		assertNotNull(bemerkungen);
		assertTrue(bemerkungen.contains("Maximaler Anspruch Beschäftigungspensum 100 %"));
		assertTrue(bemerkungen.contains("Maximaler Anspruch Beschäftigungspensum 100 %"));
	}

	@Test
	public void testFrom1GSTo2GSRechtzeitigEingereicht() {
		Betreuung betreuung = createGesuch(true);

		Gesuch gesuch = betreuung.extractGesuch();
		gesuch.setTyp(AntragTyp.MUTATION);
		gesuch.setEingangsdatum(LocalDate.of(TestDataUtil.PERIODE_JAHR_1, Month.DECEMBER, 26));

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
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 90));
		assertNotNull(gesuch.getGesuchsteller2());
		gesuch.getGesuchsteller2().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 80));

		assertNotNull(gesuch.getFamiliensituationContainer());
		gesuch.getFamiliensituationContainer().setFamiliensituationErstgesuch(new Familiensituation());

		final Familiensituation familiensituationErstgesuch = gesuch.extractFamiliensituationErstgesuch();
		assertNotNull(familiensituationErstgesuch);
		familiensituationErstgesuch.setFamilienstatus(EnumFamilienstatus.ALLEINERZIEHEND);
		familiensituationErstgesuch.setGesuchstellerKardinalitaet(EnumGesuchstellerKardinalitaet.ALLEINE);

		final Familiensituation extractedFamiliensituation = gesuch.extractFamiliensituation();
		assertNotNull(extractedFamiliensituation);
		extractedFamiliensituation.setFamilienstatus(EnumFamilienstatus.VERHEIRATET);
		extractedFamiliensituation.setAenderungPer(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.MARCH, 26));

		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);
		assertNotNull(result);
		assertEquals(3, result.size());
		assertEquals(90, result.get(0).getAnspruchberechtigtesPensum());
		assertEquals(TestDataUtil.START_PERIODE, result.get(0).getGueltigkeit().getGueltigAb());
		assertEquals(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.MARCH, 25), result.get(0).getGueltigkeit().getGueltigBis());

		assertEquals(70, result.get(1).getAnspruchberechtigtesPensum());
		assertEquals(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.MARCH, 26), result.get(1).getGueltigkeit().getGueltigAb());
		assertEquals(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.MARCH, 31), result.get(1).getGueltigkeit().getGueltigBis());

		assertEquals(70, result.get(2).getAnspruchberechtigtesPensum());
		assertEquals(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.APRIL, 1), result.get(2).getGueltigkeit().getGueltigAb());
		assertEquals(TestDataUtil.ENDE_PERIODE, result.get(2).getGueltigkeit().getGueltigBis());
	}

	@Test
	public void testFrom2GSTo1GSRechtzeitigEingereicht() {
		Betreuung betreuung = createGesuch(true);

		Gesuch gesuch = betreuung.extractGesuch();
		gesuch.setTyp(AntragTyp.MUTATION);
		gesuch.setEingangsdatum(LocalDate.of(2016, Month.DECEMBER, 26));

		assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 90));

		assertNotNull(gesuch.getGesuchsteller2());
		gesuch.getGesuchsteller2().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 70));

		assertNotNull(gesuch.getFamiliensituationContainer());
		gesuch.getFamiliensituationContainer().setFamiliensituationErstgesuch(new Familiensituation());

		final Familiensituation familiensituationErstgesuch = gesuch.extractFamiliensituationErstgesuch();
		assertNotNull(familiensituationErstgesuch);
		familiensituationErstgesuch.setFamilienstatus(EnumFamilienstatus.VERHEIRATET);

		final Familiensituation extractedFamiliensituation = gesuch.extractFamiliensituation();
		assertNotNull(extractedFamiliensituation);
		extractedFamiliensituation.setFamilienstatus(EnumFamilienstatus.ALLEINERZIEHEND);
		extractedFamiliensituation.setGesuchstellerKardinalitaet(EnumGesuchstellerKardinalitaet.ALLEINE);
		extractedFamiliensituation.setAenderungPer(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.MARCH, 26));

		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);
		assertNotNull(result);
		assertEquals(3, result.size());
		assertEquals(60, result.get(0).getAnspruchberechtigtesPensum());
		assertEquals(TestDataUtil.START_PERIODE, result.get(0).getGueltigkeit().getGueltigAb());
		assertEquals(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.MARCH, 25), result.get(0).getGueltigkeit().getGueltigBis());

		assertEquals(90, result.get(1).getAnspruchberechtigtesPensum());
		assertEquals(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.MARCH, 26), result.get(1).getGueltigkeit().getGueltigAb());
		assertEquals(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.MARCH, 31), result.get(1).getGueltigkeit().getGueltigBis());

		assertEquals(90, result.get(2).getAnspruchberechtigtesPensum());
		assertEquals(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.APRIL, 1), result.get(2).getGueltigkeit().getGueltigAb());
		assertEquals(TestDataUtil.ENDE_PERIODE, result.get(2).getGueltigkeit().getGueltigBis());
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
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, -1));
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);
		assertEquals(0, result.get(0).getAnspruchberechtigtesPensum());

		gesuch.getGesuchsteller1().setErwerbspensenContainers(new HashSet<>());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 0));

		List<VerfuegungZeitabschnitt> result2 = EbeguRuleTestsHelper.calculate(betreuung);
		assertEquals(0, result2.get(0).getAnspruchberechtigtesPensum());

		gesuch.getGesuchsteller1().setErwerbspensenContainers(new HashSet<>());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 1));
		List<VerfuegungZeitabschnitt> result3 = EbeguRuleTestsHelper.calculate(betreuung);
		assertEquals(0, result3.get(0).getAnspruchberechtigtesPensum());

		gesuch.getGesuchsteller1().setErwerbspensenContainers(new HashSet<>());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 50));
		List<VerfuegungZeitabschnitt> result4 = EbeguRuleTestsHelper.calculate(betreuung);
		assertEquals(50, result4.get(0).getAnspruchberechtigtesPensum());

		gesuch.getGesuchsteller1().setErwerbspensenContainers(new HashSet<>());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 51));

		List<VerfuegungZeitabschnitt> result5 = EbeguRuleTestsHelper.calculate(betreuung);
		assertEquals(50, result5.get(0).getAnspruchberechtigtesPensum());

		gesuch.getGesuchsteller1().setErwerbspensenContainers(new HashSet<>());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 54));
		List<VerfuegungZeitabschnitt> result6 = EbeguRuleTestsHelper.calculate(betreuung);
		assertEquals(55, result6.get(0).getAnspruchberechtigtesPensum());

		gesuch.getGesuchsteller1().setErwerbspensenContainers(new HashSet<>());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 55));
		List<VerfuegungZeitabschnitt> result7 = EbeguRuleTestsHelper.calculate(betreuung);
		assertEquals(55, result7.get(0).getAnspruchberechtigtesPensum());

		//mit zuschlag
		gesuch.getGesuchsteller1().setErwerbspensenContainers(new HashSet<>());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 55));

		List<VerfuegungZeitabschnitt> result8 = EbeguRuleTestsHelper.calculate(betreuung);
		assertEquals(55, result8.get(0).getAnspruchberechtigtesPensum());

		gesuch.getGesuchsteller1().setErwerbspensenContainers(new HashSet<>());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 59));
		List<VerfuegungZeitabschnitt> result9 = EbeguRuleTestsHelper.calculate(betreuung);
		assertEquals(60, result9.get(0).getAnspruchberechtigtesPensum());

		gesuch.getGesuchsteller1().setErwerbspensenContainers(new HashSet<>());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 99));
		List<VerfuegungZeitabschnitt> result10 = EbeguRuleTestsHelper.calculate(betreuung);
		assertEquals(100, result10.get(0).getAnspruchberechtigtesPensum());

		gesuch.getGesuchsteller1().setErwerbspensenContainers(new HashSet<>());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 100));

		List<VerfuegungZeitabschnitt> result11 = EbeguRuleTestsHelper.calculate(betreuung);
		assertEquals(100, result11.get(0).getAnspruchberechtigtesPensum());

		gesuch.getGesuchsteller1().setErwerbspensenContainers(new HashSet<>());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 101));
		List<VerfuegungZeitabschnitt> result12 = EbeguRuleTestsHelper.calculate(betreuung);
		assertEquals(100, result12.get(0).getAnspruchberechtigtesPensum());

	}

	@Test
	public void testZweiGesuchstellerAllMax() {
		Betreuung betreuung = createGesuch(true);
		Gesuch gesuch = betreuung.extractGesuch();

		assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 115));
		assertNotNull(gesuch.getGesuchsteller2());
		gesuch.getGesuchsteller2().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 110));

		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(100, result.get(0).getAnspruchberechtigtesPensum());
		final String bemerkungen = result.get(0).getBemerkungen();
		assertNotNull(bemerkungen);
		assertTrue(bemerkungen.contains("Maximaler Anspruch Beschäftigungspensum 100 %"));
	}

	@Test
	public void testMinimaleErwerbspensen() {
		Betreuung betreuung = createGesuch(false);
		Gesuch gesuch = betreuung.extractGesuch();

		assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 15));

		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(15, result.get(0).getErwerbspensumGS1().intValue());
		assertEquals(0, result.get(0).getAnspruchberechtigtesPensum());
		assertFalse(result.get(0).isBezahltVollkosten());
		assertFalse(result.get(0).getBemerkungen().isEmpty());
	}

	private Betreuung createGesuch(final boolean gs2) {
		final Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(gs2);
		final Gesuch gesuch = betreuung.extractGesuch();

		TestDataUtil.createDefaultAdressenForGS(gesuch, gs2);

		return betreuung;
	}

}
