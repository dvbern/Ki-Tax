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
import org.junit.jupiter.api.Test;

import static ch.dvbern.ebegu.rechner.KitaTagestrukturenSchwyzRechner.NORMKOSTEN_PRIMARSTUFE_WAEHREND_SCHULFREIEN_ZEIT;
import static ch.dvbern.ebegu.rechner.KitaTagestrukturenSchwyzRechner.NORMKOSTEN_PRIMARSTUFE_WAEHREND_SCHULZEIT;
import static ch.dvbern.ebegu.util.MathUtil.EXACT;
import static org.junit.jupiter.api.Assertions.assertEquals;

class KitaTagestrukturenSchwyzRechnerTest {

	@Test
	void testKindEingeschultBetreuungWaehrendSchulzeit() {
		// given
		var testee = new KitaTagestrukturenSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		setGueltigkeitGanzerApril(verfuegungZeitabschnitt);

		// TODO mobj input type correct?
		var input = verfuegungZeitabschnitt.getBgCalculationInputAsiv();
		setDefaultInputs(input);

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then
		var result = verfuegungZeitabschnitt.getBgCalculationResultAsiv();
		checkMappedInputs(input, result);
		assertEquals(
			new BigDecimal("4000.00"),
			result.getVollkosten());
		assertEquals(
			new BigDecimal("750.40"),
			result.getVerguenstigungOhneBeruecksichtigungVollkosten());
		assertEquals(
			new BigDecimal("574.00"),
			result.getVerguenstigungOhneBeruecksichtigungMinimalbeitrag());
		assertEquals(
			new BigDecimal("10.75"),
			result.getMinimalerElternbeitrag());
	}

	@Test
	void testKindEingeschultBetreuungWaehrendSchulzeitOhneGeschwister() {
		// given
		var testee = new KitaTagestrukturenSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		setGueltigkeitGanzerApril(verfuegungZeitabschnitt);

		// TODO mobj input type correct?
		var input = verfuegungZeitabschnitt.getBgCalculationInputAsiv();
		setDefaultInputs(input);
		input.setAnzahlGeschwister(0);

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then
		var result = verfuegungZeitabschnitt.getBgCalculationResultAsiv();
		checkMappedInputs(input, result);
		assertEquals(
			new BigDecimal("4000.00"),
			result.getVollkosten());
		assertEquals(
			new BigDecimal("558.80"),
			result.getVerguenstigungOhneBeruecksichtigungVollkosten());
		assertEquals(
			new BigDecimal("558.80"),
			result.getVerguenstigungOhneBeruecksichtigungMinimalbeitrag());
		assertEquals(
			new BigDecimal("0.00"),
			result.getMinimalerElternbeitrag());
	}

	@Test
	void testKindEingeschultBetreuungWaehrendSchulzeitHalberMonat() {
		// given
		var testee = new KitaTagestrukturenSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		verfuegungZeitabschnitt.setGueltigkeit(new DateRange(
			LocalDate.of(2024, Month.APRIL, 16),
			LocalDate.of(2024, Month.APRIL, 30)));

		// TODO mobj input type correct?
		var input = verfuegungZeitabschnitt.getBgCalculationInputAsiv();
		setDefaultInputs(input);

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then
		var result = verfuegungZeitabschnitt.getBgCalculationResultAsiv();
		checkMappedInputs(input, result);
		assertEquals(
			new BigDecimal("2000.00"),
			result.getVollkosten());
		assertEquals(
			new BigDecimal("375.20"),
			result.getVerguenstigungOhneBeruecksichtigungVollkosten());
		assertEquals(
			new BigDecimal("287.00"),
			result.getVerguenstigungOhneBeruecksichtigungMinimalbeitrag());
		assertEquals(
			new BigDecimal("10.75"),
			result.getMinimalerElternbeitrag());
	}

	@Test
	void testKindNichtEingeschultKeinBabyTarif() {
		// given
		var testee = new KitaTagestrukturenSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		setGueltigkeitGanzerApril(verfuegungZeitabschnitt);

		// TODO mobj input type correct?
		var input = verfuegungZeitabschnitt.getBgCalculationInputAsiv();
		setDefaultInputs(input);
		input.setEinschulungTyp(null);
		input.setBetreuungWaehrendSchulzeit(false);

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then
		var result = verfuegungZeitabschnitt.getBgCalculationResultAsiv();
		checkMappedInputs(input, result);
		assertEquals(
			new BigDecimal("4000.00"),
			result.getVollkosten());
		assertEquals(
			new BigDecimal("1788.15"),
			result.getVerguenstigungOhneBeruecksichtigungVollkosten());
		assertEquals(
			new BigDecimal("1640.00"),
			result.getVerguenstigungOhneBeruecksichtigungMinimalbeitrag());
		assertEquals(
			new BigDecimal("9.05"),
			result.getMinimalerElternbeitrag());
	}

	@Test
	void testMassgebendesEinkommenNaheObergrenze() {
		// given
		var testee = new KitaTagestrukturenSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		setGueltigkeitGanzerApril(verfuegungZeitabschnitt);

		// TODO mobj input type correct?
		var input = verfuegungZeitabschnitt.getBgCalculationInputAsiv();
		setDefaultInputs(input);
		input.setEinschulungTyp(null);
		input.setBetreuungWaehrendSchulzeit(false);
		input
			.setMassgebendesEinkommenVorAbzugFamgr(parameter.getMaxMassgebendesEinkommen().subtract(BigDecimal.TEN));

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then
		var result = verfuegungZeitabschnitt.getBgCalculationResultAsiv();
		checkMappedInputs(input, result);
		assertEquals(
			new BigDecimal("4000.00"),
			result.getVollkosten());
		assertEquals(
			new BigDecimal("0.20"),
			result.getVerguenstigungOhneBeruecksichtigungVollkosten());
		assertEquals(
			new BigDecimal("0.20"),
			result.getVerguenstigungOhneBeruecksichtigungMinimalbeitrag());
		assertEquals(
			new BigDecimal("0.00"),
			result.getMinimalerElternbeitrag());
	}

	@Test
	void testMassgebendesEinkommenNaheUntergrenze() {
		// given
		var testee = new KitaTagestrukturenSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		setGueltigkeitGanzerApril(verfuegungZeitabschnitt);

		// TODO mobj input type correct?
		var input = verfuegungZeitabschnitt.getBgCalculationInputAsiv();
		setDefaultInputs(input);

		input.setEinschulungTyp(null);
		input.setBetreuungWaehrendSchulzeit(false);
		input
			.setMassgebendesEinkommenVorAbzugFamgr(parameter.getMinMassgebendesEinkommen().add(BigDecimal.TEN));

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then
		var result = verfuegungZeitabschnitt.getBgCalculationResultAsiv();
		checkMappedInputs(input, result);
		assertEquals(
			new BigDecimal("4000.00"),
			result.getVollkosten());
		assertEquals(
			new BigDecimal("1836.65"),
			result.getVerguenstigungOhneBeruecksichtigungVollkosten());
		assertEquals(
			new BigDecimal("1640.00"),
			result.getVerguenstigungOhneBeruecksichtigungMinimalbeitrag());
		assertEquals(
			new BigDecimal("12.00"),
			result.getMinimalerElternbeitrag());
	}

	@Test
	void testTagestarifTieferAlsNormkosten() {
		// given
		var testee = new KitaTagestrukturenSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		setGueltigkeitGanzerApril(verfuegungZeitabschnitt);
		// TODO mobj input type correct?

		var input = verfuegungZeitabschnitt.getBgCalculationInputAsiv();
		setDefaultInputs(input);
		input.setEinschulungTyp(null);
		input.setBetreuungWaehrendSchulzeit(false);
		input.setMonatlicheBetreuungskosten(new BigDecimal(2000));

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then
		var result = verfuegungZeitabschnitt.getBgCalculationResultAsiv();
		checkMappedInputs(input, result);
		assertEquals(
			new BigDecimal("2000.00"),
			result.getVollkosten());
		assertEquals(
			new BigDecimal("1708.35"),
			result.getVerguenstigungOhneBeruecksichtigungVollkosten());
		assertEquals(
			new BigDecimal("1558.00"),
			result.getVerguenstigungOhneBeruecksichtigungMinimalbeitrag());
		assertEquals(
			new BigDecimal("9.15"),
			result.getMinimalerElternbeitrag());
	}

	private void setGueltigkeitGanzerApril(VerfuegungZeitabschnitt verfuegungZeitabschnitt) {
		verfuegungZeitabschnitt.setGueltigkeit(new DateRange(
			LocalDate.of(2024, Month.APRIL, 1),
			LocalDate.of(2024, Month.APRIL, 30)));
	}

	private void setDefaultInputs(BGCalculationInput bgCalculationInputAsiv) {
		bgCalculationInputAsiv.setBabyTarif(false);
		bgCalculationInputAsiv.setEinschulungTyp(EinschulungTyp.KLASSE1);
		bgCalculationInputAsiv.setBetreuungWaehrendSchulzeit(true);
		bgCalculationInputAsiv.setAnzahlGeschwister(4);
		bgCalculationInputAsiv.setBetreuungspensumProzent(new BigDecimal(80));
		bgCalculationInputAsiv.setAnspruchspensumProzent(100);
		bgCalculationInputAsiv.setMassgebendesEinkommenVorAbzugFamgr(new BigDecimal(50_000));
		bgCalculationInputAsiv.setMonatlicheBetreuungskosten(new BigDecimal(20 * 200));
	}

	@Test
	void testCalculateNormkostenForBabyTarif() {
		// given
		var testee = new KitaTagestrukturenSchwyzRechner();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		var input = verfuegungZeitabschnitt.getBgCalculationInputAsiv();
		input.setBabyTarif(true);

		var parameter = TestUtils.getRechnerParamterSchwyz();

		// when
		var normkosten = testee.calculateNormkosten(input, parameter);

		// then
		assertEquals(parameter.getMaxVerguenstigungVorschuleBabyProTg(), normkosten);
	}

	@Test
	void testCalculateNormkostenIfNotEingeschult() {
		// given
		var testee = new KitaTagestrukturenSchwyzRechner();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		var input = verfuegungZeitabschnitt.getBgCalculationInputAsiv();
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
		var input = verfuegungZeitabschnitt.getBgCalculationInputAsiv();
		input.setEinschulungTyp(EinschulungTyp.KLASSE1);
		input.setBetreuungWaehrendSchulzeit(false);
		var parameter = TestUtils.getRechnerParamterSchwyz();

		// when
		var normkosten = testee.calculateNormkosten(input, parameter);

		// then
		assertEquals(NORMKOSTEN_PRIMARSTUFE_WAEHREND_SCHULFREIEN_ZEIT, normkosten);
	}

	@Test
	void testCalculateNormkostenIfEingeschultAberWaehrendDerSchulzeitBetreut() {
		// given
		var testee = new KitaTagestrukturenSchwyzRechner();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		var input = verfuegungZeitabschnitt.getBgCalculationInputAsiv();
		input.setEinschulungTyp(EinschulungTyp.KLASSE1);
		input.setBetreuungWaehrendSchulzeit(true);
		var parameter = TestUtils.getRechnerParamterSchwyz();

		// when
		var normkosten = testee.calculateNormkosten(input, parameter);

		// then
		assertEquals(NORMKOSTEN_PRIMARSTUFE_WAEHREND_SCHULZEIT, normkosten);
	}

	@Test
	void testCalculateTagesTarif() {
		// given
		var testee = new KitaTagestrukturenSchwyzRechner();

		var parameter = TestUtils.getRechnerParamterSchwyz();
		parameter.setOeffnungstageKita(BigDecimal.valueOf(240));

		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		setGueltigkeitGanzerApril(verfuegungZeitabschnitt);
		// TODO mobj input type correct?
		verfuegungZeitabschnitt.getBgCalculationInputAsiv().setMonatlicheBetreuungskosten(new BigDecimal(2000));

		var pensum = EXACT.pctToFraction(new BigDecimal(80));

		// when
		BigDecimal tagestarif =
			testee.calculateTagesTarif(pensum, parameter, verfuegungZeitabschnitt.getBgCalculationInputAsiv());

		// then
		assertEquals(new BigDecimal("125.0000000000"), tagestarif);
	}

	private void checkMappedInputs(BGCalculationInput input, BGCalculationResult result) {
		assertEquals(MathUtil.ZWEI_NACHKOMMASTELLE.from(input.getBetreuungspensumProzent()), result.getBetreuungspensumProzent());
		assertEquals(MathUtil.ZWEI_NACHKOMMASTELLE.from(input.getAnspruchspensumProzent()), result.getAnspruchspensumZeiteinheit());
		assertEquals(MathUtil.ZWEI_NACHKOMMASTELLE.from(input.getBgPensumProzent()), result.getBgPensumZeiteinheit());
	}
}
