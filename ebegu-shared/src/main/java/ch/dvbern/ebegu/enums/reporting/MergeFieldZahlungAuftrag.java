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

public enum MergeFieldZahlungAuftrag implements MergeFieldProvider {

	generiertAmTitle(new SimpleMergeField<>("generiertAmTitle", STRING_CONVERTER)),
	faelligAmTitle(new SimpleMergeField<>("faelligAmTitle", STRING_CONVERTER)),
	institutionTitle(new SimpleMergeField<>("institutionTitle", STRING_CONVERTER)),
	nachnameTitle(new SimpleMergeField<>("nachnameTitle", STRING_CONVERTER)),
	vornameTitle(new SimpleMergeField<>("vornameTitle", STRING_CONVERTER)),
	geburtsdatumTitle(new SimpleMergeField<>("geburtsdatumTitle", STRING_CONVERTER)),
	verfuegungTitle(new SimpleMergeField<>("verfuegungTitle", STRING_CONVERTER)),
	vonTitle(new SimpleMergeField<>("vonTitle", STRING_CONVERTER)),
	bisTitle(new SimpleMergeField<>("bisTitle", STRING_CONVERTER)),
	bgPensumTitle(new SimpleMergeField<>("bgPensumTitle", STRING_CONVERTER)),
	betragCHFTitle(new SimpleMergeField<>("betragCHFTitle", STRING_CONVERTER)),
	korrekturTitle(new SimpleMergeField<>("korrekturTitle", STRING_CONVERTER)),
	zahlungIgnorierenTitle(new SimpleMergeField<>("zahlungIgnorierenTitle", STRING_CONVERTER)),

	repeatZahlungAuftragRow(new RepeatRowMergeField("repeatZahlungAuftragRow")),

	beschrieb(new SimpleMergeField<>("beschrieb", STRING_CONVERTER)),
	generiertAm(new SimpleMergeField<>("generiertAm", DATETIME_CONVERTER)),
	faelligAm(new SimpleMergeField<>("faelligAm", DATE_CONVERTER)),

	institution(new SimpleMergeField<>("institution", STRING_CONVERTER)),
	name(new SimpleMergeField<>("name", STRING_CONVERTER)),
	vorname(new SimpleMergeField<>("vorname", STRING_CONVERTER)),
	gebDatum(new SimpleMergeField<>("gebDatum", DATE_CONVERTER)),
	verfuegung(new SimpleMergeField<>("verfuegung", STRING_CONVERTER)),
	vonDatum(new SimpleMergeField<>("vonDatum", DATE_CONVERTER)),
	bisDatum(new SimpleMergeField<>("bisDatum", DATE_CONVERTER)),
	bgPensum(new SimpleMergeField<>("bgPensum", BIGDECIMAL_CONVERTER)),
	betragCHF(new SimpleMergeField<>("betragCHF", BIGDECIMAL_CONVERTER)),
	isKorrektur(new SimpleMergeField<>("isKorrektur", BOOLEAN_X_CONVERTER)),
	isIgnoriert(new SimpleMergeField<>("isIgnoriert", BOOLEAN_X_CONVERTER));

	@Nonnull
	private final MergeField<?> mergeField;

	<V> MergeFieldZahlungAuftrag(@Nonnull MergeField<V> mergeField) {
		this.mergeField = mergeField;
	}

	@Override
	@Nonnull
	public <V> MergeField<V> getMergeField() {
		//noinspection unchecked
		return (MergeField<V>) mergeField;
	}
}
