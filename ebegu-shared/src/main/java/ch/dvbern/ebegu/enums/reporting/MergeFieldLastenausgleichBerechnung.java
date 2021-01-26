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

	//Titeln
	lastenausgleichTitel(new SimpleMergeField<>("lastenausgleichTitel", STRING_CONVERTER)),
	parameterTitle(new SimpleMergeField<>("parameterTitle", STRING_CONVERTER)),
	jahrTitel(new SimpleMergeField<>("jahrTitel", STRING_CONVERTER)),
	selbstbehaltProHundertProzentPlatzTitel(new SimpleMergeField<>("selbstbehaltProHundertProzentPlatzTitel", STRING_CONVERTER)),
	gemeindeTitle(new SimpleMergeField<>("gemeindeTitle", STRING_CONVERTER)),
	bfsNummerTitel(new SimpleMergeField<>("bfsNummerTitel", STRING_CONVERTER)),
	totalBelegungTitel(new SimpleMergeField<>("totalBelegungTitel", STRING_CONVERTER)),
	totalGutscheineTitel(new SimpleMergeField<>("totalGutscheineTitel", STRING_CONVERTER)),
	bgMitSelbstbehaltTitel(new SimpleMergeField<>("bgMitSelbstbehaltTitel", STRING_CONVERTER)),
	kostenProPlatzTitel(new SimpleMergeField<>("kostenProPlatzTitel", STRING_CONVERTER)),
	selbstbehaltGemeindeTitel(new SimpleMergeField<>("selbstbehaltGemeindeTitel", STRING_CONVERTER)),
	eingabeLastenausgleichTitel(new SimpleMergeField<>("eingabeLastenausgleichTitel", STRING_CONVERTER)),
	korrekturTitle(new SimpleMergeField<>("korrekturTitle", STRING_CONVERTER)),
	bgOhneSelbstbehaltTitel(new SimpleMergeField<>("bgOhneSelbstbehaltTitel", STRING_CONVERTER)),
	totalGutscheineEingabeLastTitel(new SimpleMergeField<>("totalGutscheineEingabeLastTitel", STRING_CONVERTER)),
	kostenFuerSelbstbehaltTitel(new SimpleMergeField<>("kostenFuerSelbstbehaltTitel", STRING_CONVERTER)),

	//Werten
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
	totalBelegungOhneSelbstbehalt(new SimpleMergeField<>("totalBelegungOhneSelbstbehalt", PERCENT_CONVERTER)),
	totalGutscheineOhneSelbstbehalt(new SimpleMergeField<>("totalGutscheineOhneSelbstbehalt", BIGDECIMAL_CONVERTER)),
	kostenFuerSelbstbehalt(new SimpleMergeField<>("kostenFuerSelbstbehalt", BIGDECIMAL_CONVERTER)),

	//Erläuterungen
	erlaeuterungZ1(new SimpleMergeField<>("erlaeuterungZ1", STRING_CONVERTER)),
	erlaeuterungZ2(new SimpleMergeField<>("erlaeuterungZ2", STRING_CONVERTER)),
	erlaeuterungZ3(new SimpleMergeField<>("erlaeuterungZ3", STRING_CONVERTER)),
	erlaeuterungZ4(new SimpleMergeField<>("erlaeuterungZ4", STRING_CONVERTER)),
	erlaeuterungZ5_1(new SimpleMergeField<>("erlaeuterungZ5_1", STRING_CONVERTER)),
	erlaeuterungZ6_1(new SimpleMergeField<>("erlaeuterungZ6_1", STRING_CONVERTER)),
	erlaeuterungZ7_1(new SimpleMergeField<>("erlaeuterungZ7_1", STRING_CONVERTER)),
	erlaeuterungZ8_1(new SimpleMergeField<>("erlaeuterungZ8_1", STRING_CONVERTER)),
	erlaeuterungZ9_1(new SimpleMergeField<>("erlaeuterungZ9_1", STRING_CONVERTER)),
	erlaeuterungZ10_1(new SimpleMergeField<>("erlaeuterungZ11_1", STRING_CONVERTER)),
	erlaeuterungZ11_1(new SimpleMergeField<>("erlaeuterungZ12_1", STRING_CONVERTER)),
	erlaeuterungZ5_2(new SimpleMergeField<>("erlaeuterungZ5_2", STRING_CONVERTER)),
	erlaeuterungZ6_2(new SimpleMergeField<>("erlaeuterungZ6_2", STRING_CONVERTER)),
	erlaeuterungZ7_2(new SimpleMergeField<>("erlaeuterungZ7_2", STRING_CONVERTER)),
	erlaeuterungZ8_2(new SimpleMergeField<>("erlaeuterungZ8_2", STRING_CONVERTER)),
	erlaeuterungZ9_2(new SimpleMergeField<>("erlaeuterungZ9_2", STRING_CONVERTER)),
	erlaeuterungZ10_2(new SimpleMergeField<>("erlaeuterungZ11_2", STRING_CONVERTER)),
	erlaeuterungZ11_2(new SimpleMergeField<>("erlaeuterungZ12_2", STRING_CONVERTER)),

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
