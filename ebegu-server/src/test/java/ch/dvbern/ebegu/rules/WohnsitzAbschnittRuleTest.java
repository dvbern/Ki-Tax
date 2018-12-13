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
import ch.dvbern.ebegu.enums.EnumGesuchstellerKardinalitaet;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests fuer WohnsitzAbschnittRule
 */
public class WohnsitzAbschnittRuleTest {

	final WohnsitzAbschnittRule wohnsitzRule = new WohnsitzAbschnittRule(Constants.DEFAULT_GUELTIGKEIT);

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
		List<GesuchstellerAdresseContainer> adressen2 = new ArrayList<>();
		final GesuchstellerAdresseContainer adresse1GS2 = TestDataUtil.createDefaultGesuchstellerAdresseContainer(gesuch.getGesuchsteller2());
		Assert.assertNotNull(adresse1GS2.getGesuchstellerAdresseJA());
		adresse1GS2.getGesuchstellerAdresseJA().setNichtInGemeinde(true);
		adresse1GS2.getGesuchstellerAdresseJA().setGueltigkeit(new DateRange(Constants.START_OF_TIME, LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.APRIL, 25)));
		adressen2.add(adresse1GS2);
		final GesuchstellerAdresseContainer adresse2GS2 = TestDataUtil.createDefaultGesuchstellerAdresseContainer(gesuch.getGesuchsteller2());
		Assert.assertNotNull(adresse2GS2.getGesuchstellerAdresseJA());
		adresse2GS2.getGesuchstellerAdresseJA().setNichtInGemeinde(false);
		adresse2GS2.getGesuchstellerAdresseJA().setGueltigkeit(new DateRange(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.APRIL, 26), Constants.END_OF_TIME));
		adressen2.add(adresse2GS2);
		gesuch.getGesuchsteller2().setAdressen(adressen2);

		List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitte = wohnsitzRule.createVerfuegungsZeitabschnitte(betreuung);
		verfuegungsZeitabschnitte = wohnsitzRule.normalizeZeitabschnitte(verfuegungsZeitabschnitte, gesuch.getGesuchsperiode());

		Assert.assertNotNull(verfuegungsZeitabschnitte);
		// Es werden 4 Abschnitte erwartet weil sie danach noch gemerged werden muessen
		Assert.assertEquals(4, verfuegungsZeitabschnitte.size());

		Assert.assertEquals(Constants.START_OF_TIME, verfuegungsZeitabschnitte.get(0).getGueltigkeit().getGueltigAb());
		Assert.assertEquals(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.MARCH, 31), verfuegungsZeitabschnitte.get(0).getGueltigkeit().getGueltigBis());
		Assert.assertFalse(verfuegungsZeitabschnitte.get(0).isWohnsitzNichtInGemeindeGS1());
		Assert.assertTrue(verfuegungsZeitabschnitte.get(0).isWohnsitzNichtInGemeindeGS2());

		Assert.assertEquals(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.APRIL, 1), verfuegungsZeitabschnitte.get(1).getGueltigkeit().getGueltigAb());
		Assert.assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis(), verfuegungsZeitabschnitte.get(1).getGueltigkeit().getGueltigBis());
		Assert.assertTrue(verfuegungsZeitabschnitte.get(1).isWohnsitzNichtInGemeindeGS1());
		Assert.assertTrue(verfuegungsZeitabschnitte.get(1).isWohnsitzNichtInGemeindeGS2());

		Assert.assertEquals(Constants.START_OF_TIME, verfuegungsZeitabschnitte.get(2).getGueltigkeit().getGueltigAb());
		Assert.assertEquals(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.APRIL, 25), verfuegungsZeitabschnitte.get(2).getGueltigkeit().getGueltigBis());
		Assert.assertTrue(verfuegungsZeitabschnitte.get(2).isWohnsitzNichtInGemeindeGS1());
		Assert.assertTrue(verfuegungsZeitabschnitte.get(2).isWohnsitzNichtInGemeindeGS2());

		Assert.assertEquals(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.APRIL, 26), verfuegungsZeitabschnitte.get(3).getGueltigkeit().getGueltigAb());
		Assert.assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis(), verfuegungsZeitabschnitte.get(3).getGueltigkeit().getGueltigBis());
		Assert.assertTrue(verfuegungsZeitabschnitte.get(3).isWohnsitzNichtInGemeindeGS1());
		Assert.assertFalse(verfuegungsZeitabschnitte.get(3).isWohnsitzNichtInGemeindeGS2());

		final List<VerfuegungZeitabschnitt> mergedZerfuegungZeitabschnitte = wohnsitzRule.mergeZeitabschnitte(verfuegungsZeitabschnitte);

		Assert.assertEquals(3, mergedZerfuegungZeitabschnitte.size());

		Assert.assertEquals(Constants.START_OF_TIME, mergedZerfuegungZeitabschnitte.get(0).getGueltigkeit().getGueltigAb());
		Assert.assertEquals(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.MARCH, 31), mergedZerfuegungZeitabschnitte.get(0).getGueltigkeit().getGueltigBis());
		Assert.assertFalse(mergedZerfuegungZeitabschnitte.get(0).isWohnsitzNichtInGemeindeGS1());
		Assert.assertTrue(mergedZerfuegungZeitabschnitte.get(0).isWohnsitzNichtInGemeindeGS2());

		Assert.assertEquals(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.APRIL, 1), mergedZerfuegungZeitabschnitte.get(1).getGueltigkeit().getGueltigAb());
		Assert.assertEquals(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.APRIL, 25), mergedZerfuegungZeitabschnitte.get(1).getGueltigkeit().getGueltigBis());
		Assert.assertTrue(mergedZerfuegungZeitabschnitte.get(1).isWohnsitzNichtInGemeindeGS1());
		Assert.assertTrue(mergedZerfuegungZeitabschnitte.get(1).isWohnsitzNichtInGemeindeGS2());

		Assert.assertEquals(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.APRIL, 26), mergedZerfuegungZeitabschnitte.get(2).getGueltigkeit().getGueltigAb());
		Assert.assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis(), mergedZerfuegungZeitabschnitte.get(2).getGueltigkeit().getGueltigBis());
		Assert.assertTrue(mergedZerfuegungZeitabschnitte.get(2).isWohnsitzNichtInGemeindeGS1());
		Assert.assertFalse(mergedZerfuegungZeitabschnitte.get(2).isWohnsitzNichtInGemeindeGS2());
	}

	@Test
	public void testCreateZeitAbschnitteFamSituationMutationFrom1GSTo2GS() {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(true);
		Gesuch gesuch = betreuung.extractGesuch();

		createAdressenForGS1(gesuch);

		createAdressenForGS2(gesuch);

		Assert.assertNotNull(gesuch.getFamiliensituationContainer());
		Familiensituation familiensituationErstgesuch = new Familiensituation();
		gesuch.getFamiliensituationContainer().setFamiliensituationErstgesuch(familiensituationErstgesuch);
		familiensituationErstgesuch.setFamilienstatus(EnumFamilienstatus.ALLEINERZIEHEND);
		familiensituationErstgesuch.setGesuchstellerKardinalitaet(EnumGesuchstellerKardinalitaet.ALLEINE);
		Familiensituation familiensituation = gesuch.extractFamiliensituation();
		Assert.assertNotNull(familiensituation);
		familiensituation.setAenderungPer(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.MARCH, 26));

		List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitte = wohnsitzRule.createVerfuegungsZeitabschnitte(betreuung);
		verfuegungsZeitabschnitte = wohnsitzRule.normalizeZeitabschnitte(verfuegungsZeitabschnitte, gesuch.getGesuchsperiode());

		Assert.assertNotNull(verfuegungsZeitabschnitte);
		Assert.assertEquals(3, verfuegungsZeitabschnitte.size());

		VerfuegungZeitabschnitt abschnitt1 = verfuegungsZeitabschnitte.get(0);
		Assert.assertEquals(Constants.START_OF_TIME, abschnitt1.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.JANUARY, 31), abschnitt1.getGueltigkeit().getGueltigBis());
		Assert.assertFalse(abschnitt1.isWohnsitzNichtInGemeindeGS1());
		Assert.assertTrue(abschnitt1.isWohnsitzNichtInGemeindeGS2());

		VerfuegungZeitabschnitt abschnitt2 = verfuegungsZeitabschnitte.get(1);
		Assert.assertEquals(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.FEBRUARY, 1), abschnitt2.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis(), abschnitt2.getGueltigkeit().getGueltigBis());
		Assert.assertTrue(abschnitt2.isWohnsitzNichtInGemeindeGS1());
		Assert.assertTrue(abschnitt2.isWohnsitzNichtInGemeindeGS2());

		// Der Zivilstand wird erst ab dem Folgemonat aktiv!
		VerfuegungZeitabschnitt abschnitt3 = verfuegungsZeitabschnitte.get(2);
		Assert.assertEquals(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.APRIL, 1), abschnitt3.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis(), abschnitt3.getGueltigkeit().getGueltigBis());
		Assert.assertTrue(abschnitt3.isWohnsitzNichtInGemeindeGS1());
		Assert.assertFalse(abschnitt3.isWohnsitzNichtInGemeindeGS2());

		final List<VerfuegungZeitabschnitt> mergedZerfuegungZeitabschnitte = wohnsitzRule.mergeZeitabschnitte(verfuegungsZeitabschnitte);

		Assert.assertEquals(3, mergedZerfuegungZeitabschnitte.size());

		VerfuegungZeitabschnitt mergedAbschnitt1 = mergedZerfuegungZeitabschnitte.get(0);
		Assert.assertEquals(Constants.START_OF_TIME, mergedAbschnitt1.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.JANUARY, 31), mergedAbschnitt1.getGueltigkeit().getGueltigBis());
		Assert.assertFalse(mergedAbschnitt1.isWohnsitzNichtInGemeindeGS1());
		Assert.assertTrue(mergedAbschnitt1.isWohnsitzNichtInGemeindeGS2());

		// Alter Zivilstand gilt noch bis Ende Monat
		VerfuegungZeitabschnitt mergedAbschnitt2 = mergedZerfuegungZeitabschnitte.get(1);
		Assert.assertEquals(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.FEBRUARY, 1), mergedAbschnitt2.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.MARCH, 31), mergedAbschnitt2.getGueltigkeit().getGueltigBis());
		Assert.assertTrue(mergedAbschnitt2.isWohnsitzNichtInGemeindeGS1());
		Assert.assertTrue(mergedAbschnitt2.isWohnsitzNichtInGemeindeGS2());

		// Neuer Zivilstand gilt ab Folgemonat
		VerfuegungZeitabschnitt mergedAbschnitt3 = mergedZerfuegungZeitabschnitte.get(2);
		Assert.assertEquals(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.APRIL, 1), mergedAbschnitt3.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis(), mergedAbschnitt3.getGueltigkeit().getGueltigBis());
		Assert.assertTrue(mergedAbschnitt3.isWohnsitzNichtInGemeindeGS1());
		Assert.assertFalse(mergedAbschnitt3.isWohnsitzNichtInGemeindeGS2());
	}

	@Test
	public void testCreateZeitAbschnitteFamSituationMutationFrom2GSTo1GS() {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(true);
		Gesuch gesuch = betreuung.extractGesuch();

		createAdressenForGS1(gesuch);

		createAdressenForGS2(gesuch);

		Assert.assertNotNull(gesuch.getFamiliensituationContainer());
		Familiensituation familiensituationErstgesuch = new Familiensituation();
		familiensituationErstgesuch.setFamilienstatus(EnumFamilienstatus.VERHEIRATET);
		gesuch.getFamiliensituationContainer().setFamiliensituationErstgesuch(familiensituationErstgesuch);

		Familiensituation familiensituation = gesuch.extractFamiliensituation();
		Assert.assertNotNull(familiensituation);
		familiensituation.setFamilienstatus(EnumFamilienstatus.ALLEINERZIEHEND);
		familiensituation.setGesuchstellerKardinalitaet(EnumGesuchstellerKardinalitaet.ALLEINE);
		familiensituation.setAenderungPer(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.MARCH, 26));

		List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitte = wohnsitzRule.createVerfuegungsZeitabschnitte(betreuung);

		verfuegungsZeitabschnitte = wohnsitzRule.normalizeZeitabschnitte(verfuegungsZeitabschnitte, gesuch.getGesuchsperiode());

		Assert.assertNotNull(verfuegungsZeitabschnitte);
		Assert.assertEquals(4, verfuegungsZeitabschnitte.size());

		VerfuegungZeitabschnitt abschnitt1 = verfuegungsZeitabschnitte.get(0);
		Assert.assertEquals(Constants.START_OF_TIME, abschnitt1.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.JANUARY, 31), abschnitt1.getGueltigkeit().getGueltigBis());
		Assert.assertFalse(abschnitt1.isWohnsitzNichtInGemeindeGS1());
		Assert.assertTrue(abschnitt1.isWohnsitzNichtInGemeindeGS2());

		VerfuegungZeitabschnitt abschnitt2 = verfuegungsZeitabschnitte.get(1);
		Assert.assertEquals(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.FEBRUARY, 1), abschnitt2.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis(), abschnitt2.getGueltigkeit().getGueltigBis());
		Assert.assertTrue(abschnitt2.isWohnsitzNichtInGemeindeGS1());
		Assert.assertTrue(abschnitt2.isWohnsitzNichtInGemeindeGS2());

		VerfuegungZeitabschnitt abschnitt3 = verfuegungsZeitabschnitte.get(2);
		Assert.assertEquals(Constants.START_OF_TIME, abschnitt3.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.FEBRUARY, 25), abschnitt3.getGueltigkeit().getGueltigBis());
		Assert.assertTrue(abschnitt3.isWohnsitzNichtInGemeindeGS1());
		Assert.assertTrue(abschnitt3.isWohnsitzNichtInGemeindeGS2());

		VerfuegungZeitabschnitt abschnitt4 = verfuegungsZeitabschnitte.get(3);
		Assert.assertEquals(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.FEBRUARY, 26), abschnitt4.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.MARCH, 31), abschnitt4.getGueltigkeit().getGueltigBis());
		Assert.assertTrue(abschnitt4.isWohnsitzNichtInGemeindeGS1());
		Assert.assertFalse(abschnitt4.isWohnsitzNichtInGemeindeGS2());

		final List<VerfuegungZeitabschnitt> mergedZerfuegungZeitabschnitte = wohnsitzRule.mergeZeitabschnitte(verfuegungsZeitabschnitte);

		Assert.assertEquals(4, mergedZerfuegungZeitabschnitte.size());

		VerfuegungZeitabschnitt mergedAbschnitt1 = mergedZerfuegungZeitabschnitte.get(0);
		Assert.assertEquals(Constants.START_OF_TIME, mergedAbschnitt1.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.JANUARY, 31), mergedAbschnitt1.getGueltigkeit().getGueltigBis());
		Assert.assertFalse(mergedAbschnitt1.isWohnsitzNichtInGemeindeGS1());
		Assert.assertTrue(mergedAbschnitt1.isWohnsitzNichtInGemeindeGS2());

		VerfuegungZeitabschnitt mergedAbschnitt2 = mergedZerfuegungZeitabschnitte.get(1);
		Assert.assertEquals(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.FEBRUARY, 1), mergedAbschnitt2.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.FEBRUARY, 25), mergedAbschnitt2.getGueltigkeit().getGueltigBis());
		Assert.assertTrue(mergedAbschnitt2.isWohnsitzNichtInGemeindeGS1());
		Assert.assertTrue(mergedAbschnitt2.isWohnsitzNichtInGemeindeGS2());

		VerfuegungZeitabschnitt mergedAbschnitt3 = mergedZerfuegungZeitabschnitte.get(2);
		Assert.assertEquals(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.FEBRUARY, 26), mergedAbschnitt3.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.MARCH, 31), mergedAbschnitt3.getGueltigkeit().getGueltigBis());
		Assert.assertTrue(mergedAbschnitt3.isWohnsitzNichtInGemeindeGS1());
		Assert.assertFalse(mergedAbschnitt3.isWohnsitzNichtInGemeindeGS2());

		VerfuegungZeitabschnitt mergedAbschnitt4 = mergedZerfuegungZeitabschnitte.get(3);
		Assert.assertEquals(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.APRIL, 1), mergedAbschnitt4.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis(), mergedAbschnitt4.getGueltigkeit().getGueltigBis());
		Assert.assertTrue(mergedAbschnitt4.isWohnsitzNichtInGemeindeGS1());
		Assert.assertTrue(mergedAbschnitt4.isWohnsitzNichtInGemeindeGS2());
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

		List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitte = wohnsitzRule.createVerfuegungsZeitabschnitte(betreuung);
		verfuegungsZeitabschnitte = wohnsitzRule.normalizeZeitabschnitte(verfuegungsZeitabschnitte, gesuch.getGesuchsperiode());

		Assert.assertNotNull(verfuegungsZeitabschnitte);
		Assert.assertEquals(3, verfuegungsZeitabschnitte.size());

		Assert.assertEquals(Constants.START_OF_TIME, verfuegungsZeitabschnitte.get(0).getGueltigkeit().getGueltigAb());
		Assert.assertEquals(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.JANUARY, 25), verfuegungsZeitabschnitte.get(0).getGueltigkeit().getGueltigBis());
		Assert.assertTrue(verfuegungsZeitabschnitte.get(0).isWohnsitzNichtInGemeindeGS1());
		Assert.assertTrue(verfuegungsZeitabschnitte.get(0).isWohnsitzNichtInGemeindeGS2());

		Assert.assertEquals(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.JANUARY, 26), verfuegungsZeitabschnitte.get(1).getGueltigkeit().getGueltigAb());
		// muss 2 Monate spaeter enden
		Assert.assertEquals(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.APRIL, 30), verfuegungsZeitabschnitte.get(1).getGueltigkeit().getGueltigBis());
		Assert.assertFalse(verfuegungsZeitabschnitte.get(1).isWohnsitzNichtInGemeindeGS1());
		Assert.assertTrue(verfuegungsZeitabschnitte.get(1).isWohnsitzNichtInGemeindeGS2());

		Assert.assertEquals(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.MAY, 1), verfuegungsZeitabschnitte.get(2).getGueltigkeit().getGueltigAb());
		Assert.assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis(), verfuegungsZeitabschnitte.get(2).getGueltigkeit().getGueltigBis());
		Assert.assertTrue(verfuegungsZeitabschnitte.get(2).isWohnsitzNichtInGemeindeGS1());
		Assert.assertTrue(verfuegungsZeitabschnitte.get(2).isWohnsitzNichtInGemeindeGS2());
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

		List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitte = wohnsitzRule.createVerfuegungsZeitabschnitte(betreuung);
		verfuegungsZeitabschnitte = wohnsitzRule.normalizeZeitabschnitte(verfuegungsZeitabschnitte, gesuch.getGesuchsperiode());

		// Hinzug vor dem Start der Periode -> Es gilt für die ganze Periode -> 1 Abschnitt. Es wird aber VORNE nicht abgeschnitten (hinten schon)
		Assert.assertNotNull(verfuegungsZeitabschnitte);
		Assert.assertEquals(1, verfuegungsZeitabschnitte.size());

		Assert.assertEquals(LocalDate.of(TestDataUtil.PERIODE_JAHR_1, Month.JULY, 11), verfuegungsZeitabschnitte.get(0).getGueltigkeit().getGueltigAb());
		Assert.assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis(), verfuegungsZeitabschnitte.get(0).getGueltigkeit().getGueltigBis());
		Assert.assertFalse(verfuegungsZeitabschnitte.get(0).isWohnsitzNichtInGemeindeGS1());
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

		final GesuchstellerAdresseContainer adresse2GS1 = TestDataUtil.createDefaultGesuchstellerAdresseContainer(gesuch.getGesuchsteller1());
		Assert.assertNotNull(adresse2GS1.getGesuchstellerAdresseJA());
		adresse2GS1.getGesuchstellerAdresseJA().setNichtInGemeinde(true);
		adresse2GS1.getGesuchstellerAdresseJA().setGueltigkeit(new DateRange(LocalDate.of(TestDataUtil.PERIODE_JAHR_1, Month.JULY, 11), Constants.END_OF_TIME));
		adressen1.add(adresse2GS1);

		gesuch.getGesuchsteller1().setAdressen(adressen1);

		List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitte = wohnsitzRule.createVerfuegungsZeitabschnitte(betreuung);

		verfuegungsZeitabschnitte = wohnsitzRule.normalizeZeitabschnitte(verfuegungsZeitabschnitte, gesuch.getGesuchsperiode());

		Assert.assertNotNull(verfuegungsZeitabschnitte);
		Assert.assertEquals(2, verfuegungsZeitabschnitte.size());

		Assert.assertEquals(Constants.START_OF_TIME, verfuegungsZeitabschnitte.get(0).getGueltigkeit().getGueltigAb());
		Assert.assertEquals(LocalDate.of(TestDataUtil.PERIODE_JAHR_1, Month.SEPTEMBER, 30), verfuegungsZeitabschnitte.get(0).getGueltigkeit().getGueltigBis());
		Assert.assertFalse(verfuegungsZeitabschnitte.get(0).isWohnsitzNichtInGemeindeGS1());

		Assert.assertEquals(LocalDate.of(TestDataUtil.PERIODE_JAHR_1, Month.OCTOBER, 1), verfuegungsZeitabschnitte.get(1).getGueltigkeit().getGueltigAb());
		Assert.assertEquals(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis(), verfuegungsZeitabschnitte.get(1).getGueltigkeit().getGueltigBis());
		Assert.assertTrue(verfuegungsZeitabschnitte.get(1).isWohnsitzNichtInGemeindeGS1());
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

	private void createAdressenForGS2(Gesuch gesuch) {
		Assert.assertNotNull(gesuch.getGesuchsteller2());
		List<GesuchstellerAdresseContainer> adressen2 = new ArrayList<>();
		final GesuchstellerAdresseContainer adresse1GS2 = TestDataUtil.createDefaultGesuchstellerAdresseContainer(gesuch.getGesuchsteller2());
		Assert.assertNotNull(adresse1GS2.getGesuchstellerAdresseJA());
		adresse1GS2.getGesuchstellerAdresseJA().setNichtInGemeinde(true);
		adresse1GS2.getGesuchstellerAdresseJA().setGueltigkeit(new DateRange(Constants.START_OF_TIME, LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.FEBRUARY, 25)));
		adressen2.add(adresse1GS2);
		final GesuchstellerAdresseContainer adresse2GS2 = TestDataUtil.createDefaultGesuchstellerAdresseContainer(gesuch.getGesuchsteller2());
		Assert.assertNotNull(adresse2GS2.getGesuchstellerAdresseJA());
		adresse2GS2.getGesuchstellerAdresseJA().setNichtInGemeinde(false);
		adresse2GS2.getGesuchstellerAdresseJA().setGueltigkeit(new DateRange(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.FEBRUARY, 26), Constants.END_OF_TIME));
		adressen2.add(adresse2GS2);
		gesuch.getGesuchsteller2().setAdressen(adressen2);
	}
}
