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

import java.util.List;

import ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungImportForm.ImportForm;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.stringContainsInOrder;

class PlatzbestaetigungProcessingTest {

	@Test
	void toString_shouldShowParent() {
		var platzbestaetigung = PlatzbestaetigungProcessing.withImportFrom(
			ImportForm.PLATZBESTAETIGUNG,
			Processing.ignore("platzbestaetigung not necessary"));

		assertThat(platzbestaetigung, hasToString(
			stringContainsInOrder("state=IGNORE", "message=platzbestaetigung not necessary", "importForm=PLATZBESTAETIGUNG"))
		);
	}

	@Test
	void toString_printsAllProcessings() {
		var platzbestaetigung = PlatzbestaetigungProcessing.withImportFrom(
			ImportForm.PLATZBESTAETIGUNG,
			Processing.ignore("platzbestaetigung not necessary"));

		var mutationsMitteilung = PlatzbestaetigungProcessing.withImportFrom(
			ImportForm.MUTATIONS_MITTEILUNG,
			Processing.failure("mutationsMitteilung failed"));

		var mutationsMitteilung2 = PlatzbestaetigungProcessing.withImportFrom(
			ImportForm.MUTATIONS_MITTEILUNG,
			Processing.success());

		PlatzbestaetigungProcessing combined =
			PlatzbestaetigungProcessing.fromImport(List.of(platzbestaetigung, mutationsMitteilung, mutationsMitteilung2));

		assertThat(combined, hasToString(
			stringContainsInOrder("state=IGNORE", "state=FAILURE", "state=SUCCESS")
		));
	}
}
