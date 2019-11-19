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
import ch.dvbern.ebegu.enums.PensumUnits;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.MathUtil;
import org.junit.Test;

import static com.spotify.hamcrest.pojo.IsPojo.pojo;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Testet den Tageseltern-Rechner
 */
public class TageselternRechnerTest extends AbstractBGRechnerTest {

	private final BGRechnerParameterDTO parameterDTO = getParameter();
	private final TageselternRechner tageselternRechner = new TageselternRechner();

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
		testWithParams(geburtstagBaby, false, false, false, intervall, 20, 100000, 113.00);
		testWithParams(geburtstagKind, true, false, false, intervall, 20, 100000, 75.35);
		testWithParams(geburtstagKind, false, false, false, intervall, 20, 50000, 138.15);

		testWithParams(geburtstagKind, false, false, false, intervallTag, 20, 100000, 6.85);
		testWithParams(geburtstagKind, true, false, false, intervallTag, 20, 100000, 6.85);
		testWithParams(geburtstagKind, false, true, true, intervallTag, 20, 100000, 13.55);
		testWithParams(geburtstagKind, true, true, true, intervallTag, 20, 100000, 13.55);

		testWithParams(geburtstagKind, false, false, false, intervall, 20, 150000, 12.55);
		testWithParams(geburtstagKind, true, false, false, intervall, 20, 150000, 12.55);
		testWithParams(geburtstagKind, false, true, true, intervall, 20, 150000, 86.00);
		testWithParams(geburtstagKind, true, true, true, intervall, 20, 150000, 86.00);

		testWithParams(geburtstagBaby, false, false, false, intervall, 20, 100000, 113.00);
		testWithParams(geburtstagBaby, true, false, false, intervall, 20, 100000, 113.00);
		testWithParams(geburtstagBaby, false, true, true, intervall, 20, 100000, 186.50);
		testWithParams(geburtstagBaby, true, true, true, intervall, 20, 100000, 186.50);

		testWithParams(geburtstagKind, false, false, false, intervall, 20, 100000, 75.35);
		testWithParams(geburtstagKind, true, false, false, intervall, 20, 100000, 75.35);
		testWithParams(geburtstagKind, false, true, true, intervall, 20, 100000, 148.80);
		testWithParams(geburtstagKind, true, true, true, intervall, 20, 100000, 148.80);
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

		testWithParams(kind, false, false, false, halberAugust, 50, 68712, 329.45);
		testWithParams(kind, false, false, false, ganzerSeptember, 50, 68712, 729.50);
		testWithParams(baby, false, false, false, halberAugust, 50, 68712, 494.20);
		testWithParams(baby, false, false, false, ganzerSeptember, 50, 68712, 1094.30);

		testWithParams(baby, false, false, false, halberAugust, 50, 185447, 0.00);
		testWithParams(baby, false, false, false, ganzerSeptember, 50, 185447, 0.00);
		testWithParams(baby, false, true, true, halberAugust, 50, 185447, 211.15);
		testWithParams(baby, false, true, true, ganzerSeptember, 50, 185447, 467.50);

		testWithParams(baby, false, true, true, halberAugust, 50, 35447, 844.50);
		testWithParams(baby, false, true, true, ganzerSeptember, 50, 35447, 1870.00);

		testWithParams(kind, true, false, false, halberAugust, 50, 68712, 329.45);
		testWithParams(kind, true, false, false, ganzerSeptember, 50, 68712, 729.50);
	}

	private void testWithParams(
		@Nonnull LocalDate geburtstag,
		boolean eingeschult,
		boolean besondereBeduerfnisse,
		boolean besondereBeduerfnisseBestaetigt,
		@Nonnull DateRange intervall,
		int anspruch,
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
		verfuegungZeitabschnitt.setBetreuungspensum(MathUtil.DEFAULT.from(anspruch));
		verfuegungZeitabschnitt.setBabyTarif(geburtstag.plusYears(1)
			.isAfter(verfuegungZeitabschnitt.getGueltigkeit().getGueltigBis()));
		verfuegungZeitabschnitt.setEingeschult(eingeschult);
		verfuegungZeitabschnitt.setBesondereBeduerfnisseBestaetigt(besondereBeduerfnisseBestaetigt);

		BGCalculationResult result = tageselternRechner.calculate(verfuegungZeitabschnitt, parameterDTO);

		assertThat(result, pojo(BGCalculationResult.class)
			.withProperty("verguenstigung", equalTo(MathUtil.DEFAULT.from(expected)))
			.withProperty("verfuegteAnzahlZeiteinheiten", IsBigDecimal.greaterZeroWithScale2())
			.withProperty("anspruchsberechtigteAnzahlZeiteinheiten", IsBigDecimal.greaterZeroWithScale2())
			.withProperty("zeiteinheit", is(PensumUnits.HOURS))
		);
	}
}
