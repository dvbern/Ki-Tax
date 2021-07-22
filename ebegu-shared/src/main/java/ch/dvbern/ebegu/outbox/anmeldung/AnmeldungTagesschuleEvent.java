/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.outbox.anmeldung;

import java.util.Arrays;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.outbox.ExportedEvent;
import org.apache.avro.Schema;
import org.jetbrains.annotations.NotNull;

public class AnmeldungTagesschuleEvent implements ExportedEvent {

	@Nonnull
	private final String anmeldungId;

	@Nonnull
	private final byte[] anmeldung;

	@Nonnull
	private final Schema schema;

	public AnmeldungTagesschuleEvent(
		@Nonnull String anmeldungId,
		@Nonnull byte[] anmeldung,
		@Nonnull Schema schema) {
		this.anmeldungId = anmeldungId;
		this.anmeldung = Arrays.copyOf(anmeldung, anmeldung.length);
		this.schema = schema;
	}

	@NotNull
	@Override
	public String getAggregateType() {
		return "Anmeldung";
	}

	@NotNull
	@Override
	public String getAggregateId() {
		return anmeldungId;
	}

	@NotNull
	@Override
	public String getType() {
		return "AnmeldungTagesschule";
	}

	@NotNull
	@Override
	public byte[] getPayload() {
		return Arrays.copyOf(anmeldung, anmeldung.length);
	}

	@NotNull
	@Override
	public Schema getSchema() {
		return schema;
	}
}
