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
import java.util.Collections;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.MathUtil;
import org.junit.Assert;
import org.junit.Test;

public class VerfuegungZeitabschnittTest extends AbstractBGRechnerTest {

	private final BGRechnerParameterDTO parameterDTO = getParameter();
	private final TageselternBernRechner tageselternRechner = new TageselternBernRechner(Collections.emptyList());

	@Nonnull
	private static final BigDecimal MAX_STUNDEN_MONTH = MathUtil.DEFAULT.from(220);

	@Test
	public void whenFullMonth_30days_maxStunden() {
		DateRange gueltigkeit = new DateRange(LocalDate.of(2019, 11, 1)).withFullMonths();

		BGCalculationResult result = calculate(gueltigkeit);
		Assert.assertEquals(MAX_STUNDEN_MONTH, result.getBgPensumZeiteinheit());
	}

	@Test
	public void whenFullMonth_31days_maxStunden() {
		DateRange gueltigkeit = new DateRange(LocalDate.of(2019, 3, 1)).withFullMonths();

		BGCalculationResult result = calculate(gueltigkeit);
		Assert.assertEquals(MAX_STUNDEN_MONTH, result.getBgPensumZeiteinheit());
	}

	@Test
	public void whenFullMonth_28days_maxStunden() {
		DateRange gueltigkeit = new DateRange(LocalDate.of(2019, 2, 1)).withFullMonths();

		BGCalculationResult result = calculate(gueltigkeit);
		Assert.assertEquals(MAX_STUNDEN_MONTH, result.getBgPensumZeiteinheit());
	}

	@Test
	public void whenHalfMonth() {
		DateRange gueltigkeit = new DateRange(LocalDate.of(2019, 11, 1), LocalDate.of(2019, 11, 15));

		BGCalculationResult result = calculate(gueltigkeit);
		Assert.assertEquals(MathUtil.DEFAULT.divide(MAX_STUNDEN_MONTH, BigDecimal.valueOf(2)), result.getBgPensumZeiteinheit());
	}

	@Test
	public void whenDay() {
		DateRange gueltigkeit = new DateRange(LocalDate.of(2019, 11, 1), LocalDate.of(2019, 11, 1));

		BGCalculationResult result = calculate(gueltigkeit);
		BigDecimal ungerundeterWert = MathUtil.DEFAULT.divide(MAX_STUNDEN_MONTH, BigDecimal.valueOf(30));
		Assert.assertEquals(MathUtil.roundToNearestQuarter(ungerundeterWert), result.getBgPensumZeiteinheit());
	}

	@Nonnull
	private BGCalculationResult calculate(@Nonnull DateRange gueltigkeit) {
		VerfuegungZeitabschnitt zeitabschnitt = createZeitabschnitt(gueltigkeit);
		tageselternRechner.calculate(zeitabschnitt, parameterDTO);

		return zeitabschnitt.getBgCalculationResultAsiv();
	}

	@Nonnull
	private VerfuegungZeitabschnitt createZeitabschnitt(@Nonnull DateRange gueltigkeit) {
		VerfuegungZeitabschnitt zeitabschnitt = new VerfuegungZeitabschnitt(gueltigkeit);
		BGCalculationInput inputAsiv = zeitabschnitt.getBgCalculationInputAsiv();
		inputAsiv.setAnspruchspensumProzent(100);
		inputAsiv.setBetreuungspensumProzent(MathUtil.DEFAULT.from(100));
		inputAsiv.setEinschulungTyp(EinschulungTyp.VORSCHULALTER);

		return zeitabschnitt;
	}
}
