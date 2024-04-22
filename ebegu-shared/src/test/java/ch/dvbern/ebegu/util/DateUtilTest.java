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

package ch.dvbern.ebegu.util;

import java.time.LocalDate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests fuer DateUtil
 */
class DateUtilTest {

	@Test
	void parseStringToDateOrReturnNowTestNullString() {
		final LocalDate now = LocalDate.now();
		final LocalDate returnedDate = DateUtil.parseStringToDateOrReturnNow(null);
		Assertions.assertNotNull(returnedDate);
		Assertions.assertTrue(now.isEqual(returnedDate));
	}

	@Test
	void parseStringToDateOrReturnNowTestEmptyString() {
		final LocalDate now = LocalDate.now();
		final LocalDate returnedDate = DateUtil.parseStringToDateOrReturnNow("");
		Assertions.assertNotNull(returnedDate);
		Assertions.assertTrue(now.isEqual(returnedDate));
	}

	@Test
	void parseStringToDateOrReturnNowTest() {
		final LocalDate now = LocalDate.now();
		final LocalDate returnedDate = DateUtil.parseStringToDateOrReturnNow("2015-12-31");
		Assertions.assertNotNull(returnedDate);
		Assertions.assertEquals(2015, returnedDate.getYear());
		Assertions.assertEquals(12, returnedDate.getMonthValue());
		Assertions.assertEquals(31, returnedDate.getDayOfMonth());
	}

	@Test
	void testIncrementYear() {
		final String oldDate = "2020-05-17";
		final String newDate = DateUtil.incrementYear(oldDate);
		Assertions.assertEquals("2021-05-17", newDate);
	}
}
