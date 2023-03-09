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
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerAdresseContainer;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests fuer WohnsitzAbschnittRule
 */
public class WohnsitzAbschnittRuleTest {

	public static final LocalDate JANUARY_25 = LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.JANUARY, 25);
	public static final LocalDate JANUARY_26 = LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.JANUARY, 26);
	public static final LocalDate FEBRUARY_1 = LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.FEBRUARY, 1);
	public static final LocalDate FEBRURARY_13 = LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.FEBRUARY, 13);
	public static final LocalDate FEBRUARY_14 = LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.FEBRUARY, 14);
	public static final LocalDate JULY_10 = LocalDate.of(TestDataUtil.PERIODE_JAHR_1, Month.JULY, 10);
	public static final LocalDate JULY_11 = LocalDate.of(TestDataUtil.PERIODE_JAHR_1, Month.JULY, 11);
	public static final LocalDate MARCH_26 = LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.MARCH, 26);
	public static final LocalDate NOVEMBER_25 = LocalDate.of(TestDataUtil.PERIODE_JAHR_1, Month.NOVEMBER, 25);
	public static final LocalDate DECEMBER_1 = LocalDate.of(TestDataUtil.PERIODE_JAHR_1, Month.DECEMBER, 1);
	public static final LocalDate NOVEMBER_26 = LocalDate.of(TestDataUtil.PERIODE_JAHR_1, Month.NOVEMBER, 26);
	final WohnsitzAbschnittRule wohnsitzRule =
			new WohnsitzAbschnittRule(Constants.DEFAULT_GUELTIGKEIT, Constants.DEFAULT_LOCALE);

	@Test
	public void testCreateZeitAbschnitte() {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(true);
		Gesuch gesuch = betreuung.extractGesuch();

		assertNotNull(gesuch.getGesuchsteller1());
		List<GesuchstellerAdresseContainer> adressen1 = new ArrayList<>();
		final GesuchstellerAdresseContainer adresse1GS1 =
				TestDataUtil.createDefaultGesuchstellerAdresseContainer(gesuch.getGesuchsteller1());
		assertNotNull(adresse1GS1.getGesuchstellerAdresseJA());
		adresse1GS1.getGesuchstellerAdresseJA().setNichtInGemeinde(false);
		adresse1GS1.getGesuchstellerAdresseJA().setGueltigkeit(new DateRange(Constants.START_OF_TIME, JANUARY_25));
		adressen1.add(adresse1GS1);
		final GesuchstellerAdresseContainer adresse2GS1 =
				TestDataUtil.createDefaultGesuchstellerAdresseContainer(gesuch.getGesuchsteller1());
		assertNotNull(adresse2GS1.getGesuchstellerAdresseJA());
		adresse2GS1.getGesuchstellerAdresseJA().setNichtInGemeinde(true);
		adresse2GS1.getGesuchstellerAdresseJA().setGueltigkeit(new DateRange(JANUARY_26, Constants.END_OF_TIME));
		adressen1.add(adresse2GS1);
		gesuch.getGesuchsteller1().setAdressen(adressen1);

		assertNotNull(gesuch.getGesuchsteller2());

		List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitte =
				wohnsitzRule.createVerfuegungsZeitabschnitteIfApplicable(betreuung);
		verfuegungsZeitabschnitte =
				wohnsitzRule.normalizeZeitabschnitte(verfuegungsZeitabschnitte, gesuch.getGesuchsperiode());

		assertNotNull(verfuegungsZeitabschnitte);
		assertEquals(3, verfuegungsZeitabschnitte.size());

		VerfuegungZeitabschnitt abschnitt1 = verfuegungsZeitabschnitte.get(0);
		assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb(),
				abschnitt1.getGueltigkeit().getGueltigAb());
		assertEquals(JANUARY_25, abschnitt1.getGueltigkeit().getGueltigBis());
		assertFalse(abschnitt1.getBgCalculationInputAsiv().isWohnsitzNichtInGemeindeGS1());

		VerfuegungZeitabschnitt abschnitt2 = verfuegungsZeitabschnitte.get(1);
		assertEquals(JANUARY_26, abschnitt2.getGueltigkeit().getGueltigAb());
		assertEquals(JANUARY_26.with(TemporalAdjusters.lastDayOfMonth()), abschnitt2.getGueltigkeit().getGueltigBis());
		assertTrue(abschnitt2.getBgCalculationInputAsiv().isWohnsitzNichtInGemeindeGS1());

		VerfuegungZeitabschnitt abschnitt3 = verfuegungsZeitabschnitte.get(2);
		assertEquals(FEBRUARY_1, abschnitt3.getGueltigkeit().getGueltigAb());
		assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis(),
				abschnitt3.getGueltigkeit().getGueltigBis());
		assertTrue(abschnitt3.getBgCalculationInputAsiv().isWohnsitzNichtInGemeindeGS1());

		final List<VerfuegungZeitabschnitt> mergedZerfuegungZeitabschnitte =
				wohnsitzRule.mergeZeitabschnitte(verfuegungsZeitabschnitte);

		assertEquals(3, mergedZerfuegungZeitabschnitte.size());

		VerfuegungZeitabschnitt zeitabschnitt_1 = mergedZerfuegungZeitabschnitte.get(0);
		assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb(),
				zeitabschnitt_1.getGueltigkeit().getGueltigAb());
		assertEquals(JANUARY_25, zeitabschnitt_1.getGueltigkeit().getGueltigBis());
		assertFalse(zeitabschnitt_1.getBgCalculationInputAsiv().isWohnsitzNichtInGemeindeGS1());

		VerfuegungZeitabschnitt zeitabschnitt_2 = mergedZerfuegungZeitabschnitte.get(1);
		assertEquals(JANUARY_26, zeitabschnitt_2.getGueltigkeit().getGueltigAb());
		assertEquals(JANUARY_26.with(TemporalAdjusters.lastDayOfMonth()),
				zeitabschnitt_2.getGueltigkeit().getGueltigBis());
		assertTrue(zeitabschnitt_2.getBgCalculationInputAsiv().isWohnsitzNichtInGemeindeGS1());

		VerfuegungZeitabschnitt zeitabschnitt_3 = mergedZerfuegungZeitabschnitte.get(2);
		assertEquals(FEBRUARY_1, zeitabschnitt_3.getGueltigkeit().getGueltigAb());
		assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis(),
				zeitabschnitt_3.getGueltigkeit().getGueltigBis());
		assertTrue(zeitabschnitt_3.getBgCalculationInputAsiv().isWohnsitzNichtInGemeindeGS1());
	}

	@Test
	public void testCreateZeitAbschnitteFamSituationMutationFrom1GSTo2GS() {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(true);
		Gesuch gesuch = betreuung.extractGesuch();

		createAdressenForGS1(gesuch);

		assertNotNull(gesuch.getFamiliensituationContainer());
		Familiensituation familiensituationErstgesuch = new Familiensituation();
		gesuch.getFamiliensituationContainer().setFamiliensituationErstgesuch(familiensituationErstgesuch);
		familiensituationErstgesuch.setFamilienstatus(EnumFamilienstatus.ALLEINERZIEHEND);
		Familiensituation familiensituation = gesuch.extractFamiliensituation();
		assertNotNull(familiensituation);
		familiensituation.setAenderungPer(MARCH_26);

		List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitte =
				wohnsitzRule.createVerfuegungsZeitabschnitteIfApplicable(betreuung);
		verfuegungsZeitabschnitte =
				wohnsitzRule.normalizeZeitabschnitte(verfuegungsZeitabschnitte, gesuch.getGesuchsperiode());

		assertNotNull(verfuegungsZeitabschnitte);
		assertEquals(3, verfuegungsZeitabschnitte.size());

		// Wegzug im November -> Anspruch endet Ende November
		VerfuegungZeitabschnitt abschnitt1 = verfuegungsZeitabschnitte.get(0);
		assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb(),
				abschnitt1.getGueltigkeit().getGueltigAb());
		assertEquals(NOVEMBER_25, abschnitt1.getGueltigkeit().getGueltigBis());
		assertFalse(abschnitt1.getBgCalculationInputAsiv().isWohnsitzNichtInGemeindeGS1());

		VerfuegungZeitabschnitt abschnitt2 = verfuegungsZeitabschnitte.get(2);
		assertEquals(DECEMBER_1, abschnitt2.getGueltigkeit().getGueltigAb());
		assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis(),
				abschnitt2.getGueltigkeit().getGueltigBis());
		assertTrue(abschnitt2.getBgCalculationInputAsiv().isWohnsitzNichtInGemeindeGS1());

		final List<VerfuegungZeitabschnitt> mergedZerfuegungZeitabschnitte =
				wohnsitzRule.mergeZeitabschnitte(verfuegungsZeitabschnitte);

		assertEquals(3, mergedZerfuegungZeitabschnitte.size());

		VerfuegungZeitabschnitt mergedAbschnitt1 = mergedZerfuegungZeitabschnitte.get(0);
		assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb(),
				mergedAbschnitt1.getGueltigkeit().getGueltigAb());
		//erster Zeitabschnitt gueltig bis umzugstag
		assertEquals(NOVEMBER_25, mergedAbschnitt1.getGueltigkeit().getGueltigBis());

		// zweiter Zeitabschnitt gueltig von umzugstag bis ende Monat
		VerfuegungZeitabschnitt mergedAbschnitt2 = mergedZerfuegungZeitabschnitte.get(1);
		assertEquals(NOVEMBER_26, mergedAbschnitt2.getGueltigkeit().getGueltigAb());
		assertEquals(NOVEMBER_25.with(TemporalAdjusters.lastDayOfMonth()),
				mergedAbschnitt2.getGueltigkeit().getGueltigBis());
		assertTrue(mergedAbschnitt2.getBgCalculationInputAsiv().isWohnsitzNichtInGemeindeGS1());
		// Alter Zivilstand gilt noch bis Ende Monat
		assertEquals(NOVEMBER_26.with(TemporalAdjusters.lastDayOfMonth()),
				mergedAbschnitt2.getGueltigkeit().getGueltigBis());

		VerfuegungZeitabschnitt abschnitt3 = mergedZerfuegungZeitabschnitte.get(2);
		assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis(),
				abschnitt3.getGueltigkeit().getGueltigBis());
		assertTrue(abschnitt3.getBgCalculationInputAsiv().isWohnsitzNichtInGemeindeGS1());
	}

	@Test
	public void testCreateZeitAbschnitteFamSituationMutationFrom2GSTo1GS() {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(true);
		Gesuch gesuch = betreuung.extractGesuch();

		createAdressenForGS1(gesuch);

		assertNotNull(gesuch.getFamiliensituationContainer());
		Familiensituation familiensituationErstgesuch = new Familiensituation();
		familiensituationErstgesuch.setFamilienstatus(EnumFamilienstatus.VERHEIRATET);
		gesuch.getFamiliensituationContainer().setFamiliensituationErstgesuch(familiensituationErstgesuch);

		Familiensituation familiensituation = gesuch.extractFamiliensituation();
		assertNotNull(familiensituation);
		familiensituation.setFamilienstatus(EnumFamilienstatus.ALLEINERZIEHEND);
		familiensituation.setAenderungPer(MARCH_26);

		List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitte =
				wohnsitzRule.createVerfuegungsZeitabschnitteIfApplicable(betreuung);

		verfuegungsZeitabschnitte =
				wohnsitzRule.normalizeZeitabschnitte(verfuegungsZeitabschnitte, gesuch.getGesuchsperiode());

		assertNotNull(verfuegungsZeitabschnitte);
		assertEquals(3, verfuegungsZeitabschnitte.size());

		// Wegzug im November -> Anspruch endet Ende November
		VerfuegungZeitabschnitt abschnitt1 = verfuegungsZeitabschnitte.get(0);
		assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb(),
				abschnitt1.getGueltigkeit().getGueltigAb());
		assertEquals(NOVEMBER_25, abschnitt1.getGueltigkeit().getGueltigBis());
		assertFalse(abschnitt1.getBgCalculationInputAsiv().isWohnsitzNichtInGemeindeGS1());

		VerfuegungZeitabschnitt abschnitt2 = verfuegungsZeitabschnitte.get(1);
		assertEquals(NOVEMBER_26, abschnitt2.getGueltigkeit().getGueltigAb());
		assertEquals(NOVEMBER_26, abschnitt2.getGueltigkeit().getGueltigAb());
		assertEquals(NOVEMBER_26.with(TemporalAdjusters.lastDayOfMonth()),
				abschnitt2.getGueltigkeit().getGueltigBis());
		assertTrue(abschnitt2.getBgCalculationInputAsiv().isWohnsitzNichtInGemeindeGS1());

		final List<VerfuegungZeitabschnitt> mergedZerfuegungZeitabschnitte =
				wohnsitzRule.mergeZeitabschnitte(verfuegungsZeitabschnitte);

		assertEquals(3, mergedZerfuegungZeitabschnitte.size());

		VerfuegungZeitabschnitt mergedAbschnitt1 = mergedZerfuegungZeitabschnitte.get(0);
		assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb(),
				mergedAbschnitt1.getGueltigkeit().getGueltigAb());
		assertEquals(NOVEMBER_25, mergedAbschnitt1.getGueltigkeit().getGueltigBis());
		assertFalse(mergedAbschnitt1.getBgCalculationInputAsiv().isWohnsitzNichtInGemeindeGS1());

		VerfuegungZeitabschnitt mergedAbschnitt2 = mergedZerfuegungZeitabschnitte.get(1);
		assertEquals(NOVEMBER_26, mergedAbschnitt2.getGueltigkeit().getGueltigAb());
		assertEquals(NOVEMBER_25.with(TemporalAdjusters.lastDayOfMonth()),
				mergedAbschnitt2.getGueltigkeit().getGueltigBis());
		assertTrue(mergedAbschnitt2.getBgCalculationInputAsiv().isWohnsitzNichtInGemeindeGS1());

		VerfuegungZeitabschnitt mergedAbschnitt3 = mergedZerfuegungZeitabschnitte.get(2);
		assertEquals(NOVEMBER_26.with(TemporalAdjusters.firstDayOfNextMonth()),
				mergedAbschnitt3.getGueltigkeit().getGueltigAb());
		assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis(),
				mergedAbschnitt3.getGueltigkeit().getGueltigBis());
		assertTrue(mergedAbschnitt3.getBgCalculationInputAsiv().isWohnsitzNichtInGemeindeGS1());
	}

	@Test
	public void testNichtInBernInBernNichtInBern() {
		// der GS wohnt zuerst nicht in Bern, danach zieht er ein und dann wieder weg. Das Wegziehen sollte erst 2
		// Monaten danach beruecksichtigt werden
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(true);
		Gesuch gesuch = betreuung.extractGesuch();
		assertNotNull(gesuch.getGesuchsteller1());

		List<GesuchstellerAdresseContainer> adressen1 = new ArrayList<>();
		final GesuchstellerAdresseContainer adresse1GS1 =
				TestDataUtil.createDefaultGesuchstellerAdresseContainer(gesuch.getGesuchsteller1());
		assertNotNull(adresse1GS1.getGesuchstellerAdresseJA());
		adresse1GS1.getGesuchstellerAdresseJA().setNichtInGemeinde(true);
		adresse1GS1.getGesuchstellerAdresseJA().setGueltigkeit(new DateRange(Constants.START_OF_TIME, JANUARY_25));
		adressen1.add(adresse1GS1);

		final GesuchstellerAdresseContainer adresse2GS1 =
				TestDataUtil.createDefaultGesuchstellerAdresseContainer(gesuch.getGesuchsteller1());
		assertNotNull(adresse2GS1.getGesuchstellerAdresseJA());
		adresse2GS1.getGesuchstellerAdresseJA().setNichtInGemeinde(false);
		adresse2GS1.getGesuchstellerAdresseJA().setGueltigkeit(new DateRange(JANUARY_26, FEBRURARY_13));
		adressen1.add(adresse2GS1);

		final GesuchstellerAdresseContainer adresse3GS1 =
				TestDataUtil.createDefaultGesuchstellerAdresseContainer(gesuch.getGesuchsteller1());
		assertNotNull(adresse3GS1.getGesuchstellerAdresseJA());
		adresse3GS1.getGesuchstellerAdresseJA().setNichtInGemeinde(true);
		adresse3GS1.getGesuchstellerAdresseJA().setGueltigkeit(new DateRange(FEBRUARY_14, Constants.END_OF_TIME));
		adressen1.add(adresse3GS1);

		gesuch.getGesuchsteller1().setAdressen(adressen1);

		List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitte =
				wohnsitzRule.createVerfuegungsZeitabschnitteIfApplicable(betreuung);
		verfuegungsZeitabschnitte =
				wohnsitzRule.normalizeZeitabschnitte(verfuegungsZeitabschnitte, gesuch.getGesuchsperiode());

		assertNotNull(verfuegungsZeitabschnitte);
		assertEquals(5, verfuegungsZeitabschnitte.size());

		// In Bern: 26.Januar - 13. Februar
		//Wegzug aus Bern: 14.Feb - end of time

		// Abschnitt 1: Anfangs bis 25.Januar -> nicht in Bern
		VerfuegungZeitabschnitt abschnitt_1_nichtInBern = verfuegungsZeitabschnitte.get(0);
		assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb(),
				abschnitt_1_nichtInBern.getGueltigkeit().getGueltigAb());
		assertEquals(JANUARY_25, abschnitt_1_nichtInBern.getGueltigkeit().getGueltigBis());
		assertTrue(abschnitt_1_nichtInBern.getBgCalculationInputAsiv().isWohnsitzNichtInGemeindeGS1());
		assertFalse(abschnitt_1_nichtInBern.getBgCalculationInputAsiv().getPotentielleDoppelBetreuung());
		assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb(),
				abschnitt_1_nichtInBern.getGueltigkeit().getGueltigAb());
		assertEquals(JANUARY_25, abschnitt_1_nichtInBern.getGueltigkeit().getGueltigBis());
		assertTrue(abschnitt_1_nichtInBern.getBgCalculationInputAsiv().isWohnsitzNichtInGemeindeGS1());

		// Abschnitt 2: 26.Januar bis Monatsende (31.Janurar) in Bern + Doppelbetreuung Möglich
		VerfuegungZeitabschnitt abschnitt_2_inBern = verfuegungsZeitabschnitte.get(1);
		assertEquals(JANUARY_26, abschnitt_2_inBern.getGueltigkeit().getGueltigAb());
		assertEquals(JANUARY_26.with(TemporalAdjusters.lastDayOfMonth()),
				abschnitt_2_inBern.getGueltigkeit().getGueltigBis());
		assertFalse(abschnitt_2_inBern.getBgCalculationInputAsiv().isWohnsitzNichtInGemeindeGS1());
		assertTrue(abschnitt_2_inBern.getBgCalculationInputAsiv().getPotentielleDoppelBetreuung());

		// Dritter Abschnitt:
		// in Bern : 1.Februar bis Zum Umzugszeitpunkg
		VerfuegungZeitabschnitt abschnitt_3_inBern_ohneDoppelbetreuung = verfuegungsZeitabschnitte.get(2);
		assertEquals(FEBRUARY_1, abschnitt_3_inBern_ohneDoppelbetreuung.getGueltigkeit().getGueltigAb());
		assertEquals(FEBRURARY_13, abschnitt_3_inBern_ohneDoppelbetreuung.getGueltigkeit().getGueltigBis());
		assertFalse(abschnitt_3_inBern_ohneDoppelbetreuung.getBgCalculationInputAsiv().isWohnsitzNichtInGemeindeGS1());
		assertFalse(abschnitt_3_inBern_ohneDoppelbetreuung.getBgCalculationInputAsiv().getPotentielleDoppelBetreuung());

		// Vierter Abschnitt:
		// vom Zeitpunkt vom Wegzug bis endeMonat(wegzug)
		VerfuegungZeitabschnitt abschnitt_4_wegzug_bisEndeWegzugMonat = verfuegungsZeitabschnitte.get(3);
		assertEquals(FEBRUARY_14, abschnitt_4_wegzug_bisEndeWegzugMonat.getGueltigkeit().getGueltigAb());
		assertEquals(FEBRUARY_14.with(TemporalAdjusters.lastDayOfMonth()),
				abschnitt_4_wegzug_bisEndeWegzugMonat.getGueltigkeit().getGueltigBis());
		assertTrue(abschnitt_4_wegzug_bisEndeWegzugMonat.getBgCalculationInputAsiv().isWohnsitzNichtInGemeindeGS1());
		assertTrue(abschnitt_4_wegzug_bisEndeWegzugMonat.getBgCalculationInputAsiv().getPotentielleDoppelBetreuung());

		// Abschnitt 5
		// Wegzug-Ende_monat + 1  to ininity... and beyond
		VerfuegungZeitabschnitt abschnitt_5_to_the_end_of_Time = verfuegungsZeitabschnitte.get(4);
		assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis(),
				abschnitt_5_to_the_end_of_Time.getGueltigkeit().getGueltigBis());
		assertTrue(abschnitt_5_to_the_end_of_Time.getBgCalculationInputAsiv().isWohnsitzNichtInGemeindeGS1());
		assertFalse(abschnitt_5_to_the_end_of_Time.getBgCalculationInputAsiv().getPotentielleDoppelBetreuung());
	}

	@Test
	public void testUmzugNachBernOneMonthBeforeGesuchsperiode() {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(false);
		Gesuch gesuch = betreuung.extractGesuch();
		assertNotNull(gesuch.getGesuchsteller1());

		List<GesuchstellerAdresseContainer> adressen1 = new ArrayList<>();
		final GesuchstellerAdresseContainer adresse1GS1 =
				TestDataUtil.createDefaultGesuchstellerAdresseContainer(gesuch.getGesuchsteller1());
		assertNotNull(adresse1GS1.getGesuchstellerAdresseJA());
		adresse1GS1.getGesuchstellerAdresseJA().setNichtInGemeinde(true);
		adresse1GS1.getGesuchstellerAdresseJA().setGueltigkeit(new DateRange(Constants.START_OF_TIME, JULY_10));
		adressen1.add(adresse1GS1);

		final GesuchstellerAdresseContainer adresse2GS1 =
				TestDataUtil.createDefaultGesuchstellerAdresseContainer(gesuch.getGesuchsteller1());
		assertNotNull(adresse2GS1.getGesuchstellerAdresseJA());
		adresse2GS1.getGesuchstellerAdresseJA().setNichtInGemeinde(false);
		adresse2GS1.getGesuchstellerAdresseJA().setGueltigkeit(new DateRange(JULY_11, Constants.END_OF_TIME));
		adressen1.add(adresse2GS1);

		gesuch.getGesuchsteller1().setAdressen(adressen1);

		List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitte =
				wohnsitzRule.createVerfuegungsZeitabschnitteIfApplicable(betreuung);
		verfuegungsZeitabschnitte =
				wohnsitzRule.normalizeZeitabschnitte(verfuegungsZeitabschnitte, gesuch.getGesuchsperiode());

		// Hinzug vor dem Start der Periode -> Es gilt für die ganze Periode -> 1 Abschnitt. Es wird aber VORNE nicht
		// abgeschnitten (hinten schon)
		assertNotNull(verfuegungsZeitabschnitte);
		assertEquals(1, verfuegungsZeitabschnitte.size());

		assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb(),
				verfuegungsZeitabschnitte.get(0).getGueltigkeit().getGueltigAb());
		assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis(),
				verfuegungsZeitabschnitte.get(0).getGueltigkeit().getGueltigBis());
		assertFalse(verfuegungsZeitabschnitte.get(0).getBgCalculationInputAsiv().isWohnsitzNichtInGemeindeGS1());
	}

	@Test
	public void testUmzugAusBernOneMonthBeforeGesuchsperiode() {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(false);
		Gesuch gesuch = betreuung.extractGesuch();
		assertNotNull(gesuch.getGesuchsteller1());

		List<GesuchstellerAdresseContainer> adressen1 = new ArrayList<>();
		final GesuchstellerAdresseContainer adresse1GS1 =
				TestDataUtil.createDefaultGesuchstellerAdresseContainer(gesuch.getGesuchsteller1());
		assertNotNull(adresse1GS1.getGesuchstellerAdresseJA());
		adresse1GS1.getGesuchstellerAdresseJA().setNichtInGemeinde(false);
		adresse1GS1.getGesuchstellerAdresseJA().setGueltigkeit(new DateRange(Constants.START_OF_TIME, JULY_10));
		adressen1.add(adresse1GS1);

		// Adresse ausserhalb Bern ab Mitte letzter Monat der GP -> Anspruch bleibt bestehen bis Ende Monat!
		final GesuchstellerAdresseContainer adresse2GS1 =
				TestDataUtil.createDefaultGesuchstellerAdresseContainer(gesuch.getGesuchsteller1());
		assertNotNull(adresse2GS1.getGesuchstellerAdresseJA());
		adresse2GS1.getGesuchstellerAdresseJA().setNichtInGemeinde(true);
		adresse2GS1.getGesuchstellerAdresseJA().setGueltigkeit(new DateRange(JULY_11, Constants.END_OF_TIME));
		adressen1.add(adresse2GS1);

		gesuch.getGesuchsteller1().setAdressen(adressen1);

		List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitte =
				wohnsitzRule.createVerfuegungsZeitabschnitteIfApplicable(betreuung);

		verfuegungsZeitabschnitte =
				wohnsitzRule.normalizeZeitabschnitte(verfuegungsZeitabschnitte, gesuch.getGesuchsperiode());

		assertNotNull(verfuegungsZeitabschnitte);
		assertEquals(1, verfuegungsZeitabschnitte.size());

		assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb(),
				verfuegungsZeitabschnitte.get(0).getGueltigkeit().getGueltigAb());
		assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis(),
				verfuegungsZeitabschnitte.get(0).getGueltigkeit().getGueltigBis());
		assertTrue(verfuegungsZeitabschnitte.get(0).getBgCalculationInputAsiv().isWohnsitzNichtInGemeindeGS1());
	}

	// HELP METHODS

	private void createAdressenForGS1(Gesuch gesuch) {
		assertNotNull(gesuch.getGesuchsteller1());
		List<GesuchstellerAdresseContainer> adressen1 = new ArrayList<>();
		final GesuchstellerAdresseContainer adresse1GS1 =
				TestDataUtil.createDefaultGesuchstellerAdresseContainer(gesuch.getGesuchsteller1());
		assertNotNull(adresse1GS1.getGesuchstellerAdresseJA());
		adresse1GS1.getGesuchstellerAdresseJA().setNichtInGemeinde(false);
		adresse1GS1.getGesuchstellerAdresseJA().setGueltigkeit(new DateRange(Constants.START_OF_TIME, NOVEMBER_25));
		adressen1.add(adresse1GS1);
		final GesuchstellerAdresseContainer adresse2GS1 =
				TestDataUtil.createDefaultGesuchstellerAdresseContainer(gesuch.getGesuchsteller1());
		assertNotNull(adresse2GS1.getGesuchstellerAdresseJA());
		adresse2GS1.getGesuchstellerAdresseJA().setNichtInGemeinde(true);
		adresse2GS1.getGesuchstellerAdresseJA().setGueltigkeit(new DateRange(NOVEMBER_26, Constants.END_OF_TIME));
		adressen1.add(adresse2GS1);
		gesuch.getGesuchsteller1().setAdressen(adressen1);
	}
}
