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
import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.DATE_CONVERTER;
import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.LONG_CONVERTER;
import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.STRING_CONVERTER;

public enum MergeFieldVerrechnungKibon implements MergeFieldProvider {

	// This template exists only in german, since its use is intern

	datumErstellt(new SimpleMergeField<>("datumErstellt", DATE_CONVERTER)),
	betragProKind(new SimpleMergeField<>("betragProKind", BIGDECIMAL_CONVERTER)),

	gemeinde(new SimpleMergeField<>("gemeinde", STRING_CONVERTER)),
	gesuchsperiode(new SimpleMergeField<>("gesuchsperiode", STRING_CONVERTER)),

	kinderKantonTotal(new SimpleMergeField<>("kinderKantonTotal", LONG_CONVERTER)),
	kinderKantonBereitsVerrechnet(new SimpleMergeField<>("kinderKantonBereitsVerrechnet", LONG_CONVERTER)),
	kinderBgTotal(new SimpleMergeField<>("kinderBgTotal", LONG_CONVERTER)),
	kinderBgBereitsVerrechnet(new SimpleMergeField<>("kinderBgBereitsVerrechnet", LONG_CONVERTER)),
	kinderTsTotal(new SimpleMergeField<>("kinderTsTotal", LONG_CONVERTER)),
	kinderTsBereitsVerrechnet(new SimpleMergeField<>("kinderTsBereitsVerrechnet", LONG_CONVERTER)),
	kinderKeinAngebotTotal(new SimpleMergeField<>("kinderKeinAngebotTotal", LONG_CONVERTER)),
	kinderKeinAngebotBereitsVerrechnet(new SimpleMergeField<>("kinderKeinAngebotBereitsVerrechnet", LONG_CONVERTER)),
	kinderGemeindeTotal(new SimpleMergeField<>("kinderGemeindeTotal", LONG_CONVERTER)),
	kinderGemeindeBereitsVerrechnet(new SimpleMergeField<>("kinderGemeindeBereitsVerrechnet", LONG_CONVERTER)),
	kinderFiTotal(new SimpleMergeField<>("kinderFiTotal", LONG_CONVERTER)),
	kinderFiBereitsVerrechnet(new SimpleMergeField<>("kinderFiBereitsVerrechnet", LONG_CONVERTER)),
	kinderTagiTotal(new SimpleMergeField<>("kinderTagiTotal", LONG_CONVERTER)),
	kinderTagiBereitsVerrechnet(new SimpleMergeField<>("kinderTagiBereitsVerrechnet", LONG_CONVERTER)),

	repeatRow(new RepeatRowMergeField("repeatRow"));


	@Nonnull
	private final MergeField<?> mergeField;

	<V> MergeFieldVerrechnungKibon(@Nonnull MergeField<V> mergeField) {
		this.mergeField = mergeField;
	}

	@Override
	@Nonnull
	public <V> MergeField<V> getMergeField() {
		//noinspection unchecked
		return (MergeField<V>) mergeField;
	}
}
