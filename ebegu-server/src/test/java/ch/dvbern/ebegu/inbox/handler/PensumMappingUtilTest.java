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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AbstractMahlzeitenPensum;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.BetreuungsmitteilungPensum;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.services.BetreuungMonitoringService;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.testfaelle.Testfall01_WaeltiDagmar;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.BetreuungEventDTO;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.ZeitabschnittDTO;
import ch.dvbern.kibon.exchange.commons.types.Zeiteinheit;
import org.easymock.EasyMockExtension;
import org.easymock.Mock;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungTestUtil.betreuungWithSingleContainer;
import static ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungTestUtil.createBetreuungEventDTO;
import static ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungTestUtil.createBetreuungsmitteilungPensum;
import static ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungTestUtil.createZeitabschnittDTO;
import static ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungTestUtil.matches;
import static com.spotify.hamcrest.pojo.IsPojo.pojo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.contains;

@ExtendWith(EasyMockExtension.class)
class PensumMappingUtilTest {

	@Mock
	private BetreuungMonitoringService monitoringService = null;

	@Nonnull
	private final Gesuch gesuch = initGesuch();

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
		BetreuungEventDTO betreuungEventDTO = createBetreuungEventDTO(z);

		DateRange gueltigket = getClientPeriodeGueltigkeit(betreuung);
		EventMonitor eventMonitor =
			new EventMonitor(monitoringService, LocalDateTime.now(), betreuungEventDTO.getRefnr(), "client");

		ProcessingContext ctx =
			new ProcessingContext(betreuung, betreuungEventDTO, gueltigket, true, eventMonitor);

		BetreuungsmitteilungPensum actual =
			PensumMappingUtil.toAbstractMahlzeitenPensum(new BetreuungsmitteilungPensum(), z, ctx);

		assertThat(actual, matches(z, pensumInPercent, Constants.DEFAULT_GUELTIGKEIT));
	}

	@Test
	void ignoresMahlzeitenWhenMahlzeitenVerguenstigungNotEnabled() {
		Betreuung betreuung = betreuungWithSingleContainer(gesuch);
		ZeitabschnittDTO z = createZeitabschnittDTO(Constants.DEFAULT_GUELTIGKEIT);
		z.setAnzahlHauptmahlzeiten(BigDecimal.ONE);
		z.setAnzahlNebenmahlzeiten(BigDecimal.TEN);
		BetreuungEventDTO betreuungEventDTO = createBetreuungEventDTO(z);

		DateRange gueltigket = getClientPeriodeGueltigkeit(betreuung);
		EventMonitor eventMonitor =
			new EventMonitor(monitoringService, LocalDateTime.now(), betreuungEventDTO.getRefnr(), "client");

		ProcessingContext ctx =
			new ProcessingContext(betreuung, betreuungEventDTO, gueltigket, false, eventMonitor);

		BetreuungsmitteilungPensum actual =
			PensumMappingUtil.toAbstractMahlzeitenPensum(new BetreuungsmitteilungPensum(), z, ctx);

		assertThat(actual, pojo(AbstractMahlzeitenPensum.class)
			.where(AbstractMahlzeitenPensum::getMonatlicheHauptmahlzeiten, comparesEqualTo(BigDecimal.ZERO))
			.where(AbstractMahlzeitenPensum::getMonatlicheNebenmahlzeiten, comparesEqualTo(BigDecimal.ZERO))
			.where(AbstractMahlzeitenPensum::getTarifProHauptmahlzeit, comparesEqualTo(BigDecimal.ZERO))
			.where(AbstractMahlzeitenPensum::getTarifProNebenmahlzeit, comparesEqualTo(BigDecimal.ZERO)));
	}

	@Nonnull
	private Gesuch initGesuch() {
		Gesuchsperiode gesuchsperiode = TestDataUtil.createGesuchsperiodeXXYY(2020, 2021);
		List<InstitutionStammdaten> institutionStammdatenList = new ArrayList<>();
		institutionStammdatenList.add(TestDataUtil.createInstitutionStammdatenKitaWeissenstein());
		institutionStammdatenList.add(TestDataUtil.createInstitutionStammdatenKitaBruennen());

		Testfall01_WaeltiDagmar testfall_1GS =
			new Testfall01_WaeltiDagmar(gesuchsperiode, institutionStammdatenList, false);
		testfall_1GS.createFall();
		testfall_1GS.createGesuch(LocalDate.of(2016, Month.DECEMBER, 12));

		return testfall_1GS.fillInGesuch();
	}

	@Nonnull
	private DateRange getClientPeriodeGueltigkeit(@Nonnull Betreuung betreuung) {
		return betreuung.extractGesuchsperiode().getGueltigkeit().getOverlap(Constants.DEFAULT_GUELTIGKEIT)
			.orElseThrow(() -> new IllegalArgumentException("client gueltigkeit & periode do not overlap"));
	}

	@Nested
	class MergeSamePensenTest {

		@Test
		void nopForSinglePensum() {
			DateRange range1 = new DateRange(2020);
			BetreuungsmitteilungPensum pensum1 = createBetreuungsmitteilungPensum(range1);

			List<BetreuungsmitteilungPensum> pensen = Collections.singletonList(pensum1);
			Collection<BetreuungsmitteilungPensum> result = extendGueltigkeit(pensen);

			assertThat(result, contains(
				matches(pensum1, range1)
			));
		}

		@Test
		void shouldExtendsGueltigkeitOfAdjactentPensen() {
			DateRange range1 = new DateRange(2020);
			DateRange range2 = new DateRange(2021);
			BetreuungsmitteilungPensum pensum1 = createBetreuungsmitteilungPensum(range1);
			BetreuungsmitteilungPensum pensum2 = createBetreuungsmitteilungPensum(range2);

			Collection<BetreuungsmitteilungPensum> result = extendGueltigkeit(Arrays.asList(pensum1, pensum2));

			assertThat(result, contains(
				matches(pensum1, range1.getGueltigAb(), range2.getGueltigBis())
			));
		}

		@Test
		void shouldNotExtendGueltigkeitWhenNotSame() {
			DateRange range1 = new DateRange(2020);
			DateRange range2 = new DateRange(2021);
			BetreuungsmitteilungPensum pensum1 = createBetreuungsmitteilungPensum(range1);
			BetreuungsmitteilungPensum pensum2 = createBetreuungsmitteilungPensum(range2);
			// make them different
			pensum2.setMonatlicheBetreuungskosten(BigDecimal.TEN);

			Collection<BetreuungsmitteilungPensum> result = extendGueltigkeit(Arrays.asList(pensum1, pensum2));

			assertThat(result, contains(
				matches(pensum1, range1),
				matches(pensum2, range2)
			));
		}

		@Test
		void shouldNotExtendGueltigkeitWhenGueltigeitsGap() {
			DateRange range1 = new DateRange(2020);
			DateRange range2 = new DateRange(2022);
			BetreuungsmitteilungPensum pensum1 = createBetreuungsmitteilungPensum(range1);
			BetreuungsmitteilungPensum pensum2 = createBetreuungsmitteilungPensum(range2);

			Collection<BetreuungsmitteilungPensum> result = extendGueltigkeit(Arrays.asList(pensum1, pensum2));

			assertThat(result, contains(
				matches(pensum1, range1),
				matches(pensum2, range2)
			));
		}

		@Test
		void shouldExtendMultipleTimes() {
			DateRange range1 = new DateRange(2020);
			DateRange range2 = new DateRange(2021);
			DateRange range3 = new DateRange(2022);
			DateRange range4 = new DateRange(2023);
			DateRange range5 = new DateRange(2024);
			BetreuungsmitteilungPensum pensum1 = createBetreuungsmitteilungPensum(range1);
			BetreuungsmitteilungPensum pensum2 = createBetreuungsmitteilungPensum(range2);
			BetreuungsmitteilungPensum pensum3 = createBetreuungsmitteilungPensum(range3);
			pensum3.setPensum(BigDecimal.TEN);
			BetreuungsmitteilungPensum pensum4 = createBetreuungsmitteilungPensum(range4);
			pensum4.setPensum(BigDecimal.TEN);
			BetreuungsmitteilungPensum pensum5 = createBetreuungsmitteilungPensum(range5);

			List<BetreuungsmitteilungPensum> pensen = Arrays.asList(pensum1, pensum2, pensum3, pensum4, pensum5);
			Collection<BetreuungsmitteilungPensum> result = extendGueltigkeit(pensen);

			assertThat(result, contains(
				matches(pensum1, range1.getGueltigAb(), range2.getGueltigBis()),
				matches(pensum3, range3.getGueltigAb(), range4.getGueltigBis()),
				matches(pensum5, range5)
			));
		}

		@Nonnull
		private Collection<BetreuungsmitteilungPensum> extendGueltigkeit(
			@Nonnull Collection<BetreuungsmitteilungPensum> pensen) {

			return PensumMappingUtil.extendGueltigkeit(pensen, a -> a);
		}
	}
}
