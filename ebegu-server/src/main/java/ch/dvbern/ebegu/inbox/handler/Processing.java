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

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungImportForm.ImportForm;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Processing {

	private final ProcessingState state;

	@Nullable
	private final String message;

	private final Map<ImportForm, Processing> importProcessing;

	@Nonnull
	public static Processing success() {
		return new Processing(ProcessingState.SUCCESS, null, Map.of());
	}

	@Nonnull
	public static Processing failure(@Nonnull String message) {
		return new Processing(ProcessingState.FAILURE, message, Map.of());
	}

	@Nonnull
	public static Processing ignore(@Nonnull String message) {
		return new Processing(ProcessingState.IGNORE, message, Map.of());
	}

	@Nonnull
	public static Processing fromImport(@Nonnull Map<ImportForm, Processing> importProcessing) {
		if (importProcessing.isEmpty()) {
			return new Processing(ProcessingState.FAILURE, "Platzbestätigung oder Mutation nicht möglich.", Map.of());
		}

		if (importProcessing.values().stream().anyMatch(p -> p.getState() == ProcessingState.SUCCESS)) {
			return new Processing(ProcessingState.SUCCESS, null, importProcessing);
		}

		if (importProcessing.values().stream().allMatch(p -> p.getState() == ProcessingState.IGNORE)) {
			return new Processing(ProcessingState.IGNORE, null, importProcessing);
		}

		return new Processing(ProcessingState.FAILURE, null, importProcessing);
	}

	public boolean isProcessingSuccess() {
		return state == ProcessingState.SUCCESS;
	}

	public boolean isProcessingIgnored() {
		return state == ProcessingState.IGNORE;
	}
}
