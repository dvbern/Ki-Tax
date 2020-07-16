/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxEinstellung;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.services.EinstellungService;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.services.GesuchsperiodeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

/**
 * Resource fuer Parameter
 */
@Path("einstellung")
@Stateless
@Api("Resource fuer Einstellungen")
@DenyAll // Absichtlich keine Rolle zugelassen, erzwingt, dass es für neue Methoden definiert werden muss
public class EinstellungResource {

	@Inject
	private EinstellungService einstellungService;

	@Inject
	private GesuchsperiodeService gesuchsperiodeService;

	@Inject
	private GemeindeService gemeindeService;

	@Inject
	private JaxBConverter converter;

	@ApiOperation(value = "Create a new or update an existing kiBon parameter with the given key and value",
		response = JaxEinstellung.class,
		consumes = MediaType.APPLICATION_JSON)
	@Nullable
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_GEMEINDE, ADMIN_BG, ADMIN_TS, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public Response saveEinstellung(
		@Nonnull @NotNull @Valid JaxEinstellung jaxEinstellung,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		Einstellung einstellung;
		if (jaxEinstellung.getId() != null) {
			Optional<Einstellung> optional = einstellungService.findEinstellung(jaxEinstellung.getId());
			einstellung = optional.orElse(new Einstellung());
		} else {
			einstellung = new Einstellung();
		}
		Einstellung convertedEinstellung = converter.einstellungToEntity(jaxEinstellung, einstellung);
		Einstellung persistedEinstellung = einstellungService.saveEinstellung(convertedEinstellung);

		URI uri = uriInfo.getBaseUriBuilder()
			.path(EinstellungResource.class)
			.path("/" + persistedEinstellung.getKey())
			.build();
		return Response.created(uri).entity(converter.einstellungToJAX(persistedEinstellung)).build();
	}

	@ApiOperation(value = "Get a specific kiBon parameter by key and date", response = JaxEinstellung.class)
	@Nullable
	@GET
	@Path("/key/{key}/gemeinde/{gemeindeId}/gp/{gesuchsperiodeId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public JaxEinstellung findEinstellung(
		@Nonnull @PathParam("key") String key,
		@Nonnull @PathParam("gemeindeId") String gemeindeId,
		@Nonnull @PathParam("gesuchsperiodeId") String gesuchsperiodeId) {

		EinstellungKey einstellungKey = EinstellungKey.valueOf(key);
		Gemeinde gemeinde = gemeindeService.findGemeinde(gemeindeId).orElseThrow(() -> new EbeguEntityNotFoundException("findEinstellung",
			ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, gemeindeId));
		Gesuchsperiode gp = gesuchsperiodeService.findGesuchsperiode(gesuchsperiodeId).orElseThrow(() -> new EbeguEntityNotFoundException("findEinstellung",
			ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, gesuchsperiodeId));

		return converter.einstellungToJAX(einstellungService.findEinstellung(einstellungKey, gemeinde, gp));
	}

	@ApiOperation(value = "Get all kiBon parameter for a specific Gesuchsperiode. The id of the gesuchsperiode is " +
		"passed  as a pathParam", responseContainer = "List", response = JaxEinstellung.class)
	@Nonnull
	@GET
	@Path("/gesuchsperiode/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public List<JaxEinstellung> getAllEinstellungenBySystem(
		@Nonnull @NotNull @PathParam("id") JaxId id) {

		Objects.requireNonNull(id.getId());
		String gesuchsperiodeId = converter.toEntityId(id);
		Optional<Gesuchsperiode> gesuchsperiode = gesuchsperiodeService.findGesuchsperiode(gesuchsperiodeId);
		return gesuchsperiode
			.map(value -> einstellungService.getAllEinstellungenBySystem(value)
				.stream()
				.map(einstellung -> converter.einstellungToJAX(einstellung))
				.collect(Collectors.toList())).orElse(Collections.emptyList());
	}

	@ApiOperation(value = "Get all kiBon parameter for a specific Gesuchsperiode. The id of the gesuchsperiode is " +
		"passed  as a pathParam", responseContainer = "List", response = JaxEinstellung.class)
	@Nonnull
	@GET
	@Path("/mandant/gesuchsperiode/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public List<JaxEinstellung> getAllEinstellungenByMandant(
		@Nonnull @NotNull @PathParam("id") JaxId id) {

		Objects.requireNonNull(id.getId());
		String gesuchsperiodeId = converter.toEntityId(id);
		Optional<Gesuchsperiode> gesuchsperiode = gesuchsperiodeService.findGesuchsperiode(gesuchsperiodeId);
		return gesuchsperiode
			.map(value -> einstellungService.getAllEinstellungenByMandant(value)
				.stream()
				.map(einstellung -> converter.einstellungToJAX(einstellung))
				.collect(Collectors.toList())).orElse(Collections.emptyList());
	}
}
