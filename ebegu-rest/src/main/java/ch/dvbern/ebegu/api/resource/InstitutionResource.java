/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.api.resource;

import java.net.URI;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxExternalClientAssignment;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.dtos.JaxInstitution;
import ch.dvbern.ebegu.api.dtos.JaxInstitutionExternalClientAssignment;
import ch.dvbern.ebegu.api.dtos.JaxInstitutionListDTO;
import ch.dvbern.ebegu.api.dtos.JaxInstitutionStammdaten;
import ch.dvbern.ebegu.api.dtos.JaxInstitutionUpdate;
import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.einladung.Einladung;
import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.EinstellungenFerieninsel;
import ch.dvbern.ebegu.entities.EinstellungenTagesschule;
import ch.dvbern.ebegu.entities.ExternalClient;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionExternalClient;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.InstitutionStammdatenBetreuungsgutscheine;
import ch.dvbern.ebegu.entities.InstitutionStammdatenFerieninsel;
import ch.dvbern.ebegu.entities.InstitutionStammdatenTagesschule;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.InstitutionStatus;
import ch.dvbern.ebegu.enums.ModulTagesschuleTyp;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.errors.KibonLogLevel;
import ch.dvbern.ebegu.services.BenutzerService;
import ch.dvbern.ebegu.services.ExternalClientService;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.services.GesuchsperiodeService;
import ch.dvbern.ebegu.services.InstitutionService;
import ch.dvbern.ebegu.services.InstitutionStammdatenService;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import com.google.common.base.Preconditions;
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
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;
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
	private GesuchsperiodeService gesuchsperiodeService;

	@Inject
	private PrincipalBean principalBean;

	@Inject
	private JaxBConverter converter;

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
		if ((betreuungsangebot.isKita() || betreuungsangebot.isTagesfamilien()) &&
			!principalBean.isCallerInAnyOfRole(SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT)) {
			throw new IllegalStateException(
				"Nur ein Superadmin oder Mandant Benutzer kann einen neuen Kita/TFO Benutzer einladen. Dies wurde "
					+ "aber versucht durch: "
					+ principalBean.getBenutzer().getUsername());
		}

		Institution convertedInstitution = converter.institutionToNewEntity(institutionJAXP);
		Institution persistedInstitution = this.institutionService.createInstitution(convertedInstitution);

		initInstitutionStammdaten(stringDateBeguStart, betreuungsangebot, persistedInstitution, adminMail, gemeindeId);

		if (betreuungsangebot.isKita() || betreuungsangebot.isTagesfamilien()) {
			Benutzer benutzer = benutzerService.findBenutzerByEmail(adminMail)
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

			benutzerService.einladen(Einladung.forInstitution(benutzer, persistedInstitution));
		}

		URI uri = uriInfo.getBaseUriBuilder()
			.path(InstitutionResource.class)
			.path('/' + persistedInstitution.getId())
			.build();

		JaxInstitution jaxInstitution = converter.institutionToJAX(persistedInstitution);
		return Response.created(uri).entity(jaxInstitution).build();
	}

	private void initInstitutionStammdaten(
		@Nonnull String stringDateStartDate,
		@Nonnull BetreuungsangebotTyp betreuungsangebot,
		@Nonnull Institution persistedInstitution,
		@Nonnull String adminMail,
		@Nullable String gemeindeId
	) {
		InstitutionStammdaten institutionStammdaten = new InstitutionStammdaten();
		Gemeinde gemeinde;
		switch (betreuungsangebot) {
		case KITA:
		case TAGESFAMILIEN:
			institutionStammdaten.setInstitutionStammdatenBetreuungsgutscheine(new InstitutionStammdatenBetreuungsgutscheine());
			break;
		case TAGESSCHULE:
			gemeinde = getGemeindeOrThrowException(gemeindeId);
			InstitutionStammdatenTagesschule stammdatenTS = new InstitutionStammdatenTagesschule();
			stammdatenTS.setGemeinde(gemeinde);
			Set<EinstellungenTagesschule> einstellungenTagesschuleSet =
				gesuchsperiodeService.getAllNichtAbgeschlosseneGesuchsperioden().stream().map(
					gesuchsperiode -> {
						EinstellungenTagesschule einstellungenTagesschule = new EinstellungenTagesschule();
						einstellungenTagesschule.setInstitutionStammdatenTagesschule(stammdatenTS);
						einstellungenTagesschule.setGesuchsperiode(gesuchsperiode);
						einstellungenTagesschule.setModulTagesschuleTyp(ModulTagesschuleTyp.DYNAMISCH);
						return einstellungenTagesschule;
					}
				).collect(Collectors.toSet());

			stammdatenTS.setEinstellungenTagesschule(einstellungenTagesschuleSet);

			institutionStammdaten.setInstitutionStammdatenTagesschule(stammdatenTS);
			break;

		case FERIENINSEL:
			gemeinde = getGemeindeOrThrowException(gemeindeId);
			InstitutionStammdatenFerieninsel stammdatenFI = new InstitutionStammdatenFerieninsel();
			stammdatenFI.setGemeinde(gemeinde);

			Set<EinstellungenFerieninsel> einstellungenFerieninselSet =
				gesuchsperiodeService.getAllNichtAbgeschlosseneGesuchsperioden().stream().map(
					gesuchsperiode -> {
						EinstellungenFerieninsel einstellungenFerieninsel = new EinstellungenFerieninsel();
						einstellungenFerieninsel.setInstitutionStammdatenFerieninsel(stammdatenFI);
						einstellungenFerieninsel.setGesuchsperiode(gesuchsperiode);
						return einstellungenFerieninsel;
					}
				).collect(Collectors.toSet());

			stammdatenFI.setEinstellungenFerieninsel(einstellungenFerieninselSet);

			institutionStammdaten.setInstitutionStammdatenFerieninsel(stammdatenFI);
			break;
		}

		Adresse adresse = new Adresse();
		adresse.setStrasse("");
		adresse.setPlz("");
		adresse.setOrt("");
		institutionStammdaten.setAdresse(adresse);
		institutionStammdaten.setBetreuungsangebotTyp(betreuungsangebot);
		institutionStammdaten.setInstitution(persistedInstitution);
		institutionStammdaten.setMail(adminMail);

		LocalDate startDate = LocalDate.parse(stringDateStartDate, Constants.SQL_DATE_FORMAT);
		DateRange gueltigkeit = new DateRange(startDate, Constants.END_OF_TIME);
		institutionStammdaten.setGueltigkeit(gueltigkeit);

		institutionStammdatenService.saveInstitutionStammdaten(institutionStammdaten);
	}

	@Nonnull
	private Gemeinde getGemeindeOrThrowException(@Nullable String gemeindeId) {
		if (gemeindeId == null) {
			throw new EbeguRuntimeException("initInstitutionStammdaten()", "missing gemeindeId");
		}
		Gemeinde gemeinde =
			gemeindeService.findGemeinde(gemeindeId)
				.orElseThrow(() -> new EbeguEntityNotFoundException("initInstitutionStammdaten",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, "GemeindeId invalid: " + gemeindeId));
		return gemeinde;
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

		converter.institutionStammdatenToEntity(update.getStammdaten(), stammdaten);

		Preconditions.checkArgument(
			stammdaten.getInstitution().equals(institution),
			"Stammdaten and Institution must belong together, but %s != %s",
			stammdaten.getInstitution(),
			institution);


		if (update.getInstitutionExternalClients() != null) {
			List<InstitutionExternalClient> institutionExternalClients =
				converter.institutionExternalClientListToEntity(update.getInstitutionExternalClients(), institution);
			institutionService.saveInstitutionExternalClients(institution, institutionExternalClients);
		}

		boolean institutionUpdated = converter.institutionToEntity(update, institution, stammdaten);

		if (institutionUpdated || update.getInstitutionExternalClients() != null) {
			institutionService.updateInstitution(institution);
		}

		// set the updated institution
		stammdaten.setInstitution(institution);
		//TODO Problem bei update a collection with cascade was no longer referenced...
		//maybe we have to save the externalClient separatly but then why it doesnt work like with kindContainer
		//to analyze...
		InstitutionStammdaten persistedInstData =
			institutionStammdatenService.saveInstitutionStammdaten(stammdaten);

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
	public List<JaxInstitution> getAllInstitutionen() {
		return institutionService.getAllInstitutionen().stream()
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

		return institutionInstitutionStammdatenMap.entrySet().stream().map(map -> converter.institutionListDTOToJAX(map))
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

		Collection<ExternalClient> availableClients = externalClientService.getAllForInstitution();

		List<ExternalClient> existingExternalClient = institution.getInstitutionExternalClients().stream()
			.map(InstitutionExternalClient::getExternalClient).collect(Collectors.toList());

		availableClients.removeAll(existingExternalClient);

		JaxInstitutionExternalClientAssignment jaxInstitutionExternalClientAssignment =
			new JaxInstitutionExternalClientAssignment();
		jaxInstitutionExternalClientAssignment.getAvailableClients().addAll(converter.externalClientsToJAX(availableClients));

		jaxInstitutionExternalClientAssignment.getAssignedClients().addAll(converter.institutionExternalClientsToJAX(institution.getInstitutionExternalClients()));

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
}
