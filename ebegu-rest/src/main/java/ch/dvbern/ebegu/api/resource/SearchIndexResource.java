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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.naming.directory.SearchResult;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.dto.JaxAntragDTO;
import ch.dvbern.ebegu.dto.suchfilter.lucene.QuickSearchResultDTO;
import ch.dvbern.ebegu.dto.suchfilter.lucene.SearchEntityType;
import ch.dvbern.ebegu.dto.suchfilter.lucene.SearchFilter;
import ch.dvbern.ebegu.dto.suchfilter.lucene.SearchResultEntryDTO;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.services.Authorizer;
import ch.dvbern.ebegu.services.DossierService;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.services.GesuchstellerService;
import ch.dvbern.ebegu.services.InstitutionService;
import ch.dvbern.ebegu.services.SearchIndexService;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.ebegu.util.EnumUtil;
import ch.dvbern.ebegu.util.MonitoringUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.Validate;

@Path("search")
@Stateless
@Api(description = "Resource für Suchanfragen")
@PermitAll
public class SearchIndexResource {

	@Inject
	private SearchIndexService searchIndexService;

	@Inject
	private GesuchstellerService gesuchstellerServiceBean;

	@Inject
	private GesuchService gesuchService;

	@Inject
	private InstitutionService institutionService;

	@Inject
	private DossierService dossierService;

	@Inject
	private JaxBConverter converter;

	@Inject
	private Authorizer authorizer;

	@Inject
	private PrincipalBean principalBean;

	/**
	 * Not used at the moment
	 */
	@ApiOperation(value = "Perform a search for the searchString in the indexes determined by the filter objects",
		response = SearchResult.class)
	@POST
	@Path("/parameterized/query/{searchString}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchQuery(
		@Nonnull @PathParam("searchString") String searchStringParam,
		List<SearchFilter> filters) {
		QuickSearchResultDTO searchResult = searchIndexService.search(searchStringParam, filters);
		return Response.ok(searchResult).build();
	}

	/**
	 * Searches for the passed searchString in all lucene indizes and returns visible result objects.
	 * The search is limited to a small number of results in order to make the search as quick as possible.
	 * In case there are no results left after filtering  objects that are not visible for the current user
	 * the search is performed again with no max result limit.
	 */
	@ApiOperation(value = "Perform a search for the searchString in all indizes returning only a small number of "
		+ "results",
		response = SearchResult.class)
	@GET
	@Path("/quicksearch/{searchString}")
	public Response quicksearch(
		@Context HttpServletRequest request,
		@Nonnull @PathParam("searchString") String searchStringParam) {
		Validate.notEmpty(searchStringParam);
		QuickSearchResultDTO search = searchIndexService.quicksearch(searchStringParam, true);
		QuickSearchResultDTO quickSearchResultDTO = convertQuicksearchResultToDTO(search);
		//if no result is returened but we know that there are result we assume that the loaded results were not
		// visible
		//for the current user. In that case  try the unlimited search so the user gets his visible result
		if (quickSearchResultDTO.getResultEntities().isEmpty() && quickSearchResultDTO.getNumberOfResults() > 0) {
			QuickSearchResultDTO unlimitedSearch = searchIndexService.quicksearch(searchStringParam, false);
			quickSearchResultDTO = convertQuicksearchResultToDTO(unlimitedSearch);
		}

		return Response.ok(quickSearchResultDTO).build();
	}

	/**
	 * Searches for the passed searchString in all lucene indizes and returns all visible result objects.
	 * The search is limited to a small number of results. In case there are no results left after filtering
	 * objects that are not visible for the current user then the search is performed again with no max result limit.
	 */
	@ApiOperation(value = "Perform a search for the searchString in all indizes without a result limit",
		response = SearchResult.class)
	@GET
	@Path("/globalsearch/{searchString}")
	public Response globalsearch(
		@Context HttpServletRequest request,
		@Nonnull @PathParam("searchString") String searchStringParam) {
		Validate.notEmpty(searchStringParam);
		QuickSearchResultDTO search = searchIndexService.quicksearch(searchStringParam, false);
		QuickSearchResultDTO quickSearchResultDTO = convertQuicksearchResultToDTO(search);

		return Response.ok(quickSearchResultDTO).build();
	}

	/**
	 * Helper that reduces and normalizes the result from the Lucene search index. Duplcate Gesuche that matched in
	 * different indizes
	 * are removed as well as Gesuche that are invisible for the current user. For every Fall only the newest Gesuch
	 * that was in the
	 * original QuckSearchResultDTO will be rturned
	 */
	private QuickSearchResultDTO convertQuicksearchResultToDTO(QuickSearchResultDTO quickSearch) {
		List<Gesuch> allowedGesuche = filterUnreadableGesuche(quickSearch); //nur erlaubte Gesuche
		final QuickSearchResultDTO faelleWithMitteilungResults =
			getFaelleWithMitteilungResults(quickSearch); // muss gemacht werden bevor wir unerlaubte rausfiltern
		Map<String, Gesuch> gesucheToShow =	EbeguUtil.groupByFallAndSelectNewestAntrag(allowedGesuche); //nur neustes gesuch
		//search result anpassen so dass nur noch sichtbare Antrage drin sind und Antragdtos gesetzt sind
		QuickSearchResultDTO filteredQuickSearch = mergeAllowedGesucheWithQuickSearchResult(
			quickSearch, gesucheToShow);

		// Add all results from the list that are not yet freigegeben but have mitteilungen

		final QuickSearchResultDTO quickSearchResultDTO = QuickSearchResultDTO.reduceToSingleEntyPerAntrag
			(filteredQuickSearch); // Gesuche die in mehreren Indizes gefunden wurden auslassen so dass jedes gesuch
		// nur 1 mal drin ist

		faelleWithMitteilungResults.getResultEntities()
			.forEach(searchResultEntryDTO -> {
				String dossierId = searchResultEntryDTO.getDossierId();
				if (searchResultEntryDTO.getEntity() == SearchEntityType.DOSSIER && dossierId != null) {
					Dossier dossier = dossierService.findDossierForMandant(dossierId).orElseThrow(()
						-> new EbeguEntityNotFoundException(
						"convertQuicksearchResultToDTO",
						ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
						dossierId));
					quickSearchResultDTO.addSubResultDossier(searchResultEntryDTO, dossier);
				}
			});
		return quickSearchResultDTO;
	}

	/**
	 * Returns a new list of those results that match in the Fall-index and that already have a Mitteilung
	 * (but maybe no Gesuch or no Gesuch the current user may view). Later we add those as a Fall-Result
	 * to the searchresult but only if they dont match in another index (to avoid duplicates)
	 * Will create a list with only those results that are of type FALL and have no gesuchID. These Faelle must also
	 * have at least one Mitteilung.
	 */
	private QuickSearchResultDTO getFaelleWithMitteilungResults(QuickSearchResultDTO quickSearch) {
		return MonitoringUtil.monitor(SearchIndexResource.class, "add_faelle_withMitteilung", () -> {
			//we remeber the results that we only found in the fall index and that had a mitteilung
			QuickSearchResultDTO result = new QuickSearchResultDTO();
			for (SearchResultEntryDTO searchResult : quickSearch.getResultEntities()) {
				if (SearchEntityType.DOSSIER == searchResult.getEntity()
					&& searchResult.getGesuchID() == null
					&& searchResult.getDossierId() != null
				) {
					Dossier dossier = dossierService.findDossierForMandant(searchResult.getDossierId(), false)
						.orElseThrow(() -> new EbeguEntityNotFoundException("hasDossierAnyMitteilung",
							ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, searchResult.getDossierId()));

					if (authorizer.isReadAuthorizedDossier(dossier)
						&& dossierService.hasDossierAnyMitteilung(dossier)) {
						result.addResult(searchResult);
					}
				}
			}
			return result;
		});
	}

	private List<Gesuch> filterUnreadableGesuche(QuickSearchResultDTO search) {
		//fuer suchresultate ungleich GesuchstellerContainer kennen wir die id des gesuchs schon
		List<String> gesuchIds = search.getResultEntities().stream()
			.filter(searchResultEntryDTO -> searchResultEntryDTO.getEntity()
				!= SearchEntityType.GESUCHSTELLER_CONTAINER)
			.map(SearchResultEntryDTO::getGesuchID).collect(Collectors.toList());
		List<Gesuch> readableGesuche = gesuchService.findReadableGesuche(gesuchIds);

		//fuer die suchrestultate die im GesuchstellerIndex gematched haben muessen wir das Gesuch noch ermitteln
		List<String> gesuchstellercontainerIds = search.getResultEntities().stream()
			.filter(searchResultEntryDTO -> searchResultEntryDTO.getEntity()
				== SearchEntityType.GESUCHSTELLER_CONTAINER)
			.map(SearchResultEntryDTO::getResultId).collect(Collectors.toList());

		if (gesuchstellercontainerIds.isEmpty()) {
			return readableGesuche;
		}

		List<Gesuch> gesucheFromGesuchstellermatch =
			gesuchstellerServiceBean.findGesuchOfGesuchstellende(gesuchstellercontainerIds).stream().filter(
				gesuch -> this.authorizer.isReadAuthorized(gesuch)
			).collect(Collectors.toList());

		gesucheFromGesuchstellermatch.stream().forEach(
			gesuch -> {
				search.getResultEntities().stream().filter(
					searchResultEntryDTO -> searchResultEntryDTO.getEntity() == SearchEntityType.GESUCHSTELLER_CONTAINER
						&& ((gesuch.getGesuchsteller1() != null && searchResultEntryDTO.getResultId()
						.equals(gesuch.getGesuchsteller1().getId())) ||
						(gesuch.getGesuchsteller2() != null && searchResultEntryDTO.getResultId()
							.equals(gesuch.getGesuchsteller2().getId())))
				).forEach(searchResultEntryDTO -> searchResultEntryDTO.setGesuchID(gesuch.getId()));
			}
		);

		List<Gesuch> allGesuche = new ArrayList<>(readableGesuche);
		allGesuche.addAll(gesucheFromGesuchstellermatch);
		return allGesuche;
	}

	/**
	 * macht einen Quervergleich zwischen den beiden Collections und behaelt nur die Resultate in
	 * QuickSearchResultDTO die wir in der Gesuchmap finden. Setzt zudem das AntragDTO inds Result
	 */
	private QuickSearchResultDTO mergeAllowedGesucheWithQuickSearchResult(
		QuickSearchResultDTO quickSearch,
		Map<String, Gesuch> gesucheToShow) {
		boolean isInstOrTraegerschaft = isCurrentUserInstitutionOrTraegerschaft();
		Collection<Institution> allowedInst =
			isInstOrTraegerschaft ? institutionService.getInstitutionenReadableForCurrentBenutzer(false) : null;

		for (Iterator<SearchResultEntryDTO> iterator = quickSearch.getResultEntities().iterator();
			 iterator.hasNext(); ) {
			SearchResultEntryDTO searchEnry = iterator.next();
			Gesuch gesuch = gesucheToShow.get(searchEnry.getGesuchID());
			if (gesuch == null) {
				iterator.remove();
				quickSearch.setNumberOfResults(quickSearch.getNumberOfResults() - 1);
			} else {
				JaxAntragDTO jaxAntragDTO;
				UserRole userRole = principalBean.discoverMostPrivilegedRole();
				if (isInstOrTraegerschaft) { //fuer institutionen und traegerschaften nur erlaubte inst mitgeben
					jaxAntragDTO = this.converter.gesuchToAntragDTO(gesuch, userRole, allowedInst);
				} else {
					jaxAntragDTO = this.converter.gesuchToAntragDTO(gesuch, userRole);
				}
				searchEnry.setAntragDTO(jaxAntragDTO);
			}
		}
		return quickSearch;
	}

	private boolean isCurrentUserInstitutionOrTraegerschaft() {
		UserRole userRole = principalBean.discoverMostPrivilegedRole();
		return EnumUtil.isOneOf(userRole, UserRole.getInstitutionTraegerschaftRoles());
	}
}
