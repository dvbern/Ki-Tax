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

package ch.dvbern.ebegu.inbox.handler.pensum;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.BetreuungsmitteilungPensum;
import ch.dvbern.ebegu.types.DateRange;
import org.junit.jupiter.api.Test;

import static ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungTestUtil.createBetreuungsmitteilungPensum;
import static ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungTestUtil.matches;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

class PensumMappingUtilMergeSamePensenTest {

	@Test
	void nopForSinglePensum() {
		DateRange range1 = new DateRange(2020);
		BetreuungsmitteilungPensum pensum1 = createBetreuungsmitteilungPensum(range1);

		List<BetreuungsmitteilungPensum> pensen = Collections.singletonList(pensum1);
		Collection<BetreuungsmitteilungPensum> result = extendGueltigkeit(pensen);

		assertThat(result, contains(
			matches(pensum1, range1)
		));
	}

	@Test
	void shouldExtendsGueltigkeitOfAdjactentPensen() {
		DateRange range1 = new DateRange(2020);
		DateRange range2 = new DateRange(2021);
		BetreuungsmitteilungPensum pensum1 = createBetreuungsmitteilungPensum(range1);
		BetreuungsmitteilungPensum pensum2 = createBetreuungsmitteilungPensum(range2);

		Collection<BetreuungsmitteilungPensum> result = extendGueltigkeit(Arrays.asList(pensum1, pensum2));

		assertThat(result, contains(
			matches(pensum1, range1.getGueltigAb(), range2.getGueltigBis())
		));
	}

	@Test
	void shouldNotExtendGueltigkeitWhenNotSame() {
		DateRange range1 = new DateRange(2020);
		DateRange range2 = new DateRange(2021);
		BetreuungsmitteilungPensum pensum1 = createBetreuungsmitteilungPensum(range1);
		BetreuungsmitteilungPensum pensum2 = createBetreuungsmitteilungPensum(range2);
		// make them different
		pensum2.setMonatlicheBetreuungskosten(BigDecimal.TEN);

		Collection<BetreuungsmitteilungPensum> result = extendGueltigkeit(Arrays.asList(pensum1, pensum2));

		assertThat(result, contains(
			matches(pensum1, range1),
			matches(pensum2, range2)
		));
	}

	@Test
	void shouldNotExtendGueltigkeitWhenGueltigeitsGap() {
		DateRange range1 = new DateRange(2020);
		DateRange range2 = new DateRange(2022);
		BetreuungsmitteilungPensum pensum1 = createBetreuungsmitteilungPensum(range1);
		BetreuungsmitteilungPensum pensum2 = createBetreuungsmitteilungPensum(range2);

		Collection<BetreuungsmitteilungPensum> result = extendGueltigkeit(Arrays.asList(pensum1, pensum2));

		assertThat(result, contains(
			matches(pensum1, range1),
			matches(pensum2, range2)
		));
	}

	@Test
	void shouldExtendMultipleTimes() {
		DateRange range1 = new DateRange(2020);
		DateRange range2 = new DateRange(2021);
		DateRange range3 = new DateRange(2022);
		DateRange range4 = new DateRange(2023);
		DateRange range5 = new DateRange(2024);
		BetreuungsmitteilungPensum pensum1 = createBetreuungsmitteilungPensum(range1);
		BetreuungsmitteilungPensum pensum2 = createBetreuungsmitteilungPensum(range2);
		BetreuungsmitteilungPensum pensum3 = createBetreuungsmitteilungPensum(range3);
		pensum3.setPensum(BigDecimal.TEN);
		BetreuungsmitteilungPensum pensum4 = createBetreuungsmitteilungPensum(range4);
		pensum4.setPensum(BigDecimal.TEN);
		BetreuungsmitteilungPensum pensum5 = createBetreuungsmitteilungPensum(range5);

		List<BetreuungsmitteilungPensum> pensen = Arrays.asList(pensum1, pensum2, pensum3, pensum4, pensum5);
		Collection<BetreuungsmitteilungPensum> result = extendGueltigkeit(pensen);

		assertThat(result, contains(
			matches(pensum1, range1.getGueltigAb(), range2.getGueltigBis()),
			matches(pensum3, range3.getGueltigAb(), range4.getGueltigBis()),
			matches(pensum5, range5)
		));
	}

	@Nonnull
	private Collection<BetreuungsmitteilungPensum> extendGueltigkeit(
		@Nonnull Collection<BetreuungsmitteilungPensum> pensen) {

		return PensumMappingUtil.extendGueltigkeit(pensen, a -> a);
	}
}
