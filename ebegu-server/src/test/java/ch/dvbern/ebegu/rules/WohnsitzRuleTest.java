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
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedHashSet;
import java.util.List;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerAdresseContainer;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.MathUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests für WohnsitzRule
 */
@SuppressWarnings("ConstantConditions")
public class WohnsitzRuleTest {

	@Test
	public void testNormalfallBeideAdresseInBern() {
		Betreuung betreuung = createTestdata(true);

		final Gesuch gesuch = betreuung.extractGesuch();

		gesuch.getGesuchsteller1().addAdresse(createGesuchstellerAdresse(
			TestDataUtil.START_PERIODE,
			TestDataUtil.ENDE_PERIODE,
			false,
			gesuch.getGesuchsteller1()));

		List<VerfuegungZeitabschnitt> zeitabschnittList = EbeguRuleTestsHelper.calculate(betreuung);

		Assert.assertNotNull(zeitabschnittList);
		Assert.assertEquals(1, zeitabschnittList.size());
		VerfuegungZeitabschnitt abschnittInBern = zeitabschnittList.get(0);
		Assert.assertFalse(abschnittInBern.getBgCalculationInputAsiv().isWohnsitzNichtInGemeindeGS1());
		Assert.assertEquals(100, abschnittInBern.getAnspruchberechtigtesPensum());
		Assert.assertEquals(MathUtil.DEFAULT.from(100), abschnittInBern.getBgPensum());
	}

	@Test
	public void testEinGesuchstellerAdresseNichtBern() {
		Betreuung betreuung = createTestdata(false);

		final Gesuch gesuch = betreuung.extractGesuch();

		gesuch.getGesuchsteller1().addAdresse(createGesuchstellerAdresse(
			TestDataUtil.START_PERIODE,
			TestDataUtil.ENDE_PERIODE,
			true,
			gesuch.getGesuchsteller1()));

		createDossier(gesuch);

		List<VerfuegungZeitabschnitt> zeitabschnittList = EbeguRuleTestsHelper.calculate(betreuung);

		Assert.assertNotNull(zeitabschnittList);
		Assert.assertEquals(1, zeitabschnittList.size());
		VerfuegungZeitabschnitt abschnitt = zeitabschnittList.get(0);
		Assert.assertTrue(abschnitt.getBgCalculationInputAsiv().isWohnsitzNichtInGemeindeGS1());
		Assert.assertEquals(0, abschnitt.getAnspruchberechtigtesPensum());
		Assert.assertEquals(MathUtil.DEFAULT.from(0), abschnitt.getBgPensum());
	}

	@Test
	public void testEinGesuchstellerAdresseInBern() {
		Betreuung betreuung = createTestdata(false);

		final Gesuch gesuch = betreuung.extractGesuch();

		gesuch.getGesuchsteller1().addAdresse(createGesuchstellerAdresse(
			TestDataUtil.START_PERIODE,
			TestDataUtil.ENDE_PERIODE,
			false,
			gesuch.getGesuchsteller1()));

		List<VerfuegungZeitabschnitt> zeitabschnittList = EbeguRuleTestsHelper.calculate(betreuung);

		Assert.assertNotNull(zeitabschnittList);
		Assert.assertEquals(1, zeitabschnittList.size());
		VerfuegungZeitabschnitt abschnittInBern = zeitabschnittList.get(0);
		Assert.assertFalse(abschnittInBern.getBgCalculationInputAsiv().isWohnsitzNichtInGemeindeGS1());
		Assert.assertEquals(100, abschnittInBern.getAnspruchberechtigtesPensum());
		Assert.assertEquals(MathUtil.DEFAULT.from(100), abschnittInBern.getBgPensum());
	}

	@Test
	public void testZweiGesuchstellerEinerDavonInBern() {
		Betreuung betreuung = createTestdata(true);

		final Gesuch gesuch = betreuung.extractGesuch();

		gesuch.getGesuchsteller1().addAdresse(createGesuchstellerAdresse(
			TestDataUtil.START_PERIODE,
			TestDataUtil.ENDE_PERIODE,
			false,
			gesuch.getGesuchsteller1()));
		gesuch.getGesuchsteller2().addAdresse(createGesuchstellerAdresse(
			TestDataUtil.START_PERIODE,
			TestDataUtil.ENDE_PERIODE,
			true,
			gesuch.getGesuchsteller2()));

		List<VerfuegungZeitabschnitt> zeitabschnittList = EbeguRuleTestsHelper.calculate(betreuung);

		Assert.assertNotNull(zeitabschnittList);
		Assert.assertEquals(1, zeitabschnittList.size());
		VerfuegungZeitabschnitt abschnittInBern = zeitabschnittList.get(0);
		Assert.assertFalse(abschnittInBern.getBgCalculationInputAsiv().isWohnsitzNichtInGemeindeGS1());
		Assert.assertEquals(100, abschnittInBern.getAnspruchberechtigtesPensum());
		Assert.assertEquals(MathUtil.DEFAULT.from(100), abschnittInBern.getBgPensum());
	}

	@Test
	public void testZuzug() {
		LocalDate zuzugsDatum = LocalDate.of(TestDataUtil.PERIODE_JAHR_1, Month.OCTOBER, 16);
		Betreuung betreuung = createTestdata(true);

		final Gesuch gesuch = betreuung.extractGesuch();

		gesuch.getGesuchsteller1().addAdresse(createGesuchstellerAdresse(
			TestDataUtil.START_PERIODE,
			zuzugsDatum.minusDays(1),
			true,
			gesuch.getGesuchsteller1()));
		gesuch.getGesuchsteller1().addAdresse(createGesuchstellerAdresse(
			zuzugsDatum,
			TestDataUtil.ENDE_PERIODE,
			false,
			gesuch.getGesuchsteller1()));
		gesuch.getGesuchsteller2().addAdresse(createGesuchstellerAdresse(
			TestDataUtil.START_PERIODE,
			TestDataUtil.ENDE_PERIODE,
			true,
			gesuch.getGesuchsteller2()));

		createDossier(gesuch);

		List<VerfuegungZeitabschnitt> zeitabschnittList = EbeguRuleTestsHelper.calculate(betreuung);

		Assert.assertNotNull(zeitabschnittList);
		Assert.assertEquals(4, zeitabschnittList.size());
		VerfuegungZeitabschnitt abschnittNichtInBern = zeitabschnittList.get(0);
		Assert.assertTrue(abschnittNichtInBern.getBgCalculationInputAsiv().isWohnsitzNichtInGemeindeGS1());
		Assert.assertEquals(0, abschnittNichtInBern.getAnspruchberechtigtesPensum());
		Assert.assertEquals(MathUtil.DEFAULT.from(0), abschnittNichtInBern.getBgPensum());
		VerfuegungZeitabschnitt abschnittInBern = zeitabschnittList.get(2);
		Assert.assertEquals(zuzugsDatum, abschnittInBern.getGueltigkeit().getGueltigAb());
		Assert.assertFalse(abschnittInBern.getBgCalculationInputAsiv().isWohnsitzNichtInGemeindeGS1());
		Assert.assertEquals(100, abschnittInBern.getAnspruchberechtigtesPensum());
		Assert.assertEquals(MathUtil.DEFAULT.from(100), abschnittInBern.getBgPensum());
	}

	@Test
	public void testWegzug() {
		LocalDate wegzugsDatum = LocalDate.of(TestDataUtil.PERIODE_JAHR_1, Month.OCTOBER, 16);
		Betreuung betreuung = createTestdata(true);

		final Gesuch gesuch = betreuung.extractGesuch();

		gesuch.getGesuchsteller1().addAdresse(createGesuchstellerAdresse(
			TestDataUtil.START_PERIODE,
			wegzugsDatum.minusDays(1),
			false,
			gesuch.getGesuchsteller1()));
		gesuch.getGesuchsteller1().addAdresse(createGesuchstellerAdresse(
			wegzugsDatum,
			TestDataUtil.ENDE_PERIODE,
			true,
			gesuch.getGesuchsteller1()));

		gesuch.getGesuchsteller2().addAdresse(createGesuchstellerAdresse(
			TestDataUtil.START_PERIODE,
			TestDataUtil.ENDE_PERIODE,
			true,
			gesuch.getGesuchsteller2()));

		createDossier(gesuch);

		List<VerfuegungZeitabschnitt> zeitabschnittList = EbeguRuleTestsHelper.calculate(betreuung);

		Assert.assertNotNull(zeitabschnittList);
		Assert.assertEquals(2, zeitabschnittList.size());
		VerfuegungZeitabschnitt abschnittInBern = zeitabschnittList.get(0);
		Assert.assertFalse(abschnittInBern.getBgCalculationInputAsiv().isWohnsitzNichtInGemeindeGS1());
		Assert.assertEquals(100, abschnittInBern.getAnspruchberechtigtesPensum());
		Assert.assertEquals(MathUtil.DEFAULT.from(100), abschnittInBern.getBgPensum());
		VerfuegungZeitabschnitt abschnittNichtInBern = zeitabschnittList.get(1);
		Assert.assertTrue(abschnittNichtInBern.getBgCalculationInputAsiv().isWohnsitzNichtInGemeindeGS1());
		//Anspruch noch 2 Monate nach wegzug auf Ende Monat
		Assert.assertEquals(wegzugsDatum.with(TemporalAdjusters.lastDayOfMonth()), abschnittInBern.getGueltigkeit().getGueltigBis());
		Assert.assertEquals(0, abschnittNichtInBern.getAnspruchberechtigtesPensum());
		Assert.assertEquals(MathUtil.DEFAULT.from(0), abschnittNichtInBern.getBgPensum());
	}

	@Test
	public void testZweiGesuchstellerEinerDavonInBernMutationFamiliensituation() {
		Betreuung betreuung = createTestdata(true);
		final Gesuch gesuch = betreuung.extractGesuch();

		gesuch.getFamiliensituationContainer().setFamiliensituationErstgesuch(new Familiensituation());
		gesuch.extractFamiliensituationErstgesuch().setFamilienstatus(EnumFamilienstatus.ALLEINERZIEHEND);
		gesuch.extractFamiliensituation().setFamilienstatus(EnumFamilienstatus.VERHEIRATET);
		gesuch.extractFamiliensituation().setAenderungPer(LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.MARCH, 26));

		gesuch.getGesuchsteller1().addAdresse(createGesuchstellerAdresse(
			TestDataUtil.START_PERIODE,
			TestDataUtil.ENDE_PERIODE,
			true,
			gesuch.getGesuchsteller1()));
		gesuch.getGesuchsteller2().addAdresse(createGesuchstellerAdresse(
			TestDataUtil.START_PERIODE,
			TestDataUtil.ENDE_PERIODE,
			false,
			gesuch.getGesuchsteller2()));

		createDossier(gesuch);

		List<VerfuegungZeitabschnitt> zeitabschnittList = EbeguRuleTestsHelper.calculate(betreuung);
		Assert.assertNotNull(zeitabschnittList);
		Assert.assertEquals(2, zeitabschnittList.size());

		VerfuegungZeitabschnitt abschnittNichtInBern1 = zeitabschnittList.get(0);
		Assert.assertTrue(abschnittNichtInBern1.getBgCalculationInputAsiv().isWohnsitzNichtInGemeindeGS1());
		Assert.assertEquals(0, abschnittNichtInBern1.getAnspruchberechtigtesPensum());
		Assert.assertEquals(MathUtil.DEFAULT.from(0), abschnittNichtInBern1.getBgPensum());

		VerfuegungZeitabschnitt abschnittImmerNochNichtInBern2 = zeitabschnittList.get(1);
		Assert.assertTrue(abschnittImmerNochNichtInBern2.getBgCalculationInputAsiv().isWohnsitzNichtInGemeindeGS1());
		Assert.assertEquals(0, abschnittImmerNochNichtInBern2.getAnspruchberechtigtesPensum());
		Assert.assertEquals(MathUtil.DEFAULT.from(0), abschnittImmerNochNichtInBern2.getBgPensum());
	}

	private Betreuung createTestdata(boolean zweigesuchsteller) {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(zweigesuchsteller);
		betreuung.getInstitutionStammdaten().setBetreuungsangebotTyp(BetreuungsangebotTyp.KITA);
		betreuung.setBetreuungspensumContainers(new LinkedHashSet<>());
		BetreuungspensumContainer betreuungspensumContainer = new BetreuungspensumContainer();
		betreuungspensumContainer.setBetreuung(betreuung);
		DateRange gueltigkeit = new DateRange(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE);
		betreuungspensumContainer.setBetreuungspensumJA(new Betreuungspensum(gueltigkeit));
		betreuungspensumContainer.getBetreuungspensumJA().setPensum(MathUtil.DEFAULT.from(100));
		betreuungspensumContainer.getBetreuungspensumJA().setMonatlicheHauptmahlzeiten(BigDecimal.ZERO);
		betreuungspensumContainer.getBetreuungspensumJA().setMonatlicheNebenmahlzeiten(BigDecimal.ZERO);
		betreuung.getBetreuungspensumContainers().add(betreuungspensumContainer);
		betreuung.getKind().getGesuch().getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 100));
		if (zweigesuchsteller) {
			betreuung.getKind().getGesuch().getGesuchsteller2().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 100));
		}
		return betreuung;
	}

	private GesuchstellerAdresseContainer createGesuchstellerAdresse(LocalDate von, LocalDate bis, boolean nichtInGemeinde,
		GesuchstellerContainer gesuchsteller) {
		GesuchstellerAdresseContainer adresse = TestDataUtil.createDefaultGesuchstellerAdresseContainer(gesuchsteller);
		adresse.getGesuchstellerAdresseJA().setNichtInGemeinde(nichtInGemeinde);
		adresse.extractGueltigkeit().setGueltigAb(von);
		adresse.extractGueltigkeit().setGueltigBis(bis);
		return adresse;
	}

	private void createDossier(Gesuch gesuch) {
		Dossier dossier = TestDataUtil.createDefaultDossier();
		gesuch.setDossier(dossier);
	}
}
