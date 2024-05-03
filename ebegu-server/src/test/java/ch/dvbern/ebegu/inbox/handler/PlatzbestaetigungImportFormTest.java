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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungsmitteilung;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungImportForm.ImportForm;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.BetreuungEventDTO;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;

import static ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungTestUtil.betreuungWithSingleContainer;
import static org.easymock.EasyMock.mock;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;

class PlatzbestaetigungImportFormTest {

	@Nested
	class WhenOpenBetreuungsmitteilung {

		@ParameterizedTest
		@EnumSource(Betreuungsstatus.class)
		void shouldAlwaysIncludeMutationsMitteilung(Betreuungsstatus betreuungsstatus) {
			var ctx = createBetreuungsContext(betreuungsstatus, mock(Betreuungsmitteilung.class));

			var result = PlatzbestaetigungImportForm.importAs(ctx);
			assertThat(result, hasItem(ImportForm.MUTATIONS_MITTEILUNG));
		}
	}

	@Nested
	class When_WARTEN {

		@Test
		void platzbestaetigung() {
			var ctx = createBetreuungsContext(Betreuungsstatus.WARTEN);

			var result = PlatzbestaetigungImportForm.importAs(ctx);
			assertThat(result, contains(ImportForm.PLATZBESTAETIGUNG));
		}
	}

	@Nested
	class When_BESTAETIGT {

		@ParameterizedTest
		@EnumSource(value = AntragStatus.class,
			mode = Mode.INCLUDE,
			names = { "IN_BEARBEITUNG_GS", "IN_BEARBEITUNG_SOZIALDIENST" })
		void platzbestaetigungWhenNotYetFreigegeben(AntragStatus antragStatus) {
			var ctx = createBetreuungsContext(Betreuungsstatus.BESTAETIGT);

			ctx.getBetreuung().extractGesuch().setStatus(antragStatus);

			var result = PlatzbestaetigungImportForm.importAs(ctx);
			assertThat(result, contains(ImportForm.PLATZBESTAETIGUNG));
		}

		@ParameterizedTest
		@EnumSource(value = AntragStatus.class,
			mode = Mode.EXCLUDE,
			names = { "IN_BEARBEITUNG_GS", "IN_BEARBEITUNG_SOZIALDIENST" })
		void mutationsMitteilungWhenFreigegeben(AntragStatus antragStatus) {
			var ctx = createBetreuungsContext(Betreuungsstatus.BESTAETIGT);

			ctx.getBetreuung().extractGesuch().setStatus(antragStatus);

			var result = PlatzbestaetigungImportForm.importAs(ctx);
			assertThat(result, contains(ImportForm.MUTATIONS_MITTEILUNG));
		}
	}

	@ParameterizedTest
	@EnumSource(value = Betreuungsstatus.class,
		mode = Mode.INCLUDE,
		names = { "VERFUEGT", "STORNIERT", "GESCHLOSSEN_OHNE_VERFUEGUNG" })
	void mutationsMitteilung(Betreuungsstatus betreuungsstatus) {
		var ctx = createBetreuungsContext(betreuungsstatus);

		var result = PlatzbestaetigungImportForm.importAs(ctx);
		assertThat(result, contains(ImportForm.MUTATIONS_MITTEILUNG));
	}

	@ParameterizedTest
	@EnumSource(value = Betreuungsstatus.class,
		mode = Mode.EXCLUDE,
		names = { "WARTEN", "BESTAETIGT", "VERFUEGT", "STORNIERT", "GESCHLOSSEN_OHNE_VERFUEGUNG" })
	void ignored(Betreuungsstatus betreuungsstatus) {
		var ctx = createBetreuungsContext(betreuungsstatus);

		var result = PlatzbestaetigungImportForm.importAs(ctx);
		assertThat(result, Matchers.emptyCollectionOf(ImportForm.class));
	}

	@Nonnull
	private ProcessingContext createBetreuungsContext(
		Betreuungsstatus betreuungsstatus,
		@Nullable Betreuungsmitteilung betreuungsmitteilung
	) {
		Gesuch gesuch = PlatzbestaetigungTestUtil.initGesuch();
		Betreuung betreuung = betreuungWithSingleContainer(gesuch);
		betreuung.setBetreuungsstatus(betreuungsstatus);

		return new ProcessingContext(
			betreuung,
			betreuungsmitteilung,
			mock(BetreuungEventDTO.class),
			mock(DateRange.class),
			mock(EventMonitor.class),
			true);
	}

	@Nonnull
	private ProcessingContext createBetreuungsContext(Betreuungsstatus betreuungsstatus) {
		return createBetreuungsContext(betreuungsstatus, null);
	}
}
