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

import ch.dvbern.ebegu.api.converter.JaxFerienbetreuungConverter;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.dtos.gemeindeantrag.*;
import ch.dvbern.ebegu.api.util.RestUtil;
import ch.dvbern.ebegu.entities.gemeindeantrag.*;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.enums.gemeindeantrag.FerienbetreuungAngabenStatus;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.MergeDocException;
import ch.dvbern.ebegu.i18n.LocaleThreadLocal;
import ch.dvbern.ebegu.services.authentication.AuthorizerImpl;
import ch.dvbern.ebegu.services.gemeindeantrag.FerienbetreuungService;
import com.google.common.base.Preconditions;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.security.DenyAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import static ch.dvbern.ebegu.enums.UserRoleName.*;

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
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		ADMIN_BG, SACHBEARBEITER_BG, ADMIN_TS, SACHBEARBEITER_TS, ADMIN_FERIENBETREUUNG,
		SACHBEARBEITER_FERIENBETREUUNG })
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
		value = "Gibt den Vorgaenger des FerienbetreuungAngabenContainers mit der uebergebenen Id zurueck",
		response = JaxFerienbetreuungAngabenContainer.class)
	@Nullable
	@GET
	@Path("/vorgaenger/{containerId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		ADMIN_BG, SACHBEARBEITER_BG, ADMIN_TS, SACHBEARBEITER_TS, ADMIN_FERIENBETREUUNG,
		SACHBEARBEITER_FERIENBETREUUNG })
	public Response findFerienbetreuungVorgaengerContainer(
			@Nonnull @NotNull @PathParam("containerId") JaxId containerId
	) {
		Objects.requireNonNull(containerId);
		Objects.requireNonNull(containerId.getId());

		authorizer.checkReadAuthorizationFerienbetreuung(containerId.getId());

		final Optional<FerienbetreuungAngabenContainer> ferienbetreuungAngabenContainerOpt =
				ferienbetreuungService.findFerienbetreuungAngabenContainer(converter.toEntityId(containerId));

		final Optional<FerienbetreuungAngabenContainer> ferienbetreuungAngabenContainerVorgaenger =
				ferienbetreuungService.findFerienbetreuungAngabenVorgaengerContainer(ferienbetreuungAngabenContainerOpt.orElseThrow());

		if (ferienbetreuungAngabenContainerVorgaenger.isPresent()) {
			JaxFerienbetreuungAngabenContainer converted =
					converter.ferienbetreuungAngabenContainerToJax(ferienbetreuungAngabenContainerVorgaenger.get());
			return Response.ok(converted)
					.build();
		}
		return Response.noContent().build();
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
		value = "Speichert den Verantwortlichen eines FerienbetreuungsAngebot in der Datenbank",
		response = Void.class)
	@PUT
	@Path("/saveVerantworlicher/{containerId}")
	@Consumes(MediaType.WILDCARD)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public void saveVerantworlicher(
		@Nullable String username,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response,
		@Nonnull @NotNull @PathParam("containerId") String containerId
	) {
		Objects.requireNonNull(containerId);

		ferienbetreuungService.saveVerantwortlicher(containerId, username);
	}

	@ApiOperation(
		value = "Schliesst den FerienBetreuungAngabenContainer als Gemeinde ab und gibt ihn zur Prüfung durch"
			+ "die Kantone frei",
		response = JaxFerienbetreuungAngabenStammdaten.class)
	@PUT
	@Path("/freigeben/{containerId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_TS,
		ADMIN_BG, SACHBEARBEITER_BG,
		SACHBEARBEITER_TS, ADMIN_FERIENBETREUUNG, SACHBEARBEITER_FERIENBETREUUNG })
	public JaxFerienbetreuungAngabenContainer ferienBetreuungFreigeben(
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response,
		@Nonnull @NotNull @PathParam("containerId") JaxId containerId
	) {
		Objects.requireNonNull(containerId);
		Objects.requireNonNull(containerId.getId());

		authorizer.checkReadAuthorizationFerienbetreuung(containerId.getId());

		FerienbetreuungAngabenContainer container =
			ferienbetreuungService.findFerienbetreuungAngabenContainer(containerId.getId())
				.orElseThrow(() -> new EbeguEntityNotFoundException(
					"ferienBetreuungFreigeben",
					containerId.getId()));

		authorizer.checkWriteAuthorization(container);

		FerienbetreuungAngabenContainer persisted =
			ferienbetreuungService.ferienbetreuungAngabenFreigeben(container);
		return converter.ferienbetreuungAngabenContainerToJax(persisted);
	}

	@ApiOperation(
		value = "Markiert den FerienbetreuungAngabenContainer als Geprüft",
		response = JaxFerienbetreuungAngabenContainer.class)
	@PUT
	@Path("/geprueft/{containerId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, SACHBEARBEITER_MANDANT, ADMIN_MANDANT })
	public JaxFerienbetreuungAngabenContainer ferienBetreuungGeprueft(
		@Nonnull @NotNull @Valid JaxFerienbetreuungAngabenContainer jaxFerienbetreuungContainer,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response,
		@Nonnull @NotNull @PathParam("containerId") JaxId containerId
	) {
		assert jaxFerienbetreuungContainer.getAngabenKorrektur() != null;
		assert jaxFerienbetreuungContainer.getAngabenKorrektur().getBerechnungen() != null;

		Objects.requireNonNull(containerId);
		Objects.requireNonNull(containerId.getId());

		authorizer.checkReadAuthorizationFerienbetreuung(containerId.getId());

		FerienbetreuungAngabenContainer container =
			ferienbetreuungService.findFerienbetreuungAngabenContainer(containerId.getId())
				.orElseThrow(() -> new EbeguEntityNotFoundException(
					"ferienBetreuungGeprueft",
					containerId.getId()));

		authorizer.checkWriteAuthorization(container);

		FerienbetreuungBerechnungen berechnungen =
				converter.ferienbetreuungBerechnungentoEntity(jaxFerienbetreuungContainer.getAngabenKorrektur()
						.getBerechnungen(), new FerienbetreuungBerechnungen());

		Objects.requireNonNull(container.getAngabenKorrektur()).setFerienbetreuungBerechnungen(berechnungen);

		FerienbetreuungAngabenContainer persisted =
			ferienbetreuungService.ferienbetreuungAngabenGeprueft(container);
		return converter.ferienbetreuungAngabenContainerToJax(persisted);
	}

	@ApiOperation(
		value = "Gibt den Gemeinde Container zurück an die Gemeinde",
		response = JaxFerienbetreuungAngabenContainer.class)
	@PUT
	@Path("/zurueck-an-gemeinde/{containerId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, SACHBEARBEITER_MANDANT, ADMIN_MANDANT })
	public JaxFerienbetreuungAngabenContainer ferienBetreuungZurueckAnGemeinde(
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response,
		@Nonnull @NotNull @PathParam("containerId") JaxId containerId
	) {
		Objects.requireNonNull(containerId);
		Objects.requireNonNull(containerId.getId());

		authorizer.checkReadAuthorizationFerienbetreuung(containerId.getId());

		FerienbetreuungAngabenContainer container =
			ferienbetreuungService.findFerienbetreuungAngabenContainer(containerId.getId())
				.orElseThrow(() -> new EbeguEntityNotFoundException(
					"ferienBetreuungZurueckAnGemeinde",
					containerId.getId()));

		authorizer.checkWriteAuthorization(container);

		FerienbetreuungAngabenContainer persisted =
			ferienbetreuungService.ferienbetreuungAngabenZurueckAnGemeinde(container);
		return converter.ferienbetreuungAngabenContainerToJax(persisted);
	}

	@ApiOperation(
		value = "Schliesse den Antrag ab",
		response = JaxFerienbetreuungAngabenContainer.class)
	@PUT
	@Path("/abschliessen/{containerId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, SACHBEARBEITER_MANDANT, ADMIN_MANDANT })
	public JaxFerienbetreuungAngabenContainer abschliessen(
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response,
		@Nonnull @NotNull @PathParam("containerId") JaxId containerId
	) {
		Objects.requireNonNull(containerId);
		Objects.requireNonNull(containerId.getId());

		FerienbetreuungAngabenContainer container =
			ferienbetreuungService.findFerienbetreuungAngabenContainer(containerId.getId())
				.orElseThrow(() -> new EbeguEntityNotFoundException(
					"abschliessen",
					containerId.getId()));

		authorizer.checkReadAuthorization(container);

		FerienbetreuungAngabenContainer persisted =
			ferienbetreuungService.antragAbschliessen(container);
		return converter.ferienbetreuungAngabenContainerToJax(persisted);
	}

	@ApiOperation(
		value = "Schliesse den Antrag ab",
		response = JaxFerienbetreuungAngabenContainer.class)
	@PUT
	@Path("/zurueck-an-kanton/{containerId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, SACHBEARBEITER_MANDANT, ADMIN_MANDANT })
	public JaxFerienbetreuungAngabenContainer zurueckAnKanton(
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response,
		@Nonnull @NotNull @PathParam("containerId") JaxId containerId
	) {
		Objects.requireNonNull(containerId);
		Objects.requireNonNull(containerId.getId());

		FerienbetreuungAngabenContainer container =
			ferienbetreuungService.findFerienbetreuungAngabenContainer(containerId.getId())
				.orElseThrow(() -> new EbeguEntityNotFoundException(
					"zurueckAnKanton",
					containerId.getId()));

		authorizer.checkReadAuthorization(container);

		FerienbetreuungAngabenContainer persisted =
			ferienbetreuungService.zurueckAnKanton(container);
		return converter.ferienbetreuungAngabenContainerToJax(persisted);
	}

	@ApiOperation(
		value = "Speichert FerienbetreuungAngabenStammdaten in der Datenbank",
		response = JaxFerienbetreuungAngabenStammdaten.class)
	@Nonnull
	@PUT
	@Path("/{containerId}/stammdaten/save")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		ADMIN_BG, SACHBEARBEITER_BG, ADMIN_TS, SACHBEARBEITER_TS, ADMIN_FERIENBETREUUNG,
		SACHBEARBEITER_FERIENBETREUUNG })
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
				.orElseThrow(() -> new EbeguEntityNotFoundException(
					"saveFerienbetreuungStammdaten",
					containerId.getId()));

		authorizer.checkWriteAuthorization(container);

		FerienbetreuungAngabenStammdaten stammdaten =
			ferienbetreuungService.findFerienbetreuungAngabenStammdaten(jaxStammdaten.getId())
				.orElseThrow(() -> new EbeguEntityNotFoundException(
					"saveFerienbetreuungStammdaten",
					jaxStammdaten.getId()));

		stammdaten = converter.ferienbetreuungAngabenStammdatenToEntity(jaxStammdaten, stammdaten);

		FerienbetreuungAngabenStammdaten persisted =
			ferienbetreuungService.saveFerienbetreuungAngabenStammdaten(stammdaten);
		return converter.ferienbetreuungAngabenStammdatenToJax(persisted);
	}

	@ApiOperation(
		value = "Schliesst die FerienbetreuungAngabenStammdaten als Gemeinde ab",
		response = JaxFerienbetreuungAngabenStammdaten.class)
	@Nonnull
	@PUT
	@Path("/{containerId}/stammdaten/abschliessen")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		ADMIN_BG, SACHBEARBEITER_BG, ADMIN_TS, SACHBEARBEITER_TS, ADMIN_FERIENBETREUUNG,
		SACHBEARBEITER_FERIENBETREUUNG })
	public JaxFerienbetreuungAngabenStammdaten ferienbetreuungStammdatenAbschliessen(
		@Nonnull @NotNull @Valid JaxFerienbetreuungAngabenStammdaten jaxStammdaten,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response,
		@Nonnull @NotNull @PathParam("containerId") JaxId containerId
	) {
		Objects.requireNonNull(jaxStammdaten.getId());
		Objects.requireNonNull(containerId.getId());

		FerienbetreuungAngabenContainer container =
			ferienbetreuungService.findFerienbetreuungAngabenContainer(containerId.getId())
				.orElseThrow(() -> new EbeguEntityNotFoundException(
					"ferienbetreuungStammdatenAbschliessen",
					containerId.getId()));

		authorizer.checkWriteAuthorization(container);

		FerienbetreuungAngabenStammdaten stammdaten =
			ferienbetreuungService.findFerienbetreuungAngabenStammdaten(jaxStammdaten.getId())
				.orElseThrow(() -> new EbeguEntityNotFoundException(
					"ferienbetreuungStammdatenAbschliessen",
					jaxStammdaten.getId()));

		stammdaten = converter.ferienbetreuungAngabenStammdatenToEntity(jaxStammdaten, stammdaten);

		FerienbetreuungAngabenStammdaten persisted =
			ferienbetreuungService.ferienbetreuungAngabenStammdatenAbschliessen(stammdaten);
		return converter.ferienbetreuungAngabenStammdatenToJax(persisted);
	}

	@SuppressWarnings({ "PMD.AvoidDuplicateLiterals"})
	@ApiOperation(
		value = "Öffnet die FerienbetreuungAngabenStammdaten zur Wiederbearbeitung als Gemeinde",
		response = JaxFerienbetreuungAngabenStammdaten.class)
	@Nonnull
	@PUT
	@Path("/{containerId}/stammdaten/falsche-angaben")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		ADMIN_BG, SACHBEARBEITER_BG, ADMIN_TS, SACHBEARBEITER_TS, ADMIN_FERIENBETREUUNG,
		SACHBEARBEITER_FERIENBETREUUNG })
	public JaxFerienbetreuungAngabenStammdaten falscheAngabenFerienbetreuungStammdaten(
		@Nonnull @NotNull @Valid JaxFerienbetreuungAngabenStammdaten jaxStammdaten,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response,
		@Nonnull @NotNull @PathParam("containerId") JaxId containerId
	) {
		Objects.requireNonNull(jaxStammdaten.getId());
		Objects.requireNonNull(containerId.getId());

		FerienbetreuungAngabenContainer container =
			ferienbetreuungService.findFerienbetreuungAngabenContainer(containerId.getId())
				.orElseThrow(() -> new EbeguEntityNotFoundException(
					"falscheAngabenFerienbetreuungStammdaten",
					containerId.getId()));

		authorizer.checkWriteAuthorization(container);

		Preconditions.checkArgument(
			container.getStatus() == FerienbetreuungAngabenStatus.IN_BEARBEITUNG_GEMEINDE
				|| container.getStatus() == FerienbetreuungAngabenStatus.IN_PRUEFUNG_KANTON
				|| container.getStatus() == FerienbetreuungAngabenStatus.ZURUECK_AN_GEMEINDE,
			"FerienbetreuungAngabenContainer must be in state IN_BEARBEITUNG_GEMEINDE, ZURUECK_AN_GEMEINDE or IN_PRUEFUNG_KANTON");

		FerienbetreuungAngabenStammdaten stammdaten =
			ferienbetreuungService.findFerienbetreuungAngabenStammdaten(jaxStammdaten.getId())
				.orElseThrow(() -> new EbeguEntityNotFoundException(
					"falscheAngabenFerienbetreuungStammdaten",
					jaxStammdaten.getId()));

		stammdaten = converter.ferienbetreuungAngabenStammdatenToEntity(jaxStammdaten, stammdaten);

		FerienbetreuungAngabenStammdaten persisted =
			ferienbetreuungService.ferienbetreuungAngabenStammdatenFalscheAngaben(stammdaten);
		return converter.ferienbetreuungAngabenStammdatenToJax(persisted);
	}

	@ApiOperation(
		value = "Speichert FerienbetreuungAngabenAngebot in der Datenbank",
		response = JaxFerienbetreuungAngabenAngebot.class)
	@Nonnull
	@PUT
	@Path("/{containerId}/angebot/save")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		ADMIN_BG, SACHBEARBEITER_BG, ADMIN_TS, SACHBEARBEITER_TS, ADMIN_FERIENBETREUUNG,
		SACHBEARBEITER_FERIENBETREUUNG })
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
				.orElseThrow(() -> new EbeguEntityNotFoundException("saveFerienbetreuungAngebot",
					containerId.getId()));

		authorizer.checkWriteAuthorization(container);

		FerienbetreuungAngabenAngebot angebot =
			ferienbetreuungService.findFerienbetreuungAngabenAngebot(jaxAngebot.getId())
				.orElseThrow(() -> new EbeguEntityNotFoundException("saveFerienbetreuungAngebot", jaxAngebot.getId()));

		angebot = converter.ferienbetreuungAngabenAngebotToEntity(jaxAngebot, angebot);

		FerienbetreuungAngabenAngebot persisted = ferienbetreuungService.saveFerienbetreuungAngabenAngebot(angebot);
		return converter.ferienbetreuungAngabenAngebotToJax(persisted);
	}

	@SuppressWarnings("PMD.PreserveStackTrace")
	@ApiOperation(
		value = "Schliesst FerienbetreuungAngabenAngebot als Gemeinde ab",
		response = JaxFerienbetreuungAngabenAngebot.class)
	@Nonnull
	@PUT
	@Path("/{containerId}/angebot/abschliessen")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		ADMIN_BG, SACHBEARBEITER_BG, ADMIN_TS, SACHBEARBEITER_TS, ADMIN_FERIENBETREUUNG,
		SACHBEARBEITER_FERIENBETREUUNG })
	public JaxFerienbetreuungAngabenAngebot ferienbetreuungAngebotAbschliessen(
		@Nonnull @NotNull @Valid JaxFerienbetreuungAngabenAngebot jaxAngebot,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response,
		@Nonnull @NotNull @PathParam("containerId") JaxId containerId
	) {
		Objects.requireNonNull(jaxAngebot.getId());
		Objects.requireNonNull(containerId.getId());

		FerienbetreuungAngabenContainer container =
			ferienbetreuungService.findFerienbetreuungAngabenContainer(containerId.getId())
				.orElseThrow(() -> new EbeguEntityNotFoundException(
					"ferienbetreuungAngebotAbschliessen",
					containerId.getId()));

		authorizer.checkWriteAuthorization(container);

		FerienbetreuungAngabenAngebot angebot =
			ferienbetreuungService.findFerienbetreuungAngabenAngebot(jaxAngebot.getId())
				.orElseThrow(() -> new EbeguEntityNotFoundException(
					"ferienbetreuungAngebotAbschliessen",
					jaxAngebot.getId()));

		angebot = converter.ferienbetreuungAngabenAngebotToEntity(jaxAngebot, angebot);

		FerienbetreuungAngabenAngebot persisted =
				ferienbetreuungService.ferienbetreuungAngebotAbschliessen(angebot);
		return converter.ferienbetreuungAngabenAngebotToJax(persisted);

	}

	@ApiOperation(
		value = "Öffnet FerienbetreuungAngabenAngebot zur Wiederbearbeitung als Gemeinde",
		response = JaxFerienbetreuungAngabenAngebot.class)
	@Nonnull
	@PUT
	@Path("/{containerId}/angebot/falsche-angaben")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		ADMIN_BG, SACHBEARBEITER_BG, ADMIN_TS, SACHBEARBEITER_TS, ADMIN_FERIENBETREUUNG,
		SACHBEARBEITER_FERIENBETREUUNG })
	public JaxFerienbetreuungAngabenAngebot ferienbetreuungAngebotFalscheAngaben(
		@Nonnull @NotNull @Valid JaxFerienbetreuungAngabenAngebot jaxAngebot,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response,
		@Nonnull @NotNull @PathParam("containerId") JaxId containerId
	) {
		Objects.requireNonNull(jaxAngebot.getId());
		Objects.requireNonNull(containerId.getId());

		FerienbetreuungAngabenContainer container =
			ferienbetreuungService.findFerienbetreuungAngabenContainer(containerId.getId())
				.orElseThrow(() -> new EbeguEntityNotFoundException(
					"ferienbetreuungAngebotFalscheAngaben",
					containerId.getId()));

		authorizer.checkWriteAuthorization(container);

		Preconditions.checkArgument(
			container.getStatus() == FerienbetreuungAngabenStatus.IN_BEARBEITUNG_GEMEINDE
				|| container.getStatus() == FerienbetreuungAngabenStatus.IN_PRUEFUNG_KANTON
				|| container.getStatus() == FerienbetreuungAngabenStatus.ZURUECK_AN_GEMEINDE,
			"FerienbetreuungAngabenContainer must be in state IN_BEARBEITUNG_GEMEINDE, ZURUECK_AN_GEMEINDE or IN_PRUEFUNG_KANTON");

		FerienbetreuungAngabenAngebot angebot =
			ferienbetreuungService.findFerienbetreuungAngabenAngebot(jaxAngebot.getId())
				.orElseThrow(() -> new EbeguEntityNotFoundException(
					"ferienbetreuungAngebotFalscheAngaben",
					jaxAngebot.getId()));

		angebot = converter.ferienbetreuungAngabenAngebotToEntity(jaxAngebot, angebot);

		FerienbetreuungAngabenAngebot persisted = ferienbetreuungService.ferienbetreuungAngebotFalscheAngaben(angebot);
		return converter.ferienbetreuungAngabenAngebotToJax(persisted);

	}

	@ApiOperation(
		value = "Speichert FerienbetreuungAngabenNutzung in der Datenbank",
		response = JaxFerienbetreuungAngabenNutzung.class)
	@Nonnull
	@PUT
	@Path("/{containerId}/nutzung/save")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		ADMIN_BG, SACHBEARBEITER_BG, ADMIN_TS, SACHBEARBEITER_TS, ADMIN_FERIENBETREUUNG,
		SACHBEARBEITER_FERIENBETREUUNG })
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
				.orElseThrow(() -> new EbeguEntityNotFoundException("saveFerienbetreuungNutzung",
					containerId.getId()));

		authorizer.checkWriteAuthorization(container);

		FerienbetreuungAngabenNutzung nutzung =
			ferienbetreuungService.findFerienbetreuungAngabenNutzung(jaxNutzung.getId())
				.orElseThrow(() -> new EbeguEntityNotFoundException("saveFerienbetreuungNutzung", jaxNutzung.getId()));

		nutzung = converter.ferienbetreuungAngabenNutzungToEntity(jaxNutzung, nutzung);

		FerienbetreuungAngabenNutzung persisted = ferienbetreuungService.saveFerienbetreuungAngabenNutzung(nutzung);
		return converter.ferienbetreuungAngabenNutzungToJax(persisted);
	}

	@ApiOperation(
		value = "Schliesst FerienbetreuungAngabenNutzung als Gemeinde ab",
		response = JaxFerienbetreuungAngabenNutzung.class)
	@Nonnull
	@PUT
	@Path("/{containerId}/nutzung/abschliessen")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		ADMIN_BG, SACHBEARBEITER_BG, ADMIN_TS, SACHBEARBEITER_TS, ADMIN_FERIENBETREUUNG,
		SACHBEARBEITER_FERIENBETREUUNG })
	public JaxFerienbetreuungAngabenNutzung ferienbetreuungNutzungAbschliessen(
		@Nonnull @NotNull @Valid JaxFerienbetreuungAngabenNutzung jaxNutzung,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response,
		@Nonnull @NotNull @PathParam("containerId") JaxId containerId
	) {
		Objects.requireNonNull(jaxNutzung.getId());
		Objects.requireNonNull(containerId.getId());

		FerienbetreuungAngabenContainer container =
			ferienbetreuungService.findFerienbetreuungAngabenContainer(containerId.getId())
				.orElseThrow(() -> new EbeguEntityNotFoundException(
					"ferienbetreuungNutzungAbschliessen",
					containerId.getId()));

		authorizer.checkWriteAuthorization(container);

		FerienbetreuungAngabenNutzung nutzung =
			ferienbetreuungService.findFerienbetreuungAngabenNutzung(jaxNutzung.getId())
				.orElseThrow(() -> new EbeguEntityNotFoundException(
					"ferienbetreuungNutzungAbschliessen",
					jaxNutzung.getId()));

		nutzung = converter.ferienbetreuungAngabenNutzungToEntity(jaxNutzung, nutzung);

		FerienbetreuungAngabenNutzung persisted =
			ferienbetreuungService.ferienbetreuungAngabenNutzungAbschliessen(nutzung);
		return converter.ferienbetreuungAngabenNutzungToJax(persisted);
	}

	@ApiOperation(
		value = "Öffnet FerienbetreuungAngabenNutzung zur Wiederbearbeitung als Gemeinde",
		response = JaxFerienbetreuungAngabenNutzung.class)
	@Nonnull
	@PUT
	@Path("/{containerId}/nutzung/falsche-angaben")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		ADMIN_BG, SACHBEARBEITER_BG, ADMIN_TS, SACHBEARBEITER_TS, ADMIN_FERIENBETREUUNG,
		SACHBEARBEITER_FERIENBETREUUNG })
	public JaxFerienbetreuungAngabenNutzung ferienbetreuungNutzungFalscheAngaben(
		@Nonnull @NotNull @Valid JaxFerienbetreuungAngabenNutzung jaxNutzung,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response,
		@Nonnull @NotNull @PathParam("containerId") JaxId containerId
	) {
		Objects.requireNonNull(jaxNutzung.getId());
		Objects.requireNonNull(containerId.getId());

		FerienbetreuungAngabenContainer container =
			ferienbetreuungService.findFerienbetreuungAngabenContainer(containerId.getId())
				.orElseThrow(() -> new EbeguEntityNotFoundException(
					"ferienbetreuungNutzungFalscheAngaben",
					containerId.getId()));

		authorizer.checkWriteAuthorization(container);

		Preconditions.checkArgument(
			container.getStatus() == FerienbetreuungAngabenStatus.IN_BEARBEITUNG_GEMEINDE
				|| container.getStatus() == FerienbetreuungAngabenStatus.IN_PRUEFUNG_KANTON
				|| container.getStatus() == FerienbetreuungAngabenStatus.ZURUECK_AN_GEMEINDE,
			"FerienbetreuungAngabenContainer must be in state IN_BEARBEITUNG_GEMEINDE, ZURUECK_AN_GEMEINDE or IN_PRUEFUNG_KANTON");

		FerienbetreuungAngabenNutzung nutzung =
			ferienbetreuungService.findFerienbetreuungAngabenNutzung(jaxNutzung.getId())
				.orElseThrow(() -> new EbeguEntityNotFoundException(
					"ferienbetreuungNutzungFalscheAngaben",
					jaxNutzung.getId()));

		nutzung = converter.ferienbetreuungAngabenNutzungToEntity(jaxNutzung, nutzung);

		FerienbetreuungAngabenNutzung persisted =
			ferienbetreuungService.ferienbetreuungAngabenNutzungFalscheAngaben(nutzung);
		return converter.ferienbetreuungAngabenNutzungToJax(persisted);
	}

	@ApiOperation(
		value = "Speichert FerienbetreuungAngabenKostenEinnahmen in der Datenbank",
		response = JaxFerienbetreuungAngabenKostenEinnahmen.class)
	@Nonnull
	@PUT
	@Path("/{containerId}/kostenEinnahmen/save")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		ADMIN_BG, SACHBEARBEITER_BG, ADMIN_TS, SACHBEARBEITER_TS, ADMIN_FERIENBETREUUNG,
		SACHBEARBEITER_FERIENBETREUUNG })
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
				.orElseThrow(() -> new EbeguEntityNotFoundException(
					"saveFerienbetreuungKostenEinnahmen",
					containerId.getId()));

		authorizer.checkWriteAuthorization(container);

		FerienbetreuungAngabenKostenEinnahmen kostenEinnahmen =
			ferienbetreuungService.findFerienbetreuungAngabenKostenEinnahmen(jaxKostenEinnahmen.getId())
				.orElseThrow(() -> new EbeguEntityNotFoundException(
					"saveFerienbetreuungKostenEinnahmen",
					jaxKostenEinnahmen.getId()));

		kostenEinnahmen = converter.ferienbetreuungAngabenKostenEinnahmenToEntity(jaxKostenEinnahmen, kostenEinnahmen);

		FerienbetreuungAngabenKostenEinnahmen persisted =
			ferienbetreuungService.saveFerienbetreuungAngabenKostenEinnahmen(kostenEinnahmen);
		return converter.ferienbetreuungAngabenKostenEinnahmenToJax(persisted);
	}

	@ApiOperation(
		value = "Speichert FerienbetreuungBerechnung in der Datenbank",
		response = JaxFerienbetreuungBerechnungen.class)
	@Nonnull
	@PUT
	@Path("/{containerId}/berechnung/save")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		ADMIN_BG, SACHBEARBEITER_BG, ADMIN_TS, SACHBEARBEITER_TS, ADMIN_FERIENBETREUUNG,
		SACHBEARBEITER_FERIENBETREUUNG })
	public JaxFerienbetreuungBerechnungen saveFerienbetreuungBerechnung(
		@Nonnull @NotNull @Valid JaxFerienbetreuungBerechnungen jaxFerienbetreuungBerechnungen,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response,
		@Nonnull @NotNull @PathParam("containerId") JaxId containerId
	) {
		Objects.requireNonNull(jaxFerienbetreuungBerechnungen.getId());
		Objects.requireNonNull(containerId.getId());

		FerienbetreuungAngabenContainer container =
			ferienbetreuungService.findFerienbetreuungAngabenContainer(containerId.getId())
				.orElseThrow(() -> new EbeguEntityNotFoundException(
					"saveFerienbetreuungBerechnung",
					containerId.getId()));

		authorizer.checkWriteAuthorization(container);

		FerienbetreuungBerechnungen berechnungen =
			ferienbetreuungService.findFerienbetreuungBerechnung(jaxFerienbetreuungBerechnungen.getId())
				.orElseThrow(() -> new EbeguEntityNotFoundException(
					"saveFerienbetreuungKostenEinnahmen",
					jaxFerienbetreuungBerechnungen.getId()));

		berechnungen = converter.ferienbetreuungBerechnungentoEntity(jaxFerienbetreuungBerechnungen, berechnungen);

		FerienbetreuungBerechnungen persisted =
			ferienbetreuungService.saveFerienbetreuungBerechnungen(berechnungen);
		return converter.ferienbetreuungBerechnungenToJax(persisted);
	}

	@ApiOperation(
		value = "Schliesst FerienbetreuungAngabenNutzung als Gemeinde ab",
		response = JaxFerienbetreuungAngabenNutzung.class)
	@Nonnull
	@PUT
	@Path("/{containerId}/kostenEinnahmen/abschliessen")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		ADMIN_BG, SACHBEARBEITER_BG, ADMIN_TS, SACHBEARBEITER_TS, ADMIN_FERIENBETREUUNG,
		SACHBEARBEITER_FERIENBETREUUNG })
	public JaxFerienbetreuungAngabenKostenEinnahmen ferienbetreuungKostenEinnahmenAbschliessen(
		@Nonnull @NotNull @Valid JaxFerienbetreuungAngabenKostenEinnahmen jaxKostenEinnamen,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response,
		@Nonnull @NotNull @PathParam("containerId") JaxId containerId
	) {
		Objects.requireNonNull(jaxKostenEinnamen.getId());
		Objects.requireNonNull(containerId.getId());

		FerienbetreuungAngabenContainer container =
			ferienbetreuungService.findFerienbetreuungAngabenContainer(containerId.getId())
				.orElseThrow(() -> new EbeguEntityNotFoundException(
					"ferienbetreuungKostenEinnahmenAbschliessen",
					containerId.getId()));

		authorizer.checkWriteAuthorization(container);

		FerienbetreuungAngabenKostenEinnahmen kostenEinnahmen =
			ferienbetreuungService.findFerienbetreuungAngabenKostenEinnahmen(jaxKostenEinnamen.getId())
				.orElseThrow(() -> new EbeguEntityNotFoundException(
					"ferienbetreuungKostenEinnahmenAbschliessen",
					jaxKostenEinnamen
						.getId()));

		kostenEinnahmen = converter.ferienbetreuungAngabenKostenEinnahmenToEntity(jaxKostenEinnamen, kostenEinnahmen);

		FerienbetreuungAngabenKostenEinnahmen persisted =
			ferienbetreuungService.ferienbetreuungAngabenKostenEinnahmenAbschliessen(kostenEinnahmen);
		return converter.ferienbetreuungAngabenKostenEinnahmenToJax(persisted);
	}

	@ApiOperation(
		value = "Öffnet FerienbetreuungAngabenKostenEinnahmen zur Wiederbearbeitung als Gemeinde",
		response = JaxFerienbetreuungAngabenNutzung.class)
	@Nonnull
	@PUT
	@Path("/{containerId}/kostenEinnahmen/falsche-angaben")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		ADMIN_BG, SACHBEARBEITER_BG, ADMIN_TS, SACHBEARBEITER_TS, ADMIN_FERIENBETREUUNG,
		SACHBEARBEITER_FERIENBETREUUNG })
	public JaxFerienbetreuungAngabenKostenEinnahmen ferienbetreuungKostenEinnahmenFalscheAngaben(
		@Nonnull @NotNull @Valid JaxFerienbetreuungAngabenKostenEinnahmen jaxKostenEinnahmen,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response,
		@Nonnull @NotNull @PathParam("containerId") JaxId containerId
	) {
		Objects.requireNonNull(jaxKostenEinnahmen.getId());
		Objects.requireNonNull(containerId.getId());

		FerienbetreuungAngabenContainer container =
			ferienbetreuungService.findFerienbetreuungAngabenContainer(containerId.getId())
				.orElseThrow(() -> new EbeguEntityNotFoundException(
					"ferienbetreuungKostenEinnahmenFalscheAngaben",
					containerId.getId()));

		authorizer.checkWriteAuthorization(container);

		Preconditions.checkArgument(
			container.getStatus() == FerienbetreuungAngabenStatus.IN_BEARBEITUNG_GEMEINDE
				|| container.getStatus() == FerienbetreuungAngabenStatus.IN_PRUEFUNG_KANTON
				|| container.getStatus() == FerienbetreuungAngabenStatus.ZURUECK_AN_GEMEINDE,
			"FerienbetreuungAngabenContainer must be in state IN_BEARBEITUNG_GEMEINDE, ZURUECK_AN_GEMEINDE or IN_PRUEFUNG_KANTON");

		FerienbetreuungAngabenKostenEinnahmen kostenEinnahmen =
			ferienbetreuungService.findFerienbetreuungAngabenKostenEinnahmen(jaxKostenEinnahmen.getId())
				.orElseThrow(() -> new EbeguEntityNotFoundException(
					"ferienbetreuungKostenEinnahmenFalscheAngaben",
					jaxKostenEinnahmen
						.getId()));

		kostenEinnahmen = converter.ferienbetreuungAngabenKostenEinnahmenToEntity(jaxKostenEinnahmen, kostenEinnahmen);

		FerienbetreuungAngabenKostenEinnahmen persisted =
			ferienbetreuungService.ferienbetreuungAngabenKostenEinnahmenFalscheAngaben(kostenEinnahmen);
		return converter.ferienbetreuungAngabenKostenEinnahmenToJax(persisted);
	}

	@ApiOperation("Generiert den Report als PDF")
	@Nonnull
	@GET
	@Path("/{containerId}/report")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
			ADMIN_BG, SACHBEARBEITER_BG, ADMIN_TS, SACHBEARBEITER_TS, ADMIN_FERIENBETREUUNG,
			SACHBEARBEITER_FERIENBETREUUNG })
	public Response getFerienbetreuungReport(
			@Context UriInfo uriInfo,
			@Context HttpServletResponse response,
			@Nonnull @NotNull @PathParam("containerId") JaxId containerId
	) throws MergeDocException {
		Objects.requireNonNull(containerId.getId());

		FerienbetreuungAngabenContainer container =
				ferienbetreuungService.findFerienbetreuungAngabenContainer(containerId.getId())
						.orElseThrow(() -> new EbeguEntityNotFoundException(
								"ferienbetreuungKostenEinnahmenFalscheAngaben",
								containerId.getId()));

		authorizer.checkReadAuthorization(container);

		final Locale locale = LocaleThreadLocal.get();
		Sprache sprache = Sprache.fromLocale(locale);

		final byte[] content = ferienbetreuungService.generateFerienbetreuungReportDokument(container, sprache);

		if (content != null && content.length > 0) {
			try {
				return RestUtil.buildDownloadResponse(true, "ferienbetreuungReport.pdf",
						"application/octet-stream", content);

			} catch (IOException e) {
				return Response.status(Status.NOT_FOUND)
						.entity("Ferienbetreuung PDF Export kann nicht generiert werden")
						.build();
			}
		}

		return Response.status(Status.NO_CONTENT).build();
	}

}
