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

package ch.dvbern.ebegu.rechner;

import java.math.BigDecimal;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.rules.RuleValidity;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BGCalculationInputMergerTest {

	private BGCalculationInput bgCalculationInput1;
	private BGCalculationInput bgCalculationInput2;

	@Test
	public void test_calcualtePercentage_null() {
		bgCalculationInput1.calculateInputValuesProportionaly(50);
		Assert.assertNull(bgCalculationInput1.getErwerbspensumGS1());
	}

	@Test
	public void test_calcualtePercentage_zero() {
		Integer ewpGS1 = 0;
		bgCalculationInput1.setErwerbspensumGS1(ewpGS1);
		bgCalculationInput1.calculateInputValuesProportionaly(50);
		Assert.assertEquals(ewpGS1, bgCalculationInput1.getErwerbspensumGS1());
	}

	@Test
	public void test_calcualtePercentage_ewp1() {
		bgCalculationInput1.setErwerbspensumGS1(29);
		bgCalculationInput1.calculateInputValuesProportionaly(13.3);
		Assert.assertEquals((Integer) 4, bgCalculationInput1.getErwerbspensumGS1());
	}

	@Test
	public void test_calculationInputMerge_ewp1() {
		bgCalculationInput1.setErwerbspensumGS1(63);
		bgCalculationInput2.setErwerbspensumGS1(41);

		bgCalculationInput1.calculateInputValuesProportionaly(15.43);
		bgCalculationInput2.calculateInputValuesProportionaly(94.4);

		Assert.assertEquals((Integer) 10, bgCalculationInput1.getErwerbspensumGS1());
		Assert.assertEquals((Integer) 39, bgCalculationInput2.getErwerbspensumGS1());

		//Merge
		bgCalculationInput1.add(bgCalculationInput2);

		Assert.assertEquals((Integer) 49, bgCalculationInput1.getErwerbspensumGS1());
	}

	@Test
	public void test_calculationInputMerge_ewp2() {
		bgCalculationInput1.setErwerbspensumGS2(75);
		bgCalculationInput2.setErwerbspensumGS2(60);

		bgCalculationInput1.calculateInputValuesProportionaly(23.75);
		bgCalculationInput2.calculateInputValuesProportionaly(62.48);

		Assert.assertEquals((Integer) 18, bgCalculationInput1.getErwerbspensumGS2());
		Assert.assertEquals((Integer) 37, bgCalculationInput2.getErwerbspensumGS2());

		//Merge
		bgCalculationInput1.add(bgCalculationInput2);
		Assert.assertEquals((Integer) 55, bgCalculationInput1.getErwerbspensumGS2());
	}

	@Test
	public void test_calcualteInputMerge_betreuungspensum() {
		bgCalculationInput1.setBetreuungspensumProzent(BigDecimal.valueOf(56));
		bgCalculationInput2.setBetreuungspensumProzent(BigDecimal.valueOf(77));

		bgCalculationInput1.calculateInputValuesProportionaly(8.235);
		bgCalculationInput2.calculateInputValuesProportionaly(42.12);

		Assert.assertEquals(BigDecimal.valueOf(5), bgCalculationInput1.getBetreuungspensumProzent());
		Assert.assertEquals(BigDecimal.valueOf(32), bgCalculationInput2.getBetreuungspensumProzent());

		//Merge
		bgCalculationInput1.add(bgCalculationInput2);
		Assert.assertEquals(BigDecimal.valueOf(37), bgCalculationInput1.getBetreuungspensumProzent());
	}

	@Test
	public void test_calcualteInputMerge_anspruchspensum() {
		bgCalculationInput1.setAnspruchspensumProzent(16);
		bgCalculationInput2.setAnspruchspensumProzent(58);

		bgCalculationInput1.calculateInputValuesProportionaly(43.58);
		bgCalculationInput2.calculateInputValuesProportionaly(32.19);

		Assert.assertEquals(7, bgCalculationInput1.getAnspruchspensumProzent());
		Assert.assertEquals(19, bgCalculationInput2.getAnspruchspensumProzent());

		//Merge
		bgCalculationInput1.add(bgCalculationInput2);
		Assert.assertEquals(26, bgCalculationInput1.getAnspruchspensumProzent());
	}

	@Test
	public void test_calcualteInputMerge_anspruchspensumRest() {
		bgCalculationInput1.setAnspruchspensumRest(29);
		bgCalculationInput2.setAnspruchspensumRest(74);

		bgCalculationInput1.calculateInputValuesProportionaly(55.1);
		bgCalculationInput2.calculateInputValuesProportionaly(59.62);

		Assert.assertEquals(16, bgCalculationInput1.getAnspruchspensumRest());
		Assert.assertEquals(44, bgCalculationInput2.getAnspruchspensumRest());

		//Merge
		bgCalculationInput1.add(bgCalculationInput2);
		Assert.assertEquals(60, bgCalculationInput1.getAnspruchspensumRest());
	}

	@Test
	public void test_calculateInputMerge_fachstellenpensum() {
		bgCalculationInput1.setFachstellenpensum(43);
		bgCalculationInput2.setFachstellenpensum(86);

		bgCalculationInput1.calculateInputValuesProportionaly(31.54);
		bgCalculationInput2.calculateInputValuesProportionaly(46.58);

		Assert.assertEquals(14, bgCalculationInput1.getFachstellenpensum());
		Assert.assertEquals(40, bgCalculationInput2.getFachstellenpensum());

		//Merge
		bgCalculationInput1.add(bgCalculationInput2);
		Assert.assertEquals(54, bgCalculationInput1.getFachstellenpensum());
	}

	@Test
	public void test_calculateInputMerge_aussertOrdentlicherAnspruch() {
		bgCalculationInput1.setAusserordentlicherAnspruch(51);
		bgCalculationInput2.setAusserordentlicherAnspruch(36);

		bgCalculationInput1.calculateInputValuesProportionaly(84.1);
		bgCalculationInput2.calculateInputValuesProportionaly(22.85);

		Assert.assertEquals(43, bgCalculationInput1.getAusserordentlicherAnspruch());
		Assert.assertEquals(8, bgCalculationInput2.getAusserordentlicherAnspruch());

		//Merge
		bgCalculationInput1.add(bgCalculationInput2);
		Assert.assertEquals(51, bgCalculationInput1.getAusserordentlicherAnspruch());
	}

	@Test
	public void test_calculateInputMerge_monatlicheBetruungskosten() {
		bgCalculationInput1.setMonatlicheBetreuungskosten(BigDecimal.valueOf(62));
		bgCalculationInput2.setMonatlicheBetreuungskosten(BigDecimal.valueOf(33));

		bgCalculationInput1.calculateInputValuesProportionaly(7.135);
		bgCalculationInput2.calculateInputValuesProportionaly(90.2);

		Assert.assertEquals(BigDecimal.valueOf(4), bgCalculationInput1.getMonatlicheBetreuungskosten());
		Assert.assertEquals(BigDecimal.valueOf(30), bgCalculationInput2.getMonatlicheBetreuungskosten());

		//Merge
		bgCalculationInput1.add(bgCalculationInput2);
		Assert.assertEquals(BigDecimal.valueOf(34), bgCalculationInput1.getMonatlicheBetreuungskosten());
	}

	@Test
	public void test_calculateInputMerge_verguenstigungMahlzeitenTotal() {
		bgCalculationInput1.setVerguenstigungMahlzeitenTotal(BigDecimal.valueOf(16));
		bgCalculationInput2.setVerguenstigungMahlzeitenTotal(BigDecimal.valueOf(24));

		bgCalculationInput1.calculateInputValuesProportionaly(48.52);
		bgCalculationInput2.calculateInputValuesProportionaly(62.75);

		Assert.assertEquals(BigDecimal.valueOf(8), bgCalculationInput1.getVerguenstigungMahlzeitenTotal());
		Assert.assertEquals(BigDecimal.valueOf(15), bgCalculationInput2.getVerguenstigungMahlzeitenTotal());

		//Merge
		bgCalculationInput1.add(bgCalculationInput2);
		Assert.assertEquals(BigDecimal.valueOf(23), bgCalculationInput1.getVerguenstigungMahlzeitenTotal());
	}

	@Test
	public void test_calcualteInputMerge_tarifHauptmahlzeiten() {
		bgCalculationInput1.setTarifHauptmahlzeit(BigDecimal.valueOf(11));
		bgCalculationInput2.setTarifHauptmahlzeit(BigDecimal.valueOf(9));

		bgCalculationInput1.calculateInputValuesProportionaly(54.87);
		bgCalculationInput2.calculateInputValuesProportionaly(32.12);

		Assert.assertEquals(BigDecimal.valueOf(6), bgCalculationInput1.getTarifHauptmahlzeit());
		Assert.assertEquals(BigDecimal.valueOf(3), bgCalculationInput2.getTarifHauptmahlzeit());

		//Merge
		bgCalculationInput1.add(bgCalculationInput2);
		Assert.assertEquals(BigDecimal.valueOf(9), bgCalculationInput1.getTarifHauptmahlzeit());
	}

	@Test
	public void test_calcualteInputMerge_tarifNebenmahlzeiten() {
		bgCalculationInput1.setTarifNebenmahlzeit(BigDecimal.valueOf(4));
		bgCalculationInput2.setTarifNebenmahlzeit(BigDecimal.valueOf(6));

		bgCalculationInput1.calculateInputValuesProportionaly(72.45);
		bgCalculationInput2.calculateInputValuesProportionaly(33.66);

		Assert.assertEquals(BigDecimal.valueOf(3), bgCalculationInput1.getTarifNebenmahlzeit());
		Assert.assertEquals(BigDecimal.valueOf(2), bgCalculationInput2.getTarifNebenmahlzeit());

		//Merge
		bgCalculationInput1.add(bgCalculationInput2);
		Assert.assertEquals(BigDecimal.valueOf(5), bgCalculationInput1.getTarifNebenmahlzeit());
	}

	@Test
	public void test_calcualteInputMerge_massgebendesEinkommenVorAbzugFamGr() {
		bgCalculationInput1.setMassgebendesEinkommenVorAbzugFamgr(BigDecimal.valueOf(14253));
		bgCalculationInput2.setMassgebendesEinkommenVorAbzugFamgr(BigDecimal.valueOf(9231));

		bgCalculationInput1.calculateInputValuesProportionaly(41.53);
		bgCalculationInput2.calculateInputValuesProportionaly(36.42);

		Assert.assertEquals(BigDecimal.valueOf(5919), bgCalculationInput1.getMassgebendesEinkommenVorAbzugFamgr());
		Assert.assertEquals(BigDecimal.valueOf(3362), bgCalculationInput2.getMassgebendesEinkommenVorAbzugFamgr());

		//Merge
		bgCalculationInput1.add(bgCalculationInput2);
		Assert.assertEquals(BigDecimal.valueOf(9281), bgCalculationInput1.getMassgebendesEinkommenVorAbzugFamgr());
	}


	@Test
	public void test_calcualteInputMerge_hauptmahlzeiten() {
		bgCalculationInput1.setAnzahlHauptmahlzeiten(BigDecimal.valueOf(20));
		bgCalculationInput2.setAnzahlHauptmahlzeiten(BigDecimal.valueOf(43));

		bgCalculationInput1.calculateInputValuesProportionaly(13);
		bgCalculationInput2.calculateInputValuesProportionaly(62.1);

		Assert.assertEquals(BigDecimal.valueOf(3), bgCalculationInput1.getAnzahlHauptmahlzeiten());
		Assert.assertEquals(BigDecimal.valueOf(27), bgCalculationInput2.getAnzahlHauptmahlzeiten());

		//Merge
		bgCalculationInput1.add(bgCalculationInput2);
		Assert.assertEquals(BigDecimal.valueOf(30), bgCalculationInput1.getAnzahlHauptmahlzeiten());
	}

	@Test
	public void test_calculateInputMerge_nebenmahlzeiten() {
		bgCalculationInput1.setAnzahlNebenmahlzeiten(BigDecimal.valueOf(93));
		bgCalculationInput2.setAnzahlNebenmahlzeiten(BigDecimal.valueOf(45));

		bgCalculationInput1.calculateInputValuesProportionaly(52.78);
		bgCalculationInput2.calculateInputValuesProportionaly(46);

		Assert.assertEquals(BigDecimal.valueOf(49), bgCalculationInput1.getAnzahlNebenmahlzeiten());
		Assert.assertEquals(BigDecimal.valueOf(21), bgCalculationInput2.getAnzahlNebenmahlzeiten());

		//Merge
		bgCalculationInput1.add(bgCalculationInput2);
		Assert.assertEquals(BigDecimal.valueOf(70), bgCalculationInput1.getAnzahlNebenmahlzeiten());
	}

	@Test
	public void test_calcualteInputMerge_tsInput() {
		bgCalculationInput1.setTsVerpflegungskostenMitBetreuung(BigDecimal.valueOf(23));
		bgCalculationInput1.setTsVerpflegungskostenVerguenstigtMitBetreuung(BigDecimal.valueOf(69));
		bgCalculationInput1.setTsBetreuungszeitProWocheMitBetreuung(19);
		bgCalculationInput1.setTsBetreuungszeitProWocheOhneBetreuung(33);
		bgCalculationInput1.setTsVerpflegungskostenOhneBetreuung(BigDecimal.valueOf(47));
		bgCalculationInput1.setTsVerpflegungskostenVerguenstigtOhneBetreuung(BigDecimal.valueOf(76));

		bgCalculationInput2.setTsVerpflegungskostenMitBetreuung(BigDecimal.valueOf(75));
		bgCalculationInput2.setTsVerpflegungskostenVerguenstigtMitBetreuung(BigDecimal.valueOf(0));
		bgCalculationInput2.setTsBetreuungszeitProWocheMitBetreuung(15);
		bgCalculationInput2.setTsBetreuungszeitProWocheOhneBetreuung(13);
		bgCalculationInput2.setTsVerpflegungskostenOhneBetreuung(BigDecimal.valueOf(43));
		bgCalculationInput2.setTsVerpflegungskostenVerguenstigtOhneBetreuung(BigDecimal.valueOf(95));


		bgCalculationInput1.calculateInputValuesProportionaly(71.7);
		bgCalculationInput2.calculateInputValuesProportionaly(11.4);

		Assert.assertEquals(BigDecimal.valueOf(16), bgCalculationInput1.getTsInputMitBetreuung().getVerpflegungskosten());
		Assert.assertEquals(BigDecimal.valueOf(49), bgCalculationInput1.getTsInputMitBetreuung().getVerpflegungskostenVerguenstigt());
		Assert.assertEquals((Integer) 14, bgCalculationInput1.getTsInputMitBetreuung().getBetreuungszeitProWoche());
		Assert.assertEquals(BigDecimal.valueOf(34), bgCalculationInput1.getTsInputOhneBetreuung().getVerpflegungskosten());
		Assert.assertEquals(BigDecimal.valueOf(54), bgCalculationInput1.getTsInputOhneBetreuung().getVerpflegungskostenVerguenstigt());
		Assert.assertEquals((Integer) 24, bgCalculationInput1.getTsInputOhneBetreuung().getBetreuungszeitProWoche());

		Assert.assertEquals(BigDecimal.valueOf(9), bgCalculationInput2.getTsInputMitBetreuung().getVerpflegungskosten());
		Assert.assertEquals(BigDecimal.valueOf(0), bgCalculationInput2.getTsInputMitBetreuung().getVerpflegungskostenVerguenstigt());
		Assert.assertEquals((Integer) 2, bgCalculationInput2.getTsInputMitBetreuung().getBetreuungszeitProWoche());
		Assert.assertEquals(BigDecimal.valueOf(5), bgCalculationInput2.getTsInputOhneBetreuung().getVerpflegungskosten());
		Assert.assertEquals(BigDecimal.valueOf(11), bgCalculationInput2.getTsInputOhneBetreuung().getVerpflegungskostenVerguenstigt());
		Assert.assertEquals((Integer) 1, bgCalculationInput2.getTsInputOhneBetreuung().getBetreuungszeitProWoche());

		bgCalculationInput1.add(bgCalculationInput2);
		Assert.assertEquals(25, bgCalculationInput1.getTsInputMitBetreuung().getVerpflegungskosten().intValue());
		Assert.assertEquals(49, bgCalculationInput1.getTsInputMitBetreuung().getVerpflegungskostenVerguenstigt().intValue());
		Assert.assertEquals((Integer) 16, bgCalculationInput1.getTsInputMitBetreuung().getBetreuungszeitProWoche());
		Assert.assertEquals(39, bgCalculationInput1.getTsInputOhneBetreuung().getVerpflegungskosten().intValue());
		Assert.assertEquals(65, bgCalculationInput1.getTsInputOhneBetreuung().getVerpflegungskostenVerguenstigt().intValue());
		Assert.assertEquals((Integer) 25, bgCalculationInput1.getTsInputOhneBetreuung().getBetreuungszeitProWoche());

	}

	@Before
	public void init() {
		bgCalculationInput1 = new BGCalculationInput(new VerfuegungZeitabschnitt(), RuleValidity.ASIV);
		bgCalculationInput2 = new BGCalculationInput(new VerfuegungZeitabschnitt(), RuleValidity.ASIV);
	}
}
