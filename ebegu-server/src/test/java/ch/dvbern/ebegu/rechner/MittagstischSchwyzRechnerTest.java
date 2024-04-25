/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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
 *
 */

package ch.dvbern.ebegu.rechner;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.ebegu.util.TestUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MittagstischSchwyzRechnerTest {

	@Test
	void testKind1() {
		// given
		var testee = new MittagstischSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		setGueltigkeitGanzerApril(verfuegungZeitabschnitt);

		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		input.setTarifHauptmahlzeit(BigDecimal.valueOf(15));
		input.setAnzahlGeschwister(4);
		input.setMassgebendesEinkommenVorAbzugFamgr(new BigDecimal(10_000));
		input.setAnspruchspensumProzent(80);
		input.setBetreuungspensumProzent(BigDecimal.valueOf(80));
		input.setAbzugFamGroesse(BigDecimal.ZERO);


		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then
		var result = verfuegungZeitabschnitt.getRelevantBgCalculationResult();
		checkMappedInputs(input, result);
		assertEquals(new BigDecimal("16.40"), result.getAnspruchspensumZeiteinheit());
		assertEquals(new BigDecimal("16.40"), result.getBgPensumZeiteinheit());
		assertEquals(new BigDecimal("16.40"), result.getBetreuungspensumZeiteinheit());

		var gutscheinVorAbzugSelbstbehalt = new BigDecimal("244.35");
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungVollkosten());
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungMinimalbeitrag());
		assertEquals(new BigDecimal("123.00"), result.getVerguenstigung());
		assertEquals(new BigDecimal("123.00"), result.getMinimalerElternbeitrag());
		assertEquals(new BigDecimal("121.35"), result.getMinimalerElternbeitragGekuerzt());
		assertEquals(new BigDecimal("1.65"), result.getElternbeitrag());
	}

	@Test
	void testKind1HalberMonat() {
		// given
		var testee = new MittagstischSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		verfuegungZeitabschnitt.setGueltigkeit(new DateRange(
			LocalDate.of(2024, Month.APRIL, 16),
			LocalDate.of(2024, Month.APRIL, 30)));

		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		input.setTarifHauptmahlzeit(BigDecimal.valueOf(15));
		input.setAnzahlGeschwister(4);
		input.setMassgebendesEinkommenVorAbzugFamgr(new BigDecimal(10_000));
		input.setAnspruchspensumProzent(80);
		input.setBetreuungspensumProzent(BigDecimal.valueOf(80));
		input.setAbzugFamGroesse(BigDecimal.ZERO);

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then
		var result = verfuegungZeitabschnitt.getRelevantBgCalculationResult();
		checkMappedInputs(input, result);
		assertEquals(new BigDecimal("8.20"), result.getAnspruchspensumZeiteinheit());
		assertEquals(new BigDecimal("8.20"), result.getBgPensumZeiteinheit());
		assertEquals(new BigDecimal("8.20"), result.getBgPensumZeiteinheit());
		var gutscheinVorAbzugSelbstbehalt = new BigDecimal("122.15");
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungVollkosten());
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungMinimalbeitrag());
		assertEquals(new BigDecimal("61.50"), result.getVerguenstigung());
		assertEquals(
			new BigDecimal("61.50"),
			result.getMinimalerElternbeitrag());
		assertEquals(new BigDecimal("60.65"), result.getMinimalerElternbeitragGekuerzt());
		assertEquals(new BigDecimal("0.85"), result.getElternbeitrag());
	}


	@Test
	void testKind2() {
		// given
		var testee = new MittagstischSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		setGueltigkeitGanzerApril(verfuegungZeitabschnitt);

		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		input.setAnzahlGeschwister(1);
		input.setMassgebendesEinkommenVorAbzugFamgr(new BigDecimal("53300"));
		input.setAnspruchspensumProzent(50);
		input.setBetreuungspensumProzent(BigDecimal.valueOf(50));
		input.setTarifHauptmahlzeit(BigDecimal.valueOf(12));
		input.setAbzugFamGroesse(BigDecimal.ZERO);

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then
		var result = verfuegungZeitabschnitt.getRelevantBgCalculationResult();
		checkMappedInputs(input, result);
		assertEquals(new BigDecimal("10.25"), result.getAnspruchspensumZeiteinheit());
		assertEquals(new BigDecimal("10.25"), result.getBgPensumZeiteinheit());
		assertEquals(new BigDecimal("10.25"), result.getBetreuungspensumZeiteinheit());

		var gutscheinVorAbzugSelbstbehalt = new BigDecimal("69.90");
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungVollkosten());
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungMinimalbeitrag());
		assertEquals(new BigDecimal("46.15"), result.getVerguenstigung());
		assertEquals(
			new BigDecimal("76.90"),
			result.getMinimalerElternbeitrag());
		assertEquals(new BigDecimal("23.75"), result.getMinimalerElternbeitragGekuerzt());
		assertEquals(new BigDecimal("53.10"), result.getElternbeitrag());
	}

	@Test
	void testKind3() {
		// given
		var testee = new MittagstischSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		setGueltigkeitGanzerApril(verfuegungZeitabschnitt);

		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		input.setAnspruchspensumProzent(100);
		input.setBetreuungspensumProzent(BigDecimal.valueOf(100));
		input.setMassgebendesEinkommenVorAbzugFamgr(new BigDecimal(63_300));
		input.setAnzahlGeschwister(1);
		input.setTarifHauptmahlzeit(BigDecimal.valueOf(11.5));
		input.setAbzugFamGroesse(BigDecimal.ZERO);

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then
		var result = verfuegungZeitabschnitt.getRelevantBgCalculationResult();
		checkMappedInputs(input, result);
		assertEquals(new BigDecimal("20.50"), result.getAnspruchspensumZeiteinheit());
		assertEquals(new BigDecimal("20.50"), result.getBgPensumZeiteinheit());
		assertEquals(new BigDecimal("20.50"), result.getBetreuungspensumZeiteinheit());

		var gutscheinVorAbzugSelbstbehalt = new BigDecimal("120.55");
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungVollkosten());
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungMinimalbeitrag());
		assertEquals(new BigDecimal("82.00"), result.getVerguenstigung());
		assertEquals(
			new BigDecimal("153.75"),
			result.getMinimalerElternbeitrag());
		assertEquals(new BigDecimal("38.55"), result.getMinimalerElternbeitragGekuerzt());
		assertEquals(new BigDecimal("115.20"), result.getElternbeitrag());
	}

	@Test
	void testKind4() {
		// given
		var testee = new MittagstischSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		setGueltigkeitGanzerApril(verfuegungZeitabschnitt);

		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		input.setAnspruchspensumProzent(40);
		input.setBetreuungspensumProzent(BigDecimal.valueOf(40));
		input.setTarifHauptmahlzeit(BigDecimal.valueOf(15));
		input.setAnzahlGeschwister(2);
		input.setMassgebendesEinkommenVorAbzugFamgr(new BigDecimal(136_600));
		input.setAbzugFamGroesse(BigDecimal.ZERO);

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then
		var result = verfuegungZeitabschnitt.getRelevantBgCalculationResult();
		checkMappedInputs(input, result);
		assertEquals(new BigDecimal("8.20"), result.getAnspruchspensumZeiteinheit());
		assertEquals(new BigDecimal("8.20"), result.getBgPensumZeiteinheit());
		assertEquals(new BigDecimal("8.20"), result.getBetreuungspensumZeiteinheit());

		var gutscheinVorAbzugSelbstbehalt = new BigDecimal("12.45");
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungVollkosten());
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungMinimalbeitrag());
		assertEquals(new BigDecimal("12.45"), result.getVerguenstigung());
		assertEquals(
			new BigDecimal("61.50"),
			result.getMinimalerElternbeitrag());
		assertEquals(new BigDecimal("0.00"), result.getMinimalerElternbeitragGekuerzt());
		assertEquals(new BigDecimal("110.55"), result.getElternbeitrag());
	}

	@Test
	void testKind5_MassgebendesEinkommenNaheObergrenze() {
		// given
		var testee = new MittagstischSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		setGueltigkeitGanzerApril(verfuegungZeitabschnitt);

		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		input.setAnspruchspensumProzent(60);
		input.setBetreuungspensumProzent(BigDecimal.valueOf(60));
		input.setTarifHauptmahlzeit(BigDecimal.valueOf(18.5));
		input.setAnzahlGeschwister(3);
		input.setMassgebendesEinkommenVorAbzugFamgr(new BigDecimal(59_900));
		input.setAbzugFamGroesse(BigDecimal.ZERO);

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then
		var result = verfuegungZeitabschnitt.getRelevantBgCalculationResult();
		checkMappedInputs(input, result);
		assertEquals(new BigDecimal("12.30"), result.getAnspruchspensumZeiteinheit());
		assertEquals(new BigDecimal("12.30"), result.getBgPensumZeiteinheit());
		assertEquals(new BigDecimal("12.30"), result.getBetreuungspensumZeiteinheit());

		var gutscheinVorAbzugSelbstbehalt = new BigDecimal("127.20");
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungVollkosten());
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungMinimalbeitrag());
		assertEquals(new BigDecimal("116.85"), result.getVerguenstigung());
		assertEquals(
			new BigDecimal("92.25"),
			result.getMinimalerElternbeitrag());
		assertEquals(new BigDecimal("10.35"), result.getMinimalerElternbeitragGekuerzt());
		assertEquals(new BigDecimal("81.90"), result.getElternbeitrag());
	}

	private void setGueltigkeitGanzerApril(VerfuegungZeitabschnitt verfuegungZeitabschnitt) {
		verfuegungZeitabschnitt.setGueltigkeit(new DateRange(
			LocalDate.of(2024, Month.APRIL, 1),
			LocalDate.of(2024, Month.APRIL, 30)));
	}

	private void checkMappedInputs(BGCalculationInput input, BGCalculationResult result) {
		assertEquals(
			MathUtil.ZWEI_NACHKOMMASTELLE.from(input.getBetreuungspensumProzent()),
			result.getBetreuungspensumProzent());
		assertEquals(input.getAnspruchspensumProzent(), result.getAnspruchspensumProzent());
		assertEquals(MathUtil.ZWEI_NACHKOMMASTELLE.from(input.getBgPensumProzent()), result.getBgPensumProzent());
	}
}
