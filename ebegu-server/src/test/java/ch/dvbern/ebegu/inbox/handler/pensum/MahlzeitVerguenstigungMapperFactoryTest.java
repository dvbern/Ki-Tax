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

import ch.dvbern.ebegu.entities.AbstractMahlzeitenPensum;
import ch.dvbern.ebegu.entities.BetreuungsmitteilungPensum;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.inbox.handler.ProcessingContext;
import ch.dvbern.ebegu.services.EinstellungService;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.ZeitabschnittDTO;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungTestUtil.createZeitabschnittDTO;
import static ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungTestUtil.initProcessingContext;
import static com.spotify.hamcrest.pojo.IsPojo.pojo;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.is;

@ExtendWith(EasyMockExtension.class)
class MahlzeitVerguenstigungMapperFactoryTest extends EasyMockSupport {

	@TestSubject
	private final MahlzeitVerguenstigungMapperFactory factory = new MahlzeitVerguenstigungMapperFactory();

	@Mock
	private EinstellungService einstellungService;

	@AfterEach
	void tearDown() {
		verifyAll();
	}

	@Nested
	class WhenEinstellungEnabled {

		@Test
		void doImport() {
			ZeitabschnittDTO z = createZeitabschnittWithMahlzeiten();

			expect(einstellungService.isEnabled(eq(EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED), anyObject()))
				.andReturn(true);
			ProcessingContext ctx = initProcessingContext(z);
			BetreuungsmitteilungPensum actual = convert(ctx, z);

			assertThat(actual, pojo(AbstractMahlzeitenPensum.class)
				.where(AbstractMahlzeitenPensum::getMonatlicheHauptmahlzeiten, comparesEqualTo(BigDecimal.ONE))
				.where(AbstractMahlzeitenPensum::getMonatlicheNebenmahlzeiten, comparesEqualTo(BigDecimal.TEN))
				.where(AbstractMahlzeitenPensum::getTarifProHauptmahlzeit, comparesEqualTo(BigDecimal.valueOf(12.5)))
				.where(AbstractMahlzeitenPensum::getTarifProNebenmahlzeit, comparesEqualTo(BigDecimal.valueOf(9.5))));
		}

		@Test
		void requireHumanConfirmationWhenTarifHauptmahlzeitenMissing() {
			ZeitabschnittDTO z = createZeitabschnittWithMahlzeiten();
			z.setTarifProHauptmahlzeiten(null);

			expect(einstellungService.isEnabled(eq(EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED), anyObject()))
				.andReturn(true);
			ProcessingContext ctx = initProcessingContext(z);
			BetreuungsmitteilungPensum actual = convert(ctx, z);

			assertThat(ctx.isReadyForBestaetigen(), is(false));
			assertThat(actual.isVollstaendig(), is(false));
		}

		@Test
		void requireHumanConfirmationWhenTarifNebenmahlzeitenMissing() {
			ZeitabschnittDTO z = createZeitabschnittWithMahlzeiten();
			z.setTarifProNebenmahlzeiten(null);

			expect(einstellungService.isEnabled(eq(EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED), anyObject()))
				.andReturn(true);
			ProcessingContext ctx = initProcessingContext(z);
			BetreuungsmitteilungPensum actual = convert(ctx, z);

			assertThat(ctx.isReadyForBestaetigen(), is(false));
			assertThat(actual.isVollstaendig(), is(false));
		}
	}

	@Test
	void ignoreWhenDisabled() {
		ZeitabschnittDTO z = createZeitabschnittWithMahlzeiten();

		expect(einstellungService.isEnabled(eq(EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED), anyObject()))
			.andReturn(false);
		ProcessingContext ctx = initProcessingContext(z);
		BetreuungsmitteilungPensum actual = convert(ctx, z);

		assertThat(actual, pojo(AbstractMahlzeitenPensum.class)
			.where(AbstractMahlzeitenPensum::getMonatlicheHauptmahlzeiten, comparesEqualTo(BigDecimal.ZERO))
			.where(AbstractMahlzeitenPensum::getMonatlicheNebenmahlzeiten, comparesEqualTo(BigDecimal.ZERO))
			.where(AbstractMahlzeitenPensum::getTarifProHauptmahlzeit, comparesEqualTo(BigDecimal.ZERO))
			.where(AbstractMahlzeitenPensum::getTarifProNebenmahlzeit, comparesEqualTo(BigDecimal.ZERO)));
	}

	@Nonnull
	private ZeitabschnittDTO createZeitabschnittWithMahlzeiten() {
		ZeitabschnittDTO z = createZeitabschnittDTO(Constants.DEFAULT_GUELTIGKEIT);
		z.setAnzahlHauptmahlzeiten(BigDecimal.ONE);
		z.setTarifProHauptmahlzeiten(BigDecimal.valueOf(12.5));
		z.setAnzahlNebenmahlzeiten(BigDecimal.TEN);
		z.setTarifProNebenmahlzeiten(BigDecimal.valueOf(9.5));

		return z;
	}

	@Nonnull
	private BetreuungsmitteilungPensum convert(ProcessingContext ctx, ZeitabschnittDTO z) {
		replayAll();
		PensumMapper<AbstractMahlzeitenPensum> pensumMapper = factory.createForMahlzeitenVerguenstigung(ctx);

		BetreuungsmitteilungPensum actual = new BetreuungsmitteilungPensum();
		pensumMapper.toAbstractMahlzeitenPensum(actual, z);

		return actual;
	}
}
