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

import java.time.LocalDate;

import ch.dvbern.ebegu.entities.BetreuungsmitteilungPensum;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.ZeitabschnittDTO;
import org.junit.jupiter.api.Test;

import static ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungTestUtil.createZeitabschnittDTO;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class GueltigkeitMapperTest {

	@Test
	void importGueltigkeit() {
		ZeitabschnittDTO z = createZeitabschnittDTO(new DateRange(LocalDate.of(2024, 4, 1), LocalDate.of(2024, 4, 10)));

		BetreuungsmitteilungPensum actual = new BetreuungsmitteilungPensum();
		PensumMapper.GUELTIGKEIT_MAPPER.toAbstractMahlzeitenPensum(actual, z);

		assertThat(actual.getGueltigkeit(), is(new DateRange(LocalDate.of(2024, 4, 1), LocalDate.of(2024, 4, 10))));
	}
}
