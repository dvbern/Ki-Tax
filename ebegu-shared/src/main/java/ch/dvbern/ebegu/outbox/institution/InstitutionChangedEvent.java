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

import java.util.Arrays;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.outbox.ExportedEvent;
import org.apache.avro.Schema;

public class InstitutionChangedEvent implements ExportedEvent {

	@Nonnull
	private final String institutionId;

	@Nonnull
	private final byte[] institution;

	@Nonnull
	private final Schema schema;

	public InstitutionChangedEvent(
		@Nonnull String institutionId,
		@Nonnull byte[] institution,
		@Nonnull Schema schema) {
		this.institutionId = institutionId;
		this.institution = Arrays.copyOf(institution, institution.length);
		this.schema = schema;
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
		return Arrays.copyOf(institution, institution.length);
	}

	@Nonnull
	@Override
	public Schema getSchema() {
		return schema;
	}
}
