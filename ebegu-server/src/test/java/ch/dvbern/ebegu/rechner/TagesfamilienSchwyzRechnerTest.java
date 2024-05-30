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
import ch.dvbern.ebegu.enums.FinanzielleSituationTyp;
import ch.dvbern.ebegu.enums.betreuung.Bedarfsstufe;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.ebegu.util.TestUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;

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
		input.setEinschulungTyp(EinschulungTyp.PRIMARSTUFE);
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

	// Bug report: KIBON-3555
	@Test
	void mustCalculateTarifProZeiteinheitCorrectlyForUntermonatlicheZeitabschnitte() {
		// given
		var testee = new TagesfamilienSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		verfuegungZeitabschnitt.setGueltigkeit(new DateRange(
			LocalDate.of(2024, Month.AUGUST, 12),
			LocalDate.of(2024, Month.AUGUST, 31)));

		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		input.setAnwesenheitsTageProMonat(BigDecimal.valueOf(10));
		input.setBabyTarif(false);
		input.setEinschulungTyp(null);
		input.setBetreuungInFerienzeit(false);
		input.setMonatlicheBetreuungskosten(new BigDecimal(246));
		input.setBetreuungspensumProzent(new BigDecimal(60));
		input.setAnspruchspensumProzent(60);
		input.setMassgebendesEinkommenVorAbzugFamgr(BigDecimal.valueOf(144_301));
		input.setAbzugFamGroesse(BigDecimal.ZERO);
		input.setAnzahlGeschwister(0);


		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then
		var result = verfuegungZeitabschnitt.getRelevantBgCalculationResult();
		checkMappedInputs(input, result);
		assertEquals(new BigDecimal("79.35"), result.getAnspruchspensumZeiteinheit());
		assertEquals(new BigDecimal("79.35"), result.getBgPensumZeiteinheit());
		assertEquals(new BigDecimal("79.35"), result.getBetreuungspensumZeiteinheit());
		assertEquals(new BigDecimal("158.70"), result.getVollkosten());
		var gutscheinVorAbzugSelbstbehalt = new BigDecimal("9.05");
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungVollkosten());
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungMinimalbeitrag());
		assertEquals(new BigDecimal("0.00"), result.getVerguenstigung());
		assertEquals(
			new BigDecimal("238.05"),
			result.getMinimalerElternbeitrag());
		assertEquals(new BigDecimal("88.40"), result.getMinimalerElternbeitragGekuerzt());
		assertEquals(new BigDecimal("149.65"), result.getElternbeitrag());
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
	void testGetNormkostenOhneVermittlungsGebuehrIfVorschulalter() {
		// given
		var testee = new TagesfamilienSchwyzRechner();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		input.setEinschulungTyp(EinschulungTyp.VORSCHULALTER);
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
		input.setEinschulungTyp(EinschulungTyp.PRIMARSTUFE);
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
		input.setEinschulungTyp(EinschulungTyp.PRIMARSTUFE);
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
	void testCalculateTarifProZeiteinheit() {
		// given
		var testee = new TagesfamilienSchwyzRechner();

		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		verfuegungZeitabschnitt.getRelevantBgCalculationInput().setMonatlicheBetreuungskosten(new BigDecimal(1640));
		var effektivesPensumFaktor = new BigDecimal("0.8");
		var parameter = TestUtils.getRechnerParamterSchwyz();

		// when
		BigDecimal tagestarif =
			testee.calculateTarifProZeiteinheit(parameter, effektivesPensumFaktor, verfuegungZeitabschnitt.getRelevantBgCalculationInput());

		// then
		assertEquals(new BigDecimal("10.0000000000"), tagestarif);
	}

	@Test
	void calculateTarifProZeiteinheitMustReturnZeroIfZeroBetreuungsZeiteinheiten() {
		// given
		var testee = new TagesfamilienSchwyzRechner();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		var parameter = TestUtils.getRechnerParamterSchwyz();

		// when
		var tagesTarif = testee.calculateTarifProZeiteinheit(parameter, BigDecimal.ZERO, input);

		// then
		assertThat(tagesTarif, Matchers.is(BigDecimal.ZERO));
	}

	@Test
	void testHoereBeitragBedarfsstufe_KEINE() {
		// given
		var testee = new TagesfamilienSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		setGueltigkeitGanzerApril(verfuegungZeitabschnitt);

		var input = createBasisInputForBedarfstufeTest(verfuegungZeitabschnitt.getRelevantBgCalculationInput());
		input.setBedarfsstufe(Bedarfsstufe.KEINE);

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then
		var result = verfuegungZeitabschnitt.getRelevantBgCalculationResult();
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

	@ParameterizedTest
	@EnumSource(value = Bedarfsstufe.class,
		names = { "KEINE" },
		mode = Mode.EXCLUDE)
	void testHoereBeitragBedarfsstufe_NurGutscheinErhoert(Bedarfsstufe bedarfsstufe) {
		// given
		var testee = new TagesfamilienSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		setGueltigkeitGanzerApril(verfuegungZeitabschnitt);

		var input = createBasisInputForBedarfstufeTest(verfuegungZeitabschnitt.getRelevantBgCalculationInput());
		input.setBedarfsstufe(bedarfsstufe);

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// es hat keine Einfluss auf andere Werten als der Gutschein
		var result = verfuegungZeitabschnitt.getRelevantBgCalculationResult();
		var gutscheinVorAbzugSelbstbehalt = new BigDecimal("223.35");
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungVollkosten());
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungMinimalbeitrag());
		assertEquals(new BigDecimal("246.00"), result.getMinimalerElternbeitrag());
		assertEquals(new BigDecimal("134.15"), result.getMinimalerElternbeitragGekuerzt());
		assertEquals(new BigDecimal("111.85"), result.getElternbeitrag());
	}

	@Test
	void testHoereBeitragBedarfsstufe_BEDARFSSTUFE_1() {
		// given
		var testee = new TagesfamilienSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		setGueltigkeitGanzerApril(verfuegungZeitabschnitt);

		var input = createBasisInputForBedarfstufeTest(verfuegungZeitabschnitt.getRelevantBgCalculationInput());
		input.setBedarfsstufe(Bedarfsstufe.BEDARFSSTUFE_1);

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then 89.20 + 352 =
		var result = verfuegungZeitabschnitt.getRelevantBgCalculationResult();
		assertEquals(new BigDecimal("441.20"), result.getVerguenstigung());
	}

	@Test
	void testHoereBeitragBedarfsstufe_BEDARFSSTUFE_1_untermonatlich() {
		// given
		var testee = new TagesfamilienSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		verfuegungZeitabschnitt.setGueltigkeit(new DateRange(
			LocalDate.of(2024, Month.APRIL, 1),
			LocalDate.of(2024, Month.APRIL, 15)));

		var input = createBasisInputForBedarfstufeTest(verfuegungZeitabschnitt.getRelevantBgCalculationInput());
		input.setBedarfsstufe(Bedarfsstufe.BEDARFSSTUFE_1);

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then, AnteilMonat 0.5 => (89.20 * 0.5) + (352 * 0.5) =
		var result = verfuegungZeitabschnitt.getRelevantBgCalculationResult();
		assertEquals(new BigDecimal("220.60"), result.getVerguenstigung());
	}

	@Test
	void testHoereBeitragBedarfsstufe_BEDARFSSTUFE_2() {
		// given
		var testee = new TagesfamilienSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		setGueltigkeitGanzerApril(verfuegungZeitabschnitt);

		var input = createBasisInputForBedarfstufeTest(verfuegungZeitabschnitt.getRelevantBgCalculationInput());
		input.setBedarfsstufe(Bedarfsstufe.BEDARFSSTUFE_2);

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then 89.20 + 352 + (8.2 * 66) =
		var result = verfuegungZeitabschnitt.getRelevantBgCalculationResult();
		assertEquals(new BigDecimal("982.40"), result.getVerguenstigung());
	}

	@Test
	void testHoereBeitragBedarfsstufe_BEDARFSSTUFE_2_untermonatlich() {
		// given
		var testee = new TagesfamilienSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		verfuegungZeitabschnitt.setGueltigkeit(new DateRange(
			LocalDate.of(2024, Month.APRIL, 1),
			LocalDate.of(2024, Month.APRIL, 15)));

		var input = createBasisInputForBedarfstufeTest(verfuegungZeitabschnitt.getRelevantBgCalculationInput());
		input.setBedarfsstufe(Bedarfsstufe.BEDARFSSTUFE_2);

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then, AnteilMonat 0.5 => (89.20 * 0.5) + (352 * 0.5) + (4.1 * 66) =
		var result = verfuegungZeitabschnitt.getRelevantBgCalculationResult();
		assertEquals(new BigDecimal("491.20"), result.getVerguenstigung());
	}

	@Test
	void testHoereBeitragBedarfsstufe_BEDARFSSTUFE_3() {
		// given
		var testee = new TagesfamilienSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		setGueltigkeitGanzerApril(verfuegungZeitabschnitt);

		var input = createBasisInputForBedarfstufeTest(verfuegungZeitabschnitt.getRelevantBgCalculationInput());
		input.setBedarfsstufe(Bedarfsstufe.BEDARFSSTUFE_3);

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then 89.20 + 352 + (8.2 * 132) =
		var result = verfuegungZeitabschnitt.getRelevantBgCalculationResult();
		assertEquals(new BigDecimal("1523.60"), result.getVerguenstigung());
	}

	@Test
	void testHoereBeitragBedarfsstufe_BEDARFSSTUFE_3_untermonatlich() {
		// given
		var testee = new TagesfamilienSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		verfuegungZeitabschnitt.setGueltigkeit(new DateRange(
			LocalDate.of(2024, Month.APRIL, 1),
			LocalDate.of(2024, Month.APRIL, 15)));

		var input = createBasisInputForBedarfstufeTest(verfuegungZeitabschnitt.getRelevantBgCalculationInput());
		input.setBedarfsstufe(Bedarfsstufe.BEDARFSSTUFE_3);

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then, AnteilMonat 0.5 => (89.20 * 0.5) + (352 * 0.5) + (4.1 * 132) =
		var result = verfuegungZeitabschnitt.getRelevantBgCalculationResult();
		assertEquals(new BigDecimal("761.80"), result.getVerguenstigung());
	}

	private BGCalculationInput createBasisInputForBedarfstufeTest(BGCalculationInput input) {
		input.setAnwesenheitsTageProMonat(BigDecimal.valueOf(10));
		input.setBabyTarif(false);
		input.setEinschulungTyp(EinschulungTyp.PRIMARSTUFE);
		input.setBetreuungInFerienzeit(false);
		input.setAnzahlGeschwister(4);
		input.setBetreuungspensumProzent(new BigDecimal(40));
		input.setAnspruchspensumProzent(60);
		input.setMassgebendesEinkommenVorAbzugFamgr(new BigDecimal(27_000));
		input.setAbzugFamGroesse(BigDecimal.ZERO);
		input.setMonatlicheBetreuungskosten(new BigDecimal(1_500));
		return input;
	}
}
