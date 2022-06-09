/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.TSCalculationResult;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.util.MathUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test fuer TagesschuleTarifRechner
 */
public class TagesschuleBernRechnerTest {

	private final BigDecimal MATA_MIT_PEDAGOGISCHE_BETREUUNG = MathUtil.DEFAULT.fromNullSafe(12.24);
	private final BigDecimal MATA_OHNE_PEDAGOGISCHE_BETREUUNG = MathUtil.DEFAULT.fromNullSafe(6.11);
	private final BigDecimal MITA = MathUtil.DEFAULT.fromNullSafe(0.78);
	private final BigDecimal MAXIMAL_MASSGEGEBENES_EINKOMMEN = MathUtil.DEFAULT.fromNullSafe(160000.00);
	private final BigDecimal MINIMAL_MASSGEGEBENES_EINKOMMEN = MathUtil.DEFAULT.fromNullSafe(43000.00);

	private TagesschuleBernRechner tarifRechner = new TagesschuleBernRechner(Collections.emptyList());
	private BGRechnerParameterDTO parameterDTO;


	@Before
	public void setUp() {
		parameterDTO = getTagesschuleTarifRechnerParameterDTO();
	}

	@Test
	public void minimalTarif() {
		doTest(2, 10000, true, 0.78);
		doTest(3, 10000, true, 0.78);
		doTest(2, 37000, true, 0.78);
		doTest(3, 37000, true, 0.78);
		doTest(2, 10000, false, 0.78);
		doTest(3, 10000, false, 0.78);
		doTest(2, 37000, false, 0.78);
		doTest(3, 37000, false, 0.78);
	}

	@Test
	public void grenzeMittendrinn() {
		doTest(2, 67000, true, 3.13);
		doTest(3, 67000, true, 2.01);
		doTest(2, 67100, true, 3.14);
		doTest(3, 67100, true, 2.02);
		doTest(2, 67000, false, 1.87);
		doTest(3, 67000, false, 1.35);
		doTest(2, 67100, false, 1.88);
		doTest(3, 67100, false, 1.36);
	}

	@Test
	public void maximalTarif() {
		doTest(2, 150000, true, 11.26);
		doTest(2, 160000, true, 12.24);
		doTest(2, 1600000, true, 12.24);
		doTest(2, 150000, false, 5.65);
		doTest(2, 160000, false, 6.11);
		doTest(2, 1600000, false, 6.11);
		doTest(3, 167000, true, 11.81);
		doTest(3, 1600000, true, 12.24);
		doTest(3, 1600000, false, 6.11);
	}

	private void doTest(int familiengroesse, double einkommen, boolean paedagogischBetreut, double expectedTarif) {
		BigDecimal abzugFamiliengroesse = BigDecimal.ZERO;
		if (familiengroesse == 3) {
			abzugFamiliengroesse = MathUtil.DEFAULT.fromNullSafe(11400.00);
		}
		VerfuegungZeitabschnitt verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		BGCalculationInput inputAsiv = verfuegungZeitabschnitt.getBgCalculationInputAsiv();
		inputAsiv.setMassgebendesEinkommenVorAbzugFamgr(MathUtil.DEFAULT.fromNullSafe(einkommen));
		inputAsiv.setAbzugFamGroesse(abzugFamiliengroesse);
		inputAsiv.setAnspruchspensumProzent(100);
		if (paedagogischBetreut) {
			verfuegungZeitabschnitt.setTsBetreuungszeitProWocheMitBetreuungForAsivAndGemeinde(10);
		} else {
			verfuegungZeitabschnitt.setTsBetreuungszeitProWocheOhneBetreuungForAsivAndGemeinde(10);
		}

		verfuegungZeitabschnitt.initBGCalculationResult();
		BGCalculationResult calculationResult = tarifRechner.calculateAsiv(inputAsiv, parameterDTO);
		TSCalculationResult tsResult;
		if (paedagogischBetreut) {
			tsResult = calculationResult.getTsCalculationResultMitPaedagogischerBetreuung();
		} else {
			tsResult = calculationResult.getTsCalculationResultOhnePaedagogischerBetreuung();
		}
		Assert.assertNotNull(tsResult);
		Assert.assertEquals(MathUtil.DEFAULT.fromNullSafe(expectedTarif), tsResult.getGebuehrProStunde());
	}

	private BGRechnerParameterDTO getTagesschuleTarifRechnerParameterDTO() {
		BGRechnerParameterDTO dto = new BGRechnerParameterDTO();
		dto.setMaxMassgebendesEinkommen(MAXIMAL_MASSGEGEBENES_EINKOMMEN);
		dto.setMinMassgebendesEinkommen(MINIMAL_MASSGEGEBENES_EINKOMMEN);
		dto.setMaxTarifTagesschuleMitPaedagogischerBetreuung(MATA_MIT_PEDAGOGISCHE_BETREUUNG);
		dto.setMaxTarifTagesschuleOhnePaedagogischerBetreuung(MATA_OHNE_PEDAGOGISCHE_BETREUUNG);
		dto.setMinTarifTagesschule(MITA);
		return dto;
	}
}
