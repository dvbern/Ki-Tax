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
import ch.dvbern.ebegu.enums.betreuung.Bedarfsstufe;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.ebegu.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;

import static ch.dvbern.ebegu.rechner.KitaTagestrukturenSchwyzRechner.KITA_NORMKOSTEN_PRIMARSTUFE_SCHULFREIEN_ZEIT;
import static ch.dvbern.ebegu.rechner.KitaTagestrukturenSchwyzRechner.KITA_NORMKOSTEN_PRIMARSTUFE_SCHULZEIT;
import static org.junit.jupiter.api.Assertions.assertEquals;

class KitaTagestrukturenSchwyzRechnerTest {

	public static final BigDecimal TAGE_100_PROZENT_PENSUM = new BigDecimal("20.50");
	public static final BigDecimal TAGE_80_PROZENT_PENSUM = new BigDecimal("16.40");
	public static final BigDecimal VOLLKOSTEN = new BigDecimal("4000.00");
	public static final BigDecimal MINIMALER_ELTERNBEITRAG = new BigDecimal("492.00");

	@Test
	void testKind1() {
		// given
		var testee = new KitaTagestrukturenSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		setGueltigkeitGanzerApril(verfuegungZeitabschnitt);

		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		setDefaultInputs(input);

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then
		var result = verfuegungZeitabschnitt.getRelevantBgCalculationResult();
		checkMappedInputs(input, result);
		assertEquals(TAGE_100_PROZENT_PENSUM, result.getAnspruchspensumZeiteinheit());
		assertEquals(TAGE_80_PROZENT_PENSUM, result.getBgPensumZeiteinheit());
		assertEquals(TAGE_80_PROZENT_PENSUM, result.getBetreuungspensumZeiteinheit());
		assertEquals(
			VOLLKOSTEN,
			result.getVollkosten());
		var gutscheinVorAbzugSelbstbehalt = new BigDecimal("750.40");
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungVollkosten());
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungMinimalbeitrag());
		assertEquals(new BigDecimal("574.00"), result.getVerguenstigung());
		assertEquals(MINIMALER_ELTERNBEITRAG, result.getMinimalerElternbeitrag());
		assertEquals(new BigDecimal("176.40"), result.getMinimalerElternbeitragGekuerzt());
		assertEquals(new BigDecimal("315.60"), result.getElternbeitrag());
	}

	@Test
	void testKind1HalberMonat() {
		// given
		var testee = new KitaTagestrukturenSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		verfuegungZeitabschnitt.setGueltigkeit(new DateRange(
			LocalDate.of(2024, Month.APRIL, 16),
			LocalDate.of(2024, Month.APRIL, 30)));

		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		setDefaultInputs(input);

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then
		var result = verfuegungZeitabschnitt.getRelevantBgCalculationResult();
		checkMappedInputs(input, result);
		assertEquals(new BigDecimal("10.25"), result.getAnspruchspensumZeiteinheit());
		assertEquals(new BigDecimal("8.20"), result.getBgPensumZeiteinheit());
		assertEquals(new BigDecimal("8.20"), result.getBgPensumZeiteinheit());
		assertEquals(
			new BigDecimal("2000.00"),
			result.getVollkosten());
		var gutscheinVorAbzugSelbstbehalt = new BigDecimal("375.20");
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungVollkosten());
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungMinimalbeitrag());
		assertEquals(new BigDecimal("287.00"), result.getVerguenstigung());
		assertEquals(
			new BigDecimal("246.00"),
			result.getMinimalerElternbeitrag());
		assertEquals(new BigDecimal("88.20"), result.getMinimalerElternbeitragGekuerzt());
		assertEquals(new BigDecimal("157.80"), result.getElternbeitrag());
	}

	@Test
	void testKind2() {
		// given
		var testee = new KitaTagestrukturenSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		setGueltigkeitGanzerApril(verfuegungZeitabschnitt);

		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		setDefaultInputs(input);
		input.setAnzahlGeschwister(0);

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then
		var result = verfuegungZeitabschnitt.getRelevantBgCalculationResult();
		checkMappedInputs(input, result);
		assertEquals(TAGE_100_PROZENT_PENSUM, result.getAnspruchspensumZeiteinheit());
		assertEquals(TAGE_80_PROZENT_PENSUM, result.getBgPensumZeiteinheit());
		assertEquals(TAGE_80_PROZENT_PENSUM, result.getBetreuungspensumZeiteinheit());
		assertEquals(
			VOLLKOSTEN,
			result.getVollkosten());
		var gutscheinVorAbzugSelbstbehalt = new BigDecimal("558.80");
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungVollkosten());
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungMinimalbeitrag());
		assertEquals(new BigDecimal("558.80"), result.getVerguenstigung());
		assertEquals(
			MINIMALER_ELTERNBEITRAG,
			result.getMinimalerElternbeitrag());
		assertEquals(new BigDecimal("0.00"), result.getMinimalerElternbeitragGekuerzt());
		assertEquals(new BigDecimal("507.20"), result.getElternbeitrag());
	}

	@Test
	void testKind3() {
		// given
		var testee = new KitaTagestrukturenSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		setGueltigkeitGanzerApril(verfuegungZeitabschnitt);

		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		setDefaultInputs(input);
		input.setAnzahlGeschwister(11);

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then
		var result = verfuegungZeitabschnitt.getRelevantBgCalculationResult();
		checkMappedInputs(input, result);
		assertEquals(TAGE_100_PROZENT_PENSUM, result.getAnspruchspensumZeiteinheit());
		assertEquals(TAGE_80_PROZENT_PENSUM, result.getBgPensumZeiteinheit());
		assertEquals(TAGE_80_PROZENT_PENSUM, result.getBetreuungspensumZeiteinheit());
		assertEquals(
			VOLLKOSTEN,
			result.getVollkosten());
		var gutscheinVorAbzugSelbstbehalt = new BigDecimal("1066.00");
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungVollkosten());
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungMinimalbeitrag());
		assertEquals(new BigDecimal("574.00"), result.getVerguenstigung());
		assertEquals(
			MINIMALER_ELTERNBEITRAG,
			result.getMinimalerElternbeitrag());
		assertEquals(MINIMALER_ELTERNBEITRAG, result.getMinimalerElternbeitragGekuerzt());
		assertEquals(new BigDecimal("0.00"), result.getElternbeitrag());
	}

	@Test
	void testKind4() {
		// given
		var testee = new KitaTagestrukturenSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		setGueltigkeitGanzerApril(verfuegungZeitabschnitt);

		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		setDefaultInputs(input);
		input.setEinschulungTyp(null);
		input.setBetreuungInFerienzeit(false);

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then
		var result = verfuegungZeitabschnitt.getRelevantBgCalculationResult();
		checkMappedInputs(input, result);
		assertEquals(TAGE_100_PROZENT_PENSUM, result.getAnspruchspensumZeiteinheit());
		assertEquals(TAGE_80_PROZENT_PENSUM, result.getBgPensumZeiteinheit());
		assertEquals(TAGE_80_PROZENT_PENSUM, result.getBetreuungspensumZeiteinheit());
		assertEquals(
			VOLLKOSTEN,
			result.getVollkosten());
		var gutscheinVorAbzugSelbstbehalt = new BigDecimal("1788.15");
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungVollkosten());
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungMinimalbeitrag());
		assertEquals(new BigDecimal("1640.00"), result.getVerguenstigung());
		assertEquals(
			MINIMALER_ELTERNBEITRAG,
			result.getMinimalerElternbeitrag());
		assertEquals(new BigDecimal("148.15"), result.getMinimalerElternbeitragGekuerzt());
		assertEquals(new BigDecimal("343.85"), result.getElternbeitrag());
	}

	@Test
	void testKind5_MassgebendesEinkommenNaheObergrenze() {
		// given
		var testee = new KitaTagestrukturenSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		setGueltigkeitGanzerApril(verfuegungZeitabschnitt);

		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		setDefaultInputs(input);
		input.setEinschulungTyp(null);
		input.setBetreuungInFerienzeit(true);
		input
			.setMassgebendesEinkommenVorAbzugFamgr(parameter.getMaxMassgebendesEinkommen().subtract(BigDecimal.TEN));

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then
		var result = verfuegungZeitabschnitt.getRelevantBgCalculationResult();
		checkMappedInputs(input, result);
		assertEquals(TAGE_100_PROZENT_PENSUM, result.getAnspruchspensumZeiteinheit());
		assertEquals(TAGE_80_PROZENT_PENSUM, result.getBgPensumZeiteinheit());
		assertEquals(TAGE_80_PROZENT_PENSUM, result.getBetreuungspensumZeiteinheit());
		assertEquals(
			VOLLKOSTEN,
			result.getVollkosten());
		var gutscheinVorAbzugSelbstbehalt = new BigDecimal("0.20");
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungVollkosten());
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungMinimalbeitrag());
		assertEquals(new BigDecimal("0.20"), result.getVerguenstigung());
		assertEquals(
			MINIMALER_ELTERNBEITRAG,
			result.getMinimalerElternbeitrag());
		assertEquals(new BigDecimal("0.00"), result.getMinimalerElternbeitragGekuerzt());
		assertEquals(new BigDecimal("2131.80"), result.getElternbeitrag());
	}

	@Test
	void testKind6_MassgebendesEinkommenNaheUntergrenze() {
		// given
		var testee = new KitaTagestrukturenSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		setGueltigkeitGanzerApril(verfuegungZeitabschnitt);

		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		setDefaultInputs(input);

		input.setEinschulungTyp(null);
		input.setBetreuungInFerienzeit(true);
		input
			.setMassgebendesEinkommenVorAbzugFamgr(parameter.getMinMassgebendesEinkommen().add(BigDecimal.TEN));

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then
		var result = verfuegungZeitabschnitt.getRelevantBgCalculationResult();
		checkMappedInputs(input, result);
		assertEquals(TAGE_100_PROZENT_PENSUM, result.getAnspruchspensumZeiteinheit());
		assertEquals(TAGE_80_PROZENT_PENSUM, result.getBgPensumZeiteinheit());
		assertEquals(TAGE_80_PROZENT_PENSUM, result.getBetreuungspensumZeiteinheit());
		assertEquals(
			VOLLKOSTEN,
			result.getVollkosten());
		var gutscheinVorAbzugSelbstbehalt = new BigDecimal("1836.65");
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungVollkosten());
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungMinimalbeitrag());
		assertEquals(new BigDecimal("1640.00"), result.getVerguenstigung());
		assertEquals(
			MINIMALER_ELTERNBEITRAG,
			result.getMinimalerElternbeitrag());
		assertEquals(new BigDecimal("196.65"), result.getMinimalerElternbeitragGekuerzt());
		assertEquals(new BigDecimal("295.35"), result.getElternbeitrag());
	}

	@Test
	void testKind7_TagestarifTieferAlsNormkosten() {
		// given
		var testee = new KitaTagestrukturenSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		setGueltigkeitGanzerApril(verfuegungZeitabschnitt);

		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		setDefaultInputs(input);
		input.setEinschulungTyp(null);
		input.setBetreuungInFerienzeit(true);
		input.setMonatlicheBetreuungskosten(new BigDecimal(2000));

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then
		var result = verfuegungZeitabschnitt.getRelevantBgCalculationResult();
		checkMappedInputs(input, result);
		assertEquals(TAGE_100_PROZENT_PENSUM, result.getAnspruchspensumZeiteinheit());
		assertEquals(TAGE_80_PROZENT_PENSUM, result.getBgPensumZeiteinheit());
		assertEquals(TAGE_80_PROZENT_PENSUM, result.getBetreuungspensumZeiteinheit());
		assertEquals(
			new BigDecimal("2000.00"),
			result.getVollkosten());
		var gutscheinVorAbzugSelbstbehalt = new BigDecimal("1677.45");
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungVollkosten());
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungMinimalbeitrag());
		assertEquals(new BigDecimal("1508.00"), result.getVerguenstigung());
		assertEquals(
			MINIMALER_ELTERNBEITRAG,
			result.getMinimalerElternbeitrag());
		assertEquals(new BigDecimal("169.45"), result.getMinimalerElternbeitragGekuerzt());
		assertEquals(new BigDecimal("322.55"), result.getElternbeitrag());
	}

	// Bug report: KIBON-3555
	@Test
	void mustCalculateTarifProZeiteinheitCorrectlyForUntermonatlicheZeitabschnitte() {
		// given
		var testee = new KitaTagestrukturenSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		verfuegungZeitabschnitt.setGueltigkeit(new DateRange(
			LocalDate.of(2024, Month.AUGUST, 12),
			LocalDate.of(2024, Month.AUGUST, 31)));

		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		setDefaultInputs(input);
		input.setEinschulungTyp(null);
		input.setMonatlicheBetreuungskosten(new BigDecimal(246));
		input.setBetreuungspensumProzent(new BigDecimal(60));
		input.setAnspruchspensumProzent(60);
		input.setMassgebendesEinkommenVorAbzugFamgr(BigDecimal.valueOf(144_301));
		input.setAnzahlGeschwister(0);

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then
		var result = verfuegungZeitabschnitt.getRelevantBgCalculationResult();
		checkMappedInputs(input, result);
		assertEquals(BigDecimal.valueOf(7.94), result.getAnspruchspensumZeiteinheit());
		assertEquals(BigDecimal.valueOf(7.94), result.getBgPensumZeiteinheit());
		assertEquals(BigDecimal.valueOf(7.94), result.getBetreuungspensumZeiteinheit());
		assertEquals(
			new BigDecimal("158.70"),
			result.getVollkosten());
		var gutscheinVorAbzugSelbstbehalt = new BigDecimal("10.25");
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
		assertEquals(new BigDecimal("89.60"), result.getMinimalerElternbeitragGekuerzt());
		assertEquals(new BigDecimal("148.45"), result.getElternbeitrag());
	}

	private void setGueltigkeitGanzerApril(VerfuegungZeitabschnitt verfuegungZeitabschnitt) {
		verfuegungZeitabschnitt.setGueltigkeit(new DateRange(
			LocalDate.of(2024, Month.APRIL, 1),
			LocalDate.of(2024, Month.APRIL, 30)));
	}

	private void setDefaultInputs(BGCalculationInput input) {
		input.setBabyTarif(false);
		input.setEinschulungTyp(EinschulungTyp.KLASSE1);
		input.setBetreuungInFerienzeit(false);
		input.setAnzahlGeschwister(4);
		input.setBetreuungspensumProzent(new BigDecimal(80));
		input.setAnspruchspensumProzent(100);
		input.setMassgebendesEinkommenVorAbzugFamgr(new BigDecimal(50_000));
		input.setAbzugFamGroesse(BigDecimal.ZERO);
		input.setMonatlicheBetreuungskosten(new BigDecimal(20 * 200));
	}

	@Test
	void testCalculateNormkostenForBabyTarif() {
		// given
		var testee = new KitaTagestrukturenSchwyzRechner();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		setDefaultInputs(input);
		input.setBabyTarif(true);

		var parameter = TestUtils.getRechnerParamterSchwyz();

		// when
		var normkosten = testee.calculateNormkosten(input, parameter);

		// then
		assertEquals(parameter.getMaxVerguenstigungVorschuleBabyProTg(), normkosten);
	}

	@Test
	void testCalculateNormkostenIfFreiwilligerKindergarten() {
		// given
		var testee = new KitaTagestrukturenSchwyzRechner();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		setDefaultInputs(input);
		input.setEinschulungTyp(EinschulungTyp.FREIWILLIGER_KINDERGARTEN);
		var parameter = TestUtils.getRechnerParamterSchwyz();

		// when
		var normkosten = testee.calculateNormkosten(input, parameter);

		// then
		assertEquals(parameter.getMaxVerguenstigungVorschuleKindProTg(), normkosten);
	}

	@Test
	void testCalculateNormkostenIfNotEingeschult() {
		// given
		var testee = new KitaTagestrukturenSchwyzRechner();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		setDefaultInputs(input);
		input.setEinschulungTyp(null);
		var parameter = TestUtils.getRechnerParamterSchwyz();

		// when
		var normkosten = testee.calculateNormkosten(input, parameter);

		// then
		assertEquals(parameter.getMaxVerguenstigungVorschuleKindProTg(), normkosten);
	}

	@Test
	void testCalculateNormkostenIfEingeschultAberAusserhalbDerSchulzeitBetreut() {
		// given
		var testee = new KitaTagestrukturenSchwyzRechner();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		setDefaultInputs(input);
		input.setEinschulungTyp(EinschulungTyp.KLASSE1);
		input.setBetreuungInFerienzeit(true);
		var parameter = TestUtils.getRechnerParamterSchwyz();

		// when
		var normkosten = testee.calculateNormkosten(input, parameter);

		// then
		assertEquals(KITA_NORMKOSTEN_PRIMARSTUFE_SCHULFREIEN_ZEIT, normkosten);
	}

	@Test
	void testCalculateNormkostenIfEingeschultAberWaehrendDerSchulzeitBetreut() {
		// given
		var testee = new KitaTagestrukturenSchwyzRechner();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		setDefaultInputs(input);
		input.setEinschulungTyp(EinschulungTyp.KLASSE1);
		input.setBetreuungInFerienzeit(false);
		var parameter = TestUtils.getRechnerParamterSchwyz();

		// when
		var normkosten = testee.calculateNormkosten(input, parameter);

		// then
		assertEquals(KITA_NORMKOSTEN_PRIMARSTUFE_SCHULZEIT, normkosten);
	}

	@Test
	void testCalculateTarifProZeiteinheit() {
		// given
		var testee = new KitaTagestrukturenSchwyzRechner();

		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		verfuegungZeitabschnitt.getRelevantBgCalculationInput().setMonatlicheBetreuungskosten(new BigDecimal(1640));
		var effektivesPensumFaktor = new BigDecimal("0.8");
		var parameter = TestUtils.getRechnerParamterSchwyz();

		// when
		BigDecimal tagestarif =
			testee.calculateTarifProZeiteinheit(parameter, effektivesPensumFaktor, verfuegungZeitabschnitt.getRelevantBgCalculationInput());

		// then
		assertEquals(new BigDecimal("100.0000000000"), tagestarif);
	}

	@Test
	void testCalculateTarifProZeiteinheitZeroBetreuungstage() {
		// given
		var testee = new KitaTagestrukturenSchwyzRechner();

		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		var parameter = TestUtils.getRechnerParamterSchwyz();


		// when
		BigDecimal tagestarif =
			testee.calculateTarifProZeiteinheit(parameter, BigDecimal.ZERO, verfuegungZeitabschnitt.getRelevantBgCalculationInput());

		// then
		assertEquals(BigDecimal.ZERO, tagestarif);
	}

	@Test
	void testHoereBeitragBedarfsstufe_KEINE() {
		// given
		var testee = new KitaTagestrukturenSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		setGueltigkeitGanzerApril(verfuegungZeitabschnitt);

		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		setDefaultInputs(input);
		input.setBedarfsstufe(Bedarfsstufe.KEINE);

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then
		var result = verfuegungZeitabschnitt.getRelevantBgCalculationResult();
		assertEquals(new BigDecimal("574.00"), result.getVerguenstigung());
	}

	@ParameterizedTest
	@EnumSource(value = Bedarfsstufe.class,
		names = { "KEINE" },
		mode = Mode.EXCLUDE)
	void testHoereBeitragBedarfsstufe_NurGutscheinErhoert(Bedarfsstufe bedarfsstufe) {
		// given
		var testee = new KitaTagestrukturenSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		setGueltigkeitGanzerApril(verfuegungZeitabschnitt);

		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		setDefaultInputs(input);
		input.setBedarfsstufe(bedarfsstufe);

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then
		var result = verfuegungZeitabschnitt.getRelevantBgCalculationResult();
		var gutscheinVorAbzugSelbstbehalt = new BigDecimal("750.40");
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungVollkosten());
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungMinimalbeitrag());
		assertEquals(MINIMALER_ELTERNBEITRAG, result.getMinimalerElternbeitrag());
		assertEquals(new BigDecimal("176.40"), result.getMinimalerElternbeitragGekuerzt());
		assertEquals(new BigDecimal("315.60"), result.getElternbeitrag());
	}

	@Test
	void testHoereBeitragBedarfsstufe_BEDARFSSTUFE_1() {
		// given
		var testee = new KitaTagestrukturenSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		setGueltigkeitGanzerApril(verfuegungZeitabschnitt);

		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		setDefaultInputs(input);
		input.setBedarfsstufe(Bedarfsstufe.BEDARFSSTUFE_1);

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then 574.00 + 352
		var result = verfuegungZeitabschnitt.getRelevantBgCalculationResult();
		assertEquals(new BigDecimal("926.00"), result.getVerguenstigung());
		assertEquals(new BigDecimal("352.00"), result.getHoehererBeitrag());
		assertEquals(Bedarfsstufe.BEDARFSSTUFE_1, result.getBedarfsstufe());
	}

	@Test
	void testHoereBeitragBedarfsstufe_BEDARFSSTUFE_1_untermonatlich() {
		// given
		var testee = new KitaTagestrukturenSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		verfuegungZeitabschnitt.setGueltigkeit(new DateRange(
			LocalDate.of(2024, Month.APRIL, 1),
			LocalDate.of(2024, Month.APRIL, 15)));

		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		setDefaultInputs(input);
		input.setBedarfsstufe(Bedarfsstufe.BEDARFSSTUFE_1);

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then (574.00 * 0.5) + (352 * 0.5)
		var result = verfuegungZeitabschnitt.getRelevantBgCalculationResult();
		assertEquals(new BigDecimal("463.00"), result.getVerguenstigung());
		assertEquals(new BigDecimal("176.00"), result.getHoehererBeitrag());
		assertEquals(Bedarfsstufe.BEDARFSSTUFE_1, result.getBedarfsstufe());
	}

	@Test
	void testHoereBeitragBedarfsstufe_BEDARFSSTUFE_2() {
		// given
		var testee = new KitaTagestrukturenSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		setGueltigkeitGanzerApril(verfuegungZeitabschnitt);

		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		setDefaultInputs(input);
		input.setBedarfsstufe(Bedarfsstufe.BEDARFSSTUFE_2);

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then 574.00 + 352 + (16.4 * 66)
		var result = verfuegungZeitabschnitt.getRelevantBgCalculationResult();
		assertEquals(new BigDecimal("2008.40"), result.getVerguenstigung());
		assertEquals(new BigDecimal("1434.40"), result.getHoehererBeitrag());
		assertEquals(Bedarfsstufe.BEDARFSSTUFE_2, result.getBedarfsstufe());
	}

	@Test
	void testHoereBeitragBedarfsstufe_BEDARFSSTUFE_2_untermonatlich() {
		// given
		var testee = new KitaTagestrukturenSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		verfuegungZeitabschnitt.setGueltigkeit(new DateRange(
			LocalDate.of(2024, Month.APRIL, 1),
			LocalDate.of(2024, Month.APRIL, 15)));

		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		setDefaultInputs(input);
		input.setBedarfsstufe(Bedarfsstufe.BEDARFSSTUFE_2);

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then (574.00 * 0.5) + (352 * 0.5) + (8.2 * 66)
		var result = verfuegungZeitabschnitt.getRelevantBgCalculationResult();
		assertEquals(new BigDecimal("1004.20"), result.getVerguenstigung());
		assertEquals(new BigDecimal("717.20"), result.getHoehererBeitrag());
		assertEquals(Bedarfsstufe.BEDARFSSTUFE_2, result.getBedarfsstufe());
	}

	@Test
	void testHoereBeitragBedarfsstufe_BEDARFSSTUFE_3() {
		// given
		var testee = new KitaTagestrukturenSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		setGueltigkeitGanzerApril(verfuegungZeitabschnitt);

		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		setDefaultInputs(input);
		input.setBedarfsstufe(Bedarfsstufe.BEDARFSSTUFE_3);

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then 574.00 + 352 + (16.4 * 132)
		var result = verfuegungZeitabschnitt.getRelevantBgCalculationResult();
		assertEquals(new BigDecimal("3090.80"), result.getVerguenstigung());
		assertEquals(new BigDecimal("2516.80"), result.getHoehererBeitrag());
		assertEquals(Bedarfsstufe.BEDARFSSTUFE_3, result.getBedarfsstufe());
	}

	@Test
	void testHoereBeitragBedarfsstufe_BEDARFSSTUFE_3_untermonatlich() {
		// given
		var testee = new KitaTagestrukturenSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		verfuegungZeitabschnitt.setGueltigkeit(new DateRange(
			LocalDate.of(2024, Month.APRIL, 1),
			LocalDate.of(2024, Month.APRIL, 15)));

		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		setDefaultInputs(input);
		input.setBedarfsstufe(Bedarfsstufe.BEDARFSSTUFE_3);

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then (574.00 * 0.5) + (352 * 0.5) + (8.2 * 132)
		var result = verfuegungZeitabschnitt.getRelevantBgCalculationResult();
		assertEquals(new BigDecimal("1545.40"), result.getVerguenstigung());
		assertEquals(new BigDecimal("1258.40"), result.getHoehererBeitrag());
		assertEquals(Bedarfsstufe.BEDARFSSTUFE_3, result.getBedarfsstufe());
	}


	private void checkMappedInputs(BGCalculationInput input, BGCalculationResult result) {
		assertEquals(
			MathUtil.ZWEI_NACHKOMMASTELLE.from(input.getBetreuungspensumProzent()),
			result.getBetreuungspensumProzent());
		assertEquals(input.getAnspruchspensumProzent(), result.getAnspruchspensumProzent());
		assertEquals(MathUtil.ZWEI_NACHKOMMASTELLE.from(input.getBgPensumProzent()), result.getBgPensumProzent());
	}
}
