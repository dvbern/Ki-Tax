/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

/**
 * This Enum has values that indicate through which method the user is being invited to Kibon.
 */
public enum EinladungTyp {

	// An invitation to a new user of a Gemeinde
	MITARBEITER,

	// When a new Gemeinde is created a user must be invited as the admin of this Gemeinde
	GEMEINDE,

	// When a new Institution is created a user must be invited as the admin of this Institution
	INSTITUTION,

	// When a new Traegerschaft is created a user must be invited as the admin of this Traegerschaft
	TRAEGERSCHAFT
}
