/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

import javax.annotation.Nonnull;
import javax.enterprise.context.ApplicationScoped;

import ch.dvbern.ebegu.entities.gemeindeantrag.gemeindekennzahlen.GemeindeKennzahlen;
import ch.dvbern.kibon.exchange.commons.gemeindekennzahlen.GemeindeKennzahlenEventDTO;
import ch.dvbern.kibon.exchange.commons.gemeindekennzahlen.GemeindeKennzahlenEventDTO.Builder;
import ch.dvbern.kibon.exchange.commons.types.EinschulungTyp;
import ch.dvbern.kibon.exchange.commons.types.Mandant;
import ch.dvbern.kibon.exchange.commons.util.AvroConverter;

@ApplicationScoped
public class GemeindeKennzahlenEventConverter {

	@Nonnull
	public GemeindeKennzahlenChangedEvent of(@Nonnull GemeindeKennzahlen gemeindeKennzahlen, @Nonnull
		ch.dvbern.ebegu.enums.EinschulungTyp bgAusstellenBisUndMitStufe, @Nonnull BigDecimal erwerbspensumZuschlag) {
		GemeindeKennzahlenEventDTO dto = toGemeindeKennzahlenEventDTO(gemeindeKennzahlen);
		dto.setErwerbspensumZuschlag(erwerbspensumZuschlag);
		dto.setLimitierungKita(EinschulungTyp.valueOf(bgAusstellenBisUndMitStufe.name()));

		byte[] payload = AvroConverter.toAvroBinary(dto);

		return new GemeindeKennzahlenChangedEvent(gemeindeKennzahlen.getId(), payload, dto.getSchema());
	}

	@Nonnull
	public GemeindeKennzahlenRemovedEvent removeEventOf(@Nonnull GemeindeKennzahlen gemeindeKennzahlen) {
		GemeindeKennzahlenEventDTO dto = toGemeindeKennzahlenEventDTO(gemeindeKennzahlen);

		byte[] payload = AvroConverter.toAvroBinary(dto);

		return new GemeindeKennzahlenRemovedEvent(gemeindeKennzahlen.getId(), payload, dto.getSchema());
	}

	@Nonnull
	private GemeindeKennzahlenEventDTO toGemeindeKennzahlenEventDTO(@Nonnull GemeindeKennzahlen gemeindeKennzahlen) {
		//noinspection ConstantConditions
		Builder builder = GemeindeKennzahlenEventDTO.newBuilder()
			.setGemeindeUUID(gemeindeKennzahlen.getGemeinde().getId())
			.setBfsNummer(gemeindeKennzahlen.getGemeinde().getBfsNummer())
			.setGesuchsperiodeStart(gemeindeKennzahlen.getGesuchsperiode().getGueltigkeit().getGueltigAb())
			.setGesuchsperiodeStop(gemeindeKennzahlen.getGesuchsperiode().getGueltigkeit().getGueltigBis())
			.setKontingentierung(gemeindeKennzahlen.getGemeindeKontingentiert())
			.setKontingentierungAusgeschoepft(gemeindeKennzahlen.getNachfrageErfuellt())
			.setAnzahlKinderWarteliste(gemeindeKennzahlen.getNachfrageAnzahl() != null ? new BigDecimal(gemeindeKennzahlen.getNachfrageAnzahl()) : null)
			.setDauerWarteliste(gemeindeKennzahlen.getNachfrageDauer())
			.setLimitierungTfo(gemeindeKennzahlen.getLimitierungTfo() != null ? EinschulungTyp.valueOf(gemeindeKennzahlen.getLimitierungTfo().name()) : null)
			.setMandant(Mandant.valueOf(gemeindeKennzahlen.getGemeinde().getMandant().getMandantIdentifier().name()));

		return builder.build();
	}
}
