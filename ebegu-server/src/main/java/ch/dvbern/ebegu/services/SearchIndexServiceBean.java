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

package ch.dvbern.ebegu.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import ch.dvbern.ebegu.dto.suchfilter.lucene.EBEGUGermanAnalyzer;
import ch.dvbern.ebegu.dto.suchfilter.lucene.IndexedEBEGUFieldName;
import ch.dvbern.ebegu.dto.suchfilter.lucene.LuceneUtil;
import ch.dvbern.ebegu.dto.suchfilter.lucene.QuickSearchResultDTO;
import ch.dvbern.ebegu.dto.suchfilter.lucene.SearchEntityType;
import ch.dvbern.ebegu.dto.suchfilter.lucene.SearchFilter;
import ch.dvbern.ebegu.dto.suchfilter.lucene.SearchResultEntryDTO;
import ch.dvbern.ebegu.dto.suchfilter.lucene.Searchable;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.apache.commons.lang.Validate;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.Query;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.BooleanJunction;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.hibernate.search.query.dsl.QueryContextBuilder;
import org.hibernate.search.query.dsl.TermMatchingContext;
import org.hibernate.search.query.dsl.TermTermination;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMINISTRATOR_SCHULAMT;
import static ch.dvbern.ebegu.enums.UserRoleName.GESUCHSTELLER;
import static ch.dvbern.ebegu.enums.UserRoleName.JURIST;
import static ch.dvbern.ebegu.enums.UserRoleName.REVISOR;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_JA;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.SCHULAMT;
import static ch.dvbern.ebegu.enums.UserRoleName.STEUERAMT;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

@Stateless
@RolesAllowed({ SUPER_ADMIN, ADMIN })
public class SearchIndexServiceBean implements SearchIndexService {

	@Nonnull
	private static final List<SearchFilter> SEARCH_FILTER_FOR_ALL_ENTITIES =
		Arrays.stream(SearchEntityType.values())
			.filter(SearchEntityType::isGlobalSearch)
			.map(searchEntityType -> new SearchFilter(searchEntityType))
			.collect(Collectors.toList());

	@Nonnull
	private static final List<SearchFilter> SEARCH_FILTER_FOR_ALL_ENTITIES_WITH_LIMIT =
		Arrays.stream(SearchEntityType.values())
			.filter(SearchEntityType::isGlobalSearch)
			.map(searchEntityType -> new SearchFilter(searchEntityType, Constants.MAX_LUCENE_QUICKSEARCH_RESULTS))
			.collect(Collectors.toList());

	private static final String WILDCARD = "*";

	@Inject
	private Persistence persistence;

	@Override
	public void rebuildSearchIndex() {
		FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(persistence.getEntityManager());
		try {
			fullTextEntityManager.createIndexer().startAndWait();
		} catch (InterruptedException e) {
			throw new EbeguRuntimeException("rebuildSearchIndex", "Index konnte nicht erstellt werden", e, e.getMessage());
		}
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN, SACHBEARBEITER_JA, JURIST, REVISOR, SACHBEARBEITER_TRAEGERSCHAFT, SACHBEARBEITER_INSTITUTION, GESUCHSTELLER, STEUERAMT, ADMINISTRATOR_SCHULAMT, SCHULAMT })
	public QuickSearchResultDTO search(@Nonnull String searchText, @Nonnull List<SearchFilter> filters) {
		Validate.notNull(searchText, "searchText must be set");
		Validate.notNull(filters, "filters must be set");
		QuickSearchResultDTO result = new QuickSearchResultDTO();
		List<String> stringsToMatch = tokenizeAndAndAddWildcardToQuery(searchText);
		Validate.notNull(filters);
		for (SearchFilter filter : filters) {
			QuickSearchResultDTO subResult = searchInSingleIndex(stringsToMatch, filter);
			result.addSubResult(subResult);
		}
		return result;
	}

	/**
	 * Der uebergebene Searchtext wird hier mit einem Analyzer gesplittet und normalisiert. Zudem wird am Ende jedes erhaltenen
	 * Suchterms der wildcardmarker * eingefuegt.
	 * Es sollte drauf geachtet werden, dass der gleiche Analyzer verwendet wird mit dem jeweils auch der Index erzeugt wird.
	 * Wir fuehren diesen schritt manuell durch weil Hibernate-Search bei wildcard queries den analyzer NICHT anwendet
	 * vergl.doku (Wildcard queries do not apply the analyzer on the matching terms. Otherwise the risk of * or ? being mangled is too high.)
	 *
	 * @param searchText searchstring der tokenized werden soll
	 * @return Liste der normalizierten und um wildcards ergaenzten suchstrings
	 */
	private List<String> tokenizeAndAndAddWildcardToQuery(@Nonnull String searchText) {
		@SuppressWarnings("IOResourceOpenedButNotSafelyClosed")
		Analyzer analyzer = new EBEGUGermanAnalyzer();
		List<String> tokenizedStrings = LuceneUtil.tokenizeString(new EBEGUGermanAnalyzer(), searchText);
		analyzer.close();
		return tokenizedStrings.stream().map(term -> term + WILDCARD).collect(Collectors.toList());
	}

	/**
	 * sucht im durch den SearchFilter spezifizierten Index nach dem searchText. Es wird nicht laaenger als 500ms gesucht.
	 */
	@Nonnull
	private QuickSearchResultDTO searchInSingleIndex(@Nonnull List<String> searchText, @Nonnull SearchFilter filter) {
		QuickSearchResultDTO result = new QuickSearchResultDTO();
		FullTextQuery query = buildLuceneQuery(searchText, filter);
		query.limitExecutionTimeTo(Constants.MAX_LUCENE_QUERY_RUNTIME, TimeUnit.MILLISECONDS); //laufzeit limitieren
		if (filter.getMaxResults() != null) { //allenfalls anzahl resultate limitieren
			query.setMaxResults(filter.getMaxResults());
		}
		@SuppressWarnings("unchecked")
		List<Searchable> results = query.getResultList();
		List<SearchResultEntryDTO> searchResultEntryDTOS = SearchResultEntryDTO.convertSearchResult(filter, results);
		result.getResultEntities().addAll(searchResultEntryDTOS);
		result.setNumberOfResults(query.getResultSize());
		return result;
	}

	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN, SACHBEARBEITER_JA, JURIST, REVISOR, SACHBEARBEITER_TRAEGERSCHAFT, SACHBEARBEITER_INSTITUTION, GESUCHSTELLER, STEUERAMT, ADMINISTRATOR_SCHULAMT, SCHULAMT })
	public QuickSearchResultDTO quicksearch(String searchStringParam, boolean limitResult) {

		List<SearchFilter> filterToUse = limitResult ? SEARCH_FILTER_FOR_ALL_ENTITIES_WITH_LIMIT : SEARCH_FILTER_FOR_ALL_ENTITIES;
		return this.search(searchStringParam, filterToUse);
	}

	//hibernate-search dsl is not well suited for programmatic queries which is why this code is kind of unwieldy.
	@Nonnull
	private FullTextQuery buildLuceneQuery(@Nonnull List<String> searchTermList, @Nonnull SearchFilter filter) {
		Class<Searchable> entityClass = filter.getSearchEntityType().getEntityClass();
		Validate.notNull(filter.getSearchEntityType());

		EntityManager em = persistence.getEntityManager();
		FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(em);
		QueryContextBuilder queryContextBuilder = fullTextEntityManager.getSearchFactory().buildQueryBuilder();
		QueryBuilder qb = queryContextBuilder.forEntity(entityClass).get();
		//noinspection rawtypes
		BooleanJunction<? extends BooleanJunction> booleanJunction = qb.bool();
		// create a MUST (= AND) query for every search term
		for (String currSearchTerm : searchTermList) {
			Query subtermquery = createTermquery(currSearchTerm, filter, qb);
			booleanJunction = booleanJunction.must(subtermquery);
		}

		Query query = booleanJunction.createQuery();
		return fullTextEntityManager.createFullTextQuery(query, entityClass);
	}

	/**
	 * creats a 'subquery' for the given search term and returns it.
	 */
	private Query createTermquery(String currSearchTerm, SearchFilter filter, QueryBuilder qb) {
		//manche felder sollen ohne field bridge matched werden, daher hier die komplizierte aufteilung
		List<String> normalFieldsToSearch = new ArrayList<>(filter.getFieldsToSearch().length);
		List<String> fieldsIgnoringBridge = new ArrayList<>();
		for (IndexedEBEGUFieldName indexedField : filter.getFieldsToSearch()) {
			if (!indexedField.isIgnoreFieldBridgeInQuery()) {
				normalFieldsToSearch.add(indexedField.getIndexedFieldName());
			} else {
				fieldsIgnoringBridge.add(indexedField.getIndexedFieldName());  //geburtsdatum ignoriert field-bridge
			}
		}
		TermMatchingContext termCtxt = qb
			.keyword()
			.wildcard()
			.onFields(normalFieldsToSearch.toArray(new String[normalFieldsToSearch.size()]));

		for (String s : fieldsIgnoringBridge) {
			termCtxt = termCtxt.andField(s).ignoreFieldBridge();
		}
		TermTermination matching = termCtxt.matching(currSearchTerm);
		return matching.createQuery();
	}

}
