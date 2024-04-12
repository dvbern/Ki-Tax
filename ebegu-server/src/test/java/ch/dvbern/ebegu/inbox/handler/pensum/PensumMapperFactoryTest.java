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

import ch.dvbern.ebegu.entities.AbstractMahlzeitenPensum;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.BetreuungsmitteilungPensum;
import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.EingewoehnungPauschale;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.containers.PensumUtil;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.inbox.handler.EventMonitor;
import ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungTestUtil;
import ch.dvbern.ebegu.inbox.handler.ProcessingContext;
import ch.dvbern.ebegu.services.BetreuungMonitoringService;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.BetreuungEventDTO;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.EingewoehnungDTO;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.ZeitabschnittDTO;
import ch.dvbern.kibon.exchange.commons.types.Zeiteinheit;
import org.easymock.EasyMockExtension;
import org.easymock.Mock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungTestUtil.betreuungWithSingleContainer;
import static ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungTestUtil.createBetreuungEventDTO;
import static ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungTestUtil.createZeitabschnittDTO;
import static ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungTestUtil.matches;
import static com.spotify.hamcrest.pojo.IsPojo.pojo;
import static java.util.Objects.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@ExtendWith(EasyMockExtension.class)
class PensumMapperFactoryTest {

	@Mock
	private BetreuungMonitoringService monitoringService = null;

	@Nonnull
	private final Gesuch gesuch = PlatzbestaetigungTestUtil.initGesuch();

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

		ProcessingContext ctx = initProcessingContext(betreuung, z, true);
		PensumMapper pensumMapper = PensumMapperFactory.createPensumMapper(ctx);

		BetreuungsmitteilungPensum actual = new BetreuungsmitteilungPensum();
		pensumMapper.toAbstractMahlzeitenPensum(actual, z);

		assertThat(actual, matches(z, pensumInPercent, Constants.DEFAULT_GUELTIGKEIT));
	}

	@Test
	void ignoresMahlzeitenWhenMahlzeitenVerguenstigungNotEnabled() {
		Betreuung betreuung = betreuungWithSingleContainer(gesuch);
		ZeitabschnittDTO z = createZeitabschnittDTO(Constants.DEFAULT_GUELTIGKEIT);
		z.setAnzahlHauptmahlzeiten(BigDecimal.ONE);
		z.setAnzahlNebenmahlzeiten(BigDecimal.TEN);

		ProcessingContext ctx = initProcessingContext(betreuung, z, false);
		PensumMapper pensumMapper = PensumMapperFactory.createPensumMapper(ctx);

		BetreuungsmitteilungPensum actual = new BetreuungsmitteilungPensum();
		pensumMapper.toAbstractMahlzeitenPensum(actual, z);

		assertThat(actual, pojo(AbstractMahlzeitenPensum.class)
			.where(AbstractMahlzeitenPensum::getMonatlicheHauptmahlzeiten, comparesEqualTo(BigDecimal.ZERO))
			.where(AbstractMahlzeitenPensum::getMonatlicheNebenmahlzeiten, comparesEqualTo(BigDecimal.ZERO))
			.where(AbstractMahlzeitenPensum::getTarifProHauptmahlzeit, comparesEqualTo(BigDecimal.ZERO))
			.where(AbstractMahlzeitenPensum::getTarifProNebenmahlzeit, comparesEqualTo(BigDecimal.ZERO)));
	}

	@Test
	void readsHauptMahlzeitenWhenBetreuungsAngebotMittagstisch() {
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

		ProcessingContext ctx = initProcessingContext(betreuung, z, false);
		PensumMapper pensumMapper = PensumMapperFactory.createPensumMapper(ctx);

		BetreuungsmitteilungPensum actual = new BetreuungsmitteilungPensum();
		pensumMapper.toAbstractMahlzeitenPensum(actual, z);

		assertThat(actual, pojo(AbstractMahlzeitenPensum.class)
			.where(AbstractMahlzeitenPensum::getMonatlicheHauptmahlzeiten, comparesEqualTo(BigDecimal.valueOf(4)))
			.where(AbstractMahlzeitenPensum::getMonatlicheNebenmahlzeiten, comparesEqualTo(BigDecimal.ZERO))
			.where(AbstractMahlzeitenPensum::getTarifProHauptmahlzeit, comparesEqualTo(BigDecimal.valueOf(12.25)))
			.where(AbstractMahlzeitenPensum::getTarifProNebenmahlzeit, comparesEqualTo(BigDecimal.ZERO))
			.where(AbstractMahlzeitenPensum::getPensum, closeTo(BigDecimal.valueOf(4 * 100 / 20.5), BigDecimal.valueOf(1.0e-8)))
			.where(AbstractMahlzeitenPensum::getMonatlicheBetreuungskosten, comparesEqualTo(BigDecimal.valueOf(4 * 12.25))));
	}

	@Test
	void readEingewoehnungPauschale() {
		Betreuung betreuung = betreuungWithSingleContainer(gesuch);
		ZeitabschnittDTO z = createZeitabschnittDTO(Constants.DEFAULT_GUELTIGKEIT);
		EingewoehnungDTO eingewoehnung =
			new EingewoehnungDTO(BigDecimal.valueOf(123.45), LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));
		z.setEingewoehnung(eingewoehnung);

		ProcessingContext ctx = initProcessingContext(betreuung, z, true);
		PensumMapper pensumMapper = PensumMapperFactory.createPensumMapper(ctx);

		BetreuungsmitteilungPensum actual = new BetreuungsmitteilungPensum();
		pensumMapper.toAbstractMahlzeitenPensum(actual, z);

		assertThat(actual.getEingewoehnungPauschale(), pojo(EingewoehnungPauschale.class)
			.where(EingewoehnungPauschale::getPauschale, comparesEqualTo(BigDecimal.valueOf(123.45)))
			.where(EingewoehnungPauschale::getGueltigkeit, pojo(DateRange.class)
				.where(DateRange::getGueltigAb, comparesEqualTo(LocalDate.of(2024, 1, 1)))
				.where(DateRange::getGueltigBis, comparesEqualTo(LocalDate.of(2024, 1, 31)))
			));
	}

	@Test
	void readEingewoehnungPauschale_null() {
		Betreuung betreuung = betreuungWithSingleContainer(gesuch);
		ZeitabschnittDTO z = createZeitabschnittDTO(Constants.DEFAULT_GUELTIGKEIT);
		z.setEingewoehnung(null);

		ProcessingContext ctx = initProcessingContext(betreuung, z, true);
		PensumMapper pensumMapper = PensumMapperFactory.createPensumMapper(ctx);

		BetreuungsmitteilungPensum actual = new BetreuungsmitteilungPensum();
		pensumMapper.toAbstractMahlzeitenPensum(actual, z);

		assertThat(actual.getEingewoehnungPauschale(), is(nullValue()));
	}

	@Nonnull
	private ProcessingContext initProcessingContext(
		@Nonnull Betreuung betreuung,
		@Nonnull ZeitabschnittDTO zeitabschnitt,
		boolean mahlzeitVerguenstigungEnabled
	) {
		BetreuungEventDTO betreuungEventDTO = createBetreuungEventDTO(zeitabschnitt);

		return new ProcessingContext(
			betreuung,
			betreuungEventDTO,
			getClientPeriodeGueltigkeit(betreuung),
			mahlzeitVerguenstigungEnabled,
			new EventMonitor(monitoringService, LocalDateTime.now(), betreuungEventDTO.getRefnr(), "client"),
			new BigDecimal("20.00"),
			new BigDecimal("220.00"),
			true);
	}

	@Nonnull
	private DateRange getClientPeriodeGueltigkeit(@Nonnull Betreuung betreuung) {
		return betreuung.extractGesuchsperiode().getGueltigkeit().getOverlap(Constants.DEFAULT_GUELTIGKEIT)
			.orElseThrow(() -> new IllegalArgumentException("client gueltigkeit & periode do not overlap"));
	}
}
