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

import ch.dvbern.ebegu.entities.BetreuungsmitteilungPensum;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.services.EinstellungService;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.ZeitabschnittDTO;
import ch.dvbern.kibon.exchange.commons.types.Zeiteinheit;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungTestUtil.createZeitabschnittDTO;
import static ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungTestUtil.initProcessingContext;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;

@ExtendWith(EasyMockExtension.class)
class PensumValueMapperFactoryTest extends EasyMockSupport {

	@TestSubject
	private final PensumValueMapperFactory factory = new PensumValueMapperFactory();

	@Mock
	private EinstellungService einstellungService;

	@AfterEach
	void tearDown() {
		verifyAll();
	}

	@ParameterizedTest
	@CsvSource({
		"PERCENTAGE, 100, 100",
		"DAYS, 20, 100",
		"HOURS, 220, 100"
	})
	void mapZeitabschnittToAbstractMahlzeitenPensum(
		@Nonnull Zeiteinheit zeiteinheit,
		@Nonnull BigDecimal betreuungspensum,
		@Nonnull BigDecimal pensumInPercent
	) {
		ZeitabschnittDTO z = createZeitabschnittDTO(Constants.DEFAULT_GUELTIGKEIT);
		z.setPensumUnit(zeiteinheit);
		z.setBetreuungspensum(betreuungspensum);

		expect(einstellungService.getEinstellungAsBigDecimal(eq(EinstellungKey.OEFFNUNGSTAGE_KITA), anyObject()))
			.andReturn(BigDecimal.valueOf(240))
			.anyTimes();

		expect(einstellungService.getEinstellungAsBigDecimal(eq(EinstellungKey.OEFFNUNGSTAGE_TFO), anyObject()))
			.andReturn(BigDecimal.valueOf(240))
			.anyTimes();

		expect(einstellungService.getEinstellungAsBigDecimal(eq(EinstellungKey.OEFFNUNGSSTUNDEN_TFO), anyObject()))
			.andReturn(BigDecimal.valueOf(11))
			.anyTimes();

		BetreuungsmitteilungPensum actual = convert(z);

		assertThat(actual.getPensum(), comparesEqualTo(pensumInPercent));
	}

	@Nonnull
	private BetreuungsmitteilungPensum convert(ZeitabschnittDTO z) {
		replayAll();
		PensumValueMapper pensumMapper = factory.createForPensum(initProcessingContext(z));

		BetreuungsmitteilungPensum actual = new BetreuungsmitteilungPensum();
		pensumMapper.toAbstractMahlzeitenPensum(actual, z);

		return actual;
	}
}
