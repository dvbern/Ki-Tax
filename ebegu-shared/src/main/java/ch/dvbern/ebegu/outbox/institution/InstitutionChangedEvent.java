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

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.outbox.ExportedEvent;

public class InstitutionChangedEvent implements ExportedEvent {

	@Nonnull
	final String institutionId;

	@Nonnull
	final byte[] institution;

	public InstitutionChangedEvent(@Nonnull String institutionId, @Nonnull byte[] institution) {
		this.institutionId = institutionId;
		this.institution = institution;
	}

	@Nonnull
	@Override
	public String getAggregateType() {
		return "Institution";
	}

	@Nonnull
	@Override
	public String getAggregateId() {
		return institutionId;
	}

	@Nonnull
	@Override
	public String getType() {
		return "InstitutionChanged";
	}

	@Nonnull
	@Override
	public byte[] getPayload() {
		return institution;
	}
}
