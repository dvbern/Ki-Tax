/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.entities;

import java.time.LocalDate;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.enums.Ferienname;
import ch.dvbern.ebegu.tests.validations.AbstractValidatorTest;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.validators.dateranges.CheckGueltigkeiten;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static ch.dvbern.ebegu.tests.util.validation.ViolationMatchers.violatesAnnotation;
import static org.hamcrest.MatcherAssert.assertThat;

class GemeindeStammdatenGesuchsperiodeFerieninselValidatorTest extends AbstractValidatorTest {

	@Test
	void valid() {
		var entity = createValid();

		assertValid(entity);
	}

	@ParameterizedTest
	@CsvSource({
		"2024-01-01, 2024-01-01, 2024-01-01, 2024-01-01",
		"2024-01-01, 2024-01-01, 2024-01-01, 2024-01-02",
		"2024-01-01, 2024-01-10, 2024-01-05, 2024-01-20",
	})
	void failsWhenDateRangesOverlap(LocalDate firstStart, LocalDate firstEnd, LocalDate secondStart, LocalDate secondEnd) {
		var entity = createValid();
		entity.getZeitraumList().add(createZeitraum(new DateRange(firstStart, firstEnd)));
		entity.getZeitraumList().add(createZeitraum(new DateRange(secondStart, secondEnd)));

		assertThat(validate(entity), violatesAnnotation(CheckGueltigkeiten.class));
	}

	@ParameterizedTest
	@CsvSource({
		"2024-01-01, 2024-01-01, 2024-01-02, 2024-01-02",
		"2024-01-01, 2024-01-01, 2024-01-10, 2024-01-20",
	})
	void passesWithOverlappFreeDateRanges(LocalDate firstStart, LocalDate firstEnd, LocalDate secondStart, LocalDate secondEnd) {
		var entity = createValid();
		entity.getZeitraumList().add(createZeitraum(new DateRange(firstStart, firstEnd)));
		entity.getZeitraumList().add(createZeitraum(new DateRange(secondStart, secondEnd)));

		assertValid(entity);
	}

	@Nonnull
	private GemeindeStammdatenGesuchsperiodeFerieninsel createValid() {
		var entity = new GemeindeStammdatenGesuchsperiodeFerieninsel();
		entity.setFerienname(Ferienname.FRUEHLINGSFERIEN);
		entity.setGemeindeStammdatenGesuchsperiode(new GemeindeStammdatenGesuchsperiode());

		return entity;
	}

	@Nonnull
	private GemeindeStammdatenGesuchsperiodeFerieninselZeitraum createZeitraum(@Nonnull DateRange dateRange) {
		var zeitraum = new GemeindeStammdatenGesuchsperiodeFerieninselZeitraum();
		zeitraum.setGueltigkeit(dateRange);

		return zeitraum;
	}
}
