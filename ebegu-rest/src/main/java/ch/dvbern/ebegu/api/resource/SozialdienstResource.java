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
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.api.converter.JaxSozialdienstConverter;
import ch.dvbern.ebegu.api.dtos.JaxGemeinde;
import ch.dvbern.ebegu.api.dtos.JaxTraegerschaft;
import ch.dvbern.ebegu.api.dtos.sozialdienst.JaxSozialdienst;
import ch.dvbern.ebegu.einladung.Einladung;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.sozialdienst.Sozialdienst;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.services.BenutzerService;
import ch.dvbern.ebegu.services.SozialdienstService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

@Path("sozialdienst")
@Stateless
@Api(description = "Resource fuer Sozialhilfe Zeitraeume")
@DenyAll
public class SozialdienstResource {

	@Inject
	private JaxSozialdienstConverter jaxSozialdienstConverter;

	@Inject
	private SozialdienstService sozialdienstService;

	@Inject
	private BenutzerService benutzerService;

	@ApiOperation(value = "Erstellt eine neue Sozialdienst in der Datenbank", response = JaxSozialdienst.class)
	@Nullable
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public JaxSozialdienst createSozialdienst(
		@Nonnull @NotNull @Valid JaxSozialdienst sozialdienstJAXP,
		@Nonnull @NotNull @Valid @QueryParam("adminMail") String adminMail,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		Sozialdienst convertedSozialdienst = jaxSozialdienstConverter.sozialdienstToEntity(sozialdienstJAXP, new Sozialdienst());

		Sozialdienst persistedSozialdienst = this.sozialdienstService.createSozialdienst(convertedSozialdienst);

		final Benutzer benutzer = benutzerService.findBenutzerByEmail(adminMail)
			.orElseGet(() -> benutzerService.createAdminSozialdienstByEmail(adminMail, persistedSozialdienst));

		benutzerService.einladen(Einladung.forSozialdienst(benutzer, persistedSozialdienst));

		return jaxSozialdienstConverter.sozialdienstToJAX(persistedSozialdienst);
	}
}
