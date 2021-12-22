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
import com.spotify.hamcrest.pojo.IsPojo;
import org.hamcrest.Matcher;
import org.hamcrest.number.BigDecimalCloseTo;
import org.junit.Test;

import static com.spotify.hamcrest.pojo.IsPojo.pojo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.is;

/**
 * Testet den Kita-Rechner
 */
public class KitaBernRechnerTest extends AbstractBGRechnerTest {

	private final BGRechnerParameterDTO parameterDTO = getParameter();
	private final KitaBernRechner kitaRechner = new KitaBernRechner(Collections.emptyList());

	private final LocalDate geburtstagBaby = LocalDate.of(2018, Month.OCTOBER, 15);
	private final LocalDate geburtstagKind = LocalDate.of(2016, Month.OCTOBER, 15);

	private final DateRange intervall = new DateRange(
		LocalDate.of(2019, Month.FEBRUARY, 10),
		LocalDate.of(2019, Month.FEBRUARY, 20));

	private final DateRange intervallTag = new DateRange(
		LocalDate.of(2019, Month.FEBRUARY, 10),
		LocalDate.of(2019, Month.FEBRUARY, 10));

	@SuppressWarnings("JUnitTestMethodWithNoAssertions")
	@Test
	public void test() {
		testWithParams(geburtstagBaby, EinschulungTyp.VORSCHULALTER, false, false, intervall, 20, 20, 100000, 120.879);
		testWithParams(geburtstagKind, EinschulungTyp.KINDERGARTEN1, false, false, intervall, 20, 20, 100000, 60.440);
		testWithParams(geburtstagKind, EinschulungTyp.VORSCHULALTER, false, false, intervall, 20, 20, 50000, 147.741);

		testWithParams(geburtstagKind, EinschulungTyp.VORSCHULALTER, false, false, intervallTag, 20, 20, 100000, 7.3261);
		testWithParams(geburtstagKind, EinschulungTyp.KINDERGARTEN1, false, false, intervallTag, 20, 20, 100000, 5.495);
		testWithParams(geburtstagKind, EinschulungTyp.VORSCHULALTER, true, true, intervallTag, 20, 20, 100000, 14.469);
		testWithParams(geburtstagKind, EinschulungTyp.KINDERGARTEN1, true, true, intervallTag, 20, 20, 100000, 12.637);

		testWithParams(geburtstagKind, EinschulungTyp.VORSCHULALTER, false, false, intervall, 20, 20, 150000, 13.431);
		testWithParams(geburtstagKind, EinschulungTyp.KINDERGARTEN1, false, false, intervall, 20, 20, 150000, 10.073);
		testWithParams(geburtstagKind, EinschulungTyp.VORSCHULALTER, true, true, intervall, 20, 20, 150000, 92.002);
		testWithParams(geburtstagKind, EinschulungTyp.KINDERGARTEN1, true, true, intervall, 20, 20, 150000, 88.645);
	}

	@SuppressWarnings("JUnitTestMethodWithNoAssertions")
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
		testWithParams(kind, EinschulungTyp.VORSCHULALTER, false, false, halberAugust, 50, 50, 68712, 352.366);
		testWithParams(kind, EinschulungTyp.VORSCHULALTER, false, false, ganzerSeptember, 50, 50, 68712, 780.239);
		testWithParams(baby, EinschulungTyp.VORSCHULALTER, false, false, halberAugust, 50, 50, 68712, 528.549);
		testWithParams(baby, EinschulungTyp.VORSCHULALTER, false, false, ganzerSeptember, 50, 50, 68712, 1170.359);
		// Normalfall Baby
		testWithParams(baby, EinschulungTyp.VORSCHULALTER, false, false, halberAugust, 50, 50, 185447, 0.00);
		testWithParams(baby, EinschulungTyp.VORSCHULALTER, false, false, ganzerSeptember, 50, 50, 185447, 0.00);
		testWithParams(baby, EinschulungTyp.VORSCHULALTER, true, true, halberAugust, 50, 50, 185447, 225.806);
		testWithParams(baby, EinschulungTyp.VORSCHULALTER, true, true, ganzerSeptember, 50, 50, 185447, 500.00);
		// Besondere Beduerfnisse
		testWithParams(baby, EinschulungTyp.VORSCHULALTER, true, true, halberAugust, 50, 50, 35447, 871.613);
		testWithParams(baby, EinschulungTyp.VORSCHULALTER, true, true, ganzerSeptember, 50, 50, 35447, 1930.00);
		// Eingeschult
		testWithParams(kind, EinschulungTyp.KINDERGARTEN1, false, false, halberAugust, 50, 50, 68712, 264.275);
		testWithParams(kind, EinschulungTyp.KINDERGARTEN1, false, false, ganzerSeptember, 50, 50, 68712, 585.179);
	}

	@Test
	public void vollkostenMuessenAufVerguenstigtesPensumBezogenSein() {
		DateRange ganzerSeptember = new DateRange(
			LocalDate.of(2018, Month.SEPTEMBER, 1),
			LocalDate.of(2018, Month.SEPTEMBER, 30));

		IsPojo<BGCalculationResult> matcher = pojo(BGCalculationResult.class)
			.where(BGCalculationResult::getVollkosten, comparesEqualTo(BigDecimal.valueOf(1000)));

		testWithParams(geburtstagKind, EinschulungTyp.VORSCHULALTER, false, false, ganzerSeptember, 100, 50, 180607, matcher);
	}

	private void testWithParams(
		@Nonnull LocalDate geburtstag,
		@Nonnull EinschulungTyp einschulungTyp,
		boolean besondereBeduerfnisse,
		boolean besondereBeduerfnisseBestaetigt,
		@Nonnull DateRange intervall,
		int betreuungspensum,
		int anspruch,
		int einkommen,
		double expected
	) {

		testWithParams(
			geburtstag,
			einschulungTyp,
			besondereBeduerfnisse,
			besondereBeduerfnisseBestaetigt,
			intervall,
			betreuungspensum,
			anspruch,
			einkommen,
			defaultMatcher(expected));
	}

	private void testWithParams(
		@Nonnull LocalDate geburtstag,
		@Nonnull EinschulungTyp einschulungTyp,
		boolean besondereBeduerfnisse,
		boolean besondereBeduerfnisseBestaetigt,
		@Nonnull DateRange intervall,
		int betreuungspensum,
		int anspruch,
		int einkommen,
		@Nonnull Matcher<BGCalculationResult> matcher
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
		inputAsiv.setBetreuungspensumProzent(MathUtil.DEFAULT.from(betreuungspensum));
		inputAsiv.setBabyTarif(geburtstag.plusYears(1)
			.isAfter(verfuegungZeitabschnitt.getGueltigkeit().getGueltigBis()));
		inputAsiv.setEinschulungTyp(einschulungTyp);
		inputAsiv.setBesondereBeduerfnisseBestaetigt(besondereBeduerfnisseBestaetigt);

		BGCalculationResult result = kitaRechner.calculateAsiv(inputAsiv, parameterDTO);

		assertThat(result, matcher);
	}

	@Nonnull
	private IsPojo<BGCalculationResult> defaultMatcher(double expectedVerguenstigung) {
		return pojo(BGCalculationResult.class)
			.withProperty("verguenstigung",
				BigDecimalCloseTo.closeTo(BigDecimal.valueOf(expectedVerguenstigung), BigDecimal.valueOf(0.0005)))
			.withProperty("bgPensumZeiteinheit", IsBigDecimal.greaterZeroWithScale10())
			.withProperty("anspruchspensumZeiteinheit", IsBigDecimal.greaterZeroWithScale10())
			.withProperty("zeiteinheit", is(PensumUnits.DAYS));
	}
}
