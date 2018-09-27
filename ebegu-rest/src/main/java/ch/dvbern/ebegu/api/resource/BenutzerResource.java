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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxBenutzer;
import ch.dvbern.ebegu.api.dtos.JaxBenutzerSearchresultDTO;
import ch.dvbern.ebegu.api.dtos.JaxBerechtigungHistory;
import ch.dvbern.ebegu.dto.suchfilter.smarttable.BenutzerTableFilterDTO;
import ch.dvbern.ebegu.dto.suchfilter.smarttable.PaginationDTO;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.services.Authorizer;
import ch.dvbern.ebegu.services.BenutzerService;
import ch.dvbern.ebegu.util.MonitoringUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.tuple.Pair;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.JURIST;
import static ch.dvbern.ebegu.enums.UserRoleName.REVISOR;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.STEUERAMT;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;
import static java.util.Objects.requireNonNull;

/**
 * REST Resource fuer Benutzer  (Auf client userRS.rest.ts also eigentlich die UserResources)
 */
@Path("benutzer")
@Stateless
@Api(description = "Resource für die Verwaltung der Benutzer (User)")
public class BenutzerResource {

	@Inject
	private BenutzerService benutzerService;

	@Inject
	private JaxBConverter converter;

	@Inject
	private Authorizer authorizer;

	@ApiOperation("Erstellt einen Benutzer mit Status EINGELADEN und sendet ihm eine E-Mail")
	@POST
	@Path("/einladen")
	@RolesAllowed({
		SUPER_ADMIN,
		ADMIN_BG,
		ADMIN_GEMEINDE,
		ADMIN_TS,
		ADMIN_MANDANT,
		ADMIN_INSTITUTION,
		ADMIN_TRAEGERSCHAFT,
	})
	public JaxBenutzer einladen(@NotNull @Valid JaxBenutzer benutzerParam) {
		Benutzer benutzer = converter.jaxBenutzerToBenutzer(benutzerParam, new Benutzer());

		return converter.benutzerToJaxBenutzer(benutzerService.einladen(benutzer));
	}

	@ApiOperation(value = "Gibt alle existierenden Benutzer mit Rolle ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, "
		+ "SACHBEARBEITER_GEMEINDE zurueck",
		responseContainer = "List",
		response = JaxBenutzer.class)
	@Nonnull
	@GET
	@Path("/JAorAdmin")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		ADMIN_TRAEGERSCHAFT, ADMIN_INSTITUTION,
		SACHBEARBEITER_INSTITUTION, SACHBEARBEITER_TRAEGERSCHAFT, JURIST, REVISOR, STEUERAMT, SACHBEARBEITER_TS,
		ADMIN_TS, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public List<JaxBenutzer> getBenutzerJAorAdmin() {
		return benutzerService.getBenutzerBGorAdmin().stream()
			.map(converter::benutzerToJaxBenutzer)
			.collect(Collectors.toList());
	}

	@ApiOperation(value = "Gibt alle existierenden Benutzer mit Rolle ADMIN_TS oder SACHBEARBEITER_TS zurueck",
		responseContainer = "List",
		response = JaxBenutzer.class)
	@Nonnull
	@GET
	@Path("/SCHorAdmin")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		ADMIN_TRAEGERSCHAFT, ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION, SACHBEARBEITER_TRAEGERSCHAFT,
		JURIST, REVISOR, STEUERAMT, SACHBEARBEITER_TS, ADMIN_TS, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public List<JaxBenutzer> getBenutzerSCHorAdminSCH() {
		return benutzerService.getBenutzerSCHorAdminSCH().stream()
			.map(converter::benutzerToJaxBenutzer)
			.collect(Collectors.toList());
	}

	@ApiOperation(value = "Gibt alle existierenden Benutzer mit Rolle Gesuchsteller zurueck",
		responseContainer = "List",
		response = JaxBenutzer.class)
	@Nonnull
	@GET
	@Path("/gesuchsteller")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(SUPER_ADMIN)
	public List<JaxBenutzer> getGesuchsteller() {
		return benutzerService.getGesuchsteller().stream()
			.map(converter::benutzerToJaxBenutzer)
			.collect(Collectors.toList());
	}

	@ApiOperation(value = "Sucht Benutzer mit den uebergebenen Suchkriterien/Filtern.",
		response = JaxBenutzerSearchresultDTO.class)
	@Nonnull
	@POST
	@Path("/search")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, ADMIN_TS, ADMIN_GEMEINDE, ADMIN_INSTITUTION, ADMIN_TRAEGERSCHAFT,
		ADMIN_MANDANT, REVISOR })
	public JaxBenutzerSearchresultDTO searchBenutzer(
		@Nonnull @NotNull @Valid BenutzerTableFilterDTO benutzerSearch,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		return MonitoringUtil.monitor(GesuchResource.class, "searchBenutzer", () -> {
			Pair<Long, List<Benutzer>> searchResultPair = benutzerService.searchBenutzer(benutzerSearch);
			List<Benutzer> foundBenutzer = searchResultPair.getRight();

			List<JaxBenutzer> benutzerDTOList = foundBenutzer.stream()
				.map(converter::benutzerToJaxBenutzer)
				.collect(Collectors.toList());

			return buildResultDTO(benutzerSearch, searchResultPair, benutzerDTOList);
		});
	}

	@Nonnull
	private JaxBenutzerSearchresultDTO buildResultDTO(
		@Nonnull BenutzerTableFilterDTO benutzerSearch,
		Pair<Long, List<Benutzer>> searchResultPair,
		List<JaxBenutzer> benutzerDTOList) {

		JaxBenutzerSearchresultDTO resultDTO = new JaxBenutzerSearchresultDTO();
		resultDTO.setBenutzerDTOs(benutzerDTOList);
		PaginationDTO pagination = benutzerSearch.getPagination();
		requireNonNull(pagination).setTotalItemCount(searchResultPair.getLeft());
		resultDTO.setPaginationDTO(pagination);

		return resultDTO;
	}

	@ApiOperation(value = "Sucht den Benutzer mit dem uebergebenen  E-Mail in der Datenbank.",
		response = JaxBenutzer.class)
	@Nullable
	@GET
	@Path("/email/{email}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public JaxBenutzer findBenutzerByEmail(
		@Nonnull @NotNull @PathParam("email") String email) {

		requireNonNull(email);
		Optional<Benutzer> benutzerOptional = benutzerService.findBenutzerByEmail(email);
		benutzerOptional.ifPresent(benutzer -> authorizer.checkReadAuthorization(benutzer));

		return benutzerOptional
			.map(benutzer -> converter.benutzerToJaxBenutzer(benutzer))
			.orElse(null);
	}

	@ApiOperation(value = "Sucht den Benutzer mit dem uebergebenen Username in der Datenbank.",
		response = JaxBenutzer.class)
	@Nullable
	@GET
	@Path("/username/{username}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public JaxBenutzer findBenutzer(
		@Nonnull @NotNull @PathParam("username") String username) {

		Optional<Benutzer> benutzerOptional = benutzerService.findBenutzer(username);
		benutzerOptional.ifPresent(benutzer -> authorizer.checkReadAuthorization(benutzer));

		return benutzerOptional.map(converter::benutzerToJaxBenutzer)
			.orElse(null);
	}

	@ApiOperation(value = "Inactivates a Benutzer in the database", response = JaxBenutzer.class)
	@Nullable
	@PUT
	@Path("/inactivate")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, ADMIN_TS, ADMIN_GEMEINDE, ADMIN_TRAEGERSCHAFT, ADMIN_INSTITUTION,
		ADMIN_MANDANT })
	public JaxBenutzer inactivateBenutzer(
		@Nonnull @NotNull @Valid JaxBenutzer benutzerJax,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		Benutzer benutzer = benutzerService.sperren(benutzerJax.getUsername());
		return converter.benutzerToJaxBenutzer(benutzer);
	}

	@ApiOperation(value = "Reactivates a Benutzer in the database", response = JaxBenutzer.class)
	@Nullable
	@PUT
	@Path("/reactivate")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, ADMIN_TS, ADMIN_GEMEINDE, ADMIN_TRAEGERSCHAFT, ADMIN_INSTITUTION,
		ADMIN_MANDANT })
	public JaxBenutzer reactivateBenutzer(
		@Nonnull @NotNull @Valid JaxBenutzer benutzerJax,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		Benutzer benutzer = benutzerService.reaktivieren(benutzerJax.getUsername());
		return converter.benutzerToJaxBenutzer(benutzer);
	}

	@ApiOperation(value = "Updates a Benutzer in the database", response = JaxBenutzer.class)
	@Nullable
	@PUT
	@Path("/saveBenutzerBerechtigungen")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, ADMIN_TS, ADMIN_GEMEINDE, ADMIN_TRAEGERSCHAFT, ADMIN_INSTITUTION,
		ADMIN_MANDANT })
	public JaxBenutzer saveBenutzerBerechtigungen(
		@Nonnull @NotNull @Valid JaxBenutzer benutzerJax,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		String username = benutzerJax.getUsername();
		Benutzer benutzer = benutzerService.findBenutzer(username)
			.orElseThrow(() -> new EbeguEntityNotFoundException(
				"saveBenutzerBerechtigungen",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				username));

		boolean currentBerechtigungChanged = hasCurrentBerechtigungChanged(benutzerJax, benutzer);
		Benutzer mergedBenutzer = benutzerService.saveBenutzerBerechtigungen(
			converter.jaxBenutzerToBenutzer(benutzerJax, benutzer),
			currentBerechtigungChanged);

		return converter.benutzerToJaxBenutzer(mergedBenutzer);
	}

	private boolean hasCurrentBerechtigungChanged(
		@Nonnull JaxBenutzer jaxBenutzerNew,
		@Nonnull Benutzer benutzerOld) {

		JaxBenutzer jaxBenutzerOld = converter.benutzerToJaxBenutzer(benutzerOld);
		jaxBenutzerOld.evaluateCurrentBerechtigung();
		jaxBenutzerNew.evaluateCurrentBerechtigung();
		requireNonNull(jaxBenutzerOld.getCurrentBerechtigung());
		requireNonNull(jaxBenutzerNew.getCurrentBerechtigung());

		return !jaxBenutzerOld.getCurrentBerechtigung().isSame(jaxBenutzerNew.getCurrentBerechtigung());
	}

	@ApiOperation(value = "Gibt alle BerechtigungHistory Einträge des übergebenen Benutzers zurück",
		responseContainer = "List", response = JaxBerechtigungHistory.class)
	@Nonnull
	@GET
	@Path("/berechtigunghistory/{username}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, ADMIN_TS, ADMIN_GEMEINDE, ADMIN_TRAEGERSCHAFT, ADMIN_INSTITUTION,
		ADMIN_MANDANT, REVISOR })
	public List<JaxBerechtigungHistory> getBerechtigungHistoriesForBenutzer(
		@Nonnull @NotNull @PathParam("username") String username) {

		Benutzer benutzer = benutzerService.findBenutzer(username)
			.orElseThrow(() -> new EbeguEntityNotFoundException(
				"getBerechtigungHistoriesForBenutzer",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				"username invalid: " + username));

		return benutzerService.getBerechtigungHistoriesForBenutzer(benutzer).stream()
			.map(history -> converter.berechtigungHistoryToJax(history))
			.collect(Collectors.toList());
	}
}
