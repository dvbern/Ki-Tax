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
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.api.converter.JaxFerienbetreuungConverter;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.dtos.gemeindeantrag.JaxFerienbetreuungAngabenAngebot;
import ch.dvbern.ebegu.api.dtos.gemeindeantrag.JaxFerienbetreuungAngabenContainer;
import ch.dvbern.ebegu.api.dtos.gemeindeantrag.JaxFerienbetreuungAngabenKostenEinnahmen;
import ch.dvbern.ebegu.api.dtos.gemeindeantrag.JaxFerienbetreuungAngabenNutzung;
import ch.dvbern.ebegu.api.dtos.gemeindeantrag.JaxFerienbetreuungAngabenStammdaten;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenAngebot;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenContainer;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenKostenEinnahmen;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenNutzung;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenStammdaten;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungDokument;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.services.authentication.AuthorizerImpl;
import ch.dvbern.ebegu.services.gemeindeantrag.FerienbetreuungService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_FERIENBETREUUNG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_FERIENBETREUUNG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;
import static java.util.Objects.requireNonNull;

/**
 * REST Resource fuer die Ferienbetreuungen
 */
@Path("ferienbetreuung")
@Stateless
@Api(description = "Resource fuer die Ferienbetreuungen")
@DenyAll // Absichtlich keine Rolle zugelassen, erzwingt, dass es für neue Methoden definiert werden muss
public class FerienbetreuungResource {

	@Inject
	private FerienbetreuungService ferienbetreuungService;

	@Inject
	private JaxFerienbetreuungConverter converter;

	@Inject
	private AuthorizerImpl authorizer;

	@ApiOperation(
		value = "Gibt den FerienbetreuungAngabenContainer mit der uebergebenen Id zurueck",
		response = JaxFerienbetreuungAngabenContainer.class)
	@Nullable
	@GET
	@Path("/find/{containerId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT,
		ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_TS, SACHBEARBEITER_TS })
	public JaxFerienbetreuungAngabenContainer findFerienbetreuungContainer(
		@Nonnull @NotNull @PathParam("containerId") JaxId containerId
	) {
		Objects.requireNonNull(containerId);
		Objects.requireNonNull(containerId.getId());

		authorizer.checkReadAuthorizationFerienbetreuung(containerId.getId());

		final Optional<FerienbetreuungAngabenContainer> ferienbetreuungAngabenContainerOpt =
			ferienbetreuungService.findFerienbetreuungAngabenContainer(converter.toEntityId(containerId));

		return ferienbetreuungAngabenContainerOpt
			.map(container ->
				converter.ferienbetreuungAngabenContainerToJax(container))
			.orElse(null);
	}

	@ApiOperation(
		value = "Speichert die Kommentare eines LastenausgleichTagesschuleAngabenGemeindeContainer in der Datenbank",
		response = Void.class)
	@PUT
	@Path("/saveKommentar/{containerId}")
	@Consumes(MediaType.TEXT_PLAIN)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public void saveKommentar(
		@Nonnull String kommentar,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response,
		@Nonnull @NotNull @PathParam("containerId") JaxId containerId
	) {
		Objects.requireNonNull(containerId);
		Objects.requireNonNull(kommentar);

		ferienbetreuungService.saveKommentar(containerId.getId(), kommentar);
	}

	@ApiOperation(
		value = "Speichert FerieninselAngabenStammdaten in der Datenbank",
		response = JaxFerienbetreuungAngabenStammdaten.class)
	@Nonnull
	@PUT
	@Path("/{containerId}/stammdaten/save")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT,
		ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_TS, SACHBEARBEITER_TS })
	public JaxFerienbetreuungAngabenStammdaten saveFerienbetreuungStammdaten(
		@Nonnull @NotNull @Valid JaxFerienbetreuungAngabenStammdaten jaxStammdaten,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response,
		@Nonnull @NotNull @PathParam("containerId") JaxId containerId
	) {
		Objects.requireNonNull(jaxStammdaten.getId());
		Objects.requireNonNull(containerId.getId());

		FerienbetreuungAngabenContainer container =
			ferienbetreuungService.findFerienbetreuungAngabenContainer(containerId.getId())
			.orElseThrow(() -> new EbeguEntityNotFoundException("saveFerienbetreuungStammdaten", containerId.getId()));

		authorizer.checkWriteAuthorization(container);

		FerienbetreuungAngabenStammdaten stammdaten =
			ferienbetreuungService.findFerienbetreuungAngabenStammdaten(jaxStammdaten.getId())
			.orElseThrow(() -> new EbeguEntityNotFoundException("saveFerienbetreuungStammdaten", jaxStammdaten.getId()));

		converter.ferienbetreuungAngabenStammdatenToEntity(jaxStammdaten, stammdaten);

		FerienbetreuungAngabenStammdaten persisted = ferienbetreuungService.saveFerienbetreuungAngabenStammdaten(stammdaten);
		return converter.ferienbetreuungAngabenStammdatenToJax(persisted);
	}

	@ApiOperation(
		value = "Speichert FerieninselAngabenAngebot in der Datenbank",
		response = JaxFerienbetreuungAngabenAngebot.class)
	@Nonnull
	@PUT
	@Path("/{containerId}/angebot/save")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT,
		ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_TS, SACHBEARBEITER_TS })
	public JaxFerienbetreuungAngabenAngebot saveFerienbetreuungAngebot(
		@Nonnull @NotNull @Valid JaxFerienbetreuungAngabenAngebot jaxAngebot,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response,
		@Nonnull @NotNull @PathParam("containerId") JaxId containerId
	) {
		Objects.requireNonNull(jaxAngebot.getId());
		Objects.requireNonNull(containerId.getId());

		FerienbetreuungAngabenContainer container =
			ferienbetreuungService.findFerienbetreuungAngabenContainer(containerId.getId())
			.orElseThrow(() -> new EbeguEntityNotFoundException("saveFerienbetreuungAngebot", containerId.getId()));

		authorizer.checkWriteAuthorization(container);

		FerienbetreuungAngabenAngebot angebot =
			ferienbetreuungService.findFerienbetreuungAngabenAngebot(jaxAngebot.getId())
			.orElseThrow(() -> new EbeguEntityNotFoundException("saveFerienbetreuungAngebot", jaxAngebot.getId()));

		converter.ferienbetreuungAngabenAngebotToEntity(jaxAngebot, angebot);

		FerienbetreuungAngabenAngebot persisted = ferienbetreuungService.saveFerienbetreuungAngabenAngebot(angebot);
		return converter.ferienbetreuungAngabenAngebotToJax(persisted);
	}

	@ApiOperation(
		value = "Speichert FerieninselAngabenNutzung in der Datenbank",
		response = JaxFerienbetreuungAngabenNutzung.class)
	@Nonnull
	@PUT
	@Path("/{containerId}/nutzung/save")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT,
		ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_TS, SACHBEARBEITER_TS })
	public JaxFerienbetreuungAngabenNutzung saveFerienbetreuungNutzung(
		@Nonnull @NotNull @Valid JaxFerienbetreuungAngabenNutzung jaxNutzung,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response,
		@Nonnull @NotNull @PathParam("containerId") JaxId containerId
	) {
		Objects.requireNonNull(jaxNutzung.getId());
		Objects.requireNonNull(containerId.getId());

		FerienbetreuungAngabenContainer container =
			ferienbetreuungService.findFerienbetreuungAngabenContainer(containerId.getId())
			.orElseThrow(() -> new EbeguEntityNotFoundException("saveFerienbetreuungNutzung", containerId.getId()));

		authorizer.checkWriteAuthorization(container);

		FerienbetreuungAngabenNutzung nutzung =
			ferienbetreuungService.findFerienbetreuungAngabenNutzung(jaxNutzung.getId())
			.orElseThrow(() -> new EbeguEntityNotFoundException("saveFerienbetreuungNutzung", jaxNutzung.getId()));

		converter.ferienbetreuungAngabenNutzungToEntity(jaxNutzung, nutzung);

		FerienbetreuungAngabenNutzung persisted = ferienbetreuungService.saveFerienbetreuungAngabenNutzung(nutzung);
		return converter.ferienbetreuungAngabenNutzungToJax(persisted);
	}

	@ApiOperation(
		value = "Speichert FerieninselAngabenKostenEinnahmen in der Datenbank",
		response = JaxFerienbetreuungAngabenKostenEinnahmen.class)
	@Nonnull
	@PUT
	@Path("/{containerId}/kostenEinnahmen/save")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT,
		ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_TS, SACHBEARBEITER_TS })
	public JaxFerienbetreuungAngabenKostenEinnahmen saveFerienbetreuungKostenEinnahmen(
		@Nonnull @NotNull @Valid JaxFerienbetreuungAngabenKostenEinnahmen jaxKostenEinnahmen,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response,
		@Nonnull @NotNull @PathParam("containerId") JaxId containerId
	) {
		Objects.requireNonNull(jaxKostenEinnahmen.getId());
		Objects.requireNonNull(containerId.getId());

		FerienbetreuungAngabenContainer container =
			ferienbetreuungService.findFerienbetreuungAngabenContainer(containerId.getId())
			.orElseThrow(() -> new EbeguEntityNotFoundException("saveFerienbetreuungKostenEinnahmen", containerId.getId()));

		authorizer.checkWriteAuthorization(container);

		FerienbetreuungAngabenKostenEinnahmen kostenEinnahmen =
			ferienbetreuungService.findFerienbetreuungAngabenKostenEinnahmen(jaxKostenEinnahmen.getId())
			.orElseThrow(() -> new EbeguEntityNotFoundException("saveFerienbetreuungKostenEinnahmen", jaxKostenEinnahmen.getId()));

		converter.ferienbetreuungAngabenKostenEinnahmenToEntity(jaxKostenEinnahmen, kostenEinnahmen);

		FerienbetreuungAngabenKostenEinnahmen persisted = ferienbetreuungService.saveFerienbetreuungAngabenKostenEinnahmen(kostenEinnahmen);
		return converter.ferienbetreuungAngabenKostenEinnahmenToJax(persisted);
	}

	@ApiOperation("Loescht das Dokument mit der uebergebenen Id in der Datenbank")
	@Nullable
	@DELETE
	@Path("/{ferienbetreuungDokumentId}")
	@Consumes(MediaType.WILDCARD)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_TS,
		SACHBEARBEITER_TS, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_FERIENBETREUUNG, SACHBEARBEITER_FERIENBETREUUNG})
	public Response removeFerienbetreuungDokument(
		@Nonnull @NotNull @PathParam("ferienbetreuungDokumentId") JaxId ferienbetreuungDokumentJAXPId,
		@Context HttpServletResponse response) {

		requireNonNull(ferienbetreuungDokumentJAXPId.getId());
		String dokumentId = converter.toEntityId(ferienbetreuungDokumentJAXPId);

		FerienbetreuungDokument ferienbetreuungDokument =
			ferienbetreuungService.findDokument(dokumentId).orElseThrow(() -> new EbeguEntityNotFoundException(
				"removeFerienbetreuungDokument",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, dokumentId));

		ferienbetreuungService.removeDokument(ferienbetreuungDokument);

		return Response.ok().build();
	}

}
