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
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.ebegu.util.TestUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static ch.dvbern.ebegu.rechner.TagesfamilienSchwyzRechner.TFO_NORMKOSTEN_PRIMARSTUFE_SCHULFREIEN_ZEIT;
import static ch.dvbern.ebegu.rechner.TagesfamilienSchwyzRechner.TFO_NORMKOSTEN_PRIMARSTUFE_SCHULZEIT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TagesfamilienSchwyzRechnerTest {

	@Test
	void testKind1() {
		// given
		var testee = new TagesfamilienSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		setGueltigkeitGanzerApril(verfuegungZeitabschnitt);

		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		input.setAnwesenheitsTageProMonat(BigDecimal.valueOf(10));
		input.setBabyTarif(false);
		input.setEinschulungTyp(EinschulungTyp.KLASSE1);
		input.setBetreuungInFerienzeit(false);
		input.setAnzahlGeschwister(4);
		input.setBetreuungspensumProzent(new BigDecimal(40));
		input.setAnspruchspensumProzent(60);
		input.setMassgebendesEinkommenVorAbzugFamgr(new BigDecimal(27_000));
		input.setAbzugFamGroesse(BigDecimal.ZERO);
		input.setMonatlicheBetreuungskosten(new BigDecimal(1_500));

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then
		var result = verfuegungZeitabschnitt.getRelevantBgCalculationResult();
		checkMappedInputs(input, result);
		assertEquals(new BigDecimal("123.00"), result.getAnspruchspensumZeiteinheit());
		assertEquals(new BigDecimal("82.00"), result.getBgPensumZeiteinheit());
		assertEquals(new BigDecimal("82.00"), result.getBetreuungspensumZeiteinheit());
		assertEquals(
			new BigDecimal("1500.00"),
			result.getVollkosten());
		var gutscheinVorAbzugSelbstbehalt = new BigDecimal("223.35");
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungVollkosten());
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungMinimalbeitrag());
		assertEquals(new BigDecimal("89.20"), result.getVerguenstigung());
		assertEquals(new BigDecimal("246.00"), result.getMinimalerElternbeitrag());
		assertEquals(new BigDecimal("134.15"), result.getMinimalerElternbeitragGekuerzt());
		assertEquals(new BigDecimal("111.85"), result.getElternbeitrag());
	}

	@Test
	void testKind1_halberMonat() {
		// given
		var testee = new TagesfamilienSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		verfuegungZeitabschnitt.setGueltigkeit(new DateRange(
			LocalDate.of(2024, Month.APRIL, 1),
			LocalDate.of(2024, Month.APRIL, 15)));

		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		input.setAnwesenheitsTageProMonat(BigDecimal.valueOf(10));
		input.setBabyTarif(false);
		input.setEinschulungTyp(EinschulungTyp.KLASSE1);
		input.setBetreuungInFerienzeit(false);
		input.setAnzahlGeschwister(4);
		input.setBetreuungspensumProzent(new BigDecimal(40));
		input.setAnspruchspensumProzent(60);
		input.setMassgebendesEinkommenVorAbzugFamgr(new BigDecimal(27_000));
		input.setAbzugFamGroesse(BigDecimal.ZERO);
		input.setMonatlicheBetreuungskosten(new BigDecimal(1_500));

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then
		var result = verfuegungZeitabschnitt.getRelevantBgCalculationResult();
		checkMappedInputs(input, result);
		assertEquals(new BigDecimal("61.50"), result.getAnspruchspensumZeiteinheit());
		assertEquals(new BigDecimal("41.00"), result.getBgPensumZeiteinheit());
		assertEquals(new BigDecimal("41.00"), result.getBetreuungspensumZeiteinheit());
		assertEquals(
			new BigDecimal("750.00"),
			result.getVollkosten());
		var gutscheinVorAbzugSelbstbehalt = new BigDecimal("111.65");
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungVollkosten());
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungMinimalbeitrag());
		assertEquals(new BigDecimal("44.60"), result.getVerguenstigung());
		assertEquals(new BigDecimal("123.00"), result.getMinimalerElternbeitrag());
		assertEquals(new BigDecimal("67.05"), result.getMinimalerElternbeitragGekuerzt());
		assertEquals(new BigDecimal("55.95"), result.getElternbeitrag());
	}

	@Test
	void testKind2() {
		// given
		var testee = new TagesfamilienSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		setGueltigkeitGanzerApril(verfuegungZeitabschnitt);

		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		input.setAnwesenheitsTageProMonat(BigDecimal.valueOf(8.2));
		input.setBabyTarif(true);
		input.setEinschulungTyp(null);
		input.setBetreuungInFerienzeit(false);
		input.setAnzahlGeschwister(4);
		input.setBetreuungspensumProzent(new BigDecimal(40));
		input.setAnspruchspensumProzent(20);
		input.setMassgebendesEinkommenVorAbzugFamgr(new BigDecimal(50_000));
		input.setAbzugFamGroesse(BigDecimal.ZERO);
		input.setMonatlicheBetreuungskosten(new BigDecimal(1_230));

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then
		var result = verfuegungZeitabschnitt.getRelevantBgCalculationResult();
		checkMappedInputs(input, result);
		assertEquals(new BigDecimal("41.00"), result.getAnspruchspensumZeiteinheit());
		assertEquals(new BigDecimal("41.00"), result.getBgPensumZeiteinheit());
		assertEquals(new BigDecimal("82.00"), result.getBetreuungspensumZeiteinheit());
		assertEquals(
			new BigDecimal("1230.00"),
			result.getVollkosten());
		var gutscheinVorAbzugSelbstbehalt = new BigDecimal("423.10");
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungVollkosten());
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungMinimalbeitrag());
		assertEquals(new BigDecimal("385.40"), result.getVerguenstigung());
		assertEquals(new BigDecimal("123.00"), result.getMinimalerElternbeitrag());
		assertEquals(new BigDecimal("37.70"), result.getMinimalerElternbeitragGekuerzt());
		assertEquals(new BigDecimal("85.30"), result.getElternbeitrag());
	}

	@Test
	void testKind3() {
		// given
		var testee = new TagesfamilienSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		setGueltigkeitGanzerApril(verfuegungZeitabschnitt);

		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		input.setAnwesenheitsTageProMonat(BigDecimal.valueOf(20));
		input.setBabyTarif(false);
		input.setEinschulungTyp(EinschulungTyp.PRIMARSTUFE);
		input.setBetreuungInFerienzeit(true);
		input.setAnzahlGeschwister(1);
		input.setBetreuungspensumProzent(new BigDecimal(20));
		input.setAnspruchspensumProzent(40);
		input.setMassgebendesEinkommenVorAbzugFamgr(new BigDecimal(50_000));
		input.setAbzugFamGroesse(BigDecimal.ZERO);
		input.setMonatlicheBetreuungskosten(new BigDecimal(2_000));

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then
		var result = verfuegungZeitabschnitt.getRelevantBgCalculationResult();
		checkMappedInputs(input, result);
		assertEquals(new BigDecimal("82.00"), result.getAnspruchspensumZeiteinheit());
		assertEquals(new BigDecimal("41.00"), result.getBgPensumZeiteinheit());
		assertEquals(new BigDecimal("41.00"), result.getBetreuungspensumZeiteinheit());
		assertEquals(
			new BigDecimal("2000.00"),
			result.getVollkosten());
		var gutscheinVorAbzugSelbstbehalt = new BigDecimal("209.60");
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungVollkosten());
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungMinimalbeitrag());
		assertEquals(new BigDecimal("203.00"), result.getVerguenstigung());
		assertEquals(new BigDecimal("123.00"), result.getMinimalerElternbeitrag());
		assertEquals(new BigDecimal("6.60"), result.getMinimalerElternbeitragGekuerzt());
		assertEquals(new BigDecimal("116.40"), result.getElternbeitrag());
	}

	@Test
	void testKind4_naheEinkommensObergrenze() {
		// given
		var testee = new TagesfamilienSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		setGueltigkeitGanzerApril(verfuegungZeitabschnitt);

		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		input.setAnwesenheitsTageProMonat(BigDecimal.valueOf(20));
		input.setBabyTarif(false);
		input.setEinschulungTyp(EinschulungTyp.PRIMARSTUFE);
		input.setBetreuungInFerienzeit(true);
		input.setAnzahlGeschwister(1);
		input.setBetreuungspensumProzent(new BigDecimal(20));
		input.setAnspruchspensumProzent(40);
		input.setMassgebendesEinkommenVorAbzugFamgr(new BigDecimal(153_115));
		input.setAbzugFamGroesse(BigDecimal.ZERO);
		input.setMonatlicheBetreuungskosten(new BigDecimal(2_000));

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then
		var result = verfuegungZeitabschnitt.getRelevantBgCalculationResult();
		checkMappedInputs(input, result);
		assertEquals(new BigDecimal("82.00"), result.getAnspruchspensumZeiteinheit());
		assertEquals(new BigDecimal("41.00"), result.getBgPensumZeiteinheit());
		assertEquals(new BigDecimal("41.00"), result.getBetreuungspensumZeiteinheit());
		assertEquals(
			new BigDecimal("2000.00"),
			result.getVollkosten());
		var gutscheinVorAbzugSelbstbehalt = new BigDecimal("0.20");
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungVollkosten());
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungMinimalbeitrag());
		assertEquals(new BigDecimal("0.20"), result.getVerguenstigung());
		assertEquals(new BigDecimal("123.00"), result.getMinimalerElternbeitrag());
		assertEquals(new BigDecimal("0.00"), result.getMinimalerElternbeitragGekuerzt());
		assertEquals(new BigDecimal("325.80"), result.getElternbeitrag());
	}

	@Test
	void testKind5_naheEinkommensUntergrenze() {
		// given
		var testee = new TagesfamilienSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		setGueltigkeitGanzerApril(verfuegungZeitabschnitt);

		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		input.setAnwesenheitsTageProMonat(BigDecimal.valueOf(10));
		input.setBabyTarif(false);
		input.setEinschulungTyp(EinschulungTyp.PRIMARSTUFE);
		input.setBetreuungInFerienzeit(true);
		input.setAnzahlGeschwister(1);
		input.setBetreuungspensumProzent(new BigDecimal(60));
		input.setAnspruchspensumProzent(40);
		input.setMassgebendesEinkommenVorAbzugFamgr(new BigDecimal(47_195));
		input.setAbzugFamGroesse(BigDecimal.ZERO);
		input.setMonatlicheBetreuungskosten(new BigDecimal(2_000));

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then
		var result = verfuegungZeitabschnitt.getRelevantBgCalculationResult();
		checkMappedInputs(input, result);
		assertEquals(new BigDecimal("82.00"), result.getAnspruchspensumZeiteinheit());
		assertEquals(new BigDecimal("82.00"), result.getBgPensumZeiteinheit());
		assertEquals(new BigDecimal("123.00"), result.getBetreuungspensumZeiteinheit());
		assertEquals(
			new BigDecimal("2000.00"),
			result.getVollkosten());
		var gutscheinVorAbzugSelbstbehalt = new BigDecimal("297.25");
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungVollkosten());
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungMinimalbeitrag());
		assertEquals(new BigDecimal("272.65"), result.getVerguenstigung());
		assertEquals(new BigDecimal("246.00"), result.getMinimalerElternbeitrag());
		assertEquals(new BigDecimal("24.60"), result.getMinimalerElternbeitragGekuerzt());
		assertEquals(new BigDecimal("221.40"), result.getElternbeitrag());
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

	@Test
	void testGetNormkostenOhneVermittlungsGebuehrForBabyTarif() {
		// given
		var testee = new TagesfamilienSchwyzRechner();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		input.setBabyTarif(true);

		var parameter = TestUtils.getRechnerParamterSchwyz();

		// when
		var normkosten = testee.getNormkostenOhneVermittlungsGebuehr(input, parameter);

		// then
		assertEquals(parameter.getMaxVerguenstigungVorschuleBabyProStd(), normkosten);
	}

	@Test
	void testGetNormkostenOhneVermittlungsGebuehrIfFreiwilligerKindergarten() {
		// given
		var testee = new TagesfamilienSchwyzRechner();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		input.setEinschulungTyp(EinschulungTyp.FREIWILLIGER_KINDERGARTEN);
		var parameter = TestUtils.getRechnerParamterSchwyz();

		// when
		var normkosten = testee.getNormkostenOhneVermittlungsGebuehr(input, parameter);

		// then
		assertEquals(parameter.getMaxVerguenstigungVorschuleKindProStd(), normkosten);
	}

	@Test
	void testGetNormkostenOhneVermittlungsGebuehrIfNotEingeschult() {
		// given
		var testee = new TagesfamilienSchwyzRechner();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		input.setEinschulungTyp(null);
		var parameter = TestUtils.getRechnerParamterSchwyz();

		// when
		var normkosten = testee.getNormkostenOhneVermittlungsGebuehr(input, parameter);

		// then
		assertEquals(parameter.getMaxVerguenstigungVorschuleKindProStd(), normkosten);
	}

	@Test
	void testGetNormkostenOhneVermittlungsGebuehrIfEingeschultAberAusserhalbDerSchulzeitBetreut() {
		// given
		var testee = new TagesfamilienSchwyzRechner();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		input.setEinschulungTyp(EinschulungTyp.KLASSE1);
		input.setBetreuungInFerienzeit(true);
		var parameter = TestUtils.getRechnerParamterSchwyz();

		// when
		var normkosten = testee.getNormkostenOhneVermittlungsGebuehr(input, parameter);

		// then
		assertEquals(TFO_NORMKOSTEN_PRIMARSTUFE_SCHULFREIEN_ZEIT, normkosten);
	}

	@Test
	void testGetNormkostenOhneVermittlungsGebuehrIfEingeschultAberWaehrendDerSchulzeitBetreut() {
		// given
		var testee = new TagesfamilienSchwyzRechner();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		input.setEinschulungTyp(EinschulungTyp.KLASSE1);
		input.setBetreuungInFerienzeit(false);
		var parameter = TestUtils.getRechnerParamterSchwyz();

		// when
		var normkosten = testee.getNormkostenOhneVermittlungsGebuehr(input, parameter);

		// then
		assertEquals(TFO_NORMKOSTEN_PRIMARSTUFE_SCHULZEIT, normkosten);
	}

	@Test
	void getVermittlungsKostenMustReturnZeroIfZeroAnwesenheitsTageProMonat() {
		// given
		var testee = new TagesfamilienSchwyzRechner();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		input.setAnwesenheitsTageProMonat(BigDecimal.ZERO);
		var parameter = TestUtils.getRechnerParamterSchwyz();

		// when
		var vermittlungsKosten = testee.getVermittlungsKosten(input, parameter);

		// then
		assertThat(vermittlungsKosten, Matchers.is(BigDecimal.ZERO));
	}

	@Test
	void calculateTagesTarifMustReturnZeroIfZeroBetreuungsZeiteinheiten() {
		// given
		var testee = new TagesfamilienSchwyzRechner();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();

		// when
		var tagesTarif = testee.calculateTagesTarif(BigDecimal.ZERO, input);

		// then
		assertThat(tagesTarif, Matchers.is(BigDecimal.ZERO));
	}

}
