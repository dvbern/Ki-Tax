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

package ch.dvbern.ebegu.services.famsitchangehandler;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import javax.annotation.Nonnull;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import ch.dvbern.ebegu.services.EinstellungService;
import ch.dvbern.ebegu.services.FinanzielleSituationService;
import ch.dvbern.ebegu.services.GesuchstellerService;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;

@ApplicationScoped
public class FamSitChangeHandlerProducer {

	@Produces
	@RequestScoped
	public FamSitChangeHandler produceFinSitResetService(
		EinstellungService einstellungService,
		GesuchstellerService gesuchstellerService,
		HttpServletRequest request,
		FinanzielleSituationService finanzielleSituationService) {
		switch (getMandantFromCookie(request)) {
		case LUZERN:
			return new FamSitChangeHandlerLUBean(gesuchstellerService, einstellungService, finanzielleSituationService);
		case APPENZELL_AUSSERRHODEN:
			return new FamSitChangeHandlerARBean(gesuchstellerService, einstellungService, finanzielleSituationService);
		default:
			return new FamSitChangeHandlerBernBean(gesuchstellerService, einstellungService, finanzielleSituationService);
		}
	}

	private MandantIdentifier getMandantFromCookie(HttpServletRequest request) {
		String cookieName = "mandant";
		if (request.getCookies() != null) {
			for (Cookie cookie : request.getCookies()) {
				if (cookie.getName().equals(cookieName)) {
					var mandantNameDecoded = URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8);
					return convertCookieNameToMandantIdentifier(mandantNameDecoded);
				}
			}
		}
		throw new IllegalStateException("mandant Cookie is missing");
	}

	@Nonnull
	private static MandantIdentifier convertCookieNameToMandantIdentifier(String mandantNameDecoded) {
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
