/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

import javax.annotation.Nonnull;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.dto.SupportAnfrageDTO;
import ch.dvbern.ebegu.services.MailService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * Resource fuer Supportanfragen
 */
@Path("support")
@Stateless
@Api("Resource zum Senden von Supportanfragen")
@PermitAll
public class SupportResource {

	@Inject
	private MailService mailService;


	@ApiOperation("Sendet eine Supportanfrage an die definierte Supportadresse")
	@Nonnull
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public Response sendSupportAnfrage(
		@Nonnull @NotNull @Valid SupportAnfrageDTO fallJAXP,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		mailService.sendSupportAnfrage(fallJAXP);
		return Response.ok().build();
	}
}
