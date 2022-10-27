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

package ch.dvbern.ebegu.kafka;

import java.util.Arrays;
import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * All known event types.
 */
public enum EventType {
	PLATZBESTAETIGUNG_BETREUUNG("PlatzbestaetigungBetreuung"),
	BETREUUNG_STORNIERUNG_ANFRAGE("BetreuungStornierungAnfrage"),
	TAGESSCHULE_ANMELDUNG_BESTAETIGUNG("TagesschuleAnmeldungBestaetigung"),
	ANMELDUNG_ABLEHNEN_ANFRAGE("AnmeldungAblehnenAnfrage"),
	NEUE_VERANLAGUNG("NeueVeranlagung");

	private final String name;

	@Nonnull
	EventType(String name) {
		this.name = name;
	}

	@Nonnull
	public static Optional<EventType> of(@Nonnull String name) {
		return Arrays.stream(values())
			.filter(value -> value.getName().equals(name))
			.findAny();
	}

	@Nonnull
	public String getName() {
		return name;
	}
}
