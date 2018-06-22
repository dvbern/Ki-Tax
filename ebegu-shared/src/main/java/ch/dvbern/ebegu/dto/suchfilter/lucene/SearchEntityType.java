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

package ch.dvbern.ebegu.dto.suchfilter.lucene;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import java.util.stream.Collectors;

import javax.annotation.Nonnull;


import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.KindContainer;

import static ch.dvbern.ebegu.dto.suchfilter.lucene.IndexedEBEGUFieldName.BETREUUNG_BGNR;
import static ch.dvbern.ebegu.dto.suchfilter.lucene.IndexedEBEGUFieldName.DOSSIER_BESITZER_NAME;
import static ch.dvbern.ebegu.dto.suchfilter.lucene.IndexedEBEGUFieldName.DOSSIER_BESITZER_VORNAME;
import static ch.dvbern.ebegu.dto.suchfilter.lucene.IndexedEBEGUFieldName.DOSSIER_FALLNUMMER;
import static ch.dvbern.ebegu.dto.suchfilter.lucene.IndexedEBEGUFieldName.GESUCH_FALL_NUMMER;
import static ch.dvbern.ebegu.dto.suchfilter.lucene.IndexedEBEGUFieldName.GS_GEBDATUM;
import static ch.dvbern.ebegu.dto.suchfilter.lucene.IndexedEBEGUFieldName.GS_NACHNAME;
import static ch.dvbern.ebegu.dto.suchfilter.lucene.IndexedEBEGUFieldName.GS_VORNAME;
import static ch.dvbern.ebegu.dto.suchfilter.lucene.IndexedEBEGUFieldName.KIND_GEBDATUM;
import static ch.dvbern.ebegu.dto.suchfilter.lucene.IndexedEBEGUFieldName.KIND_NACHNAME;
import static ch.dvbern.ebegu.dto.suchfilter.lucene.IndexedEBEGUFieldName.KIND_VORNAME;

/**
 * Enum of Entities that can be searched in the index via the SearchService. Also determines the searchable fields
 * and wether the entity will be searched in a globalSearch or not
 */
public enum SearchEntityType {
	// Reihenfolge bitte entsprechend in der Wichtigkeit im GUI
	GESUCHSTELLER_CONTAINER(GesuchstellerContainer.class, new IndexedEBEGUFieldName[] { GS_NACHNAME, GS_VORNAME, GS_GEBDATUM }),
	KIND_CONTAINER(KindContainer.class, new IndexedEBEGUFieldName[] { KIND_NACHNAME, KIND_VORNAME, KIND_GEBDATUM }),
	GESUCH(Gesuch.class, new IndexedEBEGUFieldName[] { GESUCH_FALL_NUMMER }),
	DOSSIER(Dossier.class, new IndexedEBEGUFieldName[] {DOSSIER_FALLNUMMER, DOSSIER_BESITZER_NAME, DOSSIER_BESITZER_VORNAME}),
	BETREUUNG(Betreuung.class, new IndexedEBEGUFieldName[] { BETREUUNG_BGNR });


	@Nonnull
	private final Class<? extends Searchable> entityClass;

	private final boolean globalSearch;

	@Nonnull
	private final List<String> fieldNames;

	@Nonnull
	private final IndexedEBEGUFieldName[] indexedFields;

	<T extends Searchable> SearchEntityType(@Nonnull Class<T> entityClass, @Nonnull IndexedEBEGUFieldName[] indexedFields) {
		this(entityClass, indexedFields, true);
	}

	<T extends Searchable> SearchEntityType(@Nonnull Class<T> entityClass, @Nonnull IndexedEBEGUFieldName[] indexedFields, boolean globalSearch) {
		this.entityClass = entityClass;
		this.indexedFields = indexedFields.clone();
		this.fieldNames = Collections.unmodifiableList(Arrays.stream(indexedFields).map(IndexedEBEGUFieldName::getIndexedFieldName).collect(Collectors.toList()));
		this.globalSearch = globalSearch;
	}

	@Nonnull
	public <T extends Searchable> Class<T> getEntityClass() {
		//noinspection unchecked
		return (Class<T>) entityClass;
	}

	public boolean isGlobalSearch() {
		return globalSearch;
	}

	@Nonnull
	public List<String> getFieldNames() {
		return fieldNames;
	}

	@Nonnull
	public IndexedEBEGUFieldName[] getIndexedFields() {
		return indexedFields.clone();
	}
}
