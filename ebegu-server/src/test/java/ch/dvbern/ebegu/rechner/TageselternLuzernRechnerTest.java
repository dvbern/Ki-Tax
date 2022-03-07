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

public class TageselternLuzernRechnerTest extends AbstractLuzernRechnerTest {

	private final BGRechnerParameterDTO defaultParameterDTO = getRechnerParameterLuzern();

	@Test
	public void testBaby() {
		TestValues testValues = new TestValues();
		testValues.monatlicheBetreuungsKosten = MathUtil.DEFAULT.fromNullSafe(2200);
		testValues.betreuungsPensum = MathUtil.DEFAULT.fromNullSafe(60);
		testValues.anspruchsPensum = 60;
		testValues.einkommen = MathUtil.DEFAULT.fromNullSafe(48000);
		testValues.isBaby = true;

		testValues.expectedVollkosten = MathUtil.DEFAULT.fromNullSafe(2200);
		testValues.expectedVerguenstigungOhneBeruecksichtigungMinimalbetrag = BigDecimal.valueOf(2110.7);
		testValues.expectedVerguenstigungOhneBeruecksichtigungVollkosten = BigDecimal.valueOf(2110.7);
		testValues.expectedVerguenstigung = BigDecimal.valueOf(2105.3);
		testValues.expectedElternbeitrag = BigDecimal.valueOf(5.4);
		testValues.expectedMinimalerElternbeitrag = BigDecimal.valueOf(94.7);
		testValues.expectedBetreuungsTage = BigDecimal.valueOf(135.3);
		testValues.expectedAnspruchsTage =  BigDecimal.valueOf(135.3);
		testValues.expectedBgTage =  BigDecimal.valueOf(135.3);

		VerfuegungZeitabschnitt zeitabschnitt = prepareVerfuegung(testValues);
		AbstractLuzernRechner rechner = new TageselternLuzernRechner();
		rechner.calculate(zeitabschnitt, defaultParameterDTO);

		BGCalculationResult result = zeitabschnitt.getBgCalculationResultAsiv();
		assertCalculationResultResult(result, testValues);
	}

	@Test
	public void testKind() {
		TestValues testValues = new TestValues();
		testValues.monatlicheBetreuungsKosten = MathUtil.DEFAULT.fromNullSafe(1200);
		testValues.betreuungsPensum = MathUtil.DEFAULT.fromNullSafe(60);
		testValues.anspruchsPensum = 80;
		testValues.einkommen = MathUtil.DEFAULT.fromNullSafe(55000);
		testValues.isBaby = false;

		testValues.expectedVollkosten = MathUtil.DEFAULT.fromNullSafe(1200);
		testValues.expectedVerguenstigungOhneBeruecksichtigungMinimalbetrag = BigDecimal.valueOf(1200);
		testValues.expectedVerguenstigungOhneBeruecksichtigungVollkosten = BigDecimal.valueOf(1439.1);
		testValues.expectedVerguenstigung = BigDecimal.valueOf(1105.3);
		testValues.expectedElternbeitrag = BigDecimal.valueOf(333.8);
		testValues.expectedMinimalerElternbeitrag = BigDecimal.valueOf(94.70);
		testValues.expectedBetreuungsTage = BigDecimal.valueOf(135.3);
		testValues.expectedAnspruchsTage =  BigDecimal.valueOf(180.4);
		testValues.expectedBgTage =  BigDecimal.valueOf(135.3);

		VerfuegungZeitabschnitt zeitabschnitt = prepareVerfuegung(testValues);
		AbstractLuzernRechner rechner = new TageselternLuzernRechner();
		rechner.calculate(zeitabschnitt, defaultParameterDTO);

		BGCalculationResult result = zeitabschnitt.getBgCalculationResultAsiv();
		assertCalculationResultResult(result, testValues);
	}

	@Test
	public void testKindEWPGreaterThenMax() {
		TestValues testValues = new TestValues();
		testValues.monatlicheBetreuungsKosten = MathUtil.DEFAULT.fromNullSafe(1200);
		testValues.betreuungsPensum = MathUtil.DEFAULT.fromNullSafe(40);
		testValues.anspruchsPensum = 20;
		testValues.einkommen = MathUtil.DEFAULT.fromNullSafe(125001);
		testValues.isBaby = false;

		testValues.expectedVollkosten = MathUtil.DEFAULT.fromNullSafe(600);
		testValues.expectedVerguenstigungOhneBeruecksichtigungMinimalbetrag = BigDecimal.ZERO;
		testValues.expectedVerguenstigungOhneBeruecksichtigungVollkosten = BigDecimal.ZERO;
		testValues.expectedVerguenstigung = BigDecimal.ZERO;
		testValues.expectedElternbeitrag = BigDecimal.ZERO;
		testValues.expectedMinimalerElternbeitrag = BigDecimal.valueOf(31.55);
		testValues.expectedBetreuungsTage = BigDecimal.valueOf(90.2);
		testValues.expectedAnspruchsTage =  BigDecimal.valueOf(45.1);
		testValues.expectedBgTage =  BigDecimal.valueOf(45.1);

		VerfuegungZeitabschnitt zeitabschnitt = prepareVerfuegung(testValues);
		AbstractLuzernRechner rechner = new TageselternLuzernRechner();
		rechner.calculate(zeitabschnitt, defaultParameterDTO);

		BGCalculationResult result = zeitabschnitt.getBgCalculationResultAsiv();
		assertCalculationResultResult(result, testValues);
	}

	@Test
	public void testBabyEWPGreaterThenMax() {
		TestValues testValues = new TestValues();
		testValues.monatlicheBetreuungsKosten = MathUtil.DEFAULT.fromNullSafe(1200);
		testValues.betreuungsPensum = MathUtil.DEFAULT.fromNullSafe(40);
		testValues.anspruchsPensum = 20;
		testValues.einkommen = MathUtil.DEFAULT.fromNullSafe(125001);
		testValues.isBaby = true;

		testValues.expectedVollkosten = MathUtil.DEFAULT.fromNullSafe(600);
		testValues.expectedVerguenstigungOhneBeruecksichtigungMinimalbetrag = BigDecimal.ZERO;
		testValues.expectedVerguenstigungOhneBeruecksichtigungVollkosten = BigDecimal.ZERO;
		testValues.expectedVerguenstigung = BigDecimal.ZERO;
		testValues.expectedElternbeitrag = BigDecimal.ZERO;
		testValues.expectedMinimalerElternbeitrag = BigDecimal.valueOf(31.55);
		testValues.expectedBetreuungsTage = BigDecimal.valueOf(90.2);
		testValues.expectedAnspruchsTage =  BigDecimal.valueOf(45.1);
		testValues.expectedBgTage =  BigDecimal.valueOf(45.1);

		VerfuegungZeitabschnitt zeitabschnitt = prepareVerfuegung(testValues);
		AbstractLuzernRechner rechner = new TageselternLuzernRechner();
		rechner.calculate(zeitabschnitt, defaultParameterDTO);

		BGCalculationResult result = zeitabschnitt.getBgCalculationResultAsiv();
		assertCalculationResultResult(result, testValues);
	}

	@Override
	protected void assertCalculationResultResult(BGCalculationResult result, TestValues testValues) {
		super.assertCalculationResultResult(result, testValues);
		Assert.assertEquals(PensumUnits.HOURS, result.getZeiteinheit());
	}
}
