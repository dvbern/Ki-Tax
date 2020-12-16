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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.types.DateRange;

import static com.google.common.base.Preconditions.checkNotNull;

public final class GueltigkeitsUtil {

	private GueltigkeitsUtil() {
		// utliity class
	}

	@Nonnull
	public static <T extends Gueltigkeit> Optional<T> findAnyAtStichtag(
		@Nonnull Collection<T> entities,
		@Nonnull LocalDate stichtag) {

		return entities.stream()
			.filter(e -> e.getGueltigkeit().contains(stichtag))
			.findAny();
	}

	/**
	 * @return letzt gueltiges Entity in der Collection. Falls die Collection leer ist, wird ein Optional.empty
	 * zurueck gegeben.
	 */
	@Nonnull
	public static <T extends Gueltigkeit> Optional<T> findLast(@Nonnull Collection<T> existingEntities) {
		return existingEntities.stream().max(Gueltigkeit.GUELTIG_AB_COMPARATOR);
	}

	@Nonnull
	public static <T extends Gueltigkeit> Optional<T> findFirst(@Nonnull List<T> entities) {
		checkNotNull(entities);
		Optional<T> first = entities.stream()
			.reduce((a, b) -> a.getGueltigkeit().getGueltigAb().isBefore(b.getGueltigkeit().getGueltigAb()) ? a : b);

		return first;
	}

	public static <T extends Gueltigkeit> boolean hasOverlapingGueltigkeit(@Nonnull Collection<T> entities) {
		List<DateRange> collect = entities.stream()
			.map(Gueltigkeit::getGueltigkeit)
			.collect(Collectors.toList());

		return hasOverlap(collect);
	}

	public static boolean hasOverlap(@Nonnull Collection<DateRange> ranges) {
		ArrayList<DateRange> dateRanges = new ArrayList<>(ranges);
		dateRanges.sort(Comparator.comparing(DateRange::getGueltigAb));

		return IntStream.range(0, dateRanges.size() - 1).anyMatch(i -> {
			LocalDate currentBis = dateRanges.get(i).getGueltigBis();
			LocalDate nextAb = dateRanges.get(i + 1).getGueltigAb();

			return !currentBis.isBefore(nextAb);
		});
	}
}
