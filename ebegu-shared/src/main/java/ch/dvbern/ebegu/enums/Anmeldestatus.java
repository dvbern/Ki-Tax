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

package ch.dvbern.ebegu.enums;

import java.util.EnumSet;
import java.util.Set;

/**
 * Enum fuers Feld status in einer Anmeldung.
 */
public enum Anmeldestatus {

	SCHULAMT_ANMELDUNG_ERFASST,
	SCHULAMT_ANMELDUNG_AUSGELOEST,
	SCHULAMT_ANMELDUNG_UEBERNOMMEN,
	SCHULAMT_ANMELDUNG_ABGELEHNT,
	SCHULAMT_FALSCHE_INSTITUTION;

	private static final Set<Anmeldestatus> all = EnumSet.allOf(Anmeldestatus.class);
	private static final Set<Anmeldestatus> none = EnumSet.noneOf(Anmeldestatus.class);

	public static final Set<Anmeldestatus> forPendenzInstitution = EnumSet.of(SCHULAMT_ANMELDUNG_AUSGELOEST);
	public static final Set<Anmeldestatus> forPendenzSchulamt = EnumSet.of(SCHULAMT_ANMELDUNG_AUSGELOEST, SCHULAMT_FALSCHE_INSTITUTION);
	public static final Set<Anmeldestatus> anmeldungsstatusAusgeloest = EnumSet.of(SCHULAMT_ANMELDUNG_AUSGELOEST,
		SCHULAMT_ANMELDUNG_UEBERNOMMEN, SCHULAMT_ANMELDUNG_ABGELEHNT, SCHULAMT_FALSCHE_INSTITUTION);

	/**
	 * Alle SCH-Status, die ausgeloest sind, gelten als geschlossen, da sie im Verfuegungsprozess nicht beruecksichtigt werden.
	 */
	public boolean isGeschlossen() {
		return SCHULAMT_ANMELDUNG_UEBERNOMMEN == this || SCHULAMT_ANMELDUNG_ABGELEHNT == this || SCHULAMT_ANMELDUNG_AUSGELOEST == this
			|| SCHULAMT_FALSCHE_INSTITUTION == this;
	}

	public boolean isAnyStatusOfVerfuegt() {
		return SCHULAMT_ANMELDUNG_UEBERNOMMEN == this || SCHULAMT_ANMELDUNG_ABGELEHNT == this;
	}

	@SuppressWarnings({"Duplicates", "checkstyle:CyclomaticComplexity"})
	public static Set<Anmeldestatus> allowedRoles(UserRole userRole) {
		switch (userRole) {
		case SUPER_ADMIN:
		case ADMIN_BG:
		case GESUCHSTELLER:
		case JURIST:
		case REVISOR:
		case ADMIN_INSTITUTION:
		case SACHBEARBEITER_INSTITUTION:
		case SACHBEARBEITER_BG:
		case ADMIN_TRAEGERSCHAFT:
		case SACHBEARBEITER_TRAEGERSCHAFT:
		case SACHBEARBEITER_TS:
		case ADMIN_GEMEINDE:
		case SACHBEARBEITER_GEMEINDE:
		case STEUERAMT:
		case ADMIN_MANDANT:
		case SACHBEARBEITER_MANDANT:
			return all;
		default:
			return none;
		}
	}
}
