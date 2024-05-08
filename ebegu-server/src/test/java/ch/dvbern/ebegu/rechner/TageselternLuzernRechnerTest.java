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
import java.util.Collections;

import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.PensumUnits;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.ebegu.util.TestUtils;
import org.junit.Assert;
import org.junit.Test;


public class TageselternLuzernRechnerTest extends AbstractLuzernRechnerTest {

	private final BGRechnerParameterDTO defaultParameterDTO = TestUtils.getRechnerParameterLuzern();

	@Test
	public void testKind() { //Kind 1 im BG_Rechner_Luzern Excel
		TestValues testValues = new TestValues();
		testValues.stuendlicheVollkosten = MathUtil.DEFAULT.fromNullSafe(10);
		testValues.betreuungsPensum = MathUtil.DEFAULT.fromNullSafe(22.172949);
		testValues.anspruchsPensum = 20;
		testValues.einkommen = MathUtil.DEFAULT.fromNullSafe(10000);
		testValues.isBaby = false;

		testValues.expectedVollkosten = MathUtil.DEFAULT.fromNullSafe(451);
		testValues.expectedVerguenstigungOhneBeruecksichtigungMinimalbetrag = BigDecimal.valueOf(10);
		testValues.expectedVerguenstigungOhneBeruecksichtigungVollkosten = BigDecimal.valueOf(11.70);
		testValues.expectedVerguenstigung = BigDecimal.valueOf(419.45);
		testValues.expectedVerguenstigungProZeiteinheit = BigDecimal.valueOf(9.30);
		testValues.expectedElternbeitrag = BigDecimal.valueOf(2.40);
		testValues.expectedMinimalerElternbeitrag = BigDecimal.valueOf(0.7);
		testValues.expectedBetreuungsZeiteinheit = BigDecimal.valueOf(49.99);
		testValues.expectedAnspruchsZeiteinheit =  BigDecimal.valueOf(45.1);
		testValues.expectedBgZeiteinheit =  BigDecimal.valueOf(45.1);

		VerfuegungZeitabschnitt zeitabschnitt = prepareVerfuegung(testValues);
		AbstractLuzernRechner rechner = new TageselternLuzernRechner(Collections.emptyList());
		rechner.calculate(zeitabschnitt, defaultParameterDTO);

		BGCalculationResult result = zeitabschnitt.getBgCalculationResultAsiv();
		assertCalculationResultResult(result, testValues);
	}

	@Test
	public void testBaby() {//Baby 1 im BG_Rechner_Luzern Excel
		TestValues testValues = new TestValues();
		testValues.stuendlicheVollkosten = MathUtil.DEFAULT.fromNullSafe(16.3);
		testValues.betreuungsPensum = MathUtil.DEFAULT.fromNullSafe(13.3037694);
		testValues.anspruchsPensum = 10;
		testValues.einkommen = MathUtil.DEFAULT.fromNullSafe(80000);
		testValues.isBaby = true;

		testValues.expectedVollkosten = MathUtil.DEFAULT.fromNullSafe(367.55);
		testValues.expectedVerguenstigungOhneBeruecksichtigungMinimalbetrag = BigDecimal.valueOf(9.10);
		testValues.expectedVerguenstigungOhneBeruecksichtigungVollkosten = BigDecimal.valueOf(9.10);
		testValues.expectedVerguenstigung = BigDecimal.valueOf(205.60);
		testValues.expectedVerguenstigungProZeiteinheit = BigDecimal.valueOf(9.12);
		testValues.expectedElternbeitrag = BigDecimal.valueOf(0);
		testValues.expectedMinimalerElternbeitrag = BigDecimal.valueOf(0.7);
		testValues.expectedBetreuungsZeiteinheit = BigDecimal.valueOf(29.99);
		testValues.expectedAnspruchsZeiteinheit =  BigDecimal.valueOf(22.55);
		testValues.expectedBgZeiteinheit =  BigDecimal.valueOf(22.55);

		VerfuegungZeitabschnitt zeitabschnitt = prepareVerfuegung(testValues);
		AbstractLuzernRechner rechner = new TageselternLuzernRechner(Collections.emptyList());
		rechner.calculate(zeitabschnitt, defaultParameterDTO);

		BGCalculationResult result = zeitabschnitt.getBgCalculationResultAsiv();
		assertCalculationResultResult(result, testValues);
	}

	@Test
	public void testBabyMitEingewoehnung() {
		TestValues testValues = new TestValues();
		testValues.stuendlicheVollkosten = MathUtil.DEFAULT.fromNullSafe(16.3);
		testValues.betreuungsPensum = MathUtil.DEFAULT.fromNullSafe(13.3037694);
		testValues.anspruchsPensum = 10;
		testValues.einkommen = MathUtil.DEFAULT.fromNullSafe(80000);
		testValues.isBaby = true;

		testValues.expectedVollkosten = MathUtil.DEFAULT.fromNullSafe(367.55);
		testValues.expectedVerguenstigungOhneBeruecksichtigungMinimalbetrag = BigDecimal.valueOf(9.10);
		testValues.expectedVerguenstigungOhneBeruecksichtigungVollkosten = BigDecimal.valueOf(9.10);
		testValues.expectedVerguenstigungProZeiteinheit = BigDecimal.valueOf(9.12);
		testValues.expectedElternbeitrag = BigDecimal.valueOf(0);
		testValues.expectedMinimalerElternbeitrag = BigDecimal.valueOf(0.7);
		testValues.expectedBetreuungsZeiteinheit = BigDecimal.valueOf(29.99);
		testValues.expectedAnspruchsZeiteinheit =  BigDecimal.valueOf(22.55);
		testValues.expectedBgZeiteinheit =  BigDecimal.valueOf(22.55);

		//gutschein pro Monat = 205.59
		//expected pauschale * verguenstigung / vollkosten => 500 * 205.60 / 367.55 = 279.66 (279.65 auf 5rp gerundet)
		testValues.expectedGutscheinEingewoehnung = BigDecimal.valueOf(279.65);

		//expected verguenstigung = gutschein pro Monat + gutschein eingewoehnung => 205.59 + 279.65 = 485.24 (485.25 auf 5rp gerundet)
		testValues.expectedVerguenstigung = BigDecimal.valueOf(485.25);

		VerfuegungZeitabschnitt zeitabschnitt = prepareVerfuegung(testValues);
		zeitabschnitt.getBgCalculationInputAsiv().setEingewoehnungKosten(BigDecimal.valueOf(500));

		AbstractLuzernRechner rechner = new TageselternLuzernRechner(Collections.emptyList());
		rechner.calculate(zeitabschnitt, defaultParameterDTO);

		BGCalculationResult result = zeitabschnitt.getBgCalculationResultAsiv();
		assertCalculationResultResult(result, testValues);
	}

	@Test
	public void testKindEWPGreaterThenMax() { //Kind 2 im BG_Rechner_Luzern Excel
		TestValues testValues = new TestValues();
		testValues.stuendlicheVollkosten = MathUtil.DEFAULT.fromNullSafe(10);
		testValues.betreuungsPensum = MathUtil.DEFAULT.fromNullSafe(66.518847);
		testValues.anspruchsPensum = 80;
		testValues.einkommen = MathUtil.DEFAULT.fromNullSafe(125001);
		testValues.isBaby = false;

		testValues.expectedVollkosten = MathUtil.DEFAULT.fromNullSafe(1500.05);
		testValues.expectedVerguenstigungOhneBeruecksichtigungMinimalbetrag = BigDecimal.ZERO;
		testValues.expectedVerguenstigungOhneBeruecksichtigungVollkosten = BigDecimal.ZERO;
		testValues.expectedVerguenstigung = BigDecimal.ZERO;
		testValues.expectedVerguenstigungProZeiteinheit = BigDecimal.ZERO;
		testValues.expectedElternbeitrag = BigDecimal.ZERO;
		testValues.expectedMinimalerElternbeitrag = BigDecimal.valueOf(0.7);
		testValues.expectedBetreuungsZeiteinheit = BigDecimal.valueOf(150);
		testValues.expectedAnspruchsZeiteinheit =  BigDecimal.valueOf(180.4);
		testValues.expectedBgZeiteinheit =  BigDecimal.valueOf(150);

		VerfuegungZeitabschnitt zeitabschnitt = prepareVerfuegung(testValues);
		AbstractLuzernRechner rechner = new TageselternLuzernRechner(Collections.emptyList());
		rechner.calculate(zeitabschnitt, defaultParameterDTO);

		BGCalculationResult result = zeitabschnitt.getBgCalculationResultAsiv();
		assertCalculationResultResult(result, testValues);
	}

	@Test
	public void testBabyEWPMinimum() { //Baby 2 im BG_Rechner_Luzern Excel
		TestValues testValues = new TestValues();
		testValues.stuendlicheVollkosten = MathUtil.DEFAULT.fromNullSafe(16.3);
		testValues.betreuungsPensum = MathUtil.DEFAULT.fromNullSafe(27.937916);
		testValues.anspruchsPensum = 50;
		testValues.einkommen = MathUtil.DEFAULT.fromNullSafe(48000);
		testValues.isBaby = true;

		testValues.expectedVollkosten = MathUtil.DEFAULT.fromNullSafe(1027);
		testValues.expectedVerguenstigungOhneBeruecksichtigungMinimalbetrag = BigDecimal.valueOf(15.60);
		testValues.expectedVerguenstigungOhneBeruecksichtigungVollkosten = BigDecimal.valueOf(15.60);
		testValues.expectedVerguenstigung = BigDecimal.valueOf(982.85);
		testValues.expectedVerguenstigungProZeiteinheit = BigDecimal.valueOf(15.60);
		testValues.expectedElternbeitrag = BigDecimal.ZERO;
		testValues.expectedMinimalerElternbeitrag = BigDecimal.valueOf(0.7);
		testValues.expectedBetreuungsZeiteinheit = BigDecimal.valueOf(63);
		testValues.expectedAnspruchsZeiteinheit =  BigDecimal.valueOf(112.75);
		testValues.expectedBgZeiteinheit =  BigDecimal.valueOf(63);

		VerfuegungZeitabschnitt zeitabschnitt = prepareVerfuegung(testValues);
		AbstractLuzernRechner rechner = new TageselternLuzernRechner(Collections.emptyList());
		rechner.calculate(zeitabschnitt, defaultParameterDTO);

		BGCalculationResult result = zeitabschnitt.getBgCalculationResultAsiv();
		assertCalculationResultResult(result, testValues);
	}

	@Test
	public void testKindZuschlag() { //Kind 3 im BG_Rechner_Luzern Excel
		TestValues testValues = new TestValues();
		testValues.stuendlicheVollkosten = MathUtil.DEFAULT.fromNullSafe(10);
		testValues.betreuungsPensum = MathUtil.DEFAULT.fromNullSafe(32.815965);
		testValues.anspruchsPensum = 40;
		testValues.einkommen = MathUtil.DEFAULT.fromNullSafe(75000);
		testValues.besondereBeduerfnisseZuschlag = BigDecimal.valueOf(2);
		testValues.isBaby = false;

		testValues.expectedVollkosten = MathUtil.DEFAULT.fromNullSafe(740.1);
		testValues.expectedVerguenstigungOhneBeruecksichtigungMinimalbetrag = BigDecimal.valueOf(9.60);
		testValues.expectedVerguenstigungOhneBeruecksichtigungVollkosten = BigDecimal.valueOf(9.60);
		testValues.expectedVerguenstigung = BigDecimal.valueOf(710.3);;
		testValues.expectedVerguenstigungProZeiteinheit = BigDecimal.valueOf(9.60);
		testValues.expectedElternbeitrag = BigDecimal.ZERO;
		testValues.expectedMinimalerElternbeitrag = BigDecimal.valueOf(0.7);
		testValues.expectedBetreuungsZeiteinheit = BigDecimal.valueOf(74.01);
		testValues.expectedAnspruchsZeiteinheit =  BigDecimal.valueOf(90.2);
		testValues.expectedBgZeiteinheit =  BigDecimal.valueOf(74.01);

		VerfuegungZeitabschnitt zeitabschnitt = prepareVerfuegung(testValues);
		AbstractLuzernRechner rechner = new TageselternLuzernRechner(Collections.emptyList());
		rechner.calculate(zeitabschnitt, defaultParameterDTO);

		BGCalculationResult result = zeitabschnitt.getBgCalculationResultAsiv();
		assertCalculationResultResult(result, testValues);
	}

	@Test
	public void testKindOhneBetreuung() { //Kind 1 im BG_Rechner_Luzern Excel ohne Betreuungspensum (rückwirkende Eingabe)
		TestValues testValues = new TestValues();
		testValues.stuendlicheVollkosten = MathUtil.DEFAULT.fromNullSafe(10);
		testValues.betreuungsPensum = BigDecimal.ZERO;
		testValues.anspruchsPensum = 20;
		testValues.einkommen = MathUtil.DEFAULT.fromNullSafe(10000);
		testValues.isBaby = false;

		testValues.expectedVollkosten = BigDecimal.ZERO;
		testValues.expectedVerguenstigungOhneBeruecksichtigungMinimalbetrag = BigDecimal.valueOf(10);
		testValues.expectedVerguenstigungOhneBeruecksichtigungVollkosten = BigDecimal.valueOf(11.70);
		testValues.expectedVerguenstigung = BigDecimal.ZERO;
		testValues.expectedVerguenstigungProZeiteinheit = BigDecimal.valueOf(9.30);
		testValues.expectedElternbeitrag = BigDecimal.valueOf(2.40);
		testValues.expectedMinimalerElternbeitrag = BigDecimal.valueOf(0.7);
		testValues.expectedBetreuungsZeiteinheit = BigDecimal.ZERO;
		testValues.expectedAnspruchsZeiteinheit =  BigDecimal.valueOf(45.1);
		testValues.expectedBgZeiteinheit =  BigDecimal.ZERO;;

		VerfuegungZeitabschnitt zeitabschnitt = prepareVerfuegung(testValues);
		AbstractLuzernRechner rechner = new TageselternLuzernRechner(Collections.emptyList());
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
