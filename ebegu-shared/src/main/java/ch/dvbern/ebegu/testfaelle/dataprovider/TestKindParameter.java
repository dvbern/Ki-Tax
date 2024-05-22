/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.testfaelle.dataprovider;

import java.time.LocalDate;

import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.enums.Geschlecht;
import ch.dvbern.ebegu.enums.Kinderabzug;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Builder
@Getter
public class TestKindParameter {
	@NonNull
	private final Kind kind;
	@NonNull
	private final Geschlecht geschlecht;
	@NonNull
	private final String name;
	@NonNull
	private final String vorname;
	@NonNull
	private final LocalDate geburtsdatum;
	private final Boolean is18GeburtstagBeforeGPEnds;
	private final Kinderabzug kinderabzug;
	private final boolean betreuung;
}
