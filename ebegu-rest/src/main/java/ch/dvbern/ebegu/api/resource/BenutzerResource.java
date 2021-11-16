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

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
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
import javax.ws.rs.CookieParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.api.AuthConstants;
import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxBenutzer;
import ch.dvbern.ebegu.api.dtos.JaxBenutzerNoDetails;
import ch.dvbern.ebegu.api.dtos.JaxBenutzerSearchresultDTO;
import ch.dvbern.ebegu.api.dtos.JaxBerechtigungHistory;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.dto.suchfilter.smarttable.BenutzerTableFilterDTO;
import ch.dvbern.ebegu.dto.suchfilter.smarttable.PaginationDTO;
import ch.dvbern.ebegu.einladung.Einladung;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.services.Authorizer;
import ch.dvbern.ebegu.services.BenutzerService;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.services.MandantService;
import ch.dvbern.ebegu.services.SuperAdminService;
import ch.dvbern.ebegu.util.MonitoringUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.tuple.Pair;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_FERIENBETREUUNG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_SOZIALDIENST;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.JURIST;
import static ch.dvbern.ebegu.enums.UserRoleName.REVISOR;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_FERIENBETREUUNG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_SOZIALDIENST;
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
@DenyAll // Absichtlich keine Rolle zugelassen, erzwingt, dass es für neue Methoden definiert werden muss
public class BenutzerResource {

	@Inject
	private BenutzerService benutzerService;

	@Inject
	private GemeindeService gemeindeService;

	@Inject
	private SuperAdminService superAdminService;

	@Inject
	private JaxBConverter converter;

	@Inject
	private Authorizer authorizer;

	@Inject
	private MandantService mandantService;

	@Inject
	private PrincipalBean principalBean;

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
		ADMIN_FERIENBETREUUNG,
		ADMIN_SOZIALDIENST
	})
	public JaxBenutzer einladen(@NotNull @Valid JaxBenutzer benutzerParam) {
		Benutzer benutzer = converter.jaxBenutzerToBenutzer(benutzerParam, new Benutzer());

		return converter.benutzerToJaxBenutzer(benutzerService.einladen(Einladung.forMitarbeiter(benutzer),
				requireNonNull(principalBean.getMandant())));
	}

	@ApiOperation("Sendet einem Benutzer im Status EINGELADEN erneut den Einladungslink")
	@POST
	@Path("/erneutEinladen")
	@RolesAllowed(SUPER_ADMIN)
	public Response erneutEinladen(@NotNull @Valid JaxBenutzer benutzerParam) {
		Benutzer benutzer = benutzerService.findBenutzerByEmail(benutzerParam.getEmail()).orElseThrow(() -> new EbeguEntityNotFoundException("erneutEinladen",
			ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, benutzerParam.getEmail()));
		benutzerService.erneutEinladen(benutzer);
		return Response.ok().build();
	}

	@ApiOperation(value = "Gibt alle existierenden Benutzer mit Rolle ADMIN_BG, SACHBEARBEITER_BG, "
		+ "ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE zurueck",
		responseContainer = "List",
		response = JaxBenutzerNoDetails.class)
	@Nonnull
	@GET
	@Path("/BgOrGemeinde/all")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_TRAEGERSCHAFT, ADMIN_INSTITUTION,
		SACHBEARBEITER_INSTITUTION, SACHBEARBEITER_TRAEGERSCHAFT, JURIST, REVISOR, STEUERAMT, SACHBEARBEITER_TS, ADMIN_TS, ADMIN_MANDANT, SACHBEARBEITER_MANDANT,
		ADMIN_SOZIALDIENST, SACHBEARBEITER_SOZIALDIENST, ADMIN_FERIENBETREUUNG, SACHBEARBEITER_FERIENBETREUUNG})
	public List<JaxBenutzerNoDetails> getAllBenutzerBgOrGemeinde() {
		return benutzerService.getAllBenutzerBgOrGemeinde().stream()
			.map(converter::benutzerToJaxBenutzerNoDetails)
			.collect(Collectors.toList());
	}

	@ApiOperation(value = "Gibt alle existierenden Benutzer mit Rolle ADMIN_BG, SACHBEARBEITER_BG, "
		+ "ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE zurueck",
		responseContainer = "List",
		response = JaxBenutzerNoDetails.class)
	@Nonnull
	@GET
	@Path("/BgOrGemeinde/{gemeindeId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_TRAEGERSCHAFT, ADMIN_INSTITUTION,
		SACHBEARBEITER_INSTITUTION, SACHBEARBEITER_TRAEGERSCHAFT, JURIST, REVISOR, STEUERAMT, SACHBEARBEITER_TS, ADMIN_TS, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public List<JaxBenutzerNoDetails> getBenutzerBgOrGemeindeForGemeinde(@Nonnull @NotNull @PathParam("gemeindeId") JaxId gemeindeJAXPId) {

		Objects.requireNonNull(gemeindeJAXPId.getId());
		String gemeindeId = converter.toEntityId(gemeindeJAXPId);
		Gemeinde gemeinde = gemeindeService.findGemeinde(gemeindeId).
			orElseThrow(() -> new EbeguEntityNotFoundException("", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, gemeindeId));

		return benutzerService.getBenutzerBgOrGemeinde(gemeinde).stream()
			.map(converter::benutzerToJaxBenutzerNoDetails)
			.collect(Collectors.toList());
	}

	@ApiOperation(value = "Gibt alle existierenden Benutzer mit Rolle ADMIN_BG, SACHBEARBEITER_BG, "
		+ "ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_TS, SACHBEARBEITER_TS zurueck",
		responseContainer = "List",
		response = JaxBenutzer.class)
	@Nonnull
	@GET
	@Path("/TsBgOrGemeinde/{gemeindeId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		SACHBEARBEITER_TS, ADMIN_TS })
	public List<JaxBenutzer> getBenutzerTsBgOrGemeindeForGemeinde(@Nonnull @NotNull @PathParam("gemeindeId") JaxId gemeindeJAXPId) {

		Objects.requireNonNull(gemeindeJAXPId.getId());
		String gemeindeId = converter.toEntityId(gemeindeJAXPId);
		Gemeinde gemeinde = gemeindeService.findGemeinde(gemeindeId).
			orElseThrow(() -> new EbeguEntityNotFoundException("", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, gemeindeId));

		return benutzerService.getBenutzerTsBgOrGemeinde(gemeinde).stream()
			.map(converter::benutzerToJaxBenutzer)
			.collect(Collectors.toList());
	}

	@ApiOperation(value = "Gibt alle existierenden Benutzer mit Rolle ADMIN_BG, SACHBEARBEITER_BG, "
		+ "ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE zurueck",
		responseContainer = "List",
		response = JaxBenutzerNoDetails.class)
	@Nonnull
	@GET
	@Path("/BgTsOrGemeinde/all")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_TRAEGERSCHAFT, ADMIN_INSTITUTION,
		SACHBEARBEITER_INSTITUTION, SACHBEARBEITER_TRAEGERSCHAFT, JURIST, REVISOR, STEUERAMT, SACHBEARBEITER_TS, ADMIN_TS, ADMIN_MANDANT, SACHBEARBEITER_MANDANT,
		ADMIN_SOZIALDIENST, SACHBEARBEITER_SOZIALDIENST})
	public List<JaxBenutzerNoDetails> getAllBenutzerBgTsOrGemeinde() {
		return benutzerService.getAllBenutzerBgTsOrGemeinde().stream()
			.map(converter::benutzerToJaxBenutzerNoDetails)
			.collect(Collectors.toList());
	}


	@ApiOperation(value = "Gibt alle existierenden Benutzer mit Rolle ADMIN_BG, SACHBEARBEITER_BG, "
		+ "ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE zurueck",
		responseContainer = "List",
		response = JaxBenutzerNoDetails.class)
	@Nonnull
	@GET
	@Path("/TsOrGemeinde/all")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_TRAEGERSCHAFT, ADMIN_INSTITUTION,
		SACHBEARBEITER_INSTITUTION, SACHBEARBEITER_TRAEGERSCHAFT, JURIST, REVISOR, STEUERAMT, SACHBEARBEITER_TS, ADMIN_TS, ADMIN_MANDANT, SACHBEARBEITER_MANDANT,
		ADMIN_SOZIALDIENST, SACHBEARBEITER_SOZIALDIENST, ADMIN_FERIENBETREUUNG, SACHBEARBEITER_FERIENBETREUUNG})
	public List<JaxBenutzerNoDetails> getAllBenutzerTsOrGemeinde() {
		return benutzerService.getAllBenutzerTsOrGemeinde().stream()
			.map(converter::benutzerToJaxBenutzerNoDetails)
			.collect(Collectors.toList());
	}

	@ApiOperation(value = "Gibt alle existierenden Benutzer mit Rolle ADMIN_BG, SACHBEARBEITER_BG, "
		+ "ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE zurueck",
		responseContainer = "List",
		response = JaxBenutzerNoDetails.class)
	@Nonnull
	@GET
	@Path("/TsOrGemeinde/{gemeindeId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_TRAEGERSCHAFT, ADMIN_INSTITUTION,
		SACHBEARBEITER_INSTITUTION, SACHBEARBEITER_TRAEGERSCHAFT, JURIST, REVISOR, STEUERAMT, SACHBEARBEITER_TS, ADMIN_TS, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public List<JaxBenutzerNoDetails> getBenutzerTsOrGemeindeForGemeinde(@Nonnull @NotNull @PathParam("gemeindeId") JaxId gemeindeJAXPId) {

		Objects.requireNonNull(gemeindeJAXPId.getId());
		String gemeindeId = converter.toEntityId(gemeindeJAXPId);
		Gemeinde gemeinde = gemeindeService.findGemeinde(gemeindeId).
			orElseThrow(() -> new EbeguEntityNotFoundException("", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, gemeindeId));

		return benutzerService.getBenutzerTsOrGemeinde(gemeinde).stream()
			.map(converter::benutzerToJaxBenutzerNoDetails)
			.collect(Collectors.toList());
	}

	@ApiOperation(value = "Gibt alle existierenden Benutzer mit Rolle Gesuchsteller zurueck",
		responseContainer = "List",
		response = JaxBenutzerNoDetails.class)
	@Nonnull
	@GET
	@Path("/gesuchsteller")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(SUPER_ADMIN)
	public List<JaxBenutzerNoDetails> getGesuchsteller() {
		return benutzerService.getGesuchsteller().stream()
			.map(converter::benutzerToJaxBenutzerNoDetails)
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
		ADMIN_MANDANT, ADMIN_FERIENBETREUUNG, REVISOR, ADMIN_SOZIALDIENST })
	public JaxBenutzerSearchresultDTO searchBenutzer(
		@Nonnull @NotNull @Valid BenutzerTableFilterDTO benutzerSearch,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		return MonitoringUtil.monitor(GesuchResource.class, "searchBenutzer", () -> {
			Pair<Long, List<Benutzer>> searchResultPair = benutzerService.searchBenutzer(benutzerSearch, false);
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

	@ApiOperation(value = "Sucht den Benutzer mit dem uebergebenen Username in der Datenbank.",
		response = JaxBenutzer.class)
	@Nullable
	@GET
	@Path("/username/{username}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public JaxBenutzer findBenutzer(
		@Nonnull @NotNull @PathParam("username") String username,
		@CookieParam(AuthConstants.COOKIE_MANDANT) Cookie mandantCookie
		) {
		AtomicReference<Mandant> mandant = new AtomicReference<>(mandantService.getDefaultMandant());
		mandantService.findMandantByName(URLDecoder.decode(mandantCookie.getValue(), StandardCharsets.UTF_8)).ifPresent(mandant::set);
		Optional<Benutzer> benutzerOptional = benutzerService.findBenutzerById(username);
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
		ADMIN_MANDANT, ADMIN_FERIENBETREUUNG, ADMIN_SOZIALDIENST })
	public JaxBenutzer inactivateBenutzer(
		@Nonnull @NotNull @Valid JaxBenutzer benutzerJax,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		Benutzer benutzer = benutzerService.sperren(benutzerJax.getUsername(),
				requireNonNull(principalBean.getMandant()));
		return converter.benutzerToJaxBenutzer(benutzer);
	}

	@ApiOperation(value = "Reactivates a Benutzer in the database", response = JaxBenutzer.class)
	@Nullable
	@PUT
	@Path("/reactivate")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, ADMIN_TS, ADMIN_GEMEINDE, ADMIN_TRAEGERSCHAFT, ADMIN_INSTITUTION,
		ADMIN_MANDANT, ADMIN_FERIENBETREUUNG, ADMIN_SOZIALDIENST })
	public JaxBenutzer reactivateBenutzer(
		@Nonnull @NotNull @Valid JaxBenutzer benutzerJax,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		Benutzer benutzer = benutzerService.reaktivieren(benutzerJax.getUsername(),
				requireNonNull(principalBean.getMandant()));
		return converter.benutzerToJaxBenutzer(benutzer);
	}

	@ApiOperation(value = "Updates a Benutzer in the database", response = JaxBenutzer.class)
	@Nullable
	@PUT
	@Path("/saveBenutzerBerechtigungen")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, ADMIN_TS, ADMIN_GEMEINDE, ADMIN_TRAEGERSCHAFT, ADMIN_INSTITUTION,
		ADMIN_MANDANT, ADMIN_FERIENBETREUUNG, ADMIN_SOZIALDIENST })
	public JaxBenutzer saveBenutzerBerechtigungen(
		@Nonnull @NotNull @Valid JaxBenutzer benutzerJax,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		String username = benutzerJax.getUsername();
		Benutzer benutzer = benutzerService.findBenutzer(username, principalBean.getMandant())
			.orElseThrow(() -> new EbeguEntityNotFoundException(
				"saveBenutzerBerechtigungen",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				username));

		authorizer.checkWriteAuthorization(benutzer);


		String fallId = benutzerService.findFallIdIfBenutzerIsGesuchstellerWithoutFreigegebenemGesuch(benutzer);
			// Keine Exception: Es ist kein Gesuchsteller: Wir können immer löschen
		return saveBenutzerBerechtigungenForced(benutzer, benutzerJax, fallId);
	}

	@Nonnull
	private JaxBenutzer saveBenutzerBerechtigungenForced(@Nonnull Benutzer benutzerFromDB, @Nonnull JaxBenutzer benutzerJax, @Nullable String fallId) {
		boolean currentBerechtigungChanged = hasCurrentBerechtigungChanged(benutzerJax, benutzerFromDB);
		if (fallId != null) {
			superAdminService.removeFallIfExists(fallId);
		}

		Benutzer mergedBenutzer = benutzerService.saveBenutzerBerechtigungen(
			converter.jaxBenutzerToBenutzer(benutzerJax, benutzerFromDB),
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
		ADMIN_MANDANT, ADMIN_FERIENBETREUUNG, REVISOR, ADMIN_SOZIALDIENST })
	public List<JaxBerechtigungHistory> getBerechtigungHistoriesForBenutzer(
		@Nonnull @NotNull @PathParam("username") String username) {

		Benutzer benutzer = benutzerService.findBenutzer(username, principalBean.getMandant())
			.orElseThrow(() -> new EbeguEntityNotFoundException(
				"getBerechtigungHistoriesForBenutzer",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				"username invalid: " + username));

		return benutzerService.getBerechtigungHistoriesForBenutzer(benutzer).stream()
			.map(history -> converter.berechtigungHistoryToJax(history))
			.collect(Collectors.toList());
	}

	@ApiOperation(value = "Gibt true zurueck, wenn der uebergebenen Benutzer in irgendeiner Gemeinde als "
		+ "Defaultbenutzer gesetzt ist", response = Boolean.class)
	@GET
	@Path("/isdefaultuser/{username}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public boolean isBenutzerDefaultBenutzerOfAnyGemeinde(
		@Nonnull @NotNull @PathParam("username") String username) {

		return benutzerService.isBenutzerDefaultBenutzerOfAnyGemeinde(username);
	}

	@ApiOperation(value = "Löscht der Benutzer mit dem gegebenen Benutzername.", response = Response.class)
	@DELETE
	@Path("/delete/{username}")
	@Consumes(MediaType.WILDCARD)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT})
	public Response deleteBenutzer(
		@Nonnull @NotNull @PathParam("username") String username) {

		Benutzer eingeloggterBenutzer = benutzerService.getCurrentBenutzer()
			.orElseThrow(() -> new EbeguRuntimeException(
				"deleteBenutzer", "No User is logged in"));

		superAdminService.removeFallAndBenutzer(username, eingeloggterBenutzer);
		return Response.ok().build();
	}

	@ApiOperation(value = "Setzt die externalUUID des Benutzers mit der uebergebenen id zurueck.", response = Response.class)
	@PUT
	@Path("/reset/{username}")
	@Consumes(MediaType.WILDCARD)
	@RolesAllowed(SUPER_ADMIN)
	public Response deleteExternalUuidForBenutzer(
		@Nonnull @NotNull @PathParam("username") String username
	) {
		Benutzer benutzer = benutzerService.findBenutzer(username, principalBean.getMandant())
			.orElseThrow(() -> new EbeguEntityNotFoundException("deleteExternalUuidForBenutzer",	ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND));
		benutzerService.deleteExternalUUIDInNewTransaction(benutzer.getId());
		return Response.ok().build();
	}
}
