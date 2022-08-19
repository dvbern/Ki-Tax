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

package ch.dvbern.ebegu.outbox.gemeindekennzahlen;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;

import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.gemeindeantrag.gemeindekennzahlen.GemeindeKennzahlen;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.kibon.exchange.commons.Gemeindekennzahlen.GemeindeKennzahlenEventDTO;
import ch.dvbern.kibon.exchange.commons.util.AvroConverter;
import org.junit.jupiter.api.Test;

import static com.spotify.hamcrest.pojo.IsPojo.pojo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.is;

public class GemeindeKennzahlenEventConverterTest {

	private final GemeindeKennzahlenEventConverter gemeindeKennzahlenEventConverter = new GemeindeKennzahlenEventConverter();

	@Test
	public void testChangedEvent() {
		Gemeinde gemeinde = new Gemeinde();
		gemeinde.setBfsNummer(123L);
		Gesuchsperiode gesuchsperiode = new Gesuchsperiode();
		gesuchsperiode.setGueltigkeit(new DateRange());
		gesuchsperiode.getGueltigkeit().setGueltigAb(LocalDate.of(2022,8,1));
		gesuchsperiode.getGueltigkeit().setGueltigBis(LocalDate.of(2023,7,31));
		GemeindeKennzahlen gemeindeKennzahlen = new GemeindeKennzahlen();
		gemeindeKennzahlen.setGemeinde(gemeinde);
		gemeindeKennzahlen.setGesuchsperiode(gesuchsperiode);
		gemeindeKennzahlen.setLimitierungTfo(EinschulungTyp.KINDERGARTEN1);
		gemeindeKennzahlen.setNachfrageAnzahl(BigInteger.TEN);
		gemeindeKennzahlen.setNachfrageDauer(BigDecimal.ONE);
		gemeindeKennzahlen.setNachfrageErfuellt(true);
		gemeindeKennzahlen.setGemeindeKontingentiert(true);

		GemeindeKennzahlenChangedEvent gemeindeKennzahlenChangedEvent = gemeindeKennzahlenEventConverter.of(gemeindeKennzahlen);

		//noinspection deprecation
		GemeindeKennzahlenEventDTO specificRecord = AvroConverter.fromAvroBinary(gemeindeKennzahlenChangedEvent.getSchema(), gemeindeKennzahlenChangedEvent.getPayload());

		assertThat(specificRecord, is(pojo(GemeindeKennzahlenEventDTO.class)
			.where(GemeindeKennzahlenEventDTO::getBfsNummer, is(123L))
			.where(
				GemeindeKennzahlenEventDTO::getGesuchsperiodeStart,
				is(gesuchsperiode.getGueltigkeit().getGueltigAb()))
			.where(GemeindeKennzahlenEventDTO::getGesuchsperiodeStop, is(gesuchsperiode.getGueltigkeit().getGueltigBis()))
			.where(GemeindeKennzahlenEventDTO::getAnzahlKinderWarteliste, comparesEqualTo(BigDecimal.TEN))
			.where(GemeindeKennzahlenEventDTO::getDauerWarteliste, comparesEqualTo(BigDecimal.ONE))
			.where(GemeindeKennzahlenEventDTO::getKontingentierung, is(true))
			.where(GemeindeKennzahlenEventDTO::getKontingentierungAusgeschoepft, is(true))
			.where(GemeindeKennzahlenEventDTO::getLimitierungTfo, is(ch.dvbern.kibon.exchange.commons.types.EinschulungTyp.KINDERGARTEN1))
		));
	}
}
