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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.dtos.JaxInstitutionStammdaten;
import ch.dvbern.ebegu.api.dtos.JaxInstitutionStammdatenSummary;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.services.InstitutionStammdatenService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * REST Resource fuer InstitutionStammdaten
 */
@Path("institutionstammdaten")
@Stateless
@Api(description = "Resource für InstitutionsStammdaten (Daten zu einem konkreten Betreuungsangebot einer Institution)")
@PermitAll // Grundsaetzliche fuer alle Rollen (nur Lesend): Datenabhaengig. -> Authorizer
public class InstitutionStammdatenResource {

	@Inject
	private InstitutionStammdatenService institutionStammdatenService;

	@Inject
	private JaxBConverter converter;

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

	/**
	 * Sucht in der DB alle aktiven InstitutionStammdaten, deren Gueltigkeit zwischen DatumVon und DatumBis
	 * der Gesuchsperiode liegt
	 *
	 * @param gesuchsperiodeJaxId id der Gesuchsperiode fuer die Stammdaten gesucht werden sollen
	 * @return Liste mit allen InstitutionStammdaten die den Bedingungen folgen
	 */
	@ApiOperation(value = "Gibt alle Institutionsstammdaten zurueck, welche am angegebenen Datum existieren und aktiv "
		+ "sind und welche (falls TS oder FI) zur angegebenen Gemeinde gehören",
		responseContainer = "List", response = JaxInstitutionStammdaten.class)
	@Nonnull
	@GET
	@Path("/gesuchsperiode/gemeinde/active")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public List<JaxInstitutionStammdatenSummary> getAllActiveInstitutionStammdatenByGesuchsperiodeAndGemeinde(
		@Nonnull @NotNull @QueryParam("gesuchsperiodeId") JaxId gesuchsperiodeJaxId,
		@Nonnull @NotNull @QueryParam("gemeindeId") JaxId gemeindeJaxId) {

		Objects.requireNonNull(gesuchsperiodeJaxId);
		Objects.requireNonNull(gesuchsperiodeJaxId.getId());
		Objects.requireNonNull(gemeindeJaxId);
		Objects.requireNonNull(gemeindeJaxId.getId());

		String gesuchsperiodeId = converter.toEntityId(gesuchsperiodeJaxId);
		String gemeindeId = converter.toEntityId(gemeindeJaxId);

		return institutionStammdatenService.getAllActiveInstitutionStammdatenByGesuchsperiodeAndGemeinde(gesuchsperiodeId, gemeindeId).stream()
			.map(institutionStammdaten -> converter.institutionStammdatenSummaryToJAX(institutionStammdaten, new JaxInstitutionStammdatenSummary()))
			.collect(Collectors.toList());
	}

	/**
	 * Sucht in der DB alle InstitutionStammdaten, bei welchen die Institutions-id dem übergabeparameter entspricht.
	 * Falls die Institution keine Stammdaten hat gibt sie null zurück, dabei wird keine Ausnahme geworfen.
	 *
	 * @param institutionJAXPId ID der gesuchten Institution
	 * @return Die InstitutionStammdaten dieser Institution
	 */
	@ApiOperation(value = "Gibt alle Institutionsstammdaten der uebergebenen Institution zurueck, null falls keine "
		+ "vorhanden.",
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
			institutionStammdatenService.fetchInstitutionStammdatenByInstitution(institutionID, true);
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

	@ApiOperation(value = "Findet alle Tagesschulinstitutionen und Stammdaten für den momentan eingeloggten Benutzer."
		+ "Gibt alle zurück für Administratoren.", responseContainer = "List", response = JaxInstitutionStammdatenSummary.class)
	@Nonnull
	@GET
	@Path("/tagesschulen/currentuser")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public List<JaxInstitutionStammdatenSummary> getTagesschulenForCurrentBenutzer() {
		return institutionStammdatenService.getTagesschulenForCurrentBenutzer().stream()
			.map(stammdaten -> converter.institutionStammdatenSummaryToJAX(stammdaten, new JaxInstitutionStammdatenSummary()))
			.collect(Collectors.toList());
	}
}
