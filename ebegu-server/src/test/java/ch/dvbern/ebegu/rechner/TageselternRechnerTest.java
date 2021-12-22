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
import java.util.Collections;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.PensumUnits;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.MathUtil;
import org.hamcrest.number.BigDecimalCloseTo;
import org.junit.Test;

import static com.spotify.hamcrest.pojo.IsPojo.pojo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Testet den Tageseltern-Rechner
 */
public class TageselternRechnerTest extends AbstractBGRechnerTest {

	private final BGRechnerParameterDTO parameterDTO = getParameter();
	private final TageselternBernRechner tageselternRechner = new TageselternBernRechner(Collections.emptyList());

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
		testWithParams(geburtstagBaby, EinschulungTyp.VORSCHULALTER, false, false, intervall, 20, 100000, 113.022);
		testWithParams(geburtstagKind, EinschulungTyp.KINDERGARTEN1, false, false, intervall, 20, 100000, 75.348);
		testWithParams(geburtstagKind, EinschulungTyp.VORSCHULALTER, false, false, intervall, 20, 50000, 138.138);

		testWithParams(geburtstagKind, EinschulungTyp.VORSCHULALTER, false, false, intervallTag, 20, 100000, 6.850);
		testWithParams(geburtstagKind, EinschulungTyp.KINDERGARTEN1, false, false, intervallTag, 20, 100000, 6.850);
		testWithParams(geburtstagKind, EinschulungTyp.VORSCHULALTER, true, true, intervallTag, 20, 100000, 13.528);
		testWithParams(geburtstagKind, EinschulungTyp.KINDERGARTEN1, true, true, intervallTag, 20, 100000, 13.528);

		testWithParams(geburtstagKind, EinschulungTyp.VORSCHULALTER, false, false, intervall, 20, 150000, 12.558);
		testWithParams(geburtstagKind, EinschulungTyp.KINDERGARTEN1, false, false, intervall, 20, 150000, 12.558);
		testWithParams(geburtstagKind, EinschulungTyp.VORSCHULALTER, true, true, intervall, 20, 150000, 86.022);
		testWithParams(geburtstagKind, EinschulungTyp.KINDERGARTEN1, true, true, intervall, 20, 150000, 86.022);

		testWithParams(geburtstagBaby, EinschulungTyp.VORSCHULALTER, false, false, intervall, 20, 100000, 113.022);
		testWithParams(geburtstagBaby, EinschulungTyp.KINDERGARTEN1, false, false, intervall, 20, 100000, 113.022);
		testWithParams(geburtstagBaby, EinschulungTyp.VORSCHULALTER, true, true, intervall, 20, 100000, 186.486);
		testWithParams(geburtstagBaby, EinschulungTyp.KINDERGARTEN1, true, true, intervall, 20, 100000, 186.486);

		testWithParams(geburtstagKind, EinschulungTyp.VORSCHULALTER, false, false, intervall, 20, 100000, 75.348);
		testWithParams(geburtstagKind, EinschulungTyp.KINDERGARTEN1, false, false, intervall, 20, 100000, 75.348);
		testWithParams(geburtstagKind, EinschulungTyp.VORSCHULALTER, true, true, intervall, 20, 100000, 148.812);
		testWithParams(geburtstagKind, EinschulungTyp.KINDERGARTEN1, true, true, intervall, 20, 100000, 148.812);
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

		testWithParams(kind, EinschulungTyp.VORSCHULALTER, false, false, halberAugust, 50, 68712, 329.462);
		testWithParams(kind, EinschulungTyp.VORSCHULALTER, false, false, ganzerSeptember, 50, 68712, 729.524);
		testWithParams(baby, EinschulungTyp.VORSCHULALTER, false, false, halberAugust, 50, 68712, 494.194);
		testWithParams(baby, EinschulungTyp.VORSCHULALTER, false, false, ganzerSeptember, 50, 68712, 1094.286);

		testWithParams(baby, EinschulungTyp.VORSCHULALTER, false, false, halberAugust, 50, 185447, 0.000);
		testWithParams(baby, EinschulungTyp.VORSCHULALTER, false, false, ganzerSeptember, 50, 185447, 0.000);
		testWithParams(baby, EinschulungTyp.VORSCHULALTER, true, true, halberAugust, 50, 185447, 211.129);
		testWithParams(baby, EinschulungTyp.VORSCHULALTER, true, true, ganzerSeptember, 50, 185447, 467.500);

		testWithParams(baby, EinschulungTyp.VORSCHULALTER, true, true, halberAugust, 50, 35447, 844.516);
		testWithParams(baby, EinschulungTyp.VORSCHULALTER, true, true, ganzerSeptember, 50, 35447, 1870.000);

		testWithParams(kind, EinschulungTyp.KINDERGARTEN1, false, false, halberAugust, 50, 68712, 329.462);
		testWithParams(kind, EinschulungTyp.KINDERGARTEN1, false, false, ganzerSeptember, 50, 68712, 729.524);
	}

	private void testWithParams(
		@Nonnull LocalDate geburtstag,
		@Nonnull EinschulungTyp einschulungTyp,
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
			einschulungTyp,
			besondereBeduerfnisse,
			MathUtil.DEFAULT.fromNullSafe(einkommen),
			MathUtil.DEFAULT.fromNullSafe(2000));

		VerfuegungZeitabschnitt verfuegungZeitabschnitt = verfuegung.getZeitabschnitte().get(0);
		BGCalculationInput inputAsiv = verfuegungZeitabschnitt.getBgCalculationInputAsiv();
		inputAsiv.setAnspruchspensumProzent(anspruch);
		inputAsiv.setBetreuungspensumProzent(MathUtil.DEFAULT.from(anspruch));
		inputAsiv.setBabyTarif(geburtstag.plusYears(1)
			.isAfter(verfuegungZeitabschnitt.getGueltigkeit().getGueltigBis()));
		inputAsiv.setEinschulungTyp(einschulungTyp);
		inputAsiv.setBesondereBeduerfnisseBestaetigt(besondereBeduerfnisseBestaetigt);

		BGCalculationResult result = tageselternRechner.calculateAsiv(inputAsiv, parameterDTO);

		assertThat(result, pojo(BGCalculationResult.class)
			.withProperty(
				"verguenstigung",
				BigDecimalCloseTo.closeTo(BigDecimal.valueOf(expected), BigDecimal.valueOf(0.0005)))
			.withProperty("bgPensumZeiteinheit", IsBigDecimal.greaterZeroWithScale10())
			.withProperty("anspruchspensumZeiteinheit", IsBigDecimal.greaterZeroWithScale10())
			.withProperty("zeiteinheit", is(PensumUnits.HOURS))
		);
	}
}
