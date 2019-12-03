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
		doTest(10000, true, 0.78);
		doTestWithOneKinder(10000, true, 0.78);
		doTest(37000, true, 0.78);
		doTestWithOneKinder(37000, true, 0.78);
		doTest(10000, false, 0.78);
		doTestWithOneKinder(10000, false, 0.78);
		doTest(37000, false, 0.78);
		doTestWithOneKinder(37000, false, 0.78);
	}

	@Test
	public void grenzeMittendrinn() {
		doTest(67000, true, 3.13);
		doTestWithOneKinder(67000, true, 2.01);
		doTest(67100, true, 3.14);
		doTestWithOneKinder(67100, true, 2.02);
		doTest(67000, false, 1.87);
		doTestWithOneKinder(67000, false, 1.35);
		doTest(67100, false, 1.88);
		doTestWithOneKinder(67100, false, 1.36);
	}

	@Test
	public void maximalTarif() {
		doTest(150000, true, 11.26);
		doTest(160000, true, 12.24);
		doTest(1600000, true, 12.24);
		doTest(150000, false, 5.65);
		doTest(160000, false, 6.11);
		doTest(1600000, false, 6.11);
		doTestWithOneKinder(167000, true, 11.81);
		doTestWithOneKinder(1600000, true, 12.24);
		doTestWithOneKinder(1600000, false, 6.11);
	}

	private void doTest(double einkommen, boolean paedagogischBetreut, double expectedTarif) {
		VerfuegungZeitabschnitt verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		verfuegungZeitabschnitt.setMassgebendesEinkommenVorAbzugFamgr(MathUtil.DEFAULT.fromNullSafe(einkommen));
		verfuegungZeitabschnitt.setAbzugFamGroesse(BigDecimal.ZERO);
		BelegungTagesschuleModul modul = new BelegungTagesschuleModul();
		modul.setModulTagesschule(new ModulTagesschule());
		modul.getModulTagesschule().setModulTagesschuleGroup(new ModulTagesschuleGroup());
		modul.getModulTagesschule().getModulTagesschuleGroup().setWirdPaedagogischBetreut(paedagogischBetreut);
		BigDecimal calculatedTarif = tarifRechner.calculateTarif(verfuegungZeitabschnitt, parameterDTO, modul);

		Assert.assertEquals(MathUtil.DEFAULT.fromNullSafe(expectedTarif), calculatedTarif);
	}

	private void doTestWithOneKinder(double einkommen, boolean paedagogischBetreut, double expectedTarif) {
		VerfuegungZeitabschnitt verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		verfuegungZeitabschnitt.setMassgebendesEinkommenVorAbzugFamgr(MathUtil.DEFAULT.fromNullSafe(einkommen));
		verfuegungZeitabschnitt.setAbzugFamGroesse(new BigDecimal(11400.00));
		BelegungTagesschuleModul modul = new BelegungTagesschuleModul();
		modul.setModulTagesschule(new ModulTagesschule());
		modul.getModulTagesschule().setModulTagesschuleGroup(new ModulTagesschuleGroup());
		modul.getModulTagesschule().getModulTagesschuleGroup().setWirdPaedagogischBetreut(paedagogischBetreut);
		BigDecimal calculatedTarif = tarifRechner.calculateTarif(verfuegungZeitabschnitt, parameterDTO, modul);

		Assert.assertEquals(MathUtil.DEFAULT.fromNullSafe(expectedTarif), calculatedTarif);
	}

	private TagesschuleRechnerParameterDTO getTagesschuleTarifRechnerParameterDTO() {
		TagesschuleRechnerParameterDTO dto = new TagesschuleRechnerParameterDTO();
		dto.setMaxMassgebendesEinkommen(MAXIMAL_MASSGEGEBENES_EINKOMMEN);
		dto.setMinMassgebendesEinkommen(MINIMAL_MASSGEGEBENES_EINKOMMEN);
		dto.setMaxTarifMitPaedagogischerBetreuung(MATA_MIT_PEDAGOGISCHE_BETREUUNG);
		dto.setMaxTarifOhnePaedagogischeBetreuung(MATA_OHNE_PEDAGOGISCHE_BETREUUNG);
		dto.setMinTarif(MITA);
		return dto;
	}
}
