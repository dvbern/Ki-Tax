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

import java.util.Collections;
import java.util.Map;

import javax.xml.ws.Service;

import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.ws.ewk.sts.WSSSecurityGeresAssertionOutboundHandler;

public class GeresBernPortFactory extends AbstractGeresPortFactory {

	private final WSSSecurityGeresAssertionOutboundHandler wssUsernameTokenSecurityHandler;

	public GeresBernPortFactory(
		EbeguConfiguration configuration,
		WSSSecurityGeresAssertionOutboundHandler wssUsernameTokenSecurityHandler) {
		super(configuration);
		this.wssUsernameTokenSecurityHandler = wssUsernameTokenSecurityHandler;
	}

	@Override
	protected void customizeService(Service service) {
		// handler that adds assertion to header
		service.setHandlerResolver(portInfo -> Collections.singletonList(wssUsernameTokenSecurityHandler));
	}

	@Override
	protected void customizeRequestContext(Map<String, Object> requestContext) {
		// NOP
	}

	@Override
	protected String getGeresUrl() {
		return getConfig().getEbeguPersonensucheGERESEndpoint();
	}
}
