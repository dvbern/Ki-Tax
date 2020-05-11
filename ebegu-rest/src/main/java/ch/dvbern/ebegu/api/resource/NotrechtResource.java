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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.api.resource;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
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
import javax.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.dtos.JaxRueckforderungFormular;
import ch.dvbern.ebegu.api.dtos.JaxRueckforderungMitteilung;
import ch.dvbern.ebegu.entities.RueckforderungFormular;
import ch.dvbern.ebegu.entities.RueckforderungMitteilung;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.services.RueckforderungFormularService;
import ch.dvbern.ebegu.services.RueckforderungMitteilungService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

@Path("notrecht")
@Stateless
@Api(description = "Resource zum Verwalten von Rueckforderungsformularen für das Notrecht")
public class NotrechtResource {

	@Inject
	private RueckforderungFormularService rueckforderungFormularService;

	@Inject
	private RueckforderungMitteilungService rueckforderungMitteilungService;

	@Inject
	private JaxBConverter converter;

	@ApiOperation(value = "Erstellt leere Rückforderungsformulare für alle Kitas & TFOs die in kiBon existieren "
		+ "und bisher kein Rückforderungsformular haben", responseContainer = "List", response =
		JaxRueckforderungFormular.class)
	@Nullable
	@POST
	@Path("/initialize")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN })
	public List<JaxRueckforderungFormular> initializeRueckforderungFormulare() {

		List<RueckforderungFormular> createdFormulare =
			rueckforderungFormularService.initializeRueckforderungFormulare();

		return converter.rueckforderungFormularListToJax(createdFormulare);
	}

	@ApiOperation(value = "Updates a RueckforderungFormular in the database", response =
		JaxRueckforderungFormular.class)
	@Nullable
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, ADMIN_INSTITUTION, SACHBEARBEITER_MANDANT, SACHBEARBEITER_INSTITUTION})
	public JaxRueckforderungFormular update(
		@Nonnull @NotNull JaxRueckforderungFormular rueckforderungFormularJAXP,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		Objects.requireNonNull(rueckforderungFormularJAXP.getId());

		RueckforderungFormular rueckforderungFormularFromDB =
			rueckforderungFormularService.findRueckforderungFormular(rueckforderungFormularJAXP.getId())
			.orElseThrow(() -> new EbeguEntityNotFoundException("update", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				rueckforderungFormularJAXP.getId()));

		RueckforderungFormular rueckforderungFormularToMerge =
			converter.rueckforderungFormularToEntity(rueckforderungFormularJAXP,
			rueckforderungFormularFromDB);
		RueckforderungFormular modifiedRueckforderungFormular =
			this.rueckforderungFormularService.save(rueckforderungFormularToMerge);
		return converter.rueckforderungFormularToJax(modifiedRueckforderungFormular);
	}

	@ApiOperation(value = "Sucht den Benutzer mit dem uebergebenen Username in der Datenbank.",
		response = JaxRueckforderungFormular.class)
	@Nullable
	@GET
	@Path("/{rueckforderungFormId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public JaxRueckforderungFormular findRueckforderungFormular(
		@Nonnull @NotNull @PathParam("rueckforderungFormId") JaxId rueckforderungFormJaxId) {

		Objects.requireNonNull(rueckforderungFormJaxId.getId());
		String rueckforderungFormId = converter.toEntityId(rueckforderungFormJaxId);
		Optional<RueckforderungFormular> rueckforderungFormularOptional =
			rueckforderungFormularService.findRueckforderungFormular(rueckforderungFormId);

		if (!rueckforderungFormularOptional.isPresent()) {
			return null;
		}
		RueckforderungFormular rueckforderungFormularToReturn = rueckforderungFormularOptional.get();
		final JaxRueckforderungFormular jaxRueckforderungFormular =
			converter.rueckforderungFormularToJax(rueckforderungFormularToReturn);
		return jaxRueckforderungFormular;
	}

	@ApiOperation(value = "Sendet eine Nachricht an alle Besitzer von Rückforderungsformularen mit gewünschtem "
		+ "Status",
		response = JaxRueckforderungMitteilung.class)
	@Nullable
	@POST
	@Path("/mitteilung")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, ADMIN_INSTITUTION, SACHBEARBEITER_MANDANT})
	public void sendMessage(
		@Nonnull @NotNull JaxRueckforderungMitteilung jaxRueckforderungMitteilung,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {
		RueckforderungMitteilung rueckforderungMitteilung =
			converter.rueckforderungMitteilungToEntity(jaxRueckforderungMitteilung, new RueckforderungMitteilung());
		rueckforderungMitteilungService.sendMitteilung(rueckforderungMitteilung);
	}

	@ApiOperation(value = "Sendet eine Nachricht an alle Besitzer von Rückforderungsformularen mit gewünschtem "
		+ "Status",
		response = JaxRueckforderungMitteilung.class)
	@Nullable
	@POST
	@Path("/einladung")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, ADMIN_INSTITUTION, SACHBEARBEITER_MANDANT})
	public JaxRueckforderungMitteilung sendEinladung(
		@Nonnull @NotNull JaxRueckforderungMitteilung jaxRueckforderungMitteilung,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {
		RueckforderungMitteilung rueckforderungMitteilung =
			converter.rueckforderungMitteilungToEntity(jaxRueckforderungMitteilung, new RueckforderungMitteilung());
		return jaxRueckforderungMitteilung;
	}

}
