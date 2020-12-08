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
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AbstractMahlzeitenPensum;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungsmitteilung;
import ch.dvbern.ebegu.entities.BetreuungsmitteilungPensum;
import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.enums.PensumUnits;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.testfaelle.Testfall01_WaeltiDagmar;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.BetreuungEventDTO;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.ZeitabschnittDTO;
import ch.dvbern.kibon.exchange.commons.types.Zeiteinheit;
import com.spotify.hamcrest.pojo.IsPojo;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static ch.dvbern.ebegu.util.EbeguUtil.coalesce;
import static com.google.common.base.Preconditions.checkArgument;
import static com.spotify.hamcrest.pojo.IsPojo.pojo;
import static java.util.Objects.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

class PlatzbestaetigungEventHandlerTest {

	private final PlatzbestaetigungEventHandler handler = new PlatzbestaetigungEventHandler();
	private Gesuch gesuch_1GS = null;
	private Gesuchsperiode gesuchsperiode = null;

	@BeforeEach
	void setUp() {
		gesuchsperiode = TestDataUtil.createGesuchsperiodeXXYY(2020, 2021);
		TestDataUtil.createGemeindeParis();
		List<InstitutionStammdaten> institutionStammdatenList = new ArrayList<>();
		institutionStammdatenList.add(TestDataUtil.createInstitutionStammdatenKitaWeissenstein());
		institutionStammdatenList.add(TestDataUtil.createInstitutionStammdatenKitaBruennen());
		Testfall01_WaeltiDagmar testfall_1GS = new Testfall01_WaeltiDagmar(gesuchsperiode, institutionStammdatenList);
		testfall_1GS.createFall();
		testfall_1GS.createGesuch(LocalDate.of(2016, Month.DECEMBER, 12));
		gesuch_1GS = testfall_1GS.fillInGesuch();
	}

	@Test
	void testIsSame() {
		List<Betreuung> betreuungen = gesuch_1GS.extractAllBetreuungen();
		Betreuungsmitteilung betreuungsmitteilung = createBetreuungMitteilung();
		Assertions.assertTrue(handler.isSame(betreuungsmitteilung, betreuungen.get(0)));
		//then with different Betreuung
		Assertions.assertFalse(handler.isSame(betreuungsmitteilung, betreuungen.get(1)));
	}

	@Test
	void mapZeitabschnittToAbstractMahlzeitenPensum() {
		Betreuung betreuung = betreuungWithSingleContainer();
		BetreuungEventDTO betreuungEventDTO = createBetreuungEventDTO();
		ZeitabschnittDTO dto = betreuungEventDTO.getZeitabschnitte().get(0);

		BetreuungsmitteilungPensum actual = handler.mapZeitabschnitt(new BetreuungsmitteilungPensum(), dto, betreuung);

		assertThat(actual, matches(dto));
	}

	@Nonnull
	private IsPojo<AbstractMahlzeitenPensum> matches(@Nonnull ZeitabschnittDTO z) {
		return pojo(AbstractMahlzeitenPensum.class)
			.where(
				AbstractMahlzeitenPensum::getMonatlicheBetreuungskosten,
				comparesEqualTo(z.getBetreuungskosten()))
			.where(
				AbstractMahlzeitenPensum::getPensum,
				comparesEqualTo(z.getBetreuungspensum()))
			.where(
				AbstractMahlzeitenPensum::getMonatlicheHauptmahlzeiten,
				comparesEqualTo(coalesce(z.getAnzahlHauptmahlzeiten(), BigDecimal.ZERO)))
			.where(
				AbstractMahlzeitenPensum::getMonatlicheNebenmahlzeiten,
				comparesEqualTo(coalesce(z.getAnzahlNebenmahlzeiten(), BigDecimal.ZERO)))
			.where(
				AbstractMahlzeitenPensum::getTarifProHauptmahlzeit,
				comparesEqualTo(coalesce(z.getTarifProHauptmahlzeiten(), BigDecimal.ZERO)))
			.where(
				AbstractMahlzeitenPensum::getTarifProNebenmahlzeit,
				comparesEqualTo(coalesce(z.getTarifProNebenmahlzeiten(), BigDecimal.ZERO)))
			.where(
				AbstractMahlzeitenPensum::getGueltigkeit, equalTo(new DateRange(z.getVon(), z.getBis())));
	}

	@Test
	void testMapWrongZeitabschnitt() {
		Betreuung betreuung = betreuungWithSingleContainer();
		BetreuungEventDTO betreuungEventDTO = createBetreuungEventDTO();
		ZeitabschnittDTO dto = betreuungEventDTO.getZeitabschnitte().get(0);
		dto.setPensumUnit(Zeiteinheit.HOURS);

		BetreuungsmitteilungPensum betreuungsmitteilungPensum =
			handler.mapZeitabschnitt(new BetreuungsmitteilungPensum(), dto, betreuung);

		assertThat(betreuungsmitteilungPensum, is(nullValue()));
	}

	@Nested
	class MapZeitabschnitteToImportTest {

		@Test
		void testMapZeitAbschnitteToImportGoLive() {
			Betreuung betreuung = betreuungWithSingleContainer(LocalDate.of(2020, 8, 1), LocalDate.of(2021, 5, 31));
			BetreuungEventDTO betreuungEventDTO = createBetreuungEventDTO();
			ZeitabschnittDTO dto = createZeitabschnittDTO(LocalDate.of(2020, 11, 1), LocalDate.of(2021, 6, 30));

			betreuungEventDTO.setZeitabschnitte(Collections.singletonList(dto));

			List<ZeitabschnittDTO> actual =
				handler.mapZeitabschnitteToImport(betreuungEventDTO, betreuung, Constants.DEFAULT_GUELTIGKEIT);

			assertThat(actual, contains(
				gueltigkeit(LocalDate.of(2020, 8, 1), LocalDate.of(2020, 12, 31)),
				gueltigkeit(LocalDate.of(2021, 1, 1), LocalDate.of(2021, 6, 30))
			));
		}

		@Test
		void testMapZeitAbschnitteToImport() {
			BetreuungEventDTO betreuungEventDTO = createBetreuungEventDTO(
				//Zeitabschnitt bevor von soll nicht genommen werden
				createZeitabschnittDTO(LocalDate.of(2020, 11, 1), LocalDate.of(2020, 11, 30)),
				//uberlapender Zeitabschnitt von
				createZeitabschnittDTO(LocalDate.of(2020, 12, 1), LocalDate.of(2021, 1, 31)),
				//in der mitte Zeitabschnitt
				createZeitabschnittDTO(LocalDate.of(2021, 2, 1), LocalDate.of(2021, 3, 31)),
				//uberlapender Zeitabschnitt bis
				createZeitabschnittDTO(LocalDate.of(2021, 4, 1), LocalDate.of(2021, 6, 30)),
				//Zeitabschnitt nach bis soll nicht genommen werden
				createZeitabschnittDTO(LocalDate.of(2021, 7, 1), LocalDate.of(2021, 7, 31))
			);

			Betreuung betreuung = betreuungWithSingleContainer(LocalDate.of(2020, 8, 1), LocalDate.of(2021, 1, 31));

			DateRange gueltigkeit = new DateRange(LocalDate.of(2021, 1, 1), LocalDate.of(2021, 5, 1));

			List<ZeitabschnittDTO> actual =
				handler.mapZeitabschnitteToImport(betreuungEventDTO, betreuung, gueltigkeit);

			assertThat(actual, contains(
				gueltigkeit(LocalDate.of(2020, 8, 1), LocalDate.of(2020, 12, 31)),
				gueltigkeit(LocalDate.of(2021, 1, 1), LocalDate.of(2021, 1, 31)),
				gueltigkeit(LocalDate.of(2021, 2, 1), LocalDate.of(2021, 3, 31)),
				gueltigkeit(LocalDate.of(2021, 4, 1), LocalDate.of(2021, 5, 1))
			));
		}

		@Test
		void testMapZeitAbschnitteToImportSplit() {
			BetreuungEventDTO betreuungEventDTO = createBetreuungEventDTO(
				createZeitabschnittDTO(LocalDate.of(2021, 1, 1), LocalDate.of(2021, 5, 1))
			);

			Betreuung betreuung = betreuungWithSingleContainer(LocalDate.of(2020, 8, 1), LocalDate.of(2021, 7, 31));

			DateRange gueltigkeit = new DateRange(LocalDate.of(2021, 1, 1), LocalDate.of(2021, 5, 1));

			List<ZeitabschnittDTO> actual =
				handler.mapZeitabschnitteToImport(betreuungEventDTO, betreuung, gueltigkeit);

			assertThat(actual, contains(
				gueltigkeit(LocalDate.of(2020, 8, 1), LocalDate.of(2020, 12, 31)),
				gueltigkeit(LocalDate.of(2021, 5, 2), LocalDate.of(2021, 7, 31)),
				gueltigkeit(LocalDate.of(2021, 1, 1), LocalDate.of(2021, 5, 1))
			));
		}

		// der Test macht gar keinen Sinn (da DTO Zeitabschnitte vor Periode). Das ist invalid input -> sollte komplett
		// ignoriert werden
		@Test
		void testMapZeitAbschnitteToImportSplitBeforeGueltigkeit() {
			BetreuungEventDTO betreuungEventDTO = createBetreuungEventDTO(
				createZeitabschnittDTO(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 5, 1))
			);

			Betreuung betreuung = betreuungWithSingleContainer(LocalDate.of(2020, 8, 1), Constants.END_OF_TIME);

			DateRange gueltigkeit = new DateRange(LocalDate.of(2021, 1, 1), LocalDate.of(2021, 5, 1));

			List<ZeitabschnittDTO> actual =
				handler.mapZeitabschnitteToImport(betreuungEventDTO, betreuung, gueltigkeit);

			assertThat(actual, contains(
				gueltigkeit(LocalDate.of(2020, 8, 1), LocalDate.of(2020, 12, 31)),
				gueltigkeit(LocalDate.of(2021, 5, 2), Constants.END_OF_TIME)
			));
		}

		// der Test macht gar keinen Sinn (da DTO Zeitabschnitte vor Periode). Das ist invalid input -> sollte komplett
		// ignoriert werden
		@Test
		void testMapZeitAbschnitteToImportShouldSplitIfBetStartsBeforeGueltigkeitAndEndsOnGueltigkeitEnd() {
			BetreuungEventDTO betreuungEventDTO = createBetreuungEventDTO(
				createZeitabschnittDTO(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 5, 1))
			);

			Betreuung betreuung = betreuungWithSingleContainer(LocalDate.of(2020, 8, 1), LocalDate.of(2022, 12, 31));

			DateRange gueltigkeit = new DateRange(LocalDate.of(2021, 1, 1), LocalDate.of(2022, 12, 31));

			List<ZeitabschnittDTO> actual =
				handler.mapZeitabschnitteToImport(betreuungEventDTO, betreuung, gueltigkeit);

			assertThat(actual, contains(
				gueltigkeit(LocalDate.of(2020, 8, 1), LocalDate.of(2020, 12, 31))
			));
		}

		// der Test macht gar keinen Sinn (da DTO Zeitabschnitte vor Periode). Das ist invalid input -> sollte komplett
		// ignoriert werden
		@Test
		void testMapZeitAbschnitteToImportBisEndOfTimeSplitGoLive() {
			BetreuungEventDTO betreuungEventDTO = createBetreuungEventDTO(
				createZeitabschnittDTO(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 5, 1))
			);

			Betreuung betreuung = betreuungWithSingleContainer(LocalDate.of(2020, 8, 1), Constants.END_OF_TIME);

			DateRange gueltigkeit = new DateRange(LocalDate.of(2021, 1, 1), Constants.END_OF_TIME);

			List<ZeitabschnittDTO> actual =
				handler.mapZeitabschnitteToImport(betreuungEventDTO, betreuung, gueltigkeit);

			assertThat(actual, contains(
				gueltigkeit(LocalDate.of(2020, 8, 1), LocalDate.of(2020, 12, 31))
			));
		}

		// Der Fall darf nicht zu einer Mutationsmeldung führen
		@Test
		void rejectBetreuungEventWhenNotInClientGueltigkeitDoesNotIntersectBetreuungsPeriod() {
			BetreuungEventDTO betreuungEventDTO = createBetreuungEventDTO(
				createZeitabschnittDTO(LocalDate.of(2021, 6, 1), LocalDate.of(2021, 6, 30))
			);

			// full period 2020/2021
			Betreuung betreuung = betreuungWithSingleContainer(LocalDate.of(2020, 8, 1), LocalDate.of(2021, 7, 31));

			// note: client permission is after Betreuung gueltigkeit, even outside the valid period 2020/2021!
			DateRange gueltigkeit = new DateRange(LocalDate.of(2021, 8, 1), LocalDate.of(2022, 7, 31));

			List<ZeitabschnittDTO> actual =
				handler.mapZeitabschnitteToImport(betreuungEventDTO, betreuung, gueltigkeit);

			assertThat(actual, hasSize(betreuung.getBetreuungspensumContainers().size()));
		}

		@Nonnull
		private Matcher<ZeitabschnittDTO> gueltigkeit(@Nonnull LocalDate von, @Nonnull LocalDate bis) {
			return pojo(ZeitabschnittDTO.class)
				.where(ZeitabschnittDTO::getVon, is(von))
				.where(ZeitabschnittDTO::getBis, is(bis));
		}
	}

	@Nested
	class SetZeitabschnitteTest {

		@Test
		void splitDtoZeitabschnitteWithClientGueltigkeit() {
			BetreuungEventDTO betreuungEventDTO = createBetreuungEventDTO(
				//Zeitabschnitt bevor von soll nicht genommen werden
				createZeitabschnittDTO(LocalDate.of(2020, 11, 1), LocalDate.of(2020, 11, 30)),
				//uberlapende Zeitabschnitt von
				createZeitabschnittDTO(LocalDate.of(2020, 12, 1), LocalDate.of(2021, 1, 31)),
				//in der mitte Zeitabschnitt
				createZeitabschnittDTO(LocalDate.of(2021, 2, 1), LocalDate.of(2021, 3, 31)),
				///uberlapende Zeitabschnitt bis
				createZeitabschnittDTO(LocalDate.of(2021, 4, 1), LocalDate.of(2021, 6, 30)),
				///Zeitabschnitt nach bis soll nicht genommen werden
				createZeitabschnittDTO(LocalDate.of(2021, 7, 1), LocalDate.of(2021, 7, 31))
			);

			Betreuung betreuung = betreuungWithSingleContainer(LocalDate.of(2020, 8, 1), LocalDate.of(2021, 3, 31));

			DateRange gueltigkeit = new DateRange(LocalDate.of(2021, 1, 1), LocalDate.of(2021, 5, 1));

			PlatzbestaetigungProcessingContext platzbestaetigungProcessingContext =
				new PlatzbestaetigungProcessingContext(betreuung, betreuungEventDTO);

			handler.setZeitabschnitte(platzbestaetigungProcessingContext, true, gueltigkeit);

			List<BetreuungspensumContainer> result = getSortedContainers(platzbestaetigungProcessingContext);

			assertThat(result, contains(
				container(LocalDate.of(2020, 8, 1), LocalDate.of(2020, 12, 31)),
				container(LocalDate.of(2021, 1, 1), LocalDate.of(2021, 1, 31)),
				container(LocalDate.of(2021, 2, 1), LocalDate.of(2021, 3, 31)),
				container(LocalDate.of(2021, 4, 1), LocalDate.of(2021, 5, 1))
			));
		}

		@Test
		void testSetZeitabschnitteSplit() {
			BetreuungEventDTO betreuungEventDTO = createBetreuungEventDTO(
				//in der mitte Zeitabschnitt
				createZeitabschnittDTO(LocalDate.of(2021, 1, 1), LocalDate.of(2021, 5, 1))
			);

			Betreuung betreuung = betreuungWithSingleContainer(LocalDate.of(2020, 8, 1), LocalDate.of(2021, 7, 31));

			DateRange gueltigkeit = new DateRange(LocalDate.of(2021, 1, 1), LocalDate.of(2021, 5, 1));

			PlatzbestaetigungProcessingContext platzbestaetigungProcessingContext =
				new PlatzbestaetigungProcessingContext(betreuung, betreuungEventDTO);

			handler.setZeitabschnitte(platzbestaetigungProcessingContext, true, gueltigkeit);

			List<BetreuungspensumContainer> result = getSortedContainers(platzbestaetigungProcessingContext);

			assertThat(result, contains(
				container(LocalDate.of(2020, 8, 1), LocalDate.of(2020, 12, 31)),
				container(LocalDate.of(2021, 1, 1), LocalDate.of(2021, 5, 1)),
				container(LocalDate.of(2021, 5, 2), LocalDate.of(2021, 7, 31))
			));
		}

		@Test
		void testSetZeitabschnitteSplitWithEndOfTimeBetreuung() {
			BetreuungEventDTO betreuungEventDTO = createBetreuungEventDTO(
				//in der mitte Zeitabschnitt
				createZeitabschnittDTO(LocalDate.of(2021, 1, 1), LocalDate.of(2021, 5, 1))
			);

			Betreuung betreuung = betreuungWithSingleContainer(LocalDate.of(2020, 8, 1), Constants.END_OF_TIME);

			DateRange gueltigkeit = new DateRange(LocalDate.of(2021, 1, 1), LocalDate.of(2021, 5, 1));

			PlatzbestaetigungProcessingContext platzbestaetigungProcessingContext =
				new PlatzbestaetigungProcessingContext(betreuung, betreuungEventDTO);

			handler.setZeitabschnitte(platzbestaetigungProcessingContext, true, gueltigkeit);

			List<BetreuungspensumContainer> result = getSortedContainers(platzbestaetigungProcessingContext);

			assertThat(result, contains(
				container(LocalDate.of(2020, 8, 1), LocalDate.of(2020, 12, 31)),
				container(LocalDate.of(2021, 1, 1), LocalDate.of(2021, 5, 1)),
				container(LocalDate.of(2021, 5, 2), Constants.END_OF_TIME)
			));
		}

		@Test
		void testSetZeitabschnitteSplitWithBetreuungTillClientGueltigAb() {
			BetreuungEventDTO betreuungEventDTO = createBetreuungEventDTO(
				//in der mitte Zeitabschnitt
				createZeitabschnittDTO(LocalDate.of(2021, 1, 1), LocalDate.of(2021, 5, 1))
			);

			Betreuung betreuung = betreuungWithSingleContainer(LocalDate.of(2020, 8, 1), LocalDate.of(2021, 1, 1));

			DateRange gueltigkeit = new DateRange(LocalDate.of(2021, 1, 1), LocalDate.of(2021, 5, 1));

			PlatzbestaetigungProcessingContext platzbestaetigungProcessingContext =
				new PlatzbestaetigungProcessingContext(betreuung, betreuungEventDTO);

			handler.setZeitabschnitte(platzbestaetigungProcessingContext, true, gueltigkeit);

			List<BetreuungspensumContainer> result = getSortedContainers(platzbestaetigungProcessingContext);

			assertThat(result, contains(
				container(LocalDate.of(2020, 8, 1), LocalDate.of(2020, 12, 31)),
				container(LocalDate.of(2021, 1, 1), LocalDate.of(2021, 5, 1))
			));
		}

		@Nonnull
		private List<BetreuungspensumContainer> getSortedContainers(
			@Nonnull PlatzbestaetigungProcessingContext platzbestaetigungProcessingContext) {

			return platzbestaetigungProcessingContext.getBetreuung()
				.getBetreuungspensumContainers()
				.stream()
				.sorted()
				.collect(Collectors.toList());
		}

		@Nonnull
		private Matcher<BetreuungspensumContainer> container(@Nonnull LocalDate von, @Nonnull LocalDate bis) {
			return pojo(BetreuungspensumContainer.class)
				.where(BetreuungspensumContainer::getBetreuungspensumJA, pojo(Betreuungspensum.class)
					.where(Betreuungspensum::getGueltigkeit, gueltigkeit(von, bis)));
		}

		@Nonnull
		private Matcher<DateRange> gueltigkeit(@Nonnull LocalDate von, @Nonnull LocalDate bis) {
			return pojo(DateRange.class)
				.where(DateRange::getGueltigAb, is(von))
				.where(DateRange::getGueltigBis, is(bis));
		}
	}

	/**
	 * Eine BetreuungEventDTO mit genau einem Zeitabschnitt
	 */
	@Nonnull
	private BetreuungEventDTO createBetreuungEventDTO() {
		return createBetreuungEventDTO(createZeitabschnittDTO());
	}

	@SuppressWarnings("OverloadedVarargsMethod")
	@Nonnull
	private BetreuungEventDTO createBetreuungEventDTO(@Nonnull ZeitabschnittDTO... zeitabschnitte) {
		BetreuungEventDTO betreuungEventDTO = new BetreuungEventDTO();
		betreuungEventDTO.setRefnr("20.007305.002.1.3");
		betreuungEventDTO.setInstitutionId("1234-5678-9101-1121");
		betreuungEventDTO.setZeitabschnitte(Arrays.asList(zeitabschnitte));

		return betreuungEventDTO;
	}

	@Nonnull
	private ZeitabschnittDTO createZeitabschnittDTO() {
		return createZeitabschnittDTO(
			gesuchsperiode.getGueltigkeit().getGueltigAb(),
			gesuchsperiode.getGueltigkeit().getGueltigBis().withDayOfYear(31)
		);
	}

	@Nonnull
	private ZeitabschnittDTO createZeitabschnittDTO(@Nonnull LocalDate von, @Nonnull LocalDate bis) {
		return ZeitabschnittDTO.newBuilder()
			.setBetreuungskosten(MathUtil.DEFAULT.from(2000))
			.setBetreuungspensum(new BigDecimal(80))
			.setPensumUnit(Zeiteinheit.PERCENTAGE)
			.setVon(von)
			.setBis(bis)
			.build();
	}

	@Nonnull
	private Betreuungsmitteilung createBetreuungMitteilung() {
		Betreuungsmitteilung betreuungsmitteilung = new Betreuungsmitteilung();
		BetreuungsmitteilungPensum betreuungsmitteilungPensum = new BetreuungsmitteilungPensum();
		betreuungsmitteilungPensum.setMonatlicheBetreuungskosten(MathUtil.DEFAULT.from(2000));
		betreuungsmitteilungPensum.setPensum(new BigDecimal(80));
		betreuungsmitteilungPensum.setUnitForDisplay(PensumUnits.PERCENTAGE);
		DateRange dateRange = new DateRange(gesuchsperiode.getGueltigkeit());
		dateRange.setGueltigBis(gesuchsperiode.getGueltigkeit().getGueltigBis().withDayOfYear(31));
		betreuungsmitteilungPensum.setGueltigkeit(dateRange);
		Set<BetreuungsmitteilungPensum> betreuungsmitteilungPensumSet = new HashSet<>();
		betreuungsmitteilungPensumSet.add(betreuungsmitteilungPensum);
		betreuungsmitteilung.setBetreuungspensen(betreuungsmitteilungPensumSet);

		return betreuungsmitteilung;
	}

	@Nonnull
	private Betreuungspensum getSingleContainer(@Nonnull Betreuung betreuung) {
		checkArgument(
			betreuung.getBetreuungspensumContainers().size() == 1,
			"Broken test setup: expected 1 container in %s",
			betreuung);

		return betreuung.getBetreuungspensumContainers().iterator().next().getBetreuungspensumJA();
	}

	@Nonnull
	private Betreuung betreuungWithSingleContainer() {
		return betreuungWithSingleContainer(LocalDate.of(2020, 8, 1), Constants.END_OF_TIME);
	}

	@Nonnull
	private Betreuung betreuungWithSingleContainer(@Nonnull LocalDate von, @Nonnull LocalDate bis) {
		Betreuung betreuung = requireNonNull(gesuch_1GS.getFirstBetreuung());
		Betreuungspensum betreuungspensum = getSingleContainer(betreuung);
		betreuungspensum.getGueltigkeit().setGueltigAb(von);
		betreuungspensum.getGueltigkeit().setGueltigBis(bis);

		return betreuung;
	}
}
