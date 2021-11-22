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

import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.ErwerbspensumContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.test.TestDataUtil;
import org.junit.Assert;
import org.junit.Test;

import static ch.dvbern.ebegu.enums.EinstellungKey.FJKV_ANSPRUCH_MONATSWEISE;
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

	private List<VerfuegungZeitabschnitt> createGesuchAndCalculateZeitabschnitt(boolean anspruchMonatsweise, final boolean gs2, ErwerbspensumContainer... erwerbspensumContainers) {
		final Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(gs2);
		Gesuch gesuch = betreuung.extractGesuch();
		TestDataUtil.createDefaultAdressenForGS(gesuch, gs2);
		Assert.assertNotNull(gesuch.getGesuchsteller1());

		Arrays.stream(erwerbspensumContainers)
			.forEach(ewp -> gesuch.getGesuchsteller1().addErwerbspensumContainer(ewp));


		return calculateZeitabschnitt(anspruchMonatsweise, betreuung);
	}

	private Betreuung createBetreuungWithErwerpspensen(ErwerbspensumContainer... erwerbspensumContainers) {
		final Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(false);
		Gesuch gesuch = betreuung.extractGesuch();
		TestDataUtil.createDefaultAdressenForGS(gesuch, false);
		Assert.assertNotNull(gesuch.getGesuchsteller1());

		Arrays.stream(erwerbspensumContainers)
			.forEach(ewp -> gesuch.getGesuchsteller1().addErwerbspensumContainer(ewp));
		return betreuung;
	}

	private List<VerfuegungZeitabschnitt> calculateZeitabschnitt(boolean anspruchMonatsweise, Betreuung betreuung) {
		Map<EinstellungKey, Einstellung> einstellungenMap = EbeguRuleTestsHelper.getAllEinstellungen(betreuung.extractGesuchsperiode());
		einstellungenMap.get(FJKV_ANSPRUCH_MONATSWEISE).setValue(String.valueOf(anspruchMonatsweise));

		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung, einstellungenMap, true);
		assertNotNull(result);
		return result;
	}


}
