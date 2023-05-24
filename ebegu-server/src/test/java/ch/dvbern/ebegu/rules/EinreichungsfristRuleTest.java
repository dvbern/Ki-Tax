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
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.rechner.AbstractBGRechnerTest;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import org.junit.Assert;
import org.junit.Test;

import static ch.dvbern.ebegu.rules.EbeguRuleTestsHelper.calculate;
import static ch.dvbern.ebegu.util.Constants.ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS;

/**
 * Tests für Einreichungsfrist-Regel
 */
public class EinreichungsfristRuleTest extends AbstractBGRechnerTest {

	private static final BigDecimal ZERO = MathUtil.DEFAULT.from(0);

	/**
	 * Kita: Einreichung am 1.2., Start der Betreuung am 1.8.
	 */
	@Test
	public void testKitaEinreichungRechtzeitig() {
		Betreuung betreuung = EbeguRuleTestsHelper.createBetreuungWithPensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE,
			BetreuungsangebotTyp.KITA, 60, BigDecimal.valueOf(2000));
		final Gesuch gesuch = betreuung.extractGesuch();
		TestDataUtil.createDefaultAdressenForGS(gesuch, false);
		Assert.assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 60));
		gesuch.setEingangsdatum(LocalDate.of(TestDataUtil.PERIODE_JAHR_1, Month.FEBRUARY, 1));
		List<VerfuegungZeitabschnitt> result = calculate(betreuung);
		List<VerfuegungZeitabschnitt> nextRestanspruch = EbeguRuleTestsHelper.initializeRestanspruchForNextBetreuung(betreuung, result);

		Assert.assertNotNull(result);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(Integer.valueOf(60), result.get(0).getBgCalculationInputAsiv().getErwerbspensumGS1());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), result.get(0).getBetreuungspensumProzent());
		Assert.assertEquals(60 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, result.get(0).getAnspruchberechtigtesPensum());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), result.get(0).getBgPensum());
		Assert.assertEquals(0 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, nextRestanspruch.get(0).getBgCalculationInputAsiv().getAnspruchspensumRest());
		Assert.assertFalse(result.get(0).isZuSpaetEingereicht());
		Assert.assertFalse(result.get(0).getBgCalculationInputAsiv().isBezahltKompletteVollkosten());
	}
	/**
	 * Kita: Einreichung am 1.2., Start der Betreuung am 1.8.
	 */
	@Test
	public void testKitaEinreichungRechtzeitigAR() {
		Betreuung betreuung = EbeguRuleTestsHelper.createBetreuungWithPensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE,
			BetreuungsangebotTyp.KITA, 60, BigDecimal.valueOf(2000));
		final Gesuch gesuch = betreuung.extractGesuch();
		gesuch.getFall().setMandant(TestDataUtil.createMandant(MandantIdentifier.APPENZELL_AUSSERRHODEN));
		TestDataUtil.createDefaultAdressenForGS(gesuch, false);
		Assert.assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 60));
		gesuch.setEingangsdatum(LocalDate.of(TestDataUtil.PERIODE_JAHR_1, Month.FEBRUARY, 1));
		List<VerfuegungZeitabschnitt> result = calculate(betreuung);
		List<VerfuegungZeitabschnitt> nextRestanspruch = EbeguRuleTestsHelper.initializeRestanspruchForNextBetreuung(betreuung, result);

		Assert.assertNotNull(result);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(Integer.valueOf(60), result.get(0).getBgCalculationInputAsiv().getErwerbspensumGS1());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), result.get(0).getBetreuungspensumProzent());
		Assert.assertEquals(60 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, result.get(0).getAnspruchberechtigtesPensum());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), result.get(0).getBgPensum());
		Assert.assertEquals(0 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, nextRestanspruch.get(0).getBgCalculationInputAsiv().getAnspruchspensumRest());
		Assert.assertFalse(result.get(0).isZuSpaetEingereicht());
		Assert.assertFalse(result.get(0).getBgCalculationInputAsiv().isBezahltKompletteVollkosten());
	}

	/**
	 * Kita: Einreichung am 7.10., Start der Betreuung am 1.8.
	 */
	@Test
	public void testKitaEinreichungZuSpaet() {
		Betreuung betreuung = EbeguRuleTestsHelper.createBetreuungWithPensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE,
			BetreuungsangebotTyp.KITA, 60, BigDecimal.valueOf(2000));
		final Gesuch gesuch = betreuung.extractGesuch();
		TestDataUtil.createDefaultAdressenForGS(gesuch, false);
		Assert.assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 60));
		gesuch.setEingangsdatum(LocalDate.of(TestDataUtil.PERIODE_JAHR_1, Month.OCTOBER, 7));
		List<VerfuegungZeitabschnitt> result = calculate(betreuung);
		List<VerfuegungZeitabschnitt> nextRestanspruch = EbeguRuleTestsHelper.initializeRestanspruchForNextBetreuung(betreuung, result);
		Assert.assertNotNull(result);
		Assert.assertEquals(2, result.size());

		VerfuegungZeitabschnitt abschnitt1 = result.get(0);
		Assert.assertEquals(Integer.valueOf(60), abschnitt1.getBgCalculationInputAsiv().getErwerbspensumGS1());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), abschnitt1.getBetreuungspensumProzent());
		Assert.assertEquals(0, abschnitt1.getAnspruchberechtigtesPensum());
		Assert.assertEquals(MathUtil.DEFAULT.from(0), abschnitt1.getBgPensum());
		Assert.assertEquals(80, result.get(0).getBgCalculationInputAsiv().getAnspruchspensumRest());
		Assert.assertEquals(80, nextRestanspruch.get(0).getBgCalculationInputAsiv().getAnspruchspensumRest());
		Assert.assertTrue(abschnitt1.isZuSpaetEingereicht());
		Assert.assertFalse(abschnitt1.getBgCalculationInputAsiv().isBezahltKompletteVollkosten());

		VerfuegungZeitabschnitt abschnitt2 = result.get(1);
		Assert.assertEquals(Integer.valueOf(60), abschnitt2.getBgCalculationInputAsiv().getErwerbspensumGS1());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), abschnitt2.getBetreuungspensumProzent());
		Assert.assertEquals(60 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, abschnitt2.getAnspruchberechtigtesPensum());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), abschnitt2.getBgPensum());
		Assert.assertEquals(-1, abschnitt2.getBgCalculationInputAsiv().getAnspruchspensumRest());
		Assert.assertEquals(0 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, nextRestanspruch.get(1).getBgCalculationInputAsiv().getAnspruchspensumRest());
		Assert.assertFalse(abschnitt2.isZuSpaetEingereicht());
		Assert.assertFalse(abschnitt2.getBgCalculationInputAsiv().isBezahltKompletteVollkosten());
	}
	
	/**
	 * Kita: Einreichung am 7.10., Start der Betreuung am 1.8.
	 */
	@Test
	public void testKitaEinreichungNach67TagenZuSpaetAR() {
		Betreuung betreuung = EbeguRuleTestsHelper.createBetreuungWithPensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE,
			BetreuungsangebotTyp.KITA, 60, BigDecimal.valueOf(2000));
		final Gesuch gesuch = betreuung.extractGesuch();
		gesuch.getFall().setMandant(TestDataUtil.createMandant(MandantIdentifier.APPENZELL_AUSSERRHODEN));
		TestDataUtil.createDefaultAdressenForGS(gesuch, false);
		Assert.assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 60));
		gesuch.setEingangsdatum(LocalDate.of(TestDataUtil.PERIODE_JAHR_1, Month.OCTOBER, 7));
		List<VerfuegungZeitabschnitt> result = calculate(betreuung);
		List<VerfuegungZeitabschnitt> nextRestanspruch = EbeguRuleTestsHelper.initializeRestanspruchForNextBetreuung(betreuung, result);
		Assert.assertNotNull(result);
		Assert.assertEquals(4, result.size());

		// August
		VerfuegungZeitabschnitt abschnitt1 = result.get(0);
		Assert.assertEquals(Integer.valueOf(60), abschnitt1.getBgCalculationInputAsiv().getErwerbspensumGS1());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), abschnitt1.getBetreuungspensumProzent());
		Assert.assertEquals(0, abschnitt1.getAnspruchberechtigtesPensum());
		Assert.assertEquals(MathUtil.DEFAULT.from(0), abschnitt1.getBgPensum());
		Assert.assertEquals(80, result.get(0).getBgCalculationInputAsiv().getAnspruchspensumRest());
		Assert.assertEquals(80, nextRestanspruch.get(0).getBgCalculationInputAsiv().getAnspruchspensumRest());
		Assert.assertTrue(abschnitt1.isZuSpaetEingereicht());
		Assert.assertFalse(abschnitt1.getBgCalculationInputAsiv().isBezahltKompletteVollkosten());

		// 1.9.-5.9. 
		VerfuegungZeitabschnitt abschnitt2 = result.get(1);
		Assert.assertEquals(Integer.valueOf(60), abschnitt2.getBgCalculationInputAsiv().getErwerbspensumGS1());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), abschnitt2.getBetreuungspensumProzent());
		Assert.assertEquals(0, abschnitt2.getAnspruchberechtigtesPensum());
		Assert.assertEquals(MathUtil.DEFAULT.from(0), abschnitt2.getBgPensum());
		Assert.assertEquals(80, result.get(0).getBgCalculationInputAsiv().getAnspruchspensumRest());
		Assert.assertEquals(80, nextRestanspruch.get(0).getBgCalculationInputAsiv().getAnspruchspensumRest());
		Assert.assertTrue(abschnitt2.isZuSpaetEingereicht());
		Assert.assertFalse(abschnitt2.getBgCalculationInputAsiv().isBezahltKompletteVollkosten());

		// 6.9-30.9. 
		VerfuegungZeitabschnitt abschnitt3 = result.get(2);
		Assert.assertEquals(Integer.valueOf(60), abschnitt3.getBgCalculationInputAsiv().getErwerbspensumGS1());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), abschnitt3.getBetreuungspensumProzent());
		Assert.assertEquals(60 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, abschnitt3.getAnspruchberechtigtesPensum());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), abschnitt3.getBgPensum());
		Assert.assertEquals(-1, abschnitt3.getBgCalculationInputAsiv().getAnspruchspensumRest());
		Assert.assertEquals(0 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, nextRestanspruch.get(2).getBgCalculationInputAsiv().getAnspruchspensumRest());
		Assert.assertFalse(abschnitt3.isZuSpaetEingereicht());
		Assert.assertFalse(abschnitt3.getBgCalculationInputAsiv().isBezahltKompletteVollkosten());

		// Ab 1.10.
		VerfuegungZeitabschnitt abschnitt4 = result.get(3);
		Assert.assertEquals(Integer.valueOf(60), abschnitt4.getBgCalculationInputAsiv().getErwerbspensumGS1());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), abschnitt4.getBetreuungspensumProzent());
		Assert.assertEquals(60 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, abschnitt4.getAnspruchberechtigtesPensum());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), abschnitt4.getBgPensum());
		Assert.assertEquals(-1, abschnitt4.getBgCalculationInputAsiv().getAnspruchspensumRest());
		Assert.assertEquals(0 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, nextRestanspruch.get(3).getBgCalculationInputAsiv().getAnspruchspensumRest());
		Assert.assertFalse(abschnitt4.isZuSpaetEingereicht());
		Assert.assertFalse(abschnitt4.getBgCalculationInputAsiv().isBezahltKompletteVollkosten());
	}

	/**
	 * Kita: Einreichung am 31.08., Start der Betreuung am 1.8.
	 */
	@Test
	public void testKitaEinreichungNach30TagenNichtZuSpaetAR() {
		Betreuung betreuung = EbeguRuleTestsHelper.createBetreuungWithPensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE,
			BetreuungsangebotTyp.KITA, 60, BigDecimal.valueOf(2000));
		final Gesuch gesuch = betreuung.extractGesuch();
		gesuch.getFall().setMandant(TestDataUtil.createMandant(MandantIdentifier.APPENZELL_AUSSERRHODEN));
		TestDataUtil.createDefaultAdressenForGS(gesuch, false);
		Assert.assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 60));
		gesuch.setEingangsdatum(LocalDate.of(TestDataUtil.PERIODE_JAHR_1, Month.AUGUST, 31));
		List<VerfuegungZeitabschnitt> result = calculate(betreuung);
		List<VerfuegungZeitabschnitt> nextRestanspruch = EbeguRuleTestsHelper.initializeRestanspruchForNextBetreuung(betreuung, result);
		Assert.assertNotNull(result);
		Assert.assertEquals(1, result.size());

		VerfuegungZeitabschnitt abschnitt = result.get(0);
		Assert.assertEquals(Integer.valueOf(60), abschnitt.getBgCalculationInputAsiv().getErwerbspensumGS1());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), abschnitt.getBetreuungspensumProzent());
		Assert.assertEquals(60 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, abschnitt.getAnspruchberechtigtesPensum());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), abschnitt.getBgPensum());
		Assert.assertEquals(-1, abschnitt.getBgCalculationInputAsiv().getAnspruchspensumRest());
		Assert.assertEquals(0 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, nextRestanspruch.get(0).getBgCalculationInputAsiv().getAnspruchspensumRest());
		Assert.assertFalse(abschnitt.isZuSpaetEingereicht());
		Assert.assertFalse(abschnitt.getBgCalculationInputAsiv().isBezahltKompletteVollkosten());

	}

	/**
	 * Kita: Einreichung am 7.8., Start der Betreuung am 1.8.
	 * Der Anspruch beginnt im Folgemonat, also 1.9.
	 */
	@Test
	public void testKitaEinreichungNachBeginnBetreuung() {
		Betreuung betreuung = EbeguRuleTestsHelper.createBetreuungWithPensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE,
			BetreuungsangebotTyp.KITA, 60, BigDecimal.valueOf(2000));
		final Gesuch gesuch = betreuung.extractGesuch();
		TestDataUtil.createDefaultAdressenForGS(gesuch, false);
		Assert.assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 60));
		gesuch.setEingangsdatum(LocalDate.of(TestDataUtil.PERIODE_JAHR_1, Month.AUGUST, 7));
		List<VerfuegungZeitabschnitt> result = calculate(betreuung);
		List<VerfuegungZeitabschnitt> nextRestanspruch = EbeguRuleTestsHelper.initializeRestanspruchForNextBetreuung(betreuung, result);

		Assert.assertNotNull(result);
		Assert.assertEquals(2, result.size());
		VerfuegungZeitabschnitt abschnittVorAnspruch = result.get(0);
		Assert.assertEquals(Integer.valueOf(60), abschnittVorAnspruch.getBgCalculationInputAsiv().getErwerbspensumGS1());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), abschnittVorAnspruch.getBetreuungspensumProzent());
		Assert.assertEquals(0, abschnittVorAnspruch.getAnspruchberechtigtesPensum());
		Assert.assertEquals(MathUtil.DEFAULT.from(0), abschnittVorAnspruch.getBgPensum());
		Assert.assertTrue(abschnittVorAnspruch.isZuSpaetEingereicht());

		VerfuegungZeitabschnitt abschnittMitAnspruch = result.get(1);
		Assert.assertEquals(Integer.valueOf(60), abschnittMitAnspruch.getBgCalculationInputAsiv().getErwerbspensumGS1());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), abschnittMitAnspruch.getBetreuungspensumProzent());
		Assert.assertEquals(60 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, abschnittMitAnspruch.getAnspruchberechtigtesPensum());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), abschnittMitAnspruch.getBgPensum());
		Assert.assertEquals(-1, abschnittMitAnspruch.getBgCalculationInputAsiv().getAnspruchspensumRest());
		Assert.assertEquals(0 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, nextRestanspruch.get(1).getBgCalculationInputAsiv().getAnspruchspensumRest());
		Assert.assertFalse(abschnittMitAnspruch.isZuSpaetEingereicht());
		Assert.assertFalse(abschnittMitAnspruch.getBgCalculationInputAsiv().isBezahltKompletteVollkosten());
	}

	/**
	 * Kita: Einreichung am 7.8., Start der Betreuung am 5.8.
	 * Anspruch ab 1.9.
	 */
	@Test
	public void testKitaEinreichungInZuSpaetAberNachDemErsten() {
		LocalDate betreuungsStart = LocalDate.of(TestDataUtil.PERIODE_JAHR_1, Month.AUGUST, 8);
		Betreuung betreuung = EbeguRuleTestsHelper.createBetreuungWithPensum(betreuungsStart, TestDataUtil.ENDE_PERIODE, BetreuungsangebotTyp.KITA,
			60, BigDecimal.valueOf(2000));
		final Gesuch gesuch = betreuung.extractGesuch();
		TestDataUtil.createDefaultAdressenForGS(gesuch, false);
		Assert.assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 60));

		gesuch.setEingangsdatum(LocalDate.of(TestDataUtil.PERIODE_JAHR_1, Month.AUGUST, 7));
		List<VerfuegungZeitabschnitt> result = calculate(betreuung);
		List<VerfuegungZeitabschnitt> nextRestanspruch = EbeguRuleTestsHelper.initializeRestanspruchForNextBetreuung(betreuung, result);

		Assert.assertNotNull(result);
		Assert.assertEquals(3, result.size());

		VerfuegungZeitabschnitt abschnitt3 = result.get(2);
		Assert.assertEquals(betreuungsStart.plusMonths(1).withDayOfMonth(1), abschnitt3.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(Integer.valueOf(60), abschnitt3.getBgCalculationInputAsiv().getErwerbspensumGS1());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), abschnitt3.getBetreuungspensumProzent());
		Assert.assertEquals(60 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, abschnitt3.getAnspruchberechtigtesPensum());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), abschnitt3.getBgPensum());
		Assert.assertEquals(0 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, nextRestanspruch.get(2).getBgCalculationInputAsiv().getAnspruchspensumRest());
		Assert.assertFalse(abschnitt3.isZuSpaetEingereicht());
		Assert.assertFalse(abschnitt3.getBgCalculationInputAsiv().isBezahltKompletteVollkosten());
	}

	/**
	 * Kita: Einreichung am 7.10.
	 * Kita: 20.12. - 15.03.
	 * EWP1: 01.08. - 30.08.
	 * EWP2: 01.09. - 31.07.
	 *
	 * Erwartet =>
	 * 1: 01.08. - 31.10. (Vor Kita, Vor Anspruch)
	 * 1: 01.11. - 19.12. (Vor Kita, mit Anspruch)
	 * 2: 20.12. - 15.03. (Kita, Anspruch)
	 * 3: 16.03. - 31.07. (Nach Kita)
	 */
	@Test
	public void testKitaBetreuungspensumInnerhalbGesuchsperiode() {
		final LocalDate betreuungStart = LocalDate.of(TestDataUtil.START_PERIODE.getYear(), 12, 20);
		final LocalDate betreuungEnde = LocalDate.of(TestDataUtil.START_PERIODE.plusYears(1).getYear(), 3, 15);
		Betreuung betreuung = EbeguRuleTestsHelper.createBetreuungWithPensum(
			betreuungStart, betreuungEnde, BetreuungsangebotTyp.KITA, 60, BigDecimal.valueOf(2000));
		LocalDate pensumAEnd = TestDataUtil.START_PERIODE.plusMonths(1).minusDays(1);
		LocalDate pensumBStart = pensumAEnd.plusDays(1);
		Gesuch gesuch = betreuung.extractGesuch();
		Assert.assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, pensumAEnd, 50));
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(pensumBStart, TestDataUtil.ENDE_PERIODE, 60));

		LocalDate eingangsdatum = LocalDate.of(TestDataUtil.PERIODE_JAHR_1, Month.OCTOBER, 7);
		LocalDate stichtagAnspruch = eingangsdatum.plusMonths(1).withDayOfMonth(1);
		gesuch.setEingangsdatum(eingangsdatum);
		List<VerfuegungZeitabschnitt> result = calculate(betreuung);
		List<VerfuegungZeitabschnitt> nextRestanspruch = EbeguRuleTestsHelper.initializeRestanspruchForNextBetreuung(betreuung, result);
		Assert.assertNotNull(result);
		Assert.assertEquals(6, result.size());

		int abschnittNr = 0;
		VerfuegungZeitabschnitt abschnitt0 = result.get(abschnittNr);
		Assert.assertEquals(TestDataUtil.START_PERIODE, abschnitt0.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(stichtagAnspruch.minusDays(1), abschnitt0.getGueltigkeit().getGueltigBis());
		Assert.assertEquals(Integer.valueOf(50), abschnitt0.getBgCalculationInputAsiv().getErwerbspensumGS1());
		Assert.assertNull(abschnitt0.getBgCalculationInputAsiv().getErwerbspensumGS2());
		Assert.assertEquals(ZERO, abschnitt0.getBetreuungspensumProzent());
		Assert.assertEquals(0, abschnitt0.getAnspruchberechtigtesPensum());
		Assert.assertEquals(ZERO, abschnitt0.getBgPensum());
		Assert.assertEquals(70, abschnitt0.getBgCalculationInputAsiv().getAnspruchspensumRest());
		Assert.assertEquals(70, nextRestanspruch.get(abschnittNr).getBgCalculationInputAsiv().getAnspruchspensumRest());
		Assert.assertTrue(abschnitt0.isZuSpaetEingereicht());
		Assert.assertFalse(abschnitt0.getBgCalculationInputAsiv().isBezahltKompletteVollkosten());

		abschnittNr++;
		VerfuegungZeitabschnitt abschnitt1 = result.get(abschnittNr);
		Assert.assertEquals(stichtagAnspruch, abschnitt1.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(betreuungStart.minusDays(1), abschnitt1.getGueltigkeit().getGueltigBis());
		Assert.assertEquals(Integer.valueOf(60), abschnitt1.getBgCalculationInputAsiv().getErwerbspensumGS1());
		Assert.assertNull(abschnitt1.getBgCalculationInputAsiv().getErwerbspensumGS2());
		Assert.assertEquals(ZERO, abschnitt1.getBetreuungspensumProzent());
		Assert.assertEquals(60 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, abschnitt1.getAnspruchberechtigtesPensum());
		Assert.assertEquals(ZERO, abschnitt1.getBgPensum());
		Assert.assertEquals(-1, abschnitt1.getBgCalculationInputAsiv().getAnspruchspensumRest());
		Assert.assertEquals(-1, nextRestanspruch.get(abschnittNr).getBgCalculationInputAsiv().getAnspruchspensumRest());
		Assert.assertFalse(abschnitt1.isZuSpaetEingereicht());
		Assert.assertFalse(abschnitt1.getBgCalculationInputAsiv().isBezahltKompletteVollkosten());

		abschnittNr++;
		VerfuegungZeitabschnitt abschnitt2a = result.get(abschnittNr);
		abschnittNr++;
		abschnittNr++;
		VerfuegungZeitabschnitt abschnitt2c = result.get(abschnittNr);
		Assert.assertEquals(betreuungStart, abschnitt2a.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(betreuungEnde, abschnitt2c.getGueltigkeit().getGueltigBis());
		Assert.assertEquals(Integer.valueOf(60), abschnitt2a.getBgCalculationInputAsiv().getErwerbspensumGS1());
		Assert.assertNull(abschnitt2a.getBgCalculationInputAsiv().getErwerbspensumGS2());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), abschnitt2a.getBetreuungspensumProzent());
		Assert.assertEquals(60 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, abschnitt2a.getAnspruchberechtigtesPensum());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), abschnitt2a.getBgPensum());
		Assert.assertEquals(-1, abschnitt2a.getBgCalculationInputAsiv().getAnspruchspensumRest());
		Assert.assertEquals(0 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, nextRestanspruch.get(abschnittNr).getBgCalculationInputAsiv().getAnspruchspensumRest());
		Assert.assertFalse(abschnitt2a.isZuSpaetEingereicht());
		Assert.assertFalse(abschnitt2a.getBgCalculationInputAsiv().isBezahltKompletteVollkosten());

		abschnittNr++;
		VerfuegungZeitabschnitt abschnitt3 = result.get(abschnittNr);
		Assert.assertEquals(betreuungEnde.plusDays(1), abschnitt3.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(TestDataUtil.ENDE_PERIODE, abschnitt3.getGueltigkeit().getGueltigBis());
		Assert.assertEquals(Integer.valueOf(60), abschnitt3.getBgCalculationInputAsiv().getErwerbspensumGS1());
		Assert.assertNull(abschnitt3.getBgCalculationInputAsiv().getErwerbspensumGS2());
		Assert.assertEquals(ZERO, abschnitt3.getBetreuungspensumProzent());
		Assert.assertEquals(60 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, abschnitt3.getAnspruchberechtigtesPensum());
		Assert.assertEquals(ZERO, abschnitt3.getBgPensum());
		Assert.assertEquals(-1, abschnitt3.getBgCalculationInputAsiv().getAnspruchspensumRest());
		Assert.assertEquals(-1, nextRestanspruch.get(abschnittNr).getBgCalculationInputAsiv().getAnspruchspensumRest());
		Assert.assertFalse(abschnitt3.isZuSpaetEingereicht());
		Assert.assertFalse(abschnitt3.getBgCalculationInputAsiv().isBezahltKompletteVollkosten());
	}

	/**
	 * Kita: Einreichung am 7.10., Start der Betreuung am 1.8.
	 *  Anspruch ab 01.02.2018
	 *  Kita: 20.12. - 15.03.
	 *  EWP1: 01.08. - 30.08.
	 *  EWP2: 01.09. - 31.07.
	 *
	 *  Erwartet =>
	 *  1: 01.08. - 19.12. (Vor Kita, vor Anspruch)
	 *  2: 20.12. - 31.01. (Mit Kita, vor Anspruch)
	 *  3: 01.02. - 15.03. (Kita, Anspruch)
	 *  4. 16.03. - 31.07. (Nach Kita)
	 */
	@Test
	public void testKitaBetreuungspensumInnerhalbGesuchsperiodeZuSpaetEingereicht() {
		// Eingangsdatum 10.01.2018 => d.h. genereller Anspruch ab 01.02.2018
		final LocalDate eingangsdatum = LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.JANUARY, 10);
		final LocalDate startAnspruch = eingangsdatum.plusMonths(1).withDayOfMonth(1);
		// Betreuung vom 20.12.2017 - 15.03.2018
		DateRange betreuungRange = new DateRange(
			LocalDate.of(TestDataUtil.START_PERIODE.getYear(), 12, 20),
			LocalDate.of(TestDataUtil.START_PERIODE.plusYears(1).getYear(), 3, 15)
		);
		Betreuung betreuung = EbeguRuleTestsHelper.createBetreuungWithPensum(
			betreuungRange.getGueltigAb(), betreuungRange.getGueltigBis(), BetreuungsangebotTyp.KITA, 60, BigDecimal.valueOf(2000));

		// Erwerbspensum1: 01.08.2017 - 31.08.2017: 50%
		DateRange ewpRange1 = new DateRange(
			TestDataUtil.START_PERIODE,
			TestDataUtil.START_PERIODE.plusMonths(1).minusDays(1)
		);
		// Erwerbspensum2: 01.09.2017 - 31.07.2018: 60%
		DateRange ewpRange2 = new DateRange(
			ewpRange1.getGueltigBis().plusDays(1),
			TestDataUtil.ENDE_PERIODE
		);

		Gesuch gesuch = betreuung.extractGesuch();
		Assert.assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(ewpRange1.getGueltigAb(), ewpRange1.getGueltigBis(), 50));
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(ewpRange2.getGueltigAb(), ewpRange2.getGueltigBis(), 60));

		gesuch.setEingangsdatum(eingangsdatum);
		List<VerfuegungZeitabschnitt> result = calculate(betreuung);
		List<VerfuegungZeitabschnitt> nextRestanspruch = EbeguRuleTestsHelper.initializeRestanspruchForNextBetreuung(betreuung, result);
		Assert.assertNotNull(result);
		Assert.assertEquals(6, result.size());

		// Neu werden die Rules in den Tests gleich ausgefuehrt wie im echten Evaluator.
		// Der Normalizer vergleicht alle sichtbaren Daten, also auch "betreuungspensumZeiteinheit"
		// Dies ist bei untermonatlichen Abschnitten immer anders als der vorangehende oder nachfolgende Abschnitt, es werden
		// also tendentiell zuwenige Abschnitte gemergt, wenn man die Monate "wegoptimieren" moechte

		int abschnittNr = 0;
		VerfuegungZeitabschnitt abschnittVorKita = result.get(abschnittNr);
		Assert.assertEquals(ewpRange1.getGueltigAb(), abschnittVorKita.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(betreuungRange.getGueltigAb().minusDays(1), abschnittVorKita.getGueltigkeit().getGueltigBis());
		Assert.assertEquals(Integer.valueOf(50), abschnittVorKita.getBgCalculationInputAsiv().getErwerbspensumGS1());
		Assert.assertNull(abschnittVorKita.getBgCalculationInputAsiv().getErwerbspensumGS2());
		Assert.assertEquals(MathUtil.DEFAULT.from(0), abschnittVorKita.getBetreuungspensumProzent());
		Assert.assertEquals(0, abschnittVorKita.getAnspruchberechtigtesPensum());
		Assert.assertEquals(ZERO, abschnittVorKita.getBgPensum());
		Assert.assertEquals(70, abschnittVorKita.getBgCalculationInputAsiv().getAnspruchspensumRest());
		Assert.assertEquals(70, nextRestanspruch.get(abschnittNr).getBgCalculationInputAsiv().getAnspruchspensumRest());
		Assert.assertTrue(abschnittVorKita.isZuSpaetEingereicht());
		Assert.assertFalse(abschnittVorKita.getBgCalculationInputAsiv().isBezahltKompletteVollkosten());

		abschnittNr++;
		VerfuegungZeitabschnitt abschnittKitaZuSpaetEingereicht1 = result.get(abschnittNr);
		abschnittNr++;
		VerfuegungZeitabschnitt abschnittKitaZuSpaetEingereicht2 = result.get(abschnittNr);
		Assert.assertEquals(betreuungRange.getGueltigAb(), abschnittKitaZuSpaetEingereicht1.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(startAnspruch.minusDays(1), abschnittKitaZuSpaetEingereicht2.getGueltigkeit().getGueltigBis());
		Assert.assertEquals(Integer.valueOf(60), abschnittKitaZuSpaetEingereicht1.getBgCalculationInputAsiv().getErwerbspensumGS1());
		Assert.assertNull(abschnittKitaZuSpaetEingereicht1.getBgCalculationInputAsiv().getErwerbspensumGS2());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), abschnittKitaZuSpaetEingereicht1.getBetreuungspensumProzent());
		Assert.assertEquals(0, abschnittKitaZuSpaetEingereicht1.getAnspruchberechtigtesPensum());
		Assert.assertEquals(MathUtil.DEFAULT.from(0), abschnittKitaZuSpaetEingereicht1.getBgPensum());
		Assert.assertEquals(80, abschnittKitaZuSpaetEingereicht1.getBgCalculationInputAsiv().getAnspruchspensumRest());
		Assert.assertEquals(80, nextRestanspruch.get(abschnittNr).getBgCalculationInputAsiv().getAnspruchspensumRest());
		Assert.assertTrue(abschnittKitaZuSpaetEingereicht1.isZuSpaetEingereicht());
		Assert.assertFalse(abschnittKitaZuSpaetEingereicht1.getBgCalculationInputAsiv().isBezahltKompletteVollkosten());

		abschnittNr++;
		VerfuegungZeitabschnitt abschnittAnspruch1 = result.get(abschnittNr);
		abschnittNr++;
		VerfuegungZeitabschnitt abschnittAnspruch2 = result.get(abschnittNr);
		Assert.assertEquals(startAnspruch, abschnittAnspruch1.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(betreuungRange.getGueltigBis(), abschnittAnspruch2.getGueltigkeit().getGueltigBis());
		Assert.assertEquals(Integer.valueOf(60), abschnittAnspruch1.getBgCalculationInputAsiv().getErwerbspensumGS1());
		Assert.assertNull(abschnittAnspruch1.getBgCalculationInputAsiv().getErwerbspensumGS2());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), abschnittAnspruch1.getBetreuungspensumProzent());
		Assert.assertEquals(60 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, abschnittAnspruch1.getAnspruchberechtigtesPensum());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), abschnittAnspruch1.getBgPensum());
		Assert.assertEquals(-1, abschnittAnspruch1.getBgCalculationInputAsiv().getAnspruchspensumRest());
		Assert.assertEquals(0 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, nextRestanspruch.get(abschnittNr).getBgCalculationInputAsiv().getAnspruchspensumRest());
		Assert.assertFalse(abschnittAnspruch1.isZuSpaetEingereicht());
		Assert.assertFalse(abschnittAnspruch1.getBgCalculationInputAsiv().isBezahltKompletteVollkosten());

		abschnittNr++;
		VerfuegungZeitabschnitt abschnittNachKita = result.get(abschnittNr);
		Assert.assertEquals(betreuungRange.getGueltigBis().plusDays(1), abschnittNachKita.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(TestDataUtil.ENDE_PERIODE, abschnittNachKita.getGueltigkeit().getGueltigBis());
		Assert.assertEquals(Integer.valueOf(60), abschnittNachKita.getBgCalculationInputAsiv().getErwerbspensumGS1());
		Assert.assertNull(abschnittNachKita.getBgCalculationInputAsiv().getErwerbspensumGS2());
		Assert.assertEquals(ZERO, abschnittNachKita.getBetreuungspensumProzent());
		Assert.assertEquals(60 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, abschnittNachKita.getAnspruchberechtigtesPensum());
		Assert.assertEquals(ZERO, abschnittNachKita.getBgPensum());
		Assert.assertEquals(-1, abschnittNachKita.getBgCalculationInputAsiv().getAnspruchspensumRest());
		Assert.assertEquals(-1, nextRestanspruch.get(abschnittNr).getBgCalculationInputAsiv().getAnspruchspensumRest());
		Assert.assertFalse(abschnittNachKita.isZuSpaetEingereicht());
		Assert.assertFalse(abschnittNachKita.getBgCalculationInputAsiv().isBezahltKompletteVollkosten());
	}

}
