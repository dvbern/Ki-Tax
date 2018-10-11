/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.rechner;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;

import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import org.junit.Assert;
import org.junit.Test;

/**
 * Testet den Tageseltern-Rechner
 */
public class TageselternRechnerTest extends AbstractBGRechnerTest {

	private final BGRechnerParameterDTO parameterDTO = getParameter();
	private final TageselternRechner tageselternRechner = new TageselternRechner();

	@Test
	public void testEinTagMonatHohesEinkommenAnspruch15() {
		Verfuegung verfuegung = prepareVerfuegungTagiUndTageseltern(
			LocalDate.of(2016, Month.JANUARY, 21), LocalDate.of(2016, Month.JANUARY, 21),
			15, new BigDecimal("234567"), MONATLICHE_BETREUUNGSKOSTEN);

		VerfuegungZeitabschnitt calculate = tageselternRechner.calculate(verfuegung.getZeitabschnitte().get(0), verfuegung, parameterDTO);
		Assert.assertEquals(new BigDecimal("100.00"), calculate.getVollkosten());
		Assert.assertEquals(new BigDecimal("15.30"), calculate.getElternbeitrag());
		Assert.assertEquals(new BigDecimal("84.70"), calculate.getVerguenstigung());
		Assert.assertEquals(new BigDecimal("1.7"), calculate.getBetreuungsstunden());
	}

	@Test
	public void testTeilmonatMittleresEinkommen() {
		Verfuegung verfuegung = prepareVerfuegungTagiUndTageseltern(
			LocalDate.of(2016, Month.JANUARY, 21), LocalDate.of(2016, Month.JANUARY, 31),
			100, new BigDecimal("87654"), MONATLICHE_BETREUUNGSKOSTEN);

		VerfuegungZeitabschnitt calculate = tageselternRechner.calculate(verfuegung.getZeitabschnitte().get(0), verfuegung, parameterDTO);
		Assert.assertEquals(new BigDecimal("700.00"), calculate.getVollkosten());
		Assert.assertEquals(new BigDecimal("313.05"), calculate.getElternbeitrag());
		Assert.assertEquals(new BigDecimal("386.95"), calculate.getVerguenstigung());
		Assert.assertEquals(new BigDecimal("77.9"), calculate.getBetreuungsstunden());
	}

	@Test
	public void testTeilmonatMittleresEinkommen50() {
		Verfuegung verfuegung = prepareVerfuegungTagiUndTageseltern(
			LocalDate.of(2016, Month.JANUARY, 21), LocalDate.of(2016, Month.JANUARY, 31),
			50, new BigDecimal("87654"), MONATLICHE_BETREUUNGSKOSTEN);

		VerfuegungZeitabschnitt calculate = tageselternRechner.calculate(verfuegung.getZeitabschnitte().get(0), verfuegung, parameterDTO);
		Assert.assertEquals(new BigDecimal("700.00"), calculate.getVollkosten());
		Assert.assertEquals(new BigDecimal("156.55"), calculate.getElternbeitrag());
		Assert.assertEquals(new BigDecimal("543.45"), calculate.getVerguenstigung());
		Assert.assertEquals(new BigDecimal("39.0"), calculate.getBetreuungsstunden());
	}

	@Test
	public void testGanzerMonatZuWenigEinkommen() {
		Verfuegung verfuegung = prepareVerfuegungTagiUndTageseltern(
			LocalDate.of(2016, Month.JANUARY, 1), LocalDate.of(2016, Month.JANUARY, 31),
			100, new BigDecimal("27750"), MONATLICHE_BETREUUNGSKOSTEN);

		VerfuegungZeitabschnitt calculate = tageselternRechner.calculate(verfuegung.getZeitabschnitte().get(0), verfuegung, parameterDTO);
		Assert.assertEquals(new BigDecimal("2100.00"), calculate.getVollkosten());
		Assert.assertEquals(new BigDecimal("175.40"), calculate.getElternbeitrag());
		Assert.assertEquals(new BigDecimal("1924.60"), calculate.getVerguenstigung());
		Assert.assertEquals(new BigDecimal("233.8"), calculate.getBetreuungsstunden());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testZeitraumUeberMonatsende() {
		Verfuegung verfuegung = prepareVerfuegungTagiUndTageseltern(
			LocalDate.of(2016, Month.JANUARY, 10), LocalDate.of(2016, Month.FEBRUARY, 5),
			100, new BigDecimal("27750"), MONATLICHE_BETREUUNGSKOSTEN);

		tageselternRechner.calculate(verfuegung.getZeitabschnitte().get(0), verfuegung, parameterDTO);
	}
}
