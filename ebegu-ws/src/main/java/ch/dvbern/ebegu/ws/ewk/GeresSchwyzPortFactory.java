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

import java.util.Map;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import ch.dvbern.ebegu.config.EbeguConfiguration;

public class GeresSchwyzPortFactory extends AbstractGeresPortFactory {

	GeresSchwyzPortFactory(
		EbeguConfiguration config) {
		super(config);
	}

	@Override
	protected String getGeresUrl() {
		return getConfig().getGeresSchwyzEndpointUrl();
	}

	@Override
	protected void customizeService(Service service) {
		// NOP
	}

	@Override
	protected void customizeRequestContext(Map<String, Object> requestContext) {
		requestContext.put(BindingProvider.USERNAME_PROPERTY, getConfig().getGeresSchwyzUsername());
		requestContext.put(BindingProvider.PASSWORD_PROPERTY, getConfig().getGeresSchwyzPassword());
	}
}
