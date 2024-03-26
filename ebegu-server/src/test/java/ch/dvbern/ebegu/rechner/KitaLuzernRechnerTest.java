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

import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.PensumUnits;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.ebegu.util.TestUtils;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;

public class KitaLuzernRechnerTest extends AbstractLuzernRechnerTest {

	private final BGRechnerParameterDTO defaultParameterDTO = TestUtils.getRechnerParameterLuzern();


	@Test
	public void testSaeugling1() { //Test Saeugling1 gemäss Excel
		TestValues testValues = new TestValues();
		testValues.monatlicheBetreuungsKosten = MathUtil.DEFAULT.fromNullSafe(2080);
		testValues.betreuungsPensum = MathUtil.DEFAULT.fromNullSafe(80);
		testValues.anspruchsPensum = 80;
		testValues.einkommen = MathUtil.DEFAULT.fromNullSafe(50000);
		testValues.isBaby = true;

		testValues.expectedVollkosten = MathUtil.DEFAULT.fromNullSafe(2080);
		testValues.expectedVerguenstigungOhneBeruecksichtigungMinimalbetrag = BigDecimal.valueOf(2080);
		testValues.expectedVerguenstigungOhneBeruecksichtigungVollkosten = BigDecimal.valueOf(2316.25);
		testValues.expectedVerguenstigung = BigDecimal.valueOf(1834);
		testValues.expectedElternbeitrag = BigDecimal.valueOf(482.25);
		testValues.expectedMinimalerElternbeitrag = BigDecimal.valueOf(246);
		testValues.expectedBetreuungsZeiteinheit = BigDecimal.valueOf(16.4);
		testValues.expectedAnspruchsZeiteinheit =  BigDecimal.valueOf(16.4);
		testValues.expectedBgZeiteinheit =  BigDecimal.valueOf(16.4);

		VerfuegungZeitabschnitt zeitabschnitt = prepareVerfuegung(testValues);
		KitaLuzernRechner rechner = new KitaLuzernRechner(Collections.emptyList());
		rechner.calculate(zeitabschnitt, defaultParameterDTO);

		BGCalculationResult result = zeitabschnitt.getBgCalculationResultAsiv();
		assertCalculationResultResult(result, testValues);
	}

	@Test
	public void testSaeugling2() { //Test Saeugling2 gemäss Excel
		TestValues testValues = new TestValues();
		testValues.monatlicheBetreuungsKosten = MathUtil.DEFAULT.fromNullSafe(1640);
		testValues.betreuungsPensum = MathUtil.DEFAULT.fromNullSafe(60);
		testValues.anspruchsPensum = 50;
		testValues.einkommen = MathUtil.DEFAULT.fromNullSafe(80000);
		testValues.isBaby = true;

		testValues.expectedVollkosten = MathUtil.DEFAULT.fromNullSafe(1366.65);
		testValues.expectedVerguenstigungOhneBeruecksichtigungMinimalbetrag = BigDecimal.valueOf(1196.6);
		testValues.expectedVerguenstigungOhneBeruecksichtigungVollkosten = BigDecimal.valueOf(1196.6);
		testValues.expectedVerguenstigung = BigDecimal.valueOf(1196.6);
		testValues.expectedMinimalerElternbeitrag = BigDecimal.valueOf(153.75);
		testValues.expectedBetreuungsZeiteinheit = BigDecimal.valueOf(12.3);
		testValues.expectedAnspruchsZeiteinheit =  BigDecimal.valueOf(10.25);
		testValues.expectedBgZeiteinheit =  BigDecimal.valueOf(10.25);

		VerfuegungZeitabschnitt zeitabschnitt = prepareVerfuegung(testValues);
		zeitabschnitt.getBgCalculationInputAsiv().setKitaPlusZuschlag(true);
		KitaLuzernRechner rechner = new KitaLuzernRechner(Collections.emptyList());
		rechner.calculate(zeitabschnitt, defaultParameterDTO);

		assertCalculationResultResult(zeitabschnitt.getRelevantBgCalculationResult(), testValues);
	}

	@Test
	public void testKind1() { //Test Kind1 gemäss Excel
		TestValues testValues = new TestValues();
		testValues.monatlicheBetreuungsKosten = MathUtil.DEFAULT.fromNullSafe(1600);
		testValues.betreuungsPensum = MathUtil.DEFAULT.fromNullSafe(60);
		testValues.anspruchsPensum = 60;
		testValues.einkommen = MathUtil.DEFAULT.fromNullSafe(120000);
		testValues.isBaby = false;

		testValues.expectedVollkosten = MathUtil.DEFAULT.fromNullSafe(1600);
		testValues.expectedVerguenstigungOhneBeruecksichtigungMinimalbetrag = BigDecimal.valueOf(123);
		testValues.expectedVerguenstigungOhneBeruecksichtigungVollkosten = BigDecimal.valueOf(123);
		testValues.expectedVerguenstigung = BigDecimal.valueOf(123);
		testValues.expectedMinimalerElternbeitrag = BigDecimal.valueOf(184.50);
		testValues.expectedBetreuungsZeiteinheit = BigDecimal.valueOf(12.3);
		testValues.expectedAnspruchsZeiteinheit =  BigDecimal.valueOf(12.3);
		testValues.expectedBgZeiteinheit =  BigDecimal.valueOf(12.3);

		VerfuegungZeitabschnitt zeitabschnitt = prepareVerfuegung(testValues);
		KitaLuzernRechner rechner = new KitaLuzernRechner(Collections.emptyList());
		rechner.calculate(zeitabschnitt, defaultParameterDTO);

		assertCalculationResultResult(zeitabschnitt.getRelevantBgCalculationResult(), testValues);
	}

	@Test
	public void testKind2() { //Test Kind2 gemäss Excel
		TestValues testValues = new TestValues();
		testValues.monatlicheBetreuungsKosten = MathUtil.DEFAULT.fromNullSafe(1600);
		testValues.betreuungsPensum = MathUtil.DEFAULT.fromNullSafe(60);
		testValues.anspruchsPensum = 60;
		testValues.einkommen = MathUtil.DEFAULT.fromNullSafe(125001);
		testValues.isBaby = false;

		testValues.expectedVollkosten = MathUtil.DEFAULT.fromNullSafe(1600);
		testValues.expectedMinimalerElternbeitrag = BigDecimal.valueOf(184.50);
		testValues.expectedBetreuungsZeiteinheit = BigDecimal.valueOf(12.3);
		testValues.expectedAnspruchsZeiteinheit =  BigDecimal.valueOf(12.3);
		testValues.expectedBgZeiteinheit =  BigDecimal.valueOf(12.3);

		VerfuegungZeitabschnitt zeitabschnitt = prepareVerfuegung(testValues);
		KitaLuzernRechner rechner = new KitaLuzernRechner(Collections.emptyList());
		rechner.calculate(zeitabschnitt, defaultParameterDTO);

		assertCalculationResultResult(zeitabschnitt.getRelevantBgCalculationResult(), testValues);
	}

	@Test
	public void testKind3() { //Test Kind3 gemäss Excel
		TestValues testValues = new TestValues();
		testValues.monatlicheBetreuungsKosten = MathUtil.DEFAULT.fromNullSafe(1600);
		testValues.betreuungsPensum = MathUtil.DEFAULT.fromNullSafe(70);
		testValues.anspruchsPensum = 60;
		testValues.einkommen = MathUtil.DEFAULT.fromNullSafe(48000);
		testValues.isBaby = false;

		testValues.expectedVollkosten = MathUtil.DEFAULT.fromNullSafe(1371.45);
		testValues.expectedVerguenstigungOhneBeruecksichtigungMinimalbetrag = BigDecimal.valueOf(1371.45);
		testValues.expectedVerguenstigungOhneBeruecksichtigungVollkosten = BigDecimal.valueOf(1414.5);
		testValues.expectedVerguenstigung = BigDecimal.valueOf(1186.95);
		testValues.expectedMinimalerElternbeitrag = BigDecimal.valueOf(184.50);
		testValues.expectedElternbeitrag = BigDecimal.valueOf(227.55);
		testValues.expectedBetreuungsZeiteinheit = BigDecimal.valueOf(14.35);
		testValues.expectedAnspruchsZeiteinheit =  BigDecimal.valueOf(12.3);
		testValues.expectedBgZeiteinheit =  BigDecimal.valueOf(12.3);

		VerfuegungZeitabschnitt zeitabschnitt = prepareVerfuegung(testValues);
		KitaLuzernRechner rechner = new KitaLuzernRechner(Collections.emptyList());
		rechner.calculate(zeitabschnitt, defaultParameterDTO);

		assertCalculationResultResult(zeitabschnitt.getRelevantBgCalculationResult(), testValues);
	}

	@Test
	public void testKind4KitaPlus() { //Test Kind4 gemäss Excel
		TestValues testValues = new TestValues();
		testValues.monatlicheBetreuungsKosten = MathUtil.DEFAULT.fromNullSafe(1600);
		testValues.betreuungsPensum = MathUtil.DEFAULT.fromNullSafe(60);
		testValues.anspruchsPensum = 40;
		testValues.einkommen = MathUtil.DEFAULT.fromNullSafe(48000);
		testValues.isBaby = false;

		testValues.expectedVollkosten = MathUtil.DEFAULT.fromNullSafe(1066.65);
		testValues.expectedVerguenstigungOhneBeruecksichtigungMinimalbetrag = BigDecimal.valueOf(1205.40);
		testValues.expectedVerguenstigungOhneBeruecksichtigungVollkosten = BigDecimal.valueOf(1205.40);
		testValues.expectedVerguenstigung = BigDecimal.valueOf(1205.40);
		testValues.expectedMinimalerElternbeitrag = BigDecimal.valueOf(123);
		testValues.expectedBetreuungsZeiteinheit = BigDecimal.valueOf(12.3);
		testValues.expectedAnspruchsZeiteinheit =  BigDecimal.valueOf(8.2);
		testValues.expectedBgZeiteinheit =  BigDecimal.valueOf(8.2);

		VerfuegungZeitabschnitt zeitabschnitt = prepareVerfuegung(testValues);
		zeitabschnitt.getBgCalculationInputAsiv().setKitaPlusZuschlag(true);
		KitaLuzernRechner rechner = new KitaLuzernRechner(Collections.emptyList());
		rechner.calculate(zeitabschnitt, defaultParameterDTO);

		assertCalculationResultResult(zeitabschnitt.getRelevantBgCalculationResult(), testValues);
	}

	@Test
	public void testSaeuglingEWPgreaterThenMaxBaby() {  //Test Baby 3 gemäss Excel
		TestValues testValues = new TestValues();
		testValues.monatlicheBetreuungsKosten = MathUtil.DEFAULT.fromNullSafe(2080);
		testValues.betreuungsPensum = MathUtil.DEFAULT.fromNullSafe(80);
		testValues.anspruchsPensum = 80;
		testValues.einkommen = MathUtil.DEFAULT
			.fromNullSafe(TestUtils.getRechnerParameterLuzern().getMaxMassgebendesEinkommen())
			.add(BigDecimal.valueOf(1000));
		testValues.isBaby = true;

		testValues.expectedVollkosten = MathUtil.DEFAULT.fromNullSafe(2080);
		testValues.expectedMinimalerElternbeitrag = BigDecimal.valueOf(246);
		testValues.expectedBetreuungsZeiteinheit = BigDecimal.valueOf(16.4);
		testValues.expectedAnspruchsZeiteinheit =  BigDecimal.valueOf(16.4);
		testValues.expectedBgZeiteinheit =  BigDecimal.valueOf(16.4);

		VerfuegungZeitabschnitt zeitabschnitt = prepareVerfuegung(testValues);
		KitaLuzernRechner rechner = new KitaLuzernRechner(Collections.emptyList());
		rechner.calculate(zeitabschnitt, defaultParameterDTO);

		assertCalculationResultResult(zeitabschnitt.getRelevantBgCalculationResult(), testValues);
	}

	@Test
	public void testGueltigkeitNotFullMonth() {
		TestValues testValues = new TestValues();
		testValues.monatlicheBetreuungsKosten = MathUtil.DEFAULT.fromNullSafe(2080);
		testValues.betreuungsPensum = MathUtil.DEFAULT.fromNullSafe(80);
		testValues.anspruchsPensum = 80;
		testValues.einkommen = MathUtil.DEFAULT.fromNullSafe(50000);
		testValues.isBaby = true;
		testValues.gueltigkeit = dateRangePartFebruary; //Anteil am Monat = 67.857%

		testValues.expectedVollkosten = MathUtil.DEFAULT.fromNullSafe(1411.45);
		testValues.expectedVerguenstigungOhneBeruecksichtigungMinimalbetrag = BigDecimal.valueOf(1411.45);
		testValues.expectedVerguenstigungOhneBeruecksichtigungVollkosten = BigDecimal.valueOf(1571.75);
		testValues.expectedVerguenstigung = BigDecimal.valueOf(1244.5);
		testValues.expectedElternbeitrag = BigDecimal.valueOf(327.25);
		testValues.expectedMinimalerElternbeitrag = BigDecimal.valueOf(166.95);
		testValues.expectedBetreuungsZeiteinheit = BigDecimal.valueOf(11.13);
		testValues.expectedAnspruchsZeiteinheit =  BigDecimal.valueOf(11.13);
		testValues.expectedBgZeiteinheit =  BigDecimal.valueOf(11.13);


		VerfuegungZeitabschnitt zeitabschnitt = prepareVerfuegung(testValues);
		KitaLuzernRechner rechner = new KitaLuzernRechner(Collections.emptyList());
		rechner.calculate(zeitabschnitt, defaultParameterDTO);

		BGCalculationResult result = zeitabschnitt.getBgCalculationResultAsiv();
		assertCalculationResultResult(result, testValues);
	}

	@Test
	public void testKindBesondereBeduerfnisseZuschlag() { //Test Kind 5 gemäss Excel
		TestValues testValues = new TestValues();
		testValues.monatlicheBetreuungsKosten = MathUtil.DEFAULT.fromNullSafe(1600);
		testValues.betreuungsPensum = MathUtil.DEFAULT.fromNullSafe(50);
		testValues.anspruchsPensum = 40;
		testValues.besondereBeduerfnisseZuschlag = MathUtil.DEFAULT.fromNullSafe(10);
		testValues.einkommen = MathUtil.DEFAULT.fromNullSafe(52000);
		testValues.isBaby = false;

		testValues.expectedVollkosten = MathUtil.DEFAULT.fromNullSafe(1280);
		testValues.expectedVerguenstigungOhneBeruecksichtigungMinimalbetrag = BigDecimal.valueOf(976);
		testValues.expectedVerguenstigungOhneBeruecksichtigungVollkosten = BigDecimal.valueOf(976);
		testValues.expectedVerguenstigung = BigDecimal.valueOf(976);
		testValues.expectedMinimalerElternbeitrag = BigDecimal.valueOf(123);
		testValues.expectedBetreuungsZeiteinheit = BigDecimal.valueOf(10.25);
		testValues.expectedAnspruchsZeiteinheit =  BigDecimal.valueOf(8.2);
		testValues.expectedBgZeiteinheit =  BigDecimal.valueOf(8.2);

		VerfuegungZeitabschnitt zeitabschnitt = prepareVerfuegung(testValues);
		KitaLuzernRechner rechner = new KitaLuzernRechner(Collections.emptyList());
		rechner.calculate(zeitabschnitt, defaultParameterDTO);

		assertCalculationResultResult(zeitabschnitt.getRelevantBgCalculationResult(), testValues);
	}

	@Test
	public void testKindMinimalesEinkommen() { //Test Kind 6 gemäss Excel
		TestValues testValues = new TestValues();
		testValues.monatlicheBetreuungsKosten = MathUtil.DEFAULT.fromNullSafe(1600);
		testValues.betreuungsPensum = MathUtil.DEFAULT.fromNullSafe(30);
		testValues.anspruchsPensum = 50;
		testValues.einkommen = MathUtil.DEFAULT.fromNullSafe(40000);
		testValues.isBaby = false;

		testValues.expectedVollkosten = MathUtil.DEFAULT.fromNullSafe(1600);
		testValues.expectedVerguenstigungOhneBeruecksichtigungMinimalbetrag = BigDecimal.valueOf(707.25);
		testValues.expectedVerguenstigungOhneBeruecksichtigungVollkosten = BigDecimal.valueOf(707.25);
		testValues.expectedVerguenstigung = BigDecimal.valueOf(707.25);
		testValues.expectedMinimalerElternbeitrag = BigDecimal.valueOf(92.25);
		testValues.expectedBetreuungsZeiteinheit = BigDecimal.valueOf(6.15);
		testValues.expectedAnspruchsZeiteinheit =  BigDecimal.valueOf(10.25);
		testValues.expectedBgZeiteinheit =  BigDecimal.valueOf(6.15);

		VerfuegungZeitabschnitt zeitabschnitt = prepareVerfuegung(testValues);
		KitaLuzernRechner rechner = new KitaLuzernRechner(Collections.emptyList());
		rechner.calculate(zeitabschnitt, defaultParameterDTO);

		assertCalculationResultResult(zeitabschnitt.getRelevantBgCalculationResult(), testValues);
	}

	@Test
	public void unterMonatlicheZitabschnitteTest() {
		TestValues testValues = new TestValues();
		testValues.monatlicheBetreuungsKosten = MathUtil.DEFAULT.fromNullSafe(1600);
		testValues.betreuungsPensum = MathUtil.DEFAULT.fromNullSafe(30);
		testValues.anspruchsPensum = 50;
		testValues.einkommen = MathUtil.DEFAULT.fromNullSafe(40000);
		testValues.isBaby = false;
		testValues.gueltigkeit = new DateRange(
			LocalDate.of(2020, Month.AUGUST, 1),
			LocalDate.of(2020, Month.AUGUST, 15)); //48.38% des Monats

		//testValues.expectedVollkosten = MathUtil.DEFAULT.fromNullSafe(825.76);
		testValues.expectedVollkosten = MathUtil.DEFAULT.fromNullSafe(774.20);
		testValues.expectedVerguenstigungOhneBeruecksichtigungMinimalbetrag = BigDecimal.valueOf(342.2);
		testValues.expectedVerguenstigungOhneBeruecksichtigungVollkosten = BigDecimal.valueOf(342.2);
		testValues.expectedVerguenstigung = BigDecimal.valueOf(342.2);
		testValues.expectedMinimalerElternbeitrag = BigDecimal.valueOf(44.65);
		testValues.expectedBetreuungsZeiteinheit = BigDecimal.valueOf(2.98);
		testValues.expectedAnspruchsZeiteinheit =  BigDecimal.valueOf(4.96);
		testValues.expectedBgZeiteinheit =  BigDecimal.valueOf(2.98);

		VerfuegungZeitabschnitt zeitabschnitt = prepareVerfuegung(testValues);
		KitaLuzernRechner rechner = new KitaLuzernRechner(Collections.emptyList());
		rechner.calculate(zeitabschnitt, defaultParameterDTO);

		assertCalculationResultResult(zeitabschnitt.getRelevantBgCalculationResult(), testValues);
	}

	@Test
	public void testKindWithEingewoehnung() { //Test Kind1 gemäss Excel
		TestValues testValues = new TestValues();
		testValues.monatlicheBetreuungsKosten = MathUtil.DEFAULT.fromNullSafe(1600);
		testValues.betreuungsPensum = MathUtil.DEFAULT.fromNullSafe(60);
		testValues.anspruchsPensum = 60;
		testValues.einkommen = MathUtil.DEFAULT.fromNullSafe(120000);
		testValues.isBaby = false;

		testValues.expectedVollkosten = MathUtil.DEFAULT.fromNullSafe(1600);
		testValues.expectedVerguenstigungOhneBeruecksichtigungMinimalbetrag = BigDecimal.valueOf(123);
		testValues.expectedVerguenstigungOhneBeruecksichtigungVollkosten = BigDecimal.valueOf(123);
		testValues.expectedMinimalerElternbeitrag = BigDecimal.valueOf(184.50);
		testValues.expectedBetreuungsZeiteinheit = BigDecimal.valueOf(12.3);
		testValues.expectedAnspruchsZeiteinheit =  BigDecimal.valueOf(12.3);
		testValues.expectedBgZeiteinheit =  BigDecimal.valueOf(12.3);

		//gutschein pro Monat = 123
		//expected pauschale * verguenstigung / vollkosten => 500 * 123 / 1600 = 38.44
		testValues.expectedGutscheinEingewoehnung = BigDecimal.valueOf(38.44);

		//expected verguenstigung = gutschein pro Monat + gutschein eingewoehnung => 123 + 38.44 = 161.45 (161.44 auf 5 rp. gerundet)
		testValues.expectedVerguenstigung = BigDecimal.valueOf(161.45);
		VerfuegungZeitabschnitt zeitabschnitt = prepareVerfuegung(testValues);
		zeitabschnitt.getBgCalculationInputAsiv().setEingewoehnungPauschale(BigDecimal.valueOf(500));

		KitaLuzernRechner rechner = new KitaLuzernRechner(Collections.emptyList());
		rechner.calculate(zeitabschnitt, defaultParameterDTO);

		assertCalculationResultResult(zeitabschnitt.getRelevantBgCalculationResult(), testValues);
	}

	@Override
	protected void assertCalculationResultResult(BGCalculationResult result, TestValues testValues) {
		super.assertCalculationResultResult(result, testValues);
		Assert.assertEquals(PensumUnits.DAYS, result.getZeiteinheit());
	}

}
