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
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests fuer WohnsitzAbschnittRule
 */
public class WohnsitzAbschnittRuleTest {

	final WohnsitzAbschnittRule wohnsitzRule = new WohnsitzAbschnittRule(Constants.DEFAULT_GUELTIGKEIT, Constants.DEFAULT_LOCALE);

	@Test
	public void testCreateZeitAbschnitte() {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(true);
		Gesuch gesuch = betreuung.extractGesuch();

		Assert.assertNotNull(gesuch.getGesuchsteller1());
		List<GesuchstellerAdresseContainer> adressen1 = new ArrayList<>();
		final GesuchstellerAdresseContainer adresse1GS1 = TestDataUtil.createDefaultGesuchstellerAdresseContainer(gesuch.getGesuchsteller1());
		Assert.assertNotNull(adresse1GS1.getGesuchstellerAdresseJA());
		adresse1GS1.getGesuchstellerAdresseJA().setNichtInGemeinde(false);
		adresse1GS1.getGesuchstellerAdresseJA().setGueltigkeit(new DateRange(Constants.START_OF_TIME, LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.JANUARY, 25)));
		adressen1.add(adresse1GS1);
		final GesuchstellerAdresseContainer adresse2GS1 = TestDataUtil.createDefaultGesuchstellerAdresseContainer(gesuch.getGesuchsteller1());
		Assert.assertNotNull(adresse2GS1.getGesuchstellerAdresseJA());
		adresse2GS1.getGesuchstellerAdresseJA().setNichtInGemeinde(true);
		adresse2GS1.getGesuchstellerAdresseJA().setGueltigkeit(new DateRange(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.JANUARY, 26), Constants.END_OF_TIME));
		adressen1.add(adresse2GS1);
		gesuch.getGesuchsteller1().setAdressen(adressen1);

		Assert.assertNotNull(gesuch.getGesuchsteller2());

		List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitte = wohnsitzRule.createVerfuegungsZeitabschnitteIfApplicable(betreuung);
		verfuegungsZeitabschnitte = wohnsitzRule.normalizeZeitabschnitte(verfuegungsZeitabschnitte, gesuch.getGesuchsperiode());

		Assert.assertNotNull(verfuegungsZeitabschnitte);
		Assert.assertEquals(2, verfuegungsZeitabschnitte.size());

		VerfuegungZeitabschnitt abschnitt1 = verfuegungsZeitabschnitte.get(0);
		Assert.assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb(), abschnitt1.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.JANUARY, 31), abschnitt1.getGueltigkeit().getGueltigBis());
		Assert.assertFalse(abschnitt1.getBgCalculationInputAsiv().isWohnsitzNichtInGemeindeGS1());

		VerfuegungZeitabschnitt abschnitt2 = verfuegungsZeitabschnitte.get(1);
		Assert.assertEquals(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.FEBRUARY, 1), abschnitt2.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis(), abschnitt2.getGueltigkeit().getGueltigBis());
		Assert.assertTrue(abschnitt2.getBgCalculationInputAsiv().isWohnsitzNichtInGemeindeGS1());

		final List<VerfuegungZeitabschnitt> mergedZerfuegungZeitabschnitte = wohnsitzRule.mergeZeitabschnitte(verfuegungsZeitabschnitte);

		Assert.assertEquals(2, mergedZerfuegungZeitabschnitte.size());

		Assert.assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb(), mergedZerfuegungZeitabschnitte.get(0).getGueltigkeit().getGueltigAb());
		Assert.assertEquals(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.JANUARY, 31), mergedZerfuegungZeitabschnitte.get(0).getGueltigkeit().getGueltigBis());
		Assert.assertFalse(mergedZerfuegungZeitabschnitte.get(0).getBgCalculationInputAsiv().isWohnsitzNichtInGemeindeGS1());

		Assert.assertEquals(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.FEBRUARY, 1), mergedZerfuegungZeitabschnitte.get(1).getGueltigkeit().getGueltigAb());
		Assert.assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis(), mergedZerfuegungZeitabschnitte.get(1).getGueltigkeit().getGueltigBis());
		Assert.assertTrue(mergedZerfuegungZeitabschnitte.get(1).getBgCalculationInputAsiv().isWohnsitzNichtInGemeindeGS1());
	}

	@Test
	public void testCreateZeitAbschnitteFamSituationMutationFrom1GSTo2GS() {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(true);
		Gesuch gesuch = betreuung.extractGesuch();

		createAdressenForGS1(gesuch);

		Assert.assertNotNull(gesuch.getFamiliensituationContainer());
		Familiensituation familiensituationErstgesuch = new Familiensituation();
		gesuch.getFamiliensituationContainer().setFamiliensituationErstgesuch(familiensituationErstgesuch);
		familiensituationErstgesuch.setFamilienstatus(EnumFamilienstatus.ALLEINERZIEHEND);
		Familiensituation familiensituation = gesuch.extractFamiliensituation();
		Assert.assertNotNull(familiensituation);
		familiensituation.setAenderungPer(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.MARCH, 26));

		List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitte = wohnsitzRule.createVerfuegungsZeitabschnitteIfApplicable(betreuung);
		verfuegungsZeitabschnitte = wohnsitzRule.normalizeZeitabschnitte(verfuegungsZeitabschnitte, gesuch.getGesuchsperiode());

		Assert.assertNotNull(verfuegungsZeitabschnitte);
		Assert.assertEquals(2, verfuegungsZeitabschnitte.size());

		// Wegzug im November -> Anspruch endet Ende November
		VerfuegungZeitabschnitt abschnitt1 = verfuegungsZeitabschnitte.get(0);
		Assert.assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb(), abschnitt1.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(LocalDate.of(TestDataUtil.PERIODE_JAHR_1, Month.NOVEMBER, 30), abschnitt1.getGueltigkeit().getGueltigBis());
		Assert.assertFalse(abschnitt1.getBgCalculationInputAsiv().isWohnsitzNichtInGemeindeGS1());

		VerfuegungZeitabschnitt abschnitt2 = verfuegungsZeitabschnitte.get(1);
		Assert.assertEquals(LocalDate.of(TestDataUtil.PERIODE_JAHR_1, Month.DECEMBER, 1), abschnitt2.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis(), abschnitt2.getGueltigkeit().getGueltigBis());
		Assert.assertTrue(abschnitt2.getBgCalculationInputAsiv().isWohnsitzNichtInGemeindeGS1());

		final List<VerfuegungZeitabschnitt> mergedZerfuegungZeitabschnitte = wohnsitzRule.mergeZeitabschnitte(verfuegungsZeitabschnitte);

		Assert.assertEquals(2, mergedZerfuegungZeitabschnitte.size());

		VerfuegungZeitabschnitt mergedAbschnitt1 = mergedZerfuegungZeitabschnitte.get(0);
		Assert.assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb(), mergedAbschnitt1.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(LocalDate.of(TestDataUtil.PERIODE_JAHR_1, Month.NOVEMBER, 30), mergedAbschnitt1.getGueltigkeit().getGueltigBis());
		Assert.assertFalse(mergedAbschnitt1.getBgCalculationInputAsiv().isWohnsitzNichtInGemeindeGS1());

		// Alter Zivilstand gilt noch bis Ende Monat
		VerfuegungZeitabschnitt mergedAbschnitt2 = mergedZerfuegungZeitabschnitte.get(1);
		Assert.assertEquals(LocalDate.of(TestDataUtil.PERIODE_JAHR_1, Month.DECEMBER, 1), mergedAbschnitt2.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis(), mergedAbschnitt2.getGueltigkeit().getGueltigBis());
		Assert.assertTrue(mergedAbschnitt2.getBgCalculationInputAsiv().isWohnsitzNichtInGemeindeGS1());
	}

	@Test
	public void testCreateZeitAbschnitteFamSituationMutationFrom2GSTo1GS() {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(true);
		Gesuch gesuch = betreuung.extractGesuch();

		createAdressenForGS1(gesuch);

		Assert.assertNotNull(gesuch.getFamiliensituationContainer());
		Familiensituation familiensituationErstgesuch = new Familiensituation();
		familiensituationErstgesuch.setFamilienstatus(EnumFamilienstatus.VERHEIRATET);
		gesuch.getFamiliensituationContainer().setFamiliensituationErstgesuch(familiensituationErstgesuch);

		Familiensituation familiensituation = gesuch.extractFamiliensituation();
		Assert.assertNotNull(familiensituation);
		familiensituation.setFamilienstatus(EnumFamilienstatus.ALLEINERZIEHEND);
		familiensituation.setAenderungPer(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.MARCH, 26));

		List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitte = wohnsitzRule.createVerfuegungsZeitabschnitteIfApplicable(betreuung);

		verfuegungsZeitabschnitte = wohnsitzRule.normalizeZeitabschnitte(verfuegungsZeitabschnitte, gesuch.getGesuchsperiode());

		Assert.assertNotNull(verfuegungsZeitabschnitte);
		Assert.assertEquals(2, verfuegungsZeitabschnitte.size());

		// Wegzug im November -> Anspruch endet Ende November
		VerfuegungZeitabschnitt abschnitt1 = verfuegungsZeitabschnitte.get(0);
		Assert.assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb(), abschnitt1.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(LocalDate.of(TestDataUtil.PERIODE_JAHR_1, Month.NOVEMBER, 30), abschnitt1.getGueltigkeit().getGueltigBis());
		Assert.assertFalse(abschnitt1.getBgCalculationInputAsiv().isWohnsitzNichtInGemeindeGS1());

		VerfuegungZeitabschnitt abschnitt2 = verfuegungsZeitabschnitte.get(1);
		Assert.assertEquals(LocalDate.of(TestDataUtil.PERIODE_JAHR_1, Month.DECEMBER, 1), abschnitt2.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis(), abschnitt2.getGueltigkeit().getGueltigBis());
		Assert.assertTrue(abschnitt2.getBgCalculationInputAsiv().isWohnsitzNichtInGemeindeGS1());

		final List<VerfuegungZeitabschnitt> mergedZerfuegungZeitabschnitte = wohnsitzRule.mergeZeitabschnitte(verfuegungsZeitabschnitte);

		Assert.assertEquals(2, mergedZerfuegungZeitabschnitte.size());

		VerfuegungZeitabschnitt mergedAbschnitt1 = mergedZerfuegungZeitabschnitte.get(0);
		Assert.assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb(), mergedAbschnitt1.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(LocalDate.of(TestDataUtil.PERIODE_JAHR_1, Month.NOVEMBER, 30), mergedAbschnitt1.getGueltigkeit().getGueltigBis());
		Assert.assertFalse(mergedAbschnitt1.getBgCalculationInputAsiv().isWohnsitzNichtInGemeindeGS1());

		VerfuegungZeitabschnitt mergedAbschnitt2 = mergedZerfuegungZeitabschnitte.get(1);
		Assert.assertEquals(LocalDate.of(TestDataUtil.PERIODE_JAHR_1, Month.DECEMBER, 1), mergedAbschnitt2.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis(), mergedAbschnitt2.getGueltigkeit().getGueltigBis());
		Assert.assertTrue(mergedAbschnitt2.getBgCalculationInputAsiv().isWohnsitzNichtInGemeindeGS1());
	}

	@Test
	public void testNichtInBernInBernNichtInBern() {
		// der GS wohnt zuerst nicht in Bern, danach zieht er ein und dann wieder weg. Das Wegziehen sollte erst 2 Monaten danach beruecksichtigt werden
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(true);
		Gesuch gesuch = betreuung.extractGesuch();
		Assert.assertNotNull(gesuch.getGesuchsteller1());

		List<GesuchstellerAdresseContainer> adressen1 = new ArrayList<>();
		final GesuchstellerAdresseContainer adresse1GS1 = TestDataUtil.createDefaultGesuchstellerAdresseContainer(gesuch.getGesuchsteller1());
		Assert.assertNotNull(adresse1GS1.getGesuchstellerAdresseJA());
		adresse1GS1.getGesuchstellerAdresseJA().setNichtInGemeinde(true);
		adresse1GS1.getGesuchstellerAdresseJA().setGueltigkeit(new DateRange(Constants.START_OF_TIME, LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.JANUARY, 25)));
		adressen1.add(adresse1GS1);

		final GesuchstellerAdresseContainer adresse2GS1 = TestDataUtil.createDefaultGesuchstellerAdresseContainer(gesuch.getGesuchsteller1());
		Assert.assertNotNull(adresse2GS1.getGesuchstellerAdresseJA());
		adresse2GS1.getGesuchstellerAdresseJA().setNichtInGemeinde(false);
		adresse2GS1.getGesuchstellerAdresseJA().setGueltigkeit(new DateRange(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.JANUARY, 26), LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.FEBRUARY, 26)));
		adressen1.add(adresse2GS1);

		final GesuchstellerAdresseContainer adresse3GS1 = TestDataUtil.createDefaultGesuchstellerAdresseContainer(gesuch.getGesuchsteller1());
		Assert.assertNotNull(adresse3GS1.getGesuchstellerAdresseJA());
		adresse3GS1.getGesuchstellerAdresseJA().setNichtInGemeinde(true);
		adresse3GS1.getGesuchstellerAdresseJA().setGueltigkeit(new DateRange(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.FEBRUARY, 27), Constants.END_OF_TIME));
		adressen1.add(adresse3GS1);

		gesuch.getGesuchsteller1().setAdressen(adressen1);

		List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitte = wohnsitzRule.createVerfuegungsZeitabschnitteIfApplicable(betreuung);
		verfuegungsZeitabschnitte = wohnsitzRule.normalizeZeitabschnitte(verfuegungsZeitabschnitte, gesuch.getGesuchsperiode());

		Assert.assertNotNull(verfuegungsZeitabschnitte);
		Assert.assertEquals(3, verfuegungsZeitabschnitte.size());

		Assert.assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb(), verfuegungsZeitabschnitte.get(0).getGueltigkeit().getGueltigAb());
		Assert.assertEquals(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.JANUARY, 25), verfuegungsZeitabschnitte.get(0).getGueltigkeit().getGueltigBis());
		Assert.assertTrue(verfuegungsZeitabschnitte.get(0).getBgCalculationInputAsiv().isWohnsitzNichtInGemeindeGS1());

		Assert.assertEquals(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.JANUARY, 26), verfuegungsZeitabschnitte.get(1).getGueltigkeit().getGueltigAb());
		Assert.assertEquals(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.FEBRUARY, 28), verfuegungsZeitabschnitte.get(1).getGueltigkeit().getGueltigBis());
		Assert.assertFalse(verfuegungsZeitabschnitte.get(1).getBgCalculationInputAsiv().isWohnsitzNichtInGemeindeGS1());

		Assert.assertEquals(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.MARCH, 1), verfuegungsZeitabschnitte.get(2).getGueltigkeit().getGueltigAb());
		Assert.assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis(), verfuegungsZeitabschnitte.get(2).getGueltigkeit().getGueltigBis());
		Assert.assertTrue(verfuegungsZeitabschnitte.get(2).getBgCalculationInputAsiv().isWohnsitzNichtInGemeindeGS1());
	}

	@Test
	public void testUmzugNachBernOneMonthBeforeGesuchsperiode() {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(false);
		Gesuch gesuch = betreuung.extractGesuch();
		Assert.assertNotNull(gesuch.getGesuchsteller1());

		List<GesuchstellerAdresseContainer> adressen1 = new ArrayList<>();
		final GesuchstellerAdresseContainer adresse1GS1 = TestDataUtil.createDefaultGesuchstellerAdresseContainer(gesuch.getGesuchsteller1());
		Assert.assertNotNull(adresse1GS1.getGesuchstellerAdresseJA());
		adresse1GS1.getGesuchstellerAdresseJA().setNichtInGemeinde(true);
		adresse1GS1.getGesuchstellerAdresseJA().setGueltigkeit(new DateRange(Constants.START_OF_TIME, LocalDate.of(TestDataUtil.PERIODE_JAHR_1, Month.JULY, 10)));
		adressen1.add(adresse1GS1);

		final GesuchstellerAdresseContainer adresse2GS1 = TestDataUtil.createDefaultGesuchstellerAdresseContainer(gesuch.getGesuchsteller1());
		Assert.assertNotNull(adresse2GS1.getGesuchstellerAdresseJA());
		adresse2GS1.getGesuchstellerAdresseJA().setNichtInGemeinde(false);
		adresse2GS1.getGesuchstellerAdresseJA().setGueltigkeit(new DateRange(LocalDate.of(TestDataUtil.PERIODE_JAHR_1, Month.JULY, 11), Constants.END_OF_TIME));
		adressen1.add(adresse2GS1);

		gesuch.getGesuchsteller1().setAdressen(adressen1);

		List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitte = wohnsitzRule.createVerfuegungsZeitabschnitteIfApplicable(betreuung);
		verfuegungsZeitabschnitte = wohnsitzRule.normalizeZeitabschnitte(verfuegungsZeitabschnitte, gesuch.getGesuchsperiode());

		// Hinzug vor dem Start der Periode -> Es gilt für die ganze Periode -> 1 Abschnitt. Es wird aber VORNE nicht abgeschnitten (hinten schon)
		Assert.assertNotNull(verfuegungsZeitabschnitte);
		Assert.assertEquals(1, verfuegungsZeitabschnitte.size());

		Assert.assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb(), verfuegungsZeitabschnitte.get(0).getGueltigkeit().getGueltigAb());
		Assert.assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis(), verfuegungsZeitabschnitte.get(0).getGueltigkeit().getGueltigBis());
		Assert.assertFalse(verfuegungsZeitabschnitte.get(0).getBgCalculationInputAsiv().isWohnsitzNichtInGemeindeGS1());
	}

	@Test
	public void testUmzugAusBernOneMonthBeforeGesuchsperiode() {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(false);
		Gesuch gesuch = betreuung.extractGesuch();
		Assert.assertNotNull(gesuch.getGesuchsteller1());

		List<GesuchstellerAdresseContainer> adressen1 = new ArrayList<>();
		final GesuchstellerAdresseContainer adresse1GS1 = TestDataUtil.createDefaultGesuchstellerAdresseContainer(gesuch.getGesuchsteller1());
		Assert.assertNotNull(adresse1GS1.getGesuchstellerAdresseJA());
		adresse1GS1.getGesuchstellerAdresseJA().setNichtInGemeinde(false);
		adresse1GS1.getGesuchstellerAdresseJA().setGueltigkeit(new DateRange(Constants.START_OF_TIME, LocalDate.of(TestDataUtil.PERIODE_JAHR_1, Month.JULY, 10)));
		adressen1.add(adresse1GS1);

		// Adresse ausserhalb Bern ab Mitte letzter Monat der GP -> Anspruch bleibt bestehen bis Ende Monat!
		final GesuchstellerAdresseContainer adresse2GS1 = TestDataUtil.createDefaultGesuchstellerAdresseContainer(gesuch.getGesuchsteller1());
		Assert.assertNotNull(adresse2GS1.getGesuchstellerAdresseJA());
		adresse2GS1.getGesuchstellerAdresseJA().setNichtInGemeinde(true);
		adresse2GS1.getGesuchstellerAdresseJA().setGueltigkeit(new DateRange(LocalDate.of(TestDataUtil.PERIODE_JAHR_1, Month.JULY, 11), Constants.END_OF_TIME));
		adressen1.add(adresse2GS1);

		gesuch.getGesuchsteller1().setAdressen(adressen1);

		List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitte = wohnsitzRule.createVerfuegungsZeitabschnitteIfApplicable(betreuung);

		verfuegungsZeitabschnitte = wohnsitzRule.normalizeZeitabschnitte(verfuegungsZeitabschnitte, gesuch.getGesuchsperiode());

		Assert.assertNotNull(verfuegungsZeitabschnitte);
		Assert.assertEquals(1, verfuegungsZeitabschnitte.size());

		Assert.assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb(), verfuegungsZeitabschnitte.get(0).getGueltigkeit().getGueltigAb());
		Assert.assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis(), verfuegungsZeitabschnitte.get(0).getGueltigkeit().getGueltigBis());
		Assert.assertTrue(verfuegungsZeitabschnitte.get(0).getBgCalculationInputAsiv().isWohnsitzNichtInGemeindeGS1());
	}

	// HELP METHODS

	private void createAdressenForGS1(Gesuch gesuch) {
		Assert.assertNotNull(gesuch.getGesuchsteller1());
		List<GesuchstellerAdresseContainer> adressen1 = new ArrayList<>();
		final GesuchstellerAdresseContainer adresse1GS1 = TestDataUtil.createDefaultGesuchstellerAdresseContainer(gesuch.getGesuchsteller1());
		Assert.assertNotNull(adresse1GS1.getGesuchstellerAdresseJA());
		adresse1GS1.getGesuchstellerAdresseJA().setNichtInGemeinde(false);
		adresse1GS1.getGesuchstellerAdresseJA().setGueltigkeit(new DateRange(Constants.START_OF_TIME, LocalDate.of(TestDataUtil.PERIODE_JAHR_1, Month.NOVEMBER, 25)));
		adressen1.add(adresse1GS1);
		final GesuchstellerAdresseContainer adresse2GS1 = TestDataUtil.createDefaultGesuchstellerAdresseContainer(gesuch.getGesuchsteller1());
		Assert.assertNotNull(adresse2GS1.getGesuchstellerAdresseJA());
		adresse2GS1.getGesuchstellerAdresseJA().setNichtInGemeinde(true);
		adresse2GS1.getGesuchstellerAdresseJA().setGueltigkeit(new DateRange(LocalDate.of(TestDataUtil.PERIODE_JAHR_1, Month.NOVEMBER, 26), Constants.END_OF_TIME));
		adressen1.add(adresse2GS1);
		gesuch.getGesuchsteller1().setAdressen(adressen1);
	}
}
