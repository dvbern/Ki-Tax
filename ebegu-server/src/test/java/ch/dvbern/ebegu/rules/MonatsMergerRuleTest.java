/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.rules;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import ch.dvbern.ebegu.entities.Abwesenheit;
import ch.dvbern.ebegu.entities.AbwesenheitContainer;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.ErwerbspensumContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.types.DateRange;
import org.junit.Assert;
import org.junit.Test;

import static ch.dvbern.ebegu.enums.EinstellungKey.ANSPRUCH_MONATSWEISE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class MonatsMergerRuleTest {

	private final LocalDate SEPTEMBER_19 = LocalDate.of(TestDataUtil.PERIODE_JAHR_1, Month.SEPTEMBER, 19);
	private final LocalDate OCTOBER_12 = LocalDate.of(TestDataUtil.PERIODE_JAHR_1, Month.OCTOBER, 12);
	private final LocalDate DECEMBER_8 = LocalDate.of(TestDataUtil.PERIODE_JAHR_1, Month.DECEMBER, 8);
	private final LocalDate JANUAR_26 = LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.JANUARY, 26);

	@Test
	public void testMonatsMergerRuleDeaktiv() {
		//Pensum Ends during Month
		ErwerbspensumContainer erwerbspensumContainer = TestDataUtil.createErwerbspensum( TestDataUtil.START_PERIODE,
			OCTOBER_12, 60);

		//Rules Anwenden
		List<VerfuegungZeitabschnitt>
			result = createGesuchAndCalculateZeitabschnitt(false, false, erwerbspensumContainer);

		//Soll: 13 Zeitabschnitte -> Zeitabschnitte werden nicht gemerget
		assertEquals(13, result.size());
		assertEquals(OCTOBER_12, result.get(2).getGueltigkeit().getGueltigBis());
		assertEquals(OCTOBER_12.plusDays(1), result.get(3).getGueltigkeit().getGueltigAb());
	}

	@Test
	public void testMonatsMergerRuleAktiv() {
		//Pensum Ends during Month
		ErwerbspensumContainer erwerbspensumContainer = TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE,
			OCTOBER_12, 60);

		//Rules Anwenden
		List<VerfuegungZeitabschnitt>
			result = createGesuchAndCalculateZeitabschnitt(true, false, erwerbspensumContainer);

		assertZeitabschnitteFullMonth(result);
	}

	@Test
	public void testErwerpspensumEndsDuringMonthMergeC() {
		ErwerbspensumContainer erwerbspensumContainer = TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE,
			OCTOBER_12, 60);
		List<VerfuegungZeitabschnitt> result = createGesuchAndCalculateZeitabschnitt(true, false, erwerbspensumContainer);
		assertZeitabschnitteFullMonth(result);

		//Result Zeitabschnitt August, 60%
		final VerfuegungZeitabschnitt verfuegungZeitabschnittAugust = getVerfuegungZeitabschnittGueltigInMonth(result, Month.AUGUST);
		assertNotNull(verfuegungZeitabschnittAugust.getBgCalculationInputAsiv().getErwerbspensumGS1());
		assertEquals(60, verfuegungZeitabschnittAugust.getBgCalculationInputAsiv().getErwerbspensumGS1().intValue());

		//Result Zeitabschnitt Oktober, 23%
		final VerfuegungZeitabschnitt verfuegungZeitabschnittOctober = getVerfuegungZeitabschnittGueltigInMonth(result, Month.OCTOBER);
		assertNotNull(verfuegungZeitabschnittOctober.getBgCalculationInputAsiv().getErwerbspensumGS1());
		assertEquals(23, verfuegungZeitabschnittOctober.getBgCalculationInputAsiv().getErwerbspensumGS1().intValue());

		//RESULT Zeitabschnitt November 0%
		final VerfuegungZeitabschnitt verfuegungZeitabschnittNovember = getVerfuegungZeitabschnittGueltigInMonth(result, Month.NOVEMBER);
		assertNull(verfuegungZeitabschnittNovember.getBgCalculationInputAsiv().getErwerbspensumGS1());
	}

	@Test
	public void test_EWP_erhoehen() {
		//TEST PENSUM: 01.08-08-12, 40% und 09.12-31.07,60%
		LocalDate startDateP1 = TestDataUtil.START_PERIODE;
		LocalDate endDateP1 = DECEMBER_8;
		ErwerbspensumContainer ewp1 = TestDataUtil.createErwerbspensum(startDateP1, endDateP1, 40);

		LocalDate startDateP2 = DECEMBER_8.plusDays(1);
		LocalDate endDateP2 = TestDataUtil.ENDE_PERIODE;
		ErwerbspensumContainer ewp2 = TestDataUtil.createErwerbspensum(startDateP2, endDateP2, 60);

		List<VerfuegungZeitabschnitt> result = createGesuchAndCalculateZeitabschnitt(true, false, ewp1, ewp2);
		assertZeitabschnitteFullMonth(result);

		//RESULT Zeitabschnitt November, 60%
		final VerfuegungZeitabschnitt verfuegungZeitabschnittNovember = getVerfuegungZeitabschnittGueltigInMonth(result, Month.NOVEMBER);
		assertNotNull(verfuegungZeitabschnittNovember.getBgCalculationInputAsiv().getErwerbspensumGS1());
		assertEquals(40, verfuegungZeitabschnittNovember.getBgCalculationInputAsiv().getErwerbspensumGS1().intValue());

		//Result December, 55% (8 tage 10%, 23 Tag 45%)
		final VerfuegungZeitabschnitt verfuegungZeitabschnittDecember = getVerfuegungZeitabschnittGueltigInMonth(result, Month.DECEMBER);;
		assertNotNull(verfuegungZeitabschnittDecember.getBgCalculationInputAsiv().getErwerbspensumGS1());
		assertEquals(55, verfuegungZeitabschnittDecember.getBgCalculationInputAsiv().getErwerbspensumGS1().intValue());

		//Result Januar = 01.11.-31.07, 40%
		final VerfuegungZeitabschnitt verfuegungZeitabschnittJanuar = getVerfuegungZeitabschnittGueltigInMonth(result, Month.JANUARY);
		assertNotNull(verfuegungZeitabschnittJanuar.getBgCalculationInputAsiv().getErwerbspensumGS1());
		assertEquals(60, verfuegungZeitabschnittJanuar.getBgCalculationInputAsiv().getErwerbspensumGS1().intValue());
	}

	@Test
	public void test_EWP_sinken() {
		//TEST PENSUM: 01.08-08-12, 70% und 09.12-31.07,30%
		LocalDate startDateP1 = TestDataUtil.START_PERIODE;
		LocalDate endDateP1 = DECEMBER_8;
		ErwerbspensumContainer ewp1 = TestDataUtil.createErwerbspensum(startDateP1, endDateP1, 70);

		LocalDate startDateP2 = DECEMBER_8.plusDays(1);
		LocalDate endDateP2 = TestDataUtil.ENDE_PERIODE;
		ErwerbspensumContainer ewp2 = TestDataUtil.createErwerbspensum(startDateP2, endDateP2, 30);

		List<VerfuegungZeitabschnitt> result = createGesuchAndCalculateZeitabschnitt(true, false, ewp1, ewp2);
		assertZeitabschnitteFullMonth(result);

		//RESULT Zeitabschnitt November, 70%
		final VerfuegungZeitabschnitt verfuegungZeitabschnittNovember = getVerfuegungZeitabschnittGueltigInMonth(result, Month.NOVEMBER);
		assertNotNull(verfuegungZeitabschnittNovember.getBgCalculationInputAsiv().getErwerbspensumGS1());
		assertEquals(70, verfuegungZeitabschnittNovember.getBgCalculationInputAsiv().getErwerbspensumGS1().intValue());

		//Result December, 70% (Der Anspruch darf nicht sinken innerhalb des Monats => die MonatsmergerRule wird hier nicht ausgeführt)
		final VerfuegungZeitabschnitt verfuegungZeitabschnittDecember = getVerfuegungZeitabschnittGueltigInMonth(result, Month.DECEMBER);;
		assertNotNull(verfuegungZeitabschnittDecember.getBgCalculationInputAsiv().getErwerbspensumGS1());
		assertEquals(70, verfuegungZeitabschnittDecember.getBgCalculationInputAsiv().getErwerbspensumGS1().intValue());

		//Result Januar = 01.11.-31.07, 30%
		final VerfuegungZeitabschnitt verfuegungZeitabschnittJanuar = getVerfuegungZeitabschnittGueltigInMonth(result, Month.JANUARY);
		assertNotNull(verfuegungZeitabschnittJanuar.getBgCalculationInputAsiv().getErwerbspensumGS1());
		assertEquals(30, verfuegungZeitabschnittJanuar.getBgCalculationInputAsiv().getErwerbspensumGS1().intValue());
	}

	@Test
	public void testPensumEndsDuringMonthSameStartingMonth() {
		//TEST PENSUM: 01.09-19.09, 60%
		ErwerbspensumContainer ewp = TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE.plusMonths(1), SEPTEMBER_19, 60);

		List<VerfuegungZeitabschnitt> result = createGesuchAndCalculateZeitabschnitt(true, false, ewp);
		assertZeitabschnitteFullMonth(result);

		//RESULT ZeitabschnittAugust, 0%
		final VerfuegungZeitabschnitt verfuegungZeitabschnittAugust = getVerfuegungZeitabschnittGueltigInMonth(result, Month.AUGUST);
		assertNull(verfuegungZeitabschnittAugust.getBgCalculationInputAsiv().getErwerbspensumGS1());

		//Result Zeitabschnitt September = 01.09.2017-30.09.2017, 38% (19 Tage zu 60%)
		final VerfuegungZeitabschnitt verfuegungZeitabschnittSeptember = getVerfuegungZeitabschnittGueltigInMonth(result, Month.SEPTEMBER);
		assertNotNull(verfuegungZeitabschnittSeptember.getBgCalculationInputAsiv().getErwerbspensumGS1());
		assertEquals(38, verfuegungZeitabschnittSeptember.getBgCalculationInputAsiv().getErwerbspensumGS1().intValue());

		//RESULT Zeitabschnitt Aust 01.10.-31.07, 0%
		final VerfuegungZeitabschnitt verfuegungZeitabschnittOktober = getVerfuegungZeitabschnittGueltigInMonth(result, Month.OCTOBER);
		assertNull(verfuegungZeitabschnittOktober.getBgCalculationInputAsiv().getErwerbspensumGS1());
	}

	private VerfuegungZeitabschnitt getVerfuegungZeitabschnittGueltigInMonth(List<VerfuegungZeitabschnitt> verfuegungZeitabschnitts, Month month) {
		return verfuegungZeitabschnitts.stream()
			.filter(zeitabschnitt -> zeitabschnitt.getGueltigkeit().getGueltigAb().getMonth().equals(month))
			.findAny()
			.orElse(null);
	}

	private void assertZeitabschnitteFullMonth(List<VerfuegungZeitabschnitt> verfuegungZeitabschnitte) {
		//12 Zeitabschnitte
		assertEquals(12, verfuegungZeitabschnitte.size());

		for(VerfuegungZeitabschnitt zeitabschnitt : verfuegungZeitabschnitte) {
			//Jeder Zeitabschnitt beginnt am 1. des Monats
			Assert.assertEquals(zeitabschnitt.getGueltigkeit().getGueltigAb().with(TemporalAdjusters.firstDayOfMonth()),
				zeitabschnitt.getGueltigkeit().getGueltigAb());
			//Jeder Zeitabschnitt endet am letzten Tag des Monats
			Assert.assertEquals(zeitabschnitt.getGueltigkeit().getGueltigBis().with(TemporalAdjusters.lastDayOfMonth()),
				zeitabschnitt.getGueltigkeit().getGueltigBis());
			//Jeder Zeitabschnitt ist genau ein Monat lang
			Assert.assertEquals(zeitabschnitt.getGueltigkeit().getGueltigAb().getMonth(),
				zeitabschnitt.getGueltigkeit().getGueltigBis().getMonth());
		}
	}

	@Test
	public void testBemerkungenGueltigkeitAfterMonthMerge() {
		ErwerbspensumContainer ewp1 = TestDataUtil.createErwerbspensum(OCTOBER_12,
			TestDataUtil.ENDE_PERIODE, 60);
		ErwerbspensumContainer ewp2 = TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE,
			70);
		Assert.assertNotNull(ewp2.getErwerbspensumJA());
		TestDataUtil.addUnbezahlterUrlaubToErwerbspensum(ewp2.getErwerbspensumJA(), JANUAR_26, JANUAR_26.plusMonths(4));

		final Betreuung betreuung = createBetreuungWithErwerpspensen(ewp1, ewp2);
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);
		Assert.assertNotNull(result);

		result = EbeguRuleTestsHelper.runSingleAbschlussRule(new MonatsRule(false), betreuung, result);
		result = EbeguRuleTestsHelper.runSingleAbschlussRule(new MonatsMergerRule(false, true), betreuung, result);
		assertZeitabschnitteFullMonth(result);

		//Nicht gemergede Monate, sollen bei allen den Bemerkungen Gueltigkeit = null haben (das heisst die Bemerkungen sind so lange gültig wie der Zeitabschnitt zu dem sie gehören)
		assertBemerkungenGueltigkeitNull(getVerfuegungZeitabschnittGueltigInMonth(result, Month.AUGUST));
		assertBemerkungenGueltigkeitNull(getVerfuegungZeitabschnittGueltigInMonth(result, Month.SEPTEMBER));
		assertBemerkungenGueltigkeitNull(getVerfuegungZeitabschnittGueltigInMonth(result, Month.NOVEMBER));
		assertBemerkungenGueltigkeitNull(getVerfuegungZeitabschnittGueltigInMonth(result, Month.DECEMBER));
		assertBemerkungenGueltigkeitNull(getVerfuegungZeitabschnittGueltigInMonth(result, Month.JANUARY));
		assertBemerkungenGueltigkeitNull(getVerfuegungZeitabschnittGueltigInMonth(result, Month.FEBRUARY));
		assertBemerkungenGueltigkeitNull(getVerfuegungZeitabschnittGueltigInMonth(result, Month.MARCH));
		assertBemerkungenGueltigkeitNull(getVerfuegungZeitabschnittGueltigInMonth(result, Month.JUNE));
		assertBemerkungenGueltigkeitNull(getVerfuegungZeitabschnittGueltigInMonth(result, Month.JULY));


		VerfuegungZeitabschnitt zaOktober = getVerfuegungZeitabschnittGueltigInMonth(result, Month.OCTOBER);
		VerfuegungZeitabschnitt zaApril = getVerfuegungZeitabschnittGueltigInMonth(result, Month.APRIL);
		VerfuegungZeitabschnitt zaMay = getVerfuegungZeitabschnittGueltigInMonth(result, Month.MAY);
		//Bei allen gemergeten Monaten Bemerkungen 'VERFUEGUNG_MIT_ANSPRUCH' sollen dieselbe Gültigkeit haben wie der Zeitabschnitt
		assertBemerkungWithGueltigkeit(zaOktober, MsgKey.VERFUEGUNG_MIT_ANSPRUCH, zaOktober.getGueltigkeit());
		assertBemerkungWithGueltigkeit(zaApril, MsgKey.VERFUEGUNG_MIT_ANSPRUCH, zaApril.getGueltigkeit());
		assertBemerkungWithGueltigkeit(zaMay, MsgKey.VERFUEGUNG_MIT_ANSPRUCH, zaMay.getGueltigkeit());

		//Alle anderen Bemerkungen haben die Gültigkeit des ungestrecken Zeitabschnitt
		assertBemerkungWithGueltigkeit(zaMay, MsgKey.UNBEZAHLTER_URLAUB_MSG, new DateRange(zaMay.getGueltigkeit().getGueltigAb(), JANUAR_26.plusMonths(4)));
		assertBemerkungWithGueltigkeit(zaApril, MsgKey.UNBEZAHLTER_URLAUB_MSG, new DateRange(JANUAR_26.plusMonths(3), zaApril.getGueltigkeit().getGueltigBis()));

	}

	@Test
	public void abwesenheitMitteMonatMergen() {
		//Setup mit BGPensum 80% (60% plus 20% Zuschlag), Abwesenheit von 17.08-26.10
		ErwerbspensumContainer ewp = TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE,
			TestDataUtil.ENDE_PERIODE, 60);
		final Betreuung betreuung = createBetreuungWithErwerpspensen(ewp);

		Betreuungspensum betreuungspensum = new Betreuungspensum();
		betreuungspensum.setGueltigkeit(new DateRange(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE));
		betreuungspensum.setMonatlicheBetreuungskosten(new BigDecimal(2000));
		betreuungspensum.setPensum(new BigDecimal(60));
		BetreuungspensumContainer container = new BetreuungspensumContainer();
		container.setBetreuungspensumJA(betreuungspensum);

		betreuung.setBetreuungspensumContainers(new HashSet<>());
		betreuung.getBetreuungspensumContainers().add(container);

		DateRange abwesenheitGueltigkeit = new DateRange(
			LocalDate.of(TestDataUtil.PERIODE_JAHR_1, Month.AUGUST, 17),
			LocalDate.of(TestDataUtil.PERIODE_JAHR_1, Month.OCTOBER, 26));
		Abwesenheit abwesenheit = new Abwesenheit();
		abwesenheit.setGueltigkeit(abwesenheitGueltigkeit);

		AbwesenheitContainer abwesenheitContainer = new AbwesenheitContainer();
		abwesenheitContainer.setAbwesenheitJA(abwesenheit);
		abwesenheit.setAbwesenheitContainer(abwesenheitContainer);

		betreuung.getAbwesenheitContainers().add(abwesenheitContainer);

		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);
		Assert.assertNotNull(result);

		result = EbeguRuleTestsHelper.runSingleAbschlussRule(new MonatsRule(false), betreuung, result);
		result = EbeguRuleTestsHelper.runSingleAbschlussRule(new MonatsMergerRule(false, true), betreuung, result);

		//ZA August Betreuung während kompletem Monat Anteil an zu beazahlenden Vollkosten = 0
		VerfuegungZeitabschnitt zaAugust = getVerfuegungZeitabschnittGueltigInMonth(result, Month.AUGUST);
		Assert.assertFalse(zaAugust.getRelevantBgCalculationInput().isBezahltKompletteVollkosten());
		Assert.assertEquals(BigDecimal.ZERO, zaAugust.getRelevantBgCalculationInput().getBezahltVollkostenMonatAnteil());

		//ZA September Betreuung vom 01.09-15.09 -> 15 Tage -> Anteil an zu beazahlenden Vollkosten = 0.5
		VerfuegungZeitabschnitt zaSeptember = getVerfuegungZeitabschnittGueltigInMonth(result, Month.SEPTEMBER);
		Assert.assertFalse(zaSeptember.getRelevantBgCalculationInput().isBezahltKompletteVollkosten());
		Assert.assertEquals(new BigDecimal("0.5").stripTrailingZeros(),
			zaSeptember.getRelevantBgCalculationInput().getBezahltVollkostenMonatAnteil().stripTrailingZeros());

		//ZA Oktober Betreuung vom 27.10-31.10 -> 5 Tage -> Anteil an zu beazahlenden Vollkosten = 0.8387 (01.10.-26.10 => 83.87 %)
		VerfuegungZeitabschnitt zaOctober = getVerfuegungZeitabschnittGueltigInMonth(result, Month.OCTOBER);
		Assert.assertFalse(zaOctober.getRelevantBgCalculationInput().isBezahltKompletteVollkosten());
		Assert.assertEquals(new BigDecimal("0.8387").stripTrailingZeros()
			, zaOctober.getRelevantBgCalculationInput().getBezahltVollkostenMonatAnteil().round(new MathContext(4)));

		//ZA November Betreuung wieder während kompletem Monat Anteil an zu beazahlenden Vollkosten = 0
		VerfuegungZeitabschnitt zaNovember = getVerfuegungZeitabschnittGueltigInMonth(result, Month.NOVEMBER);
		Assert.assertFalse(zaNovember.getRelevantBgCalculationInput().isBezahltKompletteVollkosten());
		Assert.assertEquals(BigDecimal.ZERO, zaNovember.getRelevantBgCalculationInput().getBezahltVollkostenMonatAnteil());
	}

	private void assertBemerkungWithGueltigkeit(VerfuegungZeitabschnitt zeitabschnitt, MsgKey msgKey, DateRange gueltigkeit) {
		zeitabschnitt.getBemerkungenDTOList().getBemerkungenStream()
			.filter(bemerkung -> bemerkung.getMsgKey() == msgKey)
			.forEach(bemerkung -> Assert.assertEquals(bemerkung.getGueltigkeit(), gueltigkeit));
	}

	private void assertBemerkungenGueltigkeitNull(VerfuegungZeitabschnitt verfuegungZeitabschnitt) {
		verfuegungZeitabschnitt.getBemerkungenDTOList().getBemerkungenStream()
			.forEach(bemerkung -> Assert.assertNull(bemerkung.getGueltigkeit()));
	}


	private List<VerfuegungZeitabschnitt> createGesuchAndCalculateZeitabschnitt(boolean anspruchMonatsweise, final boolean gs2, ErwerbspensumContainer... erwerbspensumContainers) {
		final Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(gs2);
		Gesuch gesuch = betreuung.extractGesuch();
		Assert.assertNotNull(gesuch.getGesuchsteller1());

		Arrays.stream(erwerbspensumContainers)
			.forEach(ewp -> gesuch.getGesuchsteller1().addErwerbspensumContainer(ewp));


		return calculateZeitabschnitt(anspruchMonatsweise, betreuung);
	}

	private Betreuung createBetreuungWithErwerpspensen(ErwerbspensumContainer... erwerbspensumContainers) {
		final Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(false);
		Gesuch gesuch = betreuung.extractGesuch();
		Assert.assertNotNull(gesuch.getGesuchsteller1());

		Arrays.stream(erwerbspensumContainers)
			.forEach(ewp -> gesuch.getGesuchsteller1().addErwerbspensumContainer(ewp));
		return betreuung;
	}

	private List<VerfuegungZeitabschnitt> calculateZeitabschnitt(boolean anspruchMonatsweise, Betreuung betreuung) {
		Map<EinstellungKey, Einstellung> einstellungenMap = EbeguRuleTestsHelper.getAllEinstellungen(betreuung.extractGesuchsperiode());
		einstellungenMap.get(ANSPRUCH_MONATSWEISE).setValue(String.valueOf(anspruchMonatsweise));

		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung, einstellungenMap, true);
		assertNotNull(result);
		return result;
	}


}
