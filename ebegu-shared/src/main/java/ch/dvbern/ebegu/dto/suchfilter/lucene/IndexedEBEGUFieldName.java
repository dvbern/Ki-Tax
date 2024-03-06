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

import org.hibernate.search.annotations.FieldBridge;

/**
 * Enum with all the indexed fields in our Lucene Index
 */
public enum IndexedEBEGUFieldName {

	GS_VORNAME("gesuchstellerJA.nachname"),
	GS_NACHNAME("gesuchstellerJA.vorname"),
	GS_GEBDATUM("gesuchstellerJA.geburtsdatum", true),
	KIND_VORNAME("kindJA.nachname"),
	KIND_NACHNAME("kindJA.vorname"),
	KIND_GEBDATUM("kindJA.geburtsdatum", true),
	BETREUUNG_BGNR("bGNummer"),
	GESUCH_FALL_NUMMER("dossier.fall.fallNummer"),
	GESUCH_FALL_MANDANT("dossier.fall.mandant.mandantIdentifier"),
	DOSSIER_FALLNUMMER("fall.fallNummer"),
	DOSSIER_BESITZER_NAME("fall.besitzer.nachname"),
	DOSSIER_BESITZER_VORNAME("fall.besitzer.vorname"),
	DOSSIER_FALL_MANDANT("fall.mandant.mandantIdentifier");

	private final String indexedFieldName;

	/**
	 * wenn hier true gesetzt wird wird das feld beim erstellen des queries nicht ueber die {@link FieldBridge} gesucht sondern
	 * direkt als string
	 */
	private final boolean ignoreFieldBridgeInQuery;

	IndexedEBEGUFieldName(String indexedFieldName) {
		this(indexedFieldName, false);
	}

	IndexedEBEGUFieldName(String indexedFieldName, boolean ignoreFieldBridgeInQuery) {
		this.indexedFieldName = indexedFieldName;
		this.ignoreFieldBridgeInQuery = ignoreFieldBridgeInQuery;

	}

	public String getIndexedFieldName() {
		return indexedFieldName;
	}

	public boolean isIgnoreFieldBridgeInQuery() {
		return ignoreFieldBridgeInQuery;
	}

}
