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
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 100, 0));
		assertNotNull(gesuch.getGesuchsteller2());
		gesuch.getGesuchsteller2().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 40, 0));

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
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 100, 0));
		assertNotNull(gesuch.getGesuchsteller2());
		gesuch.getGesuchsteller2().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 40, 0));

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
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 60, 0));

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
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 80, 0));

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
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 120, 0));

		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(100, result.get(0).getAnspruchberechtigtesPensum());
		final String bemerkungen = result.get(0).getBemerkungen();
		assertNotNull(bemerkungen);
		assertFalse(bemerkungen.isEmpty());

		Betreuung betreuung2 = createGesuch(false);
		Gesuch gesuch2 = betreuung2.extractGesuch();

		//weniger als 100 ewp aber grosser zuschlag -> keine Bemerkung
		assertNotNull(gesuch2.getGesuchsteller1());
		gesuch2.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 90, 20));

		List<VerfuegungZeitabschnitt> noBemerkungAbschn = EbeguRuleTestsHelper.calculate(betreuung2);
		assertNotNull(noBemerkungAbschn);
		assertEquals(1, noBemerkungAbschn.size());
		assertEquals(100, noBemerkungAbschn.get(0).getAnspruchberechtigtesPensum());
		final String bemerkungen2 = noBemerkungAbschn.get(0).getBemerkungen();
		assertNotNull(bemerkungen2);
		assertTrue(bemerkungen2.contains("Erwerbspensum + Zuschlag sind auf 100% limitiert für Gesuchsteller 1"));
	}

	@Test
	public void testMehrAls100ProzentBeiBeidenGesuchstellern() {
		Betreuung betreuung = createGesuch(true);
		Gesuch gesuch = betreuung.extractGesuch();

		assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 110, 0));
		assertNotNull(gesuch.getGesuchsteller2());
		gesuch.getGesuchsteller2().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 110, 10));

		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(100, result.get(0).getAnspruchberechtigtesPensum());
		final String bemerkungen = result.get(0).getBemerkungen();
		assertNotNull(bemerkungen);
		assertTrue(bemerkungen.contains("Maximaler Anspruch Erwerbspensum 100 %"));
		assertTrue(bemerkungen.contains("Maximaler Anspruch Erwerbspensum 100 %"));
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
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 90, 0));
		assertNotNull(gesuch.getGesuchsteller2());
		gesuch.getGesuchsteller2().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 80, 0));

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
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 90, 0));
		assertNotNull(gesuch.getGesuchsteller2());
		gesuch.getGesuchsteller2().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 70, 0));

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
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, -1, 0));
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);
		assertEquals(0, result.get(0).getAnspruchberechtigtesPensum());

		gesuch.getGesuchsteller1().setErwerbspensenContainers(new HashSet<>());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 0, 0));
		List<VerfuegungZeitabschnitt> result2 = EbeguRuleTestsHelper.calculate(betreuung);
		assertEquals(0, result2.get(0).getAnspruchberechtigtesPensum());

		gesuch.getGesuchsteller1().setErwerbspensenContainers(new HashSet<>());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 1, 0));
		List<VerfuegungZeitabschnitt> result3 = EbeguRuleTestsHelper.calculate(betreuung);
		assertEquals(0, result3.get(0).getAnspruchberechtigtesPensum());

		gesuch.getGesuchsteller1().setErwerbspensenContainers(new HashSet<>());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 50, 0));
		List<VerfuegungZeitabschnitt> result4 = EbeguRuleTestsHelper.calculate(betreuung);
		assertEquals(50, result4.get(0).getAnspruchberechtigtesPensum());

		gesuch.getGesuchsteller1().setErwerbspensenContainers(new HashSet<>());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 51, 0));
		List<VerfuegungZeitabschnitt> result5 = EbeguRuleTestsHelper.calculate(betreuung);
		assertEquals(50, result5.get(0).getAnspruchberechtigtesPensum());

		gesuch.getGesuchsteller1().setErwerbspensenContainers(new HashSet<>());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 54, 0));
		List<VerfuegungZeitabschnitt> result6 = EbeguRuleTestsHelper.calculate(betreuung);
		assertEquals(55, result6.get(0).getAnspruchberechtigtesPensum());

		gesuch.getGesuchsteller1().setErwerbspensenContainers(new HashSet<>());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 55, 0));
		List<VerfuegungZeitabschnitt> result7 = EbeguRuleTestsHelper.calculate(betreuung);
		assertEquals(55, result7.get(0).getAnspruchberechtigtesPensum());

		//mit zuschlag
		gesuch.getGesuchsteller1().setErwerbspensenContainers(new HashSet<>());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 50, 5));
		List<VerfuegungZeitabschnitt> result8 = EbeguRuleTestsHelper.calculate(betreuung);
		assertEquals(55, result8.get(0).getAnspruchberechtigtesPensum());

		gesuch.getGesuchsteller1().setErwerbspensenContainers(new HashSet<>());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 59, 0));
		List<VerfuegungZeitabschnitt> result9 = EbeguRuleTestsHelper.calculate(betreuung);
		assertEquals(60, result9.get(0).getAnspruchberechtigtesPensum());

		gesuch.getGesuchsteller1().setErwerbspensenContainers(new HashSet<>());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 99, 0));
		List<VerfuegungZeitabschnitt> result10 = EbeguRuleTestsHelper.calculate(betreuung);
		assertEquals(100, result10.get(0).getAnspruchberechtigtesPensum());

		gesuch.getGesuchsteller1().setErwerbspensenContainers(new HashSet<>());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 100, 0));
		List<VerfuegungZeitabschnitt> result11 = EbeguRuleTestsHelper.calculate(betreuung);
		assertEquals(100, result11.get(0).getAnspruchberechtigtesPensum());

		gesuch.getGesuchsteller1().setErwerbspensenContainers(new HashSet<>());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 101, 0));
		List<VerfuegungZeitabschnitt> result12 = EbeguRuleTestsHelper.calculate(betreuung);
		assertEquals(100, result12.get(0).getAnspruchberechtigtesPensum());

	}

	@Test
	public void testZweiGesuchstellerZuVielZuschlag() {
		Betreuung betreuung = createGesuch(true);
		Gesuch gesuch = betreuung.extractGesuch();

		assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 80, 15));
		assertNotNull(gesuch.getGesuchsteller2());
		gesuch.getGesuchsteller2().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 40, 15));

		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(40, result.get(0).getAnspruchberechtigtesPensum());
		assertEquals("ERWERBSPENSUM: Der Zuschlag zum Erwerbspensum beträgt max. 20%", result.get(0).getBemerkungen()); // Es muss eine Bemerkung geben, da der maximale Wert fuer Zuschlag ueberschrittet wurde
	}

	@Test
	public void testZweiGesuchstellerAllMax() {
		Betreuung betreuung = createGesuch(true);
		Gesuch gesuch = betreuung.extractGesuch();

		assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 110, 5));
		assertNotNull(gesuch.getGesuchsteller2());
		gesuch.getGesuchsteller2().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 90, 20));

		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(100, result.get(0).getAnspruchberechtigtesPensum());
		final String bemerkungen = result.get(0).getBemerkungen();
		assertNotNull(bemerkungen);
		assertTrue(bemerkungen.contains("Maximaler Anspruch Erwerbspensum 100 %"));
		assertTrue(bemerkungen.contains("Erwerbspensum + Zuschlag sind auf 100% limitiert für Gesuchsteller 1"));
		assertTrue(bemerkungen.contains("Erwerbspensum + Zuschlag sind auf 100% limitiert für Gesuchsteller 2"));
	}

	@Test
	public void testZweiGesuchstellerMaximumZuschlagUeberschritten() {
		Betreuung betreuung = createGesuch(true);
		Gesuch gesuch = betreuung.extractGesuch();

		assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 83, 20));
		assertNotNull(gesuch.getGesuchsteller2());
		gesuch.getGesuchsteller2().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 90, 10));

		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(95, result.get(0).getAnspruchberechtigtesPensum());
		final String bemerkungen = result.get(0).getBemerkungen();
		assertNotNull(bemerkungen);
		assertTrue(bemerkungen.contains("Erwerbspensum + Zuschlag sind auf 100% limitiert für Gesuchsteller 1"));
		assertFalse(bemerkungen.contains("Erwerbspensum + Zuschlag sind auf 100% limitiert für Gesuchsteller 2"));
		assertTrue(bemerkungen.contains("Der Zuschlag zum Erwerbspensum beträgt max."));
	}

	private Betreuung createGesuch(final boolean gs2) {
		final Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(gs2);
		final Gesuch gesuch = betreuung.extractGesuch();

		TestDataUtil.createDefaultAdressenForGS(gesuch, gs2);

		return betreuung;
	}

}
