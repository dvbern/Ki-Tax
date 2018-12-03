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
		testWithParams(geburtstagBaby, false, false, intervall, 20, 20, 100000, 120.90);
		testWithParams(geburtstagKind, true, false, intervall, 20, 20, 100000, 60.45);
		testWithParams(geburtstagKind, false, false, intervall, 20, 20, 50000, 147.75);

		testWithParams(geburtstagKind, false, false, intervallTag, 20, 20, 100000, 7.35);
		testWithParams(geburtstagKind, true, false, intervallTag, 20, 20, 100000, 5.50);
		testWithParams(geburtstagKind, false, true, intervallTag, 20, 20, 100000, 14.45);
		testWithParams(geburtstagKind, true, true, intervallTag, 20, 20, 100000, 12.65);

		testWithParams(geburtstagKind, false, false, intervall, 20, 20, 150000, 13.45);
		testWithParams(geburtstagKind, true, false, intervall, 20, 20, 150000, 10.05);
		testWithParams(geburtstagKind, false, true, intervall, 20, 20, 150000, 92.00);
		testWithParams(geburtstagKind, true, true, intervall, 20, 20, 150000, 88.65);
	}

	@Test
	public void beispieleAusExcel() {
		LocalDate baby = LocalDate.of(2018, Month.JULY, 23);
		LocalDate kind = LocalDate.of(2014, Month.APRIL, 13);
		DateRange halberAugust = new DateRange(
			LocalDate.of(2018, Month.AUGUST, 18),
			LocalDate.of(2018, Month.AUGUST, 31));
		DateRange ganzerSeptember = new DateRange(
			LocalDate.of(2018, Month.SEPTEMBER, 1),
			LocalDate.of(2018, Month.SEPTEMBER, 30));

		// Normalfall Kind
		testWithParams(kind, false, false, halberAugust, 50, 50, 68712, 352.35);
		testWithParams(kind, false, false, ganzerSeptember, 50, 50, 68712, 780.25);
		testWithParams(baby, false, false, halberAugust, 50, 50, 68712, 528.55); // 528.55
		testWithParams(baby, false, false, ganzerSeptember, 50, 50, 68712, 1170.35); // 1170.35
		// Normalfall Baby
		testWithParams(baby, false, false, halberAugust, 50, 50, 185447, 0.00);
		testWithParams(baby, false, false, ganzerSeptember, 50, 50, 185447, 0.00);
		testWithParams(baby, false, true, halberAugust, 50, 50, 185447, 225.80);
		testWithParams(baby, false, true, ganzerSeptember, 50, 50, 185447, 500.00);
		// Besondere Beduerfnisse
		testWithParams(baby, false, true, halberAugust, 50, 50, 35447, 871.60); // 871.60
		testWithParams(baby, false, true, ganzerSeptember, 50, 50, 35447, 1930.00); // 1930.00
		// Eingeschult
		testWithParams(kind, true, false, halberAugust, 50, 50, 68712, 264.30);
		testWithParams(kind, true, false, ganzerSeptember, 50, 50, 68712, 585.20);
	}

	@Test
	public void vollkostenMuessenAufVerguenstigtesPensumBezogenSein() {
		DateRange ganzerSeptember = new DateRange(
			LocalDate.of(2018, Month.SEPTEMBER, 1),
			LocalDate.of(2018, Month.SEPTEMBER, 30));
		VerfuegungZeitabschnitt verfuegungZeitabschnitt =
			testWithParams(geburtstagKind, false, false, ganzerSeptember, 100, 50, 180607, 0.00);

		Assert.assertEquals(100, verfuegungZeitabschnitt.getBetreuungspensum().intValue());
		Assert.assertEquals(50, verfuegungZeitabschnitt.getBgPensum().intValue());
		Assert.assertEquals(1000, verfuegungZeitabschnitt.getVollkosten().intValue());
	}

	private VerfuegungZeitabschnitt testWithParams(
		@Nonnull LocalDate geburtstag,
		boolean eingeschult,
		boolean besondereBeduerfnisse,
		@Nonnull DateRange intervall,
		int betreuungspensum, int anspruch,
		int einkommen,
		double expected
	) {
		Verfuegung verfuegung = prepareVerfuegungKita(
			geburtstag,
			intervall.getGueltigAb(),
			intervall.getGueltigBis(),
			eingeschult,
			besondereBeduerfnisse,
			MathUtil.DEFAULT.fromNullSafe(einkommen),
			MathUtil.DEFAULT.fromNullSafe(2000));

		VerfuegungZeitabschnitt verfuegungZeitabschnitt = verfuegung.getZeitabschnitte().get(0);
		verfuegungZeitabschnitt.setAnspruchberechtigtesPensum(anspruch);
		verfuegungZeitabschnitt.setBetreuungspensum(MathUtil.DEFAULT.from(betreuungspensum));
		verfuegungZeitabschnitt.setBabyTarif(geburtstag.plusYears(1).isAfter(verfuegungZeitabschnitt.getGueltigkeit().getGueltigBis()));
		verfuegungZeitabschnitt.setEingeschult(eingeschult);
		verfuegungZeitabschnitt.setBesondereBeduerfnisse(besondereBeduerfnisse);

		VerfuegungZeitabschnitt calculate = kitaRechner.calculate(verfuegungZeitabschnitt, parameterDTO);
		Assert.assertEquals(MathUtil.DEFAULT.from(expected), calculate.getVerguenstigung());
		return calculate;
	}
}
