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

package ch.dvbern.ebegu.inbox.handler.pensum;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.betreuung.BetreuungEinstellungen;
import ch.dvbern.ebegu.entities.BetreuungsmitteilungPensum;
import ch.dvbern.ebegu.inbox.handler.ProcessingContext;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.ZeitabschnittDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungTestUtil.createZeitabschnittDTO;
import static ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungTestUtil.initProcessingContext;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

class BetreuungInFerienzeitMapperFactoryTest {

	@ParameterizedTest
	@NullSource
	@ValueSource(booleans = { true, false })
	void importBetreuungInFerienzeit(Boolean betreuungInFerienzeit) {
		ZeitabschnittDTO z = createZeitabschnittDTO(Constants.DEFAULT_GUELTIGKEIT);
		z.setBetreuungInFerienzeit(betreuungInFerienzeit);

		BetreuungEinstellungen einstellungen = BetreuungEinstellungen.builder()
			.schulergaenzendeBetreuungEnabled(true)
			.build();

		ProcessingContext ctx = initProcessingContext(z, einstellungen);

		BetreuungsmitteilungPensum actual = convert(ctx, z);

		assertThat(actual.getBetreuungInFerienzeit(), is(betreuungInFerienzeit));
		assertThat(ctx.isReadyForBestaetigen(), is(betreuungInFerienzeit != null));
		if (betreuungInFerienzeit == null) {
			assertThat(actual.isVollstaendig(), is(false));
		}
	}

	@Test
	void ignoreWhenDisabled() {
		ZeitabschnittDTO z = createZeitabschnittDTO(Constants.DEFAULT_GUELTIGKEIT);
		z.setBetreuungInFerienzeit(true);

		BetreuungEinstellungen einstellungen = BetreuungEinstellungen.builder()
			.schulergaenzendeBetreuungEnabled(false)
			.build();

		ProcessingContext ctx = initProcessingContext(z, einstellungen);

		BetreuungsmitteilungPensum actual = convert(ctx, z);

		assertThat(actual.getBetreuungInFerienzeit(), is(nullValue()));
	}

	@Nonnull
	private BetreuungsmitteilungPensum convert(ProcessingContext ctx, ZeitabschnittDTO z) {
		BetreuungsmitteilungPensum actual = new BetreuungsmitteilungPensum();
		BetreuungInFerienzeitMapperFactory.createForBetreuungInFerienzeit(ctx).toAbstractMahlzeitenPensum(actual, z);

		return actual;
	}
}
