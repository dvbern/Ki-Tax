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

import java.math.BigDecimal;
import java.util.List;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.rules.initalizer.RestanspruchInitializer;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.util.MathUtil;
import org.junit.Assert;
import org.junit.Test;

import static ch.dvbern.ebegu.rules.EbeguRuleTestsHelper.calculate;
import static ch.dvbern.ebegu.rules.EbeguRuleTestsHelper.calculateWithRemainingRestanspruch;
import static ch.dvbern.ebegu.util.Constants.ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS;

/**
 * Tests für Betreuungspensum-Regel
 */
public class BetreuungspensumRuleTest {

	private static final boolean IS_DEBUG = false;
	private RestanspruchInitializer restanspruchInitializer = new RestanspruchInitializer(IS_DEBUG);

	@Test
	public void testKitaNormalfall() {
		Betreuung betreuung = EbeguRuleTestsHelper.createBetreuungWithPensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, BetreuungsangebotTyp.KITA, 60,  BigDecimal.valueOf(500.50));
		Assert.assertNotNull(betreuung.getKind().getGesuch().getGesuchsteller1());
		betreuung.getKind().getGesuch().getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 60));
		List<VerfuegungZeitabschnitt> result = calculate(betreuung);

		Assert.assertNotNull(result);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(Integer.valueOf(60), result.get(0).getBgCalculationInputAsiv().getErwerbspensumGS1());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), result.get(0).getBetreuungspensumProzent());
		Assert.assertEquals(BigDecimal.valueOf(500.50), result.get(0).getBgCalculationInputAsiv().getMonatlicheBetreuungskosten());
		Assert.assertEquals(60 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, result.get(0).getAnspruchberechtigtesPensum());
		Assert.assertEquals(-1, result.get(0).getBgCalculationInputAsiv().getAnspruchspensumRest());
		result = EbeguRuleTestsHelper.runSingleAbschlussRule(restanspruchInitializer, betreuung, result);
		Assert.assertEquals(0 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, result.get(0).getBgCalculationInputAsiv().getAnspruchspensumRest());
	}

	@Test
	public void testKitaZuwenigAnspruch() {
		Betreuung betreuung = EbeguRuleTestsHelper.createBetreuungWithPensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, BetreuungsangebotTyp.KITA, 80,   BigDecimal.valueOf(200));
		Assert.assertNotNull(betreuung.getKind().getGesuch().getGesuchsteller1());
		betreuung.getKind().getGesuch().getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 60));
		List<VerfuegungZeitabschnitt> result = calculate(betreuung);

		Assert.assertNotNull(result);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(Integer.valueOf(60), result.get(0).getBgCalculationInputAsiv().getErwerbspensumGS1());
		Assert.assertEquals(MathUtil.DEFAULT.from(80), result.get(0).getBetreuungspensumProzent());
		Assert.assertEquals(BigDecimal.valueOf(200), result.get(0).getBgCalculationInputAsiv().getMonatlicheBetreuungskosten());
		Assert.assertEquals(60 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, result.get(0).getAnspruchberechtigtesPensum());
		Assert.assertEquals(-1, result.get(0).getBgCalculationInputAsiv().getAnspruchspensumRest());
		result = EbeguRuleTestsHelper.runSingleAbschlussRule(restanspruchInitializer, betreuung, result);
		Assert.assertEquals(0, result.get(0).getBgCalculationInputAsiv().getAnspruchspensumRest());
	}

	@Test
	public void testKitaMitRestanspruch() {
		Betreuung betreuung = EbeguRuleTestsHelper.createBetreuungWithPensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, BetreuungsangebotTyp.KITA, 60,  BigDecimal.valueOf(500));
		Assert.assertNotNull(betreuung.getKind().getGesuch().getGesuchsteller1());
		betreuung.getKind().getGesuch().getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 80));
		List<VerfuegungZeitabschnitt> result = calculate(betreuung);

		Assert.assertNotNull(result);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(Integer.valueOf(80), result.get(0).getBgCalculationInputAsiv().getErwerbspensumGS1());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), result.get(0).getBetreuungspensumProzent());
		Assert.assertEquals(80 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, result.get(0).getAnspruchberechtigtesPensum());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), result.get(0).getBgPensum());
		Assert.assertEquals(-1, result.get(0).getBgCalculationInputAsiv().getAnspruchspensumRest());
		Assert.assertEquals(BigDecimal.valueOf(500), result.get(0).getBgCalculationInputAsiv().getMonatlicheBetreuungskosten());
		result = EbeguRuleTestsHelper.runSingleAbschlussRule(restanspruchInitializer, betreuung, result);
		Assert.assertEquals(20 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, result.get(0).getBgCalculationInputAsiv().getAnspruchspensumRest());
	}

	@Test
	public void testZweiKitas() {
		Betreuung betreuung1 = EbeguRuleTestsHelper.createBetreuungWithPensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, BetreuungsangebotTyp.KITA, 60,  BigDecimal.valueOf(600));
		Betreuung betreuung2 = EbeguRuleTestsHelper.createBetreuungWithPensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, BetreuungsangebotTyp.KITA, 40,  BigDecimal.valueOf(400));
		Assert.assertNotNull(betreuung1.getKind().getGesuch().getGesuchsteller1());
		Assert.assertNotNull(betreuung2.getKind().getGesuch().getGesuchsteller1());
		betreuung1.getKind().getGesuch().getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 80));

		List<VerfuegungZeitabschnitt> result = calculate(betreuung1);

		Assert.assertNotNull(result);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(Integer.valueOf(80), result.get(0).getBgCalculationInputAsiv().getErwerbspensumGS1());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), result.get(0).getBetreuungspensumProzent());
		Assert.assertEquals(80 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, result.get(0).getAnspruchberechtigtesPensum());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), result.get(0).getBgPensum());
		Assert.assertEquals(-1, result.get(0).getBgCalculationInputAsiv().getAnspruchspensumRest());  //restanspruch wurde noch nie berechnet
		// Anspruchsrest fuer naechste Betreuung setzten
		List<VerfuegungZeitabschnitt> abschnForNxtBetr = EbeguRuleTestsHelper.runSingleAbschlussRule(restanspruchInitializer, betreuung1, result);
		//Nach dem Berechnen des Rests ist der Rest im  im Feld AnspruchspensumRest gesetzt, Anspruchsberechtigtes
		// Pensum ist noch 0 da noch nciht berechnet
		// fuer 2.Betr.
		Assert.assertEquals(20 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, abschnForNxtBetr.get(0).getBgCalculationInputAsiv().getAnspruchspensumRest());
		Assert.assertEquals(0, abschnForNxtBetr.get(0).getAnspruchberechtigtesPensum());

		// Kita 2: Reicht nicht mehr ganz
		betreuung2.getKind().getGesuch().getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 80));
		List<VerfuegungZeitabschnitt> resultBetr2 = calculateWithRemainingRestanspruch(betreuung2, 20);

		Assert.assertNotNull(resultBetr2);
		Assert.assertEquals(1, resultBetr2.size());
		Assert.assertEquals(Integer.valueOf(80), resultBetr2.get(0).getBgCalculationInputAsiv().getErwerbspensumGS1());
		Assert.assertEquals(MathUtil.DEFAULT.from(40), resultBetr2.get(0).getBgCalculationInputAsiv().getBetreuungspensumProzent());
		Assert.assertEquals(20, resultBetr2.get(0).getBgCalculationInputAsiv().getAnspruchspensumRest());
		Assert.assertEquals(20, resultBetr2.get(0).getBgCalculationInputAsiv().getAnspruchspensumProzent()); // Nach der Berechnung des Anspruchs kann der Anspruch nicht hoeher
		// sein
		// als der Restanspruch (20)
		Assert.assertEquals(MathUtil.DEFAULT.from(20), resultBetr2.get(0).getBgPensum());
		Assert.assertEquals(20, resultBetr2.get(0).getBgCalculationInputAsiv().getAnspruchspensumRest()); //Restanspruch wurde noch nicht neu
		// berechnet fuer naechste betreuung
		resultBetr2 = EbeguRuleTestsHelper.runSingleAbschlussRule(restanspruchInitializer, betreuung2, resultBetr2);
		Assert.assertEquals(0, resultBetr2.get(0).getBgCalculationInputAsiv().getAnspruchspensumRest()); // Nach dem initialisieren fuer das
		// nachste Betreuungspensum ist der noch
		// verbleibende restanspruch 0
	}

	@Test
	public void testTagesfamilienOhneErwerbspensum() {
		// Tageseltern Kleinkind haben die gleichen Regeln wie Kita
		Betreuung betreuung = EbeguRuleTestsHelper.createBetreuungWithPensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, BetreuungsangebotTyp.TAGESFAMILIEN, 80,  BigDecimal.valueOf(800));
		List<VerfuegungZeitabschnitt> result = calculate(betreuung);

		Assert.assertNotNull(result);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(Integer.valueOf(0), result.get(0).getBgCalculationInputAsiv().getErwerbspensumGS1());
		Assert.assertEquals(MathUtil.DEFAULT.from(80), result.get(0).getBetreuungspensumProzent());
		Assert.assertEquals(0, result.get(0).getAnspruchberechtigtesPensum());
		Assert.assertEquals(MathUtil.DEFAULT.from(0), result.get(0).getBgPensum());
		Assert.assertEquals(-1, result.get(0).getBgCalculationInputAsiv().getAnspruchspensumRest());
		Assert.assertEquals(BigDecimal.valueOf(800), result.get(0).getBgCalculationInputAsiv().getMonatlicheBetreuungskosten());
		result = EbeguRuleTestsHelper.runSingleAbschlussRule(restanspruchInitializer, betreuung, result);
		Assert.assertEquals(0, result.get(0).getBgCalculationInputAsiv().getAnspruchspensumRest());
	}

	@Test
	public void testTagesfamilienMitTiefemErwerbspensum() {
		// Tageseltern Kleinkinder haben die gleichen Regeln wie Kita
		Betreuung betreuung = EbeguRuleTestsHelper.createBetreuungWithPensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, BetreuungsangebotTyp.TAGESFAMILIEN, 80,  BigDecimal.valueOf(800));
		Assert.assertNotNull(betreuung.getKind().getGesuch().getGesuchsteller1());
		betreuung.getKind().getGesuch().getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 60));
		List<VerfuegungZeitabschnitt> result = calculate(betreuung);

		Assert.assertNotNull(result);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(Integer.valueOf(60), result.get(0).getBgCalculationInputAsiv().getErwerbspensumGS1());
		Assert.assertEquals(MathUtil.DEFAULT.from(80), result.get(0).getBetreuungspensumProzent());
		Assert.assertEquals(60 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, result.get(0).getAnspruchberechtigtesPensum());
		Assert.assertEquals(MathUtil.DEFAULT.from(60 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS), result.get(0).getBgPensum());
		Assert.assertEquals(-1, result.get(0).getBgCalculationInputAsiv().getAnspruchspensumRest());
		Assert.assertEquals(BigDecimal.valueOf(800), result.get(0).getBgCalculationInputAsiv().getMonatlicheBetreuungskosten());
		result = EbeguRuleTestsHelper.runSingleAbschlussRule(restanspruchInitializer, betreuung, result);
		Assert.assertEquals(0, result.get(0).getBgCalculationInputAsiv().getAnspruchspensumRest());
	}
}
