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

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import javax.annotation.Nonnull;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public final class MandantCookieUtil {

	public static final String COOKIE_VALUE_STADT_LUZERN = "Stadt Luzern";
	public static final String COOKIE_VALUE_APPENZELL_AUSSERRHODEN = "Appenzell Ausserrhoden";
	public static final String COOKIE_VALUE_KANTON_SOLOTHURN = "Kanton Solothurn";
	public static final String COOKIE_VALUE_KANTON_BERN = "Kanton Bern";
	public static final String COOKIE_VALUE_KANTON_SCHWYZ = "Kanton Schwyz";
	public static final String MANDANT_COOKIE_NAME = "mandant";

	private MandantCookieUtil() {
	}

	@Nonnull
	public static MandantIdentifier convertCookieNameToMandantIdentifier(String mandantNameDecoded) {
		MandantIdentifier mandantIdentifier = null;

		switch (mandantNameDecoded) {
		case COOKIE_VALUE_STADT_LUZERN:
			mandantIdentifier = MandantIdentifier.LUZERN;
			break;
		case COOKIE_VALUE_APPENZELL_AUSSERRHODEN:
			mandantIdentifier = MandantIdentifier.APPENZELL_AUSSERRHODEN;
			break;
		case COOKIE_VALUE_KANTON_SOLOTHURN:
			mandantIdentifier = MandantIdentifier.SOLOTHURN;
			break;
		case COOKIE_VALUE_KANTON_BERN:
			mandantIdentifier = MandantIdentifier.BERN;
			break;
		case COOKIE_VALUE_KANTON_SCHWYZ:
			mandantIdentifier = MandantIdentifier.SCHWYZ;
			break;
		default:
			throw new IllegalStateException("Unexpected mandant: " + mandantNameDecoded);
		}
		return mandantIdentifier;
	}

	@Nonnull
	public static MandantIdentifier getMandantFromCookie(HttpServletRequest request) {
		if (request.getCookies() != null) {
			for (Cookie cookie : request.getCookies()) {
				if (cookie.getName().equals(MANDANT_COOKIE_NAME)) {
					var mandantNameDecoded = URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8);
					return convertCookieNameToMandantIdentifier(mandantNameDecoded);
				}
			}
		}
		throw new IllegalStateException("mandant cookie is missing");
	}

}
