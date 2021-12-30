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

import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.PensumUnits;
import ch.dvbern.ebegu.util.MathUtil;
import org.junit.Assert;
import org.junit.Test;

public class KitaLuzernRechnerTest extends AbstractLuzernRechnerTest {

	private final BGRechnerParameterDTO defaultParameterDTO = getKitaParameterLuzern();


	@Test
	public void testSaeugling1() { //Test Saeugling1 gemäss Excel
		TestValues testValues = new TestValues();
		testValues.monatlicheBetreuungsKosten = MathUtil.DEFAULT.fromNullSafe(2080);
		testValues.betreuungsPensum = MathUtil.DEFAULT.fromNullSafe(80);
		testValues.anspruchsPensum = 80;
		testValues.einkommen = MathUtil.DEFAULT.fromNullSafe(50000);
		testValues.isBaby = true;

		testValues.expectedVerguenstigungOhneBeruecksichtigungMinimalbetrag = BigDecimal.valueOf(2316.25);
		testValues.expectedVerguenstigungOhneBeruecksichtigungVollkosten = BigDecimal.valueOf(1834);
		testValues.expectedVerguenstigung = BigDecimal.valueOf(1834);
		testValues.expectedElternbeitrag = BigDecimal.valueOf(482.25);
		testValues.expectedMinimalerElternbeitrag = BigDecimal.valueOf(246);
		testValues.expectedBetreuungsTage = BigDecimal.valueOf(16.4);


		VerfuegungZeitabschnitt zeitabschnitt = prepareVerfuegung(testValues);
		KitaLuzernRechner rechner = new KitaLuzernRechner();
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

		testValues.expectedVerguenstigungOhneBeruecksichtigungMinimalbetrag = BigDecimal.valueOf(868.6);
		testValues.expectedVerguenstigungOhneBeruecksichtigungVollkosten = BigDecimal.valueOf(868.6);
		testValues.expectedVerguenstigung = BigDecimal.valueOf(1196.6);
		testValues.expectedMinimalerElternbeitrag = BigDecimal.valueOf(153.75);
		testValues.expectedBetreuungsTage = BigDecimal.valueOf(10.25);

		VerfuegungZeitabschnitt zeitabschnitt = prepareVerfuegung(testValues);
		zeitabschnitt.getBgCalculationInputAsiv().setKitaPlusZuschlag(true);
		KitaLuzernRechner rechner = new KitaLuzernRechner();
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

		testValues.expectedVerguenstigungOhneBeruecksichtigungMinimalbetrag = BigDecimal.valueOf(123);
		testValues.expectedVerguenstigungOhneBeruecksichtigungVollkosten = BigDecimal.valueOf(123);
		testValues.expectedVerguenstigung = BigDecimal.valueOf(123);
		testValues.expectedMinimalerElternbeitrag = BigDecimal.valueOf(184.50);
		testValues.expectedBetreuungsTage = BigDecimal.valueOf(12.3);

		VerfuegungZeitabschnitt zeitabschnitt = prepareVerfuegung(testValues);
		KitaLuzernRechner rechner = new KitaLuzernRechner();
		rechner.calculate(zeitabschnitt, defaultParameterDTO);

		assertCalculationResultResult(zeitabschnitt.getRelevantBgCalculationResult(), testValues);
	}

	@Test
	public void testKind2() { //Test Kind1 gemäss Excel
		TestValues testValues = new TestValues();
		testValues.monatlicheBetreuungsKosten = MathUtil.DEFAULT.fromNullSafe(1600);
		testValues.betreuungsPensum = MathUtil.DEFAULT.fromNullSafe(60);
		testValues.anspruchsPensum = 60;
		testValues.einkommen = MathUtil.DEFAULT.fromNullSafe(125001);
		testValues.isBaby = false;

		testValues.expectedMinimalerElternbeitrag = BigDecimal.valueOf(184.50);
		testValues.expectedBetreuungsTage = BigDecimal.valueOf(12.3);

		VerfuegungZeitabschnitt zeitabschnitt = prepareVerfuegung(testValues);
		KitaLuzernRechner rechner = new KitaLuzernRechner();
		rechner.calculate(zeitabschnitt, defaultParameterDTO);

		assertCalculationResultResult(zeitabschnitt.getRelevantBgCalculationResult(), testValues);
	}

	@Test
	public void testKind() { //Test Kind1 gemäss Excel
		TestValues testValues = new TestValues();
		testValues.monatlicheBetreuungsKosten = MathUtil.DEFAULT.fromNullSafe(1600);
		testValues.betreuungsPensum = MathUtil.DEFAULT.fromNullSafe(70);
		testValues.anspruchsPensum = 60;
		testValues.einkommen = MathUtil.DEFAULT.fromNullSafe(48000);
		testValues.isBaby = false;

		testValues.expectedVerguenstigungOhneBeruecksichtigungMinimalbetrag = BigDecimal.valueOf(1414.50);
		testValues.expectedVerguenstigungOhneBeruecksichtigungVollkosten = BigDecimal.valueOf(1414.50);
		testValues.expectedVerguenstigung = BigDecimal.valueOf(1414.50);
		testValues.expectedMinimalerElternbeitrag = BigDecimal.valueOf(184.50);
		testValues.expectedBetreuungsTage = BigDecimal.valueOf(12.3);

		VerfuegungZeitabschnitt zeitabschnitt = prepareVerfuegung(testValues);
		KitaLuzernRechner rechner = new KitaLuzernRechner();
		rechner.calculate(zeitabschnitt, defaultParameterDTO);

		assertCalculationResultResult(zeitabschnitt.getRelevantBgCalculationResult(), testValues);
	}

	@Test
	public void testKindKitaPlus() { //Test Kind1 gemäss Excel
		TestValues testValues = new TestValues();
		testValues.monatlicheBetreuungsKosten = MathUtil.DEFAULT.fromNullSafe(1600);
		testValues.betreuungsPensum = MathUtil.DEFAULT.fromNullSafe(60);
		testValues.anspruchsPensum = 40;
		testValues.einkommen = MathUtil.DEFAULT.fromNullSafe(48000);
		testValues.isBaby = false;

		testValues.expectedVerguenstigungOhneBeruecksichtigungMinimalbetrag = BigDecimal.valueOf(943);
		testValues.expectedVerguenstigungOhneBeruecksichtigungVollkosten = BigDecimal.valueOf(943);
		testValues.expectedVerguenstigung = BigDecimal.valueOf(1205.40);
		testValues.expectedMinimalerElternbeitrag = BigDecimal.valueOf(123);
		testValues.expectedBetreuungsTage = BigDecimal.valueOf(8.2);

		VerfuegungZeitabschnitt zeitabschnitt = prepareVerfuegung(testValues);
		zeitabschnitt.getBgCalculationInputAsiv().setKitaPlusZuschlag(true);
		KitaLuzernRechner rechner = new KitaLuzernRechner();
		rechner.calculate(zeitabschnitt, defaultParameterDTO);

		assertCalculationResultResult(zeitabschnitt.getRelevantBgCalculationResult(), testValues);
	}

	@Test
	public void testSaeuglingEWPgreaterThenMaxBaby() {
		TestValues testValues = new TestValues();
		testValues.monatlicheBetreuungsKosten = MathUtil.DEFAULT.fromNullSafe(2080);
		testValues.betreuungsPensum = MathUtil.DEFAULT.fromNullSafe(80);
		testValues.anspruchsPensum = 80;
		testValues.einkommen = MathUtil.DEFAULT
			.fromNullSafe(getKitaParameterLuzern().getMaxMassgebendesEinkommen())
			.add(BigDecimal.valueOf(1000));
		testValues.isBaby = true;

		testValues.expectedMinimalerElternbeitrag = BigDecimal.valueOf(246);
		testValues.expectedBetreuungsTage = BigDecimal.valueOf(16.4);

		VerfuegungZeitabschnitt zeitabschnitt = prepareVerfuegung(testValues);
		KitaLuzernRechner rechner = new KitaLuzernRechner();
		rechner.calculate(zeitabschnitt, defaultParameterDTO);

		assertCalculationResultResult(zeitabschnitt.getRelevantBgCalculationResult(), testValues);
	}

	@Override
	protected void assertCalculationResultResult(BGCalculationResult result, TestValues testValues) {
		super.assertCalculationResultResult(result, testValues);
		Assert.assertEquals(PensumUnits.DAYS, result.getZeiteinheit());
	}

}
