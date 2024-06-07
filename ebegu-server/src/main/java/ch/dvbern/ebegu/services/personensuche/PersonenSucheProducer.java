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
 *
 */

package ch.dvbern.ebegu.services.personensuche;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.servlet.http.HttpServletRequest;

import ch.dvbern.ebegu.util.mandant.MandantCookieUtil;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import ch.dvbern.ebegu.ws.ewk.GeresClient;

@ApplicationScoped
public class PersonenSucheProducer {

	@Produces
	@RequestScoped
	public PersonenSucheService getPersonenSucheService(HttpServletRequest request, GeresClient geresClient) {
		MandantIdentifier mandant = MandantCookieUtil.getMandantFromCookie(request);
		switch (mandant) {
		case BERN:
			return new PersonenSucheBernService(geresClient);
		case SCHWYZ:
			return new PersonenSucheSchwyzService(geresClient);
		default:
			throw new IllegalStateException(String.format("Personensuche not implemented for Mandant %s", mandant.name()));
		}
	}
}
