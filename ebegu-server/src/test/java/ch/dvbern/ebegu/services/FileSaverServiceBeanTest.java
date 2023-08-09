/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.services;

import javax.annotation.Nonnull;

import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class FileSaverServiceBeanTest {

	@ParameterizedTest
	@CsvSource({
		"/app/foo.bar, /app/foo.bar",
		"/app/nested/foo.bar, /app/nested/foo.bar",
		"/app/../breakout/foo.bar, /breakout/foo.bar"
	})
	void fileNameNormalizer(@Nonnull String fileName, @Nonnull String expected) {
		String normalize = FilenameUtils.normalize(fileName, true);

		assertThat(normalize, is(expected));
	}
}
