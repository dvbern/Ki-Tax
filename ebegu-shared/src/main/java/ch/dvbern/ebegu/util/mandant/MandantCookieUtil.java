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

package ch.dvbern.ebegu.util.mandant;

import javax.annotation.Nonnull;

public final class MandantCookieUtil {

	private MandantCookieUtil() {
	}

	@Nonnull
	public static MandantIdentifier convertCookieNameToMandantIdentifier(String mandantNameDecoded) {
		MandantIdentifier mandantIdentifier = null;

		switch (mandantNameDecoded) {
		case "Stadt Luzern":
			mandantIdentifier = MandantIdentifier.LUZERN;
			break;
		case "Appenzell Ausserrhoden":
			mandantIdentifier = MandantIdentifier.APPENZELL_AUSSERRHODEN;
			break;
		case "Kanton Solothurn":
			mandantIdentifier = MandantIdentifier.SOLOTHURN;
			break;
		case "Kanton Bern":
			mandantIdentifier = MandantIdentifier.BERN;
			break;
		case "Kanton Schwyz":
			mandantIdentifier = MandantIdentifier.SCHWYZ;
			break;
		default:
			throw new IllegalStateException("Unexpected mandant: " + mandantNameDecoded);
		}
		return mandantIdentifier;
	}
}
