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
import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AbstractBetreuungsPensum;
import ch.dvbern.ebegu.entities.AbstractMahlzeitenPensum;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.BetreuungsmitteilungPensum;
import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.EingewoehnungPauschale;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.containers.PensumUtil;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.inbox.handler.EventMonitor;
import ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungTestUtil;
import ch.dvbern.ebegu.inbox.handler.ProcessingContext;
import ch.dvbern.ebegu.services.BetreuungMonitoringService;
import ch.dvbern.ebegu.services.EinstellungService;
import ch.dvbern.ebegu.services.MitteilungService;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.BetreuungEventDTO;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.EingewoehnungDTO;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.ZeitabschnittDTO;
import ch.dvbern.kibon.exchange.commons.types.Zeiteinheit;
import lombok.Builder;
import lombok.Value;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungTestUtil.betreuungWithSingleContainer;
import static ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungTestUtil.createBetreuungEventDTO;
import static ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungTestUtil.createZeitabschnittDTO;
import static ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungTestUtil.matches;
import static com.spotify.hamcrest.pojo.IsPojo.pojo;
import static java.util.Objects.requireNonNull;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@ExtendWith(EasyMockExtension.class)
class PensumMapperFactoryTest extends EasyMockSupport {

	@Mock
	private EinstellungService einstellungService;

	@Mock
	private MitteilungService mitteilungService;

	@Mock
	private BetreuungMonitoringService monitoringService = null;

	private PensumMapperFactory factory;

	@Nonnull
	private final Gesuch gesuch = PlatzbestaetigungTestUtil.initGesuch();

	@BeforeEach
	void setUp() {
		// EinstellungMock is initialized too late when trying to do field initializers instead.
		factory = new PensumMapperFactory(
			new BetreuungInFerienzeitMapperFactory(mitteilungService),
			new MahlzeitVerguenstigungMapperFactory(einstellungService),
			new PensumValueMapperFactory(einstellungService)
		);
	}

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
		Betreuung betreuung = betreuungWithSingleContainer(gesuch);
		ZeitabschnittDTO z = createZeitabschnittDTO(Constants.DEFAULT_GUELTIGKEIT);
		z.setPensumUnit(zeiteinheit);
		z.setBetreuungspensum(betreuungspensum);

		withEinstellungen(TestEinstellungen.builder().build());
		BetreuungsmitteilungPensum actual = convert(betreuung, z);

		assertThat(actual, matches(z, pensumInPercent, Constants.DEFAULT_GUELTIGKEIT));
	}

	@Nested
	class MahlzeitenverguenstigungTest {

		@Nested
		class WhenEinstellungEnabled {

			@Test
			void doImport() {
				Betreuung betreuung = betreuungWithSingleContainer(gesuch);
				ZeitabschnittDTO z = createZeitabschnittWithMahlzeiten();

				withEinstellungen(TestEinstellungen.builder().mahlzeitenvVerguenstigungEnabled(true).build());
				BetreuungsmitteilungPensum actual = convert(betreuung, z);

				assertThat(actual, pojo(AbstractMahlzeitenPensum.class)
					.where(AbstractMahlzeitenPensum::getMonatlicheHauptmahlzeiten, comparesEqualTo(BigDecimal.ONE))
					.where(AbstractMahlzeitenPensum::getMonatlicheNebenmahlzeiten, comparesEqualTo(BigDecimal.TEN))
					.where(AbstractMahlzeitenPensum::getTarifProHauptmahlzeit, comparesEqualTo(BigDecimal.valueOf(12.5)))
					.where(AbstractMahlzeitenPensum::getTarifProNebenmahlzeit, comparesEqualTo(BigDecimal.valueOf(9.5))));
			}

			@Test
			void requireHumanConfirmationWhenTarifHauptmahlzeitenMissing() {
				Betreuung betreuung = betreuungWithSingleContainer(gesuch);
				ZeitabschnittDTO z = createZeitabschnittWithMahlzeiten();
				z.setTarifProHauptmahlzeiten(null);

				withEinstellungen(TestEinstellungen.builder().mahlzeitenvVerguenstigungEnabled(true).build());
				ProcessingContext ctx = initProcessingContext(betreuung, z);
				BetreuungsmitteilungPensum actual = convert(ctx, z);

				assertThat(ctx.isReadyForBestaetigen(), is(false));
				assertThat(actual.isVollstaendig(), is(false));
			}

			@Test
			void requireHumanConfirmationWhenTarifNebenmahlzeitenMissing() {
				Betreuung betreuung = betreuungWithSingleContainer(gesuch);
				ZeitabschnittDTO z = createZeitabschnittWithMahlzeiten();
				z.setTarifProNebenmahlzeiten(null);

				withEinstellungen(TestEinstellungen.builder().mahlzeitenvVerguenstigungEnabled(true).build());
				ProcessingContext ctx = initProcessingContext(betreuung, z);
				BetreuungsmitteilungPensum actual = convert(ctx, z);

				assertThat(ctx.isReadyForBestaetigen(), is(false));
				assertThat(actual.isVollstaendig(), is(false));
			}
		}

		@Test
		void ignoreWhenDisabled() {
			Betreuung betreuung = betreuungWithSingleContainer(gesuch);
			ZeitabschnittDTO z = createZeitabschnittWithMahlzeiten();

			withEinstellungen(TestEinstellungen.builder().mahlzeitenvVerguenstigungEnabled(false).build());
			BetreuungsmitteilungPensum actual = convert(betreuung, z);

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
	}

	@Nonnull
	private BetreuungsmitteilungPensum convert(Betreuung betreuung, ZeitabschnittDTO z) {
		return convert(initProcessingContext(betreuung, z), z);
	}

	@Nonnull
	private BetreuungsmitteilungPensum convert(ProcessingContext ctx, ZeitabschnittDTO z) {
		replayAll();
		PensumMapper<AbstractBetreuungsPensum> pensumMapper = factory.createPensumMapper(ctx);

		BetreuungsmitteilungPensum actual = new BetreuungsmitteilungPensum();
		pensumMapper.toAbstractMahlzeitenPensum(actual, z);

		return actual;
	}

	@Test
	void importHauptMahlzeitenWhenBetreuungsAngebotMittagstisch() {
		BigDecimal monatlicheHauptmahlzeiten = BigDecimal.valueOf(5);
		BigDecimal tarifProHauptmahlzeit = BigDecimal.valueOf(10.5);

		Betreuung betreuung = betreuungWithSingleContainer(gesuch);

		// setup a MITTAGSTISCH
		betreuung.getInstitutionStammdaten().setBetreuungsangebotTyp(BetreuungsangebotTyp.MITTAGSTISCH);
		BetreuungspensumContainer container = betreuung.getBetreuungspensumContainers().iterator().next();
		Betreuungspensum betreuungspensum = requireNonNull(container.getBetreuungspensumJA());
		betreuungspensum.setMonatlicheHauptmahlzeiten(monatlicheHauptmahlzeiten);
		betreuungspensum.setTarifProHauptmahlzeit(tarifProHauptmahlzeit);
		PensumUtil.transformMittagstischPensum(betreuungspensum);

		ZeitabschnittDTO z = createZeitabschnittDTO(Constants.DEFAULT_GUELTIGKEIT);
		z.setAnzahlHauptmahlzeiten(BigDecimal.valueOf(4));
		z.setTarifProHauptmahlzeiten(BigDecimal.valueOf(12.25));
		z.setAnzahlNebenmahlzeiten(BigDecimal.TEN);

		withEinstellungen(TestEinstellungen.builder().build());
		BetreuungsmitteilungPensum actual = convert(betreuung, z);

		assertThat(actual, pojo(AbstractMahlzeitenPensum.class)
			.where(AbstractMahlzeitenPensum::getMonatlicheHauptmahlzeiten, comparesEqualTo(BigDecimal.valueOf(4)))
			.where(AbstractMahlzeitenPensum::getMonatlicheNebenmahlzeiten, comparesEqualTo(BigDecimal.ZERO))
			.where(AbstractMahlzeitenPensum::getTarifProHauptmahlzeit, comparesEqualTo(BigDecimal.valueOf(12.25)))
			.where(AbstractMahlzeitenPensum::getTarifProNebenmahlzeit, comparesEqualTo(BigDecimal.ZERO))
			.where(AbstractMahlzeitenPensum::getPensum, closeTo(BigDecimal.valueOf(4 * 100 / 20.5), BigDecimal.valueOf(1.0e-8)))
			.where(AbstractMahlzeitenPensum::getMonatlicheBetreuungskosten, comparesEqualTo(BigDecimal.valueOf(4 * 12.25))));
	}

	@Test
	void importEingewoehnungPauschale() {
		Betreuung betreuung = betreuungWithSingleContainer(gesuch);
		ZeitabschnittDTO z = createZeitabschnittDTO(Constants.DEFAULT_GUELTIGKEIT);
		EingewoehnungDTO eingewoehnung =
			new EingewoehnungDTO(BigDecimal.valueOf(123.45), LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));
		z.setEingewoehnung(eingewoehnung);

		withEinstellungen(TestEinstellungen.builder().build());
		BetreuungsmitteilungPensum actual = convert(betreuung, z);

		assertThat(actual.getEingewoehnungPauschale(), pojo(EingewoehnungPauschale.class)
			.where(EingewoehnungPauschale::getPauschale, comparesEqualTo(BigDecimal.valueOf(123.45)))
			.where(EingewoehnungPauschale::getGueltigkeit, pojo(DateRange.class)
				.where(DateRange::getGueltigAb, comparesEqualTo(LocalDate.of(2024, 1, 1)))
				.where(DateRange::getGueltigBis, comparesEqualTo(LocalDate.of(2024, 1, 31)))
			));
	}

	@Test
	void importEingewoehnungPauschale_null() {
		Betreuung betreuung = betreuungWithSingleContainer(gesuch);
		ZeitabschnittDTO z = createZeitabschnittDTO(Constants.DEFAULT_GUELTIGKEIT);
		z.setEingewoehnung(null);

		withEinstellungen(TestEinstellungen.builder().build());
		BetreuungsmitteilungPensum actual = convert(betreuung, z);

		assertThat(actual.getEingewoehnungPauschale(), is(nullValue()));
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(booleans = { true, false })
	void importBetreuungInFerienzeit(Boolean betreuungInFerienzeit) {
		Betreuung betreuung = betreuungWithSingleContainer(gesuch);
		ZeitabschnittDTO z = createZeitabschnittDTO(Constants.DEFAULT_GUELTIGKEIT);
		z.setBetreuungInFerienzeit(betreuungInFerienzeit);

		withEinstellungen(TestEinstellungen.builder().schulergaenzendeBetreuungEnabled(true).build());
		ProcessingContext ctx = initProcessingContext(betreuung, z);
		BetreuungsmitteilungPensum actual = convert(ctx, z);

		assertThat(actual.getBetreuungInFerienzeit(), is(betreuungInFerienzeit));
		assertThat(ctx.isReadyForBestaetigen(), is(betreuungInFerienzeit != null));
		if (betreuungInFerienzeit == null) {
			assertThat(actual.isVollstaendig(), is(false));
		}
	}

	@Nonnull
	private ProcessingContext initProcessingContext(@Nonnull Betreuung betreuung, @Nonnull ZeitabschnittDTO zeitabschnitt) {
		BetreuungEventDTO betreuungEventDTO = createBetreuungEventDTO(zeitabschnitt);

		return new ProcessingContext(
			betreuung,
			betreuungEventDTO,
			getClientPeriodeGueltigkeit(betreuung),
			new EventMonitor(monitoringService, LocalDateTime.now(), betreuungEventDTO.getRefnr(), "client"),
			true);
	}

	@Nonnull
	private DateRange getClientPeriodeGueltigkeit(@Nonnull Betreuung betreuung) {
		return betreuung.extractGesuchsperiode().getGueltigkeit().getOverlap(Constants.DEFAULT_GUELTIGKEIT)
			.orElseThrow(() -> new IllegalArgumentException("client gueltigkeit & periode do not overlap"));
	}

	private void withEinstellungen(TestEinstellungen einstellungen) {
		expect(mitteilungService.showSchulergaenzendeBetreuung(anyObject()))
			.andReturn(einstellungen.isSchulergaenzendeBetreuungEnabled())
			.anyTimes();

		expect(einstellungService.isEnabled(eq(EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED), anyObject()))
			.andReturn(einstellungen.isMahlzeitenvVerguenstigungEnabled())
			.anyTimes();

		expect(einstellungService.getEinstellungAsBigDecimal(eq(EinstellungKey.OEFFNUNGSTAGE_KITA), anyObject()))
			.andReturn(BigDecimal.valueOf(240))
			.anyTimes();

		expect(einstellungService.getEinstellungAsBigDecimal(eq(EinstellungKey.OEFFNUNGSTAGE_TFO), anyObject()))
			.andReturn(BigDecimal.valueOf(240))
			.anyTimes();

		expect(einstellungService.getEinstellungAsBigDecimal(eq(EinstellungKey.OEFFNUNGSSTUNDEN_TFO), anyObject()))
			.andReturn(BigDecimal.valueOf(11))
			.anyTimes();
	}

	@Value
	@Builder
	private static class TestEinstellungen {
		@Builder.Default
		boolean schulergaenzendeBetreuungEnabled = false;
		@Builder.Default
		boolean mahlzeitenvVerguenstigungEnabled = false;
	}
}
