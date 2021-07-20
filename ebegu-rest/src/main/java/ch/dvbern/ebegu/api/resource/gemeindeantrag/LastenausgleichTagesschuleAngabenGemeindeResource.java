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

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.security.DenyAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJBTransactionRolledbackException;
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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.dtos.JaxLastenausgleichTagesschulenStatusHistory;
import ch.dvbern.ebegu.api.dtos.gemeindeantrag.JaxLastenausgleichTagesschuleAngabenGemeindeContainer;
import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeContainer;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeStatusHistory;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.services.InstitutionService;
import ch.dvbern.ebegu.services.authentication.AuthorizerImpl;
import ch.dvbern.ebegu.services.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeService;
import ch.dvbern.ebegu.services.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeStatusHistoryService;
import ch.dvbern.ebegu.services.gemeindeantrag.LastenausgleichTagesschuleDokumentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TRAEGERSCHAFT;
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
	@Path("/abschliessen")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT,
		ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_TS, SACHBEARBEITER_TS })
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

		try {
			final LastenausgleichTagesschuleAngabenGemeindeContainer saved =
				angabenGemeindeService.lastenausgleichTagesschuleGemeindeFormularAbschliessen(converted);

			return converter.lastenausgleichTagesschuleAngabenGemeindeContainerToJax(saved);
		} catch (EJBTransactionRolledbackException e) {
			if (e.getCause() instanceof IllegalArgumentException || e.getCause() instanceof IllegalStateException) {
				throw new WebApplicationException(Response.status(Status.BAD_REQUEST)
					.entity(e.getMessage())
					.type(MediaType.TEXT_PLAIN)
					.build());
			}
			throw e;
		}
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

		try {
			final LastenausgleichTagesschuleAngabenGemeindeContainer saved =
				angabenGemeindeService.lastenausgleichTagesschuleGemeindeEinreichen(converted);
			return converter.lastenausgleichTagesschuleAngabenGemeindeContainerToJax(saved);
		} catch (EJBTransactionRolledbackException e) {
			if (e.getCause() instanceof IllegalArgumentException) {
				throw new WebApplicationException(Response.status(Status.BAD_REQUEST)
					.entity(e.getMessage())
					.type(MediaType.TEXT_PLAIN)
					.build());
			}
			throw new EJBTransactionRolledbackException(e.getMessage(), e);
		}
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
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
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
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
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
		value = "Erstellt ein Docx Dokument zum Lastenausgleich Tagesschulen für den übergebenen Gemeindeantrag",
		response = Void.class)
	@POST
	@Path("/docx-erstellen/{containerJaxId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public void dokumentErstellen(
		@Nonnull @NotNull @PathParam("containerJaxId") JaxId containerJaxId,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {
		Objects.requireNonNull(containerJaxId);
		Objects.requireNonNull(containerJaxId.getId());

		latsDokumentService.createDocx(containerJaxId.getId());

	}
}
