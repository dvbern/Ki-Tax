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

import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.INTEGER_CONVERTER;
import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.STRING_CONVERTER;

public enum MergeFieldGesuchStichtag implements MergeFieldProvider {

	gemeindeTitle(new SimpleMergeField<>("gemeindeTitle", STRING_CONVERTER)),
	referenzNummerTitle(new SimpleMergeField<>("bgNummerTitle", STRING_CONVERTER)),
	institutionTitle(new SimpleMergeField<>("institutionTitle", STRING_CONVERTER)),
	angebotTitle(new SimpleMergeField<>("angebotTitle", STRING_CONVERTER)),
	periodeTitle(new SimpleMergeField<>("periodeTitle", STRING_CONVERTER)),
	gesuchLaufNrTitle(new SimpleMergeField<>("gesuchLaufNrTitle", STRING_CONVERTER)),
	nichtFreigegebenTitle(new SimpleMergeField<>("nichtFreigegebenTitle", STRING_CONVERTER)),
	mahnungenTitle(new SimpleMergeField<>("mahnungenTitle", STRING_CONVERTER)),
	beschwerdeTitle(new SimpleMergeField<>("beschwerdeTitle", STRING_CONVERTER)),

	repeatGesuchStichtagRow(new RepeatRowMergeField("repeatGesuchStichtagRow")),

	gemeinde(new SimpleMergeField<>("gemeinde", STRING_CONVERTER)),
	referenzNummer(new SimpleMergeField<>("bgNummer", STRING_CONVERTER)),
	gesuchLaufNr(new SimpleMergeField<>("gesuchLaufNr", INTEGER_CONVERTER)),
	institution(new SimpleMergeField<>("institution", STRING_CONVERTER)),
	betreuungsTyp(new SimpleMergeField<>("betreuungsTyp", STRING_CONVERTER)),
	periode(new SimpleMergeField<>("periode", STRING_CONVERTER)),
	nichtFreigegeben(new SimpleMergeField<>("nichtFreigegeben", INTEGER_CONVERTER)),
	mahnungen(new SimpleMergeField<>("mahnungen", INTEGER_CONVERTER)),
	beschwerde(new SimpleMergeField<>("beschwerde", INTEGER_CONVERTER));

	@Nonnull
	private final MergeField<?> mergeField;

	<V> MergeFieldGesuchStichtag(@Nonnull MergeField<V> mergeField) {
		this.mergeField = mergeField;
	}

	@Override
	@Nonnull
	public <V> MergeField<V> getMergeField() {
		//noinspection unchecked
		return (MergeField<V>) mergeField;
	}
}
