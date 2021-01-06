/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.enums.reporting;

import javax.annotation.Nonnull;

import ch.dvbern.oss.lib.excelmerger.mergefields.MergeField;
import ch.dvbern.oss.lib.excelmerger.mergefields.MergeFieldProvider;
import ch.dvbern.oss.lib.excelmerger.mergefields.RepeatRowMergeField;
import ch.dvbern.oss.lib.excelmerger.mergefields.SimpleMergeField;

import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.BIGDECIMAL_CONVERTER;
import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.BOOLEAN_X_CONVERTER;
import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.DATETIME_CONVERTER;
import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.DATE_CONVERTER;
import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.STRING_CONVERTER;

/**
 * Merger fuer Statistik fuer Institutionen
 */
public enum MergeFieldInstitutionen implements MergeFieldProvider {

	repeatInstitutionenRow(new RepeatRowMergeField("repeatInstitutionenRow")),

	reportInstitutionenTitle(new SimpleMergeField<>("reportInstitutionenTitle", STRING_CONVERTER)),

	typTitle(new SimpleMergeField<>("typTitle", STRING_CONVERTER)),
	traegerschaftTitle(new SimpleMergeField<>("traegerschaftTitle", STRING_CONVERTER)),
	nameTitle(new SimpleMergeField<>("nameTitle", STRING_CONVERTER)),
	anschriftTitle(new SimpleMergeField<>("anschriftTitle", STRING_CONVERTER)),
	strasseTitle(new SimpleMergeField<>("strasseTitle", STRING_CONVERTER)),
	plzTitle(new SimpleMergeField<>("plzTitle", STRING_CONVERTER)),
	ortTitle(new SimpleMergeField<>("ortTitle", STRING_CONVERTER)),
	telefonTitle(new SimpleMergeField<>("telefonTitle", STRING_CONVERTER)),
	emailTitle(new SimpleMergeField<>("emailTitle", STRING_CONVERTER)),
	urlTitle(new SimpleMergeField<>("urlTitle", STRING_CONVERTER)),
	gueltigAbTitle(new SimpleMergeField<>("gueltigAbTitle", STRING_CONVERTER)),
	gueltigBisTitle(new SimpleMergeField<>("gueltigBisTitle", STRING_CONVERTER)),
	oeffnungstageTitle(new SimpleMergeField<>("oeffnungstageTitle", STRING_CONVERTER)),
	oeffnungszeitenTitle(new SimpleMergeField<>("oeffnungszeitenTitle", STRING_CONVERTER)),
	oeffnungsAbweichungenTitle(new SimpleMergeField<>("oeffnungsAbweichungenTitle", STRING_CONVERTER)),
	babyTitle(new SimpleMergeField<>("babyTitle", STRING_CONVERTER)),
	vorschulkindTitle(new SimpleMergeField<>("vorschulkindTitle", STRING_CONVERTER)),
	kindergartenTitle(new SimpleMergeField<>("kindergartenTitle", STRING_CONVERTER)),
	schulkindTitle(new SimpleMergeField<>("schulkindTitle", STRING_CONVERTER)),
	subventioniertTitle(new SimpleMergeField<>("subventioniertTitle", STRING_CONVERTER)),
	kapazitaetTitle(new SimpleMergeField<>("kapazitaetTitle", STRING_CONVERTER)),
	reserviertFuerFirmenTitle(new SimpleMergeField<>("reserviertFuerFirmenTitle", STRING_CONVERTER)),
	zuletztGeaendertTitle(new SimpleMergeField<>("zuletztGeaendertTitle", STRING_CONVERTER)),

	typ(new SimpleMergeField<>("typ", STRING_CONVERTER)),
	traegerschaft(new SimpleMergeField<>("traegerschaft", STRING_CONVERTER)),
	name(new SimpleMergeField<>("name", STRING_CONVERTER)),
	anschrift(new SimpleMergeField<>("anschrift", STRING_CONVERTER)),
	strasse(new SimpleMergeField<>("strasse", STRING_CONVERTER)),
	plz(new SimpleMergeField<>("plz", STRING_CONVERTER)),
	ort(new SimpleMergeField<>("ort", STRING_CONVERTER)),
	telefon(new SimpleMergeField<>("telefon", STRING_CONVERTER)),
	email(new SimpleMergeField<>("email", STRING_CONVERTER)),
	url(new SimpleMergeField<>("url", STRING_CONVERTER)),
	oeffnungstage(new SimpleMergeField<>("oeffnungstage", STRING_CONVERTER)),
	gueltigAb(new SimpleMergeField<>("gueltigAb", DATE_CONVERTER)),
	gueltigBis(new SimpleMergeField<>("gueltigBis", DATE_CONVERTER)),
	oeffnungszeiten(new SimpleMergeField<>("oeffnungszeiten", STRING_CONVERTER)),
	oeffnungsAbweichungen(new SimpleMergeField<>("oeffnungsAbweichungen", STRING_CONVERTER)),
	isBaby(new SimpleMergeField<>("isBaby", BOOLEAN_X_CONVERTER)),
	isVorschulkind(new SimpleMergeField<>("isVorschulkind", BOOLEAN_X_CONVERTER)),
	isKindergarten(new SimpleMergeField<>("isKindergarten", BOOLEAN_X_CONVERTER)),
	isSchulkind(new SimpleMergeField<>("isSchulkind", BOOLEAN_X_CONVERTER)),
	subventioniert(new SimpleMergeField<>("subventioniert", BOOLEAN_X_CONVERTER)),
	kapazitaet(new SimpleMergeField<>("kapazitaet", BIGDECIMAL_CONVERTER)),
	reserviertFuerFirmen(new SimpleMergeField<>("reserviertFuerFirmen", BIGDECIMAL_CONVERTER)),
	zuletztGeaendert(new SimpleMergeField<>("zuletztGeaendert", DATETIME_CONVERTER));

	@Nonnull
	private final MergeField<?> mergeField;

	<V> MergeFieldInstitutionen(@Nonnull MergeField<V> mergeField) {
		this.mergeField = mergeField;
	}

	@Override
	@Nonnull
	public <V> MergeField<V> getMergeField() {
		//noinspection unchecked
		return (MergeField<V>) mergeField;
	}
}
