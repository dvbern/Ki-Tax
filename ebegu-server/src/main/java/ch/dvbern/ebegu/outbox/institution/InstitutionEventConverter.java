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
import javax.enterprise.context.ApplicationScoped;

import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.Traegerschaft;
import ch.dvbern.ebegu.outbox.EventConverterUtil;
import ch.dvbern.kibon.exchange.commons.institution.AdresseDTO;
import ch.dvbern.kibon.exchange.commons.institution.InstitutionEventDTO;

@ApplicationScoped
public class InstitutionEventConverter {

	@Nonnull
	public InstitutionChangedEvent of(@Nonnull InstitutionStammdaten stammdaten) {
		InstitutionEventDTO dto = toInstitutionEventDTO(stammdaten);
		byte[] payload = EventConverterUtil.toJsonB(dto);

		return new InstitutionChangedEvent(stammdaten.getInstitution().getId(), payload);
	}

	@Nonnull
	private InstitutionEventDTO toInstitutionEventDTO(@Nonnull InstitutionStammdaten stammdaten) {
		Institution institution = stammdaten.getInstitution();

		String traegerschaft = Optional.ofNullable(institution.getTraegerschaft())
			.map(Traegerschaft::getName)
			.orElse(null);

		Adresse adr = stammdaten.getAdresse();
		AdresseDTO adresse = new AdresseDTO(
			adr.getStrasse(),
			adr.getHausnummer(),
			adr.getZusatzzeile(),
			adr.getPlz(),
			adr.getOrt(),
			adr.getLand().name()
		);

		return new InstitutionEventDTO(institution.getId(), institution.getName(), traegerschaft, adresse);
	}
}
