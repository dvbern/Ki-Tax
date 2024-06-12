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

import ch.bedag.geres.schemas._20180101.geresresidentinfoservice.ResidentInfoPortType;
import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.services.PersonenSucheAuditLogService;

@ApplicationScoped
public class GeresClientProducer {

	@Produces
	@RequestScoped
	public GeresClient produceGeresClient(
		EbeguConfiguration configuration,
		PersonenSucheAuditLogService personenSucheAuditLogService,
		ResidentInfoPortType port) {

		if (configuration.isPersonenSucheDisabled() || configuration.usePersonenSucheDummyService()) {
			return new GeresDummyClient();
		}

		return new GeresProductiveClient(personenSucheAuditLogService, port);
	}
}
