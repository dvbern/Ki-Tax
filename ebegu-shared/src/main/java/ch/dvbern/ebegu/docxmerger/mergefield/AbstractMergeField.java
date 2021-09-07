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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.docxmerger.mergefield;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class AbstractMergeField<T> {

	private static final String STARTING_TAG = "{";
	private static final String CLOSING_TAG = "}";

	@Nonnull private String name;
	@Nullable private T value;

	public AbstractMergeField(@Nonnull String name, @Nullable T value) {
		this.name = name;
		this.value = value;
	};

	@Nonnull
	public abstract String getConvertedValue();

	@Nonnull
	public String getName() {
		return name;
	}

	public void setName(@Nonnull String name) {
		this.name = name;
	}

	@Nullable
	public T getValue() {
		return value;
	}

	public void setValue(@Nullable T value) {
		this.value = value;
	}

	public String getPlaceholder() {
		return STARTING_TAG + this.name + CLOSING_TAG;
	};
}
