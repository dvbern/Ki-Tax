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

import ch.dvbern.ebegu.entities.*;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.IntegrationTyp;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.MathUtil;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.TreeSet;

import static ch.dvbern.ebegu.util.Constants.ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

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
		setPensumFachstelle(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 40, IntegrationTyp.SPRACHLICHE_INTEGRATION, betreuung);
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
		setPensumFachstelle(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 100, IntegrationTyp.SOZIALE_INTEGRATION, betreuung);

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

		setPensumFachstelle(TestDataUtil.START_PERIODE, sep30, 100, IntegrationTyp.SOZIALE_INTEGRATION, betreuung);
		setPensumFachstelle(oct1, TestDataUtil.ENDE_PERIODE, 80, IntegrationTyp.SOZIALE_INTEGRATION, betreuung);

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

		setPensumFachstelle(TestDataUtil.START_PERIODE, sep15, 100, IntegrationTyp.SOZIALE_INTEGRATION, betreuung);
		setPensumFachstelle(nov1, TestDataUtil.ENDE_PERIODE, 80, IntegrationTyp.SOZIALE_INTEGRATION, betreuung);

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

		setPensumFachstelle(TestDataUtil.START_PERIODE, sep15, 100, IntegrationTyp.SOZIALE_INTEGRATION, betreuung);
		setPensumFachstelle(sep16, sep30, 80, IntegrationTyp.SOZIALE_INTEGRATION, betreuung);

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

		setPensumFachstelle(TestDataUtil.START_PERIODE, sep15, 80, IntegrationTyp.SOZIALE_INTEGRATION, betreuung);
		setPensumFachstelle(sep16, sep30, 100, IntegrationTyp.SOZIALE_INTEGRATION, betreuung);

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
		betreuung.getKind().getKindJA().setPensumFachstelle(new TreeSet<>());

		setPensumFachstelle(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 80, IntegrationTyp.SOZIALE_INTEGRATION, betreuung);

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

	@Test
	public void testKitaMitSpracheIntegrationUndSparchfoerderung() {
		Betreuung betreuung = EbeguRuleTestsHelper.createBetreuungWithPensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE,
			BetreuungsangebotTyp.KITA, 60, new BigDecimal(2000));
		final Gesuch gesuch = betreuung.extractGesuch();
		TestDataUtil.createDefaultAdressenForGS(gesuch, false);
		betreuung.getErweiterteBetreuungContainer().getErweiterteBetreuungJA().setSprachfoerderungBestaetigt(false);
		betreuung.getKind().getKindJA().setPensumFachstelle(new TreeSet<>());
		setPensumFachstelle(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 40, IntegrationTyp.SPRACHLICHE_INTEGRATION, betreuung);

		betreuung.getKind().getGesuch().getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 40));
		var einstellungen = EbeguRuleTestsHelper.getEinstellungenConfiguratorAsiv(gesuch.getGesuchsperiode());
		einstellungen.get(EinstellungKey.SPRACHFOERDERUNG_BESTAETIGEN).setValue("true");
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculateWithCustomEinstellungen(betreuung, einstellungen);
		Assert.assertEquals(1, result.size());
		assertThat(result.get(0).getVerfuegungZeitabschnittBemerkungList().stream().anyMatch(bemerkung -> bemerkung.getBemerkung().contains("der Anspruch aufgrund sprachlicher Indikation nicht gegeben")), is(true));

		betreuung.getErweiterteBetreuungContainer().getErweiterteBetreuungJA().setSprachfoerderungBestaetigt(true);
		result = EbeguRuleTestsHelper.calculateWithCustomEinstellungen(betreuung, einstellungen);
		Assert.assertEquals(1, result.size());
		assertThat(result.get(0).getVerfuegungZeitabschnittBemerkungList().stream().anyMatch(bemerkung -> bemerkung.getBemerkung().contains("der Anspruch aufgrund sprachlicher Indikation nicht gegeben")), is(false));
	}

	private void setPensumFachstelle(LocalDate start, LocalDate ende,int pensum, IntegrationTyp integrationTyp,  Betreuung betreuung) {
		final PensumFachstelle pensumFachstelle = new PensumFachstelle();
		final Fachstelle fachstelle = new Fachstelle();
		fachstelle.setMandant(TestDataUtil.createDefaultMandant());
		pensumFachstelle.setFachstelle(fachstelle);
		pensumFachstelle.setPensum(pensum);
		pensumFachstelle.setIntegrationTyp(integrationTyp);
		pensumFachstelle.setGueltigkeit(new DateRange(start, ende));
		betreuung.getKind().getKindJA().getPensumFachstelle().add(pensumFachstelle);
	}
}
