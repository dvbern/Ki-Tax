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

import ch.dvbern.ebegu.entities.BelegungTagesschuleModul;
import ch.dvbern.ebegu.entities.ModulTagesschule;
import ch.dvbern.ebegu.entities.ModulTagesschuleGroup;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.util.MathUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test fuer TagesschuleTarifRechner
 */
public class TagesschuleRechnerTest {

	private final BigDecimal MATA_MIT_PEDAGOGISCHE_BETREUUNG = MathUtil.DEFAULT.fromNullSafe(12.24);
	private final BigDecimal MATA_OHNE_PEDAGOGISCHE_BETREUUNG = MathUtil.DEFAULT.fromNullSafe(6.11);
	private final BigDecimal MITA = MathUtil.DEFAULT.fromNullSafe(0.78);
	private final BigDecimal MAXIMAL_MASSGEGEBENES_EINKOMMEN = MathUtil.DEFAULT.fromNullSafe(160000.00);
	private final BigDecimal MINIMAL_MASSGEGEBENES_EINKOMMEN = MathUtil.DEFAULT.fromNullSafe(43000.00);

	private TagesschuleRechner tarifRechner = new TagesschuleRechner();
	private TagesschuleRechnerParameterDTO parameterDTO;


	@Before
	public void setUp() {
		parameterDTO = getTagesschuleTarifRechnerParameterDTO();
	}

	@Test
	public void minimalTarif() {
		doTestWithFamilienGroesse2(10000, true, 0.78);
		doTestWithFamilienGroesse3(10000, true, 0.78);
		doTestWithFamilienGroesse2(37000, true, 0.78);
		doTestWithFamilienGroesse3(37000, true, 0.78);
		doTestWithFamilienGroesse2(10000, false, 0.78);
		doTestWithFamilienGroesse3(10000, false, 0.78);
		doTestWithFamilienGroesse2(37000, false, 0.78);
		doTestWithFamilienGroesse3(37000, false, 0.78);
	}

	@Test
	public void grenzeMittendrinn() {
		doTestWithFamilienGroesse2(67000, true, 3.13);
		doTestWithFamilienGroesse3(67000, true, 2.01);
		doTestWithFamilienGroesse2(67100, true, 3.14);
		doTestWithFamilienGroesse3(67100, true, 2.02);
		doTestWithFamilienGroesse2(67000, false, 1.87);
		doTestWithFamilienGroesse3(67000, false, 1.35);
		doTestWithFamilienGroesse2(67100, false, 1.88);
		doTestWithFamilienGroesse3(67100, false, 1.36);
	}

	@Test
	public void maximalTarif() {
		doTestWithFamilienGroesse2(150000, true, 11.26);
		doTestWithFamilienGroesse2(160000, true, 12.24);
		doTestWithFamilienGroesse2(1600000, true, 12.24);
		doTestWithFamilienGroesse2(150000, false, 5.65);
		doTestWithFamilienGroesse2(160000, false, 6.11);
		doTestWithFamilienGroesse2(1600000, false, 6.11);
		doTestWithFamilienGroesse3(167000, true, 11.81);
		doTestWithFamilienGroesse3(1600000, true, 12.24);
		doTestWithFamilienGroesse3(1600000, false, 6.11);
	}

	private void doTestWithFamilienGroesse2(double einkommen, boolean paedagogischBetreut, double expectedTarif) {
		VerfuegungZeitabschnitt verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		verfuegungZeitabschnitt.setMassgebendesEinkommenVorAbzugFamgr(MathUtil.DEFAULT.fromNullSafe(einkommen));
		verfuegungZeitabschnitt.setAbzugFamGroesse(BigDecimal.ZERO);
		BigDecimal calculatedTarif = tarifRechner.calculateTarif(verfuegungZeitabschnitt, parameterDTO, paedagogischBetreut);

		Assert.assertEquals(MathUtil.DEFAULT.fromNullSafe(expectedTarif), calculatedTarif);
	}

	private void doTestWithFamilienGroesse3(double einkommen, boolean paedagogischBetreut, double expectedTarif) {
		VerfuegungZeitabschnitt verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		verfuegungZeitabschnitt.setMassgebendesEinkommenVorAbzugFamgr(MathUtil.DEFAULT.fromNullSafe(einkommen));
		verfuegungZeitabschnitt.setAbzugFamGroesse(MathUtil.DEFAULT.fromNullSafe(11400.00));
		BigDecimal calculatedTarif = tarifRechner.calculateTarif(verfuegungZeitabschnitt, parameterDTO, paedagogischBetreut);

		Assert.assertEquals(MathUtil.DEFAULT.fromNullSafe(expectedTarif), calculatedTarif);
	}

	private TagesschuleRechnerParameterDTO getTagesschuleTarifRechnerParameterDTO() {
		TagesschuleRechnerParameterDTO dto = new TagesschuleRechnerParameterDTO();
		dto.setMaxMassgebendesEinkommen(MAXIMAL_MASSGEGEBENES_EINKOMMEN);
		dto.setMinMassgebendesEinkommen(MINIMAL_MASSGEGEBENES_EINKOMMEN);
		dto.setMaxTarifMitPaedagogischerBetreuung(MATA_MIT_PEDAGOGISCHE_BETREUUNG);
		dto.setMaxTarifOhnePaedagogischerBetreuung(MATA_OHNE_PEDAGOGISCHE_BETREUUNG);
		dto.setMinTarif(MITA);
		return dto;
	}
}
