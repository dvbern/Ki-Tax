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
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxAntragSearchresultDTO;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.dtos.JaxPendenzBetreuungen;
import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.dto.JaxAntragDTO;
import ch.dvbern.ebegu.dto.suchfilter.smarttable.AntragTableFilterDTO;
import ch.dvbern.ebegu.dto.suchfilter.smarttable.PaginationDTO;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.services.BetreuungService;
import ch.dvbern.ebegu.services.DossierService;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.services.InstitutionService;
import ch.dvbern.ebegu.services.SearchService;
import ch.dvbern.ebegu.util.MonitoringUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.tuple.Pair;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_SOZIALDIENST;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_SOZIALDIENST;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

/**
 * REST Resource fuer Pendenzen
 */
@Path("search")
@Stateless
@Api(description = "Resource für die Verwaltung der Pendenzlisten und die Fall-Suche")
@DenyAll // Absichtlich keine Rolle zugelassen, erzwingt, dass es für neue Methoden definiert werden muss
public class SearchResource {

	@Inject
	private JaxBConverter converter;

	@Inject
	private GesuchService gesuchService;

	@Inject
	private SearchService searchService;

	@Inject
	private DossierService dossierService;

	@Inject
	private BetreuungService betreuungService;

	@Inject
	private PrincipalBean principalBean;

	@Inject
	private InstitutionService institutionService;

	/**
	 * Gibt eine Liste mit allen Pendenzen des Jugendamtes zurueck.
	 * Sollte keine Pendenze gefunden werden oder ein Fehler passieren, wird eine leere Liste zurueckgegeben.
	 */
	@ApiOperation(value = "Gibt eine Liste mit allen Pendenzen des Jugendamtes zurueck",
		responseContainer = "List", response = JaxAntragDTO.class)
	@Nonnull
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/jugendamt")
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		SACHBEARBEITER_TS, ADMIN_TS, ADMIN_SOZIALDIENST, SACHBEARBEITER_SOZIALDIENST })
	public Response getAllPendenzenJA(
		@Nonnull @NotNull AntragTableFilterDTO antragSearch,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		return MonitoringUtil.monitor(SearchResource.class, "getAllPendenzenJA", () -> {
			List<Gesuch> foundAntraege = searchService.searchPendenzen(antragSearch);

			List<JaxAntragDTO> antragDTOList = convertAntraegeToDTO(foundAntraege);
			JaxAntragSearchresultDTO resultDTO = buildResultDTO(antragSearch, antragDTOList);
			return Response.ok(resultDTO).build();
		});
	}

	/**
	 * Count allen Pendenzen des Jugendamtes zurueck.
	 */
	@ApiOperation(value = "Gibt der Count von allen Pendenzen des Jugendamtes zurueck",
		 response = Long.class)
	@Nonnull
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/jugendamt/count")
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		SACHBEARBEITER_TS, ADMIN_TS, ADMIN_SOZIALDIENST, SACHBEARBEITER_SOZIALDIENST })
	public Response countAllPendenzenJA(
		@Nonnull @NotNull AntragTableFilterDTO antragSearch,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		return MonitoringUtil.monitor(SearchResource.class, "getAllPendenzenJA", () -> {
			Long count = searchService.countPendenzen(antragSearch);
			return Response.ok(count).build();
		});
	}

	@ApiOperation(value =
		"Gibt eine Liste mit allen Betreuungen die pendent sind und zur Institution oder Traegerschaft des "
			+ "eingeloggten Benutzers gehoeren zurueck. "
			+ "Fuer das Schulamt werden alle SCH-Anmeldungen zurueckgegeben",
		responseContainer = "List",
		response = JaxPendenzBetreuungen.class)
	@Nonnull
	@GET
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/pendenzenBetreuungen")
	@RolesAllowed({ SUPER_ADMIN, ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT, ADMIN_INSTITUTION,
		SACHBEARBEITER_INSTITUTION,
		ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, SACHBEARBEITER_TS, ADMIN_TS })
	public List<JaxPendenzBetreuungen> getAllPendenzenBetreuungen() {
		Collection<AbstractPlatz> betreuungenInStatus = betreuungService.getPendenzenBetreuungen();
		List<JaxPendenzBetreuungen> pendenzenList = new ArrayList<>();
		for (AbstractPlatz betreuung : betreuungenInStatus) {
			JaxPendenzBetreuungen pendenz = new JaxPendenzBetreuungen();
			pendenz.setBetreuungsNummer(betreuung.getBGNummer());
			pendenz.setGemeinde(betreuung.extractGesuch().getDossier().getGemeinde().getName());
			pendenz.setBetreuungsId(betreuung.getId());
			pendenz.setGesuchId(betreuung.extractGesuch().getId());
			pendenz.setKindId(betreuung.getKind().getId());
			pendenz.setName(betreuung.getKind().getKindJA().getNachname());
			pendenz.setVorname(betreuung.getKind().getKindJA().getVorname());
			pendenz.setGeburtsdatum(betreuung.getKind().getKindJA().getGeburtsdatum());
			pendenz.setEingangsdatum(betreuung.extractGesuch().getEingangsdatum());
			pendenz.setGesuchsperiode(converter.gesuchsperiodeToJAX(betreuung.extractGesuchsperiode()));
			pendenz.setBetreuungsangebotTyp(Objects.requireNonNull(betreuung.getBetreuungsangebotTyp()));
			pendenz.setInstitution(converter.institutionToJAX(betreuung.getInstitutionStammdaten().getInstitution()));
			if (betreuung.getBetreuungsstatus() == Betreuungsstatus.WARTEN) {
				if (betreuung.getVorgaengerId() == null) {
					pendenz.setTyp("PLATZBESTAETIGUNG");
				} else {
					//Wenn die Betreung eine VorgängerID hat ist sie mutiert
					pendenz.setTyp("PLATZBESTAETIGUNG_MUTATION");
				}
			} else {
				pendenz.setTyp(betreuung.getBetreuungsstatus().name());
			}

			pendenzenList.add(pendenz);
		}
		return pendenzenList;
	}

	@ApiOperation(value = "Gibt alle Antraege des eingegebenen Dossiers fuer den eingeloggten Gesuchsteller zurueck.",
		 response = JaxAntragDTO.class)
	@Nonnull
	@GET
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/gesuchsteller/{dossierId}")
	@PermitAll // Grundsaetzliche fuer alle Rollen: Datenabhaengig. -> Authorizer
	public List<JaxAntragDTO> getAllAntraegeOfDossier(
		@Nonnull @NotNull @PathParam("dossierId") JaxId dossierJAXPId
	) {
		Objects.requireNonNull(dossierJAXPId.getId());
		Dossier dossier = dossierService.findDossier(dossierJAXPId.getId())
			.orElseThrow(() -> new EbeguEntityNotFoundException(
				"getAllAntraegeOfDossier",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				dossierJAXPId.getId()));

		List<Gesuch> antraege = gesuchService.getAntraegeOfDossier(dossier);
		final List<JaxAntragDTO> jaxAntragDTOS = new ArrayList<>();
		final UserRole userRole = principalBean.discoverMostPrivilegedRole();

		antraege.forEach(gesuch -> {
			final JaxAntragDTO jaxAntragDTO = converter.gesuchToAntragDTO(gesuch, userRole);
			jaxAntragDTOS.add(jaxAntragDTO);
		});

		return jaxAntragDTOS;

	}

	@ApiOperation(value = "Sucht Antraege mit den uebergebenen Suchkriterien/Filtern. Es werden nur Antraege zurueck"
		+ "gegeben, fuer die der eingeloggte Benutzer berechtigt ist.", response = JaxAntragSearchresultDTO.class)
	@Nonnull
	@POST
	@Path("/search")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Grundsaetzliche fuer alle Rollen: Datenabhaengig. -> Authorizer
	public Response searchAntraege(
		@Nonnull @NotNull AntragTableFilterDTO antragSearch,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		return MonitoringUtil.monitor(GesuchResource.class, "searchAntraege", () -> {
			List<Gesuch> foundAntraege = searchService.searchAllAntraege(antragSearch);

			List<JaxAntragDTO> antragDTOList = convertAntraegeToDTO(foundAntraege);
			JaxAntragSearchresultDTO resultDTO = buildResultDTO(antragSearch, antragDTOList);
			return Response.ok(resultDTO).build();
		});
	}

	@ApiOperation(value = "Count Antraege mit den uebergebenen Suchkriterien/Filtern. Es werden nur Antraege zurueck"
		+ "gegeben, fuer die der eingeloggte Benutzer berechtigt ist.", response = Long.class)
	@Nonnull
	@POST
	@Path("/search/count")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Grundsaetzliche fuer alle Rollen: Datenabhaengig. -> Authorizer
	public Response countAntraege(
		@Nonnull @NotNull AntragTableFilterDTO antragSearch,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		return MonitoringUtil.monitor(GesuchResource.class, "searchAntraege", () -> {
			Long count = searchService.countAllAntraege(antragSearch);

			return Response.ok(count).build();
		});
	}

	@Nonnull
	private List<JaxAntragDTO> convertAntraegeToDTO(List<Gesuch> foundAntraege) {
		Collection<Institution> allowedInst = institutionService.getInstitutionenReadableForCurrentBenutzer(false);

		List<JaxAntragDTO> antragDTOList = new ArrayList<>(foundAntraege.size());
		foundAntraege.forEach(gesuch -> {
			JaxAntragDTO antragDTO =
				converter.gesuchToAntragDTO(gesuch, principalBean.discoverMostPrivilegedRole(), allowedInst);
			antragDTO.setFamilienName(gesuch.extractFamiliennamenString());
			antragDTOList.add(antragDTO);
		});
		return antragDTOList;
	}

	@Nonnull
	private JaxAntragSearchresultDTO buildResultDTO(
		@Nonnull @NotNull AntragTableFilterDTO antragSearch,
		List<JaxAntragDTO> antragDTOList) {
		JaxAntragSearchresultDTO resultDTO = new JaxAntragSearchresultDTO();
		resultDTO.setAntragDTOs(antragDTOList);
		PaginationDTO pagination = antragSearch.getPagination();
		resultDTO.setPaginationDTO(pagination);
		return resultDTO;
	}
}
