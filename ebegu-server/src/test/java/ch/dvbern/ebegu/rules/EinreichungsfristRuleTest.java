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
import org.junit.Assert;
import org.junit.Test;

import static ch.dvbern.ebegu.rules.EbeguRuleTestsHelper.calculate;
import static ch.dvbern.ebegu.util.Constants.ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS;

/**
 * Tests für Einreichungsfrist-Regel
 */
public class EinreichungsfristRuleTest extends AbstractBGRechnerTest {

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
		Assert.assertEquals(Integer.valueOf(60), result.get(0).getErwerbspensumGS1());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), result.get(0).getBetreuungspensum());
		Assert.assertEquals(60 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, result.get(0).getAnspruchberechtigtesPensum());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), result.get(0).getBgPensum());
		Assert.assertEquals(0 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, nextRestanspruch.get(0).getAnspruchspensumRest());
		Assert.assertFalse(result.get(0).isZuSpaetEingereicht());
		Assert.assertFalse(result.get(0).isBezahltVollkosten());
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
		Assert.assertEquals(Integer.valueOf(60), abschnitt1.getErwerbspensumGS1());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), abschnitt1.getBetreuungspensum());
		Assert.assertEquals(0, abschnitt1.getAnspruchberechtigtesPensum());
		Assert.assertEquals(MathUtil.DEFAULT.from(0), abschnitt1.getBgPensum());
		Assert.assertEquals(-1, result.get(0).getAnspruchspensumRest());
		Assert.assertEquals(0, nextRestanspruch.get(0).getAnspruchspensumRest());
		Assert.assertTrue(abschnitt1.isZuSpaetEingereicht());
		Assert.assertFalse(abschnitt1.isBezahltVollkosten());

		VerfuegungZeitabschnitt abschnitt2 = result.get(1);
		Assert.assertEquals(Integer.valueOf(60), abschnitt2.getErwerbspensumGS1());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), abschnitt2.getBetreuungspensum());
		Assert.assertEquals(60 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, abschnitt2.getAnspruchberechtigtesPensum());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), abschnitt2.getBgPensum());
		Assert.assertEquals(-1, abschnitt2.getAnspruchspensumRest());
		Assert.assertEquals(0 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, nextRestanspruch.get(1).getAnspruchspensumRest());
		Assert.assertFalse(abschnitt2.isZuSpaetEingereicht());
		Assert.assertFalse(abschnitt2.isBezahltVollkosten());
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
		Assert.assertEquals(Integer.valueOf(60), abschnittVorAnspruch.getErwerbspensumGS1());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), abschnittVorAnspruch.getBetreuungspensum());
		Assert.assertEquals(0, abschnittVorAnspruch.getAnspruchberechtigtesPensum());
		Assert.assertEquals(MathUtil.DEFAULT.from(0), abschnittVorAnspruch.getBgPensum());
		Assert.assertTrue(abschnittVorAnspruch.isZuSpaetEingereicht());

		VerfuegungZeitabschnitt abschnittMitAnspruch = result.get(1);
		Assert.assertEquals(Integer.valueOf(60), abschnittMitAnspruch.getErwerbspensumGS1());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), abschnittMitAnspruch.getBetreuungspensum());
		Assert.assertEquals(60 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, abschnittMitAnspruch.getAnspruchberechtigtesPensum());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), abschnittMitAnspruch.getBgPensum());
		Assert.assertEquals(-1, abschnittMitAnspruch.getAnspruchspensumRest());
		Assert.assertEquals(0 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, nextRestanspruch.get(1).getAnspruchspensumRest());
		Assert.assertFalse(abschnittMitAnspruch.isZuSpaetEingereicht());
		Assert.assertFalse(abschnittMitAnspruch.isBezahltVollkosten());
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
		Assert.assertEquals(Integer.valueOf(60), abschnitt3.getErwerbspensumGS1());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), abschnitt3.getBetreuungspensum());
		Assert.assertEquals(60 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, abschnitt3.getAnspruchberechtigtesPensum());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), abschnitt3.getBgPensum());
		Assert.assertEquals(0 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, nextRestanspruch.get(2).getAnspruchspensumRest());
		Assert.assertFalse(abschnitt3.isZuSpaetEingereicht());
		Assert.assertFalse(abschnitt3.isBezahltVollkosten());
	}

	/**
	 * Kita: Einreichung am 7.10., Start der Betreuung am 1.8.
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

		gesuch.setEingangsdatum(LocalDate.of(TestDataUtil.PERIODE_JAHR_1, Month.OCTOBER, 7));
		List<VerfuegungZeitabschnitt> result = calculate(betreuung);
		List<VerfuegungZeitabschnitt> nextRestanspruch = EbeguRuleTestsHelper.initializeRestanspruchForNextBetreuung(betreuung, result);
		Assert.assertNotNull(result);
		Assert.assertEquals(4, result.size());

		VerfuegungZeitabschnitt abschnitt0 = result.get(0);
		Assert.assertEquals(TestDataUtil.START_PERIODE, abschnitt0.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(pensumAEnd, abschnitt0.getGueltigkeit().getGueltigBis());
		Assert.assertEquals(Integer.valueOf(50), abschnitt0.getErwerbspensumGS1());
		Assert.assertNull(abschnitt0.getErwerbspensumGS2());
		Assert.assertEquals(BigDecimal.ZERO, abschnitt0.getBetreuungspensum());
		Assert.assertEquals(50 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, abschnitt0.getAnspruchberechtigtesPensum());
		Assert.assertEquals(BigDecimal.ZERO, abschnitt0.getBgPensum());
		Assert.assertEquals(-1, result.get(0).getAnspruchspensumRest());
		Assert.assertEquals(50 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, nextRestanspruch.get(0).getAnspruchspensumRest());
		Assert.assertFalse(abschnitt0.isZuSpaetEingereicht());
		Assert.assertFalse(abschnitt0.isBezahltVollkosten());

		VerfuegungZeitabschnitt abschnitt1 = result.get(1);
		Assert.assertEquals(pensumBStart, abschnitt1.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(betreuungStart.minusDays(1), abschnitt1.getGueltigkeit().getGueltigBis());
		Assert.assertEquals(Integer.valueOf(60), abschnitt1.getErwerbspensumGS1());
		Assert.assertNull(abschnitt1.getErwerbspensumGS2());
		Assert.assertEquals(BigDecimal.ZERO, abschnitt1.getBetreuungspensum());
		Assert.assertEquals(60 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, abschnitt1.getAnspruchberechtigtesPensum());
		Assert.assertEquals(BigDecimal.ZERO, abschnitt1.getBgPensum());
		Assert.assertEquals(-1, result.get(0).getAnspruchspensumRest());
		Assert.assertEquals(60 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, nextRestanspruch.get(1).getAnspruchspensumRest());
		Assert.assertFalse(abschnitt1.isZuSpaetEingereicht());
		Assert.assertFalse(abschnitt1.isBezahltVollkosten());

		VerfuegungZeitabschnitt abschnitt2 = result.get(2);
		Assert.assertEquals(betreuungStart, abschnitt2.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(betreuungEnde, abschnitt2.getGueltigkeit().getGueltigBis());
		Assert.assertEquals(Integer.valueOf(60), abschnitt2.getErwerbspensumGS1());
		Assert.assertNull(abschnitt2.getErwerbspensumGS2());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), abschnitt2.getBetreuungspensum());
		Assert.assertEquals(60 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, abschnitt2.getAnspruchberechtigtesPensum());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), abschnitt2.getBgPensum());
		Assert.assertEquals(-1, abschnitt2.getAnspruchspensumRest());
		Assert.assertEquals(0 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, nextRestanspruch.get(2).getAnspruchspensumRest());
		Assert.assertFalse(abschnitt2.isZuSpaetEingereicht());
		Assert.assertFalse(abschnitt2.isBezahltVollkosten());

		VerfuegungZeitabschnitt abschnitt3 = result.get(3);
		Assert.assertEquals(betreuungEnde.plusDays(1), abschnitt3.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(TestDataUtil.ENDE_PERIODE, abschnitt3.getGueltigkeit().getGueltigBis());
		Assert.assertEquals(Integer.valueOf(60), abschnitt3.getErwerbspensumGS1());
		Assert.assertNull(abschnitt3.getErwerbspensumGS2());
		Assert.assertEquals(BigDecimal.ZERO, abschnitt3.getBetreuungspensum());
		Assert.assertEquals(60 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, abschnitt3.getAnspruchberechtigtesPensum());
		Assert.assertEquals(BigDecimal.ZERO, abschnitt3.getBgPensum());
		Assert.assertEquals(-1, abschnitt3.getAnspruchspensumRest());
		Assert.assertEquals(60 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, nextRestanspruch.get(3).getAnspruchspensumRest());
		Assert.assertFalse(abschnitt3.isZuSpaetEingereicht());
		Assert.assertFalse(abschnitt3.isBezahltVollkosten());
	}

	/**
	 * Kita: Einreichung am 7.10., Start der Betreuung am 1.8.
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
		Assert.assertEquals(5, result.size());

		VerfuegungZeitabschnitt abschnittEwp1 = result.get(0);
		Assert.assertEquals(ewpRange1.getGueltigAb(), abschnittEwp1.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(ewpRange1.getGueltigBis(), abschnittEwp1.getGueltigkeit().getGueltigBis());
		Assert.assertEquals(Integer.valueOf(50), abschnittEwp1.getErwerbspensumGS1());
		Assert.assertNull(abschnittEwp1.getErwerbspensumGS2());
		Assert.assertEquals(BigDecimal.ZERO, abschnittEwp1.getBetreuungspensum());
		Assert.assertEquals(0, abschnittEwp1.getAnspruchberechtigtesPensum());
		Assert.assertEquals(BigDecimal.ZERO, abschnittEwp1.getBgPensum());
		Assert.assertEquals(-1, abschnittEwp1.getAnspruchspensumRest());
		Assert.assertEquals(0, nextRestanspruch.get(0).getAnspruchspensumRest());
		Assert.assertTrue(abschnittEwp1.isZuSpaetEingereicht());
		Assert.assertFalse(abschnittEwp1.isBezahltVollkosten());

		VerfuegungZeitabschnitt abschnittEwp2_vorBetreuung = result.get(1);
		Assert.assertEquals(ewpRange2.getGueltigAb(), abschnittEwp2_vorBetreuung.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(betreuungRange.getGueltigAb().minusDays(1), abschnittEwp2_vorBetreuung.getGueltigkeit().getGueltigBis());
		Assert.assertEquals(Integer.valueOf(60), abschnittEwp2_vorBetreuung.getErwerbspensumGS1());
		Assert.assertNull(abschnittEwp2_vorBetreuung.getErwerbspensumGS2());
		Assert.assertEquals(BigDecimal.ZERO, abschnittEwp2_vorBetreuung.getBetreuungspensum());
		Assert.assertEquals(0, abschnittEwp2_vorBetreuung.getAnspruchberechtigtesPensum());
		Assert.assertEquals(BigDecimal.ZERO, abschnittEwp2_vorBetreuung.getBgPensum());
		Assert.assertEquals(-1, abschnittEwp2_vorBetreuung.getAnspruchspensumRest());
		Assert.assertEquals(0, nextRestanspruch.get(1).getAnspruchspensumRest());
		Assert.assertTrue(abschnittEwp2_vorBetreuung.isZuSpaetEingereicht());
		Assert.assertFalse(abschnittEwp2_vorBetreuung.isBezahltVollkosten());

		VerfuegungZeitabschnitt abschnittBetreuung_vorAnspruch = result.get(2);
		Assert.assertEquals(betreuungRange.getGueltigAb(), abschnittBetreuung_vorAnspruch.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(startAnspruch.minusDays(1), abschnittBetreuung_vorAnspruch.getGueltigkeit().getGueltigBis());
		Assert.assertEquals(Integer.valueOf(60), abschnittBetreuung_vorAnspruch.getErwerbspensumGS1());
		Assert.assertNull(abschnittBetreuung_vorAnspruch.getErwerbspensumGS2());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), abschnittBetreuung_vorAnspruch.getBetreuungspensum());
		Assert.assertEquals(0, abschnittBetreuung_vorAnspruch.getAnspruchberechtigtesPensum());
		Assert.assertEquals(MathUtil.DEFAULT.from(0), abschnittBetreuung_vorAnspruch.getBgPensum());
		Assert.assertEquals(-1, abschnittBetreuung_vorAnspruch.getAnspruchspensumRest());
		Assert.assertEquals(0, nextRestanspruch.get(2).getAnspruchspensumRest());
		Assert.assertTrue(abschnittBetreuung_vorAnspruch.isZuSpaetEingereicht());
		Assert.assertFalse(abschnittBetreuung_vorAnspruch.isBezahltVollkosten());

		VerfuegungZeitabschnitt abschnittAnspruch_bisEndeBetreuung = result.get(3);
		Assert.assertEquals(startAnspruch, abschnittAnspruch_bisEndeBetreuung.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(betreuungRange.getGueltigBis(), abschnittAnspruch_bisEndeBetreuung.getGueltigkeit().getGueltigBis());
		Assert.assertEquals(Integer.valueOf(60), abschnittAnspruch_bisEndeBetreuung.getErwerbspensumGS1());
		Assert.assertNull(abschnittAnspruch_bisEndeBetreuung.getErwerbspensumGS2());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), abschnittAnspruch_bisEndeBetreuung.getBetreuungspensum());
		Assert.assertEquals(60 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, abschnittAnspruch_bisEndeBetreuung.getAnspruchberechtigtesPensum());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), abschnittAnspruch_bisEndeBetreuung.getBgPensum());
		Assert.assertEquals(-1, abschnittAnspruch_bisEndeBetreuung.getAnspruchspensumRest());
		Assert.assertEquals(0 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, nextRestanspruch.get(3).getAnspruchspensumRest());
		Assert.assertFalse(abschnittAnspruch_bisEndeBetreuung.isZuSpaetEingereicht());
		Assert.assertFalse(abschnittAnspruch_bisEndeBetreuung.isBezahltVollkosten());

		VerfuegungZeitabschnitt abschnittNachBetreuung = result.get(4);
		Assert.assertEquals(betreuungRange.getGueltigBis().plusDays(1), abschnittNachBetreuung.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(TestDataUtil.ENDE_PERIODE, abschnittNachBetreuung.getGueltigkeit().getGueltigBis());
		Assert.assertEquals(Integer.valueOf(60), abschnittNachBetreuung.getErwerbspensumGS1());
		Assert.assertNull(abschnittNachBetreuung.getErwerbspensumGS2());
		Assert.assertEquals(BigDecimal.ZERO, abschnittNachBetreuung.getBetreuungspensum());
		Assert.assertEquals(60 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, abschnittNachBetreuung.getAnspruchberechtigtesPensum());
		Assert.assertEquals(BigDecimal.ZERO, abschnittNachBetreuung.getBgPensum());
		Assert.assertEquals(-1, abschnittNachBetreuung.getAnspruchspensumRest());
		Assert.assertEquals(60 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, nextRestanspruch.get(4).getAnspruchspensumRest());
		Assert.assertFalse(abschnittNachBetreuung.isZuSpaetEingereicht());
		Assert.assertFalse(abschnittNachBetreuung.isBezahltVollkosten());
	}

}
