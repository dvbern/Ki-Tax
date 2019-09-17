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

package ch.dvbern.ebegu.outbox.verfuegung;

import java.util.Arrays;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.outbox.ExportedEvent;

public class VerfuegungVerfuegtEvent implements ExportedEvent {

	/**
	 * Die BG Nummer bleibt immer dieselbe, auch wenn es eine neue Version der Verfügung (z.B. nach Mutationsmeldung)
	 * gibt. Es wird deshalb die BG Nummer statt der entity ID als aggregateId verwendet.
	 */
	@Nonnull
	private final String bgNummer;

	@Nonnull
	private final byte[] verfuegung;

	public VerfuegungVerfuegtEvent(@Nonnull String bgNummer, @Nonnull byte[] verfuegung) {
		this.bgNummer = bgNummer;
		this.verfuegung = Arrays.copyOf(verfuegung, verfuegung.length);
	}

	@Nonnull
	@Override
	public String getAggregateType() {
		return "Verfuegung";
	}

	@Nonnull
	@Override
	public String getAggregateId() {
		return bgNummer;
	}

	@Nonnull
	@Override
	public String getType() {
		return "VerfuegungVerfuegt";
	}

	@Nonnull
	@Override
	public byte[] getPayload() {
		return Arrays.copyOf(verfuegung, verfuegung.length);
	}
}
