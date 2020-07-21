/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
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

import java.util.Objects;
import java.util.Optional;

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
import javax.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxFall;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.services.FallService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.GESUCHSTELLER;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

/**
 * Resource fuer Fall
 */
@Path("falle")
@Stateless
@Api(description = "Resource zum Verwalten von Fällen (Familien)")
@DenyAll // Absichtlich keine Rolle zugelassen, erzwingt, dass es für neue Methoden definiert werden muss
public class FallResource {

	@Inject
	private FallService fallService;

	@Inject
	private JaxBConverter converter;

	@ApiOperation(value = "Creates a new Fall in the database. The transfer object also has a relation to Gesuch " +
		"which is stored in the database as well.", response = JaxFall.class)
	@Nonnull
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, GESUCHSTELLER, SACHBEARBEITER_TS, ADMIN_TS })
	public JaxFall saveFall(
		@Nonnull @NotNull @Valid JaxFall fallJAXP,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		Fall fall = new Fall();
		if (fallJAXP.getId() != null) {
			Optional<Fall> optional = fallService.findFall(fallJAXP.getId());
			fall = optional.orElse(new Fall());
		}
		Fall convertedFall = converter.fallToEntity(fallJAXP, fall);

		Fall persistedFall = this.fallService.saveFall(convertedFall);
		return converter.fallToJAX(persistedFall);
	}

	@ApiOperation(value = "Returns the Fall with the given Id.", response = JaxFall.class)
	@Nullable
	@GET
	@Path("/id/{fallId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Grundsaetzliche fuer alle Rollen: Datenabhaengig. -> Authorizer
	public JaxFall findFall(
		@Nonnull @NotNull @PathParam("fallId") JaxId fallJAXPId) {
		Objects.requireNonNull(fallJAXPId.getId());
		String fallID = converter.toEntityId(fallJAXPId);
		Optional<Fall> fallOptional = fallService.findFall(fallID);

		if (!fallOptional.isPresent()) {
			return null;
		}
		Fall fallToReturn = fallOptional.get();
		return converter.fallToJAX(fallToReturn);
	}
}
