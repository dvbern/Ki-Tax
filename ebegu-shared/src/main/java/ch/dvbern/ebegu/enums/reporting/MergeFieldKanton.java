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
import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.DATE_CONVERTER;
import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.PERCENT_CONVERTER;
import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.STRING_CONVERTER;

public enum MergeFieldKanton implements MergeFieldProvider {

	parameterTitle(new SimpleMergeField<>("parameterTitle", STRING_CONVERTER)),
	kantonTitle(new SimpleMergeField<>("kantonTitle", STRING_CONVERTER)),
	vonTitle(new SimpleMergeField<>("vonTitle", STRING_CONVERTER)),
	bisTitle(new SimpleMergeField<>("bisTitle", STRING_CONVERTER)),
	gemeindeTitle(new SimpleMergeField<>("gemeindeTitle", STRING_CONVERTER)),
	fallIdTitle(new SimpleMergeField<>("fallIdTitle", STRING_CONVERTER)),
	nachnameTitle(new SimpleMergeField<>("nachnameTitle", STRING_CONVERTER)),
	vornameTitle(new SimpleMergeField<>("vornameTitle", STRING_CONVERTER)),
	geburtsdatumTitle(new SimpleMergeField<>("geburtsdatumTitle", STRING_CONVERTER)),
	betreuungVonTitle(new SimpleMergeField<>("betreuungVonTitle", STRING_CONVERTER)),
	betreuungBisTitle(new SimpleMergeField<>("betreuungBisTitle", STRING_CONVERTER)),
	bgPensumKantonTitle(new SimpleMergeField<>("bgPensumKantonTitle", STRING_CONVERTER)),
	bgPensumGemeindeTitle(new SimpleMergeField<>("bgPensumGemeindeTitle", STRING_CONVERTER)),
	bgPensumTotalTitle(new SimpleMergeField<>("bgPensumTotalTitle", STRING_CONVERTER)),
	monatsanfangTitle(new SimpleMergeField<>("monatsanfangTitle", STRING_CONVERTER)),
	monatsendeTitle(new SimpleMergeField<>("monatsendeTitle", STRING_CONVERTER)),
	platzbelegungTageTitle(new SimpleMergeField<>("platzbelegungTageTitle", STRING_CONVERTER)),
	kostenCHFTitle(new SimpleMergeField<>("kostenCHFTitle", STRING_CONVERTER)),
	vollkostenTitle(new SimpleMergeField<>("vollkostenTitle", STRING_CONVERTER)),
	elternbeitragTitle(new SimpleMergeField<>("elternbeitragTitle", STRING_CONVERTER)),
	gutscheinKantonTitel(new SimpleMergeField<>("gutscheinKantonTitel", STRING_CONVERTER)),
	gutscheinGemeindeTitel(new SimpleMergeField<>("gutscheinGemeindeTitel", STRING_CONVERTER)),
	gutscheinTotalTitel(new SimpleMergeField<>("gutscheinTotalTitel", STRING_CONVERTER)),
	babyFaktorTitle(new SimpleMergeField<>("babyFaktorTitle", STRING_CONVERTER)),
	institutionTitle(new SimpleMergeField<>("institutionTitle", STRING_CONVERTER)),
	totalTitle(new SimpleMergeField<>("totalTitle", STRING_CONVERTER)),

	auswertungVon(new SimpleMergeField<>("auswertungVon", DATE_CONVERTER)),
	auswertungBis(new SimpleMergeField<>("auswertungBis", DATE_CONVERTER)),

	repeatRow(new RepeatRowMergeField("repeatRow")),

	gemeinde(new SimpleMergeField<>("gemeinde", STRING_CONVERTER)),
	bgNummer(new SimpleMergeField<>("bgNummer", STRING_CONVERTER)),
	gesuchId(new SimpleMergeField<>("gesuchId", STRING_CONVERTER)),
	name(new SimpleMergeField<>("name", STRING_CONVERTER)),
	vorname(new SimpleMergeField<>("vorname", STRING_CONVERTER)),
	geburtsdatum(new SimpleMergeField<>("geburtsdatum", DATE_CONVERTER)),
	zeitabschnittVon(new SimpleMergeField<>("zeitabschnittVon", DATE_CONVERTER)),
	zeitabschnittBis(new SimpleMergeField<>("zeitabschnittBis", DATE_CONVERTER)),
	bgPensumKanton(new SimpleMergeField<>("bgPensumKanton", PERCENT_CONVERTER)),
	bgPensumGemeinde(new SimpleMergeField<>("bgPensumGemeinde", PERCENT_CONVERTER)),
	bgPensumTotal(new SimpleMergeField<>("bgPensumTotal", PERCENT_CONVERTER)),
	elternbeitrag(new SimpleMergeField<>("elternbeitrag", BIGDECIMAL_CONVERTER)),
	verguenstigungKanton(new SimpleMergeField<>("verguenstigungKanton", BIGDECIMAL_CONVERTER)),
	verguenstigungGemeinde(new SimpleMergeField<>("verguenstigungGemeinde", BIGDECIMAL_CONVERTER)),
	verguenstigungTotal(new SimpleMergeField<>("verguenstigungTotal", BIGDECIMAL_CONVERTER)),
	institution(new SimpleMergeField<>("institution", STRING_CONVERTER)),
	betreuungsTyp(new SimpleMergeField<>("betreuungsTyp", STRING_CONVERTER));

	@Nonnull
	private final MergeField<?> mergeField;

	<V> MergeFieldKanton(@Nonnull MergeField<V> mergeField) {
		this.mergeField = mergeField;
	}

	@Override
	@Nonnull
	public <V> MergeField<V> getMergeField() {
		//noinspection unchecked
		return (MergeField<V>) mergeField;
	}
}
