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
import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.STRING_CONVERTER;

public enum MergeFieldZahlungAuftragPeriode implements MergeFieldProvider {

	auszahlungenPeriodeTitle(new SimpleMergeField<>("auszahlungenPeriodeTitle", STRING_CONVERTER)),
	parameterTitle(new SimpleMergeField<>("parameterTitle", STRING_CONVERTER)),
	periodeTitle(new SimpleMergeField<>("periodeTitle", STRING_CONVERTER)),
	institutionTitle(new SimpleMergeField<>("institutionTitle", STRING_CONVERTER)),
	auszahlungAmTitle(new SimpleMergeField<>("auszahlungAmTitle", STRING_CONVERTER)),
	betragCHFTitle(new SimpleMergeField<>("betragCHFTitle", STRING_CONVERTER)),
	gemeindeTitle(new SimpleMergeField<>("gemeindeTitle", STRING_CONVERTER)),

	repeatZahlungAuftragRow(new RepeatRowMergeField("repeatZahlungAuftragRow")),

	periode(new SimpleMergeField<>("periode", STRING_CONVERTER)),
	institution(new SimpleMergeField<>("institution", STRING_CONVERTER)),
	gemeinde(new SimpleMergeField<>("gemeinde", STRING_CONVERTER)),
	bezahltAm(new SimpleMergeField<>("bezahltAm", DATE_CONVERTER)),
	betragCHF(new SimpleMergeField<>("betragCHF", BIGDECIMAL_CONVERTER));

	@Nonnull
	private final MergeField<?> mergeField;

	<V> MergeFieldZahlungAuftragPeriode(@Nonnull MergeField<V> mergeField) {
		this.mergeField = mergeField;
	}

	@Override
	@Nonnull
	public <V> MergeField<V> getMergeField() {
		//noinspection unchecked
		return (MergeField<V>) mergeField;
	}
}
