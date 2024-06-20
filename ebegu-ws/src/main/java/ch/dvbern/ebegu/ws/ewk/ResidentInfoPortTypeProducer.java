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

package ch.dvbern.ebegu.ws.ewk;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.servlet.http.HttpServletRequest;

import ch.bedag.geres.schemas._20180101.geresresidentinfoservice.ResidentInfoPortType;
import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.errors.PersonenSucheServiceException;
import ch.dvbern.ebegu.util.mandant.MandantCookieUtil;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import ch.dvbern.ebegu.ws.ewk.sts.WSSSecurityGeresAssertionOutboundHandler;

@ApplicationScoped
public class ResidentInfoPortTypeProducer {
	@Produces
	@RequestScoped
	public ResidentInfoPortType createResidentInfoPortType(
		HttpServletRequest request,
		WSSSecurityGeresAssertionOutboundHandler wssUsernameTokenSecurityHandler,
		EbeguConfiguration configuration) throws PersonenSucheServiceException {

		MandantIdentifier mandant = MandantCookieUtil.getMandantFromCookie(request);
		switch (mandant) {
		case BERN:
			return new GeresBernPortFactory(configuration, wssUsernameTokenSecurityHandler).getPort();
		case SCHWYZ:
			return new GeresSchwyzPortFactory(configuration).getPort();
		default:
			throw new IllegalStateException(String.format("GERES not configured for Mandant %s", mandant));
		}
	}
}
