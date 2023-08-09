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
import java.util.HashSet;
import java.util.List;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Fachstelle;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.PensumFachstelle;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.IntegrationTyp;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.MathUtil;
import org.junit.Assert;
import org.junit.Test;

import static ch.dvbern.ebegu.util.Constants.ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

/**
 * Tests für Fachstellen-Regel
 */
public class FachstelleRuleTest {

	@Test
	public void testKitaMitFachstelleWenigerAlsPensum() {
		Betreuung betreuung = EbeguRuleTestsHelper.createBetreuungWithPensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE,
			BetreuungsangebotTyp.KITA, 60, new BigDecimal(2000));
		final Gesuch gesuch = betreuung.extractGesuch();
		TestDataUtil.createDefaultAdressenForGS(gesuch, false);
		final PensumFachstelle pensumFachstelle = new PensumFachstelle();
		pensumFachstelle.setPensum(40);
		pensumFachstelle.setGueltigkeit(new DateRange(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE));
		betreuung.getKind().getKindJA().getPensumFachstelle().add(pensumFachstelle);
		Assert.assertNotNull(betreuung.getKind().getGesuch().getGesuchsteller1());
		betreuung.getKind().getGesuch().getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 60));
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);

		Assert.assertNotNull(result);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(Integer.valueOf(60), result.get(0).getBgCalculationInputAsiv().getErwerbspensumGS1());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), result.get(0).getBetreuungspensumProzent());
		Assert.assertEquals(60 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, result.get(0).getAnspruchberechtigtesPensum());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), result.get(0).getBgPensum());
		Assert.assertEquals(-1, result.get(0).getBgCalculationInputAsiv().getAnspruchspensumRest());
		List<VerfuegungZeitabschnitt> nextZeitabschn = EbeguRuleTestsHelper.initializeRestanspruchForNextBetreuung(betreuung, result);
		Assert.assertEquals(0 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, nextZeitabschn.get(0).getBgCalculationInputAsiv().getAnspruchspensumRest());
	}

	@Test
	public void testKitaMitFachstelleMehrAlsPensum() {
		Betreuung betreuung = EbeguRuleTestsHelper.createBetreuungWithPensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE,
			BetreuungsangebotTyp.KITA, 60, new BigDecimal(2000));
		final Gesuch gesuch = betreuung.extractGesuch();
		TestDataUtil.createDefaultAdressenForGS(gesuch, false);
		final PensumFachstelle pensumFachstelle = new PensumFachstelle();
		pensumFachstelle.setPensum(100);
		pensumFachstelle.setIntegrationTyp(IntegrationTyp.SOZIALE_INTEGRATION);
		pensumFachstelle.setGueltigkeit(new DateRange(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE));
		betreuung.getKind().getKindJA().getPensumFachstelle().add(pensumFachstelle);
		Assert.assertNotNull(betreuung.getKind().getGesuch().getGesuchsteller1());
		betreuung.getKind().getGesuch().getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 60));
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);

		Assert.assertNotNull(result);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(Integer.valueOf(60), result.get(0).getBgCalculationInputAsiv().getErwerbspensumGS1());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), result.get(0).getBetreuungspensumProzent());
		Assert.assertEquals(100, result.get(0).getAnspruchberechtigtesPensum());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), result.get(0).getBgPensum());
		Assert.assertEquals(-1, result.get(0).getBgCalculationInputAsiv().getAnspruchspensumRest());
		List<VerfuegungZeitabschnitt> nextZeitabschn = EbeguRuleTestsHelper.initializeRestanspruchForNextBetreuung(betreuung, result);
		Assert.assertEquals(40, nextZeitabschn.get(0).getBgCalculationInputAsiv().getAnspruchspensumRest());
	}

	@Test
	public void testKitaMitMehrerenFachstelleMehrAlsPensum() {
		Betreuung betreuung = EbeguRuleTestsHelper.createBetreuungWithPensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE,
			BetreuungsangebotTyp.KITA, 60, new BigDecimal(2000));
		final Gesuch gesuch = betreuung.extractGesuch();
		TestDataUtil.createDefaultAdressenForGS(gesuch, false);

		var sep30 = LocalDate.of(TestDataUtil.START_PERIODE.getYear(), 9, 30);
		var oct1 = LocalDate.of(TestDataUtil.START_PERIODE.getYear(), 10, 1);

		final PensumFachstelle pensumFachstelle = new PensumFachstelle();
		pensumFachstelle.setIntegrationTyp(IntegrationTyp.SOZIALE_INTEGRATION);
		pensumFachstelle.setPensum(100);
		pensumFachstelle.setGueltigkeit(new DateRange(TestDataUtil.START_PERIODE, sep30));

		final PensumFachstelle pensumFachstelle2 = new PensumFachstelle();
		pensumFachstelle2.setIntegrationTyp(IntegrationTyp.SOZIALE_INTEGRATION);
		pensumFachstelle2.setPensum(80);
		pensumFachstelle2.setGueltigkeit(new DateRange(oct1, TestDataUtil.ENDE_PERIODE));
		betreuung.getKind().getKindJA().getPensumFachstelle().add(pensumFachstelle);
		betreuung.getKind().getKindJA().getPensumFachstelle().add(pensumFachstelle2);

		Assert.assertNotNull(betreuung.getKind().getGesuch().getGesuchsteller1());
		betreuung.getKind().getGesuch().getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 60));
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);

		Assert.assertNotNull(result);
		Assert.assertEquals(2, result.size());

		Assert.assertEquals(Integer.valueOf(60), result.get(0).getBgCalculationInputAsiv().getErwerbspensumGS1());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), result.get(0).getBetreuungspensumProzent());
		Assert.assertEquals(100, result.get(0).getAnspruchberechtigtesPensum());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), result.get(0).getBgPensum());

		Assert.assertEquals(Integer.valueOf(60), result.get(1).getBgCalculationInputAsiv().getErwerbspensumGS1());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), result.get(1).getBetreuungspensumProzent());
		Assert.assertEquals(80, result.get(1).getAnspruchberechtigtesPensum());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), result.get(1).getBgPensum());

		Assert.assertEquals(-1, result.get(0).getBgCalculationInputAsiv().getAnspruchspensumRest());
		List<VerfuegungZeitabschnitt> nextZeitabschn = EbeguRuleTestsHelper.initializeRestanspruchForNextBetreuung(betreuung, result);
		Assert.assertEquals(40, nextZeitabschn.get(0).getBgCalculationInputAsiv().getAnspruchspensumRest());
		Assert.assertEquals(20, nextZeitabschn.get(1).getBgCalculationInputAsiv().getAnspruchspensumRest());
	}


	@Test
	public void testKitaMitMehrerenFachstelleUntermonatigMehrAlsPensum() {
		Betreuung betreuung = EbeguRuleTestsHelper.createBetreuungWithPensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE,
			BetreuungsangebotTyp.KITA, 60, new BigDecimal(2000));
		final Gesuch gesuch = betreuung.extractGesuch();
		TestDataUtil.createDefaultAdressenForGS(gesuch, false);

		var sep15 = LocalDate.of(TestDataUtil.START_PERIODE.getYear(), 9, 15);
		var nov1 = LocalDate.of(TestDataUtil.START_PERIODE.getYear(), 11, 1);

		final PensumFachstelle pensumFachstelle = new PensumFachstelle();
		pensumFachstelle.setIntegrationTyp(IntegrationTyp.SOZIALE_INTEGRATION);
		pensumFachstelle.setPensum(100);
		pensumFachstelle.setGueltigkeit(new DateRange(TestDataUtil.START_PERIODE, sep15));

		final PensumFachstelle pensumFachstelle2 = new PensumFachstelle();
		pensumFachstelle2.setIntegrationTyp(IntegrationTyp.SOZIALE_INTEGRATION);
		pensumFachstelle2.setPensum(80);
		pensumFachstelle2.setGueltigkeit(new DateRange(nov1, TestDataUtil.ENDE_PERIODE));
		betreuung.getKind().getKindJA().getPensumFachstelle().add(pensumFachstelle);
		betreuung.getKind().getKindJA().getPensumFachstelle().add(pensumFachstelle2);

		Assert.assertNotNull(betreuung.getKind().getGesuch().getGesuchsteller1());
		betreuung.getKind().getGesuch().getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 40));
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);

		Assert.assertNotNull(result);
		Assert.assertEquals(3, result.size());

		Assert.assertEquals(Integer.valueOf(40), result.get(0).getBgCalculationInputAsiv().getErwerbspensumGS1());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), result.get(0).getBetreuungspensumProzent());
		Assert.assertEquals(100, result.get(0).getAnspruchberechtigtesPensum());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), result.get(0).getBgPensum());

		Assert.assertEquals(Integer.valueOf(40), result.get(1).getBgCalculationInputAsiv().getErwerbspensumGS1());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), result.get(1).getBetreuungspensumProzent());
		Assert.assertEquals(60, result.get(1).getAnspruchberechtigtesPensum());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), result.get(1).getBgPensum());

		Assert.assertEquals(Integer.valueOf(40), result.get(2).getBgCalculationInputAsiv().getErwerbspensumGS1());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), result.get(2).getBetreuungspensumProzent());
		Assert.assertEquals(80, result.get(2).getAnspruchberechtigtesPensum());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), result.get(2).getBgPensum());

		Assert.assertEquals(-1, result.get(0).getBgCalculationInputAsiv().getAnspruchspensumRest());
		List<VerfuegungZeitabschnitt> nextZeitabschn = EbeguRuleTestsHelper.initializeRestanspruchForNextBetreuung(betreuung, result);
		Assert.assertEquals(40, nextZeitabschn.get(0).getBgCalculationInputAsiv().getAnspruchspensumRest());
		Assert.assertEquals(0, nextZeitabschn.get(1).getBgCalculationInputAsiv().getAnspruchspensumRest());
		Assert.assertEquals(20, nextZeitabschn.get(2).getBgCalculationInputAsiv().getAnspruchspensumRest());
	}

	@Test
	public void testKitaMitMehrerenFachstelleUntermonatigPensumSinktMehrAlsPensum() {
		Betreuung betreuung = EbeguRuleTestsHelper.createBetreuungWithPensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE,
			BetreuungsangebotTyp.KITA, 60, new BigDecimal(2000));
		final Gesuch gesuch = betreuung.extractGesuch();
		TestDataUtil.createDefaultAdressenForGS(gesuch, false);

		var sep15 = LocalDate.of(TestDataUtil.START_PERIODE.getYear(), 9, 15);
		var sep16 = LocalDate.of(TestDataUtil.START_PERIODE.getYear(), 9, 16);
		var sep30 = LocalDate.of(TestDataUtil.START_PERIODE.getYear(), 9, 30);

		final PensumFachstelle pensumFachstelle = new PensumFachstelle();
		pensumFachstelle.setIntegrationTyp(IntegrationTyp.SOZIALE_INTEGRATION);
		pensumFachstelle.setPensum(100);
		pensumFachstelle.setGueltigkeit(new DateRange(TestDataUtil.START_PERIODE, sep15));

		final PensumFachstelle pensumFachstelle2 = new PensumFachstelle();
		pensumFachstelle2.setIntegrationTyp(IntegrationTyp.SOZIALE_INTEGRATION);
		pensumFachstelle2.setPensum(80);
		pensumFachstelle2.setGueltigkeit(new DateRange(sep16, sep30));
		betreuung.getKind().getKindJA().getPensumFachstelle().add(pensumFachstelle);
		betreuung.getKind().getKindJA().getPensumFachstelle().add(pensumFachstelle2);

		Assert.assertNotNull(betreuung.getKind().getGesuch().getGesuchsteller1());
		betreuung.getKind().getGesuch().getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 40));
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);

		Assert.assertNotNull(result);
		Assert.assertEquals(2, result.size());

		Assert.assertEquals(Integer.valueOf(40), result.get(0).getBgCalculationInputAsiv().getErwerbspensumGS1());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), result.get(0).getBetreuungspensumProzent());
		Assert.assertEquals(100, result.get(0).getAnspruchberechtigtesPensum());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), result.get(0).getBgPensum());

		Assert.assertEquals(Integer.valueOf(40), result.get(1).getBgCalculationInputAsiv().getErwerbspensumGS1());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), result.get(1).getBetreuungspensumProzent());
		Assert.assertEquals(60, result.get(1).getAnspruchberechtigtesPensum());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), result.get(1).getBgPensum());

		Assert.assertEquals(-1, result.get(0).getBgCalculationInputAsiv().getAnspruchspensumRest());
		List<VerfuegungZeitabschnitt> nextZeitabschn = EbeguRuleTestsHelper.initializeRestanspruchForNextBetreuung(betreuung, result);
		Assert.assertEquals(40, nextZeitabschn.get(0).getBgCalculationInputAsiv().getAnspruchspensumRest());
		Assert.assertEquals(0, nextZeitabschn.get(1).getBgCalculationInputAsiv().getAnspruchspensumRest());
	}


	@Test
	public void testKitaMitMehrerenFachstelleUntermonatigPensumSteigtMehrAlsPensum() {
		Betreuung betreuung = EbeguRuleTestsHelper.createBetreuungWithPensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE,
			BetreuungsangebotTyp.KITA, 60, new BigDecimal(2000));
		final Gesuch gesuch = betreuung.extractGesuch();
		TestDataUtil.createDefaultAdressenForGS(gesuch, false);

		var sep15 = LocalDate.of(TestDataUtil.START_PERIODE.getYear(), 9, 15);
		var sep16 = LocalDate.of(TestDataUtil.START_PERIODE.getYear(), 9, 16);
		var sep30 = LocalDate.of(TestDataUtil.START_PERIODE.getYear(), 9, 30);

		final PensumFachstelle pensumFachstelle = new PensumFachstelle();
		pensumFachstelle.setIntegrationTyp(IntegrationTyp.SOZIALE_INTEGRATION);
		pensumFachstelle.setPensum(80);
		pensumFachstelle.setGueltigkeit(new DateRange(TestDataUtil.START_PERIODE, sep15));

		final PensumFachstelle pensumFachstelle2 = new PensumFachstelle();
		pensumFachstelle2.setIntegrationTyp(IntegrationTyp.SOZIALE_INTEGRATION);
		pensumFachstelle2.setPensum(100);
		pensumFachstelle2.setGueltigkeit(new DateRange(sep16, sep30));
		betreuung.getKind().getKindJA().getPensumFachstelle().add(pensumFachstelle);
		betreuung.getKind().getKindJA().getPensumFachstelle().add(pensumFachstelle2);

		Assert.assertNotNull(betreuung.getKind().getGesuch().getGesuchsteller1());
		betreuung.getKind().getGesuch().getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 40));
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);

		Assert.assertNotNull(result);
		Assert.assertEquals(4, result.size());

		Assert.assertEquals(Integer.valueOf(40), result.get(0).getBgCalculationInputAsiv().getErwerbspensumGS1());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), result.get(0).getBetreuungspensumProzent());
		Assert.assertEquals(80, result.get(0).getAnspruchberechtigtesPensum());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), result.get(0).getBgPensum());

		Assert.assertEquals(Integer.valueOf(40), result.get(1).getBgCalculationInputAsiv().getErwerbspensumGS1());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), result.get(1).getBetreuungspensumProzent());
		Assert.assertEquals(80, result.get(1).getAnspruchberechtigtesPensum());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), result.get(1).getBgPensum());

		Assert.assertEquals(Integer.valueOf(40), result.get(2).getBgCalculationInputAsiv().getErwerbspensumGS1());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), result.get(2).getBetreuungspensumProzent());
		Assert.assertEquals(100, result.get(2).getAnspruchberechtigtesPensum());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), result.get(2).getBgPensum());

		Assert.assertEquals(Integer.valueOf(40), result.get(3).getBgCalculationInputAsiv().getErwerbspensumGS1());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), result.get(3).getBetreuungspensumProzent());
		Assert.assertEquals(60, result.get(3).getAnspruchberechtigtesPensum());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), result.get(3).getBgPensum());

		Assert.assertEquals(-1, result.get(0).getBgCalculationInputAsiv().getAnspruchspensumRest());
		List<VerfuegungZeitabschnitt> nextZeitabschn = EbeguRuleTestsHelper.initializeRestanspruchForNextBetreuung(betreuung, result);
		Assert.assertEquals(20, nextZeitabschn.get(0).getBgCalculationInputAsiv().getAnspruchspensumRest());
		Assert.assertEquals(20, nextZeitabschn.get(1).getBgCalculationInputAsiv().getAnspruchspensumRest());
		Assert.assertEquals(40, nextZeitabschn.get(2).getBgCalculationInputAsiv().getAnspruchspensumRest());
		Assert.assertEquals(0, nextZeitabschn.get(3).getBgCalculationInputAsiv().getAnspruchspensumRest());
	}


	@Test
	public void testKitaMitFachstelleUndRestPensum() {
		Betreuung betreuung = EbeguRuleTestsHelper.createBetreuungWithPensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE,
			BetreuungsangebotTyp.KITA, 60, new BigDecimal(2000));
		final Gesuch gesuch = betreuung.extractGesuch();
		TestDataUtil.createDefaultAdressenForGS(gesuch, false);

		betreuung.getKind().getKindJA().setPensumFachstelle(new HashSet<>());

		final PensumFachstelle pensumFachstelle = new PensumFachstelle();
		final Fachstelle fachstelle = new Fachstelle();
		fachstelle.setMandant(TestDataUtil.createDefaultMandant());
		pensumFachstelle.setFachstelle(fachstelle);
		pensumFachstelle.setPensum(80);
		pensumFachstelle.setIntegrationTyp(IntegrationTyp.SOZIALE_INTEGRATION);
		pensumFachstelle.setGueltigkeit(new DateRange(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE));
		betreuung.getKind().getKindJA().getPensumFachstelle().add(pensumFachstelle);

		assertThat(betreuung.getKind().getKindJA().getPensumFachstelle().size(), is(1));
		Assert.assertNotNull(betreuung.getKind().getGesuch().getGesuchsteller1());
		betreuung.getKind().getGesuch().getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 40));
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);

		Assert.assertNotNull(result);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(Integer.valueOf(40), result.get(0).getBgCalculationInputAsiv().getErwerbspensumGS1());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), result.get(0).getBetreuungspensumProzent());
		Assert.assertEquals(80, result.get(0).getAnspruchberechtigtesPensum());
		Assert.assertEquals(-1, result.get(0).getBgCalculationInputAsiv().getAnspruchspensumRest());
		List<VerfuegungZeitabschnitt> nextZeitabschn = EbeguRuleTestsHelper.initializeRestanspruchForNextBetreuung(betreuung, result);
		Assert.assertEquals(20, nextZeitabschn.get(0).getBgCalculationInputAsiv().getAnspruchspensumRest());
	}
}
