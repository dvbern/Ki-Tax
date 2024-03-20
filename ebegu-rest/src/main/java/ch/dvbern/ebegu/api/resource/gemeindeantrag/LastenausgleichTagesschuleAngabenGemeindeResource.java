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

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.dtos.JaxLastenausgleichTagesschulenStatusHistory;
import ch.dvbern.ebegu.api.dtos.gemeindeantrag.JaxBetreuungsstundenPrognose;
import ch.dvbern.ebegu.api.dtos.gemeindeantrag.JaxLastenausgleichTagesschuleAngabenGemeindeContainer;
import ch.dvbern.ebegu.api.dtos.gemeindeantrag.JaxLastenausgleichTagesschulePrognose;
import ch.dvbern.ebegu.api.util.RestUtil;
import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeContainer;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeStatusHistory;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.errors.MergeDocException;
import ch.dvbern.ebegu.i18n.LocaleThreadLocal;
import ch.dvbern.ebegu.services.InstitutionService;
import ch.dvbern.ebegu.services.authentication.AuthorizerImpl;
import ch.dvbern.ebegu.services.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeService;
import ch.dvbern.ebegu.services.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeStatusHistoryService;
import ch.dvbern.ebegu.services.gemeindeantrag.LastenausgleichTagesschuleAngabenInstitutionService;
import ch.dvbern.ebegu.services.gemeindeantrag.LastenausgleichTagesschuleDokumentService;
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
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static ch.dvbern.ebegu.api.resource.util.ResourceConstants.DOCX_FILE_EXTENSION;
import static ch.dvbern.ebegu.enums.UserRoleName.*;

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
	private LastenausgleichTagesschuleAngabenInstitutionService angabenInstitutionService;

	@Inject
	private LastenausgleichTagesschuleDokumentService latsDokumentService;

	@Inject
	private JaxBConverter converter;

	@Inject
	private AuthorizerImpl authorizer;

	@Inject
	private PrincipalBean principal;

	@Inject
	private InstitutionService institutionService;

	@Inject
	private LastenausgleichTagesschuleAngabenGemeindeStatusHistoryService historyService;

	@ApiOperation(
		value = "Gibt den LastenausgleichTagesschuleAngabenGemeindeContainer mit der uebergebenen Id zurueck",
		response = JaxLastenausgleichTagesschuleAngabenGemeindeContainer.class)
	@Nullable
	@GET
	@Path("/find/{latsGemeindeAngabenJaxId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, SACHBEARBEITER_INSTITUTION, ADMIN_INSTITUTION,
		ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_TS, SACHBEARBEITER_TS, ADMIN_TRAEGERSCHAFT,
		SACHBEARBEITER_TRAEGERSCHAFT })
	public JaxLastenausgleichTagesschuleAngabenGemeindeContainer findLastenausgleichTagesschuleAngabenGemeindeContainer(
		@Nonnull @NotNull @PathParam("latsGemeindeAngabenJaxId") JaxId latsGemeindeAngabenJaxId
	) {
		Objects.requireNonNull(latsGemeindeAngabenJaxId);
		Objects.requireNonNull(latsGemeindeAngabenJaxId.getId());

		authorizer.checkReadAuthorizationLATSGemeindeAntrag(latsGemeindeAngabenJaxId.getId());

		final Optional<LastenausgleichTagesschuleAngabenGemeindeContainer> latsGemeindeContainerOptional =
			angabenGemeindeService.findLastenausgleichTagesschuleAngabenGemeindeContainer(converter.toEntityId(
				latsGemeindeAngabenJaxId));

		return latsGemeindeContainerOptional
			.map(lastenausgleichTagesschuleAngabenGemeindeContainer ->
				converter.lastenausgleichTagesschuleAngabenGemeindeContainerToJax(
					lastenausgleichTagesschuleAngabenGemeindeContainer))
			// remove insti containers that the user is not allowed to read
			.map(jaxLastenausgleichTagesschuleAngabenGemeindeContainer -> {
				if (principal.isCallerInAnyOfRole(UserRole.getInstitutionTraegerschaftRoles())) {
					jaxLastenausgleichTagesschuleAngabenGemeindeContainer.setAngabenInstitutionContainers(
						jaxLastenausgleichTagesschuleAngabenGemeindeContainer.getAngabenInstitutionContainers()
							.stream()
							.filter(instiContainer -> institutionService.getInstitutionenReadableForCurrentBenutzer(
								false)
								.stream()
								.anyMatch(userInstitution -> userInstitution.getId()
									.equals(instiContainer.getInstitution().getId()))
							).collect(Collectors.toSet()));
					jaxLastenausgleichTagesschuleAngabenGemeindeContainer.setAngabenDeklaration(null);
					jaxLastenausgleichTagesschuleAngabenGemeindeContainer.setAngabenKorrektur(null);
				}
				return jaxLastenausgleichTagesschuleAngabenGemeindeContainer;
			})
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
		ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_TS, SACHBEARBEITER_TS })
	public JaxLastenausgleichTagesschuleAngabenGemeindeContainer saveLastenausgleichTagesschuleGemeinde(
		@Nonnull @NotNull @Valid JaxLastenausgleichTagesschuleAngabenGemeindeContainer latsGemeindeContainerJax,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {
		Objects.requireNonNull(latsGemeindeContainerJax.getId());
		Objects.requireNonNull(latsGemeindeContainerJax.getGemeinde().getId());

		authorizer.checkWriteAuthorizationLATSGemeindeAntrag(latsGemeindeContainerJax.getId());

		final LastenausgleichTagesschuleAngabenGemeindeContainer converted =
			getConvertedLastenausgleichTagesschuleAngabenGemeindeContainer(latsGemeindeContainerJax);

		final LastenausgleichTagesschuleAngabenGemeindeContainer saved =
			angabenGemeindeService.saveLastenausgleichTagesschuleGemeinde(converted);

		return converter.lastenausgleichTagesschuleAngabenGemeindeContainerToJax(saved);
	}

	@ApiOperation(
		value = "Gibt den LastenausgleichTagesschuleAngabenGemeindeContainer frei fuer die Bearbeitung durch die "
			+ "Institutionen",
		response = JaxLastenausgleichTagesschuleAngabenGemeindeContainer.class)
	@Nonnull
	@PUT
	@Path("/freigebenInstitution")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT,
		ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_TS, SACHBEARBEITER_TS })
	public JaxLastenausgleichTagesschuleAngabenGemeindeContainer lastenausgleichTagesschuleGemeindeFuerInstitutionenFreigeben(
		@Nonnull @NotNull @Valid JaxLastenausgleichTagesschuleAngabenGemeindeContainer latsGemeindeContainerJax,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {
		Objects.requireNonNull(latsGemeindeContainerJax.getId());
		Objects.requireNonNull(latsGemeindeContainerJax.getGemeinde().getId());

		authorizer.checkWriteAuthorizationLATSGemeindeAntrag(latsGemeindeContainerJax.getId());

		final LastenausgleichTagesschuleAngabenGemeindeContainer converted =
			getConvertedLastenausgleichTagesschuleAngabenGemeindeContainer(latsGemeindeContainerJax);

		final LastenausgleichTagesschuleAngabenGemeindeContainer saved =
			angabenGemeindeService.lastenausgleichTagesschuleGemeindeFuerInstitutionenFreigeben(converted);

		return converter.lastenausgleichTagesschuleAngabenGemeindeContainerToJax(saved);
	}

	@SuppressWarnings("PMD.PreserveStackTrace")
	@ApiOperation(
		value = "Schliesst das LastenausgleichTagesschuleAngabenGemeinde Formular ab",
		response = JaxLastenausgleichTagesschuleAngabenGemeindeContainer.class)
	@Nonnull
	@PUT
	@Path("/gemeinde/abschliessen")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT,
		ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_TS, SACHBEARBEITER_TS })
	public JaxLastenausgleichTagesschuleAngabenGemeindeContainer lastenausgleichTagesschuleGemeindeFormularAbschliessen(
		@Nonnull @NotNull @Valid JaxLastenausgleichTagesschuleAngabenGemeindeContainer latsGemeindeContainerJax,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {
		Objects.requireNonNull(latsGemeindeContainerJax.getId());
		Objects.requireNonNull(latsGemeindeContainerJax.getGemeinde().getId());

		authorizer.checkWriteAuthorizationLATSGemeindeAntrag(latsGemeindeContainerJax.getId());

		final LastenausgleichTagesschuleAngabenGemeindeContainer converted =
			getConvertedLastenausgleichTagesschuleAngabenGemeindeContainer(latsGemeindeContainerJax);

		final LastenausgleichTagesschuleAngabenGemeindeContainer saved =
				angabenGemeindeService.lastenausgleichTagesschuleGemeindeFormularAbschliessen(converted);

		return converter.lastenausgleichTagesschuleAngabenGemeindeContainerToJax(saved);

	}

	@SuppressWarnings("PMD.PreserveStackTrace")
	@ApiOperation(
		value = "Reicht den Lastenausgleich ein",
		response = JaxLastenausgleichTagesschuleAngabenGemeindeContainer.class)
	@Nonnull
	@PUT
	@Path("/einreichen")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT,
		ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_TS, SACHBEARBEITER_TS })
	public JaxLastenausgleichTagesschuleAngabenGemeindeContainer lastenausgleichTagesschuleGemeindeEinreichen(
		@Nonnull @NotNull @Valid JaxLastenausgleichTagesschuleAngabenGemeindeContainer latsGemeindeContainerJax,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {
		Objects.requireNonNull(latsGemeindeContainerJax.getId());
		Objects.requireNonNull(latsGemeindeContainerJax.getGemeinde().getId());

		authorizer.checkWriteAuthorizationLATSGemeindeAntrag(latsGemeindeContainerJax.getId());

		final LastenausgleichTagesschuleAngabenGemeindeContainer converted =
			getConvertedLastenausgleichTagesschuleAngabenGemeindeContainer(latsGemeindeContainerJax);

		final LastenausgleichTagesschuleAngabenGemeindeContainer saved =
				angabenGemeindeService.lastenausgleichTagesschuleGemeindeEinreichen(converted);
		return converter.lastenausgleichTagesschuleAngabenGemeindeContainerToJax(saved);

	}

	@SuppressWarnings("PMD.PreserveStackTrace")
	@ApiOperation(
		value = "Schliesst den Lastenausgleich Tagesschule ab",
		response = JaxLastenausgleichTagesschuleAngabenGemeindeContainer.class)
	@Nonnull
	@PUT
	@Path("/abschliessen")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public JaxLastenausgleichTagesschuleAngabenGemeindeContainer lastenausgleichTagesschuleGemeindeAbschliessen(
		@Nonnull @NotNull @Valid JaxLastenausgleichTagesschuleAngabenGemeindeContainer latsGemeindeContainerJax,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {
		Objects.requireNonNull(latsGemeindeContainerJax.getId());
		Objects.requireNonNull(latsGemeindeContainerJax.getGemeinde().getId());

		authorizer.checkWriteAuthorizationLATSGemeindeAntrag(latsGemeindeContainerJax.getId());

		final LastenausgleichTagesschuleAngabenGemeindeContainer converted =
			getConvertedLastenausgleichTagesschuleAngabenGemeindeContainer(latsGemeindeContainerJax);

		final LastenausgleichTagesschuleAngabenGemeindeContainer saved =
				angabenGemeindeService.lastenausgleichTagesschuleGemeindeAbschliessen(converted);
		return converter.lastenausgleichTagesschuleAngabenGemeindeContainerToJax(saved);

	}

	@ApiOperation(
		value = "Bestätigt die Prüfung durch den Kanton",
		response = JaxLastenausgleichTagesschuleAngabenGemeindeContainer.class)
	@Nonnull
	@PUT
	@Path("/geprueft")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public JaxLastenausgleichTagesschuleAngabenGemeindeContainer lastenausgleichTagesschuleGemeindePruefen(
		@Nonnull @NotNull @Valid JaxLastenausgleichTagesschuleAngabenGemeindeContainer latsGemeindeContainerJax,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {
		Objects.requireNonNull(latsGemeindeContainerJax.getId());
		Objects.requireNonNull(latsGemeindeContainerJax.getGemeinde().getId());

		authorizer.checkWriteAuthorizationLATSGemeindeAntrag(latsGemeindeContainerJax.getId());

		final LastenausgleichTagesschuleAngabenGemeindeContainer converted =
			getConvertedLastenausgleichTagesschuleAngabenGemeindeContainer(latsGemeindeContainerJax);

		final LastenausgleichTagesschuleAngabenGemeindeContainer saved =
			angabenGemeindeService.lastenausgleichTagesschuleGemeindePruefen(converted);

		return converter.lastenausgleichTagesschuleAngabenGemeindeContainerToJax(saved);
	}

	@Nonnull
	private LastenausgleichTagesschuleAngabenGemeindeContainer getConvertedLastenausgleichTagesschuleAngabenGemeindeContainer(
		@Nonnull JaxLastenausgleichTagesschuleAngabenGemeindeContainer latsGemeindeContainerJax
	) {
		Objects.requireNonNull(latsGemeindeContainerJax);
		Objects.requireNonNull(latsGemeindeContainerJax.getId());
		Objects.requireNonNull(latsGemeindeContainerJax.getGemeinde().getId());

		authorizer.checkReadAuthorizationLATSGemeindeAntrag(latsGemeindeContainerJax.getId());

		// Das Objekt muss in der DB schon vorhanden sein, da die Erstellung immer ueber den GemeindeAntragService
		// geschieht
		final LastenausgleichTagesschuleAngabenGemeindeContainer latsGemeindeContainer =
			angabenGemeindeService.findLastenausgleichTagesschuleAngabenGemeindeContainer(latsGemeindeContainerJax.getId())
				.orElseThrow(() -> new EbeguEntityNotFoundException(
					"getConvertedLastenausgleichTagesschuleAngabenGemeindeContainer",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					latsGemeindeContainerJax.getId()));

		final LastenausgleichTagesschuleAngabenGemeindeContainer converted =
			converter.lastenausgleichTagesschuleAngabenGemeindeContainerToEntity(
				latsGemeindeContainerJax,
				latsGemeindeContainer);
		return converted;
	}

	@ApiOperation(
		value = "Speichert die Kommentare eines LastenausgleichTagesschuleAngabenGemeindeContainer in der Datenbank",
		response = Void.class)
	@PUT
	@Path("/saveKommentar/{containerId}")
	@Consumes(MediaType.TEXT_PLAIN)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public void saveLATSKommentar(
		@Nonnull String kommentar,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response,
		@Nonnull @NotNull @PathParam("containerId") JaxId containerId
	) {
		Objects.requireNonNull(containerId);
		Objects.requireNonNull(kommentar);

		angabenGemeindeService.saveKommentar(containerId.getId(), kommentar);
	}

	@ApiOperation(
		value = "Speichert den Verantwortlichen eines LastenausgleichTagesschuleAngabenGemeindeContainer in der Datenbank",
		response = Void.class)
	@PUT
	@Path("/saveLATSVerantworlicher/{containerId}")
	@Consumes(MediaType.WILDCARD)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public void saveLATSVerantworlicher(
		@Nullable String username,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response,
		@Nonnull @NotNull @PathParam("containerId") JaxId containerId
	) {
		Objects.requireNonNull(containerId);

		angabenGemeindeService.saveVerantwortlicher(containerId.getId(), username);
	}

	@ApiOperation(
		value = "Speichert die Betreuungsstunden Prognose eines LastenausgleichTagesschuleAngabenGemeindeContainer in der Datenbank",
		response = Void.class)
	@PUT
	@Path("/savePrognose/{containerId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public void saveLATSPrognose(
		@Nonnull JaxLastenausgleichTagesschulePrognose jaxPrognose,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response,
		@Nonnull @NotNull @PathParam("containerId") JaxId containerId
	) {
		Objects.requireNonNull(containerId);
		Objects.requireNonNull(jaxPrognose);

		angabenGemeindeService.savePrognose(containerId.getId(), jaxPrognose.getPrognose(), jaxPrognose.getBemerkungen());
	}

	@ApiOperation(
		value = "Setzt ein LastenausgleichTagesschuleAngabenGemeinde von Abgeschlossen auf In Bearbeitung Gemeinde",
		response = JaxLastenausgleichTagesschuleAngabenGemeindeContainer.class)
	@PUT
	@Path("/falsche-angaben")
	@Consumes(MediaType.APPLICATION_JSON)
	@Nonnull
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_BG,
		SACHBEARBEITER_BG, ADMIN_TS, SACHBEARBEITER_TS })
	public JaxLastenausgleichTagesschuleAngabenGemeindeContainer falscheAngaben(
		@Nonnull @NotNull @Valid JaxLastenausgleichTagesschuleAngabenGemeindeContainer latsGemeindeContainerJax,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {
		Objects.requireNonNull(latsGemeindeContainerJax.getId());
		Objects.requireNonNull(latsGemeindeContainerJax.getGemeinde().getId());

		authorizer.checkWriteAuthorizationLATSGemeindeAntrag(latsGemeindeContainerJax.getId());

		final LastenausgleichTagesschuleAngabenGemeindeContainer converted =
			getConvertedLastenausgleichTagesschuleAngabenGemeindeContainer(latsGemeindeContainerJax);

		final LastenausgleichTagesschuleAngabenGemeindeContainer wiederEroeffnet =
			angabenGemeindeService.lastenausgleichTagesschuleGemeindeWiederOeffnen(converted);

		return converter.lastenausgleichTagesschuleAngabenGemeindeContainerToJax(wiederEroeffnet);
	}

	@ApiOperation(
		value = "Setzt ein LastenausgleichTagesschuleAngabenGemeinde von IN_PRUEFUNG_KANTON auf IN_BEARBEITUNG_GEMEINDE",
		response = Void.class)
	@PUT
	@Path("/zurueck-an-gemeinde")
	@Consumes(MediaType.APPLICATION_JSON)
	@Nonnull
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public JaxLastenausgleichTagesschuleAngabenGemeindeContainer zurueckAnGemeinde(
		@Nonnull @NotNull @Valid JaxLastenausgleichTagesschuleAngabenGemeindeContainer latsGemeindeContainerJax,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {
		Objects.requireNonNull(latsGemeindeContainerJax.getId());
		Objects.requireNonNull(latsGemeindeContainerJax.getGemeinde().getId());

		authorizer.checkWriteAuthorizationLATSGemeindeAntrag(latsGemeindeContainerJax.getId());

		final LastenausgleichTagesschuleAngabenGemeindeContainer converted =
			getConvertedLastenausgleichTagesschuleAngabenGemeindeContainer(latsGemeindeContainerJax);

		final LastenausgleichTagesschuleAngabenGemeindeContainer wiederEroeffnet =
			angabenGemeindeService.lastenausgleichTagesschuleGemeindeZurueckAnGemeinde(converted);

		return converter.lastenausgleichTagesschuleAngabenGemeindeContainerToJax(wiederEroeffnet);
	}

	@ApiOperation(
		value = "Setzt ein LastenausgleichTagesschuleAngabenGemeinde von GEPRUEFT auf IN_PRUEFUNG_KANTON",
		response = Void.class)
	@PUT
	@Path("/zurueck-in-pruefung")
	@Consumes(MediaType.APPLICATION_JSON)
	@Nonnull
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public JaxLastenausgleichTagesschuleAngabenGemeindeContainer zurueckInPruefungKanton(
		@Nonnull @NotNull @Valid JaxLastenausgleichTagesschuleAngabenGemeindeContainer latsGemeindeContainerJax,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {
		Objects.requireNonNull(latsGemeindeContainerJax.getId());
		Objects.requireNonNull(latsGemeindeContainerJax.getGemeinde().getId());

		authorizer.checkWriteAuthorizationLATSGemeindeAntrag(latsGemeindeContainerJax.getId());

		final LastenausgleichTagesschuleAngabenGemeindeContainer converted =
			getConvertedLastenausgleichTagesschuleAngabenGemeindeContainer(latsGemeindeContainerJax);

		final LastenausgleichTagesschuleAngabenGemeindeContainer wiederEroeffnet =
			angabenGemeindeService.lastenausgleichTagesschuleGemeindeZurueckInPruefungKanton(converted);

		return converter.lastenausgleichTagesschuleAngabenGemeindeContainerToJax(wiederEroeffnet);
	}

	@ApiOperation(
		value = "Gibt den Statushistory für die übergebene latsContainerId zurueck",
		response = JaxLastenausgleichTagesschulenStatusHistory.class)
	@Nullable
	@GET
	@Path("/verlauf/{containerJaxId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT,
		ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_TS, SACHBEARBEITER_TS })
	public List<JaxLastenausgleichTagesschulenStatusHistory> findLatsStatusHistroy(
		@Nonnull @NotNull @PathParam("containerJaxId") JaxId containerJaxId
	) {
		Objects.requireNonNull(containerJaxId);
		Objects.requireNonNull(containerJaxId.getId());

		LastenausgleichTagesschuleAngabenGemeindeContainer container =
			angabenGemeindeService.findLastenausgleichTagesschuleAngabenGemeindeContainer(containerJaxId.getId())
				.orElseThrow(() -> new EbeguEntityNotFoundException(
					"findLatsStatusHistroy",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					containerJaxId.getId()
				));

		authorizer.checkReadAuthorization(container);

		List<LastenausgleichTagesschuleAngabenGemeindeStatusHistory> historyList =
			historyService.findHistoryForContainer(container);

		return historyList.stream()
			.map(history -> converter.latsStatusHistoryToJAX(history))
			.collect(Collectors.toList());
	}


	@ApiOperation(
		value = "Findet den Lastenausgleichantrag des Vorjahres zum übergebenen Antrag",
		response = JaxLastenausgleichTagesschuleAngabenGemeindeContainer.class)
	@GET
	@Path("/previous-antrag/{currentContainerJaxId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Nullable
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_TS, SACHBEARBEITER_TS })
	public JaxLastenausgleichTagesschuleAngabenGemeindeContainer findAntragOfPreviousPeriode(
		@Nonnull @NotNull @PathParam("currentContainerJaxId") JaxId currentContainerJaxId,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {
		Objects.requireNonNull(currentContainerJaxId);
		Objects.requireNonNull(currentContainerJaxId.getId());
		LastenausgleichTagesschuleAngabenGemeindeContainer previousAntrag =
			angabenGemeindeService.findContainerOfPreviousPeriode(currentContainerJaxId.getId());

		if (previousAntrag == null) {
			return null;
		}
		return converter.lastenausgleichTagesschuleAngabenGemeindeContainerToJax(previousAntrag);
	}

	@ApiOperation(
		value = "Berechnet die erwarteten Betreuungsstunden für den übergebenen Antrag",
		response = Number.class)
	@GET
	@Path("/erwartete-betreuungsstunden/{containerJaxId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Nullable
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_TS, SACHBEARBEITER_TS })
	public Number calculateErwarteteBetreuungsstunden(
		@Nonnull @NotNull @PathParam("containerJaxId") JaxId containerJaxId,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {
		Objects.requireNonNull(containerJaxId);
		Objects.requireNonNull(containerJaxId.getId());

		return angabenGemeindeService.calculateErwarteteBetreuungsstunden(containerJaxId.getId());
	}

	@ApiOperation(
		value = "Berechnet die erwarteten Betreuungsstunden Prognossen für die nächste Gesuchsperiode für den übergebenen Antrag",
		response = Number.class)
	@GET
	@Path("/erwartete-betreuungsstunden-prognose/{containerJaxId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Nullable
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_TS, SACHBEARBEITER_TS })
	public Number calculateErwarteteBetreuungsstundenNextYear(
		@Nonnull @NotNull @PathParam("containerJaxId") JaxId containerJaxId,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {
		Objects.requireNonNull(containerJaxId);
		Objects.requireNonNull(containerJaxId.getId());

		return angabenGemeindeService.calculateErwarteteBetreuungsstundenPrognose(containerJaxId.getId());
	}


	@ApiOperation(
		value = "Erstellt ein Docx Dokument zum Lastenausgleich Tagesschulen für den übergebenen Gemeindeantrag",
		response = Response.class)
	@POST
	@Path("/docx-erstellen/{containerJaxId}/{sprache}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public Response dokumentErstellen(
		@Nonnull JaxBetreuungsstundenPrognose betreuungsstundenPrognose,
		@Nonnull @NotNull @PathParam("containerJaxId") JaxId containerJaxId,
		@Nonnull @PathParam("sprache") Sprache sprache,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {
		Objects.requireNonNull(containerJaxId);
		Objects.requireNonNull(containerJaxId.getId());

		byte[] document;
		document = latsDokumentService.createDocx(containerJaxId.getId(), sprache, betreuungsstundenPrognose.getBetreuungsstundenPrognose());

		if (document.length > 0) {
			try {
				return RestUtil.buildDownloadResponse(true, DOCX_FILE_EXTENSION,
					"application/octet-stream", document);

			} catch (IOException e) {
				throw new EbeguRuntimeException("dokumentErstellen", "error occured while building response", e);
			}
		}

		throw new EbeguRuntimeException("dokumentErstellen", "Lats Template has no content");

	}

	@ApiOperation(
		value = "Erstellt fehlende LastenausgleichTagesschuleInstitutionContainers für den gegebenen Gemeindeantrag",
		response = void.class)
	@POST
	@Path("/create-missing-institutions/{gemeindeAngabenId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_TS, SACHBEARBEITER_TS })
	public void createMissingInstitutions(
		@Nonnull @PathParam("gemeindeAngabenId") JaxId gemeindeAngabenId,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {
		Objects.requireNonNull(gemeindeAngabenId.getId());

		authorizer.checkWriteAuthorizationLATSGemeindeAntrag(gemeindeAngabenId.getId());

		final LastenausgleichTagesschuleAngabenGemeindeContainer container =
			angabenGemeindeService.findLastenausgleichTagesschuleAngabenGemeindeContainer(gemeindeAngabenId.getId())
			.orElseThrow(() -> new EbeguEntityNotFoundException("createMissingInstitutions", gemeindeAngabenId.getId()));

		angabenInstitutionService.createLastenausgleichTagesschuleInstitution(container);
	}


	@ApiOperation("Generiert den Report des LATS Containers")
	@Nonnull
	@GET
	@Path("/{containerId}/report")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
			ADMIN_BG, SACHBEARBEITER_BG, ADMIN_TS, SACHBEARBEITER_TS, ADMIN_FERIENBETREUUNG,
			SACHBEARBEITER_FERIENBETREUUNG })
	public Response getLATSReport(
			@Context UriInfo uriInfo,
			@Context HttpServletResponse response,
			@Nonnull @NotNull @PathParam("containerId") JaxId containerId
	) throws MergeDocException {
		Objects.requireNonNull(containerId.getId());

		LastenausgleichTagesschuleAngabenGemeindeContainer container =
				angabenGemeindeService.findLastenausgleichTagesschuleAngabenGemeindeContainer(containerId.getId())
						.orElseThrow(() -> new EbeguEntityNotFoundException(
								"getLATSReport",
								containerId.getId()));

		authorizer.checkReadAuthorization(container);

		final Locale locale = LocaleThreadLocal.get();
		Sprache sprache = Sprache.fromLocale(locale);
		final byte[] content = latsDokumentService.generateLATSReportDokument(container, sprache);

		if (content != null && content.length > 0) {
			try {
				return RestUtil.buildDownloadResponse(true, "report.pdf",
						"application/octet-stream", content);

			} catch (IOException e) {
				return Response.status(Status.NOT_FOUND)
						.entity("LATS Report kann nicht generiert werden")
						.build();
			}
		}

		return Response.status(Status.NO_CONTENT).build();
	}
}
