/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.util;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.types.DateRange;
import com.github.npathai.hamcrestopt.OptionalMatchers;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAndIs;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class GueltigkeitsUtilTest {

	final Gueltigkeit first = GueltigkeitImp.of(LocalDate.of(2015, 1, 5), LocalDate.of(2015, 1, 25));
	// second: directly after first
	final Gueltigkeit second = GueltigkeitImp.of(LocalDate.of(2015, 1, 26), LocalDate.of(2015, 3, 22));
	// third: 8 days after second
	final Gueltigkeit third = GueltigkeitImp.of(LocalDate.of(2015, 3, 30), LocalDate.of(2015, 4, 19));

	final List<Gueltigkeit> items = Arrays.asList(first, second, third);

	@Nested
	class FindAnyAtStichtagTest {

		@Test
		void shouldFindNoneOnStichtagOutside() {

			Optional<Gueltigkeit> actualBefore = GueltigkeitsUtil.findAnyAtStichtag(
				asList(first, second, third),
				LocalDate.of(2000, 1, 1));

			assertThat(actualBefore, isEmpty());

			Optional<Gueltigkeit> actualAfter = GueltigkeitsUtil.findAnyAtStichtag(
				asList(first, second, third),
				LocalDate.of(2999, 1, 1));

			assertThat(actualAfter, isEmpty());
		}

		@Test
		void shouldFindTheOneWhenContaining() {
			Optional<Gueltigkeit> actual = GueltigkeitsUtil.findAnyAtStichtag(
				asList(first, second, third),
				LocalDate.of(2015, 1, 12));

			assertThat(actual, isPresentAndIs(first));
		}

		@Test
		void shouldFindTheOneOnStichtagExact() {
			Optional<Gueltigkeit> actualWhenAb = GueltigkeitsUtil.findAnyAtStichtag(
				asList(first, second, third),
				second.getGueltigkeit().getGueltigAb());

			assertThat(actualWhenAb, isPresentAndIs(second));

			Optional<Gueltigkeit> actualWhenBis = GueltigkeitsUtil.findAnyAtStichtag(
				asList(first, second, third),
				second.getGueltigkeit().getGueltigBis());

			assertThat(actualWhenBis, isPresentAndIs(second));
		}
	}

	@Nested
	class FindLastTest {

		@Test
		void testFindLast_shouldReturnEmptyForEmptyCollection() {
			Optional<Gueltigkeit> last = GueltigkeitsUtil.findLast(new HashSet<>());

			assertThat(last, OptionalMatchers.isEmpty());
		}

		@Test
		void testFindLast_shouldReturnTheLastGueltigEntity() {
			Optional<Gueltigkeit> last = GueltigkeitsUtil.findLast(items);

			assertThat(last, OptionalMatchers.isPresentAndIs(third));
		}
	}

	@Test
	void testFindFirst() {
		List<Gueltigkeit> empty = emptyList();
		assertThat(GueltigkeitsUtil.findFirst(empty).isPresent(), is(false));

		List<Gueltigkeit> single = singletonList(first);
		Optional<Gueltigkeit> firstFoundSingle = GueltigkeitsUtil.findFirst(single);
		assertThat(firstFoundSingle, isPresentAndIs(first));

		List<Gueltigkeit> ordered = asList(first, second, third);
		Optional<Gueltigkeit> firstFoundOrdered = GueltigkeitsUtil.findFirst(ordered);
		assertThat(firstFoundOrdered, isPresentAndIs(first));

		List<Gueltigkeit> unOrdered = asList(third, second, first);
		Optional<Gueltigkeit> firstFoundUnOrdered = GueltigkeitsUtil.findFirst(unOrdered);
		assertThat(firstFoundUnOrdered, isPresentAndIs(first));
	}

	@Nested
	class HasOverlapTest {

		@Test
		void noRange() {
			assertThat(GueltigkeitsUtil.hasOverlap(emptyList()), is(false));
		}

		@Test
		void singleRange() {
			assertThat(GueltigkeitsUtil.hasOverlapingGueltigkeit(singletonList(first)), is(false));
		}

		@Test
		void adjacentRanges() {
			assertThat(GueltigkeitsUtil.hasOverlapingGueltigkeit(asList(first, second)), is(false));
		}

		@Test
		void rangesWithGap() {
			assertThat(GueltigkeitsUtil.hasOverlapingGueltigkeit(asList(first, third)), is(false));
		}

		@Test
		void adjacentOverlap() {
			LocalDate firstBis = first.getGueltigkeit().getGueltigBis();
			Gueltigkeit overlapping = GueltigkeitImp.of(firstBis, firstBis.plusDays(2));

			assertThat(GueltigkeitsUtil.hasOverlapingGueltigkeit(asList(first, overlapping)), is(true));
		}

		@Test
		void extendedOverlap() {
			// r1 gueltigBis is overlapping r2 and r3
			DateRange r1 = new DateRange(
				LocalDate.of(2018, 1, 1), LocalDate.of(2018, 1, 30));

			DateRange r2 = new DateRange(
				LocalDate.of(2018, 1, 10), LocalDate.of(2018, 1, 15));

			DateRange r3 = new DateRange(
				LocalDate.of(2018, 1, 16), LocalDate.of(2018, 1, 20));

			assertThat(GueltigkeitsUtil.hasOverlap(asList(r1, r2, r3)), is(true));
		}
	}

	static class GueltigkeitImp implements Gueltigkeit {

		private DateRange gueltigkeit;

		private GueltigkeitImp(@Nonnull DateRange gueltigkeit) {
			this.gueltigkeit = gueltigkeit;
		}

		@Nonnull
		public static Gueltigkeit of(@Nonnull LocalDate von, @Nonnull LocalDate bis) {
			return new GueltigkeitImp(new DateRange(von, bis));
		}

		@Nonnull
		@Override
		public DateRange getGueltigkeit() {
			return gueltigkeit;
		}

		@NotNull
		@Override
		public void setGueltigkeit(@NotNull DateRange gueltigkeit) {
			this.gueltigkeit = gueltigkeit;
		}
	}
}
