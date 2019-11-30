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
import java.time.LocalDate;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.MathUtil;
import org.junit.Test;

import static com.spotify.hamcrest.pojo.IsPojo.pojo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class VerfuegungZeitabschnittTest extends AbstractBGRechnerTest {

	private final BGRechnerParameterDTO parameterDTO = getParameter();
	private final TageselternRechner tageselternRechner = new TageselternRechner();

	private static final BigDecimal MAX_STUNDEN_MONTH = MathUtil.DEFAULT.from(220);

	@Test
	public void whenFullMonth_30days_maxStunden() {
		DateRange gueltigkeit = new DateRange(LocalDate.of(2019, 11, 1)).withFullMonths();

		BGCalculationResult result = tageselternRechner.calculate(creaateZeitabschnitt(gueltigkeit), parameterDTO);
		assertThat(result, pojo(BGCalculationResult.class)
			.withProperty("verfuegteAnzahlZeiteinheiten", is(MAX_STUNDEN_MONTH)));
	}

	@Test
	public void whenFullMonth_31days_maxStunden() {
		DateRange gueltigkeit = new DateRange(LocalDate.of(2019, 3, 1)).withFullMonths();

		BGCalculationResult result = tageselternRechner.calculate(creaateZeitabschnitt(gueltigkeit), parameterDTO);
		assertThat(result, pojo(BGCalculationResult.class)
			.withProperty("verfuegteAnzahlZeiteinheiten", is(MAX_STUNDEN_MONTH)));
	}

	@Test
	public void whenFullMonth_28days_maxStunden() {
		DateRange gueltigkeit = new DateRange(LocalDate.of(2019, 2, 1)).withFullMonths();

		BGCalculationResult result = tageselternRechner.calculate(creaateZeitabschnitt(gueltigkeit), parameterDTO);
		assertThat(result, pojo(BGCalculationResult.class)
			.withProperty("verfuegteAnzahlZeiteinheiten", is(MAX_STUNDEN_MONTH)));
	}

	@Test
	public void whenHalfMonth() {
		DateRange gueltigkeit = new DateRange(LocalDate.of(2019, 11, 1), LocalDate.of(2019, 11, 15));

		BGCalculationResult result = tageselternRechner.calculate(creaateZeitabschnitt(gueltigkeit), parameterDTO);
		assertThat(result, pojo(BGCalculationResult.class)
			.withProperty(
				"verfuegteAnzahlZeiteinheiten",
				is(MathUtil.DEFAULT.divide(MAX_STUNDEN_MONTH, BigDecimal.valueOf(2)))));
	}

	@Test
	public void whenDay() {
		DateRange gueltigkeit = new DateRange(LocalDate.of(2019, 11, 1), LocalDate.of(2019, 11, 1));

		BGCalculationResult result = tageselternRechner.calculate(creaateZeitabschnitt(gueltigkeit), parameterDTO);
		assertThat(result, pojo(BGCalculationResult.class)
			.withProperty(
				"verfuegteAnzahlZeiteinheiten",
				is(MathUtil.DEFAULT.divide(MAX_STUNDEN_MONTH, BigDecimal.valueOf(30)))));
	}

	@Nonnull
	private VerfuegungZeitabschnitt creaateZeitabschnitt(@Nonnull DateRange gueltigkeit) {
		VerfuegungZeitabschnitt zeitabschnitt = new VerfuegungZeitabschnitt(gueltigkeit);
		zeitabschnitt.setAnspruchberechtigtesPensum(100);
		zeitabschnitt.setBetreuungspensum(MathUtil.DEFAULT.from(100));

		return zeitabschnitt;
	}
}
