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

package ch.dvbern.ebegu.outbox.platzbestaetigung;

import javax.annotation.Nonnull;
import javax.enterprise.context.ApplicationScoped;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.BetreuungAnfrageEventDTO;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.GesuchstellerDTO;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.KindDTO;
import ch.dvbern.kibon.exchange.commons.util.AvroConverter;

import static java.util.Objects.requireNonNull;

@ApplicationScoped
public class BetreuungAnfrageEventConverter {

	@Nonnull
	public BetreuungAnfrageAddedEvent of(@Nonnull Betreuung betreuung) {
		BetreuungAnfrageEventDTO dto = toBetreuungAnfrageEvent(betreuung);
		byte[] payload = AvroConverter.toAvroBinary(dto);

		return new BetreuungAnfrageAddedEvent(betreuung.getBGNummer(), payload, dto.getSchema());
	}

	/**
	 * Convert einen Kibon Betreuung Entity in einer BetreuungAnfrageEventDTO
	 *
	 * @param betreuung
	 * @return
	 */
	@Nonnull
	private BetreuungAnfrageEventDTO toBetreuungAnfrageEvent(@Nonnull Betreuung betreuung) {
		return BetreuungAnfrageEventDTO.newBuilder()
			.setRefnr(betreuung.getBGNummer())
			.setInstitutionId(betreuung.getInstitutionStammdaten().getInstitution().getId())
			.setGesuchsteller(toGesuchstellerDTO(requireNonNull(betreuung.extractGesuch().getGesuchsteller1()).getGesuchstellerJA()))
			.setKind(toKindDTO(betreuung.getKind().getKindJA()))
			.setPeriodeVon(betreuung.extractGesuchsperiode().getGueltigkeit().getGueltigAb())  //Gesuschperiode von bis
			.setPeriodeBis(betreuung.extractGesuchsperiode().getGueltigkeit().getGueltigBis())
			.setBetreuungsArt(toBetreuungsangebotTyp(betreuung.getBetreuungsangebotTyp()))
			.setAbgelehntVonGesuchsteller(false) //ist immer false bei erstellung, sonst kann man mit Datum überprüfen
			.build();
	}

	@Nonnull
	private GesuchstellerDTO toGesuchstellerDTO(@Nonnull Gesuchsteller gesuchsteller) {
		return GesuchstellerDTO.newBuilder()
			.setVorname(gesuchsteller.getVorname())
			.setNachname(gesuchsteller.getNachname())
			.setEmail(gesuchsteller.getMail())
			.build();
	}

	@Nonnull
	private KindDTO toKindDTO(@Nonnull Kind kind) {
		return KindDTO.newBuilder()
			.setVorname(kind.getVorname())
			.setNachname(kind.getNachname())
			.setGeburtsdatum(kind.getGeburtsdatum())
			.build();
	}

	@Nonnull
	private ch.dvbern.kibon.exchange.commons.types.BetreuungsangebotTyp toBetreuungsangebotTyp(@Nonnull BetreuungsangebotTyp betreuungsangebotTyp) {
		if (betreuungsangebotTyp.isKita()) {
			return ch.dvbern.kibon.exchange.commons.types.BetreuungsangebotTyp.KITA;
		}
		return ch.dvbern.kibon.exchange.commons.types.BetreuungsangebotTyp.TAGESFAMILIEN;
	}
}
