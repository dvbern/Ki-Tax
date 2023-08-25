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

public class Processing {

	private final boolean processingSuccess;

	private final boolean processingIgnored;

	@Nullable
	private final String message;

	private Processing(boolean processingSuccess, boolean processingIgnored, @Nullable String message) {
		this.processingSuccess = processingSuccess;
		this.processingIgnored = processingIgnored;
		this.message = message;
	}

	@Nonnull
	public static Processing success() {
		return new Processing(true, false, null);
	}

	@Nonnull
	public static Processing failure(@Nonnull String message) {
		return new Processing(false, false, message);
	}

	@Nonnull
	public static Processing ignore(@Nonnull String message) {
		return new Processing(false, true, message);
	}

	public boolean isProcessingSuccess() {
		return processingSuccess;
	}

	public boolean isProcessingIgnored() {
		return processingIgnored;
	}

	@Nullable
	public String getMessage() {
		return message;
	}
}
