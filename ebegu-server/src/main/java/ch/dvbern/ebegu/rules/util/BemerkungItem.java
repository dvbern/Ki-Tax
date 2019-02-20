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

package ch.dvbern.ebegu.rules.util;

import java.util.Objects;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.types.DateRange;

public class BemerkungItem implements Comparable<BemerkungItem> {

	private final DateRange range;
	private final String message;

	public BemerkungItem(DateRange range, String message) {
		this.range = range;
		this.message = message;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof BemerkungItem)) {
			return false;
		}
		BemerkungItem that = (BemerkungItem) o;
		return Objects.equals(range, that.range) &&
			Objects.equals(message, that.message);
	}

	@Override
	public int hashCode() {
		return Objects.hash(range, message);
	}

	@Override
	public int compareTo(@Nonnull BemerkungItem otherItem) {
		return this.getRange().compareTo(otherItem.getRange());
	}

	public DateRange getRange() {
		return range;
	}

	public String getMessage() {
		return message;
	}
}
