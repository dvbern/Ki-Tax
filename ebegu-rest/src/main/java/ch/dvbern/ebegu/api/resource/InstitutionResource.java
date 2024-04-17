/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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

import ch.dvbern.ebegu.api.AuthConstants;
import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.*;
import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.einladung.Einladung;
import ch.dvbern.ebegu.entities.*;
import ch.dvbern.ebegu.enums.*;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.errors.KibonLogLevel;
import ch.dvbern.ebegu.services.*;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.GueltigkeitsUtil;
import ch.dvbern.ebegu.util.InstitutionStammdatenInitalizerVisitor;
import com.google.common.base.Preconditions;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

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
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static ch.dvbern.ebegu.enums.UserRoleName.*;
import static java.util.Objects.requireNonNull;

/**
 * REST Resource fuer Institution
 */
@Path("institutionen")
@Stateless
@Api(description = "Resource für Institutionen (Anbieter eines Betreuungsangebotes)")
@DenyAll // Absichtlich keine Rolle zugelassen, erzwingt, dass es für neue Methoden definiert werden muss
public class InstitutionResource {

	@Inject
	private InstitutionService institutionService;

	@Inject
	private InstitutionStammdatenService institutionStammdatenService;

	@Inject
	private ExternalClientService externalClientService;

	@Inject
	private BenutzerService benutzerService;

	@Inject
	private GemeindeService gemeindeService;

	@Inject
	private PrincipalBean principalBean;

	@Inject
	private JaxBConverter converter;

	@Inject
	private MandantService mandantService;

	@Inject
	private MitteilungService mitteilungService;

	@Inject
	private ApplicationPropertyService applicationPropertyService;

	@Inject
	private InstitutionStammdatenInitalizerService institutionStammdatenInitalizerService;

	@ApiOperation(value = "Creates a new Institution in the database.", response = JaxInstitution.class)
	@Nullable
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_TRAEGERSCHAFT,
		ADMIN_GEMEINDE, ADMIN_BG, ADMIN_TS, SACHBEARBEITER_GEMEINDE, SACHBEARBEITER_GEMEINDE, SACHBEARBEITER_TS })
	public Response createInstitution(
		@Nonnull @NotNull JaxInstitution institutionJAXP,
		@Nonnull @NotNull @Valid @QueryParam("date") String stringDateBeguStart,
		@Nonnull @NotNull @Valid @QueryParam("betreuung") BetreuungsangebotTyp betreuungsangebot,
		@Nonnull @NotNull @Valid @QueryParam("adminMail") String adminMail,
		@Nullable @Valid @QueryParam("gemeindeId") String gemeindeId,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		requireNonNull(adminMail);
		checkCreatInstitutionAllowed(betreuungsangebot);

		Institution convertedInstitution = converter.institutionToNewEntity(institutionJAXP);
		Institution persistedInstitution = this.institutionService.createInstitution(convertedInstitution);

		LocalDate startDate = LocalDate.parse(stringDateBeguStart, Constants.SQL_DATE_FORMAT);
		initInstitutionStammdaten(startDate, betreuungsangebot, persistedInstitution, adminMail, gemeindeId);

		Mandant mandant = requireNonNull(persistedInstitution.getMandant());

		if (BetreuungsangebotTyp.getBetreuungsgutscheinTypes().contains(betreuungsangebot)) {
			Benutzer benutzer = benutzerService.findBenutzer(adminMail, mandant)
				.map(b -> {
					if ((b.getRole() != UserRole.ADMIN_TRAEGERSCHAFT && b.getRole() != UserRole.GESUCHSTELLER) ||
						!Objects.equals(b.getTraegerschaft(), persistedInstitution.getTraegerschaft())) {
						// an existing user cannot be used to create a new Institution
						throw new EbeguRuntimeException(
							KibonLogLevel.INFO,
							"createInstitution",
							ErrorCodeEnum.EXISTING_USER_MAIL,
							adminMail);
					}

					return b;
				})
				.orElseGet(() -> benutzerService.createAdminInstitutionByEmail(adminMail, persistedInstitution));

			benutzerService.einladen(Einladung.forInstitution(benutzer, persistedInstitution, startDate), mandant);
		}

		URI uri = uriInfo.getBaseUriBuilder()
			.path(InstitutionResource.class)
			.path('/' + persistedInstitution.getId())
			.build();

		JaxInstitution jaxInstitution = converter.institutionToJAX(persistedInstitution);
		return Response.created(uri).entity(jaxInstitution).build();
	}

	private void checkCreatInstitutionAllowed(@Nonnull BetreuungsangebotTyp betreuungsangebot) {
		if (betreuungsangebot.isKita() || betreuungsangebot.isTagesfamilien()) {
			boolean institutionenDurchGemeindenEinladen = Boolean.TRUE.equals(this.applicationPropertyService.findApplicationPropertyAsBoolean(
				ApplicationPropertyKey.INSTITUTIONEN_DURCH_GEMEINDEN_EINLADEN,
				principalBean.getMandant()
			));
			// falls Einstellung deaktiviert, dass Institutionen durch Gemeinden eingeladen werden können, dürfen nur
			// SUPERADMIN und MANDANTROLLEN Institutionen einladen
			if (!institutionenDurchGemeindenEinladen && !principalBean.isCallerInAnyOfRole(SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT)) {
				throw new IllegalStateException(
					"Nur ein Superadmin oder Mandant Benutzer kann einen neuen Kita/TFO Benutzer einladen. Dies wurde "
						+ "aber versucht durch: "
						+ principalBean.getBenutzer().getUsername());
			}
		} else if (betreuungsangebot.isSchulamt() && principalBean.isCallerInAnyOfRole(UserRole.ADMIN_BG)) {
			throw new IllegalStateException(
				"Ein Admin BG kann keine Tagesschulen oder Ferieninseln erstellen.");
		}
	}

	private void initInstitutionStammdaten(
		@Nonnull LocalDate startDate,
		@Nonnull BetreuungsangebotTyp betreuungsangebot,
		@Nonnull Institution persistedInstitution,
		@Nonnull String adminMail,
		@Nullable String gemeindeId
	) {
		InstitutionStammdaten institutionStammdaten =
			new InstitutionStammdatenInitalizerVisitor(institutionStammdatenInitalizerService, gemeindeId)
				.initalizeInstiutionStammdaten(betreuungsangebot);

		Adresse adresse = new Adresse();
		adresse.setStrasse("");
		adresse.setPlz("");
		adresse.setOrt("");
		institutionStammdaten.setAdresse(adresse);
		institutionStammdaten.setBetreuungsangebotTyp(betreuungsangebot);
		institutionStammdaten.setInstitution(persistedInstitution);
		institutionStammdaten.setMail(adminMail);

		DateRange gueltigkeit = new DateRange(startDate, Constants.END_OF_TIME);
		institutionStammdaten.setGueltigkeit(gueltigkeit);

		institutionStammdatenService.saveInstitutionStammdaten(institutionStammdaten);
	}

	@ApiOperation(value = "Update a Institution and Stammdaten in the database.",
		response = JaxInstitutionStammdaten.class)
	@Nullable
	@PUT
	@Path("/{institutionId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_INSTITUTION, ADMIN_TRAEGERSCHAFT,
		ADMIN_GEMEINDE, ADMIN_BG, ADMIN_TS, SACHBEARBEITER_GEMEINDE, SACHBEARBEITER_TS })
	public JaxInstitutionStammdaten updateInstitutionAndStammdaten(
		@Nonnull @NotNull @PathParam("institutionId") JaxId institutionJAXPId,
		@Nonnull @NotNull @Valid JaxInstitutionUpdate update) {

		Institution institution = institutionService.findInstitution(requireNonNull(institutionJAXPId.getId()), true)
			.orElseThrow(() -> new EbeguEntityNotFoundException("update", institutionJAXPId.getId()));

		InstitutionStammdaten stammdaten = Optional.ofNullable(update.getStammdaten().getId())
			.flatMap(id -> institutionStammdatenService.findInstitutionStammdaten(id))
			.orElseGet(() -> new InstitutionStammdaten(institution));

		DateRange oldGueltigkeit = new DateRange(stammdaten.getGueltigkeit());

		converter.institutionStammdatenToEntity(update.getStammdaten(), stammdaten);

		Preconditions.checkArgument(
			stammdaten.getInstitution().equals(institution),
			"Stammdaten and Institution must belong together, but %s != %s",
			stammdaten.getInstitution(),
			institution);

		if (update.getInstitutionExternalClients() != null) {
			List<InstitutionExternalClient> institutionExternalClients =
				converter.institutionExternalClientListToEntity(update.getInstitutionExternalClients(), institution);
			if(checkExternalClientDateOverlapping(institutionExternalClients)){
				throw new EbeguRuntimeException("updateInstitutionAndStammdaten", ErrorCodeEnum.ERROR_INVALID_EXTERNAL_CLIENT_DATERANGE);
			}

			institutionService.saveInstitutionExternalClients(institution, institutionExternalClients);
		}

		boolean institutionUpdated = converter.institutionToEntity(update, institution, stammdaten);

		if (institutionUpdated || update.getInstitutionExternalClients() != null) {
			institutionService.updateInstitution(institution);
		}

		// set the updated institution
		stammdaten.setInstitution(institution);

		InstitutionStammdaten persistedInstData =
			institutionStammdatenService.saveInstitutionStammdaten(stammdaten);

		if (institutionStammdatenService.isGueltigkeitDecrease(oldGueltigkeit, stammdaten.getGueltigkeit())) {
			mitteilungService.adaptOffeneMutationsmitteilungenToInstiGueltigkeitChange(stammdaten.getInstitution(), stammdaten.getGueltigkeit());
		}

		institutionStammdatenService.fireStammdatenChangedEvent(persistedInstData);

		return converter.institutionStammdatenToJAX(persistedInstData);
	}

	@ApiOperation(value = "Find and return an Institution by his institution id as parameter",
		response = JaxInstitution.class)
	@Nullable
	@GET
	@Path("/{institutionId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Oeffentliche Daten
	public JaxInstitution findInstitution(
		@Nonnull @NotNull @PathParam("institutionId") JaxId institutionJAXPId) {

		requireNonNull(institutionJAXPId.getId());
		String institutionID = converter.toEntityId(institutionJAXPId);
		Optional<Institution> optional = institutionService.findInstitution(institutionID, true);

		return optional.map(institution -> converter.institutionToJAX(institution)).orElse(null);
	}

	@ApiOperation("Remove an Institution from the DB by its institution-id as parameter")
	@Nullable
	@DELETE
	@Path("/{institutionId}")
	@Consumes(MediaType.WILDCARD)
	@RolesAllowed(SUPER_ADMIN)
	public Response removeInstitution(
		@Nonnull @NotNull @PathParam("institutionId") JaxId institutionJAXPId,
		@Context HttpServletResponse response) {

		requireNonNull(institutionJAXPId.getId());
		institutionService.removeInstitution(converter.toEntityId(institutionJAXPId));
		return Response.ok().build();
	}

	@ApiOperation(value = "Find and return a list of all Institutionen",
		responseContainer = "List", response = JaxInstitution.class)
	@Nonnull
	@GET
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Oeffentliche Daten
	public List<JaxInstitution> getAllInstitutionen(
			@CookieParam(AuthConstants.COOKIE_MANDANT) Cookie mandantCookie
	) {
		var mandant = mandantService.findMandantByCookie(mandantCookie);

		return institutionService.getAllInstitutionen(mandant).stream()
			.map(inst -> converter.institutionToJAX(inst))
			.collect(Collectors.toList());
	}

	@ApiOperation(value = "Find and return a list of all BG Institutionen",
		responseContainer = "List", response = JaxInstitution.class)
	@Nonnull
	@GET
	@Path("/bg")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Oeffentliche Daten
	public List<JaxInstitution> getAllBgInstitutionen(
			@CookieParam(AuthConstants.COOKIE_MANDANT) Cookie mandantCookie
	) {
		var mandant = mandantService.findMandantByCookie(mandantCookie);

		return institutionService.getAllInstitutionenByType(mandant, BetreuungsangebotTyp.getBetreuungsgutscheinTypes()).stream()
			.map(inst -> converter.institutionToJAX(inst))
			.collect(Collectors.toList());
	}

	@ApiOperation(value = "Find and return a list of all editable Institutionen of the currently logged in Benutzer. "
		+ "Returns all for admins", responseContainer = "List", response = JaxInstitution.class)
	@Nonnull
	@GET
	@Path("/editable/currentuser")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Grundsaetzliche fuer alle Rollen: Datenabhaengig. -> Authorizer
	public List<JaxInstitution> getInstitutionenEditableForCurrentBenutzer() {
		return institutionService.getInstitutionenEditableForCurrentBenutzer(true).stream()
			.map(inst -> converter.institutionToJAX(inst))
			.collect(Collectors.toList());
	}

	@ApiOperation(value = "Find and return a list of all editable Institutionen of the currently logged in Benutzer. "
		+ "Returns all for admins", responseContainer = "List", response = JaxInstitution.class)
	@Nonnull
	@GET
	@Path("/editable/currentuser/listdto")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Grundsaetzliche fuer alle Rollen: Datenabhaengig. -> Authorizer
	public List<JaxInstitutionListDTO> getInstitutionenListDTOEditableForCurrentBenutzer() {
		Map<Institution, InstitutionStammdaten> institutionInstitutionStammdatenMap =
			institutionService.getInstitutionenInstitutionStammdatenEditableForCurrentBenutzer(true);

		return institutionInstitutionStammdatenMap.entrySet()
			.stream()
			.map(map -> converter.institutionListDTOToJAX(map))
			.collect(Collectors.toList());
	}

	@ApiOperation(value = "Find and return a list of all readable Institutionen of the currently logged in Benutzer. "
		+ "Returns all for admins", responseContainer = "List", response = JaxInstitution.class)
	@Nonnull
	@GET
	@Path("/readable/currentuser")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Grundsaetzliche fuer alle Rollen: Datenabhaengig. -> Authorizer
	public List<JaxInstitution> getInstitutionenReadableForCurrentBenutzer() {
		return institutionService.getInstitutionenReadableForCurrentBenutzer(false).stream()
			.map(inst -> converter.institutionToJAX(inst))
			.collect(Collectors.toList());
	}

	@ApiOperation(value = "Returns true, if the currently logged in Benutzer has any Institutionen in Status "
		+ "EINGELADEN", response = Boolean.class)
	@Nonnull
	@GET
	@Path("/hasEinladungen/currentuser")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Grundsaetzliche fuer alle Rollen: Datenabhaengig. -> Authorizer
	public Response hasInstitutionenInStatusEingeladenForCurrentBenutzer() {
		long anzahl = institutionService.getInstitutionenEditableForCurrentBenutzer(true).stream()
			.filter(inst -> inst.getStatus() == InstitutionStatus.EINGELADEN)
			.count();
		return Response.ok(anzahl > 0).build();
	}

	@ApiOperation(value = "Returns all still available external clients and all assigned external clients",
		response = JaxExternalClientAssignment.class)
	@Nonnull
	@GET
	@Path("/{institutionId}/externalclients")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_INSTITUTION, ADMIN_TRAEGERSCHAFT,
		ADMIN_GEMEINDE, ADMIN_BG, ADMIN_TS, SACHBEARBEITER_GEMEINDE, SACHBEARBEITER_BG, SACHBEARBEITER_TS })
	public Response getExternalClients(@Nonnull @NotNull @PathParam("institutionId") JaxId institutionJAXPId) {

		requireNonNull(institutionJAXPId.getId());
		String institutionID = converter.toEntityId(institutionJAXPId);
		Institution institution = institutionService.findInstitution(institutionID, true)
			.orElseThrow(() -> new EbeguEntityNotFoundException(
				"getExternalClients",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				institutionJAXPId.getId()));

		Collection<ExternalClient> availableClients = externalClientService.getAllForInstitution(institution);

		Collection<InstitutionExternalClient> institutionExternalClients =
			externalClientService.getInstitutionExternalClientForInstitution(institution);

		List<ExternalClient> existingExternalClient = institutionExternalClients
			.stream()
			.map(InstitutionExternalClient::getExternalClient)
			.collect(Collectors.toList());

		availableClients.removeAll(existingExternalClient);

		JaxInstitutionExternalClientAssignment jaxInstitutionExternalClientAssignment =
			new JaxInstitutionExternalClientAssignment();
		jaxInstitutionExternalClientAssignment.getAvailableClients()
			.addAll(converter.externalClientsToJAX(availableClients));

		jaxInstitutionExternalClientAssignment.getAssignedClients()
			.addAll(converter.institutionExternalClientsToJAX(institutionExternalClients));

		return Response.ok(jaxInstitutionExternalClientAssignment).build();
	}

	@ApiOperation(
		value = "Returns true, if the currently logged in Benutzer has any Institutionen which Stammdaten haven't been"
			+ " checked in the last 100 days",
		response = Boolean.class)
	@Nonnull
	@GET
	@Path("/isStammdatenCheckRequired/currentuser")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Grundsaetzliche fuer alle Rollen: Datenabhaengig. -> Authorizer
	public Response isStammdatenCheckRequiredForCurrentBenutzer() {
		long anzahl = institutionService.getInstitutionenEditableForCurrentBenutzer(true).stream()
			.filter(Institution::isStammdatenCheckRequired)
			.count();
		return Response.ok(anzahl > 0).build();
	}

	@ApiOperation(
		value = "Returns true, if the currently logged in Benutzer has any Institutionen which is Tagesschule",
		response = Boolean.class)
	@Nonnull
	@GET
	@Path("/istagesschulenutzende/currentuser")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Grundsaetzliche fuer alle Rollen: Datenabhaengig. -> Authorizer
	public Response isCurrentUserTageschuleNutzende() {
		boolean isTSNutzende = institutionService.isCurrentUserTagesschuleNutzende(false);
		return Response.ok(isTSNutzende).build();
	}

	@ApiOperation(
		value = "Returns the given institution",
		response = Boolean.class)
	@Nonnull
	@PUT
	@Path("/deactivateStammdatenCheckRequired/{institutionId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_TRAEGERSCHAFT, ADMIN_INSTITUTION, ADMIN_GEMEINDE, ADMIN_BG
		, ADMIN_TS, SACHBEARBEITER_GEMEINDE, SACHBEARBEITER_TS })
	public Response deactivateStammdatenCheckRequired(
		@Nonnull @NotNull @PathParam("institutionId") JaxId institutionJaxId
	) {
		requireNonNull(institutionJaxId.getId());
		final String institutionId = converter.toEntityId(institutionJaxId);

		institutionService.deactivateStammdatenCheckRequired(institutionId);
		return Response.ok().build();
	}

	private boolean checkExternalClientDateOverlapping(List<InstitutionExternalClient> institutionExternalClients) {
		return GueltigkeitsUtil.hasOverlapingGueltigkeit(institutionExternalClients);
	}

	@ApiOperation(value = "Find and return a list of all editable Institutionen of the currently logged in Benutzer. "
		+ "Returns all for admins", responseContainer = "List", response = JaxInstitution.class)
	@Nonnull
	@GET
	@Path("/gemeinde/listdto/{gemeindeId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Grundsaetzliche fuer alle Rollen: Datenabhaengig. -> Authorizer
	public List<JaxInstitutionListDTO> getInstitutionenForGemeinde(
		@Nonnull @NotNull @PathParam("gemeindeId") JaxId gemeindeJAXPId
	) {
		requireNonNull(gemeindeJAXPId.getId());
		String gemeindeId = converter.toEntityId(gemeindeJAXPId);
		Gemeinde gemeinde = gemeindeService.findGemeinde(gemeindeId).orElseThrow(
			() -> new EbeguEntityNotFoundException(
				"getInstitutionenForGemeinde",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				gemeindeId)
		);

		Map<Institution, InstitutionStammdaten> institutionInstitutionStammdatenMap =
			institutionService.getInstitutionenInstitutionStammdatenForGemeinde(gemeinde);

		return institutionInstitutionStammdatenMap.entrySet()
			.stream()
			.map(map -> converter.institutionListDTOToJAX(map))
			.collect(Collectors.toList());
	}

	@ApiOperation(value = "Gibt alle Institutionen zurück, die mindestens einmal in diesem Dossier verwendet wurden",
		responseContainer = "List", response = JaxInstitution.class)
	@Nullable
	@GET
	@Path("/findAllInstitutionen/{dossierId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Grundsaetzliche fuer alle Rollen: Datenabhaengig. -> Authorizer
	public List<JaxInstitution> findAllInstitutionen(@Nonnull @NotNull @PathParam("dossierId") JaxId jaxDossierId) {
		Objects.requireNonNull(jaxDossierId.getId());

		Collection<Institution> institutions = institutionService.findAllInstitutionen(jaxDossierId.getId());

		return institutions.stream()
			.distinct()
			.map(institution -> converter.institutionToJAX(institution))
			.collect(Collectors.toList());
	}

	@ApiOperation(value = "Setzt eine Institution aus dem Status NUR_LATS in die Konfiguration", response = JaxInstitution.class)
	@Nonnull
	@PUT
	@Path("{institutionId}/nurlatsUmwandeln")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({SUPER_ADMIN, ADMIN_TRAEGERSCHAFT, ADMIN_INSTITUTION, ADMIN_GEMEINDE, ADMIN_BG,
		ADMIN_TS, SACHBEARBEITER_GEMEINDE, SACHBEARBEITER_TS, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public JaxInstitution nurLatsInstitutionUmwandeln(@Nonnull @PathParam("institutionId") JaxId jaxInstitutionId) {
		Objects.requireNonNull(jaxInstitutionId.getId());

		Institution institution = institutionService.findInstitution(jaxInstitutionId.getId(), true)
				.orElseThrow(() -> {
					throw new EbeguEntityNotFoundException("nurLatsInstitutionUmwandeln", jaxInstitutionId.getId());
				});

		return converter.institutionToJAX(institutionService.nurLatsInstitutionUmwandeln(institution));
	}
}
