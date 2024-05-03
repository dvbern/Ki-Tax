/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.inbox.handler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Processing {

	@Nonnull
	private final ProcessingState state;

	@Nullable
	private final String message;

	@Nonnull
	public static Processing success() {
		return new Processing(ProcessingState.SUCCESS, null);
	}

	@Nonnull
	public static Processing failure(@Nonnull String message) {
		return new Processing(ProcessingState.FAILURE, message);
	}

	@Nonnull
	public static Processing ignore(@Nonnull String message) {
		return new Processing(ProcessingState.IGNORE, message);
	}

	public boolean isProcessingSuccess() {
		return state == ProcessingState.SUCCESS;
	}
}
