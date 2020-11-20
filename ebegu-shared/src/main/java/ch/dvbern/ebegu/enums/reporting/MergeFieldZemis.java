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

import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.BOOLEAN_CONVERTER;
import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.DATE_CONVERTER;
import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.LONG_CONVERTER;
import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.STRING_CONVERTER;

public enum MergeFieldZemis implements MergeFieldProvider {

	// This template exists only in german, since its use is intern

	jahr(new SimpleMergeField<>("berechnungsjahr", STRING_CONVERTER)),

	fall(new SimpleMergeField<>("fall", LONG_CONVERTER)),
	periode(new SimpleMergeField<>("periode", STRING_CONVERTER)),
	gemeinde(new SimpleMergeField<>("gemeinde", STRING_CONVERTER)),
	name(new SimpleMergeField<>("name", STRING_CONVERTER)),
	vorname(new SimpleMergeField<>("vorname", STRING_CONVERTER)),
	kindId(new SimpleMergeField<>("kindId", STRING_CONVERTER)),
	geburtsdatum(new SimpleMergeField<>("geburtsdatum", DATE_CONVERTER)),
	zemisNummer(new SimpleMergeField<>("zemisNummer", STRING_CONVERTER)),
	keinSelbstbehaltFuerGemeinde(new SimpleMergeField<>("keinSelbstbehaltFuerGemeinde", BOOLEAN_CONVERTER)),

	repeatRow(new RepeatRowMergeField("repeatRow"));


	@Nonnull
	private final MergeField<?> mergeField;

	<V> MergeFieldZemis(@Nonnull MergeField<V> mergeField) {
		this.mergeField = mergeField;
	}

	@Override
	@Nonnull
	public <V> MergeField<V> getMergeField() {
		//noinspection unchecked
		return (MergeField<V>) mergeField;
	}
}
