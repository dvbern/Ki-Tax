/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.api.resource;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.security.DenyAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxBetreuungMonitoring;
import ch.dvbern.ebegu.api.dtos.JaxExternalClient;
import ch.dvbern.ebegu.services.BetreuungMonitoringService;
import ch.dvbern.ebegu.services.ExternalClientService;

import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

@Path("betreuungMonitoring")
@Stateless
@DenyAll
public class BetreuungMonitoringResource {

	@Inject
	private BetreuungMonitoringService betreuungMonitoringService;

	@Inject
	private ExternalClientService externalClientService;

	@Inject
	private JaxBConverter converter;

	@Nonnull
	@GET
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(SUPER_ADMIN)
	public List<JaxBetreuungMonitoring> getAllBetreuungMonitoringInfosBeiCriteria(
		@QueryParam("refNummer") @Nullable String referenzNummer,
		@QueryParam("benutzer") @Nullable String benutzer
	) {
		return betreuungMonitoringService.getAllBetreuungMonitoringBeiCriteria(referenzNummer, benutzer).stream()
			.map(betreuungMonitoring -> converter.betreuungMonitoringToJax(betreuungMonitoring))
			.collect(Collectors.toList());
	}

	@Nonnull
	@GET
	@Path("/allExternalClient")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(SUPER_ADMIN)
	public List<JaxExternalClient> getAllExternalClient() {
		return externalClientService.getAll().stream()
			.map(externalClient -> converter.externalClientToJAX(externalClient))
			.collect(Collectors.toList());
	}
}
