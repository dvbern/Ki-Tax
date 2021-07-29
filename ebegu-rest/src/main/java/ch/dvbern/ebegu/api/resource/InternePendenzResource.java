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

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.security.DenyAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.dtos.JaxInternePendenz;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.InternePendenz;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.services.Authorizer;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.services.InternePendenzService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

/**
 * REST Resource fuer die internen Pendenzen
 */
@Path("gesuch/internependenz")
@Stateless
@Api(description = "Resource fuer die internen Pendenzen")
@DenyAll // Absichtlich keine Rolle zugelassen, erzwingt, dass es für neue Methoden definiert werden muss
public class InternePendenzResource {

	@Inject
	GesuchService gesuchService;

	@Inject
	Authorizer authorizer;

	@Inject
	InternePendenzService internePendenzService;

	@Inject
	JaxBConverter jaxBConverter;


	@ApiOperation(
		value = "Aktualisiert interne Pendenz",
		response = JaxInternePendenz.class)
	@Nullable
	@PUT
	@Path("/")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		ADMIN_BG, SACHBEARBEITER_BG, ADMIN_TS, SACHBEARBEITER_TS, })
	public JaxInternePendenz updateInternePendenz(
		@Nonnull @NotNull @Valid JaxInternePendenz jaxInternePendenz
	) {
		Objects.requireNonNull(jaxInternePendenz);
		Objects.requireNonNull(jaxInternePendenz.getId());
		InternePendenz internePendenz = internePendenzService.findInternePendenz(jaxInternePendenz.getId())
			.orElseThrow(() -> new EbeguEntityNotFoundException(
				"updateInternePendenz",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				jaxInternePendenz.getId())
			);

		jaxBConverter.internePendenzToEntity(jaxInternePendenz, internePendenz);
		InternePendenz persisted = internePendenzService.updateInternePendenz(internePendenz);
		if(internePendenzService.countInternePendenzenForGesuch(internePendenz.getGesuch()) == 0){
			internePendenz.getGesuch().setInternePendenz(false);
			gesuchService.updateGesuch(internePendenz.getGesuch(), false);
		}
		else if (!internePendenz.getGesuch().getInternePendenz()){
			internePendenz.getGesuch().setInternePendenz(true);
			gesuchService.updateGesuch(internePendenz.getGesuch(), false);
		}
		return jaxBConverter.internePendenzToJax(persisted);
	}

	@ApiOperation(
		value = "Erstellt eine neue interne Pendenz",
		response = JaxInternePendenz.class)
	@Nullable
	@POST
	@Path("/")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		ADMIN_BG, SACHBEARBEITER_BG, ADMIN_TS, SACHBEARBEITER_TS, })
	public JaxInternePendenz createInternePendenz(
		@Nonnull @NotNull @Valid JaxInternePendenz jaxInternePendenz
	) {
		Objects.requireNonNull(jaxInternePendenz);
		InternePendenz internePendenz = jaxBConverter.internePendenzToEntity(jaxInternePendenz, new InternePendenz());
		InternePendenz created = internePendenzService.createInternePendenz(internePendenz);
		return jaxBConverter.internePendenzToJax(created);
	}


	@ApiOperation("Löscht die interne Pendenz")
	@Nullable
	@DELETE
	@Path("/{internePendenzId}")
	@Consumes(MediaType.WILDCARD)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		ADMIN_BG, SACHBEARBEITER_BG, ADMIN_TS, SACHBEARBEITER_TS, })
	public Response deleteInternePendenz(
		@Nonnull @NotNull @PathParam("internePendenzId") JaxId internePendenzId,
		@Context HttpServletResponse response) {
		Objects.requireNonNull(internePendenzId);
		Objects.requireNonNull(internePendenzId.getId());

		InternePendenz internePendenz = internePendenzService.findInternePendenz(internePendenzId.getId())
			.orElseThrow(() -> new EbeguEntityNotFoundException(
				"deleteInternePendenz",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				internePendenzId.getId()
			));
		internePendenzService.deleteInternePendenz(internePendenz);
		if(internePendenzService.countInternePendenzenForGesuch(internePendenz.getGesuch()) == 0){
			internePendenz.getGesuch().setInternePendenz(false);
			gesuchService.updateGesuch(internePendenz.getGesuch(), false);
		}
		return Response.ok().build();
	}

	@ApiOperation(
		value = "Findet alle internen Pendenzen für die Gemeinde",
		response = JaxInternePendenz.class)
	@Nonnull
	@GET
	@Path("/all/{gesuchId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		ADMIN_BG, SACHBEARBEITER_BG, ADMIN_TS, SACHBEARBEITER_TS, })
	public List<JaxInternePendenz> findInternePendenzenForGesuch(
		@Nonnull @NotNull @PathParam("gesuchId") JaxId gesuchId
	) {
		Objects.requireNonNull(gesuchId);
		Objects.requireNonNull(gesuchId.getId());

		Gesuch gesuch = gesuchService.findGesuch(gesuchId.getId()).orElseThrow(() -> {
			throw new EbeguRuntimeException(
				"findInternePendenzenForGesuch",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				gesuchId.getId());
		});

		// Gemeindebenutzer müssen Gesuch nur lesen können, um eine Pendenz erstellen zu dürfen
		authorizer.checkReadAuthorization(gesuch);

		return internePendenzService.findInternePendenzenForGesuch(gesuch).stream()
			.map(internePendenz -> jaxBConverter.internePendenzToJax(internePendenz))
			.collect(Collectors.toList());
	}

	@ApiOperation(
		value = "Zählt die Anzahl nicht erledigter Pendenzen für die Gemeinde",
		response = Long.class)
	@Nonnull
	@GET
	@Path("/count/{gesuchId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		ADMIN_BG, SACHBEARBEITER_BG, ADMIN_TS, SACHBEARBEITER_TS, })
	public Long countInternePendenzenForGesuch(
		@Nonnull @NotNull @PathParam("gesuchId") JaxId gesuchId
	) {
		Objects.requireNonNull(gesuchId);
		Objects.requireNonNull(gesuchId.getId());

		Gesuch gesuch = gesuchService.findGesuch(gesuchId.getId()).orElseThrow(() -> {
			throw new EbeguRuntimeException(
				"countInternePendenzenForGesuch",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				gesuchId.getId());
		});

		// Gemeindebenutzer müssen Gesuch nur lesen können, um die Anzahl offener Pendenzen sehen zu können.
		authorizer.checkReadAuthorization(gesuch);

		return internePendenzService.countInternePendenzenForGesuch(gesuch);
	}
}
