/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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
import lombok.EqualsAndHashCode;
import lombok.Value;

import static ch.dvbern.ebegu.inbox.handler.ProcessingState.FAILURE;
import static ch.dvbern.ebegu.inbox.handler.ProcessingState.IGNORE;
import static ch.dvbern.ebegu.inbox.handler.ProcessingState.SUCCESS;

@Value
@EqualsAndHashCode(callSuper = true)
public class PlatzbestaetigungProcessing extends Processing {

	private final Map<ImportForm, Processing> importProcessing;

	private PlatzbestaetigungProcessing(
		@Nonnull ProcessingState state,
		@Nullable String message,
		@Nonnull Map<ImportForm, Processing> importProcessing
	) {
		super(state, message);
		this.importProcessing = importProcessing;
	}

	@Nonnull
	public static PlatzbestaetigungProcessing fromImport(@Nonnull Map<ImportForm, Processing> importProcessing) {
		if (importProcessing.isEmpty()) {
			return new PlatzbestaetigungProcessing(FAILURE, "Platzbestätigung oder Mutation nicht möglich.", Map.of());
		}

		if (importProcessing.values().stream().anyMatch(p -> p.getState() == SUCCESS)) {
			return new PlatzbestaetigungProcessing(SUCCESS, null, importProcessing);
		}

		if (importProcessing.values().stream().allMatch(p -> p.getState() == IGNORE)) {
			return new PlatzbestaetigungProcessing(IGNORE, null, importProcessing);
		}

		return new PlatzbestaetigungProcessing(FAILURE, null, importProcessing);
	}
}
