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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.dtos.JaxInstitutionStammdaten;
import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.GemeindeStatus;
import ch.dvbern.ebegu.enums.InstitutionStatus;
import ch.dvbern.ebegu.services.InstitutionStammdatenService;
import ch.dvbern.ebegu.util.DateUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * REST Resource fuer InstitutionStammdaten
 */
@Path("institutionstammdaten")
@Stateless
@Api(description = "Resource für InstitutionsStammdaten (Daten zu einem konkreten Betreuungsangebot einer Institution)")
public class InstitutionStammdatenResource {

	@Inject
	private InstitutionStammdatenService institutionStammdatenService;

	@Inject
	private JaxBConverter converter;

	@ApiOperation(value = "Speichert ein InstitutionsStammdaten", response = JaxInstitutionStammdaten.class)
	@Nullable
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public JaxInstitutionStammdaten saveInstitutionStammdaten(
		@Nonnull @NotNull @Valid JaxInstitutionStammdaten institutionStammdatenJAXP,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		InstitutionStammdaten instDaten;
		JaxInstitutionStammdaten stammdaten = institutionStammdatenJAXP;
		if (institutionStammdatenJAXP.getId() != null) {
			Optional<InstitutionStammdaten> optional =
				institutionStammdatenService.findInstitutionStammdaten(institutionStammdatenJAXP.getId());
			instDaten = optional.orElse(new InstitutionStammdaten());
		} else {
			instDaten = new InstitutionStammdaten();
			instDaten.setAdresse(new Adresse());
		}
		if (institutionStammdatenJAXP.getInstitutionStammdatenTagesschule() != null) {
			stammdaten = converter.updateJaxModuleTagesschule(institutionStammdatenJAXP);
		}
		InstitutionStammdaten convertedInstData = converter.institutionStammdatenToEntity(stammdaten, instDaten);

		// Statuswechsel eingeladen -> aktiv
		if (convertedInstData.getInstitution().getStatus() == InstitutionStatus.EINGELADEN) {
			convertedInstData.getInstitution().setStatus(InstitutionStatus.AKTIV);
		}

		InstitutionStammdaten persistedInstData =
			institutionStammdatenService.saveInstitutionStammdaten(convertedInstData);

		return converter.institutionStammdatenToJAX(persistedInstData);

	}

	@ApiOperation(value = "Sucht die InstitutionsStammdaten mit der uebergebenen Id in der Datenbank",
		response = JaxInstitutionStammdaten.class)
	@Nullable
	@GET
	@Path("/id/{institutionStammdatenId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public JaxInstitutionStammdaten findInstitutionStammdaten(
		@Nonnull @NotNull @PathParam("institutionStammdatenId") JaxId institutionStammdatenJAXPId) {

		Objects.requireNonNull(institutionStammdatenJAXPId.getId());
		String institutionStammdatenID = converter.toEntityId(institutionStammdatenJAXPId);
		Optional<InstitutionStammdaten> optional =
			institutionStammdatenService.findInstitutionStammdaten(institutionStammdatenID);

		return optional.map(institutionStammdaten -> converter.institutionStammdatenToJAX(institutionStammdaten))
			.orElse(null);
	}

	@ApiOperation(value = "Gibt alle vorhandenen Institutionsstammdaten zurueck",
		responseContainer = "List", response = JaxInstitutionStammdaten.class)
	@Nonnull
	@GET
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public List<JaxInstitutionStammdaten> getAllInstitutionStammdaten() {
		return institutionStammdatenService.getAllInstitutionStammdaten().stream()
			.map(instStammdaten -> converter.institutionStammdatenToJAX(instStammdaten))
			.collect(Collectors.toList());
	}

	@ApiOperation("Loescht die InstitutionsStammdaten mit der uebergebenen Id aus der Datenbank")
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	@Nullable
	@DELETE
	@Path("/{institutionStammdatenId}")
	@Consumes(MediaType.WILDCARD)
	public Response removeInstitutionStammdaten(
		@Nonnull @NotNull @PathParam("institutionStammdatenId") JaxId institutionStammdatenJAXPId,
		@Context HttpServletResponse response) {

		Objects.requireNonNull(institutionStammdatenJAXPId.getId());
		institutionStammdatenService.removeInstitutionStammdaten(converter.toEntityId(institutionStammdatenJAXPId));
		return Response.ok().build();
	}

	/**
	 * Sucht in der DB alle InstitutionStammdaten, bei welchen das gegebene Datum zwischen DatumVon und DatumBis liegt
	 * Wenn das Datum null ist, wird dieses automatisch als heutiges Datum gesetzt.
	 *
	 * @param stringDate Date als String mit Format "yyyy-MM-dd". Wenn null, heutiges Datum gesetzt
	 * @return Liste mit allen InstitutionStammdaten die den Bedingungen folgen
	 */
	@ApiOperation(value = "Gibt alle Institutionsstammdaten zurueck, welche am angegebenen Datum existieren",
		responseContainer = "List", response = JaxInstitutionStammdaten.class)
	@Nonnull
	@GET
	@Path("/date")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public List<JaxInstitutionStammdaten> getAllInstitutionStammdatenByDate(
		@Nullable @QueryParam("date") String stringDate) {

		LocalDate date = DateUtil.parseStringToDateOrReturnNow(stringDate);
		return institutionStammdatenService.getAllInstitutionStammdatenByDate(date).stream()
			.map(institutionStammdaten -> converter.institutionStammdatenToJAX(institutionStammdaten))
			.collect(Collectors.toList());
	}

	/**
	 * Sucht in der DB alle aktiven InstitutionStammdaten, deren Gueltigkeit zwischen DatumVon und DatumBis
	 * der Gesuchsperiode liegt
	 *
	 * @param gesuchsperiodeId id der Gesuchsperiode fuer die Stammdaten gesucht werden sollen
	 * @return Liste mit allen InstitutionStammdaten die den Bedingungen folgen
	 */
	@ApiOperation(value = "Gibt alle Institutionsstammdaten zurueck, welche am angegebenen Datum existieren und aktiv "
		+ "sind",
		responseContainer = "List", response = JaxInstitutionStammdaten.class)
	@Nonnull
	@GET
	@Path("/gesuchsperiode/active")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public List<JaxInstitutionStammdaten> getAllActiveInstitutionStammdatenByGesuchsperiode(
		@Nonnull @NotNull @QueryParam("gesuchsperiodeId") JaxId gesuchsperiodeJaxId) {

		Objects.requireNonNull(gesuchsperiodeJaxId);
		Objects.requireNonNull(gesuchsperiodeJaxId.getId());
		String gesuchsperiodeId = converter.toEntityId(gesuchsperiodeJaxId);
		return institutionStammdatenService.getAllActiveInstitutionStammdatenByGesuchsperiode(gesuchsperiodeId).stream()
			.map(institutionStammdaten -> converter.institutionStammdatenToJAX(institutionStammdaten))
			.collect(Collectors.toList());
	}

	/**
	 * Sucht in der DB alle InstitutionStammdaten, bei welchen die Institutions-id dem übergabeparameter entspricht.
	 * Falls die Institution keine Stammdaten hat, wird die Ausnahme EbeguEntityNotFoundException geworfen.
	 *
	 * @param institutionJAXPId ID der gesuchten Institution
	 * @return Die InstitutionStammdaten dieser Institution
	 */
	@ApiOperation(value = "Gibt alle Institutionsstammdaten der uebergebenen Institution zurueck, EbeguEntityNotFoundException falls keine vorhanden.",
		response = JaxInstitutionStammdaten.class)
	@Nonnull
	@GET
	@Path("/institution/{institutionId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public JaxInstitutionStammdaten getInstitutionStammdatenByInstitution(
		@Nonnull @NotNull @PathParam("institutionId") JaxId institutionJAXPId) {

		Objects.requireNonNull(institutionJAXPId.getId());
		String institutionID = converter.toEntityId(institutionJAXPId);
		InstitutionStammdaten stammdaten =
			institutionStammdatenService.getInstitutionStammdatenByInstitution(institutionID);
		return converter.institutionStammdatenToJAX(stammdaten);
	}

	/**
	 * Sucht in der DB alle InstitutionStammdaten, bei welchen die Institutions-id dem übergabeparameter entspricht.
	 * Falls die Institution keine Stammdaten hat gibt sie null zurück, dabei wird keine Ausnahme geworfen.
	 *
	 * @param institutionJAXPId ID der gesuchten Institution
	 * @return Die InstitutionStammdaten dieser Institution
	 */
	@ApiOperation(value = "Gibt alle Institutionsstammdaten der uebergebenen Institution zurueck, null falls keine vorhanden.",
		response = JaxInstitutionStammdaten.class)
	@Nullable
	@GET
	@Path("/institutionornull/{institutionId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public JaxInstitutionStammdaten fetchInstitutionStammdatenByInstitution(
		@Nonnull @NotNull @PathParam("institutionId") JaxId institutionJAXPId) {

		Objects.requireNonNull(institutionJAXPId.getId());
		String institutionID = converter.toEntityId(institutionJAXPId);
		InstitutionStammdaten stammdaten =
			institutionStammdatenService.fetchInstitutionStammdatenByInstitution(institutionID);
		return null == stammdaten ? null : converter.institutionStammdatenToJAX(stammdaten);
	}

	/**
	 * Gibt alle BetreuungsangebotsTypen zurueck, welche die Institutionen des eingeloggten Benutzers anbieten
	 */
	@ApiOperation(value = "Gibt alle BetreuungsangebotTypen aller Institutionen zurueck, zu welchen der eingeloggte " +
		"Benutzer zugeordnet ist",
		responseContainer = "List", response = JaxInstitutionStammdaten.class)
	@SuppressWarnings("InstanceMethodNamingConvention")
	@Nonnull
	@GET
	@Path("/currentuser")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public List<BetreuungsangebotTyp> getBetreuungsangeboteForInstitutionenOfCurrentBenutzer() {
		List<BetreuungsangebotTyp> result =
			new ArrayList<>(institutionStammdatenService.getBetreuungsangeboteForInstitutionenOfCurrentBenutzer());
		return result;
	}
}
