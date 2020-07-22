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

import java.util.Arrays;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.outbox.ExportedEvent;
import org.apache.avro.Schema;

public class BetreuungAnfrageAddedEvent implements ExportedEvent {

	@Nonnull
	private final String betreuungId;

	@Nonnull
	private final byte[] betreuungAnfrage;

	@Nonnull
	private final Schema schema;

	public BetreuungAnfrageAddedEvent(
		@Nonnull String betreuungId,
		@Nonnull byte[] betreuungAnfrage,
		@Nonnull Schema schema) {
		this.betreuungId = betreuungId;
		this.betreuungAnfrage = Arrays.copyOf(betreuungAnfrage, betreuungAnfrage.length);
		this.schema = schema;
	}

	@Nonnull
	@Override
	public String getAggregateType() {
		return "BetreuungAnfrage";
	}

	@Nonnull
	@Override
	public String getAggregateId() {
		return betreuungId;
	}

	@Nonnull
	@Override
	public String getType() {
		return "BetreuungAnfrageAdded";
	}

	@Nonnull
	@Override
	public byte[] getPayload() {
		return Arrays.copyOf(betreuungAnfrage, betreuungAnfrage.length);
	}

	@Nonnull
	@Override
	public Schema getSchema() {
		return schema;
	}
}
