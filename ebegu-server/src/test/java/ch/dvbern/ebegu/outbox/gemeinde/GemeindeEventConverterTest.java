/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.outbox.gemeinde;

import java.time.LocalDate;

import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.kibon.exchange.commons.gemeinde.GemeindeEventDTO;
import ch.dvbern.kibon.exchange.commons.util.AvroConverter;
import org.junit.jupiter.api.Test;

import static com.spotify.hamcrest.pojo.IsPojo.pojo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class GemeindeEventConverterTest {

	private final GemeindeEventConverter gemeindeEventConverter = new GemeindeEventConverter();

	@Test
	public void testChangedEvent() {
		Gemeinde gemeinde = new Gemeinde();
		gemeinde.setName("Test");
		gemeinde.setBetreuungsgutscheineStartdatum(LocalDate.of(2022,12,12));
		gemeinde.setBfsNummer(123L);
		gemeinde.setGueltigBis(LocalDate.of(9999,12,31));

		GemeindeChangedEvent gemeindeChangedEvent = gemeindeEventConverter.of(gemeinde);

		GemeindeEventDTO specificRecord = AvroConverter.fromAvroBinary(gemeindeChangedEvent.getSchema(), gemeindeChangedEvent.getPayload());

		assertThat(specificRecord, is(pojo(GemeindeEventDTO.class)
			.where(GemeindeEventDTO::getName, is(gemeinde.getName()))
			.where(GemeindeEventDTO::getBfsNummer, is(123L))
			.where(
				GemeindeEventDTO::getBetreuungsgutscheineAnbietenAb,
				is(gemeinde.getBetreuungsgutscheineStartdatum()))
			.where(GemeindeEventDTO::getGueltigBis, is(gemeinde.getGueltigBis()))
		));
	}
}
