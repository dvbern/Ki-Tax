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
import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.DATE_CONVERTER;
import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.PERCENT_CONVERTER;
import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.STRING_CONVERTER;

public enum MergeFieldLastenausgleichSelbstbehalt implements MergeFieldProvider {

	// This template exists only in german, since its use is intern

	jahr(new SimpleMergeField<>("jahr", BIGDECIMAL_CONVERTER)),

	bgNummer(new SimpleMergeField<>("bgNummer", STRING_CONVERTER)),
	kindName(new SimpleMergeField<>("kindName", STRING_CONVERTER)),
	kindVorname(new SimpleMergeField<>("kindVorname", STRING_CONVERTER)),
	kindGeburtsdatum(new SimpleMergeField<>("kindGeburtsdatum", DATE_CONVERTER)),
	zeitabschnittVon(new SimpleMergeField<>("zeitabschnittVon", DATE_CONVERTER)),
	zeitabschnittBis(new SimpleMergeField<>("zeitabschnittBis", DATE_CONVERTER)),
	bgPensum(new SimpleMergeField<>("bgPensum", PERCENT_CONVERTER)),
	institution(new SimpleMergeField<>("institution", STRING_CONVERTER)),
	betreuungsTyp(new SimpleMergeField<>("betreuungsTyp", STRING_CONVERTER)),
	tarif(new SimpleMergeField<>("tarif", STRING_CONVERTER)),
	zusatz(new SimpleMergeField<>("zusatz", BOOLEAN_X_CONVERTER)),
	gutschein(new SimpleMergeField<>("gutschein", BIGDECIMAL_CONVERTER)),
	keinSelbstbehaltDurchGemeinde(new SimpleMergeField<>("keinSelbstbehaltDurchGemeinde", BOOLEAN_X_CONVERTER)),

	repeatRow(new RepeatRowMergeField("repeatRow"));


	@Nonnull
	private final MergeField<?> mergeField;

	<V> MergeFieldLastenausgleichSelbstbehalt(@Nonnull MergeField<V> mergeField) {
		this.mergeField = mergeField;
	}

	@Override
	@Nonnull
	public <V> MergeField<V> getMergeField() {
		//noinspection unchecked
		return (MergeField<V>) mergeField;
	}
}
