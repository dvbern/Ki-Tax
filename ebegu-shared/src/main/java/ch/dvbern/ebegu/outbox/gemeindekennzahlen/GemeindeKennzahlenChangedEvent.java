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

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.outbox.ExportedEvent;
import org.apache.avro.Schema;
import org.jetbrains.annotations.NotNull;

public class GemeindeKennzahlenChangedEvent extends AbstractGemeindeKennzahlenEvent {

	public GemeindeKennzahlenChangedEvent(
		@Nonnull String gemeindeKennzahlenId,
		@Nonnull byte[] gemeindeKennzahlen,
		@Nonnull Schema schema) {
		super(gemeindeKennzahlenId, gemeindeKennzahlen, schema);
	}

	@NotNull
	@Override
	public String getType() {
		return "GemeindeKennzahlenChanged";
	}
}
