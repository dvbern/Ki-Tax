/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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
import ch.dvbern.oss.lib.excelmerger.mergefields.SimpleMergeField;

import static ch.dvbern.oss.lib.excelmerger.converters.StandardConverters.STRING_CONVERTER;

public enum MergeFieldGemeinden implements MergeFieldProvider {

	bfsNummer(new SimpleMergeField<>("bfsNummer", STRING_CONVERTER));

	@Nonnull
	private final SimpleMergeField<?> mergeField;

	<V> MergeFieldGemeinden(@Nonnull SimpleMergeField<V> mergeField) {
		this.mergeField = mergeField;
	}

	@Nonnull
	@Override
	public <V> MergeField<V> getMergeField() {
		//noinspect unchecked
		return (MergeField<V>) mergeField;
	}
}
