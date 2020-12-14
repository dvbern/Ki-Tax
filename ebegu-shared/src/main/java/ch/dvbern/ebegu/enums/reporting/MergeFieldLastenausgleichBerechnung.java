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
import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.PERCENT_CONVERTER;
import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.STRING_CONVERTER;

public enum MergeFieldLastenausgleichBerechnung implements MergeFieldProvider {

	// This template exists only in german, since its use is intern
	berechnungsjahr(new SimpleMergeField<>("berechnungsjahr", STRING_CONVERTER)),
	selbstbehaltProHundertProzentPlatz(new SimpleMergeField<>("selbstbehaltProHundertProzentPlatz", BIGDECIMAL_CONVERTER)),

	gemeinde(new SimpleMergeField<>("gemeinde", STRING_CONVERTER)),
	bfsNummer(new SimpleMergeField<>("bfsNummer", STRING_CONVERTER)),
	verrechnungsjahr(new SimpleMergeField<>("verrechnungsjahr", STRING_CONVERTER)),
	totalBelegung(new SimpleMergeField<>("totalBelegung", PERCENT_CONVERTER)),
	totalGutscheine(new SimpleMergeField<>("totalGutscheine", BIGDECIMAL_CONVERTER)),
	kostenProHundertProzentPlatz(new SimpleMergeField<>("kostenProHundertProzentPlatz", BIGDECIMAL_CONVERTER)),
	selbstbehaltGemeinde(new SimpleMergeField<>("selbstbehaltGemeinde", BIGDECIMAL_CONVERTER)),
	eingabeLastenausgleich(new SimpleMergeField<>("eingabeLastenausgleich", BIGDECIMAL_CONVERTER)),
	korrektur(new SimpleMergeField<>("korrektur", BOOLEAN_X_CONVERTER)),
	totalBelegungOhneSelbstbehalt(new SimpleMergeField<>("totalBelegungOhneSelbstbehalt", BIGDECIMAL_CONVERTER)),
	totalGutscheineOhneSelbstbehalt(new SimpleMergeField<>("totalGutscheineOhneSelbstbehalt", BIGDECIMAL_CONVERTER)),
	kostenFuerSelbstbehalt(new SimpleMergeField<>("kostenFuerSelbstbehalt", BIGDECIMAL_CONVERTER)),

	repeatRow(new RepeatRowMergeField("repeatRow"));

	@Nonnull
	private final MergeField<?> mergeField;

	<V> MergeFieldLastenausgleichBerechnung(@Nonnull MergeField<V> mergeField) {
		this.mergeField = mergeField;
	}

	@Override
	@Nonnull
	public <V> MergeField<V> getMergeField() {
		//noinspection unchecked
		return (MergeField<V>) mergeField;
	}
}
