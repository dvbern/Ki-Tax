/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.api.resource;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxDossier;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.services.DossierService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.Validate;

/**
 * Resource fuer Dossier
 */
@Path("dossier")
@Stateless
@Api(description = "Resource für Dossier (Fall in einer Gemeinde)")
public class DossierResource {

	@Inject
	private DossierService dossierService;

	@Inject
	private JaxBConverter converter;


	@ApiOperation(value = "Erstellt ein Dossier in der Datenbank", response = JaxDossier.class)
	@Nonnull
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response create(
		@Nonnull @NotNull JaxDossier dossierJax,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		Dossier dossierToMerge = new Dossier();
		if (dossierJax.getTimestampErstellt() != null) {
			Objects.requireNonNull(dossierJax.getGemeinde());
			Objects.requireNonNull(dossierJax.getGemeinde().getId());
			Objects.requireNonNull(dossierJax.getFall());
			Objects.requireNonNull(dossierJax.getFall().getId());
			Optional<Dossier> dossierByGemeindeAndFall = dossierService.findDossierByGemeindeAndFall(
				dossierJax.getGemeinde().getId(), dossierJax.getFall().getId());
			if (dossierByGemeindeAndFall.isPresent()) {
				dossierToMerge = dossierByGemeindeAndFall.get();
			}
		}
		Dossier convertedDossier = converter.dossierToEntity(dossierJax, dossierToMerge);
		Dossier persistedDossier = this.dossierService.saveDossier(convertedDossier);

		URI uri = uriInfo.getBaseUriBuilder()
			.path(DossierResource.class)
			.path('/' + persistedDossier.getId())
			.build();

		JaxDossier jaxDossier = converter.dossierToJAX(persistedDossier);
		return Response.created(uri).entity(jaxDossier).build();
	}

	@ApiOperation(value = "Returns the Dossier with the given Id.", response = JaxDossier.class)
	@Nullable
	@GET
	@Path("/id/{dossierId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public JaxDossier findDossier(
		@Nonnull @NotNull @PathParam("dossierId") JaxId dossierJAXPId) {
		Validate.notNull(dossierJAXPId.getId());
		String dossierId = converter.toEntityId(dossierJAXPId);
		Optional<Dossier> dossierOptional = dossierService.findDossier(dossierId);

		if (!dossierOptional.isPresent()) {
			return null;
		}
		return converter.dossierToJAX(dossierOptional.get());
	}

	@ApiOperation(value = "Returns the Dossier of the given Fall for the given Gemeinde.", response = JaxDossier.class)
	@Nullable
	@GET
	@Path("/gemeinde/{gemeindeId}/fall/{fallId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public JaxDossier getDossierForFallAndGemeinde(
		@Nonnull @NotNull @PathParam("gemeindeId") JaxId gemeindeJaxId,
		@Nonnull @NotNull @PathParam("fallId") JaxId fallJaxId) {

		Validate.notNull(gemeindeJaxId.getId());
		Validate.notNull(fallJaxId.getId());

		String gemeindeId = converter.toEntityId(gemeindeJaxId);
		String fallId = converter.toEntityId(fallJaxId);
		Optional<Dossier> dossierOptional = dossierService.findDossierByGemeindeAndFall(gemeindeId, fallId);
		if (!dossierOptional.isPresent()) {
			return null;
		}
		return converter.dossierToJAX(dossierOptional.get());
	}

	@ApiOperation(value = "Creates a new Dossier in the database if it doesnt exist with the current user as owner.", response = JaxDossier.class)
	@Nullable
	@PUT
	@Path("/createforcurrentbenutzer/{gemeindeId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public JaxDossier getOrCreateDossierAndFallForCurrentUserAsBesitzer(
		@Nonnull @NotNull @PathParam("gemeindeId") JaxId gemeindeJaxId,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		Validate.notNull(gemeindeJaxId.getId());
		String gemeindeId = converter.toEntityId(gemeindeJaxId);

		Dossier dossier = dossierService.getOrCreateDossierAndFallForCurrentUserAsBesitzer(gemeindeId);
		return converter.dossierToJAX(dossier);
	}
}
