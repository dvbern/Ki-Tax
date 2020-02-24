/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxErwerbspensumContainer;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.dtos.JaxSocialhilfeZeitraumContainer;
import ch.dvbern.ebegu.entities.FamiliensituationContainer;
import ch.dvbern.ebegu.entities.SocialhilfeZeitraumContainer;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.services.FamiliensituationService;
import ch.dvbern.ebegu.services.SocialhilfeZeitraumService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Path("socialhilfeZeitraeume")
@Stateless
@Api(description = "Resource fuer Socialhilfe Zeitraeume")
public class SocialhilfeZeitraumResource {

	@Inject
	private JaxBConverter converter;
	@Inject
	private SocialhilfeZeitraumService socialhilfeZeitraumService;
	@Inject
	private FamiliensituationService familiensituationService;

	@ApiOperation(value = "Create a new ErwerbspensumContainer in the database. The object also has a relations to " +
		"Erwerbspensum data Objects, those will be created as well", response = JaxErwerbspensumContainer.class)
	@Nonnull
	@PUT
	@Path("/{famSitId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public JaxSocialhilfeZeitraumContainer saveSocialhilfeZeitraum(
		@Nonnull @NotNull @PathParam("famSitId") JaxId famSitId,
		@Nonnull @NotNull @Valid JaxSocialhilfeZeitraumContainer jaxSocialhilfeZeitraumContainer,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) throws EbeguEntityNotFoundException {

		FamiliensituationContainer famSit =
			familiensituationService.findFamiliensituation(famSitId.getId()).orElseThrow(() -> new EbeguEntityNotFoundException("saveSocialhilfeZeitraum", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, "Familliensituation id invalid: " + famSitId.getId()));
		SocialhilfeZeitraumContainer convertedSocialhilfeZeitraumContainer =
			converter.socialhilfeZeitraumContainerToStorableEntity(jaxSocialhilfeZeitraumContainer);

		convertedSocialhilfeZeitraumContainer.setFamiliensituationContainer(famSit);

		SocialhilfeZeitraumContainer storedShZCont =
			socialhilfeZeitraumService.saveSocialhilfeZeitraum(convertedSocialhilfeZeitraumContainer);

		JaxSocialhilfeZeitraumContainer jaxShzCont = converter.socialhilfeZeitraumContainerToJAX(storedShZCont);
		return jaxShzCont;
	}

	@ApiOperation(value = "Remove the SocialhilfeZeitraum Container with the specified ID from the database.",
		response = Void.class)
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	@Nullable
	@DELETE
	@Path("/socialhilfeZeitraumId/{socialhilfeZeitraumContID}")
	@Consumes(MediaType.WILDCARD)
	public Response removeSocialhilfeZeitraum(
		@Nonnull @NotNull @PathParam("socialhilfeZeitraumContID") JaxId socialhilfeZeitraumContIDJAXPId,
		@Context HttpServletResponse response) {

		Objects.requireNonNull(socialhilfeZeitraumContIDJAXPId.getId());
		socialhilfeZeitraumService.removeSocialhilfeZeitraum(converter.toEntityId(socialhilfeZeitraumContIDJAXPId));

		return Response.ok().build();
	}

}
