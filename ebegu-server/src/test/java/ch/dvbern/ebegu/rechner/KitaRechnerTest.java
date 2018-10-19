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

import java.time.LocalDate;
import java.time.Month;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.MathUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * Testet den Kita-Rechner
 */
public class KitaRechnerTest extends AbstractBGRechnerTest {

	private final BGRechnerParameterDTO parameterDTO = getParameter();
	private final KitaRechner kitaRechner = new KitaRechner();

	private final LocalDate geburtstagBaby = LocalDate.of(2018, Month.OCTOBER, 15);
	private final LocalDate geburtstagKind = LocalDate.of(2016, Month.OCTOBER, 15);

	private final DateRange intervall = new DateRange(
		LocalDate.of(2019, Month.FEBRUARY, 10),
		LocalDate.of(2019, Month.FEBRUARY, 20));

	private final DateRange intervallTag = new DateRange(
		LocalDate.of(2019, Month.FEBRUARY, 10),
		LocalDate.of(2019, Month.FEBRUARY, 10));


	@Test
	public void test() {
		testWithParams(geburtstagBaby, false, false, intervall.getGueltigAb(), intervall.getGueltigBis(),
			100000, 112.80);

		testWithParams(geburtstagKind, true, false, intervall.getGueltigAb(), intervall.getGueltigBis(),
			100000, 60.45);

		testWithParams(geburtstagKind, false, false, intervall.getGueltigAb(), intervall.getGueltigBis(),
			50000, 147.75);


		testWithParams(geburtstagKind, false, false, intervallTag.getGueltigAb(), intervallTag.getGueltigBis(),
			100000, 7.35);

		testWithParams(geburtstagKind, true, false, intervallTag.getGueltigAb(), intervallTag.getGueltigBis(),
			100000, 5.50);

		testWithParams(geburtstagKind, false, true, intervallTag.getGueltigAb(), intervallTag.getGueltigBis(),
			100000, 14.45);

		testWithParams(geburtstagKind, true, true, intervallTag.getGueltigAb(), intervallTag.getGueltigBis(),
			100000, 12.65);


		testWithParams(geburtstagKind, false, false, intervall.getGueltigAb(), intervall.getGueltigBis(),
			150000, 13.45);

		testWithParams(geburtstagKind, true, false, intervall.getGueltigAb(), intervall.getGueltigBis(),
			150000, 10.05);

		testWithParams(geburtstagKind, false, true, intervall.getGueltigAb(), intervall.getGueltigBis(),
			150000, 92.00);

		testWithParams(geburtstagKind, true, true, intervall.getGueltigAb(), intervall.getGueltigBis(),
			150000, 88.65);

	}

	private void testWithParams(
		@Nonnull LocalDate geburtstag,
		boolean eingeschult,
		boolean besondereBeduerfnisse,
		@Nonnull LocalDate von,
		@Nonnull LocalDate bis,
		int einkommen,
		double expected
	) {
		Verfuegung verfuegung = prepareVerfuegungKita(geburtstag, von, bis, eingeschult, besondereBeduerfnisse,
			20, MathUtil.DEFAULT.fromNullSafe(einkommen), MathUtil.DEFAULT.fromNullSafe(2000));

		VerfuegungZeitabschnitt calculate = kitaRechner.calculate(verfuegung.getZeitabschnitte().get(0), verfuegung, parameterDTO);
		Assert.assertEquals(MathUtil.DEFAULT.from(expected), calculate.getVerguenstigung());
	}
}
