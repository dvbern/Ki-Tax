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
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.api.converter.JaxSozialdienstConverter;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.dtos.sozialdienst.JaxSozialdienst;
import ch.dvbern.ebegu.api.dtos.sozialdienst.JaxSozialdienstStammdaten;
import ch.dvbern.ebegu.einladung.Einladung;
import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.sozialdienst.Sozialdienst;
import ch.dvbern.ebegu.entities.sozialdienst.SozialdienstStammdaten;
import ch.dvbern.ebegu.enums.SozialdienstStatus;
import ch.dvbern.ebegu.services.Authorizer;
import ch.dvbern.ebegu.services.BenutzerService;
import ch.dvbern.ebegu.services.SozialdienstService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_SOZIALDIENST;
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

	@Inject
	private Authorizer authorizer;

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

		Sozialdienst convertedSozialdienst =
			jaxSozialdienstConverter.sozialdienstToEntity(sozialdienstJAXP, new Sozialdienst());

		Sozialdienst persistedSozialdienst = this.sozialdienstService.createSozialdienst(convertedSozialdienst);

		final Benutzer benutzer = benutzerService.findBenutzerByEmail(adminMail)
			.orElseGet(() -> benutzerService.createAdminSozialdienstByEmail(adminMail, persistedSozialdienst));

		benutzerService.einladen(Einladung.forSozialdienst(benutzer, persistedSozialdienst));

		return jaxSozialdienstConverter.sozialdienstToJAX(persistedSozialdienst);
	}

	@ApiOperation(value = "Returns all Sozialdienst",
		responseContainer = "Collection",
		response = JaxSozialdienst.class)
	@Nullable
	@GET
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Oeffentliche Daten
	public List<JaxSozialdienst> getAllSozialdienst() {

		return sozialdienstService.getAllSozialdienste().stream()
			.map(sozialdienst -> jaxSozialdienstConverter.sozialdienstToJAX(sozialdienst))
			.collect(Collectors.toList());
	}

	@ApiOperation(value = "Returns the SozialdienstStammdaten with the given SozialdienstId.",
		response = JaxSozialdienstStammdaten.class)
	@Nullable
	@GET
	@Path("/stammdaten/{sozialdienstId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Grundsaetzliche fuer alle Rollen: Datenabhaengig. -> Authorizer
	public JaxSozialdienstStammdaten getSozialdienstStammdaten(
		@Nonnull @NotNull @PathParam("sozialdienstId") JaxId sozialdienstJaxId) {

		String sozialdienstId = jaxSozialdienstConverter.toEntityId(sozialdienstJaxId);

		Optional<SozialdienstStammdaten> stammdatenFromDB =
			sozialdienstService.getSozialdienstStammdatenBySozialdienstId(sozialdienstId);
		if (!stammdatenFromDB.isPresent()) {
			stammdatenFromDB = initSozialdienstStammdaten(sozialdienstId);
		}

		authorizer.checkReadAuthorization(stammdatenFromDB.get().getSozialdienst());

		return stammdatenFromDB
			.map(stammdaten -> jaxSozialdienstConverter.sozialdienstStammdatenToJAX(stammdaten))
			.orElse(null);
	}

	private Optional<SozialdienstStammdaten> initSozialdienstStammdaten(String sozialdienstId) {
		SozialdienstStammdaten stammdaten = new SozialdienstStammdaten();
		Optional<Sozialdienst> sozialdienst = sozialdienstService.findSozialdienst(sozialdienstId);
		stammdaten.setSozialdienst(sozialdienst.orElse(new Sozialdienst()));
		stammdaten.setAdresse(new Adresse());
		return Optional.of(stammdaten);
	}

	@ApiOperation(value = "Speichert die SozialdienstStammdaten", response = JaxSozialdienstStammdaten.class)
	@Nullable
	@PUT
	@Path("/stammdaten")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_SOZIALDIENST })
	public JaxSozialdienstStammdaten saveSozialdienstStammdaten(
		@Nonnull @NotNull @Valid JaxSozialdienstStammdaten jaxStammdaten,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		SozialdienstStammdaten stammdaten;
		if (jaxStammdaten.getId() != null) {
			Optional<SozialdienstStammdaten> optional =
				sozialdienstService.getSozialdienstStammdaten(jaxStammdaten.getId());
			stammdaten = optional.orElse(new SozialdienstStammdaten());
		} else {
			stammdaten = new SozialdienstStammdaten();
		}
		if (stammdaten.isNew()) {
			stammdaten.setAdresse(new Adresse());
		}
		SozialdienstStammdaten convertedStammdaten =
			jaxSozialdienstConverter.sozialdienstStammdatenToEntity(jaxStammdaten, stammdaten);

		// Statuswechsel
		if (convertedStammdaten.getSozialdienst().getStatus() == SozialdienstStatus.EINGELADEN) {
			convertedStammdaten.getSozialdienst().setStatus(SozialdienstStatus.AKTIV);
		}

		authorizer.checkWriteAuthorization(convertedStammdaten.getSozialdienst());

		SozialdienstStammdaten persistedStammdaten =
			sozialdienstService.saveSozialdienstStammdaten(convertedStammdaten);

		return jaxSozialdienstConverter.sozialdienstStammdatenToJAX(persistedStammdaten);
	}
}
