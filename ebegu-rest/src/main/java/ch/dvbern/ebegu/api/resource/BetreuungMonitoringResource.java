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
import javax.annotation.security.DenyAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxBetreuungMonitoring;
import ch.dvbern.ebegu.api.dtos.JaxInstitution;
import ch.dvbern.ebegu.services.BetreuungMonitoringService;
import io.swagger.annotations.ApiOperation;

import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

@Path("betreuungMonitoring")
@Stateless
@DenyAll
public class BetreuungMonitoringResource {

	@Inject
	private BetreuungMonitoringService betreuungMonitoringService;

	@Inject
	private JaxBConverter converter;

	@ApiOperation(value = "Find and return a list of all betreuung monitoring entries. "
		, responseContainer = "List", response = JaxInstitution.class)
	@Nonnull
	@GET
	@Path("/last")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(SUPER_ADMIN)
	public List<JaxBetreuungMonitoring> getAllBetreuungMonitoringInfos() { return betreuungMonitoringService.getAllBetreuungMonitoringInfos().stream()
			.map(betreuungMonitoring -> converter.betreuungMonitoringToJax(betreuungMonitoring))
			.collect(Collectors.toList());
	}

	@Nonnull
	@GET
	@Path("/{refnummer}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(SUPER_ADMIN)
	public List<JaxBetreuungMonitoring> getAllBetreuungMonitoringInfosBeiReferenzNummer(
		@Nonnull @NotNull @PathParam("refnummer") String referenzNummer
		) {
		return betreuungMonitoringService.getAllBetreuungMonitoringFuerRefNummer(referenzNummer).stream()
			.map(betreuungMonitoring -> converter.betreuungMonitoringToJax(betreuungMonitoring))
			.collect(Collectors.toList());
	}
}
