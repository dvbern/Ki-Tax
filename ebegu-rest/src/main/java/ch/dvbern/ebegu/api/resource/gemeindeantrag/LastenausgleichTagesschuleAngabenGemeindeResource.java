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

package ch.dvbern.ebegu.api.resource.gemeindeantrag;

import java.util.Objects;
import java.util.Optional;

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
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.dtos.gemeindeantrag.JaxLastenausgleichTagesschuleAngabenGemeindeContainer;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeContainer;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.services.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeService;
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
 * REST Resource fuer den Lastenausgleich der Tagesschulen, Angaben der Gemeinde
 */
@Path("lats/gemeinde")
@Stateless
@Api(description = "Resource fuer den Lastenausgleich der Tagesschulen, Angaben der Gemeinde")
@DenyAll // Absichtlich keine Rolle zugelassen, erzwingt, dass es für neue Methoden definiert werden muss
public class LastenausgleichTagesschuleAngabenGemeindeResource {

	@Inject
	private LastenausgleichTagesschuleAngabenGemeindeService angabenGemeindeService;

	@Inject
	private JaxBConverter converter;

	@ApiOperation(
		value = "Gibt den LastenausgleichTagesschuleAngabenGemeindeContainer mit der uebergebenen Id zurueck",
		response = JaxLastenausgleichTagesschuleAngabenGemeindeContainer.class)
	@Nullable
	@GET
	@Path("/find/{latsGemeindeAngabenJaxId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT,
		ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_TS, SACHBEARBEITER_TS })
	public JaxLastenausgleichTagesschuleAngabenGemeindeContainer findLastenausgleichTagesschuleAngabenGemeindeContainer(
		@Nonnull @NotNull @PathParam("latsGemeindeAngabenJaxId") JaxId latsGemeindeAngabenJaxId
	) {
		Objects.requireNonNull(latsGemeindeAngabenJaxId);
		Objects.requireNonNull(latsGemeindeAngabenJaxId.getId());

		final Optional<LastenausgleichTagesschuleAngabenGemeindeContainer> latsGemeindeContainerOptional =
			angabenGemeindeService.findLastenausgleichTagesschuleAngabenGemeindeContainer(converter.toEntityId(latsGemeindeAngabenJaxId));

		return latsGemeindeContainerOptional
			.map(lastenausgleichTagesschuleAngabenGemeindeContainer ->
				converter.lastenausgleichTagesschuleAngabenGemeindeContainerToJax(lastenausgleichTagesschuleAngabenGemeindeContainer))
			.orElse(null);
	}

	@ApiOperation(
		value = "Speichert einen LastenausgleichTagesschuleAngabenGemeindeContainer in der Datenbank",
		response = JaxLastenausgleichTagesschuleAngabenGemeindeContainer.class)
	@Nonnull
	@PUT
	@Path("/save")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT,
		ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_TS, SACHBEARBEITER_TS })
	public JaxLastenausgleichTagesschuleAngabenGemeindeContainer saveLastenausgleichTagesschuleGemeinde(
		@Nonnull @NotNull @Valid JaxLastenausgleichTagesschuleAngabenGemeindeContainer latsGemeindeContainerJax,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {
		final LastenausgleichTagesschuleAngabenGemeindeContainer converted =
			getConvertedLastenausgleichTagesschuleAngabenGemeindeContainer(latsGemeindeContainerJax);

		final LastenausgleichTagesschuleAngabenGemeindeContainer saved =
			angabenGemeindeService.saveLastenausgleichTagesschuleGemeinde(converted);

		return converter.lastenausgleichTagesschuleAngabenGemeindeContainerToJax(saved);
	}

	@ApiOperation(
		value = "Gibt den LastenausgleichTagesschuleAngabenGemeindeContainer frei fuer die Bearbeitung durch die Institutionen",
		response = JaxLastenausgleichTagesschuleAngabenGemeindeContainer.class)
	@Nonnull
	@PUT
	@Path("/freigebenInstitution")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT,
		ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_TS, SACHBEARBEITER_TS })
	public JaxLastenausgleichTagesschuleAngabenGemeindeContainer lastenausgleichTagesschuleGemeindeFuerInstitutionenFreigeben(
		@Nonnull @NotNull @Valid JaxLastenausgleichTagesschuleAngabenGemeindeContainer latsGemeindeContainerJax,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {
		final LastenausgleichTagesschuleAngabenGemeindeContainer converted =
			getConvertedLastenausgleichTagesschuleAngabenGemeindeContainer(latsGemeindeContainerJax);

		final LastenausgleichTagesschuleAngabenGemeindeContainer saved =
			angabenGemeindeService.lastenausgleichTagesschuleGemeindeFuerInstitutionenFreigeben(converted);

		return converter.lastenausgleichTagesschuleAngabenGemeindeContainerToJax(saved);
	}

	@ApiOperation(
		value = "Reicht den Lastenausgleich ein",
		response = JaxLastenausgleichTagesschuleAngabenGemeindeContainer.class)
	@Nonnull
	@PUT
	@Path("/einreichen")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT,
		ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_TS, SACHBEARBEITER_TS })
	public JaxLastenausgleichTagesschuleAngabenGemeindeContainer lastenausgleichTagesschuleGemeindeEinreichen(
		@Nonnull @NotNull @Valid JaxLastenausgleichTagesschuleAngabenGemeindeContainer latsGemeindeContainerJax,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {
		final LastenausgleichTagesschuleAngabenGemeindeContainer converted =
			getConvertedLastenausgleichTagesschuleAngabenGemeindeContainer(latsGemeindeContainerJax);

		final LastenausgleichTagesschuleAngabenGemeindeContainer saved =
			angabenGemeindeService.lastenausgleichTagesschuleGemeindeEinreichen(converted);

		return converter.lastenausgleichTagesschuleAngabenGemeindeContainerToJax(saved);
	}

	@Nonnull
	private LastenausgleichTagesschuleAngabenGemeindeContainer getConvertedLastenausgleichTagesschuleAngabenGemeindeContainer(
		@Nonnull JaxLastenausgleichTagesschuleAngabenGemeindeContainer latsGemeindeContainerJax
	) {
		Objects.requireNonNull(latsGemeindeContainerJax);
		Objects.requireNonNull(latsGemeindeContainerJax.getId());

		// Das Objekt muss in der DB schon vorhanden sein, da die Erstellung immer ueber den GemeindeAntragService geschieht
		final LastenausgleichTagesschuleAngabenGemeindeContainer latsGemeindeContainer =
			angabenGemeindeService.findLastenausgleichTagesschuleAngabenGemeindeContainer(latsGemeindeContainerJax.getId())
				.orElseThrow(() -> new EbeguEntityNotFoundException(
					"getConvertedLastenausgleichTagesschuleAngabenGemeindeContainer",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					latsGemeindeContainerJax.getId()));

		final LastenausgleichTagesschuleAngabenGemeindeContainer converted =
			converter.lastenausgleichTagesschuleAngabenGemeindeContainerToEntity(latsGemeindeContainerJax, latsGemeindeContainer);
		return converted;
	}
}
