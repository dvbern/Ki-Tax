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
import java.time.LocalDate;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.BetreuungsmitteilungPensum;
import ch.dvbern.ebegu.entities.EingewoehnungPauschale;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.EingewoehnungDTO;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.ZeitabschnittDTO;
import org.junit.jupiter.api.Test;

import static ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungTestUtil.createZeitabschnittDTO;
import static com.spotify.hamcrest.pojo.IsPojo.pojo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

class EingewoehnungPauschaleMapperTest {

	@Test
	void importEingewoehnungPauschale() {
		ZeitabschnittDTO z = createZeitabschnittDTO(Constants.DEFAULT_GUELTIGKEIT);
		EingewoehnungDTO eingewoehnung =
			new EingewoehnungDTO(BigDecimal.valueOf(123.45), LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));
		z.setEingewoehnung(eingewoehnung);

		BetreuungsmitteilungPensum actual = convert(z);

		assertThat(actual.getEingewoehnungPauschale(), pojo(EingewoehnungPauschale.class)
			.where(EingewoehnungPauschale::getPauschale, comparesEqualTo(BigDecimal.valueOf(123.45)))
			.where(EingewoehnungPauschale::getGueltigkeit, pojo(DateRange.class)
				.where(DateRange::getGueltigAb, comparesEqualTo(LocalDate.of(2024, 1, 1)))
				.where(DateRange::getGueltigBis, comparesEqualTo(LocalDate.of(2024, 1, 31)))
			));
	}

	@Test
	void importEingewoehnungPauschale_null() {
		ZeitabschnittDTO z = createZeitabschnittDTO(Constants.DEFAULT_GUELTIGKEIT);
		z.setEingewoehnung(null);

		BetreuungsmitteilungPensum actual = convert(z);

		assertThat(actual.getEingewoehnungPauschale(), is(nullValue()));
	}

	@Nonnull
	private BetreuungsmitteilungPensum convert(ZeitabschnittDTO z) {
		BetreuungsmitteilungPensum actual = new BetreuungsmitteilungPensum();
		PensumMapper.EINGEWOEHNUNG_PAUSCHALE_MAPPER.toAbstractMahlzeitenPensum(actual, z);

		return actual;
	}
}
