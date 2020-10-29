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

package ch.dvbern.ebegu.outbox.institutionclient;

import javax.annotation.Nonnull;
import javax.enterprise.context.ApplicationScoped;

import ch.dvbern.ebegu.entities.InstitutionExternalClient;
import ch.dvbern.kibon.exchange.commons.institutionclient.InstitutionClientEventDTO;
import ch.dvbern.kibon.exchange.commons.util.AvroConverter;

@ApplicationScoped
public class InstitutionClientEventConverter {

	@Nonnull
	public InstitutionClientAddedEvent clientAddedEventOf(
		@Nonnull String institutionId,
		@Nonnull InstitutionExternalClient client) {

		InstitutionClientEventDTO dto = toInstitutionClientEventDTO(institutionId, client);
		byte[] payload = AvroConverter.toAvroBinary(dto);

		return new InstitutionClientAddedEvent(institutionId, payload, dto.getSchema());
	}

	@Nonnull
	public InstitutionClientRemovedEvent clientRemovedEventOf(
		@Nonnull String institutionId,
		@Nonnull InstitutionExternalClient client) {

		InstitutionClientEventDTO dto = toInstitutionClientEventDTO(institutionId, client);
		byte[] payload = AvroConverter.toAvroBinary(dto);

		return new InstitutionClientRemovedEvent(institutionId, payload, dto.getSchema());
	}

	@Nonnull
	public InstitutionClientModifiedEvent clientModifiedEventOf(
		@Nonnull String institutionId,
		@Nonnull InstitutionExternalClient client) {

		InstitutionClientEventDTO dto = toInstitutionClientEventDTO(institutionId, client);
		byte[] payload = AvroConverter.toAvroBinary(dto);

		return new InstitutionClientModifiedEvent(institutionId, payload, dto.getSchema());
	}

	@Nonnull
	private InstitutionClientEventDTO toInstitutionClientEventDTO(
		@Nonnull String institutionId,
		@Nonnull InstitutionExternalClient client) {

		return InstitutionClientEventDTO.newBuilder()
			.setInstitutionId(institutionId)
			.setClientName(client.getExternalClient().getClientName())
			.setClientType(client.getExternalClient().getType().name())
			.setGueltigAb(client.getGueltigkeit().getGueltigAb())
			.setGueltigBis(client.getGueltigkeit().getGueltigBis())
			.build();
	}
}
