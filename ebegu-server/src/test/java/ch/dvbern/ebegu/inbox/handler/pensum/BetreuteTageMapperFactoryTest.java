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

import java.math.BigDecimal;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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

class BetreuteTageMapperFactoryTest {

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = { "0", "2.5" })
	void importBetreuteTage(@Nullable BigDecimal betreuteTage) {
		ZeitabschnittDTO z = createZeitabschnittDTO(Constants.DEFAULT_GUELTIGKEIT);
		z.setBetreuteTage(betreuteTage);

		BetreuungEinstellungen einstellungen = BetreuungEinstellungen.builder()
			.betreuteTageEnabled(true)
			.build();

		ProcessingContext ctx = initProcessingContext(z, einstellungen);

		BetreuungsmitteilungPensum actual = convert(ctx, z);

		assertThat(actual.getBetreuteTage(), is(betreuteTage));
		assertThat(ctx.isReadyForBestaetigen(), is(betreuteTage != null));
		if (betreuteTage == null) {
			assertThat(actual.isVollstaendig(), is(false));
		}
	}

	@Test
	void ignoreWhenDisabled() {
		ZeitabschnittDTO z = createZeitabschnittDTO(Constants.DEFAULT_GUELTIGKEIT);
		z.setBetreuteTage(BigDecimal.ONE);

		BetreuungEinstellungen einstellungen = BetreuungEinstellungen.builder()
			.betreuteTageEnabled(false)
			.build();

		ProcessingContext ctx = initProcessingContext(z, einstellungen);

		BetreuungsmitteilungPensum actual = convert(ctx, z);

		assertThat(actual.getBetreuteTage(), is(nullValue()));
	}

	@Nonnull
	private BetreuungsmitteilungPensum convert(ProcessingContext ctx, ZeitabschnittDTO z) {
		BetreuungsmitteilungPensum actual = new BetreuungsmitteilungPensum();
		BetreuteTageMapperFactory.createForBetreuteTage(ctx).toAbstractMahlzeitenPensum(actual, z);

		return actual;
	}
}
