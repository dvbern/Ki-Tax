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

package ch.dvbern.ebegu.outbox.institution;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.enterprise.context.ApplicationScoped;

import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.Traegerschaft;
import ch.dvbern.kibon.exchange.commons.institution.AdresseDTO;
import ch.dvbern.kibon.exchange.commons.institution.InstitutionEventDTO;
import ch.dvbern.kibon.exchange.commons.util.AvroConverter;

@ApplicationScoped
public class InstitutionEventConverter {

	@Nonnull
	public InstitutionChangedEvent of(@Nonnull InstitutionStammdaten stammdaten) {
		InstitutionEventDTO dto = toInstitutionEventDTO(stammdaten);
		byte[] payload = AvroConverter.toAvroBinary(dto);

		return new InstitutionChangedEvent(stammdaten.getInstitution().getId(), payload, dto.getSchema());
	}

	@Nonnull
	private InstitutionEventDTO toInstitutionEventDTO(@Nonnull InstitutionStammdaten stammdaten) {
		Institution institution = stammdaten.getInstitution();

		return InstitutionEventDTO.newBuilder()
			.setId(institution.getId())
			.setName(institution.getName())
			.setTraegerschaft(getTraegerschaft(institution))
			.setAdresse(toAdresseDTO(stammdaten.getAdresse()))
			.build();
	}

	@Nullable
	private String getTraegerschaft(@Nonnull Institution institution) {
		return Optional.ofNullable(institution.getTraegerschaft())
			.map(Traegerschaft::getName)
			.orElse(null);
	}

	@Nonnull
	private AdresseDTO toAdresseDTO(@Nonnull Adresse adr) {
		return AdresseDTO.newBuilder()
			.setStrasse(adr.getStrasse())
			.setHausnummer(adr.getHausnummer())
			.setAdresszusatz(adr.getZusatzzeile())
			.setPlz(adr.getPlz())
			.setOrt(adr.getOrt())
			.setLand(adr.getLand().name())
			.build();
	}
}
