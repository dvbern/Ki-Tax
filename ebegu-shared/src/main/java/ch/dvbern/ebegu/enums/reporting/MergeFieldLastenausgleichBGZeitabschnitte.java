/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
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
import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.INTEGER_CONVERTER;
import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.LONG_CONVERTER;
import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.PERCENT_CONVERTER;
import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.STRING_CONVERTER;

public enum MergeFieldLastenausgleichBGZeitabschnitte implements MergeFieldProvider {

	repeatRow(new RepeatRowMergeField("repeatRow")),

	lastenausgleichDatenTitle(new SimpleMergeField<>("lastenausgleichDatenTitle", STRING_CONVERTER)),
	parameterTitle(new SimpleMergeField<>("parameterTitle", STRING_CONVERTER)),
	jahrTitle(new SimpleMergeField<>("jahrTitle", STRING_CONVERTER)),
	jahr(new SimpleMergeField<>("jahr", INTEGER_CONVERTER)),

	referenznummerTitle(new SimpleMergeField<>("referenznummerTitle", STRING_CONVERTER)),
	bfsNummerTitle(new SimpleMergeField<>("bfsNummerTitle", STRING_CONVERTER)),
	nameGemeindeTitle(new SimpleMergeField<>("nameGemeindeTitle", STRING_CONVERTER)),
	nachnameTitle(new SimpleMergeField<>("nachnameTitle", STRING_CONVERTER)),
	vornameTitle(new SimpleMergeField<>("vornameTitle", STRING_CONVERTER)),
	geburtsdatumTitle(new SimpleMergeField<>("geburtsdatumTitle", STRING_CONVERTER)),
	vonTitle(new SimpleMergeField<>("vonTitle", STRING_CONVERTER)),
	bisTitle(new SimpleMergeField<>("bisTitle", STRING_CONVERTER)),
	institutionTitle(new SimpleMergeField<>("institutionTitle", STRING_CONVERTER)),
	betreuungsangebotTypTitle(new SimpleMergeField<>("betreuungsangebotTypTitle", STRING_CONVERTER)),
	bgPensumTitle(new SimpleMergeField<>("bgPensumTitle", STRING_CONVERTER)),
	jaehrlichesBgPensumTitle(new SimpleMergeField<>("jaehrlichesBGPensumTitle", STRING_CONVERTER)),
	keinSelbstbehaltDurchGemeindeTitle(new SimpleMergeField<>("keinSelbstbehaltDurchGemeindeTitle", STRING_CONVERTER)),
	gutscheinTitle(new SimpleMergeField<>("gutscheinTitle", STRING_CONVERTER)),
	selbstbehaltGemeindeTitle(new SimpleMergeField<>("selbstbehaltGemeindeTitle", STRING_CONVERTER)),
	eingabeLastenausgleichTitle(new SimpleMergeField<>("eingabeLastenausgleichTitle", STRING_CONVERTER)),
	korrekturTitle(new SimpleMergeField<>("korrekturTitle", STRING_CONVERTER)),

	referenznummer(new SimpleMergeField<>("referenznummer", STRING_CONVERTER)),
	gemeinde(new SimpleMergeField<>("gemeinde", STRING_CONVERTER)),
	bfsNummer(new SimpleMergeField<>("bfsNummer", LONG_CONVERTER)),
	nameGemeinde(new SimpleMergeField<>("nameGemeinde", STRING_CONVERTER)),
	nachname(new SimpleMergeField<>("nachname", STRING_CONVERTER)),
	vorname(new SimpleMergeField<>("vorname", STRING_CONVERTER)),
	geburtsdatum(new SimpleMergeField<>("geburtsdatum", DATE_CONVERTER)),
	von(new SimpleMergeField<>("von", DATE_CONVERTER)),
	bis(new SimpleMergeField<>("bis", DATE_CONVERTER)),
	institution(new SimpleMergeField<>("institution", STRING_CONVERTER)),
	betreuungsangebotTyp(new SimpleMergeField<>("betreuungsangebotTyp", STRING_CONVERTER)),
	bgPensum(new SimpleMergeField<>("bgPensum", PERCENT_CONVERTER)),
	keinSelbstbehaltDurchGemeinde(new SimpleMergeField<>("keinSelbstbehaltDurchGemeinde", BOOLEAN_X_CONVERTER)),
	gutschein(new SimpleMergeField<>("gutschein", BIGDECIMAL_CONVERTER)),
	isKorrektur(new SimpleMergeField<>("isKorrektur", BOOLEAN_X_CONVERTER));

	@Nonnull
	private final MergeField<?> mergeField;

	<V> MergeFieldLastenausgleichBGZeitabschnitte(@Nonnull MergeField<V> mergeField) {
		this.mergeField = mergeField;
	}

	@Override
	@Nonnull
	public <V> MergeField<V> getMergeField() {
		//noinspection unchecked
		return (MergeField<V>) mergeField;
	}
}
